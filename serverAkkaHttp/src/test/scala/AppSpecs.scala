package wiro

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpChallenge
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{ AuthenticationFailedRejection, MethodRejection }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest

import akka.util.ByteString

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._

import io.circe.generic.auto._

import org.scalatest.{ Matchers, WordSpec }

import wiro.TestController._

class WiroSpec extends WordSpec with Matchers with ScalatestRouteTest {

  private[this] def jsonEntity(data: ByteString) = HttpEntity(
    contentType = MediaTypes.`application/json`,
    data = data
  )

  "A POST request" when {
    "it's right" should {
      "return 200 and content" in {
        val data = ByteString(
          s"""
             |{
             |    "id": 1,
             |    "user": {
             |        "id": 1,
             |        "username": "foo"
             |    }
             |}
          """.stripMargin)

        Post("/user/update", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
          status should be (OK)
          responseAs[Ok] should be (Ok("update"))
        }
      }
    }

    "points to route that includes the name of another route" should {
      "invoke the correct path" in {
        val data = ByteString(
          s"""
             |{
             |    "id": 1,
             |    "user": {
             |        "id": 1,
             |        "username": "foo"
             |    }
             |}
          """.stripMargin)

        Post("/user/updateCommand", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
          status should be (OK)
          responseAs[Ok] should be (Ok("updateCommand"))
        }
      }
    }

    "it's left" should {
      "return provided error" in {
        val data = ByteString(
          s"""
             |{
             |    "id": 2,
             |    "user": {
             |        "id": 2,
             |        "username": "foo"
             |    }
             |}
          """.stripMargin)

        Post("/user/update", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
          status should be (NotFound)
        }
      }
    }

    "operation is overridden" should {
      "overridden route should be used" in {
        val data = ByteString(
          s"""
             |{
             |    "id": 2,
             |    "user": {
             |        "id": 2,
             |        "username": "foo"
             |    }
             |}
          """.stripMargin)

        Post("/user/insert", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
          status should be (OK)
        }
      }
    }

    "has unsuitable body" should {
      "return 422" in {
        val data = ByteString(
          s"""
             |{
             |    "id": "foo",
             |    "user": {
             |        "username": "foo",
             |        "id": 1
             |    }
             |}
          """.stripMargin)

        Post("/user/update", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
          status should be (UnprocessableEntity)
        }
      }
    }

    "has unsuitable body in nested resource" should {
      "return 422" in {
        val data = ByteString(
          s"""
             |{
             |    "id": 1,
             |    "user": {
             |        "username": "foo"
             |    }
             |}
          """.stripMargin)

        Post("/user/update", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
          status should be (UnprocessableEntity)
        }
      }
    }

    "HTTP method is wrong" should {
      "return method is missing when GET" in {
        Get("/user/update?id=1") ~> userRouter.buildRoute ~> check {
          rejections shouldEqual List(MethodRejection(POST))
        }
      }

      "return method is missing when DELETE" in {
        Delete("/user/update?id=1") ~> userRouter.buildRoute ~> check {
          rejections shouldEqual List(MethodRejection(POST))
        }
      }

      "return method is missing when PUT" in {
        Put("/user/update?id=1") ~> userRouter.buildRoute ~> check {
          rejections shouldEqual List(MethodRejection(POST))
        }
      }
    }

    "operation doesn't exist" should {
      "return 405" in {
        val data = ByteString(
          s"""
             |{
             |    "id": 1,
             |    "user": {
             |        "username": "foo"
             |    }
             |}
          """.stripMargin)

        Post("/user/updat", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
          rejections shouldEqual Nil
        }
      }
    }
  }

  "A GET request" when {
    "it's right" should {
      "return 200 and content" in {
        Get("/user/read?id=1") ~> userRouter.buildRoute ~> check {
          status should be (OK)
          responseAs[User] should be (User(1, "read"))
        }
      }
    }

    "it's authenticated" should {
      "block user without proper token" in {
        Get("/user/nobodyCannaCrossIt") ~> userRouter.buildRoute ~> check {
          status should be (Unauthorized)
        }
      }

      "not block user having proper token" in {
        Get("/user/nobodyCannaCrossIt") ~> addHeader("Authorization", "Token token=bus") ~> userRouter.buildRoute ~> check {
          status should be (OK)
          responseAs[Ok] should be (Ok("di bus can swim"))
        }
      }
    }

    "points to route that includes the name of another route" should {
      "invoke the correct path" in {
        Get("/user/readQuery?id=1") ~> userRouter.buildRoute ~> check {
          status should be (OK)
          responseAs[User] should be (User(1, "readQuery"))
        }
      }
    }

    "it's left" should {
      "return provided error" in {
        Get("/user/read?id=2") ~> userRouter.buildRoute ~> check {
          status should be (NotFound)
        }
      }
    }

    "operation is overridden" should {
      "use overridden route should be used" in {
        Get("/user/number") ~> userRouter.buildRoute ~> check {
          status should be (OK)
        }
      }
    }

    "HTTP method is wrong" should {
      "return method is missing when POST" in {
        val data = ByteString(
          s"""
             |{
             |    "id": 1
             |}
          """.stripMargin)

        Post("/user/read", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
          rejections shouldEqual List(MethodRejection(GET))
        }
      }

      "return method is missing when DELETE" in {
        Delete("/user/read") ~> userRouter.buildRoute ~> check {
          rejections shouldEqual List(MethodRejection(GET))
        }
      }

      "return method is missing when PUT" in {
        Put("/user/read") ~> userRouter.buildRoute ~> check {
          rejections shouldEqual List(MethodRejection(GET))
        }
      }
    }

    "operation doesn't exist" should {
      "return be rejected" in {
        Get("/user/rea") ~> userRouter.buildRoute ~> check {
          rejections shouldEqual Nil
        }
      }
    }
  }
}
