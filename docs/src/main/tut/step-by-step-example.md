## Example

In this example we will build an api to store and retrieve users.

### 1 - Model and Controller Interface

First, let's start by defining the interface of the API controller. We want the API to support the two following
operations:

1. The client should be able to insert a new user by specifying `id` and `name`
2. The client should be able to retrieve a user by `id`

The following snippet defines a model for `User` and an interface that follows this specification.

```tut:silent
import scala.concurrent.Future
import wiro.annotation._

// Models definition
object models {
  case class User(name: String)
}

trait ControllersInterfaces {
  import models._

  // Error messages
  case class Error(msg: String)
  case class UserNotFoundError(msg: String)

  // API interface
  @path("users")
  trait UsersApi {

    @query
    def getUser(
      id: Int
    ): Future[Either[UserNotFoundError, User]]

    @command
    def insertUser(
      id: Int,
      name: String
    ): Future[Either[Error, User]]
  }
}

```
* Use the `@query` and `@command` annotations to handle `GET` and `POST` requests respectively.
* Return types must be `Future` of `Either` like above

### 2 - Controller implementation

Now that we have the API interface, we need to implement it. Let's add the following implementation
inside the `controllers` object:

```tut:silent
object controllers extends ControllersInterfaces {
  import models.User
  import scala.concurrent.ExecutionContext.Implicits.global

  val users = collection.mutable.Map.empty[Int, User] // Users DB

  // API implementation
  class UsersApiImpl() extends UsersApi {
    override def getUser(
      id: Int
    ): Future[Either[UserNotFoundError, User]] = {
      users.get(id) match {
        case Some(user) => Future(Right(user))
        case None => Future(Left(UserNotFoundError("User not found")))
      }
    }

    override def insertUser(
      id: Int,
      name: String
    ): Future[Either[Error, User]] = {
      val newUser = User(name)
      users(id) = newUser
      Future(Right(newUser))
    }
  }
}
```
### 3 - Serialization and deserialization

Requests and responses composed only by standard types can be serialized and deserialized automatically by [circe](https://github.com/circe/circe), thanks to the following import:

```tut:silent
import io.circe.generic.auto._
```

We need, however, to specify how we want the errors that we defined to be converted into HTTP responses. To do it, it is sufficient to define the corresponding implicits, like we do in the following snippet:

```tut:silent
import wiro.server.akkaHttp.ToHttpResponse
import wiro.server.akkaHttp.FailSupport._

import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, ContentType, HttpEntity}
import akka.http.scaladsl.model.MediaTypes

object errors {
  import controllers.UserNotFoundError

  import io.circe.syntax._
  implicit def notFoundToResponse = new ToHttpResponse[UserNotFoundError] {
    def response(error: UserNotFoundError) = HttpResponse(
      status = StatusCodes.NotFound,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), error.asJson.noSpaces)
    )
  }

  import controllers.Error
  implicit def errorToResponse = new ToHttpResponse[Error] {
    def response(error: Error) = HttpResponse(
      status = StatusCodes.InternalServerError,
      entity = HttpEntity(ContentType(MediaTypes.`application/json`), error.asJson.noSpaces)
    )
  }
}
```

### 4 - Router creation

Now we have everithing we need to instance and start the router:

```tut:silent
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import wiro.Config
import wiro.server.akkaHttp._

object UsersServer extends App with RouterDerivationModule {
  import controllers._
  import models._
  import errors._

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val usersRouter = deriveRouter[UsersApi](new UsersApiImpl)

  val rpcServer = new HttpRPCServer(
    config = Config("localhost", 8080),
    routers = List(usersRouter)
  )
}
```

### 5 - Requests examples

Inserting a user:

```bash
curl -XPOST 'http://localhost:8080/users/insertUser' \
-d '{"id":0, "name":"Pippo"}' \
-H "Content-Type: application/json"
```

`>> {"name":"Pippo"}`

Getting a user:
```bash
curl -XGET 'http://localhost:8080/users/getUser?id=0'
```

`>> {"name":"Pippo"}`

### 6 - Wiro client

With wiro you can also create clients and perform requests:

```tut:silent
import wiro.client.akkaHttp._

object UsersClient extends App with ClientDerivationModule {
  import controllers._
  import autowire._

  val config = Config("localhost", 8080)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val rpcClient = new RPCClient(config, deriveClientContext[UsersApi])

  rpcClient[UsersApi].insertUser(0, "Pippo").call() map (println(_))
}
```

### 7 - Testing

To write tests for the router you can use the Akka [Route Testkit](http://doc.akka.io/docs/akka-http/current/scala/http/routing-dsl/testkit.html). The router to be tested can be extracted from the object that we defined above, as follows:

```tut:silent
import org.scalatest.{ Matchers, FlatSpec }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import models.User
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._

class UserSpec extends FlatSpec with Matchers with ScalatestRouteTest {
  lazy val route = UsersServer.usersRouter.buildRoute

  it should "get a user" in {
    Get("/users/getUser?id=0") ~> route ~> check {
      responseAs[User].name shouldBe "Pippo"
    }
  }

}

```


