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
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.TreeUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.elements.TableColumnFlag;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.DeprecatedNode;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.DocumentationNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.MarkdownDocumentationNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.WorkerNode;
import org.ballerinalang.model.tree.clauses.GroupByNode;
import org.ballerinalang.model.tree.clauses.HavingNode;
import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.clauses.LimitNode;
import org.ballerinalang.model.tree.clauses.OrderByNode;
import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.OutputRateLimitNode;
import org.ballerinalang.model.tree.clauses.PatternClause;
import org.ballerinalang.model.tree.clauses.PatternStreamingEdgeInputNode;
import org.ballerinalang.model.tree.clauses.PatternStreamingInputNode;
import org.ballerinalang.model.tree.clauses.SelectClauseNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.clauses.SetAssignmentNode;
import org.ballerinalang.model.tree.clauses.StreamActionNode;
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.TableQuery;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.clauses.WindowClauseNode;
import org.ballerinalang.model.tree.clauses.WithinClause;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.MatchExpressionNode.MatchExpressionPatternNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.expressions.TableQueryExpression;
import org.ballerinalang.model.tree.expressions.XMLAttributeNode;
import org.ballerinalang.model.tree.expressions.XMLLiteralNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.ForeverNode;
import org.ballerinalang.model.tree.statements.ForkJoinNode;
import org.ballerinalang.model.tree.statements.IfNode;
import org.ballerinalang.model.tree.statements.ScopeNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.ballerinalang.model.tree.statements.TransactionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangDeprecatedNode;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression.BLangMatchExprPatternClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompensate;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStmtPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangScope;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.QuoteType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
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

    private Stack<InvokableNode> invokableNodeStack = new Stack<>();

    private Stack<ExpressionNode> exprNodeStack = new Stack<>();

    private Stack<List<ExpressionNode>> exprNodeListStack = new Stack<>();

    private Stack<Set<Whitespace>> commaWsStack = new Stack<>();

    private Stack<Set<Whitespace>> invocationWsStack = new Stack<>();

    private Stack<BLangRecordLiteral> recordLiteralNodes = new Stack<>();

    private Stack<BLangTableLiteral> tableLiteralNodes = new Stack<>();

    private Stack<BLangTryCatchFinally> tryCatchFinallyNodesStack = new Stack<>();

    private Stack<AnnotationNode> annotationStack = new Stack<>();

    private Stack<DocumentationNode> docAttachmentStack = new Stack<>();

    private Stack<MarkdownDocumentationNode> markdownDocumentationStack = new Stack<>();

    private Stack<DeprecatedNode> deprecatedAttachmentStack = new Stack<>();

    private Stack<AnnotationAttachmentNode> annotAttachmentStack = new Stack<>();

    private Stack<IfNode> ifElseStatementStack = new Stack<>();

    private Stack<TransactionNode> transactionNodeStack = new Stack<>();

    private Stack<ForkJoinNode> forkJoinNodesStack = new Stack<>();

    private Stack<ServiceNode> serviceNodeStack = new Stack<>();

    private Stack<List<BLangEndpoint>> endpointListStack = new Stack<>();

    private BLangEndpoint lastBuiltEndpoint;

    private Stack<XMLAttributeNode> xmlAttributeNodeStack = new Stack<>();

    private Stack<AttachPoint> attachPointStack = new Stack<>();

    private Stack<OrderByNode> orderByClauseStack = new Stack<>();

    private Stack<OrderByVariableNode> orderByVariableStack = new Stack<>();

    private Stack<LimitNode> limitClauseStack = new Stack<>();

    private Stack<GroupByNode> groupByClauseStack = new Stack<>();

    private Stack<HavingNode> havingClauseStack = new Stack<>();

    private Stack<WhereNode> whereClauseStack = new Stack<>();

    private Stack<SelectExpressionNode> selectExpressionsStack = new Stack<>();

    private Stack<List<SelectExpressionNode>> selectExpressionsListStack = new Stack<>();

    private Stack<SelectClauseNode> selectClausesStack = new Stack<>();

    private Stack<WindowClauseNode> windowClausesStack = new Stack<>();

    private Stack<StreamingInput> streamingInputStack = new Stack<>();

    private Stack<JoinStreamingInput> joinStreamingInputsStack = new Stack<>();

    private Stack<TableQuery> tableQueriesStack = new Stack<>();

    private Stack<SetAssignmentNode> setAssignmentStack = new Stack<>();

    private Stack<List<SetAssignmentNode>> setAssignmentListStack = new Stack<>();

    private Stack<StreamActionNode> streamActionNodeStack = new Stack<>();

    private Stack<PatternStreamingEdgeInputNode> patternStreamingEdgeInputStack = new Stack<>();

    private Stack<PatternStreamingInputNode> patternStreamingInputStack = new Stack<>();

    private Stack<StreamingQueryStatementNode> streamingQueryStatementStack = new Stack<>();

    private Stack<ForeverNode> foreverNodeStack = new Stack<>();

    private Stack<OutputRateLimitNode> outputRateLimitStack = new Stack<>();

    private Stack<WithinClause> withinClauseStack = new Stack<>();

    private Stack<PatternClause> patternClauseStack = new Stack<>();

    private Set<BLangImportPackage> imports = new HashSet<>();

    private List<VariableDefinitionNode> defaultableParamsList = new ArrayList<>();

    private Stack<VariableNode> restParamStack = new Stack<>();

    private Set<Whitespace> endpointVarWs;

    private Set<Whitespace> endpointKeywordWs;

    private Deque<BLangMatch> matchStmtStack;

    private PatternStreamingInputNode recentStreamingPatternInputNode;

    private Stack<List<MatchExpressionPatternNode>> matchExprPatternNodeListStack = new Stack<>();

    private Stack<Set<Whitespace>> operatorWs = new Stack<>();

    private Stack<Set<Whitespace>> objectFieldBlockWs = new Stack<>();

    private Stack<ScopeNode> scopeNodeStack = new Stack<>();

    private Stack<Set<Whitespace>> finiteTypeWsStack = new Stack<>();

    private BLangAnonymousModelHelper anonymousModelHelper;
    private CompilerOptions compilerOptions;

    private BLangDiagnosticLog dlog;

    private static final String IDENTIFIER_LITERAL_PREFIX = "^\"";
    private static final String IDENTIFIER_LITERAL_SUFFIX = "\"";

    public BLangPackageBuilder(CompilerContext context, CompilationUnitNode compUnit) {
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.compilerOptions = CompilerOptions.getInstance(context);
        this.compUnit = compUnit;
    }

    void addAttachPoint(AttachPoint attachPoint) {
        attachPointStack.push(attachPoint);
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

        BLangUnionTypeNode unionTypeNode;
        if (rhsTypeNode.getKind() == NodeKind.UNION_TYPE_NODE) {
            unionTypeNode = (BLangUnionTypeNode) rhsTypeNode;
            unionTypeNode.memberTypeNodes.add(0, lhsTypeNode);
            unionTypeNode.addWS(ws);
            this.typeNodeStack.push(unionTypeNode);
            return;
        } else {
            unionTypeNode = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
            unionTypeNode.memberTypeNodes.add(lhsTypeNode);
            unionTypeNode.memberTypeNodes.add(rhsTypeNode);
        }

        unionTypeNode.pos = pos;
        unionTypeNode.addWS(ws);
        this.typeNodeStack.push(unionTypeNode);
    }

    void addTupleType(DiagnosticPos pos, Set<Whitespace> ws, int members) {
        BLangTupleTypeNode tupleTypeNode = (BLangTupleTypeNode) TreeBuilder.createTupleTypeNode();
        for (int i = 0; i < members; i++) {
            final BLangType member = (BLangType) this.typeNodeStack.pop();
            tupleTypeNode.memberTypeNodes.add(0, member);
        }
        tupleTypeNode.pos = pos;
        tupleTypeNode.addWS(ws);
        this.typeNodeStack.push(tupleTypeNode);
    }

    void addRecordType(DiagnosticPos pos, Set<Whitespace> ws, boolean isFieldAnalyseRequired, boolean isAnonymous,
                       boolean sealed, boolean hasRestField) {
        // Create an anonymous record and add it to the list of records in the current package.
        BLangRecordTypeNode recordTypeNode = populateRecordTypeNode(pos, ws, isAnonymous);
        recordTypeNode.isFieldAnalyseRequired = isFieldAnalyseRequired;
        recordTypeNode.sealed = sealed;

        // If there is an explicitly defined rest field, take it.
        if (hasRestField && !sealed) {
            recordTypeNode.restFieldType = (BLangType) this.typeNodeStack.pop();
        }

        if (!isAnonymous) {
            addType(recordTypeNode);
            return;
        }
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(genName);
        typeDef.setName(anonTypeGenName);
        typeDef.flagSet.add(Flag.PUBLIC);

        typeDef.typeNode = recordTypeNode;
        typeDef.pos = pos;
        this.compUnit.addTopLevelNode(typeDef);

        addType(createUserDefinedType(pos, ws, (BLangIdentifier) TreeBuilder.createIdentifierNode(), typeDef.name));
    }

    private BLangRecordTypeNode populateRecordTypeNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isAnonymous) {
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) TreeBuilder.createRecordTypeNode();
        recordTypeNode.pos = pos;
        recordTypeNode.addWS(ws);
        recordTypeNode.isAnonymous = isAnonymous;
        this.varListStack.pop().forEach(variableNode -> {
            recordTypeNode.addField(variableNode);
        });
        return recordTypeNode;
    }

    void addFieldVariable(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                          boolean exprAvailable, int annotCount, boolean isPrivate, boolean isOptional) {
        BLangVariable field = addVar(pos, ws, identifier, exprAvailable, annotCount);

        if (!isPrivate) {
            field.flagSet.add(Flag.PUBLIC);
        }

        if (isOptional) {
            field.flagSet.add(Flag.OPTIONAL);
        }

        if (exprAvailable) {
            field.flagSet.add(Flag.DEFAULTABLE);
        }
    }

    void addFieldVariable(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean exprAvailable,
                          boolean deprecatedDocExit, int annotCount, boolean isPrivate, boolean isPublic) {
        BLangVariable field = addVar(pos, ws, identifier, exprAvailable, annotCount);

        attachAnnotations(field, annotCount);
        if (deprecatedDocExit) {
            attachDeprecatedNode(field);
        }

        if (isPublic) {
            field.flagSet.add(Flag.PUBLIC);
        } else if (isPrivate) {
            field.flagSet.add(Flag.PRIVATE);
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
        BLangType typeNode = (BLangType) this.typeNodeStack.peek();
        typeNode.addWS(ws);
        typeNode.nullable = true;
    }

    void markTypeNodeAsGrouped(Set<Whitespace> ws) {
        BLangType typeNode = (BLangType) this.typeNodeStack.peek();
        typeNode.addWS(ws);
        typeNode.grouped = true;
    }

    void addUserDefineType(Set<Whitespace> ws) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType userDefinedType = createUserDefinedType(nameReference.pos, ws,
                (BLangIdentifier) nameReference.pkgAlias, (BLangIdentifier) nameReference.name);
        userDefinedType.addWS(nameReference.ws);
        addType(userDefinedType);
    }

    void addBuiltInReferenceType(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(typeName);
        refType.pos = pos;
        refType.addWS(ws);
        addType(refType);
    }

    void addConstraintType(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType constraintType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        constraintType.pos = pos;
        constraintType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        constraintType.typeName = (BLangIdentifier) nameReference.name;
        constraintType.addWS(nameReference.ws);
        Set<Whitespace> refTypeWS = removeNthFromLast(ws, 2);

        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(typeName);
        refType.pos = pos;
        refType.addWS(refTypeWS);

        BLangConstrainedType constrainedType = (BLangConstrainedType) TreeBuilder.createConstrainedTypeNode();
        constrainedType.type = refType;
        constrainedType.constraint = constraintType;
        constrainedType.pos = pos;
        constrainedType.addWS(ws);

        addType(constrainedType);
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

    void addEndpointType(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType constraintType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        constraintType.pos = pos;
        constraintType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        constraintType.typeName = (BLangIdentifier) nameReference.name;
        constraintType.addWS(nameReference.ws);
        addType(constraintType);
    }

    void addFunctionType(DiagnosticPos pos, Set<Whitespace> ws, boolean paramsAvail,
                         boolean retParamsAvail) {
        // TODO : Fix function main ()(boolean , function(string x)(float, int)){} issue
        BLangFunctionTypeNode functionTypeNode = (BLangFunctionTypeNode) TreeBuilder.createFunctionTypeNode();
        functionTypeNode.pos = pos;
        functionTypeNode.returnsKeywordExists = true;

        if (retParamsAvail) {
            functionTypeNode.addWS(this.varStack.peek().getWS());
            functionTypeNode.returnTypeNode = (BLangType) this.varStack.pop().getTypeNode();
        } else {
            BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            nilTypeNode.pos = pos;
            nilTypeNode.typeKind = TypeKind.NIL;
            functionTypeNode.returnTypeNode = nilTypeNode;
        }

        if (paramsAvail) {
            functionTypeNode.addWS(commaWsStack.pop());
            this.varListStack.pop().forEach(v -> functionTypeNode.params.add(v));
        }

        functionTypeNode.addWS(ws);
        addType(functionTypeNode);
    }

    private void addType(TypeNode typeNode) {
        this.typeNodeStack.push(typeNode);
    }

    void addNameReference(DiagnosticPos currentPos, Set<Whitespace> ws, String pkgName, String name) {
        IdentifierNode pkgNameNode = createIdentifier(pkgName);
        IdentifierNode nameNode = createIdentifier(name);
        nameReferenceStack.push(new BLangNameReference(currentPos, ws, pkgNameNode, nameNode));
    }

    void startVarList() {
        this.varListStack.push(new ArrayList<>());
    }

    void startFunctionDef() {
        FunctionNode functionNode = TreeBuilder.createFunctionNode();
        attachAnnotations(functionNode);
        attachMarkdownDocumentations(functionNode);
        attachDeprecatedNode(functionNode);
        this.invokableNodeStack.push(functionNode);
        startEndpointDeclarationScope(((BLangFunction) functionNode).endpoints);
    }

    void startObjectFunctionDef() {
        FunctionNode functionNode = TreeBuilder.createFunctionNode();
        this.invokableNodeStack.push(functionNode);
        startEndpointDeclarationScope(((BLangFunction) functionNode).endpoints);
    }

    void startBlock() {
        this.blockNodeStack.push(TreeBuilder.createBlockNode());
    }

    private IdentifierNode createIdentifier(String value) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (value == null) {
            return node;
        }

        if (value.startsWith(IDENTIFIER_LITERAL_PREFIX) && value.endsWith(IDENTIFIER_LITERAL_SUFFIX)) {
            value = StringEscapeUtils.unescapeJava(value);
            node.setValue(value.substring(2, value.length() - 1));
            node.setLiteral(true);
        } else {
            node.setValue(value);
            node.setLiteral(false);
        }
        return node;
    }

    BLangVariable addVar(DiagnosticPos pos,
                         Set<Whitespace> ws,
                         String identifier,
                         boolean exprAvailable,
                         int annotCount) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, exprAvailable);
        attachAnnotations(var, annotCount);
        var.pos = pos;
        if (this.varListStack.empty()) {
            this.varStack.push(var);
        } else {
            this.varListStack.peek().add(var);
        }

        return var;
    }

    public BLangVariable addVarWithoutType(DiagnosticPos pos,
                                           Set<Whitespace> ws,
                                           String identifier,
                                           boolean exprAvailable,
                                           int annotCount) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNodeWithoutType(pos, ws, identifier, exprAvailable);
        attachAnnotations(var, annotCount);
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
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, null, false);
        attachAnnotations(var, annotCount);
        var.pos = pos;
        this.varStack.push(var);
    }

    void endCallableUnitSignature(DiagnosticPos pos,
                                  Set<Whitespace> ws,
                                  String identifier,
                                  DiagnosticPos identifierPos,
                                  boolean paramsAvail,
                                  boolean retParamsAvail,
                                  boolean restParamAvail) {
        InvokableNode invNode = this.invokableNodeStack.peek();
        BLangIdentifier identifierNode = (BLangIdentifier) this.createIdentifier(identifier);
        identifierNode.pos = identifierPos;
        invNode.setName(identifierNode);
        invNode.addWS(ws);
        BLangType returnTypeNode;
        if (retParamsAvail) {
            BLangVariable varNode = (BLangVariable) this.varStack.pop();
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
            this.varListStack.pop().forEach(invNode::addParameter);

            this.defaultableParamsList.forEach(variableDef -> {
                BLangVariableDef varDef = (BLangVariableDef) variableDef;
                invNode.addDefaultableParameter(varDef);
            });
            this.defaultableParamsList = new ArrayList<>();

            if (restParamAvail) {
                invNode.setRestParameter(this.restParamStack.pop());
            }

            invNode.addWS(this.commaWsStack.pop());
        }
    }

    void startLambdaFunctionDef(PackageID pkgID) {
        startFunctionDef();
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.setName(createIdentifier(anonymousModelHelper.getNextAnonymousFunctionKey(pkgID)));
        lambdaFunction.addFlag(Flag.LAMBDA);
    }

    void addLambdaFunctionDef(DiagnosticPos pos,
                              Set<Whitespace> ws,
                              boolean paramsAvail,
                              boolean retParamsAvail,
                              boolean restParamAvail) {
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.pos = pos;
        endCallableUnitSignature(pos, ws, lambdaFunction.getName().value, pos, paramsAvail, retParamsAvail,
                restParamAvail);
        BLangLambdaFunction lambdaExpr = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaExpr.function = lambdaFunction;
        lambdaExpr.pos = pos;
        addExpressionNode(lambdaExpr);
        // TODO: is null correct here
        endFunctionDef(pos, null, false, false, true, false, true);
    }

    void addArrowFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, PackageID pkgID) {
        BLangArrowFunction arrowFunctionNode = (BLangArrowFunction) TreeBuilder.createArrowFunctionNode();
        arrowFunctionNode.pos = pos;
        arrowFunctionNode.addWS(ws);
        arrowFunctionNode.functionName = createIdentifier(anonymousModelHelper.getNextAnonymousFunctionKey(pkgID));
        arrowFunctionNode.params = this.varListStack.pop();
        arrowFunctionNode.expression = (BLangExpression) this.exprNodeStack.pop();
        addExpressionNode(arrowFunctionNode);
    }

    private void startEndpointDeclarationScope(List<BLangEndpoint> endpointList) {
        endpointListStack.push(endpointList);
    }

    private void endEndpointDeclarationScope() {
        endpointListStack.pop();
    }

    void addEndpointDefinition(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean initExprExist) {
        final BLangEndpoint endpointNode = (BLangEndpoint) TreeBuilder.createEndpointNode();
        attachAnnotations(endpointNode);
        endpointNode.pos = pos;
        endpointNode.name = (BLangIdentifier) this.createIdentifier(identifier);
        endpointNode.endpointTypeNode = (BLangUserDefinedType) typeNodeStack.pop();
        if (initExprExist) {
            endpointNode.configurationExpr = (BLangExpression) this.exprNodeStack.pop();
        }
        endpointNode.addWS(ws);
        if (endpointListStack.empty()) {
            // Top level node.
            lastBuiltEndpoint = endpointNode;
            this.compUnit.addTopLevelNode(endpointNode);
        } else {
            endpointListStack.peek().add(endpointNode);
        }
    }

    void markLastEndpointAsPublic(Set<Whitespace> ws) {
        lastBuiltEndpoint.addWS(ws);
        lastBuiltEndpoint.flagSet.add(Flag.PUBLIC);
    }

    void markLastInvocationAsAsync(DiagnosticPos pos) {
        final ExpressionNode expressionNode = this.exprNodeStack.peek();
        if (expressionNode.getKind() == NodeKind.INVOCATION) {
            ((BLangInvocation) this.exprNodeStack.peek()).async = true;
        } else {
            dlog.error(pos, DiagnosticCode.START_REQUIRE_INVOCATION);
        }
    }

    void addVariableDefStatement(DiagnosticPos pos,
                                 Set<Whitespace> ws,
                                 String identifier,
                                 boolean exprAvailable,
                                 boolean endpoint) {
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        BLangVariableDef varDefNode = (BLangVariableDef) TreeBuilder.createVariableDefinitionNode();
        // TODO : Remove endpoint logic from here.
        Set<Whitespace> wsOfSemiColon = null;
        if (endpoint) {
            var.addWS(endpointVarWs);
            var.addWS(endpointKeywordWs);
            endpointVarWs = null;
            endpointKeywordWs = null;
        } else {
            wsOfSemiColon = removeNthFromLast(ws, 0);
        }
        var.pos = pos;
        var.addWS(ws);
        var.setName(this.createIdentifier(identifier));
        var.setTypeNode(this.typeNodeStack.pop());
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }

        varDefNode.pos = pos;
        varDefNode.setVariable(var);
        varDefNode.addWS(wsOfSemiColon);
        addStmtToCurrentBlock(varDefNode);
    }

    void addTypeInitExpression(DiagnosticPos pos, Set<Whitespace> ws, String initName, boolean typeAvailable,
                               boolean exprAvailable) {
        BLangTypeInit objectInitNode = (BLangTypeInit) TreeBuilder.createObjectInitNode();
        objectInitNode.pos = pos;
        objectInitNode.addWS(ws);
        if (typeAvailable) {
            objectInitNode.userDefinedType = (BLangUserDefinedType) typeNodeStack.pop();
        }

        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        invocationNode.addWS(ws);
        if (exprAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            Set<Whitespace> cws = commaWsStack.pop();
            exprNodes.forEach(exprNode -> {
                invocationNode.argExprs.add((BLangExpression) exprNode);
                objectInitNode.argsExpr.add((BLangExpression) exprNode);

            });
            invocationNode.addWS(cws);
            objectInitNode.addWS(cws);
        }

        //TODO check whether pkgName can be be empty
        IdentifierNode pkgNameNode = TreeBuilder.createIdentifierNode();
        IdentifierNode nameNode = createIdentifier(initName);
        BLangNameReference nameReference = new BLangNameReference(pos, ws, pkgNameNode, nameNode);
        invocationNode.name = (BLangIdentifier) nameReference.name;
        invocationNode.addWS(nameReference.ws);
        invocationNode.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;

        objectInitNode.objectInitInvocation = invocationNode;
        this.addExpressionNode(objectInitNode);
    }

    private void addStmtToCurrentBlock(StatementNode statement) {
        this.blockNodeStack.peek().addStatement(statement);
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
        BLangVariable variableNode = (BLangVariable) TreeBuilder.createVariableNode();
        variableNode.typeNode = (BLangType) this.typeNodeStack.pop();
        variableNode.name = (BLangIdentifier) createIdentifier(paramName);
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

    private void addExpressionNode(ExpressionNode expressionNode) {
        this.exprNodeStack.push(expressionNode);
    }

    void addLiteralValue(DiagnosticPos pos, Set<Whitespace> ws, int typeTag, Object value) {
        addLiteralValue(pos, ws, typeTag, value, String.valueOf(value));
    }

    void addLiteralValue(DiagnosticPos pos, Set<Whitespace> ws, int typeTag, Object value, String originalValue) {
        BLangLiteral litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        litExpr.addWS(ws);
        litExpr.pos = pos;
        litExpr.typeTag = typeTag;
        litExpr.value = value;
        litExpr.originalValue = originalValue;
        addExpressionNode(litExpr);
    }

    void addArrayInitExpr(DiagnosticPos pos, Set<Whitespace> ws, boolean argsAvailable) {
        List<ExpressionNode> argExprList;
        BLangArrayLiteral arrayLiteral = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
        if (argsAvailable) {
            arrayLiteral.addWS(commaWsStack.pop());
            argExprList = exprNodeListStack.pop();
        } else {
            argExprList = new ArrayList<>(0);
        }
        arrayLiteral.exprs = argExprList.stream().map(expr -> (BLangExpression) expr).collect(Collectors.toList());
        arrayLiteral.pos = pos;
        arrayLiteral.addWS(ws);
        addExpressionNode(arrayLiteral);
    }

    void addKeyValueRecord(Set<Whitespace> ws) {
        BLangRecordKeyValue keyValue = (BLangRecordKeyValue) TreeBuilder.createRecordKeyValue();
        keyValue.addWS(ws);
        keyValue.valueExpr = (BLangExpression) exprNodeStack.pop();
        keyValue.key = new BLangRecordKey((BLangExpression) exprNodeStack.pop());
        recordLiteralNodes.peek().keyValuePairs.add(keyValue);
    }

    void addMapStructLiteral(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangRecordLiteral recordTypeLiteralNode = recordLiteralNodes.pop();
        recordTypeLiteralNode.pos = pos;
        recordTypeLiteralNode.addWS(ws);
        addExpressionNode(recordTypeLiteralNode);
    }

    void startTableLiteral() {
        final BLangTableLiteral tableLiteral = (BLangTableLiteral) TreeBuilder.createTableLiteralNode();
        tableLiteralNodes.push(tableLiteral);
    }

    void endTableColumnDefinition(Set<Whitespace> ws) {
        BLangTableLiteral tableLiteral = this.tableLiteralNodes.peek();
        tableLiteral.addWS(ws);
    }

    void addTableColumn(String columnName, DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTableLiteral.BLangTableColumn tableColumn = new BLangTableLiteral.BLangTableColumn(columnName);
        tableColumn.pos = pos;
        tableColumn.addWS(ws);
        this.tableLiteralNodes.peek().columns.add(tableColumn);
    }

    void markPrimaryKeyColumn(String columnName) {
        BLangTableLiteral tableLiteral = this.tableLiteralNodes.peek();
        BLangTableLiteral.BLangTableColumn column = tableLiteral.getColumn(columnName);
        if (column != null) {
            column.flagSet.add(TableColumnFlag.PRIMARYKEY);
        }
    }

    void endTableDataList(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangRecordLiteral recordLiteral = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        List<BLangTableLiteral.BLangTableColumn> keyNames = tableLiteralNodes.peek().columns;
        List<ExpressionNode> recordValues = exprNodeListStack.pop();
        if (keyNames.size() == recordValues.size()) {
            int index = 0;
            for (ExpressionNode expr : recordValues) {
                BLangRecordKeyValue keyValue = (BLangRecordKeyValue) TreeBuilder.createRecordKeyValue();
                //Value
                keyValue.valueExpr = (BLangExpression) expr;
                //key
                BLangSimpleVarRef keyExpr = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
                keyExpr.pos = pos;
                IdentifierNode identifierNode = TreeBuilder.createIdentifierNode();
                identifierNode.setValue(keyNames.get(index).columnName);
                keyExpr.variableName = (BLangIdentifier) identifierNode;
                keyValue.key = new BLangRecordKey(keyExpr);
                //Key-Value pair
                recordLiteral.keyValuePairs.add(keyValue);
                ++index;
            }
            recordLiteral.addWS(ws);
            if (commaWsStack.size() > 0) {
                recordLiteral.addWS(commaWsStack.pop());
            }
            this.tableLiteralNodes.peek().tableDataRows.add(recordLiteral);
        }
    }

    void endTableDataArray(Set<Whitespace> ws) {
        BLangTableLiteral tableLiteral = this.tableLiteralNodes.peek();
        tableLiteral.addWS(ws);
    }

    void endTableDataRow(Set<Whitespace> ws) {
        List<ExpressionNode> argExprList = exprNodeListStack.pop();
        BLangTableLiteral tableLiteral = this.tableLiteralNodes.peek();
        tableLiteral.addWS(ws);
        tableLiteral.tableDataRows = argExprList.stream().map(expr -> (BLangExpression) expr)
                .collect(Collectors.toList());
    }

    void addTableLiteral(DiagnosticPos pos, Set<Whitespace> ws) {
        final BLangTableLiteral tableLiteral = tableLiteralNodes.pop();
        tableLiteral.addWS(ws);
        tableLiteral.pos = pos;
        addExpressionNode(tableLiteral);
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
        if (exprNodeStack.empty()) {
            throw new IllegalStateException("Expression stack cannot be empty in processing an ExpressionList");
        }
        ExpressionNode expr = exprNodeStack.pop();
        if (n > 1) {
            addExprToExprNodeList(exprList, n - 1);
        }
        exprList.add(expr);
    }


    void createSimpleVariableReference(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder
                .createSimpleVariableReferenceNode();
        varRef.pos = pos;
        varRef.addWS(ws);
        varRef.addWS(nameReference.ws);
        varRef.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        varRef.variableName = (BLangIdentifier) nameReference.name;
        this.exprNodeStack.push(varRef);
    }

    void createFunctionInvocation(DiagnosticPos pos, Set<Whitespace> ws, boolean argsAvailable) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
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

    void createInvocationNode(DiagnosticPos pos, Set<Whitespace> ws, String invocation, boolean argsAvailable,
                              boolean safeNavigate) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        invocationNode.addWS(ws);
        invocationNode.addWS(invocationWsStack.pop());
        invocationNode.safeNavigate = safeNavigate;
        if (argsAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            exprNodes.forEach(exprNode -> invocationNode.argExprs.add((BLangExpression) exprNode));
            invocationNode.addWS(commaWsStack.pop());
        }

        invocationNode.expr = (BLangVariableReference) exprNodeStack.pop();
        invocationNode.name = (BLangIdentifier) createIdentifier(invocation);
        invocationNode.pkgAlias = (BLangIdentifier) createIdentifier(null);
        addExpressionNode(invocationNode);
    }

    void createActionInvocationNode(DiagnosticPos pos, Set<Whitespace> ws, boolean async) {
        BLangInvocation invocationExpr = (BLangInvocation) exprNodeStack.pop();
        invocationExpr.actionInvocation = true;
        invocationExpr.pos = pos;
        invocationExpr.addWS(ws);
        invocationExpr.async = async;

        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = nameReference.pos;
        varRef.addWS(nameReference.ws);
        varRef.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        varRef.variableName = (BLangIdentifier) nameReference.name;
        invocationExpr.expr = varRef;
        exprNodeStack.push(invocationExpr);
    }

    void createFieldBasedAccessNode(DiagnosticPos pos, Set<Whitespace> ws, String fieldName,
                                    FieldKind fieldType, boolean safeNavigate) {
        BLangFieldBasedAccess fieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        fieldBasedAccess.pos = pos;
        fieldBasedAccess.addWS(ws);
        fieldBasedAccess.field = (BLangIdentifier) createIdentifier(fieldName);
        fieldBasedAccess.expr = (BLangVariableReference) exprNodeStack.pop();
        fieldBasedAccess.fieldKind = fieldType;
        fieldBasedAccess.safeNavigate = safeNavigate;
        addExpressionNode(fieldBasedAccess);
    }

    void createIndexBasedAccessNode(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangIndexBasedAccess indexBasedAccess = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexBasedAccess.pos = pos;
        indexBasedAccess.addWS(ws);
        indexBasedAccess.indexExpr = (BLangExpression) exprNodeStack.pop();
        indexBasedAccess.expr = (BLangVariableReference) exprNodeStack.pop();
        addExpressionNode(indexBasedAccess);
    }

    void createBracedOrTupleExpression(DiagnosticPos pos, Set<Whitespace> ws, int numberOfExpressions) {
        final BLangBracedOrTupleExpr expr = (BLangBracedOrTupleExpr) TreeBuilder.createBracedOrTupleExpression();
        expr.pos = pos;
        expr.addWS(ws);
        for (int i = 0; i < numberOfExpressions; i++) {
            expr.expressions.add(0, (BLangExpression) exprNodeStack.pop());
        }
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

    void createTypeConversionExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTypeConversionExpr typeConversionNode = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        typeConversionNode.pos = pos;
        typeConversionNode.addWS(ws);
        typeConversionNode.typeNode = (BLangType) typeNodeStack.pop();
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

    void createAwaitExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangAwaitExpr awaitExpr = TreeBuilder.createAwaitExpressionNode();
        awaitExpr.pos = pos;
        awaitExpr.addWS(ws);
        awaitExpr.expr = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(awaitExpr);
    }

    void endFunctionDef(DiagnosticPos pos,
                        Set<Whitespace> ws,
                        boolean publicFunc,
                        boolean nativeFunc,
                        boolean bodyExists,
                        boolean isReceiverAttached,
                        boolean isLambda) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
        endEndpointDeclarationScope();
        function.pos = pos;
        function.addWS(ws);
        if (!isLambda) {
            function.addWS(invocationWsStack.pop());
        }
        if (publicFunc) {
            function.flagSet.add(Flag.PUBLIC);
        }

        if (nativeFunc) {
            function.flagSet.add(Flag.NATIVE);
        }

        if (!bodyExists) {
            function.body = null;
        }

        if (isReceiverAttached) {
            //Get type node for this attached function
            TypeNode typeNode = this.typeNodeStack.pop();
            //Create and add receiver to attached functions
            BLangVariable receiver = (BLangVariable) TreeBuilder.createVariableNode();
            receiver.pos = pos;

            IdentifierNode name = createIdentifier(Names.SELF.getValue());
            receiver.setName(name);
            receiver.setTypeNode(typeNode);
            function.receiver = receiver;
            function.flagSet.add(Flag.ATTACHED);
        }

        if (!function.deprecatedAttachments.isEmpty()) {
            function.flagSet.add(Flag.DEPRECATED);
        }

        this.compUnit.addTopLevelNode(function);
    }

    void startWorker() {
        WorkerNode workerNode = TreeBuilder.createWorkerNode();
        this.invokableNodeStack.push(workerNode);
        startBlock();
    }

    void addWorker(DiagnosticPos pos, Set<Whitespace> ws, String workerName) {
        // TODO This code will not work if there are workers inside a lambda and the lambda is inside a fork/join
        BLangWorker worker = (BLangWorker) this.invokableNodeStack.pop();
        worker.setName(createIdentifier(workerName));
        worker.pos = pos;
        worker.addWS(ws);
        worker.setBody(this.blockNodeStack.pop());
        if (this.forkJoinNodesStack.empty()) {
            InvokableNode invokableNode = this.invokableNodeStack.peek();
            invokableNode.getParameters().forEach(worker::addParameter);
            worker.setReturnTypeNode(invokableNode.getReturnTypeNode());
            invokableNode.addWorker(worker);
            invokableNode.addFlag(Flag.PARALLEL);
        } else {
            ((BLangForkJoin) this.forkJoinNodesStack.peek()).workers.add(worker);
        }
    }

    void attachWorkerWS(Set<Whitespace> ws) {
        BLangWorker worker = (BLangWorker) this.invokableNodeStack.peek();
        worker.addWS(ws);
    }

    void startForkJoinStmt() {
        this.forkJoinNodesStack.push(TreeBuilder.createForkJoinNode());
    }

    void addForkJoinStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.pop();
        forkJoin.pos = pos;
        forkJoin.addWS(ws);
        this.addStmtToCurrentBlock(forkJoin);
    }

    void startJoinCause() {
        startBlock();
    }

    void addJoinCause(Set<Whitespace> ws, String identifier) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.joinedBody = (BLangBlockStmt) this.blockNodeStack.pop();
        Set<Whitespace> varWS = removeNthFromLast(ws, 3);
        forkJoin.addWS(ws);
        forkJoin.joinResultVar = (BLangVariable) this.generateBasicVarNode(
                (DiagnosticPos) this.typeNodeStack.peek().getPosition(), varWS, identifier, false);
    }

    void addJoinCondition(Set<Whitespace> ws, String joinType, List<String> workerNames, int joinCount) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.joinedWorkerCount = joinCount;
        forkJoin.joinType = ForkJoinNode.JoinType.valueOf(joinType);
        forkJoin.addWS(ws);
        workerNames.forEach(s -> forkJoin.joinedWorkers.add((BLangIdentifier) createIdentifier(s)));
    }

    void startTimeoutCause() {
        startBlock();
    }

    void addTimeoutCause(Set<Whitespace> ws, String identifier) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.timeoutBody = (BLangBlockStmt) this.blockNodeStack.pop();
        forkJoin.timeoutExpression = (BLangExpression) this.exprNodeStack.pop();
        Set<Whitespace> varWS = removeNthFromLast(ws, 3);
        forkJoin.addWS(ws);
        forkJoin.timeoutVariable = (BLangVariable) this.generateBasicVarNode(
                (DiagnosticPos) this.typeNodeStack.peek().getPosition(), varWS, identifier, false);
    }

    void endCallableUnitBody(Set<Whitespace> ws) {
        BlockNode block = this.blockNodeStack.pop();
        InvokableNode invokableNode = this.invokableNodeStack.peek();
        invokableNode.addWS(ws);
        invokableNode.setBody(block);
    }

    void addImportPackageDeclaration(DiagnosticPos pos,
                                     Set<Whitespace> ws,
                                     String orgName,
                                     List<String> nameComps,
                                     String version,
                                     String alias) {

        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        nameComps.forEach(e -> pkgNameComps.add((BLangIdentifier) this.createIdentifier(e)));
        BLangIdentifier versionNode = (BLangIdentifier) this.createIdentifier(version);
        BLangIdentifier aliasNode = (alias != null && !alias.isEmpty()) ?
                (BLangIdentifier) this.createIdentifier(alias) :
                pkgNameComps.get(pkgNameComps.size() - 1);

        BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        importDcl.pos = pos;
        importDcl.addWS(ws);
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.version = versionNode;
        importDcl.orgName = (BLangIdentifier) this.createIdentifier(orgName);
        importDcl.alias = aliasNode;
        this.compUnit.addTopLevelNode(importDcl);
        if (this.imports.contains(importDcl)) {
            this.dlog.warning(pos, DiagnosticCode.REDECLARED_IMPORT_MODULE, importDcl.getQualifiedPackageName());
        } else {
            this.imports.add(importDcl);
        }
    }

    private VariableNode generateBasicVarNode(DiagnosticPos pos,
                                              Set<Whitespace> ws,
                                              String identifier,
                                              boolean exprAvailable) {
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        IdentifierNode name = this.createIdentifier(identifier);
        var.setName(name);
        var.addWS(ws);
        var.setTypeNode(this.typeNodeStack.pop());
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        return var;
    }

    private VariableNode generateBasicVarNodeWithoutType(DiagnosticPos pos,
                                                         Set<Whitespace> ws,
                                                         String identifier,
                                                         boolean exprAvailable) {
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        IdentifierNode name = this.createIdentifier(identifier);
        var.setName(name);
        var.addWS(ws);
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        return var;
    }

    void addGlobalVariable(DiagnosticPos pos,
                           Set<Whitespace> ws,
                           String identifier,
                           boolean exprAvailable,
                           boolean publicVar) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, exprAvailable);
        attachAnnotations(var);
        if (publicVar) {
            var.flagSet.add(Flag.PUBLIC);
        }
        attachMarkdownDocumentations(var);
        attachDeprecatedNode(var);
        this.compUnit.addTopLevelNode(var);
    }

    void startObjectType() {
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) TreeBuilder.createObjectTypeNode();
        typeNodeStack.push(objectTypeNode);
        startVarList();
        startFieldBlockList();
    }

    void addObjectType(DiagnosticPos pos, Set<Whitespace> ws, boolean isFieldAnalyseRequired, boolean isAnonymous,
                       boolean isAbstract) {
        BLangObjectTypeNode objectTypeNode = populateObjectTypeNode(pos, ws, isAnonymous);
        objectTypeNode.addWS(this.objectFieldBlockWs.pop());
        objectTypeNode.isFieldAnalyseRequired = isFieldAnalyseRequired;

        if (isAbstract) {
            objectTypeNode.flagSet.add(Flag.ABSTRACT);
        }

        if (!isAnonymous) {
            addType(objectTypeNode);
            return;
        }
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(genName);
        typeDef.setName(anonTypeGenName);
        typeDef.flagSet.add(Flag.PUBLIC);

        typeDef.typeNode = objectTypeNode;
        typeDef.pos = pos;
        this.compUnit.addTopLevelNode(typeDef);

        addType(createUserDefinedType(pos, ws, (BLangIdentifier) TreeBuilder.createIdentifierNode(), typeDef.name));
    }

    private BLangObjectTypeNode populateObjectTypeNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isAnonymous) {
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeNodeStack.pop();
        objectTypeNode.pos = pos;
        objectTypeNode.addWS(ws);
        objectTypeNode.isAnonymous = isAnonymous;
        this.varListStack.pop().forEach(objectTypeNode::addField);
        return objectTypeNode;
    }

    void startFieldBlockList() {
        this.objectFieldBlockWs.push(new TreeSet<>());
    }

    void addObjectFieldsBlock(Set<Whitespace> ws) {
        Set<Whitespace> fieldObjectWhitespace = this.objectFieldBlockWs.peek();
        if (fieldObjectWhitespace != null && ws != null) {
            fieldObjectWhitespace.addAll(ws);
        }
    }

    void endFiniteType(Set<Whitespace> ws) {
        finiteTypeWsStack.push(ws);
    }

    void endTypeDefinition(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                           boolean publicType) {
        BLangTypeDefinition typeDefinition = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        BLangIdentifier identifierNode = (BLangIdentifier) this.createIdentifier(identifier);
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
                finiteTypeNode.valueSpace.add((BLangExpression) exprNodeStack.pop());
            }

            if (!members.memberTypeNodes.isEmpty()) {
                BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
                // Generate a name for the anonymous object
                String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
                IdentifierNode anonTypeGenName = createIdentifier(genName);
                typeDef.setName(anonTypeGenName);
                typeDef.flagSet.add(Flag.PUBLIC);

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
        attachDeprecatedNode(typeDefinition);
        attachAnnotations(typeDefinition);
        this.compUnit.addTopLevelNode(typeDefinition);
    }

    void endObjectInitParamList(Set<Whitespace> ws, boolean paramsAvail, boolean restParamAvail) {
        InvokableNode invNode = this.invokableNodeStack.peek();
        invNode.addWS(ws);

        if (paramsAvail) {
            this.varListStack.pop().forEach(invNode::addParameter);

            this.defaultableParamsList.forEach(variableDef -> {
                BLangVariableDef varDef = (BLangVariableDef) variableDef;
                invNode.addDefaultableParameter(varDef);
            });
            this.defaultableParamsList = new ArrayList<>();

            if (restParamAvail) {
                invNode.setRestParameter(this.restParamStack.pop());
            }
        }
    }

    void endObjectInitFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean publicFunc,
                                  boolean bodyExists, boolean markdownDocPresent, boolean deprecatedDocPresent,
                                  int annCount) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
        endEndpointDeclarationScope();
        function.setName(this.createIdentifier(identifier));
        function.pos = pos;
        function.addWS(ws);

        if (publicFunc) {
            function.flagSet.add(Flag.PUBLIC);
        }

        if (!bodyExists) {
            function.body = null;
        }

        attachAnnotations(function, annCount);
        if (markdownDocPresent) {
            attachMarkdownDocumentations(function);
        }
        if (deprecatedDocPresent) {
            attachDeprecatedNode(function);
        }

        if (!function.deprecatedAttachments.isEmpty()) {
            function.flagSet.add(Flag.DEPRECATED);
        }

        BLangValueType nillTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nillTypeNode.pos = pos;
        nillTypeNode.typeKind = TypeKind.NIL;
        function.returnTypeNode = nillTypeNode;

        function.objInitFunction = true;
        ((BLangObjectTypeNode) this.typeNodeStack.peek()).initFunction = function;
    }

    void endObjectAttachedFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, boolean publicFunc, boolean privateFunc,
                                      boolean nativeFunc, boolean bodyExists, boolean markdownDocPresent,
                                      boolean deprecatedDocPresent, int annCount) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
        endEndpointDeclarationScope();
        function.pos = pos;
        function.addWS(ws);
        function.addWS(this.invocationWsStack.pop());

        function.flagSet.add(Flag.ATTACHED);

        if (publicFunc) {
            function.flagSet.add(Flag.PUBLIC);
        } else if (privateFunc) {
            function.flagSet.add(Flag.PRIVATE);
        }

        if (nativeFunc) {
            function.flagSet.add(Flag.NATIVE);
        }

        if (!bodyExists) {
            function.body = null;
            if (!nativeFunc) {
                function.flagSet.add(Flag.INTERFACE);
                function.interfaceFunction = true;
            }
        }

        function.attachedFunction = true;

        attachAnnotations(function, annCount);
        if (markdownDocPresent) {
            attachMarkdownDocumentations(function);
        }
        if (deprecatedDocPresent) {
            attachDeprecatedNode(function);
        }

        if (!function.deprecatedAttachments.isEmpty()) {
            function.flagSet.add(Flag.DEPRECATED);
        }

        ((BLangObjectTypeNode) this.typeNodeStack.peek()).addFunction(function);
    }

    void endObjectOuterFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, boolean publicFunc, boolean nativeFunc,
                                   boolean bodyExists, String objectName) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
        endEndpointDeclarationScope();
        function.pos = pos;
        function.addWS(ws);
        function.addWS(invocationWsStack.pop());

        if (publicFunc) {
            function.flagSet.add(Flag.PUBLIC);
        }

        if (nativeFunc) {
            function.flagSet.add(Flag.NATIVE);
        }

        if (!bodyExists) {
            function.body = null;
        }

        // Create an user defined type with object type
        TypeNode objectType = createUserDefinedType(pos, ws, (BLangIdentifier) TreeBuilder.createIdentifierNode(),
                (BLangIdentifier) createIdentifier(objectName));

        //Create and add receiver to attached functions
        BLangVariable receiver = (BLangVariable) TreeBuilder.createVariableNode();
        receiver.pos = pos;

        IdentifierNode name = createIdentifier(Names.SELF.getValue());
        receiver.setName(name);

        receiver.setTypeNode(objectType);

        function.receiver = receiver;
        function.flagSet.add(Flag.ATTACHED);

        function.attachedOuterFunction = true;

        if (!function.deprecatedAttachments.isEmpty()) {
            function.flagSet.add(Flag.DEPRECATED);
        }

        this.compUnit.addTopLevelNode(function);
    }

    void addObjectParameter(DiagnosticPos pos, Set<Whitespace> ws, boolean isField,
                            String identifier, int annotCount) {
        BLangVariable var = (BLangVariable) this.generateObjectVarNode(pos, ws, isField, identifier);
        attachAnnotations(var, annotCount);
        var.pos = pos;
        if (this.varListStack.empty()) {
            this.varStack.push(var);
        } else {
            this.varListStack.peek().add(var);
        }

    }

    private VariableNode generateObjectVarNode(DiagnosticPos pos, Set<Whitespace> ws,
                                               boolean isField, String identifier) {
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        IdentifierNode name = this.createIdentifier(identifier);
        var.setName(name);
        var.addWS(ws);
        var.isField = isField;
        if (!isField) {
            var.setTypeNode(this.typeNodeStack.pop());
        }
        return var;
    }

    void startAnnotationDef(DiagnosticPos pos) {
        BLangAnnotation annotNode = (BLangAnnotation) TreeBuilder.createAnnotationNode();
        annotNode.pos = pos;
        attachAnnotations(annotNode);
        attachMarkdownDocumentations(annotNode);
        attachDeprecatedNode(annotNode);
        this.annotationStack.add(annotNode);
    }

    void endAnnotationDef(Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos, boolean publicAnnotation,
                          boolean isTypeAttached) {
        BLangAnnotation annotationNode = (BLangAnnotation) this.annotationStack.pop();
        annotationNode.addWS(ws);
        BLangIdentifier identifierNode = (BLangIdentifier) this.createIdentifier(identifier);
        identifierNode.pos = identifierPos;
        annotationNode.setName(identifierNode);

        if (publicAnnotation) {
            annotationNode.flagSet.add(Flag.PUBLIC);
        }
        while (!attachPointStack.empty()) {
            annotationNode.attachPoints.add(attachPointStack.pop());
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

    void endParameterDocumentation(DiagnosticPos pos, Set<Whitespace> ws, String parameterName, String description) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkdownParameterDocumentation parameterDocumentationNode =
                (BLangMarkdownParameterDocumentation) TreeBuilder.createMarkdownParameterDocumentationNode();
        parameterDocumentationNode.parameterName = (BLangIdentifier) createIdentifier(parameterName);
        parameterDocumentationNode.pos = pos;
        parameterDocumentationNode.addWS(ws);
        parameterDocumentationNode.addParameterDocumentationLine(description);
        markdownDocumentationNode.addParameter(parameterDocumentationNode);
    }

    void endParameterDocumentationDescription(Set<Whitespace> ws, String description) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkdownParameterDocumentation parameterDocumentation =
                markdownDocumentationNode.getParameters().getLast();
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

    void createDeprecatedNode(DiagnosticPos pos,
                              Set<Whitespace> ws,
                              String content) {
        BLangDeprecatedNode deprecatedNode = (BLangDeprecatedNode) TreeBuilder.createDeprecatedNode();

        deprecatedNode.pos = pos;
        deprecatedNode.addWS(ws);

        deprecatedNode.documentationText = content;
        deprecatedAttachmentStack.push(deprecatedNode);
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

    private void attachDeprecatedNode(DocumentableNode documentableNode) {
        if (!deprecatedAttachmentStack.empty()) {
            documentableNode.addDeprecatedAttachment(deprecatedAttachmentStack.pop());
        }
    }

    private void attachAnnotations(AnnotatableNode annotatableNode, int count) {
        if (count == 0 || annotAttachmentStack.empty()) {
            return;
        }

        List<AnnotationAttachmentNode> tempAnnotAttachments = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (annotAttachmentStack.empty()) {
                break;
            }
            tempAnnotAttachments.add(annotAttachmentStack.pop());
        }
        // reversing the collected annotations to preserve the original order
        Collections.reverse(tempAnnotAttachments);
        tempAnnotAttachments.forEach(annotatableNode::addAnnotationAttachment);
    }

    void addAssignmentStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean isVarDeclaration) {
        ExpressionNode rExprNode = exprNodeStack.pop();
        ExpressionNode lExprNode = exprNodeStack.pop();
        BLangAssignment assignmentNode = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentNode.setExpression(rExprNode);
        assignmentNode.setDeclaredWithVar(isVarDeclaration);
        assignmentNode.pos = pos;
        assignmentNode.addWS(ws);
        assignmentNode.varRef = ((BLangVariableReference) lExprNode);
        addStmtToCurrentBlock(assignmentNode);
    }

    void addTupleDestructuringStatement(DiagnosticPos pos, Set<Whitespace> ws,
                                        boolean isVarsExist, boolean varDeclaration) {
        BLangTupleDestructure stmt = (BLangTupleDestructure) TreeBuilder.createTupleDestructureStatementNode();
        stmt.pos = pos;
        stmt.addWS(ws);
        if (isVarsExist) {
            stmt.setDeclaredWithVar(varDeclaration);
            stmt.expr = (BLangExpression) exprNodeStack.pop();
            List<ExpressionNode> lExprList = exprNodeListStack.pop();
            lExprList.forEach(expressionNode -> stmt.varRefs.add((BLangVariableReference) expressionNode));
            stmt.addWS(commaWsStack.pop());
        }
        // TODO: handle ParamList Destructure.
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

    void addForeachStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.addWS(ws);
        foreach.pos = pos;
        foreach.setCollection(exprNodeStack.pop());
        foreach.addWS(commaWsStack.pop());
        List<ExpressionNode> lExprList = exprNodeListStack.pop();
        lExprList.forEach(expressionNode -> foreach.addVariable((BLangVariableReference) expressionNode));
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
            nilLiteral.typeTag = TypeTags.NIL;
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

    void endTransactionStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.pop();
        transaction.pos = pos;
        transaction.addWS(ws);
        addStmtToCurrentBlock(transaction);

        // TODO This is a temporary workaround to flag coordinator service start
        String value = compilerOptions.get(CompilerOptionName.TRANSACTION_EXISTS);
        if (value != null) {
            return;
        }

        compilerOptions.put(CompilerOptionName.TRANSACTION_EXISTS, "true");
        List<String> nameComps = getPackageNameComps(Names.TRANSACTION_PACKAGE.value);
        addImportPackageDeclaration(pos, null, Names.TRANSACTION_ORG.value, nameComps, Names.DEFAULT_VERSION.value,
                Names.DOT.value + nameComps.get(nameComps.size() - 1));
    }

    void addAbortStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangAbort abortNode = (BLangAbort) TreeBuilder.createAbortNode();
        abortNode.pos = pos;
        abortNode.addWS(ws);
        addStmtToCurrentBlock(abortNode);
    }

    void addDoneStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangDone doneNode = (BLangDone) TreeBuilder.createDoneNode();
        doneNode.pos = pos;
        doneNode.addWS(ws);
        addStmtToCurrentBlock(doneNode);
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

    void addCommittedBlock(Set<Whitespace> ws) {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.peek();
        transaction.addWS(ws);
        transaction.onCommitFunction = (BLangExpression) exprNodeStack.pop();
    }

    void addAbortedBlock(Set<Whitespace> ws) {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.peek();
        transaction.addWS(ws);
        transaction.onAbortFunction = (BLangExpression) exprNodeStack.pop();
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
        ifNode.setBody(blockNodeStack.pop());
    }

    void addElseIfBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        IfNode elseIfNode = ifElseStatementStack.pop();
        ((BLangIf) elseIfNode).pos = pos;
        elseIfNode.setCondition(exprNodeStack.pop());
        elseIfNode.setBody(blockNodeStack.pop());
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
        BlockNode elseBlock = blockNodeStack.pop();
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
        matchStmt.patternClauses = new ArrayList<>();

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

    void addMatchStmtPattern(DiagnosticPos pos, Set<Whitespace> ws, String identifier) {
        BLangMatchStmtPatternClause patternClause =
                (BLangMatchStmtPatternClause) TreeBuilder.createMatchStatementPattern();
        patternClause.pos = pos;
        patternClause.addWS(ws);

        // Create a variable node
        String patternIdentifier = identifier == null ? Names.IGNORE.value : identifier;
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        var.setName(this.createIdentifier(patternIdentifier));
        var.setTypeNode(this.typeNodeStack.pop());
        if (identifier != null) {
            Set<Whitespace> varDefWS = removeNthFromStart(ws, 0);
            var.addWS(varDefWS);
        }
        patternClause.variable = var;
        patternClause.body = (BLangBlockStmt) blockNodeStack.pop();
        patternClause.body.pos = pos;
        this.matchStmtStack.peekFirst().patternClauses.add(patternClause);
    }

    void addWorkerSendStmt(DiagnosticPos pos, Set<Whitespace> ws, String workerName, boolean isForkJoinSend, boolean
            hasKey) {
        BLangWorkerSend workerSendNode = (BLangWorkerSend) TreeBuilder.createWorkerSendNode();
        workerSendNode.setWorkerName(this.createIdentifier(workerName));
        workerSendNode.expr = (BLangExpression) exprNodeStack.pop();
        workerSendNode.isForkJoinSend = isForkJoinSend;
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

    void addWorkerReceiveStmt(DiagnosticPos pos, Set<Whitespace> ws, String workerName, boolean hasKey) {
        BLangWorkerReceive workerReceiveNode = (BLangWorkerReceive) TreeBuilder.createWorkerReceiveNode();
        workerReceiveNode.setWorkerName(this.createIdentifier(workerName));
        workerReceiveNode.expr = (BLangExpression) exprNodeStack.pop();
        workerReceiveNode.pos = pos;
        workerReceiveNode.addWS(ws);
        //if there are two expressions, this is a channel receive and the top expression is the key
        if (hasKey) {
            workerReceiveNode.keyExpr = workerReceiveNode.expr;
            workerReceiveNode.expr = (BLangExpression) exprNodeStack.pop();
            workerReceiveNode.isChannel = true;
        }
        addStmtToCurrentBlock(workerReceiveNode);
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
        attachAnnotations(serviceNode);
        attachMarkdownDocumentations(serviceNode);
        attachDeprecatedNode(serviceNode);
        serviceNodeStack.push(serviceNode);
        startEndpointDeclarationScope(serviceNode.endpoints);
    }

    void addServiceBody(Set<Whitespace> ws) {
        ServiceNode serviceNode = serviceNodeStack.peek();
        serviceNode.addWS(ws);
        blockNodeStack.pop().getStatements().forEach(stmt -> {
            if (stmt.getKind() == NodeKind.XMLNS) {
                serviceNode.addNamespaceDeclaration((BLangXMLNSStatement) stmt);
            } else {
                serviceNode.addVariable((VariableDefinitionNode) stmt);
            }
        });
    }

    void addAnonymousEndpointBind(Set<Whitespace> ws) {
        BLangService serviceNode = (BLangService) serviceNodeStack.peek();
        serviceNode.addWS(ws);
        serviceNode.addAnonymousEndpointBind((RecordLiteralNode) exprNodeStack.pop());
    }

    void addServiceEndpointAttachments(int size, Set<Whitespace> ws) {
        ServiceNode serviceNode = serviceNodeStack.peek();
        serviceNode.addWS(ws);
        for (int i = 0; i < size; i++) {
            BLangNameReference nameReference = nameReferenceStack.pop();
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
            varRef.pos = nameReference.pos;
            varRef.addWS(nameReference.ws);
            varRef.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
            varRef.variableName = (BLangIdentifier) nameReference.name;
            serviceNode.bindToEndpoint(varRef);
        }
    }

    void endServiceDef(DiagnosticPos pos, Set<Whitespace> ws, String serviceName, DiagnosticPos identifierPos,
                       boolean constrained) {
        BLangService serviceNode = (BLangService) serviceNodeStack.pop();
        BLangIdentifier identifier = (BLangIdentifier) createIdentifier(serviceName);
        identifier.pos = identifierPos;
        serviceNode.setName(identifier);
        if (constrained) {
            final BLangNameReference epName = nameReferenceStack.pop();
            serviceNode.setServiceTypeStruct(createUserDefinedType(pos, epName.ws, (BLangIdentifier) epName.pkgAlias,
                    (BLangIdentifier) epName.name));
        }
        serviceNode.pos = pos;
        serviceNode.addWS(ws);
        this.compUnit.addTopLevelNode(serviceNode);
        endEndpointDeclarationScope();
    }

    void startResourceDef() {
        ResourceNode resourceNode = TreeBuilder.createResourceNode();
        invokableNodeStack.push(resourceNode);
        startEndpointDeclarationScope(((BLangResource) resourceNode).endpoints);
    }

    void endResourceDef(DiagnosticPos pos, Set<Whitespace> ws, String resourceName, boolean markdownDocPresent,
                        boolean isDeprecated, boolean hasParameters) {
        BLangResource resourceNode = (BLangResource) invokableNodeStack.pop();
        endEndpointDeclarationScope();
        resourceNode.pos = pos;
        resourceNode.addWS(ws);
        BLangIdentifier name = (BLangIdentifier) createIdentifier(resourceName);
        name.pos = pos;
        resourceNode.setName(name);
        if (markdownDocPresent) {
            attachMarkdownDocumentations(resourceNode);
        }
        if (isDeprecated) {
            attachDeprecatedNode(resourceNode);
        }
        if (hasParameters) {
            BLangVariable firstParam = varListStack.peek().get(0);
            if (firstParam.name.value.startsWith("$") && varListStack.peek().size() > 1) {
                // This is an endpoint variable
                Set<Whitespace> wsBeforeComma = removeNthFromLast(firstParam.getWS(), 0);
                resourceNode.addWS(wsBeforeComma);
            }
            varListStack.pop().forEach(resourceNode::addParameter);
        }

        // Set the return type node
        BLangValueType nillTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nillTypeNode.pos = pos;
        nillTypeNode.typeKind = TypeKind.NIL;
        resourceNode.returnTypeNode = nillTypeNode;

        serviceNodeStack.peek().addResource(resourceNode);
    }

    void addResourceAnnotation(int annotCount) {
        BLangResource resourceNode = (BLangResource) invokableNodeStack.peek();
        attachAnnotations(resourceNode, annotCount);
    }

    void addEndpointVariable(DiagnosticPos pos, Set<Whitespace> ws, String endpointName) {
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        // endpointName has to be redefine at semantic analyze phase. So appending $ to make it work.
        IdentifierNode name = this.createIdentifier("$" + endpointName);
        var.setName(name);
        var.addWS(ws);
        // Type will be calculated at SymEnter phase.
        if (varListStack.empty()) {
            varListStack.push(new ArrayList<>());
        }
        varListStack.peek().add(0, var);
    }

    void createXMLQName(DiagnosticPos pos, Set<Whitespace> ws, String localname, String prefix) {
        BLangXMLQName qname = (BLangXMLQName) TreeBuilder.createXMLQNameNode();
        qname.localname = (BLangIdentifier) createIdentifier(localname);
        qname.prefix = (BLangIdentifier) createIdentifier(prefix);
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
                             boolean isTopLevel) {
        BLangXMLNS xmlns = (BLangXMLNS) TreeBuilder.createXMLNSNode();
        BLangIdentifier prefixIdentifer = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        prefixIdentifer.pos = pos;
        prefixIdentifer.value = prefix;

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
        namedArg.name = (BLangIdentifier) this.createIdentifier(name);
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
        BLangVariableDef defaultableParam = (BLangVariableDef) TreeBuilder.createVariableDefinitionNode();
        defaultableParam.pos = pos;
        defaultableParam.addWS(ws);
        List<BLangVariable> params = this.varListStack.peek();
        BLangVariable var = params.remove(params.size() - 1);
        var.expr = (BLangExpression) this.exprNodeStack.pop();
        defaultableParam.var = var;
        this.defaultableParamsList.add(defaultableParam);
    }

    void addRestParam(DiagnosticPos pos, Set<Whitespace> ws, String identifier, int annotCount) {
        BLangVariable restParam = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, false);
        attachAnnotations(restParam, annotCount);
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

    void startOrderByClauseNode(DiagnosticPos pos) {
        OrderByNode orderByNode = TreeBuilder.createOrderByNode();
        ((BLangOrderBy) orderByNode).pos = pos;
        this.orderByClauseStack.push(orderByNode);
    }

    void endOrderByClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        OrderByNode orderByNode = this.orderByClauseStack.peek();
        ((BLangOrderBy) orderByNode).pos = pos;
        orderByNode.addWS(ws);
        Collections.reverse(orderByVariableStack);
        while (!this.orderByVariableStack.empty()) {
            orderByNode.addOrderByVariable(this.orderByVariableStack.pop());
        }
    }

    void startOrderByVariableNode(DiagnosticPos pos) {
        OrderByVariableNode orderByVariableNode = TreeBuilder.createOrderByVariableNode();
        ((BLangOrderByVariable) orderByVariableNode).pos = pos;
        this.orderByVariableStack.push(orderByVariableNode);
    }

    void endOrderByVariableNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isAscending,
                                boolean isDescending) {
        OrderByVariableNode orderByVariableNode = this.orderByVariableStack.peek();
        ((BLangOrderByVariable) orderByVariableNode).pos = pos;
        orderByVariableNode.addWS(ws);
        orderByVariableNode.setVariableReference(this.exprNodeStack.pop());
        orderByVariableNode.setOrderByType(isAscending, isDescending);
    }

    void startLimitClauseNode(DiagnosticPos pos) {
        LimitNode limitNode = TreeBuilder.createLimitNode();
        ((BLangLimit) limitNode).pos = pos;
        this.limitClauseStack.push(limitNode);
    }

    void endLimitClauseNode(DiagnosticPos pos, Set<Whitespace> ws, String limitValue) {
        LimitNode limitNode = this.limitClauseStack.peek();
        ((BLangLimit) limitNode).pos = pos;
        limitNode.addWS(ws);
        limitNode.setLimitValue(limitValue);
    }

    void startGroupByClauseNode(DiagnosticPos pos) {
        GroupByNode groupByNode = TreeBuilder.createGroupByNode();
        ((BLangGroupBy) groupByNode).pos = pos;
        this.groupByClauseStack.push(groupByNode);
    }

    void endGroupByClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        GroupByNode groupByNode = this.groupByClauseStack.peek();
        ((BLangGroupBy) groupByNode).pos = pos;
        groupByNode.addWS(ws);
        groupByNode.addWS(commaWsStack.pop());
        this.exprNodeListStack.pop().forEach(groupByNode::addVariableReference);
    }

    void startHavingClauseNode(DiagnosticPos pos) {
        HavingNode havingNode = TreeBuilder.createHavingNode();
        ((BLangHaving) havingNode).pos = pos;
        this.havingClauseStack.push(havingNode);
    }

    void endHavingClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        HavingNode havingNode = this.havingClauseStack.peek();
        ((BLangHaving) havingNode).pos = pos;
        havingNode.addWS(ws);
        havingNode.setExpression(this.exprNodeStack.pop());
    }

    void startSelectExpressionNode(DiagnosticPos pos) {
        SelectExpressionNode selectExpr = TreeBuilder.createSelectExpressionNode();
        ((BLangSelectExpression) selectExpr).pos = pos;
        this.selectExpressionsStack.push(selectExpr);
    }

    void endSelectExpressionNode(String identifier, DiagnosticPos pos, Set<Whitespace> ws) {
        SelectExpressionNode selectExpression = this.selectExpressionsStack.peek();
        selectExpression.setExpression(exprNodeStack.pop());
        ((BLangSelectExpression) selectExpression).pos = pos;
        selectExpression.addWS(ws);
        selectExpression.setIdentifier(identifier);
    }

    void startSelectExpressionList() {
        this.selectExpressionsListStack.push(new ArrayList<>());
    }

    void endSelectExpressionList(Set<Whitespace> ws, int selectExprCount) {
        commaWsStack.push(ws);
        List<SelectExpressionNode> selectExprList = this.selectExpressionsListStack.peek();
        addSelectExprToSelectExprNodeList(selectExprList, selectExprCount);
    }

    private void addSelectExprToSelectExprNodeList(List<SelectExpressionNode> selectExprList, int n) {
        if (this.selectExpressionsStack.empty()) {
            throw new IllegalStateException("Select expression stack cannot be empty in processing a SelectClause");
        }
        SelectExpressionNode expr = this.selectExpressionsStack.pop();
        if (n > 1) {
            addSelectExprToSelectExprNodeList(selectExprList, n - 1);
        }
        selectExprList.add(expr);
    }

    void startWhereClauseNode(DiagnosticPos pos) {
        WhereNode whereNode = TreeBuilder.createWhereNode();
        ((BLangWhere) whereNode).pos = pos;
        this.whereClauseStack.push(whereNode);
    }

    void endWhereClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        WhereNode whereNode = this.whereClauseStack.peek();
        ((BLangWhere) whereNode).pos = pos;
        whereNode.addWS(ws);
        whereNode.setExpression(exprNodeStack.pop());
    }

    void startSelectClauseNode(DiagnosticPos pos) {
        SelectClauseNode selectClauseNode = TreeBuilder.createSelectClauseNode();
        ((BLangSelectClause) selectClauseNode).pos = pos;
        this.selectClausesStack.push(selectClauseNode);
    }

    void endSelectClauseNode(boolean isSelectAll, boolean isGroupByAvailable, boolean isHavingAvailable,
                             DiagnosticPos pos, Set<Whitespace> ws) {
        SelectClauseNode selectClauseNode = this.selectClausesStack.peek();
        ((BLangSelectClause) selectClauseNode).pos = pos;
        selectClauseNode.addWS(ws);
        if (!isSelectAll) {
            selectClauseNode.addWS(commaWsStack.pop());
            selectClauseNode.setSelectExpressions(this.selectExpressionsListStack.pop());
        } else {
            selectClauseNode.setSelectAll(true);
        }
        if (isGroupByAvailable) {
            selectClauseNode.setGroupBy(this.groupByClauseStack.pop());
        }
        if (isHavingAvailable) {
            selectClauseNode.setHaving(this.havingClauseStack.pop());
        }
    }

    void startWindowClauseNode(DiagnosticPos pos) {
        WindowClauseNode windowClauseNode = TreeBuilder.createWindowClauseNode();
        ((BLangWindow) windowClauseNode).pos = pos;
        this.windowClausesStack.push(windowClauseNode);
    }

    void endWindowsClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        WindowClauseNode windowClauseNode = this.windowClausesStack.peek();
        ((BLangWindow) windowClauseNode).pos = pos;
        windowClauseNode.addWS(ws);
        windowClauseNode.setFunctionInvocation(this.exprNodeStack.pop());

        if (this.exprNodeStack.size() > 1) { // contains other than the streaming input name reference
            List<ExpressionNode> exprList = new ArrayList<>();
            addExprToExprNodeList(exprList, this.exprNodeStack.size() - 1);
            StreamingInput streamingInput = this.streamingInputStack.peek();
            streamingInput.setPreFunctionInvocations(exprList);
        }

        if (!this.whereClauseStack.empty()) {
            this.streamingInputStack.peek().setWindowTraversedAfterWhere(true);
        } else {
            this.streamingInputStack.peek().setWindowTraversedAfterWhere(false);
        }
    }

    void startStreamingInputNode(DiagnosticPos pos) {
        StreamingInput streamingInput = TreeBuilder.createStreamingInputNode();
        ((BLangStreamingInput) streamingInput).pos = pos;
        this.streamingInputStack.push(streamingInput);
    }

    void endStreamingInputNode(String alias, DiagnosticPos pos,
                               Set<Whitespace> ws) {
        BLangStreamingInput streamingInput = (BLangStreamingInput) this.streamingInputStack.peek();
        streamingInput.pos = pos;
        streamingInput.addWS(ws);

        if (this.whereClauseStack.size() == 2) {
            streamingInput.setAfterStreamingCondition(this.whereClauseStack.pop());
            streamingInput.setBeforeStreamingCondition(this.whereClauseStack.pop());
        } else if (this.whereClauseStack.size() == 1) {
            if (streamingInput.isWindowTraversedAfterWhere()) {
                streamingInput.setBeforeStreamingCondition(this.whereClauseStack.pop());
            } else {
                streamingInput.setAfterStreamingCondition(this.whereClauseStack.pop());
            }
        }

        if (this.exprNodeStack.size() > 1) {
            List<ExpressionNode> exprList = new ArrayList<>();
            addExprToExprNodeList(exprList, this.exprNodeStack.size() - 1);
            streamingInput.setPostFunctionInvocations(exprList);
        }

        if (!this.windowClausesStack.empty()) {
            streamingInput.setWindowClause(this.windowClausesStack.pop());
        }
        streamingInput.setStreamReference(this.exprNodeStack.pop());
        streamingInput.setAlias(alias);
    }

    void startJoinStreamingInputNode(DiagnosticPos pos) {
        JoinStreamingInput joinStreamingInput = TreeBuilder.createJoinStreamingInputNode();
        ((BLangJoinStreamingInput) joinStreamingInput).pos = pos;
        this.joinStreamingInputsStack.push(joinStreamingInput);
    }

    void endJoinStreamingInputNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isUnidirectionalBeforeJoin,
                                   boolean isUnidirectionalAfterJoin, String joinType) {
        JoinStreamingInput joinStreamingInput = this.joinStreamingInputsStack.peek();
        ((BLangJoinStreamingInput) joinStreamingInput).pos = pos;
        joinStreamingInput.addWS(ws);
        joinStreamingInput.setStreamingInput(this.streamingInputStack.pop());
        if (this.exprNodeStack.size() > 0) {
            joinStreamingInput.setOnExpression(this.exprNodeStack.pop());
        }
        joinStreamingInput.setUnidirectionalBeforeJoin(isUnidirectionalBeforeJoin);
        joinStreamingInput.setUnidirectionalAfterJoin(isUnidirectionalAfterJoin);
        joinStreamingInput.setJoinType(joinType);
    }

    void endJoinType(Set<Whitespace> ws) {
        JoinStreamingInput joinStreamingInput = this.joinStreamingInputsStack.peek();
        joinStreamingInput.addWS(ws);
    }

    void startTableQueryNode(DiagnosticPos pos) {
        TableQuery tableQuery = TreeBuilder.createTableQueryNode();
        ((BLangTableQuery) tableQuery).pos = pos;
        this.tableQueriesStack.push(tableQuery);
    }

    void endTableQueryNode(boolean isJoinClauseAvailable, boolean isSelectClauseAvailable,
                           boolean isOrderByClauseAvailable, boolean isLimitClauseAvailable, DiagnosticPos pos,
                           Set<Whitespace> ws) {
        BLangTableQuery tableQuery = (BLangTableQuery) this.tableQueriesStack.peek();
        tableQuery.pos = pos;
        tableQuery.addWS(ws);
        tableQuery.setStreamingInput(this.streamingInputStack.pop());
        if (isJoinClauseAvailable) {
            tableQuery.setJoinStreamingInput(this.joinStreamingInputsStack.pop());
        }
        if (isSelectClauseAvailable) {
            tableQuery.setSelectClause(this.selectClausesStack.pop());
        }
        if (isOrderByClauseAvailable) {
            tableQuery.setOrderByClause(this.orderByClauseStack.pop());
        }
        if (isLimitClauseAvailable) {
            tableQuery.setLimitClause(this.limitClauseStack.pop());
        }
    }

    void addTableQueryExpression(DiagnosticPos pos, Set<Whitespace> ws) {
        TableQueryExpression tableQueryExpression = TreeBuilder.createTableQueryExpression();
        ((BLangTableQueryExpression) tableQueryExpression).pos = pos;
        tableQueryExpression.addWS(ws);
        tableQueryExpression.setTableQuery(tableQueriesStack.pop());
        this.exprNodeStack.push(tableQueryExpression);
    }

    void startSetAssignmentClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        SetAssignmentNode setAssignmentNode = TreeBuilder.createSetAssignmentNode();
        ((BLangSetAssignment) setAssignmentNode).pos = pos;
        setAssignmentNode.addWS(ws);
        this.setAssignmentStack.push(setAssignmentNode);
    }

    void endSetAssignmentClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        if (this.exprNodeStack.empty()) {
            throw new IllegalStateException("Expression stack cannot be empty in processing a Set Assignment Clause");
        }
        SetAssignmentNode setAssignmentNode = this.setAssignmentStack.peek();

        ((BLangSetAssignment) setAssignmentNode).pos = pos;
        setAssignmentNode.addWS(ws);

        setAssignmentNode.setExpression(exprNodeStack.pop());
        setAssignmentNode.setVariableReference(exprNodeStack.pop());
    }

    void startSetClauseNode() {
        this.setAssignmentListStack.push(new ArrayList<>());
    }

    void endSetClauseNode(Set<Whitespace> ws, int selectExprCount) {
        List<SetAssignmentNode> setAssignmentNodeList = this.setAssignmentListStack.peek();
        addSetAssignmentToSelectAssignmentNodeList(setAssignmentNodeList, selectExprCount);
    }

    private void addSetAssignmentToSelectAssignmentNodeList(List<SetAssignmentNode> setAssignmentNodeList, int n) {
        if (this.setAssignmentStack.empty()) {
            throw new IllegalStateException("Set expression stack cannot be empty in processing a SelectClause");
        }
        SetAssignmentNode expr = this.setAssignmentStack.pop();
        if (n > 1) {
            addSetAssignmentToSelectAssignmentNodeList(setAssignmentNodeList, n - 1);
        }
        setAssignmentNodeList.add(expr);
    }

    void startStreamActionNode(DiagnosticPos pos, PackageID packageID) {
        StreamActionNode streamActionNode = TreeBuilder.createStreamActionNode();
        ((BLangStreamAction) streamActionNode).pos = pos;
        this.streamActionNodeStack.push(streamActionNode);
        this.startLambdaFunctionDef(packageID);
        this.startBlock();
    }

    void endStreamActionNode(DiagnosticPos pos, Set<Whitespace> ws) {
        endCallableUnitBody(ws);
        StreamActionNode streamActionNode = this.streamActionNodeStack.peek();
        ((BLangStreamAction) streamActionNode).pos = pos;
        streamActionNode.addWS(ws);
        this.varListStack.push(new ArrayList<>());
        this.varListStack.peek().add(this.varStack.pop());
        this.commaWsStack.push(ws);
        this.addLambdaFunctionDef(pos, ws, true, false, false);
        streamActionNode.setInvokableBody((BLangLambdaFunction) this.exprNodeStack.pop());
    }

    void startPatternStreamingEdgeInputNode(DiagnosticPos pos) {
        PatternStreamingEdgeInputNode patternStreamingEdgeInputNode = TreeBuilder.createPatternStreamingEdgeInputNode();
        ((BLangPatternStreamingEdgeInput) patternStreamingEdgeInputNode).pos = pos;
        this.patternStreamingEdgeInputStack.push(patternStreamingEdgeInputNode);
    }

    void endPatternStreamingEdgeInputNode(DiagnosticPos pos, Set<Whitespace> ws, String alias) {
        PatternStreamingEdgeInputNode patternStreamingEdgeInputNode = this.patternStreamingEdgeInputStack.peek();

        ((BLangPatternStreamingEdgeInput) patternStreamingEdgeInputNode).pos = pos;
        patternStreamingEdgeInputNode.addWS(ws);

        if (exprNodeStack.size() == 2) {
            patternStreamingEdgeInputNode.setExpression(exprNodeStack.pop());
            patternStreamingEdgeInputNode.setStreamReference(exprNodeStack.pop());
        } else if (exprNodeStack.size() == 1) {
            patternStreamingEdgeInputNode.setStreamReference(exprNodeStack.pop());
        }

        if (!whereClauseStack.empty()) {
            patternStreamingEdgeInputNode.setWhereClause(whereClauseStack.pop());
        }
        patternStreamingEdgeInputNode.setAliasIdentifier(alias);
    }

    void startPatternStreamingInputNode(DiagnosticPos pos) {
        PatternStreamingInputNode patternStreamingInputNode = TreeBuilder.createPatternStreamingInputNode();
        ((BLangPatternStreamingInput) patternStreamingInputNode).pos = pos;
        this.patternStreamingInputStack.push(patternStreamingInputNode);
    }

    void endPatternStreamingInputNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isFollowedBy,
                                      boolean enclosedInParenthesis, boolean andWithNotAvailable,
                                      boolean forWithNotAvailable, boolean onlyAndAvailable,
                                      boolean onlyOrAvailable, boolean commaSeparated,
                                      String timeDurationValue, String timeScale) {
        if (!this.patternStreamingInputStack.empty()) {
            PatternStreamingInputNode patternStreamingInputNode = this.patternStreamingInputStack.pop();

            ((BLangPatternStreamingInput) patternStreamingInputNode).pos = pos;
            patternStreamingInputNode.addWS(ws);

            if (isFollowedBy) {
                processFollowedByPattern(patternStreamingInputNode);
            }

            if (enclosedInParenthesis) {
                processEnclosedPattern(patternStreamingInputNode);
            }

            if (andWithNotAvailable) {
                processNegationPattern(patternStreamingInputNode);
            }

            if (onlyAndAvailable) {
                processPatternWithAndCondition(patternStreamingInputNode);
            }

            if (onlyOrAvailable) {
                processPatternWithOrCondition(patternStreamingInputNode);
            }

            if (forWithNotAvailable) {
                processNegationPatternWithTimeDuration(patternStreamingInputNode, timeDurationValue, timeScale);
            }

            if (commaSeparated) {
                processCommaSeparatedSequence(patternStreamingInputNode);
            }

            if (!(isFollowedBy || enclosedInParenthesis || forWithNotAvailable ||
                    onlyAndAvailable || onlyOrAvailable || andWithNotAvailable || commaSeparated)) {
                patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
                this.recentStreamingPatternInputNode = patternStreamingInputNode;
            }
        }

        if (this.patternStreamingInputStack.empty()) {
            this.patternStreamingInputStack.push(this.recentStreamingPatternInputNode);
            this.recentStreamingPatternInputNode = null;
        }
    }

    private void processCommaSeparatedSequence(PatternStreamingInputNode patternStreamingInputNode) {
        patternStreamingInputNode.setCommaSeparated(true);
        patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
        patternStreamingInputNode.setPatternStreamingInput(this.recentStreamingPatternInputNode);
        this.recentStreamingPatternInputNode = patternStreamingInputNode;
    }

    private void processNegationPatternWithTimeDuration(PatternStreamingInputNode patternStreamingInputNode,
                                                        String timeDurationValue, String timeScale) {
        patternStreamingInputNode.setForWithNot(true);
        patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
        patternStreamingInputNode.setTimeDurationValue(timeDurationValue);
        patternStreamingInputNode.setTimeScale(timeScale);
        this.recentStreamingPatternInputNode = patternStreamingInputNode;
    }

    private void processPatternWithOrCondition(PatternStreamingInputNode patternStreamingInputNode) {
        patternStreamingInputNode.setOrOnly(true);
        patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
        patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
        this.recentStreamingPatternInputNode = patternStreamingInputNode;
    }

    private void processPatternWithAndCondition(PatternStreamingInputNode patternStreamingInputNode) {
        patternStreamingInputNode.setAndOnly(true);
        patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
        patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
        this.recentStreamingPatternInputNode = patternStreamingInputNode;
    }

    private void processNegationPattern(PatternStreamingInputNode patternStreamingInputNode) {
        patternStreamingInputNode.setAndWithNot(true);
        patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
        patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
        this.recentStreamingPatternInputNode = patternStreamingInputNode;
    }

    private void processEnclosedPattern(PatternStreamingInputNode patternStreamingInputNode) {
        patternStreamingInputNode.setEnclosedInParenthesis(true);
        patternStreamingInputNode.setPatternStreamingInput(this.recentStreamingPatternInputNode);
        this.recentStreamingPatternInputNode = patternStreamingInputNode;
    }

    private void processFollowedByPattern(PatternStreamingInputNode patternStreamingInputNode) {
        patternStreamingInputNode.setFollowedBy(true);
        patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
        patternStreamingInputNode.setPatternStreamingInput(this.recentStreamingPatternInputNode);
        this.recentStreamingPatternInputNode = patternStreamingInputNode;
    }

    void startStreamingQueryStatementNode(DiagnosticPos pos) {
        StreamingQueryStatementNode streamingQueryStatementNode = TreeBuilder.createStreamingQueryStatementNode();
        ((BLangStreamingQueryStatement) streamingQueryStatementNode).pos = pos;
        this.streamingQueryStatementStack.push(streamingQueryStatementNode);
    }

    void endStreamingQueryStatementNode(DiagnosticPos pos, Set<Whitespace> ws) {
        StreamingQueryStatementNode streamingQueryStatementNode = this.streamingQueryStatementStack.peek();

        ((BLangStreamingQueryStatement) streamingQueryStatementNode).pos = pos;
        streamingQueryStatementNode.addWS(ws);

        if (!streamingInputStack.empty()) {
            streamingQueryStatementNode.setStreamingInput(streamingInputStack.pop());

            if (!joinStreamingInputsStack.empty()) {
                streamingQueryStatementNode.setJoinStreamingInput(joinStreamingInputsStack.pop());
            }
        } else if (!patternClauseStack.empty()) {
            streamingQueryStatementNode.setPatternClause(patternClauseStack.pop());
        }

        if (!selectClausesStack.empty()) {
            streamingQueryStatementNode.setSelectClause(selectClausesStack.pop());
        } else {
            SelectClauseNode selectClauseNode = new BLangSelectClause();
            selectClauseNode.setSelectAll(true);
            streamingQueryStatementNode.setSelectClause(selectClauseNode);
        }

        if (!orderByClauseStack.empty()) {
            streamingQueryStatementNode.setOrderByClause(orderByClauseStack.pop());
        }

        if (!outputRateLimitStack.empty()) {
            streamingQueryStatementNode.setOutputRateLimitNode(outputRateLimitStack.pop());
        }

        streamingQueryStatementNode.setStreamingAction(streamActionNodeStack.pop());
    }

    void startOutputRateLimitNode(DiagnosticPos pos) {
        OutputRateLimitNode outputRateLimit = TreeBuilder.createOutputRateLimitNode();
        ((BLangOutputRateLimit) outputRateLimit).pos = pos;
        this.outputRateLimitStack.push(outputRateLimit);
    }

    void endOutputRateLimitNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isSnapshotOutputRateLimit,
                                boolean isFirst, boolean isLast, boolean isAll, String timeScale,
                                String rateLimitValue) {
        OutputRateLimitNode outputRateLimit = this.outputRateLimitStack.peek();
        ((BLangOutputRateLimit) outputRateLimit).pos = pos;
        outputRateLimit.addWS(ws);

        outputRateLimit.setSnapshot(isSnapshotOutputRateLimit);
        outputRateLimit.setOutputRateType(isFirst, isLast, isAll);
        outputRateLimit.setTimeScale(timeScale);
        outputRateLimit.setRateLimitValue(rateLimitValue);
    }

    void startWithinClause(DiagnosticPos pos) {
        WithinClause withinClause = TreeBuilder.createWithinClause();
        ((BLangWithinClause) withinClause).pos = pos;
        this.withinClauseStack.push(withinClause);
    }

    void endWithinClause(DiagnosticPos pos, Set<Whitespace> ws, String timeDurationValue, String timeScale) {
        WithinClause withinClause = this.withinClauseStack.peek();
        ((BLangWithinClause) withinClause).pos = pos;
        withinClause.addWS(ws);
        withinClause.setTimeDurationValue(timeDurationValue);
        withinClause.setTimeScale(timeScale);
    }

    void startPatternClause(DiagnosticPos pos) {
        PatternClause patternClause = TreeBuilder.createPatternClause();
        ((BLangPatternClause) patternClause).pos = pos;
        this.patternClauseStack.push(patternClause);
    }

    void endPatternClause(boolean isForEvents, boolean isWithinClauseAvailable, DiagnosticPos pos,
                          Set<Whitespace> ws) {
        PatternClause patternClause = this.patternClauseStack.peek();
        ((BLangPatternClause) patternClause).pos = pos;
        patternClause.addWS(ws);
        patternClause.setForAllEvents(isForEvents);
        patternClause.setPatternStreamingInputNode(this.patternStreamingInputStack.pop());
        if (isWithinClauseAvailable) {
            patternClause.setWithinClause(this.withinClauseStack.pop());
        }
    }

    void startForeverNode(DiagnosticPos pos) {
        ForeverNode foreverNode = TreeBuilder.createForeverNode();
        ((BLangForever) foreverNode).pos = pos;
        this.foreverNodeStack.push(foreverNode);
    }

    void endForeverNode(DiagnosticPos pos, Set<Whitespace> ws) {
        ForeverNode foreverNode = this.foreverNodeStack.pop();
        ((BLangForever) foreverNode).pos = pos;
        foreverNode.addWS(ws);

        if (!this.varListStack.empty()) {
            this.varListStack.pop().forEach(foreverNode::addParameter);
        }

        Collections.reverse(streamingQueryStatementStack);
        while (!streamingQueryStatementStack.empty()) {
            foreverNode.addStreamingQueryStatement(streamingQueryStatementStack.pop());
        }

        addStmtToCurrentBlock(foreverNode);

        // implicit import of streams module, user doesn't want to import explicitly
        List<String> nameComps = getPackageNameComps(Names.STREAMS_MODULE.value);
        addImportPackageDeclaration(pos, null, Names.STREAMS_ORG.value, nameComps, null,
                nameComps.get(nameComps.size() - 1));
    }

    void startMatchExpression() {
        this.matchExprPatternNodeListStack.add(new ArrayList<>());
    }

    void addMatchExprPattern(DiagnosticPos pos, Set<Whitespace> ws, String identifier) {
        BLangMatchExprPatternClause pattern = (BLangMatchExprPatternClause) TreeBuilder.createMatchExpressionPattern();
        pattern.expr = (BLangExpression) this.exprNodeStack.pop();
        pattern.pos = pos;
        pattern.addWS(ws);

        String patternIdentifier = identifier == null ? Names.IGNORE.value : identifier;
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        var.setName(this.createIdentifier(patternIdentifier));
        var.setTypeNode(this.typeNodeStack.pop());
        if (identifier != null) {
            Set<Whitespace> varDefWS = removeNthFromStart(ws, 0);
            var.addWS(varDefWS);
        }
        pattern.variable = var;

        this.matchExprPatternNodeListStack.peek().add(pattern);
    }

    void endMatchExpression(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangMatchExpression matchExpr = (BLangMatchExpression) TreeBuilder.createMatchExpression();
        this.matchExprPatternNodeListStack.pop()
                .forEach(pattern -> matchExpr.patternClauses.add((BLangMatchExprPatternClause) pattern));
        matchExpr.expr = (BLangExpression) this.exprNodeStack.pop();
        matchExpr.pos = pos;
        matchExpr.addWS(ws);
        addExpressionNode(matchExpr);
    }

    BLangLambdaFunction getScopesFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, boolean bodyExists, String name) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
        endEndpointDeclarationScope();
        function.pos = pos;
        function.addWS(ws);

        //always a public function
        function.flagSet.add(Flag.PUBLIC);
        function.flagSet.add(Flag.LAMBDA);

        if (!bodyExists) {
            function.body = null;
        }

        BLangIdentifier nameId = new BLangIdentifier();
        nameId.setValue(Names.GEN_VAR_PREFIX + name);
        function.name = nameId;

        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = pos;
        typeNode.typeKind = TypeKind.NIL;
        function.returnTypeNode = typeNode;

        function.receiver = null;
        BLangLambdaFunction lambda = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambda.function = function;
        return lambda;
    }

    void startScopeStmt() {
        scopeNodeStack.push(TreeBuilder.createScopeNode());
        startBlock();
    }

    void addCompensateStatement(DiagnosticPos pos, Set<Whitespace> ws, String name) {
        BLangCompensate compensateNode = (BLangCompensate) TreeBuilder.createCompensateNode();
        compensateNode.pos = pos;
        compensateNode.addWS(ws);
        compensateNode.scopeName = createIdentifier(name);
        compensateNode.invocation.name = (BLangIdentifier) createIdentifier(name);
        compensateNode.invocation.pkgAlias = (BLangIdentifier) createIdentifier(null);

        addStmtToCurrentBlock(compensateNode);
    }

    void endScopeStmt(DiagnosticPos pos, Set<Whitespace> ws, BLangIdentifier scopeName,
                      BLangLambdaFunction compensationFunction) {
        BLangScope scope = (BLangScope) scopeNodeStack.pop();
        scope.pos = pos;
        scope.addWS(ws);
        scope.setScopeName(scopeName);
        addStmtToCurrentBlock(scope);
        if (!scopeNodeStack.isEmpty()) {
            for (String child : scope.childScopes) {
                scopeNodeStack.peek().addChildScope(child);
            }
            scopeNodeStack.peek().addChildScope(scopeName.getValue());
        }

        scope.setCompensationFunction(compensationFunction);
    }

    void addScopeBlock(DiagnosticPos currentPos) {
        ScopeNode scopeNode = scopeNodeStack.peek();
        BLangBlockStmt scopeBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        scopeBlock.pos = currentPos;
        scopeNode.setScopeBody(scopeBlock);
    }

    void startOnCompensationBlock() {
        startFunctionDef();
    }

    public void addTypeReference(DiagnosticPos currentPos, Set<Whitespace> ws) {
        TypeNode typeRef = typeNodeStack.pop();
        typeRef.addWS(ws);
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeNodeStack.peek();
        objectTypeNode.addTypeReference(typeRef);
    }
}
