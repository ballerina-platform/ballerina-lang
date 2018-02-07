/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.definition;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeofExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangEndpointTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tree visitor to find the definition of variables.
 */
public class DefinitionTreeVisitor extends BLangNodeVisitor {

    private boolean terminateVisitor = false;
    private TextDocumentServiceContext context;
    private String fileName;

    public DefinitionTreeVisitor(TextDocumentServiceContext context) {
        this.context = context;
        this.fileName = context.get(DocumentServiceKeys.FILE_NAME_KEY);
        this.context.put(NodeContextKeys.NODE_KEY, null);
    }

    public void visit(BLangPackage pkgNode) {
        // Then visit each top-level element sorted using the compilation unit
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes.stream().filter(node ->
                node.getPosition().getSource().getCompilationUnitName().equals(this.fileName)
        ).collect(Collectors.toList());

        if (topLevelNodes.isEmpty()) {
            terminateVisitor = true;
            acceptNode(null);
        } else {
            topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
        }
    }

    public void visit(BLangCompilationUnit compUnit) {
        // No implementation
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
        // No implementation
    }

    public void visit(BLangImportPackage importPkgNode) {
        // No implementation
    }

    public void visit(BLangXMLNS xmlnsNode) {
        // No implementation
    }

    public void visit(BLangFunction funcNode) {
        // Check for native functions
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol)) {
            return;
        }

        if (funcNode.name.getValue().equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))) {
            if (!funcNode.params.isEmpty()) {
                funcNode.params.forEach(this::acceptNode);
            }

            if (!funcNode.retParams.isEmpty()) {
                funcNode.retParams.forEach(this::acceptNode);
            }

            if (funcNode.body != null) {
                this.acceptNode(funcNode.body);
            }

            if (!funcNode.workers.isEmpty()) {
                funcNode.workers.forEach(this::acceptNode);
            }
        }
    }

    public void visit(BLangService serviceNode) {
        if (serviceNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))) {
            if (!serviceNode.resources.isEmpty()) {
                serviceNode.resources.forEach(this::acceptNode);
            }

            if (serviceNode.initFunction != null) {
                this.acceptNode(serviceNode.initFunction);
            }
        }
    }

    public void visit(BLangResource resourceNode) {
        if (resourceNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))) {
            if (!resourceNode.params.isEmpty()) {
                resourceNode.params.forEach(this::acceptNode);
            }

            if (!resourceNode.retParams.isEmpty()) {
                resourceNode.retParams.forEach(this::acceptNode);
            }

            if (resourceNode.body != null) {
                this.acceptNode(resourceNode.body);
            }
        }
    }

    public void visit(BLangConnector connectorNode) {
        if (connectorNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))) {
            if (!connectorNode.params.isEmpty()) {
                connectorNode.params.forEach(this::acceptNode);
            }

            if (!connectorNode.varDefs.isEmpty()) {
                connectorNode.varDefs.forEach(this::acceptNode);
            }

            if (!connectorNode.actions.isEmpty()) {
                connectorNode.actions.forEach(this::acceptNode);
            }
        }
    }

    public void visit(BLangAction actionNode) {
        if (actionNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.NODE_OWNER_KEY))) {
            if (!actionNode.params.isEmpty()) {
                actionNode.params.forEach(this::acceptNode);
            }

            if (!actionNode.retParams.isEmpty()) {
                actionNode.retParams.forEach(this::acceptNode);
            }

            if (actionNode.body != null) {
                acceptNode(actionNode.body);
            }

            if (!actionNode.workers.isEmpty()) {
                actionNode.workers.forEach(this::acceptNode);
            }
        }
    }

    public void visit(BLangStruct structNode) {
        // No implementation
    }

    public void visit(BLangEnum enumNode) {
        // No implementation
    }

    public void visit(BLangEnum.BLangEnumerator enumeratorNode) {
        // No implementation
    }

    public void visit(BLangVariable varNode) {
        if (varNode.name.getValue()
                .equals(this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY))) {
            this.context.put(NodeContextKeys.NODE_KEY, varNode);
            terminateVisitor = true;
        }
    }

    public void visit(BLangWorker workerNode) {
        if (workerNode.body != null) {
            this.acceptNode(workerNode.body);
        }

        if (!workerNode.workers.isEmpty()) {
            workerNode.workers.forEach(this::acceptNode);
        }
    }

    public void visit(BLangIdentifier identifierNode) {
        // No implementation
    }

    public void visit(BLangAnnotation annotationNode) {
        // No implementation
    }

    public void visit(BLangAnnotAttribute annotationAttribute) {
        // No implementation
    }

    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        // No implementation
    }

    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        // No implementation
    }

    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        // No implementation
    }

    public void visit(BLangTransformer transformerNode) {
        // No implementation
    }

    public void visit(BLangBlockStmt blockNode) {
        if (!blockNode.stmts.isEmpty()) {
            blockNode.stmts.forEach(this::acceptNode);
        }
    }

    public void visit(BLangVariableDef varDefNode) {
        if (varDefNode.getVariable() != null) {
            this.acceptNode(varDefNode.getVariable());
        }
    }

    public void visit(BLangAssignment assignNode) {
        if (!assignNode.varRefs.isEmpty()) {
            assignNode.varRefs.forEach(this::acceptNode);
        }
    }

    public void visit(BLangBind bindNode) {
        // No implementation
    }

    public void visit(BLangAbort abortNode) {
        // No implementation
    }

    public void visit(BLangNext nextNode) {
        // No implementation
    }

    public void visit(BLangBreak breakNode) {
        // No implementation
    }

    public void visit(BLangReturn returnNode) {
        // No implementation
    }

    public void visit(BLangReturn.BLangWorkerReturn returnNode) {
        // No implementation
    }

    public void visit(BLangThrow throwNode) {
        // No implementation
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        // No implementation
    }

    public void visit(BLangExpressionStmt exprStmtNode) {
        // No implementation
    }

    public void visit(BLangIf ifNode) {
        if (ifNode.body != null) {
            this.acceptNode(ifNode.body);
        }

        if (ifNode.elseStmt != null) {
            this.acceptNode(ifNode.elseStmt);
        }
    }

    public void visit(BLangForeach foreach) {
        if (!foreach.varRefs.isEmpty()) {
            foreach.varRefs.forEach(this::acceptNode);
        }

        if (foreach.body != null) {
            this.acceptNode(foreach.body);
        }
    }

    public void visit(BLangWhile whileNode) {
        if (whileNode.body != null) {
            this.acceptNode(whileNode.body);
        }
    }

    public void visit(BLangTransaction transactionNode) {
        if (transactionNode.transactionBody != null) {
            this.acceptNode(transactionNode.transactionBody);
        }

        if (transactionNode.failedBody != null) {
            this.acceptNode(transactionNode.failedBody);
        }
    }

    public void visit(BLangTryCatchFinally tryNode) {
        if (tryNode.tryBody != null) {
            this.acceptNode(tryNode.tryBody);
        }

        if (!tryNode.catchBlocks.isEmpty()) {
            tryNode.catchBlocks.forEach(this::acceptNode);
        }

        if (tryNode.finallyBody != null) {
            this.acceptNode(tryNode.finallyBody);
        }
    }

    public void visit(BLangCatch catchNode) {
        if (catchNode.body != null) {
            this.acceptNode(catchNode.body);
        }
    }

    public void visit(BLangForkJoin forkJoin) {
        if (!forkJoin.getWorkers().isEmpty()) {
            forkJoin.getWorkers().forEach(this::acceptNode);
        }

        if (forkJoin.joinedBody != null) {
            this.acceptNode(forkJoin.joinedBody);
        }

        if (forkJoin.timeoutBody != null) {
            this.acceptNode(forkJoin.timeoutBody);
        }
    }

    public void visit(BLangLiteral literalExpr) {
        // No implementation
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        // No implementation
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        // No implementation
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        if (varRefExpr.variableName.getValue()
                .equals(this.context.get(NodeContextKeys.VAR_NAME_OF_NODE_KEY))) {
            this.context.put(NodeContextKeys.NODE_KEY, varRefExpr);
            terminateVisitor = true;
        }
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // No implementation
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        // No implementation
    }

    public void visit(BLangInvocation invocationExpr) {
        // No implementation
    }

    public void visit(BLangConnectorInit connectorInitExpr) {
        // No implementation
    }

    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        // No implementation
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        // No implementation
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        // No implementation
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        // No implementation
    }

    public void visit(BLangTypeofExpr accessExpr) {
        // No implementation
    }

    public void visit(BLangTypeCastExpr castExpr) {
        // No implementation
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        // No implementation
    }

    public void visit(BLangXMLQName xmlQName) {
        // No implementation
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
        // No implementation
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        // No implementation
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        // No implementation
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        // No implementation
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        // No implementation
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        // No implementation
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        // No implementation
    }

    public void visit(BLangWorkerSend workerSendNode) {
        // No implementation
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        // No implementation
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        if (bLangLambdaFunction.function != null) {
            this.acceptNode(bLangLambdaFunction.function);
        }
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        // No implementation
    }

    public void visit(BLangIntRangeExpression intRangeExpression) {
        // No implementation
    }

    public void visit(BLangValueType valueType) {
        // No implementation
    }

    public void visit(BLangArrayType arrayType) {
        // No implementation
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // No implementation
    }

    public void visit(BLangEndpointTypeNode endpointType) {
        // No implementation
    }

    public void visit(BLangConstrainedType constrainedType) {
        // No implementation
    }

    public void visit(BLangUserDefinedType userDefinedType) {
        // No implementation
    }

    public void visit(BLangFunctionTypeNode functionTypeNode) {
        // No implementation
    }

    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        // No implementation
    }

    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        // No implementation
    }

    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        // No implementation
    }

    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        // No implementation
    }

    public void visit(BLangFieldBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        // No implementation
    }

    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        // No implementation
    }

    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        // No implementation
    }

    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        // No implementation
    }

    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        // No implementation
    }

    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        // No implementation
    }

    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        // No implementation
    }

    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        // No implementation
    }

    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        // No implementation
    }

    public void visit(BLangInvocation.BLangTransformerInvocation iExpr) {
        // No implementation
    }

    public void visit(BLangArrayLiteral.BLangJSONArrayLiteral jsonArrayLiteral) {
        // No implementation
    }

    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        // No implementation
    }

    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        // No implementation
    }

    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        // No implementation
    }

    public void visit(BLangFieldBasedAccess.BLangEnumeratorAccessExpr enumeratorAccessExpr) {
        // No implementation
    }

    /**
     * Accept node to visit.
     *
     * @param node node to be accepted to visit.
     */
    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor) {
            return;
        }
        node.accept(this);
    }
}
