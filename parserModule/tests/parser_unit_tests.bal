import ballerina/io;
import ballerina/test;
import ballerina/log;

//invoke parser to convert to json
function parseAst(string fileLocation) returns json {
	PackageNode? pkNode = parseFile(fileLocation);
	if(pkNode is PackageNode){
		json|error jsonOut = json.convert(pkNode);
		if (jsonOut is json) {
			return jsonOut;
		}
	}
}
//@test:BeforeSuite
//function beforeFunc() {
//
//}

@test:Config
function testFunctionSigValid() {
	json actual = parseAst("resources/function/fn_signature_valid.bal");
	json expected = fnSignatureValid();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");
}
@test:Config
function testFunctionSigInvalid(){
	json actual = parseAst("resources/function/fn_signature_missing_identifier.bal");
	json expected = fnSigMissingIdentifier();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual2 = parseAst("resources/function/fn_signature_missing_lparen.bal");
	json expected2 = fnSignatureMissingLparen();
	boolean comparison2 = actual2 == expected2;
	test:assertTrue(comparison2, msg = "AssertTrue failed");

	json actual3 = parseAst("resources/function/fn_signature_missing_rparen.bal");
	json expected3 = fnSignatureMissingRparen();
	boolean comparison3 = actual3 == expected3;
	test:assertTrue(comparison3, msg = "AssertTrue failed");
}
@test:Config
function testFunctionBodyValid(){
	json actual = parseAst("resources/function/fn_body_valid.bal");
	json expected = fnBodyValid();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");
}
@test:Config
function testFunctionBodyInvalid(){
	json actual = parseAst("resources/function/fn_body_missing_lbrace.bal");
	json expected = fnBodyMissingLbrace();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual2 = parseAst("resources/function/fn_body_missing_rbrace.bal");
	json expected2 = fnBodyMissingRbrace();
	boolean comparison2 = actual2 == expected2;
	test:assertTrue(comparison2, msg = "AssertTrue failed");
}
@test:Config
function testFunctionInvalid(){
	json actual = parseAst("resources/function/fn_missing_signature_and_body.bal");
	json expected = fnMissingSigAndBody();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual2 = parseAst("resources/function/fn_incomplete_signature.bal");
	json expected2 = fnIncompleteSignature();
	boolean comparison2 = actual2 == expected2;
	test:assertTrue(comparison2, msg = "AssertTrue failed");
}
@test:Config
function testVarDefStInvalid(){
	json actual = parseAst("resources/statements/variableDefinitionStatement/varDefSts_missing_binaryR&Semicolon.bal");
	json expected = missingBinaryRandSemicolon();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual3 = parseAst("resources/statements/variableDefinitionStatement/varDefSts_missing_expr&Semicolon.bal");
	json expected3 = missingExprandSemicolon();
	boolean comparison3 = actual3 == expected3;
	test:assertTrue(comparison3, msg = "AssertTrue failed");

	json actual4 = parseAst("resources/statements/variableDefinitionStatement/varDefSts_missing_Semicolon.bal");
	json expected4 = missingSemicolon();
	boolean comparison4 = actual4 == expected4;
	test:assertTrue(comparison4, msg = "AssertTrue failed");

	json actual5 = parseAst("resources/statements/variableDefinitionStatement/varDefSts_missing_varRefIdentifier.bal");
	json expected5 = missingVarDefIdentifier();
	boolean comparison5 = actual5 == expected5;
	test:assertTrue(comparison5, msg = "AssertTrue failed");
}
@test:Config
function testVarDefStValid(){
	json actual = parseAst("resources/statements/variableDefinitionStatement/varDefSts_valid.bal");
	json expected = validVarDefStatement();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

}
@test:Config
function testBinaryExprInvalid(){
	json actual = parseAst("resources/expression/binaryExpression/binary_excess_operator.bal");
	json expected = binaryExcessOperator();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual2 = parseAst("resources/expression/binaryExpression/binary_missing_operator.bal");
	json expected2 = binaryMissingOperator();
	boolean comparison2 = actual2 == expected2;
	test:assertTrue(comparison2, msg = "AssertTrue failed");

	json actual3 = parseAst("resources/expression/binaryExpression/binary_missing_RightExpr.bal");
	json expected3 = binaryMissingRightExpr();
	boolean comparison3 = actual3 == expected3;
	test:assertTrue(comparison3, msg = "AssertTrue failed");
}
@test:Config
function testBinaryExprValid(){
	json actual = parseAst("resources/expression/binaryExpression/binary_expr_valid.bal");
	json expected = binaryExprValid();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");
}
@test:Config
function testUnaryExprValid(){
	json actual = parseAst("resources/expression/unaryExpression/valid_unary_expression.bal");
	json expected = validUnaryExpr();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual2 = parseAst("resources/expression/unaryExpression/unary_multiple_operators.bal");
	json expected2 = multipleUnaryOperators();
	boolean comparison2 = actual2 == expected2;
	test:assertTrue(comparison2, msg = "AssertTrue failed");
}
@test:Config
function testUnaryExprInvalid(){
	json actual = parseAst("resources/expression/unaryExpression/unary_invalid_operator.bal");
	json expected = invalidUnaryOperator();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");
}
@test:Config
function testTupleLiteralValid(){
	json actual = parseAst("resources/expression/tupleLiteralExpression/tuple_valid_expr.bal");
	json expected = validTupleLiteral();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");
}
@test:Config
function testTupleLiteralInvalid(){
	json actual = parseAst("resources/expression/tupleLiteralExpression/tuple_invalid_comma.bal");
	json expected = tupleInvalidComma();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual3 = parseAst("resources/expression/tupleLiteralExpression/tuple_missing_expr.bal");
	json expected3 = tupleMissingExpr();
	boolean comparison3 = actual3 == expected3;
	test:assertTrue(comparison3, msg = "AssertTrue failed");

	json actual4 = parseAst("resources/expression/tupleLiteralExpression/tuple_missing_lparen.bal");
	json expected4 = tupleMissinglparen();
	boolean comparison4 = actual4 == expected4;
	test:assertTrue(comparison4, msg = "AssertTrue failed");

	json actual5 = parseAst("resources/expression/tupleLiteralExpression/tuple_missing_rparen.bal");
	json expected5 = tupleMissingRparen();
	boolean comparison5 = actual5 == expected5;
	test:assertTrue(comparison5, msg = "AssertTrue failed");

}
