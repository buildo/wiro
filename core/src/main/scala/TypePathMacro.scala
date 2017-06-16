package wiro.server.akkaHttp

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros

import wiro.annotation.path

trait TypePathMacro {
  def typePath[A]: Seq[String] = macro TypePathMacro.typePathImpl[A]
}

object TypePathMacro extends TypePathMacro {
  def typePathImpl[A: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._

    val tpe = weakTypeOf[A].typeSymbol
    val path = tpe.fullName.toString.split('.').toSeq

    q"Seq(..$path)"
  }
}
