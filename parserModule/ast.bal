import ballerina/io;

const PACKAGE_NODE = "package";
const FUNCTION_NODE = "function";
const STATEMENT_NODE = "statement";
const VAR_DEF_STATEMENT_NODE = "variableDefinitionStatement";
const VAR_DEC_STATEMENT_NODE = "variableDeclrationStatement";
const EXPRESSION_NODE = "expression";
const BINARY_EXP_NODE = "binaryExpression";
const IDENTIFIER_NODE = "identifier";
const VAR_REF_NODE = "variableReferenceIdentifier";
const FN_SIGNATURE_NODE = "functionSignatureNode";
const BLOCK_NODE = "blockNode";
const INTEGER_LITERAL = "integerLiteral";
const QUO_STRING_LITERAL = "quotedStringLiteral";
const ERROR_NODE = "errorNode";
const RECORD_LITERAL_NODE = "recordLiteralNode";
const RECORD_KEY_VALUE_NODE = "recordKeyValueNode";
const RECORD_KEY_NODE = "recordKeyNode";
const TUPLE_LITERAL_NODE = "tupleLiteralNode";
const EMPTY_TUPLE_LITERAL_NODE = "emptyTupleLiteralNode";
const UNARY_EXPRESSION_NODE = "unaryExpressionNode";

type NodeKind PACKAGE_NODE|FUNCTION_NODE|STATEMENT_NODE|VAR_DEF_STATEMENT_NODE|VAR_DEC_STATEMENT_NODE|EXPRESSION_NODE|
BINARY_EXP_NODE|IDENTIFIER_NODE|VAR_REF_NODE | FN_SIGNATURE_NODE | BLOCK_NODE | INTEGER_LITERAL | QUO_STRING_LITERAL |
ERROR_NODE | RECORD_LITERAL_NODE | RECORD_KEY_VALUE_NODE | RECORD_KEY_NODE | TUPLE_LITERAL_NODE | EMPTY_TUPLE_LITERAL_NODE | UNARY_EXPRESSION_NODE;

const INT_TYPE = "int";
const STRING_TYPE = "string";
const ERROR_VALUE_TYPE = "errorValueType";

type ValueKind INT_TYPE|STRING_TYPE|ERROR_VALUE_TYPE;

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
//untaint type to be a value kind?
const UNTAINT_TYPE = "untaint";

type OperatorKind PLUS_OP|MINUS_OP|DIVISION_OP|MULTIPLICATION_OP|ERROR_OP|COLON_OP |COMMA_OP |
MOD_OP | LT_EQUAL_OP | GT_EQUAL_OP | GT_OP | LT_OP | EQUAL_OP | NOT_EQUAL_OP | REF_EQUAL_OP | REF_NOT_EQUAL_OP | NOT_OP | BIT_COMPLEMENT_OP |UNTAINT_TYPE;

type Node record {
    NodeKind nodeKind;
	//made the tokenList nullable since error node token list can be null
    Token[] tokenList?;
};

public type PackageNode record {
    *Node;
    DefinitionNode[] definitionList;
};

type DefinitionNode FunctionNode | ErrorNode;

type FunctionNode record {
    *Node;
    FunctionSignatureNode? fnSignature;
    BlockNode? blockNode;
};

//function identifier ()
type FunctionSignatureNode record{
    *Node;
    IdentifierNode functionIdentifier;
};

//{ <statement*> }
type BlockNode record{
    *Node;
    StatementNode[] statementList;
};

type StatementNode VariableDefinitionStatementNode | ErrorNode;

type ErrorNode record{
	*Node;
    StatementNode errorStatement?;
    FunctionNode errorFunction?;
};

type VariableDefinitionStatementNode record {
    *Node;
    ValueKind valueKind;
    VarRefIdentifier varIdentifier;
    ExpressionNode? expression;
};

type ExpressionNode BinaryExpressionNode | SimpleLiteral | VarRefIdentifier | RecordLiteralNode | TupleLiteralNode | UnaryExpressionNode ;

type BinaryExpressionNode record {
    *Node;
    OperatorKind operatorKind;
    ExpressionNode leftExpr;
    ExpressionNode rightExpr;
};

//as untaint unary expression - Valuekind is also added
type UnaryExpressionNode record {
   *Node;
    OperatorKind operatorKind;
    ExpressionNode uExpression;
};

type IdentifierNode record {
    *Node;
    string identifier;
};

type VarRefIdentifier record {
    *Node;
    string varIdentifier;
};
//LEFT_BRACE (recordKeyValue (COMMA recordKeyValue)*)? RIGHT_BRACE
type RecordLiteralNode record {
    *Node;
     RecordKeyValueNode[] recordkeyValueList;
};
//recordKey COLON expression
type RecordKeyValueNode record {
    *Node;
    RecordKeyNode recordKeyNode;
    OperatorKind operatorKind;
    ExpressionNode recordValueExpression;

};
//Identifier |   expression
type RecordKeyNode record {
    *Node;
    IdentifierNode recordKey?;
    ExpressionNode recordExpression?;

};
//LEFT_PARENTHESIS expression (COMMA expression)* RIGHT_PARENTHESIS
type TupleLiteralNode record{
    *Node;
    ExpressionNode[] tupleExprList?;
};

type SimpleLiteral IntegerLiteralNode | QuotedStringLiteralNode| EmptyTupleLiteralNode;

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