// Generated from /home/natasha/Documents/github_repos/ballerina/compiler/ballerina-lang/src/main/resources/grammar/BallerinaParser.g4 by ANTLR 4.7
package grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link BallerinaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface BallerinaParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(BallerinaParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#packageDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#packageName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageName(BallerinaParser.PackageNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#version}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVersion(BallerinaParser.VersionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#importDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportDeclaration(BallerinaParser.ImportDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#orgName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrgName(BallerinaParser.OrgNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(BallerinaParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#serviceDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#serviceBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServiceBody(BallerinaParser.ServiceBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#resourceDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#callableUnitBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallableUnitBody(BallerinaParser.CallableUnitBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#functionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#lambdaFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaFunction(BallerinaParser.LambdaFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#callableUnitSignature}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallableUnitSignature(BallerinaParser.CallableUnitSignatureContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#connectorDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#connectorBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnectorBody(BallerinaParser.ConnectorBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#actionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionDefinition(BallerinaParser.ActionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#structDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructDefinition(BallerinaParser.StructDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#structBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructBody(BallerinaParser.StructBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#privateStructBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrivateStructBody(BallerinaParser.PrivateStructBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#enumDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumDefinition(BallerinaParser.EnumDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#enumerator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumerator(BallerinaParser.EnumeratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#globalVariableDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobalVariableDefinition(BallerinaParser.GlobalVariableDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#transformerDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformerDefinition(BallerinaParser.TransformerDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code serviceAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServiceAttachPoint(BallerinaParser.ServiceAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by the {@code resourceAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceAttachPoint(BallerinaParser.ResourceAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by the {@code connectorAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnectorAttachPoint(BallerinaParser.ConnectorAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by the {@code actionAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionAttachPoint(BallerinaParser.ActionAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionAttachPoint(BallerinaParser.FunctionAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by the {@code structAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructAttachPoint(BallerinaParser.StructAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by the {@code enumAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumAttachPoint(BallerinaParser.EnumAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by the {@code constAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstAttachPoint(BallerinaParser.ConstAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parameterAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterAttachPoint(BallerinaParser.ParameterAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by the {@code annotationAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationAttachPoint(BallerinaParser.AnnotationAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by the {@code transformerAttachPoint}
	 * labeled alternative in {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransformerAttachPoint(BallerinaParser.TransformerAttachPointContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationBody(BallerinaParser.AnnotationBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#constantDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#workerDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#workerDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorkerDefinition(BallerinaParser.WorkerDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(BallerinaParser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#builtInTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBuiltInTypeName(BallerinaParser.BuiltInTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#referenceTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceTypeName(BallerinaParser.ReferenceTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#userDefineTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserDefineTypeName(BallerinaParser.UserDefineTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#anonStructTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnonStructTypeName(BallerinaParser.AnonStructTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#valueTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueTypeName(BallerinaParser.ValueTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#builtInReferenceTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBuiltInReferenceTypeName(BallerinaParser.BuiltInReferenceTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#functionTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTypeName(BallerinaParser.FunctionTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#xmlNamespaceName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlNamespaceName(BallerinaParser.XmlNamespaceNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#xmlLocalName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlLocalName(BallerinaParser.XmlLocalNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationAttachment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationAttachment(BallerinaParser.AnnotationAttachmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationAttributeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationAttributeList(BallerinaParser.AnnotationAttributeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationAttribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationAttribute(BallerinaParser.AnnotationAttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationAttributeValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationAttributeValue(BallerinaParser.AnnotationAttributeValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationAttributeArray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationAttributeArray(BallerinaParser.AnnotationAttributeArrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(BallerinaParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#variableDefinitionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#recordLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordLiteral(BallerinaParser.RecordLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#recordKeyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordKeyValue(BallerinaParser.RecordKeyValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#recordKey}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordKey(BallerinaParser.RecordKeyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#arrayLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayLiteral(BallerinaParser.ArrayLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#connectorInit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnectorInit(BallerinaParser.ConnectorInitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#endpointDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndpointDeclaration(BallerinaParser.EndpointDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#endpointDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndpointDefinition(BallerinaParser.EndpointDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#assignmentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#bindStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBindStatement(BallerinaParser.BindStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#variableReferenceList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#ifElseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfElseStatement(BallerinaParser.IfElseStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#ifClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfClause(BallerinaParser.IfClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#elseIfClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseIfClause(BallerinaParser.ElseIfClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#elseClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElseClause(BallerinaParser.ElseClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#foreachStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeachStatement(BallerinaParser.ForeachStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#intRangeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntRangeExpression(BallerinaParser.IntRangeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(BallerinaParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#nextStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNextStatement(BallerinaParser.NextStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#breakStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakStatement(BallerinaParser.BreakStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#forkJoinStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#joinClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinClause(BallerinaParser.JoinClauseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code anyJoinCondition}
	 * labeled alternative in {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code allJoinCondition}
	 * labeled alternative in {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#timeoutClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeoutClause(BallerinaParser.TimeoutClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#catchClauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchClauses(BallerinaParser.CatchClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#catchClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchClause(BallerinaParser.CatchClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#finallyClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFinallyClause(BallerinaParser.FinallyClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#throwStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThrowStatement(BallerinaParser.ThrowStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(BallerinaParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#workerInteractionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code invokeWorker}
	 * labeled alternative in {@link BallerinaParser#triggerWorker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInvokeWorker(BallerinaParser.InvokeWorkerContext ctx);
	/**
	 * Visit a parse tree produced by the {@code invokeFork}
	 * labeled alternative in {@link BallerinaParser#triggerWorker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInvokeFork(BallerinaParser.InvokeForkContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#workerReply}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorkerReply(BallerinaParser.WorkerReplyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code xmlAttribVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttribVariableReference(BallerinaParser.XmlAttribVariableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code simpleVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleVariableReference(BallerinaParser.SimpleVariableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code invocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInvocationReference(BallerinaParser.InvocationReferenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionInvocationReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionInvocationReference(BallerinaParser.FunctionInvocationReferenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fieldVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldVariableReference(BallerinaParser.FieldVariableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mapArrayVariableReference}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapArrayVariableReference(BallerinaParser.MapArrayVariableReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(BallerinaParser.FieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#index}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndex(BallerinaParser.IndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#xmlAttrib}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlAttrib(BallerinaParser.XmlAttribContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#functionInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionInvocation(BallerinaParser.FunctionInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#invocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInvocation(BallerinaParser.InvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(BallerinaParser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#expressionStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStmt(BallerinaParser.ExpressionStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#transactionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionStatement(BallerinaParser.TransactionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#transactionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionClause(BallerinaParser.TransactionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#transactionPropertyInitStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionPropertyInitStatement(BallerinaParser.TransactionPropertyInitStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#transactionPropertyInitStatementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTransactionPropertyInitStatementList(BallerinaParser.TransactionPropertyInitStatementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#lockStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLockStatement(BallerinaParser.LockStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#failedClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFailedClause(BallerinaParser.FailedClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#abortStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbortStatement(BallerinaParser.AbortStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#retriesStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRetriesStatement(BallerinaParser.RetriesStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#namespaceDeclarationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamespaceDeclarationStatement(BallerinaParser.NamespaceDeclarationStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#namespaceDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamespaceDeclaration(BallerinaParser.NamespaceDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code connectorInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnectorInitExpression(BallerinaParser.ConnectorInitExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryDivMulModExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryOrExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code xmlLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlLiteralExpression(BallerinaParser.XmlLiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code valueTypeTypeExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueTypeTypeExpression(BallerinaParser.ValueTypeTypeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code simpleLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleLiteralExpression(BallerinaParser.SimpleLiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringTemplateLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringTemplateLiteralExpression(BallerinaParser.StringTemplateLiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lambdaFunctionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaFunctionExpression(BallerinaParser.LambdaFunctionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryEqualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code recordLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordLiteralExpression(BallerinaParser.RecordLiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arrayLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayLiteralExpression(BallerinaParser.ArrayLiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeAccessExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeAccessExpression(BallerinaParser.TypeAccessExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bracedExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBracedExpression(BallerinaParser.BracedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code variableReferenceExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeCastingExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryAndExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryAddSubExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeConversionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeConversionExpression(BallerinaParser.TypeConversionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryCompareExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code builtInReferenceTypeTypeExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBuiltInReferenceTypeTypeExpression(BallerinaParser.BuiltInReferenceTypeTypeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ternaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTernaryExpression(BallerinaParser.TernaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryPowExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#nameReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNameReference(BallerinaParser.NameReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#returnParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnParameters(BallerinaParser.ReturnParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#parameterTypeNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterTypeNameList(BallerinaParser.ParameterTypeNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#parameterTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterTypeName(BallerinaParser.ParameterTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#parameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterList(BallerinaParser.ParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(BallerinaParser.ParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#fieldDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDefinition(BallerinaParser.FieldDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#simpleLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleLiteral(BallerinaParser.SimpleLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#xmlLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlLiteral(BallerinaParser.XmlLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#xmlItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlItem(BallerinaParser.XmlItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#content}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContent(BallerinaParser.ContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(BallerinaParser.CommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElement(BallerinaParser.ElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#startTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartTag(BallerinaParser.StartTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#closeTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCloseTag(BallerinaParser.CloseTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#emptyTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyTag(BallerinaParser.EmptyTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#procIns}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProcIns(BallerinaParser.ProcInsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute(BallerinaParser.AttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#text}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitText(BallerinaParser.TextContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#xmlQuotedString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlQuotedString(BallerinaParser.XmlQuotedStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#xmlSingleQuotedString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlSingleQuotedString(BallerinaParser.XmlSingleQuotedStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#xmlDoubleQuotedString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlDoubleQuotedString(BallerinaParser.XmlDoubleQuotedStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#xmlQualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitXmlQualifiedName(BallerinaParser.XmlQualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#stringTemplateLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringTemplateLiteral(BallerinaParser.StringTemplateLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#stringTemplateContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringTemplateContent(BallerinaParser.StringTemplateContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#anyIdentifierName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnyIdentifierName(BallerinaParser.AnyIdentifierNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#reservedWord}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReservedWord(BallerinaParser.ReservedWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#deprecatedAttachment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeprecatedAttachment(BallerinaParser.DeprecatedAttachmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#deprecatedText}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeprecatedText(BallerinaParser.DeprecatedTextContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#deprecatedTemplateInlineCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeprecatedTemplateInlineCode(BallerinaParser.DeprecatedTemplateInlineCodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#singleBackTickDeprecatedInlineCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleBackTickDeprecatedInlineCode(BallerinaParser.SingleBackTickDeprecatedInlineCodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#doubleBackTickDeprecatedInlineCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleBackTickDeprecatedInlineCode(BallerinaParser.DoubleBackTickDeprecatedInlineCodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tripleBackTickDeprecatedInlineCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTripleBackTickDeprecatedInlineCode(BallerinaParser.TripleBackTickDeprecatedInlineCodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#documentationAttachment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentationAttachment(BallerinaParser.DocumentationAttachmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#documentationTemplateContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentationTemplateContent(BallerinaParser.DocumentationTemplateContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#documentationTemplateAttributeDescription}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentationTemplateAttributeDescription(BallerinaParser.DocumentationTemplateAttributeDescriptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#docText}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocText(BallerinaParser.DocTextContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#documentationTemplateInlineCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentationTemplateInlineCode(BallerinaParser.DocumentationTemplateInlineCodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#singleBackTickDocInlineCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleBackTickDocInlineCode(BallerinaParser.SingleBackTickDocInlineCodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#doubleBackTickDocInlineCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleBackTickDocInlineCode(BallerinaParser.DoubleBackTickDocInlineCodeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tripleBackTickDocInlineCode}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTripleBackTickDocInlineCode(BallerinaParser.TripleBackTickDocInlineCodeContext ctx);
}