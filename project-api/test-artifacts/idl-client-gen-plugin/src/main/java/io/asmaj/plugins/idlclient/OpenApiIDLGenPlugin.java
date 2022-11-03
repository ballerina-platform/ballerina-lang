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

package io.asmaj.plugins.idlclient;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
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

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Simple IDL client generation test class.
 *
 * @since 2.3.0
 */
public class OpenApiIDLGenPlugin extends IDLGeneratorPlugin {
    @Override
    public void init(IDLPluginContext pluginContext) {
        pluginContext.addCodeGenerator(new OpenAPIClientGenerator());
    }

    private static class OpenAPIClientGenerator extends IDLClientGenerator {

        @Override
        public boolean canHandle(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            String uri = getUri(idlSourceGeneratorContext.clientNode());
            if (Files.notExists(idlSourceGeneratorContext.resourcePath())) {
                return false;
            }
            if (uri.endsWith("throwNPE")) {
                throw new NullPointerException();
            }
            if (uri.endsWith("throwRuntimeEx")) {
                throw new RuntimeException("canHandle crashed");
            }
            if (uri.endsWith("projectapiclientplugin.json")) {
                return true;
            }
            if (uri.endsWith("throwUnhandledExInPerform")) {
                return true;
            }
            return uri.equals("https://postman-echo.com/get?name=projectapiclientplugin");
        }

        public void perform(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            if (getUri(idlSourceGeneratorContext.clientNode()).endsWith("throwUnhandledExInPerform")) {
                throw new RuntimeException("perform crashed");
            }
            String moduleName = getAlias(idlSourceGeneratorContext.clientNode());
            ModuleId moduleId = ModuleId.create(moduleName, idlSourceGeneratorContext.currentPackage().packageId());
            DocumentId documentId = DocumentId.create("idl_client.bal", moduleId);
            DocumentConfig documentConfig = getClientCode(documentId);
            ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                    ModuleName.from(idlSourceGeneratorContext.currentPackage().packageName(), moduleName),
                    idlSourceGeneratorContext.currentPackage().descriptor());
            ModuleConfig moduleConfig = ModuleConfig.from(
                    moduleId, moduleDescriptor, Collections.singletonList(documentConfig),
                    Collections.emptyList(), null, new ArrayList<>());

            Node clientNode = idlSourceGeneratorContext.clientNode();
            NodeList<AnnotationNode> annotations;
            if (clientNode.kind() == SyntaxKind.MODULE_CLIENT_DECLARATION) {
                annotations = ((ModuleClientDeclarationNode) clientNode).annotations();
            } else {
                annotations = ((ClientDeclarationNode) clientNode).annotations();
            }

            idlSourceGeneratorContext.addClient(moduleConfig, annotations);
        }

        private String getAlias(Node clientNode) {
            if (clientNode.kind() == SyntaxKind.MODULE_CLIENT_DECLARATION) {
                return ((ModuleClientDeclarationNode) clientNode).clientPrefix().toString();
            }
            return ((ClientDeclarationNode) clientNode).clientPrefix().toString();
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

        private DocumentConfig getClientCode(DocumentId documentId) {
            return DocumentConfig.from(
                    documentId, "public type ClientConfiguration record {\n" +
                            "    string specVersion;\n" +
                            "};\n" +
                            "\n" +
                            "public isolated client class 'client {\n" +
                            "    public final string specVersion;\n" +
                            "\n" +
                            "    public function init(*ClientConfiguration config) {\n" +
                            "        self.specVersion = config.specVersion;\n" +
                            "    }\n" +
                            "\n" +
                            "    remote function getSpecVersion() returns string {\n" +
                            "        lock {\n" +
                            "            return self.specVersion;\n" +
                            "        }\n" +
                            "    }\n" +
                            "\n" +
                            "}", "idl_client.bal");
        }
    }
}
