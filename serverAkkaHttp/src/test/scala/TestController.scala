package wiro

import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }

import io.circe.generic.auto._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits.global

import wiro.annotation._
import wiro.reflect._
import wiro.server.akkaHttp._
import wiro.server.akkaHttp.FailSupport._
import wiro.server.akkaHttp.RouteGenerators._

object TestController {
  case class UserNotFound(userId: Int)
  case class User(id: Int, username: String)
  case class Ok(msg: String)

  implicit def notFoundToResponse = new ToHttpResponse[UserNotFound] {
    def response(error: UserNotFound) = HttpResponse(
      status = StatusCodes.NotFound,
      entity = s"User not found: ${error.userId}"
    )
  }

  //controllers interface and implementation
  @path("user")
  trait UserController {
    @command
    def update(id: Int, user: User): Future[Either[UserNotFound, Ok]]

    @query
    def find(id: Int): Future[Either[UserNotFound, User]]
  }

  private[this] class UserControllerImpl(implicit
    ec: ExecutionContext
  ) extends UserController {
    @command
    def update(id: Int, user: User): Future[Either[UserNotFound, Ok]] = Future {
      if (id == 1) Right(Ok("happy update"))
      else Left(UserNotFound(id))
    }

    @query
    def find(id: Int): Future[Either[UserNotFound, User]] = Future {
      if (id == 1) Right(User(id, "foo"))
      else Left(UserNotFound(id))
    }
  }

  private[this] val userController = new UserControllerImpl(): UserController
  def userRouter = new RouteGenerator[UserController] {
    val routes = route[UserController](userController)
    val tp = typePath[UserController]
    override val path = derivePath[UserController]
  }
}
