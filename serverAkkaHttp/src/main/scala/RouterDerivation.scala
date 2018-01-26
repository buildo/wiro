package wiro
package server.akkaHttp

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.reflect.macros.Universe

import wiro.annotation.path

import io.circe.Printer

trait RouterDerivationModule extends PathMacro with MetaDataMacro with TypePathMacro {
  def deriveRouter[A](a: A): Router = macro RouterDerivationMacro.deriveRouterImpl[A]
  def deriveRouter[A](a: A, printer: Printer): Router = macro RouterDerivationMacro.deriveRouterImplPrinter[A]
}

object RouterDerivationMacro extends RouterDerivationModule {
  //val is required to make universe public
  class MacroHelper[U <: Universe](val universe: U) {
    import universe._

    //check only annotations of path type
    //Since universe is public Tree type can be returned
    def derivePath(tpe: Type): Tree = tpe.typeSymbol.annotations.collectFirst {
      case pathAnnotation if pathAnnotation.tree.tpe <:< weakTypeOf[path] =>
        q"override val path = derivePath[$tpe]"
    }.getOrElse(EmptyTree)
  }

  def deriveRouterImpl[A: c.WeakTypeTag](c: Context)(a: c.Expr[A]): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[A]
    val derivePath = new MacroHelper[c.universe.type](c.universe).derivePath(tpe)

    q"""
    import wiro.{ OperationType, MethodMetaData }

    new Router {
      override val routes = route[$tpe]($a)
      override val methodsMetaData = deriveMetaData[$tpe]
      override val tp = typePath[$tpe]
      $derivePath
    }
    """
  }

  def deriveRouterImplPrinter[A: c.WeakTypeTag](c: Context)(a: c.Expr[A], printer: c.Expr[Printer]): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[A]
    val derivePath = new MacroHelper[c.universe.type](c.universe).derivePath(tpe)

    q"""
    import wiro.{ OperationType, MethodMetaData }

    new _root_.wiro.server.akkaHttp.Router {
      override val routes = route[$tpe]($a)
      override val methodsMetaData = deriveMetaData[$tpe]
      override val tp = typePath[$tpe]
      override implicit val printer = $printer
      $derivePath
    }
    """
  }
}
