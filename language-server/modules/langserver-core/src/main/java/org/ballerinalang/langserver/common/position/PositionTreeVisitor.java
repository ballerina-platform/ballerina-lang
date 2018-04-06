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
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangEndpointTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Tree visitor for finding the node at the given position.
 */
public class PositionTreeVisitor extends LSNodeVisitor {

    private String fileName;
    private Position position;
    private boolean terminateVisitor = false;
    private SymbolTable symTable;
    private LSServiceOperationContext context;
    private Object previousNode;
    private Stack<BLangNode> nodeStack;

    public PositionTreeVisitor(LSServiceOperationContext context) {
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
            setTerminateVisitor(true);
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

    public void visit(BLangFunction funcNode) {
        // Check for native functions
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol)) {
            return;
        }

        addTopLevelNodeToContext(funcNode, funcNode.name.getValue(), funcNode.symbol.pkgID, funcNode.symbol.kind.name(),
                funcNode.symbol.kind.name(), funcNode.symbol.owner.name.getValue(), funcNode.symbol.owner.pkgID);
        setPreviousNode(funcNode);
        this.addToNodeStack(funcNode);

        if (funcNode.receiver != null) {
            this.acceptNode(funcNode.receiver);
        }

        if (!funcNode.requiredParams.isEmpty()) {
            funcNode.requiredParams.forEach(this::acceptNode);
        }

        if (funcNode.returnTypeNode != null && !(funcNode.returnTypeNode.type instanceof BNilType)) {
            this.acceptNode(funcNode.returnTypeNode);
        }

        if (funcNode.endpoints != null && !funcNode.endpoints.isEmpty()) {
            funcNode.endpoints.forEach(this::acceptNode);
        }

        if (funcNode.body != null) {
            this.acceptNode(funcNode.body);
        }

        // Process workers
        if (!funcNode.workers.isEmpty()) {
            funcNode.workers.forEach(this::acceptNode);
        }

        if (!funcNode.defaultableParams.isEmpty()) {
            funcNode.defaultableParams.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        if (userDefinedType.getPosition() != null) {
            userDefinedType.getPosition().sCol += (previousNode instanceof BLangEndpointTypeNode
                    ? "endpoint<".length()
                    : 0);
            CommonUtil.calculateEndColumnOfGivenName(userDefinedType.getPosition(), userDefinedType.typeName.value,
                    userDefinedType.pkgAlias.value);
            if (userDefinedType.type instanceof BUnionType &&
                    HoverUtil.isMatchingPosition(userDefinedType.getPosition(), this.position)) {
                try {
                    BUnionType bUnionType = (BUnionType) userDefinedType.type;
                    for (BType type : bUnionType.memberTypes) {
                        if (type.tsymbol != null && type.tsymbol.getName().getValue().equals(userDefinedType
                                .typeName.getValue())) {
                            this.context.put(NodeContextKeys.NODE_KEY, userDefinedType);
                            this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
                            this.context.put(NodeContextKeys.NAME_OF_NODE_KEY, userDefinedType.typeName.getValue());
                            this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY, type.tsymbol.pkgID);
                            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY, type.tsymbol.kind.name());
                            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, type.tsymbol.kind.name());
                            this.context.put(NodeContextKeys.NODE_OWNER_KEY, type.tsymbol.owner.name.getValue());
                            this.context.put(NodeContextKeys.NODE_OWNER_PACKAGE_KEY, type.tsymbol.owner.pkgID);
                            this.context.put(NodeContextKeys.VAR_NAME_OF_NODE_KEY, userDefinedType.typeName.getValue());
                            setTerminateVisitor(true);
                            break;
                        }
                    }
                } catch (ClassCastException e) {
                    // Ignores
                }
            } else if (userDefinedType.type.tsymbol != null &&
                    HoverUtil.isMatchingPosition(userDefinedType.getPosition(), this.position)) {
                this.context.put(NodeContextKeys.NODE_KEY, userDefinedType);
                this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
                this.context.put(NodeContextKeys.NAME_OF_NODE_KEY, userDefinedType.typeName.getValue());
                this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY, userDefinedType.type.tsymbol.pkgID);
                this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY,
                        userDefinedType.type.tsymbol.kind.name());
                this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, userDefinedType.type.tsymbol.kind.name());
                this.context.put(NodeContextKeys.NODE_OWNER_KEY, userDefinedType.type.tsymbol.owner.name.getValue());
                this.context.put(NodeContextKeys.NODE_OWNER_PACKAGE_KEY, userDefinedType.type.tsymbol.owner.pkgID);
                this.context.put(NodeContextKeys.VAR_NAME_OF_NODE_KEY, userDefinedType.typeName.getValue());
                setTerminateVisitor(true);
            }
        }
    }

    @Override
    public void visit(BLangVariable varNode) {
        setPreviousNode(varNode);
        if (varNode.expr != null) {
            this.acceptNode(varNode.expr);
        }

        if (varNode.getTypeNode() != null) {
            this.acceptNode(varNode.getTypeNode());
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        CommonUtil.calculateEndColumnOfGivenName(varRefExpr.getPosition(), varRefExpr.variableName.value,
                varRefExpr.pkgAlias.value);
        if (varRefExpr.symbol != null && varRefExpr.symbol instanceof BEndpointVarSymbol &&
                HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            this.context.put(NodeContextKeys.NODE_KEY, varRefExpr);
            this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(NodeContextKeys.NAME_OF_NODE_KEY,
                    ((BEndpointVarSymbol) varRefExpr.symbol).name.getValue());
            this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY, ((BEndpointVarSymbol) varRefExpr.symbol).pkgID);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, ContextConstants.ENDPOINT);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY, ContextConstants.ENDPOINT);
            this.context.put(NodeContextKeys.VAR_NAME_OF_NODE_KEY, varRefExpr.variableName.getValue());
            this.context.put(NodeContextKeys.NODE_OWNER_KEY, varRefExpr.symbol.owner.name.getValue());
            this.context.put(NodeContextKeys.NODE_OWNER_PACKAGE_KEY,
                    varRefExpr.symbol.owner.pkgID);

            setTerminateVisitor(true);
        } else if (varRefExpr.type.tsymbol != null && varRefExpr.type.tsymbol.kind != null
                && varRefExpr.type.tsymbol.kind.name().equals(ContextConstants.OBJECT)
                && HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            this.context.put(NodeContextKeys.NODE_KEY, varRefExpr);
            this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(NodeContextKeys.NAME_OF_NODE_KEY, varRefExpr.type.tsymbol.name.getValue());
            this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY, varRefExpr.type.tsymbol.pkgID);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY, varRefExpr.type.tsymbol.kind.name());
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, ContextConstants.VARIABLE);
            this.context.put(NodeContextKeys.VAR_NAME_OF_NODE_KEY, varRefExpr.variableName.getValue());

            if (varRefExpr.symbol != null) {
                this.context.put(NodeContextKeys.NODE_OWNER_KEY, varRefExpr.symbol.owner.name.getValue());
                this.context.put(NodeContextKeys.NODE_OWNER_PACKAGE_KEY,
                        varRefExpr.symbol.owner.pkgID);
                this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, ContextConstants.VARIABLE);
            } else {
                this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, varRefExpr.type.tsymbol.kind.name());
                this.context.put(NodeContextKeys.NODE_OWNER_KEY, varRefExpr.type.tsymbol.owner.name.getValue());
                this.context.put(NodeContextKeys.NODE_OWNER_PACKAGE_KEY,
                        varRefExpr.type.tsymbol.owner.pkgID);
            }
            setTerminateVisitor(true);
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
                this.context.put(NodeContextKeys.NODE_OWNER_PACKAGE_KEY,
                        varRefExpr.symbol.owner.pkgID);
                this.context.put(NodeContextKeys.VAR_NAME_OF_NODE_KEY, varRefExpr.variableName.getValue());
            }
            setTerminateVisitor(true);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        setPreviousNode(fieldAccessExpr);
        if (fieldAccessExpr.expr != null) {
            this.acceptNode(fieldAccessExpr.expr);
        }
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        setPreviousNode(binaryExpr);
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
        setPreviousNode(blockNode);
        if (!blockNode.stmts.isEmpty()) {
            blockNode.stmts.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        setPreviousNode(varDefNode);
        if (varDefNode.getVariable() != null) {
            this.acceptNode(varDefNode.getVariable());
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        setPreviousNode(assignNode);
        if (assignNode.expr != null && assignNode.getPosition().sLine <= this.position.getLine()
                && assignNode.getPosition().eLine >= this.position.getLine()) {
            this.acceptNode(assignNode.expr);
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        setPreviousNode(exprStmtNode);
        if (HoverUtil.isMatchingPosition(exprStmtNode.pos, this.position)) {
            this.acceptNode(exprStmtNode.expr);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        setPreviousNode(ifNode);
        if (ifNode.expr != null && HoverUtil.isMatchingPosition(ifNode.expr.pos, this.position)) {
            acceptNode(ifNode.expr);
        }

        if (ifNode.body != null) {
            acceptNode(ifNode.body);
        }

        if (ifNode.elseStmt != null) {
            acceptNode(ifNode.elseStmt);
        }
    }

    public void visit(BLangStruct structNode) {
        addTopLevelNodeToContext(structNode, structNode.name.getValue(), structNode.symbol.pkgID,
                structNode.symbol.kind.name(), structNode.symbol.kind.name(),
                structNode.symbol.owner.name.getValue(), structNode.symbol.owner.pkgID);
        setPreviousNode(structNode);
        this.addToNodeStack(structNode);

        if (!structNode.fields.isEmpty()) {
            structNode.fields.forEach(this::acceptNode);
        }
    }

    public void visit(BLangWhile whileNode) {
        setPreviousNode(whileNode);
        if (whileNode.expr != null) {
            this.acceptNode(whileNode.expr);
        }
        if (whileNode.body != null) {
            this.acceptNode(whileNode.body);
        }
    }

    public void visit(BLangTransformer transformerNode) {
        addTopLevelNodeToContext(transformerNode, transformerNode.name.getValue(), transformerNode.symbol.pkgID,
                transformerNode.symbol.kind.name(), transformerNode.symbol.kind.name(),
                transformerNode.symbol.owner.name.getValue(), transformerNode.symbol.owner.pkgID);

        setPreviousNode(transformerNode);
        this.addToNodeStack(transformerNode);

        if (transformerNode.source != null) {
            acceptNode(transformerNode.source);
        }

        if (!transformerNode.requiredParams.isEmpty()) {
            transformerNode.requiredParams.forEach(this::acceptNode);
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
        addTopLevelNodeToContext(connectorNode, connectorNode.name.getValue(), connectorNode.symbol.pkgID,
                connectorNode.symbol.kind.name(), connectorNode.symbol.kind.name(),
                connectorNode.symbol.owner.name.getValue(), connectorNode.symbol.owner.pkgID);

        setPreviousNode(connectorNode);
        this.addToNodeStack(connectorNode);

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
        addTopLevelNodeToContext(actionNode, actionNode.name.getValue(), actionNode.symbol.pkgID,
                actionNode.symbol.kind.name(), actionNode.symbol.kind.name(),
                actionNode.symbol.owner.name.getValue(), actionNode.symbol.owner.pkgID);

        setPreviousNode(actionNode);
        this.addToNodeStack(actionNode);

        if (!actionNode.requiredParams.isEmpty()) {
            actionNode.requiredParams.forEach(this::acceptNode);
        }

        if (actionNode.body != null) {
            acceptNode(actionNode.body);
        }

        if (!actionNode.workers.isEmpty()) {
            actionNode.workers.forEach(this::acceptNode);
        }
    }

    public void visit(BLangService serviceNode) {
        addTopLevelNodeToContext(serviceNode, serviceNode.name.getValue(), serviceNode.symbol.pkgID,
                serviceNode.symbol.kind.name(), serviceNode.symbol.kind.name(),
                serviceNode.symbol.owner.name.getValue(), serviceNode.symbol.owner.pkgID);

        setPreviousNode(serviceNode);
        this.addToNodeStack(serviceNode);

        if (serviceNode.serviceTypeStruct != null) {
            this.acceptNode(serviceNode.serviceTypeStruct);
        }

        if (!serviceNode.vars.isEmpty()) {
            serviceNode.vars.forEach(this::acceptNode);
        }

        if (!serviceNode.resources.isEmpty()) {
            serviceNode.resources.forEach(this::acceptNode);
        }

        if (!serviceNode.endpoints.isEmpty()) {
            serviceNode.endpoints.forEach(this::acceptNode);
        }

        if (!serviceNode.boundEndpoints.isEmpty()) {
            serviceNode.boundEndpoints.forEach(this::acceptNode);
        }

        if (serviceNode.initFunction != null) {
            this.acceptNode(serviceNode.initFunction);
        }
    }

    public void visit(BLangResource resourceNode) {
        addTopLevelNodeToContext(resourceNode, resourceNode.name.getValue(), resourceNode.symbol.pkgID,
                resourceNode.symbol.kind.name(), resourceNode.symbol.kind.name(),
                resourceNode.symbol.owner.name.getValue(), resourceNode.symbol.owner.pkgID);

        setPreviousNode(resourceNode);
        this.addToNodeStack(resourceNode);

        if (!resourceNode.requiredParams.isEmpty()) {
            resourceNode.requiredParams.forEach(this::acceptNode);
        }

        if (resourceNode.body != null) {
            this.acceptNode(resourceNode.body);
        }

        if (!resourceNode.endpoints.isEmpty()) {
            resourceNode.endpoints.forEach(this::acceptNode);
        }

        if (!resourceNode.workers.isEmpty()) {
            resourceNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {
        setPreviousNode(tryCatchFinally);
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
        setPreviousNode(bLangCatch);
        if (bLangCatch.param != null) {
            acceptNode(bLangCatch.param);
        }

        if (bLangCatch.body != null) {
            acceptNode(bLangCatch.body);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        setPreviousNode(transactionNode);
        if (transactionNode.transactionBody != null) {
            acceptNode(transactionNode.transactionBody);
        }

        if (transactionNode.onRetryBody != null) {
            acceptNode(transactionNode.onRetryBody);
        }

        if (transactionNode.retryCount != null) {
            acceptNode(transactionNode.retryCount);
        }
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        setPreviousNode(forkJoin);

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
        setPreviousNode(workerNode);
        if (!workerNode.requiredParams.isEmpty()) {
            workerNode.requiredParams.forEach(this::acceptNode);
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
        setPreviousNode(workerSendNode);
        if (workerSendNode.expr != null) {
            this.acceptNode(workerSendNode.expr);
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        setPreviousNode(workerReceiveNode);
        if (workerReceiveNode.expr != null) {
            this.acceptNode(workerReceiveNode.expr);
        }
    }

    @Override
    public void visit(BLangReturn returnNode) {
        setPreviousNode(returnNode);

        if (returnNode.expr != null) {
            this.acceptNode(returnNode.expr);
        }
    }

    public void visit(BLangInvocation invocationExpr) {
        setPreviousNode(invocationExpr);
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
            this.context.put(NodeContextKeys.NODE_OWNER_KEY, invocationExpr.symbol.owner.name.getValue());
            this.context.put(NodeContextKeys.NODE_OWNER_PACKAGE_KEY,
                    invocationExpr.symbol.owner.pkgID);
            setTerminateVisitor(true);
        }
    }

    public void visit(BLangForeach foreach) {
        setPreviousNode(foreach);
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

    public void visit(BLangArrayLiteral arrayLiteral) {
        setPreviousNode(arrayLiteral);
        if (!arrayLiteral.exprs.isEmpty()) {
            arrayLiteral.exprs.forEach(this::acceptNode);
        }
    }

    public void visit(BLangTypeInit connectorInitExpr) {
        setPreviousNode(connectorInitExpr);
        if (connectorInitExpr.userDefinedType != null) {
            acceptNode(connectorInitExpr.userDefinedType);
        }

        if (!connectorInitExpr.argsExpr.isEmpty()) {
            connectorInitExpr.argsExpr.forEach(this::acceptNode);
        }
    }

    public void visit(BLangTypeCastExpr castExpr) {
        setPreviousNode(castExpr);
        if (castExpr.typeNode != null) {
            this.acceptNode(castExpr.typeNode);
        }

        if (castExpr.expr != null) {
            this.acceptNode(castExpr.expr);
        }
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        setPreviousNode(conversionExpr);

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

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        setPreviousNode(stringTemplateLiteral);

        if (!stringTemplateLiteral.exprs.isEmpty()) {
            stringTemplateLiteral.exprs.forEach(this::acceptNode);
        }
    }

    public void visit(BLangArrayType arrayType) {
        setPreviousNode(arrayType);
        if (arrayType.elemtype != null) {
            acceptNode(arrayType.elemtype);
        }
    }

    @Override
    public void visit(BLangEnum enumNode) {
        addTopLevelNodeToContext(enumNode, enumNode.name.getValue(), enumNode.symbol.pkgID,
                enumNode.symbol.kind.name(), enumNode.symbol.kind.name(),
                enumNode.symbol.owner.name.getValue(), enumNode.symbol.owner.pkgID);
        if (enumNode.getPosition().sLine == this.position.getLine()) {
            this.context.put(NodeContextKeys.VAR_NAME_OF_NODE_KEY, enumNode.name.getValue());
        }
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        setPreviousNode(endpointNode);
        if (endpointNode.endpointTypeNode != null) {
            this.acceptNode(endpointNode.endpointTypeNode);
        }

        if (endpointNode.configurationExpr != null) {
            this.acceptNode(endpointNode.configurationExpr);
        }
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        setPreviousNode(ternaryExpr);
        if (ternaryExpr.expr != null) {
            this.acceptNode(ternaryExpr.expr);
        }

        if (ternaryExpr.thenExpr != null) {
            this.acceptNode(ternaryExpr.thenExpr);
        }

        if (ternaryExpr.elseExpr != null) {
            this.acceptNode(ternaryExpr.elseExpr);
        }
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        setPreviousNode(unionTypeNode);
        if (!unionTypeNode.memberTypeNodes.isEmpty()) {
            unionTypeNode.memberTypeNodes.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        setPreviousNode(tupleTypeNode);
        if (!tupleTypeNode.memberTypeNodes.isEmpty()) {
            tupleTypeNode.memberTypeNodes.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        setPreviousNode(bracedOrTupleExpr);
        if (!bracedOrTupleExpr.expressions.isEmpty()) {
            bracedOrTupleExpr.expressions.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        setPreviousNode(stmt);
        if (!stmt.varRefs.isEmpty()) {
            stmt.varRefs.forEach(this::acceptNode);
        }

        if (stmt.expr != null) {
            this.acceptNode(stmt.expr);
        }
    }

    @Override
    public void visit(BLangObject objectNode) {
        setPreviousNode(objectNode);

        if (!objectNode.fields.isEmpty()) {
            objectNode.fields.forEach(this::acceptNode);
        }

        if (!objectNode.functions.isEmpty()) {
            objectNode.functions.forEach(this::acceptNode);
        }

        if (objectNode.initFunction != null) {
            this.acceptNode(objectNode.initFunction);
        }

        if (objectNode.receiver != null) {
            this.acceptNode(objectNode.receiver);
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        setPreviousNode(matchNode);
        if (matchNode.expr != null) {
            this.acceptNode(matchNode.expr);
        }

        if (!matchNode.patternClauses.isEmpty()) {
            matchNode.patternClauses.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangMatch.BLangMatchStmtPatternClause patternClauseNode) {
        setPreviousNode(patternClauseNode);
        if (patternClauseNode.variable != null) {
            this.acceptNode(patternClauseNode.variable);
        }

        if (patternClauseNode.body != null) {
            this.acceptNode(patternClauseNode.body);
        }
    }

    /**
     * Accept node to visit.
     *
     * @param node node to be accepted to visit.
     */
    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor) {
            this.position.setLine(this.position.getLine() - 1);
            return;
        }
        node.accept(this);
    }

    /**
     * Add node in to a stack to track the visited nodes.
     *
     * @param node BLangNode to be added to the stack
     */
    private void addToNodeStack(BLangNode node) {
        if (!terminateVisitor) {
            if (!this.nodeStack.empty()) {
                this.nodeStack.pop();
            }
            this.nodeStack.push(node);
        }
    }

    /**
     * Set previous node.
     *
     * @param node node to be added as previous
     */
    private void setPreviousNode(BLangNode node) {
        this.previousNode = node;
    }

    /**
     * Set terminate visitor.
     *
     * @param value boolean value for terminate visitor
     */
    private void setTerminateVisitor(boolean value) {
        this.terminateVisitor = value;
    }

    /**
     * Add the top level node to context if cursor position matches the line of signature.
     *
     * @param node node to be compared and added to the context
     */
    private void addTopLevelNodeToContext(BLangNode node, String name, PackageID currentPkg,
                                          String symbolKindOfParentNode, String symbolKindOfCurrentNode,
                                          String ownerName, PackageID ownerPkg) {
        if (node.getPosition().sLine == this.position.getLine()) {
            this.context.put(NodeContextKeys.NODE_KEY, node);
            this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(NodeContextKeys.NAME_OF_NODE_KEY, name);
            this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY, currentPkg);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY, symbolKindOfParentNode);
            this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, symbolKindOfCurrentNode);
            this.context.put(NodeContextKeys.NODE_OWNER_KEY, ownerName);
            this.context.put(NodeContextKeys.NODE_OWNER_PACKAGE_KEY, ownerPkg);
        }
    }
}
