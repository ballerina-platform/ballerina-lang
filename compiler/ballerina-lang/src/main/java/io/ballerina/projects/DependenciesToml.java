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

package io.ballerina.projects;

import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.toml.semantic.ast.TomlTableNode;

/**
 * Represents the 'Dependencies.toml' file in a package.
 *
 * @since 2.0.0
 */
public class DependenciesToml {
    private TomlDocumentContext dependenciesTomlContext;
    private Package packageInstance;

    private DependenciesToml(Package aPackage, TomlDocumentContext dependenciesTomlContext) {
        this.packageInstance = aPackage;
        this.dependenciesTomlContext = dependenciesTomlContext;
    }

    public static DependenciesToml from(TomlDocumentContext dependenciesTomlContext, Package pkg) {
        return new DependenciesToml(pkg, dependenciesTomlContext);
    }

    TomlDocumentContext dependenciesTomlContext() {
        return dependenciesTomlContext;
    }

    public Package packageInstance() {
        return packageInstance;
    }

    public String name() {
        return ProjectConstants.DEPENDENCIES_TOML;
    }

    public TomlTableNode tomlAstNode() {
        return tomlDocument().tomlAstNode();
    }

    public TomlDocument tomlDocument() {
        return this.dependenciesTomlContext.tomlDocument();
    }

    /** Returns an instance of the Document.Modifier.
     *
     * @return  module modifier
     */
    public DependenciesToml.Modifier modify() {
        return new DependenciesToml.Modifier(this);
    }

    /**
     * Inner class that handles Document modifications.
     */
    public static class Modifier {
        private TomlDocument tomlDocument;
        private Package oldPackage;

        private Modifier(DependenciesToml oldDocument) {
            this.tomlDocument = oldDocument.tomlDocument();
            this.oldPackage = oldDocument.packageInstance();
        }

        /**
         * Sets the content to be changed.
         *
         * @param content content to change with
         * @return Document.Modifier that holds the content to be changed
         */
        public DependenciesToml.Modifier withContent(String content) {
            this.tomlDocument = TomlDocument.from(ProjectConstants.DEPENDENCIES_TOML, content);
            return this;
        }

        /**
         * Returns a new document with updated content.
         *
         * @return document with updated content
         */
        public DependenciesToml apply() {
            DependenciesToml dependenciesToml = DependenciesToml.from(TomlDocumentContext.from(this.tomlDocument),
                    oldPackage);
            Package newPackage = oldPackage.modify().updateDependenciesToml(dependenciesToml).apply();
            return newPackage.dependenciesToml().get();
        }
    }
}
