// Generated from NEL.g4 by ANTLR 4.5
// jshint ignore: start
var antlr4 = require('lib/antlr4/index');

// This class defines a complete listener for a parse tree produced by NELParser.
function NELListener() {
	antlr4.tree.ParseTreeListener.call(this);
	return this;
}

NELListener.prototype = Object.create(antlr4.tree.ParseTreeListener.prototype);
NELListener.prototype.constructor = NELListener;

// Enter a parse tree produced by NELParser#script.
NELListener.prototype.enterScript = function(ctx) {
};

// Exit a parse tree produced by NELParser#script.
NELListener.prototype.exitScript = function(ctx) {
};


// Enter a parse tree produced by NELParser#handler.
NELListener.prototype.enterHandler = function(ctx) {
};

// Exit a parse tree produced by NELParser#handler.
NELListener.prototype.exitHandler = function(ctx) {
};


// Enter a parse tree produced by NELParser#statementList.
NELListener.prototype.enterStatementList = function(ctx) {
};

// Exit a parse tree produced by NELParser#statementList.
NELListener.prototype.exitStatementList = function(ctx) {
};


// Enter a parse tree produced by NELParser#statement.
NELListener.prototype.enterStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#statement.
NELListener.prototype.exitStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#titleStatement.
NELListener.prototype.enterTitleStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#titleStatement.
NELListener.prototype.exitTitleStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#participantStatement.
NELListener.prototype.enterParticipantStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#participantStatement.
NELListener.prototype.exitParticipantStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#integrationFlowDefStatement.
NELListener.prototype.enterIntegrationFlowDefStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#integrationFlowDefStatement.
NELListener.prototype.exitIntegrationFlowDefStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#inboundEndpointDefStatement.
NELListener.prototype.enterInboundEndpointDefStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#inboundEndpointDefStatement.
NELListener.prototype.exitInboundEndpointDefStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#pipelineDefStatement.
NELListener.prototype.enterPipelineDefStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#pipelineDefStatement.
NELListener.prototype.exitPipelineDefStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#outboundEndpointDefStatement.
NELListener.prototype.enterOutboundEndpointDefStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#outboundEndpointDefStatement.
NELListener.prototype.exitOutboundEndpointDefStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#groupStatement.
NELListener.prototype.enterGroupStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#groupStatement.
NELListener.prototype.exitGroupStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#groupDefStatement.
NELListener.prototype.enterGroupDefStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#groupDefStatement.
NELListener.prototype.exitGroupDefStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#messageflowStatementList.
NELListener.prototype.enterMessageflowStatementList = function(ctx) {
};

// Exit a parse tree produced by NELParser#messageflowStatementList.
NELListener.prototype.exitMessageflowStatementList = function(ctx) {
};


// Enter a parse tree produced by NELParser#messageflowStatement.
NELListener.prototype.enterMessageflowStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#messageflowStatement.
NELListener.prototype.exitMessageflowStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#mediatorStatement.
NELListener.prototype.enterMediatorStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#mediatorStatement.
NELListener.prototype.exitMediatorStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#mediatorStatementDef.
NELListener.prototype.enterMediatorStatementDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#mediatorStatementDef.
NELListener.prototype.exitMediatorStatementDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#logMediatorStatementDef.
NELListener.prototype.enterLogMediatorStatementDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#logMediatorStatementDef.
NELListener.prototype.exitLogMediatorStatementDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#logPropertyStatementDef.
NELListener.prototype.enterLogPropertyStatementDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#logPropertyStatementDef.
NELListener.prototype.exitLogPropertyStatementDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#nameSpaceStatementDef.
NELListener.prototype.enterNameSpaceStatementDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#nameSpaceStatementDef.
NELListener.prototype.exitNameSpaceStatementDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#headerMediatorStatementDef.
NELListener.prototype.enterHeaderMediatorStatementDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#headerMediatorStatementDef.
NELListener.prototype.exitHeaderMediatorStatementDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#integrationFlowDef.
NELListener.prototype.enterIntegrationFlowDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#integrationFlowDef.
NELListener.prototype.exitIntegrationFlowDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#inboundEndpointDef.
NELListener.prototype.enterInboundEndpointDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#inboundEndpointDef.
NELListener.prototype.exitInboundEndpointDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#pipelineDef.
NELListener.prototype.enterPipelineDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#pipelineDef.
NELListener.prototype.exitPipelineDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#outboundEndpointDef.
NELListener.prototype.enterOutboundEndpointDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#outboundEndpointDef.
NELListener.prototype.exitOutboundEndpointDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#routingStatement.
NELListener.prototype.enterRoutingStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#routingStatement.
NELListener.prototype.exitRoutingStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#routingStatementDef.
NELListener.prototype.enterRoutingStatementDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#routingStatementDef.
NELListener.prototype.exitRoutingStatementDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#variableStatement.
NELListener.prototype.enterVariableStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#variableStatement.
NELListener.prototype.exitVariableStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#variableDeclarationStatement.
NELListener.prototype.enterVariableDeclarationStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#variableDeclarationStatement.
NELListener.prototype.exitVariableDeclarationStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#variableAssignmentStatement.
NELListener.prototype.enterVariableAssignmentStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#variableAssignmentStatement.
NELListener.prototype.exitVariableAssignmentStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#constStatement.
NELListener.prototype.enterConstStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#constStatement.
NELListener.prototype.exitConstStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#parallelStatement.
NELListener.prototype.enterParallelStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#parallelStatement.
NELListener.prototype.exitParallelStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#parMultiThenBlock.
NELListener.prototype.enterParMultiThenBlock = function(ctx) {
};

// Exit a parse tree produced by NELParser#parMultiThenBlock.
NELListener.prototype.exitParMultiThenBlock = function(ctx) {
};


// Enter a parse tree produced by NELParser#parElseBlock.
NELListener.prototype.enterParElseBlock = function(ctx) {
};

// Exit a parse tree produced by NELParser#parElseBlock.
NELListener.prototype.exitParElseBlock = function(ctx) {
};


// Enter a parse tree produced by NELParser#ifStatement.
NELListener.prototype.enterIfStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#ifStatement.
NELListener.prototype.exitIfStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#conditionStatement.
NELListener.prototype.enterConditionStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#conditionStatement.
NELListener.prototype.exitConditionStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#conditionDef.
NELListener.prototype.enterConditionDef = function(ctx) {
};

// Exit a parse tree produced by NELParser#conditionDef.
NELListener.prototype.exitConditionDef = function(ctx) {
};


// Enter a parse tree produced by NELParser#ifMultiThenBlock.
NELListener.prototype.enterIfMultiThenBlock = function(ctx) {
};

// Exit a parse tree produced by NELParser#ifMultiThenBlock.
NELListener.prototype.exitIfMultiThenBlock = function(ctx) {
};


// Enter a parse tree produced by NELParser#ifElseBlock.
NELListener.prototype.enterIfElseBlock = function(ctx) {
};

// Exit a parse tree produced by NELParser#ifElseBlock.
NELListener.prototype.exitIfElseBlock = function(ctx) {
};


// Enter a parse tree produced by NELParser#loopStatement.
NELListener.prototype.enterLoopStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#loopStatement.
NELListener.prototype.exitLoopStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#refStatement.
NELListener.prototype.enterRefStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#refStatement.
NELListener.prototype.exitRefStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#commentStatement.
NELListener.prototype.enterCommentStatement = function(ctx) {
};

// Exit a parse tree produced by NELParser#commentStatement.
NELListener.prototype.exitCommentStatement = function(ctx) {
};


// Enter a parse tree produced by NELParser#expression.
NELListener.prototype.enterExpression = function(ctx) {
};

// Exit a parse tree produced by NELParser#expression.
NELListener.prototype.exitExpression = function(ctx) {
};



exports.NELListener = NELListener;