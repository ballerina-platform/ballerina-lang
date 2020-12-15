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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.tools.text.LinePosition;

import java.util.Optional;

/**
 * Visitor class for module visitor.
 */
public class ModuleVisitor extends NodeVisitor {

    private final JsonObject jsonModule;
    private final JsonArray functions;
    private final JsonArray services;
    private JsonArray resources;
    private String filePath;

    ModuleVisitor(JsonObject jsonModule) {
        this.jsonModule = jsonModule;
        this.functions = new JsonArray();
        this.services = new JsonArray();
        this.resources = new JsonArray();
    }

    public void visit(FunctionDefinitionNode functionNode) {
        JsonObject node = new JsonObject();
        node.addProperty(ProjectConstants.FILE_PATH, this.filePath);
        node.addProperty(ProjectConstants.NAME, functionNode.functionName().text());
        node.add(ProjectConstants.POSITION, createPositionObject(functionNode.lineRange().startLine()));
        if (functionNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
            this.resources.add(node);
        } else {
            functions.add(node);
        }
    }

    public void visit(ServiceDeclarationNode serviceNode) {
        JsonObject service = new JsonObject();
        Optional<TypeDescriptorNode> serviceNodeName = serviceNode.typeDescriptor();
        service.addProperty(ProjectConstants.FILE_PATH, this.filePath);
        serviceNodeName.ifPresent(typeDescriptorNode -> service.addProperty(ProjectConstants.NAME,
                typeDescriptorNode.toString().strip()));
        service.add(ProjectConstants.POSITION, createPositionObject(serviceNode.lineRange().startLine()));
        this.resources = new JsonArray();
        visitSyntaxNode(serviceNode);
        service.add(ProjectConstants.RESOURCES, this.resources);
        this.services.add(service);
    }

    /**
     * Visits module node.
     *
     * @param node     Root node
     * @param filePath Ballerina file path
     */
    public void visitPackage(Node node, String filePath) {
        this.filePath = filePath;
        visitSyntaxNode(node);
        this.jsonModule.add(ProjectConstants.FUNCTIONS, this.functions);
        this.jsonModule.add(ProjectConstants.SERVICES, this.services);
    }

    /**
     * Returns JSON object with module details.
     *
     * @return {@link JsonObject} Module object
     */
    public JsonObject getJsonModule() {
        return this.jsonModule;
    }

    /**
     * Creates position object with startLine and startColumn.
     *
     * @param linePosition {@link LinePosition} Start position
     * @return Position object
     */
    private JsonObject createPositionObject(LinePosition linePosition) {
        JsonObject position = new JsonObject();
        position.addProperty(ProjectConstants.START_LINE, linePosition.line());
        position.addProperty(ProjectConstants.START_COLUMN, linePosition.offset());
        return position;
    }
}
