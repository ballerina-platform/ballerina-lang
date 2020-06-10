/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.BlockFunctionBodyNode;
import org.ballerinalang.model.tree.BlockNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.DocumentationReferenceType;
import org.ballerinalang.model.tree.ExternalFunctionBodyNode;
import org.ballerinalang.model.tree.FunctionBodyNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.MarkdownDocumentationNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TableKeySpecifierNode;
import org.ballerinalang.model.tree.TableKeyTypeConstraintNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.TableMultiKeyExpressionNode;
import org.ballerinalang.model.tree.expressions.XMLAttributeNode;
import org.ballerinalang.model.tree.expressions.XMLElementFilter;
import org.ballerinalang.model.tree.expressions.XMLLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLNavigationAccess;
import org.ballerinalang.model.tree.statements.BlockStatementNode;
import org.ballerinalang.model.tree.statements.ForkJoinNode;
import org.ballerinalang.model.tree.statements.IfNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.statements.TransactionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable.BLangRecordVariableKeyValue;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangInputClause;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
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
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.QuoteType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.util.Constants.STRING_TYPE;
import static org.wso2.ballerinalang.compiler.util.Constants.WORKER_LAMBDA_VAR_PREFIX;

/**
 * This class builds the package AST of a Ballerina source file.
 *
 * @since 0.94
 */
public class BLangPackageBuilder {

    private CompilationUnitNode compUnit;

    private Stack<BLangNameReference> nameReferenceStack = new Stack<>();

    private Stack<TypeNode> typeNodeStack = new Stack<>();

    private Stack<BlockNode> blockNodeStack = new Stack<>();

    private Stack<BLangVariable> varStack = new Stack<>();

    private Stack<List<BLangVariable>> varListStack = new Stack<>();

    private Stack<List<BLangLetVariable>> letVarListStack = new Stack<>();

    private Stack<List<BLangRecordVariableKeyValue>> recordVarListStack = new Stack<>();

    private Stack<List<BLangRecordVarRefKeyValue>> recordVarRefListStack = new Stack<>();

    private Stack<InvokableNode> invokableNodeStack = new Stack<>();

    private Stack<FunctionBodyNode> funcBodyNodeStack = new Stack<>();

    private Stack<ExpressionNode> exprNodeStack = new Stack<>();

    private Stack<List<ExpressionNode>> exprNodeListStack = new Stack<>();

    private Stack<Set<Whitespace>> commaWsStack = new Stack<>();

    private Stack<Set<Whitespace>> invocationWsStack = new Stack<>();

    private Stack<BLangRecordLiteral> recordLiteralNodes = new Stack<>();

    private Stack<BLangWaitForAllExpr> waitCollectionStack = new Stack<>();

    private Stack<BLangTryCatchFinally> tryCatchFinallyNodesStack = new Stack<>();

    private Stack<AnnotationNode> annotationStack = new Stack<>();

    private Stack<MarkdownDocumentationNode> markdownDocumentationStack = new Stack<>();

    private Stack<AnnotationAttachmentNode> annotAttachmentStack = new Stack<>();

    private Stack<IfNode> ifElseStatementStack = new Stack<>();

    private Stack<BLangNode> queryClauseStack = new Stack<>();

    private Stack<TransactionNode> transactionNodeStack = new Stack<>();

    private Stack<ForkJoinNode> forkJoinNodesStack = new Stack<>();

    private Stack<ServiceNode> serviceNodeStack = new Stack<>();

    private Stack<XMLAttributeNode> xmlAttributeNodeStack = new Stack<>();

    private Stack<AttachPoint> attachPointStack = new Stack<>();

    private Set<BLangImportPackage> imports = new HashSet<>();

    private Stack<SimpleVariableNode> restParamStack = new Stack<>();

    private Deque<BLangMatch> matchStmtStack;

    private Stack<XMLElementFilter> elementFilterStack = new Stack<>();

    private Stack<Set<Whitespace>> operatorWs = new Stack<>();

    private Stack<Set<Whitespace>> objectFieldBlockWs = new Stack<>();

    private Stack<Set<Whitespace>> finiteTypeWsStack = new Stack<>();

    private Stack<Set<Whitespace>> workerDefinitionWSStack = new Stack<>();

    private Stack<Set<Whitespace>> bindingPatternIdentifierWS = new Stack<>();
    private Stack<Set<Whitespace>> letBindingPatternIdentifierWS = new Stack<>();
    private Stack<Set<Whitespace>> queryBindingPatternIdentifierWS = new Stack<>();

    private Stack<Set<Whitespace>> errorMatchPatternWS = new Stack<>();
    private Stack<Set<Whitespace>> simpleMatchPatternWS = new Stack<>();
    private Stack<Set<Whitespace>> recordKeyWS = new Stack<>();
    private Stack<Set<Whitespace>> inferParamListWSStack = new Stack<>();
    private Stack<Set<Whitespace>> invocationRuleWS = new Stack<>();
    private Stack<Set<Whitespace>> errorRestBindingPatternWS = new Stack<>();
    private Stack<Set<Whitespace>> restMatchPatternWS = new Stack<>();

    private Stack<TableKeySpecifierNode> tableKeySpecifierNodeStack = new Stack<>();
    private Stack<TableKeyTypeConstraintNode>  tableKeyTypeConstraintNodeStack = new Stack<>();
    private Stack<TableMultiKeyExpressionNode> tableMultiKeyExpressionNodeStack = new Stack<>();

    private long isInErrorType = 0;

    private boolean isInQuery = false;
    private boolean isInOnCondition = false;

    private BLangAnonymousModelHelper anonymousModelHelper;
    private CompilerOptions compilerOptions;
    private SymbolTable symTable;
    private BLangDiagnosticLogHelper dlog;

    private static final String IDENTIFIER_LITERAL_PREFIX = "'";

    public BLangPackageBuilder(CompilerContext context, CompilationUnitNode compUnit) {
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.compilerOptions = CompilerOptions.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.compUnit = compUnit;
    }

    void addAttachPoint(AttachPoint attachPoint, Set<Whitespace> ws) {
        attachPointStack.push(attachPoint);
        this.annotationStack.peek().addWS(ws);
    }

    void addValueType(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.addWS(ws);
        typeNode.pos = pos;
        typeNode.typeKind = (TreeUtils.stringToTypeKind(typeName.replaceAll("\\s+", "")));

        addType(typeNode);
    }

    void addUnionType(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangType rhsTypeNode = (BLangType) this.typeNodeStack.pop();
        BLangType lhsTypeNode = (BLangType) this.typeNodeStack.pop();
        addUnionType(lhsTypeNode, rhsTypeNode, pos, ws);
    }

    void addUnionType(BLangType lhsTypeNode, BLangType rhsTypeNode, DiagnosticPos pos, Set<Whitespace> ws) {
        BLangUnionTypeNode unionTypeNode;
        if (rhsTypeNode.getKind() == NodeKind.UNION_TYPE_NODE) {
            unionTypeNode = (BLangUnionTypeNode) rhsTypeNode;
            unionTypeNode.memberTypeNodes.add(0, lhsTypeNode);
        } else if (lhsTypeNode.getKind() == NodeKind.UNION_TYPE_NODE) {
            unionTypeNode = (BLangUnionTypeNode) lhsTypeNode;
            unionTypeNode.memberTypeNodes.add(rhsTypeNode);
        } else {
            unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
            unionTypeNode.memberTypeNodes.add(lhsTypeNode);
            unionTypeNode.memberTypeNodes.add(rhsTypeNode);
        }

        unionTypeNode.pos = pos;
        unionTypeNode.addWS(ws);
        this.typeNodeStack.push(unionTypeNode);
    }

    void addIntersectionType(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangType rhsTypeNode = (BLangType) this.typeNodeStack.pop();
        BLangType lhsTypeNode = (BLangType) this.typeNodeStack.pop();
        addIntersectionType(lhsTypeNode, rhsTypeNode, pos, ws);
    }

    private void addIntersectionType(BLangType lhsTypeNode, BLangType rhsTypeNode, DiagnosticPos pos,
                                     Set<Whitespace> ws) {
        BLangIntersectionTypeNode intersectionTypeNode;
        if (rhsTypeNode.getKind() == NodeKind.INTERSECTION_TYPE_NODE) {
            intersectionTypeNode = (BLangIntersectionTypeNode) rhsTypeNode;
            intersectionTypeNode.constituentTypeNodes.add(0, lhsTypeNode);
        } else if (lhsTypeNode.getKind() == NodeKind.INTERSECTION_TYPE_NODE) {
            intersectionTypeNode = (BLangIntersectionTypeNode) lhsTypeNode;
            intersectionTypeNode.constituentTypeNodes.add(rhsTypeNode);
        } else {
            intersectionTypeNode = (BLangIntersectionTypeNode) TreeBuilder.createIntersectionTypeNode();
            intersectionTypeNode.constituentTypeNodes.add(lhsTypeNode);
            intersectionTypeNode.constituentTypeNodes.add(rhsTypeNode);
        }

        intersectionTypeNode.pos = pos;
        intersectionTypeNode.addWS(ws);
        this.typeNodeStack.push(intersectionTypeNode);
    }

    void addTupleType(DiagnosticPos pos, Set<Whitespace> ws, int members, boolean hasRestParam) {
        BLangTupleTypeNode tupleTypeNode = (BLangTupleTypeNode) TreeBuilder.createTupleTypeNode();
        if (hasRestParam) {
            tupleTypeNode.restParamType = (BLangType) this.typeNodeStack.pop();
        }
        for (int i = 0; i < members; i++) {
            final BLangType member = (BLangType) this.typeNodeStack.pop();
            tupleTypeNode.memberTypeNodes.add(0, member);
        }
        tupleTypeNode.pos = pos;
        tupleTypeNode.addWS(ws);
        this.typeNodeStack.push(tupleTypeNode);
    }

    void addRecordType(DiagnosticPos pos, Set<Whitespace> ws, boolean isAnonymous, boolean hasRestField,
                       boolean isExclusiveTypeDesc) {
        // If there is an explicitly defined rest field, take it.
        BLangType restFieldType = null;
        if (hasRestField) {
            restFieldType = (BLangType) this.typeNodeStack.pop();
        }
        // Create an anonymous record and add it to the list of records in the current package.
        BLangRecordTypeNode recordTypeNode = populateRecordTypeNode(pos, ws, isAnonymous);
        recordTypeNode.sealed = isExclusiveTypeDesc && !hasRestField;
        recordTypeNode.restFieldType = restFieldType;

        if (!isAnonymous || isInLocalDefinition()) {
            addType(recordTypeNode);
            return;
        }
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName, null);
        typeDef.setName(anonTypeGenName);
        typeDef.flagSet.add(Flag.PUBLIC);
        typeDef.flagSet.add(Flag.ANONYMOUS);

        typeDef.typeNode = recordTypeNode;
        typeDef.pos = pos;
        this.compUnit.addTopLevelNode(typeDef);

        addType(createUserDefinedType(pos, ws, (BLangIdentifier) TreeBuilder.createIdentifierNode(), typeDef.name));
    }

    private BLangRecordTypeNode populateRecordTypeNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isAnonymous) {
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) typeNodeStack.pop();
        recordTypeNode.pos = pos;
        recordTypeNode.addWS(ws);
        recordTypeNode.isAnonymous = isAnonymous;
        recordTypeNode.isLocal = isInLocalDefinition();
        for (BLangVariable variableNode : this.varListStack.pop()) {
            recordTypeNode.addField((SimpleVariableNode) variableNode);
        }
        return recordTypeNode;
    }

    void addTableType(DiagnosticPos pos, Set<Whitespace> ws) {
        String tableTypeName = "table";
        Set<Whitespace> refTypeWS = removeNthFromLast(ws, 2);

        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(tableTypeName);
        refType.pos = pos;
        refType.addWS(refTypeWS);

        BLangTableTypeNode tableTypeNode = (BLangTableTypeNode) TreeBuilder.createTableTypeNode();
        tableTypeNode.pos = pos;
        tableTypeNode.addWS(ws);

        tableTypeNode.type = refType;
        tableTypeNode.constraint = (BLangType) typeNodeStack.pop();
        if (tableKeySpecifierNodeStack.size() > 0) {
            tableTypeNode.tableKeySpecifier =
                    (BLangTableKeySpecifier) tableKeySpecifierNodeStack.pop();
        } else if (tableKeyTypeConstraintNodeStack.size() > 0) {
            tableTypeNode.tableKeyTypeConstraint = (BLangTableKeyTypeConstraint) tableKeyTypeConstraintNodeStack.pop();
        }

        addType(tableTypeNode);
    }

    private boolean isInLocalDefinition() {
        // TODO: When supporting local defs for errors, need to get rid of the second condition
        return !this.blockNodeStack.isEmpty() && this.isInErrorType <= 0;
    }

    void addFieldVariable(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                          boolean exprAvailable, int annotCount, boolean isPrivate, boolean isOptional,
                          boolean markdownExists, boolean isReadonly) {
        BLangSimpleVariable field = addSimpleVar(pos, ws, identifier, identifierPos, exprAvailable, annotCount);

        if (!isPrivate) {
            field.flagSet.add(Flag.PUBLIC);
        }

        if (isOptional) {
            field.flagSet.add(Flag.OPTIONAL);
        } else if (!exprAvailable) {
            field.flagSet.add(Flag.REQUIRED);
        }

        if (isReadonly) {
            setReadOnlyIntersectionTypeNode(field);
        }

        if (markdownExists) {
            attachMarkdownDocumentations(field);
        }
    }

    void addObjectFieldVariable(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                                boolean exprAvailable, int annotCount, boolean isPrivate, boolean isPublic,
                                boolean markdownExists, boolean readonly) {
        BLangSimpleVariable field = addSimpleVar(pos, ws, identifier, identifierPos, exprAvailable, annotCount);

        if (isPublic) {
            field.flagSet.add(Flag.PUBLIC);
        } else if (isPrivate) {
            field.flagSet.add(Flag.PRIVATE);
        }

        if (readonly) {
            setReadOnlyIntersectionTypeNode(field);
        }

        if (markdownExists) {
            attachMarkdownDocumentations(field);
        }
    }

    void addArrayType(DiagnosticPos pos, Set<Whitespace> ws, int dimensions, int[] sizes) {
        BLangType eType = (BLangType) this.typeNodeStack.pop();
        BLangArrayType arrayTypeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        arrayTypeNode.addWS(ws);
        arrayTypeNode.pos = pos;
        arrayTypeNode.elemtype = eType;
        arrayTypeNode.dimensions = dimensions;
        arrayTypeNode.sizes = sizes;

        addType(arrayTypeNode);
    }

    void markTypeNodeAsNullable(Set<Whitespace> ws) {
        BLangType typeNode = (BLangType) this.typeNodeStack.pop();
        typeNode.addWS(ws);

        BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nilTypeNode.pos = typeNode.pos;
        nilTypeNode.typeKind = TypeKind.NIL;

        addUnionType(typeNode, nilTypeNode, typeNode.pos, ws);

        ((BLangUnionTypeNode) this.typeNodeStack.peek()).nullable = true;
    }

    void markTypeNodeAsGrouped(Set<Whitespace> ws) {
        BLangType typeNode = (BLangType) this.typeNodeStack.peek();
        typeNode.addWS(ws);
        typeNode.grouped = true;
    }

    void addUserDefineType(Set<Whitespace> ws, boolean isDistinct) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType userDefinedType = createUserDefinedType(nameReference.pos, ws,
                (BLangIdentifier) nameReference.pkgAlias, (BLangIdentifier) nameReference.name);
        userDefinedType.addWS(nameReference.ws);
        if (isDistinct) {
            userDefinedType.flagSet.add(Flag.DISTINCT);
        }
        addType(userDefinedType);
    }

    void addBuiltInReferenceType(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(typeName);
        refType.pos = pos;
        refType.addWS(ws);
        addType(refType);
    }

    void startErrorType() {
        this.isInErrorType++;
    }

    private void endErrorType() {
        this.isInErrorType--;
    }

    void addErrorType(DiagnosticPos pos, Set<Whitespace> ws, boolean detailsTypeExists, boolean isErrorTypeInfer,
                      boolean isAnonymous, boolean isDistinct) {
        BLangErrorType errorType = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        errorType.pos = pos;
        errorType.addWS(ws);

        if (isErrorTypeInfer) {
            errorType.inferErrorType = true;
        } else if (detailsTypeExists) {
            errorType.detailType = (BLangType) this.typeNodeStack.pop();
        }

        if (isDistinct) {
            errorType.flagSet.add(Flag.DISTINCT);
        }

        endErrorType();

        if (!isAnonymous) {
            addType(errorType);
            return;
        }

        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        // Generate a name for the anonymous error
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName, null);
        typeDef.setName(anonTypeGenName);
        typeDef.flagSet.add(Flag.PUBLIC);
        typeDef.flagSet.add(Flag.ANONYMOUS);
        if (isDistinct) {
            typeDef.flagSet.add(Flag.DISTINCT);
        }
        typeDef.typeNode = errorType;
        typeDef.pos = pos;
        this.compUnit.addTopLevelNode(typeDef);

        addType(createUserDefinedType(pos, ws, (BLangIdentifier) TreeBuilder.createIdentifierNode(), typeDef.name));
    }

    void addConstraintTypeWithTypeName(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
        Set<Whitespace> refTypeWS = removeNthFromLast(ws, 2);

        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(typeName);
        refType.pos = pos;
        refType.addWS(refTypeWS);

        BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
        constrainedType.type = refType;
        constrainedType.constraint = (BLangType) this.typeNodeStack.pop();
        constrainedType.pos = pos;
        constrainedType.addWS(ws);

        addType(constrainedType);
    }


    void addStreamTypeWithTypeName(DiagnosticPos pos, Set<Whitespace> ws, boolean hasConstraint, boolean hasError) {
        String streamTypeName = "stream";
        String anyTypeName = "any";
        BLangType constraint, error = null;
        if (hasError) {
            error = (BLangType) this.typeNodeStack.pop();
        }
        if (!hasConstraint) {
            addValueType(pos, ws, anyTypeName);
        }
        constraint = (BLangType) this.typeNodeStack.pop();
        Set<Whitespace> refTypeWS = removeNthFromLast(ws, 2);

        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(streamTypeName);
        refType.pos = pos;
        refType.addWS(refTypeWS);

        BLangStreamType streamType = (BLangStreamType) TreeBuilder.createStreamTypeNode();
        streamType.type = refType;
        streamType.constraint = constraint;
        streamType.error = error;
        streamType.pos = pos;
        streamType.addWS(ws);

        addType(streamType);
    }

    void addFunctionType(DiagnosticPos pos, Set<Whitespace> ws, boolean paramsAvail,
                         boolean restParamAvail, boolean retParamsAvail) {
        BLangFunctionTypeNode functionTypeNode = (BLangFunctionTypeNode) TreeBuilder.createFunctionTypeNode();
        functionTypeNode.pos = pos;
        functionTypeNode.returnsKeywordExists = true;

        if (retParamsAvail) {
            functionTypeNode.addWS(this.varStack.peek().getWS());
            functionTypeNode.returnTypeNode = this.varStack.pop().getTypeNode();
        } else {
            BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            nilTypeNode.pos = pos;
            nilTypeNode.typeKind = TypeKind.NIL;
            functionTypeNode.returnTypeNode = nilTypeNode;
        }

        if (paramsAvail) {
            functionTypeNode.addWS(commaWsStack.pop());
            functionTypeNode.params.addAll(this.varListStack.pop());
        } else if (restParamAvail) {
            // VarListStack pops out the empty list added to the var list stack if no non-rest params are available
            this.varListStack.pop();
        }

        if (restParamAvail) {
            functionTypeNode.restParam = (BLangSimpleVariable) this.restParamStack.pop();
        }
        functionTypeNode.flagSet.add(Flag.PUBLIC);

        functionTypeNode.addWS(ws);
        addType(functionTypeNode);
    }

    private void addType(TypeNode typeNode) {
        this.typeNodeStack.push(typeNode);
    }

    void addNameReference(DiagnosticPos currentPos, Set<Whitespace> ws, String pkgName, String name) {
        IdentifierNode pkgNameNode = createIdentifier(currentPos, pkgName, null);
        IdentifierNode nameNode = createIdentifier(currentPos, name, ws);
        nameReferenceStack.push(new BLangNameReference(currentPos, ws, pkgNameNode, nameNode));
    }

    void startVarList() {
        this.varListStack.push(new ArrayList<>());
    }

    void startLetVarList() {
        this.letVarListStack.push(new ArrayList<>());
    }

    void startFunctionDef(int annotCount, boolean isLambda) {
        FunctionNode functionNode = TreeBuilder.createFunctionNode();
        attachAnnotations(functionNode, annotCount, false);
        if (!isLambda) {
            attachMarkdownDocumentations(functionNode);
        }
        this.invokableNodeStack.push(functionNode);
    }

    void startObjectFunctionDef() {
        FunctionNode functionNode = TreeBuilder.createFunctionNode();
        this.invokableNodeStack.push(functionNode);
    }

    void startBlock() {
        this.blockNodeStack.push(TreeBuilder.createBlockNode());
    }

    void startBlockFunctionBody() {
        BlockFunctionBodyNode body = TreeBuilder.createBlockFunctionBodyNode();
        this.blockNodeStack.push(body);
        this.funcBodyNodeStack.push(body);
    }

    void startExprFunctionBody() {
        this.funcBodyNodeStack.push(TreeBuilder.createExprFunctionBodyNode());
    }

    void startExternFunctionBody() {
        this.funcBodyNodeStack.push(TreeBuilder.createExternFunctionBodyNode());
    }

    public BLangIdentifier createIdentifier(DiagnosticPos pos, String value) {
        return createIdentifier(pos, value, null);
    }

    private BLangIdentifier createIdentifier(DiagnosticPos pos, String value, Set<Whitespace> ws) {
        BLangIdentifier node = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        if (value == null) {
            return node;
        }

        if (value.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            if (!escapeQuotedIdentifier(value).matches("^[0-9a-zA-Z.]*$")) {
                dlog.error(pos, DiagnosticCode.IDENTIFIER_LITERAL_ONLY_SUPPORTS_ALPHANUMERICS);
            }
            String unescapedValue = StringEscapeUtils.unescapeJava(value);
            node.setValue(unescapedValue.substring(1));
            node.originalValue = value;
            node.setLiteral(true);
        } else {
            node.setValue(value);
            node.setLiteral(false);
        }
        node.pos = pos;
        if (ws != null) {
            node.addWS(ws);
        }
        return node;
    }

    BLangSimpleVariable addSimpleVar(DiagnosticPos pos,
                                     Set<Whitespace> ws,
                                     String identifier,
                                     DiagnosticPos identifierPos,
                                     boolean exprAvailable,
                                     int annotCount,
                                     boolean isPublic) {
        BLangSimpleVariable var = addSimpleVar(pos, ws, identifier, identifierPos, exprAvailable, annotCount);
        if (isPublic) {
            var.flagSet.add(Flag.PUBLIC);
        }
        return var;
    }

    BLangSimpleVariable addSimpleVar(DiagnosticPos pos,
                                     Set<Whitespace> ws,
                                     String identifier,
                                     DiagnosticPos identifierPos,
                                     boolean exprAvailable,
                                     int annotCount) {
        BLangSimpleVariable var = (BLangSimpleVariable) this.generateBasicVarNode(pos, ws, identifier, identifierPos,
                exprAvailable);
        attachAnnotations(var, annotCount, false);
        var.pos = pos;
        if (this.varListStack.empty()) {
            this.varStack.push(var);
        } else {
            this.varListStack.peek().add(var);
        }

        return var;
    }

    BLangVariable addBindingPatternMemberVariable(DiagnosticPos pos,
                                                  Set<Whitespace> ws,
                                                  String identifier,
                                                  DiagnosticPos identifierPos) {
        BLangSimpleVariable memberVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        memberVar.pos = pos;
        IdentifierNode name = this.createIdentifier(identifierPos, identifier, ws);
        ((BLangIdentifier) name).pos = identifierPos;
        memberVar.setName(name);
        memberVar.addWS(ws);
        this.varStack.push(memberVar);
        return memberVar;
    }

    void startErrorBindingNode() {
        BLangErrorVariable errorVariable = (BLangErrorVariable) TreeBuilder.createErrorVariableNode();
        this.varStack.push(errorVariable);
    }

    void endSimpleMatchPattern(Set<Whitespace> ws) {
        this.simpleMatchPatternWS.push(ws);
    }

    void endErrorMatchPattern(Set<Whitespace> ws, boolean isIndirectErrorMatchPatern) {
        if (isIndirectErrorMatchPatern) {
            BLangErrorVariable errorVariable = (BLangErrorVariable) this.varStack.peek();
            BLangUserDefinedType errorType = (BLangUserDefinedType) this.typeNodeStack.pop();
            errorVariable.typeNode = errorType;

            errorVariable.message = createIgnoreVar();
        }
        this.errorMatchPatternWS.push(ws);
        ((BLangErrorVariable) this.varStack.peek()).isInMatchStmt = true;
    }

    void addWSForErrorRestBinding(Set<Whitespace> ws) {
        this.errorRestBindingPatternWS.push(ws);
    }

    void addWSForRestMatchPattern(Set<Whitespace> ws) {
        this.restMatchPatternWS.push(ws);
    }

    void addErrorVariable(DiagnosticPos pos, Set<Whitespace> ws, boolean isUserDefinedType, String reason, String cause,
                          DiagnosticPos causePos, String restIdentifier, boolean reasonVar,
                          boolean constReasonMatchPattern, DiagnosticPos restParamPos) {
        BLangErrorVariable errorVariable = (BLangErrorVariable) varStack.peek();
        errorVariable.pos = pos;
        errorVariable.addWS(ws);

        if (isUserDefinedType) {
            errorVariable.typeNode = (BLangType) typeNodeStack.pop();
        }

        if (constReasonMatchPattern) {
            BLangLiteral reasonLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
            reasonLiteral.setValue(reason.substring(1, reason.length() - 1));
            errorVariable.reasonMatchConst = reasonLiteral;
            errorVariable.message = (BLangSimpleVariable)
                    generateBasicVarNodeWithoutType(pos, null, "$reason$", pos, false);
        } else {
            errorVariable.message = (BLangSimpleVariable)
                    generateBasicVarNodeWithoutType(pos, null, reason, pos, false);
        }

        if (this.simpleMatchPatternWS.size() > 0) {
            errorVariable.message.addWS(this.simpleMatchPatternWS.pop());
        }

        if (cause != null) {
            errorVariable.cause = (BLangSimpleVariable)
                    generateBasicVarNodeWithoutType(causePos, null, cause, causePos, false);
        }

        errorVariable.reasonVarPrefixAvailable = reasonVar;
        if (restIdentifier != null) {
            errorVariable.restDetail = (BLangSimpleVariable)
                    generateBasicVarNodeWithoutType(pos, null, restIdentifier, restParamPos, false);
            if (!this.errorRestBindingPatternWS.isEmpty()) {
                errorVariable.restDetail.addWS(this.errorRestBindingPatternWS.pop());
            }

            if (!this.restMatchPatternWS.isEmpty()) {
                errorVariable.restDetail.addWS(this.restMatchPatternWS.pop());
            }
        }
    }

    void addErrorVariableReference(DiagnosticPos pos, Set<Whitespace> ws,
                                   int numNamedArgs,
                                   boolean causeRefAvailable,
                                   boolean restPatternAvailable,
                                   boolean indirectErrorRefPattern) {
        BLangErrorVarRef errorVarRef = (BLangErrorVarRef) TreeBuilder.createErrorVariableReferenceNode();
        errorVarRef.pos = pos;
        errorVarRef.addWS(ws);

        if (restPatternAvailable) {
            BLangVariableReference restVarRef = (BLangVariableReference) this.exprNodeStack.pop();
            errorVarRef.restVar = restVarRef;
        }

        List<BLangNamedArgsExpression> namedArgs = new ArrayList<>();
        for (int i = 0; i < numNamedArgs; i++) {
            BLangNamedArgsExpression namedArg = (BLangNamedArgsExpression) this.exprNodeStack.pop();
            namedArgs.add(namedArg);
        }

        if (causeRefAvailable) {
            ExpressionNode causeRef = this.exprNodeStack.pop();
            errorVarRef.cause = (BLangVariableReference) causeRef;
        }

        ExpressionNode reasonRef = this.exprNodeStack.pop();
        errorVarRef.message = (BLangVariableReference) reasonRef;

        if (indirectErrorRefPattern) {
            errorVarRef.typeNode = (BLangType) this.typeNodeStack.pop();
        }

        Collections.reverse(namedArgs);
        errorVarRef.detail.addAll(namedArgs);

        this.exprNodeStack.push(errorVarRef);
    }

    private BLangSimpleVarRef createIgnoreVarRef() {
        BLangSimpleVarRef ignoreVarRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        BLangIdentifier ignore = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        ignore.value = Names.IGNORE.value;
        ignoreVarRef.variableName = ignore;
        return ignoreVarRef;
    }

    void addErrorDetailBinding(DiagnosticPos pos, Set<Whitespace> ws, String name, String bindingVarName) {
        BLangIdentifier bLangIdentifier = this.createIdentifier(pos, name, ws);
        bLangIdentifier.pos = pos;
        bLangIdentifier.addWS(ws);
        if (!this.varStack.empty()) {
            if (bindingVarName != null) {
                BLangErrorVariable errorVariable = (BLangErrorVariable) this.varStack.peek();
                BLangErrorVariable.BLangErrorDetailEntry detailEntry =
                        createErrorDetailEntry(pos, bindingVarName, bLangIdentifier);
                errorVariable.detail.add(detailEntry);
            } else if (this.varStack.size() == 1) {
                BLangVariable var = this.varStack.pop();
                BLangErrorVariable errorVariable = new BLangErrorVariable();
                errorVariable.message = createIgnoreVar();
                errorVariable.typeNode = (BLangType) typeNodeStack.pop();
                errorVariable.detail.add(new BLangErrorVariable.BLangErrorDetailEntry(bLangIdentifier, var));
                varStack.push(errorVariable);

            } else if (this.varStack.size() > 1) {
                BLangVariable var = this.varStack.pop();
                BLangVariable detailVar = this.varStack.peek();
                if (detailVar.getKind() == NodeKind.ERROR_VARIABLE) {
                    BLangErrorVariable errorVariable = (BLangErrorVariable) detailVar;
                    errorVariable.detail.add(new BLangErrorVariable.BLangErrorDetailEntry(bLangIdentifier, var));
                }
            }
        } else {
            BLangErrorVariable errorVariable = new BLangErrorVariable();
            errorVariable.typeNode = (BLangType) typeNodeStack.pop();

            // var stack is empty, on inner binding pattern available here such as NERR(foo={item1, itema2});
            BLangErrorVariable.BLangErrorDetailEntry detailEntry =
                    createErrorDetailEntry(pos, bindingVarName, bLangIdentifier);
            errorVariable.detail.add(detailEntry);
            varStack.push(errorVariable);
        }
    }

    private BLangSimpleVariable createIgnoreVar() {
        BLangSimpleVariable ignoredVar = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        BLangIdentifier ignore = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        ignore.value = Names.IGNORE.value;
        ignoredVar.name = ignore;
        return ignoredVar;
    }

    private BLangErrorVariable.BLangErrorDetailEntry createErrorDetailEntry(DiagnosticPos pos, String bindingVarName,
                                                                            BLangIdentifier bLangIdentifier) {
        BLangSimpleVariable simpleVariableNode = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        simpleVariableNode.name = this.createIdentifier(pos, bindingVarName, null);
        simpleVariableNode.name.pos = pos;
        simpleVariableNode.pos = pos;
        if (letVarListStack.isEmpty() && !this.bindingPatternIdentifierWS.isEmpty() && !isInQuery) {
            Set<Whitespace> ws = this.bindingPatternIdentifierWS.pop();
            simpleVariableNode.name.addWS(ws);
            simpleVariableNode.addWS(ws);
        } else if (!this.letBindingPatternIdentifierWS.isEmpty()) {
            Set<Whitespace> ws = this.letBindingPatternIdentifierWS.pop();
            simpleVariableNode.name.addWS(ws);
            simpleVariableNode.addWS(ws);
        } else if (!this.queryBindingPatternIdentifierWS.isEmpty()) {
            Set<Whitespace> ws = this.queryBindingPatternIdentifierWS.pop();
            simpleVariableNode.name.addWS(ws);
            simpleVariableNode.addWS(ws);
        }
        return new BLangErrorVariable.BLangErrorDetailEntry(bLangIdentifier, simpleVariableNode);
    }

    void addTupleVariable(DiagnosticPos pos, Set<Whitespace> ws, int members, boolean restBindingAvailable) {

        BLangTupleVariable tupleVariable = (BLangTupleVariable) TreeBuilder.createTupleVariableNode();
        tupleVariable.pos = pos;
        tupleVariable.addWS(ws);
        if (letVarListStack.isEmpty() && !this.bindingPatternIdentifierWS.isEmpty() && !isInQuery) {
            tupleVariable.addWS(this.bindingPatternIdentifierWS.pop());
        } else if (!this.letBindingPatternIdentifierWS.isEmpty()) {
            tupleVariable.addWS(this.letBindingPatternIdentifierWS.pop());
        } else if (!this.queryBindingPatternIdentifierWS.isEmpty()) {
            tupleVariable.addWS(this.queryBindingPatternIdentifierWS.pop());
        }

        if (restBindingAvailable) {
            tupleVariable.restVariable = this.varStack.pop();
        }
        for (int i = 0; i < members; i++) {
            final BLangVariable member = this.varStack.pop();
            tupleVariable.memberVariables.add(0, member);
        }
        this.varStack.push(tupleVariable);
    }

    void addTupleVariableReference(DiagnosticPos pos, Set<Whitespace> ws, int members, boolean restPatternAvailable) {
        BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) TreeBuilder.createTupleVariableReferenceNode();
        tupleVarRef.pos = pos;
        tupleVarRef.addWS(ws);
        if (restPatternAvailable) {
            tupleVarRef.restParam = this.exprNodeStack.pop();
        }
        for (int i = 0; i < members; i++) {
            final BLangExpression expr = (BLangExpression) this.exprNodeStack.pop();
            tupleVarRef.expressions.add(0, expr);
        }
        this.exprNodeStack.push(tupleVarRef);
    }

    void startRecordVariableList() {
        recordVarListStack.push(new ArrayList<>());
    }

    void startRecordVariableReferenceList() {
        recordVarRefListStack.push(new ArrayList<>());
    }

    void addRecordVariable(DiagnosticPos pos, Set<Whitespace> ws, boolean hasRestBindingPattern) {
        BLangRecordVariable recordVariable = (BLangRecordVariable) TreeBuilder.createRecordVariableNode();
        recordVariable.pos = pos;
        recordVariable.addWS(ws);
        recordVariable.variableList = this.recordVarListStack.pop();

        if (hasRestBindingPattern) {
            recordVariable.restParam = this.varStack.pop();
        }

        this.varStack.push(recordVariable);
    }

    void addRecordVariableReference(DiagnosticPos pos, Set<Whitespace> ws, boolean hasRestBindingPattern) {
        BLangRecordVarRef recordVarRef = (BLangRecordVarRef) TreeBuilder.createRecordVariableReferenceNode();
        recordVarRef.pos = pos;
        recordVarRef.addWS(ws);
        recordVarRef.recordRefFields = this.recordVarRefListStack.pop();

        if (hasRestBindingPattern) {
            recordVarRef.restParam = this.exprNodeStack.pop();
        }

        this.exprNodeStack.push(recordVarRef);
    }

    void addFieldBindingMemberVar(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                                  boolean bindingPattern) {
        BLangRecordVariableKeyValue recordKeyValue = new BLangRecordVariableKeyValue();
        recordKeyValue.key = this.createIdentifier(identifierPos, identifier);
        if (!bindingPattern) {
            addBindingPatternMemberVariable(pos, ws, identifier, identifierPos);
        }
        recordKeyValue.valueBindingPattern = this.varStack.pop();
        recordKeyValue.valueBindingPattern.addWS(ws);
        if (letVarListStack.isEmpty() && !this.bindingPatternIdentifierWS.isEmpty() && !isInQuery) {
            recordKeyValue.valueBindingPattern.addWS(this.bindingPatternIdentifierWS.pop());
        } else if (!this.letBindingPatternIdentifierWS.isEmpty()) {
            recordKeyValue.valueBindingPattern.addWS(this.letBindingPatternIdentifierWS.pop());
        } else if (!this.queryBindingPatternIdentifierWS.isEmpty()) {
            recordKeyValue.valueBindingPattern.addWS(this.queryBindingPatternIdentifierWS.pop());
        }
        this.recordVarListStack.peek().add(recordKeyValue);
    }

    void addFieldRefBindingMemberVar(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                     boolean bindingPattern) {

        BLangExpression expression;
        if (!bindingPattern) {
            addNameReference(pos, ws, null, identifier);
            createSimpleVariableReference(pos, ws);
        }
        expression = (BLangExpression) this.exprNodeStack.pop();

        BLangRecordVarRefKeyValue keyValue = new BLangRecordVarRefKeyValue();
        keyValue.variableName = createIdentifier(pos, identifier);
        keyValue.variableReference = expression;
        keyValue.variableReference.addWS(ws);
        this.recordVarRefListStack.peek().add(keyValue);
    }

    public BLangVariable addVarWithoutType(DiagnosticPos pos,
                                           Set<Whitespace> ws,
                                           String identifier,
                                           DiagnosticPos identifierPos,
                                           boolean exprAvailable,
                                           int annotCount) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNodeWithoutType(pos, ws, identifier, identifierPos,
                exprAvailable);
        attachAnnotations(var, annotCount, false);
        var.pos = pos;
        if (this.varListStack.empty()) {
            this.varStack.push(var);
        } else {
            this.varListStack.peek().add(var);
        }
        return var;
    }


    public void endFormalParameterList(Set<Whitespace> ws) {
        this.commaWsStack.push(ws);
    }

    void addReturnParam(DiagnosticPos pos,
                        Set<Whitespace> ws,
                        int annotCount) {
        BLangSimpleVariable var = (BLangSimpleVariable) this.generateBasicVarNode(pos, ws, null, null, false);
        attachAnnotations(var, annotCount, false);
        var.pos = pos;
        this.varStack.push(var);
    }

    void endFunctionSignature(DiagnosticPos pos,
                              Set<Whitespace> ws,
                              boolean paramsAvail,
                              boolean retParamsAvail,
                              boolean restParamAvail) {
        InvokableNode invNode = this.invokableNodeStack.peek();
        invNode.addWS(ws);
        BLangType returnTypeNode;
        if (retParamsAvail) {
            BLangSimpleVariable varNode = (BLangSimpleVariable) this.varStack.pop();
            returnTypeNode = varNode.getTypeNode();
            // set returns keyword to invocation node.
            invNode.addWS(varNode.getWS());
            varNode.getAnnotationAttachments().forEach(invNode::addReturnTypeAnnotationAttachment);
        } else {
            BLangValueType nillTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            nillTypeNode.pos = pos;
            nillTypeNode.typeKind = TypeKind.NIL;
            returnTypeNode = nillTypeNode;
        }
        invNode.setReturnTypeNode(returnTypeNode);

        if (paramsAvail) {
            List<BLangVariable> varList = this.varListStack.pop();
            for (BLangVariable variableNode : varList) {
                invNode.addParameter((SimpleVariableNode) variableNode);
            }

            if (restParamAvail) {
                invNode.setRestParameter(this.restParamStack.pop());
            }

            invNode.addWS(this.commaWsStack.pop());
        }
    }

    void startLambdaFunctionDef(PackageID pkgID) {
        // Passing zero for annotation count as Lambdas can't have annotations.
        startFunctionDef(0, true);
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.setName(createIdentifier(lambdaFunction.pos,
                anonymousModelHelper.getNextAnonymousFunctionKey(pkgID), null));
        lambdaFunction.addFlag(Flag.LAMBDA);
        lambdaFunction.addFlag(Flag.ANONYMOUS);
    }

    void addLambdaFunctionDef(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.pos = pos;
        // todo: verify are passing all correct positions and WS
        BLangLambdaFunction lambdaExpr = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaExpr.function = lambdaFunction;
        lambdaExpr.pos = pos;
        addExpressionNode(lambdaExpr);
        // TODO: is null correct here
        endFunctionDefinition(pos, ws, lambdaFunction.getName().value, pos, false, false, false, false, true);
    }

    void addArrowFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, PackageID pkgID) {
        BLangArrowFunction arrowFunctionNode = (BLangArrowFunction) TreeBuilder.createArrowFunctionNode();
        arrowFunctionNode.pos = pos;
        arrowFunctionNode.addWS(ws);

        if (!this.inferParamListWSStack.isEmpty()) {
            arrowFunctionNode.addWS(this.inferParamListWSStack.pop());
        }

        arrowFunctionNode.functionName = createIdentifier(pos, anonymousModelHelper.getNextAnonymousFunctionKey(pkgID),
                null);
        varListStack.pop().forEach(var -> arrowFunctionNode.params.add((BLangSimpleVariable) var));
        arrowFunctionNode.body = (BLangExprFunctionBody) this.funcBodyNodeStack.pop();
        addExpressionNode(arrowFunctionNode);
    }

    void addWSForInferParamList(Set<Whitespace> ws) {
        this.inferParamListWSStack.push(ws);
    }

    void markLastInvocationAsAsync(DiagnosticPos pos, int numAnnotations) {
        final ExpressionNode expressionNode = this.exprNodeStack.peek();
        if (expressionNode.getKind() == NodeKind.INVOCATION) {
            BLangInvocation.BLangActionInvocation invocation =
                    (BLangInvocation.BLangActionInvocation) this.exprNodeStack.peek();
            invocation.async = true;
            attachAnnotations(invocation, numAnnotations, false);
        } else {
            dlog.error(pos, DiagnosticCode.START_REQUIRE_INVOCATION);
        }
    }

    void addSimpleVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                       DiagnosticPos identifierPos, boolean isFinal, boolean isExpressionAvailable,
                                       boolean isDeclaredWithVar) {
        BLangSimpleVariableDef varDefNode = createSimpleVariableDef(pos, ws, identifier, identifierPos, isFinal,
                isExpressionAvailable, isDeclaredWithVar);
        if (letVarListStack.isEmpty() && !this.bindingPatternIdentifierWS.isEmpty() && !isInQuery) {
            varDefNode.addWS(this.bindingPatternIdentifierWS.pop());
        } else if (!this.letBindingPatternIdentifierWS.isEmpty()) {
            varDefNode.addWS(this.letBindingPatternIdentifierWS.pop());
        } else if (!this.queryBindingPatternIdentifierWS.isEmpty()) {
            varDefNode.addWS(this.queryBindingPatternIdentifierWS.pop());
        }
        addStmtToCurrentBlock(varDefNode);
    }

    void addSimpleLetVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                          DiagnosticPos identifierPos, boolean isExpressionAvailable,
                                          boolean isDeclaredWithVar, int numAnnotations) {
        BLangSimpleVariableDef varDefNode = createSimpleVariableDef(pos, ws, identifier, identifierPos,
                true, isExpressionAvailable, isDeclaredWithVar);
        if (letVarListStack.isEmpty() && !this.bindingPatternIdentifierWS.isEmpty() && !isInQuery) {
            varDefNode.addWS(this.bindingPatternIdentifierWS.pop());
        } else if (!this.letBindingPatternIdentifierWS.isEmpty()) {
            varDefNode.addWS(this.letBindingPatternIdentifierWS.pop());
        } else if (!this.queryBindingPatternIdentifierWS.isEmpty()) {
            varDefNode.addWS(this.queryBindingPatternIdentifierWS.pop());
        }
        attachAnnotations(varDefNode.var, numAnnotations, false);
        addLetVarDecl(varDefNode);
    }

    void addBindingPatternNameWhitespace(Set<Whitespace> ws) {
        if (letVarListStack.isEmpty() && !isInQuery) {
            this.bindingPatternIdentifierWS.push(ws);
        } else if (isInQuery) {
            this.queryBindingPatternIdentifierWS.push(ws);
        } else {
            this.letBindingPatternIdentifierWS.push(ws);
        }
    }

    private void addLetVarDecl(VariableDefinitionNode definitionNode) {
        BLangLetVariable letVariable = (BLangLetVariable) TreeBuilder.createLetVariableNode();
        letVariable.definitionNode = definitionNode;
        this.letVarListStack.peek().add(letVariable);
    }

    private BLangSimpleVariableDef createSimpleVariableDef(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                                           DiagnosticPos identifierPos, boolean isFinal,
                                                           boolean isExpressionAvailable, boolean isDeclaredWithVar) {
        BLangSimpleVariable var = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        BLangSimpleVariableDef varDefNode = (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        var.pos = pos;
        var.addWS(ws);
        var.setName(this.createIdentifier(identifierPos, identifier, ws));
        var.name.pos = identifierPos;

        if (isFinal) {
            markVariableAsFinal(var);
        }
        if (isDeclaredWithVar) {
            var.isDeclaredWithVar = true;
        } else {
            var.setTypeNode(this.typeNodeStack.pop());
            if (var.getTypeNode().getKind() == NodeKind.ERROR_TYPE) {
                var.isDeclaredWithVar = ((BLangErrorType) var.typeNode).inferErrorType;
            }
        }
        if (isExpressionAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }

        varDefNode.pos = pos;
        varDefNode.setVariable(var);
        varDefNode.addWS(ws);
        return varDefNode;
    }

    void addTupleVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean isFinal,
                                      boolean isDeclaredWithVar) {
        BLangTupleVariableDef varDefNode = createTupleVariableDef(pos, ws, isFinal, true, isDeclaredWithVar);
        addStmtToCurrentBlock(varDefNode);
    }

    void addTupleVariableLetDefStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean isDeclaredWithVar,
                                         int numAnnotations) {
        BLangTupleVariableDef varDefNode = createTupleVariableDef(pos, ws, true, true, isDeclaredWithVar);
        attachAnnotations(varDefNode.var, numAnnotations, false);
        addLetVarDecl(varDefNode);
    }

    void addErrorVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean isFinal,
                                      boolean isDeclaredWithVar) {
        BLangErrorVariableDef varDefNode = createErrorVariableDef(pos, ws, isFinal, true, isDeclaredWithVar);
        addStmtToCurrentBlock(varDefNode);
    }

    void addErrorVariableLetDefStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean isDeclaredWithVar,
                                         int numAnnotations) {
        BLangErrorVariableDef varDefNode = createErrorVariableDef(pos, ws, true, true, isDeclaredWithVar);
        attachAnnotations(varDefNode.errorVariable, numAnnotations, false);
        addLetVarDecl(varDefNode);
    }

    private BLangTupleVariableDef createTupleVariableDef(DiagnosticPos pos, Set<Whitespace> ws, boolean isFinal,
                                                         boolean isExpressionAvailable, boolean isDeclaredWithVar) {
        BLangTupleVariable var = (BLangTupleVariable) this.varStack.pop();
        if (isFinal) {
            markVariableAsFinal(var);
        }
        BLangTupleVariableDef varDefNode = (BLangTupleVariableDef) TreeBuilder.createTupleVariableDefinitionNode();
        if (isExpressionAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        varDefNode.pos = pos;
        varDefNode.setVariable(var);
        varDefNode.addWS(ws);
        var.isDeclaredWithVar = isDeclaredWithVar;
        if (!isDeclaredWithVar) {
            var.setTypeNode(this.typeNodeStack.pop());
        }
        return varDefNode;
    }

    private BLangErrorVariableDef createErrorVariableDef(DiagnosticPos pos, Set<Whitespace> ws, boolean isFinal,
                                                         boolean isExpressionAvailable, boolean isDeclaredWithVar) {
        BLangErrorVariable var = (BLangErrorVariable) this.varStack.pop();
        if (isFinal) {
            markVariableAsFinal(var);
        }
        BLangErrorVariableDef varDefNode = (BLangErrorVariableDef) TreeBuilder.createErrorVariableDefinitionNode();
        if (isExpressionAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        varDefNode.pos = pos;
        varDefNode.setVariable(var);
        varDefNode.addWS(ws);
        var.isDeclaredWithVar = isDeclaredWithVar;
        if (!isDeclaredWithVar) {
            var.setTypeNode(this.typeNodeStack.pop());
        }
        return varDefNode;
    }

    void addRecordVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean isFinal,
                                       boolean isDeclaredWithVar) {
        BLangRecordVariableDef varDefNode = createRecordVariableDef(pos, ws, isFinal, true, isDeclaredWithVar);
        addStmtToCurrentBlock(varDefNode);
    }

    void addRecordVariableLetDefStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean isDeclaredWithVar,
                                          int numAnnotations) {
        BLangRecordVariableDef varDefNode = createRecordVariableDef(pos, ws, true, true, isDeclaredWithVar);
        attachAnnotations(varDefNode.var, numAnnotations, false);
        addLetVarDecl(varDefNode);

    }

    private BLangRecordVariableDef createRecordVariableDef(DiagnosticPos pos, Set<Whitespace> ws, boolean isFinal,
                                                           boolean isExpressionAvailable, boolean isDeclaredWithVar) {
        BLangRecordVariableDef varDefNode = (BLangRecordVariableDef) TreeBuilder.createRecordVariableDefinitionNode();
        BLangRecordVariable var = (BLangRecordVariable) this.varStack.pop();
        if (isFinal) {
            markVariableAsFinal(var);
        }
        if (isExpressionAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        varDefNode.pos = pos;
        varDefNode.setVariable(var);
        varDefNode.addWS(ws);
        varDefNode.var = var;
        var.isDeclaredWithVar = isDeclaredWithVar;
        if (!isDeclaredWithVar) {
            var.setTypeNode(this.typeNodeStack.pop());
        }
        return varDefNode;
    }

    void addTypeInitExpression(DiagnosticPos pos, Set<Whitespace> ws, String initName, boolean typeAvailable,
                               boolean exprAvailable) {
        BLangTypeInit initNode = (BLangTypeInit) TreeBuilder.createInitNode();
        initNode.pos = pos;
        initNode.addWS(ws);
        if (typeAvailable) {
            initNode.userDefinedType = (BLangType) typeNodeStack.pop();
        }

        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        invocationNode.addWS(ws);
        if (exprAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            Set<Whitespace> cws = commaWsStack.pop();
            exprNodes.forEach(exprNode -> {
                invocationNode.argExprs.add((BLangExpression) exprNode);
                initNode.argsExpr.add((BLangExpression) exprNode);

            });
            invocationNode.addWS(cws);
            initNode.addWS(cws);
        }

        //TODO check whether pkgName can be be empty
        IdentifierNode pkgNameNode = TreeBuilder.createIdentifierNode();
        IdentifierNode nameNode = createIdentifier(pos, initName, ws);
        BLangNameReference nameReference = new BLangNameReference(pos, ws, pkgNameNode, nameNode);
        invocationNode.name = (BLangIdentifier) nameReference.name;
        invocationNode.addWS(nameReference.ws);
        invocationNode.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;

        initNode.initInvocation = invocationNode;
        this.addExpressionNode(initNode);
    }

    private void markVariableAsFinal(BLangVariable variable) {
        // Set the final flag to the variable.
        variable.flagSet.add(Flag.FINAL);
        switch (variable.getKind()) {
            case TUPLE_VARIABLE:
                // If the variable is a tuple variable, we need to set the final flag to the all member variables.
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                tupleVariable.memberVariables.forEach(this::markVariableAsFinal);
                if (tupleVariable.restVariable != null) {
                    markVariableAsFinal(tupleVariable.restVariable);
                }
                break;
            case RECORD_VARIABLE:
                // If the variable is a record variable, we need to set the final flag to the all the variables in
                // the record.
                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                recordVariable.variableList.stream()
                        .map(BLangRecordVariableKeyValue::getValue)
                        .forEach(this::markVariableAsFinal);
                if (recordVariable.restParam != null) {
                    markVariableAsFinal((BLangVariable) recordVariable.restParam);
                }
                break;
            case ERROR_VARIABLE:
                BLangErrorVariable errorVariable = (BLangErrorVariable) variable;
                markVariableAsFinal(errorVariable.message);
                errorVariable.detail.forEach(entry -> markVariableAsFinal(entry.valueBindingPattern));
                if (errorVariable.restDetail != null) {
                    markVariableAsFinal(errorVariable.restDetail);
                }
                break;
        }
    }

    private void addStmtToCurrentBlock(StatementNode statement) {
        BlockNode body = this.blockNodeStack.peek();
        body.addStatement(statement);
    }

    void startTryCatchFinallyStmt() {
        this.tryCatchFinallyNodesStack.push((BLangTryCatchFinally) TreeBuilder.createTryCatchFinallyNode());
        startBlock();
    }

    void addTryClause(DiagnosticPos pos) {
        BLangBlockStmt tryBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        tryBlock.pos = pos;
        tryCatchFinallyNodesStack.peek().tryBody = tryBlock;
    }

    void startCatchClause() {
        startBlock();
    }

    void addCatchClause(DiagnosticPos poc, Set<Whitespace> ws, String paramName) {
        BLangSimpleVariable variableNode = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        variableNode.typeNode = (BLangType) this.typeNodeStack.pop();
        variableNode.name = createIdentifier(variableNode.typeNode.pos, paramName);
        variableNode.pos = variableNode.typeNode.pos;
        variableNode.addWS(removeNthFromLast(ws, 3));

        BLangCatch catchNode = (BLangCatch) TreeBuilder.createCatchNode();
        catchNode.pos = poc;
        catchNode.addWS(ws);
        catchNode.body = (BLangBlockStmt) this.blockNodeStack.pop();
        catchNode.param = variableNode;
        tryCatchFinallyNodesStack.peek().catchBlocks.add(catchNode);
    }

    void startFinallyBlock() {
        startBlock();
    }

    void addFinallyBlock(DiagnosticPos poc, Set<Whitespace> ws) {
        BLangBlockStmt blockNode = (BLangBlockStmt) this.blockNodeStack.pop();
        BLangTryCatchFinally rootTry = tryCatchFinallyNodesStack.peek();
        rootTry.finallyBody = blockNode;
        rootTry.addWS(ws);
        blockNode.pos = poc;
    }

    void addTryCatchFinallyStmt(DiagnosticPos poc, Set<Whitespace> ws) {
        BLangTryCatchFinally stmtNode = tryCatchFinallyNodesStack.pop();
        stmtNode.pos = poc;
        stmtNode.addWS(ws);
        addStmtToCurrentBlock(stmtNode);
    }

    void addThrowStmt(DiagnosticPos poc, Set<Whitespace> ws) {
        ExpressionNode throwExpr = this.exprNodeStack.pop();
        BLangThrow throwNode = (BLangThrow) TreeBuilder.createThrowNode();
        throwNode.pos = poc;
        throwNode.addWS(ws);
        throwNode.expr = (BLangExpression) throwExpr;
        addStmtToCurrentBlock(throwNode);
    }

    void addPanicStmt(DiagnosticPos poc, Set<Whitespace> ws) {
        ExpressionNode errorExpr = this.exprNodeStack.pop();
        BLangPanic panicNode = (BLangPanic) TreeBuilder.createPanicNode();
        panicNode.pos = poc;
        panicNode.addWS(ws);
        panicNode.expr = (BLangExpression) errorExpr;
        addStmtToCurrentBlock(panicNode);
    }

    private void addExpressionNode(ExpressionNode expressionNode) {
        this.exprNodeStack.push(expressionNode);
    }

    void addLiteralValue(DiagnosticPos pos, Set<Whitespace> ws, int typeTag, Object value) {
        addLiteralValue(pos, ws, typeTag, value, String.valueOf(value));
    }

    void addLiteralValue(DiagnosticPos pos, Set<Whitespace> ws, int typeTag, Object value, String originalValue) {
        BLangLiteral litExpr;
        // If numeric literal create a numeric literal expression; otherwise create a literal expression
        if (typeTag <= TypeTags.DECIMAL) {
            litExpr = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
        } else {
            litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        }
        litExpr.addWS(ws);
        litExpr.pos = pos;
        litExpr.type = symTable.getTypeFromTag(typeTag);
        litExpr.type.tag = typeTag;
        litExpr.value = value;
        litExpr.originalValue = originalValue;
        addExpressionNode(litExpr);
    }

    void addListConstructorExpression(DiagnosticPos pos, Set<Whitespace> ws, boolean argsAvailable) {
        List<ExpressionNode> argExprList;
        BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr)
                TreeBuilder.createListConstructorExpressionNode();
        if (argsAvailable) {
            listConstructorExpr.addWS(commaWsStack.pop());
            argExprList = exprNodeListStack.pop();
        } else {
            argExprList = new ArrayList<>(0);
        }
        listConstructorExpr.exprs = argExprList.stream().map(expr -> (BLangExpression) expr)
                .collect(Collectors.toList());
        listConstructorExpr.pos = pos;
        listConstructorExpr.addWS(ws);
        addExpressionNode(listConstructorExpr);
    }

    void addIdentifierRecordField() {
        recordLiteralNodes.peek().fields.add((BLangRecordLiteral.BLangRecordVarNameField) exprNodeStack.pop());
    }

    void addKeyValueRecordField(Set<Whitespace> ws, boolean computedKey, boolean isReadonly) {
        BLangRecordKeyValueField keyValue = (BLangRecordKeyValueField) TreeBuilder.createRecordKeyValue();
        keyValue.addWS(ws);
        keyValue.valueExpr = (BLangExpression) exprNodeStack.pop();
        keyValue.key = new BLangRecordKey((BLangExpression) exprNodeStack.pop());
        if (!this.recordKeyWS.isEmpty()) {
            keyValue.addWS(this.recordKeyWS.pop());
        }
        keyValue.key.computedKey = computedKey;
        keyValue.isReadonly = isReadonly;
        recordLiteralNodes.peek().fields.add(keyValue);
    }

    void addLetClause(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangLetClause letClause = (BLangLetClause) TreeBuilder.createLetClauseNode();
        letClause.addWS(ws);
        letClause.pos = pos;
        letClause.letVarDeclarations = letVarListStack.pop();
        queryClauseStack.push(letClause);
    }

    void addLetExpression(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangLetExpression letExpression = (BLangLetExpression) TreeBuilder.createLetExpressionNode();
        letExpression.expr = (BLangExpression) exprNodeStack.pop();
        letExpression.addWS(ws);
        letExpression.pos = pos;
        letExpression.letVarDeclarations = letVarListStack.pop();
        addExpressionNode(letExpression);
    }

    void addSpreadOpRecordField(Set<Whitespace> ws) {
        BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField =
                (BLangRecordLiteral.BLangRecordSpreadOperatorField) TreeBuilder.createRecordSpreadOperatorField();
        spreadOperatorField.addWS(ws);
        spreadOperatorField.expr = (BLangExpression) exprNodeStack.pop();
        recordLiteralNodes.peek().fields.add(spreadOperatorField);
    }

    void addRecordKeyWS(Set<Whitespace> ws) {
        this.recordKeyWS.push(ws);
    }

    void addMapStructLiteral(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangRecordLiteral recordTypeLiteralNode = recordLiteralNodes.pop();
        recordTypeLiteralNode.pos = pos;
        recordTypeLiteralNode.addWS(ws);
        addExpressionNode(recordTypeLiteralNode);
    }

    void startMapStructLiteral() {
        BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        recordLiteralNodes.push(literalNode);
    }

    void startExprNodeList() {
        this.exprNodeListStack.push(new ArrayList<>());
    }

    void endExprNodeList(Set<Whitespace> ws, int exprCount) {
        commaWsStack.push(ws);
        List<ExpressionNode> exprList = exprNodeListStack.peek();
        addExprToExprNodeList(exprList, exprCount);
    }

    private void addExprToExprNodeList(List<ExpressionNode> exprList, int n) {
        for (int i = 0; i < n; i++) {
            if (exprNodeStack.empty()) {
                throw new IllegalStateException("Expression stack cannot be empty in processing an ExpressionList");
            }
            exprList.add(exprNodeStack.pop());
        }
        Collections.reverse(exprList);
    }

    void createSimpleVariableReference(DiagnosticPos pos, Set<Whitespace> ws) {
        createSimpleVariableReference(pos, ws, (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode());
    }

    void createBLangRecordVarRefNameField(DiagnosticPos pos, Set<Whitespace> ws, boolean isReadonly) {
        BLangRecordLiteral.BLangRecordVarNameField varNameField =
                (BLangRecordLiteral.BLangRecordVarNameField) TreeBuilder.createRecordVarRefNameFieldNode();
        varNameField.isReadonly = isReadonly;
        createSimpleVariableReference(pos, ws, varNameField);
    }

    private void createSimpleVariableReference(DiagnosticPos pos, Set<Whitespace> ws, BLangSimpleVarRef varRef) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        varRef.pos = pos;
        varRef.addWS(ws);
        varRef.addWS(nameReference.ws);
        varRef.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        varRef.variableName = (BLangIdentifier) nameReference.name;
        this.exprNodeStack.push(varRef);
    }

    void createFunctionInvocation(DiagnosticPos pos, Set<Whitespace> ws, boolean argsAvailable,
                                  boolean actionInvocation) {
        BLangInvocation invocationNode;

        if (actionInvocation) {
            invocationNode = (BLangInvocation) TreeBuilder.createActionInvocation();
        } else {
            invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        }

        invocationNode.pos = pos;
        invocationNode.addWS(ws);
        if (argsAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            exprNodes.forEach(exprNode -> invocationNode.argExprs.add((BLangExpression) exprNode));
            invocationNode.addWS(commaWsStack.pop());
        }

        BLangNameReference nameReference = nameReferenceStack.pop();
        invocationNode.name = (BLangIdentifier) nameReference.name;
        invocationNode.addWS(this.invocationWsStack.pop());
        invocationNode.addWS(nameReference.ws);
        invocationNode.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        addExpressionNode(invocationNode);
    }

    void startInvocationNode(Set<Whitespace> ws) {
        invocationWsStack.push(ws);
    }

    // Note: This method is for creating invocation nodes for the invocation types defined in the grammar rule
    // `variableReference`. The assumption here is that those invocations can only become an action invocation if
    // it's an async op (i.e., preceded by `start`).
    void createInvocationNode(DiagnosticPos pos, Set<Whitespace> ws, String invocation, boolean argsAvailable,
                              DiagnosticPos identifierPos, boolean async, int annots) {
        BLangInvocation invocationNode;
        if (async) {
            invocationNode = (BLangInvocation.BLangActionInvocation) TreeBuilder.createActionInvocation();
            invocationNode.async = async;
            attachAnnotations(invocationNode, annots, false);
        } else {
            invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        }
        invocationNode.pos = pos;
        invocationNode.addWS(ws);
        invocationNode.addWS(invocationWsStack.pop());
        if (!invocationRuleWS.isEmpty()) {
            invocationNode.addWS(invocationRuleWS.pop());
        }

        if (argsAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            exprNodes.forEach(exprNode -> invocationNode.argExprs.add((BLangExpression) exprNode));
            invocationNode.addWS(commaWsStack.pop());
        }

        invocationNode.expr = (BLangExpression) exprNodeStack.pop();
        invocationNode.name = createIdentifier(identifierPos, invocation, ws);
        invocationNode.pkgAlias = createIdentifier(pos, null);
        addExpressionNode(invocationNode);
    }

    void addInvocationWS(Set<Whitespace> ws) {
        invocationRuleWS.push(ws);
    }

    void createWorkerLambdaInvocationNode(DiagnosticPos pos, Set<Whitespace> ws, String invocation) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createActionInvocation();
        invocationNode.pos = pos;
        invocationNode.addWS(ws);
        invocationNode.addWS(invocationWsStack.pop());

        invocationNode.name = createIdentifier(pos, invocation, ws);
        invocationNode.pkgAlias = createIdentifier(pos, null);
        addExpressionNode(invocationNode);
    }

    void createActionInvocationNode(DiagnosticPos pos, Set<Whitespace> ws, boolean async, boolean remoteMethodCall,
                                    int numAnnotations) {
        BLangInvocation.BLangActionInvocation actionInvocation =
                (BLangInvocation.BLangActionInvocation) exprNodeStack.pop();
        actionInvocation.pos = pos;
        actionInvocation.addWS(ws);
        actionInvocation.async = async;
        actionInvocation.remoteMethodCall = remoteMethodCall;

        actionInvocation.expr = (BLangExpression) exprNodeStack.pop();
        attachAnnotations(actionInvocation, numAnnotations, false);
        exprNodeStack.push(actionInvocation);
    }

    void createFieldBasedAccessNode(DiagnosticPos pos, Set<Whitespace> ws, String fieldName, DiagnosticPos fieldNamePos,
                                    String nsPrefixName, DiagnosticPos nsPrefixPos,
                                    FieldKind fieldType, boolean optionalFieldAccess) {
        BLangFieldBasedAccess fieldBasedAccess;
        if (nsPrefixName != null) {
            BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess accessWithPrefixNode =
                    (BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess)
                            TreeBuilder.createFieldBasedAccessWithPrefixNode();
            accessWithPrefixNode.nsPrefix = createIdentifier(nsPrefixPos, nsPrefixName);
            fieldBasedAccess = accessWithPrefixNode;
        } else {
            fieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        }
        fieldBasedAccess.pos = pos;
        fieldBasedAccess.addWS(ws);
        fieldBasedAccess.field = createIdentifier(fieldNamePos, fieldName, ws);
        fieldBasedAccess.field.pos = fieldNamePos;
        fieldBasedAccess.expr = (BLangVariableReference) exprNodeStack.pop();
        fieldBasedAccess.fieldKind = fieldType;
        fieldBasedAccess.optionalFieldAccess = optionalFieldAccess;
        addExpressionNode(fieldBasedAccess);
    }

    void createMultiKeyExpressionNode(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTableMultiKeyExpr tableMultiKeyExpr = (BLangTableMultiKeyExpr) TreeBuilder.
                createTableMultiKeyExpressionNode();
        tableMultiKeyExpr.pos = pos;
        tableMultiKeyExpr.addWS(ws);
        this.exprNodeListStack.pop().forEach(expr -> tableMultiKeyExpr.multiKeyIndexExprs.
                add((BLangExpression) expr));
        tableMultiKeyExpressionNodeStack.push(tableMultiKeyExpr);
    }

    void createIndexBasedAccessNode(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangIndexBasedAccess indexBasedAccess = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexBasedAccess.pos = pos;
        indexBasedAccess.addWS(ws);
        if (tableMultiKeyExpressionNodeStack.size() == 1) {
            indexBasedAccess.indexExpr = (BLangTableMultiKeyExpr) tableMultiKeyExpressionNodeStack.pop();
        } else {
            indexBasedAccess.indexExpr = (BLangExpression) exprNodeStack.pop();
        }

        indexBasedAccess.expr = (BLangVariableReference) exprNodeStack.pop();
        addExpressionNode(indexBasedAccess);
    }

    void createGroupExpression(DiagnosticPos pos, Set<Whitespace> ws) {
        final BLangGroupExpr expr = (BLangGroupExpr) TreeBuilder.createGroupExpressionNode();
        expr.pos = pos;
        expr.addWS(ws);
        expr.expression = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(expr);
    }

    void createBinaryExpr(DiagnosticPos pos, Set<Whitespace> ws, String operator) {
        BLangBinaryExpr binaryExpressionNode = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpressionNode.pos = pos;
        binaryExpressionNode.addWS(ws);
        binaryExpressionNode.rhsExpr = (BLangExpression) exprNodeStack.pop();
        binaryExpressionNode.lhsExpr = (BLangExpression) exprNodeStack.pop();
        binaryExpressionNode.opKind = OperatorKind.valueFrom(operator);
        addExpressionNode(binaryExpressionNode);
    }

    void createElvisExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangElvisExpr elvisExpr = (BLangElvisExpr) TreeBuilder.createElvisExpressionNode();
        elvisExpr.pos = pos;
        elvisExpr.addWS(ws);
        elvisExpr.rhsExpr = (BLangExpression) exprNodeStack.pop();
        elvisExpr.lhsExpr = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(elvisExpr);
    }

    void createTypeAccessExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTypedescExpr typeAccessExpr = (BLangTypedescExpr) TreeBuilder.createTypeAccessNode();
        typeAccessExpr.pos = pos;
        typeAccessExpr.addWS(ws);
        typeAccessExpr.typeNode = (BLangType) typeNodeStack.pop();
        addExpressionNode(typeAccessExpr);
    }

    void createTypeConversionExpr(DiagnosticPos pos, Set<Whitespace> ws, int annotationCount,
                                  boolean typeNameAvailable) {
        BLangTypeConversionExpr typeConversionNode = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        attachAnnotations(typeConversionNode, annotationCount, false);
        typeConversionNode.pos = pos;
        typeConversionNode.addWS(ws);
        if (typeNameAvailable) {
            typeConversionNode.typeNode = (BLangType) typeNodeStack.pop();
        }
        typeConversionNode.expr = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(typeConversionNode);
    }

    void createUnaryExpr(DiagnosticPos pos, Set<Whitespace> ws, String operator) {
        BLangUnaryExpr unaryExpressionNode = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        unaryExpressionNode.pos = pos;
        unaryExpressionNode.addWS(ws);
        unaryExpressionNode.expr = (BLangExpression) exprNodeStack.pop();
        unaryExpressionNode.operator = OperatorKind.valueFrom(operator);
        addExpressionNode(unaryExpressionNode);
    }

    void createTernaryExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTernaryExpr ternaryExpr = (BLangTernaryExpr) TreeBuilder.createTernaryExpressionNode();
        ternaryExpr.pos = pos;
        ternaryExpr.addWS(ws);
        ternaryExpr.elseExpr = (BLangExpression) exprNodeStack.pop();
        ternaryExpr.thenExpr = (BLangExpression) exprNodeStack.pop();
        ternaryExpr.expr = (BLangExpression) exprNodeStack.pop();
        if (ternaryExpr.expr.getKind() == NodeKind.TERNARY_EXPR) {
            // Re-organizing ternary expression tree if there nested ternary expressions.
            BLangTernaryExpr root = (BLangTernaryExpr) ternaryExpr.expr;
            BLangTernaryExpr parent = root;
            while (parent.elseExpr.getKind() == NodeKind.TERNARY_EXPR) {
                parent = (BLangTernaryExpr) parent.elseExpr;
            }
            ternaryExpr.expr = parent.elseExpr;
            parent.elseExpr = ternaryExpr;
            ternaryExpr = root;
        }
        addExpressionNode(ternaryExpr);
    }

    void createCheckedExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangCheckedExpr checkedExpr = (BLangCheckedExpr) TreeBuilder.createCheckExpressionNode();
        checkedExpr.pos = pos;
        checkedExpr.addWS(ws);
        checkedExpr.expr = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(checkedExpr);
    }

    void createCheckPanickedExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangCheckPanickedExpr checkPanicExpr = (BLangCheckPanickedExpr) TreeBuilder.createCheckPanicExpressionNode();
        checkPanicExpr.pos = pos;
        checkPanicExpr.addWS(ws);
        checkPanicExpr.expr = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(checkPanicExpr);
    }

    void createTrapExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTrapExpr trapExpr = (BLangTrapExpr) TreeBuilder.createTrapExpressionNode();
        trapExpr.pos = pos;
        trapExpr.addWS(ws);
        trapExpr.expr = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(trapExpr);
    }

    void createQueryExpr(DiagnosticPos pos, Set<Whitespace> ws, boolean isTable, boolean isStream,
                         List<BLangIdentifier> keyFieldNameIdentifierList) {
        BLangQueryExpr queryExpr = (BLangQueryExpr) TreeBuilder.createQueryExpressionNode();
        queryExpr.pos = pos;
        queryExpr.addWS(ws);
        queryExpr.setIsTable(isTable);
        if (isTable) {
            for (BLangIdentifier identifier : keyFieldNameIdentifierList) {
                queryExpr.addFieldNameIdentifier(identifier);
            }
        }
        queryExpr.setIsStream(isStream);
        Collections.reverse(queryClauseStack);
        while (queryClauseStack.size() > 0) {
            queryExpr.addQueryClause(queryClauseStack.pop());

        }
        addExpressionNode(queryExpr);
    }

    void startFromClause() {
        this.isInQuery = true;
    }

    void createClauseWithSimpleVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                                        DiagnosticPos identifierPos, boolean isDeclaredWithVar,
                                                        boolean isFromClause, boolean isOuterJoin) {
        BLangSimpleVariableDef variableDefinitionNode = createSimpleVariableDef(pos, null, identifier, identifierPos,
                false, false, isDeclaredWithVar);

        if ((!this.bindingPatternIdentifierWS.isEmpty() && !isInQuery) || !this.bindingPatternIdentifierWS.isEmpty()) {
            variableDefinitionNode.var.addWS(this.bindingPatternIdentifierWS.pop());
        } else if (!this.queryBindingPatternIdentifierWS.isEmpty()) {
            variableDefinitionNode.var.addWS(this.queryBindingPatternIdentifierWS.pop());
        }

        if (isFromClause) {
            isInQuery = false;
        }

        addClause(pos, ws, variableDefinitionNode, isDeclaredWithVar, isFromClause, isOuterJoin);
    }

    void createClauseWithRecordVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws,
                                                        boolean isDeclaredWithVar, boolean isFromClause,
                                                        boolean isOuterJoin) {
        BLangRecordVariableDef variableDefinitionNode = createRecordVariableDef(pos, ws, false, false,
                isDeclaredWithVar);
        if ((!this.bindingPatternIdentifierWS.isEmpty() && !isInQuery) || !this.bindingPatternIdentifierWS.isEmpty()) {
            variableDefinitionNode.addWS(this.bindingPatternIdentifierWS.pop());
        } else if (!this.queryBindingPatternIdentifierWS.isEmpty()) {
            variableDefinitionNode.var.addWS(this.queryBindingPatternIdentifierWS.pop());
        }

        if (isFromClause) {
            isInQuery = false;
        }
        addClause(pos, ws, variableDefinitionNode, isDeclaredWithVar, isFromClause, isOuterJoin);
    }

    void createClauseWithErrorVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws,
                                                       boolean isDeclaredWithVar, boolean isFromClause,
                                                       boolean isOuterJoin) {
        BLangErrorVariableDef variableDefinitionNode = createErrorVariableDef(pos, ws, false,
                false, isDeclaredWithVar);
        if ((!this.bindingPatternIdentifierWS.isEmpty() && !isInQuery) || !this.bindingPatternIdentifierWS.isEmpty()) {
            variableDefinitionNode.addWS(this.bindingPatternIdentifierWS.pop());
        } else if (!this.queryBindingPatternIdentifierWS.isEmpty()) {
            variableDefinitionNode.addWS(this.queryBindingPatternIdentifierWS.pop());
        }

        if (isFromClause) {
            isInQuery = false;
        }
        addClause(pos, ws, variableDefinitionNode, isDeclaredWithVar, isFromClause, isOuterJoin);
    }

    void createClauseWithTupleVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws,
                                                       boolean isDeclaredWithVar, boolean isFromClause,
                                                       boolean isOuterJoin) {
        BLangTupleVariableDef variableDefinitionNode = createTupleVariableDef(pos, ws, false,
                false, isDeclaredWithVar);
        if ((!this.bindingPatternIdentifierWS.isEmpty() && !isInQuery) || !this.bindingPatternIdentifierWS.isEmpty()) {
            variableDefinitionNode.addWS(this.bindingPatternIdentifierWS.pop());
        } else if (!this.queryBindingPatternIdentifierWS.isEmpty()) {
            variableDefinitionNode.addWS(this.queryBindingPatternIdentifierWS.pop());
        }

        if (isFromClause) {
            isInQuery = false;
        }
        addClause(pos, ws, variableDefinitionNode, isDeclaredWithVar, isFromClause, isOuterJoin);
    }

    private void addClause(DiagnosticPos pos, Set<Whitespace> ws,
                           VariableDefinitionNode variableDefinitionNode,
                           boolean isDeclaredWithVar, boolean isFromClause, boolean isOuterJoin) {
        BLangInputClause inputClause;
        if (isFromClause) {
            inputClause = (BLangFromClause) TreeBuilder.createFromClauseNode();
        } else {
            inputClause = (BLangJoinClause) TreeBuilder.createJoinClauseNode();
        }
        inputClause.addWS(ws);
        inputClause.pos = pos;
        markVariableAsFinal((BLangVariable) variableDefinitionNode.getVariable());
        inputClause.setVariableDefinitionNode(variableDefinitionNode);
        inputClause.setCollection(this.exprNodeStack.pop());
        inputClause.isDeclaredWithVar = isDeclaredWithVar;
        inputClause.isOuterJoin = isOuterJoin;
        queryClauseStack.push(inputClause);

    }

    void startOnClause() {
        this.isInOnCondition = true;
    }

    void createOnClause(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangOnClause onClause = (BLangOnClause) TreeBuilder.createOnClauseNode();
        onClause.addWS(ws);
        onClause.pos = pos;
        onClause.expression = (BLangExpression) this.exprNodeStack.pop();
        queryClauseStack.push(onClause);

        isInOnCondition = false;
    }

    void createSelectClause(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangSelectClause selectClause = (BLangSelectClause) TreeBuilder.createSelectClauseNode();
        selectClause.addWS(ws);
        selectClause.pos = pos;
        selectClause.expression = (BLangExpression) this.exprNodeStack.pop();
        queryClauseStack.push(selectClause);
    }

    void createOnConflictClause(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangOnConflictClause onConflictClause = (BLangOnConflictClause) TreeBuilder.createOnConflictClauseNode();
        onConflictClause.addWS(ws);
        onConflictClause.pos = pos;
        onConflictClause.expression = (BLangExpression) this.exprNodeStack.pop();
        queryClauseStack.push(onConflictClause);
    }

    void createLimitClause(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangLimitClause limitClause = (BLangLimitClause) TreeBuilder.createLimitClauseNode();
        limitClause.addWS(ws);
        limitClause.pos = pos;
        limitClause.expression = (BLangExpression) this.exprNodeStack.pop();
        queryClauseStack.push(limitClause);
    }

    void createWhereClause(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangWhereClause whereClause = (BLangWhereClause) TreeBuilder.createWhereClauseNode();
        whereClause.addWS(ws);
        whereClause.pos = pos;
        whereClause.expression = (BLangExpression) this.exprNodeStack.pop();
        queryClauseStack.push(whereClause);
    }

    void startDoActionBlock() {
        startBlock();
    }

    void createDoClause(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangDoClause doClause = (BLangDoClause) TreeBuilder.createDoClauseNode();
        doClause.addWS(ws);
        doClause.pos = pos;
        BLangBlockStmt blockNode = (BLangBlockStmt) blockNodeStack.pop();
        blockNode.pos = pos;
        doClause.setBody(blockNode);
        doClause.addWS(ws);
        queryClauseStack.push(doClause);
    }

    void createQueryActionExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangQueryAction queryAction = (BLangQueryAction) TreeBuilder.createQueryActionNode();
        queryAction.pos = pos;
        queryAction.addWS(ws);
        Collections.reverse(queryClauseStack);
        while (queryClauseStack.size() > 0) {
            queryAction.addQueryClause(queryClauseStack.pop());
        }
        addExpressionNode(queryAction);
    }

    void endFunctionDefinition(DiagnosticPos pos, Set<Whitespace> ws, String funcName, DiagnosticPos funcNamePos,
                               boolean publicFunc, boolean remoteFunc, boolean nativeFunc, boolean privateFunc,
                               boolean isLambda) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
        function.name = this.createIdentifier(funcNamePos, funcName);
        function.pos = pos;
        function.addWS(ws);
        if (!isLambda) {
            function.addWS(invocationWsStack.pop());
        }
        if (publicFunc) {
            function.flagSet.add(Flag.PUBLIC);
        } else if (privateFunc) {
            function.flagSet.add(Flag.PRIVATE);
        }

        if (remoteFunc) {
            function.flagSet.add(Flag.REMOTE);
        }

        if (nativeFunc) {
            function.flagSet.add(Flag.NATIVE);
        }

        function.body = (BLangFunctionBody) this.funcBodyNodeStack.pop();

        if (function.body.getKind() == NodeKind.BLOCK_FUNCTION_BODY) {
            this.blockNodeStack.pop();
        }

        this.compUnit.addTopLevelNode(function);
    }

    void createTableConstructor(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTableConstructorExpr tableConstructorExpr =
                (BLangTableConstructorExpr) TreeBuilder.createTableConstructorExpressionNode();
        tableConstructorExpr.pos = pos;
        tableConstructorExpr.addWS(ws);
        if (tableKeySpecifierNodeStack.size() > 0) {
            tableConstructorExpr.tableKeySpecifier = (BLangTableKeySpecifier) tableKeySpecifierNodeStack.pop();
        }

        if (exprNodeListStack.size() != 0) {
            List<ExpressionNode> tableRecordLiteralList = exprNodeListStack.pop();
            tableRecordLiteralList.forEach(recordLiteral -> tableConstructorExpr.
                    addRecordLiteral((BLangRecordLiteral) recordLiteral));
        }
        addExpressionNode(tableConstructorExpr);
    }

    void startWorker(PackageID pkgID) {
        this.startLambdaFunctionDef(pkgID);
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.addFlag(Flag.WORKER);
        this.startBlockFunctionBody();
    }

    void addWorker(DiagnosticPos pos, Set<Whitespace> ws, String workerName, DiagnosticPos workerNamePos,
                   boolean retParamsAvail, int numAnnotations) {
        // Merge worker definition whitespaces and worker declaration whitespaces.
        if (this.workerDefinitionWSStack.size() > 0 && ws != null) {
            ws.addAll(this.workerDefinitionWSStack.pop());
        }

        endBlockFunctionBody(pos, ws);

        BLangFunction bLangFunction = (BLangFunction) this.invokableNodeStack.peek();
        // change default worker name
        bLangFunction.defaultWorkerName.value = workerName;
        if (workerNamePos != null) {
            bLangFunction.defaultWorkerName.pos = workerNamePos;
        }

        // Attach worker annotation to the function node.
        attachAnnotations(bLangFunction, numAnnotations, true);

        endFunctionSignature(pos, ws, false, retParamsAvail, false);
        addLambdaFunctionDef(pos, ws);
        String workerLambdaName = WORKER_LAMBDA_VAR_PREFIX + workerName;
        addSimpleVariableDefStatement(pos, null, workerLambdaName, null, true, true, true);

        // Check if the worker is in a fork. If so add the lambda function to the worker list in fork, else ignore.
        BLangSimpleVariableDef lamdaWrkr = getLastVarDefStmtFromBlock();
        if (lamdaWrkr != null) {
            lamdaWrkr.isWorker = true;
            if (!this.forkJoinNodesStack.empty()) {
                // TODO: Revisit the fork join worker declaration and decide whether move this to desugar.
                lamdaWrkr.isInFork = true;
                lamdaWrkr.var.flagSet.add(Flag.FORKED);
                this.forkJoinNodesStack.peek().addWorkers(lamdaWrkr);
            }
        }

        DiagnosticPos namePos = workerNamePos != null ? workerNamePos : pos;
        addNameReference(namePos, null, null, workerLambdaName);
        startInvocationNode(null);
        createWorkerLambdaInvocationNode(namePos, null, workerLambdaName);
        markLastInvocationAsAsync(namePos, numAnnotations);
        addWorkerVariableDefStatement(namePos, workerName);
        BLangSimpleVariableDef invocationStmt = getLastVarDefStmtFromBlock();
        if (invocationStmt != null) {
            invocationStmt.isWorker = true;
        }
    }

    private void addWorkerVariableDefStatement(DiagnosticPos pos, String identifier) {
        BLangSimpleVariableDef varDefNode = createSimpleVariableDef(pos, null, identifier, null, true, true, true);
        if (!this.bindingPatternIdentifierWS.isEmpty()) {
            varDefNode.addWS(this.bindingPatternIdentifierWS.pop());
        }
        varDefNode.var.flagSet.add(Flag.WORKER);
        addStmtToCurrentBlock(varDefNode);
    }

    private BLangSimpleVariableDef getLastVarDefStmtFromBlock() {
        BLangSimpleVariableDef variableDef = null;
        if (!this.blockNodeStack.isEmpty()) {
            List<? extends StatementNode> stmtsAdded =
                    this.blockNodeStack.peek().getStatements();
            if (stmtsAdded.get(stmtsAdded.size() - 1) instanceof BLangSimpleVariableDef) {
                variableDef = (BLangSimpleVariableDef) stmtsAdded.get(stmtsAdded.size() - 1);
            }
        }
        return variableDef;
    }

    void attachWorkerWS(Set<Whitespace> ws) {
        this.workerDefinitionWSStack.push(ws);
    }

    void startForkJoinStmt() {
        this.forkJoinNodesStack.push(TreeBuilder.createForkJoinNode());
    }

    void addForkJoinStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.pop();
        forkJoin.pos = pos;
        forkJoin.addWS(ws);
        this.addStmtToCurrentBlock(forkJoin);
        String nextAnonymousForkKey = anonymousModelHelper.getNextAnonymousForkKey(pos.src.pkgID);
        for (BLangSimpleVariableDef worker : forkJoin.workers) {
            BLangFunction function = ((BLangLambdaFunction) worker.var.expr).function;
            function.flagSet.add(Flag.FORKED);
            function.anonForkName = nextAnonymousForkKey;
        }
    }

    void endBlockFunctionBody(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) this.funcBodyNodeStack.peek();
        body.addWS(ws);
        body.pos = pos;
    }

    void endExprFunctionBody(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangExprFunctionBody body = (BLangExprFunctionBody) this.funcBodyNodeStack.peek();
        body.expr = (BLangExpression) this.exprNodeStack.pop();
        body.pos = pos;
        body.addWS(ws);
    }

    void endExternalFunctionBody(int annotCount, Set<Whitespace> ws) {
        ExternalFunctionBodyNode externBody = (ExternalFunctionBodyNode) this.funcBodyNodeStack.peek();
        externBody.addWS(ws);

        if (annotCount == 0) {
            return;
        }

        List<AnnotationAttachmentNode> annotAttachments =
                (List<AnnotationAttachmentNode>) externBody.getAnnotationAttachments();

        for (int i = 0; i < annotCount; i++) {
            annotAttachments.add(annotAttachmentStack.pop());
        }

        // reversing the collected annotations to preserve the original order
        Collections.reverse(annotAttachments);
    }

    void addImportPackageDeclaration(DiagnosticPos pos,
                                     Set<Whitespace> ws,
                                     String orgName,
                                     List<String> nameComps,
                                     String version,
                                     String alias) {

        BLangImportPackage importDcl = getImportPackage(pos, ws, orgName, nameComps, version, alias);
        this.compUnit.addTopLevelNode(importDcl);
        this.imports.add(importDcl);
    }

    private BLangImportPackage getImportPackage(DiagnosticPos pos, Set<Whitespace> ws, String orgName,
                                                List<String> nameComps, String version, String alias) {
        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        for (String component : nameComps) {
            pkgNameComps.add(this.createIdentifier(pos, component, null));
        }
        BLangIdentifier versionNode = this.createIdentifier(pos, version);
        BLangIdentifier aliasNode = (alias != null && !alias.isEmpty()) ?
                this.createIdentifier(pos, alias, null) :
                pkgNameComps.get(pkgNameComps.size() - 1);

        BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        importDcl.pos = pos;
        importDcl.addWS(ws);
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.version = versionNode;
        importDcl.orgName = this.createIdentifier(pos, orgName);
        importDcl.alias = aliasNode;
        importDcl.compUnit = this.createIdentifier(pos, this.compUnit.getName());
        return importDcl;
    }

    private VariableNode generateBasicVarNodeWithoutType(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                                         DiagnosticPos identifierPos, boolean isExpressionAvailable) {
        BLangSimpleVariable var = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        var.pos = pos;
        IdentifierNode name = this.createIdentifier(identifierPos, identifier, ws);
        ((BLangIdentifier) name).pos = identifierPos;
        var.setName(name);
        var.addWS(ws);
        if (isExpressionAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        return var;
    }

    private BLangConstant generateConstantNode(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                               DiagnosticPos identifierPos, boolean isTypeAvailable) {
        BLangConstant constantNode = (BLangConstant) TreeBuilder.createConstantNode();
        constantNode.pos = pos;
        BLangIdentifier name = createIdentifier(identifierPos, identifier, ws);
        constantNode.setName(name);
        constantNode.addWS(ws);
        if (isTypeAvailable) {
            constantNode.setTypeNode(this.typeNodeStack.pop());
        }
        constantNode.expr = (BLangExpression) this.exprNodeStack.pop();
        return constantNode;
    }

    private VariableNode generateBasicVarNode(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                              DiagnosticPos identifierPos, boolean isExpressionAvailable) {
        return generateBasicVarNode(pos, ws, identifier, identifierPos, false, isExpressionAvailable, true);
    }

    private VariableNode generateBasicVarNode(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                              DiagnosticPos identifierPos, boolean isDeclaredWithVar,
                                              boolean isExpressionAvailable, boolean isTypeNameProvided) {
        BLangSimpleVariable var = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        var.pos = pos;
        IdentifierNode name = this.createIdentifier(identifierPos, identifier, ws);
        ((BLangIdentifier) name).pos = identifierPos;
        var.setName(name);
        var.addWS(ws);
        if (isDeclaredWithVar) {
            var.isDeclaredWithVar = true;
        }
        if (isTypeNameProvided) {
            var.setTypeNode(this.typeNodeStack.pop());
        }
        if (isExpressionAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        return var;
    }

    void addConstant(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                     boolean isPublic, boolean isTypeAvailable) {
        identifier = escapeQuotedIdentifier(identifier);
        BLangConstant constantNode = (BLangConstant) this.generateConstantNode(pos, ws, identifier, identifierPos,
                isTypeAvailable);
        attachAnnotations(constantNode);
        constantNode.flagSet.add(Flag.CONSTANT);
        if (isPublic) {
            constantNode.flagSet.add(Flag.PUBLIC);
        }
        attachMarkdownDocumentations(constantNode);
        this.compUnit.addTopLevelNode(constantNode);

        // Check whether the value is a literal. If it is not a literal, it is an invalid case. So we don't need to
        // consider it.
        NodeKind nodeKind = constantNode.expr.getKind();
        if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL) {
            // Note - If the RHS is a literal, we need to create an anonymous type definition which can later be used
            // in type definitions.

            // Create a new literal.
            BLangLiteral literal = nodeKind == NodeKind.LITERAL ?
                    (BLangLiteral) TreeBuilder.createLiteralExpression() :
                    (BLangLiteral) TreeBuilder.createNumericLiteralExpression();
            literal.setValue(((BLangLiteral) constantNode.expr).value);
            literal.type = constantNode.expr.type;
            literal.isConstant = true;

            // Create a new finite type node.
            BLangFiniteTypeNode finiteTypeNode = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();
            finiteTypeNode.valueSpace.add(literal);

            // Create a new anonymous type definition.
            BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
            String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
            IdentifierNode anonTypeGenName = createIdentifier(identifierPos, genName);
            typeDef.setName(anonTypeGenName);
            typeDef.flagSet.add(Flag.PUBLIC);
            typeDef.flagSet.add(Flag.ANONYMOUS);
            typeDef.typeNode = finiteTypeNode;
            typeDef.pos = pos;

            // We add this type definition to the `associatedTypeDefinition` field of the constant node. Then when we
            // visit the constant node, we visit this type definition as well. By doing this, we don't need to change
            // any of the type def visiting logic in symbol enter.
            constantNode.associatedTypeDefinition = typeDef;
        }
    }

    // If this is a quoted identifier then unescape it and remove the quote prefix.
    // Else return original.
    static String escapeQuotedIdentifier(String identifier) {
        if (identifier.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            identifier = StringEscapeUtils.unescapeJava(identifier).substring(1);
        }
        return identifier;
    }

    void addEnumMember(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                       boolean isPublic, boolean hasExpression) {
        // Set typenode as string
        addValueType(pos, null, STRING_TYPE);

        // Set expression value if not set
        if (!hasExpression) {
            addLiteralValue(pos, null, TypeTags.STRING, identifier);
        }

        // Add enum member as constant
        addConstant(pos, ws, identifier, identifierPos, isPublic, true);

        // Create typenode for enum type definition member
        addNameReference(pos, ws, null, identifier);
        addUserDefineType(ws, false);
    }

    void addGlobalVariable(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                           boolean isPublic, boolean isFinal, boolean isDeclaredWithVar, boolean isExpressionAvailable,
                           boolean isListenerVar, boolean isTypeNameProvided) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, identifierPos,
                isDeclaredWithVar, isExpressionAvailable, isTypeNameProvided);

        if (isPublic) {
            var.flagSet.add(Flag.PUBLIC);
        }
        if (isFinal) {
            var.flagSet.add(Flag.FINAL);
        }
        if (isListenerVar) {
            var.flagSet.add(Flag.LISTENER);
            var.flagSet.add(Flag.FINAL);
            if (!isTypeNameProvided) {
                var.isDeclaredWithVar = true;
            }
        }
        if (var.typeNode != null && var.typeNode.getKind() == NodeKind.ERROR_TYPE) {
            var.isDeclaredWithVar = ((BLangErrorType) var.typeNode).inferErrorType;
        }

        attachAnnotations(var);
        attachMarkdownDocumentations(var);

        this.compUnit.addTopLevelNode(var);
    }

    void addTableKeyTypeConstraint(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTableKeyTypeConstraint tableKeyTypeConstraint = new BLangTableKeyTypeConstraint();
        tableKeyTypeConstraint.pos = pos;
        tableKeyTypeConstraint.addWS(ws);
        tableKeyTypeConstraint.keyType = (BLangType) typeNodeStack.pop();
        tableKeyTypeConstraintNodeStack.push(tableKeyTypeConstraint);
    }

    void addTableKeySpecifier(DiagnosticPos pos, Set<Whitespace> ws, List<BLangIdentifier> keyFieldNameIdentifierList) {
        BLangTableKeySpecifier tableKeySpecifierNode =
                (BLangTableKeySpecifier) TreeBuilder.createTableKeySpecifierNode();
        tableKeySpecifierNode.pos = pos;
        tableKeySpecifierNode.addWS(ws);

        for (BLangIdentifier identifier : keyFieldNameIdentifierList) {
            tableKeySpecifierNode.addFieldNameIdentifier(identifier);
        }
        tableKeySpecifierNodeStack.push(tableKeySpecifierNode);
    }

    void startRecordType() {
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) TreeBuilder.createRecordTypeNode();
        typeNodeStack.push(recordTypeNode);
        startVarList();
    }

    void startObjectType() {
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) TreeBuilder.createObjectTypeNode();
        typeNodeStack.push(objectTypeNode);
        startVarList();
        startFieldBlockList();
    }

    void addObjectType(DiagnosticPos pos, Set<Whitespace> ws, boolean isAnonymous, boolean isAbstract,
                       boolean isClient, boolean isService, boolean isDistinct) {
        BLangObjectTypeNode objectTypeNode = populateObjectTypeNode(pos, ws, isAnonymous);
        objectTypeNode.addWS(this.objectFieldBlockWs.pop());

        if (isAbstract) {
            objectTypeNode.flagSet.add(Flag.ABSTRACT);
        }

        if (isClient) {
            objectTypeNode.flagSet.add(Flag.CLIENT);
        }

        if (isService) {
            objectTypeNode.flagSet.add(Flag.SERVICE);
        }

        if (isDistinct) {
            objectTypeNode.flagSet.add(Flag.DISTINCT);
        }

        if (!isAnonymous) {
            addType(objectTypeNode);
            return;
        }
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName);
        typeDef.setName(anonTypeGenName);
        typeDef.flagSet.add(Flag.PUBLIC);
        typeDef.flagSet.add(Flag.ANONYMOUS);
        if (isDistinct) {
            typeDef.flagSet.add(Flag.DISTINCT);
        }
        typeDef.typeNode = objectTypeNode;
        typeDef.pos = pos;
        this.compUnit.addTopLevelNode(typeDef);

        addType(createUserDefinedType(pos, ws, (BLangIdentifier) TreeBuilder.createIdentifierNode(), typeDef.name));
    }

    void addObjectTypeName(Set<Whitespace> ws) {
        List<TopLevelNode> topLevelNodes = this.compUnit.getTopLevelNodes();
        if (!this.typeNodeStack.isEmpty() && this.typeNodeStack.peek() instanceof BLangObjectTypeNode) {
            BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) this.typeNodeStack.peek();
            objectTypeNode.addWS(ws);
        } else if (topLevelNodes.size() > 0
                && (topLevelNodes.get(topLevelNodes.size() - 1) instanceof BLangTypeDefinition)) {
            BLangTypeDefinition typeDefinition = (BLangTypeDefinition) topLevelNodes.get(topLevelNodes.size() - 1);
            if (typeDefinition.getTypeNode() instanceof BLangObjectTypeNode) {
                BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeDefinition.getTypeNode();
                objectTypeNode.addWS(ws);
            }
        }
    }

    private BLangObjectTypeNode populateObjectTypeNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isAnonymous) {
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeNodeStack.pop();
        objectTypeNode.pos = pos;
        objectTypeNode.addWS(ws);
        objectTypeNode.isAnonymous = isAnonymous;
        this.varListStack.pop().forEach(variableNode -> {
            objectTypeNode.addField((SimpleVariableNode) variableNode);
        });
        return objectTypeNode;
    }

    void startFieldBlockList() {
        this.objectFieldBlockWs.push(new TreeSet<>());
    }

    void endFiniteType(Set<Whitespace> ws) {
        finiteTypeWsStack.push(ws);
    }

    void endTypeDefinition(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                           boolean publicType) {
        BLangTypeDefinition typeDefinition = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        BLangIdentifier identifierNode = this.createIdentifier(identifierPos, identifier, ws);
        identifierNode.pos = identifierPos;
        typeDefinition.setName(identifierNode);

        if (publicType) {
            typeDefinition.flagSet.add(Flag.PUBLIC);
        }

        BLangUnionTypeNode members = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();

        while (!typeNodeStack.isEmpty()) {
            BLangType memberType = (BLangType) typeNodeStack.pop();
            if (memberType.getKind() == NodeKind.UNION_TYPE_NODE) {
                members.memberTypeNodes.addAll(((BLangUnionTypeNode) memberType).memberTypeNodes);
                members.addWS(memberType.getWS());
            } else {
                members.memberTypeNodes.add(memberType);
            }
        }

        if (!exprNodeStack.isEmpty()) {
            BLangFiniteTypeNode finiteTypeNode = (BLangFiniteTypeNode) TreeBuilder.createFiniteTypeNode();
            finiteTypeNode.addWS(finiteTypeWsStack.pop());
            while (!exprNodeStack.isEmpty()) {
                ExpressionNode expressionNode = exprNodeStack.pop();
                NodeKind exprKind = expressionNode.getKind();
                if (exprKind == NodeKind.LITERAL || exprKind == NodeKind.NUMERIC_LITERAL) {
                    BLangLiteral literal = (BLangLiteral) expressionNode;
                    String strVal = String.valueOf(literal.value);
                    if (literal.type.tag == TypeTags.FLOAT || literal.type.tag == TypeTags.DECIMAL) {
                        literal.value = NumericLiteralSupport.stripDiscriminator(strVal);
                    }
                }
                finiteTypeNode.valueSpace.add((BLangExpression) expressionNode);
            }

            // Reverse the collection so that they would appear in the correct order.
            Collections.reverse(finiteTypeNode.valueSpace);

            if (!members.memberTypeNodes.isEmpty()) {
                BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
                // Generate a name for the anonymous object
                String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
                IdentifierNode anonTypeGenName = createIdentifier(identifierPos, genName);
                typeDef.setName(anonTypeGenName);
                typeDef.flagSet.add(Flag.PUBLIC);
                typeDef.flagSet.add(Flag.ANONYMOUS);

                typeDef.typeNode = finiteTypeNode;
                typeDef.pos = pos;
                this.compUnit.addTopLevelNode(typeDef);

                members.memberTypeNodes.add(createUserDefinedType(pos, ws,
                        (BLangIdentifier) TreeBuilder.createIdentifierNode(), typeDef.name));
            } else {
                members.memberTypeNodes.add(finiteTypeNode);
            }
        }

        if (members.memberTypeNodes.isEmpty()) {
            typeDefinition.typeNode = null;
        } else if (members.memberTypeNodes.size() == 1) {
            BLangType[] memberArray = new BLangType[1];
            members.memberTypeNodes.toArray(memberArray);
            typeDefinition.typeNode = memberArray[0];
        } else {
            typeDefinition.typeNode = members;
        }

        if (finiteTypeWsStack.size() > 0) {
            typeDefinition.addWS(finiteTypeWsStack.pop());
        }

        typeDefinition.pos = pos;
        typeDefinition.addWS(ws);
        Collections.reverse(markdownDocumentationStack);
        attachMarkdownDocumentations(typeDefinition);
        attachAnnotations(typeDefinition);
        this.compUnit.addTopLevelNode(typeDefinition);
    }

    void endObjectAttachedFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, String funcName, DiagnosticPos funcNamePos,
                                      boolean publicFunc, boolean privateFunc, boolean remoteFunc, boolean resourceFunc,
                                      boolean isDeclaration, boolean markdownDocPresent, int annCount) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
        function.name = this.createIdentifier(funcNamePos, funcName);
        function.pos = pos;
        function.addWS(ws);
        function.addWS(this.invocationWsStack.pop());

        function.flagSet.add(Flag.ATTACHED);

        if (publicFunc) {
            function.flagSet.add(Flag.PUBLIC);
        } else if (privateFunc) {
            function.flagSet.add(Flag.PRIVATE);
        }
        if (remoteFunc) {
            function.flagSet.add(Flag.REMOTE);
        }
        if (resourceFunc) {
            function.flagSet.add(Flag.RESOURCE);
        }

        if (isDeclaration) {
            function.body = null;
            function.flagSet.add(Flag.INTERFACE);
            function.interfaceFunction = true;
        } else {
            function.body = (BLangFunctionBody) this.funcBodyNodeStack.pop();

            NodeKind bodyKind = function.body.getKind();
            if (bodyKind == NodeKind.BLOCK_FUNCTION_BODY) {
                this.blockNodeStack.pop();
            } else if (bodyKind == NodeKind.EXTERN_FUNCTION_BODY) {
                function.flagSet.add(Flag.NATIVE);
            }
        }

        function.attachedFunction = true;

        attachAnnotations(function, annCount, false);
        if (markdownDocPresent) {
            attachMarkdownDocumentations(function);
        }

        BLangObjectTypeNode objectNode = (BLangObjectTypeNode) this.typeNodeStack.peek();
        if (Names.USER_DEFINED_INIT_SUFFIX.value.equals(function.name.value)) {
            function.objInitFunction = true;
            if (objectNode.initFunction == null) {
                objectNode.initFunction = function;
                return;
            }
        }

        objectNode.addFunction(function);
    }

    void startAnnotationDef(DiagnosticPos pos) {
        BLangAnnotation annotNode = (BLangAnnotation) TreeBuilder.createAnnotationNode();
        annotNode.pos = pos;
        attachAnnotations(annotNode);
        attachMarkdownDocumentations(annotNode);
        this.annotationStack.add(annotNode);
    }

    void endAnnotationDef(Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos, boolean publicAnnotation,
                          boolean isTypeAttached, boolean isConst) {
        BLangAnnotation annotationNode = (BLangAnnotation) this.annotationStack.pop();
        annotationNode.addWS(ws);
        BLangIdentifier identifierNode = this.createIdentifier(identifierPos, identifier, ws);
        identifierNode.pos = identifierPos;
        annotationNode.setName(identifierNode);

        if (publicAnnotation) {
            annotationNode.flagSet.add(Flag.PUBLIC);
        }

        if (isConst) {
            annotationNode.flagSet.add(Flag.CONSTANT);
        }

        while (!attachPointStack.empty()) {
            annotationNode.addAttachPoint(attachPointStack.pop());
        }

        if (isTypeAttached) {
            annotationNode.typeNode = (BLangType) this.typeNodeStack.pop();
        }

        this.compUnit.addTopLevelNode(annotationNode);
    }

    void startMarkdownDocumentationString(DiagnosticPos currentPos) {
        BLangMarkdownDocumentation markdownDocumentationNode =
                (BLangMarkdownDocumentation) TreeBuilder.createMarkdownDocumentationNode();
        markdownDocumentationNode.pos = currentPos;
        markdownDocumentationStack.push(markdownDocumentationNode);
    }

    void endMarkdownDocumentationString(Set<Whitespace> ws) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        markdownDocumentationNode.addWS(ws);
    }

    void endMarkDownDocumentLine(Set<Whitespace> ws) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        markdownDocumentationNode.addWS(ws);
    }

    void endMarkdownDocumentationText(DiagnosticPos pos, Set<Whitespace> ws, String text) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkdownDocumentationLine documentationDescription =
                (BLangMarkdownDocumentationLine) TreeBuilder.createMarkdownDocumentationTextNode();
        documentationDescription.text = text;
        documentationDescription.pos = pos;
        documentationDescription.addWS(ws);
        markdownDocumentationNode.addDocumentationLine(documentationDescription);
    }

    void endParameterDocumentationLine(Set<Whitespace> ws) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        markdownDocumentationNode.addWS(ws);
    }

    void endDocumentationReference(DiagnosticPos pos, String referenceType, String identifier) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        // Processing the reference name because its of the pattern "service   `". Trim the spaces and trailing "`".
        String processedReferenceType = referenceType.substring(0, (referenceType.length() - 1)).trim().toUpperCase();
        BLangMarkdownReferenceDocumentation referenceDocumentation = createReferenceDocumentation(pos,
                DocumentationReferenceType.valueOf(processedReferenceType), identifier);
        markdownDocumentationNode.addReference(referenceDocumentation);
    }

    // Store single backticked content in Documentation Node.
    void endSingleBacktickedBlock(DiagnosticPos pos, String identifier) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkdownReferenceDocumentation referenceDocumentation =
                createReferenceDocumentation(pos, DocumentationReferenceType.BACKTICK_CONTENT, identifier);
        markdownDocumentationNode.addReference(referenceDocumentation);
    }

    void endParameterDocumentation(DiagnosticPos pos, Set<Whitespace> ws, String parameterName, String description) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkdownParameterDocumentation parameterDocumentationNode =
                (BLangMarkdownParameterDocumentation) TreeBuilder.createMarkdownParameterDocumentationNode();
        parameterDocumentationNode.parameterName = createIdentifier(pos, parameterName, ws);
        parameterDocumentationNode.pos = pos;
        parameterDocumentationNode.addWS(ws);
        parameterDocumentationNode.addParameterDocumentationLine(description);
        BLangMarkDownDeprecatedParametersDocumentation deprecatedParametersDocumentation =
                markdownDocumentationNode.getDeprecatedParametersDocumentation();
        if (deprecatedParametersDocumentation != null) {
            deprecatedParametersDocumentation.addParameter(parameterDocumentationNode);
        } else {
            markdownDocumentationNode.addParameter(parameterDocumentationNode);
        }
    }

    void endParameterDocumentationDescription(Set<Whitespace> ws, String description) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkdownParameterDocumentation parameterDocumentation;
        BLangMarkDownDeprecatedParametersDocumentation deprecatedParametersDocumentation =
                markdownDocumentationNode.getDeprecatedParametersDocumentation();
        if (deprecatedParametersDocumentation != null) {
            parameterDocumentation = deprecatedParametersDocumentation.getParameters().getLast();
        } else {
            parameterDocumentation = markdownDocumentationNode.getParameters().getLast();
        }
        parameterDocumentation.addWS(ws);
        parameterDocumentation.addParameterDocumentationLine(description);
    }

    void endReturnParameterDocumentation(DiagnosticPos pos, Set<Whitespace> ws, String description) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkdownReturnParameterDocumentation returnParameterDocumentation =
                (BLangMarkdownReturnParameterDocumentation) TreeBuilder
                        .createMarkdownReturnParameterDocumentationNode();
        returnParameterDocumentation.pos = pos;
        returnParameterDocumentation.addWS(ws);
        returnParameterDocumentation.addReturnParameterDocumentationLine(description);
        markdownDocumentationNode.setReturnParameter(returnParameterDocumentation);
    }

    void endReturnParameterDocumentationDescription(Set<Whitespace> ws, String description) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkdownReturnParameterDocumentation returnParameter = markdownDocumentationNode.getReturnParameter();
        returnParameter.addWS(ws);
        returnParameter.addReturnParameterDocumentationLine(description);
    }

    void endDeprecationAnnotationDocumentation(DiagnosticPos pos, Set<Whitespace> ws, String description) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkDownDeprecationDocumentation deprecationAnnotationDocumentation =
                (BLangMarkDownDeprecationDocumentation) TreeBuilder.createMarkDownDeprecationAttributeNode();
        deprecationAnnotationDocumentation.pos = pos;
        deprecationAnnotationDocumentation.addWS(ws);
        deprecationAnnotationDocumentation.addDeprecationLine(description);
        markdownDocumentationNode.setDeprecationDocumentation(deprecationAnnotationDocumentation);
    }

    void endDeprecateAnnotationDocumentationDescription(Set<Whitespace> ws, String description) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkDownDeprecationDocumentation deprecationAnnotationDocumentation =
                markdownDocumentationNode.getDeprecationDocumentation();
        deprecationAnnotationDocumentation.addWS(ws);
        deprecationAnnotationDocumentation.addDeprecationDocumentationLine(description);
    }

    void endDeprecatedParametersDocumentation(DiagnosticPos pos, Set<Whitespace> ws) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkDownDeprecatedParametersDocumentation deprecatedParametersDocumentation =
                (BLangMarkDownDeprecatedParametersDocumentation)
                        TreeBuilder.createMarkDownDeprecatedParametersAttributeNode();
        deprecatedParametersDocumentation.pos = pos;
        deprecatedParametersDocumentation.addWS(ws);
        markdownDocumentationNode.setDeprecatedParametersDocumentation(deprecatedParametersDocumentation);
    }

    void startAnnotationAttachment(DiagnosticPos currentPos) {
        BLangAnnotationAttachment annotAttachmentNode =
                (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        annotAttachmentNode.pos = currentPos;
        annotAttachmentStack.push(annotAttachmentNode);
    }

    void setAnnotationAttachmentName(Set<Whitespace> ws, boolean hasExpr, DiagnosticPos currentPos,
                                     boolean popAnnAttachment) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangAnnotationAttachment bLangAnnotationAttachment = (BLangAnnotationAttachment) annotAttachmentStack.peek();
        bLangAnnotationAttachment.pos = currentPos;
        bLangAnnotationAttachment.addWS(nameReference.ws);
        bLangAnnotationAttachment.addWS(ws);
        bLangAnnotationAttachment.setAnnotationName(nameReference.name);
        bLangAnnotationAttachment.setPackageAlias(nameReference.pkgAlias);
        if (hasExpr) {
            bLangAnnotationAttachment.setExpression(exprNodeStack.pop());
        }
        if (popAnnAttachment) {
            annotAttachmentStack.pop();
        }
    }

    private void attachAnnotations(AnnotatableNode annotatableNode) {
        annotAttachmentStack.forEach(annotatableNode::addAnnotationAttachment);
        annotAttachmentStack.clear();
    }

    private void attachMarkdownDocumentations(DocumentableNode documentableNode) {
        if (!markdownDocumentationStack.empty()) {
            documentableNode.setMarkdownDocumentationAttachment(markdownDocumentationStack.pop());
        }
    }

    private void attachAnnotations(AnnotatableNode annotatableNode, int count, boolean peek) {
        if (count == 0 || annotAttachmentStack.empty()) {
            return;
        }

        List<AnnotationAttachmentNode> tempAnnotAttachments = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (annotAttachmentStack.empty()) {
                break;
            }

            if (peek) {
                tempAnnotAttachments.add(annotAttachmentStack.peek());
            } else {
                tempAnnotAttachments.add(annotAttachmentStack.pop());
            }
        }
        // reversing the collected annotations to preserve the original order
        Collections.reverse(tempAnnotAttachments);
        tempAnnotAttachments.forEach(annotatableNode::addAnnotationAttachment);
    }

    void addAssignmentStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        ExpressionNode rExprNode = exprNodeStack.pop();
        ExpressionNode lExprNode = exprNodeStack.pop();
        BLangAssignment assignmentNode = (BLangAssignment) TreeBuilder.createAssignmentNode();
        validateLvexpr(lExprNode, DiagnosticCode.INVALID_INVOCATION_LVALUE_ASSIGNMENT);
        assignmentNode.setExpression(rExprNode);
        assignmentNode.pos = pos;
        assignmentNode.addWS(ws);
        assignmentNode.varRef = ((BLangVariableReference) lExprNode);
        addStmtToCurrentBlock(assignmentNode);
    }

    private void validateLvexpr(ExpressionNode lExprNode, DiagnosticCode errorCode) {
        if (lExprNode.getKind() == NodeKind.INVOCATION) {
            dlog.error(((BLangInvocation) lExprNode).pos, errorCode);
        }
        if (lExprNode.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR
                || lExprNode.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            validateLvexpr(((BLangAccessExpression) lExprNode).expr, errorCode);
        }
    }

    private BLangMarkdownReferenceDocumentation createReferenceDocumentation(DiagnosticPos pos,
                                                                             DocumentationReferenceType type,
                                                                             String identifier) {
        BLangMarkdownReferenceDocumentation referenceDocumentation =
                (BLangMarkdownReferenceDocumentation) TreeBuilder.createMarkdownReferenceDocumentationNode();
        referenceDocumentation.type = type;
        referenceDocumentation.pos = pos;
        referenceDocumentation.referenceName = identifier;

        return referenceDocumentation;
    }

    void addTupleDestructuringStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTupleDestructure stmt = (BLangTupleDestructure) TreeBuilder.createTupleDestructureStatementNode();
        stmt.pos = pos;
        stmt.addWS(ws);
        stmt.expr = (BLangExpression) exprNodeStack.pop();
        stmt.varRef = (BLangTupleVarRef) exprNodeStack.pop();
        stmt.addWS(stmt.varRef.getWS());
        addStmtToCurrentBlock(stmt);
    }

    void addRecordDestructuringStatement(DiagnosticPos pos, Set<Whitespace> ws) {

        BLangRecordDestructure stmt = (BLangRecordDestructure) TreeBuilder.createRecordDestructureStatementNode();
        stmt.pos = pos;
        stmt.addWS(ws);
        stmt.expr = (BLangExpression) exprNodeStack.pop();
        stmt.varRef = (BLangRecordVarRef) exprNodeStack.pop();
        addStmtToCurrentBlock(stmt);
    }

    void addErrorDestructuringStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangErrorDestructure stmt = (BLangErrorDestructure) TreeBuilder.createErrorDestructureStatementNode();
        stmt.pos = pos;
        stmt.addWS(ws);
        stmt.expr = (BLangExpression) exprNodeStack.pop();
        stmt.varRef = (BLangErrorVarRef) exprNodeStack.pop();
        addStmtToCurrentBlock(stmt);
    }

    void startForeachStatement() {
        startBlock();
    }

    void addCompoundAssignmentStatement(DiagnosticPos pos, Set<Whitespace> ws, String operator) {
        BLangCompoundAssignment assignmentNode =
                (BLangCompoundAssignment) TreeBuilder.createCompoundAssignmentNode();
        assignmentNode.setExpression(exprNodeStack.pop());

        assignmentNode.setVariable((BLangVariableReference) exprNodeStack.pop());
        assignmentNode.pos = pos;
        assignmentNode.addWS(ws);
        assignmentNode.addWS(this.operatorWs.pop());
        assignmentNode.opKind = OperatorKind.valueFrom(operator);
        addStmtToCurrentBlock(assignmentNode);
    }

    void addCompoundOperator(Set<Whitespace> ws) {
        this.operatorWs.push(ws);
    }

    void addForeachStatementWithSimpleVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                                           DiagnosticPos identifierPos, boolean isDeclaredWithVar) {
        BLangSimpleVariableDef variableDefinitionNode = createSimpleVariableDef(pos, ws, identifier, identifierPos,
                false, false, isDeclaredWithVar);
        if (!this.bindingPatternIdentifierWS.isEmpty()) {
            variableDefinitionNode.addWS(this.bindingPatternIdentifierWS.pop());
        }
        addForeachStatement(pos, ws, variableDefinitionNode, isDeclaredWithVar);
    }

    void addForeachStatementWithRecordVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws,
                                                           boolean isDeclaredWithVar) {
        BLangRecordVariableDef variableDefinitionNode = createRecordVariableDef(pos, ws, false, false,
                isDeclaredWithVar);
        addForeachStatement(pos, ws, variableDefinitionNode, isDeclaredWithVar);
    }

    void addForeachStatementWithTupleVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws,
                                                          boolean isDeclaredWithVar) {
        BLangTupleVariableDef variableDefinitionNode = createTupleVariableDef(pos, ws, false, false, isDeclaredWithVar);
        addForeachStatement(pos, ws, variableDefinitionNode, isDeclaredWithVar);
    }

    void addForeachStatementWithErrorVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws,
                                                          boolean isDeclaredWithVar) {
        BLangErrorVariableDef variableDefinitionNode = createErrorVariableDef(pos, ws, false, false, isDeclaredWithVar);
        addForeachStatement(pos, ws, variableDefinitionNode, isDeclaredWithVar);
    }

    private void addForeachStatement(DiagnosticPos pos, Set<Whitespace> ws,
                                     VariableDefinitionNode variableDefinitionNode, boolean isDeclaredWithVar) {
        BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.addWS(ws);
        foreach.pos = pos;
        foreach.setVariableDefinitionNode(variableDefinitionNode);
        foreach.setCollection(this.exprNodeStack.pop());
        foreach.isDeclaredWithVar = isDeclaredWithVar;

        BLangBlockStmt foreachBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        foreachBlock.pos = pos;
        foreach.setBody(foreachBlock);
        addStmtToCurrentBlock(foreach);
    }

    void startWhileStmt() {
        startBlock();
    }

    void addWhileStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangWhile whileNode = (BLangWhile) TreeBuilder.createWhileNode();
        whileNode.setCondition(exprNodeStack.pop());
        whileNode.pos = pos;
        whileNode.addWS(ws);
        BLangBlockStmt whileBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        whileBlock.pos = pos;
        whileNode.setBody(whileBlock);
        addStmtToCurrentBlock(whileNode);
    }

    void startBlockStmt() {
        startBlock();
    }

    void addBlockStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangBlockStmt block = (BLangBlockStmt) this.blockNodeStack.pop();
        block.pos = pos;
        block.addWS(ws);
        addStmtToCurrentBlock(block);
    }

    void startLockStmt() {
        startBlock();
    }

    void addLockStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangLock lockNode = (BLangLock) TreeBuilder.createLockNode();
        lockNode.pos = pos;
        lockNode.addWS(ws);
        BLangBlockStmt lockBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        lockBlock.pos = pos;
        lockNode.setBody(lockBlock);
        addStmtToCurrentBlock(lockNode);
    }

    public void addContinueStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangContinue nextNode = (BLangContinue) TreeBuilder.createContinueNode();
        nextNode.pos = pos;
        nextNode.addWS(ws);
        addStmtToCurrentBlock(nextNode);
    }

    void addBreakStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangBreak breakNode = (BLangBreak) TreeBuilder.createBreakNode();
        breakNode.pos = pos;
        breakNode.addWS(ws);
        addStmtToCurrentBlock(breakNode);
    }

    void addReturnStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean exprAvailable) {
        BLangReturn retStmt = (BLangReturn) TreeBuilder.createReturnNode();
        retStmt.pos = pos;
        retStmt.addWS(ws);
        if (exprAvailable) {
            retStmt.expr = (BLangExpression) this.exprNodeStack.pop();
        } else {
            BLangLiteral nilLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
            nilLiteral.pos = pos;
            nilLiteral.value = Names.NIL_VALUE;
            nilLiteral.type = symTable.nilType;
            retStmt.expr = nilLiteral;
        }
        addStmtToCurrentBlock(retStmt);
    }

    void startTransactionStmt() {
        transactionNodeStack.push(TreeBuilder.createTransactionNode());
        startBlock();
    }

    void addTransactionBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt transactionBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        transactionBlock.pos = pos;
        transactionNode.addWS(ws);
        transactionNode.setTransactionBody(transactionBlock);
    }

    void endTransactionPropertyInitStatementList(Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        transactionNode.addWS(ws);
    }

    void startOnretryBlock() {
        startBlock();
    }

    void addOnretryBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt onretryBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        onretryBlock.pos = pos;
        transactionNode.addWS(ws);
        transactionNode.setOnRetryBody(onretryBlock);
    }

    public void startCommittedBlock() {
        startBlock();
    }

    public void endCommittedBlock(DiagnosticPos currentPos, Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt committedBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        committedBlock.pos = currentPos;
        transactionNode.addWS(ws);
        transactionNode.setCommittedBody(committedBlock);
    }

    public void startAbortedBlock() {
        startBlock();
    }

    public void endAbortedBlock(DiagnosticPos currentPos, Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt abortedBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        abortedBlock.pos = currentPos;
        transactionNode.addWS(ws);
        transactionNode.setAbortedBody(abortedBlock);
    }

    void endTransactionStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.pop();
        transaction.pos = pos;
        transaction.addWS(ws);
        addStmtToCurrentBlock(transaction);

        // TODO This is a temporary workaround to flag coordinator service start
        boolean transactionsModuleAlreadyImported = this.imports.stream()
                .anyMatch(importPackage -> importPackage.orgName.value.equals(Names.BALLERINA_ORG.value)
                        && importPackage.pkgNameComps.get(0).value.equals(Names.TRANSACTION_PACKAGE.value));

        if (!transactionsModuleAlreadyImported) {
            List<String> nameComps = getPackageNameComps(Names.TRANSACTION_PACKAGE.value);
            addImportPackageDeclaration(pos, null, Names.TRANSACTION_ORG.value, nameComps, Names.EMPTY.value,
                    Names.DOT.value + Names.TRANSACTION_PACKAGE.value);
        }
    }

    void addAbortStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangAbort abortNode = (BLangAbort) TreeBuilder.createAbortNode();
        abortNode.pos = pos;
        abortNode.addWS(ws);
        addStmtToCurrentBlock(abortNode);
    }

    void addRetryStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangRetry retryNode = (BLangRetry) TreeBuilder.createRetryNode();
        retryNode.pos = pos;
        retryNode.addWS(ws);
        addStmtToCurrentBlock(retryNode);
    }

    void addRetryCountExpression(Set<Whitespace> ws) {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.peek();
        transaction.addWS(ws);
        transaction.retryCount = (BLangExpression) exprNodeStack.pop();
    }

    void startIfElseNode(DiagnosticPos pos) {
        BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = pos;
        ifElseStatementStack.push(ifNode);
        startBlock();
    }

    void addIfBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        IfNode ifNode = ifElseStatementStack.peek();
        ((BLangIf) ifNode).pos = pos;
        ifNode.addWS(ws);
        ifNode.setCondition(exprNodeStack.pop());
        BlockStatementNode blockNode = (BlockStatementNode) blockNodeStack.pop();
        ((BLangBlockStmt) blockNode).pos = pos;
        ifNode.setBody(blockNode);
    }

    void addElseIfBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        IfNode elseIfNode = ifElseStatementStack.pop();
        ((BLangIf) elseIfNode).pos = pos;
        elseIfNode.setCondition(exprNodeStack.pop());
        BlockStatementNode blockNode = (BlockStatementNode) blockNodeStack.pop();
        ((BLangBlockStmt) blockNode).pos = pos;
        elseIfNode.setBody(blockNode);
        elseIfNode.addWS(ws);

        IfNode parentIfNode = ifElseStatementStack.peek();
        while (parentIfNode.getElseStatement() != null) {
            parentIfNode = (IfNode) parentIfNode.getElseStatement();
        }
        parentIfNode.setElseStatement(elseIfNode);
    }

    void addElseBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        IfNode ifNode = ifElseStatementStack.peek();
        while (ifNode.getElseStatement() != null) {
            ifNode = (IfNode) ifNode.getElseStatement();
        }
        BlockStatementNode elseBlock = (BlockStatementNode) blockNodeStack.pop();
        elseBlock.addWS(ws);
        ((BLangBlockStmt) elseBlock).pos = pos;
        ifNode.setElseStatement(elseBlock);
    }

    void endIfElseNode(Set<Whitespace> ws) {
        IfNode ifNode = ifElseStatementStack.pop();
        ifNode.addWS(ws);
        addStmtToCurrentBlock(ifNode);
    }

    void createMatchNode(DiagnosticPos pos) {
        if (this.matchStmtStack == null) {
            this.matchStmtStack = new ArrayDeque<>();
        }

        BLangMatch matchStmt = (BLangMatch) TreeBuilder.createMatchStatement();
        matchStmt.pos = pos;

        this.matchStmtStack.addFirst(matchStmt);
    }

    void completeMatchNode(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangMatch matchStmt = this.matchStmtStack.removeFirst();
        matchStmt.pos = pos;
        matchStmt.addWS(ws);
        matchStmt.expr = (BLangExpression) this.exprNodeStack.pop();
        addStmtToCurrentBlock(matchStmt);
    }

    void startMatchStmtPattern() {
        startBlock();
    }

    void addMatchStmtStaticBindingPattern(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangMatchStaticBindingPatternClause patternClause =
                (BLangMatchStaticBindingPatternClause) TreeBuilder.createMatchStatementStaticBindingPattern();
        patternClause.pos = pos;
        patternClause.addWS(ws);

        patternClause.literal = (BLangExpression) this.exprNodeStack.pop();
        patternClause.body = (BLangBlockStmt) blockNodeStack.pop();
        patternClause.body.pos = pos;
        this.matchStmtStack.peekFirst().patternClauses.add(patternClause);
    }

    void addMatchStmtStructuredBindingPattern(DiagnosticPos pos, Set<Whitespace> ws, boolean isTypeGuardPresent) {
        BLangMatchStructuredBindingPatternClause patternClause =
                (BLangMatchStructuredBindingPatternClause) TreeBuilder.createMatchStatementStructuredBindingPattern();
        patternClause.pos = pos;
        patternClause.addWS(ws);
        if (!this.bindingPatternIdentifierWS.isEmpty()) {
            patternClause.addWS(this.bindingPatternIdentifierWS.pop());
        }

        patternClause.bindingPatternVariable = this.varStack.pop();
        if (this.errorMatchPatternWS.size() > 0) {
            patternClause.bindingPatternVariable.addWS(this.errorMatchPatternWS.pop());
        }

        // TODO: add rest match pattern ws to rest detail field in error variable when the rest detail is added.
        if (!this.restMatchPatternWS.isEmpty()) {
            patternClause.bindingPatternVariable.addWS(this.restMatchPatternWS.pop());
        }

        patternClause.body = (BLangBlockStmt) blockNodeStack.pop();
        patternClause.body.pos = pos;

        if (isTypeGuardPresent) {
            patternClause.typeGuardExpr = (BLangExpression) exprNodeStack.pop();
        }
        this.matchStmtStack.peekFirst().patternClauses.add(patternClause);
    }

    void addWorkerSendStmt(DiagnosticPos pos, Set<Whitespace> ws, String workerName, boolean hasKey) {
        BLangWorkerSend workerSendNode = (BLangWorkerSend) TreeBuilder.createWorkerSendNode();
        workerSendNode.setWorkerName(this.createIdentifier(pos, workerName, ws));
        workerSendNode.expr = (BLangExpression) exprNodeStack.pop();
        workerSendNode.pos = pos;
        workerSendNode.addWS(ws);
        //added to use for channels as well
        if (hasKey) {
            workerSendNode.keyExpr = workerSendNode.expr;
            workerSendNode.expr = (BLangExpression) exprNodeStack.pop();
            workerSendNode.isChannel = true;
        }
        addStmtToCurrentBlock(workerSendNode);
    }

    void addWorkerReceiveExpr(DiagnosticPos pos, Set<Whitespace> ws, String workerName, boolean hasKey) {
        BLangWorkerReceive workerReceiveExpr = (BLangWorkerReceive) TreeBuilder.createWorkerReceiveNode();
        workerReceiveExpr.setWorkerName(this.createIdentifier(pos, workerName, ws));
        workerReceiveExpr.pos = pos;
        workerReceiveExpr.addWS(ws);
        //if there are two expressions, this is a channel receive and the top expression is the key
        // TODO: Not needed?
        if (hasKey) {
            workerReceiveExpr.keyExpr = (BLangExpression) exprNodeStack.pop();
            workerReceiveExpr.isChannel = true;
        }
        addExpressionNode(workerReceiveExpr);
    }

    void addWorkerFlushExpr(DiagnosticPos pos, Set<Whitespace> ws, String workerName) {
        BLangWorkerFlushExpr workerFlushExpr = TreeBuilder.createWorkerFlushExpressionNode();
        if (workerName != null) {
            workerFlushExpr.workerIdentifier = createIdentifier(pos, workerName, ws);
        }
        workerFlushExpr.pos = pos;
        workerFlushExpr.addWS(ws);
        addExpressionNode(workerFlushExpr);
    }

    void addWorkerSendSyncExpr(DiagnosticPos pos, Set<Whitespace> ws, String workerName) {
        BLangWorkerSyncSendExpr workerSendExpr = TreeBuilder.createWorkerSendSyncExprNode();
        workerSendExpr.setWorkerName(this.createIdentifier(pos, workerName, ws));
        workerSendExpr.expr = (BLangExpression) exprNodeStack.pop();
        workerSendExpr.pos = pos;
        workerSendExpr.addWS(ws);
        addExpressionNode(workerSendExpr);
    }

    void addExpressionStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.pos = pos;
        exprStmt.addWS(ws);
        exprStmt.expr = (BLangExpression) exprNodeStack.pop();
        addStmtToCurrentBlock(exprStmt);
    }

    void startServiceDef(DiagnosticPos pos) {
        BLangService serviceNode = (BLangService) TreeBuilder.createServiceNode();
        serviceNode.pos = pos;
        attachMarkdownDocumentations(serviceNode);
        serviceNodeStack.push(serviceNode);
    }

    void endServiceDef(DiagnosticPos pos, Set<Whitespace> ws, String serviceName, DiagnosticPos identifierPos,
                       boolean isAnonServiceValue) {
        endServiceDef(pos, ws, serviceName, identifierPos, isAnonServiceValue, this.annotAttachmentStack.size());
    }

    void endServiceDef(DiagnosticPos pos, Set<Whitespace> ws, String serviceName, DiagnosticPos identifierPos,
                       boolean isAnonServiceValue, int annotCount) {
        // Any Service can be represented in two major components.
        //  1) A anonymous type node (Object)
        //  2) Variable assignment with "serviceName".
        //      This is a global variable if the service is defined in module level.
        //      Otherwise (isAnonServiceValue = true) it is a local variable definition, which is written by user.
        BLangService serviceNode = (BLangService) serviceNodeStack.pop();
        attachAnnotations(serviceNode, annotCount, false);
        serviceNode.pos = pos;
        serviceNode.addWS(ws);
        serviceNode.isAnonymousServiceValue = isAnonServiceValue;
        if (serviceName == null) {
            serviceName = this.anonymousModelHelper.getNextAnonymousServiceVarKey(pos.src.pkgID);
            identifierPos = pos;
        }
        String serviceTypeName = this.anonymousModelHelper.getNextAnonymousServiceTypeKey(pos.src.pkgID, serviceName);
        BLangIdentifier serviceVar = createIdentifier(identifierPos, serviceName, ws);
        serviceVar.pos = identifierPos;
        serviceNode.setName(serviceVar);
        if (!isAnonServiceValue) {
            this.exprNodeListStack.pop().forEach(expr -> serviceNode.attachedExprs.add((BLangExpression) expr));
            if (this.commaWsStack.size() > 0) {
                serviceNode.addWS(this.commaWsStack.pop());
            }
        }
        // We add all service nodes to top level, only for future reference.
        this.compUnit.addTopLevelNode(serviceNode);

        // 1) Define type nodeDefinition for service type.
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        BLangIdentifier serviceTypeID = createIdentifier(identifierPos, serviceTypeName, ws);
        serviceTypeID.pos = pos;
        typeDef.setName(serviceTypeID);
        typeDef.flagSet.add(Flag.SERVICE);
        typeDef.typeNode = (BLangType) this.typeNodeStack.pop();
        typeDef.pos = pos;
        serviceNode.serviceTypeDefinition = typeDef;
        this.compUnit.addTopLevelNode(typeDef);

        // 2) Create service constructor.
        final BLangServiceConstructorExpr serviceConstNode = (BLangServiceConstructorExpr) TreeBuilder
                .createServiceConstructorNode();
        serviceConstNode.serviceNode = serviceNode;
        serviceConstNode.pos = pos;
        serviceConstNode.addWS(ws);
        addExpressionNode(serviceConstNode);

        // Crate Global variable for service.
        if (!isAnonServiceValue) {
            BLangSimpleVariable var = (BLangSimpleVariable) generateBasicVarNodeWithoutType(identifierPos,
                    Collections.emptySet(),
                    serviceName, identifierPos,
                    true);
            var.flagSet.add(Flag.FINAL);
            var.flagSet.add(Flag.SERVICE);
            var.typeNode = createUserDefinedType(pos, ws, (BLangIdentifier) TreeBuilder.createIdentifierNode(),
                    typeDef.name);
            serviceNode.variableNode = var;
            this.compUnit.addTopLevelNode(var);
        }
    }

    void createXMLQName(DiagnosticPos pos, Set<Whitespace> ws, String localname, String prefix) {
        BLangXMLQName qname = (BLangXMLQName) TreeBuilder.createXMLQNameNode();
        qname.localname = createIdentifier(pos, localname, ws);
        qname.prefix = createIdentifier(pos, prefix, ws);
        qname.pos = pos;
        qname.addWS(ws);
        addExpressionNode(qname);
    }

    void createXMLAttribute(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangXMLAttribute xmlAttribute = (BLangXMLAttribute) TreeBuilder.createXMLAttributeNode();
        xmlAttribute.value = (BLangXMLQuotedString) exprNodeStack.pop();
        xmlAttribute.name = (BLangExpression) exprNodeStack.pop();
        xmlAttribute.pos = pos;
        xmlAttribute.addWS(ws);
        xmlAttributeNodeStack.push(xmlAttribute);
    }

    void attachXmlLiteralWS(Set<Whitespace> ws) {
        this.exprNodeStack.peek().addWS(ws);
    }

    void startXMLElement(DiagnosticPos pos, Set<Whitespace> ws, boolean isRoot) {
        BLangXMLElementLiteral xmlElement = (BLangXMLElementLiteral) TreeBuilder.createXMLElementLiteralNode();
        BLangExpression startTag = (BLangExpression) exprNodeStack.pop();
        xmlElement.addWS(ws);
        xmlElement.startTagName = startTag;
        xmlElement.pos = pos;
        xmlElement.isRoot = isRoot;
        xmlAttributeNodeStack.forEach(xmlElement::addAttribute);
        xmlAttributeNodeStack.clear();
        addExpressionNode(xmlElement);
    }

    void endXMLElement(Set<Whitespace> ws) {
        BLangExpression endTag = (BLangExpression) exprNodeStack.pop();
        BLangXMLElementLiteral xmlElement = (BLangXMLElementLiteral) exprNodeStack.peek();
        xmlElement.addWS(ws);
        xmlElement.endTagName = endTag;
    }

    void createXMLQuotedLiteral(DiagnosticPos pos,
                                Set<Whitespace> ws,
                                Stack<String> precedingTextFragments,
                                String endingText,
                                QuoteType quoteType) {
        List<BLangExpression> templateExprs =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText);
        BLangXMLQuotedString quotedString = (BLangXMLQuotedString) TreeBuilder.createXMLQuotedStringNode();
        quotedString.pos = pos;
        quotedString.quoteType = quoteType;
        quotedString.textFragments = templateExprs;
        addExpressionNode(quotedString);
    }

    void addChildToXMLElement(Set<Whitespace> ws) {
        XMLLiteralNode child = (XMLLiteralNode) exprNodeStack.pop();
        child.addWS(ws);
        BLangXMLElementLiteral parentXMLExpr = (BLangXMLElementLiteral) exprNodeStack.peek();
        parentXMLExpr.addChild(child);
    }

    void createXMLTextLiteral(DiagnosticPos pos,
                              Set<Whitespace> ws,
                              Stack<String> precedingTextFragments,
                              String endingText) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.textFragments =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText);
        xmlTextLiteral.pos = pos;
        addExpressionNode(xmlTextLiteral);
    }

    void addXMLTextToElement(DiagnosticPos pos,
                             Set<Whitespace> ws,
                             Stack<String> precedingTextFragments,
                             String endingText) {

        List<BLangExpression> templateExprs =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText);
        BLangXMLElementLiteral parentElement = (BLangXMLElementLiteral) exprNodeStack.peek();
        templateExprs.forEach(parentElement::addChild);
    }

    void createXMLCommentLiteral(DiagnosticPos pos,
                                 Set<Whitespace> ws,
                                 Stack<String> precedingTextFragments,
                                 String endingText) {

        BLangXMLCommentLiteral xmlCommentLiteral = (BLangXMLCommentLiteral) TreeBuilder.createXMLCommentLiteralNode();
        xmlCommentLiteral.textFragments =
                getExpressionsInTemplate(pos, null, precedingTextFragments, endingText);
        xmlCommentLiteral.pos = pos;
        xmlCommentLiteral.addWS(ws);
        addExpressionNode(xmlCommentLiteral);
    }

    void createXMLPILiteral(DiagnosticPos pos,
                            Set<Whitespace> ws,
                            String targetQName,
                            Stack<String> precedingTextFragments,
                            String endingText) {
        List<BLangExpression> dataExprs =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText);
        addLiteralValue(pos, ws, TypeTags.STRING, targetQName);

        BLangXMLProcInsLiteral xmlProcInsLiteral =
                (BLangXMLProcInsLiteral) TreeBuilder.createXMLProcessingIntsructionLiteralNode();
        xmlProcInsLiteral.pos = pos;
        xmlProcInsLiteral.dataFragments = dataExprs;
        xmlProcInsLiteral.target = (BLangLiteral) exprNodeStack.pop();
        addExpressionNode(xmlProcInsLiteral);
    }

    void addXMLNSDeclaration(DiagnosticPos pos,
                             Set<Whitespace> ws,
                             String namespaceUri,
                             String prefix,
                             DiagnosticPos prefixPos,
                             boolean isTopLevel) {
        BLangXMLNS xmlns = (BLangXMLNS) TreeBuilder.createXMLNSNode();
        BLangIdentifier prefixIdentifer = createIdentifier(prefixPos, prefix, ws);

        addLiteralValue(pos, removeNthFromStart(ws, 1), TypeTags.STRING, namespaceUri);
        xmlns.namespaceURI = (BLangLiteral) exprNodeStack.pop();
        xmlns.prefix = prefixIdentifer;
        xmlns.pos = pos;
        xmlns.addWS(ws);

        if (isTopLevel) {
            this.compUnit.addTopLevelNode(xmlns);
            return;
        }

        BLangXMLNSStatement xmlnsStmt = (BLangXMLNSStatement) TreeBuilder.createXMLNSDeclrStatementNode();
        xmlnsStmt.xmlnsDecl = xmlns;
        xmlnsStmt.pos = pos;
        addStmtToCurrentBlock(xmlnsStmt);
    }

    void createStringTemplateLiteral(DiagnosticPos pos, Set<Whitespace> ws, Stack<String> precedingTextFragments,
                                     String endingText) {
        BLangStringTemplateLiteral stringTemplateLiteral =
                (BLangStringTemplateLiteral) TreeBuilder.createStringTemplateLiteralNode();
        stringTemplateLiteral.exprs =
                getExpressionsInTemplate(pos, null, precedingTextFragments, endingText);
        stringTemplateLiteral.addWS(ws);
        stringTemplateLiteral.pos = pos;
        addExpressionNode(stringTemplateLiteral);
    }

    void createXmlAttributesRefExpr(DiagnosticPos pos, Set<Whitespace> ws, boolean singleAttribute) {
        BLangXMLAttributeAccess xmlAttributeAccess =
                (BLangXMLAttributeAccess) TreeBuilder.createXMLAttributeAccessNode();
        xmlAttributeAccess.pos = pos;
        xmlAttributeAccess.addWS(ws);
        if (singleAttribute) {
            xmlAttributeAccess.indexExpr = (BLangExpression) exprNodeStack.pop();
        }
        xmlAttributeAccess.expr = (BLangVariableReference) exprNodeStack.pop();
        addExpressionNode(xmlAttributeAccess);
    }

    void addIntRangeExpression(DiagnosticPos pos,
                               Set<Whitespace> ws,
                               boolean includeStart,
                               boolean includeEnd,
                               boolean noUpperBound) {
        BLangIntRangeExpression intRangeExpr = (BLangIntRangeExpression) TreeBuilder.createIntRangeExpression();
        intRangeExpr.pos = pos;
        intRangeExpr.addWS(ws);
        if (!noUpperBound) {
            intRangeExpr.endExpr = (BLangExpression) this.exprNodeStack.pop();
        }
        intRangeExpr.startExpr = (BLangExpression) this.exprNodeStack.pop();
        intRangeExpr.includeStart = includeStart;
        intRangeExpr.includeEnd = includeEnd;
        exprNodeStack.push(intRangeExpr);
    }

    void addNamedArgument(DiagnosticPos pos, Set<Whitespace> ws, String name) {
        BLangNamedArgsExpression namedArg = (BLangNamedArgsExpression) TreeBuilder.createNamedArgNode();
        namedArg.pos = pos;
        namedArg.addWS(ws);
        namedArg.name = this.createIdentifier(pos, name, ws);
        namedArg.expr = (BLangExpression) this.exprNodeStack.pop();
        addExpressionNode(namedArg);
    }

    void addRestArgument(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangRestArgsExpression varArgs = (BLangRestArgsExpression) TreeBuilder.createVarArgsNode();
        varArgs.pos = pos;
        varArgs.addWS(ws);
        varArgs.expr = (BLangExpression) this.exprNodeStack.pop();
        addExpressionNode(varArgs);
    }

    void addDefaultableParam(DiagnosticPos pos, Set<Whitespace> ws) {
        List<BLangVariable> params = this.varListStack.peek();
        BLangSimpleVariable var = (BLangSimpleVariable) params.get(params.size() - 1);
        var.expr = (BLangExpression) this.exprNodeStack.pop();
        var.pos.eCol = var.expr.pos.eCol;
        var.pos.eLine = var.expr.pos.eLine;
        var.addWS(ws);
    }

    void addRestParam(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                      int annotCount) {
        BLangSimpleVariable restParam = (BLangSimpleVariable) this.generateBasicVarNode(pos, ws, identifier,
                identifierPos, false);
        attachAnnotations(restParam, annotCount, false);
        restParam.pos = pos;

        BLangArrayType typeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        typeNode.elemtype = restParam.typeNode;
        typeNode.dimensions = 1;
        restParam.typeNode = typeNode;
        this.restParamStack.push(restParam);
    }

    // Private methods

    private List<BLangExpression> getExpressionsInTemplate(DiagnosticPos pos,
                                                           Set<Whitespace> ws,
                                                           Stack<String> precedingTextFragments,
                                                           String endingText) {
        List<BLangExpression> expressions = new ArrayList<>();
        String originalValue = endingText;
        endingText = endingText == null ? "" : StringEscapeUtils.unescapeJava(endingText);
        addLiteralValue(pos, ws, TypeTags.STRING, endingText, originalValue);
        expressions.add((BLangExpression) exprNodeStack.pop());

        while (!precedingTextFragments.empty()) {
            expressions.add((BLangExpression) exprNodeStack.pop());
            String textFragment = precedingTextFragments.pop();
            originalValue = textFragment;
            textFragment = textFragment == null ? "" : StringEscapeUtils.unescapeJava(textFragment);
            addLiteralValue(pos, ws, TypeTags.STRING, textFragment, originalValue);
            expressions.add((BLangExpression) exprNodeStack.pop());
        }

        Collections.reverse(expressions);
        return expressions;
    }

    void endCompilationUnit(Set<Whitespace> ws) {
        compUnit.addWS(ws);
    }

    void endCallableParamList(Set<Whitespace> ws) {
        this.invokableNodeStack.peek().addWS(ws);
    }

    void endFuncTypeParamList(Set<Whitespace> ws) {
        this.commaWsStack.push(ws);
    }

    private Set<Whitespace> removeNthFromLast(Set<Whitespace> ws, int n) {
        if (ws == null) {
            return null;
        }
        return removeNth(((TreeSet<Whitespace>) ws).descendingIterator(), n);
    }

    private Set<Whitespace> removeNthFromStart(Set<Whitespace> ws, int n) {
        if (ws == null) {
            return null;
        }
        return removeNth(ws.iterator(), n);
    }

    private Set<Whitespace> removeNth(Iterator<Whitespace> iterator, int n) {
        int i = 0;
        while (iterator.hasNext()) {
            Whitespace next = iterator.next();
            if (i++ == n) {
                Set<Whitespace> varWS = new TreeSet<>();
                varWS.add(next);
                iterator.remove();
                return varWS;
            }
        }
        return null;
    }

    private BLangUserDefinedType createUserDefinedType(DiagnosticPos pos,
                                                       Set<Whitespace> ws,
                                                       BLangIdentifier pkgAlias,
                                                       BLangIdentifier name) {
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.pos = pos;
        userDefinedType.addWS(ws);
        userDefinedType.pkgAlias = pkgAlias;
        userDefinedType.typeName = name;
        return userDefinedType;
    }

    private List<String> getPackageNameComps(String sourcePkg) {
        String[] pkgParts = sourcePkg.split("\\.|\\\\|\\/");
        return Arrays.asList(pkgParts);
    }

    public void addTypeReference(DiagnosticPos currentPos, Set<Whitespace> ws) {
        TypeNode typeRef = typeNodeStack.pop();
        typeRef.addWS(ws);
        BLangStructureTypeNode structureTypeNode = (BLangStructureTypeNode) typeNodeStack.peek();
        structureTypeNode.addTypeReference(typeRef);
    }

    public void createTypeTestExpression(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTypeTestExpr typeTestExpr = (BLangTypeTestExpr) TreeBuilder.createTypeTestExpressionNode();
        typeTestExpr.expr = (BLangExpression) this.exprNodeStack.pop();
        typeTestExpr.typeNode = (BLangType) this.typeNodeStack.pop();
        typeTestExpr.pos = pos;
        typeTestExpr.addWS(ws);
        addExpressionNode(typeTestExpr);
    }

    void createAnnotAccessNode(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangAnnotAccessExpr annotAccessExpr = (BLangAnnotAccessExpr) TreeBuilder.createAnnotAccessExpressionNode();
        annotAccessExpr.pos = pos;
        annotAccessExpr.addWS(ws);
        annotAccessExpr.expr = (BLangVariableReference) exprNodeStack.pop();
        BLangNameReference nameReference = nameReferenceStack.pop();
        annotAccessExpr.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        annotAccessExpr.annotationName = (BLangIdentifier) nameReference.name;
        annotAccessExpr.addWS(nameReference.ws);
        addExpressionNode(annotAccessExpr);
    }

    void handleWait(DiagnosticPos currentPos, Set<Whitespace> ws) {
        BLangWaitExpr waitExpr = TreeBuilder.createWaitExpressionNode();
        waitExpr.exprList = Collections.singletonList((BLangExpression) this.exprNodeStack.pop());
        waitExpr.pos = currentPos;
        waitExpr.addWS(ws);
        addExpressionNode(waitExpr);
    }

    void startWaitForAll() {
        BLangWaitForAllExpr bLangWaitForAll = TreeBuilder.createWaitForAllExpressionNode();
        waitCollectionStack.push(bLangWaitForAll);
    }


    void handleWaitForAll(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangWaitForAllExpr waitForAllExpr = waitCollectionStack.pop();
        waitForAllExpr.pos = pos;
        waitForAllExpr.addWS(ws);
        addExpressionNode(waitForAllExpr);
    }

    void addKeyValueToWaitForAll(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean containsExpr) {
        BLangWaitForAllExpr.BLangWaitKeyValue keyValue = TreeBuilder.createWaitKeyValueNode();
        keyValue.addWS(ws);
        keyValue.pos = pos;
        // Add the key as an identifier
        BLangIdentifier key = createIdentifier(pos, identifier, ws);
        key.setLiteral(false);
        keyValue.key = key;
        // Add the value. If it is a Identifier:expr pair then add the value by popping the expr from the expression
        // stack else the value is not assigned.
        if (containsExpr) {
            keyValue.valueExpr = (BLangExpression) exprNodeStack.pop();
        } else {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
            varRef.pos = pos;
            varRef.variableName = key;
            varRef.addWS(ws);
            varRef.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
            keyValue.keyExpr = varRef;
        }
        waitCollectionStack.peek().keyValuePairs.add(keyValue);
    }

    public void addXMLElementAccessFilter(DiagnosticPos pos, Set<Whitespace> ws, String ns,
                                          DiagnosticPos nsPos, String elementName, DiagnosticPos elemNamePos) {
        // Escape names starting with '.
        if (stringStartsWithSingleQuote(ns)) {
            ns = ns.substring(1);
        }
        if (stringStartsWithSingleQuote(elementName)) {
            elementName = elementName.substring(1);
        }
        elementFilterStack.push(new BLangXMLElementFilter(pos, ws, ns, nsPos, elementName, elemNamePos));
    }

    private boolean stringStartsWithSingleQuote(String ns) {
        return ns != null && ns.length() > 0 && ns.charAt(0) == '\'';
    }

    public void createXMLElementAccessNode(DiagnosticPos pos, Set<Whitespace> ws, int filterCount) {
        List<BLangXMLElementFilter> filters = popFilters(filterCount);

        BLangExpression expr = (BLangExpression) this.exprNodeStack.pop();
        BLangXMLElementAccess elementAccess = new BLangXMLElementAccess(pos, ws, expr, filters);
        addExpressionNode(elementAccess);
    }

    public void createXMLNavigationAccessNode(DiagnosticPos currentPos, Set<Whitespace> ws,
                                              int filterCount, int starCount, boolean isIndexed) {
        BLangExpression childIndex = null;
        if (isIndexed) {
            childIndex = (BLangExpression) this.exprNodeStack.pop();
        }
        List<BLangXMLElementFilter> filters = popFilters(filterCount);
        BLangExpression expr = (BLangExpression) this.exprNodeStack.pop();
        BLangXMLNavigationAccess xmlNavigationAccess =
                new BLangXMLNavigationAccess(currentPos, ws, expr, filters,
                        XMLNavigationAccess.NavAccessType.fromInt(starCount), childIndex);
        addExpressionNode(xmlNavigationAccess);
    }

    private List<BLangXMLElementFilter> popFilters(int filterCount) {
        ArrayList<BLangXMLElementFilter> filters = new ArrayList<>();
        for (int i = 0; i < filterCount; i++) {
            filters.add((BLangXMLElementFilter) elementFilterStack.pop());
        }
        // Filters were collected from right to left, hence need to reverse the order.
        Collections.reverse(filters);
        return filters;
    }

    private void setReadOnlyIntersectionTypeNode(BLangSimpleVariable field) {
        BLangType typeNode = field.typeNode;
        Set<Whitespace> typeNodeWS = typeNode.getWS();
        DiagnosticPos typeNodePos = typeNode.getPosition();

        BLangValueType readOnlyTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        readOnlyTypeNode.addWS(typeNodeWS);
        readOnlyTypeNode.pos = typeNodePos;
        readOnlyTypeNode.typeKind = (TreeUtils.stringToTypeKind("readonly"));

        if (typeNode.getKind() == NodeKind.INTERSECTION_TYPE_NODE) {
            ((BLangIntersectionTypeNode) typeNode).constituentTypeNodes.add(readOnlyTypeNode);
        } else {
            BLangIntersectionTypeNode intersectionTypeNode =
                    (BLangIntersectionTypeNode) TreeBuilder.createIntersectionTypeNode();
            intersectionTypeNode.constituentTypeNodes.add(typeNode);
            intersectionTypeNode.constituentTypeNodes.add(readOnlyTypeNode);
            intersectionTypeNode.addWS(typeNodeWS);
            intersectionTypeNode.pos = typeNodePos;
            field.typeNode = intersectionTypeNode;
        }

        field.flagSet.add(Flag.READONLY);
    }
}
