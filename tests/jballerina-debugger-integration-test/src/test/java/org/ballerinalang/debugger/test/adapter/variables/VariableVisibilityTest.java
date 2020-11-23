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
import org.ballerinalang.debugger.test.DebugAdapterBaseTestCase;
import org.ballerinalang.debugger.test.utils.BallerinaTestDebugPoint;
import org.ballerinalang.debugger.test.utils.DebugUtils;
import org.ballerinalang.debugger.test.utils.TestUtils;
import org.ballerinalang.test.context.BallerinaTestException;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.Variable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.debugger.test.utils.TestUtils.testEntryFilePath;
import static org.ballerinalang.debugger.test.utils.TestUtils.testProjectBaseDir;
import static org.ballerinalang.debugger.test.utils.TestUtils.testProjectPath;

/**
 * Test class for variable visibility.
 */
public class VariableVisibilityTest extends DebugAdapterBaseTestCase {

    Map<String, Variable> globalVariables = new HashMap<>();
    Map<String, Variable> localVariables = new HashMap<>();

    @BeforeClass
    public void setup() throws BallerinaTestException {
        String testProjectName = "variable-tests";
        String testModuleFileName = "main.bal";
        testProjectPath = testProjectBaseDir.toString() + File.separator + testProjectName;
        testEntryFilePath = Paths.get(testProjectPath, testModuleFileName).toString();

        TestUtils.addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 182));
        TestUtils.initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = TestUtils.waitForDebugHit(25000);

        globalVariables = TestUtils.fetchVariables(debugHitInfo.getRight(), TestUtils.VariableScope.GLOBAL);
        localVariables = TestUtils.fetchVariables(debugHitInfo.getRight(), TestUtils.VariableScope.LOCAL);
    }

    @Test
    public void globalVariableVisibilityTest() {
        // Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/26139
        // Asserts the number of globally visible variables (should be 11).
        // Assert.assertEquals(globalVariables.size(), 11);
        // global constants
        // assertVariable(globalVariables, "nameWithoutType", "Ballerina", "string");
        // assertVariable(globalVariables, "nameWithType", "Ballerina", "string");

        TestUtils.assertVariable(globalVariables, "nameMap", "map", "map");
        TestUtils.assertVariable(globalVariables, "nilWithoutType", "()", "nil");
        TestUtils.assertVariable(globalVariables, "nilWithType", "()", "nil");
        // global variables
        TestUtils.assertVariable(globalVariables, "stringValue", "Ballerina", "string");
        TestUtils.assertVariable(globalVariables, "decimalValue", "100.0", "decimal");
        TestUtils.assertVariable(globalVariables, "byteValue", "2", "int");
        TestUtils.assertVariable(globalVariables, "floatValue", "2.0", "float");
        TestUtils.assertVariable(globalVariables, "jsonVar", "map<json>", "json");
        TestUtils.assertVariable(globalVariables, " /:@[`{~⌤_IL", "IL with global var", "string");
    }

    @Test
    public void localVariableVisibilityTest() {
        // var variable visibility test
        TestUtils.assertVariable(localVariables, "varVariable", "()", "nil");

        // boolean variable visibility test
        TestUtils.assertVariable(localVariables, "booleanVar", "true", "boolean");

        // int variable visibility test
        TestUtils.assertVariable(localVariables, "intVar", "20", "int");

        // float variable visibility test
        TestUtils.assertVariable(localVariables, "floatVar", "-10.0", "float");

        // decimal variable visibility test
        TestUtils.assertVariable(localVariables, "decimalVar", "3.5", "decimal");

        // string variable visibility test
        TestUtils.assertVariable(localVariables, "stringVar", "foo", "string");

        // xml variable visibility test
        TestUtils.assertVariable(localVariables, "xmlVar",
                "<person gender=\"male\"><firstname>Praveen</firstname><lastname>Nada</lastname></person>",
                "xml");

        // array variable visibility test
        TestUtils.assertVariable(localVariables, "arrayVar", "any[4]", "array");

        // tuple variable visibility test
        TestUtils.assertVariable(localVariables, "tupleVar", "tuple[int,string]", "tuple");

        // map variable visibility test
        TestUtils.assertVariable(localVariables, "mapVar", "map", "map");

        // record variable visibility test (Student record)
        TestUtils.assertVariable(localVariables, "recordVar", " /:@[`{~⌤_123_ƮέŞŢ_Student", "record");

        // anonymous record variable visibility test
        TestUtils.assertVariable(localVariables, "anonRecord", "anonymous", "record");

        // error variable visibility test
        TestUtils.assertVariable(localVariables, "errorVar", "SimpleErrorType", "error");

        // anonymous function variable visibility test
        TestUtils.assertVariable(localVariables, "anonFunctionVar",
                "function (string,string) returns (string)", "function");

        // future variable visibility test
        TestUtils.assertVariable(localVariables, "futureVar", "future", "future");

        // object variable visibility test (Person object)
        TestUtils.assertVariable(localVariables, "objectVar", "Person_\\ /<>:@[`{~⌤_ƮέŞŢ", "object");

        TestUtils.assertVariable(localVariables, "anonObjectVar", "Person_\\ /<>:@[`{~⌤_ƮέŞŢ", "object");

        // type descriptor variable visibility test
        TestUtils.assertVariable(localVariables, "typedescVar", "int", "typedesc");

        // union variable visibility test
        TestUtils.assertVariable(localVariables, "unionVar", "foo", "string");

        // optional variable visibility test
        TestUtils.assertVariable(localVariables, "optionalVar", "foo", "string");

        // any variable visibility test
        TestUtils.assertVariable(localVariables, "anyVar", "15.0", "float");

        // anydata variable visibility test
        TestUtils.assertVariable(localVariables, "anydataVar", "619", "int");

        // byte variable visibility test
        TestUtils.assertVariable(localVariables, "byteVar", "128", "int");

        // json variable visibility test
        TestUtils.assertVariable(localVariables, "jsonVar", "map<json>", "json");

        // table variable visibility test
        TestUtils.assertVariable(localVariables, "tableVar", "table<Employee>", "table");

        // stream variable visibility test
        TestUtils.assertVariable(localVariables, "oddNumberStream", "stream<int>", "stream");

        // never variable visibility test
        TestUtils.assertVariable(localVariables, "neverVar", "", "xml");

        // variables with quoted identifiers visibility test
        TestUtils.assertVariable(localVariables, " /:@[`{~⌤_var", "IL with special characters in var", "string");
        TestUtils.assertVariable(localVariables, "üňĩćőđę_var", "IL with unicode characters in var", "string");
        TestUtils.assertVariable(localVariables, "ĠĿŐΒȂɭ_ /:@[`{~⌤_json", "map<json>", "json");
    }

    @Test
    public void localVariableChildrenVisibilityTest() throws BallerinaTestException {
        // xml child variable visibility test
        Map<String, Variable> xmlChildVariables = TestUtils.fetchChildVariables(localVariables.get("xmlVar"));
        TestUtils.assertVariable(xmlChildVariables, "attributes", "map", "map");
        TestUtils.assertVariable(xmlChildVariables, "children",
                "<firstname>Praveen</firstname><lastname>Nada</lastname>", "xml");

        // xml attributes child variable visibility test
        Map<String, Variable> xmlAttributesChildVariables =
                TestUtils.fetchChildVariables(xmlChildVariables.get("attributes"));
        TestUtils.assertVariable(xmlAttributesChildVariables, "gender", "male", "string");

        // xml children variable visibility test
        Map<String, Variable> xmlChildrenVariables = TestUtils.fetchChildVariables(xmlChildVariables.get("children"));
        TestUtils.assertVariable(xmlChildrenVariables, "0", "<firstname>Praveen</firstname>", "xml");
        TestUtils.assertVariable(xmlChildrenVariables, "1", "<lastname>Nada</lastname>", "xml");

        // xml grand children variable visibility test
        Map<String, Variable> xmlGrandChildrenVariables = TestUtils.fetchChildVariables(xmlChildrenVariables.get("0"));
        TestUtils.assertVariable(xmlGrandChildrenVariables, "children", "Praveen", "xml");

        // array child variable visibility test
        Map<String, Variable> arrayChildVariables = TestUtils.fetchChildVariables(localVariables.get("arrayVar"));
        TestUtils.assertVariable(arrayChildVariables, "[0]", "1", "int");
        TestUtils.assertVariable(arrayChildVariables, "[1]", "20", "int");
        TestUtils.assertVariable(arrayChildVariables, "[2]", "-10.0", "float");
        TestUtils.assertVariable(arrayChildVariables, "[3]", "foo", "string");

        // tuple child variable visibility test
        Map<String, Variable> tupleChildVariables = TestUtils.fetchChildVariables(localVariables.get("tupleVar"));
        TestUtils.assertVariable(tupleChildVariables, "[0]", "20", "int");
        TestUtils.assertVariable(tupleChildVariables, "[1]", "foo", "string");

        // map child variable visibility test
        Map<String, Variable> mapChildVariables = TestUtils.fetchChildVariables(localVariables.get("mapVar"));
        TestUtils.assertVariable(mapChildVariables, "city", "Colombo 03", "string");
        TestUtils.assertVariable(mapChildVariables, "country", "Sri Lanka", "string");
        TestUtils.assertVariable(mapChildVariables, "line1", "No. 20", "string");
        TestUtils.assertVariable(mapChildVariables, "line2", "Palm Grove", "string");

        // record child variable visibility test (Student record)
        Map<String, Variable> studentRecordChildVariables =
                TestUtils.fetchChildVariables(localVariables.get("recordVar"));
        TestUtils.assertVariable(studentRecordChildVariables, "1st_name", "John Doe", "string");
        TestUtils.assertVariable(studentRecordChildVariables, "grades", "Grades", "record");
        TestUtils.assertVariable(studentRecordChildVariables, "Ȧɢέ_ /:@[`{~⌤", "20", "int");

        // record child variable visibility test (Grades record)
        Map<String, Variable> gradesChildVariables =
                TestUtils.fetchChildVariables(studentRecordChildVariables.get("grades"));
        TestUtils.assertVariable(gradesChildVariables, "chemistry", "65", "int");
        TestUtils.assertVariable(gradesChildVariables, "maths", "80", "int");
        TestUtils.assertVariable(gradesChildVariables, "physics", "75", "int");

        // anonymous record child variable visibility test
        Map<String, Variable> recordChildVariables = TestUtils.fetchChildVariables(localVariables.get("anonRecord"));
        TestUtils.assertVariable(recordChildVariables, "city", "London", "string");
        TestUtils.assertVariable(recordChildVariables, "country", "UK", "string");

        // error child variable visibility test
        Map<String, Variable> errorChildVariables = TestUtils.fetchChildVariables(localVariables.get("errorVar"));
        TestUtils.assertVariable(errorChildVariables, "details", "map", "map");
        TestUtils.assertVariable(errorChildVariables, "message", "SimpleErrorType", "string");

        // error details child variable visibility test
        Map<String, Variable> errorDetailsChildVariables =
                TestUtils.fetchChildVariables(errorChildVariables.get("details"));
        TestUtils.assertVariable(errorDetailsChildVariables, "message", "Simple error occurred", "string");

        // future child variable visibility test
        Map<String, Variable> futureChildVariables = TestUtils.fetchChildVariables(localVariables.get("futureVar"));
        TestUtils.assertVariable(futureChildVariables, "isDone", "true", "boolean");
        TestUtils.assertVariable(futureChildVariables, "result", "90", "int");

        // object child variable visibility test (Person object)
        Map<String, Variable> personObjectChildVariables =
                TestUtils.fetchChildVariables(localVariables.get("objectVar"));
        TestUtils.assertVariable(personObjectChildVariables, "1st_name", "John", "string");
        TestUtils.assertVariable(personObjectChildVariables, "address", "No 20, Palm grove", "string");
        TestUtils.assertVariable(personObjectChildVariables, "parent", "()", "nil");
        TestUtils.assertVariable(personObjectChildVariables, "email", "default@abc.com", "string");
        TestUtils.assertVariable(personObjectChildVariables, "Ȧɢέ_ /:@[`{~⌤", "0", "int");

        // anonymous object child variable visibility test (AnonPerson object)
        Map<String, Variable> anonObjectChildVariables =
                TestUtils.fetchChildVariables(localVariables.get("anonObjectVar"));
        TestUtils.assertVariable(anonObjectChildVariables, "1st_name", "John", "string");
        TestUtils.assertVariable(anonObjectChildVariables, "address", "No 20, Palm grove", "string");
        TestUtils.assertVariable(anonObjectChildVariables, "parent", "()", "nil");
        TestUtils.assertVariable(anonObjectChildVariables, "email", "default@abc.com", "string");
        TestUtils.assertVariable(anonObjectChildVariables, "Ȧɢέ_ /:@[`{~⌤", "0", "int");

        // TODO: Anonymous object's grand child variables are not visible. Need to fix it.
        // Variable[] anonPersonAddressChildVariables = getChildVariable(anonObjectChildVariables[0]);
        // Arrays.sort(anonPersonAddressChildVariables, compareByName);
        // assertVariable(anonPersonAddressChildVariables[0], "city", "Colombo", "string");
        // assertVariable(anonPersonAddressChildVariables[1], "country", "Sri Lanka", "string");

        // json child variable visibility test
        Map<String, Variable> jsonChildVariables = TestUtils.fetchChildVariables(localVariables.get("jsonVar"));
        TestUtils.assertVariable(jsonChildVariables, "color", "red", "string");
        TestUtils.assertVariable(jsonChildVariables, "name", "apple", "string");
        TestUtils.assertVariable(jsonChildVariables, "price", "40", "int");
    }

    @AfterClass(alwaysRun = true)
    private void cleanup() {
        TestUtils.terminateDebugSession();
        globalVariables.clear();
        localVariables.clear();
    }
}
