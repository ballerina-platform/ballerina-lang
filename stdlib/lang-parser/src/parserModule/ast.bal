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

const PACKAGE_NODE = "package";
const FUNCTION_NODE = "function";
const STATEMENT_NODE = "statement";
const VAR_DEF_STATEMENT_NODE = "variableDefinitionStatement";
const EXPRESSION_NODE = "expression";
const BINARY_EXP_NODE = "binaryExpression";
const IDENTIFIER_NODE = "identifier";
const VAR_REF_NODE = "variableReferenceIdentifier";
const FN_SIGNATURE_NODE = "functionSignature";
const BLOCK_NODE = "blockNode";
const INTEGER_LITERAL = "integerLiteral";
const QUO_STRING_LITERAL = "quotedStringLiteral";
const RECORD_LITERAL_NODE = "recordLiteralNode";
const RECORD_KEY_VALUE_NODE = "recordKeyValueNode";
const RECORD_KEY_NODE = "recordKeyNode";
const TUPLE_LITERAL_NODE = "tupleLiteralNode";
const EMPTY_TUPLE_LITERAL_NODE = "emptyTupleLiteralNode";
const UNARY_EXPRESSION_NODE = "unaryExpressionNode";
const CONTINUE_STATEMENT_NODE = "continueStatement";

const ERROR_FN_SIGNATURE_NODE = "errorFunctionSignature";
const ERROR_BLOCK_NODE = "errorFunctionBody";
const ERROR_VAR_DEF_STATEMENT_NODE = "errorVariableDefinitionStatement";
const ERROR_VAR_DEF_IDENTIFIER_NODE = "errorVariableDefinitionIdentifier";
const ERROR_CONTIUE_STATEMENT_NODE = "errorContinueStatement";
const ERROR_STATEMENT_NODE = "errorStatement";
const ERROR_IDENTIFIER_NODE = "errorIdentifier";
const ERROR_EXPRESSION_NODE = "failedExpression";

type NodeKind PACKAGE_NODE | FUNCTION_NODE | STATEMENT_NODE | VAR_DEF_STATEMENT_NODE | EXPRESSION_NODE |
 BINARY_EXP_NODE | IDENTIFIER_NODE | VAR_REF_NODE | FN_SIGNATURE_NODE | BLOCK_NODE | INTEGER_LITERAL |
 QUO_STRING_LITERAL | RECORD_LITERAL_NODE | RECORD_KEY_VALUE_NODE | RECORD_KEY_NODE | TUPLE_LITERAL_NODE |
 EMPTY_TUPLE_LITERAL_NODE | UNARY_EXPRESSION_NODE | CONTINUE_STATEMENT_NODE | ERROR_FN_SIGNATURE_NODE |
 ERROR_BLOCK_NODE | ERROR_VAR_DEF_STATEMENT_NODE | ERROR_VAR_DEF_IDENTIFIER_NODE | ERROR_CONTIUE_STATEMENT_NODE |
 ERROR_STATEMENT_NODE | ERROR_IDENTIFIER_NODE | ERROR_EXPRESSION_NODE;

const INT_TYPE = "int";
const STRING_TYPE = "string";
const ERROR_VALUE_TYPE = "errorValueType";
const CONTINUE_TYPE = "continue";

type ValueKind INT_TYPE | STRING_TYPE | ERROR_VALUE_TYPE | CONTINUE_TYPE;

const PLUS_OP = "+";
const MINUS_OP = "-";
const DIVISION_OP = "/";
const MULTIPLICATION_OP = "*";
const ERROR_OP = "invalidOperator";
const COLON_OP = ":";
const COMMA_OP = ",";
const MOD_OP = "%";
const LT_EQUAL_OP = "<=";
const GT_EQUAL_OP = ">=";
const GT_OP = ">";
const LT_OP = "<";
const EQUAL_OP = "==";
const NOT_EQUAL_OP = "!=";
const REF_EQUAL_OP = "===";
const REF_NOT_EQUAL_OP = "!==";
const NOT_OP = "!";
const BIT_COMPLEMENT_OP = "~";
const UNTAINT_TYPE = "untaint";

type OperatorKind PLUS_OP | MINUS_OP | DIVISION_OP | MULTIPLICATION_OP | ERROR_OP | COLON_OP | COMMA_OP |
 MOD_OP | LT_EQUAL_OP | GT_EQUAL_OP | GT_OP | LT_OP | EQUAL_OP | NOT_EQUAL_OP | REF_EQUAL_OP | REF_NOT_EQUAL_OP |
 NOT_OP | BIT_COMPLEMENT_OP | UNTAINT_TYPE;

type Node record {
    NodeKind nodeKind;
    Token[] tokenList;
};

public type PackageNode record {
    *Node;
    DefinitionNode[] definitionList;
};

public type DefinitionNode FunctionNode | ();

public type FunctionNode record {
    *Node;
    FunctionUnitSignatureNode fnSignature;
    FunctionBodyNode blockNode;
};

public type FunctionUnitSignatureNode FunctionSignatureNode | ErrorFunctionSignatureNode;

type FunctionSignatureNode record {
    *Node;
    IdentifierNodeKind functionIdentifier;
};

type ErrorFunctionSignatureNode record {
    *FunctionSignatureNode;
};

public type FunctionBodyNode BlockNode | ErrorBlockNode;

type BlockNode record {
    *Node;
    StatementNode[] statementList;
};

type ErrorBlockNode record {
    *BlockNode;
};

type StatementNode VariableDefStNode | ContinueStNode | ErrorStatementNode | ();

type VariableDefStNode VariableDefinitionStatementNode | ErrorVarDefStatementNode;

type VariableDefinitionStatementNode record {
    *Node;
    ValueKind valueKind;
    VariableReferenceNode varIdentifier;
    ExpressionNode? expression;
};

type ErrorVarDefStatementNode record {
    *VariableDefinitionStatementNode;
};

type ContinueStNode ContinueStatementNode | ErrorContinueStatementNode;

type ContinueStatementNode record {
    *Node;
    ValueKind valueKind;
};

type ErrorContinueStatementNode record {
    *ContinueStatementNode;
};

type ErrorStatementNode record {
    *Node;
};

type ExpressionNode BinaryExpressionNode | SimpleLiteral | VarRefIdentifier | TupleLiteralNode | UnaryExpressionNode | ErrorExpressionNode | ();

type ErrorExpressionNode record {
    *Node;
    ExpressionNode errorExpression?;
};

type BinaryExpressionNode record {
    *Node;
    OperatorKind operatorKind;
    ExpressionNode leftExpr;
    ExpressionNode rightExpr;
};

type UnaryExpressionNode record {
    *Node;
    OperatorKind operatorKind;
    ExpressionNode uExpression;
};

type IdentifierNodeKind IdentifierNode | ErrorIdentifierNode;

type IdentifierNode record {
    *Node;
    string identifier;
};

type ErrorIdentifierNode record {
    *Node;
    string? identifier;
};

type VariableReferenceNode VarRefIdentifier | ErrorVarRefIdentifierNode;

type VarRefIdentifier record {
    *Node;
    string varIdentifier;
};

type ErrorVarRefIdentifierNode record {
    *Node;
    string? varIdentifier;
};

type TupleLiteralNode record {
    *Node;
    ExpressionNode[] tupleExprList?;
};

type SimpleLiteral IntegerLiteralNode | QuotedStringLiteralNode | EmptyTupleLiteralNode;

type IntegerLiteralNode record {
    *Node;
    string number;
};

type QuotedStringLiteralNode record {
    *Node;
    string stringLiteral;
};

type EmptyTupleLiteralNode record {
    *Node;
};
