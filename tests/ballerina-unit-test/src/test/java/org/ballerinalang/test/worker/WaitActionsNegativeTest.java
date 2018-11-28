/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.worker;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative wait action related tests.
 *
 * @since 0.985.0
 */
public class WaitActionsNegativeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/wait-actions-negative.bal");
        Assert.assertEquals(result.getErrorCount(), 19, "Wait actions negative test error count");
    }

    @Test(description = "Test negative scenarios of worker actions")
    public void testNegativeWorkerActions() {
        int index = 0;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<string>', found 'future<int>'",
                27, 22);
        index++;
        BAssertUtil.validateError(result, index, "variable assignment is required", 28, 5);

        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<int>', found 'future<string>'",
                47, 31);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<int|boolean>', found 'future" +
                "<string>'", 48, 36);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<map<int|boolean>>', found " +
                "'future<int>'", 49, 37);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<map<int|boolean>>', found " +
                "'future<string>'", 49, 41);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<map<int|boolean>>', found " +
                "'future<boolean>'", 49, 44);
        index++;
        BAssertUtil.validateError(result, index, "operator '|' cannot be applied to type 'future'",
                50, 29);
        index++;
        BAssertUtil.validateError(result, index, "operator '|' not defined for 'future<int>' and 'future<string>'",
                51, 33);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<future<int|string>>', found " +
                "'future<int>'", 52, 40);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<future<int|string>>', found " +
                "'future<string>'", 52, 43);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<int>', found 'future<string>'",
                76, 34);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<boolean|string>', found " +
                "'future<int>'", 77, 41);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<boolean|string>', found " +
                "'future<int>'", 77, 45);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<int>', found 'future<string>'",
                78, 51);
        index++;
        BAssertUtil.validateError(result, index, "invalid literal for type '$anonType$5'",
                79, 45);
        index++;
        BAssertUtil.validateError(result, index, "invalid field name 'f2' in type '$anonType$6'",
                80, 45);
        index++;
        BAssertUtil.validateError(result, index, "missing non-defaultable required record field 'f3'",
                80, 45);
        index++;
        BAssertUtil.validateError(result, index, "missing non-defaultable required record field 'f2'",
                81, 25);

    }
}
