package wiro
package server.akkaHttp

import AutowireErrorSupport._

import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive1, ExceptionHandler, Route }

import cats.syntax.traverse._
import cats.instances.map._
import cats.instances.either._
import cats.syntax.either._

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._

import FailSupport._

import io.circe.{ Json, ParsingFailure }
import io.circe.parser._

import scala.language.implicitConversions

import scala.util.{ Try, Success, Failure }
import scala.concurrent.Future

trait Router extends RPCServer with PathMacro with MetaDataMacro {
  def tp: Seq[String]
  def methodsMetaData: Map[String, MethodMetaData]
  def routes: autowire.Core.Router[Json]
  def path: String = tp.last

  def buildRoute: Route = handleExceptions(exceptionHandler) {
    pathPrefix(path) {
      methodsMetaData.map {
        case (k, v @ MethodMetaData(OperationType.Command(_))) => command(k, v)
        case (k, v @ MethodMetaData(OperationType.Query(_)))   => query(k, v)
      }.reduce(_ ~ _)
    }
  }

  def exceptionHandler = ExceptionHandler {
    case f: FailException[_] => complete(f.response)
  }

  private[this] val requestToken: Directive1[Option[String]] = {
    val TokenPattern = "Token token=(.+)".r
    optionalHeaderValueByName("Authorization").map {
      case Some(TokenPattern(token)) => Some(token)
      case _                         => None
    }
  }

  private[this] def operationName(operationFullName: String, methodMetaData: MethodMetaData): String =
    methodMetaData.operationType.name.getOrElse(operationFullName.split("""\.""").last)

  private[this] def query(operationFullName: String, methodMetaData: MethodMetaData): Route = {
    (pathPrefix(operationName(operationFullName, methodMetaData)) & pathEnd & get & parameterMap) { params =>
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
    (pathPrefix(operationName(operationFullName, methodMetaData)) & pathEnd & post & entity(as[Json])) { request =>
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

  def commandArgs(request: Json, token: Option[String]): Map[String, Json] =
    request.as[Map[String, Json]].right.get ++ token.map(tokenAsArg)

  private[this] def parseJsonOrString(s: String): Json =
    parse(s).getOrElse(Json.fromString(s))

  def queryArgs(params: Map[String, String], token: Option[String]): Map[String, Json] =
    params.mapValues(parseJsonOrString) ++ token.map(tokenAsArg)

  private[this] def tokenAsArg(token: String): (String, Json) =
    "token" -> Json.obj("token" -> Json.fromString(token))
}
