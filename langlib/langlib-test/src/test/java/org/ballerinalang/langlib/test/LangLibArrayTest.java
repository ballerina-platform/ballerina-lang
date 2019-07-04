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


import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test cases for the lang.array library.
 *
 * @since 1.0
 */
public class LangLibArrayTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/arraylib_test.bal");
    }

    @Test
    public void testLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLength");
        assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test(enabled = false)
    public void testMap() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMap");
        assertEquals(((BInteger) returns[0]).intValue(), 1);
        assertEquals(((BInteger) returns[1]).intValue(), 2);
        assertEquals(((BInteger) returns[2]).intValue(), 3);
        assertEquals(((BInteger) returns[3]).intValue(), 4);
    }

    @Test(enabled = false)
    public void testForeach() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForeach");
        assertEquals(returns[0].stringValue(), "HelloWorldfromBallerina");
    }

    @Test
    public void testSlice() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testSlice");
        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);

        BValueArray arr = (BValueArray) returns[0];
        assertEquals(arr.elementType.getTag(), TypeTags.FLOAT_TAG);
        assertEquals(arr.size(), 3);
        assertEquals(arr.getFloat(0), 23.45);
        assertEquals(arr.getFloat(1), 34.56);
        assertEquals(arr.getFloat(2), 45.67);
    }

    @Test
    public void testRemove() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemove");
        assertEquals(returns[0].stringValue(), "FooFoo");

        assertEquals(returns[1].getType().getTag(), TypeTags.ARRAY_TAG);
        BValueArray arr = (BValueArray) returns[1];

        assertEquals(arr.elementType.getTag(), TypeTags.STRING_TAG);
        assertEquals(arr.size(), 3);
        assertEquals(arr.getString(0), "Foo");
        assertEquals(arr.getString(1), "Bar");
        assertEquals(arr.getString(2), "BarBar");
    }
}
