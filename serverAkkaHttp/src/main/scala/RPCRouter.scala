package wiro.server.akkaHttp
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import de.heikoseeberger.akkahttpcirce.CirceSupport._

import wiro.models._
import FailSupport._
import AutowireErrorSupport._

import scala.language.implicitConversions

import scala.util.{ Try, Success, Failure }
import scala.concurrent.Future

import io.circe._
import io.circe.syntax._

object RouteGenerators {
  def exceptionHandler = ExceptionHandler {
    case f@FailException(_) => complete(f.response)
  } 

  private[this] def requestToken: Directive1[Option[String]] = {
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
          requestToken { token =>
            val unwrappedRequest = Try(routes(autowire.Core.Request(
              path = tp :+ operation,
              args = getQueryArgs(params, token)
            )))

            unwrappedRequest match {
              case Success(res) => complete(res)
              case Failure(f) => handleUnwrapErrors(f)
            }
          }
        }
      }
    }

    //Generates POST requests
    private[this] def commands: Route = {
      (post & pathPrefix(path / Segment)) { method =>
        entity(as[Json]) { request =>
          requestToken { token =>
            val unwrappedRequest = Try(routes(autowire.Core.Request(
              path = tp :+ method,
              args = getCommandArgs(request, token)
            )))

            unwrappedRequest match {
              case Success(res) => complete(res)
              case Failure(f) => handleUnwrapErrors(f)
            }
          }
        }
      }
    }
  }

  def getCommandArgs(request: Json, token: Option[String]): Map[String, Json] =
    request.as[Map[String, Json]].right.get
      .withToken(token)
      .withCommand

  def getQueryArgs(params: Map[String, String], token: Option[String]): Map[String, Json] = {
    params.map { case (k, v) => (k -> v.asJson) }
      .withToken(token)
      .withQuery
  }

  implicit class PimpMyMap(m: Map[String, Json]) {
    def withCommand: Map[String, Json] =
      m + ("actionCommand" -> Json.fromString(""))

    def withQuery: Map[String, Json] =
      m + ("actionQuery" -> Json.fromString(""))

    def withToken(token: Option[String]): Map[String, Json] = token match {
      case Some(t) => m + ("token" -> Json.fromString(t))
      case None => m
    }
  }

  object BoxingSupport {
    //Pattern to use existentially quantified types in scala
    case class GeneratorBox[A: RouteGenerator](a: A) {
      def routify = implicitly[RouteGenerator[A]].buildRoute
    }

    implicit def boxer[A: RouteGenerator](a: A) =
      new GeneratorBox[A](a)
  }
}
