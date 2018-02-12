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

package org.ballerinalang.langserver.common.position;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BEndpointType;
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
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Tree visitor for the get the node at the given position.
 */
public class PositionTreeVisitor extends BLangNodeVisitor {

    private String fileName;
    private Position position;
    private boolean terminateVisitor = false;
    private SymbolTable symTable;
    private TextDocumentServiceContext context;
    private Object previousNode;
    private Stack<BLangNode> nodeStack;

    public PositionTreeVisitor(TextDocumentServiceContext context) {
        this.context = context;
        this.position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        this.fileName = context.get(DocumentServiceKeys.FILE_NAME_KEY);
        this.symTable = SymbolTable.getInstance(context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
        this.position.setLine(this.position.getLine() + 1);
        this.nodeStack = new Stack<>();
        this.context.put(NodeContextKeys.NODE_STACK_KEY, nodeStack);
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

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgSymbol);
        acceptNode(pkgEnv.node);
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

        previousNode = funcNode;
        this.nodeStack.push(funcNode);

        if (!funcNode.params.isEmpty()) {
            funcNode.params.forEach(this::acceptNode);
        }

        if (!funcNode.retParams.isEmpty()) {
            funcNode.retParams.forEach(this::acceptNode);
        }

        if (funcNode.body != null) {
            this.acceptNode(funcNode.body);
        }

        // Process workers
        if (!funcNode.workers.isEmpty()) {
            funcNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        userDefinedType.getPosition().sCol += (previousNode instanceof BLangEndpointTypeNode
                ? "endpoint<".length()
                : 0);
        userDefinedType.getPosition().eCol = userDefinedType.getPosition().sCol
                + userDefinedType.typeName.value.length()
                + (!userDefinedType.pkgAlias.value.isEmpty() ? (userDefinedType.pkgAlias.value + ":").length() : 0);
        if (HoverUtil.isMatchingPosition(userDefinedType.getPosition(), this.position)) {
            this.context.put(NodeContextKeys.NODE_KEY, userDefinedType);
            this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(NodeContextKeys.NAME_OF_NODE_KEY, userDefinedType.typeName.getValue());
            this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY, userDefinedType.type.tsymbol.pkgID);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY, userDefinedType.type.tsymbol.kind.name());
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, userDefinedType.type.tsymbol.kind.name());
            terminateVisitor = true;
        }
    }

    @Override
    public void visit(BLangEnum enumNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        // No implementation
    }

    @Override
    public void visit(BLangVariable varNode) {
        previousNode = varNode;
        if (varNode.expr != null) {
            this.acceptNode(varNode.expr);
        }

        if (varNode.getTypeNode() != null) {
            this.acceptNode(varNode.getTypeNode());
        }
    }

    @Override
    public void visit(BLangLiteral litNode) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        varRefExpr.getPosition().eCol = varRefExpr.getPosition().sCol
                + varRefExpr.variableName.value.length()
                + (!varRefExpr.pkgAlias.value.isEmpty() ? (varRefExpr.pkgAlias.value + ":").length() : 0);
        if (varRefExpr.type instanceof BEndpointType && ((BEndpointType) varRefExpr.type).constraint != null
                && ((BEndpointType) varRefExpr.type).constraint.tsymbol.kind.name().equals(ContextConstants.CONNECTOR)
                && HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            this.context.put(NodeContextKeys.NODE_KEY, varRefExpr);
            this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(NodeContextKeys.NAME_OF_NODE_KEY,
                    ((BEndpointType) varRefExpr.type).constraint.tsymbol.name.getValue());
            this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY,
                    ((BEndpointType) varRefExpr.type).constraint.tsymbol.pkgID);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY,
                    ((BEndpointType) varRefExpr.type).constraint.tsymbol.kind.name());
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, ContextConstants.VARIABLE);
            if (varRefExpr.symbol != null) {
                this.context.put(NodeContextKeys.NODE_OWNER_KEY, varRefExpr.symbol.owner.name.getValue());
                this.context.put(NodeContextKeys.VAR_NAME_OF_NODE_KEY, varRefExpr.variableName.getValue());
            }
            terminateVisitor = true;
        } else if (varRefExpr.type.tsymbol != null && varRefExpr.type.tsymbol.kind != null
                && (varRefExpr.type.tsymbol.kind.name().equals(ContextConstants.ENUM)
                || varRefExpr.type.tsymbol.kind.name().equals(ContextConstants.STRUCT))
                && HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            this.context.put(NodeContextKeys.NODE_KEY, varRefExpr);
            this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(NodeContextKeys.NAME_OF_NODE_KEY, varRefExpr.type.tsymbol.name.getValue());
            this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY, varRefExpr.type.tsymbol.pkgID);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY, varRefExpr.type.tsymbol.kind.name());
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, ContextConstants.VARIABLE);
            if (varRefExpr.symbol != null) {
                this.context.put(NodeContextKeys.NODE_OWNER_KEY, varRefExpr.symbol.owner.name.getValue());
                this.context.put(NodeContextKeys.VAR_NAME_OF_NODE_KEY, varRefExpr.variableName.getValue());
                this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, ContextConstants.VARIABLE);
            } else {
                this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, varRefExpr.type.tsymbol.kind.name());
            }
            terminateVisitor = true;
        } else if (varRefExpr.pkgSymbol != null
                && HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            this.context.put(NodeContextKeys.NODE_KEY, varRefExpr);
            this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(NodeContextKeys.NAME_OF_NODE_KEY, varRefExpr.variableName.getValue());
            this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY, varRefExpr.pkgSymbol.pkgID);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY, ContextConstants.VARIABLE);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, ContextConstants.VARIABLE);
            if (varRefExpr.symbol != null) {
                this.context.put(NodeContextKeys.NODE_OWNER_KEY, varRefExpr.symbol.owner.name.getValue());
                this.context.put(NodeContextKeys.VAR_NAME_OF_NODE_KEY, varRefExpr.variableName.getValue());
            }
            terminateVisitor = true;
        }
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        previousNode = fieldAccessExpr;
        if (fieldAccessExpr.expr != null) {
            this.acceptNode(fieldAccessExpr.expr);
        }
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        previousNode = binaryExpr;
        if (binaryExpr.lhsExpr != null) {
            acceptNode(binaryExpr.lhsExpr);
        }

        if (binaryExpr.rhsExpr != null) {
            acceptNode(binaryExpr.rhsExpr);
        }
    }

    // Statements

    @Override
    public void visit(BLangBlockStmt blockNode) {
        previousNode = blockNode;
        if (!blockNode.stmts.isEmpty()) {
            blockNode.stmts.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        previousNode = varDefNode;
        if (varDefNode.getVariable() != null) {
            this.acceptNode(varDefNode.getVariable());
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        previousNode = assignNode;
        if (assignNode.expr != null && assignNode.getPosition().sLine <= this.position.getLine()
                && assignNode.getPosition().eLine >= this.position.getLine()) {
            this.acceptNode(assignNode.expr);
        }

        if (!assignNode.varRefs.isEmpty()) {
            assignNode.varRefs.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        previousNode = exprStmtNode;
        if (HoverUtil.isMatchingPosition(exprStmtNode.pos, this.position)) {
            this.acceptNode(exprStmtNode.expr);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        previousNode = ifNode;
        if (ifNode.expr != null && HoverUtil.isMatchingPosition(ifNode.expr.pos, this.position)) {
            acceptNode(ifNode.expr);
        } else if (ifNode.body != null) {
            acceptNode(ifNode.body);
        }
    }

    public void visit(BLangStruct structNode) {
        previousNode = structNode;
        this.nodeStack.push(structNode);

        if (!structNode.fields.isEmpty()) {
            structNode.fields.forEach(this::acceptNode);
        }
    }

    public void visit(BLangWhile whileNode) {
        previousNode = whileNode;
        if (whileNode.expr != null) {
            this.acceptNode(whileNode.expr);
        }
        if (whileNode.body != null) {
            this.acceptNode(whileNode.body);
        }
    }

    public void visit(BLangTransformer transformerNode) {
        previousNode = transformerNode;
        this.nodeStack.push(transformerNode);

        if (transformerNode.source != null) {
            acceptNode(transformerNode.source);
        }

        if (!transformerNode.params.isEmpty()) {
            transformerNode.params.forEach(this::acceptNode);
        }

        if (!transformerNode.retParams.isEmpty()) {
            transformerNode.retParams.forEach(this::acceptNode);
        }

        if (transformerNode.body != null) {
            acceptNode(transformerNode.body);
        }

        if (!transformerNode.workers.isEmpty()) {
            transformerNode.workers.forEach(this::acceptNode);
        }
    }

    public void visit(BLangConnector connectorNode) {
        previousNode = connectorNode;
        this.nodeStack.push(connectorNode);

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

    public void visit(BLangAction actionNode) {
        previousNode = actionNode;
        this.nodeStack.push(actionNode);

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

    public void visit(BLangService serviceNode) {
        previousNode = serviceNode;
        this.nodeStack.push(serviceNode);

        if (!serviceNode.resources.isEmpty()) {
            serviceNode.resources.forEach(this::acceptNode);
        }

        if (serviceNode.initFunction != null) {
            this.acceptNode(serviceNode.initFunction);
        }
    }

    public void visit(BLangResource resourceNode) {
        previousNode = resourceNode;
        this.nodeStack.push(resourceNode);

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

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {
        previousNode = tryCatchFinally;
        if (tryCatchFinally.tryBody != null) {
            acceptNode(tryCatchFinally.tryBody);
        }

        if (!tryCatchFinally.catchBlocks.isEmpty()) {
            tryCatchFinally.catchBlocks.forEach(this::acceptNode);
        }

        if (tryCatchFinally.finallyBody != null) {
            acceptNode(tryCatchFinally.finallyBody);
        }
    }

    @Override
    public void visit(BLangCatch bLangCatch) {
        previousNode = bLangCatch;
        if (bLangCatch.param != null) {
            acceptNode(bLangCatch.param);
        }

        if (bLangCatch.body != null) {
            acceptNode(bLangCatch.body);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        previousNode = transactionNode;
        if (transactionNode.transactionBody != null) {
            acceptNode(transactionNode.transactionBody);
        }

        if (transactionNode.failedBody != null) {
            acceptNode(transactionNode.failedBody);
        }

        if (transactionNode.retryCount != null) {
            acceptNode(transactionNode.retryCount);
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        // No implementation
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        previousNode = forkJoin;

        if (!forkJoin.workers.isEmpty()) {
            forkJoin.workers.forEach(this::acceptNode);
        }

        if (forkJoin.joinedBody != null) {
            acceptNode(forkJoin.joinedBody);
        }

        if (forkJoin.joinResultVar != null) {
            acceptNode(forkJoin.joinResultVar);
        }

        if (forkJoin.timeoutBody != null) {
            acceptNode(forkJoin.timeoutBody);
        }

        if (forkJoin.timeoutExpression != null) {
            acceptNode(forkJoin.timeoutExpression);
        }

        if (forkJoin.timeoutVariable != null) {
            acceptNode(forkJoin.timeoutVariable);
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        previousNode = workerNode;
        if (!workerNode.params.isEmpty()) {
            workerNode.params.forEach(this::acceptNode);
        }

        if (!workerNode.retParams.isEmpty()) {
            workerNode.retParams.forEach(this::acceptNode);
        }

        if (workerNode.body != null) {
            acceptNode(workerNode.body);
        }

        if (!workerNode.workers.isEmpty()) {
            workerNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        previousNode = workerSendNode;
        if (!workerSendNode.exprs.isEmpty()) {
            workerSendNode.exprs.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        previousNode = workerReceiveNode;
        if (!workerReceiveNode.exprs.isEmpty()) {
            workerReceiveNode.exprs.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangReturn returnNode) {
        previousNode = returnNode;
        if (!returnNode.exprs.isEmpty()) {
            returnNode.exprs.forEach(this::acceptNode);
        }
    }

    public void visit(BLangNext nextNode) {
        // No implementation
    }

    public void visit(BLangInvocation invocationExpr) {
        previousNode = invocationExpr;
        if (invocationExpr.expr != null) {
            this.acceptNode(invocationExpr.expr);
        }

        if (!invocationExpr.argExprs.isEmpty()) {
            invocationExpr.argExprs.forEach(this::acceptNode);
        }

        if (!terminateVisitor && HoverUtil.isMatchingPosition(invocationExpr.getPosition(), this.position)) {
            this.context.put(NodeContextKeys.NODE_KEY, invocationExpr);
            this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(NodeContextKeys.NAME_OF_NODE_KEY, invocationExpr.name.getValue());
            this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY, invocationExpr.symbol.pkgID);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY, invocationExpr.symbol.kind.name());
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, invocationExpr.symbol.kind.name());
            this.terminateVisitor = true;
        }
    }

    public void visit(BLangForeach foreach) {
        previousNode = foreach;

        if (foreach.collection != null) {
            acceptNode(foreach.collection);
        }

        if (!foreach.varRefs.isEmpty()) {
            foreach.varRefs.forEach(this::acceptNode);
        }

        if (foreach.body != null) {
            acceptNode(foreach.body);
        }
    }


    public void visit(BLangCompilationUnit compUnit) {
        // No implementation
    }

    public void visit(BLangPackageDeclaration pkgDclNode) {
        // No implementation
    }

    public void visit(BLangEnum.BLangEnumerator enumeratorNode) {
        // No implementation
    }

    public void visit(BLangIdentifier identifierNode) {
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

    public void visit(BLangBind bindNode) {
        // No implementation
    }

    public void visit(BLangBreak breakNode) {
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

    public void visit(BLangArrayLiteral arrayLiteral) {
        previousNode = arrayLiteral;
        if (!arrayLiteral.exprs.isEmpty()) {
            arrayLiteral.exprs.forEach(this::acceptNode);
        }
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        // No implementation
    }

    public void visit(BLangConnectorInit connectorInitExpr) {
        previousNode = connectorInitExpr;
        if (connectorInitExpr.connectorType != null) {
            connectorInitExpr.connectorType.type = connectorInitExpr.type;
            acceptNode(connectorInitExpr.connectorType);
        }

        if (!connectorInitExpr.argsExpr.isEmpty()) {
            connectorInitExpr.argsExpr.forEach(this::acceptNode);
        }
    }

    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        // No implementation
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        // No implementation
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        // No implementation
    }

    public void visit(BLangTypeofExpr accessExpr) {
        // No implementation
    }

    public void visit(BLangTypeCastExpr castExpr) {
        previousNode = castExpr;
        if (castExpr.typeNode != null) {
            this.acceptNode(castExpr.typeNode);
        }

        if (castExpr.expr != null) {
            this.acceptNode(castExpr.expr);
        }
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        previousNode = conversionExpr;

        if (conversionExpr.expr != null) {
            acceptNode(conversionExpr.expr);
        }

        if (conversionExpr.typeNode != null) {
            acceptNode(conversionExpr.typeNode);
        }

        if (conversionExpr.transformerInvocation != null) {
            acceptNode(conversionExpr.transformerInvocation);
        }
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
        previousNode = stringTemplateLiteral;

        if (!stringTemplateLiteral.exprs.isEmpty()) {
            stringTemplateLiteral.exprs.forEach(this::acceptNode);
        }
    }

    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        // No implementation
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        // No implementation
    }

    public void visit(BLangValueType valueType) {
        // No implementation
    }

    public void visit(BLangArrayType arrayType) {
        previousNode = arrayType;
        if (arrayType.elemtype != null) {
            acceptNode(arrayType.elemtype);
        }
    }

    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // No implementation
    }

    public void visit(BLangEndpointTypeNode endpointType) {
        previousNode = endpointType;
        if (endpointType.constraint != null) {
            acceptNode(endpointType.constraint);
        }
    }

    public void visit(BLangConstrainedType constrainedType) {
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
