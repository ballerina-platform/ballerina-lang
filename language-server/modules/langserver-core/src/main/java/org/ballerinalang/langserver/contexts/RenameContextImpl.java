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
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.RenameContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.RenameParams;

/**
 * Implementation of {@link RenameContext}.
 *
 * @since 2.0.0
 */
public class RenameContextImpl extends ReferencesContextImpl implements RenameContext {

    private LSClientCapabilities clientCapabilities;
    private RenameParams params;
    private Boolean honorsChangeAnnotations;

    RenameContextImpl(LSOperation operation,
                      String fileUri,
                      WorkspaceManager wsManager,
                      Position cursorPos,
                      LanguageServerContext serverContext,
                      RenameParams params,
                      LSClientCapabilities clientCapabilities) {
        super(operation, fileUri, wsManager, cursorPos, serverContext);
        this.clientCapabilities = clientCapabilities;
        this.params = params;
        this.honorsChangeAnnotations = clientCapabilities.getTextDocCapabilities().getRename() != null
                && Boolean.TRUE.equals(clientCapabilities.getTextDocCapabilities().getRename()
                .getHonorsChangeAnnotations());
    }

    /**
     * Represents Language server rename context Builder.
     *
     * @since 2.0.0
     */
    protected static class RenameContextBuilder extends PositionedOperationContextBuilder<RenameContext> {

        private RenameParams params;
        private LSClientCapabilities clientCapabilities;

        public RenameContextBuilder(LanguageServerContext serverContext, RenameParams params,
                                    LSClientCapabilities clientCapabilities) {
            super(LSContextOperation.TXT_RENAME, serverContext);
            this.clientCapabilities = clientCapabilities;
            this.params = params;
        }

        public RenameContext build() {
            return new RenameContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.position,
                    this.serverContext,
                    this.params,
                    this.clientCapabilities);
        }

        @Override
        public RenameContextImpl.RenameContextBuilder self() {
            return this;
        }
    }

    @Override
    public boolean getHonorsChangeAnnotations() {
        return Boolean.TRUE.equals(this.honorsChangeAnnotations);
    }

    @Override
    public RenameParams getParams() {
        return this.params;
    }

    @Override
    public LSClientCapabilities getClientCapabilities() {
        return this.clientCapabilities;
    }
}
