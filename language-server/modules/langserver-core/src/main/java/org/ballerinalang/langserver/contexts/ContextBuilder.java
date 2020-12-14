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
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.FoldingRangeContext;
import org.ballerinalang.langserver.commons.HoverContext;
import org.ballerinalang.langserver.commons.SignatureContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelpCapabilities;

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
     * @return {@link DocumentServiceContext} base context generated
     */
    public static DocumentServiceContext buildBaseContext(String uri, WorkspaceManager workspaceManager,
                                                          LSContextOperation operation) {
        return new BaseContextImpl.BaseContextBuilder(operation)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .build();
    }

    /**
     * Build the did open context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param capabilities     completion capabilities
     * @return {@link DocumentServiceContext} base context generated
     */
    public static CompletionContext buildCompletionContext(String uri,
                                                           WorkspaceManager workspaceManager,
                                                           CompletionCapabilities capabilities,
                                                           Position position) {
        return new CompletionContextImpl.CompletionContextBuilder()
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
     * @param position         cursor position
     * @return {@link SignatureContext} generated signature context
     */
    public static SignatureContext buildSignatureContext(String uri,
                                                         WorkspaceManager workspaceManager,
                                                         SignatureHelpCapabilities capabilities,
                                                         Position position) {
        return new SignatureContextImpl.SignatureContextBuilder()
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withCapabilities(capabilities)
                .withPosition(position)
                .build();
    }

    /**
     * Build the code action context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param params           code action parameters
     * @return {@link CodeActionContext} generated signature context
     */
    public static CodeActionContext buildCodeActionContext(String uri,
                                                           WorkspaceManager workspaceManager,
                                                           CodeActionParams params) {
        return new CodeActionContextImpl.CodeActionContextBuilder(params)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .build();
    }

    /**
     * Build the hover context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @return {@link HoverContext} generated hover context
     */
    public static HoverContext buildHoverContext(String uri, WorkspaceManager workspaceManager, Position position) {
        return new HoverContextImpl.HoverContextBuilder()
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .withPosition(position)
                .build();
    }

    /**
     * Build the command execution context.
     *
     * @param workspaceManager workspace manager instance
     * @return {@link ExecuteCommandContext} generated command execution context
     */
    public static ExecuteCommandContext buildExecuteCommandContext(WorkspaceManager workspaceManager,
                                                                   List<Object> arguments,
                                                                   LSClientCapabilities clientCapabilities,
                                                                   BallerinaLanguageServer languageServer) {
        return new ExecuteCommandContextImpl.ExecuteCommandContextBuilder(arguments, clientCapabilities, languageServer)
                .withWorkspaceManager(workspaceManager)
                .build();
    }

    /**
     * Build the folding range context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @param lineFoldingOnly  if line folding only or not
     * @return {@link FoldingRangeContext} generated folding range context
     */
    public static FoldingRangeContext buildFoldingRangeContext(String uri, WorkspaceManager workspaceManager,
                                                               boolean lineFoldingOnly) {
        return new FoldingRangeContextImpl.FoldingRangeContextBuilder(lineFoldingOnly)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .build();
    }
}
