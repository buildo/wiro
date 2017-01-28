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

object Server extends App {
  import controllers._
  import wiro.reflect._
  import models._
  import errors._
  import FailSupport._

  val doghouseApi = new DoghouseApiImpl

  implicit def DoghouseRouter = new RouteGenerator[DoghouseApiImpl] {
    val routes = route[DoghouseApi](doghouseApi)
    val tp = typePath[DoghouseApi]
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

  trait DoghouseApi {
    @token
    @query
    def getPuppy(
      str: String,
      dou: Double,
      int: Int,
      bol: Boolean
    ): Future[Either[Nope, Dog]]
  }

  class DoghouseApiImpl() extends DoghouseApi {
    @token
    @query
    override def getPuppy(
      str: String,
      dou: Double,
      int: Int,
      bol: Boolean
    ): Future[Either[Nope, Dog]] = Future(Left{
      println(str)
      println(dou)
      println(int)
      println(bol)
      Nope("Not doing that")
    })
  }
}
