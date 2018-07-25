package wiro
package server.akkaHttp

import akka.actor.{ Actor, ActorSystem, Props, Status }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.pipe
import akka.stream.ActorMaterializer

import com.typesafe.scalalogging.LazyLogging

import wiro.server.akkaHttp.{ Router => WiroRouter }

import pureconfig.loadConfigOrThrow

class HttpRPCServer(
  config: Config,
  routers: List[WiroRouter],
  customRoute: Route = reject
)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer
) {
  private[this] val referenceConfig = loadConfigOrThrow[ReferenceConfig]("wiro")
  private[this] val foldedRoutes = routers
    .map(_.buildRoute)
    .foldLeft(customRoute) (_ ~ _)

  val route = referenceConfig.routesPrefix match {
    case Some(prefix) => pathPrefix(prefix) { foldedRoutes }
    case None => foldedRoutes
  }

  system.actorOf(Props(new HttpRPCServerActor(config, route)), "wiro-server")
}

class HttpRPCServerActor(
  config: Config,
  route: Route
)(implicit
  system: ActorSystem,
  materializer: ActorMaterializer
) extends Actor with LazyLogging {
  import system.dispatcher

  override def receive = {
    case binding: Http.ServerBinding => logger.info(s"Binding on {}", binding.localAddress)
    case Status.Failure(cause) => logger.error(s"Unable to bind to ${config.host}:${config.port}", cause)
  }

  Http()
    .bindAndHandle(route, config.host, config.port)
    .pipeTo(self)
}
