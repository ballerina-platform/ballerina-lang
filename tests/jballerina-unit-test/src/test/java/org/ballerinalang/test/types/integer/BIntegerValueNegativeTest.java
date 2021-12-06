/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.integer;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for negative integer tests.
 */
@Test(groups = { "disableOnOldParser" })
public class BIntegerValueNegativeTest {

    @Test
    public void testIntegerValue() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/integer/integer-value-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 8);

        int index = 0;
        String expectedError = "'0xFFFFFFFFFFFFFFFF' is out of range";
        BAssertUtil.validateError(compileResult, index++, expectedError, 2, 13);

        expectedError = "'9999999999999999999' is out of range";
        BAssertUtil.validateError(compileResult, index++, expectedError, 3, 13);

        expectedError = "'-0xFFFFFFFFFFFFFFFF' is out of range";
        BAssertUtil.validateError(compileResult, index++, expectedError, 5, 13);

        expectedError = "'-9999999999999999999' is out of range";
        BAssertUtil.validateError(compileResult, index++, expectedError, 6, 13);

        expectedError = "missing semicolon token";
        BAssertUtil.validateError(compileResult, index++, expectedError, 12, 1);

        expectedError = "leading zeros in numeric literals";
        BAssertUtil.validateError(compileResult, index++, expectedError, 13, 13);

        expectedError = "leading zeros in numeric literals";
        BAssertUtil.validateError(compileResult, index++, expectedError, 14, 13);

        expectedError = "missing semicolon token";
        BAssertUtil.validateError(compileResult, index++, expectedError, 18, 1);
    }
}
