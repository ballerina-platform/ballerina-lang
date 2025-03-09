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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationAttachmentSymbol;
import io.ballerina.compiler.api.symbols.ExternalFunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
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
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.text.LineRange;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The node transformer class to get the list of module level components.
 */
public class DocumentComponentTransformer extends NodeTransformer<Optional<MapperObject>> {

    private final ModuleObject module;
    private final SemanticModel semanticModel;

    private static final String BALLERINAX_ORG_NAME = "ballerinax";
    private static final String NP_MODULE_NAME = "np";
    private static final String LLM_CALL = "LlmCall";

    DocumentComponentTransformer(ModuleObject moduleObject, SemanticModel semanticModel) {
        this.module = moduleObject;
        this.semanticModel = semanticModel;
    }

    public ModuleObject getModuleObject(Node node) {
        this.transformSyntaxNode(node);
        return this.module;
    }

    @Override
    public Optional<MapperObject> transformSyntaxNode(Node node) {
        if (!(node instanceof NonTerminalNode nonTerminalNode)) {
            return Optional.empty();
        }
        nonTerminalNode.children().forEach(child -> {
            Optional<MapperObject> mapperObject = child.apply(this);
            if (mapperObject != null) {
                mapperObject.ifPresent(this.module::addDataObject);
            }
        });
        return Optional.empty();
    }

    @Override
    public Optional<MapperObject> transform(FunctionDefinitionNode functionDefinitionNode) {
        if (functionDefinitionNode.functionName().text().equals(PackageServiceConstants.MAIN_FUNCTION)) {
            return Optional.of(new MapperObject(PackageServiceConstants.AUTOMATIONS,
                    createDataObject(PackageServiceConstants.MAIN_FUNCTION, functionDefinitionNode)));
        }

        if (isPromptAsCodeFunction(functionDefinitionNode)) {
            return Optional.of(new MapperObject(PackageServiceConstants.PROMPT_AS_CODE,
                    createDataObject(functionDefinitionNode.functionName().text(), functionDefinitionNode)));
        }

        return Optional.of(new MapperObject(PackageServiceConstants.FUNCTIONS,
                createDataObject(functionDefinitionNode.functionName().text(), functionDefinitionNode)));
    }

    @Override
    public Optional<MapperObject> transform(ListenerDeclarationNode listenerDeclarationNode) {
        return Optional.of(new MapperObject(PackageServiceConstants.LISTENERS,
                createDataObject(listenerDeclarationNode.variableName().text(), listenerDeclarationNode)));
    }

    @Override
    public Optional<MapperObject> transform(ServiceDeclarationNode serviceDeclarationNode) {
        String name = serviceDeclarationNode.absoluteResourcePath().stream().map(node -> String.join("_",
                node.toString())).collect(Collectors.joining());
        if (name.isEmpty()) {
            name = serviceDeclarationNode.typeDescriptor().map(typeDescriptorNode ->
                    typeDescriptorNode.toSourceCode().strip()).orElse("");
        }

        DataObject dataObject = createDataObject(name, serviceDeclarationNode);
        serviceDeclarationNode.members().forEach(member -> {
            if (member.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) member;
                String resourceName = functionDefinitionNode.functionName().text() + "-" +
                        functionDefinitionNode.relativeResourcePath().stream()
                                .map(Node::toSourceCode)
                                .collect(Collectors.joining(""));
                dataObject.addResource(createDataObject(resourceName, member));
            } else if (member.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION) {
                FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) member;
                String functionName = functionDefinitionNode.functionName().text();
                dataObject.addFunction(this.createDataObject(functionName, member));
            }
        });
        return Optional.of(new MapperObject(PackageServiceConstants.SERVICES, dataObject));
    }

    @Override
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

    @Override
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

    @Override
    public Optional<MapperObject> transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        Optional<Token> isConfigurable = moduleVariableDeclarationNode.qualifiers().stream()
                .filter(qualifier -> qualifier.kind() == SyntaxKind.CONFIGURABLE_KEYWORD)
                .findFirst();
        if (isConfigurable.isPresent()) {
            return Optional.of(new MapperObject(PackageServiceConstants.CONFIGURABLE_VARIABLES,
                    createDataObject(moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().toString(),
                            moduleVariableDeclarationNode)));
        }
        return Optional.of(new MapperObject(PackageServiceConstants.MODULE_LEVEL_VARIABLE,
                createDataObject(moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().toString(),
                        moduleVariableDeclarationNode)));
    }

    @Override
    public Optional<MapperObject> transform(ConstantDeclarationNode constantDeclarationNode) {
        return Optional.of(new MapperObject(PackageServiceConstants.CONSTANTS,
                createDataObject(constantDeclarationNode.variableName().text(), constantDeclarationNode)));
    }

    @Override
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
        return new DataObject(name, lineRange.fileName(), lineRange.startLine().line(), lineRange.startLine().offset(),
                lineRange.endLine().line(), lineRange.endLine().offset());
    }

    /**
     * Check whether the given function is a prompt as code function.
     *
     * @param functionDefinitionNode Function definition node
     * @return true if the function is a prompt as code function else false
     */
    private boolean isPromptAsCodeFunction(FunctionDefinitionNode functionDefinitionNode) {
        Optional<Symbol> funcSymbol = this.semanticModel.symbol(functionDefinitionNode);
        if (funcSymbol.isEmpty() || funcSymbol.get().kind() != SymbolKind.FUNCTION
                || !((FunctionSymbol) funcSymbol.get()).external()) {
            return false;
        }

        List<AnnotationAttachmentSymbol> annotAttachments =
                ((ExternalFunctionSymbol) funcSymbol.get()).annotAttachmentsOnExternal();
        return annotAttachments.stream().anyMatch(annot ->
                isNpModule(annot.typeDescriptor())
                        && annot.typeDescriptor().getName().isPresent()
                        && annot.typeDescriptor().getName().get().equals(LLM_CALL));
    }

    private boolean isNpModule(Symbol symbol) {
        Optional<ModuleSymbol> module = symbol.getModule();
        if (module.isEmpty()) {
            return false;
        }

        ModuleID moduleId = module.get().id();
        return moduleId.orgName().equals(BALLERINAX_ORG_NAME) && moduleId.packageName().equals(NP_MODULE_NAME);
    }
}
