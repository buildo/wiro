package wiro.server.akkaHttp

import akka.http.scaladsl.server.Directives._

import akka.http.scaladsl.model.headers.HttpChallenges
import akka.http.scaladsl.server.{ Route, Directive1, AuthenticationFailedRejection, StandardRoute }
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsRejected
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.generic.auto._
import io.circe.Json

import wiro.models.{ RpcRequest, WiroRequest }

import scala.language.implicitConversions

import scala.util.Try
import scala.concurrent.Future

object routeGenerators {
  import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
  import scala.concurrent.ExecutionContext.Implicits.global

  import io.circe.syntax._
  import io.circe.jawn.decode

  case class GeneratorBox[T: RouteGenerator](t: T) {
    def routify = implicitly[RouteGenerator[T]].buildRoute
  }

  implicit def boxer[T: RouteGenerator](t: T) = new GeneratorBox(t)

  private[this] def withToken: Directive1[Option[String]] = {
    val authDirective: Directive1[Option[String]] = headerValueByName("Authorization")
      .flatMap { header =>
        val TokenPattern = "Token token=(.+)".r
        header match {
          case TokenPattern(token) => provide(Some(token))
          case _                   => provide(None)
        }
      }
    authDirective.recover { x => provide(None) }
  }

  //TODO Don't necessarily need the type here, it can be simplified (no boxing)
  trait RouteGenerator[T] extends RPCController {
    def routes: autowire.Core.Router[Json]
    def tp: Seq[String]
    def buildRoute: Route = commands ~ queries

    private[this] def queries: Route = {
      (get & pathPrefix(path / Segment)) { method =>
        parameterMap { params =>
          val path = tp :+ method

          withToken { token =>
            val tryUnwrapRequest = scala.util.Try(routes(
              autowire.Core.Request(
                path,
                addTokenToParams(
                  params.map { case (k, v) =>
                    (k -> Json.fromString(v))
                  },
                  token
                )
              ))
            )

            completeUnwrappedRequest(tryUnwrapRequest)
          }

        }
      }
    }

    private[this] def commands: Route = {
      (post & pathPrefix(path / Segment)) { method =>
        entity(as[Json]) { request =>
          val rpcRequest = RpcRequest(
            path = tp :+ method,
            //TODO handle circe error
            args = request.as[Map[String, Json]].right.get
          )

          withToken { token =>
            val rpcRequestWithToken = rpcRequest.copy(
              args = addTokenToParams(rpcRequest.args, token)
            )

            val tryUnwrapRequest = scala.util.Try(routes(
              autowire.Core.Request(
                rpcRequestWithToken.path,
                rpcRequestWithToken.args
              ))
            )

            completeUnwrappedRequest(tryUnwrapRequest)
          }
        }
      }
    }

    private[this] def completeUnwrappedRequest(
      tryUnwrapRequest: Try[Future[Json]]
    ): StandardRoute = {
      tryUnwrapRequest match {
        case scala.util.Success(res) => complete(res)
        case scala.util.Failure(f) => handleUnwrapErrors(f)
      }
    }

  private[this] def handleUnwrapErrors(
    throwable: Throwable
  ) = throwable match {
    case autowire.Error.InvalidInput(xs) =>
      handleAutowireInputErrors(xs)
    case _: scala.MatchError =>
      complete(HttpResponse(
        status = StatusCodes.MethodNotAllowed,
        entity = "Method not found"
      ))
    case e: Exception =>
      //TODO find nicer way for this
      e.printStackTrace
      complete(HttpResponse(
        status = StatusCodes.InternalServerError,
        entity = "Internal Error"
      ))
    }
  }

  private[this] def handleAutowireInputErrors(
    xs: autowire.Error.Param
  ): StandardRoute = xs match {
    case autowire.Error.Param.Missing(param) =>
      complete(HttpResponse(
        status = StatusCodes.UnprocessableEntity,
        entity = s"Missing parameter $param from input"
      ))
    case autowire.Error.Param.Invalid(param, ex) =>
      if (param == "token") reject(AuthenticationFailedRejection(
        cause = CredentialsRejected,
        challenge = HttpChallenges.basic("api")
      ))
      else complete(HttpResponse(
        status = StatusCodes.UnprocessableEntity,
        entity = s"Missing parameter $param from input"
      ))
  }

  private[this] def addTokenToParams(
    params: Map[String, Json],
    token: Option[String]
  ): Map[String, Json] = token match {
    case Some(t) => params + ("token" -> Json.fromString(t))
    case None => params
  }
}
