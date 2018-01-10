package wiro.server.akkaHttp

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsRejected
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.headers.HttpChallenges
import akka.http.scaladsl.model._

import com.typesafe.scalalogging.LazyLogging

import io.circe.{ DecodingFailure, CursorOp }

object AutowireErrorSupport extends LazyLogging {
  def handleUnwrapErrors(
    throwable: Throwable
  ) = throwable match {
    case autowire.Error.InvalidInput(xs) =>
      handleAutowireInputErrors(xs)
    case e: autowire.Error.InvalidInput =>
      logger.info(s"received input is not valid, cannot process entity", e)
      complete(HttpResponse(
        status = StatusCodes.UnprocessableEntity,
        entity = "Unprocessable entity"
      ))
    case e: scala.MatchError =>
      logger.info(s"match error found, route does not exist, returning 404", e)
      complete(HttpResponse(
        status = StatusCodes.NotFound,
        entity = "Operation not found"
      ))
    case e: Exception =>
      logger.error(s"unexpected error, returning 500", e)
      complete(HttpResponse(
        status = StatusCodes.InternalServerError,
        entity = "Internal Error"
      ))
  }

  private[this] def handleMissingParamErrors(
    param: String
  ): StandardRoute = param match {
    case "token" =>
      logger.info("couldn't find token parameter, rejecting request as unauthorized")
      reject(AuthenticationFailedRejection(
        cause = CredentialsRejected,
        challenge = HttpChallenges.basic("api")
      ))
    case "actionQuery" =>
      logger.info("required query parameter is missing, method is not allowed")
      complete(HttpResponse(
        status = StatusCodes.MethodNotAllowed,
        entity = s"Method not allowed"
      ))
    case "actionCommand" =>
      logger.info("required command parameter is missing, method is not allowed")
      complete(HttpResponse(
        status = StatusCodes.MethodNotAllowed,
        entity = s"Method not allowed"
      ))
    case _ =>
      logger.info(s"missing parameter $param from input")
      complete(HttpResponse(
        status = StatusCodes.UnprocessableEntity,
        entity = s"Missing parameter '$param' from input"
      ))
  }

  private[this] def handleAutowireInputErrors(
    xs: autowire.Error.Param
  ): StandardRoute = xs match {
    case autowire.Error.Param.Missing(param) =>
      handleMissingParamErrors(param)
    case error@autowire.Error.Param.Invalid(param, e) => {
      logger.info(s"the received parameter '$param' is invalid", e)
      e match {
        case DecodingFailure(tpe, history) =>
          val path = CursorOp.opsToPath(history)
          complete(HttpResponse(
            status = StatusCodes.UnprocessableEntity,
            entity = s"Failed decoding of '${path}' of type '${tpe}'"
          ))
        case _ =>
          complete(HttpResponse(
            status = StatusCodes.UnprocessableEntity,
            entity = s"Missing parameter '$param' from input"
          ))
      }
    }
  }
}
