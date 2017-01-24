package wiro.server.akkaHttp

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.headers.HttpChallenges
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsRejected
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import de.heikoseeberger.akkahttpcirce.CirceSupport._

import wiro.models._
import FailSupport._

import scala.language.implicitConversions

import scala.util.Try
import scala.concurrent.Future

import io.circe._
import io.circe.syntax._

object RouteGenerators {
  def exceptionHandler = ExceptionHandler {
    case f@FailException(_) => complete(f.response)
  } 

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
  trait RouteGenerator[A] extends RPCController {
    def routes: autowire.Core.Router[Json]
    //complete path of the trait implementation, required by autowire to locate the method
    def tp: Seq[String]
    def buildRoute: Route = handleExceptions(exceptionHandler) {
      commands ~ queries
    }

    //Generates GET requests
    private[this] def queries: Route = {
      //Any operation can by specified by the user here
      //Autowire `routes` macro takes care of checking the operation is allowed
      (get & pathPrefix(path / Segment)) { operation =>
        parameterMap { params =>
          withToken { token =>
            val tryUnwrapRequest = scala.util.Try(routes(
              autowire.Core.Request(
                path = tp :+ operation,
                args = addQueryToParams(addTokenToParams(
                  //`Map[String, String]` is feeding autowire macro
                  //That's how it works even with types other than `String`
                  params.map { case (k, v) =>
                    (k -> Json.fromString(v))
                  },
                  token
                ))
              ))
            )

            completeUnwrappedRequest(tryUnwrapRequest)
          }

        }
      }
    }

    //Generates POST requests
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
              args = addCommandToParams(addTokenToParams(rpcRequest.args, token))
            )

            val tryUnwrapRequest = scala.util.Try(routes(
              autowire.Core.Request(
                path = rpcRequestWithToken.path,
                args = rpcRequestWithToken.args
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
      case e: autowire.Error.InvalidInput =>
        complete(HttpResponse(
          status = StatusCodes.UnprocessableEntity,
          entity = "Unprocessable entity"
        ))
      case _: scala.MatchError =>
        complete(HttpResponse(
          status = StatusCodes.MethodNotAllowed,
          entity = "Method not found"
        ))
      case _: Exception =>
        complete(HttpResponse(
          status = StatusCodes.InternalServerError,
          entity = "Internal Error"
        ))
      }
  }

  private[this] def handleAutowireInputErrors(
    xs: autowire.Error.Param
  ): StandardRoute = xs match {
    case autowire.Error.Param.Missing(param) => param match {
      case "token" => reject(AuthenticationFailedRejection(
        cause = CredentialsRejected,
        challenge = HttpChallenges.basic("api")
      ))
      case "actionQuery" => complete(HttpResponse(
        status = StatusCodes.MethodNotAllowed,
        entity = s"Method not allowed"
      ))
      case "actionCommand" => complete(HttpResponse(
        status = StatusCodes.MethodNotAllowed,
        entity = s"Method not allowed"
      ))
      case _ => complete(HttpResponse(
        status = StatusCodes.UnprocessableEntity,
        entity = s"Missing parameter $param from input"
      ))
    }
    case autowire.Error.Param.Invalid(param, ex) =>
      complete(HttpResponse(
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

  private[this] def addCommandToParams(
    params: Map[String, Json]
  ): Map[String, Json] = params + ("actionCommand" -> Json.fromString(""))

  private[this] def addQueryToParams(
    params: Map[String, Json]
  ): Map[String, Json] = params + ("actionQuery" -> Json.fromString(""))

  object BoxingSupport {
    //Pattern to use existentially quantified types in scala
    case class GeneratorBox[A: RouteGenerator](a: A) {
      def routify = implicitly[RouteGenerator[A]].buildRoute
    }

    implicit def boxer[A: RouteGenerator](a: A) =
      new GeneratorBox[A](a)
  }
}
