package wiro

import akka.http.scaladsl.testkit.ScalatestRouteTest
import autowire._

import io.circe._
import io.circe.generic.auto._

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

import wiro.client.akkaHttp._
import wiro.client.akkaHttp.FailSupport._
import wiro.TestController.{ UserController, User, userRouter }

class WiroSpec extends WordSpec with Matchers with RPCRouteTest with ScalatestRouteTest with ClientDerivationModule with ScalaFutures {
  private[this] val rpcClient = new RPCClientTest(
    deriveClientContext[UserController], userRouter.buildRoute
  ).apply[UserController]

  "A GET request" when {
    "it's right" should {
      "return 200 and content" in {
        whenReady(rpcClient.read(1).call()) {
          user => user shouldBe Right(User(1, "read"))
        }
      }
    }
  }
}
