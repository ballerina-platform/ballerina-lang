/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects;

import io.ballerina.projects.directory.WorkspaceProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.toml.semantic.ast.TomlTableNode;

public class WorkspaceBallerinaToml {
    private final TomlDocumentContext balWorkspaceTomlContext;
    private final WorkspaceProject workspace;

    private WorkspaceBallerinaToml(WorkspaceProject workspace, TomlDocumentContext ballerinaTomlContext) {
        this.workspace = workspace;
        this.balWorkspaceTomlContext = ballerinaTomlContext;
    }

    public static WorkspaceBallerinaToml from(TomlDocument tomlDocument, WorkspaceProject workspace) {
        return new WorkspaceBallerinaToml(workspace, TomlDocumentContext.from(tomlDocument));
    }

    public WorkspaceProject project() {
        return workspace;
    }

    public String name() {
        return ProjectConstants.BALLERINA_TOML;
    }

    public TomlTableNode tomlAstNode() {
        return tomlDocument().toml().rootNode();
    }

    public TomlDocument tomlDocument() {
        return this.balWorkspaceTomlContext.tomlDocument();
    }

    /** Returns an instance of the BalWorkspaceToml.Modifier.
     *
     * @return  BalWorkspaceToml modifier
     */
    public WorkspaceBallerinaToml.Modifier modify() {
        return new WorkspaceBallerinaToml.Modifier(this);
    }

    /**
     * Inner class that handles Document modifications.
     */
    public static class Modifier {
        private TomlDocument tomlDocument;
        private final WorkspaceProject workspace;

        private Modifier(WorkspaceBallerinaToml oldDocument) {
            this.tomlDocument = oldDocument.tomlDocument();
            this.workspace = oldDocument.project();
        }

        /**
         * Sets the content to be changed.
         *
         * @param content content to change with
         * @return Document.Modifier that holds the content to be changed
         */
        public WorkspaceBallerinaToml.Modifier withContent(String content) {
            this.tomlDocument = TomlDocument.from(ProjectConstants.BALLERINA_TOML, content);
            return this;
        }

        /**
         * Returns a new document with updated content.
         *
         * @return document with updated content
         */
        public WorkspaceBallerinaToml apply() {

            /* TODO: packages might have been changed, so we need to reload the projects
                reuse existing projects as much as possible compilation might change,
                dependency paths might change, reload the packages */

            WorkspaceBallerinaToml balWorkspaceToml = WorkspaceBallerinaToml.from(this.tomlDocument, this.workspace);
            return balWorkspaceToml;
        }
    }
}
