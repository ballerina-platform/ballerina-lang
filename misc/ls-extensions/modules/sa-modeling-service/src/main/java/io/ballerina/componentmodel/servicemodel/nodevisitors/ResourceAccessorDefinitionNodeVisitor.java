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

package io.ballerina.componentmodel.servicemodel.nodevisitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.NilTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.componentmodel.ComponentModel;
import io.ballerina.componentmodel.ComponentModelingConstants.ParameterIn;
import io.ballerina.componentmodel.servicemodel.components.Parameter;
import io.ballerina.componentmodel.servicemodel.components.Resource;
import io.ballerina.componentmodel.servicemodel.components.ResourceId;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Visitor class for FunctionDefinition node.
 */
public class ResourceAccessorDefinitionNodeVisitor extends NodeVisitor {
    private final String serviceId;

    private final SemanticModel semanticModel;
    List<Resource> resources = new ArrayList<>();

    private final ComponentModel.PackageId packageId;

    public ResourceAccessorDefinitionNodeVisitor(String serviceId, SemanticModel semanticModel, ComponentModel.PackageId packageId) {
        this.serviceId = serviceId;
        this.semanticModel = semanticModel;
        this.packageId = packageId;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        SyntaxKind kind = functionDefinitionNode.kind();
        if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
            StringBuilder identifierBuilder = new StringBuilder();
            StringBuilder resourcePathBuilder = new StringBuilder();
            NodeList<Node> relativeResourcePaths = functionDefinitionNode.relativeResourcePath();
            for (Node path : relativeResourcePaths) {
                if (path instanceof ResourcePathParameterNode) {
                    ResourcePathParameterNode pathParam = (ResourcePathParameterNode) path;
                    identifierBuilder.append(String.format("[%s]", pathParam.typeDescriptor().toSourceCode().trim()));
                } else {
                    identifierBuilder.append(path);
                }
                resourcePathBuilder.append(path);
            }

            String resourcePath = resourcePathBuilder.toString().trim();
            String method = functionDefinitionNode.functionName().text().trim();

            List<Parameter> parameterList = getParameters(functionDefinitionNode.functionSignature());
            List<String> returnTypes = getReturnTypes(functionDefinitionNode);

            ActionNodeVisitor actionNodeVisitor =
                    new ActionNodeVisitor(semanticModel);
            functionDefinitionNode.accept(actionNodeVisitor);

            ResourceId resourceId = new ResourceId(this.serviceId, method, resourcePath);

            // todo : complete the interactions
            Resource resource = new Resource(identifierBuilder.toString().trim(),resourceId, parameterList, returnTypes,
                    actionNodeVisitor.getInteractionList());
            resources.add(resource);
        }
    }

    private String getResourceIdentifier() {
        return "";
    }

    private List<Parameter> getParameters(FunctionSignatureNode functionSignatureNode) {
        List<Parameter> parameterList = new ArrayList<>();
        SeparatedNodeList<ParameterNode> parameterNodes = functionSignatureNode.parameters();
        for (ParameterNode parameterNode : parameterNodes) {
            Optional<Symbol> symbol = semanticModel.symbol(parameterNode);
            if (symbol.isPresent() && symbol.get().kind().equals(SymbolKind.PARAMETER)) {
                ParameterSymbol parameterSymbol = ((ParameterSymbol) symbol.get());
                TypeSymbol typeSymbol = parameterSymbol.typeDescriptor();
                List<String> paramTypes = new ArrayList<>();
                getReferencedType(typeSymbol, paramTypes);
                switch (parameterNode.kind()) {
                    case REQUIRED_PARAM:
                        RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                        String requiredParamIn = getParameterIn(requiredParameterNode.annotations());
                        String requiredParamName = requiredParameterNode.paramName().isPresent() ?
                                requiredParameterNode.paramName().get().toString() : "";
                        parameterList.add(new Parameter(paramTypes,
                                requiredParamName.trim(), requiredParamIn, true));
                        break;
                    case DEFAULTABLE_PARAM:
                        DefaultableParameterNode defaultableParameterNode = (DefaultableParameterNode) parameterNode;
                        String defaultableParamIn = getParameterIn(defaultableParameterNode.annotations());
                        String defaultableParamName = defaultableParameterNode.paramName().isPresent() ?
                                defaultableParameterNode.paramName().get().toString() : "";
                        parameterList.add(new Parameter(paramTypes,
                                defaultableParamName.trim(), defaultableParamIn, false));
                        break;
                    case INCLUDED_RECORD_PARAM:
                        break;
                    // res params

                }
            }
        }
        return parameterList;
    }

    private String getReferenceEntityName(TypeReferenceTypeSymbol typeReferenceTypeSymbol) {
        String currentPackageName = String.format("%s/%s:%s", packageId.getOrg(), packageId.getName(), packageId.getVersion());
        String referenceType = typeReferenceTypeSymbol.signature();
        if (typeReferenceTypeSymbol.getModule().isPresent() &&
                !referenceType.split(":")[0].equals(currentPackageName.split(":")[0])) {
            String orgName = typeReferenceTypeSymbol.getModule().get().id().orgName();
            String packageName =  typeReferenceTypeSymbol.getModule().get().id().packageName();
            String modulePrefix = typeReferenceTypeSymbol.getModule().get().id().modulePrefix();
            String recordName = typeReferenceTypeSymbol.getName().get();
            String version = typeReferenceTypeSymbol.getModule().get().id().version();
            referenceType = String.format("%s/%s:%s:%s:%s", orgName, packageName, modulePrefix, version, recordName);
        }
        return referenceType;
    }

    private void getReferencedType(TypeSymbol typeSymbol, List<String> paramTypes) {
        TypeDescKind typeDescKind = typeSymbol.typeKind();
        if (typeDescKind.equals(TypeDescKind.TYPE_REFERENCE)) {
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
            paramTypes.add(getReferenceEntityName(typeReferenceTypeSymbol).trim());
        } else if (typeDescKind.equals(TypeDescKind.UNION)) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
            List<TypeSymbol> memberTypeDescriptors = unionTypeSymbol.memberTypeDescriptors();
            for (TypeSymbol memberTypeSymbol : memberTypeDescriptors) {
                if (memberTypeSymbol instanceof NilTypeSymbol) {
                    paramTypes.add("null");
                } else {
                    getReferencedType(memberTypeSymbol, paramTypes);
                }
            }
        } else if (typeDescKind.equals(TypeDescKind.ARRAY)) {
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) typeSymbol;
            if (arrayTypeSymbol.memberTypeDescriptor().typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                paramTypes.add(getReferenceEntityName((TypeReferenceTypeSymbol) arrayTypeSymbol.memberTypeDescriptor()).trim());
            } else {
                paramTypes.add(arrayTypeSymbol.signature().trim());
            }

        } else {
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
            if (symbol.isPresent() && symbol.get().kind().equals(SymbolKind.RESOURCE_METHOD)) {
                ResourceMethodSymbol resourceMethodSymbol = (ResourceMethodSymbol) symbol.get();
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
