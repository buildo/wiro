package wiro.server.akkaHttp

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros

import wiro.annotation.path

trait PathMacro {
  def derivePath[A]: String = macro PathMacro.derivePathImpl[A]
}

object PathMacro extends PathMacro {
  def derivePathImpl[A: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._

    val tpe = weakTypeOf[A].typeSymbol

    tpe.annotations.collectFirst {
      case pathAnnotation if pathAnnotation.tpe <:< c.weakTypeOf[path] =>
        pathAnnotation.tree.children.tail.head
    }.headOption match {
      case Some(name) => name
      case None => c.abort(c.enclosingPosition, s"\n\nMissing annotation @path(<name>) on $tpe\n")
    }

  }
}
