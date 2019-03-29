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

type NodeKind PACKAGE_NODE|FUNCTION_NODE|STATEMENT_NODE|VAR_DEF_STATEMENT_NODE|VAR_DEC_STATEMENT_NODE|EXPRESSION_NODE|
BINARY_EXP_NODE|IDENTIFIER_NODE|VAR_REF_NODE | FN_SIGNATURE_NODE | BLOCK_NODE | INTEGER_LITERAL | QUO_STRING_LITERAL | ERROR_NODE;

const INT_TYPE = "int";
const STRING_TYPE = "string";

type ValueKind INT_TYPE|STRING_TYPE;

const PLUS_OP = "+";
const MINUS_OP = "-";
const DIVISION_OP = "/";
const MULTIPLICATION_OP = "*";
const ERROR_OP = "invalidOperator";

type OperatorKind PLUS_OP|MINUS_OP|DIVISION_OP|MULTIPLICATION_OP|ERROR_OP;

type Node record {
    NodeKind nodeKind;
	//made the tokenList nullable since error node token list can be null
    Token[] tokenList?;
};

public type PackageNode record {
    *Node;
    FunctionNode[] functionList;
};

type FunctionNode record {
    *Node;
    FunctionSignatureNode fnSignature;
    BlockNode blockNode;
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

type ExpressionNode BinaryExpressionNode | SimpleLiteral | VarRefIdentifier;

type BinaryExpressionNode record {
    *Node;
    OperatorKind operatorKind;
    ExpressionNode leftExpr;
    ExpressionNode rightExpr;
};

type IdentifierNode record {
    *Node;
    string identifier;
};

type VarRefIdentifier record {
    *Node;
    string varIdentifier;
};

type SimpleLiteral IntegerLiteralNode | QuotedStringLiteralNode;

type IntegerLiteralNode record {
    *Node;
    string number;
};

type QuotedStringLiteralNode record {
    *Node;
    string stringLiteral;
};