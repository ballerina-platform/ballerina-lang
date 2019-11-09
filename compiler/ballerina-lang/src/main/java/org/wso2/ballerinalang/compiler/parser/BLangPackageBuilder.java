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
import org.ballerinalang.model.elements.TableColumnFlag;
import org.ballerinalang.model.tree.AnnotatableNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.InvokableNode;
import org.ballerinalang.model.tree.MarkdownDocumentationNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.VariableNode;
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
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNameReference;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable.BLangRecordVariableKeyValue;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
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

    private Stack<List<BLangRecordVariableKeyValue>> recordVarListStack = new Stack<>();

    private Stack<List<BLangRecordVarRefKeyValue>> recordVarRefListStack = new Stack<>();

    private Stack<InvokableNode> invokableNodeStack = new Stack<>();

    private Stack<ExpressionNode> exprNodeStack = new Stack<>();

    private Stack<List<ExpressionNode>> exprNodeListStack = new Stack<>();

    private Stack<Set<Whitespace>> commaWsStack = new Stack<>();

    private Stack<Set<Whitespace>> invocationWsStack = new Stack<>();

    private Stack<BLangRecordLiteral> recordLiteralNodes = new Stack<>();

    private Stack<BLangTableLiteral> tableLiteralNodes = new Stack<>();

    private Stack<BLangWaitForAllExpr> waitCollectionStack = new Stack<>();

    private Stack<BLangTryCatchFinally> tryCatchFinallyNodesStack = new Stack<>();

    private Stack<AnnotationNode> annotationStack = new Stack<>();

    private Stack<MarkdownDocumentationNode> markdownDocumentationStack = new Stack<>();

    private Stack<AnnotationAttachmentNode> annotAttachmentStack = new Stack<>();

    private Stack<IfNode> ifElseStatementStack = new Stack<>();

    private Stack<TransactionNode> transactionNodeStack = new Stack<>();

    private Stack<ForkJoinNode> forkJoinNodesStack = new Stack<>();

    private Stack<ServiceNode> serviceNodeStack = new Stack<>();

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

    private Stack<SimpleVariableNode> restParamStack = new Stack<>();

    private Deque<BLangMatch> matchStmtStack;

    private PatternStreamingInputNode recentStreamingPatternInputNode;

    private Stack<Set<Whitespace>> operatorWs = new Stack<>();

    private Stack<Set<Whitespace>> objectFieldBlockWs = new Stack<>();

    private Stack<Set<Whitespace>> finiteTypeWsStack = new Stack<>();

    private Stack<Set<Whitespace>> workerDefinitionWSStack = new Stack<>();

    private Stack<Set<Whitespace>> bindingPatternIdentifierWS = new Stack<>();

    private Stack<Set<Whitespace>> errorMatchPatternWS = new Stack<>();
    private Stack<Set<Whitespace>> simpleMatchPatternWS = new Stack<>();
    private Stack<Set<Whitespace>> recordKeyWS = new Stack<>();

    private BLangAnonymousModelHelper anonymousModelHelper;
    private CompilerOptions compilerOptions;
    private SymbolTable symTable;
    private BLangDiagnosticLog dlog;

    private static final String IDENTIFIER_LITERAL_PREFIX = "'";

    public BLangPackageBuilder(CompilerContext context, CompilationUnitNode compUnit) {
        this.dlog = BLangDiagnosticLog.getInstance(context);
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

    void addRecordType(DiagnosticPos pos, Set<Whitespace> ws, boolean isFieldAnalyseRequired, boolean isAnonymous,
                       boolean hasRestField, boolean isExclusiveTypeDesc) {
        // If there is an explicitly defined rest field, take it.
        BLangType restFieldType = null;
        if (hasRestField) {
            restFieldType = (BLangType) this.typeNodeStack.pop();
        }
        // Create an anonymous record and add it to the list of records in the current package.
        BLangRecordTypeNode recordTypeNode = populateRecordTypeNode(pos, ws, isAnonymous);
        recordTypeNode.isFieldAnalyseRequired = isFieldAnalyseRequired;
        recordTypeNode.sealed = isExclusiveTypeDesc && !hasRestField;
        recordTypeNode.restFieldType = restFieldType;

        if (!isAnonymous) {
            addType(recordTypeNode);
            return;
        }
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        // Generate a name for the anonymous object
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName);
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
        this.varListStack.pop().forEach(variableNode -> {
            recordTypeNode.addField((SimpleVariableNode) variableNode);
        });
        return recordTypeNode;
    }

    void addFieldVariable(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                          boolean exprAvailable, int annotCount, boolean isPrivate, boolean isOptional) {
        BLangSimpleVariable field = addSimpleVar(pos, ws, identifier, identifierPos, exprAvailable, annotCount);

        if (!isPrivate) {
            field.flagSet.add(Flag.PUBLIC);
        }

        if (isOptional) {
            field.flagSet.add(Flag.OPTIONAL);
        } else if (!exprAvailable) {
            field.flagSet.add(Flag.REQUIRED);
        }
    }

    void addObjectFieldVariable(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                                boolean exprAvailable, int annotCount, boolean isPrivate, boolean isPublic) {
        BLangSimpleVariable field = addSimpleVar(pos, ws, identifier, identifierPos, exprAvailable, annotCount);

        attachAnnotations(field, annotCount, false);

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

    void addErrorType(DiagnosticPos pos, Set<Whitespace> ws, boolean reasonTypeExists, boolean detailsTypeExists,
                      boolean isAnonymous) {
        BLangErrorType errorType = (BLangErrorType) TreeBuilder.createErrorTypeNode();
        errorType.pos = pos;
        errorType.addWS(ws);
        if (detailsTypeExists) {
            errorType.detailType = (BLangType) this.typeNodeStack.pop();
        }
        if (reasonTypeExists) {
            errorType.reasonType = (BLangType) this.typeNodeStack.pop();
        }

        if (!isAnonymous) {
            addType(errorType);
            return;
        }
        BLangTypeDefinition typeDef = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        // Generate a name for the anonymous error
        String genName = anonymousModelHelper.getNextAnonymousTypeKey(pos.src.pkgID);
        IdentifierNode anonTypeGenName = createIdentifier(pos, genName);
        typeDef.setName(anonTypeGenName);
        typeDef.flagSet.add(Flag.PUBLIC);
        typeDef.flagSet.add(Flag.ANONYMOUS);

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
            functionTypeNode.returnTypeNode = this.varStack.pop().getTypeNode();
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
        IdentifierNode pkgNameNode = createIdentifier(currentPos, pkgName);
        IdentifierNode nameNode = createIdentifier(currentPos, name);
        nameReferenceStack.push(new BLangNameReference(currentPos, ws, pkgNameNode, nameNode));
    }

    void startVarList() {
        this.varListStack.push(new ArrayList<>());
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

    private IdentifierNode createIdentifier(DiagnosticPos pos, String value) {
        BLangIdentifier node = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        if (value == null) {
            return node;
        }

        if (value.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            if (!escapeQuotedIdentifier(value).matches("^[0-9a-zA-Z.]*$")) {
                dlog.error(pos, DiagnosticCode.IDENTIFIER_LITERAL_ONLY_SUPPORTS_ALPHANUMERICS);
            }
            value = StringEscapeUtils.unescapeJava(value);
            node.setValue(value.substring(1));
            node.setLiteral(true);
        } else {
            node.setValue(value);
            node.setLiteral(false);
        }
        node.pos = pos;
        return node;
    }

    BLangSimpleVariable addSimpleVar(DiagnosticPos pos,
                                     Set<Whitespace> ws,
                                     String identifier,
                                     DiagnosticPos identifierPos,
                                     boolean exprAvailable,
                                     int annotCount,
                                     boolean isPublic) {
        BLangSimpleVariable var  = addSimpleVar(pos, ws, identifier, identifierPos, exprAvailable, annotCount);
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
        IdentifierNode name = this.createIdentifier(identifierPos, identifier);
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

            errorVariable.reason = createIgnoreVar();
        }
        this.errorMatchPatternWS.push(ws);
        ((BLangErrorVariable) this.varStack.peek()).isInMatchStmt = true;
    }

    void addErrorVariable(DiagnosticPos pos, Set<Whitespace> ws, String reason, String restIdentifier,
                          boolean reasonVar, boolean constReasonMatchPattern, DiagnosticPos restParamPos) {
        BLangErrorVariable errorVariable = (BLangErrorVariable) varStack.peek();
        errorVariable.pos = pos;
        errorVariable.addWS(ws);

        if (constReasonMatchPattern) {
            BLangLiteral reasonLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
            reasonLiteral.setValue(reason.substring(1, reason.length() - 1));
            errorVariable.reasonMatchConst = reasonLiteral;
            errorVariable.reason = (BLangSimpleVariable)
                    generateBasicVarNodeWithoutType(pos, null, "$reason$", pos, false);
        } else {
            errorVariable.reason = (BLangSimpleVariable)
                    generateBasicVarNodeWithoutType(pos, null, reason, pos, false);
        }

        if (this.simpleMatchPatternWS.size() > 0) {
            errorVariable.reason.addWS(this.simpleMatchPatternWS.pop());
        }

        errorVariable.reasonVarPrefixAvailable = reasonVar;
        if (restIdentifier != null) {
            errorVariable.restDetail = (BLangSimpleVariable)
                    generateBasicVarNodeWithoutType(pos, null, restIdentifier, restParamPos, false);
        }
    }

    public void addErrorVariable(DiagnosticPos currentPos, Set<Whitespace> ws, String restIdName,
                                 DiagnosticPos restPos) {
        BLangErrorVariable errorVariable = (BLangErrorVariable) varStack.peek();
        errorVariable.pos = currentPos;
        errorVariable.addWS(ws);

        BLangType typeNode = (BLangType) this.typeNodeStack.pop();
        errorVariable.typeNode = typeNode;

        errorVariable.reason = (BLangSimpleVariable)
                generateBasicVarNodeWithoutType(currentPos, null, "$reason$", currentPos, false);

        if (restIdName != null) {
            errorVariable.restDetail = (BLangSimpleVariable)
                    generateBasicVarNodeWithoutType(currentPos, null, restIdName, restPos, false);
        }
    }

    void addErrorVariableReference(DiagnosticPos pos, Set<Whitespace> ws,
                                   int numNamedArgs,
                                   boolean reasonRefAvailable,
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

        if (reasonRefAvailable) {
            ExpressionNode reasonRef = this.exprNodeStack.pop();
            errorVarRef.reason = (BLangVariableReference) reasonRef;
        } else if (indirectErrorRefPattern) {
            // Indirect error ref pattern does not allow reason var ref, hence ignore it.
            errorVarRef.reason = createIgnoreVarRef();
            errorVarRef.typeNode = (BLangType) this.typeNodeStack.pop();
        } else {
            errorVarRef.reason = createIgnoreVarRef();
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
        BLangIdentifier bLangIdentifier = (BLangIdentifier) this.createIdentifier(pos, name);
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
                errorVariable.reason = createIgnoreVar();
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
        simpleVariableNode.name = (BLangIdentifier) this.createIdentifier(pos, bindingVarName);
        simpleVariableNode.name.pos = pos;
        simpleVariableNode.pos = pos;
        if (this.bindingPatternIdentifierWS.size() > 0) {
            simpleVariableNode.addWS(this.bindingPatternIdentifierWS.pop());
        }
        return new BLangErrorVariable.BLangErrorDetailEntry(bLangIdentifier, simpleVariableNode);
    }

    void addTupleVariable(DiagnosticPos pos, Set<Whitespace> ws, int members, boolean restBindingAvailable) {

        BLangTupleVariable tupleVariable = (BLangTupleVariable) TreeBuilder.createTupleVariableNode();
        tupleVariable.pos = pos;
        tupleVariable.addWS(ws);
        if (this.bindingPatternIdentifierWS.size() > 0) {
            tupleVariable.addWS(this.bindingPatternIdentifierWS.pop());
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
        recordKeyValue.key = (BLangIdentifier) this.createIdentifier(identifierPos, identifier);
        if (!bindingPattern) {
            addBindingPatternMemberVariable(pos, ws, identifier, identifierPos);
        }
        recordKeyValue.valueBindingPattern = this.varStack.pop();
        recordKeyValue.valueBindingPattern.addWS(ws);
        if (this.bindingPatternIdentifierWS.size() > 0) {
            recordKeyValue.valueBindingPattern.addWS(this.bindingPatternIdentifierWS.pop());
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
        keyValue.variableName = (BLangIdentifier) createIdentifier(pos, identifier);
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

    void endCallableUnitSignature(DiagnosticPos pos,
                                  Set<Whitespace> ws,
                                  String identifier,
                                  DiagnosticPos identifierPos,
                                  boolean paramsAvail,
                                  boolean retParamsAvail,
                                  boolean restParamAvail) {
        InvokableNode invNode = this.invokableNodeStack.peek();
        BLangIdentifier identifierNode = (BLangIdentifier) this.createIdentifier(identifierPos, identifier);
        identifierNode.pos = identifierPos;
        invNode.setName(identifierNode);
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
            this.varListStack.pop().forEach(variableNode -> {
                invNode.addParameter((SimpleVariableNode) variableNode);
            });

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
                anonymousModelHelper.getNextAnonymousFunctionKey(pkgID)));
        lambdaFunction.addFlag(Flag.LAMBDA);
        lambdaFunction.addFlag(Flag.ANONYMOUS);
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
        endFunctionDef(pos, null, false, false, false, false, true, true);
    }

    void addArrowFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, PackageID pkgID) {
        BLangArrowFunction arrowFunctionNode = (BLangArrowFunction) TreeBuilder.createArrowFunctionNode();
        arrowFunctionNode.pos = pos;
        arrowFunctionNode.addWS(ws);
        arrowFunctionNode.functionName = createIdentifier(pos, anonymousModelHelper.getNextAnonymousFunctionKey(pkgID));
        varListStack.pop().forEach(var -> arrowFunctionNode.params.add((BLangSimpleVariable) var));
        arrowFunctionNode.expression = (BLangExpression) this.exprNodeStack.pop();
        addExpressionNode(arrowFunctionNode);
    }

    void markLastInvocationAsAsync(DiagnosticPos pos, int numAnnotations) {
        final ExpressionNode expressionNode = this.exprNodeStack.peek();
        if (expressionNode.getKind() == NodeKind.INVOCATION) {
            BLangInvocation invocation = (BLangInvocation) this.exprNodeStack.peek();
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
        if (this.bindingPatternIdentifierWS.size() > 0) {
            varDefNode.addWS(this.bindingPatternIdentifierWS.pop());
        }
        addStmtToCurrentBlock(varDefNode);
    }

    void addBindingPatternNameWhitespace(Set<Whitespace> ws) {
        this.bindingPatternIdentifierWS.push(ws);
    }

    private BLangSimpleVariableDef createSimpleVariableDef(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                                           DiagnosticPos identifierPos, boolean isFinal,
                                                           boolean isExpressionAvailable, boolean isDeclaredWithVar) {
        BLangSimpleVariable var = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        BLangSimpleVariableDef varDefNode = (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        var.pos = pos;
        var.addWS(ws);
        var.setName(this.createIdentifier(identifierPos, identifier));
        var.name.pos = identifierPos;

        if (isFinal) {
            markVariableAsFinal(var);
        }
        if (isDeclaredWithVar) {
            var.isDeclaredWithVar = true;
        } else {
            var.setTypeNode(this.typeNodeStack.pop());
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

    void addErrorVariableDefStatement(DiagnosticPos pos, Set<Whitespace> ws, boolean isFinal,
                                      boolean isDeclaredWithVar) {
        BLangErrorVariableDef varDefNode = createErrorVariableDef(pos, ws, isFinal, true, isDeclaredWithVar);
        addStmtToCurrentBlock(varDefNode);
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
            initNode.userDefinedType = (BLangUserDefinedType) typeNodeStack.pop();
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
        IdentifierNode nameNode = createIdentifier(pos, initName);
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
                ((BLangTupleVariable) variable).memberVariables.forEach(this::markVariableAsFinal);
                break;
            case RECORD_VARIABLE:
                // If the variable is a record variable, we need to set the final flag to the all the variables in
                // the record.
                ((BLangRecordVariable) variable).variableList.stream()
                        .map(BLangRecordVariableKeyValue::getValue)
                        .forEach(this::markVariableAsFinal);
                break;
        }
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
        BLangSimpleVariable variableNode = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        variableNode.typeNode = (BLangType) this.typeNodeStack.pop();
        variableNode.name = (BLangIdentifier) createIdentifier(variableNode.typeNode.pos, paramName);
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

    void addKeyValueRecord(Set<Whitespace> ws, boolean computedKey) {
        BLangRecordKeyValue keyValue = (BLangRecordKeyValue) TreeBuilder.createRecordKeyValue();
        keyValue.addWS(ws);
        keyValue.valueExpr = (BLangExpression) exprNodeStack.pop();
        keyValue.key = new BLangRecordKey((BLangExpression) exprNodeStack.pop());
        if (!this.recordKeyWS.isEmpty()) {
            keyValue.addWS(this.recordKeyWS.pop());
        }
        keyValue.key.computedKey = computedKey;
        recordLiteralNodes.peek().keyValuePairs.add(keyValue);
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
        recordLiteral.pos = pos;
        if (commaWsStack.size() > 0) {
            recordLiteral.addWS(commaWsStack.pop());
        }
        this.tableLiteralNodes.peek().tableDataRows.add(recordLiteral);
    }

    void endTableDataArray(Set<Whitespace> ws) {
        BLangTableLiteral tableLiteral = this.tableLiteralNodes.peek();
        tableLiteral.addWS(ws);
    }

    void endTableDataRow(Set<Whitespace> ws) {
        List<ExpressionNode> argExprList = exprNodeListStack.pop();
        BLangTableLiteral tableLiteral = this.tableLiteralNodes.peek();
        tableLiteral.addWS(ws);
        if (commaWsStack.size() > 0) {
            tableLiteral.addWS(commaWsStack.pop());
        }
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
        for (int i = 0; i < n; i++) {
            if (exprNodeStack.empty()) {
                throw new IllegalStateException("Expression stack cannot be empty in processing an ExpressionList");
            }
            exprList.add(exprNodeStack.pop());
        }
        Collections.reverse(exprList);
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
                              DiagnosticPos identifierPos) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        invocationNode.addWS(ws);
        invocationNode.addWS(invocationWsStack.pop());
        if (argsAvailable) {
            List<ExpressionNode> exprNodes = exprNodeListStack.pop();
            exprNodes.forEach(exprNode -> invocationNode.argExprs.add((BLangExpression) exprNode));
            invocationNode.addWS(commaWsStack.pop());
        }

        invocationNode.expr = (BLangExpression) exprNodeStack.pop();
        invocationNode.name = (BLangIdentifier) createIdentifier(identifierPos, invocation);
        invocationNode.pkgAlias = (BLangIdentifier) createIdentifier(pos, null);
        addExpressionNode(invocationNode);
    }

    void createWorkerLambdaInvocationNode(DiagnosticPos pos, Set<Whitespace> ws, String invocation) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.pos = pos;
        invocationNode.addWS(ws);
        invocationNode.addWS(invocationWsStack.pop());

        invocationNode.name = (BLangIdentifier) createIdentifier(pos, invocation);
        invocationNode.pkgAlias = (BLangIdentifier) createIdentifier(pos, null);
        addExpressionNode(invocationNode);
    }

    void createActionInvocationNode(DiagnosticPos pos, Set<Whitespace> ws, boolean async, int numAnnotations) {
        BLangInvocation invocationExpr = (BLangInvocation) exprNodeStack.pop();
        invocationExpr.actionInvocation = true;
        invocationExpr.pos = pos;
        invocationExpr.addWS(ws);
        invocationExpr.async = async;

        invocationExpr.expr = (BLangExpression) exprNodeStack.pop();
        attachAnnotations(invocationExpr, numAnnotations, false);
        exprNodeStack.push(invocationExpr);
    }

    void createFieldBasedAccessNode(DiagnosticPos pos, Set<Whitespace> ws, String fieldName, DiagnosticPos fieldNamePos,
                                    FieldKind fieldType, boolean optionalFieldAccess) {
        BLangFieldBasedAccess fieldBasedAccess = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        fieldBasedAccess.pos = pos;
        fieldBasedAccess.addWS(ws);
        fieldBasedAccess.field = (BLangIdentifier) createIdentifier(fieldNamePos, fieldName);
        fieldBasedAccess.field.pos = fieldNamePos;
        fieldBasedAccess.expr = (BLangVariableReference) exprNodeStack.pop();
        fieldBasedAccess.fieldKind = fieldType;
        fieldBasedAccess.optionalFieldAccess = optionalFieldAccess;
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

    void endFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, boolean publicFunc, boolean remoteFunc,
                        boolean nativeFunc, boolean privateFunc, boolean bodyExists, boolean isLambda) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
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

        if (!bodyExists) {
            function.body = null;
        } else {
            function.body.pos = function.pos;
        }

        this.compUnit.addTopLevelNode(function);
    }

    void startWorker(PackageID pkgID) {
        this.startLambdaFunctionDef(pkgID);
        BLangFunction lambdaFunction = (BLangFunction) this.invokableNodeStack.peek();
        lambdaFunction.addFlag(Flag.WORKER);
        this.startBlock();
    }

    void addWorker(DiagnosticPos pos, Set<Whitespace> ws, String workerName, boolean retParamsAvail,
                   int numAnnotations) {
        // Merge worker definition whitespaces and worker declaration whitespaces.
        if (this.workerDefinitionWSStack.size() > 0 && ws != null) {
            ws.addAll(this.workerDefinitionWSStack.pop());
        }

        endCallableUnitBody(ws);

        BLangFunction bLangFunction = (BLangFunction) this.invokableNodeStack.peek();
        // change default worker name
        bLangFunction.defaultWorkerName.value = workerName;

        // Attach worker annotation to the function node.
        attachAnnotations(bLangFunction, numAnnotations, true);

        addLambdaFunctionDef(pos, ws, false, retParamsAvail, false);
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

        addNameReference(pos, null, null, workerLambdaName);
        startInvocationNode(null);
        createWorkerLambdaInvocationNode(pos, null, workerLambdaName);
        markLastInvocationAsAsync(pos, numAnnotations);
        addWorkerVariableDefStatement(pos, workerName);
        BLangSimpleVariableDef invocationStmt = getLastVarDefStmtFromBlock();
        if (invocationStmt != null) {
            invocationStmt.isWorker = true;
        }
    }

    private void addWorkerVariableDefStatement(DiagnosticPos pos, String identifier) {
        BLangSimpleVariableDef varDefNode = createSimpleVariableDef(pos, null, identifier, null, true, true, true);
        if (this.bindingPatternIdentifierWS.size() > 0) {
            varDefNode.addWS(this.bindingPatternIdentifierWS.pop());
        }
        varDefNode.var.flagSet.add(Flag.WORKER);
        addStmtToCurrentBlock(varDefNode);
    }

    private BLangSimpleVariableDef getLastVarDefStmtFromBlock() {
        BLangSimpleVariableDef variableDef = null;
        if (!this.blockNodeStack.isEmpty()) {
            List<? extends StatementNode> stmtsAdded = this.blockNodeStack.peek().getStatements();
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

    void endCallableUnitBody(Set<Whitespace> ws) {
        BlockNode block = this.blockNodeStack.pop();
        InvokableNode invokableNode = this.invokableNodeStack.peek();
        invokableNode.addWS(ws);
        invokableNode.setBody(block);
    }

    void endExternalFunctionBody(int annotCount) {
        InvokableNode invokableNode = this.invokableNodeStack.peek();

        if (annotCount == 0 || annotAttachmentStack.empty()) {
            return;
        }

        List<AnnotationAttachmentNode> tempAnnotAttachments = new ArrayList<>(annotCount);
        for (int i = 0; i < annotCount; i++) {
            if (annotAttachmentStack.empty()) {
                break;
            }
            tempAnnotAttachments.add(annotAttachmentStack.pop());
        }
        // reversing the collected annotations to preserve the original order
        Collections.reverse(tempAnnotAttachments);
        tempAnnotAttachments.forEach(invokableNode::addExternalAnnotationAttachment);
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
        nameComps.forEach(e -> pkgNameComps.add((BLangIdentifier) this.createIdentifier(pos, e)));
        BLangIdentifier versionNode = (BLangIdentifier) this.createIdentifier(pos, version);
        BLangIdentifier aliasNode = (alias != null && !alias.isEmpty()) ?
                (BLangIdentifier) this.createIdentifier(pos, alias) :
                pkgNameComps.get(pkgNameComps.size() - 1);

        BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        importDcl.pos = pos;
        importDcl.addWS(ws);
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.version = versionNode;
        importDcl.orgName = (BLangIdentifier) this.createIdentifier(pos, orgName);
        importDcl.alias = aliasNode;
        importDcl.compUnit = (BLangIdentifier) this.createIdentifier(pos, this.compUnit.getName());
        return importDcl;
    }

    private VariableNode generateBasicVarNodeWithoutType(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                                         DiagnosticPos identifierPos, boolean isExpressionAvailable) {
        BLangSimpleVariable var = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        var.pos = pos;
        IdentifierNode name = this.createIdentifier(identifierPos, identifier);
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
        BLangIdentifier name = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        name.pos = identifierPos;
        name.value = identifier;
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
        return generateBasicVarNode(pos, ws, identifier, identifierPos, false, isExpressionAvailable);
    }

    private VariableNode generateBasicVarNode(DiagnosticPos pos, Set<Whitespace> ws, String identifier,
                                              DiagnosticPos identifierPos, boolean isDeclaredWithVar,
                                              boolean isExpressionAvailable) {
        BLangSimpleVariable var = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        var.pos = pos;
        IdentifierNode name = this.createIdentifier(identifierPos, identifier);
        ((BLangIdentifier) name).pos = identifierPos;
        var.setName(name);
        var.addWS(ws);
        if (isDeclaredWithVar) {
            var.isDeclaredWithVar = true;
        } else {
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

    void addGlobalVariable(DiagnosticPos pos, Set<Whitespace> ws, String identifier, DiagnosticPos identifierPos,
                           boolean isPublic, boolean isFinal, boolean isDeclaredWithVar, boolean isExpressionAvailable,
                           boolean isListenerVar) {
        BLangVariable var = (BLangVariable) this.generateBasicVarNode(pos, ws, identifier, identifierPos,
                isDeclaredWithVar, isExpressionAvailable);

        if (isPublic) {
            var.flagSet.add(Flag.PUBLIC);
        }
        if (isFinal) {
            var.flagSet.add(Flag.FINAL);
        }
        if (isListenerVar) {
            var.flagSet.add(Flag.LISTENER);
        }

        attachAnnotations(var);
        attachMarkdownDocumentations(var);

        this.compUnit.addTopLevelNode(var);
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

    void addObjectType(DiagnosticPos pos, Set<Whitespace> ws, boolean isFieldAnalyseRequired, boolean isAnonymous,
                       boolean isAbstract, boolean isClient, boolean isService) {
        BLangObjectTypeNode objectTypeNode = populateObjectTypeNode(pos, ws, isAnonymous);
        objectTypeNode.addWS(this.objectFieldBlockWs.pop());
        objectTypeNode.isFieldAnalyseRequired = isFieldAnalyseRequired;

        if (isAbstract) {
            objectTypeNode.flagSet.add(Flag.ABSTRACT);
        }

        if (isClient) {
            objectTypeNode.flagSet.add(Flag.CLIENT);
        }

        if (isService) {
            objectTypeNode.flagSet.add(Flag.SERVICE);
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
        BLangIdentifier identifierNode = (BLangIdentifier) this.createIdentifier(identifierPos, identifier);
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

    void endObjectAttachedFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, boolean publicFunc, boolean privateFunc,
                                      boolean remoteFunc, boolean resourceFunc, boolean nativeFunc, boolean bodyExists,
                                      boolean markdownDocPresent, int annCount) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
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
        if (nativeFunc) {
            function.flagSet.add(Flag.NATIVE);
        }

        if (!bodyExists) {
            function.body = null;
            if (!nativeFunc) {
                function.flagSet.add(Flag.INTERFACE);
                function.interfaceFunction = true;
            }
        } else {
            function.body.pos = pos;
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
        BLangIdentifier identifierNode = (BLangIdentifier) this.createIdentifier(identifierPos, identifier);
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

    void endParameterDocumentation(DiagnosticPos pos, Set<Whitespace> ws, String parameterName, String description) {
        MarkdownDocumentationNode markdownDocumentationNode = markdownDocumentationStack.peek();
        BLangMarkdownParameterDocumentation parameterDocumentationNode =
                (BLangMarkdownParameterDocumentation) TreeBuilder.createMarkdownParameterDocumentationNode();
        parameterDocumentationNode.parameterName = (BLangIdentifier) createIdentifier(pos, parameterName);
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
        if (this.bindingPatternIdentifierWS.size() > 0) {
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
        BlockNode blockNode = blockNodeStack.pop();
        ((BLangBlockStmt) blockNode).pos = pos;
        ifNode.setBody(blockNode);
    }

    void addElseIfBlock(DiagnosticPos pos, Set<Whitespace> ws) {
        IfNode elseIfNode = ifElseStatementStack.pop();
        ((BLangIf) elseIfNode).pos = pos;
        elseIfNode.setCondition(exprNodeStack.pop());
        BlockNode blockNode = blockNodeStack.pop();
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
        if (this.bindingPatternIdentifierWS.size() > 0) {
            patternClause.addWS(this.bindingPatternIdentifierWS.pop());
        }

        patternClause.bindingPatternVariable = this.varStack.pop();
        if (this.errorMatchPatternWS.size() > 0) {
            patternClause.bindingPatternVariable.addWS(this.errorMatchPatternWS.pop());
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
        workerSendNode.setWorkerName(this.createIdentifier(pos, workerName));
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
        workerReceiveExpr.setWorkerName(this.createIdentifier(pos, workerName));
        workerReceiveExpr.pos = pos;
        workerReceiveExpr.addWS(ws);
        //if there are two expressions, this is a channel receive and the top expression is the key
        if (hasKey) {
            workerReceiveExpr.keyExpr = (BLangExpression) exprNodeStack.pop();
            workerReceiveExpr.isChannel = true;
        }
        addExpressionNode(workerReceiveExpr);
    }

    void addWorkerFlushExpr(DiagnosticPos pos, Set<Whitespace> ws, String workerName) {
        BLangWorkerFlushExpr workerFlushExpr = TreeBuilder.createWorkerFlushExpressionNode();
        if (workerName != null) {
            workerFlushExpr.workerIdentifier = (BLangIdentifier) createIdentifier(pos, workerName);
        }
        workerFlushExpr.pos = pos;
        workerFlushExpr.addWS(ws);
        addExpressionNode(workerFlushExpr);
    }

    void addWorkerSendSyncExpr(DiagnosticPos pos, Set<Whitespace> ws, String workerName) {
        BLangWorkerSyncSendExpr workerSendExpr = TreeBuilder.createWorkerSendSyncExprNode();
        workerSendExpr.setWorkerName(this.createIdentifier(pos, workerName));
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
        BLangIdentifier serviceVar = (BLangIdentifier) createIdentifier(identifierPos, serviceName);
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
        BLangIdentifier serviceTypeID = (BLangIdentifier) createIdentifier(identifierPos, serviceTypeName);
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
        qname.localname = (BLangIdentifier) createIdentifier(pos, localname);
        qname.prefix = (BLangIdentifier) createIdentifier(pos, prefix);
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
        BLangIdentifier prefixIdentifer = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        prefixIdentifer.pos = prefixPos;
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
        namedArg.name = (BLangIdentifier) this.createIdentifier(pos, name);
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
            while (this.exprNodeStack.peek().getKind() == NodeKind.INVOCATION) {
                exprList.add(this.exprNodeStack.pop());
            }
            if (exprList.size() > 0) {
                streamingInput.setPostFunctionInvocations(exprList);
            }
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
        //TODO: Patterns will be supported after 1.0.0
        dlog.error(pos, DiagnosticCode.PATTERNS_NOT_SUPPORTED);

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
            this.varListStack.pop().forEach(param -> foreverNode.addParameter((SimpleVariableNode) param));
        }

        Collections.reverse(streamingQueryStatementStack);
        while (!streamingQueryStatementStack.empty()) {
            foreverNode.addStreamingQueryStatement(streamingQueryStatementStack.pop());
        }

        addStmtToCurrentBlock(foreverNode);

        // implicit import of streams module, user doesn't want to import explicitly

        List<String> nameComps = getPackageNameComps(Names.STREAMS_MODULE.value);
        BLangImportPackage importDcl = getImportPackage(pos, null, Names.STREAMS_ORG.value, nameComps, null,
                nameComps.get(nameComps.size() - 1));
        if (!this.imports.contains(importDcl)) {
            List<TopLevelNode> topLevelNodes = this.compUnit.getTopLevelNodes();
            topLevelNodes.add(0, importDcl);
            this.imports.add(importDcl);
        }
    }

    BLangLambdaFunction getScopesFunctionDef(DiagnosticPos pos, Set<Whitespace> ws, boolean bodyExists, String name) {
        BLangFunction function = (BLangFunction) this.invokableNodeStack.pop();
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
        BLangIdentifier key = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        key.setLiteral(false);
        key.setValue(identifier);
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
}
