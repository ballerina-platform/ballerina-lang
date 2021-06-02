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

package org.ballerinalang.debugger.test.adapter.variables;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.debugger.test.BaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugTestRunner;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.Variable;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.debugger.test.utils.DebugTestRunner.VariableScope;

/**
 * Test class for variable visibility.
 */
public class VariableVisibilityTest extends BaseTestCase {

    Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo;
    Map<String, Variable> globalVariables = new HashMap<>();
    Map<String, Variable> localVariables = new HashMap<>();
    DebugTestRunner debugTestRunner;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        String testProjectName = "variable-tests";
        String testModuleFileName = "main.bal";
        debugTestRunner = new DebugTestRunner(testProjectName, testModuleFileName, true);

        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 117));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 190));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 201));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 201));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 208));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 215));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 224));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 230));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 237));
        // Todo - enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/27738
        // debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 239));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 270));
        debugTestRunner.addBreakPoint(new BallerinaTestDebugPoint(debugTestRunner.testEntryFilePath, 248));
        debugTestRunner.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
    }

    @Test(description = "Variable visibility test at the beginning(first line) of the main() method")
    public void initialVariableVisibilityTest() throws BallerinaTestException {
        debugHitInfo = debugTestRunner.waitForDebugHit(25000);
        globalVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), VariableScope.GLOBAL);
        localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), VariableScope.LOCAL);

        // TODO - enable test
        // local variable visibility test at the beginning of the main() method (should be 0).
        // Assert.assertEquals(localVariables.size(), 0);

        // global variable visibility test at the beginning of the main() method (should be 14).
         Assert.assertEquals(globalVariables.size(), 14);
    }

    @Test(dependsOnMethods = "initialVariableVisibilityTest",
        description = "Variable visibility test in the middle of the main() method for a new variable")
    public void newVariableVisibilityTest() throws BallerinaTestException {
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope.LOCAL);

        // TODO - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/28064
        // local variable visibility test in the middle of the main() method,
        // all the variables above the debug point should be visible,
        // Assert.assertEquals(localVariables.size(), 30);

        // TODO - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/28064
        // debug point variable should not be visible
        // Assert.assertFalse(localVariables.containsKey("byteVar"));

        // debug point variable should be visible when we go to the next line (STEP_OVER).
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.STEP_OVER);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope.LOCAL);
        // TODO - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/28064
        // Assert.assertEquals(localVariables.size(), 31);
        Assert.assertTrue(localVariables.containsKey("byteVar"));
    }

    @Test(dependsOnMethods = "newVariableVisibilityTest",
        description = "Variable visibility test in control flows")
    public void controlFlowVariableVisibilityTest() throws BallerinaTestException {

        // local variable visibility test inside `if` statement.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope.LOCAL);
        Assert.assertEquals(localVariables.size(), 42);

        // local variable visibility test inside `else` statement.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope.LOCAL);
        Assert.assertEquals(localVariables.size(), 42);

        // local variable visibility test inside `else-if` statement.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope.LOCAL);
        Assert.assertEquals(localVariables.size(), 42);

        // local variable visibility test inside `while` loop.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope.LOCAL);
        Assert.assertEquals(localVariables.size(), 42);

        // local variable visibility test inside `foreach` loop.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        // TODO - Enable test
        // Assert.assertEquals(localVariables.size(), 37);

        // local variable visibility test inside `match` statement.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope.LOCAL);
        Assert.assertEquals(localVariables.size(), 43);

        // Todo - enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/27738
        // local variable visibility test inside `foreach` statement + lambda function.
        // debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        // debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        // localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), DebugTestRunner.VariableScope
        // .LOCAL);
        // Assert.assertEquals(localVariables.size(), 38);
    }

    @Test(dependsOnMethods = "controlFlowVariableVisibilityTest",
        description = "Variable visibility test for global variables")
    public void globalVariableVisibilityTest() throws BallerinaTestException {

        // global variable visibility test outside main() method.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        globalVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), VariableScope.GLOBAL);
        Assert.assertEquals(globalVariables.size(), 14);

        // global variable visibility at the last line of the main() method.
        debugTestRunner.resumeProgram(debugHitInfo.getRight(), DebugTestRunner.DebugResumeKind.NEXT_BREAKPOINT);
        debugHitInfo = debugTestRunner.waitForDebugHit(10000);
        globalVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), VariableScope.GLOBAL);
        localVariables = debugTestRunner.fetchVariables(debugHitInfo.getRight(), VariableScope.LOCAL);
        Assert.assertEquals(globalVariables.size(), 14);

        // global constants
        debugTestRunner.assertVariable(globalVariables, "nameWithoutType", "Ballerina", "string");
        debugTestRunner.assertVariable(globalVariables, "nameWithType", "Ballerina", "string");
        debugTestRunner.assertVariable(globalVariables, "nameMap", "map<string> (size = 1)", "map");
        debugTestRunner.assertVariable(globalVariables, "nilWithoutType", "()", "nil");
        debugTestRunner.assertVariable(globalVariables, "nilWithType", "()", "nil");
        debugTestRunner.assertVariable(globalVariables, "RED", "RED", "string");
        debugTestRunner.assertVariable(globalVariables, "BLUE", "Blue", "string");

        // global variables
        debugTestRunner.assertVariable(globalVariables, "stringValue", "Ballerina", "string");
        debugTestRunner.assertVariable(globalVariables, "decimalValue", "100.0", "decimal");
        debugTestRunner.assertVariable(globalVariables, "byteValue", "2", "int");
        debugTestRunner.assertVariable(globalVariables, "floatValue", "2.0", "float");
        debugTestRunner.assertVariable(globalVariables, "jsonVar", "map<json> (size = 2)", "json");
        debugTestRunner.assertVariable(globalVariables, " /:@[`{~π_IL", "IL with global var", "string");
        debugTestRunner.assertVariable(globalVariables, "port", "9090", "int");
    }

    @Test(dependsOnMethods = "globalVariableVisibilityTest",
        description = "Variable visibility test for local variables at the last line of main() method")
    public void localVariableVisibilityTest() {
        // var variable visibility test
        debugTestRunner.assertVariable(localVariables, "varVariable", "()", "nil");

        // boolean variable visibility test
        debugTestRunner.assertVariable(localVariables, "booleanVar", "true", "boolean");

        // int variable visibility test
        debugTestRunner.assertVariable(localVariables, "intVar", "5", "int");

        // float variable visibility test
        debugTestRunner.assertVariable(localVariables, "floatVar", "-10.0", "float");

        // decimal variable visibility test
        debugTestRunner.assertVariable(localVariables, "decimalVar", "3.5", "decimal");

        // string variable visibility test
        debugTestRunner.assertVariable(localVariables, "stringVar", "foo", "string");

        // xml variable visibility test
        debugTestRunner.assertVariable(localVariables, "xmlVar",
                "<person gender=\"male\"><firstname>Praveen</firstname><lastname>Nada</lastname></person>",
                "xml");

        // array variable visibility test
        debugTestRunner.assertVariable(localVariables, "arrayVar", "any[4]", "array");

        // tuple variable visibility test
        debugTestRunner.assertVariable(localVariables, "tupleVar", "tuple[int,string] (size = 2)", "tuple");

        // map variable visibility test
        debugTestRunner.assertVariable(localVariables, "mapVar", "map<string> (size = 4)", "map");

        // record variable visibility test (Student record)
        debugTestRunner.assertVariable(localVariables, "recordVar", " /:@[`{~π_123_ƮέŞŢ_Student", "record");

        // anonymous record variable visibility test
        debugTestRunner.assertVariable(localVariables, "anonRecord",
                                       "record {| string city; string country; |}", "record");

        // error variable visibility test
        debugTestRunner.assertVariable(localVariables, "errorVar", "SimpleErrorType", "error");

        // anonymous function variable visibility test
        debugTestRunner.assertVariable(localVariables, "anonFunctionVar",
                "isolated function (string,string) returns (string)", "function");

        // future variable visibility test
        debugTestRunner.assertVariable(localVariables, "futureVar", "future<int>", "future");

        // object variable visibility test (Person object)
        debugTestRunner.assertVariable(localVariables, "objectVar", "Person_\\ /<>:@[`{~π_ƮέŞŢ", "object");

        debugTestRunner.assertVariable(localVariables, "anonObjectVar", "Person_\\ /<>:@[`{~π_ƮέŞŢ", "object");

        // type descriptor variable visibility test
        debugTestRunner.assertVariable(localVariables, "typedescVar", "int", "typedesc");

        // union variable visibility test
        debugTestRunner.assertVariable(localVariables, "unionVar", "foo", "string");

        // optional variable visibility test
        debugTestRunner.assertVariable(localVariables, "optionalVar", "foo", "string");

        // any variable visibility test
        debugTestRunner.assertVariable(localVariables, "anyVar", "15.0", "float");

        // anydata variable visibility test
        debugTestRunner.assertVariable(localVariables, "anydataVar", "619", "int");

        // byte variable visibility test
        debugTestRunner.assertVariable(localVariables, "byteVar", "128", "int");

        // json variable visibility test
        debugTestRunner.assertVariable(localVariables, "jsonVar", "map<json> (size = 3)", "json");

        // table variable visibility test
        debugTestRunner.assertVariable(localVariables, "tableVar", "table<Employee> (entries = 3)", "table");

        // stream variable visibility test
        debugTestRunner.assertVariable(localVariables, "oddNumberStream", "stream<int, error>", "stream");

        // variables with quoted identifiers visibility test
        debugTestRunner.assertVariable(localVariables, " /:@[`{~π_var", "IL with special characters in var", "string");
        debugTestRunner.assertVariable(localVariables, "üňĩćőđę_var", "IL with unicode characters in var", "string");
        debugTestRunner.assertVariable(localVariables, "ĠĿŐΒȂɭ_ /:@[`{~π_json", "map<json> (size = 0)", "json");
    }

    @Test(dependsOnMethods = "globalVariableVisibilityTest",
        description = "Child variable visibility test for local variables at the last line of main() method")
    public void localVariableChildrenVisibilityTest() throws BallerinaTestException {
        // xml child variable visibility test
        Map<String, Variable> xmlChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("xmlVar"));
        debugTestRunner.assertVariable(xmlChildVariables, "attributes", "map", "map");
        debugTestRunner.assertVariable(xmlChildVariables, "children", "XMLSequence (size = 2)", "xml");

        // xml attributes child variable visibility test
        Map<String, Variable> xmlAttributesChildVariables =
            debugTestRunner.fetchChildVariables(xmlChildVariables.get("attributes"));
        debugTestRunner.assertVariable(xmlAttributesChildVariables, "gender", "male", "string");

        // xml children variable visibility test
        Map<String, Variable> xmlChildrenVariables =
            debugTestRunner.fetchChildVariables(xmlChildVariables.get("children"));
        debugTestRunner.assertVariable(xmlChildrenVariables, "[0]", "<firstname>Praveen</firstname>", "xml");
        debugTestRunner.assertVariable(xmlChildrenVariables, "[1]", "<lastname>Nada</lastname>", "xml");

        // xml grand children variable visibility test
        Map<String, Variable> xmlGrandChildrenVariables =
            debugTestRunner.fetchChildVariables(xmlChildrenVariables.get("[0]"));
        debugTestRunner.assertVariable(xmlGrandChildrenVariables, "children", "XMLSequence (size = 1)", "xml");

        // array child variable visibility test
        Map<String, Variable> arrayChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("arrayVar"));
        debugTestRunner.assertVariable(arrayChildVariables, "[0]", "1", "int");
        debugTestRunner.assertVariable(arrayChildVariables, "[1]", "20", "int");
        debugTestRunner.assertVariable(arrayChildVariables, "[2]", "-10.0", "float");
        debugTestRunner.assertVariable(arrayChildVariables, "[3]", "foo", "string");

        Map<String, Variable> byteChildVars = debugTestRunner.fetchChildVariables(localVariables.get("byteArrayVar"));
        debugTestRunner.assertVariable(byteChildVars, "[0]", "105", "byte");
        debugTestRunner.assertVariable(byteChildVars, "[1]", "166", "byte");

        // tuple child variable visibility test
        Map<String, Variable> tupleChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("tupleVar"));
        debugTestRunner.assertVariable(tupleChildVariables, "[0]", "20", "int");
        debugTestRunner.assertVariable(tupleChildVariables, "[1]", "foo", "string");

        // map child variable visibility test
        Map<String, Variable> mapChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("mapVar"));
        debugTestRunner.assertVariable(mapChildVariables, "city", "Colombo 03", "string");
        debugTestRunner.assertVariable(mapChildVariables, "country", "Sri Lanka", "string");
        debugTestRunner.assertVariable(mapChildVariables, "line1", "No. 20", "string");
        debugTestRunner.assertVariable(mapChildVariables, "line2", "Palm Grove", "string");

        // record child variable visibility test (Student record)
        Map<String, Variable> studentRecordChildVariables =
            debugTestRunner.fetchChildVariables(localVariables.get("recordVar"));
        debugTestRunner.assertVariable(studentRecordChildVariables, "1st_name", "John Doe", "string");
        debugTestRunner.assertVariable(studentRecordChildVariables, "grades", "Grades", "record");
        debugTestRunner.assertVariable(studentRecordChildVariables, "Ȧɢέ_ /:@[`{~π", "20", "int");

        // record child variable visibility test (Grades record)
        Map<String, Variable> gradesChildVariables =
            debugTestRunner.fetchChildVariables(studentRecordChildVariables.get("grades"));
        debugTestRunner.assertVariable(gradesChildVariables, "chemistry", "65", "int");
        debugTestRunner.assertVariable(gradesChildVariables, "maths", "80", "int");
        debugTestRunner.assertVariable(gradesChildVariables, "physics", "75", "int");

        // anonymous record child variable visibility test
        Map<String, Variable> recordChildVariables =
            debugTestRunner.fetchChildVariables(localVariables.get("anonRecord"));
        debugTestRunner.assertVariable(recordChildVariables, "city", "London", "string");
        debugTestRunner.assertVariable(recordChildVariables, "country", "UK", "string");

        // error child variable visibility test
        Map<String, Variable> errorChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("errorVar"));
        debugTestRunner.assertVariable(errorChildVariables, "details", "map<(ballerina/lang.value:1.0.0:Cloneable " +
                "(size = 1)", "map");
        debugTestRunner.assertVariable(errorChildVariables, "message", "SimpleErrorType", "string");

        // error details child variable visibility test
        Map<String, Variable> errorDetailsChildVariables =
            debugTestRunner.fetchChildVariables(errorChildVariables.get("details"));
        debugTestRunner.assertVariable(errorDetailsChildVariables, "message", "Simple error occurred", "string");

        // future child variable visibility test
        Map<String, Variable> futureChildVariables =
            debugTestRunner.fetchChildVariables(localVariables.get("futureVar"));
        debugTestRunner.assertVariable(futureChildVariables, "isDone", "true", "boolean");
        debugTestRunner.assertVariable(futureChildVariables, "result", "90", "int");

        // object child variable visibility test (Person object)
        Map<String, Variable> personObjectChildVariables =
            debugTestRunner.fetchChildVariables(localVariables.get("objectVar"));
        debugTestRunner.assertVariable(personObjectChildVariables, "1st_name", "John", "string");
        debugTestRunner.assertVariable(personObjectChildVariables, "address", "No 20, Palm grove", "string");
        debugTestRunner.assertVariable(personObjectChildVariables, "parent", "()", "nil");
        debugTestRunner.assertVariable(personObjectChildVariables, "email", "default@abc.com", "string");
        debugTestRunner.assertVariable(personObjectChildVariables, "Ȧɢέ_ /:@[`{~π", "0", "int");

        // anonymous object child variable visibility test (AnonPerson object)
        Map<String, Variable> anonObjectChildVariables =
            debugTestRunner.fetchChildVariables(localVariables.get("anonObjectVar"));
        debugTestRunner.assertVariable(anonObjectChildVariables, "1st_name", "John", "string");
        debugTestRunner.assertVariable(anonObjectChildVariables, "address", "No 20, Palm grove", "string");
        debugTestRunner.assertVariable(anonObjectChildVariables, "parent", "()", "nil");
        debugTestRunner.assertVariable(anonObjectChildVariables, "email", "default@abc.com", "string");
        debugTestRunner.assertVariable(anonObjectChildVariables, "Ȧɢέ_ /:@[`{~π", "0", "int");

        // TODO: Anonymous object's grand child variables are not visible. Need to fix it.
        // Variable[] anonPersonAddressChildVariables = getChildVariable(anonObjectChildVariables[0]);
        // Arrays.sort(anonPersonAddressChildVariables, compareByName);
        // debugTestRunner.assertVariable(anonPersonAddressChildVariables[0], "city", "Colombo", "string");
        // debugTestRunner.assertVariable(anonPersonAddressChildVariables[1], "country", "Sri Lanka", "string");

        // json child variable visibility test
        Map<String, Variable> jsonChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("jsonVar"));
        debugTestRunner.assertVariable(jsonChildVariables, "color", "red", "string");
        debugTestRunner.assertVariable(jsonChildVariables, "name", "apple", "string");
        debugTestRunner.assertVariable(jsonChildVariables, "price", "40", "int");

        // table child variable visibility test
        Map<String, Variable> tableChildVariables = debugTestRunner.fetchChildVariables(localVariables.get("tableVar"));
        debugTestRunner.assertVariable(tableChildVariables, "[0]", "Employee", "record");
        debugTestRunner.assertVariable(tableChildVariables, "[1]", "Employee", "record");
        debugTestRunner.assertVariable(tableChildVariables, "[2]", "Employee", "record");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        debugTestRunner.terminateDebugSession();
        globalVariables.clear();
        localVariables.clear();
    }
}
