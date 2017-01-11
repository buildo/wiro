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
case class Kitten(name: String)
trait CathouseApi {
  def getKitten(name: String): Future[Kitten]
}

object CathouseApiImpl with CathouseApi {
  def getKitten(name: String) = Future(Kitten(name = name))
}
```

Run server:

```scala
implicit object CathouseRouter extends RouteGenerator[CathouseApiImpl.type] {
  val routes = route[CathouseApi](CathouseApiImpl)
  val tp = typePath[CathouseApi]
}

val rpcServer = new HttpRPCServer(
  config = ServerConfig("localhost", 8080),
  controllers = Seq(CoghouseApiImpl)
)
```

Have a look at `examples/src/main/scala` for a working example.
