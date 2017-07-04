package wiro
package client.akkaHttp

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

trait ClientDerivationModule extends TypePathMacro {
  def deriveClientContext[A]: RPCClientContext[A] = macro ClientDerivationMacro.deriveClientContextImpl[A]
}

object ClientDerivationMacro extends ClientDerivationModule {
  def deriveClientContextImpl[A: c.WeakTypeTag](c: Context): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[A]

    q"""
    import wiro.{ OperationType, MethodMetaData }

    new RPCClientContext[$tpe] {
      override val methodsMetaData = deriveMetaData[$tpe]
      override val tp = typePath[$tpe]
      override val path = derivePath[$tpe]
    }
    """
  }
}
