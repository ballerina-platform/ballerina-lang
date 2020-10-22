/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.test;

import org.ballerinalang.core.model.types.BMapType;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * This class tests the map lang module functionality.
 *
 * @since 1.0
 */
public class LangLibMapTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/maplib_test.bal");
    }

    @Test
    public void testLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLength");
        assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test
    public void testGet() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGet", new BValue[]{new BString("lk")});
        assertEquals(returns[0].stringValue(), "Sri Lanka");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*error: \\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot find key 'NonExistent'\"\\}.*")
    public void testGetNonExistentKey() {
        BRunUtil.invoke(compileResult, "testGet", new BValue[]{new BString("NonExistent")});
    }

    @Test
    public void testEntries() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEntries");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns[0];
        assertEquals(((BMapType) map.getType()).getConstrainedType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(map.size(), 3);
        assertEquals(map.get("lk").stringValue(), "[\"lk\", \"Sri Lanka\"]");
        assertEquals(map.get("us").stringValue(), "[\"us\", \"USA\"]");
        assertEquals(map.get("uk").stringValue(), "[\"uk\", \"United Kingdom\"]");
    }

    @Test
    public void testRemove() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemove", new BValue[]{new BString("uk")});
        assertEquals(returns[0].stringValue(), "United Kingdom");
        assertEquals(returns[1].getType().getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns[1];
        assertEquals(map.size(), 2);
        assertEquals(map.get("lk").stringValue(), "Sri Lanka");
        assertEquals(map.get("us").stringValue(), "USA");
    }

    @Test
    public void testRemoveIfHasKey() {
        BRunUtil.invoke(compileResult, "testRemoveIfHasKey");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*error: \\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot find key 'NonExistent'\"\\}.*")
    public void testRemoveNonExistentKey() {
        BRunUtil.invoke(compileResult, "testRemove", new BValue[]{new BString("NonExistent")});
    }

    @Test
    public void testRemoveAll() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveAll");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);
        assertEquals(returns[0].stringValue(), "{}");
        assertEquals(returns[0].size(), 0);
    }

    @Test(dataProvider = "mapKeyProvider")
    public void testHasKey(BString key, boolean expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasKey", new BValue[]{key});
        assertEquals(((BBoolean) returns[0]).booleanValue(), expected);
    }

    @Test
    public void testKeys() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testKeys");
        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);

        BValueArray arr = (BValueArray) returns[0];
        assertEquals(arr.elementType.getTag(), TypeTags.STRING_TAG);
        assertEquals(arr.size(), 3);

        List<String> keys = Arrays.asList(arr.getStringArray());
        assertTrue(keys.contains("lk"));
        assertTrue(keys.contains("us"));
        assertTrue(keys.contains("uk"));
    }

    @Test
    public void testMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMap");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns[0];
        assertEquals(((BMapType) map.getType()).getConstrainedType().getTag(), TypeTags.FLOAT_TAG);
        assertEquals(map.size(), 3);
        assertEquals(((BFloat) map.get("1")).floatValue(), 5.5);
        assertEquals(((BFloat) map.get("2")).floatValue(), 11.0);
        assertEquals(((BFloat) map.get("3")).floatValue(), 16.5);
    }

    @Test
    public void testForEach() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForEach");
        assertEquals(returns[0].stringValue(), "Sri LankaUSAUnited Kingdom");
    }

    @Test
    public void testFilter() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFilter");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns[0];
        assertEquals(((BMapType) map.getType()).getConstrainedType().getTag(), TypeTags.DECIMAL_TAG);
        assertEquals(map.size(), 2);
        assertEquals(((BDecimal) map.get("1")).decimalValue(), new BigDecimal("12.34"));
        assertEquals(((BDecimal) map.get("4")).decimalValue(), new BigDecimal("21.2"));
    }

    @Test
    public void testReduce() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReduce");
        assertEquals(((BFloat) returns[0]).floatValue(), 80.5);
    }

    @Test
    public void testBasicToArray() {
        BRunUtil.invoke(compileResult, "testBasicToArray");
    }

    @Test
    public void testLargeMapToArray() {
        BRunUtil.invoke(compileResult, "testLargeMapToArray");
    }

    @Test
    public void testRecordToArray() {
        BRunUtil.invoke(compileResult, "testRecordToArray");
    }

    @Test
    public void testOpenRecordToArray() {
        BRunUtil.invoke(compileResult, "testOpenRecordToArray");
    }

    @Test
    public void testMapOfUnionToArray() {
        BRunUtil.invoke(compileResult, "testMapOfUnionToArray");
    }

    @Test
    public void testRecordWithSameTypeFieldsToArray() {
        BRunUtil.invoke(compileResult, "testRecordWithSameTypeFieldsToArray");
    }

    @Test
    public void testAsyncFpArgsWithMaps() {
        BValue[] results = BRunUtil.invoke(compileResult, "testAsyncFpArgsWithMaps");
        assertTrue(results[0] instanceof BInteger);
        assertTrue(results[1] instanceof BMap);
        assertEquals(((BInteger) results[0]).intValue(), 118);
        assertEquals(((BInteger) ((BMap) results[1]).get("b")).intValue(), 36);
        assertEquals(((BInteger) ((BMap) results[1]).get("c")).intValue(), 78);
    }

    @DataProvider(name = "mapKeyProvider")
    public Object[][] getMapKeys() {
        return new Object[][]{
                {new BString("lk"), true},
                {new BString("invalid"), false}
        };
    }
}
