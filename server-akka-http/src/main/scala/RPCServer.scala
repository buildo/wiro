package wiro.server.akkaHttp
import wiro.models.ServerConfig

import upickle._ 

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import wiro.server.akkaHttp.routeGenerators._

import scala.io.StdIn

class HttpRPCServer(
  config: ServerConfig,
  controllers: List[GeneratorBox[_]]
)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer
) {
  import system.dispatcher

  val routes = controllers map(_.routify) reduceLeft(_ ~ _)

  val bindingFuture = Http().bindAndHandle(routes, config.host, config.port)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}

trait RPCController extends autowire.Server[Js.Value, upickle.default.Reader, upickle.default.Writer] {
  def write[Result: upickle.default.Writer](r: Result) = {
    upickle.default.writeJs(r)
  }

  def read[Result: upickle.default.Reader](p: Js.Value) = {
    upickle.default.readJs[Result](p)
  }

  def routes: Router
  def tp: Seq[String]
  def path: String
}
