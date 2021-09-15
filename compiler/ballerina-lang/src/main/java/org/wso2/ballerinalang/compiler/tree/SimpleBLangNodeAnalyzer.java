/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.tree;

import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeEntry;
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
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangWildCardBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangInputClause;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDynamicArgExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInferredTypedescDefaultNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
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
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangNamedArgMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangRestMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangSimpleMatchPattern;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.List;


/**
 * The {@link SimpleBLangNodeAnalyzer} transforms each {@link BLangNode} objects to another object of type T.
 * <p>
 * This is simplified node visitor of the {@link BLangNodeAnalyzer}.
 * <p>
 *
 * @param <T> the type of class that passed along with transform methods.
 * @since 2.0.0
 */
public abstract class SimpleBLangNodeAnalyzer<T> extends BLangNodeAnalyzer<T> {

    public abstract void analyzeNode(BLangNode node, T props);

    public abstract void analyzeNodeEntry(BLangNodeEntry nodeEntry, T props);

    public void visitNode(BLangNode node, T props) {
        if (node == null) {
            return;
        }
        node.accept(this, props);
    }

    public void visitNode(List<? extends Node> nodes, T props) {
        if (nodes == null) {
            return;
        }
        for (Node node : nodes) {
            visitNode((BLangNode) node, props);
        }
    }

    public void visitNodeEntry(BLangNodeEntry nodeEntry, T props) {
        if (nodeEntry == null) {
            return;
        }
        nodeEntry.accept(this, props);
    }

    public void visitNodeEntry(List<? extends NodeEntry> nodeEntries, T props) {
        if (nodeEntries == null) {
            return;
        }
        for (NodeEntry nodeEntry : nodeEntries) {
            visitNodeEntry((BLangNodeEntry) nodeEntry, props);
        }
    }

    // Base Nodes

    public void visit(BLangAnnotation node, T props) {
        analyzeNode(node, props);
        visitNode(node.name, props);
        visitNode(node.annAttachments, props);
        visitNode(node.markdownDocumentationAttachment, props);
        visitNode(node.typeNode, props);
    }

    public void visit(BLangAnnotationAttachment node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
        visitNode(node.annotationName, props);
        visitNode(node.pkgAlias, props);
    }

    public void visit(BLangBlockFunctionBody node, T props) {
        analyzeNode(node, props);
        visitNode(node.stmts, props);
    }

    public void visit(BLangClassDefinition node, T props) {
        analyzeNode(node, props);
        visitNode(node.name, props);
        visitNode(node.annAttachments, props);
        visitNode(node.markdownDocumentationAttachment, props);
        visitNode(node.initFunction, props);
        visitNode(node.functions, props);
        visitNode(node.fields, props);
        visitNode(node.typeRefs, props);
    }

    public void visit(BLangCompilationUnit node, T props) {
        analyzeNode(node, props);
        visitNode(node.topLevelNodes, props);
    }

    public void visit(BLangErrorVariable node, T props) {
        analyzeNode(node, props);
        visitBLangVariableNode(node, props);
        visitNode(node.message, props);
        visitNode(node.cause, props);
        visitNode(node.restDetail, props);
        visitNodeEntry(node.detail, props);
    }

    public void visit(BLangErrorVariable.BLangErrorDetailEntry nodeEntry, T props) {
        analyzeNodeEntry(nodeEntry, props);
        visitNode(nodeEntry.key, props);
        visitNode(nodeEntry.valueBindingPattern, props);
    }

    public void visit(BLangExprFunctionBody node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangExternalFunctionBody node, T props) {
        analyzeNode(node, props);
        visitNode(node.annAttachments, props);
    }

    public void visit(BLangFunction node, T props) {
        analyzeNode(node, props);
        visitBLangInvokableNode(node, props);
    }

    public void visit(BLangIdentifier node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangImportPackage node, T props) {
        analyzeNode(node, props);
        visitNode(node.orgName, props);
        visitNode(node.pkgNameComps, props);
        visitNode(node.alias, props);
        visitNode(node.compUnit, props);
        visitNode(node.version, props);
    }

    public void visit(BLangMarkdownDocumentation node, T props) {
        analyzeNode(node, props);
        visitNode(node.documentationLines, props);
        visitNode(node.parameters, props);
        visitNode(node.references, props);
        visitNode(node.returnParameter, props);
        visitNode(node.deprecationDocumentation, props);
        visitNode(node.deprecatedParametersDocumentation, props);
    }

    public void visit(BLangMarkdownReferenceDocumentation node, T props) {
        analyzeNode(node, props);
    }

    public abstract void visit(BLangPackage node, T props);

    public void visit(BLangRecordVariable node, T props) {
        analyzeNode(node, props);
        visitBLangVariableNode(node, props);
        visitNodeEntry(node.variableList, props);
        visitNode(node.restParam, props);
    }

    public void visit(BLangRecordVariable.BLangRecordVariableKeyValue nodeEntry, T props) {
        analyzeNodeEntry(nodeEntry, props);
        visitNode(nodeEntry.key, props);
        visitNode(nodeEntry.valueBindingPattern, props);
    }

    public void visit(BLangResourceFunction node, T props) {
        analyzeNode(node, props);
        visit((BLangFunction) node, props);
        visitNode(node.methodName, props);
        visitNode(node.resourcePath, props);
        visitNode(node.restPathParam, props);
        visitNode(node.pathParams, props);
    }

    public void visit(BLangRetrySpec node, T props) {
        analyzeNode(node, props);
        visitNode(node.retryManagerType, props);
        visitNode(node.argExprs, props);
    }

    public void visit(BLangService node, T props) {
        analyzeNode(node, props);
        visitNode(node.serviceVariable, props);
        visitNode(node.attachedExprs, props);
        visitNode(node.serviceClass, props);
        visitNode(node.absoluteResourcePath, props);
        visitNode(node.serviceNameLiteral, props);
        visitNode(node.name, props);
        visitNode(node.annAttachments, props);
        visitNode(node.markdownDocumentationAttachment, props);
    }

    public void visit(BLangSimpleVariable node, T props) {
        analyzeNode(node, props);
        visitBLangVariableNode(node, props);
        visitNode(node.name, props);
    }

    public void visit(BLangTableKeySpecifier node, T props) {
        analyzeNode(node, props);
        visitNode(node.fieldNameIdentifierList, props);
    }

    public void visit(BLangTableKeyTypeConstraint node, T props) {
        analyzeNode(node, props);
        visitNode(node.keyType, props);
    }

    public void visit(BLangTestablePackage node, T props) {
        analyzeNode(node, props);
        visit((BLangPackage) node, props);
    }

    public void visit(BLangTupleVariable node, T props) {
        analyzeNode(node, props);
        visitBLangVariableNode(node, props);
        visitNode(node.memberVariables, props);
        visitNode(node.restVariable, props);
    }

    public void visit(BLangTypeDefinition node, T props) {
        analyzeNode(node, props);
        visitNode(node.name, props);
        visitNode(node.typeNode, props);
        visitNode(node.annAttachments, props);
        visitNode(node.markdownDocumentationAttachment, props);
    }

    public void visit(BLangXMLNS node, T props) {
        analyzeNode(node, props);
        visitNode(node.namespaceURI, props);
        visitNode(node.prefix, props);
    }

    public void visit(BLangXMLNS.BLangLocalXMLNS node, T props) {
        analyzeNode(node, props);
        visit((BLangXMLNS) node, props);
    }

    public void visit(BLangXMLNS.BLangPackageXMLNS node, T props) {
        analyzeNode(node, props);
        visit((BLangXMLNS) node, props);
    }

    // Binding-patterns

    public void visit(BLangCaptureBindingPattern node, T props) {
        analyzeNode(node, props);
        visitNode(node.identifier, props);
    }

    public void visit(BLangErrorBindingPattern node, T props) {
        analyzeNode(node, props);
        visitNode(node.errorTypeReference, props);
        visitNode(node.errorMessageBindingPattern, props);
        visitNode(node.errorCauseBindingPattern, props);
        visitNode(node.errorFieldBindingPatterns, props);
    }

    public void visit(BLangErrorCauseBindingPattern node, T props) {
        analyzeNode(node, props);
        visitNode(node.simpleBindingPattern, props);
        visitNode(node.errorBindingPattern, props);
    }

    public void visit(BLangErrorFieldBindingPatterns node, T props) {
        analyzeNode(node, props);
        visitNode(node.namedArgBindingPatterns, props);
        visitNode(node.restBindingPattern, props);
    }

    public void visit(BLangErrorMessageBindingPattern node, T props) {
        analyzeNode(node, props);
        visitNode(node.simpleBindingPattern, props);
    }

    public void visit(BLangFieldBindingPattern node, T props) {
        analyzeNode(node, props);
        visitNode(node.fieldName, props);
        visitNode(node.bindingPattern, props);
    }

    public void visit(BLangListBindingPattern node, T props) {
        analyzeNode(node, props);
        visitNode(node.bindingPatterns, props);
        visitNode(node.restBindingPattern, props);
    }

    public void visit(BLangMappingBindingPattern node, T props) {
        analyzeNode(node, props);
        visitNode(node.fieldBindingPatterns, props);
        visitNode(node.restBindingPattern, props);
    }

    public void visit(BLangNamedArgBindingPattern node, T props) {
        analyzeNode(node, props);
        visitNode(node.argName, props);
        visitNode(node.bindingPattern, props);
    }

    public void visit(BLangRestBindingPattern node, T props) {
        analyzeNode(node, props);
        visitNode(node.variableName, props);
    }

    public void visit(BLangSimpleBindingPattern node, T props) {
        analyzeNode(node, props);
        visitNode(node.captureBindingPattern, props);
        visitNode(node.wildCardBindingPattern, props);
    }

    public void visit(BLangWildCardBindingPattern node, T props) {
        analyzeNode(node, props);
    }

    // Clauses

    public void visit(BLangDoClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.body, props);
    }

    public void visit(BLangFromClause node, T props) {
        analyzeNode(node, props);
        visitBLangInputClause(node, props);
    }

    public void visit(BLangJoinClause node, T props) {
        analyzeNode(node, props);
        visitBLangInputClause(node, props);
        visitNode(node.onClause, props);
    }

    public void visit(BLangLetClause node, T props) {
        analyzeNode(node, props);
        visitNodeEntry(node.letVarDeclarations, props);
    }

    public void visit(BLangLimitClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.expression, props);
    }

    public void visit(BLangMatchClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.matchPatterns, props);
        visitNode(node.expr, props);
        visitNode(node.blockStmt, props);
        visitNode(node.matchGuard, props);
    }

    public void visit(BLangOnClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.lhsExpr, props);
        visitNode(node.rhsExpr, props);
    }

    public void visit(BLangOnConflictClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.expression, props);
    }

    public void visit(BLangOnFailClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.body, props);
        visitNode((BLangNode) node.variableDefinitionNode, props);
    }

    public void visit(BLangOrderByClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.orderByKeyList, props);
    }

    public void visit(BLangOrderKey node, T props) {
        analyzeNode(node, props);
        visitNode(node.expression, props);
    }

    public void visit(BLangSelectClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.expression, props);
    }

    public void visit(BLangWhereClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.expression, props);
    }

    // Expressions

    public void visit(BLangAnnotAccessExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangArrowFunction node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangBinaryExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangCheckedExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangCheckPanickedExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangCommitExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangConstant node, T props) {
        analyzeNode(node, props);
        visitBLangVariableNode(node, props);
    }

    public void visit(BLangConstRef node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangDynamicArgExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangElvisExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangErrorConstructorExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangErrorVarRef node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangFieldBasedAccess node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangGroupExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangIgnoreExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangIndexBasedAccess node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangInferredTypedescDefaultNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangIntRangeExpression node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangInvocation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangInvocation.BFunctionPointerInvocation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangInvocation.BLangAttachedFunctionInvocation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangInvocation.BLangActionInvocation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangIsAssignableExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangIsLikeExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangLambdaFunction node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangLetExpression node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangListConstructorExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangLiteral node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMarkDownDeprecatedParametersDocumentation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMarkDownDeprecationDocumentation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMarkdownDocumentationLine node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMarkdownParameterDocumentation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMarkdownReturnParameterDocumentation node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMatchExpression node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMatchExpression.BLangMatchExprPatternClause node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMatchGuard node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangNamedArgsExpression node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangObjectConstructorExpression node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangQueryAction node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangQueryExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRawTemplateLiteral node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRecordLiteral node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRecordLiteral.BLangRecordKeyValueField node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRecordLiteral.BLangRecordKey node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRecordLiteral.BLangStructLiteral node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRecordLiteral.BLangMapLiteral node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRecordVarRef node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangRestArgsExpression node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangServiceConstructorExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangSimpleVarRef node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangStatementExpression node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangStringTemplateLiteral node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTableConstructorExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTableMultiKeyExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTernaryExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTransactionalExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTrapExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTupleVarRef node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTypeConversionExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTypedescExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTypeInit node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangTypeTestExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangUnaryExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangWaitExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangWaitForAllExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangWorkerFlushExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangWorkerReceive node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangWorkerSyncSendExpr node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLAttribute node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLAttributeAccess node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLCommentLiteral node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLElementAccess node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLElementFilter node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLElementLiteral node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLNavigationAccess node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLProcInsLiteral node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLQName node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLQuotedString node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLSequenceLiteral node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangXMLTextLiteral node, T props) {
        analyzeNode(node, props);
    }

    // Match patterns

    public void visit(BLangConstPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangErrorCauseMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.simpleMatchPattern, props);
        visitNode(node.errorMatchPattern, props);
    }

    public void visit(BLangErrorFieldMatchPatterns node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.namedArgMatchPatterns, props);
        visitNode(node.restMatchPattern, props);
    }

    public void visit(BLangErrorMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.errorMessageMatchPattern, props);
        visitNode(node.errorCauseMatchPattern, props);
        visitNode(node.errorFieldMatchPatterns, props);
        visitNode(node.errorTypeReference, props);
    }

    public void visit(BLangErrorMessageMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.simpleMatchPattern, props);
    }

    public void visit(BLangFieldMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.fieldName, props);
        visitNode(node.matchPattern, props);
        visitNode(node.matchPattern, props);
    }

    public void visit(BLangListMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.matchPatterns, props);
        visitNode(node.restMatchPattern, props);
    }

    public void visit(BLangMappingMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.fieldMatchPatterns, props);
        visitNode(node.restMatchPattern, props);
    }

    public void visit(BLangNamedArgMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.argName, props);
        visitNode(node.matchPattern, props);
    }

    public void visit(BLangRestMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.variableName, props);
    }

    public void visit(BLangSimpleMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.constPattern, props);
        visitNode(node.wildCardMatchPattern, props);
        visitNode(node.varVariableName, props);
    }

    public void visit(BLangVarBindingPatternMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
        visitNode(node.bindingPattern, props);
    }

    public void visit(BLangWildCardMatchPattern node, T props) {
        analyzeNode(node, props);
        visitBLangMatchPattern(node, props);
    }

    // Statements

    public void visit(BLangAssignment node, T props) {
        analyzeNode(node, props);
        visitNode(node.varRef, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangBlockStmt node, T props) {
        analyzeNode(node, props);
        visitNode(node.stmts, props);
    }

    public void visit(BLangBreak node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangCompoundAssignment node, T props) {
        analyzeNode(node, props);
        visitNode(node.varRef, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangContinue node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangDo node, T props) {
        analyzeNode(node, props);
        visitNode(node.body, props);
        visitNode(node.onFailClause, props);
    }

    public void visit(BLangErrorDestructure node, T props) {
        analyzeNode(node, props);
        visitNode(node.varRef, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangErrorVariableDef node, T props) {
        analyzeNode(node, props);
        visitNode(node.errorVariable, props);
    }

    public void visit(BLangExpressionStmt node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangFail node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangForeach node, T props) {
        analyzeNode(node, props);
        visitNode((BLangNode) node.variableDefinitionNode, props);
        visitNode(node.body, props);
        visitNode(node.collection, props);
        visitNode(node.onFailClause, props);
    }

    public void visit(BLangForkJoin node, T props) {
        analyzeNode(node, props);
        visitNode(node.workers, props);
    }

    public void visit(BLangIf node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
        visitNode(node.body, props);
        visitNode(node.elseStmt, props);
    }

    public void visit(BLangLock node, T props) {
        analyzeNode(node, props);
        visitNode(node.body, props);
        visitNode(node.onFailClause, props);
    }

    public void visit(BLangLock.BLangLockStmt node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangLock.BLangUnLockStmt node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangMatch node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
        visitNode(node.patternClauses, props);
        visitNode(node.onFailClause, props);
    }

    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.body, props);
        visitNode(node.matchExpr, props);
        visitNode(node.variable, props);
    }

    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.body, props);
        visitNode(node.matchExpr, props);
        visitNode(node.literal, props);
    }

    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause node, T props) {
        analyzeNode(node, props);
        visitNode(node.body, props);
        visitNode(node.matchExpr, props);
        visitNode(node.bindingPatternVariable, props);
        visitNode(node.typeGuardExpr, props);
    }

    public void visit(BLangMatchStatement node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
        visitNode(node.matchClauses, props);
        visitNode(node.onFailClause, props);
    }

    public void visit(BLangPanic node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangRecordDestructure node, T props) {
        analyzeNode(node, props);
        visitNode(node.varRef, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangRecordVariableDef node, T props) {
        analyzeNode(node, props);
        visitNode(node.var, props);
    }

    public void visit(BLangRetry node, T props) {
        analyzeNode(node, props);
        visitNode(node.retrySpec, props);
        visitNode(node.retryBody, props);
        visitNode(node.onFailClause, props);
    }

    public void visit(BLangRetryTransaction node, T props) {
        analyzeNode(node, props);
        visitNode(node.retrySpec, props);
        visitNode(node.transaction, props);
    }

    public void visit(BLangReturn node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangRollback node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangSimpleVariableDef node, T props) {
        analyzeNode(node, props);
        visitNode(node.var, props);
    }

    public void visit(BLangTransaction node, T props) {
        analyzeNode(node, props);
        visitNode(node.transactionBody, props);
        visitNode(node.onFailClause, props);
    }

    public void visit(BLangTupleDestructure node, T props) {
        analyzeNode(node, props);
        visitNode(node.varRef, props);
        visitNode(node.expr, props);
    }

    public void visit(BLangTupleVariableDef node, T props) {
        analyzeNode(node, props);
        visitNode(node.var, props);
    }

    public void visit(BLangWhile node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
        visitNode(node.body, props);
        visitNode(node.onFailClause, props);
    }

    public void visit(BLangWorkerSend node, T props) {
        analyzeNode(node, props);
        visitNode(node.expr, props);
        visitNode(node.workerIdentifier, props);
    }

    public void visit(BLangXMLNSStatement node, T props) {
        analyzeNode(node, props);
        visitNode(node.xmlnsDecl, props);
    }

    // Types

    public void visit(BLangArrayType node, T props) {
        analyzeNode(node, props);
        visitNode(node.elemtype, props);
        visitNode(node.sizes, props);
    }

    public void visit(BLangBuiltInRefTypeNode node, T props) {
        analyzeNode(node, props);
    }

    public void visit(BLangConstrainedType node, T props) {
        analyzeNode(node, props);
        visitNode(node.type, props);
        visitNode(node.constraint, props);
    }

    public void visit(BLangErrorType node, T props) {
        analyzeNode(node, props);
        visitNode(node.detailType, props);
    }

    public void visit(BLangFiniteTypeNode node, T props) {
        analyzeNode(node, props);
        visitNode(node.valueSpace, props);
    }

    public void visit(BLangFunctionTypeNode node, T props) {
        analyzeNode(node, props);
        visitNode(node.params, props);
        visitNode(node.restParam, props);
        visitNode(node.returnTypeNode, props);
    }

    public void visit(BLangIntersectionTypeNode node, T props) {
        analyzeNode(node, props);
        visitNode(node.constituentTypeNodes, props);
    }

    public void visit(BLangLetVariable nodeEntry, T props) {
        analyzeNodeEntry(nodeEntry, props);
        visitNode((BLangNode) nodeEntry.definitionNode, props);
    }

    public void visit(BLangObjectTypeNode node, T props) {
        analyzeNode(node, props);
        visitBLangStructureTypeNode(node, props);
        visitNode(node.functions, props);
    }

    public void visit(BLangRecordTypeNode node, T props) {
        analyzeNode(node, props);
        visitBLangStructureTypeNode(node, props);
        visitNode(node.restFieldType, props);
    }

    public void visit(BLangStreamType node, T props) {
        analyzeNode(node, props);
        visitNode(node.type, props);
        visitNode(node.constraint, props);
        visitNode(node.error, props);
    }

    public void visit(BLangTableTypeNode node, T props) {
        analyzeNode(node, props);
        visitNode(node.type, props);
        visitNode(node.constraint, props);
        visitNode(node.tableKeySpecifier, props);
        visitNode(node.tableKeyTypeConstraint, props);
    }

    public void visit(BLangTupleTypeNode node, T props) {
        analyzeNode(node, props);
        visitNode(node.memberTypeNodes, props);
        visitNode(node.restParamType, props);
    }

    public void visit(BLangUnionTypeNode node, T props) {
        analyzeNode(node, props);
        visitNode(node.memberTypeNodes, props);
    }

    public void visit(BLangUserDefinedType node, T props) {
        analyzeNode(node, props);
        visitNode(node.pkgAlias, props);
        visitNode(node.typeName, props);
    }

    public void visit(BLangValueType node, T props) {
        analyzeNode(node, props);
    }

    // Private methods

    private void visitBLangVariableNode(BLangVariable node, T props) {
        visitNode(node.typeNode, props);
        visitNode(node.annAttachments, props);
        visitNode(node.markdownDocumentationAttachment, props);
        visitNode(node.expr, props);
    }

    private void visitBLangInvokableNode(BLangInvokableNode node, T props) {
        visitNode(node.name, props);
        visitNode(node.annAttachments, props);
        visitNode(node.markdownDocumentationAttachment, props);
        visitNode(node.requiredParams, props);
        visitNode(node.restParam, props);
        visitNode(node.returnTypeNode, props);
        visitNode(node.returnTypeAnnAttachments, props);
        visitNode(node.body, props);
        visitNode(node.defaultWorkerName, props);
    }

    private void visitBLangInputClause(BLangInputClause node, T props) {
        visitNode(node.collection, props);
        visitNode((BLangNode) node.variableDefinitionNode, props);
    }

    private void visitBLangMatchPattern(BLangMatchPattern node, T props) {
        visitNode(node.matchExpr, props);
    }

    private void visitBLangStructureTypeNode(BLangStructureTypeNode node, T props) {
        visitNode(node.fields, props);
        visitNode(node.typeRefs, props);
        visitNode(node.initFunction, props);
        visitNode(node.initFunction, props);
    }
}
