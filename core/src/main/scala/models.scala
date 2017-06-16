package wiro

import io.circe.Json

case class Config(
  host: String,
  port: Int
)

case class WiroRequest(
  args: String
)

case class RpcRequest(
  path: Seq[String],
  args: Map[String, Json]
)
