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

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangScope;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangEndpointTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;
import java.util.Stack;

/**
 * Tree visitor for finding the node at the given position.
 */
public class PositionTreeVisitor extends LSNodeVisitor {

    private Position position;
    private boolean terminateVisitor = false;
    private SymbolTable symTable;
    private LSContext context;
    private Object previousNode;
    private Stack<BLangNode> nodeStack;

    public PositionTreeVisitor(LSContext context) {
        this.context = context;
        this.position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        this.symTable = SymbolTable.getInstance(context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
        this.position.setLine(this.position.getLine() + 1);
        this.nodeStack = new Stack<>();
        this.context.put(NodeContextKeys.NODE_STACK_KEY, nodeStack);
    }

    public void visit(BLangPackage pkgNode) {
        boolean isTestSrc = CommonUtil.isTestSource(this.context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY));
        BLangPackage evalPkg = isTestSrc ? pkgNode.getTestablePkg() : pkgNode;
        List<TopLevelNode> topLevelNodes = CommonUtil.getCurrentFileTopLevelNodes(evalPkg, this.context);
        topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        if (pkgSymbol == null) {
            return;
        }
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgSymbol);
        acceptNode(pkgEnv.node);
    }

    public void visit(BLangFunction funcNode) {
        // Check for native functions
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol) || !CommonUtil.isValidInvokableSymbol(funcSymbol)) {
            return;
        }

        if (HoverUtil.isMatchingPosition(HoverUtil.getIdentifierPosition(funcNode), this.position)) {
            addPosition(funcNode, this.previousNode, funcNode.name.getValue(), funcSymbol.pkgID, funcSymbol.kind.name(),
                        funcSymbol.kind.name(), funcNode.name.getValue(), funcSymbol.owner);
            setTerminateVisitor(true);
            return;
        }

        addTopLevelNodeToContext(funcNode, funcNode.name.getValue(), funcSymbol.pkgID, funcSymbol.kind.name(),
                                 funcSymbol.kind.name(), funcSymbol.owner);
        setPreviousNode(funcNode);
        this.addToNodeStack(funcNode);

        if (funcNode.receiver != null) {
            this.acceptNode(funcNode.receiver);
        }

        if (funcNode.requiredParams != null) {
            funcNode.requiredParams.forEach(this::acceptNode);
        }

        if (funcNode.returnTypeNode != null && !(funcNode.returnTypeNode.type instanceof BNilType)) {
            this.acceptNode(funcNode.returnTypeNode);
        }

        if (funcNode.endpoints != null) {
            funcNode.endpoints.forEach(this::acceptNode);
        }

        if (funcNode.body != null) {
            this.acceptNode(funcNode.body);
        }

        // Process workers
        if (funcNode.workers != null) {
            funcNode.workers.forEach(this::acceptNode);
        }

        if (funcNode.defaultableParams != null) {
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
                                                                                                     .typeName
                                                                                                     .getValue())) {
                            addPosition(userDefinedType, this.previousNode, userDefinedType.typeName.getValue(),
                                        type.tsymbol.pkgID, type.tsymbol.kind.name(), type.tsymbol.kind.name(),
                                        userDefinedType.typeName.getValue(), type.tsymbol.owner);
                            setTerminateVisitor(true);
                            break;
                        }
                    }
                } catch (ClassCastException e) {
                    // Ignores
                }
            } else if (userDefinedType.type.tsymbol != null &&
                    HoverUtil.isMatchingPosition(userDefinedType.getPosition(), this.position)) {
                addPosition(userDefinedType, this.previousNode, userDefinedType.typeName.getValue(),
                            userDefinedType.type.tsymbol.pkgID, userDefinedType.type.tsymbol.kind.name(),
                            userDefinedType.type.tsymbol.kind.name(),
                            userDefinedType.typeName.getValue(), userDefinedType.type.tsymbol.owner);
                setTerminateVisitor(true);
            }
        }
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        setPreviousNode(varNode);
        if (varNode.symbol != null) {
            CommonUtil.calculateEndColumnOfGivenName(varNode.getPosition(), varNode.symbol.name.getValue(), "");
            DiagnosticPos identifierPos = HoverUtil.getIdentifierPosition(varNode);
            if (HoverUtil.isMatchingPosition(identifierPos, this.position)) {
                addPosition(varNode, this.previousNode, varNode.symbol.name.getValue(), varNode.symbol.pkgID,
                            ContextConstants.ENDPOINT, ContextConstants.ENDPOINT, varNode.symbol.name.getValue(),
                            varNode.symbol.owner);
                setTerminateVisitor(true);
            }
        }

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
            addPosition(varRefExpr, this.previousNode, ((BEndpointVarSymbol) varRefExpr.symbol).name.getValue(),
                        ((BEndpointVarSymbol) varRefExpr.symbol).pkgID, ContextConstants.ENDPOINT,
                        ContextConstants.ENDPOINT, varRefExpr.variableName.getValue(), varRefExpr.symbol.owner);
            setTerminateVisitor(true);
        } else if (varRefExpr.type != null && varRefExpr.type.tsymbol != null && varRefExpr.type.tsymbol.kind != null
                && (varRefExpr.type.tsymbol.kind.name().equals(ContextConstants.OBJECT) ||
                varRefExpr.type.tsymbol.kind.name().equals(ContextConstants.RECORD) ||
                varRefExpr.type.tsymbol.kind.name().equals(ContextConstants.TYPE_DEF))
                && HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            if (varRefExpr.symbol != null) {
                addPosition(varRefExpr, this.previousNode, varRefExpr.type.tsymbol.name.getValue(),
                            varRefExpr.symbol.pkgID, ContextConstants.VARIABLE, varRefExpr.type.tsymbol.kind.name(),
                            varRefExpr.variableName.getValue(), varRefExpr.symbol.owner);
            } else {
                addPosition(varRefExpr, this.previousNode, varRefExpr.type.tsymbol.name.getValue(),
                            varRefExpr.type.tsymbol.pkgID, varRefExpr.type.tsymbol.kind.name(),
                            varRefExpr.type.tsymbol.kind.name(), varRefExpr.variableName.getValue(),
                            varRefExpr.type.tsymbol.owner);
            }
            setTerminateVisitor(true);
        } else if (varRefExpr.pkgSymbol != null
                && HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            if (varRefExpr.symbol != null) {
                addPosition(varRefExpr, this.previousNode, varRefExpr.variableName.getValue(),
                            varRefExpr.pkgSymbol.pkgID, ContextConstants.VARIABLE, ContextConstants.VARIABLE,
                            varRefExpr.variableName.getValue(), varRefExpr.symbol.owner);
            } else {
                addPosition(varRefExpr, this.previousNode, varRefExpr.type.tsymbol.name.getValue(),
                            varRefExpr.type.tsymbol.pkgID, varRefExpr.type.tsymbol.kind.name(),
                            varRefExpr.type.tsymbol.kind.name(), varRefExpr.variableName.getValue(),
                            varRefExpr.type.tsymbol.owner);
            }
            setTerminateVisitor(true);
        } else if (HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position) && varRefExpr.symbol != null) {
            addPosition(varRefExpr, this.previousNode, varRefExpr.symbol.name.getValue(), varRefExpr.symbol.pkgID,
                        ContextConstants.VARIABLE, ContextConstants.VARIABLE, varRefExpr.variableName.getValue(),
                        varRefExpr.symbol.owner);
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
        if (blockNode.stmts != null) {
            blockNode.stmts.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        setPreviousNode(varDefNode);
        if (varDefNode.getVariable() != null) {
            this.acceptNode(varDefNode.getVariable());
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        setPreviousNode(assignNode);
        if (assignNode.varRef != null) {
            this.acceptNode(assignNode.varRef);
        }
        if (assignNode.expr != null) {
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

    @Override
    public void visit(BLangIndexBasedAccess indexBasedAccess) {
        setPreviousNode(indexBasedAccess);
        if (indexBasedAccess.expr != null) {
            this.acceptNode(indexBasedAccess.expr);
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

    public void visit(BLangAction actionNode) {
        addTopLevelNodeToContext(actionNode, actionNode.name.getValue(), actionNode.symbol.pkgID,
                                 actionNode.symbol.kind.name(), actionNode.symbol.kind.name(), actionNode.symbol.owner);

        setPreviousNode(actionNode);
        this.addToNodeStack(actionNode);

        if (actionNode.requiredParams != null) {
            actionNode.requiredParams.forEach(this::acceptNode);
        }

        if (actionNode.body != null) {
            acceptNode(actionNode.body);
        }

        if (actionNode.workers != null) {
            actionNode.workers.forEach(this::acceptNode);
        }
    }

    public void visit(BLangService serviceNode) {
        if (HoverUtil.isMatchingPosition(HoverUtil.getIdentifierPosition(serviceNode), this.position)) {
            addPosition(serviceNode, this.previousNode, serviceNode.name.getValue(), serviceNode.symbol.pkgID,
                        serviceNode.symbol.kind.name(), serviceNode.symbol.kind.name(), serviceNode.name.getValue(),
                        serviceNode.symbol.owner);
            setTerminateVisitor(true);
            return;
        }

        addTopLevelNodeToContext(serviceNode, serviceNode.name.getValue(), serviceNode.symbol.pkgID,
                                 serviceNode.symbol.kind.name(), serviceNode.symbol.kind.name(),
                                 serviceNode.symbol.owner);

        setPreviousNode(serviceNode);
        this.addToNodeStack(serviceNode);

        if (serviceNode.serviceTypeStruct != null) {
            this.acceptNode(serviceNode.serviceTypeStruct);
        }

        if (serviceNode.vars != null) {
            serviceNode.vars.forEach(this::acceptNode);
        }

        if (serviceNode.resources != null) {
            serviceNode.resources.forEach(this::acceptNode);
        }

        if (serviceNode.endpoints != null) {
            serviceNode.endpoints.forEach(this::acceptNode);
        }

        if (serviceNode.boundEndpoints != null) {
            serviceNode.boundEndpoints.forEach(this::acceptNode);
        }

        if (serviceNode.initFunction != null) {
            this.acceptNode(serviceNode.initFunction);
        }
    }

    public void visit(BLangResource resourceNode) {
        if (HoverUtil.isMatchingPosition(HoverUtil.getIdentifierPosition(resourceNode), this.position)) {
            addPosition(resourceNode, this.previousNode, resourceNode.name.getValue(), resourceNode.symbol.pkgID,
                        resourceNode.symbol.kind.name(), resourceNode.symbol.kind.name(), resourceNode.name.getValue(),
                        resourceNode.symbol.owner);
            setTerminateVisitor(true);
            return;
        }
        addTopLevelNodeToContext(resourceNode, resourceNode.name.getValue(), resourceNode.symbol.pkgID,
                                 resourceNode.symbol.kind.name(), resourceNode.symbol.kind.name(),
                                 resourceNode.symbol.owner);

        setPreviousNode(resourceNode);
        this.addToNodeStack(resourceNode);

        if (resourceNode.requiredParams != null) {
            resourceNode.requiredParams.forEach(this::acceptNode);
        }

        if (resourceNode.body != null) {
            this.acceptNode(resourceNode.body);
        }

        if (resourceNode.endpoints != null) {
            resourceNode.endpoints.forEach(this::acceptNode);
        }

        if (resourceNode.workers != null) {
            resourceNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {
        setPreviousNode(tryCatchFinally);
        if (tryCatchFinally.tryBody != null) {
            acceptNode(tryCatchFinally.tryBody);
        }

        if (tryCatchFinally.catchBlocks != null) {
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

        if (forkJoin.workers != null) {
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
        if (workerNode.requiredParams != null) {
            workerNode.requiredParams.forEach(this::acceptNode);
        }

        if (workerNode.body != null) {
            acceptNode(workerNode.body);
        }

        if (workerNode.workers != null) {
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

        if (invocationExpr.argExprs != null) {
            invocationExpr.argExprs.forEach(this::acceptNode);
        }

        if (!terminateVisitor && HoverUtil.isMatchingPosition(invocationExpr.getPosition(), this.position)) {
            BSymbol symbol = invocationExpr.symbol;
            if (symbol != null) {
                addPosition(invocationExpr, this.previousNode, invocationExpr.name.getValue(), symbol.pkgID,
                            ContextConstants.FUNCTION, ContextConstants.FUNCTION, invocationExpr.name.getValue(),
                            symbol.owner);
            } else {
                BTypeSymbol tSymbol = invocationExpr.type.tsymbol;
                addPosition(invocationExpr, this.previousNode, invocationExpr.name.getValue(), tSymbol.pkgID,
                            ContextConstants.FUNCTION, ContextConstants.FUNCTION, invocationExpr.name.getValue(),
                            tSymbol.owner);
            }
            setTerminateVisitor(true);
        }
    }

    public void visit(BLangForeach foreach) {
        setPreviousNode(foreach);
        if (foreach.collection != null) {
            acceptNode(foreach.collection);
        }

        if (foreach.varRefs != null) {
            foreach.varRefs.forEach(this::acceptNode);
        }

        if (foreach.body != null) {
            acceptNode(foreach.body);
        }
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        setPreviousNode(arrayLiteral);
        if (arrayLiteral.exprs != null) {
            arrayLiteral.exprs.forEach(this::acceptNode);
        }
    }

    public void visit(BLangTypeInit connectorInitExpr) {
        setPreviousNode(connectorInitExpr);
        if (connectorInitExpr.userDefinedType != null) {
            acceptNode(connectorInitExpr.userDefinedType);
        }

        if (connectorInitExpr.argsExpr != null) {
            connectorInitExpr.argsExpr.forEach(this::acceptNode);
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
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        setPreviousNode(stringTemplateLiteral);

        if (stringTemplateLiteral.exprs != null) {
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
    public void visit(BLangEndpoint endpointNode) {
        setPreviousNode(endpointNode);

        DiagnosticPos identifierPos = HoverUtil.getIdentifierPosition(endpointNode);
        if (HoverUtil.isMatchingPosition(identifierPos, this.position)) {
            addPosition(endpointNode, this.previousNode, endpointNode.symbol.name.getValue(), endpointNode.symbol.pkgID,
                        ContextConstants.ENDPOINT, ContextConstants.ENDPOINT, endpointNode.symbol.name.getValue(),
                        endpointNode.symbol.owner);
            setTerminateVisitor(true);
            return;
        }

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
        if (unionTypeNode.memberTypeNodes != null) {
            unionTypeNode.memberTypeNodes.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        setPreviousNode(unaryExpr);
        if (unaryExpr.expr != null) {
            this.acceptNode(unaryExpr.expr);
        }
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        setPreviousNode(tupleTypeNode);
        if (tupleTypeNode.memberTypeNodes != null) {
            tupleTypeNode.memberTypeNodes.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        setPreviousNode(bracedOrTupleExpr);
        if (bracedOrTupleExpr.expressions != null) {
            bracedOrTupleExpr.expressions.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        setPreviousNode(stmt);
        if (stmt.varRef.expressions != null) {
            stmt.varRef.expressions.forEach(this::acceptNode);
        }

        if (stmt.expr != null) {
            this.acceptNode(stmt.expr);
        }
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        BSymbol recordSymbol = recordTypeNode.symbol;
        addTopLevelNodeToContext(recordTypeNode, recordSymbol.name.getValue(), recordSymbol.pkgID,
                                 recordSymbol.kind.name(), recordSymbol.kind.name(), recordSymbol.owner);
        setPreviousNode(recordTypeNode);
        if (recordTypeNode.fields != null) {
            recordTypeNode.fields.forEach(this::acceptNode);
        }

        if (recordTypeNode.initFunction != null &&
                !(recordTypeNode.initFunction.returnTypeNode.type instanceof BNilType)) {
            this.acceptNode(recordTypeNode.initFunction);
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        setPreviousNode(objectTypeNode);

        if (objectTypeNode.fields != null) {
            objectTypeNode.fields.forEach(this::acceptNode);
        }

        if (objectTypeNode.functions != null) {
            objectTypeNode.functions.forEach(this::acceptNode);
        }

        if (objectTypeNode.initFunction != null) {
            this.acceptNode(objectTypeNode.initFunction);
        }

        if (objectTypeNode.receiver != null) {
            this.acceptNode(objectTypeNode.receiver);
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        setPreviousNode(matchNode);
        if (matchNode.expr != null) {
            this.acceptNode(matchNode.expr);
        }

        if (matchNode.patternClauses != null) {
            matchNode.patternClauses.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        setPreviousNode(patternClauseNode);
        if (patternClauseNode.variable != null) {
            this.acceptNode(patternClauseNode.variable);
        }

        if (patternClauseNode.body != null) {
            this.acceptNode(patternClauseNode.body);
        }
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause patternClauseNode) {
        /*ignore*/
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        setPreviousNode(recordLiteral);

        if (recordLiteral.keyValuePairs != null) {
            recordLiteral.keyValuePairs.forEach((bLangRecordKeyValue -> {
                if (bLangRecordKeyValue.valueExpr != null) {
                    this.acceptNode(bLangRecordKeyValue.valueExpr);
                }
            }));
        }

        if (recordLiteral.impConversionExpr != null) {
            this.acceptNode(recordLiteral.impConversionExpr);
        }
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        setPreviousNode(bLangMatchExpression);

        if (bLangMatchExpression.impConversionExpr != null) {
            this.acceptNode(bLangMatchExpression.impConversionExpr);
        }

        if (bLangMatchExpression.expr != null) {
            this.acceptNode(bLangMatchExpression.expr);
        }

        if (bLangMatchExpression.patternClauses != null) {
            bLangMatchExpression.patternClauses.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        setPreviousNode(bLangMatchExprPatternClause);

        if (bLangMatchExprPatternClause.variable != null) {
            this.acceptNode(bLangMatchExprPatternClause.variable);
        }

        if (bLangMatchExprPatternClause.expr != null) {
            this.acceptNode(bLangMatchExprPatternClause.expr);
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        addTopLevelNodeToContext(typeDefinition, typeDefinition.name.getValue(), typeDefinition.symbol.pkgID,
                                 typeDefinition.symbol.kind.name(), typeDefinition.symbol.kind.name(),
                                 typeDefinition.symbol.owner);
        setPreviousNode(typeDefinition);

        if (typeDefinition.typeNode != null) {
            this.acceptNode(typeDefinition.typeNode);
        }
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        setPreviousNode(checkedExpr);

        if (checkedExpr.expr != null) {
            this.acceptNode(checkedExpr.expr);
        }
    }

    @Override
    public void visit(BLangScope scopeNode) {
        setPreviousNode(scopeNode);

        if (scopeNode.scopeBody != null) {
            acceptNode(scopeNode.scopeBody);
        }

        acceptNode(scopeNode.compensationFunction);
    }

    @Override
    public void visit(BLangCompoundAssignment assignment) {
        setPreviousNode(assignment);

        if (assignment.varRef != null) {
            acceptNode(assignment.varRef);
        }

        acceptNode(assignment.expr);
    }

    /**
     * Accept node to visit.
     *
     * @param node node to be accepted to visit.
     */
    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor || node == null) {
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
                                          BSymbol owner) {
        if (node.getPosition().sLine == this.position.getLine()) {
            addPosition(node, this.previousNode, name, currentPkg, symbolKindOfCurrentNode, symbolKindOfParentNode,
                        name, owner);
        }
    }

    private void addPosition(BLangNode node, Object previousNode, String name, PackageID pkgID, String nodeKind,
                             String nodeParentKind, String varName, BSymbol owner) {
        this.context.put(NodeContextKeys.NODE_KEY, node);
        this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, previousNode);
        this.context.put(NodeContextKeys.NAME_OF_NODE_KEY, name);
        this.context.put(NodeContextKeys.PACKAGE_OF_NODE_KEY, pkgID);
        this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_KEY, nodeKind);
        this.context.put(NodeContextKeys.SYMBOL_KIND_OF_NODE_PARENT_KEY, nodeParentKind);
        this.context.put(NodeContextKeys.VAR_NAME_OF_NODE_KEY, varName);
        this.context.put(NodeContextKeys.NODE_OWNER_KEY, owner.name.getValue());
        this.context.put(NodeContextKeys.NODE_OWNER_PACKAGE_KEY, owner.pkgID);
    }
}
