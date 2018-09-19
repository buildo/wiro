package wiro

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit._

import cats.implicits._
import client.akkaHttp.{ RPCClient, RPCClientContext }

import scala.concurrent.Future

trait RPCRouteTest extends RouteTest { this: TestFrameworkInterface =>
  val prefix = Some("test")
  class RPCClientTest(ctx: RPCClientContext[_], route: Route)
    extends RPCClient(config = Config("localhost", 80), prefix = prefix, ctx = ctx){
      override private[wiro] def doHttpRequest(request: HttpRequest) =
        (request ~> pathPrefix(prefix.get)(route)).response.pure[Future]
  }
}
