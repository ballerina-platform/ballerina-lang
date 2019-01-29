package com.github.gtache.lsp.client.languageserver.serverdefinition

import com.github.gtache.lsp.client.connection.{ProcessStreamConnectionProvider, StreamConnectionProvider}
import com.github.gtache.lsp.utils.Utils
import com.github.gtache.lsp.utils.coursier.CoursierImpl
import com.intellij.openapi.diagnostic.Logger

/**
  * Represents a ServerDefinition for a LanguageServer stored on a repository
  *
  * @param ext       The extension that the server manages
  * @param packge    The artifact id of the server
  * @param mainClass The main class of the server
  * @param args      The arguments to give to the main class
  */
case class ArtifactLanguageServerDefinition(ext: String, packge: String, mainClass: String, args: Array[String]) extends UserConfigurableServerDefinition {

  import ArtifactLanguageServerDefinition.typ

  override def createConnectionProvider(workingDir: String): StreamConnectionProvider = {
    val cp = CoursierImpl.resolveClasspath(packge)
    new ProcessStreamConnectionProvider(Seq("java", "-cp", cp, mainClass) ++ args, workingDir)
  }

  override def toString: String = super.toString + " " + typ + " : " + packge + " mainClass : " + mainClass + " args : " + args.mkString(" ")

  override def toArray: Array[String] = {
    Array(typ, ext, packge, mainClass) ++ args
  }

  override def equals(obj: Any): Boolean = obj match {
    case ArtifactLanguageServerDefinition(ext1, packge1, mainClass1, args1) =>
      ext == ext1 && packge == packge1 && mainClass == mainClass1 && args.toSeq == args1.toSeq
    case _ => false
  }

  override def hashCode(): Int = ext.hashCode + 3 * packge.hashCode + 7 * mainClass.hashCode + 11 * args.hashCode()

}

object ArtifactLanguageServerDefinition extends UserConfigurableServerDefinitionObject {
  private val LOG: Logger = Logger.getInstance(this.getClass)

  def fromArray(arr: Array[String]): ArtifactLanguageServerDefinition = {
    if (arr.head == typ) {
      val arrTail = arr.tail
      if (arrTail.length < 3) {
        LOG.warn("Not enough elements to translate into a ServerDefinition : " + arr.mkString(" ; "))
        null
      } else {
        ArtifactLanguageServerDefinition(arrTail.head, arrTail.tail.head, arrTail.tail.tail.head, if (arrTail.length > 3) Utils.parseArgs(arrTail.tail.tail.tail) else Array())
      }
    } else {
      null
    }
  }

  override def typ: String = "artifact"

  override def getPresentableTyp: String = "Artifact"
}
