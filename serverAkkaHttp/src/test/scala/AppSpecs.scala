package wiro

import akka.util.ByteString
import akka.http.scaladsl.model.{ HttpEntity, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.MethodRejection
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.HttpMethods._

import de.heikoseeberger.akkahttpcirce.CirceSupport._

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
          responseAs[Ok] should be (Ok("happy update"))
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

    //TODO(claudio) returned errors are not optimal
    "HTTP method is wrong" should {
      "return method is missing when GET" in {
        Get("/user/update?id=1") ~> userRouter.buildRoute ~> check {
          status should be (UnprocessableEntity)
        }
      }

      "return method is missing when DELETE" in {
        Delete("/user/update?id=1") ~> userRouter.buildRoute ~> check {
          rejections shouldEqual List(MethodRejection(POST), MethodRejection(GET))
        }
      }

      "return method is missing when PUT" in {
        Put("/user/update?id=1") ~> userRouter.buildRoute ~> check {
          rejections shouldEqual List(MethodRejection(POST), MethodRejection(GET))
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
          status should be (NotFound)
        }
      }
    }
  }

  "A GET request" when {
    "it's right" should {
      "return 200 and content" in {
        Get("/user/find?id=1") ~> userRouter.buildRoute ~> check {
          status should be (OK)
          responseAs[User] should be (User(1, "foo"))
        }
      }
    }

    "it's left" should {
      "return provided error" in {
        Get("/user/find?id=2") ~> userRouter.buildRoute ~> check {
          status should be (NotFound)
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

        Post("/user/find", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
          status should be (MethodNotAllowed)
        }
      }

      "return method is missing when DELETE" in {
        Delete("/user/find") ~> userRouter.buildRoute ~> check {
          rejections shouldEqual List(MethodRejection(POST), MethodRejection(GET))
        }
      }

      "return method is missing when PUT" in {
        Put("/user/find") ~> userRouter.buildRoute ~> check {
          rejections shouldEqual List(MethodRejection(POST), MethodRejection(GET))
        }
      }
    }

    "operation doesn't exist" should {
      "return 405" in {
        Get("/user/fin") ~> userRouter.buildRoute ~> check {
          status should be (NotFound)
        }
      }
    }
  }
}
