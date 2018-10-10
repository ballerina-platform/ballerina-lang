package com.github.gtache.lsp.client.languageserver.serverdefinition

import java.io.{InputStream, OutputStream}
import java.net.URI

import com.github.gtache.lsp.client.LanguageClientImpl
import com.github.gtache.lsp.client.connection.StreamConnectionProvider
import com.intellij.openapi.diagnostic.Logger

import scala.collection.mutable

object LanguageServerDefinition {
  val SPLIT_CHAR = ";"
  val allDefinitions: mutable.Set[LanguageServerDefinition] = mutable.Set()
  private val LOG: Logger = Logger.getInstance(LanguageServerDefinition.getClass)

  /**
    * @return All registered server definitions
    */
  def getAllDefinitions: mutable.Set[LanguageServerDefinition] = allDefinitions.clone()

  /**
    * Register a server definition
    *
    * @param definition The server definition
    */
  def register(definition: LanguageServerDefinition): Unit = {
    if (definition != null) {
      allDefinitions.add(definition)
      LOG.info("Added definition for " + definition)
    } else {
      LOG.warn("Trying to add a null definition")
    }
  }
}

/**
  * A trait representing a ServerDefinition
  */
trait LanguageServerDefinition {

  import LanguageServerDefinition.LOG

  private val mappedExtensions: mutable.Set[String] = mutable.Set(ext)
  private val streamConnectionProviders: mutable.Map[String, StreamConnectionProvider] = mutable.Map()

  /**
    * @return The extension that the language server manages
    */
  def ext: String

  /**
    * @return The id of the language server (same as extension)
    */
  def id: String = ext


  /**
    * Starts a Language server for the given directory and returns a tuple (InputStream, OutputStream)
    *
    * @param workingDir The root directory
    * @return The input and output streams of the server
    */
  def start(workingDir: String): (InputStream, OutputStream) = {
    streamConnectionProviders.get(workingDir) match {
      case Some(streamConnectionProvider) =>
        (streamConnectionProvider.getInputStream, streamConnectionProvider.getOutputStream)
      case None =>
        val streamConnectionProvider = createConnectionProvider(workingDir)
        streamConnectionProvider.start()
        streamConnectionProviders.put(workingDir, streamConnectionProvider)
        (streamConnectionProvider.getInputStream, streamConnectionProvider.getOutputStream)
    }
  }

  /**
    * Stops the Language server corresponding to the given working directory
    *
    * @param workingDir The root directory
    */
  def stop(workingDir: String): Unit = {
    streamConnectionProviders.get(workingDir) match {
      case Some(s) => s.stop()
        streamConnectionProviders.remove(workingDir)
      case None => LOG.warn("No connection for workingDir " + workingDir + " and ext " + ext)
    }
  }

  /**
    * Adds a file extension for this LanguageServer
    *
    * @param ext the extension
    */
  def addMappedExtension(ext: String): Unit = {
    mappedExtensions.add(ext)
  }

  /**
    * Removes a file extension for this LanguageServer
    *
    * @param ext the extension
    */
  def removeMappedExtension(ext: String): Unit = {
    mappedExtensions.remove(ext)
  }

  /**
    * @return the extensions linked to this LanguageServer
    */
  def getMappedExtensions: mutable.Set[String] = mappedExtensions.clone()

  /**
    * @return the LanguageClient for this LanguageServer
    */
  def createLanguageClient: LanguageClientImpl = new LanguageClientImpl

  def getInitializationOptions(uri: URI): Any = null

  override def toString: String = "ServerDefinition for " + ext

  /**
    * Creates a StreamConnectionProvider given the working directory
    *
    * @param workingDir The root directory
    * @return The stream connection provider
    */
  protected def createConnectionProvider(workingDir: String): StreamConnectionProvider

}
