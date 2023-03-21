/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator.generators.entrypoint.nodevisitors;

import io.ballerina.architecturemodelgenerator.generators.service.nodevisitors.ActionNodeVisitor;
import io.ballerina.architecturemodelgenerator.model.ElementLocation;
import io.ballerina.architecturemodelgenerator.model.EntryPoint;
import io.ballerina.architecturemodelgenerator.model.service.DisplayAnnotation;
import io.ballerina.architecturemodelgenerator.model.service.FunctionParameter;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Annotatable;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.architecturemodelgenerator.ProjectDesignConstants.MAIN;
import static io.ballerina.architecturemodelgenerator.generators.GeneratorUtils.getElementLocation;
import static io.ballerina.architecturemodelgenerator.generators.GeneratorUtils.getReferencedType;
import static io.ballerina.architecturemodelgenerator.generators.GeneratorUtils.getServiceAnnotation;

/**
 * Build entry point model based on a given Ballerina package.
 *
 * @since 2201.2.2
 */
public class FunctionNodeVisitor extends NodeVisitor {

    private final PackageCompilation packageCompilation;
    private final SemanticModel semanticModel;
    private final Package currentPackage;
    private EntryPoint entryPoint = null;
    private final Path filePath;

    public FunctionNodeVisitor(PackageCompilation packageCompilation, SemanticModel semanticModel,
                               Package currentPackage, Path filePath) {
        this.packageCompilation = packageCompilation;
        this.semanticModel = semanticModel;
        this.currentPackage = currentPackage;
        this.filePath = filePath;
    }

    public EntryPoint getEntryPoint() {
        return entryPoint;
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        if (functionDefinitionNode.functionName().text().equals(MAIN)) {
            DisplayAnnotation annotation = null;
            Optional<Symbol> clientSymbol = semanticModel.symbol(functionDefinitionNode);
            if (clientSymbol.isPresent()) {
                Annotatable annotatableSymbol = (Annotatable) clientSymbol.get();
                annotation = getServiceAnnotation(annotatableSymbol, filePath.toString());
            }

            ElementLocation elementLocation = getElementLocation(filePath.toString(),
                    functionDefinitionNode.lineRange());
            List<FunctionParameter> funcParamList = new ArrayList<>();

            getParameters(functionDefinitionNode.functionSignature(), funcParamList);
            List<String> returnTypes = getMainReturnTypes(functionDefinitionNode);

            ActionNodeVisitor actionNodeVisitor =
                    new ActionNodeVisitor(packageCompilation, semanticModel, currentPackage, filePath.toString());
            functionDefinitionNode.accept(actionNodeVisitor);

            entryPoint = new EntryPoint(annotation == null ? null : annotation.getId(), funcParamList,
                    returnTypes, actionNodeVisitor.getInteractionList(), annotation, elementLocation);
        }
    }

    private List<String> getMainReturnTypes(FunctionDefinitionNode functionDefinitionNode) {
        List<String> returnTypes = new ArrayList<>();
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptor = functionSignature.returnTypeDesc();
        if (returnTypeDescriptor.isPresent()) {
            Optional<Symbol> symbol = semanticModel.symbol(functionDefinitionNode);
            if (symbol.isPresent()) {
                FunctionSymbol functionSymbol = (FunctionSymbol) symbol.get();
                Optional<TypeSymbol> returnTypeSymbol = functionSymbol.typeDescriptor().returnTypeDescriptor();
                returnTypeSymbol.ifPresent(typeSymbol ->
                        returnTypes.addAll(getReferencedType(typeSymbol, currentPackage)));
            }
            // need to split by pipe sign
        }
        return returnTypes;
    }

    private void getParameters(FunctionSignatureNode functionSignatureNode,
                               List<FunctionParameter> functionParameters) {
        SeparatedNodeList<ParameterNode> parameterNodes = functionSignatureNode.parameters();
        for (ParameterNode parameterNode : parameterNodes) {
            ElementLocation elementLocation = getElementLocation(this.filePath.toString(),
                    parameterNode.lineRange());
            Optional<Symbol> symbol = semanticModel.symbol(parameterNode);
            if (symbol.isPresent() && symbol.get().kind().equals(SymbolKind.PARAMETER)) {
                String paramName = "";
                boolean isRequired = false;
                ParameterSymbol parameterSymbol = ((ParameterSymbol) symbol.get());
                TypeSymbol typeSymbol = parameterSymbol.typeDescriptor();
                List<String> paramTypes = getReferencedType(typeSymbol, currentPackage);
                switch (parameterNode.kind()) {
                    case REQUIRED_PARAM:
                        RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                        paramName = requiredParameterNode.paramName().isPresent() ?
                                requiredParameterNode.paramName().get().toString() : "";
                        isRequired = true;
                        break;
                    case DEFAULTABLE_PARAM:
                        DefaultableParameterNode defaultableParameterNode = (DefaultableParameterNode) parameterNode;
                        paramName = defaultableParameterNode.paramName().isPresent() ?
                                defaultableParameterNode.paramName().get().toString() : "";
                        break;
                    case INCLUDED_RECORD_PARAM:
                        break;
                    // res params
                }
                functionParameters.add(new FunctionParameter(paramTypes, paramName, isRequired, elementLocation));
            }
        }
    }
}
