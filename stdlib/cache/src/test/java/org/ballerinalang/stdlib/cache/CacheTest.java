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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.stdlib.common.CommonTestUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for cache package.
 */
public class CacheTest {

    private CompileResult compileResult;
    private static final Log log = LogFactory.getLog(CacheTest.class);

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compileOffline("test-src/cache-test.bal");
        CommonTestUtils.printDiagnostics(compileResult, log);
    }

    @Test
    public void testCreateCache() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateCache");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void testPutNewEntry() {
        String key = "Hello";
        String value = "Ballerina";
        BValue[] args = new BValue[2];
        args[0] = new BString(key);
        args[1] = new BString(value);
        BValue[] returns = BRunUtil.invoke(compileResult, "testPutNewEntry", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testPutExistingEntry() {
        String key = "Hello";
        String value = "Ballerina";
        BValue[] args = new BValue[2];
        args[0] = new BString(key);
        args[1] = new BString(value);
        BValue[] returns = BRunUtil.invoke(compileResult, "testPutExistingEntry", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), value);
    }

    @Test
    public void testPutWithMaxAge() {
        String key = "Hello";
        String value = "Ballerina";
        int maxAge = 5;
        BValue[] args = new BValue[3];
        args[0] = new BString(key);
        args[1] = new BString(value);
        args[2] = new BInteger(maxAge);
        BValue[] returns = BRunUtil.invoke(compileResult, "testPutWithMaxAge", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testGetExistingValue() {
        String key = "Hello";
        String value = "Ballerina";
        BValue[] args = new BValue[2];
        args[0] = new BString(key);
        args[1] = new BString(value);
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetExistingEntry", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testGetNonExistingValue() {
        String key = "Hello";
        BValue[] args = new BValue[1];
        args[0] = new BString(key);
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetNonExistingEntry", args);
        Assert.assertNull(returns[0]);
    }

    @Test
    public void testGetExpiredValue() {
        String key = "Hello";
        String value = "Ballerina";
        BValue[] args = new BValue[2];
        args[0] = new BString(key);
        args[1] = new BString(value);
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetExpiredEntry", args);
        Assert.assertNull(returns[0]);
    }

    @Test
    public void testRemove() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemove");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void testRemoveAll() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveAll");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void testHasKey() {
        String key = "Hello";
        String value = "Ballerina";
        BValue[] args = new BValue[2];
        args[0] = new BString(key);
        args[1] = new BString(value);
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasKey", args);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testKeys() {
        String key1 = "Hello";
        String value1 = "Ballerina";
        String key2 = "Ballerina";
        String value2 = "Language";
        BValue[] args = new BValue[4];
        args[0] = new BString(key1);
        args[1] = new BString(value1);
        args[2] = new BString(key2);
        args[3] = new BString(value2);
        BValue[] returns = BRunUtil.invoke(compileResult, "testKeys", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        String[] expected = new String[]{"Hello", "Ballerina"};
        String[] actual = removeEmptyValues(((BValueArray) returns[0]).getStringArray());
        Assert.assertTrue(Arrays.equals(actual, expected));
    }

    @Test
    public void testCapacity() {
        int capacity = 10;
        BValue[] args = new BValue[1];
        args[0] = new BInteger(capacity);
        BValue[] returns = BRunUtil.invoke(compileResult, "testCapacity", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), capacity);
    }

    @Test
    public void testSize() {
        String key1 = "Hello";
        String value1 = "Ballerina";
        String key2 = "Ballerina";
        String value2 = "Language";
        BValue[] args = new BValue[4];
        args[0] = new BString(key1);
        args[1] = new BString(value1);
        args[2] = new BString(key2);
        args[3] = new BString(value2);
        BValue[] returns = BRunUtil.invoke(compileResult, "testSize", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), args.length / 2);
    }

    @Test
    public void testCacheEvictionWithCapacity1() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEvictionWithCapacity1", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BInteger);
        String[] expected = new String[]{"C", "D", "E", "F", "G", "H", "I", "J", "K"};
        String[] actual = removeEmptyValues(((BValueArray) returns[0]).getStringArray());
        Assert.assertTrue(Arrays.equals(actual, expected));
        Assert.assertEquals(((BInteger) returns[1]).intValue(), expected.length);
    }

    @Test
    public void testCacheEvictionWithCapacity2() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEvictionWithCapacity2", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BInteger);
        String[] expected = new String[]{"A", "D", "E", "F", "G", "H", "I", "J", "K"};
        String[] actual = removeEmptyValues(((BValueArray) returns[0]).getStringArray());
        Assert.assertTrue(Arrays.equals(actual, expected));
        Assert.assertEquals(((BInteger) returns[1]).intValue(), expected.length);
    }

    @Test
    public void testCacheEvictionWithTimer1() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEvictionWithTimer1", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BInteger);
        String[] expected = new String[]{};
        String[] actual = removeEmptyValues(((BValueArray) returns[0]).getStringArray());
        Assert.assertTrue(Arrays.equals(actual, expected));
        Assert.assertEquals(((BInteger) returns[1]).intValue(), expected.length);
    }

    @Test
    public void testCacheEvictionWithTimer2() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(compileResult, "testCacheEvictionWithTimer2", args);
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BInteger);
        String[] expected = new String[]{"B"};
        String[] actual = removeEmptyValues(((BValueArray) returns[0]).getStringArray());
        Assert.assertTrue(Arrays.equals(actual, expected));
        Assert.assertEquals(((BInteger) returns[1]).intValue(), expected.length);
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
    public void testCreateCacheWithNegativeEvictionFactor() {
        BRunUtil.invoke(compileResult, "testCreateCacheWithNegativeEvictionFactor");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateCacheWithInvalidEvictionFactor() {
        BRunUtil.invoke(compileResult, "testCreateCacheWithInvalidEvictionFactor");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateCacheWithZeroDefaultMaxAge() {
        BRunUtil.invoke(compileResult, "testCreateCacheWithZeroDefaultMaxAge");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testCreateCacheWithNegativeDefaultMaxAge() {
        BRunUtil.invoke(compileResult, "testCreateCacheWithNegativeDefaultMaxAge");
    }

    private String[] removeEmptyValues(String[] arr) {
        List<String> list = new ArrayList<>(Arrays.asList(arr));
        list.removeAll(Arrays.asList("", null));
        return list.toArray(new String[0]);
    }
}
