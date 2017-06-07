
## Server Example

```tut:silent
import scala.concurrent.Future

import wiro.server.akkaHttp.{ RouterDerivationModule, ToHttpResponse, FailSupport, HttpRPCServer }
import wiro.models.Config

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, ContentType, HttpEntity}
import akka.http.scaladsl.model.MediaTypes

import io.circe.generic.auto._

object models {
  case class Dog(name: String)
  case class Kitten(name: String)
}

object controllers {
  import models._
  import wiro.annotation._
  import FailSupport._

  case class Nope(msg: String)
  case class Wa(lol: String, bah: Int, dah: Int)

  // Controller interface
  @path("woff")
  trait DoghouseApi {
    @command(name = Some("puppy"))
    def getPuppy(
      wa: Int
    ): Future[Either[Nope, Dog]]

    @command(name = Some("pallino"))
    def getPallino(
      something: String
    ): Future[Either[Nope, Dog]]
  }

  // Controller implementation
  class DoghouseApiImpl() extends DoghouseApi {
    override def getPallino(
      something: String
    ): Future[Either[Nope, Dog]] = Future(Right(Dog("pallino")))

    override def getPuppy(
      wa: Int
    ): Future[Either[Nope, Dog]] = Future(Left{
      println(wa)
      Nope("Not doing that")
    })
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

// Creating the wiro server
object Server extends App with RouterDerivationModule {
  import controllers._
  import wiro.reflect._
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
```

## Requests Examples

Pallino:

```bash
curl -XPOST 'http://localhost:8080/woff/pallino' \
-d '{"something":"foo"}' \
-H "Content-Type: application/json"
```
`>> {"name":"pallino"}`

Puppy:

```bash
curl -XPOST 'http://localhost:8080/woff/puppy' \
-d '{"wa":"1"}' \
-H "Content-Type: application/json"
```

`>> {"msg":"Not doing that"}`

## Client Example

Add the following code:

```tut:silent
import wiro.client._

object Client extends App with ClientDerivationModule {
  import controllers._
  import autowire._
  import wiro.reflect._

  val config = Config("localhost", 8080)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val doghouseClient = deriveClientContext[DoghouseApi]
  val rpcClient = new RPCClient(config, doghouseClient)

  val res = rpcClient[DoghouseApi].getPuppy(1).call()

  res map (println(_))
}
```

You can the run this Client App to interact with the server defined above.
