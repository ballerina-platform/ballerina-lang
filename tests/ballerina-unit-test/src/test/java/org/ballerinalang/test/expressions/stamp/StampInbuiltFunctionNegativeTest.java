/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.expressions.stamp;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for stamping variables.
 *
 * @since 0.983.0
 */
public class StampInbuiltFunctionNegativeTest {

    private CompileResult compileResult;
    private CompileResult recordNegativeTestCompileResult;
    private CompileResult jsonNegativeTestCompileResult;
    private CompileResult xmlNegativeTestCompileResult;
    private CompileResult mapNegativeTestCompileResult;
    private CompileResult objectNegativeTestCompileResult;
    private CompileResult arrayNegativeTestCompileResult;
    private CompileResult tupleNegativeTestCompileResult;
    private CompileResult unionNegativeTestCompileResult;
    private CompileResult anydataNegativeTestCompileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/negative/stamp-expr-negative-test.bal");
        recordNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/record-stamp-expr-negative-test.bal");
        jsonNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/json-stamp-expr-negative-test.bal");
        xmlNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/xml-stamp-expr-negative-test.bal");
        mapNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/map-stamp-expr-negative-test.bal");
        objectNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/object-stamp-expr-negative-test.bal");
        arrayNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/array-stamp-expr-negative-test.bal");
        tupleNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/tuple-stamp-expr-negative-test.bal");
        unionNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/union-stamp-expr-negative-test.bal");
        anydataNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/stamp/negative/anydata-stamp-expr-negative-test.bal");
    }


    //----------------------------- NegativeTest cases ------------------------------------------------------

    @Test
    public void testStampNegativeTest() {

        Assert.assertEquals(compileResult.getErrorCount(), 16);

        //Negative test case to verify the unsupported type for stamp operation.
        BAssertUtil.validateError(compileResult, 0,
                "function 'stamp' defined on not supported type 'stream<Employee>'",
                18, 35);

        //Negative test case to verify the no of arguments get passed to stamp function.
        BAssertUtil.validateError(compileResult, 2,
                "too many arguments in call to 'stamp()'",
                25, 24);

        //Negative test case to confirm primitive types are not supported for stamp operation.
        BAssertUtil.validateError(compileResult, 4,
                "function 'stamp' defined on not supported type 'string'",
                32, 22);

        //Negative test case to confirm primitive types are not supported for stamp operation.
        BAssertUtil.validateError(compileResult, 6,
                "function 'stamp' defined on not supported type 'string[]'",
                39, 20);

        //Negative test case to confirm values cannot be stamped as union type.
        BAssertUtil.validateError(compileResult, 8,
                "Incompatible stamp type: type 'json' cannot be stamped as type " +
                        "'int|float|json'",
                47, 33);

        //Negative test case to confirm values cannot be stamped as primitive type.
        BAssertUtil.validateError(compileResult, 10,
                "Incompatible stamp type: type 'any' cannot be stamped as type 'string'",
                53, 27);

        //Negative test case to confirm values cannot be stamped as primitive type arrays.
        BAssertUtil.validateError(compileResult, 12,
                "Incompatible stamp type: type 'json' cannot be stamped as type 'int[]'",
                62, 26);

        //Negative test case to confirm invalid types cannot be used as argument for stamp function.
        BAssertUtil.validateError(compileResult, 14,
                "undefined symbol 'TestType'",
                70, 40);
    }

    @Test
    public void testRecordStampNegativeTest() {

        Assert.assertEquals(recordNegativeTestCompileResult.getErrorCount(), 14);

        //Negative test case to confirm record cannot be stamped as xml.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 0,
                "Incompatible stamp type: type 'Employee' cannot be " +
                        "stamped as type 'xml'",
                42, 20);

        //Negative test case to confirm open record to closed record stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 2,
                "Incompatible stamp type: type 'Teacher' cannot be stamped as " +
                        "type 'Employee'",
                49, 25);

        //TODO enable test case once respective source is fixed in Ballerina core
        //Negative test case to confirm closed record to closed record stamp conversion.
//        BAssertUtil.validateError(recordNegativeTestCompileResult, 4,
//                "Incompatible stamp type: variable 'teacher' with type 'Teacher' cannot be stamped as type 'Student'",
//                57, 5);

        //Negative test case to confirm closed record to object stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 4,
                "Incompatible stamp type: type 'Teacher' cannot be stamped as " +
                        "type 'TeacherObj'",
                65, 30);

        //Negative test case to confirm closed record to map stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 6,
                "Incompatible stamp type: type 'Person' cannot be stamped as " +
                        "type 'map<string>'",
                73, 28);

        //Negative test case to confirm closed record to array stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 8,
                "Incompatible stamp type: type 'Employee' cannot be stamped as type" +
                        " 'string[]'",
                80, 28);

        //Negative test case to confirm closed record to tuple stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 10,
                "Incompatible stamp type: type 'Employee' cannot be stamped as type" +
                        " '(string,string)'",
                88, 35);

        //Negative test case to confirm record(with  object) to anydata stamp conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 12,
                "Incompatible stamp type: type 'ExtendedEmployee' cannot be stamped as type 'anydata'",
                109, 28);

    }

    @Test
    public void testJSONStampNegativeTest() {

        Assert.assertEquals(jsonNegativeTestCompileResult.getErrorCount(), 6);

        //Negative test case to confirm record cannot be stamped as xml.
        BAssertUtil.validateError(jsonNegativeTestCompileResult, 0,
                "Incompatible stamp type: type 'json' cannot be stamped as type " +
                        "'xml'", 12, 20);

        //Negative test case to confirm record cannot be stamped as xml.
        BAssertUtil.validateError(jsonNegativeTestCompileResult, 2,
                "Incompatible stamp type: type 'json' cannot be stamped as " +
                        "type 'EmployeeObj'", 19, 31);

        //Negative test case to confirm record cannot be stamped as tuple.
        BAssertUtil.validateError(jsonNegativeTestCompileResult, 4,
                "Incompatible stamp type: type 'json' cannot be stamped as " +
                        "type '(string,string)'", 27, 35);
    }

    @Test
    public void testXMLStampNegativeTest() {

        Assert.assertEquals(xmlNegativeTestCompileResult.getErrorCount(), 12);

        //Negative test case to confirm xml cannot be stamped as record.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 0,
                "Incompatible stamp type: type 'xml' cannot be stamped as type 'BookRecord'",
                13, 30);

        //Negative test case to confirm xml cannot be stamped as json.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 2,
                "Incompatible stamp type: type 'xml' cannot be stamped as type 'json'",
                21, 22);

        //Negative test case to confirm xml cannot be stamped as Object.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 4,
                "Incompatible stamp type: type 'xml' cannot be stamped as type 'BookObject'",
                29, 30);

        //Negative test case to confirm xml cannot be stamped as map.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 6,
                "Incompatible stamp type: type 'xml' cannot be stamped as type 'map'",
                37, 20);

        //Negative test case to confirm xml cannot be stamped as array.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 8,
                "Incompatible stamp type: type 'xml' cannot be stamped as type 'BookRecord[]'",
                45, 31);

        //Negative test case to confirm xml cannot be stamped as tuple.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 10,
                "Incompatible stamp type: type 'xml' cannot be stamped as type '(string,string)'",
                53, 35);
    }

    @Test
    public void testMapStampNegativeTest() {

        Assert.assertEquals(mapNegativeTestCompileResult.getErrorCount(), 8);

        //Negative test case to confirm map cannot be stamped as xml.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 0,
                "Incompatible stamp type: type 'map' cannot be stamped as type 'xml'",
                11, 20);

        //Negative test case to confirm map cannot be stamped as array.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 2,
                "Incompatible stamp type: type 'map' cannot be stamped as type 'string[]'",
                18, 27);

        //Negative test case to confirm map cannot be stamped as tuple.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 4,
                "Incompatible stamp type: type 'map' cannot be stamped as type '(string,string)'",
                25, 34);

        //Negative test case to confirm map cannot be stamped as object.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 6,
                "Incompatible stamp type: type 'map' cannot be stamped as type 'IntObject'",
                32, 29);
    }

    @Test
    public void testObjectStampNegativeTest() {

        Assert.assertEquals(objectNegativeTestCompileResult.getErrorCount(), 12);

        //Negative test case to confirm object cannot be stamped as record.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 0,
                "Incompatible stamp type: type 'PersonObj' cannot be stamped as type 'Employee'",
                26, 25);

        //Negative test case to confirm object cannot be stamped as json.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 2,
                "Incompatible stamp type: type 'PersonObj' cannot be stamped as type 'json'",
                34, 22);

        //Negative test case to confirm object cannot be stamped as xml.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 4,
                "Incompatible stamp type: type 'PersonObj' cannot be stamped as type 'xml'",
                41, 20);

        //Negative test case to confirm object cannot be stamped as map.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 6,
                "Incompatible stamp type: type 'PersonObj' cannot be stamped as type 'map'",
                48, 20);

        //Negative test case to confirm object cannot be stamped as array.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 8,
                "Incompatible stamp type: type 'PersonObj' cannot be stamped as type 'any[]'",
                55, 22);

        //Negative test case to confirm object cannot be stamped as tuple.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 10,
                "Incompatible stamp type: type 'PersonObj' cannot be stamped as type '(int,string)'",
                62, 32);
    }

    @Test
    public void testArrayStampNegativeTest() {

        Assert.assertEquals(arrayNegativeTestCompileResult.getErrorCount(), 10);

        //Negative test case to confirm array cannot be stamped as record.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 0,
                "Incompatible stamp type: type 'any[]' cannot be stamped as type 'Employee'",
                19, 25);

        //Negative test case to confirm array cannot be stamped as xml.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 2,
                "Incompatible stamp type: type 'any[]' cannot be stamped as type 'xml'",
                27, 20);

        //Negative test case to confirm array cannot be stamped as object.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 4,
                "Incompatible stamp type: type 'any[]' cannot be stamped as type 'EmployeeObject'",
                35, 34);

        //Negative test case to confirm array cannot be stamped as map.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 6,
                "Incompatible stamp type: type 'any[]' cannot be stamped as type 'map'",
                43, 20);

        //Negative test case to confirm array cannot be stamped as tuple.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 8,
                "Incompatible stamp type: type 'any[]' cannot be stamped as type '(string,string,string)'",
                51, 43);
    }

    @Test
    public void testTupleStampNegativeTest() {

        Assert.assertEquals(tupleNegativeTestCompileResult.getErrorCount(), 12);

        //Negative test case to confirm tuple cannot be stamped as record.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 0,
                "Incompatible stamp type: type '(string,string,string)' cannot be stamped as type 'Employee'",
                26, 28);

        //Negative test case to confirm tuple cannot be stamped as json.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 2,
                "Incompatible stamp type: type '(string,string,string)' cannot be stamped as type 'json'",
                33, 22);

        //Negative test case to confirm tuple cannot be stamped as xml.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 4,
                "Incompatible stamp type: type '(string,string,string)' cannot be stamped as type 'xml'",
                40, 20);

        //Negative test case to confirm tuple cannot be stamped as object.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 6,
                "Incompatible stamp type: type '(string,int)' cannot be stamped as type 'EmployeeObj'",
                47, 31);

        //Negative test case to confirm tuple cannot be stamped as map.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 8,
                "Incompatible stamp type: type '(string,string,string)' cannot be stamped as type 'map'",
                54, 20);

        //Negative test case to confirm tuple cannot be stamped as object.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 10,
                "Incompatible stamp type: type '(string,string,string)' cannot be stamped as type 'string[]'",
                61, 27);
    }

    @Test
    public void testUnionStampNegativeTest() {

        Assert.assertEquals(unionNegativeTestCompileResult.getErrorCount(), 4);

        //Negative test case to validate invalid stamp conversion from union to json.
        BAssertUtil.validateError(unionNegativeTestCompileResult, 0,
                "Incompatible stamp type: type 'int|float|Employee' cannot be stamped as type 'json'",
                12, 21);

        //Negative test case to confirm tuple cannot be stamped as json.
        BAssertUtil.validateError(unionNegativeTestCompileResult, 2,
                "Incompatible stamp type: type 'int|float|xml' cannot be stamped as type 'Employee'",
                19, 30);

    }

    @Test
    public void testAnydataStampNegativeTest() {

        Assert.assertEquals(anydataNegativeTestCompileResult.getErrorCount(), 3);

        //Negative test case to validate invalid stamp conversion from anydata to object.

        BAssertUtil.validateError(anydataNegativeTestCompileResult, 0,
                "incompatible types: expected 'anydata', found 'PersonObj'",
                14, 24);

        BAssertUtil.validateError(anydataNegativeTestCompileResult, 1,
                "Incompatible stamp type: type 'anydata' cannot be stamped as type 'PersonObj'",
                15, 28);

    }
}

