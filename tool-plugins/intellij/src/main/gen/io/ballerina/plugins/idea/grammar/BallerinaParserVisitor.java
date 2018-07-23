// Generated from /home/nino/Desktop/ballerina/tool-plugins/intellij/src/main/java/io/ballerina/plugins/idea/grammar/BallerinaParser.g4 by ANTLR 4.7
package io.ballerina.plugins.idea.grammar;
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
	 * Visit a parse tree produced by {@link BallerinaParser#serviceEndpointAttachments}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServiceEndpointAttachments(BallerinaParser.ServiceEndpointAttachmentsContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#resourceParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceParameterList(BallerinaParser.ResourceParameterListContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#typeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDefinition(BallerinaParser.TypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#objectBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectBody(BallerinaParser.ObjectBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#objectInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectInitializer(BallerinaParser.ObjectInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#objectInitializerParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectInitializerParameterList(BallerinaParser.ObjectInitializerParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#objectFunctions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectFunctions(BallerinaParser.ObjectFunctionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#objectFieldDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectFieldDefinition(BallerinaParser.ObjectFieldDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#fieldDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldDefinition(BallerinaParser.FieldDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#recordRestFieldDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordRestFieldDefinition(BallerinaParser.RecordRestFieldDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#sealedLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSealedLiteral(BallerinaParser.SealedLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#restDescriptorPredicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestDescriptorPredicate(BallerinaParser.RestDescriptorPredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#objectParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectParameterList(BallerinaParser.ObjectParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#objectParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectParameter(BallerinaParser.ObjectParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#objectDefaultableParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectDefaultableParameter(BallerinaParser.ObjectDefaultableParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#objectFunctionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectFunctionDefinition(BallerinaParser.ObjectFunctionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#globalVariableDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobalVariableDefinition(BallerinaParser.GlobalVariableDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttachmentPoint(BallerinaParser.AttachmentPointContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#globalEndpointDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobalEndpointDefinition(BallerinaParser.GlobalEndpointDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#endpointDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndpointDeclaration(BallerinaParser.EndpointDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#endpointType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndpointType(BallerinaParser.EndpointTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#endpointInitlization}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndpointInitlization(BallerinaParser.EndpointInitlizationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#finiteType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFiniteType(BallerinaParser.FiniteTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#finiteTypeUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFiniteTypeUnit(BallerinaParser.FiniteTypeUnitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tupleTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleTypeNameLabel(BallerinaParser.TupleTypeNameLabelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code recordTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordTypeNameLabel(BallerinaParser.RecordTypeNameLabelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unionTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionTypeNameLabel(BallerinaParser.UnionTypeNameLabelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code simpleTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleTypeNameLabel(BallerinaParser.SimpleTypeNameLabelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nullableTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullableTypeNameLabel(BallerinaParser.NullableTypeNameLabelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arrayTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayTypeNameLabel(BallerinaParser.ArrayTypeNameLabelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code objectTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectTypeNameLabel(BallerinaParser.ObjectTypeNameLabelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code groupTypeNameLabel}
	 * labeled alternative in {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupTypeNameLabel(BallerinaParser.GroupTypeNameLabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#recordFieldDefinitionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRecordFieldDefinitionList(BallerinaParser.RecordFieldDefinitionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#simpleTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleTypeName(BallerinaParser.SimpleTypeNameContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#tableLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableLiteral(BallerinaParser.TableLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tableColumnDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableColumnDefinition(BallerinaParser.TableColumnDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tableColumn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableColumn(BallerinaParser.TableColumnContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tableDataArray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableDataArray(BallerinaParser.TableDataArrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tableDataList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableDataList(BallerinaParser.TableDataListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tableData}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableData(BallerinaParser.TableDataContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#arrayLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayLiteral(BallerinaParser.ArrayLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeInitExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeInitExpr(BallerinaParser.TypeInitExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#assignmentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tupleDestructuringStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleDestructuringStatement(BallerinaParser.TupleDestructuringStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#compoundAssignmentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundAssignmentStatement(BallerinaParser.CompoundAssignmentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#compoundOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundOperator(BallerinaParser.CompoundOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#postIncrementStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostIncrementStatement(BallerinaParser.PostIncrementStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#postArithmeticOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostArithmeticOperator(BallerinaParser.PostArithmeticOperatorContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#matchStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchStatement(BallerinaParser.MatchStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#matchPatternClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchPatternClause(BallerinaParser.MatchPatternClauseContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#continueStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueStatement(BallerinaParser.ContinueStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#breakStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakStatement(BallerinaParser.BreakStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#scopeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScopeStatement(BallerinaParser.ScopeStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#scopeClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScopeClause(BallerinaParser.ScopeClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#compensationClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompensationClause(BallerinaParser.CompensationClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#compensateStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompensateStatement(BallerinaParser.CompensateStatementContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#invocationArgList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInvocationArgList(BallerinaParser.InvocationArgListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#invocationArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInvocationArg(BallerinaParser.InvocationArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#actionInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionInvocation(BallerinaParser.ActionInvocationContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#onretryClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnretryClause(BallerinaParser.OnretryClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#abortStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbortStatement(BallerinaParser.AbortStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#retryStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRetryStatement(BallerinaParser.RetryStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#retriesStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRetriesStatement(BallerinaParser.RetriesStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#oncommitStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOncommitStatement(BallerinaParser.OncommitStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#onabortStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnabortStatement(BallerinaParser.OnabortStatementContext ctx);
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
	 * Visit a parse tree produced by the {@code bitwiseShiftExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitwiseShiftExpression(BallerinaParser.BitwiseShiftExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeAccessExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeAccessExpression(BallerinaParser.TypeAccessExpressionContext ctx);
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
	 * Visit a parse tree produced by the {@code checkedExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCheckedExpression(BallerinaParser.CheckedExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bitwiseExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitwiseExpression(BallerinaParser.BitwiseExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bracedOrTupleExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBracedOrTupleExpression(BallerinaParser.BracedOrTupleExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryDivMulModExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tableLiteralExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableLiteralExpression(BallerinaParser.TableLiteralExpressionContext ctx);
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
	 * Visit a parse tree produced by the {@code awaitExprExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAwaitExprExpression(BallerinaParser.AwaitExprExpressionContext ctx);
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
	 * Visit a parse tree produced by the {@code variableReferenceExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code matchExprExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchExprExpression(BallerinaParser.MatchExprExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code actionInvocationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionInvocationExpression(BallerinaParser.ActionInvocationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryCompareExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code integerRangeExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerRangeExpression(BallerinaParser.IntegerRangeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code elvisExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElvisExpression(BallerinaParser.ElvisExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tableQueryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableQueryExpression(BallerinaParser.TableQueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ternaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTernaryExpression(BallerinaParser.TernaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeInitExpression(BallerinaParser.TypeInitExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code awaitExpr}
	 * labeled alternative in {@link BallerinaParser#awaitExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAwaitExpr(BallerinaParser.AwaitExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#shiftExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftExpression(BallerinaParser.ShiftExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#shiftExprPredicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftExprPredicate(BallerinaParser.ShiftExprPredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#matchExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchExpression(BallerinaParser.MatchExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#matchExpressionPatternClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMatchExpressionPatternClause(BallerinaParser.MatchExpressionPatternClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#nameReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNameReference(BallerinaParser.NameReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#functionNameReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionNameReference(BallerinaParser.FunctionNameReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#returnParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnParameter(BallerinaParser.ReturnParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#lambdaReturnParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaReturnParameter(BallerinaParser.LambdaReturnParameterContext ctx);
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
	 * Visit a parse tree produced by the {@code simpleParameter}
	 * labeled alternative in {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleParameter(BallerinaParser.SimpleParameterContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tupleParameter}
	 * labeled alternative in {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTupleParameter(BallerinaParser.TupleParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#defaultableParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultableParameter(BallerinaParser.DefaultableParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#restParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestParameter(BallerinaParser.RestParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#formalParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameterList(BallerinaParser.FormalParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#simpleLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleLiteral(BallerinaParser.SimpleLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#integerLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntegerLiteral(BallerinaParser.IntegerLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#emptyTupleLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEmptyTupleLiteral(BallerinaParser.EmptyTupleLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#blobLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlobLiteral(BallerinaParser.BlobLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#namedArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedArgs(BallerinaParser.NamedArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#restArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRestArgs(BallerinaParser.RestArgsContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#tableQuery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableQuery(BallerinaParser.TableQueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#foreverStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeverStatement(BallerinaParser.ForeverStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#doneStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoneStatement(BallerinaParser.DoneStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#streamingQueryStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStreamingQueryStatement(BallerinaParser.StreamingQueryStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#patternClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPatternClause(BallerinaParser.PatternClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#withinClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithinClause(BallerinaParser.WithinClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(BallerinaParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#orderByVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByVariable(BallerinaParser.OrderByVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(BallerinaParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#selectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClause(BallerinaParser.SelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#selectExpressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectExpressionList(BallerinaParser.SelectExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#selectExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectExpression(BallerinaParser.SelectExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(BallerinaParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(BallerinaParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#streamingAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStreamingAction(BallerinaParser.StreamingActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#setClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetClause(BallerinaParser.SetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#setAssignmentClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetAssignmentClause(BallerinaParser.SetAssignmentClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#streamingInput}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStreamingInput(BallerinaParser.StreamingInputContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#joinStreamingInput}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinStreamingInput(BallerinaParser.JoinStreamingInputContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#outputRateLimit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOutputRateLimit(BallerinaParser.OutputRateLimitContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#patternStreamingInput}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPatternStreamingInput(BallerinaParser.PatternStreamingInputContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#patternStreamingEdgeInput}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPatternStreamingEdgeInput(BallerinaParser.PatternStreamingEdgeInputContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(BallerinaParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#windowClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowClause(BallerinaParser.WindowClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#orderByType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByType(BallerinaParser.OrderByTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinType(BallerinaParser.JoinTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#timeScale}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeScale(BallerinaParser.TimeScaleContext ctx);
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
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#documentationString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentationString(BallerinaParser.DocumentationStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#documentationLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentationLine(BallerinaParser.DocumentationLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#parameterDocumentationLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDocumentationLine(BallerinaParser.ParameterDocumentationLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#returnParameterDocumentationLine}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnParameterDocumentationLine(BallerinaParser.ReturnParameterDocumentationLineContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#documentationContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentationContent(BallerinaParser.DocumentationContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#parameterDescription}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDescription(BallerinaParser.ParameterDescriptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#returnParameterDescription}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnParameterDescription(BallerinaParser.ReturnParameterDescriptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#documentationText}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentationText(BallerinaParser.DocumentationTextContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#documentationReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocumentationReference(BallerinaParser.DocumentationReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#definitionReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinitionReference(BallerinaParser.DefinitionReferenceContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#definitionReferenceType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinitionReferenceType(BallerinaParser.DefinitionReferenceTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#parameterDocumentation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDocumentation(BallerinaParser.ParameterDocumentationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#returnParameterDocumentation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnParameterDocumentation(BallerinaParser.ReturnParameterDocumentationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#docParameterName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocParameterName(BallerinaParser.DocParameterNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#docParameterDescription}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDocParameterDescription(BallerinaParser.DocParameterDescriptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#singleBacktickedBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleBacktickedBlock(BallerinaParser.SingleBacktickedBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#singleBacktickedContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleBacktickedContent(BallerinaParser.SingleBacktickedContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#doubleBacktickedBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleBacktickedBlock(BallerinaParser.DoubleBacktickedBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#doubleBacktickedContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleBacktickedContent(BallerinaParser.DoubleBacktickedContentContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tripleBacktickedBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTripleBacktickedBlock(BallerinaParser.TripleBacktickedBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#tripleBacktickedContent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTripleBacktickedContent(BallerinaParser.TripleBacktickedContentContext ctx);
}