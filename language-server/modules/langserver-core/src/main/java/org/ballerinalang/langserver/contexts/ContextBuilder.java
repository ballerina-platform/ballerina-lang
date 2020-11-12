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

import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.HoverContext;
import org.ballerinalang.langserver.commons.NewLSContext;
import org.ballerinalang.langserver.commons.SignatureContext;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelpCapabilities;

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
     * @return {@link NewLSContext} base context generated
     */
    public static NewLSContext buildBaseContext(String uri, BallerinaWorkspaceManager workspaceManager,
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
     * @return {@link NewLSContext} base context generated
     */
    public static CompletionContext buildCompletionContext(String uri,
                                                           BallerinaWorkspaceManager workspaceManager,
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
                                                         BallerinaWorkspaceManager workspaceManager,
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
     * @param cursorPosition   code action triggered cursor position
     * @param startPosition    code action start position
     * @return {@link CodeActionContext} generated signature context
     */
    public static CodeActionContext buildCodeActionContext(String uri,
                                                           BallerinaWorkspaceManager workspaceManager,
                                                           Position cursorPosition,
                                                           Position startPosition) {
        return new CodeActionContextImpl.CodeActionContextBuilder(cursorPosition, startPosition)
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .build();
    }

    /**
     * Build the hover context.
     *
     * @param uri              file uri
     * @param workspaceManager workspace manager instance
     * @return {@link CodeActionContext} generated signature context
     */
    public static HoverContext buildHoverContext(String uri, BallerinaWorkspaceManager workspaceManager) {
        return new HoverContextImpl.HoverContextBuilder()
                .withFileUri(uri)
                .withWorkspaceManager(workspaceManager)
                .build();
    }
}
