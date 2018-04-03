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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml;

import org.ballerinalang.toml.util.SingletonStack;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class to test the singleton stack used to handle the keys (properties) in the toml file.
 */
public class SingletonStackTest {
    @Test(description = "The value in the stack should be retrieved/popped out if a value is present")
    public void testPopWithValue() {
        SingletonStack singletonStackTest = new SingletonStack();
        singletonStackTest.push("hello");
        Assert.assertEquals(singletonStackTest.pop(), "hello");
    }

    @Test(description = "The value should be returned as null if its not instantiated")
    public void testPopWithoutInstaniating() {
        SingletonStack singletonStackTest = null;
        Assert.assertEquals(singletonStackTest, null);
    }

    @Test(description = "The value should not be pushed to the stack if the stack already contains a value it should " +
            "give an exception", expectedExceptions = IllegalStateException.class)
    public void testPopWithoutValue() {
        SingletonStack singletonStackTest = new SingletonStack();
        singletonStackTest.pop();
        Assert.assertEquals(singletonStackTest.present(), true);
    }

    @Test(description = "Return true if the value exists")
    public void testWithValuePresent() {
        SingletonStack singletonStackTest = new SingletonStack();
        singletonStackTest.push("hello");
        Assert.assertEquals(singletonStackTest.present(), true);
    }

    @Test(description = "Return false if a value does not exists")
    public void testWithValueAbsent() {
        SingletonStack singletonStackTest = new SingletonStack();
        Assert.assertEquals(singletonStackTest.present(), false);
    }

    @Test(description = "The value should be pushed to the stack if the stack is not null")
    public void testPushToStackWithValue() {
        SingletonStack singletonStackTest = new SingletonStack();
        singletonStackTest.push("hello");
        Assert.assertEquals(singletonStackTest.present(), true);
        Assert.assertEquals(singletonStackTest.pop(), "hello");
    }

    @Test(description = "The value should not be pushed to the stack if the stack already contains a value it should " +
            "give an exception", expectedExceptions = IllegalStateException.class)
    public void testPushMultipleValuesToStack() {
        SingletonStack singletonStackTest = new SingletonStack();
        singletonStackTest.push("hello");
        singletonStackTest.push("world");
        Assert.assertEquals(singletonStackTest.present(), true);
    }
}
