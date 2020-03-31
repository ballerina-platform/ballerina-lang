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
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.command.ExecuteCommandKeys;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContextImpl;

import java.util.List;

/**
 * Represents the Workspace Service Operation Context.
 *
 * @since 1.2.0
 */
public class WorkspaceServiceOperationContext extends LSContextImpl {
    private WorkspaceServiceOperationContext(LSOperation operation) {
        super(operation);
    }

    /**
     * Represents the Workspace Service Operation Context Builder.
     */
    public static class ServiceOperationContextBuilder extends ContextBuilder<ServiceOperationContextBuilder> {
        public ServiceOperationContextBuilder(LSOperation operation) {
            super(operation);
        }

        ServiceOperationContextBuilder withExecuteCommandParams(List<Object> args,
                                                                WorkspaceDocumentManager documentManager,
                                                                BallerinaLanguageServer langServer,
                                                                LSClientCapabilities clientCapabilities) {
            this.lsContext.put(ExecuteCommandKeys.COMMAND_ARGUMENTS_KEY, args);
            this.lsContext.put(DocumentServiceKeys.DOC_MANAGER_KEY, documentManager);
            this.lsContext.put(ExecuteCommandKeys.LANGUAGE_SERVER_KEY, langServer);
            this.lsContext.put(ExecuteCommandKeys.LANGUAGE_CLIENT_KEY, langServer.getClient());
            this.lsContext.put(ExecuteCommandKeys.LS_CLIENT_CAPABILITIES_KEY, clientCapabilities);
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
