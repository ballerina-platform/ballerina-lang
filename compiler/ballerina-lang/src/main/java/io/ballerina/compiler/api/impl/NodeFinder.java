/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.compiler.api.impl;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorCauseBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorFieldBindingPatterns;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangErrorMessageBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangFieldBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangMappingBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangNamedArgBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangRestBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangSimpleBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementFilter;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorCauseMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorFieldMatchPatterns;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangErrorMessageMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangFieldMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangListMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMappingMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangNamedArgMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangRestMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangSimpleMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDo;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatchStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetryTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the enclosing AST node for the given position.
 *
 * @since 2.0.0
 */
class NodeFinder extends BaseVisitor {

    private LineRange range;
    private BLangNode enclosingNode;
    private BLangNode enclosingContainer;
    private final boolean allowExprStmts;

    NodeFinder(boolean allowExprStmts) {
        this.allowExprStmts = allowExprStmts;
    }

    void lookup(BLangPackage module, LineRange range) {
        List<TopLevelNode> topLevelNodes = new ArrayList<>(module.topLevelNodes);
        BLangTestablePackage tests = module.getTestablePkg();

        if (tests != null) {
            topLevelNodes.addAll(tests.topLevelNodes);
        }

        lookupTopLevelNodesV2(topLevelNodes, range);
    }

    BLangNode lookup(BLangCompilationUnit unit, LineRange range) {
        return lookupTopLevelNodesV2(unit.topLevelNodes, range);
    }

    BLangNode lookupEnclosingContainer(BLangPackage module, LineRange range) {
        this.enclosingContainer = module;
        lookup(module, range);
        return this.enclosingContainer;
    }

    BLangNode lookupEnclosingContainer(BLangCompilationUnit compilationUnit, LineRange range) {
        this.enclosingContainer = compilationUnit;
        lookup(compilationUnit, range);
        return this.enclosingContainer;
    }

    private BLangNode lookupTopLevelNodesV2(List<TopLevelNode> nodes, LineRange range) {
        this.range = range;
        this.enclosingNode = null;

        for (TopLevelNode node : nodes) {
            BLangNode bLangNode = (BLangNode) node;
            if (PositionUtil.isRangeWithinNode(this.range, node.getPosition())
                    && !isLambdaFunction(node) && !isClassForService(node)) {
                bLangNode.accept(this);
                return this.enclosingNode;
            }
        }
        return this.enclosingNode;
    }

    private boolean lookupNodesV2(List<? extends BLangNode> nodes) {
        for (BLangNode node : nodes) {
            if (lookupNodeV2(node)) {
                return true;
            }
        }
        return false;
    }

    private boolean lookupNodeV2(BLangNode node) {
        if (node == null) {
            return false;
        }
        if (PositionUtil.isRangeWithinNode(this.range, node.pos)) {
            node.accept(this);
            return this.enclosingNode != null;
        }
        return false;
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        lookupNodeV2(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        // Compare the target lookup pos with the function symbol pos to ensure that we are not looking for the
        // container of the function.
        if (!this.range.equals(funcNode.symbol.pos.lineRange())) {
            this.enclosingContainer = funcNode;
        }

        if (lookupNodesV2(funcNode.requiredParams)) {
            return;
        }
        if (lookupNodeV2(funcNode.restParam)) {
            return;
        }
        if (lookupNodeV2(funcNode.returnTypeNode)) {
            return;
        }
        lookupNodeV2(funcNode.body);
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        this.enclosingContainer = blockFuncBody;
        lookupNodesV2(blockFuncBody.stmts);
    }

    @Override
    public void visit(BLangExprFunctionBody exprFuncBody) {
        lookupNodeV2(exprFuncBody.expr);
    }

    @Override
    public void visit(BLangExternalFunctionBody externFuncBody) {
        lookupNodesV2(externFuncBody.annAttachments);
    }

    @Override
    public void visit(BLangService serviceNode) {
        if (lookupNodesV2(serviceNode.annAttachments)) {
            return;
        }
        if (lookupNodesV2(serviceNode.attachedExprs)) {
            return;
        }
        lookupNodeV2(serviceNode.serviceClass);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        lookupNodeV2(typeDefinition.typeNode);
    }

    @Override
    public void visit(BLangConstant constant) {
        if (lookupNodeV2(constant.typeNode)) {
            return;
        }
        if (lookupNodeV2(constant.expr)) {
            return;
        }
        setEnclosingNode(constant, constant.name.pos);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if (lookupNodesV2(varNode.annAttachments)) {
            return;
        }
        if (lookupNodeV2(varNode.typeNode)) {
            return;
        }
        if (lookupNodeV2(varNode.expr)) {
            return;
        }
        setEnclosingNode(varNode, varNode.name.pos);
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        if (lookupNodeV2(annotationNode.typeNode)) {
            return;
        }
        setEnclosingNode(annotationNode, annotationNode.name.pos);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        if (lookupNodeV2(annAttachmentNode.expr)) {
            return;
        }
        setEnclosingNode(annAttachmentNode, annAttachmentNode.annotationName.pos);
    }

    @Override
    public void visit(BLangTableKeySpecifier tableKeySpecifierNode) {
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint tableKeyTypeConstraint) {
        lookupNodeV2(tableKeyTypeConstraint.keyType);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        this.enclosingContainer = blockNode;
        lookupNodesV2(blockNode.stmts);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        lookupNodeV2(varDefNode.var);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        if (lookupNodeV2(assignNode.varRef)) {
            return;
        }
        lookupNodeV2(assignNode.expr);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        if (lookupNodeV2(compoundAssignNode.varRef)) {
            return;
        }
        lookupNodeV2(compoundAssignNode.expr);
    }

    @Override
    public void visit(BLangRetry retryNode) {
        if (lookupNodeV2(retryNode.retryBody)) {
            return;
        }
        if (lookupNodeV2(retryNode.retrySpec)) {
            return;
        }
        lookupNodeV2(retryNode.onFailClause);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        if (lookupNodeV2(retryTransaction.transaction)) {
            return;
        }
        lookupNodeV2(retryTransaction.retrySpec);
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
        if (lookupNodeV2(retrySpec.retryManagerType)) {
            return;
        }
        lookupNodesV2(retrySpec.argExprs);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        lookupNodeV2(returnNode.expr);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        lookupNodeV2(panicNode.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        lookupNodeV2(xmlnsStmtNode.xmlnsDecl);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        lookupNodeV2(exprStmtNode.expr);

        if (this.allowExprStmts) {
            setEnclosingNode(exprStmtNode.expr, exprStmtNode.pos);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        if (lookupNodeV2(ifNode.expr)) {
            return;
        }
        if (lookupNodeV2(ifNode.body)) {
            return;
        }
        lookupNodeV2(ifNode.elseStmt);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        if (lookupNodesV2(queryAction.queryClauseList)) {
            return;
        }
        lookupNodeV2(queryAction.doClause);
    }

    @Override
    public void visit(BLangForeach foreach) {
        if (lookupNodeV2((BLangNode) foreach.variableDefinitionNode)) {
            return;
        }
        if (lookupNodeV2(foreach.collection)) {
            return;
        }
        if (lookupNodeV2(foreach.body)) {
            return;
        }
        lookupNodeV2(foreach.onFailClause);
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        if (lookupNodeV2(fromClause.collection)) {
            return;
        }
        if (lookupNodeV2((BLangNode) fromClause.variableDefinitionNode)) {
            return;
        }
        this.enclosingNode = fromClause;
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        if (lookupNodeV2(joinClause.collection)) {
            return;
        }
        if (lookupNodeV2((BLangNode) joinClause.variableDefinitionNode)) {
            return;
        }
        lookupNodeV2(joinClause.onClause);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable var : letClause.letVarDeclarations) {
            if (lookupNodeV2((BLangNode) var.definitionNode)) {
                return;
            }
        }
    }

    @Override
    public void visit(BLangOnClause onClause) {
        if (lookupNodeV2(onClause.lhsExpr)) {
            return;
        }
        lookupNodeV2(onClause.rhsExpr);
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
        lookupNodeV2(orderKeyClause.expression);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        for (OrderKeyNode key : orderByClause.orderByKeyList) {
            if (lookupNodeV2((BLangNode) key)) {
                return;
            }
        }
        this.enclosingNode = orderByClause;
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        lookupNodeV2(selectClause.expression);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        lookupNodeV2(whereClause.expression);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        lookupNodeV2(doClause.body);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        lookupNodeV2(onConflictClause.expression);
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        lookupNodeV2(limitClause.expression);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        if (lookupNodeV2(whileNode.expr)) {
            return;
        }
        if (lookupNodeV2(whileNode.body)) {
            return;
        }
        lookupNodeV2(whileNode.onFailClause);
    }

    @Override
    public void visit(BLangLock lockNode) {
        if (lookupNodeV2(lockNode.body)) {
            return;
        }
        lookupNodeV2(lockNode.onFailClause);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        if (lookupNodeV2(transactionNode.transactionBody)) {
            return;
        }
        lookupNodeV2(transactionNode.onFailClause);
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        if (lookupNodeV2(stmt.expr)) {
            return;
        }
        lookupNodeV2(stmt.varRef);
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        if (lookupNodeV2(stmt.expr)) {
            return;
        }
        lookupNodeV2(stmt.varRef);
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        if (lookupNodeV2(stmt.expr)) {
            return;
        }
        lookupNodeV2(stmt.varRef);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        lookupNodesV2(forkJoin.workers);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        if (lookupNodeV2(workerSendNode.expr)) {
            return;
        }
        setEnclosingNode(workerSendNode, workerSendNode.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        setEnclosingNode(workerReceiveNode, workerReceiveNode.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        lookupNodeV2(rollbackNode.expr);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (lookupNodeV2((BLangNode) field)) {
                return;
            }
        }
        this.enclosingNode = recordLiteral;
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        if (lookupNodesV2(varRefExpr.expressions)) {
            return;
        }
        lookupNodeV2(varRefExpr.restParam);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        for (BLangRecordVarRef.BLangRecordVarRefKeyValue recordRefField : varRefExpr.recordRefFields) {
            if (lookupNodeV2(recordRefField.getBindingPattern())) {
                return;
            }
        }
        lookupNodeV2(varRefExpr.restParam);
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        if (lookupNodeV2(varRefExpr.message)) {
            return;
        }
        if (lookupNodesV2(varRefExpr.detail)) {
            return;
        }
        if (lookupNodeV2(varRefExpr.cause)) {
            return;
        }
        lookupNodeV2(varRefExpr.restVar);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        if (setEnclosingNode(varRefExpr, varRefExpr.variableName.pos)) {
            return;
        }

        setEnclosingNode(varRefExpr, varRefExpr.pos);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (setEnclosingNode(fieldAccessExpr, fieldAccessExpr.field.pos)) {
            return;
        }

        lookupNodeV2(fieldAccessExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        if (lookupNodeV2(indexAccessExpr.expr)) {
            return;
        }
        if (lookupNodeV2(indexAccessExpr.indexExpr)) {
            return;
        }
        this.enclosingNode = indexAccessExpr;
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        // Looking up args expressions since requiredArgs and restArgs get set only when compilation is successful
        if (lookupNodesV2(invocationExpr.argExprs)) {
            return;
        }
        if (lookupNodeV2(invocationExpr.expr)) {
            return;
        }

        if (setEnclosingNode(invocationExpr, invocationExpr.name.pos)) {
            return;
        }

        setEnclosingNode(invocationExpr, invocationExpr.pos);
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        if (lookupNodeV2(typeInit.userDefinedType)) {
            return;
        }
        if (lookupNodesV2(typeInit.argsExpr)) {
            return;
        }
        setEnclosingNode(typeInit, typeInit.pos);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        if (lookupNodesV2(actionInvocationExpr.argExprs)) {
            return;
        }
        if (lookupNodesV2(actionInvocationExpr.restArgs)) {
            return;
        }
        if (lookupNodeV2(actionInvocationExpr.expr)) {
            return;
        }
        if (setEnclosingNode(actionInvocationExpr, actionInvocationExpr.name.pos)) {
            return;
        }

        setEnclosingNode(actionInvocationExpr, actionInvocationExpr.pos);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        if (lookupNodeV2(ternaryExpr.expr)) {
            return;
        }
        if (lookupNodeV2(ternaryExpr.thenExpr)) {
            return;
        }
        if (lookupNodeV2(ternaryExpr.elseExpr)) {
            return;
        }
        this.enclosingNode = ternaryExpr;
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        lookupNodesV2(awaitExpr.exprList);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        if (lookupNodeV2(trapExpr.expr)) {
            return;
        }
        this.enclosingNode = trapExpr;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (lookupNodeV2(binaryExpr.lhsExpr)) {
            return;
        }
        if (lookupNodeV2(binaryExpr.rhsExpr)) {
            return;
        }
        if (PositionUtil.withinRange(binaryExpr.pos.lineRange(), this.range)) {
            this.enclosingNode = binaryExpr;
        }
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        if (lookupNodeV2(elvisExpr.lhsExpr)) {
            return;
        }
        lookupNodeV2(elvisExpr.rhsExpr);
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        if (lookupNodeV2(groupExpr.expression)) {
            return;
        }
        this.enclosingNode = groupExpr;
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        for (BLangLetVariable var : letExpr.letVarDeclarations) {
            if (lookupNodeV2((BLangNode) var.definitionNode)) {
                return;
            }
        }

        if (lookupNodeV2(letExpr.expr)) {
            return;
        }
        this.enclosingNode = letExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        if (lookupNodesV2(listConstructorExpr.exprs)) {
            return;
        }
        this.enclosingNode = listConstructorExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangListConstructorSpreadOpExpr spreadOpExpr) {
        lookupNodeV2(spreadOpExpr.expr);
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        if (lookupNodeV2(tableConstructorExpr.tableKeySpecifier)) {
            return;
        }
        if (lookupNodesV2(tableConstructorExpr.recordLiteralList)) {
            return;
        }
        if (PositionUtil.withinRange(tableConstructorExpr.pos.lineRange(), this.range)) {
            this.enclosingNode = tableConstructorExpr;
        }
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        lookupNodesV2(tupleLiteral.exprs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        lookupNodesV2(arrayLiteral.exprs);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        if (lookupNodeV2(unaryExpr.expr)) {
            return;
        }
        this.enclosingNode = unaryExpr;
    }

    @Override
    public void visit(BLangTypedescExpr typedescExpr) {
        lookupNodeV2(typedescExpr.typeNode);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        if (lookupNodesV2(conversionExpr.annAttachments)) {
            return;
        }
        if (lookupNodeV2(conversionExpr.typeNode)) {
            return;
        }
        if (lookupNodeV2(conversionExpr.expr)) {
            return;
        }
        this.enclosingNode = conversionExpr;
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        if (setEnclosingNode(xmlQName, xmlQName.pos)
                || setEnclosingNode(xmlQName, xmlQName.prefix.pos)) {
            return;
        }

        setEnclosingNode(xmlQName, xmlQName.localname.pos);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {

    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        if (lookupNodeV2(xmlElementLiteral.startTagName)) {
            return;
        }
        if (lookupNodesV2(xmlElementLiteral.attributes)) {
            return;
        }
        if (lookupNodesV2(xmlElementLiteral.children)) {
            return;
        }
        if (lookupNodeV2(xmlElementLiteral.endTagName)) {
            return;
        }
        this.enclosingNode = xmlElementLiteral;
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        if (lookupNodeV2(xmlTextLiteral.concatExpr)) {
            return;
        }
        lookupNodesV2(xmlTextLiteral.textFragments);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        if (lookupNodeV2(xmlCommentLiteral.concatExpr)) {
            return;
        }
        lookupNodesV2(xmlCommentLiteral.textFragments);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        if (lookupNodeV2(xmlProcInsLiteral.dataConcatExpr)) {
            return;
        }
        if (lookupNodesV2(xmlProcInsLiteral.dataFragments)) {
            return;
        }
        lookupNodeV2(xmlProcInsLiteral.target);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        if (lookupNodeV2(xmlQuotedString.concatExpr)) {
            return;
        }
        lookupNodesV2(xmlQuotedString.textFragments);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        lookupNodesV2(stringTemplateLiteral.exprs);
        setEnclosingNode(stringTemplateLiteral, stringTemplateLiteral.pos);
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        if (lookupNodesV2(rawTemplateLiteral.strings)) {
            return;
        }
        if (lookupNodesV2(rawTemplateLiteral.insertions)) {
            return;
        }
        this.enclosingNode = rawTemplateLiteral;
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        if (lookupNodeV2(bLangLambdaFunction.function)) {
            return;
        }
        if (PositionUtil.withinRange(bLangLambdaFunction.pos.lineRange(), this.range)) {
            this.enclosingNode = bLangLambdaFunction;
        }
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        if (lookupNodesV2(bLangArrowFunction.params)) {
            return;
        }
        if (lookupNodeV2(bLangArrowFunction.body)) {
            return;
        }
        this.enclosingNode = bLangArrowFunction;
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        lookupNodeV2(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        if (lookupNodeV2(bLangNamedArgsExpression.expr)) {
            return;
        }
        setEnclosingNode(bLangNamedArgsExpression.name, bLangNamedArgsExpression.name.pos);
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        if (lookupNodeV2(assignableExpr.lhsExpr)) {
            return;
        }
        lookupNodeV2(assignableExpr.typeNode);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        if (lookupNodeV2(checkedExpr.expr)) {
            return;
        }
        this.enclosingNode = checkedExpr;
    }

    @Override
    public void visit(BLangFail failExpr) {
        lookupNodeV2(failExpr.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        if (lookupNodeV2(checkPanickedExpr.expr)) {
            return;
        }
        this.enclosingNode = checkPanickedExpr;
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        lookupNodeV2(serviceConstructorExpr.serviceNode);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        if (lookupNodeV2(typeTestExpr.expr)) {
            return;
        }
        if (lookupNodeV2(typeTestExpr.typeNode)) {
            return;
        }
        this.enclosingNode = typeTestExpr;
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        if (lookupNodeV2(typeTestExpr.expr)) {
            return;
        }
        lookupNodeV2(typeTestExpr.typeNode);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        if (lookupNodeV2(annotAccessExpr.expr)) {
            return;
        }
        this.enclosingNode = annotAccessExpr;
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        if (lookupNodesV2(queryExpr.queryClauseList)) {
            return;
        }
        this.enclosingNode = queryExpr;
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        lookupNodeV2(arrayType.elemtype);
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        lookupNodeV2(constrainedType.constraint);
    }

    @Override
    public void visit(BLangStreamType streamType) {
        if (lookupNodeV2(streamType.constraint)) {
            return;
        }
        lookupNodeV2(streamType.error);
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
        if (lookupNodeV2(tableType.constraint)) {
            return;
        }
        if (lookupNodeV2(tableType.tableKeySpecifier)) {
            return;
        }
        lookupNodeV2(tableType.tableKeyTypeConstraint);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        setEnclosingNode(userDefinedType, userDefinedType.typeName.pos);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        if (lookupNodesV2(functionTypeNode.params)) {
            return;
        }
        if (lookupNodeV2(functionTypeNode.restParam)) {
            return;
        }
        lookupNodeV2(functionTypeNode.returnTypeNode);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        lookupNodesV2(unionTypeNode.memberTypeNodes);
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        lookupNodesV2(intersectionTypeNode.constituentTypeNodes);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        if (lookupNodesV2(classDefinition.annAttachments)) {
            return;
        }
        if (lookupNodesV2(classDefinition.fields)) {
            return;
        }
        if (lookupNodesV2(classDefinition.referencedFields)) {
            return;
        }
        if (lookupNodeV2(classDefinition.initFunction)) {
            return;
        }
        if (lookupNodesV2(classDefinition.functions)) {
            return;
        }
        if (lookupAnnAttachmentsAttachedToFunctions(classDefinition.functions)) {
            return;
        }
        if (lookupNodesV2(classDefinition.typeRefs)) {
            return;
        }
        setEnclosingNode(classDefinition, classDefinition.name.pos);
    }

    private boolean lookupAnnAttachmentsAttachedToFunctions(List<BLangFunction> functions) {
        for (BLangFunction func : functions) {
            if (lookupNodesV2(func.annAttachments)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation) {
        if (lookupNodesV2(resourceAccessInvocation.annAttachments)) {
            return;
        }
        if (lookupNodeV2(resourceAccessInvocation.expr)) {
            return;
        }
        if (lookupNodeV2(resourceAccessInvocation.resourceAccessPathSegments)) {
            return;
        }
        if (lookupNodesV2(resourceAccessInvocation.requiredArgs)) {
            return;
        }
        if (lookupNodesV2(resourceAccessInvocation.restArgs)) {
            return;
        }
        setEnclosingNode(resourceAccessInvocation, resourceAccessInvocation.pos);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        if (lookupNodesV2(objectTypeNode.fields)) {
            return;
        }
        if (lookupNodesV2(objectTypeNode.functions)) {
            return;
        }
        lookupNodesV2(objectTypeNode.typeRefs);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        if (lookupNodesV2(recordTypeNode.fields)) {
            return;
        }
        lookupNodesV2(recordTypeNode.typeRefs);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        lookupNodesV2(finiteTypeNode.valueSpace);
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        if (lookupNodesV2(tupleTypeNode.memberTypeNodes)) {
            return;
        }
        lookupNodeV2(tupleTypeNode.restParamType);
    }

    @Override
    public void visit(BLangErrorType errorType) {
        lookupNodeV2(errorType.detailType);
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        if (lookupNodeV2(errorConstructorExpr.errorTypeRef)) {
            return;
        }
        if (lookupNodesV2(errorConstructorExpr.positionalArgs)) {
            return;
        }
        if (lookupNodesV2(errorConstructorExpr.namedArgs)) {
            return;
        }
        this.enclosingNode = errorConstructorExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        if (lookupNodeV2(fieldAccessExpr.expr)) {
            return;
        }
        lookupNodeV2(fieldAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        if (setEnclosingNode(functionVarRef, functionVarRef.field.pos)) {
            return;
        }
        lookupNodeV2(functionVarRef.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        if (lookupNodeV2(mapKeyAccessExpr.expr)) {
            return;
        }
        lookupNodeV2(mapKeyAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        if (lookupNodeV2(arrayIndexAccessExpr.expr)) {
            return;
        }
        lookupNodeV2(arrayIndexAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
        if (lookupNodeV2(tableKeyAccessExpr.expr)) {
            return;
        }
        lookupNodeV2(tableKeyAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
        if (lookupNodeV2(xmlAccessExpr.expr)) {
            return;
        }
        lookupNodeV2(xmlAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        for (RecordLiteralNode.RecordField field : mapLiteral.fields) {
            if (lookupNodeV2((BLangNode) field)) {
                return;
            }
        }
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        for (RecordLiteralNode.RecordField field : structLiteral.fields) {
            if (lookupNodeV2((BLangNode) field)) {
                return;
            }
        }
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        if (lookupNodesV2(bFunctionPointerInvocation.requiredArgs)) {
            return;
        }
        if (lookupNodesV2(bFunctionPointerInvocation.restArgs)) {
            return;
        }
        setEnclosingNode(bFunctionPointerInvocation, bFunctionPointerInvocation.name.pos);
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        if (setEnclosingNode(iExpr, iExpr.name.pos)) {
            return;
        }

        if (lookupNodeV2(iExpr.expr)) {
            return;
        }
        if (lookupNodesV2(iExpr.requiredArgs)) {
            return;
        }
        lookupNodesV2(iExpr.restArgs);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        lookupNodesV2(jsonArrayLiteral.exprs);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        if (lookupNodeV2(jsonAccessExpr.expr)) {
            return;
        }
        lookupNodeV2(jsonAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        if (lookupNodeV2(stringAccessExpr.expr)) {
            return;
        }
        lookupNodeV2(stringAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        if (setEnclosingNode(xmlnsNode, xmlnsNode.prefix.pos)) {
            return;
        }
        lookupNodeV2(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        if (setEnclosingNode(xmlnsNode, xmlnsNode.prefix.pos)) {
            return;
        }

        lookupNodeV2(xmlnsNode.namespaceURI);
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        lookupNodesV2(bLangXMLSequenceLiteral.xmlItems);
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        if (lookupNodeV2(bLangStatementExpression.stmt)) {
            return;
        }
        lookupNodeV2(bLangStatementExpression.expr);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        if (lookupNodesV2(bLangTupleVariable.annAttachments)) {
            return;
        }
        if (lookupNodeV2(bLangTupleVariable.typeNode)) {
            return;
        }
        if (lookupNodesV2(bLangTupleVariable.memberVariables)) {
            return;
        }
        if (lookupNodeV2(bLangTupleVariable.restVariable)) {
            return;
        }
        lookupNodeV2(bLangTupleVariable.expr);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        lookupNodeV2(bLangTupleVariableDef.var);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        for (BLangRecordVariable.BLangRecordVariableKeyValue var : bLangRecordVariable.variableList) {
            if (lookupNodeV2(var.valueBindingPattern)) {
                return;
            }
        }
        if (lookupNodeV2(bLangRecordVariable.restParam)) {
            return;
        }
        if (lookupNodeV2(bLangRecordVariable.expr)) {
            return;
        }
        lookupNodesV2(bLangRecordVariable.annAttachments);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        lookupNodeV2(bLangRecordVariableDef.var);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        if (lookupNodesV2(bLangErrorVariable.annAttachments)) {
            return;
        }
        if (lookupNodeV2(bLangErrorVariable.typeNode)) {
            return;
        }
        if (lookupNodeV2(bLangErrorVariable.message)) {
            return;
        }

        for (BLangErrorVariable.BLangErrorDetailEntry detail : bLangErrorVariable.detail) {
            if (lookupNodeV2(detail.valueBindingPattern)) {
                return;
            }
        }

        if (lookupNodeV2(bLangErrorVariable.detailExpr)) {
            return;
        }
        if (lookupNodeV2(bLangErrorVariable.cause)) {
            return;
        }
        if (lookupNodeV2(bLangErrorVariable.reasonMatchConst)) {
            return;
        }
        if (lookupNodeV2(bLangErrorVariable.restDetail)) {
            return;
        }
        lookupNodeV2(bLangErrorVariable.expr);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        lookupNodeV2(bLangErrorVariableDef.errorVariable);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        setEnclosingNode(workerFlushExpr, workerFlushExpr.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        if (lookupNodeV2(syncSendExpr.expr)) {
            return;
        }
        setEnclosingNode(syncSendExpr, syncSendExpr.workerIdentifier.pos);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        lookupNodesV2(waitForAllExpr.keyValuePairs);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        lookupNodesV2(waitLiteral.keyValuePairs);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        if (lookupNodeV2(recordKeyValue.key)) {
            return;
        }
        lookupNodeV2(recordKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKey recordKey) {
        lookupNodeV2(recordKey.expr);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
        lookupNodeV2(spreadOperatorField.expr);
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        if (lookupNodeV2(waitKeyValue.keyExpr)) {
            return;
        }
        lookupNodeV2(waitKeyValue.valueExpr);
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
        setEnclosingNode(xmlElementFilter, xmlElementFilter.elemNamePos);
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        if (lookupNodeV2(xmlElementAccess.expr)) {
            return;
        }
        if (lookupNodesV2(xmlElementAccess.filters)) {
            return;
        }
        this.enclosingNode = xmlElementAccess;
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        if (lookupNodeV2(xmlNavigation.expr)) {
            return;
        }
        if (lookupNodeV2(xmlNavigation.childIndex)) {
            return;
        }
        if (lookupNodesV2(xmlNavigation.filters)) {
            return;
        }
        this.enclosingNode = xmlNavigation;
    }

    @Override
    public void visit(BLangMatchStatement matchStatementNode) {
        if (lookupNodeV2(matchStatementNode.expr)) {
            return;
        }
        if (lookupNodesV2(matchStatementNode.matchClauses)) {
            return;
        }
        lookupNodeV2(matchStatementNode.onFailClause);
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        if (lookupNodesV2(matchClause.matchPatterns)) {
            return;
        }
        if (lookupNodeV2(matchClause.matchGuard)) {
            return;
        }
        lookupNodeV2(matchClause.blockStmt);
    }

    @Override
    public void visit(BLangMappingMatchPattern mappingMatchPattern) {
        if (lookupNodesV2(mappingMatchPattern.fieldMatchPatterns)) {
            return;
        }
        lookupNodeV2(mappingMatchPattern.restMatchPattern);
    }

    @Override
    public void visit(BLangFieldMatchPattern fieldMatchPattern) {
        lookupNodeV2(fieldMatchPattern.matchPattern);
    }

    @Override
    public void visit(BLangListMatchPattern listMatchPattern) {
        if (lookupNodesV2(listMatchPattern.matchPatterns)) {
            return;
        }
        lookupNodeV2(listMatchPattern.restMatchPattern);
    }

    @Override
    public void visit(BLangErrorMatchPattern errorMatchPattern) {
        if (lookupNodeV2(errorMatchPattern.errorTypeReference)) {
            return;
        }
        if (lookupNodeV2(errorMatchPattern.errorMessageMatchPattern)) {
            return;
        }
        if (lookupNodeV2(errorMatchPattern.errorCauseMatchPattern)) {
            return;
        }
        lookupNodeV2(errorMatchPattern.errorFieldMatchPatterns);
    }

    @Override
    public void visit(BLangErrorMessageMatchPattern errorMessageMatchPattern) {
        lookupNodeV2(errorMessageMatchPattern.simpleMatchPattern);
    }

    @Override
    public void visit(BLangErrorCauseMatchPattern errorCauseMatchPattern) {
        if (lookupNodeV2(errorCauseMatchPattern.simpleMatchPattern)) {
            return;
        }
        lookupNodeV2(errorCauseMatchPattern.errorMatchPattern);
    }

    @Override
    public void visit(BLangErrorFieldMatchPatterns errorFieldMatchPatterns) {
        if (lookupNodesV2(errorFieldMatchPatterns.namedArgMatchPatterns)) {
            return;
        }
        lookupNodeV2(errorFieldMatchPatterns.restMatchPattern);
    }

    @Override
    public void visit(BLangSimpleMatchPattern simpleMatchPattern) {
        if (lookupNodeV2(simpleMatchPattern.constPattern)) {
            return;
        }
        lookupNodeV2(simpleMatchPattern.varVariableName);
    }

    @Override
    public void visit(BLangNamedArgMatchPattern namedArgMatchPattern) {
        lookupNodeV2(namedArgMatchPattern.matchPattern);
    }

    @Override
    public void visit(BLangMatchGuard matchGuard) {
        lookupNodeV2(matchGuard.expr);
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
        lookupNodeV2(constMatchPattern.expr);
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
        lookupNodeV2(varBindingPattern.getBindingPattern());
    }

    @Override
    public void visit(BLangRestMatchPattern restMatchPattern) {
        // ignore
    }

    @Override
    public void visit(BLangMappingBindingPattern mappingBindingPattern) {
        if (lookupNodesV2(mappingBindingPattern.fieldBindingPatterns)) {
            return;
        }
        lookupNodeV2(mappingBindingPattern.restBindingPattern);
    }

    @Override
    public void visit(BLangFieldBindingPattern fieldBindingPattern) {
        lookupNodeV2(fieldBindingPattern.bindingPattern);
    }

    @Override
    public void visit(BLangRestBindingPattern restBindingPattern) {
        // ignore
    }

    @Override
    public void visit(BLangErrorBindingPattern errorBindingPattern) {
        if (lookupNodeV2(errorBindingPattern.errorMessageBindingPattern)) {
            return;
        }
        if (lookupNodeV2(errorBindingPattern.errorTypeReference)) {
            return;
        }
        if (lookupNodeV2(errorBindingPattern.errorCauseBindingPattern)) {
            return;
        }
        lookupNodeV2(errorBindingPattern.errorFieldBindingPatterns);
    }

    @Override
    public void visit(BLangErrorMessageBindingPattern errorMessageBindingPattern) {
        lookupNodeV2(errorMessageBindingPattern.simpleBindingPattern);
    }

    @Override
    public void visit(BLangErrorCauseBindingPattern errorCauseBindingPattern) {
        if (lookupNodeV2(errorCauseBindingPattern.simpleBindingPattern)) {
            return;
        }
        lookupNodeV2(errorCauseBindingPattern.errorBindingPattern);
    }

    @Override
    public void visit(BLangErrorFieldBindingPatterns errorFieldBindingPatterns) {
        if (lookupNodesV2(errorFieldBindingPatterns.namedArgBindingPatterns)) {
            return;
        }
        lookupNodeV2(errorFieldBindingPatterns.restBindingPattern);
    }

    @Override
    public void visit(BLangSimpleBindingPattern simpleBindingPattern) {
        lookupNodeV2(simpleBindingPattern.captureBindingPattern);
    }

    @Override
    public void visit(BLangNamedArgBindingPattern namedArgBindingPattern) {
        lookupNodeV2(namedArgBindingPattern.bindingPattern);
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
        setEnclosingNode(captureBindingPattern, captureBindingPattern.getIdentifier().getPosition());
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern) {
        if (lookupNodesV2(listBindingPattern.bindingPatterns)) {
            return;
        }
        lookupNodeV2(listBindingPattern.restBindingPattern);
    }

    @Override
    public void visit(BLangDo doNode) {
        if (lookupNodeV2(doNode.body)) {
            return;
        }
        lookupNodeV2(doNode.onFailClause);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        this.enclosingNode = literalExpr;
    }

    @Override
    public void visit(BLangValueType valueType) {
        this.enclosingNode = valueType;
    }

    @Override
    public void visit(BLangConstRef constRef) {
        this.enclosingNode = constRef;
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess) {
        this.enclosingNode = nsPrefixedFieldBasedAccess;
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        lookupNodeV2((BLangNode) onFailClause.variableDefinitionNode);
        lookupNodeV2(onFailClause.body);

        // Adding this as the last stmt to ensure that var define in on fail clause will also be considered.
        this.enclosingContainer = onFailClause;
    }

    private boolean setEnclosingNode(BLangNode node, Location pos) {
        if (PositionUtil.isRangeWithinNode(this.range, pos)
                && (this.enclosingNode == null
                || PositionUtil.isRangeWithinNode(pos.lineRange(), this.enclosingNode.pos))) {
            this.enclosingNode = node;
            return true;
        }
        if (this.enclosingNode == null && PositionUtil.withinRange(node.pos.lineRange(), this.range)) {
            this.enclosingNode = node;
            return true;
        }
        return false;
    }

    private boolean isLambdaFunction(TopLevelNode node) {
        if (node.getKind() != NodeKind.FUNCTION) {
            return false;
        }

        BLangFunction func = (BLangFunction) node;
        return func.flagSet.contains(Flag.LAMBDA);
    }

    private boolean isClassForService(TopLevelNode node) {
        if (node.getKind() != NodeKind.CLASS_DEFN) {
            return false;
        }

        return ((BLangClassDefinition) node).flagSet.contains(Flag.SERVICE);
    }
}
