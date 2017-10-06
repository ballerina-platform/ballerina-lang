/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.structs;

import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined struct types in ballerina.
 */
public class StructNegativeTest {
    CompileResult result;
    
    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/structs/structs-negative.bal");
    }

    @Test
    public void testStructNegative() {
        // test duplicate struct definitions
        BTestUtils.validateError(result, 0, "redeclared symbol 'Department'", 6, 1);

        // test struct with duplicate fields
        BTestUtils.validateError(result, 1, "redeclared symbol 'id'", 14, 5);

        // test undeclared struct init
        BTestUtils.validateError(result, 2, "unknown type 'Department123'", 18, 5);

        // test undeclared struct access
        BTestUtils.validateError(result, 3, "undefined symbol 'dpt1'", 23, 5);

        // test undeclared struct-field access
        BTestUtils.validateError(result, 4, "undefined field 'id' in struct 'Department'", 29, 5);

        // test undeclared field init
        BTestUtils.validateError(result, 5, "undefined field 'age' in struct 'Department'", 34, 37);

        // test field init with mismatching type
        BTestUtils.validateError(result, 6, "incompatible types: expected 'string', found 'int'", 39, 31);

        // test struct init with invalid field name
        BTestUtils.validateError(result, 7,
                "invalid field name in 'struct' literal, identifier or string literal expected", 44, 23);

    }

    @Test(description = "Test defining a struct constant")
    public void testStructConstant() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs", "constants");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "missing token {'int', 'float', 'boolean', 'string', 'blob'} before 'Person'");
    }

    @Test(description = "Test accessing an field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: NullReferenceError.*")
    public void testGetNonInitField() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BTestUtils.invoke(compileResult, "testGetNonInitAttribute");
    }

    @Test(description = "Test accessing an arrays field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: NullReferenceError.*")
    public void testGetNonInitArrayField() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BTestUtils.invoke(compileResult, "testGetNonInitArrayAttribute");
    }

    @Test(description = "Test accessing the field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: NullReferenceError.*")
    public void testGetNonInitLastField() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BTestUtils.invoke(compileResult, "testGetNonInitLastAttribute");
    }

    @Test(description = "Test setting an field of a noninitialized child struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: NullReferenceError.*")
    public void testSetNonInitField() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BTestUtils.invoke(compileResult, "testSetFieldOfNonInitChildStruct");
    }

    @Test(description = "Test setting the field of a noninitialized root struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: NullReferenceError.*")
    public void testSetNonInitLastField() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BTestUtils.invoke(compileResult, "testSetFieldOfNonInitStruct");
    }

}
