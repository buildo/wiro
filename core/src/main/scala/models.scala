package wiro

import io.circe._

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

  case object Command
  case object Query

  type Command = Command.type
  type Query = Query.type

  trait Codecs {
    implicit val encodeCommand: Encoder[Command] = new Encoder[Command] {
        final def apply(a: Command): Json =
        Json.obj("object" -> Json.fromString(s"${a.productPrefix}"))
    }

    implicit val decodeCommand: Decoder[Command] =
      Decoder.instance { hCursor =>
        hCursor.get[String]("object") match {
          case Right("Command") =>
            Right(Command)
          case Left(DecodingFailure(a, b)) =>
            Left(DecodingFailure(a, b))
          case _ =>
            Left(DecodingFailure("Unmatched object", hCursor.history))
        }
      }

    implicit val encodeQuery: Encoder[Query] = new Encoder[Query] {
      final def apply(a: Query): Json =
        Json.obj("object" -> Json.fromString("${a.productPrefix}"))
    }

    implicit val decodeQuery: Decoder[Query] =
      Decoder.instance { hCursor =>
        hCursor.get[String]("object") match {
          case Right("Query") => Right(Query)
          case Left(DecodingFailure(a, b)) =>
            Left(DecodingFailure(a, b))
          case _ =>
            Left(DecodingFailure("Unmatched object", hCursor.history))
        }
      }
  }
}
