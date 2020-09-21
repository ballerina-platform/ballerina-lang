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

import org.ballerinalang.core.model.types.BRecordType;
import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.util.Flags;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;

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
    private CompileResult negativeCompileResult;
    private final long index = 250;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/arrays/array-fill-test.bal");
        negativeCompileResult =
                BCompileUtil.compile("test-src/statements/arrays/array-fill-test-negative.bal");
    }

    @Test
    public void testNilArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index), null};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testNilArrayFill", args);
        BValueArray nilArr = (BValueArray) returns[0];
        assertEquals(nilArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(nilArr.getBValue(i));
        }

        assertNull(nilArr.getBValue(index));
    }

    @Test
    public void testBooleanArrayFill() {
        final boolean value = true;
        final int bFALSE = 0;
        final int bTRUE = 1;
        BValue[] args = new BValue[]{new BInteger(index), new BBoolean(value)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanArrayFill", args);
        BValueArray booleanArr = (BValueArray) returns[0];
        assertEquals(booleanArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(booleanArr.getBoolean(i), bFALSE);
        }

        assertEquals(booleanArr.getBoolean(index), bTRUE);
    }

    @Test
    public void testIntArrayFill() {
        final long value = 100;
        BValue[] args = new BValue[]{new BInteger(index), new BInteger(value)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntArrayFill", args);
        BValueArray intArr = (BValueArray) returns[0];
        assertEquals(intArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(intArr.getInt(i), 0);
        }

        assertEquals(intArr.getInt(index), value);
    }

    @Test
    public void testFloatArrayFill() {
        final double value = 23.45;
        BValue[] args = new BValue[]{new BInteger(index), new BFloat(value)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatArrayFill", args);
        BValueArray floatArr = (BValueArray) returns[0];
        assertEquals(floatArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(floatArr.getFloat(i), 0.0);
        }

        assertEquals(floatArr.getFloat(index), value);
    }

    @Test
    public void testDecimalArrayFill() {
        final BigDecimal value = new BigDecimal("23.45");
        BValue[] args = new BValue[]{new BInteger(index), new BDecimal(value)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testDecimalArrayFill", args);
        BValueArray decimalArr = (BValueArray) returns[0];
        assertEquals(decimalArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(((BDecimal) decimalArr.getBValue(i)).decimalValue(), BigDecimal.ZERO);
        }

        assertEquals(((BDecimal) decimalArr.getBValue(index)).decimalValue(), value);
    }

    @Test
    public void testStringArrayFill() {
        final String value = "Hello World!";
        BValue[] args = new BValue[]{new BInteger(index), new BString(value)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testStringArrayFill", args);
        BValueArray stringArr = (BValueArray) returns[0];
        assertEquals(stringArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(stringArr.getString(i), "");
        }

        assertEquals(stringArr.getString(index), value);
    }

    @Test
    public void testArrayOfArraysFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testArrayOfArraysFill", args);
        BValueArray arr2d = (BValueArray) returns[0];
        assertEquals(arr2d.size(), index + 1);

        for (int i = 0; i < index; i++) {
            BValueArray arr = (BValueArray) arr2d.getBValue(i);
            assertEquals(arr.stringValue(), "[]");
        }

        BValueArray arr = (BValueArray) arr2d.getBValue(index);
        assertEquals(arr.stringValue(), "[{name:\"John\", age:25}, {name:\"John\", age:25}]");
    }

    @Test
    public void testTupleArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testTupleArrayFill", args);
        BValueArray tupleArr = (BValueArray) returns[0];
        assertEquals(tupleArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            BValueArray tuple = (BValueArray) tupleArr.getBValue(i);
            assertEquals(tuple.getBValue(0).stringValue(), "");
            assertEquals(((BInteger) tuple.getBValue(1)).intValue(), 0);
        }

        BValueArray tuple = (BValueArray) tupleArr.getBValue(index);
        assertEquals(tuple.getBValue(0).stringValue(), "Hello World!");
        assertEquals(((BInteger) tuple.getBValue(1)).intValue(), 100);
    }

    @Test
    public void testTupleSealedArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testTupleSealedArrayFill", args);
        BValueArray tupleArr = (BValueArray) returns[0];
        assertEquals(tupleArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            BValueArray tuple = (BValueArray) tupleArr.getBValue(i);
            assertEquals(tuple.getBValue(0).stringValue(), "");
            assertEquals(((BInteger) tuple.getBValue(1)).intValue(), 0);
        }

        BValueArray tuple = (BValueArray) tupleArr.getBValue(index);
        assertEquals(tuple.getBValue(0).stringValue(), "Hello World!");
        assertEquals(((BInteger) tuple.getBValue(1)).intValue(), 100);
    }

    @Test
    public void testMapArrayFill() {
        BMap<String, BValue> emptyMap = new BMap<>(BTypes.typeMap);
        BMap<String, BValue> map = new BMap<>(BTypes.typeMap);
        map.put("1", new BInteger(1));
        BValue[] args = new BValue[]{new BInteger(index), map};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testMapArrayFill", args);
        BValueArray mapArr = (BValueArray) returns[0];
        assertEquals(mapArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            validateMapValue((BMap<String, BValue>) mapArr.getBValue(i), emptyMap);
        }

        validateMapValue((BMap<String, BValue>) mapArr.getBValue(index), map);
    }

    @Test
    public void testTableArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BRunUtil.invoke(compileResult, "testTableArrayFill", args);
    }

    @Test
    public void testXMLArrayFill() {
        final String value = "<name>Pubudu</name>";
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testXMLArrayFill", args);
        BValueArray xmlArr = (BValueArray) returns[0];
        assertEquals(xmlArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(xmlArr.getBValue(i).stringValue(), "");
        }

        assertEquals(xmlArr.getBValue(index).stringValue(), value);
    }

    @Test
    public void testUnionArrayFill1() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUnionArrayFill1", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.getBValue(i));
        }

        assertEquals(unionArr.getBValue(index).stringValue(), "Hello World!");
    }

    @Test
    public void testUnionArrayFill2() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUnionArrayFill2", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(unionArr.getBValue(i).stringValue(), "");
        }

        assertEquals(unionArr.getBValue(index).stringValue(), "Hello World!");
    }

    @Test
    public void testUnionArrayFill3() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUnionArrayFill3", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.getBValue(i));
        }

        assertEquals(unionArr.getBValue(index).stringValue(), "{name:\"John\", age:25}");
    }

    @Test
    public void testUnionArrayFill4() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testUnionArrayFill4", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(((BInteger) unionArr.getBValue(i)).intValue(), 0);
        }

        assertEquals(((BInteger) unionArr.getBValue(index)).intValue(), 1);
    }

    @Test
    public void testFiniteTypeArrayFill1() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testFiniteTypeArrayFill1", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(((BInteger) unionArr.getBValue(i)).intValue(), 0);
        }

        assertEquals(((BInteger) unionArr.getBValue(index)).intValue(), 5);
    }

    @Test
    public void testFiniteTypeArrayFill2() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testFiniteTypeArrayFill2", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(((BFloat) unionArr.getBValue(i)).floatValue(), 0.0);
        }

        assertEquals(((BFloat) unionArr.getBValue(index)).floatValue(), 1.2);
    }

    @Test
    public void testFiniteTypeArrayFill3() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testFiniteTypeArrayFill3", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertFalse(((BBoolean) unionArr.getBValue(i)).booleanValue());
        }

        assertTrue(((BBoolean) unionArr.getBValue(index)).booleanValue());
    }

    @Test
    public void testFiniteTypeArrayFill4() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testFiniteTypeArrayFill4", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.getBValue(i));
        }

        assertTrue(((BBoolean) unionArr.getBValue(index)).booleanValue());
    }

    @Test
    public void testFiniteTypeArrayFill5() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testFiniteTypeArrayFill5", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(((BDecimal) unionArr.getBValue(i)).decimalValue(), new BigDecimal("0.0"));
        }

        assertEquals(((BDecimal) unionArr.getBValue(index)).decimalValue(), new BigDecimal("1.2"));
    }

    @Test
    public void testOptionalTypeArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testOptionalTypeArrayFill", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.getBValue(i));
        }

        assertEquals(unionArr.getBValue(index).stringValue(), "Hello World!");
    }

    @Test
    public void testAnyArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testAnyArrayFill", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.getBValue(i));
        }

        assertEquals(unionArr.getBValue(index).stringValue(), "{name:\"John\", age:25}");
    }

    @Test
    public void testAnySealedArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testAnySealedArrayFill", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.getBValue(i));
        }

        assertEquals(unionArr.getBValue(index).stringValue(), "{name:\"John\", age:25}");
    }

    @Test
    public void testAnydataArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testAnydataArrayFill", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.getBValue(i));
        }

        assertEquals(unionArr.getBValue(index).stringValue(), "{name:\"John\", age:25}");
    }

    @Test
    public void testAnydataSealedArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testAnydataSealedArrayFill", args);
        BValueArray unionArr = (BValueArray) returns[0];
        assertEquals(unionArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(unionArr.getBValue(i));
        }

        assertEquals(unionArr.getBValue(index).stringValue(), "{name:\"John\", age:25}");
    }

    @Test
    public void testByteArrayFill() {
        final byte value = 100;
        BValue[] args = new BValue[]{new BInteger(index), new BByte(value)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteArrayFill", args);
        BValueArray byteArr = (BValueArray) returns[0];
        assertEquals(byteArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(byteArr.getByte(i), 0);
        }

        assertEquals(byteArr.getByte(index), value);
    }

    @Test
    public void testJSONArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testJSONArrayFill", args);
        BValueArray jsonArr = (BValueArray) returns[0];
        assertEquals(jsonArr.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertNull(jsonArr.getBValue(i));
        }

        assertEquals(jsonArr.getBValue(index).stringValue(), "{\"name\":\"John\", \"age\":25}");
    }

    @Test
    public void testSingletonTypeArrayFill() {
        BValue[] args = new BValue[]{new BInteger(index)};
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testSingletonTypeArrayFill", args);
        BValueArray singletonArray = (BValueArray) returns[0];
        assertEquals(singletonArray.size(), index + 1);

        for (int i = 0; i < index; i++) {
            assertEquals(singletonArray.getRefValue(i).stringValue(), "1");
        }

        assertEquals(singletonArray.getBValue(index).stringValue(), "1");
    }

    @Test
    public void testSingletonTypeArrayFill1() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testSingletonTypeArrayFill1");
        BValueArray singletonArray = (BValueArray) returns[0];
        assertEquals(singletonArray.size(), 2);
        assertEquals(singletonArray.getRefValue(0).stringValue(), "true");
        assertEquals(singletonArray.getRefValue(1).stringValue(), "true");
    }

    @Test
    public void testSingletonTypeArrayStaticFill() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testSingletonTypeArrayStaticFill");
        BValueArray singletonArray = (BValueArray) returns[0];
        assertEquals(singletonArray.size(), 2);
        assertEquals(singletonArray.getRefValue(0).stringValue(), "true");
        assertEquals(singletonArray.getRefValue(1).stringValue(), "true");
    }

    @Test
    public void testSequentialArrayInsertion() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testSequentialArrayInsertion");
        BValueArray resultArray = (BValueArray) returns[0];
        assertEquals(resultArray.size(), 5);
    }

    @Test
    public void testTwoDimensionalArrayFill() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testTwoDimensionalArrayFill");
        BValueArray resultArray = (BValueArray) returns[0];
        assertEquals(resultArray.size(), 2);
        assertEquals(resultArray.getRefValue(0).stringValue(), "[]");
        assertEquals(resultArray.getRefValue(1).stringValue(), "[1, 3]");
    }

    @Test
    public void testArrayFillWithObjs() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testArrayFillWithObjs");
        BValueArray resultArray = (BValueArray) returns[0];
        assertEquals(resultArray.size(), 3);
    }

    @Test
    public void testFiniteTypesInUnionArrayFill() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testFiniteTypeArrayFill");
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);
        Assert.assertEquals(((BValueArray) returns[0]).getValues()[5].value().toString(), "1.2");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testArrayFillWithObjWithInitParam() {
        BRunUtil.invokeFunction(negativeCompileResult, "testArrayFillWithObjWithInitParam");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testArrayFillWithIntFiniteTypes() {
        BRunUtil.invokeFunction(negativeCompileResult, "testArrayFillWithIntFiniteTypes");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testArrayFillWithFloatFiniteTypes() {
        BRunUtil.invokeFunction(negativeCompileResult, "testArrayFillWithFloatFiniteTypes");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testArrayFillWithStringFiniteTypes() {
        BRunUtil.invokeFunction(negativeCompileResult, "testArrayFillWithStringFiniteTypes");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testArrayFillWithTypedesc() {
        BRunUtil.invokeFunction(negativeCompileResult, "testArrayFillWithTypedesc");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testNonSequentialArrayInsertion() {
        BRunUtil.invokeFunction(negativeCompileResult, "testNonSequentialArrayInsertion");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testIllegalArrayInsertion() {
        BRunUtil.invokeFunction(negativeCompileResult, "testIllegalArrayInsertion");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testIllegalTwoDimensionalArrayInsertion() {
        BRunUtil.invokeFunction(negativeCompileResult, "testIllegalTwoDimensionalArrayInsertion");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testRecordTypeWithRequiredFieldsArrayFill() {
        BRunUtil.invokeFunction(negativeCompileResult, "testRecordTypeWithRequiredFieldsArrayFill");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testFiniteTypeArrayFillNegative() {
        BRunUtil.invokeFunction(negativeCompileResult, "testFiniteTypeArrayFill");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*array of length .* cannot be expanded into array of length .* " +
                    "without filler values.*")
    public void testFiniteTypeArrayFillNegative2() {
        BRunUtil.invokeFunction(negativeCompileResult, "testFiniteTypeArrayFill2");
    }

    private void validateMapValue(BMap<String, BValue> actual, BMap<String, BValue> expected) {
        assertEquals(actual.size(), expected.size());

        expected.getMap().forEach((key, value) -> {
            assertEquals(((BInteger) actual.get(key)).intValue(), ((BInteger) value).intValue());
        });
    }

    private BMap<String, BValue> getImplicitInitRecordValue(BRecordType recordType) {
        BMap<String, BValue> record = new BMap<>(recordType);
        recordType.getFields().entrySet().stream()
                .filter(entry -> !Flags.isFlagOn(entry.getValue().flags, Flags.OPTIONAL))
                .forEach(entry -> record.put(entry.getKey(), entry.getValue().fieldType.getZeroValue()));
        return record;
    }
}
