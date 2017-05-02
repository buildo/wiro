package wiro.server.akkaHttp

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros

import RouteGenerators._

trait RouterDerivationMacro {
  def deriveRouter[A](a: A): RouteGenerator[A] = macro RouterDerivationMacro.deriveRouterImpl[A]
}

object RouterDerivationMacro extends RouterDerivationMacro {
  def deriveRouterImpl[A: c.WeakTypeTag](c: Context)(a: c.Expr[A]): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[A]

    q"""
    new RouteGenerator[$tpe] {
      override val routes = route[$tpe]($a)
      override val methodsMetaData = deriveMetaData[$tpe]
      override val tp = typePath[$tpe]
      override val path = derivePath[$tpe]
    }
    """
  }
}
