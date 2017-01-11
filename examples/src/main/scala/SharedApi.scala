package wiro.apps

object interface {
  trait DoghouseApi {
    def getPuppy(token: String, puppyName: String): Dog
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
