package wiro.server.akkaHttp

import akka.http.scaladsl.server.Directives._

import upickle._ 
import akka.http.scaladsl.model.headers.HttpChallenges
import akka.http.scaladsl.server.{ Route, Directive1, AuthenticationFailedRejection, StandardRoute }
import akka.http.scaladsl.server.AuthenticationFailedRejection.{ CredentialsMissing, CredentialsRejected }
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.generic.auto._

import wiro.models.{ RpcRequest, WiroRequest }

package object routeGenerators {
  import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
  import scala.concurrent.ExecutionContext.Implicits.global

  private[this] def withToken: Directive1[Option[String]] = {
    val authDirective: Directive1[Option[String]] = headerValueByName("Authorization").flatMap { header =>
      val TokenPattern = "Token token=(.+)".r
      header match {
        case TokenPattern(token) => provide(Some(token))
        case _                   => provide(None)
      }
    }
    authDirective.recover { x => provide(None) }
  }

  //Don't necessarily need the type here, it can be simplified
  trait RouteGenerator[T] extends RPCController {
    def routes: autowire.Core.Router[upickle.Js.Value]
    def tp: Seq[String]
    def buildRoute: Route = {
      (post & pathPrefix(path / Segment)) { method =>
        entity(as[WiroRequest]) { request =>
          val rpcRequest = RpcRequest(
            path = tp :+ method,
            args = upickle.json
              .read(request.args)
              .asInstanceOf[Js.Obj]
              .value.toMap
          )

          withToken { token =>
            val rpcRequestWithToken = addTokenToRpcRequest(rpcRequest, token)

            val tryUnwrapRequest = scala.util.Try(routes(
              autowire.Core.Request(
                rpcRequestWithToken.path,
                rpcRequestWithToken.args
              )).map(upickle.json.write(_))
            )

            handleUnwrapErrors(tryUnwrapRequest)
          }
        }
      }
    }

    private[this] def handleUnwrapErrors(tryUnwrapRequest: scala.util.Try[scala.concurrent.Future[String]]) = {
      lazy val challenge = HttpChallenges.basic("api")

      tryUnwrapRequest match {
        case scala.util.Success(res) => complete(res)
        case scala.util.Failure(f) => f match {
          case autowire.Error.InvalidInput(xs) =>
            xs match {
              case autowire.Error.Param.Missing(param) =>
                complete(HttpResponse(StatusCodes.UnprocessableEntity, entity = s"Missing parameter $param from input"))
              case autowire.Error.Param.Invalid(param, ex) =>
                if (param == "token") reject(AuthenticationFailedRejection(CredentialsRejected, challenge))
                else complete(HttpResponse(StatusCodes.UnprocessableEntity, entity = s"Missing parameter $param from input"))
            }
          case _: scala.MatchError =>
            complete(HttpResponse(StatusCodes.NotFound, entity = "Method not found"))
          case e: Exception =>
            //TODO find nicer way for this
            e.printStackTrace
            complete(HttpResponse(StatusCodes.InternalServerError, entity = "Internal Error"))
        }
      }
    }

    private[this] def addTokenToRpcRequest(rpcRequest: wiro.models.RpcRequest, token: Option[String]): wiro.models.RpcRequest = {
      val rpcRequestWithToken = token match {
        case Some(t) => rpcRequest.copy(args = rpcRequest.args + ("token" -> Js.Str(t)))
        case None => rpcRequest
      }

      rpcRequestWithToken
    }
  }

  case class GeneratorBox[T: RouteGenerator](t: T) {
    def routify = implicitly[RouteGenerator[T]].buildRoute
  }

  implicit def boxer[T: RouteGenerator](t: T) = new GeneratorBox(t)
}
