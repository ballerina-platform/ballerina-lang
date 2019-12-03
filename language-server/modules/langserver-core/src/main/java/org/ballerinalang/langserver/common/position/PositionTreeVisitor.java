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
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

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
        topLevelNodes.stream()
                .filter(CommonUtil.checkInvalidTypesDefs())
                .forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
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
            addPosition(funcNode, this.previousNode);
            setTerminateVisitor();
            return;
        }

        addTopLevelNodeToContext(funcNode);
        setPreviousNode(funcNode);
        this.addToNodeStack(funcNode);

        if (funcNode.requiredParams != null) {
            funcNode.requiredParams.forEach(this::acceptNode);
        }

        if (funcNode.returnTypeNode != null && !(funcNode.returnTypeNode.type instanceof BNilType)) {
            this.acceptNode(funcNode.returnTypeNode);
        }

        if (funcNode.body != null) {
            this.acceptNode(funcNode.body);
        }

        // Process workers
        if (funcNode.workers != null) {
            funcNode.workers.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        if (userDefinedType.getPosition() != null) {
            CommonUtil.calculateEndColumnOfGivenName(userDefinedType.getPosition(), userDefinedType.typeName.value,
                                                     userDefinedType.pkgAlias.value);
            if (userDefinedType.type instanceof BUnionType &&
                    HoverUtil.isMatchingPosition(userDefinedType.getPosition(), this.position)) {
                try {
                    BUnionType bUnionType = (BUnionType) userDefinedType.type;
                    for (BType type : bUnionType.getMemberTypes()) {
                        if (type.tsymbol != null && type.tsymbol.getName().getValue().equals(userDefinedType
                                                                                                     .typeName
                                                                                                     .getValue())) {
                            addPosition(userDefinedType, this.previousNode);
                            setTerminateVisitor();
                            break;
                        }
                    }
                } catch (ClassCastException e) {
                    // Ignores
                }
            } else if (userDefinedType.type.tsymbol != null &&
                    HoverUtil.isMatchingPosition(userDefinedType.getPosition(), this.position)) {
                addPosition(userDefinedType, this.previousNode);
                setTerminateVisitor();
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
                addPosition(varNode, this.previousNode);
                setTerminateVisitor();
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
        if (varRefExpr.type != null && varRefExpr.type.tsymbol != null && varRefExpr.type.tsymbol.kind != null
                && (varRefExpr.type.tsymbol.kind.name().equals(ContextConstants.OBJECT) ||
                varRefExpr.type.tsymbol.kind.name().equals(ContextConstants.RECORD) ||
                varRefExpr.type.tsymbol.kind.name().equals(ContextConstants.TYPE_DEF))
                && HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            addPosition(varRefExpr, this.previousNode);
            setTerminateVisitor();
        } else if (varRefExpr.pkgSymbol != null
                && HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position)) {
            addPosition(varRefExpr, this.previousNode);
            setTerminateVisitor();
        } else if (HoverUtil.isMatchingPosition(varRefExpr.getPosition(), this.position) && varRefExpr.symbol != null) {
            addPosition(varRefExpr, this.previousNode);
            setTerminateVisitor();
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

    public void visit(BLangService serviceNode) {
        if (serviceNode.attachedExprs != null) {
            serviceNode.attachedExprs.forEach(this::acceptNode);
        }

        if (HoverUtil.isMatchingPosition(HoverUtil.getIdentifierPosition(serviceNode), this.position)) {
            addPosition(serviceNode, this.previousNode);
            setTerminateVisitor();
            return;
        }
        List<BLangNode> serviceContent = new ArrayList<>();
        BLangObjectTypeNode serviceType = (BLangObjectTypeNode) serviceNode.serviceTypeDefinition.typeNode;
        List<BLangFunction> serviceFunctions = ((BLangObjectTypeNode) serviceNode.serviceTypeDefinition.typeNode)
                .getFunctions();
        List<BLangSimpleVariable> serviceFields = serviceType.getFields().stream()
                .map(simpleVar -> (BLangSimpleVariable) simpleVar)
                .collect(Collectors.toList());
        serviceContent.addAll(serviceFunctions);
        serviceContent.addAll(serviceFields);
        serviceContent.sort(new CommonUtil.BLangNodeComparator());

        addTopLevelNodeToContext(serviceNode);

        setPreviousNode(serviceNode);
        this.addToNodeStack(serviceNode);

        serviceContent.forEach(this::acceptNode);
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
        //TODO Worker receive node is now an expression not a statement
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
            addPosition(invocationExpr, this.previousNode);
            setTerminateVisitor();
        }
    }

    public void visit(BLangForeach foreach) {
        setPreviousNode(foreach);
        if (foreach.collection != null) {
            acceptNode(foreach.collection);
        }

        acceptNode((BLangNode) foreach.variableDefinitionNode);

        if (foreach.body != null) {
            acceptNode(foreach.body);
        }
    }

    public void visit(BLangListConstructorExpr listConstructorExpr) {
        setPreviousNode(listConstructorExpr);
        if (listConstructorExpr.exprs != null) {
            listConstructorExpr.exprs.forEach(this::acceptNode);
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
    public void visit(BLangGroupExpr groupExpr) {
        setPreviousNode(groupExpr);
        if (groupExpr.expression != null) {
            this.acceptNode(groupExpr.expression);
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
        addTopLevelNodeToContext(recordTypeNode);
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
        addTopLevelNodeToContext(typeDefinition);
        setPreviousNode(typeDefinition);

        if (typeDefinition.typeNode != null) {
            this.acceptNode(typeDefinition.typeNode);
        }
    }

    @Override
    public void visit(BLangConstant constant) {
        addTopLevelNodeToContext(constant);
        setPreviousNode(constant);

        if (constant.typeNode != null) {
            this.acceptNode(constant.typeNode);
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
     */
    private void setTerminateVisitor() {
        this.terminateVisitor = true;
    }

    /**
     * Add the top level node to context if cursor position matches the line of signature.
     *
     * @param node node to be compared and added to the context
     */
    private void addTopLevelNodeToContext(BLangNode node) {
        if (node.getPosition().sLine == this.position.getLine()) {
            addPosition(node, this.previousNode);
        }
    }

    private void addPosition(BLangNode node, Object previousNode) {
        this.context.put(NodeContextKeys.NODE_KEY, node);
        this.context.put(NodeContextKeys.PREVIOUSLY_VISITED_NODE_KEY, previousNode);
    }
}
