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

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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

        BValue[] returns = BRunUtil.invoke(result, "errorTest");
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).getReason(), "error3");
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
                "error: error3 {\"message\":\"msg3\"}\n" + "\tat flush-workers:$lambda$_12(flush-workers.bal:187)\n" +
                "\t   flush-workers:$lambda$_12$lambda12$(flush-workers.bal:178)";
        Assert.assertEquals(expectedException.getMessage().trim(), result.trim());
    }

    @Test
    public void flushInDefaultError() {

        BValue[] returns = BRunUtil.invoke(result, "flushInDefaultError");
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertEquals(((BError) returns[0]).getReason(), "err");
    }

    @Test
    public void flushInDefault() {

        BValue[] returns = BRunUtil.invoke(result, "flushInDefault");
        Assert.assertEquals(returns[0].stringValue(), "25");
    }
}
