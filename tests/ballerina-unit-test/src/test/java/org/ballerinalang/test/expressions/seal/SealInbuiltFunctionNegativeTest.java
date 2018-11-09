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
package org.ballerinalang.test.expressions.seal;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for sealing variables.
 *
 * @since 0.983.0
 */
public class SealInbuiltFunctionNegativeTest {

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
        compileResult = BCompileUtil.compile("test-src/expressions/seal/negative/seal-expr-negative-test.bal");
        recordNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/seal/negative/record-seal-expr-negative-test.bal");
        jsonNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/seal/negative/json-seal-expr-negative-test.bal");
        xmlNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/seal/negative/xml-seal-expr-negative-test.bal");
        mapNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/seal/negative/map-seal-expr-negative-test.bal");
        objectNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/seal/negative/object-seal-expr-negative-test.bal");
        arrayNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/seal/negative/array-seal-expr-negative-test.bal");
        tupleNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/seal/negative/tuple-seal-expr-negative-test.bal");
        unionNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/seal/negative/union-seal-expr-negative-test.bal");
        anydataNegativeTestCompileResult = BCompileUtil.
                compile("test-src/expressions/seal/negative/anydata-seal-expr-negative-test.bal");
    }


    //----------------------------- NegativeTest cases ------------------------------------------------------

    @Test
    public void testSealNegativeTest() {

        Assert.assertEquals(compileResult.getErrorCount(), 14);

        //Negative test case to verify the unsupported type for seal operation.
        BAssertUtil.validateError(compileResult, 0,
                "function 'seal' defined on not supported type 'stream<Employee>'",
                18, 35);

        //Negative test case to verify the no of arguments get passed to seal function.
        BAssertUtil.validateError(compileResult, 2,
                "too many arguments in call to 'seal()'",
                25, 24);

        //Negative test case to confirm primitive types are not supported for seal operation.
        BAssertUtil.validateError(compileResult, 4,
                "function 'seal' defined on not supported type 'string'",
                32, 22);

        //Negative test case to confirm primitive types are not supported for seal operation.
        BAssertUtil.validateError(compileResult, 6,
                "function 'seal' defined on not supported type 'string[]'",
                39, 20);

        //Negative test case to confirm values cannot be sealed as union type.
        BAssertUtil.validateError(compileResult, 8,
                "Incompatible seal type: type 'json' cannot be sealed as type " +
                        "'int|float|json'",
                47, 33);

        //Negative test case to confirm values cannot be sealed as primitive type.
        BAssertUtil.validateError(compileResult, 10,
                "Incompatible seal type: type 'any' cannot be sealed as type 'string'",
                53, 27);

        //Negative test case to confirm values cannot be sealed as primitive type arrays.
        BAssertUtil.validateError(compileResult, 12,
                "Incompatible seal type: type 'json' cannot be sealed as type 'int[]'",
                62, 26);
    }

    @Test
    public void testRecordSealNegativeTest() {

        Assert.assertEquals(recordNegativeTestCompileResult.getErrorCount(), 14);

        //Negative test case to confirm record cannot be sealed as xml.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 0,
                "Incompatible seal type: type 'Employee' cannot be " +
                        "sealed as type 'xml'",
                42, 20);

        //Negative test case to confirm open record to closed record seal conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 2,
                "Incompatible seal type: type 'Teacher' cannot be sealed as " +
                        "type 'Employee'",
                49, 25);

        //TODO enable test case once respective source is fixed in Ballerina core
        //Negative test case to confirm closed record to closed record seal conversion.
//        BAssertUtil.validateError(recordNegativeTestCompileResult, 4,
//                "Incompatible seal type: variable 'teacher' with type 'Teacher' cannot be sealed as type 'Student'",
//                57, 5);

        //Negative test case to confirm closed record to object seal conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 4,
                "Incompatible seal type: type 'Teacher' cannot be sealed as " +
                        "type 'TeacherObj'",
                65, 30);

        //Negative test case to confirm closed record to map seal conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 6,
                "Incompatible seal type: type 'Person' cannot be sealed as " +
                        "type 'map<string>'",
                73, 28);

        //Negative test case to confirm closed record to array seal conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 8,
                "Incompatible seal type: type 'Employee' cannot be sealed as type" +
                        " 'string[]'",
                80, 28);

        //Negative test case to confirm closed record to tuple seal conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 10,
                "Incompatible seal type: type 'Employee' cannot be sealed as type" +
                        " '(string,string)'",
                88, 35);

        //Negative test case to confirm record(with  object) to anydata seal conversion.
        BAssertUtil.validateError(recordNegativeTestCompileResult, 12,
                "Incompatible seal type: type 'ExtendedEmployee' cannot be sealed as type 'anydata'",
                109, 28);

    }

    @Test
    public void testJSONSealNegativeTest() {

        Assert.assertEquals(jsonNegativeTestCompileResult.getErrorCount(), 6);

        //Negative test case to confirm record cannot be sealed as xml.
        BAssertUtil.validateError(jsonNegativeTestCompileResult, 0,
                "Incompatible seal type: type 'json' cannot be sealed as type " +
                        "'xml'", 12, 20);

        //Negative test case to confirm record cannot be sealed as xml.
        BAssertUtil.validateError(jsonNegativeTestCompileResult, 2,
                "Incompatible seal type: type 'json' cannot be sealed as " +
                        "type 'EmployeeObj'", 19, 31);

        //Negative test case to confirm record cannot be sealed as tuple.
        BAssertUtil.validateError(jsonNegativeTestCompileResult, 4,
                "Incompatible seal type: type 'json' cannot be sealed as " +
                        "type '(string,string)'", 27, 35);
    }

    @Test
    public void testXMLSealNegativeTest() {

        Assert.assertEquals(xmlNegativeTestCompileResult.getErrorCount(), 12);

        //Negative test case to confirm xml cannot be sealed as record.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 0,
                "Incompatible seal type: type 'xml' cannot be sealed as type 'BookRecord'",
                13, 30);

        //Negative test case to confirm xml cannot be sealed as json.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 2,
                "Incompatible seal type: type 'xml' cannot be sealed as type 'json'",
                21, 22);

        //Negative test case to confirm xml cannot be sealed as Object.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 4,
                "Incompatible seal type: type 'xml' cannot be sealed as type 'BookObject'",
                29, 30);

        //Negative test case to confirm xml cannot be sealed as map.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 6,
                "Incompatible seal type: type 'xml' cannot be sealed as type 'map'",
                37, 20);

        //Negative test case to confirm xml cannot be sealed as array.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 8,
                "Incompatible seal type: type 'xml' cannot be sealed as type 'BookRecord[]'",
                45, 31);

        //Negative test case to confirm xml cannot be sealed as tuple.
        BAssertUtil.validateError(xmlNegativeTestCompileResult, 10,
                "Incompatible seal type: type 'xml' cannot be sealed as type '(string,string)'",
                53, 35);
    }

    @Test
    public void testMapSealNegativeTest() {

        Assert.assertEquals(mapNegativeTestCompileResult.getErrorCount(), 8);

        //Negative test case to confirm map cannot be sealed as xml.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 0,
                "Incompatible seal type: type 'map' cannot be sealed as type 'xml'",
                11, 20);

        //Negative test case to confirm map cannot be sealed as array.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 2,
                "Incompatible seal type: type 'map' cannot be sealed as type 'string[]'",
                18, 27);

        //Negative test case to confirm map cannot be sealed as tuple.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 4,
                "Incompatible seal type: type 'map' cannot be sealed as type '(string,string)'",
                25, 34);

        //Negative test case to confirm map cannot be sealed as object.
        BAssertUtil.validateError(mapNegativeTestCompileResult, 6,
                "Incompatible seal type: type 'map' cannot be sealed as type 'IntObject'",
                32, 29);
    }

    @Test
    public void testObjectSealNegativeTest() {

        Assert.assertEquals(objectNegativeTestCompileResult.getErrorCount(), 12);

        //Negative test case to confirm object cannot be sealed as record.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 0,
                "Incompatible seal type: type 'PersonObj' cannot be sealed as type 'Employee'",
                26, 25);

        //Negative test case to confirm object cannot be sealed as json.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 2,
                "Incompatible seal type: type 'PersonObj' cannot be sealed as type 'json'",
                34, 22);

        //Negative test case to confirm object cannot be sealed as xml.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 4,
                "Incompatible seal type: type 'PersonObj' cannot be sealed as type 'xml'",
                41, 20);

        //Negative test case to confirm object cannot be sealed as map.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 6,
                "Incompatible seal type: type 'PersonObj' cannot be sealed as type 'map'",
                48, 20);

        //Negative test case to confirm object cannot be sealed as array.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 8,
                "Incompatible seal type: type 'PersonObj' cannot be sealed as type 'any[]'",
                55, 22);

        //Negative test case to confirm object cannot be sealed as tuple.
        BAssertUtil.validateError(objectNegativeTestCompileResult, 10,
                "Incompatible seal type: type 'PersonObj' cannot be sealed as type '(int,string)'",
                62, 32);
    }

    @Test
    public void testArraySealNegativeTest() {

        Assert.assertEquals(arrayNegativeTestCompileResult.getErrorCount(), 10);

        //Negative test case to confirm array cannot be sealed as record.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 0,
                "Incompatible seal type: type 'any[]' cannot be sealed as type 'Employee'",
                19, 25);

        //Negative test case to confirm array cannot be sealed as xml.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 2,
                "Incompatible seal type: type 'any[]' cannot be sealed as type 'xml'",
                27, 20);

        //Negative test case to confirm array cannot be sealed as object.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 4,
                "Incompatible seal type: type 'any[]' cannot be sealed as type 'EmployeeObject'",
                35, 34);

        //Negative test case to confirm array cannot be sealed as map.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 6,
                "Incompatible seal type: type 'any[]' cannot be sealed as type 'map'",
                43, 20);

        //Negative test case to confirm array cannot be sealed as tuple.
        BAssertUtil.validateError(arrayNegativeTestCompileResult, 8,
                "Incompatible seal type: type 'any[]' cannot be sealed as type '(string,string,string)'",
                51, 43);
    }

    @Test
    public void testTupleSealNegativeTest() {

        Assert.assertEquals(tupleNegativeTestCompileResult.getErrorCount(), 12);

        //Negative test case to confirm tuple cannot be sealed as record.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 0,
                "Incompatible seal type: type '(string,string,string)' cannot be sealed as type 'Employee'",
                26, 28);

        //Negative test case to confirm tuple cannot be sealed as json.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 2,
                "Incompatible seal type: type '(string,string,string)' cannot be sealed as type 'json'",
                33, 22);

        //Negative test case to confirm tuple cannot be sealed as xml.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 4,
                "Incompatible seal type: type '(string,string,string)' cannot be sealed as type 'xml'",
                40, 20);

        //Negative test case to confirm tuple cannot be sealed as object.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 6,
                "Incompatible seal type: type '(string,int)' cannot be sealed as type 'EmployeeObj'",
                47, 31);

        //Negative test case to confirm tuple cannot be sealed as map.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 8,
                "Incompatible seal type: type '(string,string,string)' cannot be sealed as type 'map'",
                54, 20);

        //Negative test case to confirm tuple cannot be sealed as object.
        BAssertUtil.validateError(tupleNegativeTestCompileResult, 10,
                "Incompatible seal type: type '(string,string,string)' cannot be sealed as type 'string[]'",
                61, 27);
    }

    @Test
    public void testUnionSealNegativeTest() {

        Assert.assertEquals(unionNegativeTestCompileResult.getErrorCount(), 4);

        //Negative test case to validate invalid seal conversion from union to json.
        BAssertUtil.validateError(unionNegativeTestCompileResult, 0,
                "Incompatible seal type: type 'int|float|Employee' cannot be sealed as type 'json'",
                12, 21);

        //Negative test case to confirm tuple cannot be sealed as json.
        BAssertUtil.validateError(unionNegativeTestCompileResult, 2,
                "Incompatible seal type: type 'int|float|xml' cannot be sealed as type 'Employee'",
                19, 30);

    }

    @Test
    public void testAnydataSealNegativeTest() {

        Assert.assertEquals(anydataNegativeTestCompileResult.getErrorCount(), 3);

        //Negative test case to validate invalid seal conversion from anydata to object.

        BAssertUtil.validateError(anydataNegativeTestCompileResult, 0,
                "incompatible types: expected 'anydata', found 'PersonObj'",
                14, 24);

        BAssertUtil.validateError(anydataNegativeTestCompileResult, 1,
                "Incompatible seal type: type 'anydata' cannot be sealed as type 'PersonObj'",
                15, 28);

    }
}

