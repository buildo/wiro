package wiro

import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }

import io.circe.generic.auto._

import scala.concurrent.{ ExecutionContext, Future }

import wiro.annotation._
import wiro.server.akkaHttp._
import wiro.server.akkaHttp.FailSupport._

object TestController extends RouterDerivationModule {
  case class UserNotFound(userId: Int)
  case class Conflict(userId: Int)
  case object GenericError
  type GenericError = GenericError.type
  case class User(id: Int, username: String)
  case class Ok(msg: String)
  case class Unauthorized(msg: String)

  implicit def genericErrorToResponse = new ToHttpResponse[GenericError] {
    def response(error: GenericError) = HttpResponse(
      status = StatusCodes.InternalServerError,
      entity = "Very Bad"
    )
  }

  implicit def unauthorizedToResponse = new ToHttpResponse[Unauthorized] {
    def response(error: Unauthorized) = HttpResponse(
      status = StatusCodes.Unauthorized,
      entity = "Very Bad"
    )
  }

  implicit def conflictToResponse = new ToHttpResponse[Conflict] {
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
    @query
    def nobodyCannaCrossIt(token: Auth): Future[Either[Unauthorized, Ok]]

    @command
    def update(id: Int, user: User): Future[Either[UserNotFound, Ok]]

    @command
    def updateCommand(id: Int, user: User): Future[Either[UserNotFound, Ok]]

    @query
    def read(id: Int): Future[Either[UserNotFound, User]]

    @query
    def readQuery(id_p: Int): Future[Either[UserNotFound, User]]

    @command(name = Some("insert"))
    def insertUser(id: Int, user: User): Future[Either[Conflict, Ok]]

    @query(name = Some("number"))
    def usersNumber(): Future[Either[GenericError, Int]]
  }

  private[this] class UserControllerImpl(implicit
    ec: ExecutionContext
  ) extends UserController {
    def nobodyCannaCrossIt(token: Auth): Future[Either[Unauthorized, Ok]] = Future {
      if (token == Auth("bus")) Right(Ok("di bus can swim"))
      else Left(Unauthorized("yuh cannot cross it"))
    }

    def update(id: Int, user: User): Future[Either[UserNotFound, Ok]] = Future {
      if (id == 1) Right(Ok("update"))
      else Left(UserNotFound(id))
    }

    def updateCommand(id: Int, user: User): Future[Either[UserNotFound, Ok]] = Future {
      if (id == 1) Right(Ok("updateCommand"))
      else Left(UserNotFound(id))
    }

    def read(id: Int): Future[Either[UserNotFound, User]] = Future {
      if (id == 1) Right(User(id, "read"))
      else Left(UserNotFound(id))
    }

    def readQuery(idP: Int): Future[Either[UserNotFound, User]] = Future {
      if (idP == 1) Right(User(idP, "readQuery"))
      else Left(UserNotFound(idP))
    }

    def insertUser(id: Int, user: User): Future[Either[Conflict, Ok]] = Future {
      Right(Ok("inserted!"))
    }

    def usersNumber(): Future[Either[GenericError, Int]] = Future {
      Right(1)
    }
  }

  import scala.concurrent.ExecutionContext.Implicits.global
  private[this] val userController = new UserControllerImpl
  def userRouter = deriveRouter[UserController](userController)
}
