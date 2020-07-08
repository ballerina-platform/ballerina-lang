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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

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
        addBreakPoint(new BallerinaTestDebugPoint(testEntryFilePath, 150));
        initDebugSession(DebugUtils.DebuggeeExecutionKind.RUN);

        Pair<BallerinaTestDebugPoint, StoppedEventArguments> debugHitInfo = waitForDebugHit(10000);
        variables = fetchDebugHitVariables(debugHitInfo.getRight());
        Arrays.sort(variables, compareByName);

        // var variable visibility test
        Assert.assertEquals(variables[0].getName(), "v01_varVariable");
        Assert.assertEquals(variables[0].getValue(), "()");
        Assert.assertEquals(variables[0].getType(), "nil");

        // boolean variable visibility test
        Assert.assertEquals(variables[1].getName(), "v02_booleanVar");
        Assert.assertEquals(variables[1].getValue(), "true");
        Assert.assertEquals(variables[1].getType(), "boolean");

        // int variable visibility test
        Assert.assertEquals(variables[2].getName(), "v03_intVar");
        Assert.assertEquals(variables[2].getValue(), "20");
        Assert.assertEquals(variables[2].getType(), "int");

        // float variable visibility test
        Assert.assertEquals(variables[3].getName(), "v04_floatVar");
        Assert.assertEquals(variables[3].getValue(), "-10.0");
        Assert.assertEquals(variables[3].getType(), "float");

        // decimal variable visibility test
        Assert.assertEquals(variables[4].getName(), "v05_decimalVar");
        Assert.assertEquals(variables[4].getValue(), "3.5");
        Assert.assertEquals(variables[4].getType(), "decimal");

        // string variable visibility test
        Assert.assertEquals(variables[5].getName(), "v06_stringVar");
        Assert.assertEquals(variables[5].getValue(), "foo");
        Assert.assertEquals(variables[5].getType(), "string");

        // xml variable visibility test
        Assert.assertEquals(variables[6].getName(), "v07_xmlVar");
        Assert.assertEquals(variables[6].getValue(),
                "<person gender=\"male\"><firstname>Praveen</firstname><lastname>Nada</lastname></person>");
        Assert.assertEquals(variables[6].getType(), "xml");

        // array variable visibility test
        Assert.assertEquals(variables[7].getName(), "v08_arrayVar");
        Assert.assertEquals(variables[7].getValue(), "any[4]");
        Assert.assertEquals(variables[7].getType(), "array");

        // tuple variable visibility test
        Assert.assertEquals(variables[8].getName(), "v09_tupleVar");
        Assert.assertEquals(variables[8].getValue(), "tuple[int,string]");
        Assert.assertEquals(variables[8].getType(), "tuple");

        // map variable visibility test
        Assert.assertEquals(variables[9].getName(), "v10_mapVar");
        Assert.assertEquals(variables[9].getValue(), "map");
        Assert.assertEquals(variables[9].getType(), "map");

        // record variable visibility test (Student record)
        Assert.assertEquals(variables[10].getName(), "v11_john");
        Assert.assertEquals(variables[10].getValue(), "Student");
        Assert.assertEquals(variables[10].getType(), "record");

        // anonymous record variable visibility test
        Assert.assertEquals(variables[11].getName(), "v12_anonRecord");
        Assert.assertEquals(variables[11].getValue(), "anonymous");
        Assert.assertEquals(variables[11].getType(), "record");

        // error variable visibility test
        Assert.assertEquals(variables[12].getName(), "v13_errorVar");
        Assert.assertEquals(variables[12].getValue(), "SimpleErrorType");
        Assert.assertEquals(variables[12].getType(), "error");

        // anonymous function variable visibility test
        Assert.assertEquals(variables[13].getName(), "v14_anonFunctionVar");
        Assert.assertEquals(variables[13].getValue(), "function (string,string) returns (string)");
        Assert.assertEquals(variables[13].getType(), "function");

        // future variable visibility test
        Assert.assertEquals(variables[14].getName(), "v15_futureVar");
        Assert.assertEquals(variables[14].getValue(), "future");
        Assert.assertEquals(variables[14].getType(), "future");

        // object variable visibility test (Person object)
        Assert.assertEquals(variables[15].getName(), "v16_objectVar");
        Assert.assertEquals(variables[15].getValue(), "Person");
        Assert.assertEquals(variables[15].getType(), "object");

        // anonymous object variable visibility test (AnonPerson object)
        Assert.assertEquals(variables[16].getName(), "v17_anonObjectVar");
        Assert.assertEquals(variables[16].getValue(), "AnonPerson");
//        Assert.assertEquals(variables[16].getType(), "object");

        // type descriptor variable visibility test
        Assert.assertEquals(variables[17].getName(), "v18_typedescVar");
        Assert.assertEquals(variables[17].getValue(), "int");
        Assert.assertEquals(variables[17].getType(), "typedesc");

        // union variable visibility test
        Assert.assertEquals(variables[18].getName(), "v19_unionVar");
        Assert.assertEquals(variables[18].getValue(), "foo");
        Assert.assertEquals(variables[18].getType(), "string");

        // optional variable visibility test
        Assert.assertEquals(variables[19].getName(), "v20_optionalVar");
        Assert.assertEquals(variables[19].getValue(), "foo");
        Assert.assertEquals(variables[19].getType(), "string");

        // any variable visibility test
        Assert.assertEquals(variables[20].getName(), "v21_anyVar");
        Assert.assertEquals(variables[20].getValue(), "15.0");
        Assert.assertEquals(variables[20].getType(), "float");

        // anydata variable visibility test
        Assert.assertEquals(variables[21].getName(), "v22_anydataVar");
        Assert.assertEquals(variables[21].getValue(), "619");
        Assert.assertEquals(variables[21].getType(), "int");

        // byte variable visibility test
        Assert.assertEquals(variables[22].getName(), "v23_byteVar");
        Assert.assertEquals(variables[22].getValue(), "128");
        Assert.assertEquals(variables[22].getType(), "int");

        // json variable visibility test
        Assert.assertEquals(variables[23].getName(), "v24_jsonVar");
        Assert.assertEquals(variables[23].getValue(), "object");
        Assert.assertEquals(variables[23].getType(), "json");

        // table variable visibility test
        Assert.assertEquals(variables[24].getName(), "v25_tableVar");
        Assert.assertEquals(variables[24].getValue(), "table<Employee>");
        Assert.assertEquals(variables[24].getType(), "table");

        // stream variable visibility test
        Assert.assertEquals(variables[25].getName(), "v26_oddNumberStream");
        Assert.assertEquals(variables[25].getValue(), "stream<int>");
        Assert.assertEquals(variables[25].getType(), "stream");

        // never variable visibility test
        Assert.assertEquals(variables[26].getName(), "v27_neverVar");
        Assert.assertEquals(variables[26].getValue(), "");
        Assert.assertEquals(variables[26].getType(), "xml");
    }

    @Test(dependsOnMethods = "parentVariableVisibilityTest")
    public void childVariableVisibilityTest() throws BallerinaTestException {
        // xml child variable visibility test
        Variable[] xmlChildVariables = getChildVariable(variables[6]);
        Arrays.sort(xmlChildVariables, compareByName);
        Assert.assertEquals(xmlChildVariables[0].getName(), "attributes");
        Assert.assertEquals(xmlChildVariables[0].getValue(), "map");
        Assert.assertEquals(xmlChildVariables[0].getType(), "map");
        Assert.assertEquals(xmlChildVariables[1].getName(), "children");
        Assert.assertEquals(xmlChildVariables[1].getValue(),
                "<firstname>Praveen</firstname><lastname>Nada</lastname>");
        Assert.assertEquals(xmlChildVariables[1].getType(), "xml");

        // xml attributes child variable visibility test
        Variable[] xmlAttributesChildVariables = getChildVariable(xmlChildVariables[0]);
        Assert.assertEquals(xmlAttributesChildVariables[0].getName(), "gender");
        Assert.assertEquals(xmlAttributesChildVariables[0].getValue(), "male");
        Assert.assertEquals(xmlAttributesChildVariables[0].getType(), "string");

        // xml children variable visibility test
        Variable[] xmlChildrenVariables = getChildVariable(xmlChildVariables[1]);
        Assert.assertEquals(xmlChildrenVariables[0].getValue(), "<firstname>Praveen</firstname>");
        Assert.assertEquals(xmlChildrenVariables[0].getType(), "xml");
        Assert.assertEquals(xmlChildrenVariables[1].getValue(), "<lastname>Nada</lastname>");
        Assert.assertEquals(xmlChildrenVariables[1].getType(), "xml");

        // xml grand children variable visibility test
        Variable[] xmlGrandChildrenVariables = getChildVariable(xmlChildrenVariables[0]);
        Arrays.sort(xmlGrandChildrenVariables, compareByName);
        Assert.assertEquals(xmlGrandChildrenVariables[1].getName(), "children");
        Assert.assertEquals(xmlGrandChildrenVariables[1].getValue(), "Praveen");
        Assert.assertEquals(xmlGrandChildrenVariables[1].getType(), "xml");

        // array child variable visibility test
        Variable[] arrayChildVariables = getChildVariable(variables[7]);
        Assert.assertEquals(arrayChildVariables[0].getValue(), "1");
        Assert.assertEquals(arrayChildVariables[0].getType(), "int");
        Assert.assertEquals(arrayChildVariables[1].getValue(), "20");
        Assert.assertEquals(arrayChildVariables[1].getType(), "int");
        Assert.assertEquals(arrayChildVariables[2].getValue(), "-10.0");
        Assert.assertEquals(arrayChildVariables[2].getType(), "float");
        Assert.assertEquals(arrayChildVariables[3].getValue(), "foo");
        Assert.assertEquals(arrayChildVariables[3].getType(), "string");

        // tuple child variable visibility test
        Variable[] tupleChildVariables = getChildVariable(variables[8]);
        Assert.assertEquals(tupleChildVariables[0].getValue(), "20");
        Assert.assertEquals(tupleChildVariables[0].getType(), "int");
        Assert.assertEquals(tupleChildVariables[1].getValue(), "foo");
        Assert.assertEquals(tupleChildVariables[1].getType(), "string");

        // map child variable visibility test
        Variable[] mapChildVariables = getChildVariable(variables[9]);
        Arrays.sort(mapChildVariables, compareByName);
        Assert.assertEquals(mapChildVariables[0].getName(), "city");
        Assert.assertEquals(mapChildVariables[0].getValue(), "Colombo 03");
        Assert.assertEquals(mapChildVariables[0].getType(), "string");
        Assert.assertEquals(mapChildVariables[1].getName(), "country");
        Assert.assertEquals(mapChildVariables[1].getValue(), "Sri Lanka");
        Assert.assertEquals(mapChildVariables[1].getType(), "string");
        Assert.assertEquals(mapChildVariables[2].getName(), "line1");
        Assert.assertEquals(mapChildVariables[2].getValue(), "No. 20");
        Assert.assertEquals(mapChildVariables[2].getType(), "string");
        Assert.assertEquals(mapChildVariables[3].getName(), "line2");
        Assert.assertEquals(mapChildVariables[3].getValue(), "Palm Grove");
        Assert.assertEquals(mapChildVariables[3].getType(), "string");

        // record child variable visibility test (Student record)
        Variable[] studentRecordChildVariables = getChildVariable(variables[10]);
        Arrays.sort(studentRecordChildVariables, compareByName);
        Assert.assertEquals(studentRecordChildVariables[0].getName(), "age");
        Assert.assertEquals(studentRecordChildVariables[0].getValue(), "20");
        Assert.assertEquals(studentRecordChildVariables[0].getType(), "int");
        Assert.assertEquals(studentRecordChildVariables[1].getName(), "grades");
        Assert.assertEquals(studentRecordChildVariables[1].getValue(), "Grades");
        Assert.assertEquals(studentRecordChildVariables[1].getType(), "record");
        Assert.assertEquals(studentRecordChildVariables[2].getName(), "name");
        Assert.assertEquals(studentRecordChildVariables[2].getValue(), "John Doe");
        Assert.assertEquals(studentRecordChildVariables[2].getType(), "string");

        // record child variable visibility test (Grades record)
        Variable[] studentGradesChildVariables = getChildVariable(studentRecordChildVariables[1]);
        Arrays.sort(studentGradesChildVariables, compareByName);
        Assert.assertEquals(studentGradesChildVariables[0].getName(), "chemistry");
        Assert.assertEquals(studentGradesChildVariables[0].getValue(), "65");
        Assert.assertEquals(studentGradesChildVariables[0].getType(), "int");
        Assert.assertEquals(studentGradesChildVariables[1].getName(), "maths");
        Assert.assertEquals(studentGradesChildVariables[1].getValue(), "80");
        Assert.assertEquals(studentGradesChildVariables[1].getType(), "int");
        Assert.assertEquals(studentGradesChildVariables[2].getName(), "physics");
        Assert.assertEquals(studentGradesChildVariables[2].getValue(), "75");
        Assert.assertEquals(studentGradesChildVariables[2].getType(), "int");

        // anonymous record child variable visibility test
        Variable[] recordChildVariables = getChildVariable(variables[11]);
        Arrays.sort(recordChildVariables, compareByName);
        Assert.assertEquals(recordChildVariables[0].getName(), "city");
        Assert.assertEquals(recordChildVariables[0].getValue(), "London");
        Assert.assertEquals(recordChildVariables[0].getType(), "string");
        Assert.assertEquals(recordChildVariables[1].getName(), "country");
        Assert.assertEquals(recordChildVariables[1].getValue(), "UK");
        Assert.assertEquals(recordChildVariables[1].getType(), "string");

        // error child variable visibility test
        Variable[] errorChildVariables = getChildVariable(variables[12]);
        Arrays.sort(errorChildVariables, compareByName);
        Assert.assertEquals(errorChildVariables[0].getName(), "details");
        Assert.assertEquals(errorChildVariables[0].getValue(), "map");
        Assert.assertEquals(errorChildVariables[0].getType(), "map");
        Assert.assertEquals(errorChildVariables[1].getName(), "message");
        Assert.assertEquals(errorChildVariables[1].getValue(), "SimpleErrorType");
        Assert.assertEquals(errorChildVariables[1].getType(), "string");

        // error details child variable visibility test
        Variable[] errorDetailsChildVariables = getChildVariable(errorChildVariables[0]);
        Assert.assertEquals(errorDetailsChildVariables[0].getName(), "message");
        Assert.assertEquals(errorDetailsChildVariables[0].getValue(), "Simple error occurred");
        Assert.assertEquals(errorDetailsChildVariables[0].getType(), "string");

        // future child variable visibility test
        Variable[] futureChildVariables = getChildVariable(variables[14]);
        Arrays.sort(futureChildVariables, compareByName);
        Assert.assertEquals(futureChildVariables[0].getName(), "isDone");
        Assert.assertEquals(futureChildVariables[0].getValue(), "true");
        Assert.assertEquals(futureChildVariables[0].getType(), "boolean");
        Assert.assertEquals(futureChildVariables[1].getName(), "result");
        Assert.assertEquals(futureChildVariables[1].getValue(), "90");
        Assert.assertEquals(futureChildVariables[1].getType(), "int");

        // object child variable visibility test (Person object)
        Variable[] personObjectChildVariables = getChildVariable(variables[15]);
        Arrays.sort(personObjectChildVariables, compareByName);
        Assert.assertEquals(personObjectChildVariables[0].getName(), "address");
        Assert.assertEquals(personObjectChildVariables[0].getValue(), "No 20, Palm grove");
        Assert.assertEquals(personObjectChildVariables[0].getType(), "string");
        Assert.assertEquals(personObjectChildVariables[1].getName(), "age");
        Assert.assertEquals(personObjectChildVariables[1].getValue(), "0");
        Assert.assertEquals(personObjectChildVariables[1].getType(), "int");
        Assert.assertEquals(personObjectChildVariables[2].getName(), "email");
        Assert.assertEquals(personObjectChildVariables[2].getValue(), "default@abc.com");
        Assert.assertEquals(personObjectChildVariables[2].getType(), "string");
        Assert.assertEquals(personObjectChildVariables[3].getName(), "name");
        Assert.assertEquals(personObjectChildVariables[3].getValue(), "");
        Assert.assertEquals(personObjectChildVariables[3].getType(), "string");

        // anonymous object child variable visibility test (AnonPerson object)
        Variable[] anonPersonObjectChildVariables = getChildVariable(variables[16]);
        Arrays.sort(anonPersonObjectChildVariables, compareByName);
        Assert.assertEquals(anonPersonObjectChildVariables[0].getName(), "address");
//        Assert.assertEquals(anonPersonObjectChildVariables[0].getValue(), "Address");
//        Assert.assertEquals(anonPersonObjectChildVariables[0].getType(), "record");
        Assert.assertEquals(anonPersonObjectChildVariables[1].getName(), "age");
        Assert.assertEquals(anonPersonObjectChildVariables[1].getValue(), "25");
        Assert.assertEquals(anonPersonObjectChildVariables[1].getType(), "int");
        Assert.assertEquals(anonPersonObjectChildVariables[2].getName(), "name");
        Assert.assertEquals(anonPersonObjectChildVariables[2].getValue(), "John Doe");
        Assert.assertEquals(anonPersonObjectChildVariables[2].getType(), "string");

        // TODO: Anonymous object's grand child variables are not visible. Need to fix it.

//        Variable[] anonPersonAddressChildVariables = getChildVariable(anonPersonObjectChildVariables[0]);
//        Arrays.sort(anonPersonAddressChildVariables, compareByName);
//        Assert.assertEquals(anonPersonAddressChildVariables[0].getName(), "city");
//        Assert.assertEquals(anonPersonAddressChildVariables[0].getValue(), "Colombo");
//        Assert.assertEquals(anonPersonAddressChildVariables[0].getType(), "string");
//        Assert.assertEquals(anonPersonAddressChildVariables[1].getName(), "country");
//        Assert.assertEquals(anonPersonAddressChildVariables[1].getValue(), "Sri Lanka");
//        Assert.assertEquals(anonPersonAddressChildVariables[1].getType(), "string");

        // json child variable visibility test
        Variable[] jsonChildVariables = getChildVariable(variables[23]);
        Arrays.sort(jsonChildVariables, compareByName);
        Assert.assertEquals(jsonChildVariables[0].getName(), "color");
        Assert.assertEquals(jsonChildVariables[0].getValue(), "red");
        Assert.assertEquals(jsonChildVariables[0].getType(), "string");
        Assert.assertEquals(jsonChildVariables[1].getName(), "name");
        Assert.assertEquals(jsonChildVariables[1].getValue(), "apple");
        Assert.assertEquals(jsonChildVariables[1].getType(), "string");
        Assert.assertEquals(jsonChildVariables[2].getName(), "price");
        Assert.assertEquals(jsonChildVariables[2].getValue(), "40");
        Assert.assertEquals(jsonChildVariables[2].getType(), "int");

    }

    @AfterClass
    private void cleanup() {
        terminateDebugSession();
    }
}
