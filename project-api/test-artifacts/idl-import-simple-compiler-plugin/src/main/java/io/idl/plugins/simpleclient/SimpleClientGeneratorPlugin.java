/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.idl.plugins.simpleclient;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.plugins.IDLClientGenerator;
import io.ballerina.projects.plugins.IDLGeneratorPlugin;
import io.ballerina.projects.plugins.IDLPluginContext;
import io.ballerina.projects.plugins.IDLSourceGeneratorContext;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Simple client generator plugin for compiler tests with client declarations.
 *
 * @since 2201.3.0
 */
public class SimpleClientGeneratorPlugin extends IDLGeneratorPlugin {
    @Override
    public void init(IDLPluginContext pluginContext) {
        pluginContext.addCodeGenerator(new SimpleClientGenerator());
    }

    private static class SimpleClientGenerator extends IDLClientGenerator {

        private int id = 1;

        @Override
        public boolean canHandle(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            String uri = getUri(idlSourceGeneratorContext.clientNode());

            if (uri.startsWith("http://example.com/disallow")) {
                return false;
            }

            return uri.startsWith("http://example.com");
        }

        @Override
        public void perform(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            String module = "client" + id++;
            ModuleId moduleId = ModuleId.create(module, idlSourceGeneratorContext.currentPackage().packageId());
            DocumentId documentId = DocumentId.create("simple_client", moduleId);
            DocumentConfig documentConfig = getClientCode(documentId, getUri(idlSourceGeneratorContext.clientNode()));
            ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                    ModuleName.from(idlSourceGeneratorContext.currentPackage().packageName(), module),
                    idlSourceGeneratorContext.currentPackage().descriptor());
            ModuleConfig moduleConfig = ModuleConfig.from(
                    moduleId, moduleDescriptor, Collections.singletonList(documentConfig),
                    Collections.emptyList(), null, new ArrayList<>());
            idlSourceGeneratorContext.addClient(moduleConfig);
        }

        private String getUri(Node clientNode) {
            BasicLiteralNode clientUri;

            if (clientNode.kind() == SyntaxKind.MODULE_CLIENT_DECLARATION) {
                clientUri = ((ModuleClientDeclarationNode) clientNode).clientUri();
            } else {
                clientUri = ((ClientDeclarationNode) clientNode).clientUri();
            }

            String text = clientUri.literalToken().text();
            return text.substring(1, text.length() - 1);
        }

        private DocumentConfig getClientCode(DocumentId documentId, String uri) {
            if ("http://example.com/apis/one.yaml".equals(uri)) {
                return DocumentConfig.from(
                        documentId, "public const DEFAULT_URL = \"http://www.example.com\";\n" +
                                "\n" +
                                "public type IntMap map<int>;\n" +
                                "\n" +
                                "public type XmlElement xml:Element;\n" +
                                "\n" +
                                "public type ClientConfiguration record {\n" +
                                "    string url?;\n" +
                                "    int 'limit;\n" +
                                "};\n" +
                                "\n" +
                                "public isolated client class 'client {\n" +
                                "    public final string url;\n" +
                                "    private int 'limit;\n" +
                                "\n" +
                                "    public function init(*ClientConfiguration config) {\n" +
                                "        self.url = config.url ?: DEFAULT_URL;\n" +
                                "        self.'limit = config.'limit;\n" +
                                "    }\n" +
                                "\n" +
                                "    remote function getLimit() returns int {\n" +
                                "        lock {\n" +
                                "            return self.'limit;\n" +
                                "        }\n" +
                                "    }\n" +
                                "\n" +
                                "    remote function setLimit(int 'limit) {\n" +
                                "        lock {\n" +
                                "            self.'limit = 'limit;\n" +
                                "        }\n" +
                                "    }\n" +
                                "}", "simple_client.bal");
            }

            return DocumentConfig.from(
                    documentId, "public type Config record {\n" +
                            "    string url;\n" +
                            "};\n" +
                            "\n" +
                            "public isolated client class 'client {\n" +
                            "    private string url;\n" +
                            "\n" +
                            "    public function init(*Config config) {\n" +
                            "        self.url = config.url;\n" +
                            "    }\n" +
                            "\n" +
                            "    remote function getUrl() returns string {\n" +
                            "        lock {\n" +
                            "            return self.url;\n" +
                            "        }\n" +
                            "    }\n" +
                            "}", "simple_client.bal");
        }
    }
}
