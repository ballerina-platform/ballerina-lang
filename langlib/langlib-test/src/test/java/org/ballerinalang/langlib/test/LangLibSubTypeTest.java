/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This class tests builtin subtype related functionality.
 *
 * @since 1.0
 */
public class LangLibSubTypeTest {

    private static final String FOUND_BYTE = " found 'byte'";
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
    private static final String EXPECT_CHAR = "incompatible types: expected 'string:Char',";
    private static final String FOUND_STRING = " found 'string'";


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
//        JvmRunUtil.invoke(compileResult, "testConstReference");
        BRunUtil.invoke(compileResult, "testLeftShift");
        BRunUtil.invoke(compileResult, "testRightShift");
        BRunUtil.invoke(compileResult, "testUnsignedRightShift");
        BRunUtil.invoke(compileResult, "testBitwiseAnd");
        BRunUtil.invoke(compileResult, "testBitwiseOr");
        BRunUtil.invoke(compileResult, "testBitwiseXor");
        BRunUtil.invoke(compileResult, "testFiniteTypeAsIntSubType");
        BRunUtil.invoke(compileResult, "testLanglibFunctionsForUnionIntSubtypes");
    }

    @Test
    public void testCharSubType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/subtypes/char_subtypes_test.bal");
        BRunUtil.invoke(compileResult, "testValueAssignment");
        BRunUtil.invoke(compileResult, "testConcat");
        BRunUtil.invoke(compileResult, "testCharLangLib");
        BRunUtil.invoke(compileResult, "testList");
        BRunUtil.invoke(compileResult, "testMapping");
        BRunUtil.invoke(compileResult, "testFromCodePointIntInSurrogateRange");
        BRunUtil.invoke(compileResult, "testFromCodePointIntsInSurrogateRange");
        BRunUtil.invoke(compileResult, "testStringAssignabilityToSingleCharVarDef");
        BRunUtil.invoke(compileResult, "testToCodePointWithChaType");
        BRunUtil.invoke(compileResult, "testFiniteTypeAsStringSubType");
        BRunUtil.invoke(compileResult, "testLanglibFunctionsForUnionStringSubtypes");
    }

    @Test
    public void testNegativeIntSubType() {

        CompileResult result = BCompileUtil.compile("test-src/subtypes/int_subtype_test_negative.bal");
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
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'NewInt',"
                + FOUND_INT, 65, 16);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'NewInt',"
                + FOUND_INT, 67, 17);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'NewInt',"
                + FOUND_INT, 69, 17);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + " found 'NewInt'", 72, 24);

        // TODO : Fix this, Issue : #21542
//        // Consts
//        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 77, 33);

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

        // TODO : Fix this, Issue : #21542
//        // Const reference
//        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_32 + FOUND_INT, 139, 29);
//        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_16 + FOUND_INT, 140, 29);
//        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_INT, 141, 28);
//        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 142, 18);

        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 188, 14);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'float'," + FOUND_INT, 189, 15);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 190, 23);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 191, 14);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 192, 14);
        BAssertUtil.validateError(result, err++, EXPECT_UNSIGNED_8 + FOUND_INT, 193, 24);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 194, 14);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'decimal'," + FOUND_INT, 195, 17);

        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 209, 14);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'float'," + FOUND_INT, 210, 15);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 211, 23);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 212, 14);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_BYTE, 213, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_8, 214, 22);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_UNSIGNED_16, 215, 14);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'decimal'," + FOUND_UNSIGNED_32, 216,
                                  17);

        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 230, 14);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'float'," + FOUND_INT, 231, 15);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 232, 23);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 233, 14);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_BYTE, 234, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_8, 235, 22);
        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_UNSIGNED_16, 236, 14);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'decimal'," + FOUND_UNSIGNED_32, 237,
                                  17);

        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 251, 14);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'float'," + FOUND_INT, 252, 15);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 253, 23);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'decimal'," + FOUND_BYTE, 254, 17);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_8, 255, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_16 + FOUND_UNSIGNED_16, 256, 23);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_8, 257, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_32, 258, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_8, 259, 22);

        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 273, 14);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'float'," + FOUND_INT, 274, 15);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 275, 23);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'decimal'," + FOUND_BYTE, 276, 17);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_8, 277, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_16 + FOUND_UNSIGNED_32, 278, 23);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_32, 279, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_INT, 280, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_INT, 281, 22);

        BAssertUtil.validateError(result, err++, EXPECT_BYTE + FOUND_INT, 295, 14);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'float'," + FOUND_INT, 296, 15);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_32 + FOUND_INT, 297, 23);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'decimal'," + FOUND_BYTE, 298, 17);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_16, 299, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_16 + FOUND_UNSIGNED_32, 300, 23);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_UNSIGNED_32, 301, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_INT, 302, 22);
        BAssertUtil.validateError(result, err++, EXPECT_SIGNED_8 + FOUND_INT, 303, 22);

        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Unsigned32', found 'X'", 312,
                24);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Unsigned8[]', found 'X[]'", 315,
                25);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Signed8[]', found 'Y[]'",
                318, 23);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Signed8', found 'Z'", 321,
                21);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(string:Char|int:Signed8)', found " +
                        "'Z'", 322, 33);
        BAssertUtil.validateError(result, err++, "incompatible types: expected " +
                "'(float|string:Char|int:Signed8)[]', found 'Z[]'", 325, 43);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(float|string|int:Unsigned8)[]', " +
                "found 'Z[]'", 326, 40);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int', found " +
                "'InvalidIntType'", 337, 25);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int', found " +
                "'(int:Signed32|int:Signed16|string)'", 338, 25);
        BAssertUtil.validateError(result, err++, "undefined function 'toHexString' in type " +
                "'InvalidIntType'", 340, 17);
        BAssertUtil.validateError(result, err++, "undefined function 'toHexString' in type " +
                "'(int:Signed32|int:Signed16|string)'", 341, 17);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int', " +
                "found 'InvalidIntFiniteType'", 343, 25);
        BAssertUtil.validateError(result, err++, "undefined function 'toHexString' in " +
                "type 'InvalidIntFiniteType'", 344, 17);

        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Signed16', found 'int'", 348, 23);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Signed16', found 'int'", 349, 23);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Signed8', found 'int'", 350, 22);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Signed8', found 'int'", 351, 22);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Unsigned16', found 'int'", 352, 25);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Unsigned16', found 'int'", 353, 25);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Unsigned8', found 'int'", 354, 24);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'int:Unsigned8', found 'int'", 355, 24);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Signed16)', " +
                "found 'int'", 367, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Signed16)', " +
                "found 'int'", 368, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Signed8)', " +
                "found 'int'", 369, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Signed8)', " +
                "found 'int'", 370, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Signed16)', " +
                "found 'int'", 372, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Signed16)', " +
                "found 'int'", 373, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Signed8)', " +
                "found 'int'", 374, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Signed8)', " +
                "found 'int'", 375, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Signed32)', " +
                "found 'int'", 377, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Signed32)', " +
                "found 'int'", 378, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Signed8)', " +
                "found 'int'", 379, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Signed8)', " +
                "found 'int'", 380, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Signed32)', " +
                "found 'int'", 382, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Signed32)', " +
                "found 'int'", 383, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Signed8)', " +
                "found 'int'", 384, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Signed8)', " +
                "found 'int'", 385, 34);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned8)', " +
                "found 'int'", 387, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned8)', " +
                "found 'int'", 388, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed8)', " +
                "found 'int'", 389, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed8)', " +
                "found 'int'", 390, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned8)', " +
                "found 'int'", 392, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned8)', " +
                "found 'int'", 393, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed8)', " +
                "found 'int'", 394, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed8)', " +
                "found 'int'", 395, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|byte)', found 'int'",
                397, 26);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|byte)', found 'int'",
                398, 26);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed8)', found 'int'",
                399, 26);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed8)', found 'int'",
                400, 26);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|byte)', found 'int'",
                402, 26);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|byte)', found 'int'",
                403, 26);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed8)', found 'int'",
                404, 26);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed8)', found 'int'",
                405, 26);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned16)', " +
                "found 'int'", 407, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned16)', " +
                "found 'int'", 408, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed8)', " +
                "found 'int'", 409, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed8)', " +
                "found 'int'", 410, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned16)', " +
                "found 'int'", 412, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned16)', " +
                "found 'int'", 413, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed8)', " +
                "found 'int'", 414, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed8)', " +
                "found 'int'", 415, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned32)', " +
                "found 'int'", 417, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned32)', " +
                "found 'int'", 418, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed8)', " +
                "found 'int'", 419, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed8)', " +
                "found 'int'", 420, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned32)', " +
                "found 'int'", 422, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed8|int:Unsigned32)', " +
                "found 'int'", 423, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed8)', " +
                "found 'int'", 424, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed8)', " +
                "found 'int'", 425, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Signed32)', " +
                "found 'int'", 427, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Signed32)', " +
                "found 'int'", 428, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Signed16)', " +
                "found 'int'", 429, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Signed16)', " +
                "found 'int'", 430, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Signed32)', " +
                "found 'int'", 432, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Signed32)', " +
                "found 'int'", 433, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Signed16)', " +
                "found 'int'", 434, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Signed16)', " +
                "found 'int'", 435, 35);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned8)', " +
                "found 'int'", 437, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned8)', " +
                "found 'int'", 438, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed16)', " +
                "found 'int'", 439, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed16)', " +
                "found 'int'", 440, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned8)', " +
                "found 'int'", 442, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned8)', " +
                "found 'int'", 443, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed16)', " +
                "found 'int'", 444, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed16)', " +
                "found 'int'", 445, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|byte)', found 'int'",
                447, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|byte)', found 'int'",
                448, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed16)', found 'int'",
                449, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed16)', found 'int'",
                450, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|byte)', found 'int'",
                452, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|byte)', found 'int'",
                453, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed16)', found 'int'",
                454, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed16)', found 'int'",
                455, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned16)', " +
                "found 'int'", 457, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned16)', " +
                "found 'int'", 458, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed16)', " +
                "found 'int'", 459, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed16)', " +
                "found 'int'", 460, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned16)', " +
                "found 'int'", 462, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned16)', " +
                "found 'int'", 463, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed16)', " +
                "found 'int'", 464, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed16)', " +
                "found 'int'", 465, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned32)', " +
                "found 'int'", 467, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned32)', " +
                "found 'int'", 468, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed16)', " +
                "found 'int'", 469, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed16)', " +
                "found 'int'", 470, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned32)', " +
                "found 'int'", 472, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed16|int:Unsigned32)', " +
                "found 'int'", 473, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed16)', " +
                "found 'int'", 474, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed16)', " +
                "found 'int'", 475, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned8)', " +
                "found 'int'", 477, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned8)', " +
                "found 'int'", 478, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed32)', " +
                "found 'int'", 479, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed32)', " +
                "found 'int'", 480, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned8)', " +
                "found 'int'", 482, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned8)', " +
                "found 'int'", 483, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed32)', " +
                "found 'int'", 484, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Signed32)', " +
                "found 'int'", 485, 36);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|byte)', found 'int'",
                487, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|byte)', found 'int'",
                488, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed32)', found 'int'",
                489, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed32)', found 'int'",
                490, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|byte)', found 'int'",
                492, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|byte)', found 'int'",
                493, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed32)', found 'int'",
                494, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Signed32)', found 'int'",
                495, 27);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned16)', " +
                "found 'int'", 497, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned16)', " +
                "found 'int'", 498, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed32)', " +
                "found 'int'", 499, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed32)', " +
                "found 'int'", 500, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned16)', " +
                "found 'int'", 502, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned16)', " +
                "found 'int'", 503, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed32)', " +
                "found 'int'", 504, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Signed32)', " +
                "found 'int'", 505, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned32)', " +
                "found 'int'", 507, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned32)', " +
                "found 'int'", 508, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed32)', " +
                "found 'int'", 509, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed32)', " +
                "found 'int'", 510, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned32)', " +
                "found 'int'", 512, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Signed32|int:Unsigned32)', " +
                "found 'int'", 513, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed32)', " +
                "found 'int'", 514, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Signed32)', " +
                "found 'int'", 515, 37);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Unsigned16)', " +
                "found 'int'", 517, 38);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Unsigned16)', " +
                "found 'int'", 518, 38);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Unsigned8)', " +
                "found 'int'", 519, 38);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Unsigned8)', " +
                "found 'int'", 520, 38);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Unsigned32)', " +
                "found 'int'", 522, 38);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|int:Unsigned32)', " +
                "found 'int'", 523, 38);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Unsigned8)', " +
                "found 'int'", 524, 38);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Unsigned8)', " +
                "found 'int'", 525, 38);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Unsigned16)', found 'int'",
                527, 29);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Unsigned16)', found 'int'",
                528, 29);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|byte)', found 'int'",
                529, 29);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|byte)', found 'int'",
                530, 29);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Unsigned32)', found 'int'",
                532, 29);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(byte|int:Unsigned32)', found 'int'",
                533, 29);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|byte)', found 'int'",
                534, 29);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|byte)', found 'int'",
                535, 29);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Unsigned32)', " +
                "found 'int'", 537, 39);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned16|int:Unsigned32)', " +
                "found 'int'", 538, 39);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Unsigned16)', " +
                "found 'int'", 539, 39);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned32|int:Unsigned16)', " +
                "found 'int'", 540, 39);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType1', found 'int'", 542, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType1', found 'int'", 543, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType1', found 'int'", 544, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType1', found 'int'", 545, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType2', found 'int'", 547, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType2', found 'int'", 548, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType3', found 'int'", 550, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType3', found 'int'", 551, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType3', found 'int'", 552, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType3', found 'int'", 553, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType4', found 'int'", 555, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType4', found 'int'", 556, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType4', found 'int'", 557, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'IntType4', found 'int'", 558, 18);
        BAssertUtil.validateError(result, err++, "incompatible types: expected " +
                "'(int:Signed8|int:Signed32|int:Unsigned32)', found 'int'", 560, 49);
        BAssertUtil.validateError(result, err++, "incompatible types: expected " +
                "'(int:Unsigned32|int:Unsigned8|int:Signed16)', found 'int'", 561, 51);

        Assert.assertEquals(result.getErrorCount(), err);
    }

    @Test
    public void testNegativeCharSubType() {
        CompileResult result = BCompileUtil.compile("test-src/subtypes/char_subtypes_test_negative.bal");

        int err = 0;
        // testValueAssignment
        BAssertUtil.validateError(result, err++, EXPECT_CHAR + FOUND_STRING, 21, 16);
        BAssertUtil.validateError(result, err++, EXPECT_CHAR + FOUND_STRING, 24, 16);
        BAssertUtil.validateError(result, err++, EXPECT_CHAR + FOUND_STRING, 33, 17);
        BAssertUtil.validateError(result, err++, EXPECT_CHAR + FOUND_STRING, 34, 17);
        BAssertUtil.validateError(result, err++, EXPECT_CHAR + FOUND_STRING, 35, 17);
        BAssertUtil.validateError(result, err++, EXPECT_CHAR + FOUND_STRING, 39, 28);
        BAssertUtil.validateError(result, err++, EXPECT_CHAR + FOUND_STRING, 43, 19);
        BAssertUtil.validateError(result, err++, EXPECT_CHAR + FOUND_STRING, 48, 13);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'string:Char', found 'X'", 56, 21);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'string:Char[]', found 'X[]'", 59,
                23);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'string:Char[]', found 'Y[]'", 62,
                23);
        BAssertUtil.validateError(result, err++, "incompatible types: expected '(int:Unsigned8|string:Char)[]', " +
                "found 'Y[]'", 63, 39);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'string', " +
                "found 'StringFiniteType'", 74, 29);
        BAssertUtil.validateError(result, err++, "incompatible types: expected 'string', found 'StringType'", 75, 29);
        BAssertUtil.validateError(result, err++, "undefined function 'toLowerAscii' in " +
                "type 'StringFiniteType'", 77, 14);
        BAssertUtil.validateError(result, err++, "undefined function 'toLowerAscii' in type 'StringType'", 78, 14);
        Assert.assertEquals(result.getErrorCount(), err);
    }
}
