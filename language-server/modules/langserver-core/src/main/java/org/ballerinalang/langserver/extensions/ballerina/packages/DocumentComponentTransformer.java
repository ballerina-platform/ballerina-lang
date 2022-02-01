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

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The node transformer class to get the list of module level components.
 */
public class DocumentComponentTransformer extends NodeTransformer<Optional<MapperObject>> {
    private final ModuleObject module;

    DocumentComponentTransformer(ModuleObject moduleObject) {
        this.module = moduleObject;
    }

    public ModuleObject getModuleObject(Node node) {
        this.transformSyntaxNode(node);
        return this.module;
    }

    @Override
    public Optional<MapperObject> transformSyntaxNode(Node node) {
        if (!(node instanceof NonTerminalNode)) {
            return Optional.empty();
        }
        ((NonTerminalNode) node).children().forEach(child -> {
            Optional<MapperObject> mapperObject = child.apply(this);
            if (mapperObject != null) {
                mapperObject.ifPresent(this.module::addDataObject);
            }
        });
        return Optional.empty();
    }

    public Optional<MapperObject> transform(FunctionDefinitionNode functionDefinitionNode) {
        return Optional.of(new MapperObject(PackageServiceConstants.FUNCTIONS,
                createDataObject(functionDefinitionNode.functionName().text(), functionDefinitionNode)));
    }

    public Optional<MapperObject> transform(ListenerDeclarationNode listenerDeclarationNode) {
        return Optional.of(new MapperObject(PackageServiceConstants.LISTENERS,
                createDataObject(listenerDeclarationNode.variableName().text(), listenerDeclarationNode)));
    }

    public Optional<MapperObject> transform(ServiceDeclarationNode serviceDeclarationNode) {
        String name = serviceDeclarationNode.absoluteResourcePath().stream().map(node -> String.join("_",
                node.toString())).collect(Collectors.joining());
        DataObject dataObject = createDataObject(name, serviceDeclarationNode);
        serviceDeclarationNode.members().forEach(member -> {
            if (member.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
                dataObject.addResource(createDataObject(((FunctionDefinitionNode) member).functionName().text(),
                        member));
            }
        });
        return Optional.of(new MapperObject(PackageServiceConstants.SERVICES, dataObject));
    }

    public Optional<MapperObject> transform(ClassDefinitionNode classDefinitionNode) {
        DataObject dataObject = createDataObject(classDefinitionNode.className().text(), classDefinitionNode);
        classDefinitionNode.members().forEach(member -> {
            if (member.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                dataObject.addFunction(createDataObject(((FunctionDefinitionNode) member).functionName().text(),
                        member));
            }
        });
        return Optional.of(new MapperObject(PackageServiceConstants.CLASSES, dataObject));
    }

    public Optional<MapperObject> transform(TypeDefinitionNode typeDefinitionNode) {
        if (typeDefinitionNode.typeDescriptor().kind() == SyntaxKind.RECORD_TYPE_DESC) {
            return Optional.of(new MapperObject(PackageServiceConstants.RECORDS,
                    createDataObject(typeDefinitionNode.typeName().text(), typeDefinitionNode)));
        } else if (typeDefinitionNode.typeDescriptor().kind() == SyntaxKind.OBJECT_TYPE_DESC) {
            return Optional.of(new MapperObject(PackageServiceConstants.OBJECTS,
                    createDataObject(typeDefinitionNode.typeName().text(), typeDefinitionNode)));
        } else {
            return Optional.of(new MapperObject(PackageServiceConstants.TYPES,
                    createDataObject(typeDefinitionNode.typeName().text(), typeDefinitionNode)));
        }
    }

    public Optional<MapperObject> transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        return Optional.of(new MapperObject(PackageServiceConstants.MODULE_LEVEL_VARIABLE,
                createDataObject(moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().toString(),
                moduleVariableDeclarationNode)));
    }

    public Optional<MapperObject> transform(ConstantDeclarationNode constantDeclarationNode) {
        return Optional.of(new MapperObject(PackageServiceConstants.CONSTANTS,
                createDataObject(constantDeclarationNode.variableName().text(), constantDeclarationNode)));
    }

    public Optional<MapperObject> transform(EnumDeclarationNode enumDeclarationNode) {
        return Optional.of(new MapperObject(PackageServiceConstants.ENUMS,
                createDataObject(enumDeclarationNode.identifier().text(), enumDeclarationNode)));
    }

    /**
     * Create a json object with the component data and add it to the module array.
     *
     * @param name Object name
     * @param node Node to infer data
     */
    private DataObject createDataObject(String name, Node node) {
        LineRange lineRange = node.lineRange();
        return new DataObject(name, lineRange.filePath(), lineRange.startLine().line(), lineRange.startLine().offset(),
                lineRange.endLine().line(), lineRange.endLine().offset());
    }
}
