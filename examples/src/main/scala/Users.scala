import scala.concurrent.Future

// Models definition
object models {
  case class User(name: String)
}

object controllers {
  import models._
  import wiro.annotation._

  // Error message
  case class Error(msg: String)

  // API interface
  @path("users")
  trait UsersApi {

    @query(name = Some("getUser"))
    def getUser(
      id: Int
    ): Future[Either[Error, User]]

    @command(name = Some("insertUser"))
    def insertUser(
      id: Int,
      name: String
    ): Future[Either[Error, User]]
  }
}
