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
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.clauses.PatternStreamingEdgeInputNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable.BLangErrorDetailEntry;
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
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFunctionClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOutputRateLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingEdgeInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSetAssignment;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangTableQuery;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWithinClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
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
class NodeCloner extends BLangNodeVisitor {

    private static final CompilerContext.Key<NodeCloner> NODE_CLONER_KEY =
            new CompilerContext.Key<>();

    BLangNode result;

    NodeCloner(CompilerContext context) {

    }

    public static NodeCloner getInstance(CompilerContext context) {

        NodeCloner cleaner = context.get(NODE_CLONER_KEY);
        if (cleaner == null) {
            cleaner = new NodeCloner(context);
        }
        return cleaner;
    }

    private <T extends BLangNode> List<T> cloneList(List<T> nodes) {

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
    <T extends Node> T clone(T node) {

        if (node == null) {
            return null;
        }
        BLangNode currentClone = this.result;

        BLangNode bLangNode = (BLangNode) node;
        bLangNode.accept(this);
        BLangNode result = this.result;
        BLangNode source = (BLangNode) node;

        result.pos = source.pos;
        result.addWS(source.getWS());
        result.type = source.type;

        this.result = currentClone;
        return (T) result;
    }

    /* Helper methods */

    private void cloneBLangInvokableNode(BLangInvokableNode source, BLangInvokableNode clone) {

        clone.name = source.name;
        clone.defaultWorkerName = source.defaultWorkerName;
        clone.returnTypeNode = clone(source.returnTypeNode);
        clone.returnTypeAnnAttachments = cloneList(source.returnTypeAnnAttachments);
        clone.externalAnnAttachments = cloneList(source.externalAnnAttachments);
        clone.body = clone(source.body);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.markdownDocumentationAttachment = clone(source.markdownDocumentationAttachment);

        clone.annAttachments = cloneList(source.annAttachments);
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
        clone.isFieldAnalyseRequired = source.isFieldAnalyseRequired;
        clone.typeRefs = cloneList(source.typeRefs);
    }

    private void cloneBLangMatchBindingPatternClause(BLangMatch.BLangMatchBindingPatternClause source,
                                                     BLangMatch.BLangMatchBindingPatternClause clone) {

        clone.body = clone(source.body);
        clone.matchExpr = clone(source.matchExpr);
        clone.isLastPattern = source.isLastPattern;
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

        BLangCompilationUnit compilationUnit = new BLangCompilationUnit();
        compilationUnit.name = source.name;
        compilationUnit.hash = source.hash;
        for (TopLevelNode node : source.topLevelNodes) {
            compilationUnit.topLevelNodes.add(clone(node));
        }
        result = compilationUnit;
    }

    @Override
    public void visit(BLangImportPackage source) {

        BLangImportPackage clone = new BLangImportPackage();
        clone.pkgNameComps = source.pkgNameComps;
        clone.version = source.version;
        clone.alias = source.alias;
        clone.orgName = source.orgName;
        clone.compUnit = source.compUnit;
        this.result = clone;
    }

    @Override
    public void visit(BLangXMLNS source) {

        BLangXMLNS clone = new BLangXMLNS();
        ;
        clone.namespaceURI = clone(source.namespaceURI);
        clone.prefix = source.prefix;
        this.result = clone;
    }

    @Override
    public void visit(BLangFunction source) {

        BLangFunction clone = new BLangFunction();

        clone.attachedFunction = source.attachedFunction;
        clone.objInitFunction = source.objInitFunction;
        clone.interfaceFunction = source.interfaceFunction;
        clone.anonForkName = source.anonForkName;

        cloneBLangInvokableNode(source, clone);
        this.result = clone;
    }

    @Override
    public void visit(BLangService source) {

        BLangService clone = new BLangService();
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.annAttachments = cloneList(source.annAttachments);
        clone.markdownDocumentationAttachment = clone(source.markdownDocumentationAttachment);

        clone.name = source.name;
        clone.serviceTypeDefinition = clone(source.serviceTypeDefinition);
        clone.attachedExprs = cloneList(source.attachedExprs);

        // TODO : Handle this properly.
        clone.variableNode = clone(source.variableNode);
        clone.isAnonymousServiceValue = source.isAnonymousServiceValue;

        this.result = clone;
    }

    @Override
    public void visit(BLangResource resourceNode) {
        // Ignore
    }

    @Override
    public void visit(BLangTypeDefinition source) {

        BLangTypeDefinition clone = new BLangTypeDefinition();
        clone.name = source.name;
        clone.typeNode = clone(source.typeNode);
        clone.annAttachments = cloneList(source.annAttachments);
        clone.markdownDocumentationAttachment = clone(source.markdownDocumentationAttachment);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.precedence = source.precedence;

        result = clone;
    }

    @Override
    public void visit(BLangConstant source) {

        BLangConstant clone = new BLangConstant();
        clone.name = source.name;
        clone.associatedTypeDefinition = clone(source.associatedTypeDefinition);

        cloneBLangVariable(source, clone);
        result = clone;
    }

    @Override
    public void visit(BLangSimpleVariable source) {

        BLangSimpleVariable clone = new BLangSimpleVariable();
        clone.name = source.name;

        cloneBLangVariable(source, clone);
        result = clone;
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
    public void visit(BLangIdentifier identifierNode) {

        result = identifierNode;
    }

    @Override
    public void visit(BLangAnnotation source) {

        BLangAnnotation clone = new BLangAnnotation();
        clone.name = source.name;
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.annAttachments = cloneList(source.annAttachments);
        clone.markdownDocumentationAttachment = clone(source.markdownDocumentationAttachment);
        clone.typeNode = clone(source.typeNode);
        clone.getAttachPoints().addAll(source.getAttachPoints());

        result = clone;
    }

    @Override
    public void visit(BLangAnnotationAttachment source) {

        BLangAnnotationAttachment clone = new BLangAnnotationAttachment();
        clone.expr = clone(source.expr);
        clone.annotationName = source.annotationName;
        clone.attachPoints.addAll(source.attachPoints);
        clone.pkgAlias = source.pkgAlias;

        result = clone;
    }

    @Override
    public void visit(BLangBlockStmt source) {

        BLangBlockStmt clone = new BLangBlockStmt();
        clone.stmts = cloneList(source.stmts);

        result = clone;
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
        clone.var = clone(source.var);
        clone.isInFork = source.isInFork;
        clone.isWorker = source.isWorker;

        result = clone;
    }

    @Override
    public void visit(BLangAssignment source) {

        BLangAssignment clone = new BLangAssignment();
        clone.varRef = clone(source.varRef);
        clone.expr = clone(source.expr);
        clone.declaredWithVar = source.declaredWithVar;

        result = clone;
    }

    @Override
    public void visit(BLangCompoundAssignment source) {

        BLangCompoundAssignment clone = new BLangCompoundAssignment();
        clone.varRef = clone(source.varRef);
        clone.expr = clone(source.expr);
        clone.opKind = source.opKind;

        result = clone;
    }

    @Override
    public void visit(BLangAbort abortNode) {

        result = new BLangAbort();
    }

    @Override
    public void visit(BLangRetry retryNode) {

        result = new BLangRetry();
    }

    @Override
    public void visit(BLangContinue continueNode) {

        result = new BLangContinue();
    }

    @Override
    public void visit(BLangBreak breakNode) {

        result = new BLangBreak();
    }

    @Override
    public void visit(BLangReturn source) {

        BLangReturn clone = new BLangReturn();
        clone.expr = clone(source.expr);

        result = clone;
    }

    @Override
    public void visit(BLangPanic panicNode) {

        BLangPanic clone = new BLangPanic();
        clone.expr = clone(panicNode.expr);

        result = clone;
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {

        BLangXMLNSStatement clone = new BLangXMLNSStatement();
        clone.xmlnsDecl = clone(xmlnsStmtNode.xmlnsDecl);

        result = clone;
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {

        BLangExpressionStmt clone = new BLangExpressionStmt();
        clone.expr = clone(exprStmtNode.expr);

        result = clone;
    }

    @Override
    public void visit(BLangIf source) {

        BLangIf clone = new BLangIf();
        clone.expr = clone(source.expr);
        clone.body = clone(source.body);
        clone.elseStmt = clone(source.elseStmt);

        result = clone;
    }

    @Override
    public void visit(BLangMatch source) {

        BLangMatch clone = new BLangMatch();
        clone.expr = clone(source.expr);
        clone.patternClauses = cloneList(source.patternClauses);

        result = clone;
    }

    @Override
    public void visit(BLangMatchTypedBindingPatternClause source) {

        BLangMatchTypedBindingPatternClause clone = new BLangMatchTypedBindingPatternClause();
        clone.variable = clone(source.variable);
        cloneBLangMatchBindingPatternClause(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangMatchStaticBindingPatternClause source) {

        BLangMatchStaticBindingPatternClause clone = new BLangMatchStaticBindingPatternClause();
        clone.literal = clone(source.literal);
        cloneBLangMatchBindingPatternClause(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangMatchStructuredBindingPatternClause source) {

        BLangMatchStructuredBindingPatternClause clone = new BLangMatchStructuredBindingPatternClause();
        clone.bindingPatternVariable = clone(source.bindingPatternVariable);
        clone.typeGuardExpr = clone(source.typeGuardExpr);
        cloneBLangMatchBindingPatternClause(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangForeach source) {

        BLangForeach clone = new BLangForeach();
        clone.collection = clone(source.collection);
        clone.body = clone(source.body);
        clone.variableDefinitionNode = (VariableDefinitionNode) clone((BLangNode) source.variableDefinitionNode);
        clone.isDeclaredWithVar = source.isDeclaredWithVar;

        result = clone;
    }

    @Override
    public void visit(BLangWhile source) {

        BLangWhile clone = new BLangWhile();
        clone.expr = clone(source.expr);
        clone.body = clone(source.body);

        result = clone;
    }

    @Override
    public void visit(BLangLock source) {

        BLangLock clone = new BLangLock();
        clone.body = clone(source.body);

        result = clone;
    }

    @Override
    public void visit(BLangTransaction source) {

        BLangTransaction clone = new BLangTransaction();
        clone.transactionBody = clone(source.transactionBody);
        clone.onRetryBody = clone(source.onRetryBody);
        clone.committedBody = clone(source.committedBody);
        clone.abortedBody = clone(source.abortedBody);
        clone.retryCount = clone(source.retryCount);

        result = clone;
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        // Ignore.
    }

    @Override
    public void visit(BLangTupleDestructure source) {

        BLangTupleDestructure clone = new BLangTupleDestructure();
        clone.varRef = clone(source.varRef);
        clone.expr = clone(source.expr);

        result = clone;
    }

    @Override
    public void visit(BLangRecordDestructure source) {

        BLangRecordDestructure clone = new BLangRecordDestructure();
        clone.varRef = clone(source.varRef);
        clone.expr = clone(source.expr);
        clone.declaredWithVar = source.declaredWithVar;

        result = clone;
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {

        BLangErrorDestructure clone = new BLangErrorDestructure();
        clone.varRef = clone(stmt.varRef);
        clone.expr = clone(stmt.expr);

        result = clone;
    }

    @Override
    public void visit(BLangCatch catchNode) {
        // Ignore
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {

        BLangForkJoin clone = new BLangForkJoin();
        clone.workers = cloneList(forkJoin.workers);

        result = clone;
    }

    @Override
    public void visit(BLangOrderBy orderBy) {

        BLangOrderBy clone = new BLangOrderBy();
        clone.varRefs = cloneList(orderBy.varRefs);

        result = clone;
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {

        BLangOrderByVariable clone = new BLangOrderByVariable();
        clone.varRef = (ExpressionNode) clone((BLangNode) orderByVariable.varRef);
        clone.orderByType = orderByVariable.orderByType;

        result = clone;
    }

    @Override
    public void visit(BLangLimit limit) {

        result = limit;
    }

    @Override
    public void visit(BLangGroupBy groupBy) {

        BLangGroupBy clone = new BLangGroupBy();
        clone.varRefs = cloneList(groupBy.varRefs);

        result = clone;
    }

    @Override
    public void visit(BLangHaving having) {

        BLangHaving clone = new BLangHaving();
        clone.expression = clone(having.expression);

        result = clone;
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {

        BLangSelectExpression clone = new BLangSelectExpression();
        clone.identifier = selectExpression.identifier;
        clone.expression = clone(selectExpression.expression);

        result = clone;
    }

    @Override
    public void visit(BLangSelectClause selectClause) {

        BLangSelectClause clone = new BLangSelectClause();
        for (SelectExpressionNode e : selectClause.selectExpressions) {
            clone.selectExpressions.add(clone((BLangSelectExpression) e));
        }
        clone.isSelectAll = selectClause.isSelectAll;
        clone.groupBy = clone(selectClause.groupBy);
        clone.having = clone(selectClause.having);

        result = clone;
    }

    @Override
    public void visit(BLangWhere whereClause) {

        BLangWhere clone = new BLangWhere();
        clone.expression = clone(whereClause.expression);

        result = clone;
    }

    @Override
    public void visit(BLangStreamingInput source) {

        BLangStreamingInput clone = new BLangStreamingInput();
        clone.beforeStreamingCondition = clone(source.beforeStreamingCondition);
        clone.windowClause = clone(source.windowClause);
        clone.afterStreamingCondition = clone(source.afterStreamingCondition);
        clone.streamReference = clone(source.streamReference);
        clone.alias = source.alias;
        clone.isWindowTraversedAfterWhere = source.isWindowTraversedAfterWhere;
        for (ExpressionNode e : source.preInvocations) {
            clone.preInvocations.add((ExpressionNode) clone((BLangNode) e));
        }
        for (ExpressionNode e : source.postInvocations) {
            clone.postInvocations.add((ExpressionNode) clone((BLangNode) e));
        }

        result = clone;
    }

    @Override
    public void visit(BLangJoinStreamingInput source) {

        BLangJoinStreamingInput clone = new BLangJoinStreamingInput();
        clone.streamingInput = clone(source.streamingInput);
        clone.onExpression = clone(source.onExpression);
        clone.joinType = source.joinType;
        clone.isUnidirectionalBeforeJoin = source.isUnidirectionalBeforeJoin;
        clone.isUnidirectionalAfterJoin = source.isUnidirectionalAfterJoin;

        result = clone;
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {

        BLangTableQuery clone = new BLangTableQuery();
        clone.streamingInput = clone(tableQuery.streamingInput);
        clone.joinStreamingInput = clone(tableQuery.joinStreamingInput);
        clone.selectClauseNode = clone(tableQuery.selectClauseNode);
        clone.orderByNode = clone(tableQuery.orderByNode);
        clone.limitNode = clone(tableQuery.limitNode);

        result = clone;
    }

    @Override
    public void visit(BLangStreamAction streamAction) {

        BLangStreamAction clone = new BLangStreamAction();
        clone.lambdaFunction = clone(streamAction.lambdaFunction);

        result = clone;
    }

    @Override
    public void visit(BLangFunctionClause functionClause) {

        BLangFunctionClause clone = new BLangFunctionClause();
        clone.functionInvocation = clone(functionClause.functionInvocation);

        result = clone;
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {

        BLangSetAssignment clone = new BLangSetAssignment();
        clone.variableReferenceNode = clone(setAssignmentClause.variableReferenceNode);
        clone.expressionNode = clone(setAssignmentClause.expressionNode);

        result = clone;
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput source) {

        BLangPatternStreamingEdgeInput clone = new BLangPatternStreamingEdgeInput();
        clone.streamRef = clone(source.streamRef);
        clone.alias = source.alias;
        clone.expressionNode = clone(source.expressionNode);
        clone.whereNode = clone(source.whereNode);

        result = clone;
    }

    @Override
    public void visit(BLangWindow windowClause) {

        BLangWindow clone = new BLangWindow();
        clone.functionInvocation = clone(windowClause.functionInvocation);

        result = clone;
    }

    @Override
    public void visit(BLangPatternStreamingInput source) {

        BLangPatternStreamingInput clone = new BLangPatternStreamingInput();
        clone.patternStreamingInput = clone(source.patternStreamingInput);

        for (PatternStreamingEdgeInputNode node : source.patternStreamingEdgeInputNodeList) {
            clone.patternStreamingEdgeInputNodeList.add(clone((BLangPatternStreamingEdgeInput) node));
        }

        clone.isFollowedBy = source.isFollowedBy;
        clone.isEnclosedInParenthesis = source.isEnclosedInParenthesis;
        clone.isNotWithFor = source.isNotWithFor;
        clone.isNotWithAnd = source.isNotWithAnd;
        clone.isAndOnly = source.isAndOnly;
        clone.isOrOnly = source.isOrOnly;
        clone.isCommaSeparated = source.isCommaSeparated;
        clone.timeScale = source.timeScale;
        clone.timeDurationValue = source.timeDurationValue;

        result = clone;
    }

    @Override
    public void visit(BLangWorkerSend source) {

        BLangWorkerSend clone = new BLangWorkerSend();
        clone.expr = clone(source.expr);
        clone.workerIdentifier = source.workerIdentifier;
        clone.keyExpr = clone(source.keyExpr);

        result = clone;
    }

    @Override
    public void visit(BLangWorkerReceive source) {

        BLangWorkerReceive clone = new BLangWorkerReceive();

        clone.workerIdentifier = source.workerIdentifier;
        clone.keyExpr = clone(source.keyExpr);
        clone.isChannel = source.isChannel;

        result = clone;
    }

    @Override
    public void visit(BLangForever foreverStatement) {

        BLangForever clone = new BLangForever();
        for (StreamingQueryStatementNode node : foreverStatement.streamingQueryStatementNodeList) {
            clone.addStreamingQueryStatement(clone((BLangStreamingQueryStatement) node));
        }
        clone.params = cloneList(foreverStatement.params);

        result = clone;
    }

    @Override
    public void visit(BLangLiteral source) {

        BLangLiteral clone = new BLangLiteral();
        cloneBLangLiteral(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangNumericLiteral source) {

        BLangNumericLiteral clone = new BLangNumericLiteral();
        cloneBLangLiteral(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangTableLiteral source) {

        BLangTableLiteral clone = new BLangTableLiteral();
        clone.columns.addAll(source.columns);
        clone.tableDataRows = cloneList(source.tableDataRows);
        clone.indexColumnsArrayLiteral = clone(source.indexColumnsArrayLiteral);
        clone.keyColumnsArrayLiteral = clone(source.keyColumnsArrayLiteral);

        result = clone;
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {

        BLangRecordLiteral clone = new BLangRecordLiteral();
        clone.keyValuePairs = cloneList(recordLiteral.keyValuePairs);

        result = clone;
    }

    @Override
    public void visit(BLangTupleVarRef source) {

        BLangTupleVarRef clone = new BLangTupleVarRef();
        clone.pkgAlias = source.pkgAlias;
        clone.expressions = cloneList(source.expressions);
        clone.restParam = (ExpressionNode) clone((BLangNode) source.restParam);

        result = clone;
    }

    @Override
    public void visit(BLangRecordVarRef source) {

        BLangRecordVarRef clone = new BLangRecordVarRef();
        clone.pkgAlias = source.pkgAlias;
        for (BLangRecordVarRefKeyValue field : source.recordRefFields) {
            BLangRecordVarRefKeyValue keyValue = new BLangRecordVarRefKeyValue();
            keyValue.variableName = field.variableName;
            keyValue.variableReference = clone(field.variableReference);
            clone.recordRefFields.add(keyValue);
        }
        clone.restParam = (ExpressionNode) clone((BLangNode) source.restParam);

        result = clone;
    }

    @Override
    public void visit(BLangErrorVarRef source) {

        BLangErrorVarRef clone = new BLangErrorVarRef();
        clone.pkgAlias = source.pkgAlias;
        clone.reason = clone(source.reason);
        clone.detail = cloneList(source.detail);
        clone.restVar = clone(source.restVar);
        clone.typeNode = clone(source.typeNode);

        result = clone;
    }

    @Override
    public void visit(BLangSimpleVarRef source) {

        BLangSimpleVarRef clone = new BLangSimpleVarRef();
        clone.pkgAlias = source.pkgAlias;
        clone.variableName = source.variableName;

        result = clone;
    }

    @Override
    public void visit(BLangFieldBasedAccess source) {

        BLangFieldBasedAccess clone = new BLangFieldBasedAccess();
        clone.field = source.field;
        clone.fieldKind = source.fieldKind;
        cloneBLangAccessExpression(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangIndexBasedAccess source) {

        BLangIndexBasedAccess clone = new BLangIndexBasedAccess();
        cloneBLangIndexBasedAccess(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangInvocation source) {

        BLangInvocation clone = new BLangInvocation();
        clone.pkgAlias = source.pkgAlias;
        clone.name = source.name;
        clone.argExprs = cloneList(source.argExprs);
        clone.functionPointerInvocation = source.functionPointerInvocation;
        clone.actionInvocation = source.actionInvocation;
        clone.langLibInvocation = source.langLibInvocation;
        clone.async = source.async;
        clone.builtinMethodInvocation = source.builtinMethodInvocation;
        clone.builtInMethod = source.builtInMethod;
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.annAttachments = cloneList(source.annAttachments);

        cloneBLangAccessExpression(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangTypeInit source) {

        BLangTypeInit clone = new BLangTypeInit();
        clone.userDefinedType = clone(source.userDefinedType);
        clone.argsExpr = cloneList(source.argsExpr);
        clone.initInvocation = clone(source.initInvocation);

        result = clone;
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {

        // Ignore
    }

    @Override
    public void visit(BLangInvocation.BLangBuiltInMethodInvocation builtInMethodInvocation) {

        // Ignore
    }

    @Override
    public void visit(BLangTernaryExpr source) {

        BLangTernaryExpr clone = new BLangTernaryExpr();
        clone.expr = clone(source.expr);
        clone.thenExpr = clone(source.thenExpr);
        clone.elseExpr = clone(source.elseExpr);

        result = clone;
    }

    @Override
    public void visit(BLangWaitExpr source) {

        BLangWaitExpr clone = new BLangWaitExpr();
        clone.exprList = cloneList(source.exprList);

        result = clone;
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {

        BLangTrapExpr clone = new BLangTrapExpr();
        clone.expr = clone(trapExpr.expr);

        result = clone;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {

        BLangBinaryExpr clone = new BLangBinaryExpr();
        clone.lhsExpr = clone(binaryExpr.lhsExpr);
        clone.rhsExpr = clone(binaryExpr.rhsExpr);
        clone.opKind = binaryExpr.opKind;
        clone.opSymbol = binaryExpr.opSymbol;

        result = clone;
    }

    @Override
    public void visit(BLangElvisExpr source) {

        BLangElvisExpr clone = new BLangElvisExpr();
        clone.lhsExpr = clone(source.lhsExpr);
        clone.rhsExpr = clone(source.rhsExpr);

        result = clone;
    }

    @Override
    public void visit(BLangGroupExpr source) {

        BLangGroupExpr clone = new BLangGroupExpr();
        clone.expression = clone(source.expression);
        clone.isTypedescExpr = source.isTypedescExpr;
        clone.typedescType = source.typedescType;

        result = clone;
    }

    @Override
    public void visit(BLangListConstructorExpr source) {

        BLangListConstructorExpr clone = new BLangListConstructorExpr();
        clone.exprs = cloneList(source.exprs);
        clone.isTypedescExpr = source.isTypedescExpr;
        clone.typedescType = source.typedescType;

        result = clone;
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
        clone.expr = clone(source.expr);
        clone.operator = source.operator;

        result = clone;
    }

    @Override
    public void visit(BLangTypedescExpr source) {

        BLangTypedescExpr clone = new BLangTypedescExpr();
        clone.typeNode = clone(source.typeNode);
        clone.resolvedType = source.resolvedType;

        result = clone;
    }

    @Override
    public void visit(BLangTypeConversionExpr source) {

        BLangTypeConversionExpr clone = new BLangTypeConversionExpr();
        clone.expr = clone(source.expr);
        clone.typeNode = clone(source.typeNode);
        clone.targetType = source.targetType;
        clone.annAttachments = cloneList(source.annAttachments);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        clone.checkTypes = source.checkTypes;

        result = clone;
    }

    @Override
    public void visit(BLangXMLQName source) {

        BLangXMLQName clone = new BLangXMLQName();
        clone.localname = source.localname;
        clone.prefix = source.prefix;
        clone.namespaceURI = source.namespaceURI;
        clone.isUsedInXML = source.isUsedInXML;

        result = clone;
    }

    @Override
    public void visit(BLangXMLAttribute source) {

        BLangXMLAttribute clone = new BLangXMLAttribute();
        clone.name = clone(source.name);
        ;
        clone.value = clone(source.value);
        clone.isNamespaceDeclr = source.isNamespaceDeclr;

        result = clone;
    }

    @Override
    public void visit(BLangXMLElementLiteral source) {

        BLangXMLElementLiteral clone = new BLangXMLElementLiteral();
        clone.startTagName = clone(source.startTagName);
        clone.endTagName = clone(source.endTagName);
        clone.attributes = cloneList(source.attributes);
        clone.children = cloneList(source.children);

        clone.inlineNamespaces = cloneList(source.inlineNamespaces);
        clone.isRoot = source.isRoot;

        result = clone;
    }

    @Override
    public void visit(BLangXMLTextLiteral source) {

        BLangXMLTextLiteral clone = new BLangXMLTextLiteral();
        clone.textFragments = cloneList(source.textFragments);
        clone.concatExpr = clone(source.concatExpr);

        result = clone;
    }

    @Override
    public void visit(BLangXMLCommentLiteral source) {

        BLangXMLCommentLiteral clone = new BLangXMLCommentLiteral();
        clone.textFragments = cloneList(source.textFragments);
        clone.concatExpr = clone(source.concatExpr);

        result = clone;
    }

    @Override
    public void visit(BLangXMLProcInsLiteral source) {

        BLangXMLProcInsLiteral clone = new BLangXMLProcInsLiteral();
        clone.target = clone(source.target);
        clone.dataFragments = cloneList(source.dataFragments);
        clone.dataConcatExpr = clone(source.dataConcatExpr);

        result = clone;
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {

        BLangXMLQuotedString clone = new BLangXMLQuotedString();
        clone.textFragments = cloneList(xmlQuotedString.textFragments);
        clone.quoteType = xmlQuotedString.quoteType;
        clone.concatExpr = clone(xmlQuotedString.concatExpr);

        result = clone;
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {

        BLangStringTemplateLiteral clone = new BLangStringTemplateLiteral();
        clone.exprs = cloneList(stringTemplateLiteral.exprs);

        result = clone;
    }

    @Override
    public void visit(BLangLambdaFunction lambdaFunction) {

        BLangLambdaFunction clone = new BLangLambdaFunction();
        clone.function = clone(lambdaFunction.function);

        result = clone;
    }

    @Override
    public void visit(BLangArrowFunction source) {

        BLangArrowFunction clone = new BLangArrowFunction();
        clone.params = cloneList(source.params);
        clone.expression = clone(source.expression);
        clone.funcType = source.funcType;
        clone.functionName = source.functionName;

        result = clone;
    }

    @Override
    public void visit(BLangXMLAttributeAccess source) {

        BLangXMLAttributeAccess clone = new BLangXMLAttributeAccess();
        cloneBLangIndexBasedAccess(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangIntRangeExpression source) {

        BLangIntRangeExpression clone = new BLangIntRangeExpression();
        clone.includeStart = source.includeStart;
        clone.includeEnd = source.includeEnd;
        clone.startExpr = clone(source.startExpr);
        clone.endExpr = clone(source.endExpr);

        result = clone;
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {

        BLangTableQueryExpression clone = new BLangTableQueryExpression();
        clone.tableQuery = clone((BLangTableQuery) tableQueryExpression.tableQuery);

        result = clone;
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {

        BLangRestArgsExpression clone = new BLangRestArgsExpression();
        clone.expr = clone(bLangVarArgsExpression.expr);

        result = clone;
    }

    @Override
    public void visit(BLangNamedArgsExpression source) {

        BLangNamedArgsExpression clone = new BLangNamedArgsExpression();
        clone.name = source.name;
        clone.expr = clone(source.expr);

        result = clone;
    }

    @Override
    public void visit(BLangStreamingQueryStatement source) {

        BLangStreamingQueryStatement clone = new BLangStreamingQueryStatement();
        clone.streamingInput = clone((BLangStreamingInput) source.streamingInput);
        clone.joinStreamingInput = clone((BLangJoinStreamingInput) source.joinStreamingInput);
        clone.patternClause = clone((BLangPatternClause) source.patternClause);
        clone.selectClauseNode = clone((BLangSelectClause) source.selectClauseNode);
        clone.orderByNode = clone((BLangOrderBy) source.orderByNode);
        clone.streamActionNode = clone((BLangStreamAction) source.streamActionNode);
        clone.outputRateLimitNode = clone((BLangOutputRateLimit) source.outputRateLimitNode);

        result = clone;
    }

    @Override
    public void visit(BLangWithinClause source) {

        BLangWithinClause clone = new BLangWithinClause();
        clone.timeScale = source.timeScale;
        clone.timeDurationValue = source.timeDurationValue;

        result = clone;
    }

    @Override
    public void visit(BLangOutputRateLimit source) {

        BLangOutputRateLimit clone = new BLangOutputRateLimit();
        clone.outputRateType = source.outputRateType;
        clone.timeScale = source.timeScale;
        clone.rateLimitValue = source.rateLimitValue;
        clone.isSnapshot = source.isSnapshot;

        result = clone;
    }

    @Override
    public void visit(BLangPatternClause source) {

        BLangPatternClause clone = new BLangPatternClause();
        clone.patternStreamingInput = clone((BLangPatternStreamingInput) source.patternStreamingInput);
        clone.forAllEvents = source.forAllEvents;
        clone.withinClause = clone((BLangWithinClause) source.withinClause);

        result = clone;
    }

    @Override
    public void visit(BLangIsAssignableExpr source) {

        BLangIsAssignableExpr clone = new BLangIsAssignableExpr();
        clone.lhsExpr = clone(source.lhsExpr);
        clone.targetType = source.targetType;
        clone.typeNode = clone(source.typeNode);

        result = clone;
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
    public void visit(BLangCheckedExpr checkedExpr) {

        BLangCheckedExpr clone = new BLangCheckedExpr();
        clone.expr = clone(checkedExpr.expr);

        result = clone;
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {

        BLangCheckPanickedExpr clone = new BLangCheckPanickedExpr();
        clone.expr = clone(checkPanickedExpr.expr);

        result = clone;
    }

    @Override
    public void visit(BLangServiceConstructorExpr source) {

        BLangServiceConstructorExpr clone = new BLangServiceConstructorExpr();
        clone.serviceNode = clone(source.serviceNode);

        result = clone;
    }

    @Override
    public void visit(BLangTypeTestExpr source) {

        BLangTypeTestExpr clone = new BLangTypeTestExpr();
        clone.expr = clone(source.expr);
        clone.typeNode = clone(source.typeNode);

        result = clone;
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
        clone.pkgAlias = source.pkgAlias;
        clone.annotationName = source.annotationName;
        cloneBLangAccessExpression(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangValueType source) {

        BLangValueType clone = new BLangValueType();
        clone.typeKind = source.typeKind;

        result = clone;
    }

    @Override
    public void visit(BLangArrayType arrayType) {

        BLangArrayType clone = new BLangArrayType();
        clone.elemtype = clone(arrayType.elemtype);
        clone.dimensions = arrayType.dimensions;
        clone.sizes = arrayType.sizes;

        result = clone;
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode source) {

        BLangBuiltInRefTypeNode clone = new BLangBuiltInRefTypeNode();
        clone.typeKind = source.typeKind;

        result = clone;
    }

    @Override
    public void visit(BLangConstrainedType source) {

        BLangConstrainedType clone = new BLangConstrainedType();
        clone.type = clone(source.type);
        clone.constraint = clone(source.constraint);

        result = clone;
    }

    @Override
    public void visit(BLangUserDefinedType source) {

        BLangUserDefinedType clone = new BLangUserDefinedType();
        clone.pkgAlias = source.pkgAlias;
        clone.typeName = source.typeName;
        clone.flagSet = cloneSet(source.flagSet, Flag.class);

        result = clone;
    }

    @Override
    public void visit(BLangFunctionTypeNode source) {

        BLangFunctionTypeNode clone = new BLangFunctionTypeNode();
        clone.params = cloneList(source.params);
        clone.returnTypeNode = clone(source.returnTypeNode);
        clone.returnsKeywordExists = source.returnsKeywordExists;

        result = clone;
    }

    @Override
    public void visit(BLangUnionTypeNode source) {

        BLangUnionTypeNode clone = new BLangUnionTypeNode();
        clone.memberTypeNodes = cloneList(source.memberTypeNodes);

        result = clone;
    }

    @Override
    public void visit(BLangObjectTypeNode source) {

        BLangObjectTypeNode clone = new BLangObjectTypeNode();
        clone.functions = cloneList(source.functions);
        clone.initFunction = clone(source.initFunction);
        clone.receiver = clone(source.receiver);
        clone.flagSet = cloneSet(source.flagSet, Flag.class);
        cloneBLangStructureTypeNode(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangRecordTypeNode source) {

        BLangRecordTypeNode clone = new BLangRecordTypeNode();
        clone.sealed = source.sealed;
        clone.restFieldType = clone(source.restFieldType);
        cloneBLangStructureTypeNode(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {

        BLangFiniteTypeNode clone = new BLangFiniteTypeNode();
        clone.valueSpace = cloneList(finiteTypeNode.valueSpace);

        result = clone;
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {

        BLangTupleTypeNode clone = new BLangTupleTypeNode();
        clone.memberTypeNodes = cloneList(tupleTypeNode.memberTypeNodes);
        clone.restParamType = clone(tupleTypeNode.restParamType);

        result = clone;
    }

    @Override
    public void visit(BLangErrorType errorType) {

        BLangErrorType clone = new BLangErrorType();
        clone.reasonType = clone(errorType.reasonType);
        clone.detailType = clone(errorType.detailType);

        result = clone;
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
    public void visit(BLangSimpleVarRef.BLangConstRef constRef) {

        // Ignore
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
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {

        // Ignore
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {

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
    public void visit(BLangRecordLiteral.BLangStreamLiteral streamLiteral) {

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
    public void visit(BLangStatementExpression bLangStatementExpression) {

        BLangStatementExpression clone = new BLangStatementExpression();
        clone.expr = clone(bLangStatementExpression.expr);
        clone.stmt = clone(bLangStatementExpression.stmt);

        result = clone;
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine source) {

        BLangMarkdownDocumentationLine clone = new BLangMarkdownDocumentationLine();
        clone.text = source.text;

        result = clone;
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation source) {

        BLangMarkdownParameterDocumentation clone = new BLangMarkdownParameterDocumentation();
        clone.parameterName = source.parameterName;
        clone.parameterDocumentationLines = source.parameterDocumentationLines;

        result = clone;
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation source) {

        BLangMarkdownReturnParameterDocumentation clone = new BLangMarkdownReturnParameterDocumentation();
        clone.returnParameterDocumentationLines = source.returnParameterDocumentationLines;
        clone.type = source.type;

        result = clone;
    }

    @Override
    public void visit(BLangMarkdownDocumentation source) {

        BLangMarkdownDocumentation clone = new BLangMarkdownDocumentation();
        clone.documentationLines.addAll(cloneList(source.documentationLines));
        clone.parameters.addAll(cloneList(source.parameters));
        clone.references.addAll(cloneList(source.references));
        clone.returnParameter = clone(source.returnParameter);

        result = clone;
    }

    @Override
    public void visit(BLangTupleVariable source) {

        BLangTupleVariable clone = new BLangTupleVariable();
        clone.memberVariables = cloneList(source.memberVariables);
        clone.restVariable = clone(source.restVariable);
        cloneBLangVariable(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {

        BLangTupleVariableDef clone = new BLangTupleVariableDef();
        clone.var = clone(bLangTupleVariableDef.var);

        result = clone;
    }

    @Override
    public void visit(BLangRecordVariable source) {

        BLangRecordVariable clone = new BLangRecordVariable();
        for (BLangRecordVariableKeyValue keyValue : source.variableList) {
            BLangRecordVariableKeyValue newKeyValue = new BLangRecordVariableKeyValue();
            newKeyValue.key = keyValue.key;
            newKeyValue.valueBindingPattern = clone(keyValue.valueBindingPattern);
            clone.variableList.add(newKeyValue);
        }
        clone.restParam = clone((BLangVariable) source.restParam);
        cloneBLangVariable(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {

        BLangRecordVariableDef clone = new BLangRecordVariableDef();
        clone.var = clone(bLangRecordVariableDef.var);

        result = clone;
    }

    @Override
    public void visit(BLangErrorVariable source) {

        BLangErrorVariable clone = new BLangErrorVariable();
        clone.reason = clone(source.reason);
        for (BLangErrorDetailEntry entry : source.detail) {
            clone.detail.add(new BLangErrorDetailEntry(entry.key, clone(entry.valueBindingPattern)));
        }
        clone.restDetail = clone(source.restDetail);
        clone.detailExpr = clone(source.detailExpr);
        clone.reasonVarPrefixAvailable = source.reasonVarPrefixAvailable;
        clone.reasonMatchConst = source.reasonMatchConst;
        clone.isInMatchStmt = source.isInMatchStmt;
        cloneBLangVariable(source, clone);

        result = clone;
    }

    @Override
    public void visit(BLangErrorVariableDef source) {

        BLangErrorVariableDef clone = new BLangErrorVariableDef();
        clone.errorVariable = clone(source.errorVariable);

        result = clone;
    }

    @Override
    public void visit(BLangWorkerFlushExpr source) {

        BLangWorkerFlushExpr clone = new BLangWorkerFlushExpr();
        clone.workerIdentifier = source.workerIdentifier;
        clone.workerIdentifierList.addAll(source.workerIdentifierList);

        result = clone;
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr source) {

        BLangWorkerSyncSendExpr clone = new BLangWorkerSyncSendExpr();
        clone.workerIdentifier = source.workerIdentifier;
        clone.expr = clone(source.expr);

        result = clone;
    }

    @Override
    public void visit(BLangWaitForAllExpr source) {

        BLangWaitForAllExpr clone = new BLangWaitForAllExpr();
        for (BLangWaitKeyValue keyValue : source.keyValuePairs) {
            BLangWaitKeyValue newKeyValue = new BLangWaitKeyValue();
            newKeyValue.pos = keyValue.pos;
            newKeyValue.addWS(keyValue.getWS());
            newKeyValue.key = keyValue.key;
            newKeyValue.keyExpr = clone(keyValue.keyExpr);
            newKeyValue.valueExpr = clone(keyValue.valueExpr);
            clone.keyValuePairs.add(newKeyValue);
        }

        result = clone;
    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation source) {

        BLangMarkdownReferenceDocumentation clone = new BLangMarkdownReferenceDocumentation();
        clone.qualifier = source.qualifier;
        clone.typeName = source.typeName;
        clone.identifier = source.identifier;
        clone.referenceName = source.referenceName;
        clone.kind = source.kind;
        clone.type = source.type;

        result = clone;
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {

        // Ignore
    }

    @Override
    public void visit(BLangRecordKeyValue source) {

        BLangRecordKeyValue clone = new BLangRecordKeyValue();
        clone.pos = source.pos;
        clone.addWS(source.getWS());

        BLangRecordKey newKey = new BLangRecordKey(clone(source.key.expr));
        newKey.computedKey = source.key.computedKey;
        clone.key = newKey;

        clone.valueExpr = clone(source.valueExpr);

        result = clone;
    }

    @Override
    public void visit(BLangWaitKeyValue source) {

        BLangWaitKeyValue clone = new BLangWaitKeyValue();
        clone.pos = source.pos;
        clone.addWS(source.getWS());
        clone.key = source.key;
        clone.valueExpr = clone(source.valueExpr);
        clone.keyExpr = clone(source.keyExpr);

        result = clone;
    }
}
