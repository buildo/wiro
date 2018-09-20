package wiro

import akka.http.scaladsl.testkit.ScalatestRouteTest
import autowire._
import io.circe.generic.auto._
import org.scalatest.concurrent.{ ScalaFutures, Futures }
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.time._
import wiro.client.akkaHttp._
import wiro.client.akkaHttp.FailSupport._
import wiro.TestController.{User, UserController, userRouter}
import wiro.annotation._

import scala.concurrent.Future

case class GithubError(
  message: String,
  documentation_url: String
)

@path("buildo")
trait GithubController {
  @query
  def wiro(): Future[Either[GithubError, String]]
}

class WiroSpec extends WordSpec with Matchers with RPCRouteTest with ScalatestRouteTest with ClientDerivationModule with ScalaFutures with Futures {
  private[this] val rpcClient = new RPCClientTest(
    deriveClientContext[UserController], userRouter.buildRoute
  ).apply[UserController]

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  "A GET request" when {
    "it's right" should {
      "return 200 and content" in {
        whenReady(rpcClient.read(1).call(), Timeout(Span(5, Seconds))) {
          user => user shouldBe Right(User(1, "read"))
        }
      }
    }
  }

  "An https request" should {
    "return 200" in {
      val config = Config("api.github.com", 443)
      val github = new RPCClient(config, ctx = deriveClientContext[GithubController], scheme = "https")
      github[GithubController].wiro().call().futureValue shouldBe a[Left[GithubError, _]]
    }
  }
}
