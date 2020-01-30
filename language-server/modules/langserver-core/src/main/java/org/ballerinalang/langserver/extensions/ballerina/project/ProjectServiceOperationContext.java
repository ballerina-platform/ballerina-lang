/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.project;

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.LSOperation;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContextImpl;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;

/**
 * Operation Context for Project Service.
 *
 * @since 1.2.0
 */
public class ProjectServiceOperationContext extends LSContextImpl {
    private ProjectServiceOperationContext(LSOperation operation) {
        super(operation);
    }

    /**
     * Operation Context for Project Service Builder.
     */
    public static class ProjectServiceContextBuilder extends ContextBuilder<ProjectServiceContextBuilder> {
        public ProjectServiceContextBuilder(LSOperation operation) {
            super(operation);
        }

        public ProjectServiceContextBuilder withModulesParams(String sourceRoot,
                                                              WorkspaceDocumentManager documentManager) {
            this.lsContext.put(DocumentServiceKeys.SOURCE_ROOT_KEY, sourceRoot);
            this.lsContext.put(DocumentServiceKeys.DOC_MANAGER_KEY, documentManager);
            return this;
        }

        @Override
        public LSContext build() {
            return this.lsContext;
        }

        @Override
        protected ProjectServiceContextBuilder self() {
            return this;
        }
    }
}
