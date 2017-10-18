/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.nativeimpl.functions;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for caching package.
 */
public class CachingTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BTestUtils.compile("test-src/nativeimpl/functions/cachingTest.bal");
    }

    @Test
    public void testCreateCache() {
        String cacheName = "userCache";
        int timeout = 60;
        int capacity = 10;
        BValue[] args = new BValue[3];
        args[0] = new BString(cacheName);
        args[1] = new BInteger(timeout);
        args[2] = new BInteger(capacity);
        BValue[] returns = BTestUtils.invoke(compileResult, "testCreateCache", args);
        Assert.assertTrue(returns.length == 3);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertTrue(returns[1] instanceof BInteger);
        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(returns[0].stringValue(), cacheName);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), timeout);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), capacity);
    }

    @Test
    public void testPut() {
        String cacheName = "userCache";
        int timeout = 60;
        int capacity = 10;
        String key = "Ballerina";
        String value = "Rocks";
        BValue[] args = new BValue[5];
        args[0] = new BString(cacheName);
        args[1] = new BInteger(timeout);
        args[2] = new BInteger(capacity);
        args[3] = new BString(key);
        args[4] = new BString(value);
        BValue[] returns = BTestUtils.invoke(compileResult, "testPut", args);
        Assert.assertTrue(returns.length == 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testGet() {
        String cacheName = "userCache";
        int timeout = 60;
        int capacity = 10;
        String key = "Ballerina";
        String value = "Rocks";
        BValue[] args = new BValue[5];
        args[0] = new BString(cacheName);
        args[1] = new BInteger(timeout);
        args[2] = new BInteger(capacity);
        args[3] = new BString(key);
        args[4] = new BString(value);
        BValue[] returns = BTestUtils.invoke(compileResult, "testGet", args);
        Assert.assertTrue(returns.length == 2);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "Rocks");
    }

    @Test
    public void testRemove() {
        String cacheName = "userCache";
        int timeout = 60;
        int capacity = 10;
        String key = "Ballerina";
        String value = "Rocks";
        BValue[] args = new BValue[5];
        args[0] = new BString(cacheName);
        args[1] = new BInteger(timeout);
        args[2] = new BInteger(capacity);
        args[3] = new BString(key);
        args[4] = new BString(value);
        BValue[] returns = BTestUtils.invoke(compileResult, "testRemove", args);
        Assert.assertTrue(returns.length == 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void testClear() {
        String cacheName = "userCache";
        int timeout = 60;
        int capacity = 10;
        BValue[] args = new BValue[3];
        args[0] = new BString(cacheName);
        args[1] = new BInteger(timeout);
        args[2] = new BInteger(capacity);
        BValue[] returns = BTestUtils.invoke(compileResult, "testClear", args);
        Assert.assertTrue(returns.length == 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void testCacheEviction() {
        String cacheName = "userCache";
        int timeout = 60;
        int capacity = 2;
        BValue[] args = new BValue[3];
        args[0] = new BString(cacheName);
        args[1] = new BInteger(timeout);
        args[2] = new BInteger(capacity);
        BValue[] returns = BTestUtils.invoke(compileResult, "testCacheEviction", args);
        Assert.assertTrue(returns.length == 1);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "B");
        Assert.assertEquals(((BStringArray) returns[0]).get(1), "C");
    }
}
