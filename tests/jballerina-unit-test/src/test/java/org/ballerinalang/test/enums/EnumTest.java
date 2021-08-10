/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.enums;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for enums in Ballerina.
 *
 * @since 2.0
 */
public class EnumTest {
    private CompileResult compileResult, negativeTest, accessTest, accessTestNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/enums/enums.bal");
        negativeTest = BCompileUtil.compile("test-src/enums/enums-negative.bal");

        BCompileUtil.compileAndCacheBala("test-src/enums/TestEnumDefinitionProject");
        accessTest = BCompileUtil.compile("test-src/enums/enum-access.bal");
        accessTestNegative = BCompileUtil.compile("test-src/enums/enum-access-negative.bal");
    }

    @Test(description = "Positive tests for enums", dataProvider = "FunctionList")
    public void testLetExpression(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @Test(description = "Positive access tests for enums")
    public void testAccessPositive() {
        BRunUtil.invoke(accessTest, "test");
    }

    @DataProvider(name = "FunctionList")
    public Object[][] getTestFunctions() {
        return new Object[][]{
                {"testBasicEnumSupport"},
                {"testEnumAsType"},
                {"testEnumAsGlobalRef"},
                {"testEnumInRecursiveArray"},
        };
    }

    @Test
    public void testNegative() {
        int i = 0;
        validateError(negativeTest, i++, "missing identifier", 19, 1);
        validateError(negativeTest, i++, "missing identifier", 23, 1);
        validateError(negativeTest, i++, "incompatible types: expected 'string', found 'int'", 37, 16);
        validateError(negativeTest, i++, "incompatible types: expected 'string', found 'float'", 38, 13);
        validateError(negativeTest, i++, "redeclared symbol 'OPEN'", 43, 5);
        validateError(negativeTest, i++, "symbol 'CLOSED' is already initialized with '0'", 50, 5);
        validateError(negativeTest, i++, "incompatible types: expected 'int', found 'string'", 54, 18);
        validateError(negativeTest, i++, "incompatible types: expected 'float', found 'string'", 55, 16);
        validateError(negativeTest, i++, "incompatible types: expected 'Shape', found 'string'", 59, 15);
        validateError(negativeTest, i++, "incompatible types: expected 'Artist', found 'string'", 60, 16);
        validateError(negativeTest, i++, "incompatible types: expected 'Ed Shereen', found 'string'", 61, 12);
        assertEquals(negativeTest.getErrorCount(), i);
    }

    @Test
    public void testNegativeAccess() {
        int i = 0;
        validateError(accessTestNegative, i++, "attempt to refer to non-accessible symbol 'PF'", 21, 23);
        validateError(accessTestNegative, i++, "undefined symbol 'PF'", 21, 23);
        validateError(accessTestNegative, i++, "attempt to refer to non-accessible symbol 'Bands'", 22, 4);
        validateError(accessTestNegative, i++, "unknown type 'Bands'", 22, 4);
        validateError(accessTestNegative, i++, "undefined symbol 'Queen'", 22, 23);
        validateError(accessTestNegative, i++, "attempt to refer to non-accessible symbol 'PF'", 23, 4);
        validateError(accessTestNegative, i++, "unknown type 'PF'", 23, 4);
        assertEquals(accessTestNegative.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        negativeTest = null;
        accessTest = null;
        accessTestNegative = null;
    }
}
