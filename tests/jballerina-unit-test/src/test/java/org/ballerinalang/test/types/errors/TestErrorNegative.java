/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.errors;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative test cases for error.
 */
public class TestErrorNegative {

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/errors/error-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 2);
        BAssertUtil.validateError(compileResult, 0, "cannot infer type of the error from '(error|string)'", 2, 22);
        BAssertUtil.validateError(compileResult, 1, "cannot infer type of the error from '(string|error)'", 3, 12);
    }
}
