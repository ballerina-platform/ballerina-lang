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
package org.ballerinalang.stdlib.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.stdlib.common.CommonTestUtils.printDiagnostics;

/**
 * Test class for cache package.
 */
public class CacheTest {

    private CompileResult compileResult;
    private static final Log log = LogFactory.getLog(CacheTest.class);

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/cache/cache-test.bal");
        printDiagnostics(compileResult, log);
    }

    @Test
    public void testCreateCache() {
        int expiryTime = 20000;
        int capacity = 10;
        float evictionFactor = 0.1f;
        BValue[] args = new BValue[3];
        args[0] = new BInteger(expiryTime);
        args[1] = new BInteger(capacity);
        args[2] = new BFloat(evictionFactor);
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateCache", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void testPut() {
        String key = "Ballerina";
        String value = "Rocks";
        BValue[] args = new BValue[2];
        args[0] = new BString(key);
        args[1] = new BString(value);
        BValue[] returns = BRunUtil.invoke(compileResult, "testPut", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testGettingExistingValue() {
        String key = "Ballerina";
        String value = "Rocks";
        BValue[] args = new BValue[2];
        args[0] = new BString(key);
        args[1] = new BString(value);
        BValue[] returns = BRunUtil.invoke(compileResult, "testGettingExistingValue", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "Rocks");
    }

    @Test
    public void testGettingNonExistingValue() {
        String key = "Ballerina";
        BValue[] args = new BValue[1];
        args[0] = new BString(key);
        BValue[] returns = BRunUtil.invoke(compileResult, "testGettingNonExistingValue", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNull(returns[0]);
    }

    @Test
    public void testRemove() {
        String key = "Ballerina";
        String value = "Rocks";
        BValue[] args = new BValue[2];
        args[0] = new BString(key);
        args[1] = new BString(value);
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemove", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void testCacheEviction1() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEviction1", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "C");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "D");
        Assert.assertEquals(((BValueArray) returns[0]).getString(2), "E");
        Assert.assertEquals(((BValueArray) returns[0]).getString(3), "F");
        Assert.assertEquals(((BValueArray) returns[0]).getString(4), "G");
        Assert.assertEquals(((BValueArray) returns[0]).getString(5), "H");
        Assert.assertEquals(((BValueArray) returns[0]).getString(6), "I");
        Assert.assertEquals(((BValueArray) returns[0]).getString(7), "J");
        Assert.assertEquals(((BValueArray) returns[0]).getString(8), "K");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9);
    }

    @Test
    public void testCacheEviction2() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEviction2", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "B");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "D");
        Assert.assertEquals(((BValueArray) returns[0]).getString(2), "E");
        Assert.assertEquals(((BValueArray) returns[0]).getString(3), "F");
        Assert.assertEquals(((BValueArray) returns[0]).getString(4), "G");
        Assert.assertEquals(((BValueArray) returns[0]).getString(5), "H");
        Assert.assertEquals(((BValueArray) returns[0]).getString(6), "I");
        Assert.assertEquals(((BValueArray) returns[0]).getString(7), "J");
        Assert.assertEquals(((BValueArray) returns[0]).getString(8), "K");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9);
    }

    @Test
    public void testCacheEviction3() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEviction3", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "A");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "B");
        Assert.assertEquals(((BValueArray) returns[0]).getString(2), "E");
        Assert.assertEquals(((BValueArray) returns[0]).getString(3), "F");
        Assert.assertEquals(((BValueArray) returns[0]).getString(4), "G");
        Assert.assertEquals(((BValueArray) returns[0]).getString(5), "H");
        Assert.assertEquals(((BValueArray) returns[0]).getString(6), "I");
        Assert.assertEquals(((BValueArray) returns[0]).getString(7), "J");
        Assert.assertEquals(((BValueArray) returns[0]).getString(8), "K");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9);
    }

    @Test
    public void testCacheEviction4() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEviction4", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "A");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "B");
        Assert.assertEquals(((BValueArray) returns[0]).getString(2), "C");
        Assert.assertEquals(((BValueArray) returns[0]).getString(3), "D");
        Assert.assertEquals(((BValueArray) returns[0]).getString(4), "F");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 5);
    }

    @Test
    public void testExpiredCacheAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExpiredCacheAccess");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateCacheWithZeroExpiryTime() {
        BRunUtil.invoke(compileResult, "testCreateCacheWithZeroExpiryTime");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateCacheWithNegativeExpiryTime() {
        BRunUtil.invoke(compileResult, "testCreateCacheWithNegativeExpiryTime");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateCacheWithZeroCapacity() {
        BRunUtil.invoke(compileResult, "testCreateCacheWithZeroCapacity");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateCacheWithNegativeCapacity() {
        BRunUtil.invoke(compileResult, "testCreateCacheWithNegativeCapacity");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateCacheWithZeroEvictionFactor() {
        BRunUtil.invoke(compileResult, "testCreateCacheWithZeroEvictionFactor");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateCacheWithInvalidEvictionFactor() {
        BRunUtil.invoke(compileResult, "testCreateCacheWithInvalidEvictionFactor");
    }
}
