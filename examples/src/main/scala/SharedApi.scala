package wiro.apps

import scala.concurrent.Future

import wiro.models.Command
import wiro.annotation._

object interface {
  trait DoghouseApi {
    @auth
    @command
    def getPuppy(puppyName: String): Future[Dog]
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
