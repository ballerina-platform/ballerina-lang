/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugger.test.adapter.evaluation;

import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test implementation for debug expression evaluation scenarios.
 */
public class ExpressionEvaluationTest extends ExpressionEvaluationBaseTest {

    @BeforeClass
    public void setup() throws BallerinaTestException {
        prepareForEvaluation();
    }

    @Override
    @Test
    public void literalEvaluationTest() throws BallerinaTestException {
        // nil literal
        assertExpression(context, "()", "()", "nil");
        // boolean literal
        assertExpression(context, "true", "true", "boolean");
        // numeric literal (int)
        assertExpression(context, "10", "10", "int");
        // numeric literal (float)
        assertExpression(context, "20.0", "20.0", "float");
        // Todo - add following tests after the implementation
        //  - hex int
        //  - hex float
        //  - string literal
        //  - byte-array literal
    }

    @Override
    @Test
    public void listConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void mappingConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void stringTemplateEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void xmlTemplateEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void newConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void variableReferenceEvaluationTest() throws BallerinaTestException {
        // var variable test
        assertExpression(context, NIL_VAR, "()", "nil");
        // boolean variable test
        assertExpression(context, BOOLEAN_VAR, "true", "boolean");
        // int variable test
        assertExpression(context, INT_VAR, "20", "int");
        // float variable test
        assertExpression(context, FLOAT_VAR, "-10.0", "float");
        // decimal variable test
        assertExpression(context, DECIMAL_VAR, "3.5", "decimal");
        // string variable test
        assertExpression(context, STRING_VAR, "foo", "string");
        // xml variable test
        assertExpression(context, XML_VAR, "<person " +
                "gender=\"male\"><firstname>Praveen</firstname><lastname>Nada</lastname></person>", "xml");
        // array variable test
        assertExpression(context, ARRAY_VAR, "any[4]", "array");
        // tuple variable test
        assertExpression(context, TUPLE_VAR, "tuple[int,string]", "tuple");
        // map variable test
        assertExpression(context, MAP_VAR, "map", "map");
        // record variable test (Student record)
        assertExpression(context, RECORD_VAR, " /:@[`{~⌤_123_ƮέŞŢ_Student", "record");
        // anonymous record variable test
        assertExpression(context, ANON_RECORD_VAR, "anonymous", "record");
        // error variable test
        assertExpression(context, ERROR_VAR, "SimpleErrorType", "error");
        // anonymous function variable test
        assertExpression(context, ANON_FUNCTION_VAR, "function (string,string) returns (string)", "function");
        // future variable test
        assertExpression(context, FUTURE_VAR, "future", "future");
        // object variable test (Person object)
        assertExpression(context, OBJECT_VAR, "Person_\\ /<>:@[`{~⌤_ƮέŞŢ", "object");
        // type descriptor variable test
        assertExpression(context, TYPEDESC_VAR, "int", "typedesc");
        // union variable test
        assertExpression(context, UNION_VAR, "foo", "string");
        // optional variable test
        assertExpression(context, OPTIONAL_VAR, "foo", "string");
        // any variable test
        assertExpression(context, ANY_VAR, "15.0", "float");
        // anydata variable test
        assertExpression(context, ANYDATA_VAR, "619", "int");
        // byte variable test
        assertExpression(context, BYTE_VAR, "128", "int");
        // table variable test
        assertExpression(context, TABLE_VAR, "table<Employee>", "table");
        // stream variable test
        assertExpression(context, STREAM_VAR, "stream<int>", "stream");
        // never variable test
        assertExpression(context, NEVER_VAR, "", "xml");
        // json variable test
        assertExpression(context, JSON_VAR, "map<json>", "json");
        // anonymous object variable test (AnonPerson object)
        assertExpression(context, ANON_OBJECT_VAR, "Person_\\ /<>:@[`{~⌤_ƮέŞŢ", "object");

        // Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/26139
        // assertExpression(context, GL, "Ballerina", "string");
        // assertExpression(context, "gv02_nameWithType", "Ballerina", "string");
        assertExpression(context, GLOBAL_VAR_03, "map", "map");
        assertExpression(context, GLOBAL_VAR_04, "()", "nil");
        assertExpression(context, GLOBAL_VAR_05, "()", "nil");
        // global variables
        assertExpression(context, GLOBAL_VAR_06, "Ballerina", "string");
        assertExpression(context, GLOBAL_VAR_07, "100.0", "decimal");
        assertExpression(context, GLOBAL_VAR_08, "2", "int");
        assertExpression(context, GLOBAL_VAR_09, "2.0", "float");
        assertExpression(context, GLOBAL_VAR_10, "map<json>", "json");
        assertExpression(context, GLOBAL_VAR_11, "IL with global var", "string");

        // Todo - add test for qualified name references, after adding support
    }

    @Override
    @Test
    public void fieldAccessEvaluationTest() throws BallerinaTestException {
        // objects fields
        assertExpression(context, OBJECT_VAR + ".address", "No 20, Palm grove", "string");
        // record fields
        assertExpression(context, RECORD_VAR + ".'Ȧɢέ_\\ \\/\\:\\@\\[\\`\\{\\~⌤", "20", "int");
        // json fields
        assertExpression(context, JSON_VAR + ".name", "apple", "string");
        // nested field access (chain access)
        assertExpression(context, RECORD_VAR + ".grades.maths", "80", "int");
        // optional field access
        assertExpression(context, RECORD_VAR + "?.undefined", "()", "nil");
    }

    @Override
    @Test
    public void xmlAttributeAccessEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void annotationAccessEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void memberAccessEvaluationTest() throws BallerinaTestException {
        // strings
        assertExpression(context, STRING_VAR + "[0]", "f", "string");
        // lists
        assertExpression(context, ARRAY_VAR + "[0]", "1", "int");
        // maps
        // assertExpression(context, mapVar + "[\"country\"]", "Sri Lanka", "string");
        // assertExpression(context, mapVar + "[\"undefined\"]", "()", "nil");
        // json
        // assertExpression(context, jsonVar + "[\"color\"]", "red", "string");
        // assertExpression(context, jsonVar + "[\"undefined\"]", "()", "nil");
        // Todo - add following tests after the implementation
        //  - xml member access
    }

    @Override
    @Test
    public void functionCallEvaluationTest() throws BallerinaTestException {
        // functions in a different module file.
        assertExpression(context, "sum(34,56)", "90", "int");
        assertExpression(context, "getName(\"John\")", "Name: John", "string");
    }

    @Override
    @Test
    public void methodCallEvaluationTest() throws BallerinaTestException {
        // object methods
        assertExpression(context, OBJECT_VAR + ".getSum(34,56)", "90", "int");
        // Todo - add lang-lib functions related tests, after the implementation
    }

    @Override
    @Test
    public void errorConstructorEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void anonymousFunctionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void letExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void typeCastEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void typeOfExpressionEvaluationTest() throws BallerinaTestException {
        // primitive types
        assertExpression(context, String.format("typeof %s", BOOLEAN_VAR), "boolean", "typedesc");
        assertExpression(context, String.format("typeof %s", INT_VAR), "int", "typedesc");
        assertExpression(context, String.format("typeof %s", FLOAT_VAR), "float", "typedesc");
        // reference types
        assertExpression(context, String.format("typeof %s", JSON_VAR), "map<json>", "typedesc");
        assertExpression(context, String.format("typeof %s[0]", STRING_VAR), "string", "typedesc");
        assertExpression(context, String.format("typeof typeof %s", BOOLEAN_VAR), "typedesc", "typedesc");
    }

    @Override
    @Test
    public void unaryExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void multiplicativeExpressionEvaluationTest() throws BallerinaTestException {
        ///////////////////////////////-----------multiplication----------------////////////////////////////////////////
        // int * int
        assertExpression(context, String.format("%s * %s", INT_VAR, INT_VAR), "400", "int");
        // float * int
        assertExpression(context, String.format("%s * %s", FLOAT_VAR, INT_VAR), "-200.0", "float");
        // int * float
        assertExpression(context, String.format("%s * %s", INT_VAR, FLOAT_VAR), "-200.0", "float");
        // float * float
        assertExpression(context, String.format("%s * %s", FLOAT_VAR, FLOAT_VAR), "100.0", "float");
        // Todo - Enable after adding support
        //        // decimal * decimal
        //        assertExpression(context, String.format("%s * %s", decimalVar, decimalVar), "10.0", "decimal");
        //        // int * decimal
        //        assertExpression(context, String.format("%s * %s", decimalVar, decimalVar), "70", "decimal");
        //        // decimal * int
        //        assertExpression(context, String.format("%s * %s", decimalVar, decimalVar), "70", "decimal");
        //        // float * decimal
        //        assertExpression(context, String.format("%s * %s", decimalVar, decimalVar), "-35", "decimal");
        //        // decimal * float
        //        assertExpression(context, String.format("%s * %s", decimalVar, decimalVar), "-35", "decimal");

        ///////////////////////////////-------------division--------------------////////////////////////////////////////
        // int / int
        assertExpression(context, String.format("%s / %s", INT_VAR, INT_VAR), "1", "int");
        // float / int
        assertExpression(context, String.format("%s / %s", FLOAT_VAR, INT_VAR), "-0.5", "float");
        // int / float
        assertExpression(context, String.format("%s / %s", INT_VAR, FLOAT_VAR), "-2.0", "float");
        // float / float
        assertExpression(context, String.format("%s / %s", FLOAT_VAR, FLOAT_VAR), "1.0", "float");
        // Todo - Enable after adding support
        //        // decimal / decimal
        //        assertExpression(context, String.format("%s / %s", decimalVar, decimalVar), "10.0", "decimal");
        //        // int / decimal
        //        assertExpression(context, String.format("%s / %s", decimalVar, decimalVar), "70", "decimal");
        //        // decimal / int
        //        assertExpression(context, String.format("%s / %s", decimalVar, decimalVar), "70", "decimal");
        //        // float / decimal
        //        assertExpression(context, String.format("%s / %s", decimalVar, decimalVar), "-35", "decimal");
        //        // decimal / float
        //        assertExpression(context, String.format("%s / %s", decimalVar, decimalVar), "-35", "decimal");

        /////////////////////////////////-----------modulus----------------/////////////////////////////////////////////
        // int % int
        assertExpression(context, String.format("%s %% %s", INT_VAR, INT_VAR), "0", "int");
        // float % int
        assertExpression(context, String.format("%s %% %s", FLOAT_VAR, INT_VAR), "-10.0", "float");
        // int % float
        assertExpression(context, String.format("%s %% %s", INT_VAR, FLOAT_VAR), "0.0", "float");
        // float % float
        assertExpression(context, String.format("%s %% %s", FLOAT_VAR, FLOAT_VAR), "-0.0", "float");
        // Todo - Enable after adding support
        //        // decimal % decimal
        //        assertExpression(context, String.format("%s % %s", decimalVar, decimalVar), "10.0", "decimal");
        //        // int % decimal
        //        assertExpression(context, String.format("%s % %s", decimalVar, decimalVar), "70", "decimal");
        //        // decimal % int
        //        assertExpression(context, String.format("%s % %s", decimalVar, decimalVar), "70", "decimal");
        //        // float % decimal
        //        assertExpression(context, String.format("%s % %s", decimalVar, decimalVar), "-35", "decimal");
        //        // decimal % float
        //        assertExpression(context, String.format("%s % %s", decimalVar, decimalVar), "-35", "decimal");
    }

    @Override
    @Test
    public void additiveExpressionEvaluationTest() throws BallerinaTestException {
        //////////////////////////////-------------addition------------------///////////////////////////////////////////
        // int + int
        assertExpression(context, String.format("%s + %s", INT_VAR, INT_VAR), "40", "int");
        // float + int
        assertExpression(context, String.format("%s + %s", FLOAT_VAR, INT_VAR), "10.0", "float");
        // int + float
        assertExpression(context, String.format("%s + %s", INT_VAR, FLOAT_VAR), "10.0", "float");
        // float + float
        assertExpression(context, String.format("%s + %s", FLOAT_VAR, FLOAT_VAR), "-20.0", "float");
        // string + string
        assertExpression(context, String.format("%s + %s", STRING_VAR, STRING_VAR), "foofoo", "string");
        // Todo - Enable after adding support
        //        // decimal + decimal
        //        assertExpression(context, String.format("%s + %s", decimalVar, decimalVar), "7.0", "decimal");
        //        // int + decimal
        //        assertExpression(context, String.format("%s + %s", decimalVar, decimalVar), "27.0", "decimal");
        //        // decimal + int
        //        assertExpression(context, String.format("%s + %s", decimalVar, decimalVar), "27.0", "decimal");
        //        // float + decimal
        //        assertExpression(context, String.format("%s + %s", decimalVar, decimalVar), "-6.5", "decimal");
        //        // decimal + float
        //        assertExpression(context, String.format("%s + %s", decimalVar, decimalVar), "-6.5", "decimal");
        //        // xml + xml
        //        assertExpression(context, String.format("%s + %s", xmlVar, xmlVar), "", "xml");

        //////////////////////////////-------------subtraction------------------////////////////////////////////////////
        // int - int
        assertExpression(context, String.format("%s - %s", INT_VAR, INT_VAR), "0", "int");
        // float - int
        assertExpression(context, String.format("%s - %s", FLOAT_VAR, INT_VAR), "-30.0", "float");
        // int - float
        assertExpression(context, String.format("%s - %s", INT_VAR, FLOAT_VAR), "30.0", "float");
        // float - float
        assertExpression(context, String.format("%s - %s", FLOAT_VAR, FLOAT_VAR), "0.0", "float");
        // Todo - Enable after adding support
        //        // decimal - decimal
        //        assertExpression(context, String.format("%s - %s", decimalVar, decimalVar), "0.0", "decimal");
        //        // int - decimal
        //        assertExpression(context, String.format("%s - %s", decimalVar, decimalVar), "16.5", "decimal");
        //        // decimal - int
        //        assertExpression(context, String.format("%s - %s", decimalVar, decimalVar), "-16.5", "decimal");
        //        // float - decimal
        //        assertExpression(context, String.format("%s - %s", decimalVar, decimalVar), "-13.5", "decimal");
        //        // decimal - float
        //        assertExpression(context, String.format("%s - %s", decimalVar, decimalVar), "13.5", "decimal");
    }

    @Override
    @Test
    public void shiftExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void rangeExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void comparisonEvaluationTest() throws BallerinaTestException {
        // expression < expression
        // int - int
        assertExpression(context, String.format("%s < %s", INT_VAR, INT_VAR), "false", "boolean");
        // float - int
        assertExpression(context, String.format("%s < %s", FLOAT_VAR, INT_VAR), "true", "boolean");
        // int - float
        assertExpression(context, String.format("%s < %s", INT_VAR, FLOAT_VAR), "false", "boolean");
        // float - float
        assertExpression(context, String.format("%s < %s", FLOAT_VAR, FLOAT_VAR), "false", "boolean");
        // Todo - Enable after adding support
        //        // decimal - decimal
        //        assertExpression(context, String.format("%s < %s", decimalVar, decimalVar), "false", "boolean");
        //        // int - decimal
        //        assertExpression(context, String.format("%s < %s", decimalVar, decimalVar), "false", "boolean");
        //        // decimal - int
        //        assertExpression(context, String.format("%s < %s", decimalVar, decimalVar), "false", "boolean");
        //        // float - decimal
        //        assertExpression(context, String.format("%s < %s", decimalVar, decimalVar), "false", "boolean");
        //        // decimal - float
        //        assertExpression(context, String.format("%s < %s", decimalVar, decimalVar), "false", "boolean");

        // expression > expression
        // int - int
        assertExpression(context, String.format("%s > %s", INT_VAR, INT_VAR), "false", "boolean");
        // float - int
        assertExpression(context, String.format("%s > %s", FLOAT_VAR, INT_VAR), "false", "boolean");
        // int - float
        assertExpression(context, String.format("%s > %s", INT_VAR, FLOAT_VAR), "true", "boolean");
        // float - float
        assertExpression(context, String.format("%s > %s", FLOAT_VAR, FLOAT_VAR), "false", "boolean");
        // Todo - Enable after adding support
        //        // decimal - decimal
        //        assertExpression(context, String.format("%s > %s", decimalVar, decimalVar), "false", "boolean");
        //        // int - decimal
        //        assertExpression(context, String.format("%s > %s", decimalVar, decimalVar), "false", "boolean");
        //        // decimal - int
        //        assertExpression(context, String.format("%s > %s", decimalVar, decimalVar), "false", "boolean");
        //        // float - decimal
        //        assertExpression(context, String.format("%s > %s", decimalVar, decimalVar), "false", "boolean");
        //        // decimal - float
        //        assertExpression(context, String.format("%s > %s", decimalVar, decimalVar), "false", "boolean");

        // expression <= expression
        // int - int
        assertExpression(context, String.format("%s <= %s", INT_VAR, INT_VAR), "true", "boolean");
        // float - int
        assertExpression(context, String.format("%s <= %s", FLOAT_VAR, INT_VAR), "true", "boolean");
        // int - float
        assertExpression(context, String.format("%s <= %s", INT_VAR, FLOAT_VAR), "false", "boolean");
        // float - float
        assertExpression(context, String.format("%s <= %s", FLOAT_VAR, FLOAT_VAR), "true", "boolean");
        // Todo - Enable after adding support
        //        // decimal - decimal
        //        assertExpression(context, String.format("%s <= %s", decimalVar, decimalVar), "false", "boolean");
        //        // int - decimal
        //        assertExpression(context, String.format("%s <= %s", decimalVar, decimalVar), "false", "boolean");
        //        // decimal - int
        //        assertExpression(context, String.format("%s <= %s", decimalVar, decimalVar), "false", "boolean");
        //        // float - decimal
        //        assertExpression(context, String.format("%s <= %s", decimalVar, decimalVar), "false", "boolean");
        //        // decimal - float
        //        assertExpression(context, String.format("%s <= %s", decimalVar, decimalVar), "false", "boolean");

        // expression >= expression
        // int - int
        assertExpression(context, String.format("%s >= %s", INT_VAR, INT_VAR), "true", "boolean");
        // float - int
        assertExpression(context, String.format("%s >= %s", FLOAT_VAR, INT_VAR), "false", "boolean");
        // int - float
        assertExpression(context, String.format("%s >= %s", INT_VAR, FLOAT_VAR), "true", "boolean");
        // float - float
        assertExpression(context, String.format("%s >= %s", FLOAT_VAR, FLOAT_VAR), "true", "boolean");
        // Todo - Enable after adding support
        //        // decimal - decimal
        //        assertExpression(context, String.format("%s >= %s", decimalVar, decimalVar), "false", "boolean");
        //        // int - decimal
        //        assertExpression(context, String.format("%s >= %s", decimalVar, decimalVar), "false", "boolean");
        //        // decimal - int
        //        assertExpression(context, String.format("%s >= %s", decimalVar, decimalVar), "false", "boolean");
        //        // float - decimal
        //        assertExpression(context, String.format("%s >= %s", decimalVar, decimalVar), "false", "boolean");
        //        // decimal - float
        //        assertExpression(context, String.format("%s >= %s", decimalVar, decimalVar), "false", "boolean");
    }

    @Override
    @Test
    public void typeTestEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void equalityEvaluationTest() throws BallerinaTestException {
        // value equality
        assertExpression(context, "2.0==2.00", "true", "boolean");
        assertExpression(context, String.format("%s==%s", INT_VAR, FLOAT_VAR), "false", "boolean");
        assertExpression(context, String.format("%s==%s", OBJECT_VAR, ANON_OBJECT_VAR), "false", "boolean");
        assertExpression(context, "2.0!=2.00", "false", "boolean");
        assertExpression(context, String.format("%s!=%s", INT_VAR, FLOAT_VAR), "true", "boolean");
        assertExpression(context, String.format("%s!=%s", OBJECT_VAR, ANON_OBJECT_VAR), "true", "boolean");
        // reference equality
        assertExpression(context, "2.0===2.00", "true", "boolean");
        assertExpression(context, String.format("%s===%s", INT_VAR, FLOAT_VAR), "false", "boolean");
        assertExpression(context, String.format("%s===%s", OBJECT_VAR, ANON_OBJECT_VAR), "false", "boolean");
        assertExpression(context, "2.0!==2.00", "false", "boolean");
        assertExpression(context, String.format("%s!==%s", INT_VAR, FLOAT_VAR), "true", "boolean");
        assertExpression(context, String.format("%s!==%s", OBJECT_VAR, ANON_OBJECT_VAR), "true", "boolean");
    }

    @Override
    @Test
    public void binaryBitwiseEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void logicalEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void conditionalExpressionEvaluationTest() throws BallerinaTestException {
        // expression ? expression : expression
        assertExpression(context, String.format("%s ? %s : %s", BOOLEAN_VAR, INT_VAR, FLOAT_VAR), "20", "int");
        // expression ?: expression
        assertExpression(context, String.format("%s ?: %s", INT_VAR, FLOAT_VAR), "20", "int");
        assertExpression(context, String.format("%s ?: %s", NIL_VAR, FLOAT_VAR), "-10.0", "float");
    }

    @Override
    @Test
    public void checkingExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void trapExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void queryExpressionEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @Override
    @Test
    public void xmlNavigationEvaluationTest() throws BallerinaTestException {
        // Todo
    }

    @AfterClass
    private void cleanup() {
        terminateDebugSession();
        this.context = null;
    }
}
