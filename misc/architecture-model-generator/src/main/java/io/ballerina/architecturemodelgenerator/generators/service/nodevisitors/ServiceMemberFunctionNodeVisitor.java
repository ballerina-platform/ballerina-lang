/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator.generators.service.nodevisitors;

import io.ballerina.architecturemodelgenerator.ComponentModel.PackageId;
import io.ballerina.architecturemodelgenerator.ProjectDesignConstants.ParameterIn;
import io.ballerina.architecturemodelgenerator.model.ElementLocation;
import io.ballerina.architecturemodelgenerator.model.service.Dependency;
import io.ballerina.architecturemodelgenerator.model.service.FunctionParameter;
import io.ballerina.architecturemodelgenerator.model.service.RemoteFunction;
import io.ballerina.architecturemodelgenerator.model.service.Resource;
import io.ballerina.architecturemodelgenerator.model.service.ResourceId;
import io.ballerina.architecturemodelgenerator.model.service.ResourceParameter;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParenthesisedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.projects.Package;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.ballerina.architecturemodelgenerator.generators.GeneratorUtils.findNode;
import static io.ballerina.architecturemodelgenerator.generators.GeneratorUtils.getClientModuleName;
import static io.ballerina.architecturemodelgenerator.generators.GeneratorUtils.getElementLocation;
import static io.ballerina.architecturemodelgenerator.generators.GeneratorUtils.getServiceAnnotation;

/**
 * Visitor class for FunctionDefinition node.
 *
 * @since 2201.2.2
 */
public class ServiceMemberFunctionNodeVisitor extends NodeVisitor {

    private final String serviceId;
    private final SemanticModel semanticModel;
    private final SyntaxTree syntaxTree;
    private final Package currentPackage;
    private List<Resource> resources = new LinkedList<>();
    private List<RemoteFunction> remoteFunctions = new LinkedList<>();
    private final List<Dependency> dependencies = new LinkedList<>();
    private final String filePath;

    public ServiceMemberFunctionNodeVisitor(String serviceId, SemanticModel semanticModel, SyntaxTree syntaxTree,
                                            Package currentPackage, String filePath) {
        this.serviceId = serviceId;
        this.semanticModel = semanticModel;
        this.syntaxTree = syntaxTree;
        this.currentPackage = currentPackage;
        this.filePath = filePath;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public List<RemoteFunction> getRemoteFunctions() {
        return remoteFunctions;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        ElementLocation elementLocation = getElementLocation(filePath,
                functionDefinitionNode.lineRange());
        SyntaxKind kind = functionDefinitionNode.kind();
        switch (kind) {
            case RESOURCE_ACCESSOR_DEFINITION: {
                StringBuilder identifierBuilder = new StringBuilder();
                StringBuilder resourcePathBuilder = new StringBuilder();
                List<ResourceParameter> resourceParameterList = new ArrayList<>();
                NodeList<Node> relativeResourcePaths = functionDefinitionNode.relativeResourcePath();
                for (Node path : relativeResourcePaths) {
                    if (path instanceof ResourcePathParameterNode) {
                        ResourcePathParameterNode pathParam = (ResourcePathParameterNode) path;
                        resourceParameterList.add(getPathParameter(pathParam));
                        identifierBuilder.append(String.format("[%s]",
                                pathParam.typeDescriptor().toSourceCode().trim()));
                    } else {
                        identifierBuilder.append(path);
                    }
                    resourcePathBuilder.append(path);
                }

                String resourcePath = resourcePathBuilder.toString().trim();
                String method = functionDefinitionNode.functionName().text().trim();

                getParameters(functionDefinitionNode.functionSignature(), true,
                        resourceParameterList, null);
                List<String> returnTypes = getReturnTypes(functionDefinitionNode);

                ActionNodeVisitor actionNodeVisitor =
                        new ActionNodeVisitor(semanticModel, currentPackage, filePath);
                functionDefinitionNode.accept(actionNodeVisitor);

                ResourceId resourceId = new ResourceId(this.serviceId, method, resourcePath);
                Resource resource = new Resource(identifierBuilder.toString().trim(),
                        resourceId, resourceParameterList, returnTypes,
                        actionNodeVisitor.getInteractionList(), elementLocation);
                resources.add(resource);

                break;
            }
            case OBJECT_METHOD_DEFINITION: {
                boolean isRemote = functionDefinitionNode.qualifierList().stream().
                        anyMatch(item -> item.kind().equals(SyntaxKind.REMOTE_KEYWORD));
                if (isRemote) {
                    String name = functionDefinitionNode.functionName().text().trim();
                    List<FunctionParameter> parameterList = new ArrayList<>();
                    getParameters(functionDefinitionNode.functionSignature(),
                            false, null, parameterList);
                    List<String> returnTypes = getReturnTypes(functionDefinitionNode);

                    ActionNodeVisitor actionNodeVisitor = new ActionNodeVisitor(
                            semanticModel, currentPackage, filePath);
                    functionDefinitionNode.accept(actionNodeVisitor);

                    RemoteFunction remoteFunction = new RemoteFunction(name, parameterList, returnTypes,
                            actionNodeVisitor.getInteractionList(), elementLocation);
                    remoteFunctions.add(remoteFunction);
                }
                break;
            }
        }
    }

    private ResourceParameter getPathParameter(ResourcePathParameterNode resourcePathParameterNode) {
        ElementLocation elementLocation = getElementLocation(this.filePath,
                resourcePathParameterNode.lineRange());
        String name = resourcePathParameterNode.paramName().get().text();
        List<String> paramTypes = new LinkedList<>();
        Optional<Symbol> symbol = semanticModel.symbol(resourcePathParameterNode);
        if (symbol.isPresent()) {
            PathParameterSymbol parameterSymbol = ((PathParameterSymbol) symbol.get());
            paramTypes = getReferencedType(parameterSymbol.typeDescriptor());
        } // todo : implement else
        return new ResourceParameter(paramTypes, name, ParameterIn.PATH.getValue(), true, elementLocation);
    }

    private void getParameters(FunctionSignatureNode functionSignatureNode, boolean isResource,
                               List<ResourceParameter> resourceParams, List<FunctionParameter> remoteFunctionParams) {

        SeparatedNodeList<ParameterNode> parameterNodes = functionSignatureNode.parameters();
        for (ParameterNode parameterNode : parameterNodes) {
            ElementLocation elementLocation = getElementLocation(this.filePath,
                    parameterNode.lineRange());
            Optional<Symbol> symbol = semanticModel.symbol(parameterNode);
            if (symbol.isPresent() && symbol.get().kind().equals(SymbolKind.PARAMETER)) {
                String paramIn = "";
                String paramName = "";
                boolean isRequired = false;
                ParameterSymbol parameterSymbol = ((ParameterSymbol) symbol.get());
                TypeSymbol typeSymbol = parameterSymbol.typeDescriptor();
                List<String> paramTypes = getReferencedType(typeSymbol);
                switch (parameterNode.kind()) {
                    case REQUIRED_PARAM:
                        RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                        paramIn = getParameterIn(requiredParameterNode.annotations());
                        paramName = requiredParameterNode.paramName().isPresent() ?
                                requiredParameterNode.paramName().get().toString() : "";
                        isRequired = true;
                        break;
                    case DEFAULTABLE_PARAM:
                        DefaultableParameterNode defaultableParameterNode = (DefaultableParameterNode) parameterNode;
                        paramIn = getParameterIn(defaultableParameterNode.annotations());
                        paramName = defaultableParameterNode.paramName().isPresent() ?
                                defaultableParameterNode.paramName().get().toString() : "";
                        break;
                    case INCLUDED_RECORD_PARAM:
                        break;
                    // res params

                }
                if (isResource) {
                    // todo : param kind
                    resourceParams.add(new ResourceParameter(
                            paramTypes, paramName.trim(), paramIn, isRequired, elementLocation));
                } else {
                    remoteFunctionParams.add(new FunctionParameter(paramTypes, paramName, isRequired, elementLocation));
                }
            }
        }
    }

    private String getReferenceEntityName(TypeReferenceTypeSymbol typeReferenceTypeSymbol) {
        PackageId packageId = new PackageId(currentPackage);
        String currentPackageName = String.format
                ("%s/%s:%s", packageId.getOrg(), packageId.getName(), packageId.getVersion());
        String referenceType = typeReferenceTypeSymbol.signature();
        if (typeReferenceTypeSymbol.getModule().isPresent() &&
                !referenceType.split(":")[0].equals(currentPackageName.split(":")[0])) {
            String orgName = typeReferenceTypeSymbol.getModule().get().id().orgName();
            String packageName = typeReferenceTypeSymbol.getModule().get().id().packageName();
            String modulePrefix = typeReferenceTypeSymbol.getModule().get().id().modulePrefix();
            String recordName = typeReferenceTypeSymbol.getName().get();
            String version = typeReferenceTypeSymbol.getModule().get().id().version();
            referenceType = String.format("%s/%s:%s:%s:%s", orgName, packageName, modulePrefix, version, recordName);
        }
        return referenceType;
    }

    private List<String> getReferencedType(TypeSymbol typeSymbol) {
        List<String> paramTypes = new LinkedList<>();
        TypeDescKind typeDescKind = typeSymbol.typeKind();
        switch (typeDescKind) {
            case TYPE_REFERENCE:
                TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
                paramTypes.add(getReferenceEntityName(typeReferenceTypeSymbol).trim());
                break;
            case UNION:
                UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
                List<TypeSymbol> memberTypeDescriptors = unionTypeSymbol.memberTypeDescriptors();
                for (TypeSymbol memberTypeDescriptor : memberTypeDescriptors) {
                    paramTypes.addAll(getReferencedType(memberTypeDescriptor));
                }
                break;
            case ARRAY:
                ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) typeSymbol;
                if (arrayTypeSymbol.memberTypeDescriptor().typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                    paramTypes.add(getReferenceEntityName(
                            (TypeReferenceTypeSymbol) arrayTypeSymbol.memberTypeDescriptor()).trim());
                } else {
                    paramTypes.add(arrayTypeSymbol.signature().trim());
                }
                break;
            case NIL:
                paramTypes.add("null");
                break;
            default:
                paramTypes.add(typeDescKind.getName());
        }
        return paramTypes;
    }

    private String getParameterIn(NodeList<AnnotationNode> annotationNodes) {

        String in = "";
        Optional<AnnotationNode> payloadAnnotation = annotationNodes.stream().filter(annot ->
                annot.toString().trim().equals("@http:Payload")).findAny();
        Optional<AnnotationNode> headerAnnotation = annotationNodes.stream().filter(annot ->
                annot.toString().trim().equals("@http:Header")).findAny();
        // do we need to handle http:Request

        if (payloadAnnotation.isPresent()) {
            in = ParameterIn.BODY.getValue();
        } else if (headerAnnotation.isPresent()) {
            in = ParameterIn.HEADER.getValue();
        } else {
            in = ParameterIn.QUERY.getValue();
        }
        return in;
    }

    private List<String> getReturnTypes(FunctionDefinitionNode functionDefinitionNode) {

        List<String> returnTypes = new ArrayList<>();
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptor = functionSignature.returnTypeDesc();
        if (returnTypeDescriptor.isPresent()) {
            Optional<Symbol> symbol = semanticModel.symbol(functionDefinitionNode);
            if (symbol.isPresent() && symbol.get().kind().equals(SymbolKind.METHOD) ||
                    symbol.get().kind().equals(SymbolKind.RESOURCE_METHOD)) {
                MethodSymbol resourceMethodSymbol = (MethodSymbol) symbol.get();
                Optional<TypeSymbol> returnTypeSymbol = resourceMethodSymbol.typeDescriptor().returnTypeDescriptor();
                returnTypeSymbol.ifPresent(typeSymbol -> returnTypes.addAll(getReferencedType(typeSymbol)));
            }
            // need to split by pipe sign
        }
        return returnTypes;
    }

    @Override
    public void visit(ObjectFieldNode objectFieldNode) {
        if (hasInvocationReferences(objectFieldNode)) {
            return;
        }
        Node fieldTypeName = getReferredNode(objectFieldNode.typeName());
        if (fieldTypeName != null) {
            Optional<Symbol> fieldTypeNameSymbol = semanticModel.symbol(fieldTypeName);
            if (fieldTypeNameSymbol.isPresent()) {
                ClassSymbol referredClassSymbol = getReferredClassSymbol((TypeSymbol) fieldTypeNameSymbol.get());
                if (referredClassSymbol != null) {
                    boolean isClientClass = referredClassSymbol.qualifiers().stream()
                            .anyMatch(qualifier -> qualifier.equals(Qualifier.CLIENT));
                    if (isClientClass) {
                        String serviceId = objectFieldNode.metadata().isPresent() ?
                                getServiceAnnotation(objectFieldNode.metadata().get().annotations(), filePath).getId() :
                                UUID.randomUUID().toString();
                        Dependency dependency = new Dependency(serviceId,
                                getClientModuleName(referredClassSymbol),
                                getElementLocation(filePath, objectFieldNode.lineRange()));
                        dependencies.add(dependency);
                    }
                }
            }
        }
    }

    @Override
    public void visit(ConstantDeclarationNode constantDeclarationNode) {

    }

    private Node getReferredNode(Node typeName) {
        Node qualifiedNameRefNode = null;
        if (typeName.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE) ||
                typeName.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
            qualifiedNameRefNode = typeName;
        } else if (typeName.kind().equals(SyntaxKind.UNION_TYPE_DESC)) {
            Node leftTypeDescNode = getReferredNode(((UnionTypeDescriptorNode) typeName).leftTypeDesc());
            Node rightTypeDescNode = getReferredNode(((UnionTypeDescriptorNode) typeName).rightTypeDesc());
            if (leftTypeDescNode != null && (leftTypeDescNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE) ||
                    leftTypeDescNode.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE))) {
                qualifiedNameRefNode = leftTypeDescNode;
            }
            if (rightTypeDescNode != null && (rightTypeDescNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE) ||
                    rightTypeDescNode.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE))) {
                qualifiedNameRefNode = rightTypeDescNode;
            }
        } else if (typeName.kind().equals(SyntaxKind.PARENTHESISED_TYPE_DESC)) {
            Node typeDescNode = getReferredNode(((ParenthesisedTypeDescriptorNode) typeName).typedesc());
            if (typeDescNode != null && (typeDescNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE) ||
                    typeDescNode.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE))) {
                qualifiedNameRefNode = typeDescNode;
            }
        }
        return qualifiedNameRefNode;
    }

    private ClassSymbol getReferredClassSymbol(TypeSymbol symbol) {
        ClassSymbol classSymbol = null;
        if (symbol.kind().equals(SymbolKind.CLASS)) {
            classSymbol = (ClassSymbol) symbol;
        } else if (symbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
            TypeReferenceTypeSymbol typeRefTypeSymbol = (TypeReferenceTypeSymbol) symbol;
            TypeSymbol typeDescTypeSymbol = typeRefTypeSymbol.typeDescriptor();
            classSymbol = getReferredClassSymbol(typeDescTypeSymbol);
        }
        return classSymbol;
    }

    private boolean hasInvocationReferences(ObjectFieldNode clientDeclarationNode) {
        Optional<Symbol> objFieldNodeSymbol = semanticModel.symbol(clientDeclarationNode);
        if (objFieldNodeSymbol.isEmpty()) {
            return false;
        }
        List<LineRange> objFieldNodeRefs = semanticModel.references(objFieldNodeSymbol.get())
                .stream().map(Location::lineRange).collect(Collectors.toList());
        for (LineRange lineRange : objFieldNodeRefs) {
            Node referredNode = findNode(syntaxTree, lineRange);
            while (!referredNode.kind().equals(SyntaxKind.SERVICE_DECLARATION) &&
                    !referredNode.kind().equals(SyntaxKind.MODULE_PART)) {
                if (referredNode.kind().equals(SyntaxKind.REMOTE_METHOD_CALL_ACTION) ||
                        referredNode.kind().equals(SyntaxKind.CLIENT_RESOURCE_ACCESS_ACTION)) {
                    return true;
                }
                referredNode = referredNode.parent();
            }
        }
        return false;
    }
}
