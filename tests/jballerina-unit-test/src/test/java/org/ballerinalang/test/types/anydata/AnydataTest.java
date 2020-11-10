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

//import org.ballerinalang.core.model.types.BArrayType;
//import org.ballerinalang.core.model.types.TypeTags;
//import org.ballerinalang.core.model.values.BBoolean;
//import org.ballerinalang.core.model.values.BByte;
//import org.ballerinalang.core.model.values.BFloat;
//import org.ballerinalang.core.model.values.BInteger;
//import org.ballerinalang.core.model.values.BMap;
//import org.ballerinalang.core.model.values.BValue;
//import org.ballerinalang.core.model.values.BValueArray;
//import org.ballerinalang.core.model.values.BXML;
//import org.ballerinalang.test.util.BCompileUtil;
//import org.ballerinalang.test.util.BRunUtil;
//import org.ballerinalang.test.util.CompileResult;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import static org.testng.Assert.assertEquals;
//import static org.testng.Assert.assertNull;
//import static org.testng.Assert.assertTrue;

/**
 * Test cases for `anydata` type.
 *
 * @since 0.985.0
 */
public class AnydataTest {

//    private CompileResult result;
//
//    @BeforeClass
//    public void setup() {
//        result = BCompileUtil.compile("test-src/types/anydata/anydata_test.bal");
//    }
//
//    @Test(description = "Test allowed literals for anydata")
//    public void testAllowedLiterals() {
//        BValue[] returns = BRunUtil.invoke(result, "testLiteralValueAssignment");
//        assertEquals(((BInteger) returns[0]).intValue(), 10);
//        assertEquals(((BFloat) returns[1]).floatValue(), 23.45);
//        assertTrue(((BBoolean) returns[2]).booleanValue());
//        assertEquals(returns[3].stringValue(), "Hello World!");
//    }
//
//    @Test(description = "Test allowed types for anydata")
//    public void testValueTypesAssignment() {
//        BValue[] returns = BRunUtil.invoke(result, "testValueTypesAssignment");
//        assertEquals(((BInteger) returns[0]).intValue(), 10);
//        assertEquals(((BFloat) returns[1]).floatValue(), 23.45);
//        assertTrue(((BBoolean) returns[2]).booleanValue());
//        assertEquals(returns[3].stringValue(), "Hello World!");
//    }
//
//    @Test(description = "Test record types assignment")
//    public void testRecordAssignment() {
//        BValue[] returns = BRunUtil.invoke(result, "testRecordAssignment");
//
//        BMap foo = (BMap) returns[0];
//        assertEquals(foo.getType().getName(), "Foo");
//        assertEquals(((BInteger) foo.get("a")).intValue(), 20);
//
//        BMap closedFoo = (BMap) returns[1];
//        assertEquals(closedFoo.getType().getName(), "ClosedFoo");
//        assertEquals(((BInteger) closedFoo.get("ca")).intValue(), 35);
//    }
//
//    @Test(description = "Test cyclic record types assignment")
//    public void testCyclicRecordAssignment() {
//        BValue[] returns = BRunUtil.invoke(result, "testCyclicRecordAssignment");
//        Assert.assertEquals(returns.length, 1);
//        Assert.assertTrue(returns[0] instanceof BMap<?, ?>);
//        BMap bMap = (BMap) returns[0];
//        Assert.assertEquals(bMap.get("name").stringValue(), "Child");
//        Assert.assertEquals(((BInteger) bMap.get("age")).intValue(), 25);
//        Assert.assertEquals(((BMap) bMap.get("parent")).get("name").stringValue(), "Parent");
//    }
//
//    @Test(description = "Test XML assignment")
//    public void testXMLAssignment() {
//        BValue[] returns = BRunUtil.invoke(result, "testXMLAssignment");
//
//        assertTrue(returns[0] instanceof BXML);
//        assertEquals(returns[0].stringValue(), "<book>The Lost World</book>");
//
//        assertTrue(returns[1] instanceof BXML);
//        assertEquals(returns[1].stringValue(), "<book>Count of Monte Cristo</book>");
//    }
//
//    @Test(description = "Test JSON assignment")
//    public void testJSONAssignment() {
//        BValue[] returns = BRunUtil.invoke(result, "testJSONAssignment");
//        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);
//        assertEquals(returns[0].stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
//    }
//
//    @Test(description = "Test table assignment")
//    public void testTableAssignment() {
//        BRunUtil.invoke(result, "testTableAssignment");
//    }
//
//    @Test(description = "Test map assignment")
//    public void testMapAssignment() {
//        BRunUtil.invoke(result, "testMapAssignment");
//    }
//
//    @Test(description = "Test for maps constrained by anydata")
//    public void testConstrainedMaps() {
//        BRunUtil.invoke(result, "testConstrainedMaps");
//    }
//
//    @Test(description = "Test array assignment")
//    public void testArrayAssignment() {
//        BRunUtil.invoke(result, "testArrayAssignment");
//    }
//
//    @Test(description = "Test union assignment")
//    public void testUnionAssignment() {
//        BValue[] returns = BRunUtil.invoke(result, "testUnionAssignment");
//        assertEquals(returns[0].stringValue(), "hello world!");
//        assertEquals(((BInteger) returns[1]).intValue(), 123);
//        assertEquals(((BFloat) returns[2]).floatValue(), 23.45);
//        assertTrue(((BBoolean) returns[3]).booleanValue());
//        assertEquals(((BByte) returns[4]).intValue(), 255);
//    }
//
//    @Test(description = "Test union assignment for more complex types")
//    public void testUnionAssignment2() {
//        BRunUtil.invoke(result, "testUnionAssignment2");
//    }
//
//    @Test(description = "Test tuple assignment")
//    public void testTupleAssignment() {
//        BRunUtil.invoke(result, "testTupleAssignment");
//    }
//
//    @Test(description = "Test nil assignment")
//    public void testNilAssignment() {
//        BValue[] returns = BRunUtil.invoke(result, "testNilAssignment");
//        assertNull(returns[0]);
//    }
//
//    @Test(description = "Test finite type assignment")
//    public void testFiniteTypeAssignment() {
//        BValue[] returns = BRunUtil.invoke(result, "testFiniteTypeAssignment");
//        assertEquals(returns[0].stringValue(), "A");
//        assertEquals(returns[1].stringValue(), "Z");
//        assertEquals(((BInteger) returns[2]).intValue(), 123);
//        assertEquals(((BFloat) returns[3]).floatValue(), 23.45);
//        assertTrue(((BBoolean) returns[4]).booleanValue());
//    }
//
//    @Test(description = "Test anydata array")
//    public void testAnydataArray() {
//        BValue[] returns = BRunUtil.invokeFunction(result, "testAnydataArray", new BValue[]{});
//        assertEquals(TypeTags.ANYDATA_TAG, ((BArrayType) returns[0].getType()).getElementType().getTag());
//        BValueArray adArr = (BValueArray) returns[0];
//
//        assertEquals(((BInteger) adArr.getRefValue(0)).intValue(), 1234);
//        assertEquals(((BFloat) adArr.getRefValue(1)).floatValue(), 23.45);
//        assertTrue(((BBoolean) adArr.getRefValue(2)).booleanValue());
//        assertEquals(adArr.getRefValue(3).stringValue(), "Hello World!");
//        assertEquals(((BByte) adArr.getRefValue(4)).byteValue(), 10);
//        assertEquals(adArr.getRefValue(5).stringValue(), "{a:15}");
//        assertEquals(adArr.getRefValue(6).stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
//        assertEquals(adArr.getRefValue(7).stringValue(), "<book>The Lost World</book>");
//    }
//
//    @Test(description = "Test anydata to value type conversion")
//    public void testAnydataToValueTypes() {
//        BValue[] returns = BRunUtil.invoke(result, "testAnydataToValueTypes");
//        assertEquals(((BInteger) returns[0]).intValue(), 33);
//        assertEquals(((BFloat) returns[1]).floatValue(), 23.45);
//        assertTrue(((BBoolean) returns[2]).booleanValue());
//        assertEquals(returns[3].stringValue(), "Hello World!");
//    }
//
//    @Test(description = "Test anydata to json conversion")
//    public void testAnydataToJson() {
//        BValue[] returns = BRunUtil.invoke(result, "testAnydataToJson");
//        assertEquals(returns[0].getType().getTag(), TypeTags.MAP_TAG);
//        assertEquals(returns[0].stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
//    }
//
//    @Test(description = "Test anydata to xml conversion")
//    public void testAnydataToXml() {
//        BValue[] returns = BRunUtil.invoke(result, "testAnydataToXml");
//        assertEquals(returns[0].getType().getTag(), TypeTags.XML_TAG);
//        assertEquals(returns[0].stringValue(), "<book>The Lost World</book>");
//    }
//
//    @Test(description = "Test anydata to record conversion")
//    public void testAnydataToRecord() {
//        BValue[] returns = BRunUtil.invoke(result, "testAnydataToRecord");
//        assertEquals(returns[0].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
//        assertEquals(returns[0].stringValue(), "{a:15}");
//    }
//
//    @Test(description = "Test anydata to table conversion")
//    public void testAnydataToTable() {
//        BRunUtil.invoke(result, "testAnydataToTable");
//    }
//
//    @Test(description = "Test anydata to union conversion")
//    public void testAnydataToUnion() {
//        BValue[] returns = BRunUtil.invoke(result, "testAnydataToUnion");
//        assertEquals(returns[0].getType().getTag(), TypeTags.INT_TAG);
//        assertEquals(returns[1].getType().getTag(), TypeTags.FLOAT_TAG);
//        assertEquals(returns[2].getType().getTag(), TypeTags.STRING_TAG);
//        assertEquals(returns[3].getType().getTag(), TypeTags.BOOLEAN_TAG);
//        assertEquals(returns[4].getType().getTag(), TypeTags.BYTE_TAG);
//        assertEquals(((BInteger) returns[0]).intValue(), 10);
//        assertEquals(((BFloat) returns[1]).floatValue(), 23.45);
//        assertEquals(returns[2].stringValue(), "hello world!");
//        assertTrue(((BBoolean) returns[3]).booleanValue());
//        assertEquals(((BByte) returns[4]).intValue(), 255);
//    }
//
//    @Test(description = "Test anydata to union conversion for complex types")
//    public void testAnydataToUnion2() {
//        BRunUtil.invoke(result, "testAnydataToUnion2");
//    }
//
//    @Test(description = "Test anydata to tuple conversion")
//    public void testAnydataToTuple() {
//        BValue[] returns = BRunUtil.invokeFunction(result, "testAnydataToTuple");
//        assertEquals(returns[0].getType().getTag(), TypeTags.TUPLE_TAG);
//        assertEquals(returns[0].getType().toString(), "[int,float,boolean,string,byte]");
//        assertEquals(returns[0].stringValue(), "[123, 23.45, true, \"hello world!\", 255]");
//    }
//
//    @Test(description = "Test anydata to tuple conversion")
//    public void testAnydataToTuple2() {
//        BValue[] returns = BRunUtil.invokeFunction(result, "testAnydataToTuple2");
//        assertEquals(returns[0].getType().getTag(), TypeTags.TUPLE_TAG);
//        assertEquals(returns[0].getType().toString(), "[json,xml]");
//        assertEquals(returns[0].stringValue(), "[{\"name\":\"apple\", \"color\":\"red\", \"price\":40}, <book>The " +
//                "Lost World</book>]");
//    }
//
//    @Test(description = "Test anydata to tuple conversion")
//    public void testAnydataToTuple3() {
//        BRunUtil.invokeFunction(result, "testAnydataToTuple3");
//    }
//
//    @Test(description = "Test anydata to nil conversion")
//    public void testAnydataToNil() {
//        BValue[] returns = BRunUtil.invoke(result, "testAnydataToNil");
//        assertNull(returns[0]);
//    }
//
//    @Test(description = "Test anydata to finite type conversion")
//    public void testAnydataToFiniteType() {
//        BValue[] returns = BRunUtil.invoke(result, "testAnydataToFiniteType");
//        assertEquals(returns[0].stringValue(), "A");
//        assertEquals(returns[1].stringValue(), "Z");
//        assertEquals(((BInteger) returns[2]).intValue(), 123);
//        assertEquals(((BFloat) returns[3]).floatValue(), 23.45);
//        assertTrue(((BBoolean) returns[4]).booleanValue());
//    }
//
//    @Test(description = "Test type testing on any")
//    public void testTypeCheckingOnAny() {
//        BValue[] returns = BRunUtil.invokeFunction(result, "testTypeCheckingOnAny");
//
//        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);
//        assertEquals(((BArrayType) returns[0].getType()).getElementType().getTag(), TypeTags.ANYDATA_TAG);
//
//        BValueArray rets = (BValueArray) returns[0];
//        assertEquals(((BInteger) rets.getRefValue(0)).intValue(), 10);
//        assertEquals(((BFloat) rets.getRefValue(1)).floatValue(), 23.45);
//        assertTrue(((BBoolean) rets.getRefValue(2)).booleanValue());
//        assertEquals(rets.getRefValue(3).stringValue(), "hello world!");
//        assertEquals(rets.getRefValue(4).stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
//        assertEquals(rets.getRefValue(5).stringValue(), "<book>The Lost World</book>");
//        assertEquals(rets.getRefValue(6).stringValue(), "{a:15}");
//        assertEquals(rets.getRefValue(7).stringValue(), "{ca:15}");
//    }
//
//    @Test
//    public void testArraysWithErrorsAsAnydata() {
//        BValue[] returns = BRunUtil.invoke(result, "testArraysWithErrorsAsAnydata");
//        assertTrue(((BBoolean) returns[0]).booleanValue());
//    }
//
//    @Test
//    public void testTuplesWithErrorsAsAnydata() {
//        BValue[] returns = BRunUtil.invoke(result, "testTuplesWithErrorsAsAnydata");
//        assertTrue(((BBoolean) returns[0]).booleanValue());
//    }
//
//    @Test
//    public void testMapsWithErrorsAsAnydata() {
//        BValue[] returns = BRunUtil.invoke(result, "testMapsWithErrorsAsAnydata");
//        assertTrue(((BBoolean) returns[0]).booleanValue());
//    }
//
//    @Test
//    public void testRecordsWithErrorsAsAnydata() {
//        BValue[] returns = BRunUtil.invoke(result, "testRecordsWithErrorsAsAnydata");
//        assertTrue(((BBoolean) returns[0]).booleanValue());
//    }
}
