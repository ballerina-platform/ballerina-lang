// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/log;

class Parser {
    //stack which holds the operators during expression rule
    OperatorStack operatorStack = new;
    //fill the operator map
    OperatorMapper operatorMapper = new;
    //fill value type map
    ValueTypeMapper valueTypeMapper = new;
    //stack which holds the expression for the expression rule
    ExpressionStack expStack = new;
    //keeps track on error recovery
    boolean errorRecovered = true;
    //keep track of error tokens
    Token[] errorTokens = [];
    //error token count
    int errorCount = 0;
    //keeps track on next expected token should be an operator or operand
    boolean expectOperand = true;
    //if invalid occurrence is captured in an expression
    boolean invalidOccurence = false;
    // tuple list expression count
    int tupleListPos = 0;
    //counter in the operator token in list in tuple literal expression
    int separatorCount = 0;
    //there is a operator in the prior
    boolean priorOperator = false;
	//buffer which store the tokens
    private ParserBufferReader parserBuffer;

    public function init(ParserBufferReader parserBuffer) {
        self.parserBuffer = parserBuffer;
    }

    public function parse() returns @tainted PackageNode {
        DefinitionNode?[] defList = [];
        //definition list position
        int pos = 0;
        //parse while the current token is not EOF token
        while (!self.parserBuffer.isEOFToken()) {
            Token currToken = self.parserBuffer.consumeToken();

            if (currToken.tokenType == FUNCTION) {
                FunctionNode functionNode = self.parseFunction(currToken);
                DefinitionNode defNode = functionNode;
                defList[pos] = defNode;
                pos += 1;
            }
        }
        //consume the EOF Token
        Token currToken = self.parserBuffer.consumeToken();
        PackageNode pkNode = {
            nodeKind: PACKAGE_NODE,
            tokenList: [currToken],
            definitionList: <DefinitionNode[]>(defList.cloneReadOnly())
        }
        return pkNode;
    }

    # Method to consume the token from the token buffer if the token matches the expected token.
    # If the token doesnt match , panic recovery method will be invoked.
    # 
    # + expectedToken - Token type.
    # + rule - Grammar rule to which the token should be parsed.
    # + return - Token.
    function matchToken(int expectedToken, NodeKind rule) returns @tainted Token {
        if (self.LAToken(1) == expectedToken) {
            Token currToken = self.parserBuffer.consumeToken();
            return currToken;
        } else {
            if (self.LAToken(1) == LEXER_ERROR_TOKEN) {
                log:printError(self.LookaheadToken(1).lineNumber.toString() + ":" + self.LookaheadToken(1).startPos.toString() + ": expected " +
                    tokenNames[expectedToken] + ";found an invalid token '" + self.LookaheadToken(1).text + "'");
            } else {
                log:printError(self.LookaheadToken(1).lineNumber.toString() + ":" + self.LookaheadToken(1).startPos.toString() + ": expected " +
                    tokenNames[expectedToken] + "; found '" + self.LookaheadToken(1).text + "'");
            }
            Token panicToken = self.panicRecovery(expectedToken, rule);
            return panicToken;
        }
    }

    # Error recovery : token  insertion.
    # The expected token will be inserted inplace of the mismatched token.
    # 
    # + expectedToken - Token type of the expected token.
    # + return - Token.
    function insertToken(int expectedToken) returns @tainted Token {
        log:printError(tokenNames[expectedToken] + " inserted");
        return {
            tokenType: expectedToken,
            text: tokenNames[expectedToken],
            startPos: -1,
            endPos: -1,
            lineNumber: 0,
            index: -1,
            whiteSpace: ""
        };
    }

    # Error recovery : delete token.
    # Mismatched token is consumed and added to the error token list.
    # 
    # + return - Token
    function deleteToken() returns @tainted Token {
        Token invalidToken = self.parserBuffer.consumeToken();
        log:printError(invalidToken.lineNumber.toString() + ":" + invalidToken.startPos.toString() + ": invalid token '" +
            invalidToken.text + "'");
        self.errorTokens[self.errorCount] = invalidToken;
        self.errorCount += 1;
        self.invalidOccurence = true;
        return invalidToken;
    }

    # Panic error recovery
    # Each grammar rule will have a specific error recovery.
    # Here all the token will be removed and added to the error token list until we meet a terminal token
    # 
    # + expectedToken - Token type of the expected token.
    # + rule - Grammar rule which the token should be parsed.
    # + return - Token
    function panicRecovery(int expectedToken, NodeKind rule) returns @tainted Token {
        boolean panicMode = true;
        self.errorRecovered = false;

        if (rule == VAR_DEF_STATEMENT_NODE) {
            if (expectedToken == SEMICOLON) {
                Token insertedSemicolon = self.insertToken(expectedToken);
                insertedSemicolon.text = semicolonSym;
                return insertedSemicolon;
            } else if (expectedToken == ASSIGN) {
                Token insertedAssign = self.insertToken(expectedToken);
                insertedAssign.text = assignSym;
                return insertedAssign;
            } else {
                int[] exprPanic = [ASSIGN, SEMICOLON, RBRACE, EOF];
                while (panicMode) {
                    if (self.LAToken(1) == exprPanic[0] || self.LAToken(1) == exprPanic[1] ||
                        self.LAToken(1) == exprPanic[3]) {
                        break;
                    } else if (self.LAToken(1) == exprPanic[2] && self.LookaheadToken(1).whiteSpace == "\n") {
                        break;
                    }

                    Token currentToken = self.parserBuffer.consumeToken();
                    self.errorTokens[self.errorCount] = currentToken;
                    self.errorCount += 1;
                }
                return {
                    tokenType: PARSER_ERROR_TOKEN,
                    text: "<unexpected Token: Expected " + tokenNames[expectedToken] + ">",
                    startPos: -1,
                    endPos: -1,
                    lineNumber: 0,
                    index: -1,
                    whiteSpace: ""
                };
            }
        } else if (rule == BLOCK_NODE) {
            if (expectedToken == RBRACE) {
                Token insertedRbrace = self.insertToken(expectedToken);
                insertedRbrace.text = rBraceSymbol;
                return insertedRbrace;
            } else if (expectedToken == LBRACE) {
                Token insertedLbrace = self.insertToken(expectedToken);
                insertedLbrace.text = lBraceSymbol;
                return insertedLbrace;
            }
        } else if (rule == FN_SIGNATURE_NODE) {
            int[] functionSignaturePanic = [LBRACE, RBRACE, EOF];

            while (panicMode) {
                if (self.LAToken(1) == functionSignaturePanic[0]) {
                    break;
                } else if (self.LAToken(1) == functionSignaturePanic[1] || self.LAToken(1) == functionSignaturePanic[2]) {
                    break;
                }
                Token currentToken = self.parserBuffer.consumeToken();
                self.errorTokens[self.errorCount] = currentToken;
                self.errorCount += 1;
            }
            return {
                tokenType: PARSER_ERROR_TOKEN,
                text: "<unexpected Token: Expected " + tokenNames[expectedToken] + ">",
                startPos: -1,
                endPos: -1,
                lineNumber: 0,
                index: -1,
                whiteSpace: ""
            };
        } else if (rule == STATEMENT_NODE) {
            self.errorRecovered = false;
            int[] statementPanic = [SEMICOLON, RBRACE];
            while (panicMode) {
                if (self.LAToken(1) == statementPanic[0]) {
                    panicMode = false;
                } else if (self.LAToken(1) == statementPanic[1]) {
                    break;
                }
                Token currentToken = self.parserBuffer.consumeToken();
                self.errorTokens[self.errorCount] = currentToken;
                self.errorCount += 1;
            }
            return {
                tokenType: PARSER_ERROR_TOKEN,
                text: "<unexpected Token: Expected " + tokenNames[expectedToken] + ">",
                startPos: -1,
                endPos: -1,
                lineNumber: 0,
                index: -1,
                whiteSpace: ""
            };
        } else if (rule == CONTINUE_STATEMENT_NODE) {
            if (expectedToken == SEMICOLON) {
                Token insertedSemicolon = self.insertToken(expectedToken);
                insertedSemicolon.text = semicolonSym;
                return insertedSemicolon;
            }
        }
        //this return statement execute if the rule doesnt match to any of the above mentioned rules
        return {
            tokenType: PARSER_ERROR_TOKEN,
            text: "<unexpected token, Expected : " + tokenNames[expectedToken] + ">",
            startPos: -1,
            endPos: -1,
            lineNumber: 0,
            index: -1,
            whiteSpace: ""
        };
    }

    # Lookahead the  token-type of the next token.
    # 
    # + lACount - Look-ahead count.
    # + return - int
    function LAToken(int lACount) returns int {
        Token laToken = self.parserBuffer.lookAheadToken(lookAheadCount = lACount);
        return laToken.tokenType;
    }

    # Lookahead the next token.
    # 
    # + laCount - Look-ahead count.
    # + return - Token
    function LookaheadToken(int laCount) returns Token {
        return self.parserBuffer.lookAheadToken(lookAheadCount = laCount);
    }

    # Function definition.
    # | FUNCTION <callable Unit Signature> <callable Unit body>
    # 
    # + currToken - Current token.
    # + return - FunctionNode
    function parseFunction(Token currToken) returns @tainted FunctionNode {
        Token functionToken = currToken;
        //parse function signature
        FunctionUnitSignatureNode signatureNode = self.parseCallableUnitSignature();
        //parse function body
        FunctionBodyNode fnBodyNode = self.parseCallableUnitBody();
        //return Function Node
        FunctionNode functionNode = {
            nodeKind: FUNCTION_NODE,
            tokenList: [functionToken],
            fnSignature: signatureNode,
            blockNode: fnBodyNode
        };
        return functionNode;
    }

    # Callable Unit Signature.
    # | IDENTIFIER ()
    # 
    # + return - FunctionUnitSignatureNode
    function parseCallableUnitSignature() returns @tainted FunctionUnitSignatureNode {
        Token identifier = self.matchToken(IDENTIFIER, FN_SIGNATURE_NODE);
        //if the identifier is mismatched, then panic recovery method is executed and
        // tokens upto lbrace will be consumed.
        // the error tokens will be added to errToken list
        if (identifier.tokenType == PARSER_ERROR_TOKEN) {
            ErrorIdentifierNode errorIdNode = {
                nodeKind: ERROR_IDENTIFIER_NODE,
                tokenList: [],
                identifier: ()
            };
            ErrorFunctionSignatureNode fSignature = {
                nodeKind: ERROR_FN_SIGNATURE_NODE,
                tokenList: self.errorTokens,
                functionIdentifier: errorIdNode
            };
            self.resetErrorFlag();
            return fSignature;
        } else {
            IdentifierNode idNode = {
                nodeKind: IDENTIFIER_NODE,
                tokenList: [identifier],
                identifier: identifier.text
            };
            //error recovery for lParen and rParen : panic error recovery
            //tokens will be consumed until a lBrace is reached
            Token lParen = self.matchToken(LPAREN, FN_SIGNATURE_NODE);
            if (lParen.tokenType == PARSER_ERROR_TOKEN) {
                ErrorFunctionSignatureNode fSignature = {
                    nodeKind: ERROR_FN_SIGNATURE_NODE,
                    tokenList: self.errorTokens,
                    functionIdentifier: idNode
                };
                self.resetErrorFlag();
                return fSignature;
            } else {
                Token rParen = self.matchToken(RPAREN, FN_SIGNATURE_NODE);
                if (rParen.tokenType == PARSER_ERROR_TOKEN) {
                    //appending the errorTokens with lParen which was correctly parsed
                    self.errorTokens = self.appendErrorTknList([lParen]);
                    ErrorFunctionSignatureNode fSignature = {
                        nodeKind: ERROR_FN_SIGNATURE_NODE,
                        tokenList: self.errorTokens,
                        functionIdentifier: idNode
                    };
                    self.resetErrorFlag();
                    return fSignature;
                } else {
                    FunctionSignatureNode fSignature = {
                        nodeKind: FN_SIGNATURE_NODE,
                        tokenList: [lParen, rParen],
                        functionIdentifier: idNode
                    };
                    return fSignature;
                }
            }
        }
    }

    # Append the tokens of a grammar to the errToken list in an error situation
    # 
    # + tokenLst - Token list which should be appended to the error tokenList.
    # + return - Token[]
    function appendErrorTknList(Token[] tokenLst) returns Token[] {
        int errListSize = self.errorTokens.length();
        int count = 0;
        while (count < tokenLst.length()) {
            self.errorTokens[errListSize] = tokenLst[count];
            errListSize += 1;
            count += 1;
        }
        return self.errorTokens;
    }

    # Callable Unit Body.
    # | { <statement*>}
    # error recovery method: token insertion.
    # 
    # + return - FunctionBodyNode
    function parseCallableUnitBody() returns @tainted FunctionBodyNode {
        StatementNode?[] stsList = [];
        //position of statements in stsList
        int pos = 0;
        //token insertion if lBrace is mismatched
        Token lBrace = self.matchToken(LBRACE, BLOCK_NODE);
        //if the lbrace is inserted, set the errorRecovered true, otherwise the statement will be errorStatement
        self.errorRecovered = true;
        while (self.LAToken(1) != RBRACE) {
            if (self.LAToken(1) == EOF) {
                break;
            }
            StatementNode statementNode = self.parseStatement();
            stsList[pos] = statementNode;
            pos += 1;
        }
        //Token insertion if rBrace not found
        Token rBrace = self.matchToken(RBRACE, BLOCK_NODE);
        //the error token which is returned in an error situation, have the token endPosition set as -1
        if (rBrace.endPos == -1 || lBrace.endPos == -1) {
            ErrorBlockNode erNode = {
                nodeKind: ERROR_BLOCK_NODE,
                tokenList: [lBrace, rBrace],
                statementList: <StatementNode[]>(stsList.cloneReadOnly())
            };
            self.resetErrorFlag();
            return erNode;
        } else {
            BlockNode blNode = {
                nodeKind: BLOCK_NODE,
                tokenList: [lBrace, rBrace],
                statementList: <StatementNode[]>(stsList.cloneReadOnly())
            };
            return blNode;
        }
    }

    # Statement
    # |<variable definition statement>
    # |<continue statement>
    # 
    # + return - StatementNode?
    function parseStatement() returns @tainted StatementNode {
        //check if the LA token belongs to any of the statement type or else return an error node
        if (self.LAToken(1) == INT) {
            VariableDefStNode vDefNode = self.parseVariableDefinitionStatementNode();
            return vDefNode;
        } else if (self.LAToken(1) == CONTINUE) {
            ContinueStNode continueNode = self.parseContinueStatementNode();
            return continueNode;
        } else {
            log:printError(self.LookaheadToken(1).lineNumber.toString() + ":" + self.LookaheadToken(1).startPos.toString() +
                ": no viable statement type found for token: '" + self.LookaheadToken(1).text + "'");
            Token panicToken = self.panicRecovery(INT, STATEMENT_NODE);

            ErrorStatementNode erNode = {
                nodeKind: ERROR_STATEMENT_NODE,
                tokenList: self.errorTokens
            };
            self.resetErrorFlag();
            return erNode;
        }
    }

    # Variable definition statement
    # | <valueTypeName> IDENTIFIER SEMICOLON // int a;
    # | <valeuTypeName>  IDENTIFIER ASSIGN <expression> SEMICOLON // int a  = b + 8;
    # 
    # + return - VariableDefinitionStatementNode
    function parseVariableDefinitionStatementNode() returns @tainted VariableDefStNode {
        //value type token
        Token valTypeToken = self.parseValueTypeName();
        //it is not necessary to check the validity of the value type since,
        //we only call this method if its a valid value type name.
        ValueKind valueKind1 = self.matchValueType(valTypeToken);
        Token identifier = self.matchToken(IDENTIFIER, VAR_DEF_STATEMENT_NODE);

        //if the identifier is mismatched,
        //the tokens will be consumed until ASSIGN or SEMICOLON is found.
        if (identifier.tokenType == PARSER_ERROR_TOKEN) {
            ErrorVarRefIdentifierNode vRefIdNode = {
                nodeKind: ERROR_VAR_DEF_IDENTIFIER_NODE,
                tokenList: self.errorTokens,
                varIdentifier: ()
            };
            self.resetErrorFlag();

            if (self.LAToken(1) == SEMICOLON) {
                Token semicolon = self.matchToken(SEMICOLON, VAR_DEF_STATEMENT_NODE);

                VariableDefinitionStatementNode vDefStatement = {
                    nodeKind: VAR_DEF_STATEMENT_NODE,
                    tokenList: [valTypeToken, semicolon],
                    valueKind: valueKind1,
                    varIdentifier: vRefIdNode,
                    expression: ()
                };
                self.resetErrorFlag();
                return vDefStatement;
            } else if (self.LAToken(1) == ASSIGN) {
                VariableDefStNode vStatementNode = self.parseVarDefExpr(vRefIdNode, valTypeToken, valueKind1);
                return vStatementNode;
            } else {
                VariableDefinitionStatementNode vDefNode = {
                    nodeKind: VAR_DEF_STATEMENT_NODE,
                    tokenList: [valTypeToken],
                    valueKind: valueKind1,
                    varIdentifier: vRefIdNode,
                    expression: ()
                };
                self.resetErrorFlag();
                return vDefNode;
            }
        }

        //variable definition statement with valid identifier
        VarRefIdentifier vRefIdNode = {
            nodeKind: VAR_REF_NODE,
            tokenList: [identifier],
            varIdentifier: identifier.text
        };

        if (self.LAToken(1) == SEMICOLON) {
            Token semicolon = self.matchToken(SEMICOLON, VAR_DEF_STATEMENT_NODE);
            VariableDefinitionStatementNode vDefStatement = {
                nodeKind: VAR_DEF_STATEMENT_NODE,
                tokenList: [valTypeToken, semicolon],
                valueKind: valueKind1,
                varIdentifier: vRefIdNode,
                expression: ()
            };
            self.resetErrorFlag();
            return vDefStatement;
        } else {
            VariableDefStNode vStatementNode = self.parseVarDefExpr(vRefIdNode, valTypeToken, valueKind1);
            return vStatementNode;
        }
    }

    # Reset the errorCount, and set error recovered to True, once an error is recovered.
    function resetErrorFlag() {
        self.errorCount = 0;
        self.errorTokens = [];
        self.errorRecovered = true;
    }

    # Parse ( ASSING EXPR SEMICOLON ) in variable definition statement.
    # 
    # + vRefIdNode - variable reference Node
    # + valTypeToken - value type Token
    # + valueKind1 - value kind of the valueType token.
    # + return - VariableDefStNode
    function parseVarDefExpr(VariableReferenceNode vRefIdNode, Token valTypeToken, ValueKind valueKind1) returns @tainted VariableDefStNode {
        //token insertion if token assign is mismatched
        Token assign = self.matchToken(ASSIGN, VAR_DEF_STATEMENT_NODE);
        self.errorRecovered = true;
        ExpressionNode exprNode = self.expressionBuilder(VAR_DEF_STATEMENT_NODE);
        //if no semicolon found in the end of the expr,
        //then errorRecovered will set to false within the expressionBuilder method itself
        if (exprNode is ()) {
            log:printError(assign.lineNumber.toString() + ":" + assign.endPos.toString() +
                " : no valid expression found in variable definition statement.");
            self.errorRecovered = false;
        }
        if (self.errorRecovered == false || self.invalidOccurence == true) {
            ErrorExpressionNode erNode = {
                nodeKind: ERROR_EXPRESSION_NODE,
                tokenList: self.errorTokens,
                errorExpression: exprNode
            };
            self.resetErrorFlag();
            self.invalidOccurence = false;
            Token semicolon = self.matchToken(SEMICOLON, VAR_DEF_STATEMENT_NODE);
            //tokens inserted using token insertion method will have the token end position as -1
            if (semicolon.endPos == -1 || assign.endPos == -1) {
                ErrorVarDefStatementNode errSt = {
                    nodeKind: ERROR_VAR_DEF_STATEMENT_NODE,
                    tokenList: [valTypeToken, assign, semicolon],
                    valueKind: valueKind1,
                    varIdentifier: vRefIdNode,
                    expression: erNode
                };
                self.resetErrorFlag();
                return errSt;
            } else {
                VariableDefinitionStatementNode vDefNode = {
                    nodeKind: VAR_DEF_STATEMENT_NODE,
                    tokenList: [valTypeToken, assign, semicolon],
                    valueKind: valueKind1,
                    varIdentifier: vRefIdNode,
                    expression: erNode
                };
                return vDefNode;
            }
        } else {
            //token insertion if semicolon is mismatched
            Token semicolon = self.matchToken(SEMICOLON, VAR_DEF_STATEMENT_NODE);
            if (semicolon.endPos == -1 || assign.endPos == -1) {
                ErrorVarDefStatementNode errSt = {
                    nodeKind: ERROR_VAR_DEF_STATEMENT_NODE,
                    tokenList: [valTypeToken, assign, semicolon],
                    valueKind: valueKind1,
                    varIdentifier: vRefIdNode,
                    expression: exprNode
                };
                self.resetErrorFlag();
                return errSt;
            } else {
                VariableDefinitionStatementNode vDefNode = {
                    nodeKind: VAR_DEF_STATEMENT_NODE,
                    tokenList: [valTypeToken, assign, semicolon],
                    valueKind: valueKind1,
                    varIdentifier: vRefIdNode,
                    expression: exprNode
                };
                self.resetErrorFlag();
                return vDefNode;
            }
        }
    }

    # Continue statement
    # |CONTINUE SEMICOLON
    # 
    # + return - ContinueStatementNode
    function parseContinueStatementNode() returns @tainted ContinueStNode {
        Token valTypeToken = self.parseValueTypeName();
        ValueKind valueKind1 = self.matchValueType(valTypeToken);

        if (self.LAToken(1) == SEMICOLON) {
            Token semicolon = self.matchToken(SEMICOLON, CONTINUE_STATEMENT_NODE);
            ContinueStatementNode continueStatement = {
                nodeKind: CONTINUE_STATEMENT_NODE,
                tokenList: [valTypeToken, semicolon],
                valueKind: valueKind1
            };
            return continueStatement;
        } else {
            //token insertion if semicolon is mismatched
            Token semicolon = self.matchToken(SEMICOLON, CONTINUE_STATEMENT_NODE);
            ErrorContinueStatementNode errcontinue = {
                nodeKind: ERROR_CONTIUE_STATEMENT_NODE,
                tokenList: [semicolon],
                valueKind: valueKind1
            };
            self.resetErrorFlag();
            return errcontinue;
        }
    }

    # Expression
    # | <simple literal>
    # | <variable reference>
    # | expression (DIVISION | MULTIPLICATION | MOD) expression
    # | expression ( ADD | SUB ) expression
    # | expression (LT_EQUAL | GT_EQUAL | GT | LT)expression
    # | expression (EQUAL | NOT_EQUAL) expression
    # | expression (REF_EQUAL | REF_NOT_EQUAL) expression
    # | (ADD | SUB | NOT | BIT_COMPLEMENT | UNTAINT) expression
    # | <tuple literal>
    #
    # the statementType determines the break condition or the terminal token of the while loop
    # 
    # + statementType - type of the statement, in which the expression is included.
    # + return - ExpressionNode
    function expressionBuilder(string statementType) returns @tainted ExpressionNode {
        //position count of the tuple list
        self.tupleListPos = 0;

        //the statements which terminate with semicolon and the statements which will terminate with other tokens such as
        //foreach statement which ends will be separated to different if statements.
        if (statementType == VAR_DEF_STATEMENT_NODE) {
            //tracks the validity of the expression received from the expression helper function
            boolean isExpr = true;
            while (self.LAToken(1) != SEMICOLON && isExpr == true) {
                isExpr = self.parseExpression();

                if (isExpr == false) {
                    self.errorRecovered = false;
                }
            }
        }
        //if the expression stack contains any operators, build the expressions based on the operators
        self.buildFinalExpr();
        //pop the final expression from the expr stack
        ExpressionNode expressionNode = self.expStack.pop();
        return expressionNode;
    }

    # Build the expression based on the operators remain in the operator stack
    function buildFinalExpr() {
        //tuple expression list
        ExpressionNode?[] tupleList = [];
        //list to keep track of commas
        Token[] commaList = [];
        while (self.operatorStack.peek() != -1) {
            Token operator = self.operatorStack.pop();

            if (operator.tokenType == LPAREN) {
                log:printError(operator.lineNumber.toString() + ":" + operator.startPos.toString() + " : invalid tuple literal, missing ')'");
                self.invalidOccurence = true;

                commaList[self.separatorCount] = operator;
                self.separatorCount += 1;
                ExpressionNode commaExpr = self.expStack.pop();
                tupleList[self.tupleListPos] = commaExpr;
                self.tupleListPos += 1;
                //reversing the array so that the expressions in the tuple list will be in proper order
                tupleList = self.reverseTupleList(tupleList);

                TupleLiteralNode tupleLNode = {
                    nodeKind: TUPLE_LITERAL_NODE,
                    tokenList: commaList,
                    tupleExprList:<ExpressionNode[]>(tupleList.cloneReadOnly())
                };
                self.expStack.push(tupleLNode);
                self.tupleListPos = 0;
            //build the unary expressions
            } else if ((operator.tokenType >= NOT && operator.tokenType <= UNARY_PLUS)) {
                //unary_minus and unary_plus token types are converted to ADD and SUB tokenType
                self.convertToValidOp(operator);
                OperatorKind oprKind = self.matchOperatorType(operator);

                if (self.expectOperand == true) {
                    self.expectOperand = false;
                    log:printError(operator.lineNumber.toString() + ":" + operator.startPos.toString() + " : missing unary expression");
                    self.invalidOccurence = true;
                    UnaryExpressionNode uExpression = {
                        nodeKind: UNARY_EXPRESSION_NODE,
                        tokenList: [operator],
                        operatorKind: oprKind,
                        uExpression: ()
                    };
                    self.expStack.push(uExpression);
                } else {
                    ExpressionNode expressionNode = self.expStack.pop();
                    UnaryExpressionNode uExpression = {
                        nodeKind: UNARY_EXPRESSION_NODE,
                        tokenList: [operator],
                        operatorKind: oprKind,
                        uExpression: expressionNode
                    };
                    self.expStack.push(uExpression);
                }
            }
            else if (operator.tokenType == COMMA) {
                if (self.expectOperand == true) {
                    log:printError(operator.lineNumber.toString() + ":" + operator.startPos.toString() + " : missing expression after comma");
                    //token deletion without calling the method,
                    //as the comma token is already consumed and pushed to the opr stack
                    self.errorTokens[self.errorCount] = operator;
                    self.errorCount += 1;
                    self.invalidOccurence = true;
                    continue;
                }

                self.invalidOccurence = true;
                self.errorRecovered = false;
                commaList[self.separatorCount] = operator;
                self.separatorCount += 1;
                ExpressionNode commaExpr = self.expStack.pop();
                tupleList[self.tupleListPos] = commaExpr;
                self.tupleListPos += 1;
            } else {
                OperatorKind opKind = self.matchOperatorType(operator);
                ExpressionNode expr2 = self.expStack.pop();
                ExpressionNode expr1 = self.expStack.pop();
                if (expr1 is ()) {
                    self.errorRecovered = false;
                    log:printError(operator.lineNumber.toString() + ":" + operator.startPos.toString() +
                        " : invalid binary expression, binary RHS expression not found");
                    BinaryExpressionNode binaryExpression = {
                        nodeKind: BINARY_EXP_NODE,
                        tokenList: [operator],
                        operatorKind: opKind,
                        leftExpr: expr2,
                        rightExpr: expr1
                    };
                    self.expStack.push(binaryExpression);
                } else {
                    BinaryExpressionNode binaryExpression = {
                        nodeKind: BINARY_EXP_NODE,
                        tokenList: [operator],
                        operatorKind: opKind,
                        leftExpr: expr1,
                        rightExpr: expr2
                    };
                    self.expStack.push(binaryExpression);
                }
            }
        }
        //here only comma separated expression is found, missing lParen and rParen
        if (self.tupleListPos > 0) {
            Token lastOperator = commaList[commaList.length() - 1];
            log:printError(lastOperator.lineNumber.toString() + ":" + lastOperator.startPos.toString() + " : invalid tuple literal expression");
            ExpressionNode commaExpr = self.expStack.pop();
            tupleList[self.tupleListPos] = commaExpr;
            self.tupleListPos += 1;
            tupleList = self.reverseTupleList(tupleList);
            TupleLiteralNode tupleLNode = {
                nodeKind: TUPLE_LITERAL_NODE,
                tokenList: commaList,
                tupleExprList:<ExpressionNode[]>(tupleList.cloneReadOnly())
            };
            self.expStack.push(tupleLNode);
        }
        self.separatorCount = 0;
        self.tupleListPos = 0;
    }

    # Reversing the tupleList so that the expressions in the tuple list will be in proper order.
    # 
    # + tupleList - list that should be reversed.
    # + return - ExpressionNode?[]
    function reverseTupleList(ExpressionNode?[] tupleList) returns ExpressionNode?[] {
        int reverseCount = 0;
        while (reverseCount < tupleList.length() / 2) {
            ExpressionNode temp = tupleList[reverseCount];
            tupleList[reverseCount] = tupleList[tupleList.length() - reverseCount - 1];
            tupleList[tupleList.length() - reverseCount - 1] = temp;
            reverseCount += 1;
        }
        return tupleList;
    }

    # Convert the Unary_plus and unary_minus operators back to the valid types (ADD,SUB)
    # 
    # + operator - operator token.
    function convertToValidOp(Token operator) {
        if (operator.tokenType == UNARY_MINUS) {
            operator.tokenType = SUB;
        } else if (operator.tokenType == UNARY_PLUS) {
            operator.tokenType = ADD;
        }
    }

    # Helper function to build the expression.
    # Expression is parsed using shunting yard algorithm
    # 
    # + return - boolean
    function parseExpression() returns boolean {
        if (self.LAToken(1) == LPAREN) {
            self.priorOperator = false;
            //Lparen is expected after a operator,
            //After consuming an operator, the expectOperand must be set to True.
            //but here since the expectOperand is False, that means the previous token must be an operand
            //which causes an invalid occurrence scenario.
            //therefore the token is deleted and added to the errToken list
            if (self.expectOperand == false) {
                Token invalidToken = self.deleteToken();
                return true;
            } else {
                Token lParen = self.matchToken(LPAREN, EXPRESSION_NODE);
                //check for empty tuple literal
                if (self.LAToken(1) == RPAREN) {
                    Token rParen = self.matchToken(RPAREN, EXPRESSION_NODE);
                    EmptyTupleLiteralNode emptyTuple = {
                        nodeKind: EMPTY_TUPLE_LITERAL_NODE,
                        tokenList: [lParen, rParen]
                    };
                    SimpleLiteral simpleLiteral = emptyTuple;
                    self.expStack.push(simpleLiteral);
                    self.expectOperand = false;
                    return true;
                } else {
                    self.operatorStack.push(lParen);
                    self.expectOperand = true;
                    return true;
                }
            }
        } else if (self.LAToken(1) == NUMBER) {
            self.priorOperator = false;
            if (self.expectOperand == false) {
                Token invalidToken = self.deleteToken();
                return true;
            } else {
                Token number = self.matchToken(NUMBER, EXPRESSION_NODE);
                IntegerLiteralNode intLit = {
                    nodeKind: INTEGER_LITERAL,
                    tokenList: [number],
                    number: number.text
                };
                SimpleLiteral sLit = intLit;
                self.expStack.push(sLit);
                self.expectOperand = false;
                return true;
            }
        } else if (self.LAToken(1) == IDENTIFIER) {
            self.priorOperator = false;
            if (self.expectOperand == false) {
                Token invalidToken = self.deleteToken();
                return true;
            } else {
                Token identifier = self.matchToken(IDENTIFIER, EXPRESSION_NODE);
                VarRefIdentifier varRef = {
                    nodeKind: VAR_REF_NODE,
                    tokenList: [identifier],
                    varIdentifier: identifier.text
                };
                self.expStack.push(varRef);
                self.expectOperand = false;
                return true;
            }
        } else if ((self.LAToken(1) >= ADD && self.LAToken(1) <= UNTAINT) || self.LAToken(1) == COMMA) { //binary, unary operators and comma

            //if the expression stack is empty then that means no prior expression , so this is a unary expr
            if (self.expStack.isEmpty() == true || self.priorOperator == true) {
                if (self.LAToken(1) == SUB || self.LAToken(1) == ADD ||
                   (self.LAToken(1) >= NOT && self.LAToken(1) <= UNTAINT)) { // unary operators

                    if (self.LAToken(1) == SUB) {
                        Token unarySub = self.matchToken(SUB, EXPRESSION_NODE);
                        unarySub.tokenType = UNARY_MINUS;
                        self.operatorStack.push(unarySub);
                    } else if (self.LAToken(1) == ADD) {
                        Token unaryAdd = self.matchToken(ADD, EXPRESSION_NODE);
                        unaryAdd.tokenType = UNARY_PLUS;
                        self.operatorStack.push(unaryAdd);
                    } else {
                        Token unaryOpToken = self.matchToken(self.LAToken(1), EXPRESSION_NODE);
                        self.operatorStack.push(unaryOpToken);
                    }
                    self.expectOperand = true;
                    self.priorOperator = true;
                    return true;
                } else { //Operator which is not an unary operator,
                //Eg:(,2) in such instance where the comma is in invalid position comma is deleted and added to the errorlist.
                    Token invalidToken = self.deleteToken();
                    return true;
                }
            } else {
                //setting the prior operator to true
                self.priorOperator = true;
                while (self.operatorStack.opPrecedence(self.operatorStack.peek()) >= self.operatorStack.opPrecedence(self.LAToken(1))) {
                    //if the lookahead token is a comma and also the opr stack peek is also a comma,
                    //then we dont pop the comma from the stack Eg: (2,3)
                    //it will be popped in the end when we reach the RPAREN
                    if (self.operatorStack.peek() == COMMA) {
                        if (self.LAToken(1) == COMMA) {
                            Token comma = self.matchToken(COMMA, EXPRESSION_NODE);
                            self.operatorStack.push(comma);
                            self.expectOperand = true;
                            return true;
                        }
                    }
                    Token operator = self.operatorStack.pop();
                    //build unary expressions, and push it to the expression stack
                    if ((operator.tokenType >= NOT && operator.tokenType <= UNARY_PLUS)) { // unary operators
                        self.priorOperator = false;
                        //unary_minus and unary_plus token types are converted to ADD and SUB tokenType
                        self.convertToValidOp(operator);

                        OperatorKind oprKind = self.matchOperatorType(operator);
                        ExpressionNode expressionNode = self.expStack.pop();
                        UnaryExpressionNode uExpression = {
                            nodeKind: UNARY_EXPRESSION_NODE,
                            tokenList: [operator],
                            operatorKind: oprKind,
                            uExpression: expressionNode
                        };
                        self.expStack.push(uExpression);
                    } else { //build binary expressions by popping two expressions and push it to the expression stack
                        OperatorKind opKind = self.matchOperatorType(operator);
                        ExpressionNode expr2 = self.expStack.pop();
                        ExpressionNode expr1 = self.expStack.pop();
                        BinaryExpressionNode binaryExpression = {
                            nodeKind: BINARY_EXP_NODE,
                            tokenList: [operator],
                            operatorKind: opKind,
                            leftExpr: expr1,
                            rightExpr: expr2
                        };
                        self.expStack.push(binaryExpression);
                    }
                }
                //consume the binary operators and push them to the stack
                if ((self.LAToken(1) >= ADD && self.LAToken(1) <= REF_EQUAL) || self.LAToken(1) == COMMA) {
                    Token binaryOpToken = self.matchToken(self.LAToken(1), EXPRESSION_NODE);
                    self.operatorStack.push(binaryOpToken);
                }
                self.expectOperand = true;
                return true;
            }
        } else if (self.LAToken(1) == RPAREN) {
            //tuple expression list
            ExpressionNode?[] tupleList = [];
            //list to keep track of commas
            Token[] commaList = [];

            Token rParen = self.matchToken(RPAREN, EXPRESSION_NODE);
            commaList[self.separatorCount] = rParen;
            self.separatorCount += 1;
            while (self.operatorStack.peek() != LPAREN) {
                Token operator = self.operatorStack.pop();
                if (operator.tokenType == PARSER_ERROR_TOKEN) {
                    //considering only possible option to return parser error token would be a missing lparen
                    log:printError(operator.lineNumber.toString() + ":" + operator.startPos.toString() +
                        " : invalid tuple literal, missing '('");
                    Token insertlParen = self.insertToken(LPAREN);
                    insertlParen.text = "(";
                    self.operatorStack.push(insertlParen);
                    self.invalidOccurence = true;
                    break;
                } else if (operator.tokenType == COMMA) {
                    if (self.expectOperand == true) {
                        log:printError(operator.lineNumber.toString() + ":" + operator.startPos.toString() +
                            " : missing expression after comma");
                        //token deletion without calling the method,
                        // because the comma token is already consumed and pushed to the opr stack
                        self.errorTokens[self.errorCount] = operator;
                        self.errorCount += 1;
                        self.invalidOccurence = true;
                        continue;
                    }
                    commaList[self.separatorCount] = operator;
                    self.separatorCount += 1;

                    ExpressionNode commaExpr = self.expStack.pop();
                    tupleList[self.tupleListPos] = commaExpr;
                    self.tupleListPos += 1;
                    continue;
                } else if ((operator.tokenType >= NOT && operator.tokenType <= UNARY_PLUS)) { // unary operators
                    //unary_minus and unary_plus token types are converted to ADD and SUB tokenType
                    self.convertToValidOp(operator);
                    OperatorKind oprKind = self.matchOperatorType(operator);
                    if (self.expectOperand == true) {
                        self.expectOperand = false;
                        log:printError(operator.lineNumber.toString() + ":" + operator.startPos.toString() + " : missing unary expression");

                        self.invalidOccurence = true;
                        UnaryExpressionNode uExpression = {
                            nodeKind: UNARY_EXPRESSION_NODE,
                            tokenList: [operator],
                            operatorKind: oprKind,
                            uExpression: ()
                        };
                        self.expStack.push(uExpression);
                    } else {
                        ExpressionNode expressionNode = self.expStack.pop();
                        UnaryExpressionNode uExpression = {
                            nodeKind: UNARY_EXPRESSION_NODE,
                            tokenList: [operator],
                            operatorKind: oprKind,
                            uExpression: expressionNode
                        };
                        self.expStack.push(uExpression);
                    }
                } else {
                    if (self.expectOperand == true) {
                        log:printError(operator.lineNumber.toString() + ":" + operator.startPos.toString() +
                            " : invalid binary expression, binary RHS expression not found");
                        self.invalidOccurence = true;
                        OperatorKind opKind = self.matchOperatorType(operator);
                        ExpressionNode expr1 = self.expStack.pop();
                        BinaryExpressionNode binaryExpression = {
                            nodeKind: BINARY_EXP_NODE,
                            tokenList: [operator],
                            operatorKind: opKind,
                            leftExpr: expr1,
                            rightExpr: ()
                        };
                        self.expStack.push(binaryExpression);
                        self.expectOperand = false;
                    } else {
                        OperatorKind opKind = self.matchOperatorType(operator);
                        ExpressionNode expr2 = self.expStack.pop();
                        ExpressionNode expr1 = self.expStack.pop();
                        BinaryExpressionNode binaryExpression = {
                            nodeKind: BINARY_EXP_NODE,
                            tokenList: [operator],
                            operatorKind: opKind,
                            leftExpr: expr1,
                            rightExpr: expr2
                        };
                        self.expStack.push(binaryExpression);
                    }
                }
            }
            //popping the lParen
            Token leftParen = self.operatorStack.pop();
            commaList[self.separatorCount] = leftParen;
            self.separatorCount += 1;

            ExpressionNode parentExpr = self.expStack.pop();
            if (parentExpr is ()) {
                EmptyTupleLiteralNode emptyTuple = {
                    nodeKind: EMPTY_TUPLE_LITERAL_NODE,
                    tokenList: commaList
                };
                SimpleLiteral simpleLiteral = emptyTuple;
                self.expStack.push(simpleLiteral);
            } else {
                tupleList[self.tupleListPos] = parentExpr;
                self.tupleListPos += 1;
                //reversing the array so that the expressions in the tuple list will be in proper order
                tupleList = self.reverseTupleList(tupleList);

                TupleLiteralNode tupleLNode = {
                    nodeKind: TUPLE_LITERAL_NODE,
                    tokenList: commaList,
                    tupleExprList:<ExpressionNode[]>(tupleList.cloneReadOnly())
                };
                self.expStack.push(tupleLNode);
                self.tupleListPos = 0;
            }
            self.separatorCount = 0;
            self.expectOperand = false;
            self.priorOperator = false;
            return true;
        }
        return false;
    }

    # ValueTypeName
    # | INT
    # | STRING
    # 
    # + return - Token
    function parseValueTypeName() returns @tainted Token {
        if (self.LAToken(1) == INT) {
            Token intType = self.matchToken(INT, STATEMENT_NODE);
            return intType;
        } else if (self.LAToken(1) == STRING) {
            Token stringType = self.matchToken(STRING, STATEMENT_NODE);
            return stringType;
        } else if (self.LAToken(1) == CONTINUE) {
            Token continueType = self.matchToken(CONTINUE, CONTINUE_STATEMENT_NODE);
            return continueType;
        } else {
            return {
                tokenType: PARSER_ERROR_TOKEN,
                text: "<unexpected " + tokenNames[self.LAToken(1)] + ">",
                startPos: -1,
                endPos: -1,
                lineNumber: 0,
                index: -1,
                whiteSpace: ""
            };
        }
    }

    //check the value type for the given token
    function matchValueType(Token valTypeToken) returns ValueKind {
        var valKind = self.valueTypeMapper.valueKindMap[tokenNames[valTypeToken.tokenType]];

        if (valKind is ValueKind) {
            return valKind;
        } else {
            return ERROR_VALUE_TYPE;
        }
    }

    //check the operator kind of the operator token
    function matchOperatorType(Token operator) returns OperatorKind {
        var oprKind = self.operatorMapper.operatorTypeMap[tokenNames[operator.tokenType]];

        if (oprKind is OperatorKind) {
            return oprKind;
        } else {
            return ERROR_OP;
        }
    }
};

# Operator stack to store the operators
class OperatorStack {
    Token[] oprStack = [];
    int top;

    public function init(int top = 0) {
        self.top = top;
    }

    function push(Token token2) {
        self.oprStack[self.top] = token2;
        self.top = self.top + 1;
    }

    function pop() returns Token {
        if (self.top != 0) {
            self.top = self.top - 1;
            Token operatorToken = self.oprStack[self.top];
            return operatorToken;
        } else {
            return {
                tokenType: PARSER_ERROR_TOKEN,
                text: "<missing operator>",
                startPos: -1,
                endPos: -1,
                lineNumber: 0,
                index: -1,
                whiteSpace: ""
            }
        }
    }

    function peek() returns int {
        int topToken = -1;
        if (self.top != 0) {
            Token peekToken = self.oprStack[self.top - 1];
            topToken = peekToken.tokenType;
        }
        return topToken;
    }

    function isEmpty() returns boolean {
        if (self.oprStack.length() == 1) {
            return false;
        }
        return true;
    }

    function size() returns int {
        return self.oprStack.length();
    }

    function opPrecedence(int opToken) returns int {
        if (opToken == COMMA) {
            return 0;
        } else if (opToken == REF_EQUAL || opToken == REF_NOT_EQUAL) {
            return 2;
        } else if (opToken == EQUAL || opToken == NOT_EQUAL) {
            return 3;
        } else if (opToken == LT_EQUAL || opToken == GT_EQUAL || opToken == LT || opToken == GT) {
            return 4;
        } else if (opToken == ADD || opToken == SUB) {
            return 5;
        } else if (opToken == DIV || opToken == MUL || opToken == MOD) {
            return 6;
        } else if (opToken == UNARY_MINUS || opToken == UNARY_PLUS || opToken == NOT || opToken == BIT_COMPLEMENT ||
                   opToken == UNTAINT) {
            return 7;
        }
        return -1;
    }
};

# Expression stack stores each expression built
class ExpressionStack {
    ExpressionNode[] exprStack = [()];
    int top;

    public function init(int top = 0) {
        self.top = top;
    }

    function push(ExpressionNode token2) {
        self.exprStack[self.top] = token2;
        self.top = self.top + 1;
    }

    function pop() returns ExpressionNode {
        if (self.top == 0) {
            return;
        }
        self.top = self.top - 1;
        return self.exprStack[self.top];
    }

    function topExpr() returns ExpressionNode {
        return self.exprStack[self.top - 1];
    }

    function isEmpty() returns boolean {
        return self.exprStack.length() == 1 && self.exprStack[0] is ();
    }
}
