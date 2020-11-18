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
package org.ballerinalang.langserver.extensions.ballerina.document.visitor;

import org.ballerinalang.langserver.extensions.ballerina.document.ASTModification;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Common node visitor to override and remove assertion errors from BLangNodeVisitor methods.
 */
public class UnusedNodeVisitor extends BaseNodeVisitor {

    private String unitName;
    private Map<Diagnostic.DiagnosticPosition, ASTModification> deleteRanges;
    private Map<Diagnostic.DiagnosticPosition, ASTModification> toBeDeletedRanges = new HashMap<>();
    private Map<String, BLangImportPackage> unusedImports = new HashMap<>();
    private Set<String> usedImports = new HashSet<>();
    private Map<String, Diagnostic.DiagnosticPosition> variables = new HashMap<>();

    public UnusedNodeVisitor(String unitName, Map<Diagnostic.DiagnosticPosition, ASTModification> deleteRanges) {
        this.unitName = unitName;
        this.deleteRanges = deleteRanges;
        this.toBeDeletedRanges.putAll(deleteRanges);
    }

    public Collection<BLangImportPackage> unusedImports() {
        return unusedImports.values();
    }

    public Set<String> usedImports() {
        return usedImports;
    }

    public Collection<ASTModification> toBeDeletedRanges() {
        return toBeDeletedRanges.values();
    }

    private void addImportNode(BLangImportPackage importPkgNode) {
        unusedImports.put(importPkgNode.getAlias().getValue(), importPkgNode);
    }

    private void removeImportNode(BLangIdentifier identifierNode) {
        Diagnostic.DiagnosticPosition range = getDeleteRange(identifierNode.getPosition());
        if (range == null) {
            BLangImportPackage bLangImportPackage = unusedImports.remove(identifierNode.getValue());
            if (bLangImportPackage != null) {
                usedImports.add(bLangImportPackage.getQualifiedPackageName());
            }
        }
    }

    //Monitor only the variables that are in deleted range
    private void addVariableNode(BLangSimpleVariable varNode) {
        Diagnostic.DiagnosticPosition range = getDeleteRange(varNode.getPosition());
        if (range != null) {
            variables.put(varNode.getName().getValue(), range);
        }
    }

    //Remove the delete range when the reference is not from any given delete ranges
    private void removeVariableNode(BLangIdentifier identifierNode) {
        Diagnostic.DiagnosticPosition range = getDeleteRange(identifierNode.getPosition());
        if (range == null) {
            Diagnostic.DiagnosticPosition variableDeleteRange = variables.remove(identifierNode.getValue());
            if (variableDeleteRange != null) {
                toBeDeletedRanges.remove(variableDeleteRange);
            }
        }
    }

    private Diagnostic.DiagnosticPosition getDeleteRange(Diagnostic.DiagnosticPosition position) {
        if (position != null) {
            for (Diagnostic.DiagnosticPosition aPosition : deleteRanges.keySet()) {
                if (aPosition.getStartLine() <= position.getStartLine() &&
                        aPosition.getEndLine() >= position.getEndLine() &&
                        aPosition.getStartColumn() <= position.getStartColumn() &&
                        aPosition.getEndColumn() >= position.getEndColumn()) {
                    return aPosition;
                }
            }
        }
        return null;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        pkgNode.getCompilationUnits().forEach(c -> c.accept(this));
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
        // No implementation
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        if (compUnit.getName().equals(unitName)) {
            compUnit.getTopLevelNodes().forEach(n -> ((BLangNode) n).accept(this));
        }
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        addImportNode(importPkgNode);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        // No implementation
    }

    @Override
    public void visit(BLangFunction funcNode) {
        funcNode.getParameters().forEach(parm -> parm.accept(this));
        funcNode.getBody().accept(this);
    }

    @Override
    public void visit(BLangService serviceNode) {
        serviceNode.getServiceClass().accept(this);
        serviceNode.getResources().forEach(resource -> resource.accept(this));
        serviceNode.getAttachedExprs().forEach(expr -> expr.accept(this));
    }

    @Override
    public void visit(BLangResource resourceNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        // No implementation
    }

    @Override
    public void visit(BLangConstant constant) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        addVariableNode(varNode);
        if (varNode.getTypeNode() != null) {
            varNode.getTypeNode().accept(this);
        }

        if (varNode.getInitialExpression() != null) {
            varNode.getInitialExpression().accept(this);
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        // No implementation
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        // No implementation
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        // No implementation
    }

    // Statements
    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.getStatements().forEach(s -> s.accept(this));
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        blockFuncBody.getStatements().forEach(s -> s.accept(this));
    }

    @Override
    public void visit(BLangExprFunctionBody exprFuncBody) {
        exprFuncBody.getExpr().accept(this);
    }

    @Override
    public void visit(BLangExternalFunctionBody externFuncBody) {
        // No implementation
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        // No implementation
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        // No implementation
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        // No implementation
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        varDefNode.getVariable().accept(this);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        assignNode.getExpression().accept(this);
        ((BLangExpression) assignNode.getVariable()).accept(this);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        // No implementation
    }

    @Override
    public void visit(BLangRetry retryNode) {
        // No implementation
    }

    @Override
    public void visit(BLangContinue continueNode) {
        // No implementation
    }

    @Override
    public void visit(BLangBreak breakNode) {
        // No implementation
    }

    @Override
    public void visit(BLangReturn returnNode) {
        // No implementation
    }

    @Override
    public void visit(BLangThrow throwNode) {
        // No implementation
    }

    @Override
    public void visit(BLangPanic panicNode) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        // No implementation
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.getExpression().accept(this);
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.getCondition().accept(this);
        ifNode.getBody().accept(this);
        if (ifNode.getElseStatement() != null) {
            ifNode.getElseStatement().accept(this);
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        // No implementation
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        // No implementation
    }

    @Override
    public void visit(BLangForeach foreach) {
        foreach.body.accept(this);
        foreach.collection.accept(this);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        whileNode.getBody().accept(this);
        whileNode.getCondition().accept(this);
    }

    @Override
    public void visit(BLangLock lockNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        // No implementation
    }

    @Override
    public void visit(BLangCatch catchNode) {
        // No implementation
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        // No implementation
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        // No implementation
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        // No implementation
    }


    // Expressions

    @Override
    public void visit(BLangLiteral literalExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        removeVariableNode(varRefExpr.getVariableName());
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (invocationExpr.getPackageAlias() != null) {
            removeImportNode((BLangIdentifier) invocationExpr.getPackageAlias());
        }
        ((BLangIdentifier) invocationExpr.getName()).accept(this);
        if (invocationExpr.getExpression() != null) {
            invocationExpr.getExpression().accept(this);
        }
        invocationExpr.getArgumentExpressions().forEach(e -> ((BLangExpression) e).accept(this));
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        // No implementation
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        // No implementation
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        // No implementation
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
        // No implementation
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        // No implementation
    }

    // Type nodes

    @Override
    public void visit(BLangValueType valueType) {
        // No implementation
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        // No implementation
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        // No implementation
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        // No implementation
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        removeImportNode(userDefinedType.getPackageAlias());
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorType errorType) {
        // No implementation
    }


    // expressions that will used only from the Desugar phase

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        removeVariableNode(localVarRef.getVariableName());
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        // No implementation
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        // No implementation
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        bLangStatementExpression.getStatement().accept(this);
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        // No implementation
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        // No implementation
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        // No implementation
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        // No implementation
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        // No implementation
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        // No implementation
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        // No implementation
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        // No implementation
    }

    @Override
    public void visit(
            BLangMatch.BLangMatchStructuredBindingPatternClause bLangMatchStmtStructuredBindingPatternClause) {
        // No implementation
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        // No implementation
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        // No implementation
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        // no implementation
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        // no implementation
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        // no implementation
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        // no implementation
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        // no implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        // no implementation
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        // no implementation
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
        // no implementation
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        // no implementation
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        // no implementation
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        // no implementation
    }

    @Override
    public void visit(BLangDoClause doClause) {
        // no implementation
    }

    @Override
    public void visit(BLangConstRef constRef) {
        // no implementation
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
        // no implementation
    }

    @Override
    public void visit(BLangLetClause letClause) {
        // no implementation
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        // no implementation
    }

    @Override
    public void visit(BLangLetVariable letVariable) {
        // no implementation
    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {
        // no implementation
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        // no implementation
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        // no implementation
    }

    @Override
    public void visit(BLangStreamType streamType) {
        // no implementation
    }
}
