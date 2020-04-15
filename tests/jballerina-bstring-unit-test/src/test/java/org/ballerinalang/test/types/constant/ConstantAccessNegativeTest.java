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

package org.ballerinalang.test.types.constant;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Constant access negative test cases.
 */
public class ConstantAccessNegativeTest {

    @Test
    public void accessPublicConstantFromOtherPackage() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/types/constant/access", "main2");
        Assert.assertEquals(compileResult.getErrorCount(), 5);
        BAssertUtil.validateError(compileResult, 0, "attempt to refer to non-accessible symbol 'address'", 5, 16);
        BAssertUtil.validateError(compileResult, 1, "undefined symbol 'address'", 5, 16);
        BAssertUtil.validateError(compileResult, 2, "cannot update constant value", 7, 5);
        BAssertUtil.validateError(compileResult, 3, "incompatible types: expected 'int', found 'string'", 9, 13);
        BAssertUtil.validateError(compileResult, 4, "incompatible types: expected 'C|D', found 'A'", 11, 13);
    }
}
