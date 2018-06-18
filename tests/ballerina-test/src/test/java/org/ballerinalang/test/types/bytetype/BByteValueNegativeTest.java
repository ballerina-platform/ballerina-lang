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
package org.ballerinalang.test.types.bytetype;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This test class will test the byte value negative test cases.
 */
public class BByteValueNegativeTest {

    @Test(description = "Test blob value negative")
    public void testBlobValueNegative() {
        CompileResult result = BCompileUtil.compile("test-src/types/byte/byte-value-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 18);
        String msg1 = "incompatible types: expected 'byte', found 'int'";
        String msg2 = "incompatible types: expected 'byte', found 'float'";
        String msg3 = "incompatible types: expected 'byte', found 'string'";
        BAssertUtil.validateError(result, 0, msg1 , 2, 15);
        BAssertUtil.validateError(result, 1, msg1 , 3, 15);
        BAssertUtil.validateError(result, 2, msg1 , 4, 15);
        BAssertUtil.validateError(result, 3, msg1 , 5, 15);
        BAssertUtil.validateError(result, 4, msg2 , 6, 15);
        BAssertUtil.validateError(result, 5, msg2 , 7, 15);
        BAssertUtil.validateError(result, 6, msg2 , 8, 15);
        BAssertUtil.validateError(result, 7, msg1 , 9, 24);
        BAssertUtil.validateError(result, 8, msg1 , 10, 21);
        BAssertUtil.validateError(result, 9, msg2 , 11, 21);
        BAssertUtil.validateError(result, 10, msg1 , 11, 30);
        BAssertUtil.validateError(result, 11, msg1 , 11, 35);
        BAssertUtil.validateError(result, 12, msg2 , 12, 21);
        BAssertUtil.validateError(result, 13, msg2 , 12, 27);
        BAssertUtil.validateError(result, 14, msg1 , 12, 35);
        BAssertUtil.validateError(result, 15, msg1 , 15, 14);
        BAssertUtil.validateError(result, 16, msg2 , 18, 14);
        BAssertUtil.validateError(result, 17, msg3 , 21, 14);
    }
}
