/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.parser;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable.BLangErrorDetailEntry;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable.BLangRecordVariableKeyValue;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordSpreadOperatorField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordVarNameField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableMultiKeyExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr.BLangWaitKeyValue;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStaticBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStructuredBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchTypedBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Node Visitor for cloning AST nodes.
 *
 * @since 1.1
 */
public class NodeCloner extends BLangNodeVisitor {

    private static final CompilerContext.Key<NodeCloner> NODE_CLONER_KEY = new CompilerContext.Key<>();

    int currentCloneAttempt;

    private NodeCloner() {

    }

    public static NodeCloner getInstance(CompilerContext context) {

        NodeCloner nodeCloner = context.get(NODE_CLONER_KEY);
        if (nodeCloner == null) {
            nodeCloner = new NodeCloner();
            context.put(NODE_CLONER_KEY, nodeCloner);
        }
        return nodeCloner;
    }

    synchronized BLangCompilationUnit cloneCUnit(BLangCompilationUnit source) {

        source.cloneAttempt += 1;
        currentCloneAttempt = source.cloneAttempt;
        source.accept(this);
        BLangCompilationUnit clone = (BLangCompilationUnit) source.cloneRef;
        clone.pos = source.pos;
        clone.addWS(source.getWS());
        return clone;
    }

    private <T extends Node> List<T> cloneList(List<T> nodes) {

        if (nodes == null) {
            return null;
        }
        List<T> cloneList = new ArrayList<>();
        for (T node : nodes) {
            T clone = (T) clone(node);
            cloneList.add(clone);
        }
        return cloneList;
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> T clone(T source) {

        if (source == null) {
            return null;
        }
        BLangNode result;
        BLangNode sourceNode = (BLangNode) source;
        if (sourceNode.cloneRef != null && ((BLangNode) source).cloneAttempt == this.currentCloneAttempt) {
            // This is already cloned.
            result = sourceNode.cloneRef;
        } else {
            sourceNode.cloneAttempt = this.currentCloneAttempt;
            sourceNode.cloneRef = null;
            sourceNode.accept(this);
            result = sourceNode.cloneRef;
            result.pos = sourceNode.pos;
            result.addWS(source.getWS());
            result.type = sourceNode.type;
        }
        return (T) result;
    }

    /* Helper methods */

    private void cloneBLangInvokableNode(BLangInvokableNode source, BLangInvokableNode clone) {

        clone.name = source.name;
        clone.defaultWorkerName = source.defaultWorkerName;
        clone.requiredParams = cloneList(source.requiredParams);
        clone.returnTypeNode = clone(source.returnTypeNode);
        clone.returnTypeAnnAttachments = cloneList(source.returnTypeAnnAttachments);
        clone.body = clone(source.body);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.markdownDocumentationAttachment = clone(source.markdownDocumentationAttachment);
        clone.annAttachments = cloneList(source.annAttachments);
        clone.restParam = clone(source.restParam);
    }

    private void cloneBLangVariable(BLangVariable source, BLangVariable clone) {

        clone.typeNode = clone(source.typeNode);
        clone.expr = clone(source.expr);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.annAttachments = cloneList(source.annAttachments);
        clone.markdownDocumentationAttachment = clone(source.markdownDocumentationAttachment);
        clone.isDeclaredWithVar = source.isDeclaredWithVar;
    }

    private void cloneBLangLiteral(BLangLiteral source, BLangLiteral clone) {

        clone.value = source.value;
        clone.originalValue = source.originalValue;
        clone.isJSONContext = source.isJSONContext;
        clone.isFiniteContext = source.isFiniteContext;
        clone.isConstant = source.isConstant;
        clone.type = source.type;
    }

    private void cloneBLangAccessExpression(BLangAccessExpression source, BLangAccessExpression clone) {

        clone.expr = clone(source.expr);
        clone.optionalFieldAccess = source.optionalFieldAccess;
        clone.errorSafeNavigation = source.errorSafeNavigation;
        clone.nilSafeNavigation = source.nilSafeNavigation;
        clone.originalType = source.originalType;
        clone.leafNode = source.leafNode;
    }

    private void cloneBLangIndexBasedAccess(BLangIndexBasedAccess source, BLangIndexBasedAccess clone) {

        clone.indexExpr = clone(source.indexExpr);
        cloneBLangAccessExpression(source, clone);
    }

    private void cloneBLangStructureTypeNode(BLangStructureTypeNode source, BLangStructureTypeNode clone) {

        clone.fields = cloneList(source.fields);
        clone.initFunction = clone(source.initFunction);
        clone.isAnonymous = source.isAnonymous;
        clone.isLocal = source.isLocal;
        clone.typeRefs = cloneList(source.typeRefs);
    }

    private void cloneBLangMatchBindingPatternClause(BLangMatch.BLangMatchBindingPatternClause source,
                                                     BLangMatch.BLangMatchBindingPatternClause clone) {

        clone.body = clone(source.body);
        clone.matchExpr = clone(source.matchExpr);
        clone.isLastPattern = source.isLastPattern;
    }

    private void cloneBLangType(BLangType source, BLangType clone) {

        clone.nullable = source.nullable;
        clone.grouped = source.grouped;
    }

    private <T extends Enum<T>> EnumSet<T> cloneSet(Set<T> source, Class<T> elementType) {

        if (source == null || source.isEmpty()) {
            return EnumSet.noneOf(elementType);
        } else {
            return EnumSet.copyOf(source);
        }
    }

    /* Visitor Methods */

    @Override
    public void visit(BLangPackage pkgNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangCompilationUnit source) {

        BLangCompilationUnit clone = new BLangCompilationUnit();
        source.cloneRef = clone;
        clone.name = source.name;
        for (TopLevelNode node : source.topLevelNodes) {
            clone.topLevelNodes.add(clone(node));
        }
    }

    @Override
    public void visit(BLangImportPackage source) {

        BLangImportPackage clone = new BLangImportPackage();
        source.cloneRef = clone;
        clone.pkgNameComps = source.pkgNameComps;
        clone.version = source.version;
        clone.alias = source.alias;
        clone.orgName = source.orgName;
        clone.compUnit = source.compUnit;
    }

    @Override
    public void visit(BLangXMLNS source) {

        BLangXMLNS clone = new BLangXMLNS();
        source.cloneRef = clone;
        clone.namespaceURI = clone(source.namespaceURI);
        clone.prefix = source.prefix;
    }

    @Override
    public void visit(BLangFunction source) {

        BLangFunction clone = new BLangFunction();
        source.cloneRef = clone;

        clone.attachedFunction = source.attachedFunction;
        clone.objInitFunction = source.objInitFunction;
        clone.interfaceFunction = source.interfaceFunction;
        clone.anonForkName = source.anonForkName;

        cloneBLangInvokableNode(source, clone);
    }

    @Override
    public void visit(BLangBlockFunctionBody source) {
        BLangBlockFunctionBody clone = new BLangBlockFunctionBody();
        source.cloneRef = clone;
        clone.pos = source.pos;
        clone.stmts = cloneList(source.stmts);
    }

    @Override
    public void visit(BLangExprFunctionBody source) {
        BLangExprFunctionBody clone = new BLangExprFunctionBody();
        source.cloneRef = clone;
        clone.pos = source.pos;
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangExternalFunctionBody source) {
        BLangExternalFunctionBody clone = new BLangExternalFunctionBody();
        source.cloneRef = clone;
        clone.annAttachments = cloneList(source.annAttachments);
        clone.pos = source.pos;
    }

    @Override
    public void visit(BLangService source) {

        BLangService clone = new BLangService();
        source.cloneRef = clone;

        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.annAttachments = cloneList(source.annAttachments);
        clone.markdownDocumentationAttachment = clone(source.markdownDocumentationAttachment);

        clone.name = source.name;
        clone.serviceTypeDefinition = clone(source.serviceTypeDefinition);
        clone.attachedExprs = cloneList(source.attachedExprs);

        clone.variableNode = clone(source.variableNode);
        clone.isAnonymousServiceValue = source.isAnonymousServiceValue;
    }

    @Override
    public void visit(BLangResource resourceNode) {
        // Ignore
    }

    @Override
    public void visit(BLangTypeDefinition source) {

        BLangTypeDefinition clone = new BLangTypeDefinition();
        source.cloneRef = clone;
        clone.name = source.name;
        clone.typeNode = clone(source.typeNode);
        clone.annAttachments = cloneList(source.annAttachments);
        clone.markdownDocumentationAttachment = clone(source.markdownDocumentationAttachment);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.precedence = source.precedence;
    }

    @Override
    public void visit(BLangConstant source) {

        BLangConstant clone = new BLangConstant();
        source.cloneRef = clone;
        clone.name = source.name;
        clone.associatedTypeDefinition = clone(source.associatedTypeDefinition);

        cloneBLangVariable(source, clone);
    }

    @Override
    public void visit(BLangSimpleVariable source) {

        BLangSimpleVariable clone = new BLangSimpleVariable();
        source.cloneRef = clone;
        clone.name = source.name;

        cloneBLangVariable(source, clone);
    }

    @Override
    public void visit(BLangWorker workerNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangIdentifier source) {

        source.cloneRef = source;
    }

    @Override
    public void visit(BLangAnnotation source) {

        BLangAnnotation clone = new BLangAnnotation();
        source.cloneRef = clone;
        clone.name = source.name;
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.annAttachments = cloneList(source.annAttachments);
        clone.markdownDocumentationAttachment = clone(source.markdownDocumentationAttachment);
        clone.typeNode = clone(source.typeNode);
        clone.getAttachPoints().addAll(source.getAttachPoints());
    }

    @Override
    public void visit(BLangAnnotationAttachment source) {

        BLangAnnotationAttachment clone = new BLangAnnotationAttachment();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
        clone.annotationName = source.annotationName;
        clone.attachPoints.addAll(source.attachPoints);
        clone.pkgAlias = source.pkgAlias;
    }

    @Override
    public void visit(BLangBlockStmt source) {

        BLangBlockStmt clone = new BLangBlockStmt();
        source.cloneRef = clone;
        clone.stmts = cloneList(source.stmts);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangSimpleVariableDef source) {

        BLangSimpleVariableDef clone = new BLangSimpleVariableDef();
        source.cloneRef = clone;
        clone.var = clone(source.var);
        clone.isInFork = source.isInFork;
        clone.isWorker = source.isWorker;
    }

    @Override
    public void visit(BLangAssignment source) {

        BLangAssignment clone = new BLangAssignment();
        source.cloneRef = clone;
        clone.varRef = clone(source.varRef);
        clone.expr = clone(source.expr);
        clone.declaredWithVar = source.declaredWithVar;
    }

    @Override
    public void visit(BLangCompoundAssignment source) {

        BLangCompoundAssignment clone = new BLangCompoundAssignment();
        source.cloneRef = clone;
        clone.varRef = clone(source.varRef);
        clone.expr = clone(source.expr);
        clone.opKind = source.opKind;
    }

    @Override
    public void visit(BLangAbort source) {

        source.cloneRef = new BLangAbort();
    }

    @Override
    public void visit(BLangRetry source) {

        source.cloneRef = new BLangRetry();
    }

    @Override
    public void visit(BLangContinue source) {

        source.cloneRef = new BLangContinue();
    }

    @Override
    public void visit(BLangBreak source) {

        source.cloneRef = new BLangBreak();
    }

    @Override
    public void visit(BLangReturn source) {

        BLangReturn clone = new BLangReturn();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangPanic source) {

        BLangPanic clone = new BLangPanic();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement source) {

        BLangXMLNSStatement clone = new BLangXMLNSStatement();
        source.cloneRef = clone;
        clone.xmlnsDecl = clone(source.xmlnsDecl);
    }

    @Override
    public void visit(BLangExpressionStmt source) {

        BLangExpressionStmt clone = new BLangExpressionStmt();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangIf source) {

        BLangIf clone = new BLangIf();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
        clone.body = clone(source.body);
        clone.elseStmt = clone(source.elseStmt);
    }

    @Override
    public void visit(BLangMatch source) {

        BLangMatch clone = new BLangMatch();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
        clone.patternClauses = cloneList(source.patternClauses);
    }

    @Override
    public void visit(BLangMatchTypedBindingPatternClause source) {

        BLangMatchTypedBindingPatternClause clone = new BLangMatchTypedBindingPatternClause();
        source.cloneRef = clone;
        clone.variable = clone(source.variable);
        cloneBLangMatchBindingPatternClause(source, clone);
    }

    @Override
    public void visit(BLangMatchStaticBindingPatternClause source) {

        BLangMatchStaticBindingPatternClause clone = new BLangMatchStaticBindingPatternClause();
        source.cloneRef = clone;
        clone.literal = clone(source.literal);
        cloneBLangMatchBindingPatternClause(source, clone);
    }

    @Override
    public void visit(BLangMatchStructuredBindingPatternClause source) {

        BLangMatchStructuredBindingPatternClause clone = new BLangMatchStructuredBindingPatternClause();
        source.cloneRef = clone;
        clone.bindingPatternVariable = clone(source.bindingPatternVariable);
        clone.typeGuardExpr = clone(source.typeGuardExpr);
        cloneBLangMatchBindingPatternClause(source, clone);
    }

    @Override
    public void visit(BLangForeach source) {

        BLangForeach clone = new BLangForeach();
        source.cloneRef = clone;
        clone.collection = clone(source.collection);
        clone.body = clone(source.body);
        clone.variableDefinitionNode = (VariableDefinitionNode) clone((BLangNode) source.variableDefinitionNode);
        clone.isDeclaredWithVar = source.isDeclaredWithVar;
    }

    @Override
    public void visit(BLangWhile source) {

        BLangWhile clone = new BLangWhile();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
        clone.body = clone(source.body);
    }

    @Override
    public void visit(BLangLock source) {

        BLangLock clone = new BLangLock();
        source.cloneRef = clone;
        clone.body = clone(source.body);
    }

    @Override
    public void visit(BLangTransaction source) {

        BLangTransaction clone = new BLangTransaction();
        source.cloneRef = clone;
        clone.transactionBody = clone(source.transactionBody);
        clone.onRetryBody = clone(source.onRetryBody);
        clone.committedBody = clone(source.committedBody);
        clone.abortedBody = clone(source.abortedBody);
        clone.retryCount = clone(source.retryCount);
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangTupleDestructure source) {

        BLangTupleDestructure clone = new BLangTupleDestructure();
        source.cloneRef = clone;
        clone.varRef = clone(source.varRef);
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangRecordDestructure source) {

        BLangRecordDestructure clone = new BLangRecordDestructure();
        source.cloneRef = clone;
        clone.varRef = clone(source.varRef);
        clone.expr = clone(source.expr);
        clone.declaredWithVar = source.declaredWithVar;
    }

    @Override
    public void visit(BLangErrorDestructure source) {

        BLangErrorDestructure clone = new BLangErrorDestructure();
        source.cloneRef = clone;
        clone.varRef = clone(source.varRef);
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangCatch catchNode) {
        // Ignore
    }

    @Override
    public void visit(BLangForkJoin source) {

        BLangForkJoin clone = new BLangForkJoin();
        source.cloneRef = clone;
        clone.workers = cloneList(source.workers);
    }

    @Override
    public void visit(BLangWorkerSend source) {

        BLangWorkerSend clone = new BLangWorkerSend();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
        clone.workerIdentifier = source.workerIdentifier;
        clone.keyExpr = clone(source.keyExpr);
    }

    @Override
    public void visit(BLangWorkerReceive source) {

        BLangWorkerReceive clone = new BLangWorkerReceive();
        source.cloneRef = clone;

        clone.workerIdentifier = source.workerIdentifier;
        clone.keyExpr = clone(source.keyExpr);
        clone.isChannel = source.isChannel;
    }

    @Override
    public void visit(BLangLiteral source) {

        if (source.getKind() == NodeKind.NUMERIC_LITERAL) {
            visit((BLangNumericLiteral) source);
            return;
        }
        BLangLiteral clone = new BLangLiteral();
        source.cloneRef = clone;
        cloneBLangLiteral(source, clone);
    }

    @Override
    public void visit(BLangNumericLiteral source) {

        BLangNumericLiteral clone = new BLangNumericLiteral();
        source.cloneRef = clone;
        cloneBLangLiteral(source, clone);
    }

    @Override
    public void visit(BLangRecordLiteral source) {

        BLangRecordLiteral clone = new BLangRecordLiteral();
        source.cloneRef = clone;
        clone.fields = cloneList(source.fields);
    }

    @Override
    public void visit(BLangTupleVarRef source) {

        BLangTupleVarRef clone = new BLangTupleVarRef();
        source.cloneRef = clone;
        clone.pkgAlias = source.pkgAlias;
        clone.expressions = cloneList(source.expressions);
        clone.restParam = (ExpressionNode) clone((BLangNode) source.restParam);
    }

    @Override
    public void visit(BLangRecordVarRef source) {

        BLangRecordVarRef clone = new BLangRecordVarRef();
        source.cloneRef = clone;
        clone.pkgAlias = source.pkgAlias;
        for (BLangRecordVarRefKeyValue field : source.recordRefFields) {
            BLangRecordVarRefKeyValue keyValue = new BLangRecordVarRefKeyValue();
            keyValue.variableName = field.variableName;
            keyValue.variableReference = clone(field.variableReference);
            clone.recordRefFields.add(keyValue);
        }
        clone.restParam = (ExpressionNode) clone((BLangNode) source.restParam);
    }

    @Override
    public void visit(BLangErrorVarRef source) {

        BLangErrorVarRef clone = new BLangErrorVarRef();
        source.cloneRef = clone;
        clone.pkgAlias = source.pkgAlias;
        clone.message = clone(source.message);
        clone.cause = clone(source.cause);
        clone.detail = cloneList(source.detail);
        clone.restVar = clone(source.restVar);
        clone.typeNode = clone(source.typeNode);
    }

    @Override
    public void visit(BLangSimpleVarRef source) {

        BLangSimpleVarRef clone;

        if (source instanceof BLangRecordVarNameField) {
            BLangRecordVarNameField clonedVarNameField = new BLangRecordVarNameField();
            clonedVarNameField.isReadonly = ((BLangRecordVarNameField) source).isReadonly;
            clone = clonedVarNameField;
        } else {
            clone = new BLangSimpleVarRef();
        }

        source.cloneRef = clone;
        clone.pkgAlias = source.pkgAlias;
        clone.variableName = source.variableName;
    }

    @Override
    public void visit(BLangFieldBasedAccess source) {

        BLangFieldBasedAccess clone;
        if (source instanceof BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) {
            BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess fieldBasedAccess =
                    new BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess();
            fieldBasedAccess.nsPrefix = ((BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess) source).nsPrefix;
            clone = fieldBasedAccess;
        } else {
            clone = new BLangFieldBasedAccess();
        }

        source.cloneRef = clone;
        clone.field = source.field;
        clone.fieldKind = source.fieldKind;
        cloneBLangAccessExpression(source, clone);
    }

    @Override
    public void visit(BLangIndexBasedAccess source) {

        BLangIndexBasedAccess clone = new BLangIndexBasedAccess();
        source.cloneRef = clone;
        cloneBLangIndexBasedAccess(source, clone);
    }

    @Override
    public void visit(BLangTableMultiKeyExpr source) {

        BLangTableMultiKeyExpr clone = new BLangTableMultiKeyExpr();
        source.cloneRef = clone;
        clone.multiKeyIndexExprs = cloneList(source.multiKeyIndexExprs);
    }

    @Override
    public void visit(BLangInvocation source) {

        BLangInvocation clone = new BLangInvocation();
        source.cloneRef = clone;
        clone.pkgAlias = source.pkgAlias;
        clone.name = source.name;
        clone.argExprs = cloneList(source.argExprs);
        clone.functionPointerInvocation = source.functionPointerInvocation;
        clone.langLibInvocation = source.langLibInvocation;
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.annAttachments = cloneList(source.annAttachments);
        clone.requiredArgs = cloneList(source.requiredArgs);

        cloneBLangAccessExpression(source, clone);
    }

    @Override
    public void visit(BLangTypeInit source) {

        BLangTypeInit clone = new BLangTypeInit();
        source.cloneRef = clone;
        clone.userDefinedType = clone(source.userDefinedType);
        clone.argsExpr = cloneList(source.argsExpr);
        clone.initInvocation = clone(source.initInvocation);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation source) {
        BLangInvocation.BLangActionInvocation clone = new BLangInvocation.BLangActionInvocation();
        source.cloneRef = clone;
        clone.pkgAlias = source.pkgAlias;
        clone.name = source.name;
        clone.argExprs = cloneList(source.argExprs);
        clone.functionPointerInvocation = source.functionPointerInvocation;
        clone.langLibInvocation = source.langLibInvocation;
        clone.async = source.async;
        clone.remoteMethodCall = source.remoteMethodCall;
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.annAttachments = cloneList(source.annAttachments);
        clone.requiredArgs = cloneList(source.requiredArgs);

        cloneBLangAccessExpression(source, clone);
    }

    @Override
    public void visit(BLangTernaryExpr source) {

        BLangTernaryExpr clone = new BLangTernaryExpr();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
        clone.thenExpr = clone(source.thenExpr);
        clone.elseExpr = clone(source.elseExpr);
    }

    @Override
    public void visit(BLangWaitExpr source) {

        BLangWaitExpr clone = new BLangWaitExpr();
        source.cloneRef = clone;
        clone.exprList = cloneList(source.exprList);
    }

    @Override
    public void visit(BLangTrapExpr source) {

        BLangTrapExpr clone = new BLangTrapExpr();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangBinaryExpr source) {

        BLangBinaryExpr clone = new BLangBinaryExpr();
        source.cloneRef = clone;
        clone.lhsExpr = clone(source.lhsExpr);
        clone.rhsExpr = clone(source.rhsExpr);
        clone.opKind = source.opKind;
        clone.opSymbol = source.opSymbol;
    }

    @Override
    public void visit(BLangElvisExpr source) {

        BLangElvisExpr clone = new BLangElvisExpr();
        source.cloneRef = clone;
        clone.lhsExpr = clone(source.lhsExpr);
        clone.rhsExpr = clone(source.rhsExpr);
    }

    @Override
    public void visit(BLangGroupExpr source) {

        BLangGroupExpr clone = new BLangGroupExpr();
        source.cloneRef = clone;
        clone.expression = clone(source.expression);
        clone.isTypedescExpr = source.isTypedescExpr;
        clone.typedescType = source.typedescType;
    }

    @Override
    public void visit(BLangLetExpression source) {
        BLangLetExpression clone = new BLangLetExpression();
        source.cloneRef = clone;
        clone.letVarDeclarations = cloneLetVarDeclarations(source.letVarDeclarations);
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangListConstructorExpr source) {

        BLangListConstructorExpr clone = new BLangListConstructorExpr();
        source.cloneRef = clone;
        clone.exprs = cloneList(source.exprs);
        clone.isTypedescExpr = source.isTypedescExpr;
        clone.typedescType = source.typedescType;
    }

    public void visit(BLangTableConstructorExpr source) {

        BLangTableConstructorExpr clone = new BLangTableConstructorExpr();
        source.cloneRef = clone;
        clone.recordLiteralList = cloneList(source.recordLiteralList);
        clone.tableKeySpecifier = clone(source.tableKeySpecifier);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangUnaryExpr source) {

        BLangUnaryExpr clone = new BLangUnaryExpr();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
        clone.operator = source.operator;
    }

    @Override
    public void visit(BLangTypedescExpr source) {

        BLangTypedescExpr clone = new BLangTypedescExpr();
        source.cloneRef = clone;
        clone.typeNode = clone(source.typeNode);
        clone.resolvedType = source.resolvedType;
    }

    @Override
    public void visit(BLangTypeConversionExpr source) {

        BLangTypeConversionExpr clone = new BLangTypeConversionExpr();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
        clone.typeNode = clone(source.typeNode);
        clone.targetType = source.targetType;
        clone.annAttachments = cloneList(source.annAttachments);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.checkTypes = source.checkTypes;
    }

    @Override
    public void visit(BLangXMLQName source) {

        BLangXMLQName clone = new BLangXMLQName();
        source.cloneRef = clone;
        clone.localname = source.localname;
        clone.prefix = source.prefix;
        clone.namespaceURI = source.namespaceURI;
        clone.isUsedInXML = source.isUsedInXML;
    }

    @Override
    public void visit(BLangXMLAttribute source) {

        BLangXMLAttribute clone = new BLangXMLAttribute();
        source.cloneRef = clone;
        clone.name = clone(source.name);
        clone.value = clone(source.value);
        clone.isNamespaceDeclr = source.isNamespaceDeclr;
    }

    @Override
    public void visit(BLangXMLElementLiteral source) {

        BLangXMLElementLiteral clone = new BLangXMLElementLiteral();
        source.cloneRef = clone;
        clone.startTagName = clone(source.startTagName);
        clone.endTagName = clone(source.endTagName);
        clone.attributes = cloneList(source.attributes);
        clone.children = cloneList(source.children);

        clone.inlineNamespaces = cloneList(source.inlineNamespaces);
        clone.isRoot = source.isRoot;
    }

    @Override
    public void visit(BLangXMLTextLiteral source) {

        BLangXMLTextLiteral clone = new BLangXMLTextLiteral();
        source.cloneRef = clone;
        clone.textFragments = cloneList(source.textFragments);
        clone.concatExpr = clone(source.concatExpr);
    }

    @Override
    public void visit(BLangXMLCommentLiteral source) {

        BLangXMLCommentLiteral clone = new BLangXMLCommentLiteral();
        source.cloneRef = clone;
        clone.textFragments = cloneList(source.textFragments);
        clone.concatExpr = clone(source.concatExpr);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral source) {

        BLangXMLProcInsLiteral clone = new BLangXMLProcInsLiteral();
        source.cloneRef = clone;
        clone.target = clone(source.target);
        clone.dataFragments = cloneList(source.dataFragments);
        clone.dataConcatExpr = clone(source.dataConcatExpr);
    }

    @Override
    public void visit(BLangXMLQuotedString source) {

        BLangXMLQuotedString clone = new BLangXMLQuotedString();
        source.cloneRef = clone;
        clone.textFragments = cloneList(source.textFragments);
        clone.quoteType = source.quoteType;
        clone.concatExpr = clone(source.concatExpr);
    }

    @Override
    public void visit(BLangStringTemplateLiteral source) {

        BLangStringTemplateLiteral clone = new BLangStringTemplateLiteral();
        source.cloneRef = clone;
        clone.exprs = cloneList(source.exprs);
    }

    @Override
    public void visit(BLangLambdaFunction source) {

        BLangLambdaFunction clone = new BLangLambdaFunction();
        source.cloneRef = clone;
        clone.function = clone(source.function);
    }

    @Override
    public void visit(BLangArrowFunction source) {

        BLangArrowFunction clone = new BLangArrowFunction();
        source.cloneRef = clone;
        clone.params = cloneList(source.params);
        clone.body = clone(source.body);
        clone.funcType = source.funcType;
        clone.functionName = source.functionName;
    }

    @Override
    public void visit(BLangXMLAttributeAccess source) {

        BLangXMLAttributeAccess clone = new BLangXMLAttributeAccess();
        source.cloneRef = clone;
        cloneBLangIndexBasedAccess(source, clone);
    }

    @Override
    public void visit(BLangIntRangeExpression source) {

        BLangIntRangeExpression clone = new BLangIntRangeExpression();
        source.cloneRef = clone;
        clone.includeStart = source.includeStart;
        clone.includeEnd = source.includeEnd;
        clone.startExpr = clone(source.startExpr);
        clone.endExpr = clone(source.endExpr);
    }

    @Override
    public void visit(BLangRestArgsExpression source) {

        BLangRestArgsExpression clone = new BLangRestArgsExpression();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression source) {

        BLangNamedArgsExpression clone = new BLangNamedArgsExpression();
        source.cloneRef = clone;
        clone.name = source.name;
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangIsAssignableExpr source) {

        BLangIsAssignableExpr clone = new BLangIsAssignableExpr();
        source.cloneRef = clone;
        clone.lhsExpr = clone(source.lhsExpr);
        clone.targetType = source.targetType;
        clone.typeNode = clone(source.typeNode);
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {

        // Ignore
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {

        // Ignore
    }

    @Override
    public void visit(BLangCheckedExpr source) {

        BLangCheckedExpr clone = new BLangCheckedExpr();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr source) {

        BLangCheckPanickedExpr clone = new BLangCheckPanickedExpr();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangServiceConstructorExpr source) {

        BLangServiceConstructorExpr clone = new BLangServiceConstructorExpr();
        source.cloneRef = clone;
        clone.serviceNode = clone(source.serviceNode);
    }

    @Override
    public void visit(BLangTypeTestExpr source) {

        BLangTypeTestExpr clone = new BLangTypeTestExpr();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
        clone.typeNode = clone(source.typeNode);
    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {
        // Ignore.
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangAnnotAccessExpr source) {

        BLangAnnotAccessExpr clone = new BLangAnnotAccessExpr();
        source.cloneRef = clone;
        clone.pkgAlias = source.pkgAlias;
        clone.annotationName = source.annotationName;
        cloneBLangAccessExpression(source, clone);
    }

    @Override
    public void visit(BLangQueryAction source) {

        BLangQueryAction clone = new BLangQueryAction();
        source.cloneRef = clone;
        clone.queryClauseList = cloneList(source.queryClauseList);
    }

    @Override
    public void visit(BLangQueryExpr source) {

        BLangQueryExpr clone = new BLangQueryExpr();
        source.cloneRef = clone;
        clone.queryClauseList = cloneList(source.queryClauseList);
        clone.fieldNameIdentifierList = cloneList(source.fieldNameIdentifierList);
        clone.isStream = source.isStream;
        clone.isTable = source.isTable;
    }

    @Override
    public void visit(BLangFromClause source) {

        BLangFromClause clone = new BLangFromClause();
        source.cloneRef = clone;
        clone.variableDefinitionNode = (VariableDefinitionNode) clone((BLangNode) source.variableDefinitionNode);
        clone.collection = clone(source.collection);
        clone.isDeclaredWithVar = source.isDeclaredWithVar;
        clone.varType = source.varType;
        clone.resultType = source.resultType;
        clone.nillableResultType = source.nillableResultType;
    }

    @Override
    public void visit(BLangJoinClause source) {

        BLangJoinClause clone = new BLangJoinClause();
        source.cloneRef = clone;
        clone.variableDefinitionNode = (VariableDefinitionNode) clone((BLangNode) source.variableDefinitionNode);
        clone.collection = clone(source.collection);
        clone.isDeclaredWithVar = source.isDeclaredWithVar;
        clone.varType = source.varType;
        clone.resultType = source.resultType;
        clone.nillableResultType = source.nillableResultType;

    }

    @Override
    public void visit(BLangLetClause source) {
        BLangLetClause clone = new BLangLetClause();
        source.cloneRef = clone;
        clone.letVarDeclarations = cloneLetVarDeclarations(source.letVarDeclarations);
    }

    private List<BLangLetVariable> cloneLetVarDeclarations (List<BLangLetVariable> letVarDeclarations) {
        List<BLangLetVariable> cloneDefs = new ArrayList<>();
        for (BLangLetVariable letVarDeclaration : letVarDeclarations) {
            BLangLetVariable clonedVar = new BLangLetVariable();
            clonedVar.definitionNode = clone(letVarDeclaration.definitionNode);
            cloneDefs.add(clonedVar);
        }
        return cloneDefs;
    }

    @Override
    public void visit(BLangOnClause source) {

        BLangOnClause clone = new BLangOnClause();
        source.cloneRef = clone;
        clone.expression = clone(source.expression);
    }

    @Override
    public void visit(BLangSelectClause source) {

        BLangSelectClause clone = new BLangSelectClause();
        source.cloneRef = clone;
        clone.expression = clone(source.expression);
    }

    @Override
    public void visit(BLangOnConflictClause source) {

        BLangOnConflictClause clone = new BLangOnConflictClause();
        source.cloneRef = clone;
        clone.expression = clone(source.expression);
    }

    @Override
    public void visit(BLangLimitClause source) {

        BLangLimitClause clone = new BLangLimitClause();
        source.cloneRef = clone;
        clone.expression = clone(source.expression);
    }

    @Override
    public void visit(BLangWhereClause source) {

        BLangWhereClause clone = new BLangWhereClause();
        source.cloneRef = clone;
        clone.expression = clone(source.expression);
    }

    @Override
    public void visit(BLangDoClause source) {

        BLangDoClause clone = new BLangDoClause();
        source.cloneRef = clone;
        clone.body = clone(source.body);
    }

    @Override
    public void visit(BLangValueType source) {

        BLangValueType clone = new BLangValueType();
        source.cloneRef = clone;
        clone.typeKind = source.typeKind;
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangArrayType source) {

        BLangArrayType clone = new BLangArrayType();
        source.cloneRef = clone;
        clone.elemtype = clone(source.elemtype);
        clone.dimensions = source.dimensions;
        clone.sizes = source.sizes;
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode source) {

        BLangBuiltInRefTypeNode clone = new BLangBuiltInRefTypeNode();
        source.cloneRef = clone;
        clone.typeKind = source.typeKind;
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangConstrainedType source) {

        BLangConstrainedType clone = new BLangConstrainedType();
        source.cloneRef = clone;
        clone.type = clone(source.type);
        clone.constraint = clone(source.constraint);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangStreamType source) {

        BLangStreamType clone = new BLangStreamType();
        source.cloneRef = clone;
        clone.type = clone(source.type);
        clone.constraint = clone(source.constraint);
        clone.error = clone(source.error);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangUserDefinedType source) {

        BLangUserDefinedType clone = new BLangUserDefinedType();
        source.cloneRef = clone;
        clone.pkgAlias = source.pkgAlias;
        clone.typeName = source.typeName;
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangFunctionTypeNode source) {

        BLangFunctionTypeNode clone = new BLangFunctionTypeNode();
        source.cloneRef = clone;
        clone.params = cloneList(source.params);
        clone.restParam = clone(source.restParam);
        clone.returnTypeNode = clone(source.returnTypeNode);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.returnsKeywordExists = source.returnsKeywordExists;
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangUnionTypeNode source) {

        BLangUnionTypeNode clone = new BLangUnionTypeNode();
        source.cloneRef = clone;
        clone.memberTypeNodes = cloneList(source.memberTypeNodes);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangIntersectionTypeNode source) {

        BLangIntersectionTypeNode clone = new BLangIntersectionTypeNode();
        source.cloneRef = clone;
        clone.constituentTypeNodes = cloneList(source.constituentTypeNodes);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangObjectTypeNode source) {

        BLangObjectTypeNode clone = new BLangObjectTypeNode();
        source.cloneRef = clone;
        clone.functions = cloneList(source.functions);
        clone.initFunction = clone(source.initFunction);
        clone.receiver = clone(source.receiver);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        cloneBLangStructureTypeNode(source, clone);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangRecordTypeNode source) {

        BLangRecordTypeNode clone = new BLangRecordTypeNode();
        source.cloneRef = clone;
        clone.sealed = source.sealed;
        clone.restFieldType = clone(source.restFieldType);
        clone.analyzed = source.analyzed;
        cloneBLangStructureTypeNode(source, clone);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangTableTypeNode source) {

        BLangTableTypeNode clone = new BLangTableTypeNode();
        source.cloneRef = clone;
        clone.type = clone(source.type);
        clone.tableKeySpecifier = clone(source.tableKeySpecifier);
        clone.tableKeyTypeConstraint = clone(source.tableKeyTypeConstraint);
        clone.constraint = clone(source.constraint);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangTableKeySpecifier source) {

        BLangTableKeySpecifier clone = new BLangTableKeySpecifier();
        source.cloneRef = clone;
        clone.fieldNameIdentifierList = cloneList(source.fieldNameIdentifierList);
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint source) {

        BLangTableKeyTypeConstraint clone = new BLangTableKeyTypeConstraint();
        source.cloneRef = clone;
        clone.keyType = clone(source.keyType);
    }

    @Override
    public void visit(BLangFiniteTypeNode source) {

        BLangFiniteTypeNode clone = new BLangFiniteTypeNode();
        source.cloneRef = clone;
        clone.valueSpace = cloneList(source.valueSpace);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangTupleTypeNode source) {

        BLangTupleTypeNode clone = new BLangTupleTypeNode();
        source.cloneRef = clone;
        clone.memberTypeNodes = cloneList(source.memberTypeNodes);
        clone.restParamType = clone(source.restParamType);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangErrorType source) {

        BLangErrorType clone = new BLangErrorType();
        source.cloneRef = clone;
        clone.detailType = clone(source.detailType);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        cloneBLangType(source, clone);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        // Ignore
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        // Ignore
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        // Ignore
    }

    @Override
    public void visit(BLangConstRef constRef) {
        BLangConstRef clone = new BLangConstRef();
        constRef.cloneRef = clone;
        clone.pkgAlias = constRef.pkgAlias;
        clone.originalValue = constRef.originalValue;
        clone.value = constRef.value;
        clone.variableName = constRef.variableName;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        // Ignore
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
        // Ignore
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        // Ignore
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        // Ignore
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        // Ignore
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        // Ignore
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        // Ignore
    }

    @Override
    public void visit(BLangStatementExpression source) {

        BLangStatementExpression clone = new BLangStatementExpression();
        source.cloneRef = clone;
        clone.expr = clone(source.expr);
        clone.stmt = clone(source.stmt);
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine source) {

        BLangMarkdownDocumentationLine clone = new BLangMarkdownDocumentationLine();
        source.cloneRef = clone;
        clone.text = source.text;
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation source) {

        BLangMarkdownParameterDocumentation clone = new BLangMarkdownParameterDocumentation();
        source.cloneRef = clone;
        clone.parameterName = source.parameterName;
        clone.parameterDocumentationLines = source.parameterDocumentationLines;
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation source) {

        BLangMarkdownReturnParameterDocumentation clone = new BLangMarkdownReturnParameterDocumentation();
        source.cloneRef = clone;
        clone.returnParameterDocumentationLines = source.returnParameterDocumentationLines;
        clone.type = source.type;
    }

    @Override
    public void visit(BLangMarkDownDeprecationDocumentation source) {
        BLangMarkDownDeprecationDocumentation clone = new BLangMarkDownDeprecationDocumentation();
        source.cloneRef = clone;
        clone.deprecationLine = source.deprecationLine;
        clone.deprecationDocumentationLines = source.deprecationDocumentationLines;
        clone.isCorrectDeprecationLine = source.isCorrectDeprecationLine;
    }

    @Override
    public void visit(BLangMarkDownDeprecatedParametersDocumentation source) {
        BLangMarkDownDeprecatedParametersDocumentation clone = new BLangMarkDownDeprecatedParametersDocumentation();
        source.cloneRef = clone;
        clone.parameters = source.parameters;
    }

    @Override
    public void visit(BLangMarkdownDocumentation source) {
        BLangMarkdownDocumentation clone = new BLangMarkdownDocumentation();
        source.cloneRef = clone;
        clone.documentationLines.addAll(cloneList(source.documentationLines));
        clone.parameters.addAll(cloneList(source.parameters));
        clone.references.addAll(cloneList(source.references));
        clone.returnParameter = clone(source.returnParameter);
        clone.deprecationDocumentation = clone(source.deprecationDocumentation);
        clone.deprecatedParametersDocumentation = clone(source.deprecatedParametersDocumentation);
    }

    @Override
    public void visit(BLangTupleVariable source) {

        BLangTupleVariable clone = new BLangTupleVariable();
        source.cloneRef = clone;
        clone.memberVariables = cloneList(source.memberVariables);
        clone.restVariable = clone(source.restVariable);
        cloneBLangVariable(source, clone);
    }

    @Override
    public void visit(BLangTupleVariableDef source) {

        BLangTupleVariableDef clone = new BLangTupleVariableDef();
        source.cloneRef = clone;
        clone.var = clone(source.var);
    }

    @Override
    public void visit(BLangRecordVariable source) {

        BLangRecordVariable clone = new BLangRecordVariable();
        source.cloneRef = clone;
        for (BLangRecordVariableKeyValue keyValue : source.variableList) {
            BLangRecordVariableKeyValue newKeyValue = new BLangRecordVariableKeyValue();
            newKeyValue.key = keyValue.key;
            newKeyValue.valueBindingPattern = clone(keyValue.valueBindingPattern);
            clone.variableList.add(newKeyValue);
        }
        clone.restParam = clone((BLangVariable) source.restParam);
        cloneBLangVariable(source, clone);
    }

    @Override
    public void visit(BLangRecordVariableDef source) {

        BLangRecordVariableDef clone = new BLangRecordVariableDef();
        source.cloneRef = clone;
        clone.var = clone(source.var);
    }

    @Override
    public void visit(BLangErrorVariable source) {

        BLangErrorVariable clone = new BLangErrorVariable();
        source.cloneRef = clone;
        clone.message = clone(source.message);
        for (BLangErrorDetailEntry entry : source.detail) {
            clone.detail.add(new BLangErrorDetailEntry(entry.key, clone(entry.valueBindingPattern)));
        }
        clone.restDetail = clone(source.restDetail);
        clone.detailExpr = clone(source.detailExpr);
        clone.cause = clone(source.cause);
        clone.reasonVarPrefixAvailable = source.reasonVarPrefixAvailable;
        clone.reasonMatchConst = source.reasonMatchConst;
        clone.isInMatchStmt = source.isInMatchStmt;
        cloneBLangVariable(source, clone);
    }

    @Override
    public void visit(BLangErrorVariableDef source) {

        BLangErrorVariableDef clone = new BLangErrorVariableDef();
        source.cloneRef = clone;
        clone.errorVariable = clone(source.errorVariable);
    }

    @Override
    public void visit(BLangWorkerFlushExpr source) {

        BLangWorkerFlushExpr clone = new BLangWorkerFlushExpr();
        source.cloneRef = clone;
        clone.workerIdentifier = source.workerIdentifier;
        clone.workerIdentifierList.addAll(source.workerIdentifierList);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr source) {

        BLangWorkerSyncSendExpr clone = new BLangWorkerSyncSendExpr();
        source.cloneRef = clone;
        clone.workerIdentifier = source.workerIdentifier;
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangWaitForAllExpr source) {

        BLangWaitForAllExpr clone = new BLangWaitForAllExpr();
        source.cloneRef = clone;
        for (BLangWaitKeyValue keyValue : source.keyValuePairs) {
            clone.keyValuePairs.add(clone(keyValue));
        }
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation source) {

        BLangMarkdownReferenceDocumentation clone = new BLangMarkdownReferenceDocumentation();
        source.cloneRef = clone;
        clone.qualifier = source.qualifier;
        clone.typeName = source.typeName;
        clone.identifier = source.identifier;
        clone.referenceName = source.referenceName;
        clone.kind = source.kind;
        clone.type = source.type;
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangRecordKeyValueField source) {

        BLangRecordKeyValueField clone = new BLangRecordKeyValueField();
        source.cloneRef = clone;
        clone.pos = source.pos;
        clone.isReadonly = source.isReadonly;
        clone.addWS(source.getWS());

        BLangRecordKey newKey = new BLangRecordKey(clone(source.key.expr));
        newKey.computedKey = source.key.computedKey;
        clone.key = newKey;

        clone.valueExpr = clone(source.valueExpr);
    }


    @Override
    public void visit(BLangRecordSpreadOperatorField source) {

        BLangRecordSpreadOperatorField clone = new BLangRecordSpreadOperatorField();
        source.cloneRef = clone;
        clone.pos = source.pos;
        clone.addWS(source.getWS());
        clone.expr = clone(source.expr);
    }

    @Override
    public void visit(BLangWaitKeyValue source) {

        BLangWaitKeyValue clone = new BLangWaitKeyValue();
        source.cloneRef = clone;
        clone.pos = source.pos;
        clone.addWS(source.getWS());
        clone.key = source.key;
        clone.valueExpr = clone(source.valueExpr);
        clone.keyExpr = clone(source.keyExpr);
    }

    @Override
    public void visit(BLangXMLElementFilter source) {
        BLangXMLElementFilter clone = new BLangXMLElementFilter(
                source.pos,
                source.getWS(),
                source.namespace,
                source.nsPos,
                source.name,
                source.elemNamePos);
        source.cloneRef = clone;
    }

    @Override
    public void visit(BLangXMLElementAccess source) {
        BLangXMLElementAccess clone = new BLangXMLElementAccess(
                source.pos,
                source.getWS(),
                clone(source.expr),
                cloneList(source.filters));
        source.cloneRef = clone;
    }

    @Override
    public void visit(BLangXMLNavigationAccess source) {
        BLangXMLNavigationAccess clone = new BLangXMLNavigationAccess(
                source.pos,
                source.getWS(),
                clone(source.expr),
                cloneList(source.filters),
                source.navAccessType,
                clone(source.childIndex));
        source.cloneRef = clone;
    }
}
