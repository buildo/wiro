# Wiro!

Wiro is a library for generating HTTP routes from traits.

## What's wrong with routers?

After years spent writing routers we realized that routes were merely a one-to-one mapping with controllers' methods.

## RPC?

Wiro exposes operations to manipulate data using HTTP as a transport protocol.
This is sometimes referred to as "WYGOPIAO": What You GET Or POST Is An Operation, and it's closly related to RPC.

## Example

Trait and implementation:

```scala
case class Dog(name: String)
trait DoghouseApi {
  def getPuppy(puppyName: String): Future[Dog]
}

object DoghouseApiImpl with DoghouseApi {
  def getPuppy(puppyName: String) = Future(Dog(name = puppyName))
}
```

Run server:

```scala
implicit object DoghouseRouter extends RouteGenerator[DoghouseApiImpl.type] {
  val routes = route[DoghouseApi](DoghouseApiImpl)
  val tp = typePath[DoghouseApi]
}

val rpcServer = new HttpRPCServer(
  config = ServerConfig("localhost", 8080),
  controllers = Seq(DoghouseApiImpl)
)
```

Have a look at `examples/src/main/scala` for a working example.
