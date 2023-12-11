/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.regexp;

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
 * Class to test regular expression values.
 *
 * @since 2201.3.0
 */
public class RegExpValueTest {
    private CompileResult compileResult;

    private static final String START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE =
            "start char code is greater than end char code";
    private static final String QUANTIFIER_MIN_GREATER_THAN_MAX =
            "quantifier minimum is greater than maximum";

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/regexp/regexp_value_test.bal");
    }

    @Test(dataProvider = "dataToTestRegExp")
    public void testRegExpValue(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestRegExp() {
        return new Object[]{
                "testRegExpValueWithLiterals",
                "testRegExpValueWithEscapes",
                "testRegExpValueWithCharacterClass",
                "testRegExpValueWithCharacterClass2",
                "testRegExpValueWithCapturingGroups",
                "testRegExpValueWithCapturingGroups2",
                "testRegExpValueWithCapturingGroups3",
                "testRegExpValueWithCapturingGroups4",
                "testRegExpValueWithCapturingGroups5",
                "testComplexRegExpValue",
                "testComplexRegExpValue2",
                "testEqualityWithRegExp",
                "testExactEqualityWithRegExp",
                "testInvalidInsertionsInRegExp",
                "testFreezeDirectWithRegExp"
        };
    }

    @Test
    public void testRegExpValueNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/types/regexp/regexp_value_negative_test.bal");
        int index = 0;
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 18, 28);
        validateError(negativeResult, index++, QUANTIFIER_MIN_GREATER_THAN_MAX, 19, 29);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 20, 28);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 21, 28);
        validateError(negativeResult, index++, "duplicate flag 'i'", 22, 29);
        validateError(negativeResult, index++, "duplicate flag 'm'", 23, 29);
        validateError(negativeResult, index++, "duplicate flag 'i'", 24, 29);
        validateError(negativeResult, index++, "duplicate flag 'i'", 25, 29);
        validateError(negativeResult, index++, "duplicate flag 's'", 26, 19);
        validateError(negativeResult, index++, "duplicate flag 's'", 27, 19);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 27, 26);
        validateError(negativeResult, index++, QUANTIFIER_MIN_GREATER_THAN_MAX, 27, 31);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 28, 18);
        validateError(negativeResult, index++, QUANTIFIER_MIN_GREATER_THAN_MAX, 28, 24);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 28, 32);
        validateError(negativeResult, index++, "duplicate flag 'm'", 28, 42);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 28, 49);
        validateError(negativeResult, index++, "duplicate flag 'x'", 28, 55);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 28, 61);
        validateError(negativeResult, index++, "duplicate flag 'x'", 28, 67);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 28, 75);
        validateError(negativeResult, index++, "invalid char after backslash", 29, 30);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 30, 37);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 31, 43);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 32, 14);
        validateError(negativeResult, index++, START_CHAR_CODE_GREATER_THAN_END_CHAR_CODE, 33, 21);
        validateError(negativeResult, index++, "empty character class disallowed", 34, 27);
        validateError(negativeResult, index++, "empty character class disallowed", 35, 37);
        validateError(negativeResult, index++, "empty character class disallowed", 36, 31);
        validateError(negativeResult, index++, "invalid token in regular expression", 37, 16);
        validateError(negativeResult, index++, "invalid token in regular expression", 38, 16);
        validateError(negativeResult, index++, "invalid token in regular expression", 39, 16);
        validateError(negativeResult, index++, "incompatible types: expected 'boolean', found " +
                "'regexp:RegExp'", 40, 9);
        validateError(negativeResult, index++, "missing backtick token", 42, 1);
        validateError(negativeResult, index++, "missing close brace token", 42, 1);
        validateError(negativeResult, index++, "missing colon token", 42, 1);
        validateError(negativeResult, index++, "missing expression", 42, 1);
        validateError(negativeResult, index++, "missing semicolon token", 42, 1);
        assertEquals(negativeResult.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
