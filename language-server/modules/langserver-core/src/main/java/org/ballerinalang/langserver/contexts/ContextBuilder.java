/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.contexts;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.BallerinaDefinitionContext;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.DocumentSymbolContext;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.FoldingRangeContext;
import org.ballerinalang.langserver.commons.HoverContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.PrepareRenameContext;
import org.ballerinalang.langserver.commons.ReferencesContext;
import org.ballerinalang.langserver.commons.RenameContext;
import org.ballerinalang.langserver.commons.SemanticTokensContext;
import org.ballerinalang.langserver.commons.SignatureContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelpCapabilities;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

/**
 * Builds the language server operation contexts on demand.
 *
 * @since 2.0.0
 */
public class ContextBuilder {

    private ContextBuilder() {
    }

    /**
     * Build the did open context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param operation        language server operation
     * @param serverContext    language server context
     * @return {@link DocumentServiceContext} base context generated
     */
    public static DocumentServiceContext buildDocumentServiceContext(String uri, WorkspaceManager workspaceManager,
                                                                     LSContextOperation operation,
                                                                     LanguageServerContext serverContext) {
        return new BaseContextImpl.BaseContextBuilder(operation, serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .build();
    }

    /**
     * Build the did open context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param operation        language server operation
     * @param serverContext    language server context
     * @param cancelChecker    cancellation checker
     * @return {@link DocumentServiceContext} base context generated
     */
    public static DocumentServiceContext buildDocumentServiceContext(String uri, WorkspaceManager workspaceManager,
                                                                     LSContextOperation operation,
                                                                     LanguageServerContext serverContext,
                                                                     CancelChecker cancelChecker) {
        return new BaseContextImpl.BaseContextBuilder(operation, serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withCancelChecker(cancelChecker)
                .build();
    }

    /**
     * Build the did open context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param capabilities     completion capabilities
     * @param serverContext    language server context
     * @param position         cursor position where the completion triggered
     * @return {@link DocumentServiceContext} base context generated
     */
    public static CompletionContext buildCompletionContext(String uri,
                                                           WorkspaceManager workspaceManager,
                                                           CompletionCapabilities capabilities,
                                                           LanguageServerContext serverContext,
                                                           Position position) {
        return new CompletionContextImpl.CompletionContextBuilder(serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withCapabilities(capabilities)
                .withCursorPosition(position)
                .build();
    }

    /**
     * Build the signature help context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param capabilities     signature help capabilities
     * @param serverContext    language server context
     * @param position         cursor position
     * @param cancelChecker    cancellation checker
     * @return {@link SignatureContext} generated signature context
     */
    public static SignatureContext buildSignatureContext(String uri,
                                                         WorkspaceManager workspaceManager,
                                                         SignatureHelpCapabilities capabilities,
                                                         LanguageServerContext serverContext,
                                                         Position position,
                                                         CancelChecker cancelChecker) {
        return new SignatureContextImpl.SignatureContextBuilder(serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withCapabilities(capabilities)
                .withPosition(position)
                .withCancelChecker(cancelChecker)
                .build();
    }

    /**
     * Build the References context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param serverContext    language server context
     * @param position         cursor position
     * @param cancelChecker    cancellation checker
     * @return {@link SignatureContext} generated signature context
     */
    public static ReferencesContext buildReferencesContext(String uri,
                                                           WorkspaceManager workspaceManager,
                                                           LanguageServerContext serverContext,
                                                           Position position, CancelChecker cancelChecker) {
        return new ReferencesContextImpl.ReferencesContextBuilder(serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withPosition(position)
                .withCancelChecker(cancelChecker)
                .build();
    }

    /**
     * Build the prepare rename context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param serverContext    language server context
     * @param position         cursor position
     * @param cancelChecker    cancellation checker
     * @return {@link SignatureContext} generated signature context
     */
    public static PrepareRenameContext buildPrepareRenameContext(String uri,
                                                                 WorkspaceManager workspaceManager,
                                                                 LanguageServerContext serverContext,
                                                                 Position position,
                                                                 CancelChecker cancelChecker) {
        return new PrepareRenameContextImpl.PrepareRenameContextBuilder(serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withPosition(position)
                .withCancelChecker(cancelChecker)
                .build();
    }

    /**
     * Build the rename context.
     *
     * @param params             Rename parameters
     * @param workspaceManager   workspace manager instance
     * @param serverContext      language server context
     * @param clientCapabilities lang server client capabilities
     * @param cancelChecker      cancellation checker
     * @return {@link SignatureContext} generated signature context
     */
    public static RenameContext buildRenameContext(RenameParams params,
                                                   WorkspaceManager workspaceManager,
                                                   LanguageServerContext serverContext,
                                                   LSClientCapabilities clientCapabilities,
                                                   CancelChecker cancelChecker) {
        return new RenameContextImpl.RenameContextBuilder(serverContext, params, clientCapabilities)
                .withFileUri(params.getTextDocument().getUri())
                .withWorkspaceManager(workspaceManager)
                .withPosition(params.getPosition())
                .withCancelChecker(cancelChecker)
                .build();
    }

    /**
     * Build the code action context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param serverContext    language server context
     * @param params           code action parameters
     * @param cancelChecker    cancellation checker
     * @return {@link CodeActionContext} generated signature context
     */
    public static CodeActionContext buildCodeActionContext(String uri,
                                                           WorkspaceManager workspaceManager,
                                                           LanguageServerContext serverContext,
                                                           CodeActionParams params,
                                                           CancelChecker cancelChecker) {
        return new CodeActionContextImpl.CodeActionContextBuilder(params, serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withCancelChecker(cancelChecker)
                .build();
    }

    /**
     * Build the hover context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param serverContext    language server context
     * @param position         cursor position where the hover request executed
     * @param cancelChecker    cancellation checker
     * @return {@link HoverContext} generated hover context
     */
    public static HoverContext buildHoverContext(String uri,
                                                 WorkspaceManager workspaceManager,
                                                 LanguageServerContext serverContext,
                                                 Position position,
                                                 CancelChecker cancelChecker) {
        return new HoverContextImpl.HoverContextBuilder(serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withPosition(position)
                .withCancelChecker(cancelChecker)
                .build();
    }

    /**
     * Build the command execution context.
     *
     * @param workspaceManager   workspace manager instance
     * @param serverContext      language server context
     * @param arguments          list of command arguments
     * @param clientCapabilities language server client capabilities
     * @param languageServer     language server instance
     * @return {@link ExecuteCommandContext} generated command execution context
     */
    public static ExecuteCommandContext buildExecuteCommandContext(WorkspaceManager workspaceManager,
                                                                   LanguageServerContext serverContext,
                                                                   List<CommandArgument> arguments,
                                                                   LSClientCapabilities clientCapabilities,
                                                                   BallerinaLanguageServer languageServer) {
        return new ExecuteCommandContextImpl.ExecuteCommandContextBuilder(serverContext,
                arguments, clientCapabilities, languageServer)
                .withWorkspaceManager(workspaceManager)
                .build();
    }

    /**
     * Build the folding range context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param serverContext    language server context
     * @param lineFoldingOnly  if line folding only or not
     * @param cancelChecker    cancellation checker
     * @return {@link FoldingRangeContext} generated folding range context
     */
    public static FoldingRangeContext buildFoldingRangeContext(String uri, WorkspaceManager workspaceManager,
                                                               LanguageServerContext serverContext,
                                                               boolean lineFoldingOnly,
                                                               CancelChecker cancelChecker) {
        return new FoldingRangeContextImpl.FoldingRangeContextBuilder(lineFoldingOnly, serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withCancelChecker(cancelChecker)
                .build();
    }

    /**
     * Build the goto definition context.
     *
     * @param uri              file URI
     * @param workspaceManager workspace manager instance
     * @param serverContext    language server context
     * @param position         position where the definition operation invoked
     * @param cancelChecker    cancellation checker
     * @return {@link BallerinaDefinitionContext}
     */
    public static BallerinaDefinitionContext buildDefinitionContext(String uri,
                                                                    WorkspaceManager workspaceManager,
                                                                    LanguageServerContext serverContext,
                                                                    Position position,
                                                                    CancelChecker cancelChecker) {
        return new BallerinaDefinitionContextImpl.DefinitionContextBuilder(serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withCursorPosition(position)
                .withCancelChecker(cancelChecker)
                .build();
    }

    /**
     * Build the document symbols context.
     *
     * @param params document symbol params.
     * @param workspaceManager workspace manager instance.
     * @param serverContext language server context.
     * @param clientCapabilities client capabilities.
     * @return {@link DocumentSymbolContext} generated document symbol context.
     */
    public static DocumentSymbolContext buildDocumentSymbolContext(DocumentSymbolParams params,
                                                                   WorkspaceManager workspaceManager,
                                                                   LanguageServerContext serverContext,
                                                                   LSClientCapabilities clientCapabilities) {
        return new DocumentSymbolContextImpl.DocumentSymbolContextBuilder(params, serverContext, clientCapabilities)
                .withFileUri(params.getTextDocument().getUri())
                .withWorkspaceManager(workspaceManager)
                .build();
    }

    /**
     * Build the semantic tokens context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param serverContext    language server context
     * @param cancelChecker    cancellation checker
     * @return {@link SemanticTokensContext} generated semantic tokens context
     */
    public static SemanticTokensContext buildSemanticTokensContext(String uri,
                                                                   WorkspaceManager workspaceManager,
                                                                   LanguageServerContext serverContext,
                                                                   CancelChecker cancelChecker) {
        return new SemanticTokensContextImpl.SemanticTokensContextBuilder(serverContext)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withCancelChecker(cancelChecker)
                .build();
    }
}
