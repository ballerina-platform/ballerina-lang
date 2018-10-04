package com.github.gtache.lsp.client.languageserver

import org.eclipse.lsp4j._

/**
  * Class containing the options of the language server
  *
  * @param syncKind                        The type of synchronization
  * @param completionOptions               The completion options
  * @param signatureHelpOptions            The signatureHelp options
  * @param codeLensOptions                 The codeLens options
  * @param documentOnTypeFormattingOptions The onTypeFormatting options
  * @param documentLinkOptions             The link options
  * @param executeCommandOptions           The execute options
  */
case class ServerOptions(syncKind: TextDocumentSyncKind, completionOptions: CompletionOptions, signatureHelpOptions: SignatureHelpOptions, codeLensOptions: CodeLensOptions, documentOnTypeFormattingOptions: DocumentOnTypeFormattingOptions, documentLinkOptions: DocumentLinkOptions, executeCommandOptions: ExecuteCommandOptions) {

}
