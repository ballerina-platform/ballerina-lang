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

import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;

/**
 * Language server's base context implementation.
 *
 * @since 1.2.0
 */
public class BaseContextImpl extends AbstractDocumentServiceContext {

    BaseContextImpl(LSOperation operation,
                    String fileUri,
                    WorkspaceManager wsManager,
                    LanguageServerContext serverContext) {
        super(operation, fileUri, wsManager, serverContext);
    }

    /**
     * Represents Language server base context Builder.
     *
     * @since 2.0.0
     */
    protected static class BaseContextBuilder extends AbstractContextBuilder<BaseContextBuilder> {

        /**
         * Context Builder constructor.
         *
         * @param lsOperation LS Operation for the particular invocation
         */
        public BaseContextBuilder(LSOperation lsOperation,
                                  LanguageServerContext serverContext) {
            super(lsOperation, serverContext);
        }

        @Override
        public DocumentServiceContext build() {
            return new BaseContextImpl(this.operation, this.fileUri, this.wsManager, this.serverContext);
        }

        @Override
        public BaseContextBuilder self() {
            return this;
        }
    }
}
