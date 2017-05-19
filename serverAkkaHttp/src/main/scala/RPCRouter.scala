package wiro.server.akkaHttp

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Route, Directive1, ExceptionHandler }
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
  trait RouteGenerator[A] extends RPCController with PathMacro with MetaDataMacro {
    def routes: autowire.Core.Router[Json]
    //complete path of the trait implementation, required by autowire to locate the method
    def tp: Seq[String]
    def methodsMetaData: Map[String, MethodMetaData]
    def buildRoute: Route = handleExceptions(exceptionHandler) {
      pathPrefix(path) {
        methodsMetaData map { case (k, v) =>
          v.operationType match {
            case _: OperationType.Command => command(k, v)
            case _: OperationType.Query => query(k, v)
          }
        } reduce (_ ~ _)
      }
    }

    private[this] def operationName(operationFullName: String, methodMetaData: MethodMetaData): String =
      methodMetaData.operationType.name match {
        case Some(n) => n
        case None => operationFullName.split("""\.""").last
      }

    private[this] def query(operationFullName: String, methodMetaData: MethodMetaData): Route = {
      (get & pathPrefix(operationName(operationFullName, methodMetaData)) & parameterMap) { params =>
        requestToken { token =>
          val appliedRequest = Try(routes(autowire.Core.Request(
            path = operationFullName.split("""\."""),
            args = queryArgs(params, token)
          )))

          appliedRequest match {
            case Success(res) => complete(res)
            case Failure(f) => handleUnwrapErrors(f)
          }
        }
      }
    }

    //Generates POST requests
    private[this] def command(operationFullName: String, methodMetaData: MethodMetaData): Route = {
      (post & pathPrefix(operationName(operationFullName, methodMetaData)) & entity(as[Json])) { request =>
        requestToken { token =>
          val appliedRequest = Try(routes(autowire.Core.Request(
            path = operationFullName.split("""\."""),
            args = commandArgs(request, token)
          )))

          appliedRequest match {
            case Success(res) => complete(res)
            case Failure(f) => handleUnwrapErrors(f)
          }
        }
      }
    }
  }

  def commandArgs(request: Json, token: Option[String]): Map[String, Json] =
    request.as[Map[String, Json]].right.get.withToken(token)

  def queryArgs(params: Map[String, String], token: Option[String]): Map[String, Json] =
    params.mapValues(_.asJson).withToken(token)

  implicit class PimpMyMap(m: Map[String, Json]) {
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
