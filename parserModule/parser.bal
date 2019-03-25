import ballerina/io;
import ballerina/log;


OperatorStack oprStack = new();
ExprStack expStack = new();
boolean recovered = true;
//tokenRecovery for token insertion
boolean tokenRecovery = true;
//keep track of error tokens
Token[] errTokens = [];
//error token counter
int errCount = 0;


type Parser object {
	private ParserBufferReader parserBuffer;
	public function __init(ParserBufferReader parserBuffer) {
		self.parserBuffer = parserBuffer;
	}
	public function parse() returns PackageNode {
		FunctionNode[] fnList = [];
		int pos = 0;
		while (!self.parserBuffer.isEOFToken()) {
			Token currToken = self.parserBuffer.consumeToken();
			if (currToken.tokenType == FUNCTION) {
				FunctionNode function1 = self.parseFunction(currToken);
				fnList[pos] = function1;
				pos += 1;
			}
		}
		Token currToken = self.parserBuffer.consumeToken();
		PackageNode pk2 = { nodeKind: PACKAGE_NODE, tokenList: [currToken], functionList: fnList };
		return pk2;
	}

	//method to consume the token from the token buffer
	function matchToken(int mToken) returns Token{
		if (self.LAToken(1) == mToken) {
			Token currToken = self.parserBuffer.consumeToken();
			io:println(tokenNames[currToken.tokenType]);
			return currToken;
		} else {
			tokenRecovery = false;
			string capturedErr = tokenNames[self.LAToken(1)];
			//Token insertedToken = self.insertToken(mToken);
			Token panicToken = self.panicRecovery(mToken);
			//Token errorToken = self.getLAToken();
			log:printError("Expected " + tokenNames[mToken] + ";found " + capturedErr);
			//io:println(errorToken);
			return panicToken;
		}
	}

	//error recovery token insertion
	function insertToken(int mToken) returns Token {
		tokenRecovery = true;
		return { tokenType: mToken, text: "<missing " + tokenNames[mToken] + ">" , startPos: -1 , endPos:-1,
			lineNumber: 0, index: -1, whiteSpace: "" };
	}
	//panic recovery
	function panicRecovery(int mToken) returns Token {
		int[] exprPanic = [SEMICOLON,RBRACE];
		boolean panicMode = true;
		while(panicMode){
			if(self.LAToken(1) == exprPanic[0]){
				panicMode = false;
			}
			else if (self.LAToken(1) == exprPanic[1]){
				break;
			}
			Token currToken1 = self.parserBuffer.consumeToken();
			errTokens[errCount] = currToken1;
			errCount += 1;
		}
		return { tokenType: mToken, text: "<missing " + tokenNames[mToken] + ">" , startPos: -1 , endPos:-1,
			lineNumber: 0, index: -1, whiteSpace: "" };

	}
	//lookahead the next token
	function LAToken(int lACount) returns int {
		Token laToken = self.parserBuffer.lookAheadToken(lookAheadCount = lACount);
		int lToken = laToken.tokenType;
		return lToken;
	}

	//get the lookahead token to be returned in errors
	function getLAToken () returns Token{
		Token laToken2 = self.parserBuffer.lookAheadToken(lookAheadCount = 1);
		return laToken2;
	}


	//Function definition
	//    | FUNCTION <callable Unit Signature> <callable Unit body>
	function parseFunction(Token currToken) returns FunctionNode {
		Token functionToken = currToken;
		FunctionSignatureNode signatureNode = self.parseCallableUnitSignature();
		BlockNode bNode = self.parseCallableUnitBody();
		FunctionNode fn1 = { nodeKind: FUNCTION_NODE, tokenList: [functionToken], fnSignature: signatureNode, blockNode:
		bNode };
		return fn1;
	}
	//Callable Unit Signature
	//    | IDENTIFIER () make this a callableUnitSignatureNode
	function parseCallableUnitSignature() returns FunctionSignatureNode {
		Token identifier = self.matchToken(IDENTIFIER);
		Token lParen = self.matchToken(LPAREN);
		Token rParen = self.matchToken(RPAREN);
		IdentifierNode idNode = { nodeKind: IDENTIFIER_NODE, tokenList: [identifier], identifier: identifier.text };
		FunctionSignatureNode signature1 = { nodeKind: FN_SIGNATURE_NODE, tokenList: [lParen, rParen],
			functionIdentifier: idNode };
		return signature1;
	}
	//Callable Unit Body
	//    | { <statement*>} make this blockNode
	function parseCallableUnitBody() returns BlockNode {
		StatementNode[] stsList = [];
		int pos = 0;
		Token lBrace = self.matchToken(LBRACE);
		while (self.LAToken(1) != RBRACE) {
			StatementNode stNode = self.parseStatement();
			if(recovered == false){
				Token errTkn  =  errTokens[0];
				ErrorNode erNode = {nodeKind: ERROR_NODE,tokenList:[errTkn],errorStatement:stNode};
				int listCount = 0;
				while(listCount < errTokens.length()){
					erNode.tokenList[listCount] = errTokens[listCount];
					listCount += 1;
				}
				stsList[pos] = erNode;
				pos += 1;
				recovered = true;
			}else{
				stsList[pos] = stNode;
				pos += 1;
			}
		}
		Token rBrace = self.matchToken(RBRACE);
		BlockNode blNode = { nodeKind: BLOCK_NODE, tokenList: [lBrace, rBrace], statementList: stsList };
		return blNode;
	}
	//Statement
	//    |<variable definition statement>
	function parseStatement() returns StatementNode {
		VariableDefinitionStatementNode varD = self.parseVariableDefinitionStatementNode();
		StatementNode stNode = varD;
		return stNode;
	}
	//Variable definition statement
	//    | <valueTypeName> IDENTIFIER SEMICOLON // int a;
	//    | <valeuTypeName>  IDENTIFIER ASSIGN <expression> SEMICOLON // int a  = b + 8;
	function parseVariableDefinitionStatementNode() returns VariableDefinitionStatementNode {
		Token valueTypeTkn = self.parseValueTypeName();
		ValueKind valueKind1 = self.matchValueType(valueTypeTkn);
		Token identifier = self.matchToken(IDENTIFIER);
		VarRefIdentifier vRef = { nodeKind: VAR_REF_NODE, tokenList: [identifier], varIdentifier: identifier.text };
		if (self.LAToken(1) == SEMICOLON) {
			Token semiC = self.matchToken(SEMICOLON);
			VariableDefinitionStatementNode vDef = { nodeKind: VAR_DEF_STATEMENT_NODE, tokenList: [valueTypeTkn, semiC],
				valueKind: valueKind1, varIdentifier: vRef, expression: null };
			return vDef;
		} else {
			Token assign = self.matchToken(ASSIGN);
			ExpressionNode exprNode = self.expressionBuilder();
			if(exprNode == null){
				log:printError("Expected Expression" + ";found null");
			}
			Token semiC2 = self.matchToken(SEMICOLON);
			VariableDefinitionStatementNode vDef2 = { nodeKind: VAR_DEF_STATEMENT_NODE, tokenList: [valueTypeTkn,assign, semiC2],
				valueKind: valueKind1, varIdentifier: vRef, expression: exprNode };
			return vDef2;
		}
	}

	function expressionBuilder() returns ExpressionNode {
		boolean isExpr = true;
		while (self.LAToken(1) != SEMICOLON && isExpr == true) {
			isExpr = self.parseExpression2();
			if(isExpr == false){
				recovered = false;
			}
		}
		while (oprStack.peek() != -1) {
			Token operator = oprStack.pop();
			OperatorKind opKind = self.matchOperatorType(operator);
			ExpressionNode expr2 = expStack.pop();
			ExpressionNode expr1 = expStack.pop();
			BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind: opKind,
				leftExpr: expr1, rightExpr: expr2 };
			expStack.push(bExpr);
		}
		ExpressionNode exp2 = expStack.pop();
		return exp2;
	}

	//Expression
	//    | <simple literal>
	//    | <varaiable reference>
	//    | expression ( ADD | SUB ) expression
	//    | expression (DIVISION | MULTIPLICATION) expression
	//expression is parsed using shunting yard algorithm
	function parseExpression2() returns boolean{
		if (self.LAToken(1) == LPAREN) {
			Token lParen = self.matchToken(LPAREN);
			oprStack.push(lParen);
			return true;
		} else if (self.LAToken(1) == NUMBER){
			Token number = self.matchToken(NUMBER);
			IntegerLiteralNode intLit = { nodeKind: INTEGER_LITERAL, tokenList: [number], number: number.text };
			SimpleLiteral sLit = intLit;
			expStack.push(sLit);
			return true;
		} else if (self.LAToken(1) == IDENTIFIER){
			Token identifier = self.matchToken(IDENTIFIER);
			VarRefIdentifier varRef = { nodeKind: VAR_REF_NODE, tokenList: [identifier], varIdentifier: identifier.text };
			expStack.push(varRef);
			//check whether the next token is also an identifier
			if(self.LAToken(1) == IDENTIFIER){
				return false;
			}
			return true;
		} else if (self.LAToken(1) == ADD || self.LAToken(1) == SUBSTRACTION || self.LAToken(1) == DIVISION || self.
			LAToken(1) == MULTIPLICATION) {
			while (oprStack.opPrecedence(oprStack.peek()) >= oprStack.opPrecedence(self.LAToken(1))) {
				Token operator = oprStack.pop();
				OperatorKind opKind = self.matchOperatorType(operator);
				ExpressionNode expr2 = expStack.pop();
				ExpressionNode expr1 = expStack.pop();
				BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind: opKind,
					leftExpr: expr1, rightExpr: expr2 };
				expStack.push(bExpr);
			}
			if (self.LAToken(1) == ADD) {
				Token add = self.matchToken(ADD);
				oprStack.push(add);
			} else if (self.LAToken(1) == MULTIPLICATION){
				Token multply = self.matchToken(MULTIPLICATION);
				oprStack.push(multply);
			} else if (self.LAToken(1) == SUBSTRACTION){
				Token subs = self.matchToken(SUBSTRACTION);
				oprStack.push(subs);
			}
			return true;
		} else if (self.LAToken(1) == RPAREN){
			Token rParen = self.matchToken(RPAREN);
			while (oprStack.peek() != LPAREN) {
				Token operator = oprStack.pop();
				OperatorKind opKind = self.matchOperatorType(operator);
				ExpressionNode expr2 = expStack.pop();
				ExpressionNode expr1 = expStack.pop();
				BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind: opKind,
					leftExpr: expr1, rightExpr: expr2 };
				expStack.push(bExpr);
			}
			//popping the lParen
			Token leftToken = oprStack.pop();

			ExpressionNode parenExpr = expStack.topExpr();
			if(parenExpr is BinaryExpressionNode){
				Token finalOperator = parenExpr.tokenList[0];
				parenExpr.tokenList = [finalOperator,rParen,leftToken];


			}
			return true;
		}

		return false;
	}
	//valueTypeName
	//    | INT
	//    | STRING
	function parseValueTypeName() returns Token {
		if (self.LAToken(1) == INT) {
			Token int1 = self.matchToken(INT);
			return int1;
		} else {
			Token string1 = self.matchToken(STRING);
			return string1;
		}
	}

	//check the value type for the given token
	function matchValueType(Token valueTypeTkn) returns ValueKind {
		if (tokenNames[valueTypeTkn.tokenType] == "INT") {
			return INT_TYPE;
		} else {
			return STRING_TYPE;
		}
	}


	//check the operator kind of the operator token
	function matchOperatorType(Token operator) returns OperatorKind {
		if (tokenNames[operator.tokenType] == "ADD") {
			return PLUS_OP;
		} else if (tokenNames[operator.tokenType] == "SUBSTRACTION"){
			return MINUS_OP;
		} else if (tokenNames[operator.tokenType] == "DIVISION"){
			return DIVISION_OP;
		} else {
			return MULTIPLICATION_OP;
		}
	}
};


//operator stack to store the operators
type OperatorStack object {
	Token[] opStack = [];
	int top;
	public function __init(int top = 0) {
		self.top = top;
	}
	function push(Token token2) {
		self.opStack[self.top] = token2;
		self.top = self.top + 1;
	}
	function pop() returns Token {
		self.top = self.top - 1;
		Token operatorToken = self.opStack[self.top];
		return operatorToken;
	}
	function peek() returns int {
		int topTkn2 = -1;
		if (self.top != 0) {
			Token topTkn = self.opStack[self.top - 1];
			topTkn2 = topTkn.tokenType;
		}
		return topTkn2;
	}
	function opPrecedence(int opToken) returns int {
		if (opToken == ADD || opToken == SUBSTRACTION) {
			return 0;
		} else if (opToken == DIVISION || opToken == MULTIPLICATION){
			return 1;
		}
		return -1;
	}
};

type ExprStack object {
	ExpressionNode[] exprStack = [];
	int top;
	public function __init(int top = 0) {
		self.top = top;
	}
	function push(ExpressionNode token2) {
		self.exprStack[self.top] = token2;
		self.top = self.top + 1;
	}
	function pop() returns ExpressionNode {
		if(self.top != 0) {
			self.top = self.top - 1;
			ExpressionNode exprsNode = self.exprStack[self.top];
			return exprsNode;
		}
	}
	function topExpr() returns ExpressionNode {
		ExpressionNode topExpr = self.exprStack[self.top -1];
		return topExpr;
	}
};


//Grammar
//Function definition
//    | FUNCTION <callable Unit Signature> <callable Unit body>
//Callable Unit Signature
//    | IDENTIFIER ()
//Callable Unit Body
//    | { <statement*>}
//Statement
//    |<variable definition statement>
//
//Variable definition statement
//    | <valueTypeName> IDENTIFIER SEMICOLON // int a;
//    | <valeuTypeName>  IDENTIFIER ASSIGN <expression> SEMICOLON // int a  = b;
//
//valueTypeName
//    | INT
//    | STRING
//Expression
//    | <simple literal>
//    | <varaiable reference>
//    | expression ( ADD | SUB ) expression
//    | expression (DIVISION | MULTIPLICATION) expression
//
//Simple Literal
//    | IntegerLiteral //[0-9]
//
//Variable reference
//    | IDENTIFIER
