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

package org.ballerinalang.test.taintchecking;

import io.ballerina.runtime.api.StringUtils;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test taint checker for function calls.
 *
 * @since slp2
 */
public class FunctionCallTest {

    private static final String TAINTED_VALUE_FOR_B = "tainted value passed to untainted parameter 'b'";
    private static final String TAINTED_VALUE_FOR_I = "tainted value passed to untainted parameter 'i'";
    private static final String TAINTED_VALUE_FOR_S = "tainted value passed to untainted parameter 's'";

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/taintchecking/expressions/func_call.bal");
    }

    @Test
    public void testFuncCall() {
        BRunUtil.invoke(result, "main", new Object[]{1L, StringUtils.fromString("str"), true, false});
    }

    @Test(groups = "disableOnOldParser")
    public void testFuncCallNegative() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/taintchecking/expressions/func_call_negative.bal");
        int i = 0;

        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_I, 18, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_S, 18, 12);

        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_I, 20, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_S, 20, 12);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_B, 20, 15);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_B, 20, 19);

        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_I, 23, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_S, 23, 12);
        // https://github.com/ballerina-platform/ballerina-lang/issues/23307#issuecomment-647352681
        // BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_B, 23, 19);

        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_I, 25, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_S, 25, 12);
        // https://github.com/ballerina-platform/ballerina-lang/issues/23307#issuecomment-647352681
        // BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_B, 25, 15);
        // BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_B, 25, 19);

        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_I, 28, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_B, 28, 12);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_S, 28, 12);

        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_B, 31, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_I, 31, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_S, 31, 9);

        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_B, 34, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_I, 34, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_S, 34, 9);

        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_B, 37, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_I, 37, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_S, 37, 9);

        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_I, 38, 9);
        BAssertUtil.validateError(negativeResult, i++, TAINTED_VALUE_FOR_S, 38, 9);

        Assert.assertEquals(i, negativeResult.getDiagnostics().length);
    }

}
