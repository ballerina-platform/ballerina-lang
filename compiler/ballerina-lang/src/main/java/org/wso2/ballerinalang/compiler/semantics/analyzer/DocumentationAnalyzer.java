/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @since 0.981.0
 */
public class DocumentationAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<DocumentationAnalyzer> DOCUMENTATION_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private SymbolEnv env;
    private BLangNode parent;
    private BLangDiagnosticLog dlog;

    public static DocumentationAnalyzer getInstance(CompilerContext context) {
        DocumentationAnalyzer documentationAnalyzer = context.get(DOCUMENTATION_ANALYZER_KEY);
        if (documentationAnalyzer == null) {
            documentationAnalyzer = new DocumentationAnalyzer(context);
        }
        return documentationAnalyzer;
    }

    public DocumentationAnalyzer(CompilerContext context) {
        context.put(DOCUMENTATION_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        parent = pkgNode;
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgNode.symbol);
        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeNode((BLangNode) topLevelNode, pkgEnv));
        pkgNode.completedPhases.add(CompilerPhase.CODE_ANALYZE);
        parent = null;
        return pkgNode;
    }

    private void analyzeNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        BLangNode myParent = parent;
        node.parent = parent;
        parent = node;
        node.accept(this);
        parent = myParent;
        this.env = prevEnv;
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {

    }

    @Override
    public void visit(BLangAnnotation annotationNode) {

    }

    @Override
    public void visit(BLangEndpoint endpointNode) {

    }

    @Override
    public void visit(BLangEnum enumNode) {

    }

    @Override
    public void visit(BLangVariable varNode) {
        validateNoParameters(varNode);
        validateReturnParameter(varNode, null, false);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        validateParameters(funcNode, funcNode.getParameters(), DiagnosticCode.UNDOCUMENTED_PARAMETER,
                DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER, DiagnosticCode.PARAMETER_ALREADY_DOCUMENTED);

        boolean hasReturn = true;
        if (funcNode.returnTypeNode.getKind() == NodeKind.VALUE_TYPE) {
            hasReturn = ((BLangValueType) funcNode.returnTypeNode).typeKind != TypeKind.NIL;
        }
        validateReturnParameter(funcNode, funcNode, hasReturn);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {

    }

    @Override
    public void visit(BLangService serviceNode) {
        validateNoParameters(serviceNode);
        validateReturnParameter(serviceNode, null, false);

        serviceNode.getResources().forEach(r -> analyzeNode(r, env));
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        BLangType typeNode = typeDefinition.getTypeNode();
        if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
            List<? extends VariableNode> fields = ((BLangObjectTypeNode) typeNode).getFields();
            validateParameters(typeDefinition, fields, DiagnosticCode.UNDOCUMENTED_FIELD,
                    DiagnosticCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticCode.FIELD_ALREADY_DOCUMENTED);
            validateReturnParameter(typeDefinition, null, false);

            ((BLangObjectTypeNode) typeDefinition.getTypeNode()).getFunctions().forEach(f -> analyzeNode(f, env));
        } else if (typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE) {
            List<? extends VariableNode> fields = ((BLangRecordTypeNode) typeNode).getFields();
            validateParameters(typeDefinition, fields, DiagnosticCode.UNDOCUMENTED_FIELD,
                    DiagnosticCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticCode.FIELD_ALREADY_DOCUMENTED);
            validateReturnParameter(typeDefinition, null, false);
        }
    }

    @Override
    public void visit(BLangResource resourceNode) {
        validateParameters(resourceNode, resourceNode.getParameters(), DiagnosticCode.UNDOCUMENTED_PARAMETER,
                DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER, DiagnosticCode.PARAMETER_ALREADY_DOCUMENTED);

        validateReturnParameter(resourceNode, null, false);
    }

    private void validateParameters(DocumentableNode documentableNode, List<? extends VariableNode> actualParameters,
                                    DiagnosticCode undocumentedParameter, DiagnosticCode noSuchParameter,
                                    DiagnosticCode parameterAlreadyDefined) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }

        Map<String, BLangMarkdownParameterDocumentation> documentedParameterMap = new HashMap<>();
        documentation.parameters.forEach(parameter -> {
            String parameterName = parameter.getParameterName().getValue();
            // Check for duplicate documentations.
            if (documentedParameterMap.containsKey(parameterName)) {
                dlog.warning(parameter.pos, parameterAlreadyDefined, parameterName);
            } else {
                documentedParameterMap.put(parameterName, parameter);
            }
        });

        Map<String, VariableNode> actualParameterMap = new HashMap<>();
        actualParameters.forEach(parameter -> {
            String name = parameter.getName().getValue();
            actualParameterMap.put(name, parameter);
            // Set the symbol in the documentation node.
            BLangMarkdownParameterDocumentation param = documentation.getParameterDocumentations().get(name);
            if (param != null) {
                param.setSymbol(((BLangVariable) parameter).symbol);
            }
        });

        // Temporary list to hold matching parameter names.
        List<String> matchedParameters = new LinkedList<>();
        actualParameterMap.forEach((name, node) -> {
            if (documentedParameterMap.containsKey(name)) {
                matchedParameters.add(name);
            }
        });

        // Remove matched parameters.
        matchedParameters.forEach(documentedParameterMap::remove);
        matchedParameters.forEach(actualParameterMap::remove);

        documentedParameterMap.forEach((name, node) -> dlog.warning(node.pos, noSuchParameter, name));
        actualParameterMap.forEach((name, node) -> dlog.warning(((BLangNode) node).pos, undocumentedParameter, name));
    }

    private void validateNoParameters(DocumentableNode documentableNode) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }
        Map<String, BLangMarkdownParameterDocumentation> parameterDocumentations =
                documentation.getParameterDocumentations();
        if (parameterDocumentations.isEmpty()) {
            return;
        }
        parameterDocumentations.forEach((parameter, parameterDocumentation) -> {
            dlog.warning(parameterDocumentation.pos, DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER, parameter);
        });
    }

    private void validateReturnParameter(DocumentableNode documentableNode, BLangNode node, boolean isExpected) {
        BLangMarkdownDocumentation documentationAttachment = documentableNode.getMarkdownDocumentationAttachment();
        if (documentationAttachment == null) {
            return;
        }

        BLangMarkdownReturnParameterDocumentation returnParameter = documentationAttachment.getReturnParameter();
        if (returnParameter == null && isExpected) {
            dlog.warning(node.pos, DiagnosticCode.UNDOCUMENTED_RETURN_PARAMETER);
        } else if (returnParameter != null && !isExpected) {
            dlog.warning(returnParameter.pos, DiagnosticCode.NO_DOCUMENTABLE_RETURN_PARAMETER);
        } else if (returnParameter != null) {
            returnParameter.setReturnType(((BLangFunction) node).getReturnTypeNode().type);
        }
    }
}
