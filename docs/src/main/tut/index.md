---
layout: home
title:  "Home"
section: "home"
---

[ ![Download](https://api.bintray.com/packages/buildo/maven/wiro-http-server/images/download.svg) ](https://bintray.com/buildo/maven/wiro-http-server/_latestVersion)
[![Build Status](https://drone.our.buildo.io/api/badges/buildo/wiro/status.svg)](https://drone.our.buildo.io/buildo/wiro)

<a name="getting-started"></a>

{% include_relative getting-started.md %}

## Features

- Automatic generation of http routes from controllers
- Extensible error module

## What's wrong with routers?

We think routes should be a one-to-one mapping with controllers' methods.
Wiro exposes controllers' operations using HTTP as a transport protocol.

This is sometimes referred to as *WYGOPIAO*: What You GET Or POST Is An Operation, and it's closly related to RPC.

## Example

You can find a complete example project at https://github.com/buildo/wiro-example.

```scala
//models
case class NotFound(userId: Int)
case class User(id: Int, username: String)

//defines how to serialize an error
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import wiro.server.akkaHttp.FailSupport._
import wiro.server.akkaHttp.ToHttpResponse

implicit def notFoundToResponse: ToHttpResponse[NotFound] = error => HttpResponse(
    status = StatusCodes.NotFound,
    entity = s"User not found: ${error.userId}"
  )
  
//controllers interface and implementation

trait UserController {
  @command
  def update(id: Int, user: User): Future[Either[NotFound, Ok]]

  @query
  def find(id: Int): Future[Either[NotFound, User]]
}

class UserControllerImpl(implicit
  ec: ExecutionContext
) extends UserController {
  @command
  def update(id: Int, user: User): Future[Either[NotFound, Ok]] = Future { Right(Ok("happy update")) }

  @query
  def find(id: Int): Future[Either[NotFound, User]] = Future { NotFound(id) }
}

val userController = new UserControllerImpl(): UserController

implicit def UserRouter = new RouteGenerator[UserController] {
  val routes = route[UserController](userController)
  val tp = typePath[UserController]
}

val rpcServer = new HttpRPCServer(
  config = ServerConfig("localhost", 8080),
  controllers = List(userController)
)
```

## Requests Examples

Update user:

```bash
curl -XPOST 'http://localhost:8080/UserController/update' \
-d '{"user": {"id": 3, "username": "Babo"}, "id": 2}' \
-H "Content-Type: application/json"
```
`>> {"msg":"happy update"}`

Find user:

```bash
curl 'http://localhost:8080/UserController/find?id=3'
```

`>> {"id":1,"username":"Pippo"}`

## Specifying the controller path
By default wiro uses the controller name in the generated path, as in `/UserController/find`.

You can override this default by annotating the UserController with `@path`:

```scala
import wiro.annotation.path

@path("custom")
trait UserController {
  // ...
}
```

then overriding the `path` field in the `RouteGenerator` definition:

```scala
implicit def UserRouter = new RouteGenerator[UserController] {
  val routes = route[UserController](userController)
  val tp = typePath[UserController]
  override val path = derivePath[UserController]
}
```

Try this out:

```bash
curl 'http://localhost:8080/custom/find?id=3'
```
