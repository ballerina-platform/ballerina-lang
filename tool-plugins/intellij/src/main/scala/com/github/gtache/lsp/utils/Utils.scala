package com.github.gtache.lsp.utils

import java.util
import java.util.ResourceBundle

import com.intellij.openapi.diagnostic.Logger

import scala.annotation.varargs

/**
  * Object containing some useful methods for the plugin
  */
object Utils {

  val bundle: ResourceBundle = ResourceBundle.getBundle("com.github.gtache.lsp.LSPBundle")
  val lineSeparator: String = System.getProperty("line.separator")

  private val LOG: Logger = Logger.getInstance(Utils.getClass)

  /**
    * Transforms an array into a string (using mkString, useful for Java)
    *
    * @param arr The array
    * @param sep A separator
    * @return The result of mkString
    */
  def arrayToString(arr: Array[Any], sep: String = ""): String = {
    arr.mkString(sep)
  }

  /**
    * Concatenate multiple arrays
    *
    * @param arr The arrays
    * @return The concatenated arrays
    */
  @varargs def concatenateArrays(arr: Array[Any]*): Array[Any] = {
    arr.flatten.toArray
  }

  def stringToList(str: String, sep: String = lineSeparator): util.List[String] = {
    import scala.collection.JavaConverters._
    str.split(sep).toIndexedSeq.asJava
  }


}
