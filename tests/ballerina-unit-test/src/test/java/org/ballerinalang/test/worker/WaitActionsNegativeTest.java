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
        Assert.assertEquals(result.getErrorCount(), 22, "Wait actions negative test error count");
    }

    @Test(description = "Test negative scenarios of worker actions")
    public void testNegativeWorkerActions() {
        int index = 0;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<string>', found 'future<int>'",
                                  57, 22);
        index++;
        BAssertUtil.validateError(result, index, "variable assignment is required", 58, 5);

        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<int>', found 'future<string>'",
                                  67, 31);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<int|boolean>', found 'future" +
                "<string>'", 68, 36);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<map<int|boolean>>', found " +
                "'future<int>'", 69, 37);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<map<int|boolean>>', found " +
                "'future<string>'", 69, 41);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<map<int|boolean>>', found " +
                "'future<boolean>'", 69, 44);
        index++;
        BAssertUtil.validateError(result, index, "operator '|' cannot be applied to type 'future'",
                                  70, 27);
        index++;
        BAssertUtil.validateError(result, index, "operator '|' not defined for 'future<int>' and 'future<string>'",
                                  71, 33);
        index++;
        BAssertUtil.validateError(result, index, "operator '|' cannot be applied to type 'future'",
                                  71, 33);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<future<int|string>>', found " +
                "'future<int>'", 72, 40);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<future<int|string>>', found " +
                "'future<string>'", 72, 43);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<int>', found 'future<string>'",
                                  82, 34);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<boolean|string>', found " +
                "'future<int>'", 83, 41);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<boolean|string>', found " +
                "'future<int>'", 83, 45);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<int>', found 'future<string>'",
                                  84, 51);
        index++;
        BAssertUtil.validateError(result, index, "missing non-defaultable required record field 'f3'",
                                  86, 45);
        index++;
        BAssertUtil.validateError(result, index, "missing non-defaultable required record field 'f2'",
                                  87, 25);
        index++;
        BAssertUtil.validateError(result, index, "invalid literal for type 'sealedRec'", 88, 26);
        index++;
        BAssertUtil.validateError(result, index, "invalid field name 'status' in type 'sealedRec'", 89, 26);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<int>', found 'future<string>'", 90, 55);
        index++;
        BAssertUtil.validateError(result, index, "incompatible types: expected 'future<string>', found 'future<int>'", 91, 54);

    }
}
