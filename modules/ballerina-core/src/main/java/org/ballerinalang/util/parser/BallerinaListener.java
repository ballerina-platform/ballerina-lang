// Generated from Ballerina.g4 by ANTLR 4.5.3
package org.ballerinalang.util.parser;
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
	 * Enter a parse tree produced by {@link BallerinaParser#nativeFunction}.
	 * @param ctx the parse tree
	 */
	void enterNativeFunction(BallerinaParser.NativeFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#nativeFunction}.
	 * @param ctx the parse tree
	 */
	void exitNativeFunction(BallerinaParser.NativeFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(BallerinaParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(BallerinaParser.FunctionContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#nativeConnector}.
	 * @param ctx the parse tree
	 */
	void enterNativeConnector(BallerinaParser.NativeConnectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#nativeConnector}.
	 * @param ctx the parse tree
	 */
	void exitNativeConnector(BallerinaParser.NativeConnectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#nativeConnectorBody}.
	 * @param ctx the parse tree
	 */
	void enterNativeConnectorBody(BallerinaParser.NativeConnectorBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#nativeConnectorBody}.
	 * @param ctx the parse tree
	 */
	void exitNativeConnectorBody(BallerinaParser.NativeConnectorBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#connector}.
	 * @param ctx the parse tree
	 */
	void enterConnector(BallerinaParser.ConnectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#connector}.
	 * @param ctx the parse tree
	 */
	void exitConnector(BallerinaParser.ConnectorContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#nativeAction}.
	 * @param ctx the parse tree
	 */
	void enterNativeAction(BallerinaParser.NativeActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#nativeAction}.
	 * @param ctx the parse tree
	 */
	void exitNativeAction(BallerinaParser.NativeActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#action}.
	 * @param ctx the parse tree
	 */
	void enterAction(BallerinaParser.ActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#action}.
	 * @param ctx the parse tree
	 */
	void exitAction(BallerinaParser.ActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#structDefinition}.
	 * @param ctx the parse tree
	 */
	void enterStructDefinition(BallerinaParser.StructDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#structDefinition}.
	 * @param ctx the parse tree
	 */
	void exitStructDefinition(BallerinaParser.StructDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#structDefinitionBody}.
	 * @param ctx the parse tree
	 */
	void enterStructDefinitionBody(BallerinaParser.StructDefinitionBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#structDefinitionBody}.
	 * @param ctx the parse tree
	 */
	void exitStructDefinitionBody(BallerinaParser.StructDefinitionBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeMapperDefinition}.
	 * @param ctx the parse tree
	 */
	void enterTypeMapperDefinition(BallerinaParser.TypeMapperDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeMapperDefinition}.
	 * @param ctx the parse tree
	 */
	void exitTypeMapperDefinition(BallerinaParser.TypeMapperDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#nativeTypeMapper}.
	 * @param ctx the parse tree
	 */
	void enterNativeTypeMapper(BallerinaParser.NativeTypeMapperContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#nativeTypeMapper}.
	 * @param ctx the parse tree
	 */
	void exitNativeTypeMapper(BallerinaParser.NativeTypeMapperContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeMapper}.
	 * @param ctx the parse tree
	 */
	void enterTypeMapper(BallerinaParser.TypeMapperContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeMapper}.
	 * @param ctx the parse tree
	 */
	void exitTypeMapper(BallerinaParser.TypeMapperContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeMapperInput}.
	 * @param ctx the parse tree
	 */
	void enterTypeMapperInput(BallerinaParser.TypeMapperInputContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeMapperInput}.
	 * @param ctx the parse tree
	 */
	void exitTypeMapperInput(BallerinaParser.TypeMapperInputContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#typeMapperBody}.
	 * @param ctx the parse tree
	 */
	void enterTypeMapperBody(BallerinaParser.TypeMapperBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeMapperBody}.
	 * @param ctx the parse tree
	 */
	void exitTypeMapperBody(BallerinaParser.TypeMapperBodyContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#returnParameters}.
	 * @param ctx the parse tree
	 */
	void enterReturnParameters(BallerinaParser.ReturnParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#returnParameters}.
	 * @param ctx the parse tree
	 */
	void exitReturnParameters(BallerinaParser.ReturnParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#namedParameterList}.
	 * @param ctx the parse tree
	 */
	void enterNamedParameterList(BallerinaParser.NamedParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#namedParameterList}.
	 * @param ctx the parse tree
	 */
	void exitNamedParameterList(BallerinaParser.NamedParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#namedParameter}.
	 * @param ctx the parse tree
	 */
	void enterNamedParameter(BallerinaParser.NamedParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#namedParameter}.
	 * @param ctx the parse tree
	 */
	void exitNamedParameter(BallerinaParser.NamedParameterContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#typeMapperType}.
	 * @param ctx the parse tree
	 */
	void enterTypeMapperType(BallerinaParser.TypeMapperTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#typeMapperType}.
	 * @param ctx the parse tree
	 */
	void exitTypeMapperType(BallerinaParser.TypeMapperTypeContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#simpleType}.
	 * @param ctx the parse tree
	 */
	void enterSimpleType(BallerinaParser.SimpleTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#simpleType}.
	 * @param ctx the parse tree
	 */
	void exitSimpleType(BallerinaParser.SimpleTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#simpleTypeArray}.
	 * @param ctx the parse tree
	 */
	void enterSimpleTypeArray(BallerinaParser.SimpleTypeArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#simpleTypeArray}.
	 * @param ctx the parse tree
	 */
	void exitSimpleTypeArray(BallerinaParser.SimpleTypeArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#simpleTypeIterate}.
	 * @param ctx the parse tree
	 */
	void enterSimpleTypeIterate(BallerinaParser.SimpleTypeIterateContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#simpleTypeIterate}.
	 * @param ctx the parse tree
	 */
	void exitSimpleTypeIterate(BallerinaParser.SimpleTypeIterateContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#withFullSchemaType}.
	 * @param ctx the parse tree
	 */
	void enterWithFullSchemaType(BallerinaParser.WithFullSchemaTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#withFullSchemaType}.
	 * @param ctx the parse tree
	 */
	void exitWithFullSchemaType(BallerinaParser.WithFullSchemaTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#withFullSchemaTypeArray}.
	 * @param ctx the parse tree
	 */
	void enterWithFullSchemaTypeArray(BallerinaParser.WithFullSchemaTypeArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#withFullSchemaTypeArray}.
	 * @param ctx the parse tree
	 */
	void exitWithFullSchemaTypeArray(BallerinaParser.WithFullSchemaTypeArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#withFullSchemaTypeIterate}.
	 * @param ctx the parse tree
	 */
	void enterWithFullSchemaTypeIterate(BallerinaParser.WithFullSchemaTypeIterateContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#withFullSchemaTypeIterate}.
	 * @param ctx the parse tree
	 */
	void exitWithFullSchemaTypeIterate(BallerinaParser.WithFullSchemaTypeIterateContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#withScheamURLType}.
	 * @param ctx the parse tree
	 */
	void enterWithScheamURLType(BallerinaParser.WithScheamURLTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#withScheamURLType}.
	 * @param ctx the parse tree
	 */
	void exitWithScheamURLType(BallerinaParser.WithScheamURLTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#withSchemaURLTypeArray}.
	 * @param ctx the parse tree
	 */
	void enterWithSchemaURLTypeArray(BallerinaParser.WithSchemaURLTypeArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#withSchemaURLTypeArray}.
	 * @param ctx the parse tree
	 */
	void exitWithSchemaURLTypeArray(BallerinaParser.WithSchemaURLTypeArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#withSchemaURLTypeIterate}.
	 * @param ctx the parse tree
	 */
	void enterWithSchemaURLTypeIterate(BallerinaParser.WithSchemaURLTypeIterateContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#withSchemaURLTypeIterate}.
	 * @param ctx the parse tree
	 */
	void exitWithSchemaURLTypeIterate(BallerinaParser.WithSchemaURLTypeIterateContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#withSchemaIdType}.
	 * @param ctx the parse tree
	 */
	void enterWithSchemaIdType(BallerinaParser.WithSchemaIdTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#withSchemaIdType}.
	 * @param ctx the parse tree
	 */
	void exitWithSchemaIdType(BallerinaParser.WithSchemaIdTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#withScheamIdTypeArray}.
	 * @param ctx the parse tree
	 */
	void enterWithScheamIdTypeArray(BallerinaParser.WithScheamIdTypeArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#withScheamIdTypeArray}.
	 * @param ctx the parse tree
	 */
	void exitWithScheamIdTypeArray(BallerinaParser.WithScheamIdTypeArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#withScheamIdTypeIterate}.
	 * @param ctx the parse tree
	 */
	void enterWithScheamIdTypeIterate(BallerinaParser.WithScheamIdTypeIterateContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#withScheamIdTypeIterate}.
	 * @param ctx the parse tree
	 */
	void exitWithScheamIdTypeIterate(BallerinaParser.WithScheamIdTypeIterateContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#variableDefinitionStatement}.
	 * @param ctx the parse tree
	 */
	void enterVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#variableDefinitionStatement}.
	 * @param ctx the parse tree
	 */
	void exitVariableDefinitionStatement(BallerinaParser.VariableDefinitionStatementContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#variableReferenceList}.
	 * @param ctx the parse tree
	 */
	void enterVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#variableReferenceList}.
	 * @param ctx the parse tree
	 */
	void exitVariableReferenceList(BallerinaParser.VariableReferenceListContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#ifClause}.
	 * @param ctx the parse tree
	 */
	void enterIfClause(BallerinaParser.IfClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#ifClause}.
	 * @param ctx the parse tree
	 */
	void exitIfClause(BallerinaParser.IfClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#elseIfClause}.
	 * @param ctx the parse tree
	 */
	void enterElseIfClause(BallerinaParser.ElseIfClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#elseIfClause}.
	 * @param ctx the parse tree
	 */
	void exitElseIfClause(BallerinaParser.ElseIfClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void enterElseClause(BallerinaParser.ElseClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#elseClause}.
	 * @param ctx the parse tree
	 */
	void exitElseClause(BallerinaParser.ElseClauseContext ctx);
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
	 * Enter a parse tree produced by the {@code anyJoinCondition}
	 * labeled alternative in {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 */
	void enterAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code anyJoinCondition}
	 * labeled alternative in {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 */
	void exitAnyJoinCondition(BallerinaParser.AnyJoinConditionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code allJoinCondition}
	 * labeled alternative in {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 */
	void enterAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code allJoinCondition}
	 * labeled alternative in {@link BallerinaParser#joinConditions}.
	 * @param ctx the parse tree
	 */
	void exitAllJoinCondition(BallerinaParser.AllJoinConditionContext ctx);
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
	 * Enter a parse tree produced by the {@code structFieldIdentifier}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterStructFieldIdentifier(BallerinaParser.StructFieldIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code structFieldIdentifier}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitStructFieldIdentifier(BallerinaParser.StructFieldIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleVariableIdentifier}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterSimpleVariableIdentifier(BallerinaParser.SimpleVariableIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleVariableIdentifier}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitSimpleVariableIdentifier(BallerinaParser.SimpleVariableIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mapArrayVariableIdentifier}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void enterMapArrayVariableIdentifier(BallerinaParser.MapArrayVariableIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mapArrayVariableIdentifier}
	 * labeled alternative in {@link BallerinaParser#variableReference}.
	 * @param ctx the parse tree
	 */
	void exitMapArrayVariableIdentifier(BallerinaParser.MapArrayVariableIdentifierContext ctx);
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
	 * Enter a parse tree produced by {@link BallerinaParser#callableUnitName}.
	 * @param ctx the parse tree
	 */
	void enterCallableUnitName(BallerinaParser.CallableUnitNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#callableUnitName}.
	 * @param ctx the parse tree
	 */
	void exitCallableUnitName(BallerinaParser.CallableUnitNameContext ctx);
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
	 * Enter a parse tree produced by the {@code connectorInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterConnectorInitExpression(BallerinaParser.ConnectorInitExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code connectorInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitConnectorInitExpression(BallerinaParser.ConnectorInitExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryDivMulModExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryDivMulModExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryDivMulModExpression(BallerinaParser.BinaryDivMulModExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code functionInvocationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionInvocationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionInvocationExpression(BallerinaParser.FunctionInvocationExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code refTypeInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterRefTypeInitExpression(BallerinaParser.RefTypeInitExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code refTypeInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitRefTypeInitExpression(BallerinaParser.RefTypeInitExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bracedExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBracedExpression(BallerinaParser.BracedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bracedExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBracedExpression(BallerinaParser.BracedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variableReferenceExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code variableReferenceExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitVariableReferenceExpression(BallerinaParser.VariableReferenceExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code actionInvocationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterActionInvocationExpression(BallerinaParser.ActionInvocationExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code actionInvocationExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitActionInvocationExpression(BallerinaParser.ActionInvocationExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code binaryAddSubExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryAddSubExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryAddSubExpression(BallerinaParser.BinaryAddSubExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrayInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterArrayInitExpression(BallerinaParser.ArrayInitExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrayInitExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitArrayInitExpression(BallerinaParser.ArrayInitExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryCompareExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryCompareExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryCompareExpression(BallerinaParser.BinaryCompareExpressionContext ctx);
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
	 * Enter a parse tree produced by the {@code unaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(BallerinaParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(BallerinaParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryPowExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryPowExpression}
	 * labeled alternative in {@link BallerinaParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryPowExpression(BallerinaParser.BinaryPowExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#mapStructInitKeyValueList}.
	 * @param ctx the parse tree
	 */
	void enterMapStructInitKeyValueList(BallerinaParser.MapStructInitKeyValueListContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#mapStructInitKeyValueList}.
	 * @param ctx the parse tree
	 */
	void exitMapStructInitKeyValueList(BallerinaParser.MapStructInitKeyValueListContext ctx);
	/**
	 * Enter a parse tree produced by {@link BallerinaParser#mapStructInitKeyValue}.
	 * @param ctx the parse tree
	 */
	void enterMapStructInitKeyValue(BallerinaParser.MapStructInitKeyValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link BallerinaParser#mapStructInitKeyValue}.
	 * @param ctx the parse tree
	 */
	void exitMapStructInitKeyValue(BallerinaParser.MapStructInitKeyValueContext ctx);
}