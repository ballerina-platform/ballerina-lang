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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.test.worker;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests the flush action.
 */
public class WorkerFlushTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/flush-workers.bal");
        Assert.assertEquals(result.getErrorCount(), 0, result.toString());
    }

    @Test
    public void simpleFlushTest() {
        BValue[] returns = BRunUtil.invoke(result, "singleFlush");
        Assert.assertEquals(returns[0].stringValue(), "w2w2w2w2w2w1w1w1w1w1");
    }

    @Test
    public void flushReturnNilTest() {
        BValue[] returns = BRunUtil.invoke(result, "flushReturn");
        Assert.assertNull(returns[0]);
    }

    @Test
    public void flushAll() {
        BValue[] returns = BRunUtil.invoke(result, "flushAll");
        Assert.assertFalse(returns[0].stringValue().startsWith("w1"),
                "Returned wrong value:" + returns[0].stringValue());
    }

    @Test
    public void errorBeforeFlush() {
        BRunUtil.invoke(result, "errorTest");
    }

    @Test
    public void panicBeforeFlush() {
        Exception expectedException = null;
        try {
            BRunUtil.invoke(result, "panicTest");
        } catch (Exception e) {
            expectedException = e;
        }
        Assert.assertNotNull(expectedException);
        String result =
                "error: error3 {\"message\":\"msg3\"}\n" +
                        "\tat flush-workers:$lambda$_13(flush-workers.bal:193)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test
    public void flushInDefaultError() {
        BRunUtil.invoke(result, "flushInDefaultError");
    }

    @Test
    public void flushInDefault() {
        BValue[] returns = BRunUtil.invoke(result, "flushInDefault");
        Assert.assertEquals(returns[0].stringValue(), "25");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
