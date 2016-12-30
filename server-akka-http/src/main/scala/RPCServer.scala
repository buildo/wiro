package wiro.server.akkaHttp
import wiro.models.ServerConfig

import upickle._ 

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http

import scala.io.StdIn

class RPCServer(
  serverConfig: ServerConfig,
  apiImpl: AutowireRPCServer,
  path: String = "rpc"
)(implicit
  actorSystem: ActorSystem,
  materializer: ActorMaterializer
) {
  import scala.concurrent.ExecutionContext.Implicits.global

  val route = new RPCRouterImpl(apiImpl, path).route
  val bindingFuture = Http().bindAndHandle(route, serverConfig.host, serverConfig.port)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => actorSystem.terminate()) // and shutdown when done
}

trait AutowireRPCServer extends autowire.Server[Js.Value, upickle.default.Reader, upickle.default.Writer] {
  def write[Result: upickle.default.Writer](r: Result) = {
    upickle.default.writeJs(r)
  }

  def read[Result: upickle.default.Reader](p: Js.Value) = {
    upickle.default.readJs[Result](p)
  }

  def routes: Router
}
