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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined struct types in ballerina.
 */
@Test(groups = {"broken"})
public class StructNegativeTest {
    CompileResult result;
    
    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/structs/structs-negative.bal");
    }

    @Test
    public void testStructNegative() {
        // test duplicate struct definitions
        BAssertUtil.validateError(result, 0, "redeclared symbol 'Department'", 6, 1);

        // test struct with duplicate fields
        BAssertUtil.validateError(result, 1, "redeclared symbol 'id'", 14, 5);

        // test undeclared struct init
        BAssertUtil.validateError(result, 2, "unknown type 'Department123'", 18, 5);

        BAssertUtil.validateError(result, 3, "invalid literal for type 'other'", 18, 26);

        // test undeclared struct access
        BAssertUtil.validateError(result, 4, "undefined symbol 'dpt1'", 23, 5);

        BAssertUtil.validateError(result, 5, "variable 'dpt' is not initialized", 28, 5);

        // test undeclared struct-field access
        BAssertUtil.validateError(result, 6, "undefined field 'id' in struct 'Department'", 29, 5);

        // test undeclared field init
        BAssertUtil.validateError(result, 7, "undefined field 'age' in struct 'Department'", 34, 37);

        // test field init with mismatching type
        BAssertUtil.validateError(result, 8, "incompatible types: expected 'string', found 'int'", 39, 31);
    }

    @Test
    public void testInvalidStructLiteralKey() {
        CompileResult result = BCompileUtil.compile("test-src/structs/invalid-struct-literal-key-negative.bal");
        // test struct init with invalid field name
        BAssertUtil.validateError(result, 0, "invalid key: only identifiers are allowed for struct literal keys", 12,
                23);
    }

    @Test
    public void testExpressionAsStructLiteralKey() {
        CompileResult result = BCompileUtil.compile("test-src/structs/expression-as-struct-literal-key-negative.bal");
        BAssertUtil.validateError(result, 0, "invalid key: only identifiers are allowed for struct literal keys", 7,
                21);
    }

    @Test(description = "Test defining a struct constant")
    public void testStructConstant() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/structs", "constants");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "incompatible types: expected 'constants:Person', found 'int'");
    }

    @Test(description = "Test accessing an field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*error:.*array index out of range.*")
    public void testGetNonInitField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BRunUtil.invoke(compileResult, "testGetNonInitAttribute");
    }

    @Test(description = "Test accessing an arrays field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*error:.*array index out of range.*")
    public void testGetNonInitArrayField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BRunUtil.invoke(compileResult, "testGetNonInitArrayAttribute");
    }

    @Test(description = "Test accessing the field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = ".*error:.*array index out of range.*")
    public void testGetNonInitLastField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BRunUtil.invoke(compileResult, "testGetNonInitLastAttribute");
    }

    @Test(description = "Test setting an field of a noninitialized child struct")
    public void testSetNonInitField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BRunUtil.invoke(compileResult, "testSetFieldOfNonInitChildStruct");
    }

    @Test(description = "Test setting the field of a noninitialized root struct")
    public void testSetNonInitLastField() {
        CompileResult compileResult = BCompileUtil.compile("test-src/structs/struct.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BRunUtil.invoke(compileResult, "testSetFieldOfNonInitStruct");
    }

}
