package wiro.apps

import scala.concurrent.Future
import wiro.server.akkaHttp._
import wiro.models.ServerConfig

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import wiro.server.akkaHttp.routeGenerators._

object Server extends App {
  import wiro.reflect._
  import interface._
  import ApiImpl._
  import io.circe.generic.auto._

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  implicit object CathouseRouter extends RouteGenerator[CathouseApiImpl.type] {
    val routes = route[CathouseApi](CathouseApiImpl)
    val tp = typePath[CathouseApi]
  }

  implicit object DoghouseRouter extends RouteGenerator[DoghouseApiImpl.type] {
    val routes = route[DoghouseApi](DoghouseApiImpl)
    val tp = typePath[DoghouseApi]
  }

  val rpcServer = new HttpRPCServer(
    config = ServerConfig("localhost", 8080),
    controllers = List(DoghouseApiImpl, CathouseApiImpl)
  )
}

object ApiImpl {
  import interface._

  // server-side implementation
  object DoghouseApiImpl extends DoghouseApi {
    def getPuppy(
      token: String, 
      puppyName: String
    ) = Future(Dog(puppyName))
  }

  object CathouseApiImpl extends CathouseApi {
    override def getKitten(
      kittenName: String
    ) = Future(Kitten(kittenName))
  }
}
