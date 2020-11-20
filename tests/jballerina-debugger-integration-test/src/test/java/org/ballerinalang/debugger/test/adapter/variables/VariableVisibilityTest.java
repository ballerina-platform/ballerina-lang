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

/**
 * Test class for variable visibility.
 */
public class VariableVisibilityTest extends DebugAdapterBaseTestCase {

    Map<String, Variable> globalVariables = new HashMap<>();
    Map<String, Variable> localVariables = new HashMap<>();

    @BeforeClass
    public void setup() throws BallerinaTestException {
        testProjectName = "variable-tests";
        testModuleFileName = "main.bal";
        testProjectPath = testProjectBaseDir.toString() + File.separator + testProjectName;
        testEntryFilePath = Paths.get(testProjectPath, testModuleFileName).toString();

        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 182));
        initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);
        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(25000);

        globalVariables = fetchVariables(debugHitInfo.getRight(), VariableScope.GLOBAL);
        localVariables = fetchVariables(debugHitInfo.getRight(), VariableScope.LOCAL);
    }

    @Test
    public void globalVariableVisibilityTest() {
        // Todo - Enable after fixing https://github.com/ballerina-platform/ballerina-lang/issues/26139
        // Asserts the number of globally visible variables (should be 11).
        // Assert.assertEquals(globalVariables.size(), 11);
        // global constants
        // assertVariable(globalVariables, "nameWithoutType", "Ballerina", "string");
        // assertVariable(globalVariables, "nameWithType", "Ballerina", "string");

        assertVariable(globalVariables, "nameMap", "map", "map");
        assertVariable(globalVariables, "nilWithoutType", "()", "nil");
        assertVariable(globalVariables, "nilWithType", "()", "nil");
        // global variables
        assertVariable(globalVariables, "stringValue", "Ballerina", "string");
        assertVariable(globalVariables, "decimalValue", "100.0", "decimal");
        assertVariable(globalVariables, "byteValue", "2", "int");
        assertVariable(globalVariables, "floatValue", "2.0", "float");
        assertVariable(globalVariables, "jsonVar", "map<json>", "json");
        assertVariable(globalVariables, " /:@[`{~⌤_IL", "IL with global var", "string");
    }

    @Test
    public void localVariableVisibilityTest() {
        // var variable visibility test
        assertVariable(localVariables, "varVariable", "()", "nil");

        // boolean variable visibility test
        assertVariable(localVariables, "booleanVar", "true", "boolean");

        // int variable visibility test
        assertVariable(localVariables, "intVar", "20", "int");

        // float variable visibility test
        assertVariable(localVariables, "floatVar", "-10.0", "float");

        // decimal variable visibility test
        assertVariable(localVariables, "decimalVar", "3.5", "decimal");

        // string variable visibility test
        assertVariable(localVariables, "stringVar", "foo", "string");

        // xml variable visibility test
        assertVariable(localVariables, "xmlVar",
                "<person gender=\"male\"><firstname>Praveen</firstname><lastname>Nada</lastname></person>",
                "xml");

        // array variable visibility test
        assertVariable(localVariables, "arrayVar", "any[4]", "array");

        // tuple variable visibility test
        assertVariable(localVariables, "tupleVar", "tuple[int,string]", "tuple");

        // map variable visibility test
        assertVariable(localVariables, "mapVar", "map", "map");

        // record variable visibility test (Student record)
        assertVariable(localVariables, "recordVar", " /:@[`{~⌤_123_ƮέŞŢ_Student", "record");

        // anonymous record variable visibility test
        assertVariable(localVariables, "anonRecord", "anonymous", "record");

        // error variable visibility test
        assertVariable(localVariables, "errorVar", "SimpleErrorType", "error");

        // anonymous function variable visibility test
        assertVariable(localVariables, "anonFunctionVar",
                "function (string,string) returns (string)", "function");

        // future variable visibility test
        assertVariable(localVariables, "futureVar", "future", "future");

        // object variable visibility test (Person object)
        assertVariable(localVariables, "objectVar", "Person_\\ /<>:@[`{~⌤_ƮέŞŢ", "object");

        assertVariable(localVariables, "anonObjectVar", "Person_\\ /<>:@[`{~⌤_ƮέŞŢ", "object");

        // type descriptor variable visibility test
        assertVariable(localVariables, "typedescVar", "int", "typedesc");

        // union variable visibility test
        assertVariable(localVariables, "unionVar", "foo", "string");

        // optional variable visibility test
        assertVariable(localVariables, "optionalVar", "foo", "string");

        // any variable visibility test
        assertVariable(localVariables, "anyVar", "15.0", "float");

        // anydata variable visibility test
        assertVariable(localVariables, "anydataVar", "619", "int");

        // byte variable visibility test
        assertVariable(localVariables, "byteVar", "128", "int");

        // json variable visibility test
        assertVariable(localVariables, "jsonVar", "map<json>", "json");

        // table variable visibility test
        assertVariable(localVariables, "tableVar", "table<Employee>", "table");

        // stream variable visibility test
        assertVariable(localVariables, "oddNumberStream", "stream<int>", "stream");

        // never variable visibility test
        assertVariable(localVariables, "neverVar", "", "xml");

        // variables with quoted identifiers visibility test
        assertVariable(localVariables, " /:@[`{~⌤_var", "IL with special characters in var", "string");
        assertVariable(localVariables, "üňĩćőđę_var", "IL with unicode characters in var", "string");
        assertVariable(localVariables, "ĠĿŐΒȂɭ_ /:@[`{~⌤_json", "map<json>", "json");
    }

    @Test
    public void localVariableChildrenVisibilityTest() throws BallerinaTestException {
        // xml child variable visibility test
        Map<String, Variable> xmlChildVariables = fetchChildVariables(localVariables.get("xmlVar"));
        assertVariable(xmlChildVariables, "attributes", "map", "map");
        assertVariable(xmlChildVariables, "children", "<firstname>Praveen</firstname><lastname>Nada</lastname>", "xml");

        // xml attributes child variable visibility test
        Map<String, Variable> xmlAttributesChildVariables = fetchChildVariables(xmlChildVariables.get("attributes"));
        assertVariable(xmlAttributesChildVariables, "gender", "male", "string");

        // xml children variable visibility test
        Map<String, Variable> xmlChildrenVariables = fetchChildVariables(xmlChildVariables.get("children"));
        assertVariable(xmlChildrenVariables, "0", "<firstname>Praveen</firstname>", "xml");
        assertVariable(xmlChildrenVariables, "1", "<lastname>Nada</lastname>", "xml");

        // xml grand children variable visibility test
        Map<String, Variable> xmlGrandChildrenVariables = fetchChildVariables(xmlChildrenVariables.get("0"));
        assertVariable(xmlGrandChildrenVariables, "children", "Praveen", "xml");

        // array child variable visibility test
        Map<String, Variable> arrayChildVariables = fetchChildVariables(localVariables.get("arrayVar"));
        assertVariable(arrayChildVariables, "[0]", "1", "int");
        assertVariable(arrayChildVariables, "[1]", "20", "int");
        assertVariable(arrayChildVariables, "[2]", "-10.0", "float");
        assertVariable(arrayChildVariables, "[3]", "foo", "string");

        // tuple child variable visibility test
        Map<String, Variable> tupleChildVariables = fetchChildVariables(localVariables.get("tupleVar"));
        assertVariable(tupleChildVariables, "[0]", "20", "int");
        assertVariable(tupleChildVariables, "[1]", "foo", "string");

        // map child variable visibility test
        Map<String, Variable> mapChildVariables = fetchChildVariables(localVariables.get("mapVar"));
        assertVariable(mapChildVariables, "city", "Colombo 03", "string");
        assertVariable(mapChildVariables, "country", "Sri Lanka", "string");
        assertVariable(mapChildVariables, "line1", "No. 20", "string");
        assertVariable(mapChildVariables, "line2", "Palm Grove", "string");

        // record child variable visibility test (Student record)
        Map<String, Variable> studentRecordChildVariables = fetchChildVariables(localVariables.get("recordVar"));
        assertVariable(studentRecordChildVariables, "1st_name", "John Doe", "string");
        assertVariable(studentRecordChildVariables, "grades", "Grades", "record");
        assertVariable(studentRecordChildVariables, "Ȧɢέ_ /:@[`{~⌤", "20", "int");

        // record child variable visibility test (Grades record)
        Map<String, Variable> gradesChildVariables = fetchChildVariables(studentRecordChildVariables.get("grades"));
        assertVariable(gradesChildVariables, "chemistry", "65", "int");
        assertVariable(gradesChildVariables, "maths", "80", "int");
        assertVariable(gradesChildVariables, "physics", "75", "int");

        // anonymous record child variable visibility test
        Map<String, Variable> recordChildVariables = fetchChildVariables(localVariables.get("anonRecord"));
        assertVariable(recordChildVariables, "city", "London", "string");
        assertVariable(recordChildVariables, "country", "UK", "string");

        // error child variable visibility test
        Map<String, Variable> errorChildVariables = fetchChildVariables(localVariables.get("errorVar"));
        assertVariable(errorChildVariables, "details", "map", "map");
        assertVariable(errorChildVariables, "message", "SimpleErrorType", "string");

        // error details child variable visibility test
        Map<String, Variable> errorDetailsChildVariables = fetchChildVariables(errorChildVariables.get("details"));
        assertVariable(errorDetailsChildVariables, "message", "Simple error occurred", "string");

        // future child variable visibility test
        Map<String, Variable> futureChildVariables = fetchChildVariables(localVariables.get("futureVar"));
        assertVariable(futureChildVariables, "isDone", "true", "boolean");
        assertVariable(futureChildVariables, "result", "90", "int");

        // object child variable visibility test (Person object)
        Map<String, Variable> personObjectChildVariables = fetchChildVariables(localVariables.get("objectVar"));
        assertVariable(personObjectChildVariables, "1st_name", "John", "string");
        assertVariable(personObjectChildVariables, "address", "No 20, Palm grove", "string");
        assertVariable(personObjectChildVariables, "parent", "()", "nil");
        assertVariable(personObjectChildVariables, "email", "default@abc.com", "string");
        assertVariable(personObjectChildVariables, "Ȧɢέ_ /:@[`{~⌤", "0", "int");

        // anonymous object child variable visibility test (AnonPerson object)
        Map<String, Variable> anonObjectChildVariables = fetchChildVariables(localVariables.get("anonObjectVar"));
        assertVariable(anonObjectChildVariables, "1st_name", "John", "string");
        assertVariable(anonObjectChildVariables, "address", "No 20, Palm grove", "string");
        assertVariable(anonObjectChildVariables, "parent", "()", "nil");
        assertVariable(anonObjectChildVariables, "email", "default@abc.com", "string");
        assertVariable(anonObjectChildVariables, "Ȧɢέ_ /:@[`{~⌤", "0", "int");

        // TODO: Anonymous object's grand child variables are not visible. Need to fix it.
        // Variable[] anonPersonAddressChildVariables = getChildVariable(anonObjectChildVariables[0]);
        // Arrays.sort(anonPersonAddressChildVariables, compareByName);
        // assertVariable(anonPersonAddressChildVariables[0], "city", "Colombo", "string");
        // assertVariable(anonPersonAddressChildVariables[1], "country", "Sri Lanka", "string");

        // json child variable visibility test
        Map<String, Variable> jsonChildVariables = fetchChildVariables(localVariables.get("jsonVar"));
        assertVariable(jsonChildVariables, "color", "red", "string");
        assertVariable(jsonChildVariables, "name", "apple", "string");
        assertVariable(jsonChildVariables, "price", "40", "int");
    }

    @AfterClass(alwaysRun = true)
    private void cleanup() {
        terminateDebugSession();
        globalVariables.clear();
        localVariables.clear();
    }
}
