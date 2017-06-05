import scala.concurrent.Future
import wiro.server.akkaHttp.{ RouterDerivationModule, ToHttpResponse, FailSupport, HttpRPCServer }
import wiro.models.Config

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, ContentType, HttpEntity}
import akka.http.scaladsl.model.MediaTypes

import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.CirceSupport

object controllers {
  import models._
  import wiro.annotation._

  // Error message
  case class UserNotFoundError(msg: String)
  case class AnotherError(msg: String)

  // API interface
  @path("users")
  trait UsersApi {

    @query(name = Some("getUser"))
    def getUser(
      id: Int
    ): Future[Either[UserNotFoundError, User]]

    @command(name = Some("insertUser"))
    def insertUser(
      id: Int,
      name: String
    ): Future[Either[AnotherError, User]]
  }

  val users = collection.mutable.Map.empty[Int, User]

  // API implementation
  class UsersApiImpl() extends UsersApi {
    override def getUser(
      id: Int
    ): Future[Either[UserNotFoundError, User]] = {
      users.get(id) match {
        case Some(user) => Future(Right(user))
        case None => Future(Left(UserNotFoundError("User not found")))
      }
    }

    override def insertUser(
      id: Int,
      name: String
    ): Future[Either[AnotherError, User]] = {
      val newUser = User(name)
      users(id) = newUser
      Future(Right(newUser))
    }
  }
}


object errors {
  import FailSupport._
  import controllers.UserNotFoundError

  import io.circe.syntax._
  implicit def notFoundToResponse = new ToHttpResponse[UserNotFoundError] {
    def response(error: UserNotFoundError) = HttpResponse(
      status = StatusCodes.NotFound,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), error.asJson.noSpaces)
    )
  }

  import controllers.AnotherError
  implicit def anotherErrorToResponse = new ToHttpResponse[AnotherError] {
    def response(error: AnotherError) = HttpResponse(
      status = StatusCodes.InternalServerError,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), error.asJson.noSpaces)
    )
  }
}

object UsersServer extends App with RouterDerivationModule {
  import controllers._
  import wiro.reflect._
  import models._
  import errors._
  import FailSupport._

  val usersRouter = deriveRouter[UsersApi](new UsersApiImpl)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val rpcServer = new HttpRPCServer(
    config = Config("localhost", 8080),
    routers = List(usersRouter)
  )
}

// Models definition
object models {
  case class User(name: String)
}
