package wiro.apps

import wiro.server.akkaHttp._
import wiro.models.ServerConfig

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import wiro.server.akkaHttp.routeGenerators._

object Server extends App {
  import ApiImpl._

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  import wiro.reflect._
  implicit object CathouseRouter extends RouteGenerator[CathouseApiImpl.type] {
    val routes = route[CathouseApi](CathouseApiImpl)
    val tp = typePath[CathouseApi]
    val path = "cathouse"
  }

  implicit object DoghouseRouter extends RouteGenerator[DoghouseApiImpl.type] {
    val routes = route[DoghouseApi](DoghouseApiImpl)
    val tp = typePath[DoghouseApi]
    val path = "doghouse"
  }

  val rpcServer = new HttpRPCServer(
    config = ServerConfig("localhost", 8080),
    controllers = List(DoghouseApiImpl, CathouseApiImpl)
  )
}

object ApiImpl {
  import wiro.annotation._

  // server-side implementation
  object DoghouseApiImpl extends DoghouseApi {
    @auth
    def getPuppy(
       puppyName: String
    ) = println("ciao")
  }

  object CathouseApiImpl extends CathouseApi {
    override def getKitten(kittenName: String): Kitten = Kitten(name = kittenName)
  }
}
