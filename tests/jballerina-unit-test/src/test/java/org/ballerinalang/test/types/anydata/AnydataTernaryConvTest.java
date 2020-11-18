/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.anydata;

import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for converting between types using the ternary operator.
 *
 * @since 0.985.0
 */
public class AnydataTernaryConvTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/anydata/anydata_conversion_using_ternary.bal");
    }

    @Test(description = "Test anydata to value type conversion using ternary operator")
    public void testAnydataToValueTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToValueTypes");
        assertEquals(((BInteger) returns[0]).intValue(), 33);
        assertEquals(((BFloat) returns[1]).floatValue(), 23.45);
        assertTrue(((BBoolean) returns[2]).booleanValue());
        assertEquals(returns[3].stringValue(), "Hello World!");
    }

    @Test(description = "Test anydata to json conversion")
    public void testAnydataToJson() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToJson");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);
        assertEquals(returns[0].stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
    }

    @Test(description = "Test anydata to xml conversion")
    public void testAnydataToXml() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToXml");
        assertEquals(returns[0].getType().getTag(), TypeTags.XML_TAG);
        assertEquals(returns[0].stringValue(), "<book>The Lost World</book>");
    }

    @Test(description = "Test anydata to record conversion")
    public void testAnydataToRecord() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToRecord");
        assertEquals(returns[0].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns[0].stringValue(), "{a:15}");
    }

    @Test(description = "Test anydata to union conversion")
    public void testAnydataToUnion() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToUnion");
        assertEquals(returns[0].getType().getTag(), TypeTags.INT_TAG);
        assertEquals(returns[1].getType().getTag(), TypeTags.FLOAT_TAG);
        assertEquals(returns[2].getType().getTag(), TypeTags.STRING_TAG);
        assertEquals(returns[3].getType().getTag(), TypeTags.BOOLEAN_TAG);
        assertEquals(returns[4].getType().getTag(), TypeTags.BYTE_TAG);
        assertEquals(((BInteger) returns[0]).intValue(), 10);
        assertEquals(((BFloat) returns[1]).floatValue(), 23.45);
        assertEquals(returns[2].stringValue(), "hello world!");
        assertTrue(((BBoolean) returns[3]).booleanValue());
        assertEquals(((BByte) returns[4]).intValue(), 255);
    }

    @Test(description = "Test anydata to union conversion for complex types")
    public void testAnydataToUnion2() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToUnion2");
        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);
        assertEquals(returns[1].getType().getTag(), TypeTags.XML_TAG);
        assertEquals(returns[2].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns[3].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns[0].stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
        assertEquals(returns[1].stringValue(), "<book>The Lost World</book>");
        assertEquals(returns[2].stringValue(), "{a:15}");
        assertEquals(returns[3].stringValue(), "{ca:15}");
    }

    @Test(description = "Test anydata to tuple conversion")
    public void testAnydataToTuple() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testAnydataToTuple");
        assertEquals(returns[0].getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(returns[0].getType().toString(), "[int,float,boolean,string,byte]");
        assertEquals(returns[0].stringValue(), "[123, 23.45, true, \"hello world!\", 255]");
    }

    @Test(description = "Test anydata to tuple conversion")
    public void testAnydataToTuple2() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testAnydataToTuple2");
        assertEquals(returns[0].getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(returns[0].getType().toString(), "[json,xml]");
        assertEquals(returns[0].stringValue(), "[{\"name\":\"apple\", \"color\":\"red\", \"price\":40}, <book>The " +
                "Lost World</book>]");
    }

    @Test(description = "Test anydata to tuple conversion")
    public void testAnydataToTuple3() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testAnydataToTuple3");
        assertEquals(returns[0].getType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(returns[0].getType().toString(),
                     "[[int|float|string|boolean|byte|json|xml|ClosedFoo|Foo|map<anydata>|anydata[][]" +
                             ",string],int,float]");
        assertEquals(returns[0].stringValue(), "[[[{\"name\":\"apple\", \"color\":\"red\", \"price\":40}, <book>The " +
                "Lost World</book>], \"hello world!\"], 123, 23.45]");
    }

    @Test(description = "Test anydata to nil conversion")
    public void testAnydataToNil() {
        BValue[] returns = BRunUtil.invoke(result, "testAnydataToNil");
        assertNull(returns[0]);
    }

    @Test(description = "Test type testing on any")
    public void testTypeCheckingOnAny() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testTypeCheckingOnAny");

        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);
        assertEquals(((BArrayType) returns[0].getType()).getElementType().getTag(), TypeTags.ANYDATA_TAG);

        BValueArray rets = (BValueArray) returns[0];
        assertEquals(((BInteger) rets.getRefValue(0)).intValue(), 10);
        assertEquals(((BFloat) rets.getRefValue(1)).floatValue(), 23.45);
        assertTrue(((BBoolean) rets.getRefValue(2)).booleanValue());
        assertEquals(rets.getRefValue(3).stringValue(), "hello world!");
        assertEquals(rets.getRefValue(4).stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
        assertEquals(rets.getRefValue(5).stringValue(), "<book>The Lost World</book>");
        assertEquals(rets.getRefValue(6).stringValue(), "{a:15}");
        assertEquals(rets.getRefValue(7).stringValue(), "{ca:15}");
    }
}
