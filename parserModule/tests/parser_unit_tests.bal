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
@test:BeforeSuite
function beforeFunc() {

}
//valid fucntion signature test
@test:Config
function testFunctionSigValid() {
	json actual = parseAst("parserModule/tests/resources/function/fn_signature.txt");
	json expected = fnSignatureValid();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");
	//test:assertEquals(actual,expected, msg = "function signature failed");
}
@test:Config
function testFunctionSigInvalid(){
	json actual = parseAst("parserModule/tests/resources/function/fn_signature_missing_identifier.txt");
	json expected = fnSigMissingIdentifier();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual2 = parseAst("parserModule/tests/resources/function/fn_signature_missing_lparen.txt");
	json expected2 = fnSignatureMissingLparen();
	boolean comparison2 = actual2 == expected2;
	test:assertTrue(comparison2, msg = "AssertTrue failed");

	json actual3 = parseAst("parserModule/tests/resources/function/fn_signature_missing_rparen.txt");
	json expected3 = fnSignatureMissingRparen();
	boolean comparison3 = actual3 == expected3;
	test:assertTrue(comparison3, msg = "AssertTrue failed");
}
@test:Config
function testFunctionBodyValid(){
	json actual = parseAst("parserModule/tests/resources/function/fn_body.txt");
	json expected = fnBodyValid();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");
}
@test:Config
function testFunctionBodyInvalid(){
	json actual = parseAst("parserModule/tests/resources/function/fn_body_missing_lbrace.txt");
	json expected = fnBodyMissingLbrace();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual2 = parseAst("parserModule/tests/resources/function/fn_body_missing_rbrace.txt");
	json expected2 = fnBodyMissingRbrace();
	boolean comparison2 = actual2 == expected2;
	test:assertTrue(comparison2, msg = "AssertTrue failed");
}
@test:Config
function testFunctionInvalid(){
	json actual = parseAst("parserModule/tests/resources/function/fn_missing_signature_and_body.txt");
	json expected = fnMissingSigAndBody();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual2 = parseAst("parserModule/tests/resources/function/fn_incomplete_signature.txt");
	json expected2 = fnIncompleteSignature();
	boolean comparison2 = actual2 == expected2;
	test:assertTrue(comparison2, msg = "AssertTrue failed");
}
@test:Config
function testVarDefStInvalid(){
	json actual = parseAst("parserModule/tests/resources/statements/variableDefinitionStatement/varDefSts_missing_binaryR&Semicolon.txt");
	json expected = missingBinaryRandSemicolon();
	boolean comparison = actual == expected;
	test:assertTrue(comparison, msg = "AssertTrue failed");

	json actual2 = parseAst("parserModule/tests/resources/statements/variableDefinitionStatement/varDefSts_missing_binaryRight.txt");
	json expected2 = missingBinaryRight();
	boolean comparison2 = actual2 == expected2;
	test:assertTrue(comparison2, msg = "AssertTrue failed");

	json actual3 = parseAst("parserModule/tests/resources/statements/variableDefinitionStatement/varDefSts_missing_expr&Semicolon.txt");
	json expected3 = missingExprandSemicolon();
	boolean comparison3 = actual3 == expected3;
	test:assertTrue(comparison3, msg = "AssertTrue failed");

	json actual4 = parseAst("parserModule/tests/resources/statements/variableDefinitionStatement/varDefSts_missing_Semicolon.txt");
	json expected4 = missingSemicolon();
	boolean comparison4 = actual4 == expected4;
	test:assertTrue(comparison4, msg = "AssertTrue failed");

	json actual5 = parseAst("parserModule/tests/resources/statements/variableDefinitionStatement/varDefSts_missing_varRefIdentifier.txt");
	json expected5 = missingVarDefIdentifier();
	boolean comparison5 = actual5 == expected5;
	test:assertTrue(comparison5, msg = "AssertTrue failed");
}






