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
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
        result = BCompileUtil.compile("test-src/structs/structs-negative.bal");
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

    @Test(groups = { "disableOnOldParser" })
    public void testInvalidStructLiteralKey() {
        CompileResult result = BCompileUtil.compile("test-src/structs/invalid-struct-literal-key-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "undefined symbol 'dptName'", 12, 23);
        BAssertUtil.validateError(result, i++, "missing comma token", 12, 30);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'string', found 'int'", 12, 31);
        BAssertUtil.validateError(result, i++, "missing open bracket token", 16, 17);
        BAssertUtil.validateError(result, i++, "undefined symbol 'a'", 16, 18);
        BAssertUtil.validateError(result, i++, "missing close bracket token", 16, 20);
        BAssertUtil.validateError(result, i++,
                "a type compatible with mapping constructor expressions not found in type 'boolean'", 20, 16);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'person', found '()'", 20, 20);
        BAssertUtil.validateError(result, i++, "missing close brace token", 20, 20);
        BAssertUtil.validateError(result, i++, "missing question mark token", 20, 20);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'person', found 'string'", 20, 24);
        BAssertUtil.validateError(result, i++, "invalid token '}'", 20, 29);
    }

    @Test(groups = { "disableOnOldParser" })
    public void testExpressionAsStructLiteralKey() {
        CompileResult result = BCompileUtil.compile("test-src/structs/expression-as-struct-literal-key-negative.bal");
        int i = 0;
        BAssertUtil.validateError(result, i++, "missing open bracket token", 7, 21);
        BAssertUtil.validateError(result, i++, "undefined symbol 'a'", 7, 22);
        BAssertUtil.validateError(result, i++, "missing close bracket token", 7, 24);
        BAssertUtil.validateError(result, i++,
                "a type compatible with mapping constructor expressions not found in type 'boolean'", 11, 21);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'Department', found '()'", 11, 25);
        BAssertUtil.validateError(result, i++, "missing close brace token", 11, 25);
        BAssertUtil.validateError(result, i++, "missing question mark token", 11, 25);
        BAssertUtil.validateError(result, i++, "incompatible types: expected 'Department', found 'string'", 11, 29);
        BAssertUtil.validateError(result, i++, "invalid token '}'", 11, 34);
    }

    @Test(description = "Test defining a struct constant")
    public void testStructConstant() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/structs/proj", "constants");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].message(),
                            "incompatible types: expected 'ballerina-test/constants:0.0.0:Person', found 'int'");
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
