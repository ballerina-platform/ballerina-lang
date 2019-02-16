package com.github.gtache.lsp.client.languageserver.serverdefinition

import com.github.gtache.lsp.utils.Utils

object RawCommandServerDefinition extends UserConfigurableServerDefinitionObject {
  /**
    * Transforms an array of string into the corresponding UserConfigurableServerDefinition
    *
    * @param arr The array
    * @return The server definition
    */
  override def fromArray(arr: Array[String]): CommandServerDefinition = {
    if (arr.head == typ) {
      val arrTail = arr.tail
      if (arrTail.length > 1) {
        RawCommandServerDefinition(arrTail.head, Utils.parseArgs(arrTail.tail))
      } else {
        null
      }
    } else {
      null
    }
  }

  override def typ = "rawCommand"

  override def getPresentableTyp = "Raw command"
}

/**
  * A class representing a raw command to launch a languageserver
  *
  * @param ext     The extension
  * @param command The command to run
  */
case class RawCommandServerDefinition(ext: String, command: Array[String]) extends CommandServerDefinition {

  import RawCommandServerDefinition.typ

  /**
    * @return The array corresponding to the server definition
    */
  override def toArray: Array[String] = Array(typ, ext) ++ command

  override def toString: String = typ + " : " + command.mkString(" ")

  override def equals(obj: scala.Any): Boolean = obj match {
    case RawCommandServerDefinition(ext1, commands1) =>
      ext == ext1 && command.toSeq == commands1.toSeq
    case _ => false
  }

  override def hashCode(): Int = ext.hashCode + 3 * command.hashCode()

}
