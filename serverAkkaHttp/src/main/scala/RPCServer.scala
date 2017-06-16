package wiro
package server.akkaHttp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ StandardRoute, Route }
import akka.stream.ActorMaterializer

import scala.io.StdIn

import wiro.server.akkaHttp.{ Router => WiroRouter }

class HttpRPCServer(
  config: Config,
  routers: List[WiroRouter],
  customRoute: Route = reject
)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer
) {
  import system.dispatcher

  val route = routers
    .map(_.buildRoute)
    .foldLeft(customRoute) (_ ~ _)

  val bindingFuture = Http().bindAndHandle(route, config.host, config.port)

  println(s"Server online at http://${config.host}:${config.port}/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
