## Example

In this example we will build an api to store and retrieve users.

### 1 - Model and Controller Interface

First, let's start by defining the interface of the API controller. We want the API to support the two following
operations:

1. The client should be able to insert a new user by specifying `id` and `name`
2. The client should be able to retrieve a user by `id`

The following snippet defines a model for `User` and an interface that follows this specification.

```scala
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

```
