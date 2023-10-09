/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.statements.arrays;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Map;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;
import static java.math.BigDecimal.ZERO;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for filling the elements of the array with its type's implicit initial value.
 *
 * @since 0.990.4
 */
public class ArrayFillTest {

    private CompileResult compileResult;
    private static final long index = 250;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-fill-test.bal");
    }

    @Test
    public void testNilArrayFill() {
        Object[] args = new Object[]{(index), null};
        Object returns = BRunUtil.invoke(compileResult, "testNilArrayFill", args);
        BArray nilArr = (BArray) returns;
        assertEquals(nilArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(nilArr.get(i));
        }

        assertNull(nilArr.get(index));
    }

    @Test
    public void testBooleanArrayFill() {
        final boolean value = true;
        Object[] args = new Object[]{(index), (value)};
        Object returns = BRunUtil.invoke(compileResult, "testBooleanArrayFill", args);
        BArray booleanArr = (BArray) returns;
        assertEquals(booleanArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertFalse(booleanArr.getBoolean(i));
        }

        assertTrue(booleanArr.getBoolean(index));
    }

    @Test
    public void testIntArrayFill() {
        final long value = 100;
        Object[] args = new Object[]{(index), (value)};
        Object returns = BRunUtil.invoke(compileResult, "testIntArrayFill", args);
        BArray intArr = (BArray) returns;
        assertEquals(intArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(intArr.getInt(i), 0);
        }

        assertEquals(intArr.getInt(index), value);
    }

    @Test
    public void testFloatArrayFill() {
        final double value = 23.45;
        Object[] args = new Object[]{(index), (value)};
        Object returns = BRunUtil.invoke(compileResult, "testFloatArrayFill", args);
        BArray floatArr = (BArray) returns;
        assertEquals(floatArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(floatArr.getFloat(i), 0.0);
        }

        assertEquals(floatArr.getFloat(index), value);
    }

    @Test
    public void testDecimalArrayFill() {
        final BigDecimal value = new BigDecimal("23.45");
        Object[] args = new Object[]{(index), ValueCreator.createDecimalValue(value)};
        Object returns = BRunUtil.invoke(compileResult, "testDecimalArrayFill", args);
        BArray decimalArr = (BArray) returns;
        assertEquals(decimalArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(decimalArr.get(i), ValueCreator.createDecimalValue(ZERO));
        }

        assertEquals(decimalArr.get(index), ValueCreator.createDecimalValue(value));
    }

    @Test
    public void testStringArrayFill() {
        final String value = "Hello World!";
        Object[] args = new Object[]{(index), StringUtils.fromString(value)};
        Object returns = BRunUtil.invoke(compileResult, "testStringArrayFill", args);
        BArray stringArr = (BArray) returns;
        assertEquals(stringArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(stringArr.getString(i), "");
        }

        assertEquals(stringArr.getString(index), value);
    }

    @Test
    public void testArrayOfArraysFill() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testArrayOfArraysFill", args);
        BArray arr2d = (BArray) returns;
        assertEquals(arr2d.size(), index + 1);

        for (int i = 0; i < index; i++) {
            BArray arr = (BArray) arr2d.get(i);
            assertEquals(arr.toString(), "[]");
        }

        BArray arr = (BArray) arr2d.get(index);
        assertEquals(arr.toString(), "[{\"name\":\"John\",\"age\":25},{\"name\":\"John\",\"age\":25}]");
    }

    @Test
    public void testTupleArrayFill() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testTupleArrayFill", args);
        BArray tupleArr = (BArray) returns;
        assertEquals(tupleArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            BArray tuple = (BArray) tupleArr.get(i);
            assertEquals(tuple.get(0).toString(), "");
            assertEquals((tuple.get(1)), 0L);
        }

        BArray tuple = (BArray) tupleArr.get(index);
        assertEquals(tuple.get(0).toString(), "Hello World!");
        assertEquals((tuple.get(1)), 100L);
    }

    @Test
    public void testTupleSealedArrayFill() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testTupleSealedArrayFill", args);
        BArray tupleArr = (BArray) returns;
        assertEquals(tupleArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            BArray tuple = (BArray) tupleArr.get(i);
            assertEquals(tuple.get(0).toString(), "");
            assertEquals((tuple.get(1)), 0L);
        }

        BArray tuple = (BArray) tupleArr.get(index);
        assertEquals(tuple.get(0).toString(), "Hello World!");
        assertEquals((tuple.get(1)), 100L);
    }

    @Test
    public void testMapArrayFill() {
        BMap emptyMap = ValueCreator.createMapValue(TypeCreator.createMapType(PredefinedTypes.TYPE_ANY));
        BMap map = ValueCreator.createMapValue(TypeCreator.createMapType(PredefinedTypes.TYPE_ANY));
        map.put("1", 1);
        Object[] args = new Object[]{(index), map};
        Object returns = BRunUtil.invoke(compileResult, "testMapArrayFill", args);
        BArray mapArr = (BArray) returns;
        assertEquals(mapArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            validateMapValue((BMap<String, Object>) mapArr.get(i), emptyMap);
        }

        validateMapValue((BMap<String, Object>) mapArr.get(index), map);
    }

    @Test
    public void testTableArrayFill() {
        Object[] args = new Object[]{(index)};
        BRunUtil.invoke(compileResult, "testTableArrayFill", args);
    }

    @Test
    public void testXMLArrayFill() {
        final String value = "<name>Pubudu</name>";
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testXMLArrayFill", args);
        BArray xmlArr = (BArray) returns;
        assertEquals(xmlArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(xmlArr.get(i).toString(), "");
        }

        assertEquals(xmlArr.get(index).toString(), value);
    }

    @Test
    public void testUnionArrayFill1() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testUnionArrayFill1", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.get(i));
        }

        assertEquals(unionArr.get(index).toString(), "Hello World!");
    }

    @Test
    public void testUnionArrayFill2() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testUnionArrayFill2", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(unionArr.get(i).toString(), "");
        }

        assertEquals(unionArr.get(index).toString(), "Hello World!");
    }

    @Test
    public void testUnionArrayFill3() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testUnionArrayFill3", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.get(i));
        }

        assertEquals(unionArr.get(index).toString(), "{\"name\":\"John\",\"age\":25}");
    }

    @Test
    public void testUnionArrayFill4() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testUnionArrayFill4", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(unionArr.get(i), 0L);
        }

        assertEquals(unionArr.get(index), 1L);
    }

    @Test
    public void testFiniteTypeArrayFill1() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testFiniteTypeArrayFill1", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(unionArr.get(i), 0L);
        }

        assertEquals(unionArr.get(index), 5L);
    }

    @Test
    public void testFiniteTypeArrayFill2() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testFiniteTypeArrayFill2", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals((unionArr.get(i)), 0.0);
        }

        assertEquals((unionArr.get(index)), 1.2);
    }

    @Test
    public void testFiniteTypeArrayFill3() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testFiniteTypeArrayFill3", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertFalse((Boolean) unionArr.get(i));
        }

        assertTrue((Boolean) unionArr.get(index));
    }

    @Test
    public void testFiniteTypeArrayFill4() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testFiniteTypeArrayFill4", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.get(i));
        }

        assertTrue((Boolean) unionArr.get(index));
    }

    @Test
    public void testFiniteTypeArrayFill5() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testFiniteTypeArrayFill5", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals((unionArr.get(i)), ValueCreator.createDecimalValue("0.0"));
        }

        assertEquals((unionArr.get(index)), ValueCreator.createDecimalValue("1.2"));
    }

    @Test
    public void testOptionalTypeArrayFill() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testOptionalTypeArrayFill", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.get(i));
        }

        assertEquals(unionArr.get(index).toString(), "Hello World!");
    }

    @Test
    public void testAnyArrayFill() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testAnyArrayFill", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.get(i));
        }

        assertEquals(unionArr.get(index).toString(), "{\"name\":\"John\",\"age\":25}");
    }

    @Test
    public void testAnySealedArrayFill() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testAnySealedArrayFill", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.get(i));
        }

        assertEquals(unionArr.get(index).toString(), "{\"name\":\"John\",\"age\":25}");
    }

    @Test
    public void testAnydataArrayFill() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testAnydataArrayFill", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.get(i));
        }

        assertEquals(unionArr.get(index).toString(), "{\"name\":\"John\",\"age\":25}");
    }

    @Test
    public void testAnydataSealedArrayFill() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testAnydataSealedArrayFill", args);
        BArray unionArr = (BArray) returns;
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.get(i));
        }

        assertEquals(unionArr.get(index).toString(), "{\"name\":\"John\",\"age\":25}");
    }

    @Test
    public void testByteArrayFill() {
        final byte value = 100;
        Object[] args = new Object[]{(index), (value)};
        Object returns = BRunUtil.invoke(compileResult, "testByteArrayFill", args);
        BArray byteArr = (BArray) returns;
        assertEquals(byteArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(byteArr.getByte(i), 0);
        }

        assertEquals(byteArr.getByte(index), value);
    }

    @Test
    public void testJSONArrayFill() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testJSONArrayFill", args);
        BArray jsonArr = (BArray) returns;
        assertEquals(jsonArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(jsonArr.get(i));
        }

        assertEquals(jsonArr.get(index).toString(), "{\"name\":\"John\",\"age\":25}");
    }

    @Test
    public void testSingletonTypeArrayFill() {
        Object[] args = new Object[]{(index)};
        Object returns = BRunUtil.invoke(compileResult, "testSingletonTypeArrayFill", args);
        BArray singletonArray = (BArray) returns;
        assertEquals(singletonArray.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(singletonArray.getRefValue(i).toString(), "1");
        }

        assertEquals(singletonArray.get(index).toString(), "1");
    }

    @Test
    public void testSingletonTypeArrayFill1() {
        Object returns = BRunUtil.invoke(compileResult, "testSingletonTypeArrayFill1");
        BArray singletonArray = (BArray) returns;
        assertEquals(singletonArray.size(), 2);
        assertEquals(singletonArray.getRefValue(0).toString(), "true");
        assertEquals(singletonArray.getRefValue(1).toString(), "true");
    }

    @Test
    public void testSingletonTypeArrayStaticFill() {
        Object returns = BRunUtil.invoke(compileResult, "testSingletonTypeArrayStaticFill");
        BArray singletonArray = (BArray) returns;
        assertEquals(singletonArray.size(), 2);
        assertEquals(singletonArray.getRefValue(0).toString(), "true");
        assertEquals(singletonArray.getRefValue(1).toString(), "true");
    }

    @Test
    public void testSequentialArrayInsertion() {
        Object returns = BRunUtil.invoke(compileResult, "testSequentialArrayInsertion");
        BArray resultArray = (BArray) returns;
        assertEquals(resultArray.size(), 5);
    }

    @Test
    public void testTwoDimensionalArrayFill() {
        Object returns = BRunUtil.invoke(compileResult, "testTwoDimensionalArrayFill");
        BArray resultArray = (BArray) returns;
        assertEquals(resultArray.size(), 2);
        assertEquals(resultArray.getRefValue(0).toString(), "[]");
        assertEquals(resultArray.getRefValue(1).toString(), "[1,3]");
    }

    @Test
    public void testArrayFillWithObjs() {
        Object returns = BRunUtil.invoke(compileResult, "testArrayFillWithObjs");
        BArray resultArray = (BArray) returns;
        assertEquals(resultArray.size(), 3);
    }

    @Test
    public void testFiniteTypesInUnionArrayFill() {
        Object returns = BRunUtil.invoke(compileResult, "testFiniteTypeArrayFill");
        Assert.assertEquals(getType(returns).getTag(), TypeTags.ARRAY_TAG);
        Assert.assertEquals(((BArray) returns).getValues()[5].toString(), "1.2");
    }

    @Test
    public void testReadOnlyArrayFill() {
        BRunUtil.invoke(compileResult, "testReadOnlyArrayFill");
    }

    @Test
    public void testFiniteTypeUnionArrayFill() {
        BRunUtil.invoke(compileResult, "testFiniteTypeUnionArrayFill");
    }

    private void validateMapValue(BMap<String, Object> actual, BMap<String, Object> expected) {
        assertEquals(actual.size(), expected.size());

        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            assertEquals(actual.get(entry.getKey()), entry.getValue());
        }
    }

    @Test
    public void testXMLSubtypesArrayFill() {
        BRunUtil.invoke(compileResult, "testXMLSubtypesArrayFill");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
