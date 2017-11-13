package wiro

import cats.implicits._
import client.akkaHttp.{ RPCClient, RPCClientContext }

import akka.http.scaladsl.testkit.RouteTest
import akka.http.scaladsl.testkit.TestFrameworkInterface
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Route

import scala.concurrent.Future

trait RPCRouteTest extends RouteTest { this: TestFrameworkInterface ⇒
  class RPCClientTest(ctx: RPCClientContext[_], route: Route)
    extends RPCClient(config = Config("localhost", 80), ctx = ctx){
      override private[wiro] def doHttpRequest(request: HttpRequest) =
        (request ~> route).response.pure[Future]
  }
}
