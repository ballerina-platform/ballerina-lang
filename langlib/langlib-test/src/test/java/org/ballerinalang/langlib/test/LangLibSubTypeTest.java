/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.test;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This class tests builtin subtype related functionality.
 *
 * @since 1.0
 */
public class LangLibSubTypeTest {

    private static final String FOUND_INT = " found 'int'";
    private static final String FOUND_SIGNED_32 = " found 'int:Signed32'";
    private static final String FOUND_SIGNED_16 = " found 'int:Signed16'";
    private static final String FOUND_SIGNED_8 = " found 'int:Signed8'";
    private static final String FOUND_UNSIGNED_32 = " found 'int:Unsigned32'";
    private static final String FOUND_UNSIGNED_16 = " found 'int:Unsigned16'";
    private static final String FOUND_UNSIGNED_8 = " found 'int:Unsigned8'";
    private static final String EXPECT_SIGNED_32 = "incompatible types: expected 'int:Signed32',";
    private static final String EXPECT_SIGNED_16 = "incompatible types: expected 'int:Signed16',";
    private static final String EXPECT_SIGNED_8 = "incompatible types: expected 'int:Signed8',";
    private static final String EXPECT_UNSIGNED_32 = "incompatible types: expected 'int:Unsigned32',";
    private static final String EXPECT_UNSIGNED_16 = "incompatible types: expected 'int:Unsigned16',";
    private static final String EXPECT_UNSIGNED_8 = "incompatible types: expected 'int:Unsigned8',";
    private static final String EXPECT_BYTE = "incompatible types: expected 'byte',";


    @Test
    public void testIntSubType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/subtypes/int_subtypes_test.bal");
        BRunUtil.invoke(compileResult, "testValueAssignment");
        BRunUtil.invoke(compileResult, "testSigned32Assignment");
        BRunUtil.invoke(compileResult, "testSigned16Assignment");
        BRunUtil.invoke(compileResult, "testSigned8Assignment");
        BRunUtil.invoke(compileResult, "testUnsigned32Assignment");
        BRunUtil.invoke(compileResult, "testUnsigned16Assignment");
        BRunUtil.invoke(compileResult, "testUnsigned8Assignment");
        BRunUtil.invoke(compileResult, "testTypeAlias");
        BRunUtil.invoke(compileResult, "testMathsOperators");
        BRunUtil.invoke(compileResult, "testTypeCastingWithInt");
        BRunUtil.invoke(compileResult, "testTypeCastingWith32");
        BRunUtil.invoke(compileResult, "testTypeCastingWith16");
        BRunUtil.invoke(compileResult, "testTypeCastingWith8");
        BRunUtil.invoke(compileResult, "testTypeCastingWithFloat");
        BRunUtil.invoke(compileResult, "testTypeCastingWithDecimal");
        BRunUtil.invoke(compileResult, "testTypeTest");
        BRunUtil.invoke(compileResult, "testList");
        BRunUtil.invoke(compileResult, "testMapping");
    }

    @Test
    public void testNegative() {

        CompileResult result = BCompileUtil.compile("test-src/int_subtype_assignment_test_negative.bal");
        int err = 0;
        // testValueAssignment
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 21, 24);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 23, 24);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_16 + FOUND_INT, 25, 24);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_16 + FOUND_INT, 27, 24);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_INT, 29, 23);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_INT, 31, 23);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_32 + FOUND_INT, 33, 26);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_32 + FOUND_INT, 35, 26);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_16 + FOUND_INT, 37, 26);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_16 + FOUND_INT, 39, 26);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_INT, 41, 25);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_INT, 43, 25);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 45, 15);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 47, 15);
        // testIntAssignment
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 52, 23);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_16 + FOUND_INT, 53, 23);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_INT, 54, 22);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_32 + FOUND_INT, 55, 25);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_16 + FOUND_INT, 56, 25);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_INT, 57, 24);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 58, 14);
        // testTypeAlias
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 65, 16);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 67, 17);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 69, 17);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_SIGNED_32, 72, 24);

        // Consts
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 77, 33);

        // Across Assignments
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_16 + FOUND_SIGNED_32, 81, 23);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_SIGNED_32, 82, 22);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_32 + FOUND_SIGNED_32, 83, 25);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_16 + FOUND_SIGNED_32, 84, 25);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_SIGNED_32, 85, 24);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_SIGNED_32, 86, 14);

        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_SIGNED_16, 92, 22);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_32 + FOUND_SIGNED_16, 93, 25);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_16 + FOUND_SIGNED_16, 94, 25);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_SIGNED_16, 95, 24);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_SIGNED_16, 96, 14);

        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_32 + FOUND_SIGNED_8, 103, 25);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_16 + FOUND_SIGNED_8, 104, 25);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_SIGNED_8, 105, 24);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_SIGNED_8, 106, 14);

        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_UNSIGNED_32, 111, 23);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_16 + FOUND_UNSIGNED_32, 112, 23);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_32, 113, 22);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_16 + FOUND_UNSIGNED_32, 114, 25);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_UNSIGNED_32, 115, 24);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_UNSIGNED_32, 116, 14);

        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_16 + FOUND_UNSIGNED_16, 122, 23);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_16, 123, 22);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_UNSIGNED_16, 125, 24);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_UNSIGNED_16, 126, 14);

        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_8, 133, 22);

        // Const reference
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_32 + FOUND_INT, 139, 29);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_16 + FOUND_INT, 140, 29);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_INT, 141, 28);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 142, 18);

        Assert.assertEquals(result.getErrorCount(), err);

    }
}
