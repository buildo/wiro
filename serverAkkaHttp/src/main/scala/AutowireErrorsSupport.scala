package wiro.server.akkaHttp

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.AuthenticationFailedRejection.CredentialsRejected
import akka.http.scaladsl.model.headers.HttpChallenges
import akka.http.scaladsl.model._

import io.circe.{ DecodingFailure, CursorOp }

object AutowireErrorSupport {
  def handleUnwrapErrors(
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

  private[this] def handleMissingParamErrors(
    param: String
  ): StandardRoute = param match {
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

  private[this] def handleAutowireInputErrors(
    xs: autowire.Error.Param
  ): StandardRoute = xs match {
    case autowire.Error.Param.Missing(param) =>
      handleMissingParamErrors(param)
    case autowire.Error.Param.Invalid(param, ex) => {
      ex match {
        case DecodingFailure(tpe, history) =>
          val path = CursorOp.opsToPath(history)
          complete(HttpResponse(
            status = StatusCodes.UnprocessableEntity,
            entity = s"Failed decoding of '${path}' of type '${tpe}'"
          ))
        case _ =>
          complete(HttpResponse(
            status = StatusCodes.UnprocessableEntity,
            entity = s"Missing parameter $param from input"
          ))
      }
    }
  }
}
