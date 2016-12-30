package wiro.apps

import wiro.server.akkaHttp._
import wiro.models.ServerConfig

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Server extends App {
  import ApiImpl._

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val rpcServer = new RPCServer(
    serverConfig = ServerConfig("localhost", 8080),
    apiImpl = DoghouseApiImpl
  )
}

object ApiImpl {
  // server-side implementation, and router
  object DoghouseApiImpl extends AutowireRPCServer with DoghouseApi {
    def getPuppy(puppyName: String): Dog = Dog(name = puppyName)
    def getDogsNumber: Int = 10

    val routes = route[DoghouseApi](this)
  }
}
