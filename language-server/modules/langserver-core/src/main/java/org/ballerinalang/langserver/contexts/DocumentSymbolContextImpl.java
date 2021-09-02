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
package org.ballerinalang.langserver.contexts;

import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.DocumentSymbolContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolTag;
import org.eclipse.lsp4j.SymbolTagSupportCapabilities;

import java.util.Optional;

/**
 * Document Symbol Context implementation.
 *
 * @since 2.0.0
 */
public class DocumentSymbolContextImpl extends AbstractDocumentServiceContext implements DocumentSymbolContext {

    private Boolean labelSupport;
    private SymbolTagSupportCapabilities supportedTags;
    private Boolean hierarchicalDocumentSymbolSupport;
    private LSClientCapabilities clientCapabilities;
    private DocumentSymbolParams params;
    private DocumentSymbolCapabilities documentSymbolClientCapabilities;
    boolean deprecatedTagSupport;

    DocumentSymbolContextImpl(LSOperation operation, String fileUri, WorkspaceManager wsManager,
                              LanguageServerContext serverContext,
                              DocumentSymbolParams params, LSClientCapabilities clientCapabilities) {
        super(operation, fileUri, wsManager, serverContext);
        this.clientCapabilities = clientCapabilities;
        this.documentSymbolClientCapabilities = clientCapabilities.getTextDocCapabilities().getDocumentSymbol();
        if (this.documentSymbolClientCapabilities != null) {
            this.hierarchicalDocumentSymbolSupport = this.getDocumentSymbolClientCapabilities()
                    .getHierarchicalDocumentSymbolSupport();
            this.supportedTags = this.getDocumentSymbolClientCapabilities().getTagSupport();
            this.labelSupport = this.getDocumentSymbolClientCapabilities().getLabelSupport();
        }
        if (this.supportedTags != null &&
                this.supportedTags.getValueSet().contains(SymbolTag.Deprecated)) {
            this.deprecatedTagSupport = true;
        } else {
            this.deprecatedTagSupport = false;
        }
        this.params = params;
    }

    @Override
    public DocumentSymbolParams getParams() {
        return this.params;
    }

    @Override
    public LSClientCapabilities getClientCapabilities() {
        return this.clientCapabilities;
    }

    @Override
    public DocumentSymbolCapabilities getDocumentSymbolClientCapabilities() {
        return this.documentSymbolClientCapabilities;
    }

    @Override
    public boolean getHierarchicalDocumentSymbolSupport() {
        return Boolean.TRUE.equals(this.hierarchicalDocumentSymbolSupport);
    }

    @Override
    public boolean getLabelSupport() {
        return Boolean.TRUE.equals(this.labelSupport);
    }

    @Override
    public Optional<SymbolTagSupportCapabilities> supportedTags() {
        return Optional.ofNullable(this.supportedTags);
    }

    @Override
    public boolean deprecatedSupport() {
        return Boolean.TRUE.equals(this.deprecatedTagSupport);
    }

    /**
     * Represents Language server document symbol context Builder.
     *
     * @since 2.0.0
     */
    protected static class DocumentSymbolContextBuilder extends AbstractContextBuilder<DocumentSymbolContextBuilder> {

        private DocumentSymbolParams params;
        private LSClientCapabilities clientCapabilities;

        public DocumentSymbolContextBuilder(DocumentSymbolParams params,
                                            LanguageServerContext serverContext,
                                            LSClientCapabilities clientCapabilities) {
            super(LSContextOperation.TXT_DOC_SYMBOL, serverContext);
            this.clientCapabilities = clientCapabilities;
            this.params = params;
        }

        public DocumentSymbolContext build() {
            return new DocumentSymbolContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.serverContext,
                    this.params,
                    this.clientCapabilities);
        }

        @Override
        public DocumentSymbolContextBuilder self() {
            return this;
        }
    }
}
