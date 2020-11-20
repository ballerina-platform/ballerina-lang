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

import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.WorkspaceServiceContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;

/**
 * Language server workspace service context implementation.
 *
 * @since 1.2.0
 */
public class AbstractWorkspaceServiceContext implements WorkspaceServiceContext {

    private final LSOperation operation;

    private final WorkspaceManager workspaceManager;

    AbstractWorkspaceServiceContext(LSOperation operation, WorkspaceManager wsManager) {
        this.operation = operation;
        this.workspaceManager = wsManager;
    }

    @Override
    public LSOperation operation() {
        return this.operation;
    }

    @Override
    public WorkspaceManager workspace() {
        return this.workspaceManager;
    }

    /**
     * Represents Language server context Builder.
     *
     * @param <T> builder type
     * @since 2.0.0
     */
    protected abstract static class AbstractContextBuilder<T extends AbstractContextBuilder<T>> {
        protected final LSOperation operation;
        protected String fileUri;
        protected WorkspaceManager wsManager;

        /**
         * Context Builder constructor.
         *
         * @param lsOperation LS Operation for the particular invocation
         */
        public AbstractContextBuilder(LSOperation lsOperation) {
            this.operation = lsOperation;
        }

        public T withWorkspaceManager(WorkspaceManager workspaceManager) {
            this.wsManager = workspaceManager;
            return self();
        }

        public WorkspaceServiceContext build() {
            return new AbstractWorkspaceServiceContext(this.operation, this.wsManager);
        }

        public abstract T self();
    }
}
