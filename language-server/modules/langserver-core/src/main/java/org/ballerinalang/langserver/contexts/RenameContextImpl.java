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
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.Position;

/**
 * Implementation of {@link RenameContext}.
 *
 * @since 2.0.0
 */
public class RenameContextImpl extends ReferencesContextImpl implements RenameContext {

    RenameContextImpl(LSOperation operation,
                      String fileUri,
                      WorkspaceManager wsManager,
                      Position cursorPos,
                      LanguageServerContext serverContext) {
        super(operation, fileUri, wsManager, cursorPos, serverContext);
    }

    /**
     * Represents Language server rename context Builder.
     *
     * @since 2.0.0
     */
    protected static class RenameContextBuilder extends PositionedOperationContextBuilder<RenameContext> {

        public RenameContextBuilder(LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_RENAME, serverContext);
        }

        public RenameContext build() {
            return new RenameContextImpl(this.operation,
                    this.fileUri,
                    this.wsManager,
                    this.position,
                    this.serverContext);
        }

        @Override
        public RenameContextImpl.RenameContextBuilder self() {
            return this;
        }
    }
}
