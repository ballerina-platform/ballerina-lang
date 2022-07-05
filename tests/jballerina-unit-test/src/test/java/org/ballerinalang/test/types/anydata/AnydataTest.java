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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.types.BArrayType;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for `anydata` type.
 *
 * @since 0.985.0
 */
public class AnydataTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/anydata/anydata_test.bal");
    }

    @Test(description = "Test allowed literals for anydata")
    public void testAllowedLiterals() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testLiteralValueAssignment");
        assertEquals(returns.get(0), 10L);
        assertEquals(returns.get(1), 23.45);
        assertTrue((Boolean) returns.get(2));
        assertEquals(returns.get(3).toString(), "Hello World!");
    }

    @Test(description = "Test allowed types for anydata")
    public void testValueTypesAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testValueTypesAssignment");
        assertEquals(returns.get(0), 10L);
        assertEquals(returns.get(1), 23.45);
        assertTrue((Boolean) returns.get(2));
        assertEquals(returns.get(3).toString(), "Hello World!");
    }

    @Test(description = "Test record types assignment")
    public void testRecordAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecordAssignment");

        BMap foo = (BMap) returns.get(0);
        assertEquals(getType(foo).getName(), "Foo");
        assertEquals((foo.get(StringUtils.fromString("a"))), 20L);

        BMap closedFoo = (BMap) returns.get(1);
        assertEquals(getType(closedFoo).getName(), "ClosedFoo");
        assertEquals((closedFoo.get(StringUtils.fromString("ca"))), 35L);
    }

    @Test(description = "Test cyclic record types assignment")
    public void testCyclicRecordAssignment() {
        Object returns = BRunUtil.invoke(result, "testCyclicRecordAssignment");
        Assert.assertTrue(returns instanceof BMap<?, ?>);
        BMap bMap = (BMap) returns;
        Assert.assertEquals(bMap.get(StringUtils.fromString("name")).toString(), "Child");
        Assert.assertEquals(bMap.get(StringUtils.fromString("age")), 25L);
        Assert.assertEquals(
                ((BMap) bMap.get(StringUtils.fromString("parent"))).get(StringUtils.fromString("name")).toString(),
                "Parent");
    }

    @Test(description = "Test XML assignment")
    public void testXMLAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testXMLAssignment");

        assertTrue(returns.get(0) instanceof BXml);
        assertEquals(returns.get(0).toString(), "<book>The Lost World</book>");

        assertTrue(returns.get(1) instanceof BXml);
        assertEquals(returns.get(1).toString(), "<book>Count of Monte Cristo</book>");
    }

    @Test(description = "Test JSON assignment")
    public void testJSONAssignment() {
        Object returns = BRunUtil.invoke(result, "testJSONAssignment");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);
        assertEquals(returns.toString(), "{\"name\":\"apple\",\"color\":\"red\",\"price\":40}");
    }

    @Test(description = "Test union assignment")
    public void testUnionAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testUnionAssignment");
        assertEquals(returns.get(0).toString(), "hello world!");
        assertEquals(returns.get(1), 123L);
        assertEquals(returns.get(2), 23.45);
        assertTrue((Boolean) returns.get(3));
        assertEquals(returns.get(4), 255);
    }

    @Test(description = "Test nil assignment")
    public void testNilAssignment() {
        Object returns = BRunUtil.invoke(result, "testNilAssignment");
        assertNull(returns);
    }

    @Test(description = "Test finite type assignment")
    public void testFiniteTypeAssignment() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testFiniteTypeAssignment");
        assertEquals(returns.get(0).toString(), "A");
        assertEquals(returns.get(1).toString(), "Z");
        assertEquals(returns.get(2), 123L);
        assertEquals(returns.get(3), 23.45);
        assertTrue((Boolean) returns.get(4));
    }

    @Test(description = "Test anydata array")
    public void testAnydataArray() {
        Object returns = BRunUtil.invoke(result, "testAnydataArray", new Object[]{});
        assertEquals(TypeTags.ANYDATA_TAG, ((BArrayType) getType(returns)).getElementType().getTag());
        BArray adArr = (BArray) returns;

        assertEquals(adArr.getRefValue(0), 1234L);
        assertEquals(adArr.getRefValue(1), 23.45);
        assertTrue((Boolean) adArr.getRefValue(2));
        assertEquals(adArr.getRefValue(3).toString(), "Hello World!");
        assertEquals(adArr.getRefValue(4), 10);
        assertEquals(adArr.getRefValue(5).toString(), "{\"a\":15}");
        assertEquals(adArr.getRefValue(6).toString(), "{\"name\":\"apple\",\"color\":\"red\",\"price\":40}");
        assertEquals(adArr.getRefValue(7).toString(), "<book>The Lost World</book>");
    }

    @Test(description = "Test anydata to value type conversion")
    public void testAnydataToValueTypes() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testAnydataToValueTypes");
        assertEquals(returns.get(0), 33L);
        assertEquals(returns.get(1), 23.45);
        assertTrue((Boolean) returns.get(2));
        assertEquals(returns.get(3).toString(), "Hello World!");
    }

    @Test(description = "Test anydata to json conversion")
    public void testAnydataToJson() {
        Object returns = BRunUtil.invoke(result, "testAnydataToJson");
        assertEquals(getType(returns).getTag(), TypeTags.MAP_TAG);
        assertEquals(returns.toString(), "{\"name\":\"apple\",\"color\":\"red\",\"price\":40}");
    }

    @Test(description = "Test anydata to xml conversion")
    public void testAnydataToXml() {
        Object returns = BRunUtil.invoke(result, "testAnydataToXml");
        assertEquals(getType(returns).getTag(), TypeTags.XML_ELEMENT_TAG);
        assertEquals(returns.toString(), "<book>The Lost World</book>");
    }

    @Test(description = "Test anydata to record conversion")
    public void testAnydataToRecord() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testAnydataToRecord");
        assertEquals(getType(returns.get(0)).getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns.get(0).toString(), "{\"a\":15}");
    }

    @Test(description = "Test anydata to union conversion")
    public void testAnydataToUnion() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testAnydataToUnion");
        assertEquals(getType(returns.get(0)).getTag(), TypeTags.INT_TAG);
        assertEquals(getType(returns.get(1)).getTag(), TypeTags.FLOAT_TAG);
        assertEquals(getType(returns.get(2)).getTag(), TypeTags.STRING_TAG);
        assertEquals(getType(returns.get(3)).getTag(), TypeTags.BOOLEAN_TAG);
        assertEquals(getType(returns.get(4)).getTag(), TypeTags.BYTE_TAG);
        assertEquals(returns.get(0), 10L);
        assertEquals(returns.get(1), 23.45);
        assertEquals(returns.get(2).toString(), "hello world!");
        assertTrue((Boolean) returns.get(3));
        assertEquals(returns.get(4), 255);
    }

    @Test(description = "Test anydata to tuple conversion")
    public void testAnydataToTuple() {
        Object returns = BRunUtil.invoke(result, "testAnydataToTuple");
        assertEquals(getType(returns).getTag(), TypeTags.TUPLE_TAG);
        assertEquals(getType(returns).toString(), "[int,float,boolean,string,byte]");
        assertEquals(returns.toString(), "[123,23.45,true,\"hello world!\",255]");
    }

    @Test(description = "Test anydata to tuple conversion")
    public void testAnydataToTuple2() {
        Object returns = BRunUtil.invoke(result, "testAnydataToTuple2");
        assertEquals(getType(returns).getTag(), TypeTags.TUPLE_TAG);
        assertEquals(getType(returns).toString(),
                "[json,xml<(lang.xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>]");
        assertEquals(returns.toString(), "[{\"name\":\"apple\",\"color\":\"red\",\"price\":40},`<book>The Lost " +
                "World</book>`]");
    }

    @Test(description = "Test anydata to nil conversion")
    public void testAnydataToNil() {
        Object returns = BRunUtil.invoke(result, "testAnydataToNil");
        assertNull(returns);
    }

    @Test(description = "Test anydata to finite type conversion")
    public void testAnydataToFiniteType() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testAnydataToFiniteType");
        assertEquals(returns.get(0).toString(), "A");
        assertEquals(returns.get(1).toString(), "Z");
        assertEquals(returns.get(2), 123L);
        assertEquals(returns.get(3), 23.45);
        assertTrue((Boolean) returns.get(4));
    }

    @Test(description = "Test type testing on any")
    public void testTypeCheckingOnAny() {
        Object returns = BRunUtil.invoke(result, "testTypeCheckingOnAny");

        assertEquals(getType(returns).getTag(), TypeTags.ARRAY_TAG);
        assertEquals(((BArrayType) getType(returns)).getElementType().getTag(), TypeTags.ANYDATA_TAG);

        BArray rets = (BArray) returns;
        assertEquals(rets.getRefValue(0), 10L);
        assertEquals(rets.getRefValue(1), 23.45);
        assertTrue((Boolean) rets.getRefValue(2));
        assertEquals(rets.getRefValue(3).toString(), "hello world!");
        assertEquals(rets.getRefValue(4).toString(), "{\"name\":\"apple\",\"color\":\"red\",\"price\":40}");
        assertEquals(rets.getRefValue(5).toString(), "<book>The Lost World</book>");
        assertEquals(rets.getRefValue(6).toString(), "{\"a\":15}");
        assertEquals(rets.getRefValue(7).toString(), "{\"ca\":15}");
    }

    @Test(dataProvider = "getAnydataFuntion")
    public void testAnydata(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "getAnydataFuntion")
    public Object[] getAnydataFuntion() {
        return new String[]{
                "testMapOfCharIsAnydata",
                "testCharArrayIsAnydata",
                "testMapOfIntSubtypeIsAnydata",
                "testArrayOfIntSubtypeIsAnydata",
                "testMapOfNeverTypeIsAnydata",
                "testRuntimeIsAnydata",
                "testAnydataToTuple3",
                "testAnydataToUnion2",
                "testAnydataToTable",
                "testTupleAssignment",
                "testUnionAssignment2",
                "testTableAssignment",
                "testMapAssignment",
                "testConstrainedMaps",
                "testArrayAssignment",
                "testAnytoAnydataTypeCast"
        };
    }
}
