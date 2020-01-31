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
package org.ballerinalang.langserver;

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContextImpl;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.signature.SignatureKeys;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.SignatureHelpCapabilities;

import java.util.ArrayList;

/**
 * Represents Document Service operation context.
 *
 * @since 1.2.0
 */
public class DocumentServiceOperationContext extends LSContextImpl {
    private DocumentServiceOperationContext(LSOperation operation) {
        super(operation);
    }

    /**
     * Represents Document Service operation context Builder.
     */
    public static class ServiceOperationContextBuilder extends ContextBuilder<ServiceOperationContextBuilder> {
        public ServiceOperationContextBuilder(LSOperation operation) {
            super(operation);
        }

        ServiceOperationContextBuilder withCompletionParams(CompletionCapabilities capabilities) {
            this.lsContext.put(CompletionKeys.CLIENT_CAPABILITIES_KEY, capabilities);
            return this;
        }

        ServiceOperationContextBuilder withHoverParams() {
            this.lsContext.put(DocumentServiceKeys.IS_CACHE_SUPPORTED, true);
            this.lsContext.put(DocumentServiceKeys.IS_CACHE_OUTDATED_SUPPORTED, true);
            this.lsContext.put(DocumentServiceKeys.COMPILE_FULL_PROJECT, false);
            return this;
        }

        ServiceOperationContextBuilder withSignatureParams(SignatureHelpCapabilities capabilities) {
            this.lsContext.put(SignatureKeys.SIGNATURE_HELP_CAPABILITIES_KEY, capabilities);
            return this;
        }

        ServiceOperationContextBuilder withDefinitionParams() {
            this.lsContext.put(DocumentServiceKeys.COMPILE_FULL_PROJECT, true);
            return this;
        }

        ServiceOperationContextBuilder withReferencesParams() {
            this.lsContext.put(DocumentServiceKeys.COMPILE_FULL_PROJECT, true);
            return this;
        }

        ServiceOperationContextBuilder withDocumentSymbolParams(String fileUri) {
            this.lsContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
            this.lsContext.put(DocumentServiceKeys.SYMBOL_LIST_KEY, new ArrayList<>());
            return this;
        }

        ServiceOperationContextBuilder withCodeActionParams(WorkspaceDocumentManager documentManager) {
            this.lsContext.put(DocumentServiceKeys.DOC_MANAGER_KEY, documentManager);
            return this;
        }

        ServiceOperationContextBuilder withFormattingParams(String fileUri) {
            this.lsContext.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
            return this;
        }

        ServiceOperationContextBuilder withRenameParams() {
            this.lsContext.put(DocumentServiceKeys.COMPILE_FULL_PROJECT, true);
            return this;
        }

        @Override
        public LSContext build() {
            return this.lsContext;
        }

        @Override
        protected ServiceOperationContextBuilder self() {
            return this;
        }
    }
}
