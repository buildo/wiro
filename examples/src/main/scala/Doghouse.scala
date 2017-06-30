package wiro.apps

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, ContentType, HttpEntity}
import akka.http.scaladsl.model.MediaTypes

import io.circe.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import wiro.{ Config, Token }
import wiro.server.akkaHttp._
import wiro.client.akkaHttp._

object controllers {
  import models._
  import wiro.annotation._
  import FailSupport._

  case class Nope(msg: String)
  case class Wa(lol: String, bah: Int, dah: Int)

  @path("woff")
  trait DoghouseApi {
    @query(name = Some("puppy"))
    def getPuppy(
      token: Token,
      wa: Int
    ): Future[Either[Nope, Dog]]

    @command(name = Some("pallino"))
    def getPallino(
      something: String
    ): Future[Either[Nope, Dog]]
  }

  class DoghouseApiImpl() extends DoghouseApi {
    override def getPallino(
      something: String
    ): Future[Either[Nope, Dog]] = Future(Right(Dog("pallino")))

    override def getPuppy(
      token: Token,
      wa: Int
    ): Future[Either[Nope, Dog]] = Future {
      println(token)
      if (token == Token("tokenone")) Right(Dog("pallino"))
      else Left(Nope("nope"))
    }
  }
}

object errors {
  import FailSupport._
  import controllers.Nope

  import io.circe.syntax._
  implicit def nopeToResponse = new ToHttpResponse[Nope] {
    def response(error: Nope) = HttpResponse(
      status = StatusCodes.UnprocessableEntity,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), error.asJson.noSpaces)
    )
  }
}

object Client extends App with ClientDerivationModule {
  import controllers._
  import autowire._

  val config = Config("localhost", 8080)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val doghouseClient = deriveClientContext[DoghouseApi]
  val rpcClient = new RPCClient(config, doghouseClient)

  val res = rpcClient[DoghouseApi].getPuppy(1).call()

  res map (println(_))
}

object Server extends App with RouterDerivationModule {
  import controllers._
  import models._
  import errors._
  import FailSupport._

  val doghouseRouter = deriveRouter[DoghouseApi](new DoghouseApiImpl)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val rpcServer = new HttpRPCServer(
    config = Config("localhost", 8080),
    routers = List(doghouseRouter)
  )
}

object models {
  case class Dog(name: String)
  case class Kitten(name: String)
}
