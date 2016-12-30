package wiro

package object apps {
  trait DoghouseApi {
    def getPuppy(puppyName: String): Dog
    def getDogsNumber(): Int
  }

  case class Dog(
    name: String
  )
}
