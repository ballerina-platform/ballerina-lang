/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.clauses.OnClauseNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableMultiKeyExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTransactionalExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementFilter;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangWildCardMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDo;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

/**
 * A node visitor to desugar constant after semantic analyze phase.
 *
 * @since 1.2.0
 */
public class ConstantPropagation extends BLangNodeVisitor {

    private static final CompilerContext.Key<ConstantPropagation> CONSTANT_PROPAGATION_KEY =
            new CompilerContext.Key<>();

    private BLangNode result;
    private Types types;

    public static ConstantPropagation getInstance(CompilerContext context) {
        ConstantPropagation constantPropagation = context.get(CONSTANT_PROPAGATION_KEY);
        if (constantPropagation == null) {
            constantPropagation = new ConstantPropagation(context);
        }

        return constantPropagation;
    }

    private ConstantPropagation(CompilerContext context) {
        context.put(CONSTANT_PROPAGATION_KEY, this);
        this.types = Types.getInstance(context);
    }

    public BLangPackage perform(BLangPackage pkgNode) {
        return rewrite(pkgNode);
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.CONSTANT_PROPAGATION)) {
            return;
        }

        for (TopLevelNode topLevelNode : pkgNode.topLevelNodes) {
            rewrite((BLangNode) topLevelNode);
        }

        pkgNode.completedPhases.add(CompilerPhase.CONSTANT_PROPAGATION);
        result = pkgNode;
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE
                || typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE
                || typeDefinition.typeNode.getKind() == NodeKind.ERROR_TYPE
                || typeDefinition.typeNode.getKind() == NodeKind.FINITE_TYPE_NODE) {
            typeDefinition.typeNode = rewrite(typeDefinition.typeNode);
        }

        rewrite(typeDefinition.annAttachments);
        result = typeDefinition;
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        rewrite(classDefinition.functions);
        rewrite(classDefinition.fields);
        classDefinition.initFunction = rewrite(classDefinition.initFunction);
        classDefinition.receiver = rewrite(classDefinition.receiver);
        rewrite(classDefinition.annAttachments);
        result = classDefinition;
    }

    @Override
    public void visit(BLangSimpleVariableDef varNode) {
        varNode.var = rewrite(varNode.var);
        result = varNode;
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        rewrite(varNode.annAttachments);
        varNode.expr = rewrite(varNode.expr);
        result = varNode;
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        rewrite(recordTypeNode.fields);
        result = recordTypeNode;
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        annAttachmentNode.expr = rewrite(annAttachmentNode.expr);
        result = annAttachmentNode;
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        rewrite(annotationNode.annAttachments);
        result = annotationNode;
    }

    @Override
    public void visit(BLangFunction funcNode) {
        rewrite(funcNode.requiredParams);
        rewrite(funcNode.workers);
        funcNode.body = rewrite(funcNode.body);
        rewrite(funcNode.annAttachments);

        if (funcNode.returnTypeNode != null) {
            rewrite(funcNode.returnTypeAnnAttachments);
        }

        result = funcNode;
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        result = literalExpr;
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        rewrite(blockNode.stmts);
        result = blockNode;
    }

    @Override
    public void visit(BLangReturn langReturn) {
        langReturn.expr = rewrite(langReturn.expr);
        result = langReturn;
    }

    @Override
    public void visit(BLangConstant constant) {
        constant.expr = rewrite(constant.expr);
        result = constant;
    }

    @Override
    public void visit(BLangErrorType errorType) {
        result = errorType;
    }

    @Override
    public void visit(BLangInvocation bLangInvocationNode) {
        bLangInvocationNode.expr = rewrite(bLangInvocationNode.expr);
        rewrite(bLangInvocationNode.requiredArgs);
        rewrite(bLangInvocationNode.restArgs);
        result = bLangInvocationNode;
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInv) {
        actionInv.expr = rewrite(actionInv.expr);
        rewrite(actionInv.requiredArgs);
        rewrite(actionInv.restArgs);
        result = actionInv;
    }

    @Override
    public void visit(BLangNamedArgsExpression namedArgsExpression) {
        namedArgsExpression.expr = rewrite(namedArgsExpression.expr);
        result = namedArgsExpression;
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.expr = rewrite(ifNode.expr);
        ifNode.body = rewrite(ifNode.body);
        ifNode.elseStmt = rewrite(ifNode.elseStmt);
        result = ifNode;
    }

    @Override
    public void visit(BLangGroupExpr bLangGroupExpr) {
        bLangGroupExpr.expression = rewrite(bLangGroupExpr.expression);
        result = bLangGroupExpr;
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.expr = rewrite(typeTestExpr.expr);
        result = typeTestExpr;
    }

    @Override
    public void visit(BLangPanic panic) {
        panic.expr = rewrite(panic.expr);
        result = panic;
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        checkedExpr.expr = rewrite(checkedExpr.expr);
        result = checkedExpr;
    }

    @Override
    public void visit(BLangFail failNode) {
        failNode.expr = rewrite(failNode.expr);
        result = failNode;
    }

    @Override
    public void visit(BLangTypeInit bLangTypeInit) {
        rewrite(bLangTypeInit.argsExpr);
        bLangTypeInit.initInvocation = rewrite(bLangTypeInit.initInvocation);
        result = bLangTypeInit;
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        rewrite(objectTypeNode.functions);
        rewrite(objectTypeNode.fields);
        result = objectTypeNode;
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        assignNode.varRef = rewrite(assignNode.varRef);
        assignNode.expr = rewrite(assignNode.expr);
        result = assignNode;
    }

    @Override
    public void visit(BLangRecordLiteral bLangRecordLiteral) {
        List<RecordLiteralNode.RecordField> fields =  bLangRecordLiteral.fields;
        List<RecordLiteralNode.RecordField> updatedFields = new ArrayList<>(fields.size());

        for (RecordLiteralNode.RecordField field : fields) {
            BLangRecordLiteral.RecordField updatedField;
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValueField =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                keyValueField.valueExpr = rewrite(keyValueField.valueExpr);
                updatedField = keyValueField;
            } else {
               // TODO: Constants on mapping constructors are not desugared #issue-21127
               updatedField = field;
            }
            updatedFields.add(updatedField);
        }

        bLangRecordLiteral.fields = updatedFields;
        result = bLangRecordLiteral;
    }

    @Override
    public void visit(BLangExpressionStmt stmt) {
        stmt.expr = rewrite(stmt.expr);
        result = stmt;
    }

    @Override
    public void visit(BLangLock lock) {
        lock.body = rewrite(lock.body);
        lock.onFailClause = rewrite(lock.onFailClause);
        result = lock;
    }

    @Override
    public void visit(BLangWhile whileNode) {
        whileNode.expr = rewrite(whileNode.expr);
        whileNode.body = rewrite(whileNode.body);
        whileNode.onFailClause = rewrite(whileNode.onFailClause);
        result = whileNode;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.rhsExpr = rewrite(binaryExpr.rhsExpr);
        binaryExpr.lhsExpr = rewrite(binaryExpr.lhsExpr);
        result = binaryExpr;
    }

    @Override
    public void visit(BLangService serviceNode) {
        rewrite(serviceNode.annAttachments);

        if (serviceNode.isAnonymousServiceValue) {
            result = serviceNode;
            return;
        }

        rewrite(serviceNode.attachedExprs);
        rewrite(serviceNode.resourceFunctions);
        result = serviceNode;
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        result = lockStmtNode;
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        compoundAssignNode.varRef = rewrite(compoundAssignNode.varRef);
        compoundAssignNode.modifiedExpr = rewrite(compoundAssignNode.modifiedExpr);
        compoundAssignNode.expr = rewrite(compoundAssignNode.expr);
        result = compoundAssignNode;
    }

    @Override
    public void visit(BLangMatchStatement matchStatement) {
        matchStatement.expr = rewrite(matchStatement.expr);
        rewrite(matchStatement.matchClauses);
        matchStatement.onFailClause = rewrite(matchStatement.onFailClause);
        result = matchStatement;
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        rewrite(matchClause.matchPatterns);
        matchClause.blockStmt = rewrite(matchClause.blockStmt);
        result = matchClause;
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
        constMatchPattern.expr = rewrite(constMatchPattern.expr);
        result = constMatchPattern;
    }

    @Override
    public void visit(BLangWildCardMatchPattern wildCardMatchPattern) {
        result = wildCardMatchPattern;
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
        result = varBindingPattern;
    }

    @Override
    public void visit(BLangMatch matchNode) {
        matchNode.expr = rewrite(matchNode.expr);
        rewrite(matchNode.patternClauses);
        matchNode.onFailClause = rewrite(matchNode.onFailClause);
        result = matchNode;
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        patternClauseNode.variable = rewrite(patternClauseNode.variable);
        patternClauseNode.body = rewrite(patternClauseNode.body);
        patternClauseNode.matchExpr = rewrite(patternClauseNode.matchExpr);
        result = patternClauseNode;
    }

    @Override
    public void visit(BLangForeach foreach) {
        foreach.collection = rewrite(foreach.collection);
        foreach.body = rewrite(foreach.body);
        foreach.onFailClause = rewrite(foreach.onFailClause);
        result = foreach;
    }

    @Override
    public void visit(BLangDo doNode) {
        doNode.body = rewrite(doNode.body);
        doNode.onFailClause = rewrite(doNode.onFailClause);
        result = doNode;
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody = rewrite(transactionNode.transactionBody);
        transactionNode.onFailClause = rewrite(transactionNode.onFailClause);
        result = transactionNode;
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        rollbackNode.expr = rewrite(rollbackNode.expr);
        result = rollbackNode;
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        stmt.varRef = rewrite(stmt.varRef);
        stmt.expr = rewrite(stmt.expr);
        result = stmt;
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        stmt.varRef = rewrite(stmt.varRef);
        stmt.expr = rewrite(stmt.expr);
        result = stmt;
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        stmt.varRef = rewrite(stmt.varRef);
        stmt.expr = rewrite(stmt.expr);
        result = stmt;
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.keyExpr = rewrite(workerSendNode.keyExpr);
        workerSendNode.expr = rewrite(workerSendNode.expr);
        result = workerSendNode;
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        result = workerReceiveNode;
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        result = literalExpr;
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        rewrite(varRefExpr.expressions);
        result = varRefExpr;
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        fieldAccessExpr.expr = rewrite(fieldAccessExpr.expr);
        result = fieldAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.expr = rewrite(indexAccessExpr.expr);
        indexAccessExpr.indexExpr = rewrite(indexAccessExpr.indexExpr);
        result = indexAccessExpr;
    }

    @Override
    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
        rewrite(tableMultiKeyExpr.multiKeyIndexExprs);
        result = tableMultiKeyExpr;
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.expr = rewrite(ternaryExpr.expr);
        ternaryExpr.thenExpr = rewrite(ternaryExpr.thenExpr);
        ternaryExpr.elseExpr = rewrite(ternaryExpr.elseExpr);
        result = ternaryExpr;
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        rewrite(awaitExpr.exprList);
        result = awaitExpr;
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.expr = rewrite(trapExpr.expr);
        result = trapExpr;
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        elvisExpr.lhsExpr = rewrite(elvisExpr.lhsExpr);
        elvisExpr.rhsExpr = rewrite(elvisExpr.rhsExpr);
        result = elvisExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        rewrite(listConstructorExpr.exprs);
        result = listConstructorExpr;
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        rewrite(tableConstructorExpr.recordLiteralList);
        result = tableConstructorExpr;
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr = rewrite(unaryExpr.expr);
        result = unaryExpr;
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        rewrite(conversionExpr.annAttachments);
        conversionExpr.expr = rewrite(conversionExpr.expr);
        result = conversionExpr;
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        rewrite(stringTemplateLiteral.exprs);
        result = stringTemplateLiteral;
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        rewrite(rawTemplateLiteral.strings);
        rewrite(rawTemplateLiteral.insertions);
        result = rawTemplateLiteral;
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        bLangLambdaFunction.function = rewrite(bLangLambdaFunction.function);
        result = bLangLambdaFunction;
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        bLangArrowFunction.body = rewrite(bLangArrowFunction.body);
        bLangArrowFunction.function = rewrite(bLangArrowFunction.function);
        result = bLangArrowFunction;
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        intRangeExpression.startExpr = rewrite(intRangeExpression.startExpr);
        intRangeExpression.endExpr = rewrite(intRangeExpression.endExpr);
        result = intRangeExpression;
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        bLangVarArgsExpression.expr = rewrite(bLangVarArgsExpression.expr);
        result = bLangVarArgsExpression;
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        serviceConstructorExpr.serviceNode = rewrite(serviceConstructorExpr.serviceNode);
        result = serviceConstructorExpr;
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        typeTestExpr.expr = rewrite(typeTestExpr.expr);
        result = typeTestExpr;
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        bLangStatementExpression.expr = rewrite(bLangStatementExpression.expr);
        result = bLangStatementExpression;
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        checkPanickedExpr.expr = rewrite(checkPanickedExpr.expr);
        result = checkPanickedExpr;
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        result = finiteTypeNode;
    }

    @Override
    public void visit(BLangBreak breakNode) {
        result = breakNode;
    }

    @Override
    public void visit(BLangContinue continueNode) {
        result = continueNode;
    }

    @Override
    public void visit(BLangTupleVariableDef tupleVariableDef) {
        tupleVariableDef.var = rewrite(tupleVariableDef.var);
        result = tupleVariableDef;
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        bLangTupleVariable.expr = rewrite(bLangTupleVariable.expr);
        result = bLangTupleVariable;
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        bLangMatchStmtStaticBindingPatternClause.literal =
                rewrite(bLangMatchStmtStaticBindingPatternClause.literal);
        bLangMatchStmtStaticBindingPatternClause.body =
                rewrite(bLangMatchStmtStaticBindingPatternClause.body);
        result = bLangMatchStmtStaticBindingPatternClause;
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause
                                  bLangMatchStmtStructuredBindingPatternClause) {
        result = bLangMatchStmtStructuredBindingPatternClause;
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        result = accessExpr;
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        annotAccessExpr.expr = rewrite(annotAccessExpr.expr);
        result = annotAccessExpr;
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        result = xmlElementLiteral;
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        rewrite(xmlTextLiteral.textFragments);
        result = xmlTextLiteral;
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        result = waitForAllExpr;
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
        result = xmlElementFilter;
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        result = xmlElementAccess;
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        xmlNavigation.childIndex = rewrite(xmlNavigation.childIndex);
        result = xmlNavigation;
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        result = xmlnsNode;
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        result = xmlnsStmtNode;
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        xmlAttributeAccessExpr.expr = rewrite(xmlAttributeAccessExpr.expr);
        result = xmlAttributeAccessExpr;
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        result = xmlAttribute;
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        rewrite(xmlCommentLiteral.textFragments);
        result = xmlCommentLiteral;
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        result = workerFlushExpr;
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
        result = transactionalExpr;
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
        result = commitExpr;
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        result = bLangRecordVariableDef;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        result = forkJoin;
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        result = syncSendExpr;
    }

    @Override
    public void visit(BLangRetry retryNode) {
        retryNode.retryBody = rewrite(retryNode.retryBody);
        retryNode.onFailClause = rewrite(retryNode.onFailClause);
        result = retryNode;
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        result = retryTransaction;
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        result = xmlProcInsLiteral;
    }

    @Override
    public void visit(BLangValueType valueType) {
        result = valueType;
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        result = bLangErrorVariableDef;
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        List<BLangNode> clauses = queryExpr.getQueryClauses();
        for (int i = 0; i < clauses.size(); i++) {
            BLangNode clause = clauses.get(i);
            clauses.set(i, rewrite(clause));
        }
        result = queryExpr;
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        fromClause.collection = rewrite(fromClause.collection);
        result = fromClause;
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        joinClause.collection = rewrite(joinClause.collection);
        if (joinClause.onClause != null) {
            joinClause.onClause = (OnClauseNode) rewrite((BLangNode) joinClause.onClause);
        }
        result = joinClause;
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            letVariable.definitionNode = (VariableDefinitionNode) rewrite((BLangNode) letVariable.definitionNode);
        }
        result = letClause;
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        selectClause.expression = rewrite(selectClause.expression);
        result = selectClause;
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        whereClause.expression = rewrite(whereClause.expression);
        result = whereClause;
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        onConflictClause.expression = rewrite(onConflictClause.expression);
        result = onConflictClause;
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        limitClause.expression = rewrite(limitClause.expression);
        result = limitClause;
    }

    @Override
    public void visit(BLangOnClause onClause) {
        onClause.lhsExpr = rewrite(onClause.lhsExpr);
        onClause.rhsExpr = rewrite(onClause.rhsExpr);
        result = onClause;
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
        orderKeyClause.expression = rewrite(orderKeyClause.expression);
        result = orderKeyClause;
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        orderByClause.orderByKeyList.forEach(value -> rewrite((BLangNode) value));
        result = orderByClause;
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        List<BLangNode> clauses = queryAction.getQueryClauses();
        for (int i = 0; i < clauses.size(); i++) {
            BLangNode clause = clauses.get(i);
            clauses.set(i, rewrite(clause));
        }
        result = queryAction;
    }

    @Override
    public void visit(BLangDoClause doClause) {
        doClause.body = rewrite(doClause.body);
        result = doClause;
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        onFailClause.body = rewrite(onFailClause.body);
        result = onFailClause;
    }

    @Override
    public void visit(BLangExternalFunctionBody externFuncBody) {
        rewrite(externFuncBody.annAttachments);
        result = externFuncBody;
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        rewrite(blockFuncBody.stmts);
        result = blockFuncBody;
    }

    @Override
    public void visit(BLangExprFunctionBody exprFuncBody) {
        exprFuncBody.expr = rewrite(exprFuncBody.expr);
        result = exprFuncBody;
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        letExpr.expr = rewrite(letExpr.expr);
        for (BLangLetVariable var : letExpr.letVarDeclarations) {
            var.definitionNode = (VariableDefinitionNode) rewrite((BLangNode) var.definitionNode);
        }
        result = letExpr;
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {

        if (varRefExpr.symbol == null) {
            result = varRefExpr;
            return;
        }

        BSymbol ownerSymbol = varRefExpr.symbol.owner;

        if (((ownerSymbol.tag & SymTag.PACKAGE) == SymTag.PACKAGE ||
                (ownerSymbol.tag & SymTag.SERVICE) == SymTag.SERVICE) &&
                (varRefExpr.symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT) {
            BConstantSymbol constSymbol = (BConstantSymbol) varRefExpr.symbol;

            // If the var ref is a const-ref of value type, then replace the ref
            // from a simple literal
            if (constSymbol.literalType.tag <= TypeTags.BOOLEAN || constSymbol.literalType.tag == TypeTags.NIL) {
                BLangConstRef constRef = ASTBuilderUtil.createBLangConstRef(varRefExpr.pos, constSymbol.literalType,
                        constSymbol.value.value);
                constRef.variableName = varRefExpr.variableName;
                constRef.symbol = constSymbol;
                constRef.pkgAlias = varRefExpr.pkgAlias;
                if (varRefExpr.impConversionExpr != null) {
                    BLangTypeConversionExpr implConversionExpr = (BLangTypeConversionExpr)
                            TreeBuilder.createTypeConversionNode();
                    implConversionExpr.expr = constRef;
                    implConversionExpr.pos = varRefExpr.impConversionExpr.pos;
                    implConversionExpr.type = varRefExpr.impConversionExpr.type;
                    implConversionExpr.targetType = varRefExpr.impConversionExpr.targetType;
                    constRef.impConversionExpr = implConversionExpr;
                } else {
                    types.setImplicitCastExpr(constRef, constRef.type, varRefExpr.type);
                }
                result = constRef;
                return;
            }
        }
        result = varRefExpr;
    }

    @SuppressWarnings("unchecked")
    <E extends BLangNode> E rewrite(E node) {
        if (node == null) {
            return null;
        }

        if (node.constantPropagated) {
            return node;
        }

        node.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        resultNode.constantPropagated = true;

        return (E) resultNode;
    }

    private <E extends BLangNode> void rewrite(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i)));
        }
    }

}
