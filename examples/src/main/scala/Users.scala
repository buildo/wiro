import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, ContentType, HttpEntity, MediaTypes }
import akka.stream.ActorMaterializer

import io.circe.generic.auto._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import wiro.Config
import wiro.server.akkaHttp._
import wiro.server.akkaHttp.{ FailSupport => ServerFailSupport }
import wiro.client.akkaHttp._
import wiro.client.akkaHttp.{ FailSupport => ClientFailSupport }

// Models definition
object models {
  case class User(name: String)
}

object controllers {
  import models._
  import wiro.annotation._

  // Error messages
  case class Error(msg: String)
  case class UserNotFoundError(msg: String)

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
    ): Future[Either[Error, User]]
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
    ): Future[Either[Error, User]] = {
      val newUser = User(name)
      users(id) = newUser
      Future(Right(newUser))
    }
  }
}


object errors {
  import controllers.UserNotFoundError

  import io.circe.syntax._
  implicit def notFoundToResponse = new ToHttpResponse[UserNotFoundError] {
    def response(error: UserNotFoundError) = HttpResponse(
      status = StatusCodes.NotFound,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), error.asJson.noSpaces)
    )
  }

  import controllers.Error
  implicit def errorToResponse = new ToHttpResponse[Error] {
    def response(error: Error) = HttpResponse(
      status = StatusCodes.InternalServerError,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), error.asJson.noSpaces)
    )
  }
}

object UsersServer extends App with RouterDerivationModule {
  import controllers._
  import models._
  import errors._
  import ServerFailSupport._

  val usersRouter = deriveRouter[UsersApi](new UsersApiImpl)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val rpcServer = new HttpRPCServer(
    config = Config("localhost", 8080),
    routers = List(usersRouter)
  )
}

object UsersClient extends App with ClientDerivationModule {
  import controllers._
  import autowire._
  import ClientFailSupport._

  val config = Config("localhost", 8080)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val rpcClient = new RPCClient(config, ctx = deriveClientContext[UsersApi])

  rpcClient[UsersApi].insertUser(0, "Pippo").call() map (println(_))
}
