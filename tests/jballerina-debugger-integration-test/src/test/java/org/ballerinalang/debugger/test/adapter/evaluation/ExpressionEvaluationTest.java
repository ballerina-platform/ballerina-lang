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
        assertExpression(context, nilVar, "()", "nil");
        // boolean variable test
        assertExpression(context, booleanVar, "true", "boolean");
        // int variable test
        assertExpression(context, intVar, "20", "int");
        // float variable test
        assertExpression(context, floatVar, "-10.0", "float");
        // decimal variable test
        assertExpression(context, decimalVar, "3.5", "decimal");
        // string variable test
        assertExpression(context, stringVar, "foo", "string");
        // xml variable test
        assertExpression(context, xmlVar, "<person " +
                "gender=\"male\"><firstname>Praveen</firstname><lastname>Nada</lastname></person>", "xml");
        // array variable test
        assertExpression(context, arrayVar, "any[4]", "array");
        // tuple variable test
        assertExpression(context, tupleVar, "tuple[int,string]", "tuple");
        // map variable test
        assertExpression(context, mapVar, "map", "map");
        // record variable test (Student record)
        assertExpression(context, recordVar, "Student", "record");
        // anonymous record variable test
        assertExpression(context, anonRecordVar, "anonymous", "record");
        // error variable test
        assertExpression(context, errorVar, "SimpleErrorType", "error");
        // anonymous function variable test
        assertExpression(context, anonFunctionVar, "function (string,string) returns (string)", "function");
        // future variable test
        assertExpression(context, futureVar, "future", "future");
        // object variable test (Person object)
        assertExpression(context, objectVar, "Person", "object");
        // type descriptor variable test
        assertExpression(context, typeDescVar, "int", "typedesc");
        // union variable test
        assertExpression(context, unionVar, "foo", "string");
        // optional variable test
        assertExpression(context, optionalVar, "foo", "string");
        // any variable test
        assertExpression(context, anyVar, "15.0", "float");
        // anydata variable test
        assertExpression(context, anydataVar, "619", "int");
        // byte variable test
        assertExpression(context, byteVar, "128", "int");
        // table variable test
        assertExpression(context, tableVar, "table<Employee>", "table");
        // stream variable test
        assertExpression(context, streamVar, "stream<int>", "stream");
        // never variable test
        assertExpression(context, neverVar, "", "xml");
        // json variable test
        assertExpression(context, jsonVar, "object", "json");
        // Todo - Enable after fixing
        // anonymous object variable test (AnonPerson object)
        // assertVariable(context, anonObjectVar, "AnonPerson", "object");

        // Todo - add test for qualified name references, after adding support
    }

    @Override
    @Test
    public void fieldAccessEvaluationTest() throws BallerinaTestException {
        // objects fields
        assertExpression(context, objectVar + ".address", "No 20, Palm grove", "string");
        // record fields
        assertExpression(context, recordVar + ".age", "20", "int");
        // json fields
        assertExpression(context, jsonVar + ".name", "apple", "string");
        // nested field access (chain access)
        assertExpression(context, recordVar + ".grades.maths", "80", "int");
        // optional field access
        assertExpression(context, recordVar + "?.undefined", "()", "nil");
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
        assertExpression(context, stringVar + "[0]", "f", "string");
        // lists
        assertExpression(context, arrayVar + "[0]", "1", "int");
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
        // Todo
    }

    @Override
    @Test
    public void methodCallEvaluationTest() throws BallerinaTestException {
        // object methods
        assertExpression(context, objectVar + ".getSum(34,56)", "90", "int");
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
        // Todo
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
        assertExpression(context, String.format("%s * %s", intVar, intVar), "400", "int");
        // float * int
        assertExpression(context, String.format("%s * %s", floatVar, intVar), "-200.0", "float");
        // int * float
        assertExpression(context, String.format("%s * %s", intVar, floatVar), "-200.0", "float");
        // float * float
        assertExpression(context, String.format("%s * %s", floatVar, floatVar), "100.0", "float");
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
        assertExpression(context, String.format("%s / %s", intVar, intVar), "1", "int");
        // float / int
        assertExpression(context, String.format("%s / %s", floatVar, intVar), "-0.5", "float");
        // int / float
        assertExpression(context, String.format("%s / %s", intVar, floatVar), "-2.0", "float");
        // float / float
        assertExpression(context, String.format("%s / %s", floatVar, floatVar), "1.0", "float");
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
        assertExpression(context, String.format("%s %% %s", intVar, intVar), "0", "int");
        // float % int
        assertExpression(context, String.format("%s %% %s", floatVar, intVar), "-10.0", "float");
        // int % float
        assertExpression(context, String.format("%s %% %s", intVar, floatVar), "0.0", "float");
        // float % float
        assertExpression(context, String.format("%s %% %s", floatVar, floatVar), "-0.0", "float");
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
        assertExpression(context, String.format("%s + %s", intVar, intVar), "40", "int");
        // float + int
        assertExpression(context, String.format("%s + %s", floatVar, intVar), "10.0", "float");
        // int + float
        assertExpression(context, String.format("%s + %s", intVar, floatVar), "10.0", "float");
        // float + float
        assertExpression(context, String.format("%s + %s", floatVar, floatVar), "-20.0", "float");
        // string + string
        assertExpression(context, String.format("%s + %s", stringVar, stringVar), "foofoo", "string");
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
        assertExpression(context, String.format("%s - %s", intVar, intVar), "0", "int");
        // float - int
        assertExpression(context, String.format("%s - %s", floatVar, intVar), "-30.0", "float");
        // int - float
        assertExpression(context, String.format("%s - %s", intVar, floatVar), "30.0", "float");
        // float - float
        assertExpression(context, String.format("%s - %s", floatVar, floatVar), "0.0", "float");
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
        assertExpression(context, String.format("%s < %s", intVar, intVar), "false", "boolean");
        // float - int
        assertExpression(context, String.format("%s < %s", floatVar, intVar), "true", "boolean");
        // int - float
        assertExpression(context, String.format("%s < %s", intVar, floatVar), "false", "boolean");
        // float - float
        assertExpression(context, String.format("%s < %s", floatVar, floatVar), "false", "boolean");
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
        assertExpression(context, String.format("%s > %s", intVar, intVar), "false", "boolean");
        // float - int
        assertExpression(context, String.format("%s > %s", floatVar, intVar), "false", "boolean");
        // int - float
        assertExpression(context, String.format("%s > %s", intVar, floatVar), "true", "boolean");
        // float - float
        assertExpression(context, String.format("%s > %s", floatVar, floatVar), "false", "boolean");
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
        assertExpression(context, String.format("%s <= %s", intVar, intVar), "true", "boolean");
        // float - int
        assertExpression(context, String.format("%s <= %s", floatVar, intVar), "true", "boolean");
        // int - float
        assertExpression(context, String.format("%s <= %s", intVar, floatVar), "false", "boolean");
        // float - float
        assertExpression(context, String.format("%s <= %s", floatVar, floatVar), "true", "boolean");
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
        assertExpression(context, String.format("%s >= %s", intVar, intVar), "true", "boolean");
        // float - int
        assertExpression(context, String.format("%s >= %s", floatVar, intVar), "false", "boolean");
        // int - float
        assertExpression(context, String.format("%s >= %s", intVar, floatVar), "true", "boolean");
        // float - float
        assertExpression(context, String.format("%s >= %s", floatVar, floatVar), "true", "boolean");
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
        // Todo
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
        assertExpression(context, String.format("%s ? %s : %s", booleanVar, intVar, floatVar), "20", "int");
        // expression ?: expression
        assertExpression(context, String.format("%s ?: %s", intVar, floatVar), "20", "int");
        assertExpression(context, String.format("%s ?: %s", nilVar, floatVar), "-10.0", "float");
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
