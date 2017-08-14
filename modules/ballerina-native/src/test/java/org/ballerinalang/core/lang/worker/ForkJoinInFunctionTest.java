/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.lang.worker;

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for usages of fork-join in functions.
 */
public class ForkJoinInFunctionTest {
    private ProgramFile bProgramFile;

    @BeforeClass
    public void setup() {
        bProgramFile = BTestUtils.getProgramFile("samples/fork-join-in-all.bal");
    }


    @Test(description = "Test Fork Join All")
    public void testForkJoinAll() {
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testForkJoinAll", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        Assert.assertEquals(((BRefValueArray) returns[0]).size(), 2);
        Assert.assertTrue(((BRefValueArray) returns[0]).get(0) instanceof BMessage);
        Assert.assertTrue(((BRefValueArray) returns[0]).get(1) instanceof BMessage);
    }

    @Test(description = "Test Fork Join Any")
    public void testForkJoinAny() {
        bProgramFile = BTestUtils.getProgramFile("samples/fork-join-some.bal");
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testForkJoinAny", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
        Assert.assertEquals(((BRefValueArray) returns[0]).size(), 1);
        Assert.assertTrue(((BRefValueArray) returns[0]).get(0) instanceof BMessage);

    }

    @Test(description = "Test Fork Join All of specific")
    public void testForkJoinAllOfSpecific() {
        bProgramFile = BTestUtils.getProgramFile("samples/fork-join-all-specific.bal");
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testForkJoinAllOfSpecific", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        Assert.assertEquals(((BRefValueArray) returns[0]).size(), 2);
        Assert.assertTrue(((BRefValueArray) returns[0]).get(0) instanceof BMessage);
        Assert.assertTrue(((BRefValueArray) returns[0]).get(1) instanceof BMessage);
    }

    @Test(description = "Test Fork Join Any of specific")
    public void testForkJoinAnyOfSpecific() {
        bProgramFile = BTestUtils.getProgramFile("samples/fork-join-any-specific.bal");
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testForkJoinAnyOfSpecific", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BRefValueArray);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 5);
        Assert.assertEquals(((BRefValueArray) returns[0]).size(), 1);
        Assert.assertTrue(((BRefValueArray) returns[0]).get(0) instanceof BMessage);
    }

    @Test(description = "Test Fork Join Without Timeout Expression")
    public void testForkJoinWithoutTimeoutExpression() {
        bProgramFile = BTestUtils.getProgramFile("samples/fork-join-without-timeout.bal");
        BValue[] returns = BLangFunctions.invokeNew(bProgramFile, "testForkJoinWithoutTimeoutExpression");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
        Assert.assertTrue(returns[1] instanceof BFloat);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 1.23);
    }

    @Test(description = "Test Fork Join With Workers in same function")
    public void testForkJoinWithWorkersInSameFunction() {
        try {
            BTestUtils.getProgramFile("samples/fork-join-and workers-under-same-funtion.bal");
        } catch (Exception retEx) {
            Assert.fail("Parsing bal file should not throw exception at BLangModelBuilder.", retEx);
        }
    }
}
