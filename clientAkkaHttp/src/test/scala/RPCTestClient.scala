package wiro

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{ RouteTest, TestFrameworkInterface }

import cats.implicits._
import client.akkaHttp.{ RPCClient, RPCClientContext }

import scala.concurrent.Future

trait RPCRouteTest extends RouteTest { this: TestFrameworkInterface =>
  class RPCClientTest(ctx: RPCClientContext[_], route: Route)
    extends RPCClient(config = Config("localhost", 80), ctx = ctx){
      override private[wiro] def doHttpRequest(request: HttpRequest) =
        (request ~> route).response.pure[Future]
  }
}
