package wiro
package server.akkaHttp

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

import wiro.annotation.path

trait RouterDerivationModule extends PathMacro with MetaDataMacro with TypePathMacro {
  def deriveRouter[A](a: A): Router = macro RouterDerivationMacro.deriveRouterImpl[A]
}

object RouterDerivationMacro extends RouterDerivationModule {
  def deriveRouterImpl[A: c.WeakTypeTag](c: Context)(a: c.Expr[A]): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[A]

    //check only annotations of path type
    val pathAnnotated = tpe.typeSymbol.annotations.collectFirst {
      case pathAnnotation if pathAnnotation.tree.tpe <:< c.weakTypeOf[path] => pathAnnotation
    }

    val derivePath = pathAnnotated match {
      case None => EmptyTree
      case _ => q"override val path = derivePath[$tpe]"
    }

    q"""
    import wiro.{ OperationType, MethodMetaData, AuthenticationType }

    new Router {
      override val routes = route[$tpe]($a)
      override val methodsMetaData = deriveMetaData[$tpe]
      override val tp = typePath[$tpe]
      $derivePath
    }
    """
  }
}
