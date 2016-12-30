package wiro.models

case class ClientConfig(
  host: String,
  port: Int
)

case class ServerConfig(
  host: String,
  port: Int
)

case class RpcRequest(
  path: Seq[String],
  args: String
)
