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
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BArrayType;
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for record type.
 */
public class LangLibRecordTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/recordlib_test.bal");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }

    @Test
    public void testLength() {
        Object returns = BRunUtil.invoke(compileResult, "testLength");
        assertEquals(returns, 2L);
    }

    @Test
    public void testGet() {
        Object returns = BRunUtil.invoke(compileResult, "testGet", new Object[]{StringUtils.fromString("name")});
        assertEquals(returns.toString(), "John Doe");

        returns = BRunUtil.invoke(compileResult, "testGet", new Object[]{StringUtils.fromString("age")});
        assertEquals(returns, 25L);
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
        assertEquals(map.size(), 2);
        assertEquals(map.get(StringUtils.fromString("name")).toString(), "[\"name\",\"John Doe\"]");
        assertEquals(map.get(StringUtils.fromString("age")).toString(), "[\"age\",25]");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*OperationNotSupported \\{\"message\":\"failed .*")
    public void testRemove() {
        BRunUtil.invoke(compileResult, "testRemove", new Object[]{StringUtils.fromString("name")});
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*OperationNotSupported \\{\"message\":\"failed .*")
    public void testRemoveAll() {
        BRunUtil.invoke(compileResult, "testRemoveAll");
    }

    @Test(dataProvider = "mapKeyProvider")
    public void testHasKey(BString key, boolean expected) {
        Object returns = BRunUtil.invoke(compileResult, "testHasKey", new Object[]{key});
        assertEquals(returns, expected);
    }

    @Test
    public void testHasKey2() {
        Object returns = BRunUtil.invoke(compileResult, "testHasKey2");
        BArray arr = (BArray) returns;
        assertFalse(arr.getBoolean(0));
        assertTrue(arr.getBoolean(1));
    }

    @Test
    public void testKeys() {
        Object returns = BRunUtil.invoke(compileResult, "testKeys");
        Type arrType = getType(returns);
        assertEquals(arrType.getTag(), TypeTags.ARRAY_TAG);

        BArray arr = (BArray) returns;
        assertEquals(((BArrayType) arrType).getElementType().getTag(), TypeTags.STRING_TAG);
        assertEquals(arr.size(), 2);

        List<String> keys = Arrays.asList(arr.getStringArray());
        assertTrue(keys.contains("name"));
        assertTrue(keys.contains("age"));
    }

    @Test
    public void testMap() {
        Object returns = BRunUtil.invoke(compileResult, "testMap");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns;
        assertEquals(((BMapType) map.getType()).getConstrainedType().getTag(), TypeTags.INT_TAG);
        assertEquals(map.size(), 2);
        assertEquals(map.get(StringUtils.fromString("name")), 8L);
        assertEquals(map.get(StringUtils.fromString("age")), 25L);
    }

    @Test
    public void testForEach() {
        Object returns = BRunUtil.invoke(compileResult, "testForEach");
        assertEquals(returns.toString(), "John Doe25");
    }

    @Test
    public void testFilter() {
        Object returns = BRunUtil.invoke(compileResult, "testFilter");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns;
        assertEquals(((BMapType) map.getType()).getConstrainedType().getTag(), TypeTags.INT_TAG);
        assertEquals(map.size(), 2);
        assertEquals(map.get(StringUtils.fromString("physics")), 75L);
        assertEquals(map.get(StringUtils.fromString("ict")), 85L);
    }

    @Test
    public void testReduce() {
        Object returns = BRunUtil.invoke(compileResult, "testReduce");
        assertEquals(returns, 61.25d);
    }

    @DataProvider(name = "mapKeyProvider")
    public Object[][] getMapKeys() {
        return new Object[][]{
                {StringUtils.fromString("name"), true},
                {StringUtils.fromString("age"), true},
                {StringUtils.fromString("invalid"), false}
        };
    }

    @Test
    public void testReadOnlyRecordFilter() {
        BRunUtil.invoke(compileResult, "testReadOnlyRecordFilter");
    }
}
