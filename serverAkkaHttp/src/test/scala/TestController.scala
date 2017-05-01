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
  case class Conflict(userId: Int)
  case object GenericError
  type GenericError = GenericError.type
  case class User(id: Int, username: String)
  case class Ok(msg: String)

  implicit def GenericErrorToResponse = new ToHttpResponse[GenericError] {
    def response(error: GenericError) = HttpResponse(
      status = StatusCodes.InternalServerError,
      entity = "Very Bad"
    )
  }

  implicit def ConflictToResponse = new ToHttpResponse[Conflict] {
    def response(error: Conflict) = HttpResponse(
      status = StatusCodes.Conflict,
      entity = s"User already exists: ${error.userId}"
    )
  }

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

    @command(name = Some("insert"))
    def insertUser(id: Int, user: User): Future[Either[Conflict, Ok]]

    @query(name = Some("number"))
    def usersNumber(): Future[Either[GenericError, Int]]
  }

  private[this] class UserControllerImpl(implicit
    ec: ExecutionContext
  ) extends UserController {
    def update(id: Int, user: User): Future[Either[UserNotFound, Ok]] = Future {
      if (id == 1) Right(Ok("happy update"))
      else Left(UserNotFound(id))
    }

    def find(id: Int): Future[Either[UserNotFound, User]] = Future {
      if (id == 1) Right(User(id, "foo"))
      else Left(UserNotFound(id))
    }

    def insertUser(id: Int, user: User): Future[Either[Conflict, Ok]] = Future {
      Right(Ok("inserted!"))
    }

    def usersNumber(): Future[Either[GenericError, Int]] = Future {
      Right(1)
    }
  }

  private[this] val userController = new UserControllerImpl(): UserController
  def userRouter = new RouteGenerator[UserController] {
    override val routes = route[UserController](userController)
    override val tp = typePath[UserController]
    override val methodsMetaData = deriveMetaData[UserController]
    override val path = derivePath[UserController]
  }
}
