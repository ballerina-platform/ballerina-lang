package com.github.gtache.lsp.client.languageserver.wrapper

import java.io.IOException
import java.util.concurrent.CompletableFuture

import com.github.gtache.lsp.client.languageserver.ServerStatus
import com.github.gtache.lsp.client.languageserver.requestmanager.RequestManager
import com.github.gtache.lsp.client.languageserver.serverdefinition.LanguageServerDefinition
import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.requests.Timeouts
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.eclipse.lsp4j.jsonrpc.messages.Message
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.{RegistrationParams, ServerCapabilities, UnregistrationParams}
import org.jetbrains.annotations.Nullable

/**
  * A LanguageServerWrapper represents a connection to a LanguageServer and manages starting / stopping it as well as  connecting / disconnecting documents to it
  */
trait LanguageServerWrapper {


  /**
    * Tells the wrapper if a request was timed out or not
    *
    * @param timeouts The type of timeout
    * @param success  if it didn't timeout
    */
  def notifyResult(timeouts: Timeouts, success: Boolean): Unit

  def notifySuccess(timeouts: Timeouts): Unit = notifyResult(timeouts, success = true)

  def notifyFailure(timeouts: Timeouts): Unit = notifyResult(timeouts, success = false)

  def getConnectedFiles: Iterable[String]

  /**
    * @return The current status of this server
    */
  def getStatus: ServerStatus

  /**
    * @return the server definition corresponding to this wrapper
    */
  def getServerDefinition: LanguageServerDefinition

  /**
    * @return the project corresponding to this wrapper
    */
  def getProject: Project

  /**
    * Register a capability for the language server
    *
    * @param params The registration params
    * @return
    */
  def registerCapability(params: RegistrationParams): CompletableFuture[Void]

  /**
    * Unregister a capability for the language server
    *
    * @param params The unregistration params
    * @return
    */
  def unregisterCapability(params: UnregistrationParams): CompletableFuture[Void]

  /**
    * Returns the EditorEventManager for a given uri
    *
    * @param uri the URI as a string
    * @return the EditorEventManager (or null)
    */
  def getEditorManagerFor(uri: String): EditorEventManager

  /**
    * @return The request manager for this wrapper
    */
  def getRequestManager: RequestManager

  /**
    * Starts the LanguageServer
    */
  def start(): Unit

  /**
    * @return whether the underlying connection to language languageServer is still active
    */
  def isActive: Boolean

  /**
    * Connects an editor to the languageServer
    *
    * @param editor the editor
    */
  @throws[IOException]
  def connect(editor: Editor): Unit

  /**
    * Disconnects an editor from the LanguageServer
    *
    * @param path The uri of the editor
    */
  def disconnect(path: String): Unit

  /**
    * Checks if the wrapper is already connected to the document at the given path
    */
  def isConnectedTo(location: String): Boolean

  /**
    * @return the LanguageServer
    */
  @Nullable def getServer: LanguageServer

  /**
    * @return the languageServer capabilities, or null if initialization job didn't complete
    */
  @Nullable def getServerCapabilities: ServerCapabilities

  /**
    * @return The language ID that this wrapper is dealing with if defined in the content type mapping for the language languageServer
    */
  @Nullable def getLanguageId(contentTypes: Array[String]): String

  def logMessage(message: Message): Unit

  /**
    * Stops the wrapper
    */
  def stop(): Unit

  /**
    * Notifies the wrapper that the server has crashed / stopped unexpectedly
    *
    * @param e The exception returned
    */
  def crashed(e: Exception): Unit

  def removeWidget(): Unit


}
