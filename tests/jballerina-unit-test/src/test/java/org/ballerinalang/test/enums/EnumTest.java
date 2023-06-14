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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for enums in Ballerina.
 *
 * @since 2.0
 */
public class EnumTest {
    private static final String DUPLICATE_METADATA_WARNING =
            "cannot specify metadata on more than one duplicate enum member";

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
    public Object[] getTestFunctions() {
        return new Object[]{
                "testBasicEnumSupport",
                "testEnumAsType",
                "testEnumAsGlobalRef",
                "testEnumInRecursiveArray",
                "testEnumMemberContainingNumericEscape",
                "testEnumMemberContainingQuotedIdentifier"
        };
    }

    @Test
    public void testNegative() {
        int i = 0;
        validateError(negativeTest, i++, "missing identifier", 19, 1);
        validateError(negativeTest, i++, "missing identifier", 23, 1);
        validateError(negativeTest, i++, "incompatible types: expected 'string', found '1'", 37, 16);
        validateError(negativeTest, i++, "incompatible types: expected 'string', found '2.0f'", 38, 13);
        validateError(negativeTest, i++, "redeclared symbol 'OPEN'", 43, 5);
        validateError(negativeTest, i++, "symbol 'CLOSED' is already initialized with '0'", 50, 5);
        validateError(negativeTest, i++, "symbol 'HOLD' is already initialized with 'HOD'", 51, 5);
        validateError(negativeTest, i++, "incompatible types: expected 'int', found 'string'", 55, 18);
        validateError(negativeTest, i++, "incompatible types: expected 'float', found 'string'", 56, 16);
        validateError(negativeTest, i++, "incompatible types: expected 'Shape', found 'string'", 60, 15);
        validateError(negativeTest, i++, "incompatible types: expected 'Artist', found 'string'", 61, 16);
        validateError(negativeTest, i++, "incompatible types: expected '\"Ed Shereen\"', found 'string'", 62, 12);
        validateError(negativeTest, i++, "expression is not a constant expression", 65, 18);
        validateError(negativeTest, i++, "undefined symbol 'x'", 65, 18);
        validateError(negativeTest, i++, "symbol 'A' is already initialized", 69, 5);
        validateError(negativeTest, i++, "symbol 'B' is already initialized with '1'", 70, 5);
        validateError(negativeTest, i++, "symbol 'A' is already initialized", 74, 5);
        validateError(negativeTest, i++, "expression is not a constant expression", 75, 1);
        validateError(negativeTest, i++, "missing expression", 75, 1);
        validateError(negativeTest, i++, "illegal cyclic reference '[CYCLIC1, CYCLIC2]'", 78, 5);
        validateError(negativeTest, i++, "illegal cyclic reference '[D, E]'", 82, 1);
        validateError(negativeTest, i++, "symbol 'D' is already initialized", 86, 5);
        validateError(negativeTest, i++, "symbol 'E' is already initialized", 87, 5);
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

    @Test
    public void testMetadataOnEnumMembers() {
        CompileResult result = BCompileUtil.compile("test-src/enums/enum_metadata_test.bal");
        int index = 0;
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 20, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 22, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 27, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 29, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 34, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 41, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 47, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 49, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 54, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 56, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 61, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 69, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 74, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 80, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 86, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 91, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 100, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 105, 5);
        validateWarning(result, index++, DUPLICATE_METADATA_WARNING, 113, 5);
        Assert.assertEquals(result.getDiagnostics().length, index);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        negativeTest = null;
        accessTest = null;
        accessTestNegative = null;
    }
}
