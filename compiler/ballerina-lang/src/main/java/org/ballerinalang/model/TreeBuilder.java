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
package org.ballerinalang.model;

import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationAttributeNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.ConnectorNode;
import org.ballerinalang.model.tree.DeprecatedNode;
import org.ballerinalang.model.tree.DocumentationNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.EnumNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.ImportPackageNode;
import org.ballerinalang.model.tree.ObjectNode;
import org.ballerinalang.model.tree.PackageDeclarationNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.RecordNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.StructNode;
import org.ballerinalang.model.tree.TransformerNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.WorkerNode;
import org.ballerinalang.model.tree.XMLNSDeclarationNode;
import org.ballerinalang.model.tree.clauses.FunctionClauseNode;
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
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeValueNode;
import org.ballerinalang.model.tree.expressions.ArrayLiteralNode;
import org.ballerinalang.model.tree.expressions.BinaryExpressionNode;
import org.ballerinalang.model.tree.expressions.BracedOrTupleExpression;
import org.ballerinalang.model.tree.expressions.CheckedExpressionNode;
import org.ballerinalang.model.tree.expressions.DocumentationAttributeNode;
import org.ballerinalang.model.tree.expressions.ElvisExpressionNode;
import org.ballerinalang.model.tree.expressions.FieldBasedAccessNode;
import org.ballerinalang.model.tree.expressions.IndexBasedAccessNode;
import org.ballerinalang.model.tree.expressions.IntRangeExpression;
import org.ballerinalang.model.tree.expressions.InvocationNode;
import org.ballerinalang.model.tree.expressions.LambdaFunctionNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.model.tree.expressions.MatchExpressionNode;
import org.ballerinalang.model.tree.expressions.MatchExpressionNode.MatchExpressionPatternNode;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.expressions.RestArgsNode;
import org.ballerinalang.model.tree.expressions.SimpleVariableReferenceNode;
import org.ballerinalang.model.tree.expressions.StatementExpressionNode;
import org.ballerinalang.model.tree.expressions.StringTemplateLiteralNode;
import org.ballerinalang.model.tree.expressions.TableLiteralNode;
import org.ballerinalang.model.tree.expressions.TableQueryExpression;
import org.ballerinalang.model.tree.expressions.TernaryExpressionNode;
import org.ballerinalang.model.tree.expressions.TypeCastNode;
import org.ballerinalang.model.tree.expressions.TypeConversionNode;
import org.ballerinalang.model.tree.expressions.TypeInitNode;
import org.ballerinalang.model.tree.expressions.TypedescExpressionNode;
import org.ballerinalang.model.tree.expressions.UnaryExpressionNode;
import org.ballerinalang.model.tree.expressions.XMLAttributeNode;
import org.ballerinalang.model.tree.expressions.XMLCommentLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLElementLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLProcessingInstructionLiteralNode;
import org.ballerinalang.model.tree.expressions.XMLQNameNode;
import org.ballerinalang.model.tree.expressions.XMLQuotedStringNode;
import org.ballerinalang.model.tree.expressions.XMLTextLiteralNode;
import org.ballerinalang.model.tree.statements.AbortNode;
import org.ballerinalang.model.tree.statements.AssignmentNode;
import org.ballerinalang.model.tree.statements.BindNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.BreakNode;
import org.ballerinalang.model.tree.statements.CatchNode;
import org.ballerinalang.model.tree.statements.CompoundAssignmentNode;
import org.ballerinalang.model.tree.statements.DoneNode;
import org.ballerinalang.model.tree.statements.ExpressionStatementNode;
import org.ballerinalang.model.tree.statements.ForeachNode;
import org.ballerinalang.model.tree.statements.ForeverNode;
import org.ballerinalang.model.tree.statements.ForkJoinNode;
import org.ballerinalang.model.tree.statements.IfNode;
import org.ballerinalang.model.tree.statements.LockNode;
import org.ballerinalang.model.tree.statements.MatchNode;
import org.ballerinalang.model.tree.statements.MatchNode.MatchStatementPatternNode;
import org.ballerinalang.model.tree.statements.NextNode;
import org.ballerinalang.model.tree.statements.PostIncrementNode;
import org.ballerinalang.model.tree.statements.RetryNode;
import org.ballerinalang.model.tree.statements.ReturnNode;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.ballerinalang.model.tree.statements.ThrowNode;
import org.ballerinalang.model.tree.statements.TransactionNode;
import org.ballerinalang.model.tree.statements.TryCatchFinallyNode;
import org.ballerinalang.model.tree.statements.TupleDestructureNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.tree.statements.WhileNode;
import org.ballerinalang.model.tree.statements.WorkerReceiveNode;
import org.ballerinalang.model.tree.statements.WorkerSendNode;
import org.ballerinalang.model.tree.statements.XMLNSDeclStatementNode;
import org.ballerinalang.model.tree.types.ArrayTypeNode;
import org.ballerinalang.model.tree.types.BuiltInReferenceTypeNode;
import org.ballerinalang.model.tree.types.ConstrainedTypeNode;
import org.ballerinalang.model.tree.types.FunctionTypeNode;
import org.ballerinalang.model.tree.types.TupleTypeNode;
import org.ballerinalang.model.tree.types.UnionTypeNode;
import org.ballerinalang.model.tree.types.UserDefinedTypeNode;
import org.ballerinalang.model.tree.types.ValueTypeNode;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangDeprecatedNode;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangEnum.BLangEnumerator;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

/**
 * This contains the functionality of building the nodes in the AST.
 *
 * @since 0.94
 */
public class TreeBuilder {

    public static CompilationUnitNode createCompilationUnit() {
        return new BLangCompilationUnit();
    }

    public static PackageNode createPackageNode() {
        return new BLangPackage();
    }

    public static PackageDeclarationNode createPackageDeclarationNode() {
        return new BLangPackageDeclaration();
    }

    public static IdentifierNode createIdentifierNode() {
        return new BLangIdentifier();
    }

    public static ImportPackageNode createImportPackageNode() {
        return new BLangImportPackage();
    }

    public static XMLNSDeclarationNode createXMLNSNode() {
        return new BLangXMLNS();
    }

    public static XMLNSDeclStatementNode createXMLNSDeclrStatementNode() {
        return new BLangXMLNSStatement();
    }

    public static VariableNode createVariableNode() {
        return new BLangVariable();
    }

    public static EndpointNode createEndpointNode() {
        return new BLangEndpoint();
    }

    public static FunctionNode createFunctionNode() {
        return new BLangFunction();
    }

    public static BlockNode createBlockNode() {
        return new BLangBlockStmt();
    }

    public static TryCatchFinallyNode createTryCatchFinallyNode() {
        return new BLangTryCatchFinally();
    }

    public static CatchNode createCatchNode() {
        return new BLangCatch();
    }

    public static ThrowNode createThrowNode() {
        return new BLangThrow();
    }

    public static TupleDestructureNode createTupleDestructureStatementNode() {
        return new BLangTupleDestructure();
    }

    public static ExpressionStatementNode createExpressionStatementNode() {
        return new BLangExpressionStmt();
    }

    public static LiteralNode createLiteralExpression() {
        return new BLangLiteral();
    }

    public static ArrayLiteralNode createArrayLiteralNode() {
        return new BLangArrayLiteral();
    }

    public static RecordLiteralNode createRecordLiteralNode() {
        return new BLangRecordLiteral();
    }

    public static RecordLiteralNode.RecordKeyValueNode createRecordKeyValue() {
        return new BLangRecordLiteral.BLangRecordKeyValue();
    }

    public static TableLiteralNode createTableLiteralNode() {
        return new BLangTableLiteral();
    }

    public static VariableDefinitionNode createVariableDefinitionNode() {
        return new BLangVariableDef();
    }

    public static ValueTypeNode createValueTypeNode() {
        return new BLangValueType();
    }

    public static ArrayTypeNode createArrayTypeNode() {
        return new BLangArrayType();
    }

    public static UnionTypeNode createUnionTypeNode() {
        return new BLangUnionTypeNode();
    }

    public static TupleTypeNode createTupleTypeNode() {
        return new BLangTupleTypeNode();
    }

    public static UserDefinedTypeNode createUserDefinedTypeNode() {
        return new BLangUserDefinedType();
    }

    public static BuiltInReferenceTypeNode createBuiltInReferenceTypeNode() {
        return new BLangBuiltInRefTypeNode();
    }

    public static ConstrainedTypeNode createConstrainedTypeNode() {
        return new BLangConstrainedType();
    }

    public static FunctionTypeNode createFunctionTypeNode() {
        return new BLangFunctionTypeNode();
    }

    public static RecordNode createRecordNode() {
        return new BLangRecord();
    }

    public static SimpleVariableReferenceNode createSimpleVariableReferenceNode() {
        return new BLangSimpleVarRef();
    }

    public static InvocationNode createInvocationNode() {
        return new BLangInvocation();
    }

    public static FieldBasedAccessNode createFieldBasedAccessNode() {
        return new BLangFieldBasedAccess();
    }

    public static IndexBasedAccessNode createIndexBasedAccessNode() {
        return new BLangIndexBasedAccess();
    }

    public static TernaryExpressionNode createTernaryExpressionNode() {
        return new BLangTernaryExpr();
    }

    public static CheckedExpressionNode createCheckExpressionNode() {
        return new BLangCheckedExpr();
    }

    public static BLangAwaitExpr createAwaitExpressionNode() {
        return new BLangAwaitExpr();
    }

    public static BinaryExpressionNode createBinaryExpressionNode() {
        return new BLangBinaryExpr();
    }

    public static ElvisExpressionNode createElvisExpressionNode() {
        return new BLangElvisExpr();
    }

    public static BracedOrTupleExpression createBracedOrTupleExpression() {
        return new BLangBracedOrTupleExpr();
    }

    public static TypeCastNode createTypeCastNode() {
        return new BLangTypeCastExpr();
    }

    public static TypedescExpressionNode createTypeAccessNode() {
        return new BLangTypedescExpr();
    }

    public static TypeConversionNode createTypeConversionNode() {
        return new BLangTypeConversionExpr();
    }

    public static UnaryExpressionNode createUnaryExpressionNode() {
        return new BLangUnaryExpr();
    }

    public static LambdaFunctionNode createLambdaFunctionNode() {
        return new BLangLambdaFunction();
    }

    public static TypeInitNode createObjectInitNode() {
        return new BLangTypeInit();
    }

    public static StructNode createStructNode() {
        return new BLangStruct();
    }

    public static ObjectNode createObjectNode() {
        return new BLangObject();
    }

    public static TypeDefinition createTypeDefinition() {
        return new BLangTypeDefinition();
    }

    public static EnumNode createEnumNode() {
        return new BLangEnum();
    }

    public static EnumNode.Enumerator createEnumeratorNode() {
        return new BLangEnumerator();
    }

    public static ConnectorNode createConnectorNode() {
        return new BLangConnector();
    }

    public static ActionNode createActionNode() {
        return new BLangAction();
    }

    public static AssignmentNode createAssignmentNode() {
        return new BLangAssignment();
    }

    public static CompoundAssignmentNode createCompoundAssignmentNode() {
        return new BLangCompoundAssignment();
    }

    public static PostIncrementNode createPostIncrementNode() {
        return new BLangPostIncrement();
    }

    public static BindNode createBindNode() {
        return new BLangBind();
    }

    public static AbortNode createAbortNode() {
        return new BLangAbort();
    }
    
    public static DoneNode createDoneNode() {
        return new BLangDone();
    }

    public static RetryNode createRetryNode() {
        return new BLangRetry();
    }

    public static TransactionNode createTransactionNode() {
        return new BLangTransaction();
    }

    public static ReturnNode createReturnNode() {
        return new BLangReturn();
    }

    public static AnnotationNode createAnnotationNode() {
        return new BLangAnnotation();
    }

    public static AnnotationAttributeNode createAnnotAttributeNode() {
        return new BLangAnnotAttribute();
    }


    public static AnnotationAttachmentAttributeNode createAnnotAttachmentAttributeNode() {
        return new BLangAnnotAttachmentAttribute();
    }

    public static AnnotationAttachmentAttributeValueNode createAnnotAttributeValueNode() {
        return new BLangAnnotAttachmentAttributeValue();
    }

    public static AnnotationAttachmentNode createAnnotAttachmentNode() {
        return new BLangAnnotationAttachment();
    }

    public static DocumentationNode createDocumentationNode() {
        return new BLangDocumentation();
    }

    public static DeprecatedNode createDeprecatedNode() {
        return new BLangDeprecatedNode();
    }

    public static DocumentationAttributeNode createDocumentationAttributeNode() {
        return new BLangDocumentationAttribute();
    }

    public static IfNode createIfElseStatementNode() {
        return new BLangIf();
    }

    public static MatchNode createMatchStatement() {
        return new BLangMatch();
    }

    public static MatchStatementPatternNode createMatchStatementPattern() {
        return new BLangMatchStmtPatternClause();
    }

    public static ServiceNode createServiceNode() {
        return new BLangService();
    }

    public static ResourceNode createResourceNode() {
        return new BLangResource();
    }

    public static WorkerNode createWorkerNode() {
        return new BLangWorker();
    }

    public static WorkerReceiveNode createWorkerReceiveNode() {
        return new BLangWorkerReceive();
    }

    public static WorkerSendNode createWorkerSendNode() {
        return new BLangWorkerSend();
    }

    public static ForkJoinNode createForkJoinNode() {
        return new BLangForkJoin();
    }

    public static ForeachNode createForeachNode() {
        return new BLangForeach();
    }

    public static WhileNode createWhileNode() {
        return new BLangWhile();
    }

    public static LockNode createLockNode() {
        return new BLangLock();
    }

    public static NextNode createNextNode() {
        return new BLangNext();
    }

    public static BreakNode createBreakNode() {
        return new BLangBreak();
    }

    public static XMLQNameNode createXMLQNameNode() {
        return new BLangXMLQName();
    }

    public static XMLAttributeNode createXMLAttributeNode() {
        return new BLangXMLAttribute();
    }

    public static XMLElementLiteralNode createXMLElementLiteralNode() {
        return new BLangXMLElementLiteral();
    }

    public static XMLTextLiteralNode createXMLTextLiteralNode() {
        return new BLangXMLTextLiteral();
    }

    public static XMLCommentLiteralNode createXMLCommentLiteralNode() {
        return new BLangXMLCommentLiteral();
    }

    public static XMLProcessingInstructionLiteralNode createXMLProcessingIntsructionLiteralNode() {
        return new BLangXMLProcInsLiteral();
    }

    public static XMLQuotedStringNode createXMLQuotedStringNode() {
        return new BLangXMLQuotedString();
    }

    public static StringTemplateLiteralNode createStringTemplateLiteralNode() {
        return new BLangStringTemplateLiteral();
    }

    public static IndexBasedAccessNode createXMLAttributeAccessNode() {
        return new BLangXMLAttributeAccess();
    }

    public static TransformerNode createTransformerNode() {
        return new BLangTransformer();
    }

    public static IntRangeExpression createIntRangeExpression() {
        return new BLangIntRangeExpression();
    }

    public static OrderByNode createOrderByNode() {
        return new BLangOrderBy();
    }

    public static OrderByVariableNode createOrderByVariableNode() {
        return new BLangOrderByVariable();
    }

    public static LimitNode createLimitNode() {
        return new BLangLimit();
    }

    public static BLangGroupBy createGroupByNode() {
        return new BLangGroupBy();
    }

    public static HavingNode createHavingNode() {
        return new BLangHaving();
    }

    public static SelectExpressionNode createSelectExpressionNode() {
        return new BLangSelectExpression();
    }

    public static WhereNode createWhereNode() {
        return new BLangWhere();
    }

    public static SelectClauseNode createSelectClauseNode() {
        return new BLangSelectClause();
    }

    public static FunctionClauseNode createFunctionClauseNode() {
        return new BLangFunctionClause();
    }

    public static WindowClauseNode createWindowClauseNode() {
        return new BLangWindow();
    }

    public static StreamingInput createStreamingInputNode() {
        return new BLangStreamingInput();
    }

    public static JoinStreamingInput createJoinStreamingInputNode() {
        return new BLangJoinStreamingInput();
    }

    public static OutputRateLimitNode createOutputRateLimitNode() {
        return new BLangOutputRateLimit();
    }

    public static TableQuery createTableQueryNode() {
        return new BLangTableQuery();
    }

    public static TableQueryExpression createTableQueryExpression() {
        return new BLangTableQueryExpression();
    }

    public static RestArgsNode createVarArgsNode() {
        return new BLangRestArgsExpression();
    }

    public static NamedArgNode createNamedArgNode() {
        return new BLangNamedArgsExpression();
    }

    public static SetAssignmentNode createSetAssignmentNode() {
        return new BLangSetAssignment();
    }

    public static StreamActionNode createStreamActionNode() {
        return new BLangStreamAction();
    }

    public static PatternStreamingEdgeInputNode createPatternStreamingEdgeInputNode() {
        return new BLangPatternStreamingEdgeInput();
    }

    public static PatternStreamingInputNode createPatternStreamingInputNode() {
        return new BLangPatternStreamingInput();
    }

    public static StreamingQueryStatementNode createStreamingQueryStatementNode() {
        return new BLangStreamingQueryStatement();
    }

    public static ForeverNode createForeverNode() {
        return new BLangForever();
    }

    public static WithinClause createWithinClause() {
        return new BLangWithinClause();
    }

    public static PatternClause createPatternClause() {
        return new BLangPatternClause();
    }

    public static MatchExpressionPatternNode createMatchExpressionPattern() {
        return new BLangMatchExprPatternClause();
    }

    public static MatchExpressionNode createMatchExpression() {
        return new BLangMatchExpression();
    }

    public static StatementExpressionNode creatStatementExpression() {
        return new BLangStatementExpression();
    }
}
