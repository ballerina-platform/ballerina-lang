// Generated from /home/shan/Documents/WSO2/Highlighters/plugin-intellij/src/main/java/org/ballerinalang/plugins/idea/grammar/Ballerina.g4 by ANTLR 4.6
package org.ballerinalang.plugins.idea.grammar;
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
	 * Visit a parse tree produced by {@link BallerinaParser#structDefinitionBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructDefinitionBody(BallerinaParser.StructDefinitionBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#structField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructField(BallerinaParser.StructFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationDefinition(BallerinaParser.AnnotationDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#attachmentPoint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttachmentPoint(BallerinaParser.AttachmentPointContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#annotationBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnnotationBody(BallerinaParser.AnnotationBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeMapperDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeMapperDefinition(BallerinaParser.TypeMapperDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#nativeTypeMapper}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNativeTypeMapper(BallerinaParser.NativeTypeMapperContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeMapper}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeMapper(BallerinaParser.TypeMapperContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeMapperInput}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeMapperInput(BallerinaParser.TypeMapperInputContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeMapperBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeMapperBody(BallerinaParser.TypeMapperBodyContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#returnParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnParameters(BallerinaParser.ReturnParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#namedParameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedParameterList(BallerinaParser.NamedParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#namedParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedParameter(BallerinaParser.NamedParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#returnTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnTypeList(BallerinaParser.ReturnTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#qualifiedTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedTypeName(BallerinaParser.QualifiedTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeMapperType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeMapperType(BallerinaParser.TypeMapperTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#unqualifiedTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnqualifiedTypeName(BallerinaParser.UnqualifiedTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#simpleType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleType(BallerinaParser.SimpleTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#simpleTypeArray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleTypeArray(BallerinaParser.SimpleTypeArrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#simpleTypeIterate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleTypeIterate(BallerinaParser.SimpleTypeIterateContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#withFullSchemaType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithFullSchemaType(BallerinaParser.WithFullSchemaTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#withFullSchemaTypeArray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithFullSchemaTypeArray(BallerinaParser.WithFullSchemaTypeArrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#withFullSchemaTypeIterate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithFullSchemaTypeIterate(BallerinaParser.WithFullSchemaTypeIterateContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#withScheamURLType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithScheamURLType(BallerinaParser.WithScheamURLTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#withSchemaURLTypeArray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithSchemaURLTypeArray(BallerinaParser.WithSchemaURLTypeArrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#withSchemaURLTypeIterate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithSchemaURLTypeIterate(BallerinaParser.WithSchemaURLTypeIterateContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#withSchemaIdType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithSchemaIdType(BallerinaParser.WithSchemaIdTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#withScheamIdTypeArray}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithScheamIdTypeArray(BallerinaParser.WithScheamIdTypeArrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#withScheamIdTypeIterate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithScheamIdTypeIterate(BallerinaParser.WithScheamIdTypeIterateContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(BallerinaParser.TypeNameContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#packagePath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackagePath(BallerinaParser.PackagePathContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#packageName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackageName(BallerinaParser.PackageNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(BallerinaParser.AliasContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#variableDefinitionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#assignmentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStatement(BallerinaParser.AssignmentStatementContext ctx);
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
	 * Visit a parse tree produced by the {@code structFieldIdentifier}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructFieldIdentifier(BallerinaParser.StructFieldIdentifierContext ctx);
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
	 * Visit a parse tree produced by {@link BallerinaParser#callableUnitName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallableUnitName(BallerinaParser.CallableUnitNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#backtickString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBacktickString(BallerinaParser.BacktickStringContext ctx);
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
	 * Visit a parse tree produced by the {@code templateExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplateExpression(BallerinaParser.TemplateExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionInvocationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryEqualExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryEqualExpression(BallerinaParser.BinaryEqualExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code refTypeInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefTypeInitExpression(BallerinaParser.RefTypeInitExpressionContext ctx);
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
	 * Visit a parse tree produced by the {@code binaryAddSubExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arrayInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayInitExpression(BallerinaParser.ArrayInitExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryCompareExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx);
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
	 * Visit a parse tree produced by the {@code binaryPowExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#mapStructInitKeyValueList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapStructInitKeyValueList(BallerinaParser.MapStructInitKeyValueListContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#mapStructInitKeyValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapStructInitKeyValue(BallerinaParser.MapStructInitKeyValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link BallerinaParser#nameReference}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNameReference(BallerinaParser.NameReferenceContext ctx);
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
}