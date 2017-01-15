package wiro.server.akkaHttp
import wiro.models.ServerConfig

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import wiro.server.akkaHttp.routeGenerators._

import scala.io.StdIn

import io.circe._
import io.circe.syntax._

import wiro.models.Codecs

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

  println(s"Server online at http://${config.host}:${config.port}/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}

trait RPCController extends autowire.Server[Json, Decoder, Encoder] with Codecs {
  def write[Result: Encoder](r: Result): Json = r.asJson
  //TODO handle circe error here
  def read[Result: Decoder](p: Json): Result = p.as[Result].right.get

  def routes: Router
  def tp: Seq[String]
  def path: String = tp.last
}
