package wiro.server.akkaHttp

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros
import wiro.annotation.path

import RouteGenerators._

trait RouterDerivationModule {
  def deriveRouter[A](a: A): RouteGenerator[A] = macro RouterDerivationMacro.deriveRouterImpl[A]
}

object RouterDerivationMacro extends RouterDerivationModule {
  def deriveRouterImpl[A: c.WeakTypeTag](c: Context)(a: c.Expr[A]): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[A]

    //check only annotations of path type
    val pathAnnotated = tpe.typeSymbol.annotations.collectFirst {
      case pathAnnotation if pathAnnotation.tpe <:< c.weakTypeOf[path] => pathAnnotation
    }

    val derivePath = pathAnnotated match {
      case None => EmptyTree
      case _ => q"override val path = derivePath[$tpe]"
    }

    q"""
    import wiro.server.akkaHttp.{ OperationType, AuthenticationType, MethodMetaData }

    new RouteGenerator[$tpe] {
      override val routes = route[$tpe]($a)
      override val methodsMetaData = deriveMetaData[$tpe]
      override val tp = typePath[$tpe]
      $derivePath
    }
    """
  }
}
