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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.error;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for Invalid error destructure binding pattern.
 *
 * @since Swan Lake
 */
public class ErrorDestructureNegetiveTest {

    @Test(description = "Test error destruturing binding pattern with error cause error binding pattern")
    public void testErrorDestructuring() {
        CompileResult destructuringResult = BCompileUtil.
                compile("test-src/error/error_destructure_binding_pattern_error_cause.bal");
        BAssertUtil.validateError(destructuringResult, 0,
                "incompatible types: expected 'error?', found 'error'", 20, 21);
        Assert.assertEquals(1, destructuringResult.getErrorCount());
    }

    @Test(description = "Negative test for error destruturing with error field binding patterns")
    public void testErrorDetailBindingNegative() {
        CompileResult resultNegative =
                BCompileUtil.compile("test-src/error/error_destructure_field_binding_negative.bal");
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 31, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 31, 56);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 32, 29);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid field binding pattern; can only bind required fields", 32, 65);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'msg'", 45, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'extra'", 45, 48);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'msg'", 46, 29);
        BAssertUtil.validateError(resultNegative, i++,
                "cannot bind undefined error detail field 'extra'", 46, 57);

        Assert.assertEquals(resultNegative.getErrorCount(), i);
    }
}
