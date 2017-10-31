/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.structs;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Testing negaive test cases.
 */
public class PrivateFieldNegativeTest {

    @Test
    public void testPrivateInMiddle() {
        CompileResult result = BCompileUtil.compile("test-src/structs/fields/private-order-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "private fields must be defined after the public fields", 3, 5);
        BAssertUtil.validateError(result, 1, "private fields must be defined after the public fields", 6, 5);
        BAssertUtil.validateError(result, 2, "private fields must be defined after the public fields", 17, 5);
        BAssertUtil.validateError(result, 3, "private fields must be defined after the public fields", 27, 9);
    }

    @Test
    public void testPrivateAccess() {
        CompileResult result = BCompileUtil.compile(this, "test-src/structs/fields", "negative");
        Assert.assertEquals(result.getErrorCount(), 4);
        BAssertUtil.validateError(result, 0, "attempt to refer to non-public symbol 'aInt'", 6, 28);
        BAssertUtil.validateError(result, 1, "attempt to refer to non-public symbol 'aChild'", 6, 37);
        BAssertUtil.validateError(result, 2, "attempt to refer to non-public symbol 'bValue'", 7, 63);
        BAssertUtil.validateError(result, 3, "attempt to refer to non-public symbol 'privateStruct'", 7, 76);
    }

    @Test
    public void testCasting() {
        CompileResult result = BCompileUtil.compile(this, "test-src/structs/fields", "testequalneg");
        Assert.assertEquals(result.getErrorCount(), 7);
        BAssertUtil.validateError(result, 0, "unsafe cast from 'person:Person' to 'abc:ABCEmployee', use multi-return" +
                " cast expression", 13, 16);
        BAssertUtil.validateError(result, 1, "unsafe cast from 'any' to 'person:Person', use multi-return cast " +
                "expression", 22, 13);
        BAssertUtil.validateError(result, 2, "unsafe cast from 'person:Person' to 'abc:ABCEmployee', use multi-return" +
                " cast expression", 31, 15);
        BAssertUtil.validateError(result, 3, "unsafe cast from 'person:Person' to 'fake:FakeEmployee', use " +
                "multi-return cast expression", 37, 15);
        BAssertUtil.validateError(result, 4, "unsafe cast from 'abc:ABCEmployee' to 'fake:FakeEmployee', use " +
                "multi-return cast expression", 44, 16);
        BAssertUtil.validateError(result, 5, "unsafe cast from 'abc:ABCEmployee' to 'xyz:XYZEmployee', use " +
                "multi-return cast expression", 51, 16);
        BAssertUtil.validateError(result, 6, "unsafe cast from 'xyz:XYZEmployee' to 'abc:ABCEmployee', use " +
                "multi-return cast expression", 58, 16);
    }
}
