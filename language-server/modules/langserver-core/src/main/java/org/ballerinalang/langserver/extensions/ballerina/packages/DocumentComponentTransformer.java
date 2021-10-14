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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.text.LineRange;

import java.util.stream.Collectors;

/**
 * The node transformer class to get the list of module level components.
 */
public class DocumentComponentTransformer extends NodeTransformer<JsonElement> {
    private final JsonObject module;

    DocumentComponentTransformer(JsonObject module) {
        this.module = module;
    }

    @Override
    public JsonElement transformSyntaxNode(Node node) {
        if (!(node instanceof NonTerminalNode)) {
            return this.module;
        }
        ((NonTerminalNode) node).children().forEach(child -> {
            child.apply(this);
        });
        return this.module;
    }

    public JsonElement transform(FunctionDefinitionNode functionDefinitionNode) {
        JsonObject jsonFunction = new JsonObject();
        jsonFunction.addProperty(PackageServiceConstants.NAME, functionDefinitionNode.functionName().text());
        setPositionData(functionDefinitionNode, jsonFunction);
        module.getAsJsonArray(PackageServiceConstants.FUNCTIONS).add(jsonFunction);
        return null;
    }

    public JsonElement transform(ListenerDeclarationNode listenerDeclarationNode) {
        addJsonObject(PackageServiceConstants.LISTENERS, listenerDeclarationNode.variableName().text(),
                listenerDeclarationNode, this.module);
        return null;
    }

    public JsonElement transform(ServiceDeclarationNode serviceDeclarationNode) {
        JsonObject jsonService = new JsonObject();
        String name = serviceDeclarationNode.absoluteResourcePath().stream().map(node -> String.join("_",
                node.toString())).collect(Collectors.joining());
        jsonService.addProperty(PackageServiceConstants.NAME, name);
        jsonService.add(PackageServiceConstants.RESOURCES, new JsonArray());
        setPositionData(serviceDeclarationNode, jsonService);
        serviceDeclarationNode.members().forEach(member -> {
            if (member.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
                addJsonObject(PackageServiceConstants.RESOURCES,
                        ((FunctionDefinitionNode) member).functionName().text(), member, jsonService);
            }
        });
        module.getAsJsonArray(PackageServiceConstants.SERVICES).add(jsonService);
        return null;
    }

    public JsonElement transform(ClassDefinitionNode classDefinitionNode) {
        JsonObject jsonClass = new JsonObject();
        jsonClass.addProperty(PackageServiceConstants.NAME, classDefinitionNode.className().text());
        jsonClass.add(PackageServiceConstants.FUNCTIONS, new JsonArray());
        setPositionData(classDefinitionNode, jsonClass);
        classDefinitionNode.members().forEach(member -> {
            if (member.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                addJsonObject(PackageServiceConstants.FUNCTIONS,
                        ((FunctionDefinitionNode) member).functionName().text(), member, jsonClass);
            }
        });
        this.module.getAsJsonArray(PackageServiceConstants.CLASSES).add(jsonClass);
        return null;
    }

    public JsonElement transform(TypeDefinitionNode typeDefinitionNode) {
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
        return null;
    }

    public JsonElement transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        addJsonObject(PackageServiceConstants.MODULE_LEVEL_VARIABLE,
                moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().toString(),
                moduleVariableDeclarationNode, this.module);
        return null;
    }

    public JsonElement transform(ConstantDeclarationNode constantDeclarationNode) {
        addJsonObject(PackageServiceConstants.CONSTANTS, constantDeclarationNode.variableName().text(),
                constantDeclarationNode, this.module);
        return null;
    }

    public JsonElement transform(EnumDeclarationNode enumDeclarationNode) {
        addJsonObject(PackageServiceConstants.ENUMS, enumDeclarationNode.identifier().text(), enumDeclarationNode,
                this.module);
        return null;
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
     * @param parentObject The parent json object into which the new object is added
     */
    private void addJsonObject(String type, String name, Node node, JsonObject parentObject) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PackageServiceConstants.NAME, name);
        setPositionData(node, jsonObject);
        parentObject.getAsJsonArray(type).add(jsonObject);
    }
}
