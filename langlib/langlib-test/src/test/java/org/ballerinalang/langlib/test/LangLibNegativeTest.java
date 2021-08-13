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
package org.ballerinalang.langlib.test;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for lang lib methods.
 *
 * @since 1.1.1
 */
public class LangLibNegativeTest {

    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        negativeResult = BCompileUtil.compile("test-src/langlib_test_negative.bal");
    }

    @Test
    public void testLangLibNegative() {
        int err = 0;
        BAssertUtil.validateError(negativeResult, err++, "undefined function 'indexOf' in type 'map<string>'", 21, 47);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected '(float[]|error)', found " +
                "'(json|error)'", 36, 25);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected '(int|string|float[]|error)', " +
                "found '(json|error)'", 40, 37);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'any', found '(json|error)'",
                52, 14);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'any', found '(json|error)'",
                53, 14);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'any', found '(int|error)'",
                54, 14);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'any', found '(json|error)'",
                56, 14);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'any', found '(json|error)'",
                57, 14);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'any', found '(int|error)'",
                58, 14);
        BAssertUtil.validateError(negativeResult, err++, "undefined function 'toXML' in type 'int'",
                63, 19);;
        BAssertUtil.validateError(negativeResult, err++, "undefined function 'toHexString' in type '12|foo'", 78, 18);
        Assert.assertEquals(negativeResult.getErrorCount(), err);
    }
}
