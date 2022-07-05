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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;
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

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }

    @Test
    public void testLength() {
        Object returns = BRunUtil.invoke(compileResult, "testLength");
        assertEquals(returns, 3L);
    }

    @Test
    public void testGet() {
        Object returns = BRunUtil.invoke(compileResult, "testGet", new Object[]{StringUtils.fromString("lk")});
        assertEquals(returns.toString(), "Sri Lanka");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*error: \\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot find key 'NonExistent'\"\\}.*")
    public void testGetNonExistentKey() {
        BRunUtil.invoke(compileResult, "testGet", new Object[]{StringUtils.fromString("NonExistent")});
    }

    @Test
    public void testEntries() {
        Object returns = BRunUtil.invoke(compileResult, "testEntries");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns;
        assertEquals(((BMapType) map.getType()).getConstrainedType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(map.size(), 3);
        assertEquals(map.get(StringUtils.fromString("lk")).toString(), "[\"lk\",\"Sri Lanka\"]");
        assertEquals(map.get(StringUtils.fromString("us")).toString(), "[\"us\",\"USA\"]");
        assertEquals(map.get(StringUtils.fromString("uk")).toString(), "[\"uk\",\"United Kingdom\"]");
    }

    @Test
    public void testRemove() {
        Object returns = BRunUtil.invoke(compileResult, "testRemove", new Object[]{StringUtils.fromString("uk")});
        BArray result = (BArray) returns;
        assertEquals(result.get(0).toString(), "United Kingdom");
        assertEquals(getType(result.get(1)).getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) result.get(1);
        assertEquals(map.size(), 2);
        assertEquals(map.get(StringUtils.fromString("lk")).toString(), "Sri Lanka");
        assertEquals(map.get(StringUtils.fromString("us")).toString(), "USA");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*error: \\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot find key 'NonExistent'\"\\}.*")
    public void testRemoveNonExistentKey() {
        BRunUtil.invoke(compileResult, "testRemove", new Object[]{StringUtils.fromString("NonExistent")});
    }

    @Test
    public void testRemoveAll() {
        Object returns = BRunUtil.invoke(compileResult, "testRemoveAll");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);
        assertEquals(returns.toString(), "{}");
        assertEquals(((BMap) returns).size(), 0);
    }

    @Test(dataProvider = "mapKeyProvider")
    public void testHasKey(BString key, boolean expected) {
        Object returns = BRunUtil.invoke(compileResult, "testHasKey", new Object[]{key});
        assertEquals(returns, expected);
    }

    @Test
    public void testKeys() {
        Object returns = BRunUtil.invoke(compileResult, "testKeys");
        assertEquals(getType(returns).getTag(), TypeTags.ARRAY_TAG);

        BArray arr = (BArray) returns;
        assertEquals(((ArrayType) arr.getType()).getElementType().getTag(), TypeTags.STRING_TAG);
        assertEquals(arr.size(), 3);

        List<String> keys = Arrays.asList(arr.getStringArray());
        assertTrue(keys.contains("lk"));
        assertTrue(keys.contains("us"));
        assertTrue(keys.contains("uk"));
    }

    @Test
    public void testMap() {
        Object returns = BRunUtil.invoke(compileResult, "testMap");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns;
        assertEquals(((BMapType) map.getType()).getConstrainedType().getTag(), TypeTags.FLOAT_TAG);
        assertEquals(map.size(), 3);
        assertEquals(map.get(StringUtils.fromString("1")), 5.5d);
        assertEquals(map.get(StringUtils.fromString("2")), 11.0d);
        assertEquals(map.get(StringUtils.fromString("3")), 16.5d);
    }

    @Test
    public void testForEach() {
        Object returns = BRunUtil.invoke(compileResult, "testForEach");
        assertEquals(returns.toString(), "Sri LankaUSAUnited Kingdom");
    }

    @Test
    public void testFilter() {
        Object returns = BRunUtil.invoke(compileResult, "testFilter");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns;
        assertEquals(((BMapType) map.getType()).getConstrainedType().getTag(), TypeTags.DECIMAL_TAG);
        assertEquals(map.size(), 2);
        assertEquals(map.get(StringUtils.fromString("1")), ValueCreator.createDecimalValue("12.34"));
        assertEquals(map.get(StringUtils.fromString("4")), ValueCreator.createDecimalValue("21.2"));
    }

    @Test
    public void testReduce() {
        Object returns = BRunUtil.invoke(compileResult, "testReduce");
        assertEquals(returns, 80.5d);
    }

    @Test
    public void testAsyncFpArgsWithMaps() {
        Object results = BRunUtil.invoke(compileResult, "testAsyncFpArgsWithMaps");
        BArray arr = (BArray) results;
        assertTrue(arr.get(0) instanceof Long);
        assertTrue(arr.get(1) instanceof BMap);
        assertEquals(arr.get(0), 118L);
        assertEquals(((BMap) arr.get(1)).get(StringUtils.fromString("b")), 36L);
        assertEquals(((BMap) arr.get(1)).get(StringUtils.fromString("c")), 78L);
    }

    @DataProvider(name = "mapKeyProvider")
    public Object[][] getMapKeys() {
        return new Object[][]{
                {StringUtils.fromString("lk"), true},
                {StringUtils.fromString("invalid"), false}
        };
    }

    @Test(dataProvider = "FunctionList")
    public void testMapFunctions(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @DataProvider(name = "FunctionList")
    public Object[] testFunctions() {
        return new Object[]{
                "testRemoveIfHasKey",
                "testBasicToArray",
                "testLargeMapToArray",
                "testRecordToArray",
                "testOpenRecordToArray",
                "testMapOfUnionToArray",
                "testRecordWithSameTypeFieldsToArray",
                "testReadOnlyMapFilter"
        };
    }
}
