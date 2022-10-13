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

package io.ballerina.componentmodel.servicemodel.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
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
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.componentmodel.ComponentModel;
import io.ballerina.componentmodel.ComponentModelingConstants.ParameterIn;
import io.ballerina.componentmodel.servicemodel.components.FunctionParameter;
import io.ballerina.componentmodel.servicemodel.components.RemoteFunction;
import io.ballerina.componentmodel.servicemodel.components.Resource;
import io.ballerina.componentmodel.servicemodel.components.ResourceId;
import io.ballerina.componentmodel.servicemodel.components.ResourceParameter;
import io.ballerina.projects.Package;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Visitor class for FunctionDefinition node.
 *
 * @since 2201.2.2
 */
public class ServiceMemberFunctionNodeVisitor extends NodeVisitor {

    private final String serviceId;
    private final SemanticModel semanticModel;
    private final Package currentPackage;
    List<Resource> resources = new LinkedList<>();
    List<RemoteFunction> remoteFunctions = new LinkedList<>();
    private final ComponentModel.PackageId packageId;

    public ServiceMemberFunctionNodeVisitor(String serviceId, SemanticModel semanticModel, Package currentPackage,
                                            ComponentModel.PackageId packageId) {

        this.serviceId = serviceId;
        this.semanticModel = semanticModel;
        this.currentPackage = currentPackage;
        this.packageId = packageId;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public List<RemoteFunction> getRemoteFunctions() {
        return remoteFunctions;
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {

        SyntaxKind kind = functionDefinitionNode.kind();
        switch (kind) {
            case RESOURCE_ACCESSOR_DEFINITION: {
                StringBuilder identifierBuilder = new StringBuilder();
                StringBuilder resourcePathBuilder = new StringBuilder();
                NodeList<Node> relativeResourcePaths = functionDefinitionNode.relativeResourcePath();
                for (Node path : relativeResourcePaths) {
                    if (path instanceof ResourcePathParameterNode) {
                        ResourcePathParameterNode pathParam = (ResourcePathParameterNode) path;
                        identifierBuilder.append(String.format("[%s]",
                                pathParam.typeDescriptor().toSourceCode().trim()));
                    } else {
                        identifierBuilder.append(path);
                    }
                    resourcePathBuilder.append(path);
                }

                String resourcePath = resourcePathBuilder.toString().trim();
                String method = functionDefinitionNode.functionName().text().trim();

                List<ResourceParameter> resourceParameterList = new ArrayList<>();
                getParameters(functionDefinitionNode.functionSignature(), true,
                        resourceParameterList, null);
                List<String> returnTypes = getReturnTypes(functionDefinitionNode);

                ActionNodeVisitor actionNodeVisitor =
                        new ActionNodeVisitor(semanticModel, currentPackage);
                functionDefinitionNode.accept(actionNodeVisitor);

                ResourceId resourceId = new ResourceId(this.serviceId, method, resourcePath);
                Resource resource = new Resource(identifierBuilder.toString().trim(),
                        resourceId, resourceParameterList, returnTypes, actionNodeVisitor.getInteractionList());
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

                    ActionNodeVisitor actionNodeVisitor = new ActionNodeVisitor(semanticModel, currentPackage);
                    functionDefinitionNode.accept(actionNodeVisitor);

                    RemoteFunction remoteFunction = new RemoteFunction(name, parameterList, returnTypes,
                            actionNodeVisitor.getInteractionList());
                    remoteFunctions.add(remoteFunction);
                }
                break;
            }
        }
    }

    private void getParameters(FunctionSignatureNode functionSignatureNode, boolean isResource,
                               List<ResourceParameter> resourceParams, List<FunctionParameter> remoteFunctionParams) {

        SeparatedNodeList<ParameterNode> parameterNodes = functionSignatureNode.parameters();
        for (ParameterNode parameterNode : parameterNodes) {
            Optional<Symbol> symbol = semanticModel.symbol(parameterNode);
            if (symbol.isPresent() && symbol.get().kind().equals(SymbolKind.PARAMETER)) {
                String paramIn = "";
                String paramName = "";
                boolean isRequired = false;
                ParameterSymbol parameterSymbol = ((ParameterSymbol) symbol.get());
                TypeSymbol typeSymbol = parameterSymbol.typeDescriptor();
                List<String> paramTypes = new ArrayList<>();
                getReferencedType(typeSymbol, paramTypes);
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
                    resourceParams.add(new ResourceParameter(paramTypes, paramName.trim(), paramIn, isRequired));
                } else {
                    remoteFunctionParams.add(new FunctionParameter(paramTypes, paramName, isRequired));
                }
            }
        }
    }

    private String getReferenceEntityName(TypeReferenceTypeSymbol typeReferenceTypeSymbol) {

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

    private void getReferencedType(TypeSymbol typeSymbol, List<String> paramTypes) {

        TypeDescKind typeDescKind = typeSymbol.typeKind();
        switch (typeDescKind) {
            case TYPE_REFERENCE:
                TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
                paramTypes.add(getReferenceEntityName(typeReferenceTypeSymbol).trim());
                break;
            case UNION:
                UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
                List<TypeSymbol> memberTypeDescriptors = unionTypeSymbol.memberTypeDescriptors();
                memberTypeDescriptors.forEach(member -> getReferencedType(member, paramTypes));
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
                returnTypeSymbol.ifPresent(typeSymbol -> getReferencedType(typeSymbol, returnTypes));
            }
            // need to split by pipe sign
        }
        return returnTypes;
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {

    }

    @Override
    public void visit(ConstantDeclarationNode constantDeclarationNode) {

    }
}
