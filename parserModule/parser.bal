import ballerina/io;
import ballerina/log;


type Parser object {
	//stack which holds the operators during expression grammar
	OperatorStack oprStack = new();
	//stack which holds the expression for the expression rule
	ExprStack expStack = new();
	//keeps track on error recovery
	boolean errorRecovered = true;
	//keep track of error tokens
	Token[] errTokens = [];
	//error token counter
	int errCount = 0;
	//keep track of next valid occurence
	boolean expOperand = true;
	//if invalid occurence is captured in an expression
	boolean invalidOccurence = false;
	//invalid expression
	boolean invalidExpression = false;
	// tuple list expression count
	int tupleListPos = 0;
	//comma list count
	int commaCount = 0;
	//there is a operator in the prior
	boolean priorOperator = false;
	
	private ParserBufferReader parserBuffer;
	public function __init(ParserBufferReader parserBuffer) {
		self.parserBuffer = parserBuffer;
	}

	public function parse() returns PackageNode {
		DefinitionNode[] dList = [];
		//definition list position
		int pos = 0;
		while (!self.parserBuffer.isEOFToken()) {
			Token currToken = self.parserBuffer.consumeToken();
			if (currToken.tokenType == FUNCTION) {
				FunctionNode function1 = self.parseFunction(currToken);
				DefinitionNode defNode = function1;
				if(self.errorRecovered == false){
					if(self.errTokens.length()>0){
						ErrorNode fnErNode = {nodeKind: ERROR_NODE,tokenList:self.errTokens,errorFunction:function1};
						dList[pos] = fnErNode;
						pos += 1;
						self.errorRecovered = true;
						self.errTokens = [];
					}
					else{
						ErrorNode erNode = {nodeKind: ERROR_NODE,errorFunction:function1};
						dList[pos] = erNode;
						pos += 1;
						self.errorRecovered = true;
					}
				}
				else{
					dList[pos] = defNode;
					pos += 1;
				}
			}
		}
		Token currToken = self.parserBuffer.consumeToken();
		PackageNode pk2 = { nodeKind: PACKAGE_NODE, tokenList: [currToken], definitionList: dList };
		return pk2;
	}
	//method to consume the token from the token buffer if the token matches the expected token
	//if the token doesnt match , panicrecovery method will be invoked.
	function matchToken(int mToken , NodeKind rule) returns Token{
		if (self.LAToken(1) == mToken) {
			Token currToken = self.parserBuffer.consumeToken();
			//io:println(tokenNames[currToken.tokenType]);
			return currToken;
		} else {
			string capturedErr = tokenNames[self.LAToken(1)];
			Token panicToken = self.panicRecovery(mToken, rule);
			log:printError("Expected " + tokenNames[mToken] + ";found " + capturedErr);
			return panicToken;
		}
	}

	//error recovery : token insertion
	function insertToken(int mToken) returns Token {
		log:printError(tokenNames[mToken] + " inserted" );
		return { tokenType: mToken, text: tokenNames[mToken] , startPos: -1 , endPos:-1,
			lineNumber: 0, index: -1, whiteSpace: "" };
	}
	//error recovery : delete token
	function deleteToken() returns Token{
		Token currToken2 = self.parserBuffer.consumeToken();
		log:printError("unexpected Token: " + tokenNames[currToken2.tokenType]);
		return currToken2;
	}
	//panic recovery
	//each grammar rule will have a specific panic recovery.
	//here all the token will be removed and added to the error token list until we meet a terminal token
	function panicRecovery(int mToken,NodeKind rule) returns Token {
		boolean panicMode = true;
		if(rule == "variableDefinitionStatement"){
			if(mToken == SEMICOLON){
				Token insertSemi = self.insertToken(mToken);
				insertSemi.text = ";";
				return insertSemi;
			}else if(mToken == ASSIGN){
				Token insertAssign = self.insertToken(mToken);
				insertAssign.text = "=";
				return insertAssign;
			}else{
				//recovered = false;
				self.errorRecovered = false;
				int[] exprPanic = [SEMICOLON,RBRACE];
				while(panicMode){
					if(self.LAToken(1) == exprPanic[0]){
						panicMode = false;
						break;
					}
					else if (self.LAToken(1) == exprPanic[1]){
						break;
					}
					Token currToken1 = self.parserBuffer.consumeToken();
					self.errTokens[self.errCount] = currToken1;
					self.errCount += 1;
				}
				return { tokenType: PARSER_ERROR_TOKEN, text: "<unexpected Token: " + tokenNames[mToken] + ">" , startPos: -1 , endPos:-1,
					lineNumber: 0, index: -1, whiteSpace: "" };
			}
		}
		else if(rule == "function"){
			self.errorRecovered = false;
			//check if the expected token is a lBrace, then insert
			if(mToken == RBRACE){
				Token insertRbrace = self.insertToken(mToken);
				insertRbrace.text = "}";
				return insertRbrace;
			}else if(mToken == LBRACE){
				Token insertLbrace = self.insertToken(mToken);
				insertLbrace.text = "{";
				return insertLbrace;
			}else{
				int[] functionPanic = [RBRACE,EOF];
				while(panicMode){
					if(self.LAToken(1) == functionPanic[0]){
						panicMode = false;
					}
					else if (self.LAToken(1) == functionPanic[1]){
						return { tokenType: PARSER_ERROR_TOKEN, text: "<unexpected " + tokenNames[self.LAToken(1)] + ">" , startPos: -1 , endPos:-1,
						lineNumber: 0, index: -1, whiteSpace: "" };
					}
					Token currToken1 = self.parserBuffer.consumeToken();
					self.errTokens[self.errCount] = currToken1;
					self.errCount += 1;
				}
				return { tokenType: PARSER_ERROR_TOKEN, text: "<unexpected Token: Expected " + tokenNames[mToken] + ">" , startPos: -1 , endPos:-1,
					lineNumber: 0, index: -1, whiteSpace: "" };
			}
		}else if(rule == "statement"){
			self.errorRecovered = false;
			int[] functionPanic = [SEMICOLON,RBRACE];
			while(panicMode){
					if(self.LAToken(1) == functionPanic[0]){
						panicMode = false;
					}
					else if (self.LAToken(1) == functionPanic[1]){
						return { tokenType: PARSER_ERROR_TOKEN, text: "<unexpected " + tokenNames[self.LAToken(1)] + ">" , startPos: -1 , endPos:-1,
						lineNumber: 0, index: -1, whiteSpace: "" };
					}
					Token currToken1 = self.parserBuffer.consumeToken();
					self.errTokens[self.errCount] = currToken1;
					self.errCount += 1;
				}
				return { tokenType: PARSER_ERROR_TOKEN, text: "<unexpected Token: Expected " + tokenNames[mToken] + ">" , startPos: -1 , endPos:-1,
					lineNumber: 0, index: -1, whiteSpace: "" };

		}
		//this return statement will be in an else statement
		return { tokenType: PARSER_ERROR_TOKEN, text: "<unexpected " + tokenNames[mToken] + ">" , startPos: -1 , endPos:-1,
			lineNumber: 0, index: -1, whiteSpace: "" };

	}

	//lookahead the next token
	function LAToken(int lACount) returns int {
		Token laToken = self.parserBuffer.lookAheadToken(lookAheadCount = lACount);
		int lToken = laToken.tokenType;
		return lToken;
	}

	//Function definition
	//    | FUNCTION <callable Unit Signature> <callable Unit body>
	function parseFunction(Token currToken) returns FunctionNode {
		Token functionToken = currToken;
		FunctionSignatureNode? signatureNode = self.parseCallableUnitSignature();
		if(signatureNode == null){
			FunctionNode fn1 = { nodeKind: FUNCTION_NODE, tokenList: [functionToken], fnSignature: null, blockNode:null };
			return fn1;
		}else if (self.errorRecovered == false){
			FunctionNode fn1 = { nodeKind: FUNCTION_NODE, tokenList: [functionToken], fnSignature: signatureNode, blockNode:null };
			return fn1;
		}else{
			BlockNode bNode = self.parseCallableUnitBody();
			FunctionNode fn1 = { nodeKind: FUNCTION_NODE, tokenList: [functionToken], fnSignature: signatureNode, blockNode:
			bNode };
			return fn1;
		}
	}
	//Callable Unit Signature
	//    | IDENTIFIER ()
	//error recovery used: panic error recovery
	function parseCallableUnitSignature() returns FunctionSignatureNode? {
		Token identifier = self.matchToken(IDENTIFIER,FUNCTION_NODE);
		if(identifier.tokenType == PARSER_ERROR_TOKEN){
		return null;
		}else{
			Token lParen = self.matchToken(LPAREN,FUNCTION_NODE);
			if(lParen.tokenType == PARSER_ERROR_TOKEN){
				io:println(lParen.tokenType);
				IdentifierNode idNode = { nodeKind: IDENTIFIER_NODE, tokenList: [identifier], identifier: identifier.text };
				FunctionSignatureNode signature1 = { nodeKind: FN_SIGNATURE_NODE,functionIdentifier: idNode };
			return signature1;
			}else{
				Token rParen = self.matchToken(RPAREN,FUNCTION_NODE);
				if(rParen.tokenType == PARSER_ERROR_TOKEN){
				IdentifierNode idNode = { nodeKind: IDENTIFIER_NODE, tokenList: [identifier], identifier: identifier.text };
				//i have removed the rParen from the tokenList
				FunctionSignatureNode signature1 = { nodeKind: FN_SIGNATURE_NODE, tokenList: [lParen],functionIdentifier: idNode };
				return signature1;
				}else{
				IdentifierNode idNode = { nodeKind: IDENTIFIER_NODE, tokenList: [identifier], identifier: identifier.text };
				FunctionSignatureNode signature1 = { nodeKind: FN_SIGNATURE_NODE, tokenList: [lParen, rParen],
				functionIdentifier: idNode };
				return signature1;
				}
			}
			}
	}
	//Callable Unit Body
	//    | { <statement*>}
	//error recovery method: token insertion
	function parseCallableUnitBody() returns BlockNode {
		StatementNode[] stsList = [];
		int pos = 0;
		//token insertion if lBrace is mismatched
		Token lBrace = self.matchToken(LBRACE,FUNCTION_NODE);
		while (self.LAToken(1) != RBRACE) {
			if(self.LAToken(1) == EOF){
			break;
			}
			StatementNode? stNode = self.parseStatement();
			if(stNode is ErrorNode){
				//ErrorNode erNode = {nodeKind: ERROR_NODE,errorStatement:stNode};
				stsList[pos] = stNode;
				pos += 1;
				self.errorRecovered = true;
				self.invalidOccurence = false;
				self.errTokens = [];

			}
			else if(self.errorRecovered == false || self.invalidOccurence == true){//this was recovered == false
				if(self.errTokens.length()>0){
					ErrorNode erNode = {nodeKind: ERROR_NODE,tokenList:self.errTokens,errorStatement:stNode};
					stsList[pos] = erNode;
					pos += 1;
					//recovered = true;
					self.errorRecovered = true;
					self.invalidOccurence = false;
					self.errTokens = [];
				}
				else{
					ErrorNode erNode = {nodeKind: ERROR_NODE,errorStatement:stNode};
					stsList[pos] = erNode;
					pos += 1;
					//recovered = true;
					self.errorRecovered = true;
					self.invalidOccurence = false;
				}
			}else{
				stsList[pos] = stNode;
				pos += 1;
			}
		}
		//Token insertion if rBrace not found
		Token rBrace = self.matchToken(RBRACE,FUNCTION_NODE);
		BlockNode blNode = { nodeKind: BLOCK_NODE, tokenList: [lBrace, rBrace], statementList: stsList };
		return blNode;
	}
	//Statement
	//    |<variable definition statement>
	function parseStatement() returns StatementNode? {
		//check if the LA belongs to any of the statement type or else return an error node
		if(self.LAToken(1) == INT){
			VariableDefinitionStatementNode varD = self.parseVariableDefinitionStatementNode();
			StatementNode stNode = varD;
			return stNode;
		}else{
		Token panicToken = self.panicRecovery(INT, STATEMENT_NODE);
		ErrorNode erNode = {nodeKind: ERROR_NODE,tokenList:self.errTokens};
		StatementNode errSt = erNode;
		return errSt;
		}
	}
	//Variable definition statement
	//    | <valueTypeName> IDENTIFIER SEMICOLON // int a;
	//    | <valeuTypeName>  IDENTIFIER ASSIGN <expression> SEMICOLON // int a  = b + 8;
	function parseVariableDefinitionStatementNode() returns VariableDefinitionStatementNode {

		Token valueTypeTkn = self.parseValueTypeName();
		//it is not necessary to check the validity of the value type since we only call this method if its a valid value type name???
		ValueKind valueKind1 = self.matchValueType(valueTypeTkn);
		Token identifier = self.matchToken(IDENTIFIER,VAR_DEF_STATEMENT_NODE);
		if(identifier.tokenType == PARSER_ERROR_TOKEN){
			if (self.LAToken(1) == SEMICOLON) {
			Token semiC = self.matchToken(SEMICOLON,VAR_DEF_STATEMENT_NODE);
			VariableDefinitionStatementNode vDef = { nodeKind: VAR_DEF_STATEMENT_NODE, tokenList: [valueTypeTkn, semiC],
				valueKind: valueKind1, varIdentifier: null, expression: null };
			return vDef;
			}else{
			VariableDefinitionStatementNode vDef2 = { nodeKind: VAR_DEF_STATEMENT_NODE, tokenList: [valueTypeTkn],
				valueKind: valueKind1, varIdentifier: null, expression: null };
			return vDef2;
			}
		}
		VarRefIdentifier vRef = { nodeKind: VAR_REF_NODE, tokenList: [identifier], varIdentifier: identifier.text };
		if (self.LAToken(1) == SEMICOLON) {
			Token semiC = self.matchToken(SEMICOLON,VAR_DEF_STATEMENT_NODE);
			VariableDefinitionStatementNode vDef = { nodeKind: VAR_DEF_STATEMENT_NODE, tokenList: [valueTypeTkn, semiC],
				valueKind: valueKind1, varIdentifier: vRef, expression: null };
			return vDef;
		} else {
			//token insertion if token assign is mismatched
			Token assign = self.matchToken(ASSIGN,VAR_DEF_STATEMENT_NODE);
			ExpressionNode exprNode = self.expressionBuilder();
			//if no semicolon found in the end of the expr, then errorRecovered will set to false within the expressionBuilder method itself
			if(exprNode == null){
				log:printError("No Expression found");
				//recovered = false;
				self.errorRecovered = false;
			}
			//token insertion if semicolon is mismatched
			Token semiC2 = self.matchToken(SEMICOLON,VAR_DEF_STATEMENT_NODE);
			VariableDefinitionStatementNode vDef2 = { nodeKind: VAR_DEF_STATEMENT_NODE, tokenList: [valueTypeTkn,assign, semiC2],
				valueKind: valueKind1, varIdentifier: vRef, expression: exprNode };
			return vDef2;
		}
	}
	//expression
	//| <simple literal>
    //| <variable reference>
    //| <record literal>
    //| expression (DIVISION | MULTIPLICATION | MOD) expression
    //| expression ( ADD | SUB ) expression
    //| expression (LT_EQUAL | GT_EQUAL | GT | LT)expression
    //| expression (EQUAL | NOT_EQUAL) expression
    //| expression (REF_EQUAL | REF_NOT_EQUAL) expression
    //| (ADD | SUB | NOT | BIT_COMPLEMENT | UNTAINT) expression
    //| <tuple literal>
	function expressionBuilder() returns ExpressionNode {
		self.tupleListPos = 0;
		//tracks the validity of the expression received from the expression helper function
		boolean isExpr = true;
		while (self.LAToken(1) != SEMICOLON && isExpr == true){
			isExpr = self.parseExpression2();

			if(isExpr == false){
				//recovered = false;
				self.errorRecovered = false;
			}
		}
		//if the expression stack contains any operators, build the expressions based on the operators
		while (self.oprStack.peek() != -1) {
			Token operator = self.oprStack.pop();
			if(operator.tokenType == LPAREN){
				log:printError("<Missing RPAREN>");
				self.invalidOccurence = true;
				OperatorKind opKind = self.matchOperatorType(operator);
			ExpressionNode expr2 = self.expStack.pop();
			ExpressionNode expr1 = self.expStack.pop();
			BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind: opKind,
				leftExpr: expr1, rightExpr: expr2 };
			self.expStack.push(bExpr);
			}else if (operator.tokenType == UNARY_MINUS){
				OperatorKind opKind3 = self.matchOperatorType(operator);
				if(self.expOperand == true){
					self.expOperand = false;
					log:printError("<missing unary expression>");
					self.invalidOccurence = true;
					UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind3,
					uExpression : null };
					self.expStack.push(uExpression);
				}else{
				ExpressionNode expr3 = self.expStack.pop();
				UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind3,
				uExpression : expr3 };
				self.expStack.push(uExpression);
				}
			}else if (operator.tokenType == UNARY_PLUS){
				OperatorKind opKind4 = self.matchOperatorType(operator);
				if(self.expOperand == true){
					self.expOperand = false;
					log:printError("<missing unary expression>");
					self.invalidOccurence = true;
					UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
					uExpression : null };
					self.expStack.push(uExpression);
				}else{
					ExpressionNode expr4 = self.expStack.pop();
					UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
				uExpression : expr4 };
					self.expStack.push(uExpression);
				}
			}else if (operator.tokenType == NOT){
				OperatorKind opKind4 = self.matchOperatorType(operator);
				if(self.expOperand == true){
					self.expOperand = false;
					log:printError("<missing unary expression>");
					self.invalidOccurence = true;
					UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
					uExpression : null };
					self.expStack.push(uExpression);
				}else{
				ExpressionNode expr4 = self.expStack.pop();
				UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
				uExpression : expr4 };
				self.expStack.push(uExpression);
				}
			}else if (operator.tokenType == BIT_COMPLEMENT){
				OperatorKind opKind4 = self.matchOperatorType(operator);
				if(self.expOperand == true){
					self.expOperand = false;
					log:printError("<missing unary expression>");
					self.invalidOccurence = true;
					UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
					uExpression : null };
					self.expStack.push(uExpression);
				}else{
				ExpressionNode expr4 = self.expStack.pop();
				UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
				uExpression : expr4 };
				self.expStack.push(uExpression);
				}
			}else if (operator.tokenType == UNTAINT){
				OperatorKind opKind4 = self.matchOperatorType(operator);
				if(self.expOperand == true){
					self.expOperand = false;
					log:printError("<missing unary expression>");
					self.invalidOccurence = true;
					UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
					uExpression : null };
					self.expStack.push(uExpression);
				}else{
				ExpressionNode expr4 = self.expStack.pop();
				UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
				uExpression : expr4 };
				self.expStack.push(uExpression);
				}
			}
			else{
				OperatorKind opKind = self.matchOperatorType(operator);
				ExpressionNode expr2 = self.expStack.pop();
				ExpressionNode expr1 = self.expStack.pop();
				if(expr1 == null){
					//recovered = false;
					self.errorRecovered = false;
					log:printError("binary right expression not found");
					BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind: opKind,
					leftExpr: expr2, rightExpr: expr1 };
				self.expStack.push(bExpr);
				}else{
					BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind: opKind,
					leftExpr: expr1, rightExpr: expr2 };
				self.expStack.push(bExpr);
				}
			}
		}
		//pop the final expression from the expr stack
		ExpressionNode exp2 = self.expStack.pop();
		return exp2;
	}

	//helper function to build the expression
	//expression is parsed using shunting yard algorithm
	function parseExpression2() returns boolean {
		if (self.LAToken(1) == LPAREN) {
		self.priorOperator = false;
			if (self.expOperand == false) {
				Token invalidToken = self.deleteToken();
				self.errTokens[self.errCount] = invalidToken;
				self.errCount += 1;
				self.invalidOccurence = true;
				return true;
			} else {
				Token lParen = self.matchToken(LPAREN, EXPRESSION_NODE);
				//check for empty tuple literal
				if(self.LAToken(1) == RPAREN){
					Token rParen = self.matchToken(RPAREN, EXPRESSION_NODE);
					EmptyTupleLiteralNode emptyTuple = {nodeKind:EMPTY_TUPLE_LITERAL_NODE, tokenList:[lParen,rParen] };
					SimpleLiteral smLiteral = emptyTuple;
					self.expStack.push(smLiteral);
					return true;
				}else{
					self.oprStack.push(lParen);
					self.expOperand = true;
					return true;
				}
			}
		} else if (self.LAToken(1) == NUMBER){
			self.priorOperator = false;
			if (self.expOperand == false) {
				Token invalidToken = self.deleteToken();
				self.errTokens[self.errCount] = invalidToken;
				self.errCount += 1;
				self.invalidOccurence = true;
				return true;
			} else {
				Token number = self.matchToken(NUMBER, EXPRESSION_NODE);
				IntegerLiteralNode intLit = { nodeKind: INTEGER_LITERAL, tokenList: [number], number: number.text };
				SimpleLiteral sLit = intLit;
				self.expStack.push(sLit);
				self.expOperand = false;
				return true;
			}
		} else if (self.LAToken(1) == IDENTIFIER){
			self.priorOperator = false;
			if (self.expOperand == false) {
				Token invalidToken = self.deleteToken();
				self.errTokens[self.errCount] = invalidToken;
				self.errCount += 1;
				self.invalidOccurence = true;
				return true;
			} else {
				Token identifier = self.matchToken(IDENTIFIER, EXPRESSION_NODE);
				VarRefIdentifier varRef = { nodeKind: VAR_REF_NODE, tokenList: [identifier], varIdentifier: identifier.
				text };
				self.expStack.push(varRef);
				self.expOperand = false;
				return true;
			}
		} else if (self.LAToken(1) == ADD || self.LAToken(1) == SUB || self.LAToken(1) == DIV || self.
			LAToken(1) == MUL || self.LAToken(1) == MOD || self.LAToken(1) == LT_EQUAL || self.LAToken(1) == GT_EQUAL || self.LAToken(1) == GT
			 || self.LAToken(1) == LT || self.LAToken(1) == EQUAL || self.LAToken(1) == NOT_EQUAL || self.LAToken(1) == REF_EQUAL || self.LAToken(1) == REF_NOT_EQUAL
			  || self.LAToken(1) == NOT || self.LAToken(1) == BIT_COMPLEMENT || self.LAToken(1) == UNTAINT || self.LAToken(1) == COMMA ) {

			//if the expression stack is empty then that means no prior expression , so this is a unary expr
			if(self.expStack.isEmpty() == true || self.priorOperator == true){
				if(self.LAToken(1) == SUB){
					Token unarySub = self.matchToken(SUB, EXPRESSION_NODE);
					unarySub.tokenType = UNARY_MINUS;
					self.oprStack.push(unarySub);
					self.expOperand = true;
					self.priorOperator = true;
					return true;
				}else if (self.LAToken(1) == ADD){
					Token unaryAdd = self.matchToken(ADD, EXPRESSION_NODE);
					unaryAdd.tokenType = UNARY_PLUS;
					self.oprStack.push(unaryAdd);
					self.expOperand = true;
					self.priorOperator = true;
					return true;
				}else if (self.LAToken(1) == NOT){
					Token unaryNot = self.matchToken(NOT, EXPRESSION_NODE);
					self.oprStack.push(unaryNot);
					self.expOperand = true;
					self.priorOperator = true;
					return true;
				}else if (self.LAToken(1) == BIT_COMPLEMENT){
					Token unaryBit = self.matchToken(BIT_COMPLEMENT, EXPRESSION_NODE);
					self.oprStack.push(unaryBit);
					self.expOperand = true;
					self.priorOperator = true;
					return true;
				}else if (self.LAToken(1) == UNTAINT){
					Token unaryUnTaint = self.matchToken(UNTAINT, EXPRESSION_NODE);
					self.oprStack.push(unaryUnTaint);
					self.expOperand = true;
					self.priorOperator = true;
					return true;
				}else{ //Operator which is not an unary operator - Ex:(,2) -> in such instance where the comma is in invalid position comma is deleted and added to the errorlist.
					Token invalidToken = self.deleteToken();
					self.errTokens[self.errCount] = invalidToken;
					self.errCount += 1;
					self.invalidOccurence = true;
					return true;
				}
			}
			else{
				//setting the prior operator to true
				self.priorOperator = true;
				while (self.oprStack.opPrecedence(self.oprStack.peek()) >= self.oprStack.opPrecedence(self.LAToken(1))) {
					//if the lookahead token is a comma and also the opr stack peek is also a comma, then we dont pop the comma from the stack Ex: (2,3)
					if(self.oprStack.peek() == COMMA){
						Token comma = self.matchToken(COMMA, EXPRESSION_NODE);
						self.oprStack.push(comma);
						self.expOperand = true;
						return true;
					}
					Token operator = self.oprStack.pop();

					if (operator.tokenType == UNARY_MINUS){
						self.priorOperator = false;
						OperatorKind opKind3 = self.matchOperatorType(operator);
						ExpressionNode expr3 = self.expStack.pop();
						UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind3,
						uExpression : expr3 };
						self.expStack.push(uExpression);
					}else if (operator.tokenType == UNARY_PLUS){
						self.priorOperator = false;
						OperatorKind opKind4 = self.matchOperatorType(operator);
						ExpressionNode expr4 = self.expStack.pop();
						UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
						uExpression : expr4 };
						self.expStack.push(uExpression);
					}else if (operator.tokenType == NOT){
						self.priorOperator = false;
						OperatorKind opKindNot = self.matchOperatorType(operator);
						ExpressionNode exprNot = self.expStack.pop();
						UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKindNot,
						uExpression : exprNot };
						self.expStack.push(uExpression);
					}else if (operator.tokenType == BIT_COMPLEMENT){
						self.priorOperator = false;
						OperatorKind opKindBit = self.matchOperatorType(operator);
						ExpressionNode exprBit = self.expStack.pop();
						UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKindBit,
						uExpression : exprBit };
						self.expStack.push(uExpression);
					}else if (operator.tokenType == UNTAINT){
						self.priorOperator = false;
						OperatorKind opKindBit = self.matchOperatorType(operator);
						ExpressionNode exprBit = self.expStack.pop();
						UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKindBit,
						uExpression : exprBit };
						self.expStack.push(uExpression);
					}else{
					OperatorKind opKind = self.matchOperatorType(operator);
					ExpressionNode expr2 = self.expStack.pop();
					ExpressionNode expr1 = self.expStack.pop();
					BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind:
					opKind,leftExpr: expr1, rightExpr: expr2 };
					self.expStack.push(bExpr);
					}
				}
				if(self.LAToken(1) == ADD) {
					Token add = self.matchToken(ADD, EXPRESSION_NODE);
					self.oprStack.push(add);
				} else if (self.LAToken(1) == MUL){
					Token multply = self.matchToken(MUL, EXPRESSION_NODE);
					self.oprStack.push(multply);
				} else if (self.LAToken(1) == SUB){
					Token subs = self.matchToken(SUB, EXPRESSION_NODE);
					self.oprStack.push(subs);
				}else if (self.LAToken(1) == DIV){
					Token div = self.matchToken(DIV, EXPRESSION_NODE);
					self.oprStack.push(div);
				}else if (self.LAToken(1) == MOD){
					Token mod = self.matchToken(MOD, EXPRESSION_NODE);
					self.oprStack.push(mod);
				}else if (self.LAToken(1) == LT_EQUAL){
					Token ltEqual = self.matchToken(LT_EQUAL, EXPRESSION_NODE);
					self.oprStack.push(ltEqual);
				}else if (self.LAToken(1) == GT_EQUAL){
					Token gtEqual = self.matchToken(GT_EQUAL, EXPRESSION_NODE);
					self.oprStack.push(gtEqual);
				}else if (self.LAToken(1) == GT){
					Token gt = self.matchToken(GT, EXPRESSION_NODE);
					self.oprStack.push(gt);
				}else if (self.LAToken(1) == LT){
					Token lt = self.matchToken(LT, EXPRESSION_NODE);
					self.oprStack.push(lt);
				}else if (self.LAToken(1) == EQUAL){
					Token equal = self.matchToken(EQUAL, EXPRESSION_NODE);
					self.oprStack.push(equal);
				}else if (self.LAToken(1) == NOT_EQUAL){
					Token notEqual = self.matchToken(NOT_EQUAL, EXPRESSION_NODE);
					self.oprStack.push(notEqual);
				}else if (self.LAToken(1) == REF_EQUAL){
					Token refEqual = self.matchToken(REF_EQUAL, EXPRESSION_NODE);
					self.oprStack.push(refEqual);
				}else if (self.LAToken(1) == REF_NOT_EQUAL){
					Token refNotEqual = self.matchToken(REF_NOT_EQUAL, EXPRESSION_NODE);
					self.oprStack.push(refNotEqual);
				}else if (self.LAToken(1) == COMMA){
					Token comma = self.matchToken(COMMA, EXPRESSION_NODE);
					self.oprStack.push(comma);
				}
				//if(self.LAToken(1) == ADD || self.LAToken(1) == MUL || self.LAToken(1) == SUB){
				//	Token actualTkn = self.matchToken(self.LAToken(1), EXPRESSION_NODE);
				//	self.oprStack.push(actualTkn);
				//}
				self.expOperand = true;
				return true;
			}

		} else if (self.LAToken(1) == RPAREN){
			//self.priorOperator = false;
			//tuple expression list
			ExpressionNode[] tupleList = [];
			//list to keep track of commas
			Token[] commaList = [];

				Token rParen = self.matchToken(RPAREN, EXPRESSION_NODE);
				commaList[self.commaCount] = rParen;
				self.commaCount +=1;
				while (self.oprStack.peek() != LPAREN) {
					Token operator = self.oprStack.pop();
					if(operator.tokenType == PARSER_ERROR_TOKEN){
					log:printError("<missing token>");
					self.invalidOccurence = true;
					break;
					}else if(operator.tokenType == COMMA){
						commaList[self.commaCount] = operator;
						self.commaCount +=1;
						if(self.expOperand == true){
						//ExpressionNode exprComma = null;
						log:printError("<missing expression>");
						//tupleList[self.tupleListPos] = exprComma;
						//self.tupleListPos += 1;
						self.invalidOccurence = true;
						continue;
						}

						ExpressionNode exprComma = self.expStack.pop();
						tupleList[self.tupleListPos] = exprComma;
						self.tupleListPos += 1;
						continue;
					}else if (operator.tokenType == UNARY_MINUS){

						OperatorKind opKind3 = self.matchOperatorType(operator);

						if(self.expOperand == true){
						self.expOperand = false;
						log:printError("<missing expression>");
						self.invalidOccurence = true;
						UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind3,
						uExpression : null };
						self.expStack.push(uExpression);
						}else{
							ExpressionNode expr3 = self.expStack.pop();
							UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind3,
						uExpression : expr3 };
						self.expStack.push(uExpression);
						}
					}else if (operator.tokenType == UNARY_PLUS){
						OperatorKind opKind4 = self.matchOperatorType(operator);
						if(self.expOperand == true){
							self.expOperand = false;
							log:printError("<missing expression>");
							self.invalidOccurence = true;
							UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
						uExpression : null };
							self.expStack.push(uExpression);
						}else{
							ExpressionNode expr4 = self.expStack.pop();
							UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
						uExpression : expr4 };
							self.expStack.push(uExpression);
						}
					}else if (operator.tokenType == NOT){
						OperatorKind opKind4 = self.matchOperatorType(operator);
						if(self.expOperand == true){
							self.expOperand = false;
							log:printError("<missing expression>");
							self.invalidOccurence = true;
							UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
						uExpression : null };
							self.expStack.push(uExpression);
						}else{
						ExpressionNode expr4 = self.expStack.pop();
						UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
						uExpression : expr4 };
						self.expStack.push(uExpression);
						}
					}else if (operator.tokenType == BIT_COMPLEMENT){
						OperatorKind opKind4 = self.matchOperatorType(operator);
						if(self.expOperand == true){
							self.expOperand = false;
							log:printError("<missing expression>");
							self.invalidOccurence = true;
							UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
						uExpression : null };
							self.expStack.push(uExpression);
						}else{
						ExpressionNode expr4 = self.expStack.pop();
						UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
						uExpression : expr4 };
						self.expStack.push(uExpression);
						}
					}else if (operator.tokenType == UNTAINT){
						OperatorKind opKind4 = self.matchOperatorType(operator);
						if(self.expOperand == true){
							self.expOperand = false;
							log:printError("<missing expression>");
							self.invalidOccurence = true;
							UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
						uExpression : null };
							self.expStack.push(uExpression);
						}else{
						ExpressionNode expr4 = self.expStack.pop();
						UnaryExpressionNode uExpression = { nodeKind: UNARY_EXPRESSION_NODE, tokenList: [operator], operatorKind: opKind4,
						uExpression : expr4 };
						self.expStack.push(uExpression);
						}
					}else{
					if(self.expOperand == true){
						log:printError("<missing expression>");
						self.invalidOccurence = true;
						OperatorKind opKind = self.matchOperatorType(operator);
						ExpressionNode expr2 = self.expStack.pop();
						ExpressionNode expr1 = self.expStack.pop();
						BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind:
						opKind,leftExpr: expr2, rightExpr: expr1};
						self.expStack.push(bExpr);
					}else{
						OperatorKind opKind = self.matchOperatorType(operator);
						ExpressionNode expr2 = self.expStack.pop();
						ExpressionNode expr1 = self.expStack.pop();
						BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind:
					opKind,leftExpr: expr1, rightExpr: expr2};
						self.expStack.push(bExpr);
					}

					}
				}
				//popping the lParen
				Token leftToken = self.oprStack.pop();
				commaList[self.commaCount] = leftToken;
				self.commaCount +=1;

				//ExpressionNode parenExpr = self.expStack.topExpr();
				ExpressionNode parenExpr = self.expStack.pop();
					tupleList[self.tupleListPos] = parenExpr;
					self.tupleListPos += 1;
					//reversing the array
					int reverseCount = 0;
					while(reverseCount < tupleList.length() /2){
						ExpressionNode temp = tupleList[reverseCount];
						tupleList[reverseCount] = tupleList[tupleList.length() - reverseCount - 1];
						tupleList[tupleList.length() - reverseCount - 1] = temp;
						reverseCount += 1;
					}

					TupleLiteralNode tupleLNode = {nodeKind: TUPLE_LITERAL_NODE , tokenList: commaList,tupleExprList:tupleList };
					self.expStack.push(tupleLNode);

				self.expOperand = false;
				self.priorOperator = false;
				return true;


		}else if (self.LAToken(1) == LBRACE){//for record literal
			self.priorOperator = false;
			RecordKeyValueNode[] recordList = [];
			int pos = 0;
			if (self.expOperand == false) {
				Token invalidToken = self.deleteToken();
				self.errTokens[self.errCount] = invalidToken;
				self.errCount += 1;
				self.invalidOccurence = true;
				return true;
			} else {
				Token lBrace = self.matchToken(LBRACE, EXPRESSION_NODE);
				self.oprStack.push(lBrace);
				if(self.LAToken(1) != RBRACE){
				//self.expOperand = true;
				RecordKeyValueNode rkNode = self.parseRecordKeyValue();
					if(self.LAToken(1) == RBRACE){
						Token rBrace1 = self.matchToken(RBRACE, EXPRESSION_NODE);
						//pop the lBrace
						Token leftBrace2 = self.oprStack.pop();
						recordList[pos] = rkNode;
						pos += 1;
						RecordLiteralNode rLiteralNode = {nodeKind:RECORD_LITERAL_NODE,tokenList:[leftBrace2,rBrace1],recordkeyValueList:recordList};
						self.expStack.push(rLiteralNode);
						return true;
					}else{
						recordList[pos] = rkNode;
						pos += 1;
						while(self.LAToken(1) != RBRACE){
							if(self.LAToken(1) == COMMA){
								self.expOperand = true;
								Token comma = self.matchToken(COMMA, EXPRESSION_NODE);
							}
						RecordKeyValueNode rkNode2 = self.parseRecordKeyValue();
						recordList[pos] = rkNode2;
						pos += 1;
						}
						//while (self.oprStack.peek() != LBRACE) {
						//
						//	//ExpressionNode expr2 = self.expStack.pop();
						//	//ExpressionNode expr1 = self.expStack.pop();
						//	//BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind:
						//	//opKind,leftExpr: expr1, rightExpr: expr2 };
						//	//self.expStack.push(bExpr);
						//}
						//pop the LBrace
						Token lftBrace = self.oprStack.pop();
						Token rBrace1 = self.matchToken(RBRACE, EXPRESSION_NODE);
						RecordLiteralNode rLiteralNode = {nodeKind:RECORD_LITERAL_NODE,tokenList:[lBrace,rBrace1],recordkeyValueList:recordList};
						self.expStack.push(rLiteralNode);

					}
				}else if (self.LAToken(1) == RBRACE){
					Token leftBrace = self.oprStack.pop();
					Token rBrace = self.matchToken(RBRACE, EXPRESSION_NODE);
					RecordLiteralNode recordNode = {nodeKind:RECORD_LITERAL_NODE, tokenList:[leftBrace,rBrace],recordkeyValueList:recordList};
					self.expStack.push(recordNode);
					return true;
				}
				}
				return true;
			}
			return false;
		}

	//record key value
	//recordKey COLON expression
	function parseRecordKeyValue() returns RecordKeyValueNode{
	//record key
	//recordKey COLON expression
		if(self.LAToken(1) == IDENTIFIER && self.LAToken(2) == COLON){
			Token id = self.matchToken(IDENTIFIER, EXPRESSION_NODE);
			IdentifierNode idNode = {nodeKind:IDENTIFIER_NODE,tokenList:[id],identifier:id.text};
			RecordKeyNode idkeyNode = {nodeKind:RECORD_KEY_NODE,recordKey: idNode};
			Token colon = self.matchToken(COLON, EXPRESSION_NODE);
			OperatorKind opKind = self.matchOperatorType(colon);
			self.expOperand = true;

			boolean sf2 = true;
			while(self.LAToken(1) != RBRACE && self.LAToken(1) != COMMA && sf2 == true){
				sf2 = self.parseExpression2();
				if(sf2 == false){
				//recovered = false;
				self.errorRecovered = false;
				}
			}
			while (self.oprStack.peek() != LBRACE) {
				Token operator = self.oprStack.pop();
				OperatorKind opKind1 = self.matchOperatorType(operator);
				ExpressionNode expr2 = self.expStack.pop();
				ExpressionNode expr1 = self.expStack.pop();
				BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind:
				opKind1,leftExpr: expr1, rightExpr: expr2 };
				self.expStack.push(bExpr);
			}
			ExpressionNode expr4 = self.expStack.pop();
			RecordKeyValueNode rKeyValueNode = {nodeKind:RECORD_KEY_VALUE_NODE, tokenList:[colon],recordKeyNode:idkeyNode,operatorKind:opKind,recordValueExpression:expr4};
			return rKeyValueNode;

		}else{
			boolean parseRKey = true;
			while(self.LAToken(1) != COLON && parseRKey == true ){
				parseRKey = self.parseExpression2();
			}
			while (self.oprStack.peek() != LBRACE) {
				Token operator = self.oprStack.pop();
				OperatorKind opKind1 = self.matchOperatorType(operator);
				ExpressionNode expr2 = self.expStack.pop();
				ExpressionNode expr1 = self.expStack.pop();
				BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind:
				opKind1,leftExpr: expr1, rightExpr: expr2 };
				self.expStack.push(bExpr);
			}
		 	ExpressionNode expr4 = self.expStack.pop();
		 	RecordKeyNode idkeyNode = {nodeKind:RECORD_KEY_NODE,recordExpression: expr4};
		 	Token colon = self.matchToken(COLON, EXPRESSION_NODE);
		 	OperatorKind opKind = self.matchOperatorType(colon);
			self.expOperand = true;

			boolean sf3 = true;
			while(self.LAToken(1) != RBRACE && self.LAToken(1) != COMMA && sf3 == true){
				sf3 = self.parseExpression2();
				if(sf3 == false){
				//recovered = false;
				self.errorRecovered = false;
				}
			}
			while (self.oprStack.peek() != LBRACE) {
				Token operator = self.oprStack.pop();
				OperatorKind opKind1 = self.matchOperatorType(operator);
				ExpressionNode expr2 = self.expStack.pop();
				ExpressionNode expr1 = self.expStack.pop();
				BinaryExpressionNode bExpr = { nodeKind: BINARY_EXP_NODE, tokenList: [operator], operatorKind:
				opKind1,leftExpr: expr1, rightExpr: expr2 };
				self.expStack.push(bExpr);
			}
			ExpressionNode expr5 = self.expStack.pop();
			//RecordKeyNode idkeyNode2 = {nodeKind:RECORD_KEY_NODE,recordExpression: expr5};
			RecordKeyValueNode rKeyValueNode = {nodeKind:RECORD_KEY_VALUE_NODE, tokenList:[colon],recordKeyNode:idkeyNode,operatorKind:opKind,recordValueExpression:expr5};
			return rKeyValueNode;
		 }
	}
	//valueTypeName
	//    | INT
	//    | STRING
	function parseValueTypeName() returns Token {
		if (self.LAToken(1) == INT) {
			Token int1 = self.matchToken(INT,STATEMENT_NODE);
			return int1;
		}else if (self.LAToken(1) == STRING) {
			Token string1 = self.matchToken(STRING,STATEMENT_NODE);
			return string1;
		}else{
			return  { tokenType: PARSER_ERROR_TOKEN, text: "<unexpected " + tokenNames[self.LAToken(1)] + ">" , startPos: -1 , endPos:-1,
			lineNumber: 0, index: -1, whiteSpace: "" };
		}
	}

	//check the value type for the given token
	function matchValueType(Token valueTypeTkn) returns ValueKind {
		if (tokenNames[valueTypeTkn.tokenType] == "INT") {
			return INT_TYPE;
		}else if (tokenNames[valueTypeTkn.tokenType] == "STRING")  {
			return STRING_TYPE;
		}else{
			return ERROR_VALUE_TYPE;
		}
	}
	//check the operator kind of the operator token
	function matchOperatorType(Token operator) returns OperatorKind {
		if (tokenNames[operator.tokenType] == "ADD") {
			return PLUS_OP;
		} else if (tokenNames[operator.tokenType] == "SUB"){
			return MINUS_OP;
		} else if (tokenNames[operator.tokenType] == "DIV"){
			return DIVISION_OP;
		} else if (tokenNames[operator.tokenType] == "MUL") {
			return MULTIPLICATION_OP;
		}else if(tokenNames[operator.tokenType] == "COLON"){
			return COLON_OP;
		}else if(tokenNames[operator.tokenType] == "MOD"){
			return MOD_OP;
		}else if(tokenNames[operator.tokenType] == "LT_EQUAL"){
			return LT_EQUAL_OP;
		}else if(tokenNames[operator.tokenType] == "GT_EQUAL"){
			return GT_EQUAL_OP;
		}else if(tokenNames[operator.tokenType] == "GT"){
			return GT_OP;
		}else if(tokenNames[operator.tokenType] == "LT"){
			return LT_OP;
		}else if(tokenNames[operator.tokenType] == "EQUAL"){
			return EQUAL_OP;
		}else if(tokenNames[operator.tokenType] == "NOT_EQUAL"){
			return NOT_EQUAL_OP;
		}else if(tokenNames[operator.tokenType] == "REF_EQUAL"){
			return REF_EQUAL_OP;
		}else if(tokenNames[operator.tokenType] == "REF_NOT_EQUAL"){
			return REF_NOT_EQUAL_OP;
		}else if (tokenNames[operator.tokenType] == "UNARY_MINUS"){
			return MINUS_OP;
		}else if (tokenNames[operator.tokenType] == "UNARY_PLUS"){
			return PLUS_OP;
		}else if (tokenNames[operator.tokenType] == "NOT"){
			return NOT_OP;
		}else if (tokenNames[operator.tokenType] == "BIT_COMPLEMENT"){
			return BIT_COMPLEMENT_OP;
		}else if (tokenNames[operator.tokenType] == "UNTAINT")  {
			return UNTAINT_TYPE;
		}else{
			return ERROR_OP;
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
		if(self.top != 0){
			self.top = self.top - 1;
			Token operatorToken = self.opStack[self.top];
			return operatorToken;
		}else{
		return { tokenType: PARSER_ERROR_TOKEN, text: "<missing operator>" , startPos: -1 , endPos:-1,
			lineNumber: 0, index: -1, whiteSpace: "" };
		}
	}
	function peek() returns int {
		int topTkn2 = -1;
		if (self.top != 0) {
			Token topTkn = self.opStack[self.top - 1];
			topTkn2 = topTkn.tokenType;
		}
		return topTkn2;
	}
	function isEmpty() returns boolean {
		if(self.opStack.length() == 1){
			return false;
		}
		return true;
	}
	function size() returns int {
		int opSize = self.opStack.length();
		return opSize;
	}
	//ToDo: add the operatory associativity
	function opPrecedence(int opToken) returns int {
		if(opToken == COMMA){
			return 0;
		}else if (opToken == REF_EQUAL  || opToken == REF_NOT_EQUAL){
			return 1;
		}else if (opToken == EQUAL || opToken == NOT_EQUAL){
			return 2;
		}else if (opToken == LT_EQUAL || opToken == GT_EQUAL || opToken == LT || opToken == GT ){
			return 3;
		}else if (opToken == ADD || opToken == SUB) {
			return 4;
		} else if (opToken == DIV || opToken == MUL || opToken == MOD){
			return 5;
		}else if (opToken == UNARY_MINUS || opToken == UNARY_PLUS || opToken == NOT || opToken == BIT_COMPLEMENT ){
			return 6;
		}else if (opToken == COLON){
			return 7;
		}
		return -1;
	}
};

type ExprStack object {
	ExpressionNode[] exprStack = [null];
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
	function isEmpty() returns boolean {
		if(self.exprStack.length() == 1 && self.exprStack[0] == null ){
			return true;
		}
		return false;
	}
	function printStack(){
		int i = 0;
		while (i < self.exprStack.length()){
			io:println(self.exprStack[i]);
			i+= 1;
		}
	}
};
