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

import ballerina/io;
import ballerina/log;

type Parser object {
    //stack which holds the operators during expression rule
    OperatorStack oprStack = new();
    //fill the operator map
    OperatorMapper oprMap = new();
    //fill value type map
    ValueTypeMapper valMap = new();
    //stack which holds the expression for the expression rule
    ExprStack expStack = new();
    //keeps track on error recovery
    boolean errorRecovered = true;
    //keep track of error tokens
    Token[] errTokens = [];
    //error token count
    int errCount = 0;
    //keeps track on next expected token should be an operator or operand
    boolean expOperand = true;
    //if invalid occurence is captured in an expression
    boolean invalidOccurence = false;
    // tuple list expression count
    int tupleListPos = 0;
    //counter in the operator token in list in tuple literal expression
    int separatorCount = 0;
    //there is a operator in the prior
    boolean priorOperator = false;


    private ParserBufferReader parserBuffer;

    public function __init(ParserBufferReader parserBuffer) {
        self.parserBuffer = parserBuffer;
    }

    public function parse() returns PackageNode {
        DefinitionNode?[] dList = [];
        //definition list position
        int pos = 0;
        //parse while the current token is not EOF token
        while (!self.parserBuffer.isEOFToken()) {
            Token currToken = self.parserBuffer.consumeToken();

            if (currToken.tokenType == FUNCTION) {
                FunctionNode function1 = self.parseFunction(currToken);
                DefinitionNode defNode = function1;
                dList[pos] = defNode;
                pos += 1;
            }
        }
        //consume the EOF Token
        Token currToken = self.parserBuffer.consumeToken();
        PackageNode pkNode = {
            nodeKind: PACKAGE_NODE,
            tokenList: [currToken],
            definitionList: <DefinitionNode[]>(dList.freeze())
        };
        return pkNode;
    }

    #method to consume the token from the token buffer if the token matches the expected token
    #if the token doesnt match , panic recovery method will be invoked.
    # +return - Token
    function matchToken(int mToken, NodeKind rule) returns Token {
        if (self.LAToken(1) == mToken) {
            Token currToken = self.parserBuffer.consumeToken();
            return currToken;
        } else {
            if (self.LAToken(1) == LEXER_ERROR_TOKEN) {
                log:printError(self.LookaheadToken(1).lineNumber + ":" + self.LookaheadToken(1).startPos + ": expected " +
                tokenNames[mToken] + ";found an invalid token '" + self.LookaheadToken(1).text + "'");
            } else {
                log:printError(self.LookaheadToken(1).lineNumber + ":" + self.LookaheadToken(1).startPos + ": expected " +
                tokenNames[mToken] + "; found '" + self.LookaheadToken(1).text + "'");
            }

            Token panicToken = self.panicRecovery(mToken, rule);
            return panicToken;
        }
    }

    #error recovery : token  insertion
    # the expected token will be inserted inplace of the mismatched token
    # +return - Token
    function insertToken(int mToken) returns Token {
        log:printError(tokenNames[mToken] + " inserted");
        return {
            tokenType: mToken,
            text: tokenNames[mToken],
            startPos: -1,
            endPos: -1,
            lineNumber: 0,
            index: -1,
            whiteSpace: ""
        };
    }

    #error recovery : delete token
    # mismatched token is consumed and added to the error token list
    # +return - Token
    function deleteToken() returns Token {
        Token invalidToken = self.parserBuffer.consumeToken();
        log:printError(invalidToken.lineNumber + ":" + invalidToken.startPos + ": invalid token '" +
        invalidToken.text + "'");
        self.errTokens[self.errCount] = invalidToken;
        self.errCount += 1;
        self.invalidOccurence = true;
        return invalidToken;
    }

    #panic recovery
    #each grammar rule will have a specific error recovery.
    #here all the token will be removed and added to the error token list until we meet a terminal token
    # +return - Token
    function panicRecovery(int mToken, NodeKind rule) returns Token {
        boolean panicMode = true;
        self.errorRecovered = false;

        if (rule == "variableDefinitionStatement") {
            if (mToken == SEMICOLON) {
                Token insertSemi = self.insertToken(mToken);
                insertSemi.text = ";";

                return insertSemi;
            } else if (mToken == ASSIGN) {
                Token insertAssign = self.insertToken(mToken);
                insertAssign.text = "=";

                return insertAssign;
            } else {
                int[] exprPanic = [ASSIGN, SEMICOLON, RBRACE, EOF];
                while (panicMode) {
                    if (self.LAToken(1) == exprPanic[0]) {
                        break;
                    } else if (self.LAToken(1) == exprPanic[1]) {
                        break;
                    } else if (self.LAToken(1) == exprPanic[2] && self.LookaheadToken(1).whiteSpace == "\n") {
                        break;
                    } else if (self.LAToken(1) == exprPanic[3]) {
                        break;
                    }

                    Token currToken1 = self.parserBuffer.consumeToken();
                    self.errTokens[self.errCount] = currToken1;
                    self.errCount += 1;
                }
                return {
                    tokenType: PARSER_ERROR_TOKEN,
                    text: "<unexpected Token: " + tokenNames[mToken] + ">",
                    startPos: -1,
                    endPos: -1,
                    lineNumber: 0,
                    index: -1,
                    whiteSpace: ""
                };
            }
        } else if (rule == "blockNode") {
            if (mToken == RBRACE) {
                Token insertRbrace = self.insertToken(mToken);
                insertRbrace.text = "}";

                return insertRbrace;
            } else if (mToken == LBRACE) {
                Token insertLbrace = self.insertToken(mToken);
                insertLbrace.text = "{";

                return insertLbrace;
            }

        } else if (rule == "functionSignature") {
            int[] functionSignaturePanic = [LBRACE, RBRACE, EOF];

            while (panicMode) {
                if (self.LAToken(1) == functionSignaturePanic[0]) {
                    break;
                } else if (self.LAToken(1) == functionSignaturePanic[1]) {
                    break;
                } else if (self.LAToken(1) == functionSignaturePanic[2]) {
                    break;
                }
                Token currToken1 = self.parserBuffer.consumeToken();
                self.errTokens[self.errCount] = currToken1;
                self.errCount += 1;
            }
            return {
                tokenType: PARSER_ERROR_TOKEN,
                text: "<unexpected Token: Expected " + tokenNames[mToken] + ">",
                startPos: -1,
                endPos: -1,
                lineNumber: 0,
                index: -1,
                whiteSpace: ""
            };
        } else if (rule == "statement") {
            self.errorRecovered = false;
            int[] statementPanic = [SEMICOLON, RBRACE];
            while (panicMode) {
                if (self.LAToken(1) == statementPanic[0]) {
                    panicMode = false;
                } else if (self.LAToken(1) == statementPanic[1]) {
                    break;
                }
                Token currToken1 = self.parserBuffer.consumeToken();
                self.errTokens[self.errCount] = currToken1;
                self.errCount += 1;
            }
            return {
                tokenType: PARSER_ERROR_TOKEN,
                text: "<unexpected Token: Expected " + tokenNames[mToken] + ">",
                startPos: -1,
                endPos: -1,
                lineNumber: 0,
                index: -1,
                whiteSpace: ""
            };

        } else if (rule == "continueStatement") {
            if (mToken == SEMICOLON) {
                Token insertSemi = self.insertToken(mToken);
                insertSemi.text = ";";
                return insertSemi;
            }
        }
        //this return statement execute if the rule doesnt match to any of the above mentioned rules
        return {
            tokenType: PARSER_ERROR_TOKEN,
            text: "<unexpected " + tokenNames[mToken] + ">",
            startPos: -1,
            endPos: -1,
            lineNumber: 0,
            index: -1,
            whiteSpace: ""
        };
    }

    #lookahead the  token-type of the next token
    # +return - int
    function LAToken(int lACount) returns int {
        Token laToken = self.parserBuffer.lookAheadToken(lookAheadCount = lACount);
        int lToken = laToken.tokenType;
        return lToken;
    }

    #lookahead the next token
    # +return - Token
    function LookaheadToken(int laCount) returns Token {
        Token laToken = self.parserBuffer.lookAheadToken(lookAheadCount = laCount);
        return laToken;
    }

    #Function definition
    #  | FUNCTION <callable Unit Signature> <callable Unit body>
    # +return - FunctionNode
    function parseFunction(Token currToken) returns FunctionNode {
        Token functionToken = currToken;
        //parse function signature
        FunctionUnitSignatureNode signatureNode = self.parseCallableUnitSignature();
        //parse function body
        FunctionBodyNode bNode = self.parseCallableUnitBody();
        //return Function Node
        FunctionNode fn1 = {
            nodeKind: FUNCTION_NODE,
            tokenList: [functionToken],
            fnSignature: signatureNode,
            blockNode: bNode
        };
        return fn1;
    }


    #Callable Unit Signature
    #    | IDENTIFIER ()
    # +return - FunctionUnitSignatureNode
    function parseCallableUnitSignature() returns FunctionUnitSignatureNode {
        Token identifier = self.matchToken(IDENTIFIER, FN_SIGNATURE_NODE);

        //if the identifier is mismatched, then panic recovery method is excecuted and
        // tokens upto lbrace will be consumed.
        // the error tokens will be added to errToken list
        if (identifier.tokenType == PARSER_ERROR_TOKEN) {
            ErrorIdentifierNode erIdNode = {
                nodeKind: ER_IDENTIFIER_NODE,
                tokenList: [],
                identifier: ()
            };
            ErrorFunctionSignatureNode fSignature = {
                nodeKind: ER_FN_SIGNATURE_NODE,
                tokenList: self.errTokens,
                functionIdentifier: erIdNode
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
                    nodeKind: ER_FN_SIGNATURE_NODE,
                    tokenList: self.errTokens,
                    functionIdentifier: idNode
                };
                self.resetErrorFlag();
                return fSignature;
            } else {
                Token rParen = self.matchToken(RPAREN, FN_SIGNATURE_NODE);
                if (rParen.tokenType == PARSER_ERROR_TOKEN) {
                    //appending the errTokens with lParen which was correctly parsed
                    self.errTokens = self.appendErrorTknList([lParen]);
                    ErrorFunctionSignatureNode fSignature = {
                        nodeKind: ER_FN_SIGNATURE_NODE,
                        tokenList: self.errTokens,
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

    #append the tokens of a grammar to the errToken list in an error situation
    # +return - Token[]
    function appendErrorTknList(Token[] tokenLst) returns Token[] {
        int errListSize = self.errTokens.length();
        int count = 0;
        while (count < tokenLst.length()) {
            self.errTokens[errListSize] = tokenLst[count];
            errListSize += 1;
            count += 1;
        }
        return self.errTokens;
    }


    #Callable Unit Body
    #    | { <statement*>}
    #error recovery method: token insertion
    # +return - FunctionBodyNode
    function parseCallableUnitBody() returns FunctionBodyNode {
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

            StatementNode stNode = self.parseStatement();
            stsList[pos] = stNode;
            pos += 1;
        }
        //Token insertion if rBrace not found
        Token rBrace = self.matchToken(RBRACE, BLOCK_NODE);
        //the error token which is returned in an error situation, have the token endPosition set as -1
        if (rBrace.endPos == -1 || lBrace.endPos == -1) {
            ErrorBlockNode erNode = {
                nodeKind: ER_BLOCK_NODE,
                tokenList: [lBrace, rBrace],
                statementList: <StatementNode[]>(stsList.freeze())
            };
            self.resetErrorFlag();
            return erNode;
        } else {
            BlockNode blNode = {
                nodeKind: BLOCK_NODE,
                tokenList: [lBrace, rBrace],
                statementList: <StatementNode[]>(stsList.freeze())
            };
            return blNode;
        }
    }

    #Statement
    #	|<variable definition statement>
    #	|<continue statement>
    # +return - StatementNode?
    function parseStatement() returns StatementNode {
        //check if the LA token belongs to any of the statement type or else return an error node
        if (self.LAToken(1) == INT) {
            VariableDefStNode varD = self.parseVariableDefinitionStatementNode();
            return varD;

        } else if (self.LAToken(1) == CONTINUE) {
            ContinueStNode continueNode = self.parseContinueStatementNode();
            return continueNode;

        } else {
            log:printError(self.LookaheadToken(1).lineNumber + ":" + self.LookaheadToken(1).startPos +
            ": no viable statement type found for token: '" + self.LookaheadToken(1).text + "'");
            Token panicToken = self.panicRecovery(INT, STATEMENT_NODE);

            ErrorStatementNode erNode = {
                nodeKind: ER_STATEMENT_NODE,
                tokenList: self.errTokens
            };
            self.resetErrorFlag();
            return erNode;
        }
    }

    #Variable definition statement
    #    | <valueTypeName> IDENTIFIER SEMICOLON // int a;
    #    | <valeuTypeName>  IDENTIFIER ASSIGN <expression> SEMICOLON // int a  = b + 8;
    # +return - VariableDefinitionStatementNode
    function parseVariableDefinitionStatementNode() returns VariableDefStNode {

        Token valueTypeTkn = self.parseValueTypeName();
        //it is not necessary to check the validity of the value type since we only call this method if its a valid value type name.
        ValueKind valueKind1 = self.matchValueType(valueTypeTkn);
        Token identifier = self.matchToken(IDENTIFIER, VAR_DEF_STATEMENT_NODE);

        //if the identifier is mismatched,
        //the tokens will be consumed until ASSIGN or SEMICOLON is found.
        if (identifier.tokenType == PARSER_ERROR_TOKEN) {
            ErrorVarRefIdentifierNode vRef = {
                nodeKind: ER_VAR_DEF_IDENTIFIER_NODE,
                tokenList: self.errTokens,
                varIdentifier: ()
            };
            self.resetErrorFlag();

            if (self.LAToken(1) == SEMICOLON) {
                Token semiC = self.matchToken(SEMICOLON, VAR_DEF_STATEMENT_NODE);

                VariableDefinitionStatementNode vDef = {
                    nodeKind: VAR_DEF_STATEMENT_NODE,
                    tokenList: [valueTypeTkn, semiC],
                    valueKind: valueKind1,
                    varIdentifier: vRef,
                    expression: ()
                };
                self.resetErrorFlag();
                return vDef;

            } else if (self.LAToken(1) == ASSIGN) {
                VariableDefStNode vStatementNode = self.parseVarDefExpr(vRef, valueTypeTkn, valueKind1);
                return vStatementNode;
            } else {
                VariableDefinitionStatementNode vDef2 = {
                    nodeKind: VAR_DEF_STATEMENT_NODE,
                    tokenList: [valueTypeTkn],
                    valueKind: valueKind1,
                    varIdentifier: vRef,
                    expression: ()
                };
                self.resetErrorFlag();
                return vDef2;
            }
        }

        //variable definition statement with valid identifier
        VarRefIdentifier vRef = {
            nodeKind: VAR_REF_NODE,
            tokenList: [identifier],
            varIdentifier: identifier.text
        };


        if (self.LAToken(1) == SEMICOLON) {
            Token semiC = self.matchToken(SEMICOLON, VAR_DEF_STATEMENT_NODE);
            VariableDefinitionStatementNode vDef = {
                nodeKind: VAR_DEF_STATEMENT_NODE,
                tokenList: [valueTypeTkn, semiC],
                valueKind: valueKind1,
                varIdentifier: vRef,
                expression: ()
            };
            self.resetErrorFlag();
            return vDef;

        } else {
            VariableDefStNode vStatementNode = self.parseVarDefExpr(vRef, valueTypeTkn, valueKind1);
            return vStatementNode;
        }
    }

    # reset the errCount, and set error recovered to True, once an error is recovered
    function resetErrorFlag() {
        self.errCount = 0;
        self.errTokens = [];
        self.errorRecovered = true;
    }


    #parse ( ASSING EXPR SEMICOLON ) in variable definition statement
    # +return - VariableDefStNode
    function parseVarDefExpr(VariableReferenceNode vRef, Token valueTypeTkn, ValueKind valueKind1) returns VariableDefStNode {
        //token insertion if token assign is mismatched
        Token assign = self.matchToken(ASSIGN, VAR_DEF_STATEMENT_NODE);
        self.errorRecovered = true;
        ExpressionNode exprNode = self.expressionBuilder(VAR_DEF_STATEMENT_NODE);
        //if no semicolon found in the end of the expr, then errorRecovered will set to false within the expressionBuilder method itself
        if (exprNode is ()) {
            log:printError(assign.lineNumber + ":" + assign.endPos + " : no valid expression found in variable definition statement.");
            self.errorRecovered = false;
        }
        if (self.errorRecovered == false || self.invalidOccurence == true) {
            ErrorExpressionNode erNode = {
                nodeKind: ER_EXPRESSION_NODE,
                tokenList: self.errTokens,
                errorExpression: exprNode
            };
            self.resetErrorFlag();
            self.invalidOccurence = false;
            Token semiC2 = self.matchToken(SEMICOLON, VAR_DEF_STATEMENT_NODE);
            //tokens inserted using token insertion method will have the token endposition as -1
            if (semiC2.endPos == -1 || assign.endPos == -1) {

                ErrorVarDefStatementNode errSt = {
                    nodeKind: ER_VAR_DEF_STATEMENT_NODE,
                    tokenList: [valueTypeTkn, assign, semiC2],
                    valueKind: valueKind1,
                    varIdentifier: vRef,
                    expression: erNode
                };
                self.resetErrorFlag();
                return errSt;
            } else {
                VariableDefinitionStatementNode vDef2 = {
                    nodeKind: VAR_DEF_STATEMENT_NODE,
                    tokenList: [valueTypeTkn, assign, semiC2],
                    valueKind: valueKind1,
                    varIdentifier: vRef,
                    expression: erNode
                };
                return vDef2;
            }


        } else {
            //token insertion if semicolon is mismatched
            Token semiC2 = self.matchToken(SEMICOLON, VAR_DEF_STATEMENT_NODE);
            if (semiC2.endPos == -1 || assign.endPos == -1) {
                ErrorVarDefStatementNode errSt = {
                    nodeKind: ER_VAR_DEF_STATEMENT_NODE,
                    tokenList: [valueTypeTkn, assign, semiC2],
                    valueKind: valueKind1,
                    varIdentifier: vRef,
                    expression: exprNode
                };
                self.resetErrorFlag();
                return errSt;

            } else {
                VariableDefinitionStatementNode vDef2 = {
                    nodeKind: VAR_DEF_STATEMENT_NODE,
                    tokenList: [valueTypeTkn, assign, semiC2],
                    valueKind: valueKind1,
                    varIdentifier: vRef,
                    expression: exprNode
                };
                self.resetErrorFlag();
                return vDef2;
            }

        }
    }


    #continue statement
    #|CONTINUE SEMICOLON
    # +return - ContinueStatementNode
    function parseContinueStatementNode() returns ContinueStNode {

        Token valueTypeTkn = self.parseValueTypeName();
        ValueKind valueKind1 = self.matchValueType(valueTypeTkn);

        if (self.LAToken(1) == SEMICOLON) {
            Token semiC = self.matchToken(SEMICOLON, CONTINUE_STATEMENT_NODE);
            ContinueStatementNode contSt = {
                nodeKind: CONTINUE_STATEMENT_NODE,
                tokenList: [valueTypeTkn, semiC],
                valueKind: valueKind1
            };

            return contSt;
        } else {
            //token insertion if semicolon is mismatched
            Token semiC2 = self.matchToken(SEMICOLON, CONTINUE_STATEMENT_NODE);

            ErrorContinueStatementNode errcontinue = {
                nodeKind: ER_CONTIUE_STATEMENT_NODE,
                tokenList: [semiC2],
                valueKind: valueKind1
            };
            self.resetErrorFlag();
            return errcontinue;
        }

    }

    #expression
    #| <simple literal>
    #| <variable reference>
    #| expression (DIVISION | MULTIPLICATION | MOD) expression
    #| expression ( ADD | SUB ) expression
    #| expression (LT_EQUAL | GT_EQUAL | GT | LT)expression
    #| expression (EQUAL | NOT_EQUAL) expression
    #| expression (REF_EQUAL | REF_NOT_EQUAL) expression
    #| (ADD | SUB | NOT | BIT_COMPLEMENT | UNTAINT) expression
    #| <tuple literal>
    #
    #the statementType determines the break condition or the terminal token of the while loop
    # +return - ExpressionNode
    function expressionBuilder(string statementType) returns ExpressionNode {
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
        ExpressionNode exp2 = self.expStack.pop();
        return exp2;
    }

    #build the expression based on the operators remain in the operator stack
    function buildFinalExpr() {
        //tuple expression list
        ExpressionNode?[] tupleList = [];
        //list to keep track of commas
        Token[] commaList = [];
        while (self.oprStack.peek() != -1) {
            Token operator = self.oprStack.pop();

            if (operator.tokenType == LPAREN) {
                log:printError(operator.lineNumber + ":" + operator.startPos + " : invalid tuple literal, missing ')'");
                self.invalidOccurence = true;

                commaList[self.separatorCount] = operator;
                self.separatorCount += 1;

                ExpressionNode exprComma = self.expStack.pop();
                tupleList[self.tupleListPos] = exprComma;
                self.tupleListPos += 1;
                //reversing the array so that the expressions in the tuple list will be in proper order
                tupleList = self.reverseTupleList(tupleList);

                TupleLiteralNode tupleLNode = {
                    nodeKind: TUPLE_LITERAL_NODE,
                    tokenList: commaList,
                    tupleExprList:<ExpressionNode[]>(tupleList.freeze())
                };
                self.expStack.push(tupleLNode);
                self.tupleListPos = 0;

            //build the unary expressions
            } else if ((operator.tokenType >= NOT && operator.tokenType <= UNARY_PLUS)) {
                //unary_minus and unary_plus token types are converted to ADD and SUB tokenType
                self.convertToValidOp(operator);
                OperatorKind opKind3 = self.matchOperatorType(operator);

                if (self.expOperand == true) {
                    self.expOperand = false;
                    log:printError(operator.lineNumber + ":" + operator.startPos + " : missing unary expression");
                    self.invalidOccurence = true;
                    UnaryExpressionNode uExpression = {
                        nodeKind: UNARY_EXPRESSION_NODE,
                        tokenList: [operator],
                        operatorKind: opKind3,
                        uExpression: ()
                    };
                    self.expStack.push(uExpression);
                } else {
                    ExpressionNode expr3 = self.expStack.pop();
                    UnaryExpressionNode uExpression = {
                        nodeKind: UNARY_EXPRESSION_NODE,
                        tokenList: [operator],
                        operatorKind: opKind3,
                        uExpression: expr3
                    };
                    self.expStack.push(uExpression);
                }
            }
            else if (operator.tokenType == COMMA) {
                if (self.expOperand == true) {
                    log:printError(operator.lineNumber + ":" + operator.startPos + " : missing expression after comma");
                    //token deletion without calling the method as the comma token is already consumed and pushed to the opr stack
                    self.errTokens[self.errCount] = operator;
                    self.errCount += 1;
                    self.invalidOccurence = true;
                    continue;
                }

                self.invalidOccurence = true;
                self.errorRecovered = false;
                commaList[self.separatorCount] = operator;
                self.separatorCount += 1;

                ExpressionNode exprComma = self.expStack.pop();
                tupleList[self.tupleListPos] = exprComma;
                self.tupleListPos += 1;
            }
            else {
                OperatorKind opKind = self.matchOperatorType(operator);
                ExpressionNode expr2 = self.expStack.pop();
                ExpressionNode expr1 = self.expStack.pop();
                if (expr1 is ()) {
                    self.errorRecovered = false;
                    log:printError(operator.lineNumber + ":" + operator.startPos +
                    " : invalid binary expression, binary RHS expression not found");
                    BinaryExpressionNode bExpr = {
                        nodeKind: BINARY_EXP_NODE,
                        tokenList: [operator],
                        operatorKind: opKind,
                        leftExpr: expr2,
                        rightExpr: expr1
                    };
                    self.expStack.push(bExpr);
                } else {
                    BinaryExpressionNode bExpr = {
                        nodeKind: BINARY_EXP_NODE,
                        tokenList: [operator],
                        operatorKind: opKind,
                        leftExpr: expr1,
                        rightExpr: expr2
                    };
                    self.expStack.push(bExpr);
                }
            }
        }
        //here only comma separated expression is found, missing lParen and rParen
        if (self.tupleListPos > 0) {
            Token lastOperator = commaList[commaList.length() - 1];
            log:printError(lastOperator.lineNumber + ":" + lastOperator.startPos + " : invalid tuple literal expression");
            ExpressionNode exprComma = self.expStack.pop();
            tupleList[self.tupleListPos] = exprComma;
            self.tupleListPos += 1;
            tupleList = self.reverseTupleList(tupleList);
            TupleLiteralNode tupleLNode = {
                nodeKind: TUPLE_LITERAL_NODE,
                tokenList: commaList,
                tupleExprList:<ExpressionNode[]>(tupleList.freeze())
            };
            self.expStack.push(tupleLNode);

        }
        self.separatorCount = 0;
        self.tupleListPos = 0;
    }

    # reversing the tupleList so that the expressions in the tuple list will be in proper order.
    # +return - ExpressionNode?[]
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

    #convert the Unary_plus and unary_minus operators back to the valid types (ADD,SUB)
    function convertToValidOp(Token operator) {
        if (operator.tokenType == UNARY_MINUS) {
            operator.tokenType = SUB;
        } else if (operator.tokenType == UNARY_PLUS) {
            operator.tokenType = ADD;
        }

    }

    #helper function to build the expression
    #expression is parsed using shunting yard algorithm
    # +return - boolean
    function parseExpression() returns boolean {
        if (self.LAToken(1) == LPAREN) {
            self.priorOperator = false;
            //Lapren is expected after a operator,
            //After consuming an operator, the expOperand must be set to True.
            //but here since the expOperand is False, that means the previous token must be an operand
            //which causes an invalid occurence scenario.
            //therefore the token is deleted and added to the errToken list
            if (self.expOperand == false) {
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
                    SimpleLiteral smLiteral = emptyTuple;
                    self.expStack.push(smLiteral);
                    self.expOperand = false;
                    return true;
                } else {
                    self.oprStack.push(lParen);
                    self.expOperand = true;
                    return true;
                }
            }
        } else if (self.LAToken(1) == NUMBER) {
            self.priorOperator = false;
            if (self.expOperand == false) {
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
                self.expOperand = false;
                return true;
            }
        } else if (self.LAToken(1) == IDENTIFIER) {
            self.priorOperator = false;
            if (self.expOperand == false) {
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
                self.expOperand = false;
                return true;
            }
        } else if ((self.LAToken(1) >= ADD && self.LAToken(1) <= UNTAINT) || self.LAToken(1) == COMMA) {            //binary, unary operators and comma

            //if the expression stack is empty then that means no prior expression , so this is a unary expr
            if (self.expStack.isEmpty() == true || self.priorOperator == true) {
                if (self.LAToken(1) == SUB || self.LAToken(1) == ADD || (self.LAToken(1) >= 25 && self.LAToken(1) <= 27)) {                    // unary operators

                    if (self.LAToken(1) == SUB) {
                        Token unarySub = self.matchToken(SUB, EXPRESSION_NODE);
                        unarySub.tokenType = UNARY_MINUS;
                        self.oprStack.push(unarySub);
                        self.expOperand = true;
                        self.priorOperator = true;
                        return true;
                    } else if (self.LAToken(1) == ADD) {
                        Token unaryAdd = self.matchToken(ADD, EXPRESSION_NODE);
                        unaryAdd.tokenType = UNARY_PLUS;
                        self.oprStack.push(unaryAdd);
                        self.expOperand = true;
                        self.priorOperator = true;
                        return true;
                    } else {
                        Token unaryOpToken = self.matchToken(self.LAToken(1), EXPRESSION_NODE);
                        self.oprStack.push(unaryOpToken);
                        self.expOperand = true;
                        self.priorOperator = true;
                        return true;
                    }
                } else {                    //Operator which is not an unary operator - Ex:(,2) -> in such instance where the comma is in invalid position comma is deleted and added to the errorlist.
                    Token invalidToken = self.deleteToken();
                    return true;
                }
            } else {
                //setting the prior operator to true
                self.priorOperator = true;
                while (self.oprStack.opPrecedence(self.oprStack.peek()) >= self.oprStack.opPrecedence(self.LAToken(1))) {
                    //if the lookahead token is a comma and also the opr stack peek is also a comma, then we dont pop the comma from the stack Ex: (2,3)
                    //it will be popped in the end when we reach the RPAREN
                    if (self.oprStack.peek() == COMMA) {
                        if (self.LAToken(1) == COMMA) {
                            Token comma = self.matchToken(COMMA, EXPRESSION_NODE);
                            self.oprStack.push(comma);
                            self.expOperand = true;
                            return true;
                        }
                    }
                    Token operator = self.oprStack.pop();
                    //build unary expressions, and push it to the expression stack
                    if ((operator.tokenType >= NOT && operator.tokenType <= UNARY_PLUS)) {                        // unary operators
                        self.priorOperator = false;
                        //unary_minus and unary_plus token types are converted to ADD and SUB tokenType
                        self.convertToValidOp(operator);

                        OperatorKind opKind3 = self.matchOperatorType(operator);
                        ExpressionNode expr3 = self.expStack.pop();
                        UnaryExpressionNode uExpression = {
                            nodeKind: UNARY_EXPRESSION_NODE,
                            tokenList: [operator],
                            operatorKind: opKind3,
                            uExpression: expr3
                        };
                        self.expStack.push(uExpression);
                    } else {                        //build binary expressions by popping two expressions and push it to the expression stack
                        OperatorKind opKind = self.matchOperatorType(operator);
                        ExpressionNode expr2 = self.expStack.pop();
                        ExpressionNode expr1 = self.expStack.pop();
                        BinaryExpressionNode bExpr = {
                            nodeKind: BINARY_EXP_NODE,
                            tokenList: [operator],
                            operatorKind: opKind,
                            leftExpr: expr1,
                            rightExpr: expr2
                        };
                        self.expStack.push(bExpr);
                    }
                }
                //consume the binary operators and push them to the stack
                if ((self.LAToken(1) >= ADD && self.LAToken(1) <= REF_EQUAL) || self.LAToken(1) == COMMA) {

                    Token binaryOpToken = self.matchToken(self.LAToken(1), EXPRESSION_NODE);
                    self.oprStack.push(binaryOpToken);
                }

                self.expOperand = true;
                return true;
            }

        } else if (self.LAToken(1) == RPAREN) {
            //self.priorOperator = false;
            //tuple expression list
            ExpressionNode?[] tupleList = [];
            //list to keep track of commas
            Token[] commaList = [];

            Token rParen = self.matchToken(RPAREN, EXPRESSION_NODE);
            commaList[self.separatorCount] = rParen;
            self.separatorCount += 1;
            while (self.oprStack.peek() != LPAREN) {
                Token operator = self.oprStack.pop();
                if (operator.tokenType == PARSER_ERROR_TOKEN) {
                    //considering only possible option to return parser error token would be a missing lparen
                    log:printError(operator.lineNumber + ":" + operator.startPos + " : invalid tuple literal, missing '('");
                    Token insertlParen = self.insertToken(LPAREN);
                    insertlParen.text = "(";
                    self.oprStack.push(insertlParen);
                    self.invalidOccurence = true;
                    break;
                } else if (operator.tokenType == COMMA) {
                    if (self.expOperand == true) {

                        log:printError(operator.lineNumber + ":" + operator.startPos + " : missing expression after comma");
                        //token deletion without calling the method as the comma token is already consumed and pushed to the opr stack
                        self.errTokens[self.errCount] = operator;
                        self.errCount += 1;
                        self.invalidOccurence = true;
                        continue;
                    }
                    commaList[self.separatorCount] = operator;
                    self.separatorCount += 1;

                    ExpressionNode exprComma = self.expStack.pop();
                    tupleList[self.tupleListPos] = exprComma;
                    self.tupleListPos += 1;
                    continue;
                } else if ((operator.tokenType >= NOT && operator.tokenType <= UNARY_PLUS)) {                    // unary operators

                    //unary_minus and unary_plus token types are converted to ADD and SUB tokenType
                    self.convertToValidOp(operator);
                    OperatorKind opKind3 = self.matchOperatorType(operator);
                    if (self.expOperand == true) {
                        self.expOperand = false;
                        log:printError(operator.lineNumber + ":" + operator.startPos + " : missing unary expression");

                        self.invalidOccurence = true;
                        UnaryExpressionNode uExpression = {
                            nodeKind: UNARY_EXPRESSION_NODE,
                            tokenList: [operator],
                            operatorKind: opKind3,
                            uExpression: ()
                        };
                        self.expStack.push(uExpression);
                    } else {
                        ExpressionNode expr3 = self.expStack.pop();
                        UnaryExpressionNode uExpression = {
                            nodeKind: UNARY_EXPRESSION_NODE,
                            tokenList: [operator],
                            operatorKind: opKind3,
                            uExpression: expr3
                        };
                        self.expStack.push(uExpression);
                    }
                } else {
                    if (self.expOperand == true) {

                        log:printError(operator.lineNumber + ":" + operator.startPos +
                        " : invalid binary expression, binary RHS expression not found");
                        self.invalidOccurence = true;
                        OperatorKind opKind = self.matchOperatorType(operator);
                        ExpressionNode expr1 = self.expStack.pop();
                        BinaryExpressionNode bExpr = {
                            nodeKind: BINARY_EXP_NODE,
                            tokenList: [operator],
                            operatorKind: opKind,
                            leftExpr: expr1,
                            rightExpr: ()
                        };
                        self.expStack.push(bExpr);
                        self.expOperand = false;
                    } else {
                        OperatorKind opKind = self.matchOperatorType(operator);
                        ExpressionNode expr2 = self.expStack.pop();
                        ExpressionNode expr1 = self.expStack.pop();
                        BinaryExpressionNode bExpr = {
                            nodeKind: BINARY_EXP_NODE,
                            tokenList: [operator],
                            operatorKind: opKind,
                            leftExpr: expr1,
                            rightExpr: expr2
                        };
                        self.expStack.push(bExpr);
                    }

                }
            }
            //popping the lParen
            Token leftToken = self.oprStack.pop();
            commaList[self.separatorCount] = leftToken;
            self.separatorCount += 1;

            ExpressionNode parenExpr = self.expStack.pop();
            if (parenExpr is ()) {
                EmptyTupleLiteralNode emptyTuple = {
                    nodeKind: EMPTY_TUPLE_LITERAL_NODE,
                    tokenList: commaList
                };
                SimpleLiteral smLiteral = emptyTuple;
                self.expStack.push(smLiteral);
            } else {
                tupleList[self.tupleListPos] = parenExpr;
                self.tupleListPos += 1;
                //reversing the array so that the expressions in the tuple list will be in proper order
                tupleList = self.reverseTupleList(tupleList);

                TupleLiteralNode tupleLNode = {
                    nodeKind: TUPLE_LITERAL_NODE,
                    tokenList: commaList,
                    tupleExprList:<ExpressionNode[]>(tupleList.freeze())
                };
                self.expStack.push(tupleLNode);
                self.tupleListPos = 0;
            }

            self.separatorCount = 0;
            self.expOperand = false;
            self.priorOperator = false;
            return true;
        }
        return false;
    }



    #valueTypeName
    #   | INT
    #   | STRING
    # +return - Token
    function parseValueTypeName() returns Token {
        if (self.LAToken(1) == INT) {
            Token int1 = self.matchToken(INT, STATEMENT_NODE);
            return int1;
        } else if (self.LAToken(1) == STRING) {
            Token string1 = self.matchToken(STRING, STATEMENT_NODE);
            return string1;
        } else if (self.LAToken(1) == CONTINUE) {
            Token continue1 = self.matchToken(CONTINUE, CONTINUE_STATEMENT_NODE);
            return continue1;
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
    function matchValueType(Token valueTypeTkn) returns ValueKind {
        var valKind = self.valMap.valueKindMap[tokenNames[valueTypeTkn.tokenType]];

        if (valKind is ValueKind) {
            return valKind;
        } else {
            return ERROR_VALUE_TYPE;
        }
    }

    //check the operator kind of the operator token
    function matchOperatorType(Token operator) returns OperatorKind {
        var opKind = self.oprMap.operatorTypeMap[tokenNames[operator.tokenType]];

        if (opKind is OperatorKind) {
            return opKind;
        } else {
            return ERROR_OP;
        }
    }

};

#operator stack to store the operators
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
        if (self.top != 0) {
            self.top = self.top - 1;
            Token operatorToken = self.opStack[self.top];
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
            };
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
        if (self.opStack.length() == 1) {
            return false;
        }
        return true;
    }

    function size() returns int {
        int opSize = self.opStack.length();
        return opSize;
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
        } else if (opToken == UNARY_MINUS || opToken == UNARY_PLUS || opToken == NOT || opToken == BIT_COMPLEMENT || opToken == UNTAINT) {
            return 7;
        }
        return -1;
    }
};

# Expression stack stores each expression built
type ExprStack object {
    ExpressionNode[] exprStack = [()];
    int top;

    public function __init(int top = 0) {
        self.top = top;
    }

    function push(ExpressionNode token2) {
        self.exprStack[self.top] = token2;
        self.top = self.top + 1;
    }

    function pop() returns ExpressionNode {
        if (self.top != 0) {
            self.top = self.top - 1;
            ExpressionNode exprsNode = self.exprStack[self.top];
            return exprsNode;
        }
    }

    function topExpr() returns ExpressionNode {
        ExpressionNode topExpr = self.exprStack[self.top - 1];
        return topExpr;
    }

    function isEmpty() returns boolean {
        if (self.exprStack.length() == 1 && self.exprStack[0] is ()) {
            return true;
        }
        return false;
    }

};
