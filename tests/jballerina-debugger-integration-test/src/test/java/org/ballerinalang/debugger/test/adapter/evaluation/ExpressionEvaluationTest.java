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

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.DebugAdapterBaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Test implementation for debug expression evaluation scenarios.
 */
public class ExpressionEvaluationTest extends DebugAdapterBaseTestCase {

    private StoppedEventArguments context;

    private static final String nilVar = "v01_varVariable";
    private static final String booleanVar = "v02_booleanVar";
    private static final String intVar = "v03_intVar";
    private static final String floatVar = "v04_floatVar";
    private static final String decimalVar = "v05_decimalVar";
    private static final String stringVar = "v06_stringVar";
    private static final String xmlVar = "v07_xmlVar";
    private static final String arrayVar = "v08_arrayVar";
    private static final String tupleVar = "v09_tupleVar";
    private static final String mapVar = "v10_mapVar";
    private static final String recordVar = "v11_john";
    private static final String anonRecordVar = "v12_anonRecord";
    private static final String errorVar = "v13_errorVar";
    private static final String anonFunctionVar = "v14_anonFunctionVar";
    private static final String futureVar = "v15_futureVar";
    private static final String objectVar = "v16_objectVar";
    private static final String anonObjectVar = "v17_anonObjectVar";
    private static final String typeDescVar = "v18_typedescVar";
    private static final String unionVar = "v19_unionVar";
    private static final String optionalVar = "v20_optionalVar";
    private static final String anyVar = "v21_anyVar";
    private static final String anydataVar = "v22_anydataVar";
    private static final String byteVar = "v23_byteVar";
    private static final String jsonVar = "v24_jsonVar";
    private static final String tableVar = "v25_tableVar";
    private static final String streamVar = "v26_oddNumberStream";
    private static final String neverVar = "v27_neverVar";

    @BeforeClass(enabled = false)
    public void setup() throws BallerinaTestException {
        testProjectName = "basic-project";
        testModuleName = "advanced";
        testModuleFileName = "main.bal";
        testProjectPath = Paths.get(testProjectBaseDir.toString(), testProjectName).toString();
        testEntryFilePath = Paths.get(testProjectPath, "src", testModuleName, testModuleFileName).toString();

        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 154));
        initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(25000);
        this.context = debugHitInfo.getRight();
    }

    @Test(enabled = false)
    public void variableEvaluationTest() throws BallerinaTestException {
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
    }

    @Test(enabled = false)
    public void arithmeticEvaluationTest() throws BallerinaTestException {
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

    @Test(enabled = false)
    public void fieldAccessEvaluationTest() throws BallerinaTestException {
        // objects fields
        assertExpression(context, objectVar + ".address", "No 20, Palm grove", "string");
        // record fields
        assertExpression(context, recordVar + ".age", "20", "int");
        // json fields
        assertExpression(context, jsonVar + ".name", "apple", "string");
        // nested field access (chain access)
        assertExpression(context, recordVar + ".grades.maths", "80", "int");
    }

    @Test(enabled = false)
    public void methodCallEvaluationTest() throws BallerinaTestException {
        // object methods
        assertExpression(context, objectVar + ".getSum(34,56)", "90", "int");
        // Todo - add lang-lib functions related tests, after the implementation
    }

    @Test(enabled = false)
    public void expressionEvaluationNegativeTest() throws BallerinaTestException {
        // empty expressions
        assertEvaluationError(context, "  ", EvaluationExceptionKind.EMPTY.getString());
        // unsupported expressions
        assertEvaluationError(context, "~x", String.format(EvaluationExceptionKind.UNSUPPORTED_EXPRESSION
                .getString(), "~x - UNARY_EXPRESSION"));
        // syntactically incorrect expressions (additional semi-colon)
        assertEvaluationError(context, "x + 5;;", String.format(EvaluationExceptionKind.SYNTAX_ERROR
                .getString(), "invalid token ';'"));
        // semantically incorrect expressions (addition between int + string)
        assertEvaluationError(context, String.format("%s + %s", intVar, stringVar),
                String.format(EvaluationExceptionKind.UNSUPPORTED_EXPRESSION.getString(),
                        "'+' operation is not supported for types: 'int' and 'string'"));
        // undefined variables
        assertEvaluationError(context, "x", String.format(EvaluationExceptionKind.VARIABLE_NOT_FOUND.getString(), "x"));
        // undefined field access
        assertEvaluationError(context, jsonVar + ".undefined",
                String.format(EvaluationExceptionKind.FIELD_NOT_FOUND.getString(), "undefined", jsonVar));
        // undefined object methods
        assertEvaluationError(context, objectVar + ".undefined()",
                String.format(EvaluationExceptionKind.OBJECT_METHOD_NOT_FOUND.getString(), "undefined"));
        // Todo - Enable
        // assignment statements
        // assertEvaluationError(context, "int x = 5;", "");

        // Todo - Add negative tests for function invocations related errors. (invalid argument validation, etc.).
    }

    @AfterClass(enabled = false)
    private void cleanup() {
        terminateDebugSession();
        this.context = null;
    }
}
