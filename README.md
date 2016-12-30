# Wiro!

RPC/http library for scala

## Example

Define your API:

```scala
case class Dog(name: String)
trait DoghouseApi {
  def getPuppy(puppyName: String): Dog
}
```

Server:

```scala
val rpcServer = new RPCServer(
  serverConfig = ServerConfig("localhost", 8080),
  apiImpl = DoghouseApiImpl
)

object DoghouseApiImpl extends AutowireRPCServer with DoghouseApi {
  def getPuppy(puppyName: String): Dog = Dog(name = puppyName)

  val routes = route[DoghouseApi](this)
}
```

Client:

```scala
val doghouse = new WiroClient(
  conf = ClientConfig("localhost", 8080),
  actorSystem = actorSystem,
  materializer = materializer
)[DoghouseApi]

val futureDog: Future[Dog] = doghouse.getPuppy("a").call()
```
