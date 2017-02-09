package wiro.server.akkaHttp

import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ StandardRoute, Route }

import scala.io.StdIn

import wiro.models.ServerConfig
import wiro.server.akkaHttp.RouteGenerators._
import wiro.server.akkaHttp.RouteGenerators.BoxingSupport._

class HttpRPCServer(
  config: ServerConfig,
  controllers: List[GeneratorBox[_]],
  customRoute: Route = reject
)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer
) {
  import system.dispatcher

  val route = controllers
    .map(_.routify)
    .foldLeft(customRoute) (_ ~ _)

  val bindingFuture = Http().bindAndHandle(route, config.host, config.port)

  println(s"Server online at http://${config.host}:${config.port}/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
