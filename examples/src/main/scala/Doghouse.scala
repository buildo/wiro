package wiro.apps

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, ContentType, HttpEntity, MediaTypes }

import io.circe.Json
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import wiro.{ Config, Auth, OperationParameters }
import wiro.server.akkaHttp._
import wiro.server.akkaHttp.{ FailSupport => ServerFailSupport }
import wiro.client.akkaHttp._
import wiro.client.akkaHttp.{ FailSupport => ClientFailSupport }

object controllers {
  import models._
  import wiro.annotation._
  import ServerFailSupport._

  case class Nope(msg: String)
  case class Wa(lol: String, bah: Int, dah: Int)

  @path("woff")
  trait DoghouseApi {
    @query(name = Some("puppy"))
    def getPuppy(
      token: Auth,
      wa: Int,
      parameters: OperationParameters
    ): Future[Either[Nope, Dog]]

    @query(name = Some("pallino"))
    def getPallino(
      something: String
    ): Future[Either[Nope, Dog]]
  }

  class DoghouseApiImpl() extends DoghouseApi {
    override def getPallino(
      something: String
    ): Future[Either[Nope, Dog]] = Future {
      Right(Dog("pallino"))
    }

    override def getPuppy(
      token: Auth,
      wa: Int,
      parameters: OperationParameters
    ): Future[Either[Nope, Dog]] = Future {
      if (token == Auth("tokenone")) Right(Dog("pallino"))
      else Left(Nope("nope"))
    }
  }
}

object errors {
  import ServerFailSupport._
  import controllers.Nope

  import io.circe.syntax._
  implicit def nopeToResponse = new ToHttpResponse[Nope] {
    def response(error: Nope) = HttpResponse(
      status = StatusCodes.UnprocessableEntity,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), error.asJson.noSpaces)
    )
  }
}

object Client extends App with ClientDerivationModule {
  import controllers._
  import autowire._
  import ClientFailSupport._

  val config = Config("localhost", 8080)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val doghouseClient = deriveClientContext[DoghouseApi]
  val rpcClient = new RPCClient(config, ctx = doghouseClient)

  val res = rpcClient[DoghouseApi].getPuppy(Auth("tokenone"), 1, OperationParameters(parameters = Map())).call()

  res map (println(_)) recover { case e: Exception => e.printStackTrace }
}

object Server extends App with RouterDerivationModule {
  import controllers._
  import models._
  import errors._
  import ServerFailSupport._

  val doghouseRouter = deriveRouter[DoghouseApi](new DoghouseApiImpl)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val rpcServer = new HttpRPCServer(
    config = Config("localhost", 8080),
    routers = List(doghouseRouter)
  )
}

object models {
  case class Dog(name: String)
  case class Kitten(name: String)
}
