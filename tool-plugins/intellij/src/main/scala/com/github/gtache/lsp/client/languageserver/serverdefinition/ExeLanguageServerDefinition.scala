package com.github.gtache.lsp.client.languageserver.serverdefinition

import com.intellij.openapi.diagnostic.Logger

/**
  * Class representing server definitions corresponding to an executable file
  * This class is basically a more convenient way to write a RawCommand
  *
  * @param ext  The extension
  * @param path The path to the exe file
  * @param args The arguments for the exe file
  */
case class ExeLanguageServerDefinition(ext: String, path: String, args: Array[String]) extends CommandServerDefinition {

  import ExeLanguageServerDefinition.typ

  override def toArray: Array[String] = Array(typ, ext, path) ++ args

  override def toString: String = typ + " : path " + path + " args : " + args.mkString(" ")

  override def command: Array[String] = Array(path) ++ args

  override def equals(obj: scala.Any): Boolean = obj match {
    case ExeLanguageServerDefinition(ext1, path1, args1) =>
      ext == ext1 && path == path1 && args.toSeq == args1.toSeq
    case _ => false
  }

  override def hashCode(): Int = ext.hashCode + 3 * path.hashCode + 7 * args.hashCode()

}

object ExeLanguageServerDefinition extends UserConfigurableServerDefinitionObject {
  private val LOG: Logger = Logger.getInstance(this.getClass)

  override def fromArray(arr: Array[String]): ExeLanguageServerDefinition = {
    if (arr.head == typ) {
      val arrTail = arr.tail
      if (arrTail.length < 2) {
        LOG.warn("Not enough elements to translate into a ServerDefinition : " + arr)
        null
      } else {
        ExeLanguageServerDefinition(arrTail.head, arrTail.tail.head, if (arrTail.length > 2) arrTail.tail.tail else Array())
      }
    } else {
      null
    }
  }

  override def typ: String = "exe"

  override def getPresentableTyp: String = "Executable"
}