// Generated from WUML.g4 by ANTLR 4.5.3
// jshint ignore: start
define(function(require, exports, module) {
	var antlr4 = require('../antlr4/index');

// This class defines a complete listener for a parse tree produced by WUMLParser.
	function WUMLListener() {
		antlr4.tree.ParseTreeListener.call(this);
		return this;
	}

	WUMLListener.prototype = Object.create(antlr4.tree.ParseTreeListener.prototype);
	WUMLListener.prototype.constructor = WUMLListener;

// Enter a parse tree produced by WUMLParser#sourceFile.
	WUMLListener.prototype.enterSourceFile = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#sourceFile.
	WUMLListener.prototype.exitSourceFile = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#definition.
	WUMLListener.prototype.enterDefinition = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#definition.
	WUMLListener.prototype.exitDefinition = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#constants.
	WUMLListener.prototype.enterConstants = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#constants.
	WUMLListener.prototype.exitConstants = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#resources.
	WUMLListener.prototype.enterResources = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#resources.
	WUMLListener.prototype.exitResources = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#packageDef.
	WUMLListener.prototype.enterPackageDef = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#packageDef.
	WUMLListener.prototype.exitPackageDef = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#path.
	WUMLListener.prototype.enterPath = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#path.
	WUMLListener.prototype.exitPath = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#source.
	WUMLListener.prototype.enterSource = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#source.
	WUMLListener.prototype.exitSource = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#api.
	WUMLListener.prototype.enterApi = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#api.
	WUMLListener.prototype.exitApi = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#resourcePath.
	WUMLListener.prototype.enterResourcePath = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#resourcePath.
	WUMLListener.prototype.exitResourcePath = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#getMethod.
	WUMLListener.prototype.enterGetMethod = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#getMethod.
	WUMLListener.prototype.exitGetMethod = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#postMethod.
	WUMLListener.prototype.enterPostMethod = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#postMethod.
	WUMLListener.prototype.exitPostMethod = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#putMethod.
	WUMLListener.prototype.enterPutMethod = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#putMethod.
	WUMLListener.prototype.exitPutMethod = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#deleteMethod.
	WUMLListener.prototype.enterDeleteMethod = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#deleteMethod.
	WUMLListener.prototype.exitDeleteMethod = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#headMethod.
	WUMLListener.prototype.enterHeadMethod = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#headMethod.
	WUMLListener.prototype.exitHeadMethod = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#prodAnt.
	WUMLListener.prototype.enterProdAnt = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#prodAnt.
	WUMLListener.prototype.exitProdAnt = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#conAnt.
	WUMLListener.prototype.enterConAnt = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#conAnt.
	WUMLListener.prototype.exitConAnt = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#antApiOperation.
	WUMLListener.prototype.enterAntApiOperation = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#antApiOperation.
	WUMLListener.prototype.exitAntApiOperation = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#antApiResponses.
	WUMLListener.prototype.enterAntApiResponses = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#antApiResponses.
	WUMLListener.prototype.exitAntApiResponses = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#antApiResponseSet.
	WUMLListener.prototype.enterAntApiResponseSet = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#antApiResponseSet.
	WUMLListener.prototype.exitAntApiResponseSet = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#antApiResponse.
	WUMLListener.prototype.enterAntApiResponse = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#antApiResponse.
	WUMLListener.prototype.exitAntApiResponse = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#elementValuePairs.
	WUMLListener.prototype.enterElementValuePairs = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#elementValuePairs.
	WUMLListener.prototype.exitElementValuePairs = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#sourceElementValuePairs.
	WUMLListener.prototype.enterSourceElementValuePairs = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#sourceElementValuePairs.
	WUMLListener.prototype.exitSourceElementValuePairs = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#apiElementValuePairs.
	WUMLListener.prototype.enterApiElementValuePairs = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#apiElementValuePairs.
	WUMLListener.prototype.exitApiElementValuePairs = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#protocol.
	WUMLListener.prototype.enterProtocol = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#protocol.
	WUMLListener.prototype.exitProtocol = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#host.
	WUMLListener.prototype.enterHost = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#host.
	WUMLListener.prototype.exitHost = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#port.
	WUMLListener.prototype.enterPort = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#port.
	WUMLListener.prototype.exitPort = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#tags.
	WUMLListener.prototype.enterTags = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#tags.
	WUMLListener.prototype.exitTags = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#tag.
	WUMLListener.prototype.enterTag = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#tag.
	WUMLListener.prototype.exitTag = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#descripton.
	WUMLListener.prototype.enterDescripton = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#descripton.
	WUMLListener.prototype.exitDescripton = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#producer.
	WUMLListener.prototype.enterProducer = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#producer.
	WUMLListener.prototype.exitProducer = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#constant.
	WUMLListener.prototype.enterConstant = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#constant.
	WUMLListener.prototype.exitConstant = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#elementValuePair.
	WUMLListener.prototype.enterElementValuePair = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#elementValuePair.
	WUMLListener.prototype.exitElementValuePair = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#elementValue.
	WUMLListener.prototype.enterElementValue = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#elementValue.
	WUMLListener.prototype.exitElementValue = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#resource.
	WUMLListener.prototype.enterResource = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#resource.
	WUMLListener.prototype.exitResource = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#httpMethods.
	WUMLListener.prototype.enterHttpMethods = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#httpMethods.
	WUMLListener.prototype.exitHttpMethods = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#qualifiedName.
	WUMLListener.prototype.enterQualifiedName = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#qualifiedName.
	WUMLListener.prototype.exitQualifiedName = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#resourceDeclaration.
	WUMLListener.prototype.enterResourceDeclaration = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#resourceDeclaration.
	WUMLListener.prototype.exitResourceDeclaration = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#resourceName.
	WUMLListener.prototype.enterResourceName = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#resourceName.
	WUMLListener.prototype.exitResourceName = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#block.
	WUMLListener.prototype.enterBlock = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#block.
	WUMLListener.prototype.exitBlock = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#blockStatement.
	WUMLListener.prototype.enterBlockStatement = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#blockStatement.
	WUMLListener.prototype.exitBlockStatement = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#tryCatchBlock.
	WUMLListener.prototype.enterTryCatchBlock = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#tryCatchBlock.
	WUMLListener.prototype.exitTryCatchBlock = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#tryClause.
	WUMLListener.prototype.enterTryClause = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#tryClause.
	WUMLListener.prototype.exitTryClause = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#catchClause.
	WUMLListener.prototype.enterCatchClause = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#catchClause.
	WUMLListener.prototype.exitCatchClause = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#exceptionHandler.
	WUMLListener.prototype.enterExceptionHandler = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#exceptionHandler.
	WUMLListener.prototype.exitExceptionHandler = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#exceptionType.
	WUMLListener.prototype.enterExceptionType = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#exceptionType.
	WUMLListener.prototype.exitExceptionType = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#ifElseBlock.
	WUMLListener.prototype.enterIfElseBlock = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#ifElseBlock.
	WUMLListener.prototype.exitIfElseBlock = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#ifBlock.
	WUMLListener.prototype.enterIfBlock = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#ifBlock.
	WUMLListener.prototype.exitIfBlock = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#elseBlock.
	WUMLListener.prototype.enterElseBlock = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#elseBlock.
	WUMLListener.prototype.exitElseBlock = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#localVariableDeclarationStatement.
	WUMLListener.prototype.enterLocalVariableDeclarationStatement = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#localVariableDeclarationStatement.
	WUMLListener.prototype.exitLocalVariableDeclarationStatement = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#localVaribaleInitializationStatement.
	WUMLListener.prototype.enterLocalVaribaleInitializationStatement = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#localVaribaleInitializationStatement.
	WUMLListener.prototype.exitLocalVaribaleInitializationStatement = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#localVaribaleAssignmentStatement.
	WUMLListener.prototype.enterLocalVaribaleAssignmentStatement = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#localVaribaleAssignmentStatement.
	WUMLListener.prototype.exitLocalVaribaleAssignmentStatement = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#logMediatorStatement.
	WUMLListener.prototype.enterLogMediatorStatement = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#logMediatorStatement.
	WUMLListener.prototype.exitLogMediatorStatement = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#newTypeObjectCreation.
	WUMLListener.prototype.enterNewTypeObjectCreation = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#newTypeObjectCreation.
	WUMLListener.prototype.exitNewTypeObjectCreation = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#mediatorCall.
	WUMLListener.prototype.enterMediatorCall = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#mediatorCall.
	WUMLListener.prototype.exitMediatorCall = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#invokeMediatorCall.
	WUMLListener.prototype.enterInvokeMediatorCall = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#invokeMediatorCall.
	WUMLListener.prototype.exitInvokeMediatorCall = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#sendToMediatorCall.
	WUMLListener.prototype.enterSendToMediatorCall = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#sendToMediatorCall.
	WUMLListener.prototype.exitSendToMediatorCall = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#dataMapMediatorCall.
	WUMLListener.prototype.enterDataMapMediatorCall = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#dataMapMediatorCall.
	WUMLListener.prototype.exitDataMapMediatorCall = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#receiveFromMediatorCall.
	WUMLListener.prototype.enterReceiveFromMediatorCall = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#receiveFromMediatorCall.
	WUMLListener.prototype.exitReceiveFromMediatorCall = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#customMediatorCall.
	WUMLListener.prototype.enterCustomMediatorCall = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#customMediatorCall.
	WUMLListener.prototype.exitCustomMediatorCall = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#logMediatorCall.
	WUMLListener.prototype.enterLogMediatorCall = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#logMediatorCall.
	WUMLListener.prototype.exitLogMediatorCall = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#messageModificationStatement.
	WUMLListener.prototype.enterMessageModificationStatement = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#messageModificationStatement.
	WUMLListener.prototype.exitMessageModificationStatement = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#returnStatement.
	WUMLListener.prototype.enterReturnStatement = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#returnStatement.
	WUMLListener.prototype.exitReturnStatement = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#parExpression.
	WUMLListener.prototype.enterParExpression = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#parExpression.
	WUMLListener.prototype.exitParExpression = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#expression.
	WUMLListener.prototype.enterExpression = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#expression.
	WUMLListener.prototype.exitExpression = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#evalExpression.
	WUMLListener.prototype.enterEvalExpression = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#evalExpression.
	WUMLListener.prototype.exitEvalExpression = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#literal.
	WUMLListener.prototype.enterLiteral = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#literal.
	WUMLListener.prototype.exitLiteral = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#mediaType.
	WUMLListener.prototype.enterMediaType = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#mediaType.
	WUMLListener.prototype.exitMediaType = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#type.
	WUMLListener.prototype.enterType = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#type.
	WUMLListener.prototype.exitType = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#classType.
	WUMLListener.prototype.enterClassType = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#classType.
	WUMLListener.prototype.exitClassType = function (ctx) {
	};


// Enter a parse tree produced by WUMLParser#messagePropertyName.
	WUMLListener.prototype.enterMessagePropertyName = function (ctx) {
	};

// Exit a parse tree produced by WUMLParser#messagePropertyName.
	WUMLListener.prototype.exitMessagePropertyName = function (ctx) {
	};


	module.exports.WUMLListener = WUMLListener;
});