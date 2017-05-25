package wiro.apps

import wiro.client._

import scala.concurrent.Future
import wiro.server.akkaHttp.{ RouterDerivationModule, ToHttpResponse, FailSupport, HttpRPCServer }
import wiro.models.Config

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, ContentType, HttpEntity}
import akka.http.scaladsl.model.MediaTypes

import wiro.server.akkaHttp.RouteGenerators._

import io.circe.generic.auto._

object controllers {
  import models._
  import wiro.annotation._
  import FailSupport._

  case class Nope(msg: String)
  case class Wa(lol: String, bah: Int, dah: Int)

  @path("woff")
  trait DoghouseApi {
    @command(name = Some("puppy"))
    def getPuppy(
      wa: Int
    ): Future[Either[Nope, Dog]]

    @command(name = Some("pallino"))
    def getPallino(
      something: String
    ): Future[Either[Nope, Dog]]
  }

  class DoghouseApiImpl() extends DoghouseApi {
    override def getPallino(
      something: String
    ): Future[Either[Nope, Dog]] = Future(Right(Dog("pallino")))

    override def getPuppy(
      wa: Int
    ): Future[Either[Nope, Dog]] = Future(Left{
      println(wa)
      Nope("Not doing that")
    })
  }
}

object errors {
  import FailSupport._
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
  import wiro.reflect._

  val config = Config("localhost", 8080)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val doghouseClient = deriveClientContext[DoghouseApi]
  val rpcClient = new RPCClient(config, doghouseClient)

  val res = rpcClient[DoghouseApi].getPuppy(1).call()

  res map (println(_))
}

object Server extends App with RouterDerivationModule {
  import controllers._
  import wiro.reflect._
  import models._
  import errors._
  import FailSupport._

  val doghouseApi = new DoghouseApiImpl: DoghouseApi
  implicit def DoghouseRouter = deriveRouter[DoghouseApi](doghouseApi)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val rpcServer = new HttpRPCServer(
    config = Config("localhost", 8080),
    controllers = List(doghouseApi)
  )
}

object models {
  case class Dog(name: String)
  case class Kitten(name: String)
}
