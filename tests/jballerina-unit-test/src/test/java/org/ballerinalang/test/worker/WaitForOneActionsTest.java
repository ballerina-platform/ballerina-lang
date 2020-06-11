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

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Wait for any action related tests.
 */
public class WaitForOneActionsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/wait-for-one-actions.bal");
        Assert.assertEquals(result.getErrorCount(), 0, "Wait for any actions test error count");
    }

    @Test
    public void waitTest1() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(vals[0].stringValue(), "7");
    }

    @Test
    public void waitTest2() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(vals[0].stringValue(), "true");
    }

    @Test
    public void waitTest3() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest3", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(vals[0].stringValue(), "hello foo");
    }

    @Test
    public void waitTest4() { // Need to check
        BValue[] vals = BRunUtil.invoke(result, "waitTest4", new BValue[0]);
        Assert.assertEquals(vals.length, 3);
        Assert.assertEquals(vals[0].stringValue(), "22");
        Assert.assertEquals(vals[1].stringValue(), "hello bar");
        Assert.assertEquals(vals[2].stringValue(), "hello xyz");
    }

    @Test
    public void waitTest5() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest5", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(vals[0].stringValue(), "{\"fname\":\"foo\", \"lname\":\"bar\"}");
    }

    @Test
    public void waitTest6() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest6", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(vals[0].stringValue(), "99");
    }

    @Test
    public void waitTest7() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest7", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(vals[0].stringValue(), "66");
    }

    @Test (expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: err from panic.*")
    public void waitTest8() {
        BRunUtil.invoke(result, "waitTest8");
    }

    @Test
    public void waitTest9() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest9", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertNull(vals[0]);
    }

    @Test
    public void asyncObjectCreationTest() {
        BRunUtil.invoke(result, "asyncObjectCreationTest", new BValue[0]);
    }
}
