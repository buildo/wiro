package wiro.client

import io.circe.Json

package object akkaHttp {
  trait WiroDecoder[A] {
    def decode(j: Json): A
  }
}
