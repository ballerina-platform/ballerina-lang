// Generated from Ballerina.g4 by ANTLR 4.5.3
package org.wso2.ballerina.core.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BallerinaParser}.
 */
public interface BallerinaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(BallerinaParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(BallerinaParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#packageDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitPackageDeclaration(BallerinaParser.PackageDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterImportDeclaration(BallerinaParser.ImportDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#importDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitImportDeclaration(BallerinaParser.ImportDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#serviceDefinition}.
	 * @param ctx the parse tree
	 */
	void enterServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#serviceDefinition}.
	 * @param ctx the parse tree
	 */
	void exitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#serviceBody}.
	 * @param ctx the parse tree
	 */
	void enterServiceBody(BallerinaParser.ServiceBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#serviceBody}.
	 * @param ctx the parse tree
	 */
	void exitServiceBody(BallerinaParser.ServiceBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#serviceBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterServiceBodyDeclaration(BallerinaParser.ServiceBodyDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#serviceBodyDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitServiceBodyDeclaration(BallerinaParser.ServiceBodyDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#resourceDefinition}.
	 * @param ctx the parse tree
	 */
	void enterResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#resourceDefinition}.
	 * @param ctx the parse tree
	 */
	void exitResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionBody}.
	 * @param ctx the parse tree
	 */
	void enterFunctionBody(BallerinaParser.FunctionBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionBody}.
	 * @param ctx the parse tree
	 */
	void exitFunctionBody(BallerinaParser.FunctionBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#connectorDefinition}.
	 * @param ctx the parse tree
	 */
	void enterConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#connectorDefinition}.
	 * @param ctx the parse tree
	 */
	void exitConnectorDefinition(BallerinaParser.ConnectorDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#connectorBody}.
	 * @param ctx the parse tree
	 */
	void enterConnectorBody(BallerinaParser.ConnectorBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#connectorBody}.
	 * @param ctx the parse tree
	 */
	void exitConnectorBody(BallerinaParser.ConnectorBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#actionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterActionDefinition(BallerinaParser.ActionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#actionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitActionDefinition(BallerinaParser.ActionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#connectorDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterConnectorDeclaration(BallerinaParser.ConnectorDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#connectorDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitConnectorDeclaration(BallerinaParser.ConnectorDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeDefinition}.
	 * @param ctx the parse tree
	 */
	void enterTypeDefinition(BallerinaParser.TypeDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeDefinition}.
	 * @param ctx the parse tree
	 */
	void exitTypeDefinition(BallerinaParser.TypeDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeDefinitionBody}.
	 * @param ctx the parse tree
	 */
	void enterTypeDefinitionBody(BallerinaParser.TypeDefinitionBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeDefinitionBody}.
	 * @param ctx the parse tree
	 */
	void exitTypeDefinitionBody(BallerinaParser.TypeDefinitionBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeConvertorDefinition}.
	 * @param ctx the parse tree
	 */
	void enterTypeConvertorDefinition(BallerinaParser.TypeConvertorDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeConvertorDefinition}.
	 * @param ctx the parse tree
	 */
	void exitTypeConvertorDefinition(BallerinaParser.TypeConvertorDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeConvertorBody}.
	 * @param ctx the parse tree
	 */
	void enterTypeConvertorBody(BallerinaParser.TypeConvertorBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeConvertorBody}.
	 * @param ctx the parse tree
	 */
	void exitTypeConvertorBody(BallerinaParser.TypeConvertorBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#constantDefinition}.
	 * @param ctx the parse tree
	 */
	void enterConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#constantDefinition}.
	 * @param ctx the parse tree
	 */
	void exitConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaration(BallerinaParser.VariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaration(BallerinaParser.VariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#workerDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#workerDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#returnTypeList}.
	 * @param ctx the parse tree
	 */
	void enterReturnTypeList(BallerinaParser.ReturnTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#returnTypeList}.
	 * @param ctx the parse tree
	 */
	void exitReturnTypeList(BallerinaParser.ReturnTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeNameList}.
	 * @param ctx the parse tree
	 */
	void enterTypeNameList(BallerinaParser.TypeNameListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeNameList}.
	 * @param ctx the parse tree
	 */
	void exitTypeNameList(BallerinaParser.TypeNameListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#qualifiedTypeName}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedTypeName(BallerinaParser.QualifiedTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#qualifiedTypeName}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedTypeName(BallerinaParser.QualifiedTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#unqualifiedTypeName}.
	 * @param ctx the parse tree
	 */
	void enterUnqualifiedTypeName(BallerinaParser.UnqualifiedTypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#unqualifiedTypeName}.
	 * @param ctx the parse tree
	 */
	void exitUnqualifiedTypeName(BallerinaParser.UnqualifiedTypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeNameWithOptionalSchema}.
	 * @param ctx the parse tree
	 */
	void enterTypeNameWithOptionalSchema(BallerinaParser.TypeNameWithOptionalSchemaContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeNameWithOptionalSchema}.
	 * @param ctx the parse tree
	 */
	void exitTypeNameWithOptionalSchema(BallerinaParser.TypeNameWithOptionalSchemaContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(BallerinaParser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(BallerinaParser.TypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#qualifiedReference}.
	 * @param ctx the parse tree
	 */
	void enterQualifiedReference(BallerinaParser.QualifiedReferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#qualifiedReference}.
	 * @param ctx the parse tree
	 */
	void exitQualifiedReference(BallerinaParser.QualifiedReferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(BallerinaParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(BallerinaParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(BallerinaParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(BallerinaParser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#packageName}.
	 * @param ctx the parse tree
	 */
	void enterPackageName(BallerinaParser.PackageNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#packageName}.
	 * @param ctx the parse tree
	 */
	void exitPackageName(BallerinaParser.PackageNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#literalValue}.
	 * @param ctx the parse tree
	 */
	void enterLiteralValue(BallerinaParser.LiteralValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#literalValue}.
	 * @param ctx the parse tree
	 */
	void exitLiteralValue(BallerinaParser.LiteralValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#annotation}.
	 * @param ctx the parse tree
	 */
	void enterAnnotation(BallerinaParser.AnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#annotation}.
	 * @param ctx the parse tree
	 */
	void exitAnnotation(BallerinaParser.AnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#annotationName}.
	 * @param ctx the parse tree
	 */
	void enterAnnotationName(BallerinaParser.AnnotationNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#annotationName}.
	 * @param ctx the parse tree
	 */
	void exitAnnotationName(BallerinaParser.AnnotationNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#elementValuePairs}.
	 * @param ctx the parse tree
	 */
	void enterElementValuePairs(BallerinaParser.ElementValuePairsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#elementValuePairs}.
	 * @param ctx the parse tree
	 */
	void exitElementValuePairs(BallerinaParser.ElementValuePairsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#elementValuePair}.
	 * @param ctx the parse tree
	 */
	void enterElementValuePair(BallerinaParser.ElementValuePairContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#elementValuePair}.
	 * @param ctx the parse tree
	 */
	void exitElementValuePair(BallerinaParser.ElementValuePairContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#elementValue}.
	 * @param ctx the parse tree
	 */
	void enterElementValue(BallerinaParser.ElementValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#elementValue}.
	 * @param ctx the parse tree
	 */
	void exitElementValue(BallerinaParser.ElementValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 */
	void enterElementValueArrayInitializer(BallerinaParser.ElementValueArrayInitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 */
	void exitElementValueArrayInitializer(BallerinaParser.ElementValueArrayInitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(BallerinaParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(BallerinaParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#assignmentStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#ifElseStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfElseStatement(BallerinaParser.IfElseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#ifElseStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfElseStatement(BallerinaParser.IfElseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#iterateStatement}.
	 * @param ctx the parse tree
	 */
	void enterIterateStatement(BallerinaParser.IterateStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#iterateStatement}.
	 * @param ctx the parse tree
	 */
	void exitIterateStatement(BallerinaParser.IterateStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(BallerinaParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(BallerinaParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#breakStatement}.
	 * @param ctx the parse tree
	 */
	void enterBreakStatement(BallerinaParser.BreakStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#breakStatement}.
	 * @param ctx the parse tree
	 */
	void exitBreakStatement(BallerinaParser.BreakStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#forkJoinStatement}.
	 * @param ctx the parse tree
	 */
	void enterForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#forkJoinStatement}.
	 * @param ctx the parse tree
	 */
	void exitForkJoinStatement(BallerinaParser.ForkJoinStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#joinClause}.
	 * @param ctx the parse tree
	 */
	void enterJoinClause(BallerinaParser.JoinClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#joinClause}.
	 * @param ctx the parse tree
	 */
	void exitJoinClause(BallerinaParser.JoinClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 */
	void enterJoinConditions(BallerinaParser.JoinConditionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 */
	void exitJoinConditions(BallerinaParser.JoinConditionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#timeoutClause}.
	 * @param ctx the parse tree
	 */
	void enterTimeoutClause(BallerinaParser.TimeoutClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#timeoutClause}.
	 * @param ctx the parse tree
	 */
	void exitTimeoutClause(BallerinaParser.TimeoutClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 */
	void enterTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 */
	void exitTryCatchStatement(BallerinaParser.TryCatchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#catchClause}.
	 * @param ctx the parse tree
	 */
	void enterCatchClause(BallerinaParser.CatchClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#catchClause}.
	 * @param ctx the parse tree
	 */
	void exitCatchClause(BallerinaParser.CatchClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#throwStatement}.
	 * @param ctx the parse tree
	 */
	void enterThrowStatement(BallerinaParser.ThrowStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#throwStatement}.
	 * @param ctx the parse tree
	 */
	void exitThrowStatement(BallerinaParser.ThrowStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(BallerinaParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(BallerinaParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#replyStatement}.
	 * @param ctx the parse tree
	 */
	void enterReplyStatement(BallerinaParser.ReplyStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#replyStatement}.
	 * @param ctx the parse tree
	 */
	void exitReplyStatement(BallerinaParser.ReplyStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#workerInteractionStatement}.
	 * @param ctx the parse tree
	 */
	void enterWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#workerInteractionStatement}.
	 * @param ctx the parse tree
	 */
	void exitWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#triggerWorker}.
	 * @param ctx the parse tree
	 */
	void enterTriggerWorker(BallerinaParser.TriggerWorkerContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#triggerWorker}.
	 * @param ctx the parse tree
	 */
	void exitTriggerWorker(BallerinaParser.TriggerWorkerContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#workerReply}.
	 * @param ctx the parse tree
	 */
	void enterWorkerReply(BallerinaParser.WorkerReplyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#workerReply}.
	 * @param ctx the parse tree
	 */
	void exitWorkerReply(BallerinaParser.WorkerReplyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#commentStatement}.
	 * @param ctx the parse tree
	 */
	void enterCommentStatement(BallerinaParser.CommentStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#commentStatement}.
	 * @param ctx the parse tree
	 */
	void exitCommentStatement(BallerinaParser.CommentStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#actionInvocationStatement}.
	 * @param ctx the parse tree
	 */
	void enterActionInvocationStatement(BallerinaParser.ActionInvocationStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#actionInvocationStatement}.
	 * @param ctx the parse tree
	 */
	void exitActionInvocationStatement(BallerinaParser.ActionInvocationStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#variableAccessor}.
	 * @param ctx the parse tree
	 */
	void enterVariableAccessor(BallerinaParser.VariableAccessorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#variableAccessor}.
	 * @param ctx the parse tree
	 */
	void exitVariableAccessor(BallerinaParser.VariableAccessorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeInitializeExpression}.
	 * @param ctx the parse tree
	 */
	void enterTypeInitializeExpression(BallerinaParser.TypeInitializeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeInitializeExpression}.
	 * @param ctx the parse tree
	 */
	void exitTypeInitializeExpression(BallerinaParser.TypeInitializeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#assignmentRHSExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentRHSExpression(BallerinaParser.AssignmentRHSExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#assignmentRHSExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentRHSExpression(BallerinaParser.AssignmentRHSExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentList(BallerinaParser.ArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentList(BallerinaParser.ArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(BallerinaParser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(BallerinaParser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionInvocationStatement}.
	 * @param ctx the parse tree
	 */
	void enterFunctionInvocationStatement(BallerinaParser.FunctionInvocationStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionInvocationStatement}.
	 * @param ctx the parse tree
	 */
	void exitFunctionInvocationStatement(BallerinaParser.FunctionInvocationStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionInvocationExpression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionInvocationExpression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#functionName}.
	 * @param ctx the parse tree
	 */
	void enterFunctionName(BallerinaParser.FunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#functionName}.
	 * @param ctx the parse tree
	 */
	void exitFunctionName(BallerinaParser.FunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#actionInvocation}.
	 * @param ctx the parse tree
	 */
	void enterActionInvocation(BallerinaParser.ActionInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#actionInvocation}.
	 * @param ctx the parse tree
	 */
	void exitActionInvocation(BallerinaParser.ActionInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#backtickString}.
	 * @param ctx the parse tree
	 */
	void enterBacktickString(BallerinaParser.BacktickStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#backtickString}.
	 * @param ctx the parse tree
	 */
	void exitBacktickString(BallerinaParser.BacktickStringContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryOrExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryOrExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code argumentListExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterArgumentListExpression(BallerinaParser.ArgumentListExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code argumentListExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitArgumentListExpression(BallerinaParser.ArgumentListExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryComparisonExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryComparisonExpression(BallerinaParser.BinaryComparisonExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryComparisonExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryComparisonExpression(BallerinaParser.BinaryComparisonExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code templateExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTemplateExpression(BallerinaParser.TemplateExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code templateExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTemplateExpression(BallerinaParser.TemplateExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryPlusMinusExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryPlusMinusExpression(BallerinaParser.BinaryPlusMinusExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryPlusMinusExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryPlusMinusExpression(BallerinaParser.BinaryPlusMinusExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code preSingleDualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPreSingleDualExpression(BallerinaParser.PreSingleDualExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code preSingleDualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPreSingleDualExpression(BallerinaParser.PreSingleDualExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryEqualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryEqualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code inlineFunctionInovcationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterInlineFunctionInovcationExpression(BallerinaParser.InlineFunctionInovcationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code inlineFunctionInovcationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitInlineFunctionInovcationExpression(BallerinaParser.InlineFunctionInovcationExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code accessArrayElementExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAccessArrayElementExpression(BallerinaParser.AccessArrayElementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code accessArrayElementExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAccessArrayElementExpression(BallerinaParser.AccessArrayElementExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryMulDivPercentExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryMulDivPercentExpression(BallerinaParser.BinaryMulDivPercentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryMulDivPercentExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryMulDivPercentExpression(BallerinaParser.BinaryMulDivPercentExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code accessMemberDotExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAccessMemberDotExpression(BallerinaParser.AccessMemberDotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code accessMemberDotExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAccessMemberDotExpression(BallerinaParser.AccessMemberDotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeCastingExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeCastingExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTypeCastingExpression(BallerinaParser.TypeCastingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryAndExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryAndExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryAndExpression(BallerinaParser.BinaryAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code literalExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLiteralExpression(BallerinaParser.LiteralExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code literalExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLiteralExpression(BallerinaParser.LiteralExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mapInitializerExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMapInitializerExpression(BallerinaParser.MapInitializerExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mapInitializerExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMapInitializerExpression(BallerinaParser.MapInitializerExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(BallerinaParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(BallerinaParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(BallerinaParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(BallerinaParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#mapInitKeyValue}.
	 * @param ctx the parse tree
	 */
	void enterMapInitKeyValue(BallerinaParser.MapInitKeyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#mapInitKeyValue}.
	 * @param ctx the parse tree
	 */
	void exitMapInitKeyValue(BallerinaParser.MapInitKeyValueContext ctx);
}