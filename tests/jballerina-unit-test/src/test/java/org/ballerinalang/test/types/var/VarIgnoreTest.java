/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package org.ballerinalang.test.types.var;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of variable ignoring.
 */
public class VarIgnoreTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/var/var-ignore.bal");
    }

    @Test(description = "Test long value assignment")
    public void testIntegerValue() {
        Object returns = BRunUtil.invoke(result, "m", new Object[]{});
        Assert.assertEquals(returns, 3L);
    }

    @Test(description = "Test var ignore negative cases.")
    public void testVarIgnoreNegative() {
        CompileResult res = BCompileUtil.compile("test-src/types/var/var-ignore-negative.bal");
        int i = 0;
        BAssertUtil.validateError(res, i++, "a wildcard binding pattern can be used only "
                + "with a value that belong to type 'any'", 1, 1);
        BAssertUtil.validateError(res, i++, "incompatible types: expected 'error', found 'string'", 2, 11);
        BAssertUtil.validateError(res, i++, "a wildcard binding pattern can be used only "
                + "with a value that belong to type 'any'", 3, 1);
        BAssertUtil.validateError(res, i++, "incompatible types: expected 'string', found 'int'", 7, 16);
        BAssertUtil.validateError(res, i++, "a wildcard binding pattern can be used only "
                + "with a value that belong to type 'any'", 9, 5);
        BAssertUtil.validateError(res, i++, "a wildcard binding pattern can be used only "
                + "with a value that belong to type 'any'", 10, 5);
        BAssertUtil.validateError(res, i++, "incompatible types: expected 'error', found 'string'", 11, 15);
        Assert.assertEquals(res.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
