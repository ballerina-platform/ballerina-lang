package com.github.gtache.lsp.client

import java.util
import java.util.concurrent.{CompletableFuture, FutureTask}

import com.github.gtache.lsp.client.languageserver.wrapper.LanguageServerWrapper
import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.requests.WorkspaceEditHandler
import com.github.gtache.lsp.utils.{ApplicationUtils, FileUtils}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.UIUtil
import org.eclipse.lsp4j._
import org.eclipse.lsp4j.services.{LanguageClient, LanguageServer}

import scala.collection.JavaConverters._


/**
  * Implementation of the LanguageClient
  */
class LanguageClientImpl extends LanguageClient {
  private val LOG: Logger = Logger.getInstance(classOf[LanguageClientImpl])
  private var server: LanguageServer = _
  private var wrapper: LanguageServerWrapper = _

  /**
    * Connects the LanguageClient to the server
    *
    * @param server The LanguageServer
    */
  def connect(server: LanguageServer, wrapper: LanguageServerWrapper): Unit = {
    this.server = server
    this.wrapper = wrapper
  }

  override def applyEdit(params: ApplyWorkspaceEditParams): CompletableFuture[ApplyWorkspaceEditResponse] = {
    CompletableFuture.supplyAsync(() => {
      new ApplyWorkspaceEditResponse(WorkspaceEditHandler.applyEdit(params.getEdit))
    })
  }

  override def configuration(configurationParams: ConfigurationParams): CompletableFuture[util.List[AnyRef]] = super.configuration(configurationParams)

  override def workspaceFolders(): CompletableFuture[util.List[WorkspaceFolder]] = super.workspaceFolders()

  override def registerCapability(params: RegistrationParams): CompletableFuture[Void] = wrapper.registerCapability(params)

  override def unregisterCapability(params: UnregistrationParams): CompletableFuture[Void] = wrapper.unregisterCapability(params)

  override def telemetryEvent(o: Any): Unit = {
    //TODO
  }

  override def publishDiagnostics(publishDiagnosticsParams: PublishDiagnosticsParams): Unit = {
    val uri = FileUtils.sanitizeURI(publishDiagnosticsParams.getUri)
    val diagnostics = publishDiagnosticsParams.getDiagnostics
    EditorEventManager.forUri(uri).foreach(e => e.diagnostics(diagnostics.asScala))
  }

  override def showMessage(messageParams: MessageParams): Unit = {
    val title = "Language Server message"
    val message = messageParams.getMessage
    ApplicationUtils.invokeLater(() => messageParams.getType match {
      case MessageType.Error => Messages.showErrorDialog(message, title)
      case MessageType.Warning => Messages.showWarningDialog(message, title)
      case MessageType.Info => Messages.showInfoMessage(message, title)
      case MessageType.Log => Messages.showInfoMessage(message, title)
      case _ => LOG.warn("No message type for " + message)
    })
  }

  override def showMessageRequest(showMessageRequestParams: ShowMessageRequestParams): CompletableFuture[MessageActionItem] = {
    val actions = showMessageRequestParams.getActions
    val title = "Language Server message"
    val message = showMessageRequestParams.getMessage
    val icon = showMessageRequestParams.getType match {
      case MessageType.Error => UIUtil.getErrorIcon
      case MessageType.Warning => UIUtil.getWarningIcon
      case MessageType.Info => UIUtil.getInformationIcon
      case MessageType.Log => UIUtil.getInformationIcon
      case _ =>
        LOG.warn("No message type for " + message)
        null
    }

    val task = new FutureTask[Int](() => Messages.showDialog(message, title, actions.asScala.map(a => a.getTitle).toArray, 0, icon))
    ApplicationManager.getApplication.invokeAndWait(task)
    val exitCode = task.get()

    CompletableFuture.completedFuture(new MessageActionItem(actions.get(exitCode).getTitle))
  }

  override def logMessage(messageParams: MessageParams): Unit = {
    val message = messageParams.getMessage
    messageParams.getType match {
      case MessageType.Error =>
        LOG.error(message)
      case MessageType.Warning =>
        LOG.warn(message)
      case MessageType.Info =>
        LOG.info(message)
      case MessageType.Log =>
        LOG.debug(message)
      case _ =>
        LOG.warn("Unknown message type for " + message)
    }
  }
}
