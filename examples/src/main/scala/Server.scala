package wiro.apps

import scala.concurrent.Future
import wiro.server.akkaHttp._
import wiro.models.ServerConfig

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }

import wiro.server.akkaHttp.RouteGenerators._

import io.circe.generic.auto._

object errors {
  import FailSupport._
  import controllers.Nope

  import io.circe.syntax._
  implicit def nopeToResponse = new ToHttpResponse[Nope] {
    def response(error: Nope) = HttpResponse(
      status = StatusCodes.UnprocessableEntity,
      entity = error.asJson.noSpaces
    )
  }
}

object Server extends App with MetaDataMacro {
  import controllers._
  import wiro.reflect._
  import models._
  import errors._
  import FailSupport._

  val doghouseApi = new DoghouseApiImpl

  implicit def DoghouseRouter = new RouteGenerator[DoghouseApiImpl] {
    override val routes = route[DoghouseApi](doghouseApi)
    override val methodsMetaData = deriveMetaData[DoghouseApi]
    override val tp = typePath[DoghouseApi]
    override val path = derivePath[DoghouseApi]
  }

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val rpcServer = new HttpRPCServer(
    config = ServerConfig("localhost", 8080),
    controllers = List(doghouseApi)
  )
}

object models {
  case class Dog(name: String)
  case class Kitten(name: String)
}

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
    @command(name = Some("pallino"))
    override def getPallino(
      something: String
    ): Future[Either[Nope, Dog]] = Future(Right(Dog("pallino")))

    @command(name = Some("puppy"))
    override def getPuppy(
      wa: Int
    ): Future[Either[Nope, Dog]] = Future(Left{
      println(wa)
      Nope("Not doing that")
    })
  }
}
