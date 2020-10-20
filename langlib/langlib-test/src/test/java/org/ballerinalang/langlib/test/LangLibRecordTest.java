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

import java.util.Arrays;
import java.util.List;

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

    @Test
    public void testLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLength");
        assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test
    public void testGet() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGet", new BValue[]{new BString("name")});
        assertEquals(returns[0].stringValue(), "John Doe");

        returns = BRunUtil.invoke(compileResult, "testGet", new BValue[]{new BString("age")});
        assertEquals(((BInteger) returns[0]).intValue(), 25);
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
        assertEquals(map.size(), 2);
        assertEquals(map.get("name").stringValue(), "[\"name\", \"John Doe\"]");
        assertEquals(map.get("age").stringValue(), "[\"age\", 25]");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*OperationNotSupported \\{\"message\":\"failed .*")
    public void testRemove() {
        BRunUtil.invoke(compileResult, "testRemove", new BValue[]{new BString("name")});
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp =
                  ".*OperationNotSupported \\{\"message\":\"failed .*")
    public void testRemoveAll() {
        BRunUtil.invoke(compileResult, "testRemoveAll");
    }

    @Test(dataProvider = "mapKeyProvider")
    public void testHasKey(BString key, boolean expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasKey", new BValue[]{key});
        assertEquals(((BBoolean) returns[0]).booleanValue(), expected);
    }

    @Test
    public void testHasKey2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasKey2");
        assertFalse(((BBoolean) returns[0]).booleanValue());
        assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testKeys() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testKeys");
        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);

        BValueArray arr = (BValueArray) returns[0];
        assertEquals(arr.elementType.getTag(), TypeTags.STRING_TAG);
        assertEquals(arr.size(), 2);

        List<String> keys = Arrays.asList(arr.getStringArray());
        assertTrue(keys.contains("name"));
        assertTrue(keys.contains("age"));
    }

    @Test
    public void testMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMap");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns[0];
        assertEquals(((BMapType) map.getType()).getConstrainedType().getTag(), TypeTags.INT_TAG);
        assertEquals(map.size(), 2);
        assertEquals(((BInteger) map.get("name")).intValue(), 8);
        assertEquals(((BInteger) map.get("age")).intValue(), 25);
    }

    @Test
    public void testForEach() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForEach");
        assertEquals(returns[0].stringValue(), "John Doe25");
    }

    @Test
    public void testFilter() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFilter");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);

        BMap map = (BMap) returns[0];
        assertEquals(((BMapType) map.getType()).getConstrainedType().getTag(), TypeTags.INT_TAG);
        assertEquals(map.size(), 2);
        assertEquals(((BInteger) map.get("physics")).intValue(), 75);
        assertEquals(((BInteger) map.get("ict")).intValue(), 85);
    }

    @Test
    public void testReduce() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReduce");
        assertEquals(((BFloat) returns[0]).floatValue(), 61.25);
    }

    @DataProvider(name = "mapKeyProvider")
    public Object[][] getMapKeys() {
        return new Object[][]{
                {new BString("name"), true},
                {new BString("age"), true},
                {new BString("invalid"), false}
        };
    }
}
