package com.github.gtache.lsp.client.languageserver.requestmanager

import java.util.concurrent.CompletableFuture

import org.eclipse.lsp4j._
import org.eclipse.lsp4j.jsonrpc.messages.CancelParams

/**
  * Handles requests between server and client
  */
trait RequestManager {

  //Client
  def showMessage(messageParams: MessageParams): Unit

  def showMessageRequest(showMessageRequestParams: ShowMessageRequestParams): CompletableFuture[MessageActionItem]

  def logMessage(messageParams: MessageParams): Unit

  def telemetryEvent(o: Any): Unit

  def registerCapability(params: RegistrationParams): CompletableFuture[Void]

  def unregisterCapability(params: UnregistrationParams): CompletableFuture[Void]

  def applyEdit(params: ApplyWorkspaceEditParams): CompletableFuture[ApplyWorkspaceEditResponse]

  def publishDiagnostics(publishDiagnosticsParams: PublishDiagnosticsParams): Unit

  //Server
  //General
  def initialize(params: InitializeParams): CompletableFuture[InitializeResult]

  def initialized(params: InitializedParams): Unit

  def shutdown: CompletableFuture[AnyRef]

  def exit(): Unit

  def cancelRequest(params: CancelParams): Unit

  //Workspace
  def didChangeConfiguration(params: DidChangeConfigurationParams): Unit

  def didChangeWatchedFiles(params: DidChangeWatchedFilesParams): Unit

  def symbol(params: WorkspaceSymbolParams): CompletableFuture[java.util.List[_ <: SymbolInformation]]

  def executeCommand(params: ExecuteCommandParams): CompletableFuture[AnyRef]


  //Document
  def didOpen(params: DidOpenTextDocumentParams): Unit

  def didChange(params: DidChangeTextDocumentParams): Unit

  def willSave(params: WillSaveTextDocumentParams): Unit

  def willSaveWaitUntil(params: WillSaveTextDocumentParams): CompletableFuture[java.util.List[TextEdit]]

  def didSave(params: DidSaveTextDocumentParams): Unit

  def didClose(params: DidCloseTextDocumentParams): Unit

  def completion(params: CompletionParams): CompletableFuture[jsonrpc.messages.Either[java.util.List[CompletionItem], CompletionList]]

  def completionItemResolve(unresolved: CompletionItem): CompletableFuture[CompletionItem]

  def hover(params: TextDocumentPositionParams): CompletableFuture[Hover]

  def signatureHelp(params: TextDocumentPositionParams): CompletableFuture[SignatureHelp]

  def references(params: ReferenceParams): CompletableFuture[java.util.List[_ <: Location]]

  def documentHighlight(params: TextDocumentPositionParams): CompletableFuture[java.util.List[_ <: DocumentHighlight]]

  def documentSymbol(params: DocumentSymbolParams): CompletableFuture[java.util.List[_ <: SymbolInformation]]

  def formatting(params: DocumentFormattingParams): CompletableFuture[java.util.List[_ <: TextEdit]]

  def rangeFormatting(params: DocumentRangeFormattingParams): CompletableFuture[java.util.List[_ <: TextEdit]]

  def onTypeFormatting(params: DocumentOnTypeFormattingParams): CompletableFuture[java.util.List[_ <: TextEdit]]

  def definition(params: TextDocumentPositionParams): CompletableFuture[java.util.List[_ <: Location]]

  def codeAction(params: CodeActionParams): CompletableFuture[java.util.List[_ <: Command]]

  def codeLens(params: CodeLensParams): CompletableFuture[java.util.List[_ <: CodeLens]]

  def resolveCodeLens(unresolved: CodeLens): CompletableFuture[CodeLens]

  def documentLink(params: DocumentLinkParams): CompletableFuture[java.util.List[DocumentLink]]

  def documentLinkResolve(unresolved: DocumentLink): CompletableFuture[DocumentLink]

  def rename(params: RenameParams): CompletableFuture[WorkspaceEdit]

  def implementation(params: TextDocumentPositionParams): CompletableFuture[java.util.List[_ <: Location]]

  def typeDefinition(params: TextDocumentPositionParams): CompletableFuture[java.util.List[_ <: Location]]

  def documentColor(params: DocumentColorParams): CompletableFuture[java.util.List[ColorInformation]]

  def colorPresentation(params: ColorPresentationParams): CompletableFuture[java.util.List[ColorPresentation]]

}
