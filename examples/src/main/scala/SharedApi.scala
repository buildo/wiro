package wiro

package object apps {
  import wiro.annotation._

  trait DoghouseApi {
    def getPuppy(token: String, puppyName: String)
  }

  case class Dog(
    name: String
  )

  trait CathouseApi {
    def getKitten(kittenName: String): Kitten
  }

  case class Kitten(
    name: String
  )
}
