package wiro.apps

import scala.concurrent.Future

import wiro.models.Command

object interface {
  trait DoghouseApi {
    def getPuppy(action: Command, token: String, puppyName: String): Future[Dog]
  }

  case class Dog(
    name: String
  )

  trait CathouseApi {
    def getKitten(kittenName: String): Future[Kitten]
    def getKittens(number: Int): Future[List[Kitten]]
  }

  case class Kitten(
    name: String
  )
}
