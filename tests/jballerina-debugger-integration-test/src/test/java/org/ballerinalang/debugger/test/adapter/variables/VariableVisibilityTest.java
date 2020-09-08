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
import java.util.Arrays;
import java.util.Comparator;

/**
 * Test class for variable visibility.
 */
public class VariableVisibilityTest extends DebugAdapterBaseTestCase {
    Variable[] variables;
    Comparator<Variable> compareByName = Comparator.comparing(Variable::getName);

    @BeforeClass
    public void setup() {
        testProjectName = "basic-project";
        testModuleName = "advanced";
        testModuleFileName = "main.bal";
        testProjectPath = testProjectBaseDir.toString() + File.separator + testProjectName;
        testEntryFilePath = Paths.get(testProjectPath, "src", testModuleName, testModuleFileName).toString();
    }

    @Test
    public void parentVariableVisibilityTest() throws BallerinaTestException {
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 154));
        initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(10000);
        variables = fetchDebugHitVariables(debugHitInfo.getRight());
        Arrays.sort(variables, compareByName);

        // var variable visibility test
        assertVariable(variables[0], "v01_varVariable", "()", "nil");

        // boolean variable visibility test
        assertVariable(variables[1], "v02_booleanVar", "true", "boolean");

        // int variable visibility test
        assertVariable(variables[2], "v03_intVar", "20", "int");

        // float variable visibility test
        assertVariable(variables[3], "v04_floatVar", "-10.0", "float");

        // decimal variable visibility test
        assertVariable(variables[4], "v05_decimalVar", "3.5", "decimal");

        // string variable visibility test
        assertVariable(variables[5], "v06_stringVar", "foo", "string");

        // xml variable visibility test
        assertVariable(variables[6], "v07_xmlVar",
                "<person gender=\"male\"><firstname>Praveen</firstname><lastname>Nada</lastname></person>",
                "xml");

        // array variable visibility test
        assertVariable(variables[7], "v08_arrayVar", "any[4]", "array");

        // tuple variable visibility test
        assertVariable(variables[8], "v09_tupleVar", "tuple[int,string]", "tuple");

        // map variable visibility test
        assertVariable(variables[9], "v10_mapVar", "map", "map");

        // record variable visibility test (Student record)
        assertVariable(variables[10], "v11_john", "Student", "record");

        // anonymous record variable visibility test
        assertVariable(variables[11], "v12_anonRecord", "anonymous", "record");

        // error variable visibility test
        assertVariable(variables[12], "v13_errorVar", "SimpleErrorType", "error");

        // anonymous function variable visibility test
        assertVariable(variables[13], "v14_anonFunctionVar",
                "function (string,string) returns (string)", "function");

        // future variable visibility test
        assertVariable(variables[14], "v15_futureVar", "future", "future");

        // object variable visibility test (Person object)
        assertVariable(variables[15], "v16_objectVar", "Person", "object");

        // anonymous object variable visibility test (AnonPerson object)
//        assertVariable(variables[16], "v17_anonObjectVar", "AnonPerson", "object");

        // type descriptor variable visibility test
        assertVariable(variables[17], "v18_typedescVar", "int", "typedesc");

        // union variable visibility test
        assertVariable(variables[18], "v19_unionVar", "foo", "string");

        // optional variable visibility test
        assertVariable(variables[19], "v20_optionalVar", "foo", "string");

        // any variable visibility test
        assertVariable(variables[20], "v21_anyVar", "15.0", "float");

        // anydata variable visibility test
        assertVariable(variables[21], "v22_anydataVar", "619", "int");

        // byte variable visibility test
        assertVariable(variables[22], "v23_byteVar", "128", "int");

        // json variable visibility test
        assertVariable(variables[23], "v24_jsonVar", "object", "json");

        // table variable visibility test
        assertVariable(variables[24], "v25_tableVar", "table<Employee>", "table");

        // stream variable visibility test
        assertVariable(variables[25], "v26_oddNumberStream", "stream<int>",
                "stream");

        // never variable visibility test
        assertVariable(variables[26], "v27_neverVar", "", "xml");
    }

    @Test(enabled = false, dependsOnMethods = "parentVariableVisibilityTest")
    public void childVariableVisibilityTest() throws BallerinaTestException {
        // xml child variable visibility test
        Variable[] xmlChildVariables = getChildVariable(variables[6]);
        Arrays.sort(xmlChildVariables, compareByName);
        assertVariable(xmlChildVariables[0], "attributes", "map", "map");
        assertVariable(xmlChildVariables[1], "children",
                "<firstname>Praveen</firstname><lastname>Nada</lastname>", "xml");

        // xml attributes child variable visibility test
        Variable[] xmlAttributesChildVariables = getChildVariable(xmlChildVariables[0]);
        assertVariable(xmlAttributesChildVariables[0], "gender", "male", "string");

        // xml children variable visibility test
        Variable[] xmlChildrenVariables = getChildVariable(xmlChildVariables[1]);
        assertVariable(xmlChildrenVariables[0], "0", "<firstname>Praveen</firstname>",
                "xml");
        assertVariable(xmlChildrenVariables[1], "1", "<lastname>Nada</lastname>",
                "xml");

        // xml grand children variable visibility test
        Variable[] xmlGrandChildrenVariables = getChildVariable(xmlChildrenVariables[0]);
        Arrays.sort(xmlGrandChildrenVariables, compareByName);
        assertVariable(xmlGrandChildrenVariables[1], "children", "Praveen", "xml");

        // array child variable visibility test
        Variable[] arrayChildVariables = getChildVariable(variables[7]);
        assertVariable(arrayChildVariables[0], "[0]", "1", "int");
        assertVariable(arrayChildVariables[1], "[1]", "20", "int");
        assertVariable(arrayChildVariables[2], "[2]", "-10.0", "float");
        assertVariable(arrayChildVariables[3], "[3]", "foo", "string");

        // tuple child variable visibility test
        Variable[] tupleChildVariables = getChildVariable(variables[8]);
        assertVariable(tupleChildVariables[0], "[0]", "20", "int");
        assertVariable(tupleChildVariables[1], "[1]", "foo", "string");

        // map child variable visibility test
        Variable[] mapChildVariables = getChildVariable(variables[9]);
        Arrays.sort(mapChildVariables, compareByName);
        assertVariable(mapChildVariables[0], "city", "Colombo 03", "string");
        assertVariable(mapChildVariables[1], "country", "Sri Lanka", "string");
        assertVariable(mapChildVariables[2], "line1", "No. 20", "string");
        assertVariable(mapChildVariables[3], "line2", "Palm Grove", "string");

        // record child variable visibility test (Student record)
        Variable[] studentRecordChildVariables = getChildVariable(variables[10]);
        Arrays.sort(studentRecordChildVariables, compareByName);
        assertVariable(studentRecordChildVariables[0], "age", "20", "int");
        assertVariable(studentRecordChildVariables[1], "grades", "Grades",
                "record");
        assertVariable(studentRecordChildVariables[2], "name", "John Doe",
                "string");

        // record child variable visibility test (Grades record)
        Variable[] studentGradesChildVariables = getChildVariable(studentRecordChildVariables[1]);
        Arrays.sort(studentGradesChildVariables, compareByName);
        assertVariable(studentGradesChildVariables[0], "chemistry", "65", "int");
        assertVariable(studentGradesChildVariables[1], "maths", "80", "int");
        assertVariable(studentGradesChildVariables[2], "physics", "75", "int");

        // anonymous record child variable visibility test
        Variable[] recordChildVariables = getChildVariable(variables[11]);
        Arrays.sort(recordChildVariables, compareByName);
        assertVariable(recordChildVariables[0], "city", "London", "string");
        assertVariable(recordChildVariables[1], "country", "UK", "string");

        // error child variable visibility test
        Variable[] errorChildVariables = getChildVariable(variables[12]);
        Arrays.sort(errorChildVariables, compareByName);
        assertVariable(errorChildVariables[0], "details", "map", "map");
        assertVariable(errorChildVariables[1], "message", "SimpleErrorType",
                "string");

        // error details child variable visibility test
        Variable[] errorDetailsChildVariables = getChildVariable(errorChildVariables[0]);
        assertVariable(errorDetailsChildVariables[0], "message", "Simple error occurred", "string");

        // future child variable visibility test
        Variable[] futureChildVariables = getChildVariable(variables[14]);
        Arrays.sort(futureChildVariables, compareByName);
        assertVariable(futureChildVariables[0], "isDone", "true", "boolean");
        assertVariable(futureChildVariables[1], "result", "90", "int");

        // object child variable visibility test (Person object)
        Variable[] personObjectChildVariables = getChildVariable(variables[15]);
        Arrays.sort(personObjectChildVariables, compareByName);
        assertVariable(personObjectChildVariables[0], "address", "No 20, Palm grove", "string");
        assertVariable(personObjectChildVariables[1], "age", "0", "int");
        assertVariable(personObjectChildVariables[2], "email", "default@abc.com", "string");
        assertVariable(personObjectChildVariables[3], "name", "", "string");

        // anonymous object child variable visibility test (AnonPerson object)
        Variable[] anonPersonObjectChildVariables = getChildVariable(variables[16]);
        Arrays.sort(anonPersonObjectChildVariables, compareByName);
//        assertVariable(anonPersonObjectChildVariables[0], "address", "Address", "record");
        assertVariable(anonPersonObjectChildVariables[1], "age", "25", "int");
        assertVariable(anonPersonObjectChildVariables[2], "name", "John Doe", "string");

        // TODO: Anonymous object's grand child variables are not visible. Need to fix it.

//        Variable[] anonPersonAddressChildVariables = getChildVariable(anonPersonObjectChildVariables[0]);
//        Arrays.sort(anonPersonAddressChildVariables, compareByName);
//        assertVariable(anonPersonAddressChildVariables[0], "city", "Colombo", "string");
//        assertVariable(anonPersonAddressChildVariables[1], "country", "Sri Lanka", "string");

        // json child variable visibility test
        Variable[] jsonChildVariables = getChildVariable(variables[23]);
        Arrays.sort(jsonChildVariables, compareByName);
        assertVariable(jsonChildVariables[0], "color", "red", "string");
        assertVariable(jsonChildVariables[1], "name", "apple", "string");
        assertVariable(jsonChildVariables[2], "price", "40", "int");
    }

    @AfterClass
    private void cleanup() {
        terminateDebugSession();
    }
}
