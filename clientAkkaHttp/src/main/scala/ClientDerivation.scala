package wiro.client

import scala.reflect.macros.blackbox.Context
import scala.language.experimental.macros

trait ClientDerivationMacro {
  def deriveClientContext[A]: RPCClientContext[A] = macro ClientDerivationMacro.deriveClientContextImpl[A]
}

object ClientDerivationMacro extends ClientDerivationMacro {
  def deriveClientContextImpl[A: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[A]

    q"""
    new RPCClientContext[$tpe] {
      override val methodsMetaData = deriveMetaData[$tpe]
      override val tp = typePath[$tpe]
      override val path = derivePath[$tpe]
    }
    """
  }
}
