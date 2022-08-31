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
 * IDL client generation test class.
 */
public class OpenApiIDLGenPlugin extends IDLGeneratorPlugin {
    @Override
    public void init(IDLPluginContext pluginContext) {
        pluginContext.addCodeGenerator(new OpenAPIClientGenerator());
    }

    private static class OpenAPIClientGenerator extends IDLClientGenerator {

        @Override
        public boolean canHandle(Node clientNode) {
            return !getUri(clientNode).startsWith("http://example.com");
        }

        @Override
        public void perform(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            ModuleId moduleId = ModuleId.create("client1",
                    idlSourceGeneratorContext.currentPackage().packageId());
            DocumentId documentId = DocumentId.create("openApiClient", moduleId);
            DocumentConfig documentConfig = DocumentConfig.from(
                    documentId, "type openApiClient record {};", "openApiClient");
            ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                    ModuleName.from(idlSourceGeneratorContext.currentPackage().packageName(), "client1"),
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

    }
}
