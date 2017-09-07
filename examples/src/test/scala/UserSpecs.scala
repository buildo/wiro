/**
  * Created by sam on 07/08/17.
  */
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{AuthenticationFailedRejection, MethodRejection}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import controllers._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.generic.auto._
import models._
import org.scalatest.{FlatSpec, Matchers}
import wiro.server.akkaHttp.RouterDerivationModule
import wiro.server.akkaHttp._






class UserSpecs extends FlatSpec with Matchers with ScalatestRouteTest with RouterDerivationModule {


  val route = deriveRouter[UsersApi](new UsersApiImpl).buildRoute


  it should "get a user" in {
    Get("/users/getUser?id=0") ~> route ~> check {
      responseAs[User].name shouldBe "Pippo"
    }
  }
}
