/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.util;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;

import java.util.UUID;

/**
 * A collection of utilities related to LS client.
 *
 * @since 2.0.0
 */
public class LSClientUtil {

    public static final String EXECUTE_COMMAND_CAPABILITY_ID = UUID.randomUUID().toString();

    private LSClientUtil() {

    }

    /**
     * Check if the LS client support dynamic command registration.
     *
     * @param clientCapabilities LS client capabilities
     * @return True if dynamic command registration is supported
     */
    public static boolean isDynamicCommandRegistrationSupported(ClientCapabilities clientCapabilities) {
        WorkspaceClientCapabilities workspaceCapabilities = clientCapabilities.getWorkspace();
        return workspaceCapabilities != null && workspaceCapabilities.getExecuteCommand() != null &&
                Boolean.TRUE.equals(workspaceCapabilities.getExecuteCommand().getDynamicRegistration());
    }

    /**
     * Check if the LS client supports semanticTokens' dynamic registration.
     *
     * @param capabilities LS text document client capabilities
     * @return True if dynamic registration is supported, otherwise false
     */
    public static boolean isDynamicSemanticTokensRegistrationSupported(TextDocumentClientCapabilities capabilities) {
        return capabilities != null && capabilities.getSemanticTokens() != null &&
                Boolean.TRUE.equals(capabilities.getSemanticTokens().getDynamicRegistration());
    }

    public static boolean isDynamicHoverRegistrationSupported(TextDocumentClientCapabilities capabilities) {
        return capabilities != null && capabilities.getHover() != null &&
                Boolean.TRUE.equals(capabilities.getHover().getDynamicRegistration());
    }

    public static boolean isDynamicDefinitionRegistrationSupported(TextDocumentClientCapabilities capabilities) {
        return capabilities != null && capabilities.getDefinition() != null &&
                Boolean.TRUE.equals(capabilities.getDefinition().getDynamicRegistration());
    }

    public static boolean isDynamicReferencesRegistrationSupported(TextDocumentClientCapabilities capabilities) {
        return capabilities != null && capabilities.getReferences() != null &&
                Boolean.TRUE.equals(capabilities.getReferences().getDynamicRegistration());
    }

    public static boolean isDynamicSynchronizationRegistrationSupported(TextDocumentClientCapabilities capabilities) {
        return capabilities != null && capabilities.getSynchronization() != null &&
                Boolean.TRUE.equals(capabilities.getSynchronization().getDynamicRegistration());
    }

    public static boolean isDynamicCompletionRegistrationSupported(TextDocumentClientCapabilities capabilities) {
        return capabilities != null && capabilities.getCompletion() != null &&
                Boolean.TRUE.equals(capabilities.getCompletion().getDynamicRegistration());
    }

    public static boolean isCodeActionResolveSupported(TextDocumentClientCapabilities capabilities) {
        return capabilities != null && capabilities.getCodeAction() != null &&
                capabilities.getCodeAction().getResolveSupport() != null &&
                capabilities.getCodeAction().getResolveSupport().getProperties().contains("edit");
    }

    /**
     * Whether the client supports the prepare-rename operation.
     * 
     * @param clientCapabilities {@link ClientCapabilities}
     * @return whether the client supports prepareRename or not
     */
    public static boolean clientSupportsPrepareRename(ClientCapabilities clientCapabilities) {
        TextDocumentClientCapabilities textDocument = clientCapabilities.getTextDocument();
        return textDocument != null && textDocument.getRename() != null &&
                Boolean.TRUE.equals(textDocument.getRename().getPrepareSupport());
    }
}
