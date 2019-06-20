## Ballerina-lang-parser

The parser is implemented following the LL(K) recursive Descent parsing pattern. This pattern analyses the syntactic structure using k>1 lookahead tokens.

**Token Structure**
```ballerina
Token record {
    int tokenType;
    string text;
    int startPos;
    int endPos;
    int lineNumber;
    int index;
    string? whiteSpace;
}
```

Grammar supported by the current implementation is given below.

**Parser Grammar**
```antlrv4
Definition
    | Function definition
Function definition
    | FUNCTION <callable Unit Signature> <callable Unit body>
Callable Unit Signature
    | IDENTIFIER ()
Callable Unit Body
    | { <statement*>}
Statement
    |<variable definition statement>
    |<continue statement>
Variable definition statement
    | <valueTypeName> IDENTIFIER SEMICOLON
    | <valueTypeName>  IDENTIFIER ASSIGN <expression> SEMICOLON
Continue statement
    | CONTINUE SEMICOLON
valueTypeName
    | INT
Expression
    | <simple literal>
    | <variable reference>
    | expression (DIVISION | MULTIPLICATION | MOD) expression
    | expression ( ADD | SUB ) expression
    | expression (LT_EQUAL | GT_EQUAL | GT | LT)expression
    | expression (EQUAL | NOT_EQUAL) expression
    | expression (REF_EQUAL | REF_NOT_EQUAL) expression
    | (ADD | SUB | NOT | BIT_COMPLEMENT | UNTAINT) expression
    | <tuple literal>
    
Simple Literal
    | IntegerLiteral //[0-9]
    | EmptyTupleLiteral

Variable reference
    | IDENTIFIER

EmptyTupleLiteral
    | LBRACE RBRACE

TupleLiteral
     |LEFT_PARENTHESIS expression (COMMA expression)* RIGHT_PARENTHESIS
```


#### Testing the parser

1. Navigate to the <PROJECT_ROOT_DIRECTORY>/inputFile.txt and add the source code that is needed to be tested.

	Sample source code:
	```ballerina
	function foo(){
    	int a = 2 + (3*5);
    }
	```

2. Run the `ballerina run parserModule` command from the <PROJECT_ROOT_DIRECTORY>

The built AST will be printed as a Record.

Sample serialized json output:
```json
{
	"nodeKind": "package",
	"tokenList": [{
		"tokenType": 51,
		"text": "EOF",
		"startPos": 1,
		"endPos": 1,
		"lineNumber": 3,
		"index": 18,
		"whiteSpace": null
	}],
	"definitionList": [{
		"nodeKind": "function",
		"tokenList": [{
			"tokenType": 53,
			"text": "function",
			"startPos": 1,
			"endPos": 8,
			"lineNumber": 1,
			"index": 1,
			"whiteSpace": null
		}],
		"fnSignature": {
			"nodeKind": "functionSignature",
			"tokenList": [{
				"tokenType": 6,
				"text": "(",
				"startPos": 13,
				"endPos": 13,
				"lineNumber": 1,
				"index": 3,
				"whiteSpace": null
			}, {
				"tokenType": 7,
				"text": ")",
				"startPos": 14,
				"endPos": 14,
				"lineNumber": 1,
				"index": 4,
				"whiteSpace": null
			}],
			"functionIdentifier": {
				"nodeKind": "identifier",
				"tokenList": [{
					"tokenType": 46,
					"text": "foo",
					"startPos": 10,
					"endPos": 12,
					"lineNumber": 1,
					"index": 2,
					"whiteSpace": " "
				}],
				"identifier": "foo"
			}
		},
		"blockNode": {
			"nodeKind": "blockNode",
			"tokenList": [{
				"tokenType": 0,
				"text": "{",
				"startPos": 15,
				"endPos": 15,
				"lineNumber": 1,
				"index": 5,
				"whiteSpace": null
			}, {
				"tokenType": 1,
				"text": "}",
				"startPos": 1,
				"endPos": 1,
				"lineNumber": 3,
				"index": 17,
				"whiteSpace": "\n"
			}],
			"statementList": [{
				"nodeKind": "variableDefinitionStatement",
				"tokenList": [{
					"tokenType": 54,
					"text": "int",
					"startPos": 5,
					"endPos": 7,
					"lineNumber": 2,
					"index": 6,
					"whiteSpace": "\n\t"
				}, {
					"tokenType": 45,
					"text": "=",
					"startPos": 11,
					"endPos": 11,
					"lineNumber": 2,
					"index": 8,
					"whiteSpace": " "
				}, {
					"tokenType": 2,
					"text": ";",
					"startPos": 22,
					"endPos": 22,
					"lineNumber": 2,
					"index": 16,
					"whiteSpace": null
				}],
				"valueKind": "int",
				"varIdentifier": {
					"nodeKind": "variableReferenceIdentifier",
					"tokenList": [{
						"tokenType": 46,
						"text": "a",
						"startPos": 9,
						"endPos": 9,
						"lineNumber": 2,
						"index": 7,
						"whiteSpace": " "
					}],
					"varIdentifier": "a"
				},
				"expression": {
					"nodeKind": "binaryExpression",
					"tokenList": [{
						"tokenType": 12,
						"text": "+",
						"startPos": 15,
						"endPos": 15,
						"lineNumber": 2,
						"index": 10,
						"whiteSpace": " "
					}],
					"operatorKind": "+",
					"leftExpr": {
						"nodeKind": "integerLiteral",
						"tokenList": [{
							"tokenType": 47,
							"text": "2",
							"startPos": 13,
							"endPos": 13,
							"lineNumber": 2,
							"index": 9,
							"whiteSpace": " "
						}],
						"number": "2"
					},
					"rightExpr": {
						"nodeKind": "tupleLiteralNode",
						"tokenList": [{
							"tokenType": 7,
							"text": ")",
							"startPos": 21,
							"endPos": 21,
							"lineNumber": 2,
							"index": 15,
							"whiteSpace": null
						}, {
							"tokenType": 6,
							"text": "(",
							"startPos": 17,
							"endPos": 17,
							"lineNumber": 2,
							"index": 11,
							"whiteSpace": " "
						}],
						"tupleExprList": [{
							"nodeKind": "binaryExpression",
							"tokenList": [{
								"tokenType": 15,
								"text": "*",
								"startPos": 19,
								"endPos": 19,
								"lineNumber": 2,
								"index": 13,
								"whiteSpace": null
							}],
							"operatorKind": "*",
							"leftExpr": {
								"nodeKind": "integerLiteral",
								"tokenList": [{
									"tokenType": 47,
									"text": "3",
									"startPos": 18,
									"endPos": 18,
									"lineNumber": 2,
									"index": 12,
									"whiteSpace": null
								}],
								"number": "3"
							},
							"rightExpr": {
								"nodeKind": "integerLiteral",
								"tokenList": [{
									"tokenType": 47,
									"text": "5",
									"startPos": 20,
									"endPos": 20,
									"lineNumber": 2,
									"index": 14,
									"whiteSpace": null
								}],
								"number": "5"
							}
						}]
					}
				}
			}]
		}
	}]
}


```


#### Unit Tests
To execute the Unit tests, run the following command from the <PROJECT_ROOT_DIRECTORY>

 `ballerina test parserModule` 
 
 serialized JSON outputs for the unit tests can be found below.
 
1. [function rule](resources/function/output(AST)).

2. [variable definition statement rule](resources/statements/variableDefinitionStatement/output(AST)).

3. [binary expression rule](resources/expression/binaryExpression/output(AST)).

4. [tuple literal expression rule](resources/expression/tupleLiteralExpression/output(AST)).

5. [unary expression rule](resources/expression/unaryExpression/output(AST)). 
 
 