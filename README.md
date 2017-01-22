# Wiro!

Wiro is a library for generating HTTP routes from traits.

## What's wrong with routers?

After years spent writing routers we realized that routes were merely a one-to-one mapping with controllers' methods.

## RPC?

Wiro exposes operations to manipulate data using HTTP as a transport protocol.

This is sometimes referred to as *WYGOPIAO*: What You GET Or POST Is An Operation, and it's closly related to RPC.

## Example

```scala
case class Dog(name: String)

case object ParentalControlError

trait DoghouseApi {
  @token
  @command
  def getPuppy(
    puppyName: String
  ): Future[Either[ParentalControlError.type, Dog]]
}

object DoghouseApiImpl extends DoghouseApi {
  @token
  @command
  override def getPuppy(
    puppyName: String
  ): Future[Either[ParentalControlError.type, Dog]] = Future(Left(ParentalControlError))
}

implicit def DoghouseRouter = new RouteGenerator[DoghouseApiImpl.type] {
  val routes = route[DoghouseApi](DoghouseApiImpl)
  val tp = typePath[DoghouseApi]
}

implicit def parentalControlResponse = new ToHttpResponse[ParentalControlError.type] {
  def response = HttpResponse(
    status = StatusCodes.BlockedByParentalControls,
    entity = "Don't do that!"
  )
}

implicit val system = ActorSystem()
implicit val materializer = ActorMaterializer()

val rpcServer = new HttpRPCServer(
  config = ServerConfig("localhost", 8080),
  controllers = List(DoghouseApiImpl)
)
```

```bash
curl -XPOST http://localhost:8080/DoghouseApi/getPuppy \
  -H "Content-Type: application/json" \
  -H "Authorization: Token token=sadasdsa" \
  -d '{"puppyName": "blabla"}'
> Don't do that!
```

Have a look at `examples/src/main/scala` for a working example.

## Missing Features

- [x] annotation syntax for providing authentication and specifying request type (`@auth def controllerMethod`, `@command def controllerMethod`)
- [x] currently both Get and Post are generated for each method (see branch commandsAndQueries for an attempt)
- [ ] Improved user handling. We are currently just returning the token


