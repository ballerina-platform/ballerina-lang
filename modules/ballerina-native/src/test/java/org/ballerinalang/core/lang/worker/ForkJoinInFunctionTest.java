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

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ForkJoinInFunctionTest {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/fork-join-in-function.bal");
    }


    @Test(description = "Test Fork Join All")
    public void testForkJoinAll() {
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testForkJoinAll", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BArray);
        Assert.assertEquals(((BArray) returns[0]).size(), 2);
        Assert.assertTrue(((BArray) returns[0]).get(0) instanceof BMessage);
        Assert.assertTrue(((BArray) returns[0]).get(1) instanceof BMessage);
    }

    @Test(description = "Test Fork Join Any")
    public void testForkJoinAny() {
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testForkJoinAny", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BArray);
        Assert.assertEquals(((BArray) returns[0]).size(), 1);
        Assert.assertTrue(((BArray) returns[0]).get(0) instanceof BMessage);

    }

    @Test(description = "Test Fork Join All of specific")
    public void testForkJoinAllOfSpecific() {
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testForkJoinAllOfSpecific", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BArray);
        Assert.assertEquals(((BArray) returns[0]).size(), 2);
        Assert.assertTrue(((BArray) returns[0]).get(0) instanceof BMessage);
        Assert.assertTrue(((BArray) returns[0]).get(1) instanceof BMessage);
    }

    @Test(description = "Test Fork Join Any of specific")
    public void testForkJoinAnyOfSpecific() {
        BValue[] args = {new BMessage()};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testForkJoinAnyOfSpecific", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BArray);
        Assert.assertEquals(((BArray) returns[0]).size(), 1);
        Assert.assertTrue(((BArray) returns[0]).get(0) instanceof BMessage);
    }
}
