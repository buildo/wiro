package wiro

package object models {

  case class ClientConfig(
    host: String,
    port: Int
  )

  case class ServerConfig(
    host: String,
    port: Int
  )

  case class WiroRequest(
    args: String
  )

  case class RpcRequest(
    path: Seq[String],
    args: Map[String, io.circe.Json]
  )

  object CommandSingleton
  object QuerySingleton

  type Command = CommandSingleton.type
  type Query = QuerySingleton.type
}
