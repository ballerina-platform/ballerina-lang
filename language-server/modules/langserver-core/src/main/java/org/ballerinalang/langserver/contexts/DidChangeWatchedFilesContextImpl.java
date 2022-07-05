/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.langserver.commons.DidChangeWatchedFilesContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;

/**
 * DidChangeWatchedFiles context implementation.
 *
 * @since 2.0.0
 */
public class DidChangeWatchedFilesContextImpl
        extends AbstractWorkspaceServiceContext implements DidChangeWatchedFilesContext {

    DidChangeWatchedFilesContextImpl(WorkspaceManager wsManager, LanguageServerContext serverContext) {
        super(LSContextOperation.WS_WF_CHANGED, wsManager, serverContext);
    }

    /**
     * Represents Language server didChangeWatchedFiles context Builder.
     *
     * @since 2.0.0
     */
    protected static class DidChangeWatchedFilesContextBuilder
            extends AbstractContextBuilder<DidChangeWatchedFilesContextImpl.DidChangeWatchedFilesContextBuilder> {
        private final LanguageServerContext serverContext;

        /**
         * Context Builder constructor.
         */
        public DidChangeWatchedFilesContextBuilder(WorkspaceManager workspaceManager,
                                                   LanguageServerContext serverContext) {
            super(LSContextOperation.WS_EXEC_CMD, serverContext);
            this.wsManager = workspaceManager;
            this.serverContext = serverContext;
        }

        public DidChangeWatchedFilesContext build() {
            return new DidChangeWatchedFilesContextImpl(
                    this.wsManager,
                    this.serverContext);
        }

        @Override
        public DidChangeWatchedFilesContextBuilder self() {
            return this;
        }
    }
}
