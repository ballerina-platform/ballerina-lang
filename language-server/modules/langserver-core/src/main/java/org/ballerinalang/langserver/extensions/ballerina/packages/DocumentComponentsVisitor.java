/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.ballerina.packages;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.text.LineRange;

import java.util.stream.Collectors;

/**
 * The components Visitor class to get the list of module level components.
 */
public class DocumentComponentsVisitor extends NodeVisitor {
    private final JsonObject module;

    private JsonObject currentService = null;
    private JsonObject currentClass = null;
    private boolean isInsideClass;
    private boolean isInsideService;

    DocumentComponentsVisitor(JsonObject module) {
        this.module = module;
    }

    public void getDocumentComponents(Node node) {
        visitSyntaxNode(node);
    }

    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        JsonObject jsonFunction = new JsonObject();
        jsonFunction.addProperty(PackageServiceConstants.NAME, functionDefinitionNode.functionName().text());
        setPositionData(functionDefinitionNode, jsonFunction);
        if (this.isInsideClass) {
            currentClass.getAsJsonArray(PackageServiceConstants.FUNCTIONS).add(jsonFunction);
        } else if (this.isInsideService) {
            currentService.getAsJsonArray(PackageServiceConstants.RESOURCES).add(jsonFunction);
        } else {
            module.getAsJsonArray(PackageServiceConstants.FUNCTIONS).add(jsonFunction);
        }
    }

    public void visit(ListenerDeclarationNode listenerDeclarationNode) {
        addJsonObject(PackageServiceConstants.LISTENERS, listenerDeclarationNode.variableName().text(),
                listenerDeclarationNode);
    }

    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        isInsideService = true;
        currentService = new JsonObject();
        String name = serviceDeclarationNode.absoluteResourcePath().stream().map(node -> String.join("_",
                node.toString())).collect(Collectors.joining());
        currentService.addProperty(PackageServiceConstants.NAME, name);
        currentService.add(PackageServiceConstants.RESOURCES, new JsonArray());
        setPositionData(serviceDeclarationNode, currentService);
        visitSyntaxNode(serviceDeclarationNode);
        module.getAsJsonArray(PackageServiceConstants.SERVICES).add(currentService);
        isInsideService = false;
    }

    public void visit(ClassDefinitionNode classDefinitionNode) {
        isInsideClass = true;
        currentClass = new JsonObject();
        currentClass.addProperty(PackageServiceConstants.NAME, classDefinitionNode.className().text());
        currentClass.add(PackageServiceConstants.FUNCTIONS, new JsonArray());
        setPositionData(classDefinitionNode, currentClass);
        visitSyntaxNode(classDefinitionNode);
        this.module.getAsJsonArray(PackageServiceConstants.CLASSES).add(currentClass);
        isInsideClass = false;
    }

    public void visit(TypeDefinitionNode typeDefinitionNode) {
        JsonObject jsonType = new JsonObject();
        jsonType.addProperty(PackageServiceConstants.NAME, typeDefinitionNode.typeName().text());
        setPositionData(typeDefinitionNode, jsonType);
        if (typeDefinitionNode.typeDescriptor().kind() == SyntaxKind.RECORD_TYPE_DESC) {
            module.getAsJsonArray(PackageServiceConstants.RECORDS).add(jsonType);
        } else if (typeDefinitionNode.typeDescriptor().kind() == SyntaxKind.OBJECT_TYPE_DESC) {
            module.getAsJsonArray(PackageServiceConstants.OBJECTS).add(jsonType);
        } else {
            module.getAsJsonArray(PackageServiceConstants.TYPES).add(jsonType);
        }
    }

    public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        addJsonObject(PackageServiceConstants.MODULE_LEVEL_VARIABLE,
                moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().toString(),
                moduleVariableDeclarationNode);
    }

    public void visit(ConstantDeclarationNode constantDeclarationNode) {
        addJsonObject(PackageServiceConstants.CONSTANTS, constantDeclarationNode.variableName().text(),
                constantDeclarationNode);
    }

    public void visit(EnumDeclarationNode enumDeclarationNode) {
        addJsonObject(PackageServiceConstants.ENUMS, enumDeclarationNode.identifier().text(), enumDeclarationNode);
    }

    /**
     * Set positional data for a node.
     *
     * @param node       {@link Node}
     * @param jsonObject JSON Node to set positional data
     */
    private static void setPositionData(Node node, JsonObject jsonObject) {
        LineRange lineRange = node.lineRange();
        jsonObject.addProperty(PackageServiceConstants.FILE_PATH, lineRange.filePath());
        jsonObject.addProperty(PackageServiceConstants.START_LINE, lineRange.startLine().line());
        jsonObject.addProperty(PackageServiceConstants.START_COLUMN, lineRange.startLine().offset());
        jsonObject.addProperty(PackageServiceConstants.END_LINE, lineRange.endLine().line());
        jsonObject.addProperty(PackageServiceConstants.END_COLUMN, lineRange.endLine().offset());
    }

    /**
     * Create a json object with the component data and add it to the module array.
     *
     * @param type Module component type
     * @param name Object name
     * @param node Node to infer data
     */
    private void addJsonObject(String type, String name, Node node) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PackageServiceConstants.NAME, name);
        setPositionData(node, jsonObject);
        module.getAsJsonArray(type).add(jsonObject);
    }
}
