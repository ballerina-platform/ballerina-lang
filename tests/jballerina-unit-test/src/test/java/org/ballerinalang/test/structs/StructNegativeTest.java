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

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined struct types in ballerina.
 */
public class StructNegativeTest {
    CompileResult result;
    CompileResult result2;
    
    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/structs/structs-negative.bal");

        result2 = BCompileUtil.compile("test-src/structs/struct.bal");
        Assert.assertEquals(result2.getErrorCount(), 0);
        Assert.assertEquals(result2.getWarnCount(), 2);
        BAssertUtil.validateWarning(result2, 0, "unused variable 'person'", 132, 5);
        BAssertUtil.validateWarning(result2, 1, "unused variable 'dpt'", 137, 5);
    }

    @Test
    public void testStructNegative() {
        // test duplicate struct definitions
        BAssertUtil.validateError(result, 0, "redeclared symbol 'Department'", 6, 6);

        // test struct with duplicate fields
        BAssertUtil.validateError(result, 1, "redeclared symbol 'id'", 14, 12);

        // test undeclared struct init
        BAssertUtil.validateError(result, 2, "unknown type 'Department123'", 18, 5);

        // test undeclared struct access
        BAssertUtil.validateError(result, 3, "undefined symbol 'dpt1'", 23, 5);

        // test undeclared struct-field access
        BAssertUtil.validateError(result, 4, "undefined field 'id' in record 'Department'", 29, 5);

        // test undeclared field init
        BAssertUtil.validateError(result, 5, "undefined field 'age' in record 'Department'", 34, 37);

        // test field init with mismatching type
        BAssertUtil.validateError(result, 6, "incompatible types: expected 'string', found 'int'", 39, 31);
    }

    @Test
    public void testInvalidStructLiteralKey() {
        CompileResult result = BCompileUtil.compile("test-src/structs/invalid-struct-literal-key-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'string', found '[int]'", 12, 30);
        BAssertUtil.validateError(result, i++, "missing colon token", 12, 30);
        BAssertUtil.validateError(result, i++, "missing comma token", 12, 33);
        BAssertUtil.validateError(result, i++, "missing identifier", 12, 33);
        BAssertUtil.validateError(result, i++, "incompatible types: expected a map or a record, found 'other'", 16, 17);
        BAssertUtil.validateError(result, i++, "missing ellipsis token", 16, 17);
        BAssertUtil.validateError(result, i++, "undefined symbol 'a'", 16, 18);
        BAssertUtil.validateError(result, i++, "missing comma token", 16, 20);
        BAssertUtil.validateError(result, i++, "missing identifier", 16, 20);
        BAssertUtil.validateError(result, i++, "missing colon token", 20, 20);
        BAssertUtil.validateError(result, i++, "missing comma token", 20, 22);
        BAssertUtil.validateError(result, i++, "missing identifier", 20, 22);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testExpressionAsStructLiteralKey() {
        CompileResult result = BCompileUtil.compile("test-src/structs/expression-as-struct-literal-key-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "incompatible types: expected a map or a record, found 'other'", 7, 21);
        BAssertUtil.validateError(result, i++, "missing ellipsis token", 7, 21);
        BAssertUtil.validateError(result, i++, "undefined symbol 'a'", 7, 22);
        BAssertUtil.validateError(result, i++, "missing comma token", 7, 24);
        BAssertUtil.validateError(result, i++, "missing identifier", 7, 24);
        BAssertUtil.validateError(result, i++, "missing colon token", 11, 25);
        BAssertUtil.validateError(result, i++, "missing comma token", 11, 27);
        BAssertUtil.validateError(result, i++, "missing identifier", 11, 27);
        Assert.assertEquals(result.getErrorCount(), i);
    }

    @Test(description = "Test defining a struct constant")
    public void testStructConstant() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/ConstantsTestProject");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].message(),
                            "incompatible types: expected 'testorg/constants:0.0.0:Person', found 'int'");
    }

    @Test(description = "Test accessing an field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*error:.*array index out of range.*")
    public void testGetNonInitField() {
        BRunUtil.invoke(result2, "testGetNonInitAttribute");
    }

    @Test(description = "Test accessing an arrays field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*error:.*array index out of range.*")
    public void testGetNonInitArrayField() {
        BRunUtil.invoke(result2, "testGetNonInitArrayAttribute");
    }

    @Test(description = "Test accessing the field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*error:.*array index out of range.*")
    public void testGetNonInitLastField() {
        BRunUtil.invoke(result2, "testGetNonInitLastAttribute");
    }

    @Test(description = "Test setting an field of a noninitialized child struct")
    public void testSetNonInitField() {
        BRunUtil.invoke(result2, "testSetFieldOfNonInitChildStruct");
    }

    @Test(description = "Test setting the field of a noninitialized root struct")
    public void testSetNonInitLastField() {
        BRunUtil.invoke(result2, "testSetFieldOfNonInitStruct");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
