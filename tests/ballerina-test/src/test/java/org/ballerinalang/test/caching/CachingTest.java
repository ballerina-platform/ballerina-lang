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
package org.ballerinalang.test.caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.Arrays;

/**
 * Test class for caching package.
 */
public class CachingTest {

    private CompileResult compileResult;
    private static final double DELTA = 0.0000000001;
    private static final Log log = LogFactory.getLog(CachingTest.class);

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/caching/caching-test.bal");
        printDiagnostics(compileResult);
    }

    private void printDiagnostics(CompileResult timerCompileResult) {
        Arrays.asList(timerCompileResult.getDiagnostics()).
                forEach(e -> log.info(e.getMessage() + " : " + e.getPosition()));
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
        Assert.assertTrue(returns.length == 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testGet() {
        String key = "Ballerina";
        String value = "Rocks";
        BValue[] args = new BValue[2];
        args[0] = new BString(key);
        args[1] = new BString(value);
        BValue[] returns = BRunUtil.invoke(compileResult, "testGet", args);
        Assert.assertTrue(returns.length == 2);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(returns[1].stringValue(), "Rocks");
    }

    @Test
    public void testRemove() {
        String key = "Ballerina";
        String value = "Rocks";
        BValue[] args = new BValue[2];
        args[0] = new BString(key);
        args[1] = new BString(value);
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemove", args);
        Assert.assertTrue(returns.length == 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void testCacheEviction1() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEviction1", args);
        Assert.assertTrue(returns.length == 2);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "C");
        Assert.assertEquals(((BStringArray) returns[0]).get(1), "D");
        Assert.assertEquals(((BStringArray) returns[0]).get(2), "E");
        Assert.assertEquals(((BStringArray) returns[0]).get(3), "F");
        Assert.assertEquals(((BStringArray) returns[0]).get(4), "G");
        Assert.assertEquals(((BStringArray) returns[0]).get(5), "H");
        Assert.assertEquals(((BStringArray) returns[0]).get(6), "I");
        Assert.assertEquals(((BStringArray) returns[0]).get(7), "J");
        Assert.assertEquals(((BStringArray) returns[0]).get(8), "K");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9);
    }

    @Test
    public void testCacheEviction2() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEviction2", args);
        Assert.assertTrue(returns.length == 2);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "B");
        Assert.assertEquals(((BStringArray) returns[0]).get(1), "D");
        Assert.assertEquals(((BStringArray) returns[0]).get(2), "E");
        Assert.assertEquals(((BStringArray) returns[0]).get(3), "F");
        Assert.assertEquals(((BStringArray) returns[0]).get(4), "G");
        Assert.assertEquals(((BStringArray) returns[0]).get(5), "H");
        Assert.assertEquals(((BStringArray) returns[0]).get(6), "I");
        Assert.assertEquals(((BStringArray) returns[0]).get(7), "J");
        Assert.assertEquals(((BStringArray) returns[0]).get(8), "K");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9);
    }

    @Test
    public void testCacheEviction3() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEviction3", args);
        Assert.assertTrue(returns.length == 2);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "A");
        Assert.assertEquals(((BStringArray) returns[0]).get(1), "B");
        Assert.assertEquals(((BStringArray) returns[0]).get(2), "E");
        Assert.assertEquals(((BStringArray) returns[0]).get(3), "F");
        Assert.assertEquals(((BStringArray) returns[0]).get(4), "G");
        Assert.assertEquals(((BStringArray) returns[0]).get(5), "H");
        Assert.assertEquals(((BStringArray) returns[0]).get(6), "I");
        Assert.assertEquals(((BStringArray) returns[0]).get(7), "J");
        Assert.assertEquals(((BStringArray) returns[0]).get(8), "K");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 9);
    }

    @Test
    public void testCacheEviction4() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEviction4", args);
        Assert.assertTrue(returns.length == 2);
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "A");
        Assert.assertEquals(((BStringArray) returns[0]).get(1), "B");
        Assert.assertEquals(((BStringArray) returns[0]).get(2), "C");
        Assert.assertEquals(((BStringArray) returns[0]).get(3), "D");
        Assert.assertEquals(((BStringArray) returns[0]).get(4), "F");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 5);
    }

    @Test
    public void testExpiredCacheAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testExpiredCacheAccess");
        Assert.assertTrue(returns.length == 1);
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
