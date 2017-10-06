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
import org.testng.annotations.Test;

/**
 * Test cases for user defined struct types in ballerina.
 */
public class StructAccessWithIndexNegativeTest {

    @Test(description = "Test accessing an undeclared struct")
    public void testUndeclaredStructAccess() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/undeclared-struct-access-with-index.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(), "undefined symbol 'dpt1'");
    }

    @Test(description = "Test accessing an undeclared field of a struct")
    public void testUndeclaredFieldAccess() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/undeclared-attribute-access-as-index.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "undefined field 'id' in struct 'Department'");
    }

    @Test(description = "Test accesing a struct with a dynamic index")
    public void testExpressionAsStructIndex() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct-access-with-dynamic-index.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "invalid index expression: expected string literal");
    }

    @Test(description = "Test accessing an field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: NullReferenceError.*")
    public void testGetNonInitField() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct-with-indexed-access.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BTestUtils.invoke(compileResult, "testGetNonInitAttribute");
    }

    @Test(description = "Test accessing an arrays field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: NullReferenceError.*")
    public void testGetNonInitArrayField() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct-with-indexed-access.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BTestUtils.invoke(compileResult, "testGetNonInitArrayAttribute");
    }

    @Test(description = "Test accessing the field of a noninitialized struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: NullReferenceError.*")
    public void testGetNonInitLastField() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct-with-indexed-access.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BTestUtils.invoke(compileResult, "testGetNonInitLastAttribute");
    }

    @Test(description = "Test setting an field of a noninitialized child struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: NullReferenceError.*")
    public void testSetNonInitField() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct-with-indexed-access.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BTestUtils.invoke(compileResult, "testSetFieldOfNonInitChildStruct");
    }

    @Test(description = "Test setting the field of a noninitialized root struct",
          expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: NullReferenceError.*")
    public void testSetNonInitLastField() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/struct-with-indexed-access.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        BTestUtils.invoke(compileResult, "testSetFieldOfNonInitStruct");
    }
}
