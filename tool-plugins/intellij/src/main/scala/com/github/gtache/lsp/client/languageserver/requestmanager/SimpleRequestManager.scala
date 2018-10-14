package com.github.gtache.lsp.client.languageserver.requestmanager

import java.util
import java.util.concurrent.CompletableFuture

import com.github.gtache.lsp.client.languageserver.ServerStatus
import com.github.gtache.lsp.client.languageserver.wrapper.LanguageServerWrapper
import com.intellij.openapi.diagnostic.Logger
import org.eclipse.lsp4j._
import org.eclipse.lsp4j.jsonrpc.messages.CancelParams
import org.eclipse.lsp4j.services.{LanguageClient, LanguageServer, TextDocumentService, WorkspaceService}

/**
  * Basic implementation of a RequestManager which just passes requests from client to server and vice-versa
  */
class SimpleRequestManager(wrapper: LanguageServerWrapper, server: LanguageServer, client: LanguageClient, serverCapabilities: ServerCapabilities) extends RequestManager {

  private val textDocumentOptions = if (serverCapabilities.getTextDocumentSync.isRight) serverCapabilities.getTextDocumentSync.getRight else null
  private val workspaceService: WorkspaceService = server.getWorkspaceService
  private val textDocumentService: TextDocumentService = server.getTextDocumentService
  private val LOG: Logger = Logger.getInstance(classOf[SimpleRequestManager])

  //Client
  override def showMessage(messageParams: MessageParams): Unit = client.showMessage(messageParams)

  override def showMessageRequest(showMessageRequestParams: ShowMessageRequestParams): CompletableFuture[MessageActionItem] = client.showMessageRequest(showMessageRequestParams)

  override def logMessage(messageParams: MessageParams): Unit = client.logMessage(messageParams)

  override def telemetryEvent(o: Any): Unit = client.telemetryEvent(o)

  override def registerCapability(params: RegistrationParams): CompletableFuture[Void] = client.registerCapability(params)

  override def unregisterCapability(params: UnregistrationParams): CompletableFuture[Void] = client.unregisterCapability(params)

  override def applyEdit(params: ApplyWorkspaceEditParams): CompletableFuture[ApplyWorkspaceEditResponse] = client.applyEdit(params)

  override def publishDiagnostics(publishDiagnosticsParams: PublishDiagnosticsParams): Unit = client.publishDiagnostics(publishDiagnosticsParams)

  //General
  override def initialize(params: InitializeParams): CompletableFuture[InitializeResult] = {
    if (checkStatus) try {
      server.initialize(params)
    } catch {
      case e: Exception => crashed(e)
        null
    } else null
  }

  override def initialized(params: InitializedParams): Unit =
    if (checkStatus) try {
      server.initialized(params)
    } catch {
      case e: Exception => crashed(e)
    }

  override def shutdown: CompletableFuture[AnyRef] =
    if (checkStatus) try {
      server.shutdown()
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def exit(): Unit =
    if (checkStatus) try {
      server.exit()
    } catch {
      case e: Exception => crashed(e)
    }

  override def cancelRequest(params: CancelParams): Unit = {
  }

  //Workspace
  override def didChangeConfiguration(params: DidChangeConfigurationParams): Unit =
    if (checkStatus) try {
      workspaceService.didChangeConfiguration(params)
    } catch {
      case e: Exception => crashed(e)
    }

  override def didChangeWatchedFiles(params: DidChangeWatchedFilesParams): Unit =
    if (checkStatus) try {
      workspaceService.didChangeWatchedFiles(params)
    } catch {
      case e: Exception => crashed(e)
    }

  override def symbol(params: WorkspaceSymbolParams): CompletableFuture[java.util.List[_ <: SymbolInformation]] =
    if (checkStatus) try {
      if (serverCapabilities.getWorkspaceSymbolProvider) workspaceService.symbol(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def executeCommand(params: ExecuteCommandParams): CompletableFuture[AnyRef] =
    if (checkStatus) try {
      if (serverCapabilities.getExecuteCommandProvider != null) workspaceService.executeCommand(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  //TextDocument
  override def didOpen(params: DidOpenTextDocumentParams): Unit =
    if (checkStatus) try {
      if (textDocumentOptions == null || textDocumentOptions.getOpenClose) textDocumentService.didOpen(params)
    } catch {
      case e: Exception => crashed(e)
    }

  override def didChange(params: DidChangeTextDocumentParams): Unit =
    if (checkStatus) try {
      if (textDocumentOptions == null || textDocumentOptions.getChange != null) textDocumentService.didChange(params)
    } catch {
      case e: Exception => crashed(e)
    }

  override def willSave(params: WillSaveTextDocumentParams): Unit =
    if (checkStatus) try {
      if (textDocumentOptions == null || textDocumentOptions.getWillSave) textDocumentService.willSave(params)
    } catch {
      case e: Exception => crashed(e)
    }

  override def willSaveWaitUntil(params: WillSaveTextDocumentParams): CompletableFuture[java.util.List[TextEdit]] =
    if (checkStatus) try {
      if (textDocumentOptions == null || textDocumentOptions.getWillSaveWaitUntil) textDocumentService.willSaveWaitUntil(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def didSave(params: DidSaveTextDocumentParams): Unit =
    if (checkStatus) try {
      if (textDocumentOptions == null || textDocumentOptions.getSave != null) textDocumentService.didSave(params)
    } catch {
      case e: Exception => crashed(e)
    }

  override def didClose(params: DidCloseTextDocumentParams): Unit =
    if (checkStatus) try {
      if (textDocumentOptions == null || textDocumentOptions.getOpenClose) textDocumentService.didClose(params)
    } catch {
      case e: Exception => crashed(e)
    }

  override def completion(params: CompletionParams): CompletableFuture[jsonrpc.messages.Either[java.util.List[CompletionItem], CompletionList]] =
    if (checkStatus) try {
      if (serverCapabilities.getCompletionProvider != null) textDocumentService.completion(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def completionItemResolve(unresolved: CompletionItem): CompletableFuture[CompletionItem] =
    if (checkStatus) try {
      if (serverCapabilities.getCompletionProvider != null && serverCapabilities.getCompletionProvider.getResolveProvider) textDocumentService.resolveCompletionItem(unresolved) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def hover(params: TextDocumentPositionParams): CompletableFuture[Hover] =
    if (checkStatus) try {
      if (serverCapabilities.getHoverProvider) textDocumentService.hover(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  private def checkStatus: Boolean = wrapper.getStatus == ServerStatus.STARTED

  private def crashed(e: Exception): Unit = {
    LOG.warn(e)
    wrapper.crashed(e)
  }

  override def signatureHelp(params: TextDocumentPositionParams): CompletableFuture[SignatureHelp] =
    if (checkStatus) try {
      if (serverCapabilities.getSignatureHelpProvider != null) textDocumentService.signatureHelp(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def references(params: ReferenceParams): CompletableFuture[java.util.List[_ <: Location]] =
    if (checkStatus) try {
      if (serverCapabilities.getReferencesProvider) textDocumentService.references(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def documentHighlight(params: TextDocumentPositionParams): CompletableFuture[java.util.List[_ <: DocumentHighlight]] =
    if (checkStatus) try {
      if (serverCapabilities.getDocumentHighlightProvider) textDocumentService.documentHighlight(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def documentSymbol(params: DocumentSymbolParams): CompletableFuture[java.util.List[_ <: SymbolInformation]] =
    if (checkStatus) try {
      if (serverCapabilities.getDocumentSymbolProvider) textDocumentService.documentSymbol(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def formatting(params: DocumentFormattingParams): CompletableFuture[java.util.List[_ <: TextEdit]] =
    if (checkStatus) try {
      if (serverCapabilities.getDocumentFormattingProvider) textDocumentService.formatting(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def rangeFormatting(params: DocumentRangeFormattingParams): CompletableFuture[java.util.List[_ <: TextEdit]] =
    if (checkStatus) try {
      if (serverCapabilities.getDocumentRangeFormattingProvider) textDocumentService.rangeFormatting(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def onTypeFormatting(params: DocumentOnTypeFormattingParams): CompletableFuture[java.util.List[_ <: TextEdit]] =
    if (checkStatus) try {
      if (serverCapabilities.getDocumentOnTypeFormattingProvider != null) textDocumentService.onTypeFormatting(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def definition(params: TextDocumentPositionParams): CompletableFuture[java.util.List[_ <: Location]] =
    if (checkStatus) try {
      if (serverCapabilities.getDefinitionProvider) textDocumentService.definition(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def codeAction(params: CodeActionParams): CompletableFuture[java.util.List[_ <: Command]] =
    if (checkStatus) try {
      if (serverCapabilities.getCodeActionProvider) textDocumentService.codeAction(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def codeLens(params: CodeLensParams): CompletableFuture[java.util.List[_ <: CodeLens]] =
    if (checkStatus) try {
      if (serverCapabilities.getCodeLensProvider != null) textDocumentService.codeLens(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def resolveCodeLens(unresolved: CodeLens): CompletableFuture[CodeLens] =
    if (checkStatus) try {
      if (serverCapabilities.getCodeLensProvider != null && serverCapabilities.getCodeLensProvider.isResolveProvider) textDocumentService.resolveCodeLens(unresolved) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def documentLink(params: DocumentLinkParams): CompletableFuture[java.util.List[DocumentLink]] =
    if (checkStatus) try {
      if (serverCapabilities.getDocumentLinkProvider != null) textDocumentService.documentLink(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def documentLinkResolve(unresolved: DocumentLink): CompletableFuture[DocumentLink] =
    if (checkStatus) try {
      if (serverCapabilities.getDocumentLinkProvider != null && serverCapabilities.getDocumentLinkProvider.getResolveProvider) textDocumentService.documentLinkResolve(unresolved) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def rename(params: RenameParams): CompletableFuture[WorkspaceEdit] =
    if (checkStatus) try {
      if (serverCapabilities.getRenameProvider) textDocumentService.rename(params) else null
    } catch {
      case e: Exception => crashed(e)
        null
    } else null

  override def implementation(params: TextDocumentPositionParams): CompletableFuture[util.List[_ <: Location]] = throw new NotImplementedError()

  override def typeDefinition(params: TextDocumentPositionParams): CompletableFuture[util.List[_ <: Location]] = throw new NotImplementedError()

  override def documentColor(params: DocumentColorParams): CompletableFuture[util.List[ColorInformation]] = throw new NotImplementedError()

  override def colorPresentation(params: ColorPresentationParams): CompletableFuture[util.List[ColorPresentation]] = throw new NotImplementedError()
}
