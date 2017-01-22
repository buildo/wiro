package wiro.apps

import scala.concurrent.Future
import wiro.server.akkaHttp._
import wiro.models.{ ServerConfig, Command }

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }

import wiro.server.akkaHttp.RouteGenerators._

import io.circe.generic.auto._

object errors {
  import FailSupport._
  import controllers.ImATeaPot

  implicit def teapotToResponse = new ToHttpResponse[ImATeaPot.type] {
    def response = HttpResponse(
      status = StatusCodes.BlockedByParentalControls,
      entity = "Don't do that!"
    )
  }
}

object router {
  import wiro.reflect._
  import controllers._
  import models._
  import errors._
  import FailSupport._

  implicit def DoghouseRouter = new RouteGenerator[DoghouseApiImpl.type] {
    val routes = route[DoghouseApi](DoghouseApiImpl)
    val tp = typePath[DoghouseApi]
  }
}

object Server extends App {
  import router._
  import controllers._

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val rpcServer = new HttpRPCServer(
    config = ServerConfig("localhost", 8080),
    controllers = List(DoghouseApiImpl)
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

  case object ImATeaPot

  trait DoghouseApi {
    @token
    @command
    def getPuppy(puppyName: String): Future[Either[ImATeaPot.type, Dog]]
  }

  object DoghouseApiImpl extends DoghouseApi {
    @token
    @command
    override def getPuppy(
      puppyName: String
    ): Future[Either[ImATeaPot.type, Dog]] = Future(Left(ImATeaPot))
  }
}
