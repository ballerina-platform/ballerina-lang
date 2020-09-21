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

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Wait for any action related tests.
 */
public class WaitForAnyActionsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/workers/wait-for-any-actions.bal");
        Assert.assertEquals(result.getErrorCount(), 0, "Wait for any actions test error count");
    }

    @Test
    public void waitTest1() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        // TODO: 11/21/18 Since we are dealing with threads we can't ensure which will be returned first, so atm we
        // check if either values are returned. Need to fix this in a proper way
        Assert.assertTrue(Arrays.asList("7", "22").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest2() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest2", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("82", "22", "2").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest3() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest3", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("hello foo", "hello bar").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest4() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest4", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("30", "hello bar", "true").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest5() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest5", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("{\"line1\":\"No. 20\", \"line2\":\"Palm Grove\", \"city\":\"Colombo 03\"}",
                                        "{\"fname\": \"foo\", \"lname\": \"bar\"}").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest6() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest6", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("176", "150", "hello foo").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest7() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest7", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("4", "22", "true", "hello foo").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest8() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest8", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("hello xyz", "28").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest9() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest9", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("hello xyz", "99").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest10() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest10", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("77", "hello foo", "hello bar", "hello xyz").contains(vals[0].stringValue()));
    }

    @Test()
    public void waitTest11() {
        // in this case it returns result of wait f1|f2|f3; where f1 and f2 panics. So the panic also one of
        // possible results here
        try {
            BValue[] vals = BRunUtil.invoke(result, "waitTest11", new BValue[0]);
            Assert.assertEquals(vals.length, 1);
            Assert.assertEquals(vals[0].stringValue(), "hello foo");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("err from panic"));
        }
    }

    @Test (expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: err from panic.*")
    public void waitTest12() {
        BRunUtil.invoke(result, "waitTest12", new BValue[0]);
    }

    @Test
    public void waitTest13() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest13", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals("0", vals[0].stringValue());
    }

    @Test
    public void waitTest14() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest14", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("150", "7").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest15() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest15", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("150", "7", "60", "299").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest16() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest16", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("150", "hello foo", "7", "60", "12", "hello bar").
                contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest17() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest17", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(Arrays.asList("10", "20", "30").contains(vals[0].stringValue()));
    }

    @Test
    public void waitTest18() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest18", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals("65", vals[0].stringValue());
    }

    @Test
    public void waitTest19() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest19", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals("err returned {}", vals[0].stringValue());
    }

    @Test
    public void waitTest20() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest20", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals("hello moo", vals[0].stringValue());
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: A hazardous error occurred!!! Panic!!.*")
    public void waitTest21() {
        BRunUtil.invoke(result, "waitTest21", new BValue[0]);
    }

    @Test
    public void waitTest22() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest22", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals("hello foo", vals[0].stringValue());
    }

    @Test
    public void waitTest23() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest23", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertNull(vals[0]);
    }

    @Test
    public void waitTest24() {
        BValue[] vals = BRunUtil.invoke(result, "waitTest24", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertTrue(vals[0] instanceof BError);
        Assert.assertEquals(((BError) vals[0]).getReason(), "A hazardous error occurred!!! Abort immediately!!");
    }
}
