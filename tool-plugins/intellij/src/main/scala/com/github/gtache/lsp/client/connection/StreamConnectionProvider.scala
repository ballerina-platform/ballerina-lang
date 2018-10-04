/* Adapted from lsp4e */
package com.github.gtache.lsp.client.connection

import java.io.{IOException, InputStream, OutputStream}
import java.net.URI

import org.eclipse.lsp4j.jsonrpc.messages.Message
import org.eclipse.lsp4j.services.LanguageServer

/**
  * A class representing a stream connection
  */
trait StreamConnectionProvider {
  @throws[IOException]
  def start(): Unit

  def getInputStream: InputStream

  def getOutputStream: OutputStream

  /**
    * User provided initialization options.
    */
  def getInitializationOptions(rootUri: URI): Any = null

  def stop(): Unit

  /**
    * Allows to hook custom behavior on messages.
    *
    * @param message        a message
    * @param languageServer the language server receiving/sending the message.
    * @param rootURI
    */
  def handleMessage(message: Message, languageServer: LanguageServer, rootURI: URI): Unit = {
  }
}
