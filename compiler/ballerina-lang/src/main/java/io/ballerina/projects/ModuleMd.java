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

/**
 * Represents the 'Module.md' file of a module.
 *
 * @since 2.0.0
 * @deprecated use {@link ModuleReadmeMd} instead
 */
@Deprecated (forRemoval = true, since = "2.11.0")
public class ModuleMd {

    private final MdDocumentContext mdDocumentContext;
    private final Module module;

    ModuleMd(MdDocumentContext documentContext, Module module) {
        this.mdDocumentContext = documentContext;
        this.module = module;
    }

    static ModuleMd from(MdDocumentContext documentContext, Module module) {
        return new ModuleMd(documentContext, module);
    }

    public Module module() {
        return module;
    }

    public String content() {
        return mdDocumentContext.content();
    }


    /**
     * Returns an instance of the Document.Modifier.
     *
     * @return  module modifier
     */
    public Modifier modify() {
        return new Modifier(this);
    }

    /**
     * Inner class that handles Document modifications.
     */
    public static class Modifier {
        private String content;
        private final String name;
        private final DocumentId documentId;
        private final Module oldModule;

        private Modifier(ModuleMd oldDocument) {
            this.content = oldDocument.mdDocumentContext.content();
            this.oldModule = oldDocument.module();
            this.name = oldDocument.mdDocumentContext.name();
            this.documentId = oldDocument.mdDocumentContext.documentId();
        }

        /**
         * Sets the content to be changed.
         *
         * @param content content to change with
         * @return Document.Modifier that holds the content to be changed
         */
        public Modifier withContent(String content) {
            this.content = content;
            return this;
        }

        /**
         * Returns a new document with updated content.
         *
         * @return document with updated content
         */
        public ModuleMd apply() {
            MdDocumentContext moduleMd = MdDocumentContext.from(DocumentConfig.from(this.documentId,
                    this.content, this.name));
            Module newModule = oldModule.modify().updateModuleMd(moduleMd).apply();
            return newModule.moduleMd().get();
        }
    }
}
