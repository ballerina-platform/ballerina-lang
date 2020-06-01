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

import ballerina/test;

//invoke parser to convert to json
function parseAst(string fileLocation) returns @tainted json {
    PackageNode? pkNode = parseFile(fileLocation);
    if (pkNode is PackageNode) {
        json | error jsonOut = pkNode.cloneWithType(json);
        if (jsonOut is json) {
            return jsonOut;
        } else {
            test:assertFail(msg = "JSON conversion failed: " + <string>jsonOut.detail()?.message);
        }
    }
}

@test:Config {}
function testFunctionSigValid() {
    json actual = parseAst("src/parserModule/resources/function/fn_signature_valid.bal");
    json expected = fnSignatureValid();
    test:assertEquals(actual, expected);
}

@test:Config {}
function testFunctionSigInvalid() {
    json actual = parseAst("src/parserModule/resources/function/fn_signature_missing_identifier.bal");
    json expected = fnSigMissingIdentifier();
    test:assertEquals(actual, expected);

    json actual2 = parseAst("src/parserModule/resources/function/fn_signature_missing_lparen.bal");
    json expected2 = fnSignatureMissingLparen();
    test:assertEquals(actual2, expected2);

    json actual3 = parseAst("src/parserModule/resources/function/fn_signature_missing_rparen.bal");
    json expected3 = fnSignatureMissingRparen();
    test:assertEquals(actual3, expected3);
}

@test:Config {}
function testFunctionBodyValid() {
    json actual = parseAst("src/parserModule/resources/function/fn_body_valid.bal");
    json expected = fnBodyValid();
    test:assertEquals(actual, expected);
}

@test:Config {}
function testFunctionBodyInvalid() {
    json actual = parseAst("src/parserModule/resources/function/fn_body_missing_lbrace.bal");
    json expected = fnBodyMissingLbrace();
    test:assertEquals(actual, expected);

    json actual2 = parseAst("src/parserModule/resources/function/fn_body_missing_rbrace.bal");
    json expected2 = fnBodyMissingRbrace();
    test:assertEquals(actual2, expected2);
}

@test:Config {}
function testFunctionInvalid() {
    json actual = parseAst("src/parserModule/resources/function/fn_missing_signature_and_body.bal");
    json expected = fnMissingSigAndBody();
    test:assertEquals(actual, expected);

    json actual2 = parseAst("src/parserModule/resources/function/fn_incomplete_signature.bal");
    json expected2 = fnIncompleteSignature();
    test:assertEquals(actual2, expected2);
}

@test:Config {}
function testVarDefStInvalid() {
    json actual = parseAst("src/parserModule/resources/statements/variableDefinitionStatement/varDefSts_missing_binaryR&Semicolon.bal");
    json expected = missingBinaryRandSemicolon();
    test:assertEquals(actual, expected);

    json actual3 = parseAst("src/parserModule/resources/statements/variableDefinitionStatement/varDefSts_missing_expr&Semicolon.bal");
    json expected3 = missingExprandSemicolon();
    test:assertEquals(actual3, expected3);

    json actual4 = parseAst("src/parserModule/resources/statements/variableDefinitionStatement/varDefSts_missing_Semicolon.bal");
    json expected4 = missingSemicolon();
    test:assertEquals(actual4, expected4);

    json actual5 = parseAst("src/parserModule/resources/statements/variableDefinitionStatement/varDefSts_missing_varRefIdentifier.bal");
    json expected5 = missingVarDefIdentifier();
    test:assertEquals(actual5, expected5);
}

@test:Config {}
function testVarDefStValid() {
    json actual = parseAst("src/parserModule/resources/statements/variableDefinitionStatement/varDefSts_valid.bal");
    json expected = validVarDefStatement();
    test:assertEquals(actual, expected);
}

@test:Config {}
function testBinaryExprInvalid() {
    json actual = parseAst("src/parserModule/resources/expression/binaryExpression/binary_excess_operator.bal");
    json expected = binaryExcessOperator();
    test:assertEquals(actual, expected);

    json actual2 = parseAst("src/parserModule/resources/expression/binaryExpression/binary_missing_operator.bal");
    json expected2 = binaryMissingOperator();
    test:assertEquals(actual2, expected2);

    json actual3 = parseAst("src/parserModule/resources/expression/binaryExpression/binary_missing_RightExpr.bal");
    json expected3 = binaryMissingRightExpr();
    test:assertEquals(actual3, expected3);
}

@test:Config {}
function testBinaryExprValid() {
    json actual = parseAst("src/parserModule/resources/expression/binaryExpression/binary_expr_valid.bal");
    json expected = binaryExprValid();
    test:assertEquals(actual, expected);
}

@test:Config {}
function testUnaryExprValid() {
    json actual = parseAst("src/parserModule/resources/expression/unaryExpression/valid_unary_expression.bal");
    json expected = validUnaryExpr();
    test:assertEquals(actual, expected);

    json actual2 = parseAst("src/parserModule/resources/expression/unaryExpression/unary_multiple_operators.bal");
    json expected2 = multipleUnaryOperators();
    test:assertEquals(actual2, expected2);
}

@test:Config {}
function testUnaryExprInvalid() {
    json actual = parseAst("src/parserModule/resources/expression/unaryExpression/unary_invalid_operator.bal");
    json expected = invalidUnaryOperator();
    test:assertEquals(actual, expected);
}

@test:Config {}
function testTupleLiteralValid() {
    json actual = parseAst("src/parserModule/resources/expression/tupleLiteralExpression/tuple_valid_expr.bal");
    json expected = validTupleLiteral();
    test:assertEquals(actual, expected);
}

@test:Config {}
function testTupleLiteralInvalid() {
    json actual = parseAst("src/parserModule/resources/expression/tupleLiteralExpression/tuple_invalid_comma.bal");
    json expected = tupleInvalidComma();
    test:assertEquals(actual, expected);

    json actual3 = parseAst("src/parserModule/resources/expression/tupleLiteralExpression/tuple_missing_expr.bal");
    json expected3 = tupleMissingExpr();
    test:assertEquals(actual3, expected3);

    json actual4 = parseAst("src/parserModule/resources/expression/tupleLiteralExpression/tuple_missing_lparen.bal");
    json expected4 = tupleMissinglparen();
    test:assertEquals(actual4, expected4);

    json actual5 = parseAst("src/parserModule/resources/expression/tupleLiteralExpression/tuple_missing_rparen.bal");
    json expected5 = tupleMissingRparen();
    test:assertEquals(actual5, expected5);
}
