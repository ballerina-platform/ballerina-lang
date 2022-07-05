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

import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object vals = BRunUtil.invoke(result, "waitTest1", new Object[0]);

        // TODO: 11/21/18 Since we are dealing with threads we can't ensure which will be returned first, so atm we
        // check if either values are returned. Need to fix this in a proper way
        Assert.assertTrue(Arrays.asList("7", "22").contains(vals.toString()));
    }

    @Test
    public void waitTest2() {
        Object vals = BRunUtil.invoke(result, "waitTest2", new Object[0]);

        Assert.assertTrue(Arrays.asList("82", "22", "2").contains(vals.toString()));
    }

    @Test
    public void waitTest3() {
        Object vals = BRunUtil.invoke(result, "waitTest3", new Object[0]);

        Assert.assertTrue(Arrays.asList("hello foo", "hello bar").contains(vals.toString()));
    }

    @Test
    public void waitTest4() {
        Object vals = BRunUtil.invoke(result, "waitTest4", new Object[0]);

        Assert.assertTrue(Arrays.asList("30", "hello bar", "true").contains(vals.toString()));
    }

    @Test
    public void waitTest5() {
        BMap vals = (BMap) BRunUtil.invoke(result, "waitTest5", new Object[0]);
        Assert.assertTrue(Arrays.asList("{\"line1\":\"No. 20\",\"line2\":\"Palm Grove\",\"city\":\"Colombo 03\"}",
                "{\"fname\":\"foo\",\"lname\":\"bar\"}").contains(vals.toString()));
    }

    @Test
    public void waitTest6() {
        Object vals = BRunUtil.invoke(result, "waitTest6", new Object[0]);

        Assert.assertTrue(Arrays.asList("176", "150", "hello foo").contains(vals.toString()));
    }

    @Test
    public void waitTest7() {
        Object vals = BRunUtil.invoke(result, "waitTest7", new Object[0]);

        Assert.assertTrue(Arrays.asList("4", "22", "true", "hello foo").contains(vals.toString()));
    }

    @Test
    public void waitTest8() {
        Object vals = BRunUtil.invoke(result, "waitTest8", new Object[0]);

        Assert.assertTrue(Arrays.asList("hello xyz", "28").contains(vals.toString()));
    }

    @Test
    public void waitTest9() {
        Object vals = BRunUtil.invoke(result, "waitTest9", new Object[0]);

        Assert.assertTrue(Arrays.asList("hello xyz", "99").contains(vals.toString()));
    }

    @Test
    public void waitTest10() {
        Object vals = BRunUtil.invoke(result, "waitTest10", new Object[0]);

        Assert.assertTrue(Arrays.asList("77", "hello foo", "hello bar", "hello xyz").contains(vals.toString()));
    }

    @Test()
    public void waitTest11() {
        // in this case it returns result of wait f1|f2|f3; where f1 and f2 panics. So the panic also one of
        // possible results here
        try {
            Object vals = BRunUtil.invoke(result, "waitTest11", new Object[0]);

            Assert.assertEquals(vals.toString(), "hello foo");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("err from panic"));
        }
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: err from panic.*")
    public void waitTest12() {
        BRunUtil.invoke(result, "waitTest12", new Object[0]);
    }

    @Test
    public void waitTest13() {
        Object vals = BRunUtil.invoke(result, "waitTest13", new Object[0]);

        Assert.assertEquals("0", vals.toString());
    }

    @Test
    public void waitTest14() {
        Object vals = BRunUtil.invoke(result, "waitTest14", new Object[0]);

        Assert.assertTrue(Arrays.asList("150", "7").contains(vals.toString()));
    }

    @Test
    public void waitTest15() {
        Object vals = BRunUtil.invoke(result, "waitTest15", new Object[0]);

        Assert.assertTrue(Arrays.asList("150", "7", "60", "299").contains(vals.toString()));
    }

    @Test
    public void waitTest16() {
        Object vals = BRunUtil.invoke(result, "waitTest16", new Object[0]);

        Assert.assertTrue(Arrays.asList("150", "hello foo", "7", "60", "12", "hello bar").
                contains(vals.toString()));
    }

    @Test
    public void waitTest17() {
        Object vals = BRunUtil.invoke(result, "waitTest17", new Object[0]);

        Assert.assertTrue(Arrays.asList("10", "20", "30").contains(vals.toString()));
    }

    @Test
    public void waitTest18() {
        Object vals = BRunUtil.invoke(result, "waitTest18", new Object[0]);

        Assert.assertEquals("65", vals.toString());
    }

    @Test
    public void waitTest19() {
        BRunUtil.invoke(result, "waitTest19", new Object[0]);
    }

    @Test
    public void waitTest20() {
        Object vals = BRunUtil.invoke(result, "waitTest20", new Object[0]);

        Assert.assertEquals("hello moo", vals.toString());
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: A hazardous error occurred!!! Panic!!.*")
    public void waitTest21() {
        BRunUtil.invoke(result, "waitTest21", new Object[0]);
    }

    @Test
    public void waitTest22() {
        Object vals = BRunUtil.invoke(result, "waitTest22", new Object[0]);

        Assert.assertEquals("hello foo", vals.toString());
    }

    @Test
    public void waitTest23() {
        Object vals = BRunUtil.invoke(result, "waitTest23", new Object[0]);

        Assert.assertNull(vals);
    }

    @Test
    public void waitTest24() {
        BRunUtil.invoke(result, "waitTest24", new Object[0]);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
