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
import org.ballerinalang.model.elements.DocTag;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.DeprecatedNode;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.DocumentationNode;
import org.ballerinalang.model.tree.EnumNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ObjectNode;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.RecordNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.StructNode;
import org.ballerinalang.model.tree.TransformerNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.WorkerNode;
import org.ballerinalang.model.tree.clauses.FunctionClauseNode;
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
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.ballerinalang.model.tree.statements.TransactionNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.types.TypeNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangDeprecatedNode;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangEnum.BLangEnumerator;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackageDeclaration;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStmtPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPostIncrement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
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

    private Stack<VariableNode> varStack = new Stack<>();

    private Stack<List<VariableNode>> varListStack = new Stack<>();

    private Stack<InvokableNode> invokableNodeStack = new Stack<>();

    private Stack<ExpressionNode> exprNodeStack = new Stack<>();

    private Stack<List<ExpressionNode>> exprNodeListStack = new Stack<>();

    private Stack<Set<Whitespace>> commaWsStack = new Stack<>();

    private Stack<Set<Whitespace>> invocationWsStack = new Stack<>();

    private Stack<BLangRecordLiteral> recordLiteralNodes = new Stack<>();

    private Stack<BLangTryCatchFinally> tryCatchFinallyNodesStack = new Stack<>();

    private Stack<StructNode> structStack = new Stack<>();

    private Stack<RecordNode> recordStack = new Stack<>();

    private Stack<ObjectNode> objectStack = new Stack<>();

    private Stack<EnumNode> enumStack = new Stack<>();

    private List<BLangEnumerator> enumeratorList = new ArrayList<>();

    private Stack<ConnectorNode> connectorNodeStack = new Stack<>();

    private Stack<List<ActionNode>> actionNodeStack = new Stack<>();

    private Stack<AnnotationNode> annotationStack = new Stack<>();

    private Stack<DocumentationNode> docAttachmentStack = new Stack<>();

    private Stack<DeprecatedNode> deprecatedAttachmentStack = new Stack<>();

    private Stack<AnnotationAttachmentNode> annotAttachmentStack = new Stack<>();

    private Stack<IfNode> ifElseStatementStack = new Stack<>();

    private Stack<TransactionNode> transactionNodeStack = new Stack<>();

    private Stack<ForkJoinNode> forkJoinNodesStack = new Stack<>();

    private Stack<ServiceNode> serviceNodeStack = new Stack<>();

    private Stack<List<BLangEndpoint>> endpointListStack = new Stack<>();

    private BLangEndpoint lastBuiltEndpoint;

    private Stack<XMLAttributeNode> xmlAttributeNodeStack = new Stack<>();

    private Stack<BLangAnnotationAttachmentPoint> attachmentPointStack = new Stack<>();

    private Stack<OrderByNode> orderByClauseStack = new Stack<>();

    private Stack<OrderByVariableNode> orderByVariableStack = new Stack<>();

    private Stack<LimitNode> limitClauseStack = new Stack<>();

    private Stack<GroupByNode> groupByClauseStack = new Stack<>();

    private Stack<HavingNode> havingClauseStack = new Stack<>();

    private Stack<WhereNode> whereClauseStack = new Stack<>();

    private Stack<SelectExpressionNode> selectExpressionsStack = new Stack<>();

    private Stack<List<SelectExpressionNode>> selectExpressionsListStack = new Stack<>();

    private Stack<SelectClauseNode> selectClausesStack = new Stack<>();

    private Stack<FunctionClauseNode> functionClausesStack = new Stack<>();

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

    private BLangAnonymousModelHelper anonymousModelHelper;
    private CompilerOptions compilerOptions;

    /**
     * Keep the number of anonymous structs found so far in the current package.
     * This field is used to generate a name for an anonymous struct.
     */
    private int anonStructCount = 0;

    protected int lambdaFunctionCount = 0;

    private BLangDiagnosticLog dlog;

    private static final String IDENTIFIER_LITERAL_PREFIX = "^\"";
    private static final String IDENTIFIER_LITERAL_SUFFIX = "\"";

    public BLangPackageBuilder(CompilerContext context, CompilationUnitNode compUnit) {
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.compilerOptions = CompilerOptions.getInstance(context);
        this.compUnit = compUnit;
    }

    public void addAttachPoint(BLangAnnotationAttachmentPoint.AttachmentPoint attachPoint) {
        attachmentPointStack.push(new BLangAnnotationAttachmentPoint(attachPoint));
    }

    public void addValueType(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.addWS(ws);
        typeNode.pos = pos;
        typeNode.typeKind = (TreeUtils.stringToTypeKind(typeName.replaceAll("\\s+", "")));

        addType(typeNode);
    }

    public void addUnionType(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangType rhsTypeNode = (BLangType) this.typeNodeStack.pop();
        BLangType lhsTypeNode = (BLangType) this.typeNodeStack.pop();

        BLangUnionTypeNode unionTypeNode;
        if (rhsTypeNode.getKind() == NodeKind.UNION_TYPE_NODE) {
            unionTypeNode = (BLangUnionTypeNode) rhsTypeNode;
            unionTypeNode.memberTypeNodes.add(0, lhsTypeNode);
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

    public void addTupleType(DiagnosticPos pos, Set<Whitespace> ws, int members) {
        BLangTupleTypeNode tupleTypeNode = (BLangTupleTypeNode) TreeBuilder.createTupleTypeNode();
        for (int i = 0; i < members; i++) {
            final BLangType member = (BLangType) this.typeNodeStack.pop();
            tupleTypeNode.memberTypeNodes.add(0, member);
        }
        tupleTypeNode.pos = pos;
        tupleTypeNode.addWS(ws);
        this.typeNodeStack.push(tupleTypeNode);
    }

    void startRecordDef() {
        RecordNode recordNode = TreeBuilder.createRecordNode();
        attachAnnotations(recordNode);
        attachDocumentations(recordNode);
        attachDeprecatedNode(recordNode);
        this.recordStack.add(recordNode);
        startVarList();
    }

    private void endRecordDef(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean publicRecord) {
        BLangRecord recordNode = populateRecordNode(pos, ws, createIdentifier(identifier), false);
        recordNode.setName(this.createIdentifier(identifier));
        if (publicRecord) {
            recordNode.flagSet.add(Flag.PUBLIC);
        }

        this.compUnit.addTopLevelNode(recordNode);
    }

    void addAnonRecordType(DiagnosticPos pos, Set<Whitespace> ws) {
        // Generate a name for the anonymous record
        String genName = anonymousModelHelper.getNextAnonymousRecordKey(pos.src.pkgID);
        IdentifierNode anonRecordGenName = createIdentifier(genName);

        // Create an anonymous record and add it to the list of records in the current package.
        BLangRecord recordNode = populateRecordNode(pos, ws, anonRecordGenName, true);
        recordNode.addFlag(Flag.PUBLIC);
        this.compUnit.addTopLevelNode(recordNode);

        addType(createUserDefinedType(pos, ws, (BLangIdentifier) TreeBuilder.createIdentifierNode(), recordNode.name));

    }

    private BLangRecord populateRecordNode(DiagnosticPos pos, Set<Whitespace> ws,
                                           IdentifierNode name, boolean isAnonymous) {
        BLangRecord recordNode = (BLangRecord) this.recordStack.pop();
        recordNode.pos = pos;
        recordNode.addWS(ws);
        recordNode.name = (BLangIdentifier) name;
        recordNode.isAnonymous = isAnonymous;
        this.varListStack.pop().forEach(variableNode -> {
            ((BLangVariable) variableNode).docTag = DocTag.FIELD;
            recordNode.addField(variableNode);
        });
        return recordNode;
    }

    void addFieldToRecord(DiagnosticPos pos, Set<Whitespace> ws,
                          String identifier, boolean exprAvailable, int annotCount) {

        Set<Whitespace> wsForSemiColon = removeNthFromLast(ws, 0);
        BLangRecord recordNode = (BLangRecord) this.recordStack.peek();
        recordNode.addWS(wsForSemiColon);
        BLangVariable field = addVar(pos, ws, identifier, exprAvailable, annotCount);

        field.flagSet.add(Flag.PUBLIC);
    }

    public void addArrayType(DiagnosticPos pos, Set<Whitespace> ws, int dimensions) {
        BLangType eType = (BLangType) this.typeNodeStack.pop();
        BLangArrayType arrayTypeNode = (BLangArrayType) TreeBuilder.createArrayTypeNode();
        arrayTypeNode.addWS(ws);
        arrayTypeNode.pos = pos;
        arrayTypeNode.elemtype = eType;
        arrayTypeNode.dimensions = dimensions;

        addType(arrayTypeNode);
    }

    public void markTypeNodeAsNullable(Set<Whitespace> ws) {
        BLangType typeNode = (BLangType) this.typeNodeStack.peek();
        typeNode.addWS(ws);
        typeNode.nullable = true;
    }

    public void markTypeNodeAsGrouped(Set<Whitespace> ws) {
        BLangType typeNode = (BLangType) this.typeNodeStack.peek();
        typeNode.addWS(ws);
        typeNode.grouped = true;
    }

    public void addUserDefineType(Set<Whitespace> ws) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType userDefinedType = createUserDefinedType(nameReference.pos, ws,
                (BLangIdentifier) nameReference.pkgAlias, (BLangIdentifier) nameReference.name);
        userDefinedType.addWS(nameReference.ws);
        addType(userDefinedType);
    }

    public void addAnonStructType(DiagnosticPos pos, Set<Whitespace> ws) {
        // Generate a name for the anonymous struct
        String genName = anonymousModelHelper.getNextAnonymousStructKey(pos.src.pkgID);
        IdentifierNode anonStructGenName = createIdentifier(genName);

        // Create an anonymous struct and add it to the list of structs in the current package.
        BLangStruct structNode = populateStructNode(pos, ws, anonStructGenName, true);
        this.compUnit.addTopLevelNode(structNode);

        addType(createUserDefinedType(pos, ws, (BLangIdentifier) TreeBuilder.createIdentifierNode(), structNode.name));

    }

    public void addBuiltInReferenceType(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
        BLangBuiltInRefTypeNode refType = (BLangBuiltInRefTypeNode) TreeBuilder.createBuiltInReferenceTypeNode();
        refType.typeKind = TreeUtils.stringToTypeKind(typeName);
        refType.pos = pos;
        refType.addWS(ws);
        addType(refType);
    }

    public void addConstraintType(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
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

    public void addConstraintTypeWithTypeName(DiagnosticPos pos, Set<Whitespace> ws, String typeName) {
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

    public void addEndpointType(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangNameReference nameReference = nameReferenceStack.pop();
        BLangUserDefinedType constraintType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        constraintType.pos = pos;
        constraintType.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        constraintType.typeName = (BLangIdentifier) nameReference.name;
        constraintType.addWS(nameReference.ws);
        addType(constraintType);
    }

    public void addFunctionType(DiagnosticPos pos, Set<Whitespace> ws, boolean paramsAvail, boolean paramsTypeOnly,
                                boolean retParamsAvail) {
        // TODO : Fix function main ()(boolean , function(string x)(float, int)){} issue
        BLangFunctionTypeNode functionTypeNode = (BLangFunctionTypeNode) TreeBuilder.createFunctionTypeNode();
        functionTypeNode.pos = pos;
        functionTypeNode.returnsKeywordExists = true;

        if (retParamsAvail) {
            functionTypeNode.returnTypeNode = (BLangType) this.varStack.pop().getTypeNode();
        } else {
            BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            nilTypeNode.pos = pos;
            nilTypeNode.typeKind = TypeKind.NIL;
            functionTypeNode.returnTypeNode = nilTypeNode;
        }

        if (paramsAvail) {
            functionTypeNode.addWS(commaWsStack.pop());
            this.varListStack.pop().forEach(v -> functionTypeNode.paramTypeNodes.add((BLangType) v.getTypeNode()));
        }

        functionTypeNode.addWS(ws);
        addType(functionTypeNode);
    }

    private void addType(TypeNode typeNode) {
        this.typeNodeStack.push(typeNode);
    }

    public void addNameReference(DiagnosticPos currentPos, Set<Whitespace> ws, String pkgName, String name) {
        IdentifierNode pkgNameNode = createIdentifier(pkgName);
        IdentifierNode nameNode = createIdentifier(name);
        nameReferenceStack.push(new BLangNameReference(currentPos, ws, pkgNameNode, nameNode));
    }

    public void startVarList() {
        this.varListStack.push(new ArrayList<>());
    }

    public void startFunctionDef() {
        FunctionNode functionNode = TreeBuilder.createFunctionNode();
        attachAnnotations(functionNode);
        attachDocumentations(functionNode);
        attachDeprecatedNode(functionNode);
        this.invokableNodeStack.push(functionNode);
        startEndpointDeclarationScope(((BLangFunction) functionNode).endpoints);
    }

    public void startBlock() {
        this.blockNodeStack.push(TreeBuilder.createBlockNode());
    }

    private IdentifierNode createIdentifier(String value) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (value == null) {
            return node;
        }

        if (value.startsWith(IDENTIFIER_LITERAL_PREFIX) && value.endsWith(IDENTIFIER_LITERAL_SUFFIX)) {
            node.setValue(value.substring(2, value.length() - 1));
            node.setLiteral(true);
        } else {
            node.setValue(value);
            node.setLiteral(false);
        }
        return node;
    }

    public void addVarToStruct(DiagnosticPos pos,
                               Set<Whitespace> ws,
                               String identifier,
                               boolean exprAvailable,
                               int annotCount,
                               boolean isPrivate) {

        Set<Whitespace> wsForSemiColon = removeNthFromLast(ws, 0);
        BLangStruct structNode = (BLangStruct) this.structStack.peek();
        structNode.addWS(wsForSemiColon);
        BLangVariable field = addVar(pos, ws, identifier, exprAvailable, annotCount);

        if (!isPrivate) {
            field.flagSet.add(Flag.PUBLIC);
        }
    }

    public BLangVariable addVar(DiagnosticPos pos,
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

    public BLangVariable addReturnParam(DiagnosticPos pos,
                                        Set<Whitespace> ws,
                                        String identifier,
                                        boolean exprAvailable,
                                        int annotCount) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, exprAvailable);
        attachAnnotations(var, annotCount);
        var.pos = pos;
        this.varStack.push(var);
        return var;
    }

    public void endCallableUnitSignature(DiagnosticPos pos,
                                         Set<Whitespace> ws,
                                         String identifier,
                                         boolean paramsAvail,
                                         boolean retParamsAvail,
                                         boolean restParamAvail) {
        InvokableNode invNode = this.invokableNodeStack.peek();
        invNode.setName(this.createIdentifier(identifier));
        invNode.addWS(ws);
        BLangType returnTypeNode;
        if (retParamsAvail) {
            BLangVariable varNode = (BLangVariable) this.varStack.pop();
            returnTypeNode = varNode.getTypeNode();
            returnTypeNode.addWS(varNode.getWS());
            varNode.getAnnotationAttachments().forEach(invNode::addReturnTypeAnnotationAttachment);
        } else {
            BLangValueType nillTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            nillTypeNode.pos = pos;
            nillTypeNode.typeKind = TypeKind.NIL;
            returnTypeNode = nillTypeNode;
        }
        invNode.setReturnTypeNode(returnTypeNode);

        if (paramsAvail) {
            this.varListStack.pop().forEach(variableNode -> {
                ((BLangVariable) variableNode).docTag = DocTag.PARAM;
                invNode.addParameter(variableNode);
            });

            this.defaultableParamsList.forEach(variableDef -> {
                BLangVariableDef varDef = (BLangVariableDef) variableDef;
                varDef.var.docTag = DocTag.PARAM;
                invNode.addDefaultableParameter(varDef);
            });
            this.defaultableParamsList = new ArrayList<>();

            if (restParamAvail) {
                invNode.setRestParameter(this.restParamStack.pop());
            }
        }
    }

    public void startLambdaFunctionDef(PackageID pkgID) {
        startFunctionDef();
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.setName(createIdentifier(anonymousModelHelper.getNextAnonymousFunctionKey(pkgID)));
        lambdaFunction.addFlag(Flag.LAMBDA);
    }

    public void addLambdaFunctionDef(DiagnosticPos pos,
                                     Set<Whitespace> ws,
                                     boolean paramsAvail,
                                     boolean retParamsAvail,
                                     boolean restParamAvail) {
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.pos = pos;
        endCallableUnitSignature(pos, ws, lambdaFunction.getName().value, paramsAvail, retParamsAvail, restParamAvail);
        BLangLambdaFunction lambdaExpr = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaExpr.function = lambdaFunction;
        lambdaExpr.pos = pos;
        addExpressionNode(lambdaExpr);
        // TODO: is null correct here
        endFunctionDef(pos, null, false, false, true, false, true);
        //this is added for analysing closures
        if (!(blockNodeStack.empty())) {
            lambdaFunction.enclBlockStmt = (BLangBlockStmt) blockNodeStack.peek();
        }
    }

    private void startEndpointDeclarationScope(List<BLangEndpoint> endpointList) {
        endpointListStack.push(endpointList);
    }

    private void endEndpointDeclarationScope() {
        endpointListStack.pop();
    }

    public void addEndpointDefinition(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean initExprExist) {
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

    public void markLastEndpointAsPublic() {
        lastBuiltEndpoint.flagSet.add(Flag.PUBLIC);
    }

    public void markLastInvocationAsAsync(DiagnosticPos pos) {
        final ExpressionNode expressionNode = this.exprNodeStack.peek();
        if (expressionNode.getKind() == NodeKind.INVOCATION) {
            ((BLangInvocation) this.exprNodeStack.peek()).async = true;
        } else {
            dlog.error(pos, DiagnosticCode.START_REQUIRE_INVOCATION);
        }
    }

    public void addVariableDefStatement(DiagnosticPos pos,
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

    public void addTypeInitExpression(DiagnosticPos pos, Set<Whitespace> ws, String initName, boolean typeAvailable,
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

    public void startTryCatchFinallyStmt() {
        this.tryCatchFinallyNodesStack.push((BLangTryCatchFinally) TreeBuilder.createTryCatchFinallyNode());
        startBlock();
    }

    public void addTryClause(DiagnosticPos pos) {
        BLangBlockStmt tryBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        tryBlock.pos = pos;
        tryCatchFinallyNodesStack.peek().tryBody = tryBlock;
    }

    public void startCatchClause() {
        startBlock();
    }

    public void addCatchClause(DiagnosticPos poc, Set<Whitespace> ws, String paramName) {
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

    public void startFinallyBlock() {
        startBlock();
    }

    public void addFinallyBlock(DiagnosticPos poc, Set<Whitespace> ws) {
        BLangBlockStmt blockNode = (BLangBlockStmt) this.blockNodeStack.pop();
        BLangTryCatchFinally rootTry = tryCatchFinallyNodesStack.peek();
        rootTry.finallyBody = blockNode;
        rootTry.addWS(ws);
        blockNode.pos = poc;
    }

    public void addTryCatchFinallyStmt(DiagnosticPos poc, Set<Whitespace> ws) {
        BLangTryCatchFinally stmtNode = tryCatchFinallyNodesStack.pop();
        stmtNode.pos = poc;
        stmtNode.addWS(ws);
        addStmtToCurrentBlock(stmtNode);
    }

    public void addThrowStmt(DiagnosticPos poc, Set<Whitespace> ws) {
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

    public void addLiteralValue(DiagnosticPos pos, Set<Whitespace> ws, int typeTag, Object value) {
        BLangLiteral litExpr = (BLangLiteral) TreeBuilder.createLiteralExpression();
        litExpr.addWS(ws);
        litExpr.pos = pos;
        litExpr.typeTag = typeTag;
        litExpr.value = value;
        addExpressionNode(litExpr);
    }

    public void addArrayInitExpr(DiagnosticPos pos, Set<Whitespace> ws, boolean argsAvailable) {
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

    public void addKeyValueRecord(Set<Whitespace> ws) {
        BLangRecordKeyValue keyValue = (BLangRecordKeyValue) TreeBuilder.createRecordKeyValue();
        keyValue.addWS(ws);
        keyValue.valueExpr = (BLangExpression) exprNodeStack.pop();
        keyValue.key = new BLangRecordKey((BLangExpression) exprNodeStack.pop());
        recordLiteralNodes.peek().keyValuePairs.add(keyValue);
    }

    public void addMapStructLiteral(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangRecordLiteral recordTypeLiteralNode = recordLiteralNodes.pop();
        recordTypeLiteralNode.pos = pos;
        recordTypeLiteralNode.addWS(ws);
        addExpressionNode(recordTypeLiteralNode);
    }

    public void addTableLiteral(DiagnosticPos pos, Set<Whitespace> ws) {
        final BLangTableLiteral tableLiteral = (BLangTableLiteral) TreeBuilder.createTableLiteralNode();
        tableLiteral.addWS(ws);
        tableLiteral.pos = pos;
        tableLiteral.configurationExpr = (BLangExpression) this.exprNodeStack.pop();
        addExpressionNode(tableLiteral);
    }

    public void startMapStructLiteral() {
        BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        recordLiteralNodes.push(literalNode);
    }

    public void startExprNodeList() {
        this.exprNodeListStack.push(new ArrayList<>());
    }

    public void endExprNodeList(Set<Whitespace> ws, int exprCount) {
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


    public void createSimpleVariableReference(DiagnosticPos pos, Set<Whitespace> ws) {
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

    public void createFunctionInvocation(DiagnosticPos pos, Set<Whitespace> ws, boolean argsAvailable) {
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
        invocationNode.addWS(nameReference.ws);
        invocationNode.pkgAlias = (BLangIdentifier) nameReference.pkgAlias;
        addExpressionNode(invocationNode);
    }

    public void startInvocationNode(Set<Whitespace> ws) {
        invocationWsStack.push(ws);
    }

    public void createInvocationNode(DiagnosticPos pos, Set<Whitespace> ws, String invocation, boolean argsAvailable,
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

    public void createActionInvocationNode(DiagnosticPos pos, Set<Whitespace> ws, boolean async) {
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

    public void createFieldBasedAccessNode(DiagnosticPos pos, Set<Whitespace> ws, String fieldName,
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

    public void createIndexBasedAccessNode(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangIndexBasedAccess indexBasedAccess = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexBasedAccess.pos = pos;
        indexBasedAccess.addWS(ws);
        indexBasedAccess.indexExpr = (BLangExpression) exprNodeStack.pop();
        indexBasedAccess.expr = (BLangVariableReference) exprNodeStack.pop();
        addExpressionNode(indexBasedAccess);
    }

    public void createBracedOrTupleExpression(DiagnosticPos pos, Set<Whitespace> ws, int numberOfExpressions) {
        final BLangBracedOrTupleExpr expr = (BLangBracedOrTupleExpr) TreeBuilder.createBracedOrTupleExpression();
        expr.pos = pos;
        expr.addWS(ws);
        for (int i = 0; i < numberOfExpressions; i++) {
            expr.expressions.add(0, (BLangExpression) exprNodeStack.pop());
        }
        addExpressionNode(expr);
    }

    public void createBinaryExpr(DiagnosticPos pos, Set<Whitespace> ws, String operator) {
        BLangBinaryExpr binaryExpressionNode = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpressionNode.pos = pos;
        binaryExpressionNode.addWS(ws);
        binaryExpressionNode.rhsExpr = (BLangExpression) exprNodeStack.pop();
        binaryExpressionNode.lhsExpr = (BLangExpression) exprNodeStack.pop();
        binaryExpressionNode.opKind = OperatorKind.valueFrom(operator);
        addExpressionNode(binaryExpressionNode);
    }

    public void createElvisExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangElvisExpr elvisExpr = (BLangElvisExpr) TreeBuilder.createElvisExpressionNode();
        elvisExpr.pos = pos;
        elvisExpr.addWS(ws);
        elvisExpr.rhsExpr = (BLangExpression) exprNodeStack.pop();
        elvisExpr.lhsExpr = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(elvisExpr);
    }

    public void createTypeCastExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTypeCastExpr typeCastNode = (BLangTypeCastExpr) TreeBuilder.createTypeCastNode();
        typeCastNode.pos = pos;
        typeCastNode.addWS(ws);
        typeCastNode.expr = (BLangExpression) exprNodeStack.pop();
        typeCastNode.typeNode = (BLangType) typeNodeStack.pop();
        addExpressionNode(typeCastNode);
    }

    public void createTypeAccessExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangTypedescExpr typeAccessExpr = (BLangTypedescExpr) TreeBuilder.createTypeAccessNode();
        typeAccessExpr.pos = pos;
        typeAccessExpr.addWS(ws);
        typeAccessExpr.typeNode = (BLangType) typeNodeStack.pop();
        addExpressionNode(typeAccessExpr);
    }

    public void createTypeConversionExpr(DiagnosticPos pos, Set<Whitespace> ws, boolean namedTransformer) {
        BLangTypeConversionExpr typeConversionNode = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        typeConversionNode.pos = pos;
        typeConversionNode.addWS(ws);
        typeConversionNode.typeNode = (BLangType) typeNodeStack.pop();
        typeConversionNode.expr = (BLangExpression) exprNodeStack.pop();
        if (namedTransformer) {
            typeConversionNode.transformerInvocation = (BLangInvocation) exprNodeStack.pop();
        }
        addExpressionNode(typeConversionNode);
    }

    public void createUnaryExpr(DiagnosticPos pos, Set<Whitespace> ws, String operator) {
        BLangUnaryExpr unaryExpressionNode = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        unaryExpressionNode.pos = pos;
        unaryExpressionNode.addWS(ws);
        unaryExpressionNode.expr = (BLangExpression) exprNodeStack.pop();
        unaryExpressionNode.operator = OperatorKind.valueFrom(operator);
        addExpressionNode(unaryExpressionNode);
    }

    public void createTernaryExpr(DiagnosticPos pos, Set<Whitespace> ws) {
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

    public void createCheckedExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangCheckedExpr checkedExpr = (BLangCheckedExpr) TreeBuilder.createCheckExpressionNode();
        checkedExpr.pos = pos;
        checkedExpr.addWS(ws);
        checkedExpr.expr = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(checkedExpr);
    }

    public void createAwaitExpr(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangAwaitExpr awaitExpr = (BLangAwaitExpr) TreeBuilder.createAwaitExpressionNode();
        awaitExpr.pos = pos;
        awaitExpr.addWS(ws);
        awaitExpr.expr = (BLangExpression) exprNodeStack.pop();
        addExpressionNode(awaitExpr);
    }

    public void endFunctionDef(DiagnosticPos pos,
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
            BLangVariable receiver = (BLangVariable) this.varStack.pop();
            receiver.docTag = DocTag.RECEIVER;
            function.receiver = receiver;
            function.flagSet.add(Flag.ATTACHED);
        }

        if (!function.deprecatedAttachments.isEmpty()) {
            function.flagSet.add(Flag.DEPRECATED);
        }

        this.compUnit.addTopLevelNode(function);
    }

    public void startWorker() {
        WorkerNode workerNode = TreeBuilder.createWorkerNode();
        this.invokableNodeStack.push(workerNode);
        startBlock();
    }

    public void addWorker(DiagnosticPos pos, Set<Whitespace> ws, String workerName) {
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

    public void attachWorkerWS(Set<Whitespace> ws) {
        BLangWorker worker = (BLangWorker) this.invokableNodeStack.peek();
        worker.addWS(ws);
    }

    public void startForkJoinStmt() {
        this.forkJoinNodesStack.push(TreeBuilder.createForkJoinNode());
    }

    public void addForkJoinStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.pop();
        forkJoin.pos = pos;
        forkJoin.addWS(ws);
        this.addStmtToCurrentBlock(forkJoin);
    }

    public void startJoinCause() {
        startBlock();
    }

    public void addJoinCause(Set<Whitespace> ws, String identifier) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.joinedBody = (BLangBlockStmt) this.blockNodeStack.pop();
        Set<Whitespace> varWS = removeNthFromLast(ws, 3);
        forkJoin.addWS(ws);
        forkJoin.joinResultVar = (BLangVariable) this.generateBasicVarNode(
                (DiagnosticPos) this.typeNodeStack.peek().getPosition(), varWS, identifier, false);
    }

    public void addJoinCondition(Set<Whitespace> ws, String joinType, List<String> workerNames, int joinCount) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.joinedWorkerCount = joinCount;
        forkJoin.joinType = ForkJoinNode.JoinType.valueOf(joinType);
        forkJoin.addWS(ws);
        workerNames.forEach(s -> forkJoin.joinedWorkers.add((BLangIdentifier) createIdentifier(s)));
    }

    public void startTimeoutCause() {
        startBlock();
    }

    public void addTimeoutCause(Set<Whitespace> ws, String identifier) {
        BLangForkJoin forkJoin = (BLangForkJoin) this.forkJoinNodesStack.peek();
        forkJoin.timeoutBody = (BLangBlockStmt) this.blockNodeStack.pop();
        forkJoin.timeoutExpression = (BLangExpression) this.exprNodeStack.pop();
        Set<Whitespace> varWS = removeNthFromLast(ws, 3);
        forkJoin.addWS(ws);
        forkJoin.timeoutVariable = (BLangVariable) this.generateBasicVarNode(
                (DiagnosticPos) this.typeNodeStack.peek().getPosition(), varWS, identifier, false);
    }

    public void endCallableUnitBody(Set<Whitespace> ws) {
        BlockNode block = this.blockNodeStack.pop();
        InvokableNode invokableNode = this.invokableNodeStack.peek();
        invokableNode.addWS(ws);
        invokableNode.setBody(block);
    }

    public void setPackageDeclaration(DiagnosticPos pos, Set<Whitespace> ws, List<String> nameComps, String version) {
        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        nameComps.forEach(e -> pkgNameComps.add((BLangIdentifier) this.createIdentifier(e)));
        BLangIdentifier versionNode = (BLangIdentifier) this.createIdentifier(version);

        BLangPackageDeclaration pkgDcl = (BLangPackageDeclaration) TreeBuilder.createPackageDeclarationNode();
        pkgDcl.pos = pos;
        // TODO: orgname is null, fix it.
        pkgDcl.addWS(ws);
        pkgDcl.pkgNameComps = pkgNameComps;
        pkgDcl.version = versionNode;
        this.compUnit.addTopLevelNode(pkgDcl);
    }

    public void addImportPackageDeclaration(DiagnosticPos pos,
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
            this.dlog.warning(pos, DiagnosticCode.REDECLARED_IMPORT_PACKAGE, importDcl.getQualifiedPackageName());
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

    public void addGlobalVariable(DiagnosticPos pos,
                                  Set<Whitespace> ws,
                                  String identifier,
                                  boolean exprAvailable,
                                  boolean publicVar) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, exprAvailable);
        attachAnnotations(var);
        if (publicVar) {
            var.flagSet.add(Flag.PUBLIC);
        }
        var.docTag = DocTag.VARIABLE;
        attachDocumentations(var);
        attachDeprecatedNode(var);
        this.compUnit.addTopLevelNode(var);
    }

    public void addConstVariable(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                 boolean publicVar, boolean safeAssignment) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, true);
        var.flagSet.add(Flag.FINAL);
        if (publicVar) {
            var.flagSet.add(Flag.PUBLIC);
        }
        var.docTag = DocTag.VARIABLE;
        var.safeAssignment = safeAssignment;

        attachAnnotations(var);
        attachDocumentations(var);
        attachDeprecatedNode(var);
        this.compUnit.addTopLevelNode(var);
    }

    public void startStructDef() {
        StructNode structNode = TreeBuilder.createStructNode();
        attachAnnotations(structNode);
        attachDocumentations(structNode);
        attachDeprecatedNode(structNode);
        this.structStack.add(structNode);
    }

    public void endStructDef(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean publicStruct) {
        BLangStruct structNode = populateStructNode(pos, ws, createIdentifier(identifier), false);
        structNode.setName(this.createIdentifier(identifier));
        if (publicStruct) {
            structNode.flagSet.add(Flag.PUBLIC);
        }

        this.compUnit.addTopLevelNode(structNode);
    }

    void startObjectDef() {
        ObjectNode objectNode = TreeBuilder.createObjectNode();
        attachAnnotations(objectNode);
        attachDocumentations(objectNode);
        attachDeprecatedNode(objectNode);
        this.objectStack.add(objectNode);
        startVarList();
    }

    private void endObjectDef(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean publicRecord) {
        BLangObject objectNode = populateObjectNode(pos, ws, createIdentifier(identifier), false);
        objectNode.setName(this.createIdentifier(identifier));
        if (publicRecord) {
            objectNode.flagSet.add(Flag.PUBLIC);
        }

        this.compUnit.addTopLevelNode(objectNode);
    }

    void addAnonObjectType(DiagnosticPos pos, Set<Whitespace> ws) {
        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousObjectKey(pos.src.pkgID);
        IdentifierNode anonObjectGenName = createIdentifier(genName);

        // Create an anonymous object and add it to the list of objects in the current package.
        BLangObject objectNode = populateObjectNode(pos, ws, anonObjectGenName, true);
        objectNode.addFlag(Flag.PUBLIC);
        this.compUnit.addTopLevelNode(objectNode);

        addType(createUserDefinedType(pos, ws, (BLangIdentifier) TreeBuilder.createIdentifierNode(), objectNode.name));

    }

    void addObjectFieldsBlock (Set<Whitespace> ws) {
        BLangObject objectNode = (BLangObject) this.objectStack.peek();
        objectNode.addWS(ws);
    }

    private BLangObject populateObjectNode(DiagnosticPos pos, Set<Whitespace> ws,
                                           IdentifierNode name, boolean isAnonymous) {
        BLangObject objectNode = (BLangObject) this.objectStack.pop();
        objectNode.pos = pos;
        objectNode.addWS(ws);
        objectNode.name = (BLangIdentifier) name;
        objectNode.isAnonymous = isAnonymous;
        this.varListStack.pop().forEach(variableNode -> {
            ((BLangVariable) variableNode).docTag = DocTag.FIELD;
            objectNode.addField(variableNode);
        });
        return objectNode;
    }

    void endTypeDefinition(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean publicStruct) {
        //TODO only adding object type for now
        if (!this.objectStack.isEmpty()) {
            endObjectDef(pos, ws, identifier, publicStruct);
        } else if (!this.recordStack.isEmpty()) {
            endRecordDef(pos, ws, identifier, publicStruct);
        } else {
            BLangTypeDefinition typeDefinition = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
            typeDefinition.setName(this.createIdentifier(identifier));

            if (publicStruct) {
                typeDefinition.flagSet.add(Flag.PUBLIC);
            }

            BLangUnionTypeNode members = (BLangUnionTypeNode) TreeBuilder.createUnionTypeNode();
            while (!typeNodeStack.isEmpty()) {
                BLangType memberType = (BLangType) typeNodeStack.pop();
                if (memberType.getKind() == NodeKind.UNION_TYPE_NODE) {
                    members.memberTypeNodes.addAll(((BLangUnionTypeNode) memberType).memberTypeNodes);
                } else {
                    members.memberTypeNodes.add(memberType);
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

            while (!exprNodeStack.isEmpty()) {
                typeDefinition.valueSpace.add((BLangExpression) exprNodeStack.pop());
            }

            typeDefinition.pos = pos;
            typeDefinition.addWS(ws);
            attachDocumentations(typeDefinition);
            attachDeprecatedNode(typeDefinition);
            this.compUnit.addTopLevelNode(typeDefinition);
        }
    }

    void endObjectInitParamList(Set<Whitespace> ws, boolean paramsAvail, boolean restParamAvail) {
        InvokableNode invNode = this.invokableNodeStack.peek();
        invNode.addWS(ws);

        if (paramsAvail) {
            this.varListStack.pop().forEach(variableNode -> {
                ((BLangVariable) variableNode).docTag = DocTag.PARAM;
                invNode.addParameter(variableNode);
            });

            this.defaultableParamsList.forEach(variableDef -> {
                BLangVariableDef varDef = (BLangVariableDef) variableDef;
                varDef.var.docTag = DocTag.PARAM;
                invNode.addDefaultableParameter(varDef);
            });
            this.defaultableParamsList = new ArrayList<>();

            if (restParamAvail) {
                invNode.setRestParameter(this.restParamStack.pop());
            }
        }
    }

    void endObjectInitFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                  boolean publicFunc, boolean bodyExists) {
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

        if (!function.deprecatedAttachments.isEmpty()) {
            function.flagSet.add(Flag.DEPRECATED);
        }

        BLangValueType nillTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nillTypeNode.pos = pos;
        nillTypeNode.typeKind = TypeKind.NIL;
        function.returnTypeNode = nillTypeNode;

        ((BLangObject) this.objectStack.peek()).initFunction = function;
    }

    void endObjectAttachedFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, boolean publicFunc,
                                      boolean nativeFunc, boolean bodyExists) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
        endEndpointDeclarationScope();
        function.pos = pos;
        function.addWS(ws);
        function.addWS(this.invocationWsStack.pop());

        function.flagSet.add(Flag.ATTACHED);

        if (publicFunc) {
            function.flagSet.add(Flag.PUBLIC);
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

        if (!function.deprecatedAttachments.isEmpty()) {
            function.flagSet.add(Flag.DEPRECATED);
        }

        this.objectStack.peek().addFunction(function);
    }

    void endObjectOuterFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, boolean publicFunc, boolean nativeFunc,
                                   boolean bodyExists, String objectName) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
        endEndpointDeclarationScope();
        function.pos = pos;
        function.addWS(ws);

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
        receiver.addWS(ws);

        receiver.docTag = DocTag.RECEIVER;
        receiver.setTypeNode(objectType);

        receiver.docTag = DocTag.RECEIVER;
        function.receiver = receiver;
        function.flagSet.add(Flag.ATTACHED);

        function.attachedOuterFunction = true;

        if (!function.deprecatedAttachments.isEmpty()) {
            function.flagSet.add(Flag.DEPRECATED);
        }

        this.compUnit.addTopLevelNode(function);
    }

    BLangVariable addObjectParameter(DiagnosticPos pos, Set<Whitespace> ws, boolean isField,
                                     String identifier, boolean exprAvailable, int annotCount) {
        BLangVariable var = (BLangVariable) this.generateObjectVarNode(pos, ws, isField, identifier, exprAvailable);
        attachAnnotations(var, annotCount);
        var.pos = pos;
        if (this.varListStack.empty()) {
            this.varStack.push(var);
        } else {
            this.varListStack.peek().add(var);
        }

        return var;
    }

    private VariableNode generateObjectVarNode(DiagnosticPos pos, Set<Whitespace> ws,
                                               boolean isField, String identifier, boolean exprAvailable) {
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        IdentifierNode name = this.createIdentifier(identifier);
        var.setName(name);
        var.addWS(ws);
        var.isField = isField;
        if (!isField) {
            var.setTypeNode(this.typeNodeStack.pop());
        }
        if (exprAvailable) {
            var.setInitialExpression(this.exprNodeStack.pop());
        }
        return var;
    }

    void addFieldToObject(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                          boolean exprAvailable, int annotCount, boolean isPrivate) {

        Set<Whitespace> wsForSemiColon = removeNthFromLast(ws, 0);
        BLangObject objectNode = (BLangObject) this.objectStack.peek();
        objectNode.addWS(wsForSemiColon);
        BLangVariable field = addVar(pos, ws, identifier, exprAvailable, annotCount);

        if (!isPrivate) {
            field.flagSet.add(Flag.PUBLIC);
        }
    }

    public void startEnumDef(DiagnosticPos pos) {
        BLangEnum bLangEnum = (BLangEnum) TreeBuilder.createEnumNode();
        bLangEnum.pos = pos;
        attachAnnotations(bLangEnum);
        attachDocumentations(bLangEnum);
        attachDeprecatedNode(bLangEnum);
        this.enumStack.add(bLangEnum);
    }

    public void endEnumDef(String identifier, boolean publicEnum) {
        BLangEnum enumNode = (BLangEnum) this.enumStack.pop();
        enumNode.name = (BLangIdentifier) this.createIdentifier(identifier);
        if (publicEnum) {
            enumNode.flagSet.add(Flag.PUBLIC);
        }

        enumeratorList.forEach(enumNode::addEnumerator);
        this.compUnit.addTopLevelNode(enumNode);
        enumeratorList = new ArrayList<>();
    }

    public void addEnumerator(DiagnosticPos pos, Set<Whitespace> ws, String name) {
        BLangEnumerator enumerator = (BLangEnumerator) TreeBuilder.createEnumeratorNode();
        enumerator.pos = pos;
        enumerator.addWS(ws);
        enumerator.name = (BLangIdentifier) createIdentifier(name);
        enumeratorList.add(enumerator);
    }


    public void startConnectorDef() {
        ConnectorNode connectorNode = TreeBuilder.createConnectorNode();
        attachAnnotations(connectorNode);
        attachDocumentations(connectorNode);
        attachDeprecatedNode(connectorNode);
        this.connectorNodeStack.push(connectorNode);
        startEndpointDeclarationScope(((BLangConnector) connectorNode).endpoints);
    }

    public void startConnectorBody() {
        /* end of connector definition header, so let's populate
         * the connector information before processing the body */
        ConnectorNode connectorNode = this.connectorNodeStack.peek();
        if (!this.varListStack.empty()) {
            this.varListStack.pop().forEach(variableNode -> {
                ((BLangVariable) variableNode).docTag = DocTag.PARAM;
                connectorNode.addParameter(variableNode);
            });
        }
        /* add a temporary block node to contain connector variable definitions */
        this.blockNodeStack.add(TreeBuilder.createBlockNode());
        /* action node list to contain the actions of the connector */
        this.actionNodeStack.add(new ArrayList<>());
    }

    public void endConnectorDef(DiagnosticPos pos, Set<Whitespace> ws, String identifier, boolean publicCon) {
        BLangConnector connectorNode = (BLangConnector) this.connectorNodeStack.pop();
        connectorNode.pos = pos;
        connectorNode.addWS(ws);
        connectorNode.setName(this.createIdentifier(identifier));
        if (publicCon) {
            connectorNode.flagSet.add(Flag.PUBLIC);
        }
        endEndpointDeclarationScope();
        this.compUnit.addTopLevelNode(connectorNode);
    }

    public void endConnectorBody(Set<Whitespace> ws) {
        ConnectorNode connectorNode = this.connectorNodeStack.peek();
        connectorNode.addWS(ws);
        this.blockNodeStack.pop().getStatements().forEach(
                e -> connectorNode.addVariableDef((VariableDefinitionNode) e));
        this.actionNodeStack.pop().forEach(connectorNode::addAction);
    }

    public void startActionDef() {
        ActionNode actionNode = TreeBuilder.createActionNode();
        this.invokableNodeStack.push(actionNode);
        startEndpointDeclarationScope(((BLangAction) actionNode).endpoints);
    }

    public void endActionDef(DiagnosticPos pos,
                             Set<Whitespace> ws, int annotCount,
                             boolean nativeAction, boolean bodyExists, boolean docExists, boolean isDeprecated) {
        BLangAction actionNode = (BLangAction) this.invokableNodeStack.pop();
        endEndpointDeclarationScope();
        actionNode.pos = pos;
        actionNode.addWS(ws);
        if (nativeAction) {
            actionNode.flagSet.add(Flag.NATIVE);
        }

        if (!bodyExists) {
            actionNode.body = null;
        }

        if (docExists) {
            attachDocumentations(actionNode);
        }

        if (isDeprecated) {
            attachDeprecatedNode(actionNode);
        }

        attachAnnotations(actionNode, annotCount);
        this.connectorNodeStack.peek().addAction(actionNode);
    }

    public void startAnnotationDef(DiagnosticPos pos) {
        BLangAnnotation annotNode = (BLangAnnotation) TreeBuilder.createAnnotationNode();
        annotNode.pos = pos;
        attachAnnotations(annotNode);
        attachDocumentations(annotNode);
        attachDeprecatedNode(annotNode);
        this.annotationStack.add(annotNode);
    }

    public void endAnnotationDef(Set<Whitespace> ws, String identifier, boolean publicAnnotation,
                                 boolean isTypeAttached) {
        BLangAnnotation annotationNode = (BLangAnnotation) this.annotationStack.pop();
        annotationNode.addWS(ws);
        annotationNode.setName(this.createIdentifier(identifier));

        if (publicAnnotation) {
            annotationNode.flagSet.add(Flag.PUBLIC);
        }
        while (!attachmentPointStack.empty()) {
            annotationNode.attachmentPoints.add(attachmentPointStack.pop());
        }
        if (isTypeAttached) {
            annotationNode.typeNode = (BLangType) this.typeNodeStack.pop();
        }

        this.compUnit.addTopLevelNode(annotationNode);
    }

    public void startDocumentationAttachment(DiagnosticPos currentPos) {
        BLangDocumentation docAttachmentNode =
                (BLangDocumentation) TreeBuilder.createDocumentationNode();
        docAttachmentNode.pos = currentPos;
        docAttachmentStack.push(docAttachmentNode);
    }

    public void endDocumentationAttachment(Set<Whitespace> ws) {
        DocumentationNode docAttachmentNode = docAttachmentStack.peek();
        docAttachmentNode.addWS(ws);
    }

    public void setDocumentationAttachmentContent(DiagnosticPos pos,
                                                  Set<Whitespace> ws,
                                                  String contentText) {
        DocumentationNode docAttachmentNode = docAttachmentStack.peek();
        docAttachmentNode.addWS(ws);

        docAttachmentNode.setDocumentationText(contentText);
    }

    public void createDocumentationAttribute(DiagnosticPos pos,
                                             Set<Whitespace> ws,
                                             String attributeName,
                                             String endText, String docPrefix) {
        BLangDocumentationAttribute attrib =
                (BLangDocumentationAttribute) TreeBuilder.createDocumentationAttributeNode();
        attrib.documentationField = (BLangIdentifier) createIdentifier(attributeName);

        attrib.documentationText = endText;
        attrib.docTag = DocTag.fromString(docPrefix);

        attrib.pos = pos;
        attrib.addWS(ws);
        docAttachmentStack.peek().addAttribute(attrib);
    }

    public void createDeprecatedNode(DiagnosticPos pos,
                                     Set<Whitespace> ws,
                                     String content) {
        BLangDeprecatedNode deprecatedNode = (BLangDeprecatedNode) TreeBuilder.createDeprecatedNode();

        deprecatedNode.pos = pos;
        deprecatedNode.addWS(ws);

        deprecatedNode.documentationText = content;
        deprecatedAttachmentStack.push(deprecatedNode);
    }

    public void startAnnotationAttachment(DiagnosticPos currentPos) {
        BLangAnnotationAttachment annotAttachmentNode =
                (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        annotAttachmentNode.pos = currentPos;
        annotAttachmentStack.push(annotAttachmentNode);
    }

    public void setAnnotationAttachmentName(Set<Whitespace> ws, boolean hasExpr, DiagnosticPos currentPos,
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
        annotAttachmentStack.forEach(annot -> annotatableNode.addAnnotationAttachment(annot));
        annotAttachmentStack.clear();
    }

    private void attachDocumentations(DocumentableNode documentableNode) {
        if (!docAttachmentStack.empty()) {
            documentableNode.addDocumentationAttachment(docAttachmentStack.pop());
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
        tempAnnotAttachments.forEach(annot -> annotatableNode.addAnnotationAttachment(annot));
    }

    public void addAssignmentStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean isVarDeclaration) {
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

    public void addTupleDestructuringStatement(DiagnosticPos pos, Set<Whitespace> ws,
                                               boolean isVarsExist, boolean varDeclaration) {
        BLangTupleDestructure stmt = (BLangTupleDestructure) TreeBuilder.createTupleDestructureStatementNode();
        stmt.pos = pos;
        stmt.addWS(ws);
        if (isVarsExist) {
            stmt.setDeclaredWithVar(varDeclaration);
            stmt.expr = (BLangExpression) exprNodeStack.pop();
            List<ExpressionNode> lExprList = exprNodeListStack.pop();
            lExprList.forEach(expressionNode -> stmt.varRefs.add((BLangVariableReference) expressionNode));
        }
        // TODO: handle ParamList Destructure.
        addStmtToCurrentBlock(stmt);
    }

    public void startForeachStatement() {
        startBlock();
    }

    public void addCompoundAssignmentStatement(DiagnosticPos pos, Set<Whitespace> ws, String operator) {
        BLangCompoundAssignment assignmentNode =
                (BLangCompoundAssignment) TreeBuilder.createCompoundAssignmentNode();
        assignmentNode.setExpression(exprNodeStack.pop());
        assignmentNode.setVariable((BLangVariableReference) exprNodeStack.pop());
        assignmentNode.pos = pos;
        assignmentNode.addWS(ws);
        assignmentNode.opKind = OperatorKind.valueFrom(operator);
        addStmtToCurrentBlock(assignmentNode);
    }

    public void addPostIncrementStatement(DiagnosticPos pos, Set<Whitespace> ws, String operator) {
        BLangPostIncrement postIncrement =
                (BLangPostIncrement) TreeBuilder.createPostIncrementNode();
        postIncrement.setVariable((BLangVariableReference) exprNodeStack.pop());
        postIncrement.pos = pos;
        postIncrement.addWS(ws);
        addLiteralValue(pos, ws, TypeTags.INT, Long.parseLong("1"));
        postIncrement.increment = (BLangExpression) exprNodeStack.pop();
        postIncrement.opKind = OperatorKind.valueFrom(operator);
        addStmtToCurrentBlock(postIncrement);
    }

    public void addForeachStatement(DiagnosticPos pos, Set<Whitespace> ws) {
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

    public void startWhileStmt() {
        startBlock();
    }

    public void addWhileStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangWhile whileNode = (BLangWhile) TreeBuilder.createWhileNode();
        whileNode.setCondition(exprNodeStack.pop());
        whileNode.pos = pos;
        whileNode.addWS(ws);
        BLangBlockStmt whileBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        whileBlock.pos = pos;
        whileNode.setBody(whileBlock);
        addStmtToCurrentBlock(whileNode);
    }

    public void startLockStmt() {
        startBlock();
    }

    public void addLockStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangLock lockNode = (BLangLock) TreeBuilder.createLockNode();
        lockNode.pos = pos;
        lockNode.addWS(ws);
        BLangBlockStmt lockBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        lockBlock.pos = pos;
        lockNode.setBody(lockBlock);
        addStmtToCurrentBlock(lockNode);
    }

    public void addNextStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangNext nextNode = (BLangNext) TreeBuilder.createNextNode();
        nextNode.pos = pos;
        nextNode.addWS(ws);
        addStmtToCurrentBlock(nextNode);
    }

    public void addBreakStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangBreak breakNode = (BLangBreak) TreeBuilder.createBreakNode();
        breakNode.pos = pos;
        breakNode.addWS(ws);
        addStmtToCurrentBlock(breakNode);
    }

    public void addReturnStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean exprAvailable) {
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

    public void startTransactionStmt() {
        transactionNodeStack.push(TreeBuilder.createTransactionNode());
        startBlock();
    }

    public void addTransactionBlock(DiagnosticPos pos) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt transactionBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        transactionBlock.pos = pos;
        transactionNode.setTransactionBody(transactionBlock);
    }

    public void endTransactionBlock(Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        transactionNode.getTransactionBody().addWS(ws);
    }

    public void startOnretryBlock() {
        startBlock();
    }

    public void addOnretryBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        TransactionNode transactionNode = transactionNodeStack.peek();
        BLangBlockStmt onretryBlock = (BLangBlockStmt) this.blockNodeStack.pop();
        onretryBlock.pos = pos;
        transactionNode.addWS(ws);
        transactionNode.setOnRetryBody(onretryBlock);
    }

    public void endTransactionStmt(DiagnosticPos pos, Set<Whitespace> ws, boolean distributedTransactionEnabled) {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.pop();
        transaction.pos = pos;
        transaction.addWS(ws);
        addStmtToCurrentBlock(transaction);

        if (distributedTransactionEnabled) {
            // TODO This is a temporary workaround to flag coordinator service start
            String value = compilerOptions.get(CompilerOptionName.TRANSACTION_EXISTS);
            if (value != null) {
                return;
            }

            compilerOptions.put(CompilerOptionName.TRANSACTION_EXISTS, "true");
            List<String> nameComps = getPackageNameComps(Names.TRANSACTION_PACKAGE.value);
            addImportPackageDeclaration(pos, null, Names.TRANSACTION_ORG.value,
                    nameComps, Names.DEFAULT_VERSION.value,
                    Names.DOT.value + nameComps.get(nameComps.size() - 1));
        }
    }

    public void addAbortStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangAbort abortNode = (BLangAbort) TreeBuilder.createAbortNode();
        abortNode.pos = pos;
        abortNode.addWS(ws);
        addStmtToCurrentBlock(abortNode);
    }

    public void addDoneStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangDone doneNode = (BLangDone) TreeBuilder.createDoneNode();
        doneNode.pos = pos;
        doneNode.addWS(ws);
        addStmtToCurrentBlock(doneNode);
    }

    public void addRetryStatement(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangRetry retryNode = (BLangRetry) TreeBuilder.createRetryNode();
        retryNode.pos = pos;
        retryNode.addWS(ws);
        addStmtToCurrentBlock(retryNode);
    }

    public void addRetryCountExpression() {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.peek();
        transaction.retryCount = (BLangExpression) exprNodeStack.pop();
    }

    public void addCommittedBlock() {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.peek();
        transaction.onCommitFunction = (BLangExpression) exprNodeStack.pop();
    }

    public void addAbortedBlock() {
        BLangTransaction transaction = (BLangTransaction) transactionNodeStack.peek();
        transaction.onAbortFunction = (BLangExpression) exprNodeStack.pop();
    }

    public void startIfElseNode(DiagnosticPos pos) {
        BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = pos;
        ifElseStatementStack.push(ifNode);
        startBlock();
    }

    public void addIfBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        IfNode ifNode = ifElseStatementStack.peek();
        ((BLangIf) ifNode).pos = pos;
        ifNode.addWS(ws);
        ifNode.setCondition(exprNodeStack.pop());
        ifNode.setBody(blockNodeStack.pop());
    }

    public void addElseIfBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        IfNode elseIfNode = ifElseStatementStack.pop();
        ((BLangIf) elseIfNode).pos = pos;
        elseIfNode.setCondition(exprNodeStack.pop());
        elseIfNode.setBody(blockNodeStack.pop());
        Set<Whitespace> elseWS = removeNthFromStart(ws, 0);
        elseIfNode.addWS(ws);

        IfNode parentIfNode = ifElseStatementStack.peek();
        while (parentIfNode.getElseStatement() != null) {
            parentIfNode = (IfNode) parentIfNode.getElseStatement();
        }
        parentIfNode.addWS(elseWS);
        parentIfNode.setElseStatement(elseIfNode);
    }

    public void addElseBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        IfNode ifNode = ifElseStatementStack.peek();
        while (ifNode.getElseStatement() != null) {
            ifNode = (IfNode) ifNode.getElseStatement();
        }
        ifNode.addWS(ws);
        BlockNode elseBlock = blockNodeStack.pop();
        ((BLangBlockStmt) elseBlock).pos = pos;
        ifNode.setElseStatement(elseBlock);
    }

    public void endIfElseNode(Set<Whitespace> ws) {
        IfNode ifNode = ifElseStatementStack.pop();
        ifNode.addWS(ws);
        addStmtToCurrentBlock(ifNode);
    }

    public void createMatchNode(DiagnosticPos pos) {
        if (this.matchStmtStack == null) {
            this.matchStmtStack = new ArrayDeque<>();
        }

        BLangMatch matchStmt = (BLangMatch) TreeBuilder.createMatchStatement();
        matchStmt.pos = pos;
        matchStmt.patternClauses = new ArrayList<>();

        this.matchStmtStack.addFirst(matchStmt);
    }

    public void completeMatchNode(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangMatch matchStmt = this.matchStmtStack.removeFirst();
        matchStmt.pos = pos;
        matchStmt.addWS(ws);
        matchStmt.expr = (BLangExpression) this.exprNodeStack.pop();
        addStmtToCurrentBlock(matchStmt);
    }

    public void startMatchStmtPattern() {
        startBlock();
    }

    public void addMatchStmtPattern(DiagnosticPos pos, Set<Whitespace> ws, String identifier) {
        BLangMatchStmtPatternClause patternClause =
                (BLangMatchStmtPatternClause) TreeBuilder.createMatchStatementPattern();
        patternClause.pos = pos;

        Set<Whitespace> varDefWS = removeNthFromStart(ws, 0);
        patternClause.addWS(ws);

        // Create a variable node
        identifier = identifier == null ? Names.IGNORE.value : identifier;
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        var.setName(this.createIdentifier(identifier));
        var.setTypeNode(this.typeNodeStack.pop());
        var.addWS(varDefWS);
        patternClause.variable = var;
        patternClause.body = (BLangBlockStmt) blockNodeStack.pop();
        patternClause.body.pos = pos;
        this.matchStmtStack.peekFirst().patternClauses.add(patternClause);
    }

    public void addWorkerSendStmt(DiagnosticPos pos, Set<Whitespace> ws, String workerName, boolean isForkJoinSend) {
        BLangWorkerSend workerSendNode = (BLangWorkerSend) TreeBuilder.createWorkerSendNode();
        workerSendNode.setWorkerName(this.createIdentifier(workerName));
        workerSendNode.expr = (BLangExpression) exprNodeStack.pop();
        workerSendNode.isForkJoinSend = isForkJoinSend;
        workerSendNode.pos = pos;
        workerSendNode.addWS(ws);
        addStmtToCurrentBlock(workerSendNode);
    }

    public void addWorkerReceiveStmt(DiagnosticPos pos, Set<Whitespace> ws, String workerName) {
        BLangWorkerReceive workerReceiveNode = (BLangWorkerReceive) TreeBuilder.createWorkerReceiveNode();
        workerReceiveNode.setWorkerName(this.createIdentifier(workerName));
        workerReceiveNode.expr = (BLangExpression) exprNodeStack.pop();
        workerReceiveNode.pos = pos;
        workerReceiveNode.addWS(ws);
        addStmtToCurrentBlock(workerReceiveNode);
    }

    public void addExpressionStmt(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.pos = pos;
        exprStmt.addWS(ws);
        exprStmt.expr = (BLangExpression) exprNodeStack.pop();
        addStmtToCurrentBlock(exprStmt);
    }

    public void startServiceDef(DiagnosticPos pos) {
        BLangService serviceNode = (BLangService) TreeBuilder.createServiceNode();
        serviceNode.pos = pos;
        attachAnnotations(serviceNode);
        attachDocumentations(serviceNode);
        attachDeprecatedNode(serviceNode);
        serviceNodeStack.push(serviceNode);
        startEndpointDeclarationScope(serviceNode.endpoints);
    }

    public void addServiceBody(Set<Whitespace> ws) {
        ServiceNode serviceNode = serviceNodeStack.peek();
        serviceNode.addWS(ws);
        blockNodeStack.pop().getStatements()
                .forEach(varDef -> serviceNode.addVariable((VariableDefinitionNode) varDef));
    }

    public void addAnonymousEndpointBind() {
        BLangService serviceNode = (BLangService) serviceNodeStack.peek();
        serviceNode.addAnonymousEndpointBind((RecordLiteralNode) exprNodeStack.pop());
    }

    public void addServiceEndpointAttachments(int size, Set<Whitespace> ws) {
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

    public void endServiceDef(DiagnosticPos pos, Set<Whitespace> ws, String serviceName, boolean constrained) {
        BLangService serviceNode = (BLangService) serviceNodeStack.pop();
        serviceNode.setName(createIdentifier(serviceName));
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

    public void startResourceDef() {
        ResourceNode resourceNode = TreeBuilder.createResourceNode();
        invokableNodeStack.push(resourceNode);
        startEndpointDeclarationScope(((BLangResource) resourceNode).endpoints);
    }

    public void endResourceDef(DiagnosticPos pos, Set<Whitespace> ws, String resourceName,
                               boolean docExists, boolean isDeprecated, boolean hasParameters) {
        BLangResource resourceNode = (BLangResource) invokableNodeStack.pop();
        endEndpointDeclarationScope();
        resourceNode.pos = pos;
        resourceNode.addWS(ws);
        resourceNode.setName(createIdentifier(resourceName));
        if (docExists) {
            attachDocumentations(resourceNode);
        }
        if (isDeprecated) {
            attachDeprecatedNode(resourceNode);
        }
        if (hasParameters) {
            BLangVariable firstParam = (BLangVariable) varListStack.peek().get(0);
            if (firstParam.name.value.startsWith("$")) {
                // This is an endpoint variable
                Set<Whitespace> wsBeforeComma = removeNthFromLast(firstParam.getWS(), 0);
                resourceNode.addWS(wsBeforeComma);
            }
            varListStack.pop().forEach(variableNode -> {
                ((BLangVariable) variableNode).docTag = DocTag.PARAM;
                resourceNode.addParameter(variableNode);
            });
        }

        // Set the return type node
        BLangValueType nillTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nillTypeNode.pos = pos;
        nillTypeNode.typeKind = TypeKind.NIL;
        resourceNode.returnTypeNode = nillTypeNode;

        serviceNodeStack.peek().addResource(resourceNode);
    }

    public void addResourceAnnotation(int annotCount) {
        BLangResource resourceNode = (BLangResource) invokableNodeStack.peek();
        attachAnnotations(resourceNode, annotCount);
    }

    public void addEndpointVariable(DiagnosticPos pos, Set<Whitespace> ws, String endpointName) {
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

    public void createXMLQName(DiagnosticPos pos, Set<Whitespace> ws, String localname, String prefix) {
        BLangXMLQName qname = (BLangXMLQName) TreeBuilder.createXMLQNameNode();
        qname.localname = (BLangIdentifier) createIdentifier(localname);
        qname.prefix = (BLangIdentifier) createIdentifier(prefix);
        qname.pos = pos;
        qname.addWS(ws);
        addExpressionNode(qname);
    }

    public void createXMLAttribute(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangXMLAttribute xmlAttribute = (BLangXMLAttribute) TreeBuilder.createXMLAttributeNode();
        xmlAttribute.value = (BLangXMLQuotedString) exprNodeStack.pop();
        xmlAttribute.name = (BLangExpression) exprNodeStack.pop();
        xmlAttribute.pos = pos;
        xmlAttribute.addWS(ws);
        xmlAttributeNodeStack.push(xmlAttribute);
    }

    public void attachXmlLiteralWS(Set<Whitespace> ws) {
        this.exprNodeStack.peek().addWS(ws);
    }

    public void startXMLElement(DiagnosticPos pos, Set<Whitespace> ws, boolean isRoot) {
        BLangXMLElementLiteral xmlElement = (BLangXMLElementLiteral) TreeBuilder.createXMLElementLiteralNode();
        BLangExpression startTag = (BLangExpression) exprNodeStack.pop();
        xmlElement.addWS(ws);
        xmlElement.startTagName = startTag;
        xmlElement.pos = pos;
        xmlElement.isRoot = isRoot;
        xmlAttributeNodeStack.forEach(attribute -> xmlElement.addAttribute(attribute));
        xmlAttributeNodeStack.clear();
        addExpressionNode(xmlElement);
    }

    public void endXMLElement(Set<Whitespace> ws) {
        BLangExpression endTag = (BLangExpression) exprNodeStack.pop();
        BLangXMLElementLiteral xmlElement = (BLangXMLElementLiteral) exprNodeStack.peek();
        xmlElement.addWS(ws);
        xmlElement.endTagName = endTag;
    }

    public void createXMLQuotedLiteral(DiagnosticPos pos,
                                       Set<Whitespace> ws,
                                       Stack<String> precedingTextFragments,
                                       String endingText,
                                       QuoteType quoteType) {
        List<BLangExpression> templateExprs =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText, NodeKind.LITERAL);
        BLangXMLQuotedString quotedString = (BLangXMLQuotedString) TreeBuilder.createXMLQuotedStringNode();
        quotedString.pos = pos;
        quotedString.quoteType = quoteType;
        quotedString.textFragments = templateExprs;
        addExpressionNode(quotedString);
    }

    public void addChildToXMLElement(Set<Whitespace> ws) {
        XMLLiteralNode child = (XMLLiteralNode) exprNodeStack.pop();
        child.addWS(ws);
        BLangXMLElementLiteral parentXMLExpr = (BLangXMLElementLiteral) exprNodeStack.peek();
        parentXMLExpr.addChild(child);
    }

    public void createXMLTextLiteral(DiagnosticPos pos,
                                     Set<Whitespace> ws,
                                     Stack<String> precedingTextFragments,
                                     String endingText) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.textFragments =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText, NodeKind.XML_TEXT_LITERAL);
        xmlTextLiteral.pos = pos;
        addExpressionNode(xmlTextLiteral);
    }

    public void addXMLTextToElement(DiagnosticPos pos,
                                    Set<Whitespace> ws,
                                    Stack<String> precedingTextFragments,
                                    String endingText) {

        List<BLangExpression> templateExprs =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText, NodeKind.XML_TEXT_LITERAL);
        BLangXMLElementLiteral parentElement = (BLangXMLElementLiteral) exprNodeStack.peek();
        templateExprs.forEach(expr -> parentElement.addChild(expr));
    }

    public void createXMLCommentLiteral(DiagnosticPos pos,
                                        Set<Whitespace> ws,
                                        Stack<String> precedingTextFragments,
                                        String endingText) {

        BLangXMLCommentLiteral xmlCommentLiteral = (BLangXMLCommentLiteral) TreeBuilder.createXMLCommentLiteralNode();
        xmlCommentLiteral.textFragments =
                getExpressionsInTemplate(pos, null, precedingTextFragments, endingText, NodeKind.LITERAL);
        xmlCommentLiteral.pos = pos;
        xmlCommentLiteral.addWS(ws);
        addExpressionNode(xmlCommentLiteral);
    }

    public void createXMLPILiteral(DiagnosticPos pos,
                                   Set<Whitespace> ws,
                                   String targetQName,
                                   Stack<String> precedingTextFragments,
                                   String endingText) {
        List<BLangExpression> dataExprs =
                getExpressionsInTemplate(pos, ws, precedingTextFragments, endingText, NodeKind.LITERAL);
        addLiteralValue(pos, ws, TypeTags.STRING, targetQName);

        BLangXMLProcInsLiteral xmlProcInsLiteral =
                (BLangXMLProcInsLiteral) TreeBuilder.createXMLProcessingIntsructionLiteralNode();
        xmlProcInsLiteral.pos = pos;
        xmlProcInsLiteral.dataFragments = dataExprs;
        xmlProcInsLiteral.target = (BLangLiteral) exprNodeStack.pop();
        ;
        addExpressionNode(xmlProcInsLiteral);
    }

    public void addXMLNSDeclaration(DiagnosticPos pos,
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

    public void createStringTemplateLiteral(DiagnosticPos pos, Set<Whitespace> ws, Stack<String> precedingTextFragments,
                                            String endingText) {
        BLangStringTemplateLiteral stringTemplateLiteral =
                (BLangStringTemplateLiteral) TreeBuilder.createStringTemplateLiteralNode();
        stringTemplateLiteral.exprs =
                getExpressionsInTemplate(pos, null, precedingTextFragments, endingText, NodeKind.LITERAL);
        stringTemplateLiteral.addWS(ws);
        stringTemplateLiteral.pos = pos;
        addExpressionNode(stringTemplateLiteral);
    }

    public void createXmlAttributesRefExpr(DiagnosticPos pos, Set<Whitespace> ws, boolean singleAttribute) {
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

    public void startTransformerDef() {
        TransformerNode transformerNode = TreeBuilder.createTransformerNode();
        attachAnnotations(transformerNode);
        attachDocumentations(transformerNode);
        attachDeprecatedNode(transformerNode);
        this.invokableNodeStack.push(transformerNode);
    }

    public void endTransformerDef(DiagnosticPos pos,
                                  Set<Whitespace> ws,
                                  boolean publicFunc,
                                  String name,
                                  boolean paramsAvailable) {

        BLangTransformer transformer = (BLangTransformer) this.invokableNodeStack.pop();
        transformer.pos = pos;
        transformer.addWS(ws);
        transformer.setName(this.createIdentifier(name));

        if (paramsAvailable) {
            this.varListStack.pop().forEach(variableNode -> {
                ((BLangVariable) variableNode).docTag = DocTag.PARAM;
                transformer.addParameter(variableNode);
            });
        }

        // get the source and the target params
        List<VariableNode> mappingParams = this.varListStack.pop();

        // set the first mapping-param as the source for transformer
        VariableNode source = mappingParams.get(0);
        ((BLangVariable) source).docTag = DocTag.RECEIVER;
        transformer.setSource(source);

        // TODO Fix with the single return model
        transformer.retParams = new ArrayList<>();
        BLangVariable target = (BLangVariable) mappingParams.get(1);
        target.docTag = DocTag.RECEIVER;
        transformer.retParams.add(target);
        transformer.returnTypeNode = target.typeNode;

        if (publicFunc) {
            transformer.flagSet.add(Flag.PUBLIC);
        }

        this.compUnit.addTopLevelNode(transformer);
    }

    public void addIntRangeExpression(DiagnosticPos pos,
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

    public void addNamedArgument(DiagnosticPos pos, Set<Whitespace> ws, String name) {
        BLangNamedArgsExpression namedArg = (BLangNamedArgsExpression) TreeBuilder.createNamedArgNode();
        namedArg.pos = pos;
        namedArg.addWS(ws);
        namedArg.name = (BLangIdentifier) this.createIdentifier(name);
        namedArg.expr = (BLangExpression) this.exprNodeStack.pop();
        addExpressionNode(namedArg);
    }

    public void addRestArgument(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangRestArgsExpression varArgs = (BLangRestArgsExpression) TreeBuilder.createVarArgsNode();
        varArgs.pos = pos;
        varArgs.addWS(ws);
        varArgs.expr = (BLangExpression) this.exprNodeStack.pop();
        addExpressionNode(varArgs);
    }

    public void addDefaultableParam(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangVariableDef defaultableParam = (BLangVariableDef) TreeBuilder.createVariableDefinitionNode();
        defaultableParam.pos = pos;
        defaultableParam.addWS(ws);
        List<VariableNode> params = this.varListStack.peek();
        BLangVariable var = (BLangVariable) params.remove(params.size() - 1);
        var.expr = (BLangExpression) this.exprNodeStack.pop();
        defaultableParam.var = var;
        this.defaultableParamsList.add(defaultableParam);
    }

    public void addRestParam(DiagnosticPos pos, Set<Whitespace> ws, String identifier, int annotCount) {
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
                                                           String endingText,
                                                           NodeKind targetStrExprKind) {
        List<BLangExpression> expressions = new ArrayList<>();

        endingText = endingText == null ? "" : StringEscapeUtils.unescapeJava(endingText);
        addLiteralValue(pos, ws, TypeTags.STRING, endingText);
        expressions.add((BLangExpression) exprNodeStack.pop());

        while (!precedingTextFragments.empty()) {
            expressions.add((BLangExpression) exprNodeStack.pop());
            String textFragment = precedingTextFragments.pop();
            textFragment = textFragment == null ? "" : StringEscapeUtils.unescapeJava(textFragment);
            addLiteralValue(pos, ws, TypeTags.STRING, textFragment);
            expressions.add((BLangExpression) exprNodeStack.pop());
        }

        Collections.reverse(expressions);
        return expressions;
    }

    public void endCompilationUnit(Set<Whitespace> ws) {
        compUnit.addWS(ws);
    }

    public void endCallableParamList(Set<Whitespace> ws) {
        this.invokableNodeStack.peek().addWS(ws);
    }

    public void endFuncTypeParamList(Set<Whitespace> ws) {
        this.commaWsStack.push(ws);
    }

    public void endConnectorParamList(Set<Whitespace> ws) {
        this.connectorNodeStack.peek().addWS(ws);
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

    private BLangStruct populateStructNode(DiagnosticPos pos,
                                           Set<Whitespace> ws,
                                           IdentifierNode name,
                                           boolean isAnonymous) {
        BLangStruct structNode = (BLangStruct) this.structStack.pop();
        structNode.pos = pos;
        structNode.addWS(ws);
        structNode.name = (BLangIdentifier) name;
        structNode.isAnonymous = isAnonymous;
        this.varListStack.pop().forEach(variableNode -> {
            ((BLangVariable) variableNode).docTag = DocTag.FIELD;
            structNode.addField(variableNode);
        });
        return structNode;
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

    public void startOrderByClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        OrderByNode orderByNode = TreeBuilder.createOrderByNode();
        ((BLangOrderBy) orderByNode).pos = pos;
        orderByNode.addWS(ws);
        this.orderByClauseStack.push(orderByNode);
    }

    public void endOrderByClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        OrderByNode orderByNode = this.orderByClauseStack.peek();
        ((BLangOrderBy) orderByNode).pos = pos;
        orderByNode.addWS(ws);
        Collections.reverse(orderByVariableStack);
        while (!this.orderByVariableStack.empty()) {
            orderByNode.addOrderByVariable(this.orderByVariableStack.pop());
        }
    }

    public void startOrderByVariableNode(DiagnosticPos pos, Set<Whitespace> ws) {
        OrderByVariableNode orderByVariableNode = TreeBuilder.createOrderByVariableNode();
        ((BLangOrderByVariable) orderByVariableNode).pos = pos;
        orderByVariableNode.addWS(ws);
        this.orderByVariableStack.push(orderByVariableNode);
    }

    public void endOrderByVariableNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isAscending,
                                       boolean isDescending) {
        OrderByVariableNode orderByVariableNode = this.orderByVariableStack.peek();
        ((BLangOrderByVariable) orderByVariableNode).pos = pos;
        orderByVariableNode.addWS(ws);
        orderByVariableNode.setVariableReference(this.exprNodeStack.pop());
        orderByVariableNode.setOrderByType(isAscending, isDescending);
    }

    public void startLimitClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        LimitNode limitNode = TreeBuilder.createLimitNode();
        ((BLangLimit) limitNode).pos = pos;
        limitNode.addWS(ws);
        this.limitClauseStack.push(limitNode);
    }

    public void endLimitClauseNode(DiagnosticPos pos, Set<Whitespace> ws, String limitValue) {
        LimitNode limitNode = this.limitClauseStack.peek();
        ((BLangLimit) limitNode).pos = pos;
        limitNode.addWS(ws);
        limitNode.setLimitValue(limitValue);
    }

    public void startGroupByClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        GroupByNode groupByNode = TreeBuilder.createGroupByNode();
        ((BLangGroupBy) groupByNode).pos = pos;
        groupByNode.addWS(ws);
        this.groupByClauseStack.push(groupByNode);
    }

    public void endGroupByClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        GroupByNode groupByNode = this.groupByClauseStack.peek();
        ((BLangGroupBy) groupByNode).pos = pos;
        groupByNode.addWS(ws);
        this.exprNodeListStack.pop().forEach(groupByNode::addVariableReference);
    }

    public void startHavingClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        HavingNode havingNode = TreeBuilder.createHavingNode();
        ((BLangHaving) havingNode).pos = pos;
        havingNode.addWS(ws);
        this.havingClauseStack.push(havingNode);
    }

    public void endHavingClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        HavingNode havingNode = this.havingClauseStack.peek();
        ((BLangHaving) havingNode).pos = pos;
        havingNode.addWS(ws);
        havingNode.setExpression(this.exprNodeStack.pop());
    }

    public void startSelectExpressionNode(DiagnosticPos pos, Set<Whitespace> ws) {
        SelectExpressionNode selectExpr = TreeBuilder.createSelectExpressionNode();
        ((BLangSelectExpression) selectExpr).pos = pos;
        selectExpr.addWS(ws);
        this.selectExpressionsStack.push(selectExpr);
    }

    public void endSelectExpressionNode(String identifier, DiagnosticPos pos, Set<Whitespace> ws) {
        SelectExpressionNode selectExpression = this.selectExpressionsStack.peek();
        selectExpression.setExpression(exprNodeStack.pop());
        ((BLangSelectExpression) selectExpression).pos = pos;
        selectExpression.addWS(ws);
        selectExpression.setIdentifier(identifier);
    }

    public void startSelectExpressionList() {
        this.selectExpressionsListStack.push(new ArrayList<>());
    }

    public void endSelectExpressionList(Set<Whitespace> ws, int selectExprCount) {
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

    public void startWhereClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        WhereNode whereNode = TreeBuilder.createWhereNode();
        ((BLangWhere) whereNode).pos = pos;
        whereNode.addWS(ws);
        this.whereClauseStack.push(whereNode);
    }

    public void endWhereClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        WhereNode whereNode = this.whereClauseStack.peek();
        ((BLangWhere) whereNode).pos = pos;
        whereNode.addWS(ws);
        whereNode.setExpression(exprNodeStack.pop());
    }

    public void startSelectClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        SelectClauseNode selectClauseNode = TreeBuilder.createSelectClauseNode();
        ((BLangSelectClause) selectClauseNode).pos = pos;
        selectClauseNode.addWS(ws);
        this.selectClausesStack.push(selectClauseNode);
    }

    public void endSelectClauseNode(boolean isSelectAll, boolean isGroupByAvailable, boolean isHavingAvailable,
                                    DiagnosticPos pos, Set<Whitespace> ws) {
        SelectClauseNode selectClauseNode = this.selectClausesStack.peek();
        ((BLangSelectClause) selectClauseNode).pos = pos;
        selectClauseNode.addWS(ws);
        if (!isSelectAll) {
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

    public void startWindowClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        WindowClauseNode windowClauseNode = TreeBuilder.createWindowClauseNode();
        ((BLangWindow) windowClauseNode).pos = pos;
        windowClauseNode.addWS(ws);
        this.windowClausesStack.push(windowClauseNode);
    }

    public void endWindowsClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        WindowClauseNode windowClauseNode = this.windowClausesStack.peek();
        ((BLangWindow) windowClauseNode).pos = pos;
        windowClauseNode.addWS(ws);
        windowClauseNode.setFunctionInvocation(this.exprNodeStack.pop());

        if (!this.whereClauseStack.empty()) {
            this.streamingInputStack.peek().setWindowTraversedAfterWhere(true);
        } else {
            this.streamingInputStack.peek().setWindowTraversedAfterWhere(false);
        }
    }

    public void startStreamingInputNode(DiagnosticPos pos, Set<Whitespace> ws) {
        StreamingInput streamingInput = TreeBuilder.createStreamingInputNode();
        ((BLangStreamingInput) streamingInput).pos = pos;
        streamingInput.addWS(ws);
        this.streamingInputStack.push(streamingInput);
    }

    public void endStreamingInputNode(String alias, DiagnosticPos pos,
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

        if (!this.windowClausesStack.empty()) {
            streamingInput.setWindowClause(this.windowClausesStack.pop());
        }
        streamingInput.setStreamReference(this.exprNodeStack.pop());
        streamingInput.setAlias(alias);
    }

    public void startJoinStreamingInputNode(DiagnosticPos pos, Set<Whitespace> ws) {
        JoinStreamingInput joinStreamingInput = TreeBuilder.createJoinStreamingInputNode();
        ((BLangJoinStreamingInput) joinStreamingInput).pos = pos;
        joinStreamingInput.addWS(ws);
        this.joinStreamingInputsStack.push(joinStreamingInput);
    }

    public void endJoinStreamingInputNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isUnidirectionalBeforeJoin,
                                          boolean isUnidirectionalAfterJoin, String joinType) {
        JoinStreamingInput joinStreamingInput = this.joinStreamingInputsStack.peek();
        ((BLangJoinStreamingInput) joinStreamingInput).pos = pos;
        joinStreamingInput.addWS(ws);
        joinStreamingInput.setStreamingInput(this.streamingInputStack.pop());
        joinStreamingInput.setOnExpression(this.exprNodeStack.pop());
        joinStreamingInput.setUnidirectionalBeforeJoin(isUnidirectionalBeforeJoin);
        joinStreamingInput.setUnidirectionalAfterJoin(isUnidirectionalAfterJoin);
        joinStreamingInput.setJoinType(joinType);
    }

    public void startTableQueryNode(DiagnosticPos pos, Set<Whitespace> ws) {
        TableQuery tableQuery = TreeBuilder.createTableQueryNode();
        ((BLangTableQuery) tableQuery).pos = pos;
        tableQuery.addWS(ws);
        this.tableQueriesStack.push(tableQuery);
    }

    public void endTableQueryNode(boolean isJoinClauseAvailable, boolean isSelectClauseAvailable,
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

    public void addTableQueryExpression(DiagnosticPos pos, Set<Whitespace> ws) {
        TableQueryExpression tableQueryExpression = TreeBuilder.createTableQueryExpression();
        ((BLangTableQueryExpression) tableQueryExpression).pos = pos;
        tableQueryExpression.addWS(ws);
        tableQueryExpression.setTableQuery(tableQueriesStack.pop());
        this.exprNodeStack.push(tableQueryExpression);
    }

    public void startSetAssignmentClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        SetAssignmentNode setAssignmentNode = TreeBuilder.createSetAssignmentNode();
        ((BLangSetAssignment) setAssignmentNode).pos = pos;
        setAssignmentNode.addWS(ws);
        this.setAssignmentStack.push(setAssignmentNode);
    }

    public void endSetAssignmentClauseNode(DiagnosticPos pos, Set<Whitespace> ws) {
        if (this.exprNodeStack.empty()) {
            throw new IllegalStateException("Expression stack cannot be empty in processing a Set Assignment Clause");
        }
        SetAssignmentNode setAssignmentNode = this.setAssignmentStack.peek();

        ((BLangSetAssignment) setAssignmentNode).pos = pos;
        setAssignmentNode.addWS(ws);

        setAssignmentNode.setExpression(exprNodeStack.pop());
        setAssignmentNode.setVariableReference(exprNodeStack.pop());
    }

    public void startSetClauseNode() {
        this.setAssignmentListStack.push(new ArrayList<>());
    }

    public void endSetClauseNode(Set<Whitespace> ws, int selectExprCount) {
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

    public void startStreamActionNode(DiagnosticPos pos, Set<Whitespace> ws, PackageID packageID) {
        StreamActionNode streamActionNode = TreeBuilder.createStreamActionNode();
        ((BLangStreamAction) streamActionNode).pos = pos;
        streamActionNode.addWS(ws);
        this.streamActionNodeStack.push(streamActionNode);
        this.startLambdaFunctionDef(packageID);
    }

    public void endStreamActionNode(DiagnosticPos pos, Set<Whitespace> ws) {
        StreamActionNode streamActionNode = this.streamActionNodeStack.peek();
        ((BLangStreamAction) streamActionNode).pos = pos;
        streamActionNode.addWS(ws);
        this.addLambdaFunctionDef(pos, ws, true, false, false);
        // we dont pop the exprNodeStack.It will be popped in a later stage after creating streamActionNode
        streamActionNode.setInvokableBody((BLangLambdaFunction) this.exprNodeStack.pop());
    }

    public void startPatternStreamingEdgeInputNode(DiagnosticPos pos, Set<Whitespace> ws) {
        PatternStreamingEdgeInputNode patternStreamingEdgeInputNode = TreeBuilder.createPatternStreamingEdgeInputNode();
        ((BLangPatternStreamingEdgeInput) patternStreamingEdgeInputNode).pos = pos;
        patternStreamingEdgeInputNode.addWS(ws);
        this.patternStreamingEdgeInputStack.push(patternStreamingEdgeInputNode);
    }

    public void endPatternStreamingEdgeInputNode(DiagnosticPos pos, Set<Whitespace> ws, String alias) {
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

    public void startPatternStreamingInputNode(DiagnosticPos pos, Set<Whitespace> ws) {
        PatternStreamingInputNode patternStreamingInputNode = TreeBuilder.createPatternStreamingInputNode();
        ((BLangPatternStreamingInput) patternStreamingInputNode).pos = pos;
        patternStreamingInputNode.addWS(ws);
        this.patternStreamingInputStack.push(patternStreamingInputNode);
    }

    public void endPatternStreamingInputNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isFollowedBy,
                                             boolean enclosedInParenthesis, boolean andWithNotAvailable,
                                             boolean forWithNotAvailable, boolean onlyAndAvailable,
                                             boolean onlyOrAvailable, boolean commaSeparated) {
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
                processNegationPatternWithTimeDuration(patternStreamingInputNode);
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

    private void processNegationPatternWithTimeDuration(PatternStreamingInputNode patternStreamingInputNode) {
        patternStreamingInputNode.setForWithNot(true);
        patternStreamingInputNode.addPatternStreamingEdgeInput(this.patternStreamingEdgeInputStack.pop());
        patternStreamingInputNode.setTimeExpr(this.exprNodeStack.pop());
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

    public void startStreamingQueryStatementNode(DiagnosticPos pos, Set<Whitespace> ws) {
        StreamingQueryStatementNode streamingQueryStatementNode = TreeBuilder.createStreamingQueryStatementNode();
        ((BLangStreamingQueryStatement) streamingQueryStatementNode).pos = pos;
        streamingQueryStatementNode.addWS(ws);
        this.streamingQueryStatementStack.push(streamingQueryStatementNode);
    }

    public void endStreamingQueryStatementNode(DiagnosticPos pos, Set<Whitespace> ws) {
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
        }

        if (!orderByClauseStack.empty()) {
            streamingQueryStatementNode.setOrderByClause(orderByClauseStack.pop());
        }

        if (!outputRateLimitStack.empty()) {
            streamingQueryStatementNode.setOutputRateLimitNode(outputRateLimitStack.pop());
        }

        streamingQueryStatementNode.setStreamingAction(streamActionNodeStack.pop());
    }

    public void startOutputRateLimitNode(DiagnosticPos pos, Set<Whitespace> ws) {
        OutputRateLimitNode outputRateLimit = TreeBuilder.createOutputRateLimitNode();
        ((BLangOutputRateLimit) outputRateLimit).pos = pos;
        outputRateLimit.addWS(ws);
        this.outputRateLimitStack.push(outputRateLimit);
    }

    public void endOutputRateLimitNode(DiagnosticPos pos, Set<Whitespace> ws, boolean isSnapshotOutputRateLimit,
                                       boolean isEventBasedOutputRateLimit, boolean isFirst, boolean isLast,
                                       boolean isAll, String timeScale, String rateLimitValue) {
        OutputRateLimitNode outputRateLimit = this.outputRateLimitStack.peek();
        ((BLangOutputRateLimit) outputRateLimit).pos = pos;
        outputRateLimit.addWS(ws);

        outputRateLimit.setSnapshot(isSnapshotOutputRateLimit);
        outputRateLimit.setEventBasedRateLimit(isEventBasedOutputRateLimit);
        outputRateLimit.setOutputRateType(isFirst, isLast, isAll);
        outputRateLimit.setTimeScale(timeScale);
        outputRateLimit.setRateLimitValue(rateLimitValue);
    }

    public void startWithinClause(DiagnosticPos pos, Set<Whitespace> ws) {
        WithinClause withinClause = TreeBuilder.createWithinClause();
        ((BLangWithinClause) withinClause).pos = pos;
        withinClause.addWS(ws);
        this.withinClauseStack.push(withinClause);
    }

    public void endWithinClause(DiagnosticPos pos, Set<Whitespace> ws) {
        WithinClause withinClause = this.withinClauseStack.peek();
        ((BLangWithinClause) withinClause).pos = pos;
        withinClause.addWS(ws);
        withinClause.setWithinTimePeriod(exprNodeStack.pop());
    }

    public void startPatternClause(DiagnosticPos pos, Set<Whitespace> ws) {
        PatternClause patternClause = TreeBuilder.createPatternClause();
        ((BLangPatternClause) patternClause).pos = pos;
        patternClause.addWS(ws);
        this.patternClauseStack.push(patternClause);
    }

    public void endPatternClause(boolean isForEvents, boolean isWithinClauseAvailable, DiagnosticPos pos,
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

    public void startForeverNode(DiagnosticPos pos, Set<Whitespace> ws) {
        ForeverNode foreverNode = TreeBuilder.createForeverNode();
        ((BLangForever) foreverNode).pos = pos;
        foreverNode.addWS(ws);
        this.foreverNodeStack.push(foreverNode);
    }

    public void endForeverNode(DiagnosticPos pos, Set<Whitespace> ws) {
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
    }

    public void startMatchExpression() {
        this.matchExprPatternNodeListStack.add(new ArrayList<>());
    }

    public void addMatchExprPattaern(DiagnosticPos pos, Set<Whitespace> ws, String identifier) {
        BLangMatchExprPatternClause pattern = (BLangMatchExprPatternClause) TreeBuilder.createMatchExpressionPattern();
        pattern.expr = (BLangExpression) this.exprNodeStack.pop();
        pattern.pos = pos;
        pattern.addWS(ws);

        identifier = identifier == null ? Names.IGNORE.value : identifier;
        BLangVariable var = (BLangVariable) TreeBuilder.createVariableNode();
        var.pos = pos;
        var.setName(this.createIdentifier(identifier));
        var.setTypeNode(this.typeNodeStack.pop());
        Set<Whitespace> varDefWS = removeNthFromStart(ws, 0);
        var.addWS(varDefWS);
        pattern.variable = var;

        this.matchExprPatternNodeListStack.peek().add(pattern);
    }

    public void endMatchExpression(DiagnosticPos pos, Set<Whitespace> ws) {
        BLangMatchExpression matchExpr = (BLangMatchExpression) TreeBuilder.createMatchExpression();
        this.matchExprPatternNodeListStack.pop()
                .forEach(pattern -> matchExpr.patternClauses.add((BLangMatchExprPatternClause) pattern));
        matchExpr.expr = (BLangExpression) this.exprNodeStack.pop();
        matchExpr.pos = pos;
        matchExpr.addWS(ws);
        addExpressionNode(matchExpr);
    }
}
