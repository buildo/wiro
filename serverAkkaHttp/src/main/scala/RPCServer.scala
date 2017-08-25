package wiro
package server.akkaHttp

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props, Status }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.pipe
import akka.stream.ActorMaterializer

import wiro.server.akkaHttp.{ Router => WiroRouter }

class HttpRPCServer(
  config: Config,
  routers: List[WiroRouter],
  customRoute: Route = reject
)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer
) {
  val route = routers
    .map(_.buildRoute)
    .foldLeft(customRoute) (_ ~ _)

  system.actorOf(Props(new HttpRPCServerActor(config, route)), "wiro-server")
}

class HttpRPCServerActor(
  config: Config,
  route: Route
)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer
) extends Actor with ActorLogging {
  import system.dispatcher

  override def receive = {
    case binding: Http.ServerBinding => log.info("Binding on {}", binding.localAddress)
    case Status.Failure(cause) => log.error(cause, s"Unable to bind to ${config.host}:${config.port}")
  }

  Http()
    .bindAndHandle(route, config.host, config.port)
    .pipeTo(self)
}
