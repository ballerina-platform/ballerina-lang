// Generated from Ballerina.g4 by ANTLR 4.5.3
package org.wso2.ballerina.core.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link BallerinaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface BallerinaVisitor<T> extends ParseTreeVisitor<T> {
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
	 * Visit a parse tree produced by {@link BallerinaParser#importDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportDeclaration(BallerinaParser.ImportDeclarationContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#serviceBodyDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitServiceBodyDeclaration(BallerinaParser.ServiceBodyDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#resourceDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResourceDefinition(BallerinaParser.ResourceDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#functionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#functionBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionBody(BallerinaParser.FunctionBodyContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#connectorDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConnectorDeclaration(BallerinaParser.ConnectorDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDefinition(BallerinaParser.TypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeDefinitionBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeDefinitionBody(BallerinaParser.TypeDefinitionBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeConvertorDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeConvertorDefinition(BallerinaParser.TypeConvertorDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeConvertorBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeConvertorBody(BallerinaParser.TypeConvertorBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#constantDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantDefinition(BallerinaParser.ConstantDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(BallerinaParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#workerDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorkerDeclaration(BallerinaParser.WorkerDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#returnTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnTypeList(BallerinaParser.ReturnTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeNameList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeNameList(BallerinaParser.TypeNameListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#qualifiedTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedTypeName(BallerinaParser.QualifiedTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#unqualifiedTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnqualifiedTypeName(BallerinaParser.UnqualifiedTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeNameWithOptionalSchema}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeNameWithOptionalSchema(BallerinaParser.TypeNameWithOptionalSchemaContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(BallerinaParser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#qualifiedReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedReference(BallerinaParser.QualifiedReferenceContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#packageName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageName(BallerinaParser.PackageNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#literalValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralValue(BallerinaParser.LiteralValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotation(BallerinaParser.AnnotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationName(BallerinaParser.AnnotationNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#elementValuePairs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValuePairs(BallerinaParser.ElementValuePairsContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#elementValuePair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValuePair(BallerinaParser.ElementValuePairContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#elementValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValue(BallerinaParser.ElementValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#elementValueArrayInitializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElementValueArrayInitializer(BallerinaParser.ElementValueArrayInitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(BallerinaParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#assignmentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#ifElseStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfElseStatement(BallerinaParser.IfElseStatementContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#iterateStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIterateStatement(BallerinaParser.IterateStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(BallerinaParser.WhileStatementContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinConditions(BallerinaParser.JoinConditionsContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#catchClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCatchClause(BallerinaParser.CatchClauseContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#replyStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReplyStatement(BallerinaParser.ReplyStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#workerInteractionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorkerInteractionStatement(BallerinaParser.WorkerInteractionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#triggerWorker}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriggerWorker(BallerinaParser.TriggerWorkerContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#workerReply}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWorkerReply(BallerinaParser.WorkerReplyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#commentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommentStatement(BallerinaParser.CommentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#actionInvocationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionInvocationStatement(BallerinaParser.ActionInvocationStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code simpleVariableIdentifier}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleVariableIdentifier(BallerinaParser.SimpleVariableIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mapArrayVariableIdentifier}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapArrayVariableIdentifier(BallerinaParser.MapArrayVariableIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code structFieldIdentifier}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructFieldIdentifier(BallerinaParser.StructFieldIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(BallerinaParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(BallerinaParser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#functionInvocationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionInvocationStatement(BallerinaParser.FunctionInvocationStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#functionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionName(BallerinaParser.FunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#actionInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionInvocation(BallerinaParser.ActionInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#backtickString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBacktickString(BallerinaParser.BacktickStringContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryOrExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryOrExpression(BallerinaParser.BinaryOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryGTExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryGTExpression(BallerinaParser.BinaryGTExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeInitializeExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeInitializeExpression(BallerinaParser.TypeInitializeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code templateExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplateExpression(BallerinaParser.TemplateExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryLEExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryLEExpression(BallerinaParser.BinaryLEExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionInvocationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryGEExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryGEExpression(BallerinaParser.BinaryGEExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryEqualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx);
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
	 * Visit a parse tree produced by the {@code actionInvocationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActionInvocationExpression(BallerinaParser.ActionInvocationExpressionContext ctx);
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
	 * Visit a parse tree produced by the {@code binaryNotEqualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryNotEqualExpression(BallerinaParser.BinaryNotEqualExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryDivitionExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryDivitionExpression(BallerinaParser.BinaryDivitionExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryModExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryModExpression(BallerinaParser.BinaryModExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binarySubExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinarySubExpression(BallerinaParser.BinarySubExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryMultiplicationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryMultiplicationExpression(BallerinaParser.BinaryMultiplicationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code literalExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralExpression(BallerinaParser.LiteralExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryLTExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryLTExpression(BallerinaParser.BinaryLTExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mapInitializerExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapInitializerExpression(BallerinaParser.MapInitializerExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryPowExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryAddExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryAddExpression(BallerinaParser.BinaryAddExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#mapInitKeyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapInitKeyValue(BallerinaParser.MapInitKeyValueContext ctx);
}