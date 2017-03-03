// Generated from Ballerina.g4 by ANTLR 4.5.3
// jshint ignore: start
var antlr4 = require('antlr4/index');

// This class defines a complete generic visitor for a parse tree produced by BallerinaParser.

function BallerinaVisitor() {
	antlr4.tree.ParseTreeVisitor.call(this);
	return this;
}

BallerinaVisitor.prototype = Object.create(antlr4.tree.ParseTreeVisitor.prototype);
BallerinaVisitor.prototype.constructor = BallerinaVisitor;

// Visit a parse tree produced by BallerinaParser#compilationUnit.
BallerinaVisitor.prototype.visitCompilationUnit = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#packageDeclaration.
BallerinaVisitor.prototype.visitPackageDeclaration = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#importDeclaration.
BallerinaVisitor.prototype.visitImportDeclaration = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#serviceDefinition.
BallerinaVisitor.prototype.visitServiceDefinition = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#serviceBody.
BallerinaVisitor.prototype.visitServiceBody = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#serviceBodyDeclaration.
BallerinaVisitor.prototype.visitServiceBodyDeclaration = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#resourceDefinition.
BallerinaVisitor.prototype.visitResourceDefinition = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#functionDefinition.
BallerinaVisitor.prototype.visitFunctionDefinition = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#nativeFunction.
BallerinaVisitor.prototype.visitNativeFunction = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#balFunction.
BallerinaVisitor.prototype.visitBalFunction = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#functionBody.
BallerinaVisitor.prototype.visitFunctionBody = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#connectorDefinition.
BallerinaVisitor.prototype.visitConnectorDefinition = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#nativeConnector.
BallerinaVisitor.prototype.visitNativeConnector = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#nativeConnectorBody.
BallerinaVisitor.prototype.visitNativeConnectorBody = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#connector.
BallerinaVisitor.prototype.visitConnector = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#connectorBody.
BallerinaVisitor.prototype.visitConnectorBody = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#nativeAction.
BallerinaVisitor.prototype.visitNativeAction = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#action.
BallerinaVisitor.prototype.visitAction = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#structDefinition.
BallerinaVisitor.prototype.visitStructDefinition = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#structDefinitionBody.
BallerinaVisitor.prototype.visitStructDefinitionBody = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#typeMapperDefinition.
BallerinaVisitor.prototype.visitTypeMapperDefinition = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#nativeTypeMapper.
BallerinaVisitor.prototype.visitNativeTypeMapper = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#typeMapper.
BallerinaVisitor.prototype.visitTypeMapper = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#typeMapperInput.
BallerinaVisitor.prototype.visitTypeMapperInput = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#typeMapperBody.
BallerinaVisitor.prototype.visitTypeMapperBody = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#constantDefinition.
BallerinaVisitor.prototype.visitConstantDefinition = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#workerDeclaration.
BallerinaVisitor.prototype.visitWorkerDeclaration = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#returnParameters.
BallerinaVisitor.prototype.visitReturnParameters = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#namedParameterList.
BallerinaVisitor.prototype.visitNamedParameterList = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#namedParameter.
BallerinaVisitor.prototype.visitNamedParameter = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#returnTypeList.
BallerinaVisitor.prototype.visitReturnTypeList = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#qualifiedTypeName.
BallerinaVisitor.prototype.visitQualifiedTypeName = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#typeMapperType.
BallerinaVisitor.prototype.visitTypeMapperType = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#unqualifiedTypeName.
BallerinaVisitor.prototype.visitUnqualifiedTypeName = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#simpleType.
BallerinaVisitor.prototype.visitSimpleType = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#simpleTypeArray.
BallerinaVisitor.prototype.visitSimpleTypeArray = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#simpleTypeIterate.
BallerinaVisitor.prototype.visitSimpleTypeIterate = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#withFullSchemaType.
BallerinaVisitor.prototype.visitWithFullSchemaType = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#withFullSchemaTypeArray.
BallerinaVisitor.prototype.visitWithFullSchemaTypeArray = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#withFullSchemaTypeIterate.
BallerinaVisitor.prototype.visitWithFullSchemaTypeIterate = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#withScheamURLType.
BallerinaVisitor.prototype.visitWithScheamURLType = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#withSchemaURLTypeArray.
BallerinaVisitor.prototype.visitWithSchemaURLTypeArray = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#withSchemaURLTypeIterate.
BallerinaVisitor.prototype.visitWithSchemaURLTypeIterate = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#withSchemaIdType.
BallerinaVisitor.prototype.visitWithSchemaIdType = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#withScheamIdTypeArray.
BallerinaVisitor.prototype.visitWithScheamIdTypeArray = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#withScheamIdTypeIterate.
BallerinaVisitor.prototype.visitWithScheamIdTypeIterate = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#typeName.
BallerinaVisitor.prototype.visitTypeName = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#parameterList.
BallerinaVisitor.prototype.visitParameterList = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#parameter.
BallerinaVisitor.prototype.visitParameter = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#packageName.
BallerinaVisitor.prototype.visitPackageName = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#literalValue.
BallerinaVisitor.prototype.visitLiteralValue = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#annotation.
BallerinaVisitor.prototype.visitAnnotation = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#annotationName.
BallerinaVisitor.prototype.visitAnnotationName = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#elementValuePairs.
BallerinaVisitor.prototype.visitElementValuePairs = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#elementValuePair.
BallerinaVisitor.prototype.visitElementValuePair = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#elementValue.
BallerinaVisitor.prototype.visitElementValue = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#elementValueArrayInitializer.
BallerinaVisitor.prototype.visitElementValueArrayInitializer = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#statement.
BallerinaVisitor.prototype.visitStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#variableDefinitionStatement.
BallerinaVisitor.prototype.visitVariableDefinitionStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#assignmentStatement.
BallerinaVisitor.prototype.visitAssignmentStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#variableReferenceList.
BallerinaVisitor.prototype.visitVariableReferenceList = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#ifElseStatement.
BallerinaVisitor.prototype.visitIfElseStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#ifClause.
BallerinaVisitor.prototype.visitIfClause = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#elseIfClause.
BallerinaVisitor.prototype.visitElseIfClause = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#elseClause.
BallerinaVisitor.prototype.visitElseClause = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#iterateStatement.
BallerinaVisitor.prototype.visitIterateStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#whileStatement.
BallerinaVisitor.prototype.visitWhileStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#breakStatement.
BallerinaVisitor.prototype.visitBreakStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#forkJoinStatement.
BallerinaVisitor.prototype.visitForkJoinStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#joinClause.
BallerinaVisitor.prototype.visitJoinClause = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#anyJoinCondition.
BallerinaVisitor.prototype.visitAnyJoinCondition = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#allJoinCondition.
BallerinaVisitor.prototype.visitAllJoinCondition = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#timeoutClause.
BallerinaVisitor.prototype.visitTimeoutClause = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#tryCatchStatement.
BallerinaVisitor.prototype.visitTryCatchStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#catchClause.
BallerinaVisitor.prototype.visitCatchClause = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#throwStatement.
BallerinaVisitor.prototype.visitThrowStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#returnStatement.
BallerinaVisitor.prototype.visitReturnStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#replyStatement.
BallerinaVisitor.prototype.visitReplyStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#workerInteractionStatement.
BallerinaVisitor.prototype.visitWorkerInteractionStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#triggerWorker.
BallerinaVisitor.prototype.visitTriggerWorker = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#workerReply.
BallerinaVisitor.prototype.visitWorkerReply = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#commentStatement.
BallerinaVisitor.prototype.visitCommentStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#actionInvocationStatement.
BallerinaVisitor.prototype.visitActionInvocationStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#structFieldIdentifier.
BallerinaVisitor.prototype.visitStructFieldIdentifier = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#simpleVariableIdentifier.
BallerinaVisitor.prototype.visitSimpleVariableIdentifier = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#mapArrayVariableIdentifier.
BallerinaVisitor.prototype.visitMapArrayVariableIdentifier = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#argumentList.
BallerinaVisitor.prototype.visitArgumentList = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#expressionList.
BallerinaVisitor.prototype.visitExpressionList = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#functionInvocationStatement.
BallerinaVisitor.prototype.visitFunctionInvocationStatement = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#functionName.
BallerinaVisitor.prototype.visitFunctionName = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#actionInvocation.
BallerinaVisitor.prototype.visitActionInvocation = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#callableUnitName.
BallerinaVisitor.prototype.visitCallableUnitName = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#backtickString.
BallerinaVisitor.prototype.visitBacktickString = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#connectorInitExpression.
BallerinaVisitor.prototype.visitConnectorInitExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#binaryDivMulModExpression.
BallerinaVisitor.prototype.visitBinaryDivMulModExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#binaryOrExpression.
BallerinaVisitor.prototype.visitBinaryOrExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#templateExpression.
BallerinaVisitor.prototype.visitTemplateExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#functionInvocationExpression.
BallerinaVisitor.prototype.visitFunctionInvocationExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#binaryEqualExpression.
BallerinaVisitor.prototype.visitBinaryEqualExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#refTypeInitExpression.
BallerinaVisitor.prototype.visitRefTypeInitExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#bracedExpression.
BallerinaVisitor.prototype.visitBracedExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#variableReferenceExpression.
BallerinaVisitor.prototype.visitVariableReferenceExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#actionInvocationExpression.
BallerinaVisitor.prototype.visitActionInvocationExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#typeCastingExpression.
BallerinaVisitor.prototype.visitTypeCastingExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#binaryAndExpression.
BallerinaVisitor.prototype.visitBinaryAndExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#binaryAddSubExpression.
BallerinaVisitor.prototype.visitBinaryAddSubExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#arrayInitExpression.
BallerinaVisitor.prototype.visitArrayInitExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#binaryCompareExpression.
BallerinaVisitor.prototype.visitBinaryCompareExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#literalExpression.
BallerinaVisitor.prototype.visitLiteralExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#unaryExpression.
BallerinaVisitor.prototype.visitUnaryExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#binaryPowExpression.
BallerinaVisitor.prototype.visitBinaryPowExpression = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#mapStructInitKeyValueList.
BallerinaVisitor.prototype.visitMapStructInitKeyValueList = function(ctx) {
};


// Visit a parse tree produced by BallerinaParser#mapStructInitKeyValue.
BallerinaVisitor.prototype.visitMapStructInitKeyValue = function(ctx) {
};



exports.BallerinaVisitor = BallerinaVisitor;