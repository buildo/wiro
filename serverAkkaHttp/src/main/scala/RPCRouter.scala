package wiro
package server.akkaHttp

import AutowireErrorSupport._

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive0, Directive1, ExceptionHandler, Route }
import akka.http.scaladsl.model.HttpEntity

import cats.syntax.either._

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._

import FailSupport._

import io.circe.{ Json, JsonObject, Printer, ParsingFailure }
import io.circe.parser._

import com.typesafe.scalalogging.LazyLogging

trait Router extends RPCServer with PathMacro with MetaDataMacro with LazyLogging {
  def tp: Seq[String]
  def methodsMetaData: Map[String, MethodMetaData]
  def routes: autowire.Core.Router[Json]
  def path: String = tp.last
  implicit def printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  def buildRoute: Route = handleExceptions(exceptionHandler) {
    pathPrefix(path) {
      methodsMetaData.map {
        case (k, v @ MethodMetaData(OperationType.Command(_))) => command(k, v)
        case (k, v @ MethodMetaData(OperationType.Query(_)))   => query(k, v)
      }.reduce(_ ~ _)
    }
  }

  def exceptionHandler = ExceptionHandler {
    case e: FailException[_] =>
      //logging left
      e.response.entity match {
        case HttpEntity.Strict(_, data) => logger.error(s"${e.response.status.value} ${data.utf8String}")
        case complexEntity => logger.error(s"${e.response.status} ${e.response.entity}")
      }
      complete(e.response)
  }

  private[this] val requestToken: Directive1[Option[String]] = {
    val TokenPattern = "Token token=(.+)".r
    optionalHeaderValueByName("Authorization").map {
      case Some(TokenPattern(token)) => Some(token)
      case _                         => None
    }
  }

  private[this] def operationPath(operationFullName: String): Array[String] =
    operationFullName.split('.')

  private[this] def operationName(operationFullName: String, methodMetaData: MethodMetaData): String =
    methodMetaData.operationType.name.getOrElse(operationPath(operationFullName).last)

  private[this] def autowireRequest(operationFullName: String, args: Map[String, Json]): autowire.Core.Request[Json] =
    autowire.Core.Request(path = operationPath(operationFullName), args = args)

  private[this] def autowireRequestRoute(operationFullName: String, args: Map[String, Json]): Route =
    Either.catchNonFatal(routes(autowireRequest(operationFullName, args)))
      .fold(handleUnwrapErrors, result => complete(result))

  private[this] def autowireRequestRouteWithToken(operationFullName: String, args: Map[String, Json], headers: Map[String, Json]): Route =
    requestToken(token => autowireRequestRoute(operationFullName, args ++ token.map(tokenAsArg) ++ Some(headersAsArg(headers))))

  private[this] def routePathPrefix(operationFullName: String, methodMetaData: MethodMetaData): Directive0 =
    pathPrefix(operationName(operationFullName, methodMetaData))

  private[this] def loggingClientIP: Directive0 = extractClientIP.map { ip => logger.debug(s"client ip: ${ip}") }

  //Generates GET requests
  private[this] def query(operationFullName: String, methodMetaData: MethodMetaData): Route =
    loggingClientIP {
      (routePathPrefix(operationFullName, methodMetaData) & pathEnd & get & parameterMap) { params =>
        headersDirective { headers =>
          val args = params.mapValues(parseJsonObjectOrString)
          autowireRequestRouteWithToken(operationFullName, args, headers)
        }
      }
    }

  private[this] def headersDirective: Directive1[Map[String, Json]] =
    extract(_.request.headers).map(_.map(header => header.name -> Json.fromString(header.value)).toMap)

  //Generates POST requests
  private[this] def command(operationFullName: String, methodMetaData: MethodMetaData): Route =
    loggingClientIP {
      (routePathPrefix(operationFullName, methodMetaData) & pathEnd & post & entity(as[JsonObject])) { request =>
        headersDirective { headers => autowireRequestRouteWithToken(operationFullName, request.toMap, headers) }
      }
    }

  private[this] def parseJsonObject(s: String): Either[ParsingFailure, Json] = {
    val failure = ParsingFailure("The parsed Json is not an object", new Exception())
    parse(s).ensure(failure)(_.isObject)
  }

  private[this] def parseJsonObjectOrString(s: String): Json =
    parseJsonObject(s).getOrElse(Json.fromString(s))

  private[this] def tokenAsArg(token: String): (String, Json) =
    "token" -> Json.obj("token" -> Json.fromString(token))

  private[this] def headersAsArg(headersMap: Map[String, Json]): (String, Json) =
    "parameters" -> Json.obj("parameters" -> Json.fromFields(headersMap))
}
