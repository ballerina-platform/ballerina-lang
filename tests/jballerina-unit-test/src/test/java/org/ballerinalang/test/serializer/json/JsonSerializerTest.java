/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.serializer.json;

import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.util.serializer.JsonSerializer;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValueArray;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test Serialization of objects of various java types.
 * <p>
 * Note: make sure to test reference sharing.
 */
public class JsonSerializerTest {
    private static final String STRING_1 = "String1";
    private static final String STRING_2 = "String2";

    @Test(description = "Test serializing any ArrayList")
    public void testObjectSerializationToJson() {
        JsonSerializer jsonSerializer = new JsonSerializer();
        String json = jsonSerializer.serialize(Arrays.asList("1", "2", "3"));

        Assert.assertTrue(json.contains("\"1\", \"2\", \"3\""));

        String numJson = jsonSerializer.serialize(Arrays.asList(3, 3, 3, 3, 3));
        Assert.assertTrue(numJson.contains("3, 3, 3, 3, 3"));
    }

    @Test(description = "Test deserialization of BValueArray")
    public void testJsonDeserializeBRefValueArrayReconstruction() {
        BValueArray array = new BValueArray(new BArrayType(BTypes.typeAny));
        BString str1 = new BString(STRING_1);
        BString str2 = new BString(STRING_2);
        BInteger bint = new BInteger(4343);
        array.append(str1);
        array.append(str2);
        array.append(str1);
        array.append(bint);

        String serialize = new JsonSerializer().serialize(array);
        BValueArray deArray = new JsonSerializer().deserialize(serialize, BValueArray.class);

        BString string1 = (BString) deArray.getRefValue(0);
        Assert.assertEquals(string1.value(), STRING_1);
        // reference sharing test
        Assert.assertSame(deArray.getRefValue(0), deArray.getRefValue(2));

        Assert.assertEquals(4343, ((BInteger) deArray.getRefValue(3)).intValue());
    }

    @SuppressWarnings("unchecked")
    @Test(description = "Test deserialization of BValueArray when elements are maps")
    public void testJsonDeserializeBRefValueArrayReconstructionWithMapElements() {
        BValueArray array = new BValueArray(new BArrayType(BTypes.typeMap));
        BString str1 = new BString(STRING_1);
        BString str2 = new BString(STRING_2);
        BMap<String, BString> map1 = new BMap<>();
        map1.put("A", str1);
        map1.put("B", str2);
        BMap<String, BString> map2 = new BMap<>();
        map1.put("A", str1);
        BMap<String, BString> map3 = new BMap<>();
        array.append(map1);
        array.append(map2);
        // append same item
        array.append(map3);
        array.append(map3);

        String serialize = new JsonSerializer().serialize(array);
        BValueArray deArray = new JsonSerializer().deserialize(serialize, BValueArray.class);

        BMap map = (BMap) deArray.getRefValue(0);
        Assert.assertEquals(((BString) map.get("A")).value(), STRING_1);
        // reference sharing test
        //noinspection SimplifiedTestNGAssertion
        Assert.assertTrue(deArray.getRefValue(2) == deArray.getRefValue(3));
    }

    @Test(description = "Test deserialization of StringFieldA[]")
    public void testJsonDeserializeArrayOfStringFieldA() {
        StringFieldA sf0 = new StringFieldA("A");
        StringFieldA sf1 = new StringFieldA("B");
        StringFieldA[] array = {sf0, sf1, sf1};

        String serialize = new JsonSerializer().serialize(array);
        Object deserialize = new JsonSerializer().deserialize(serialize, StringFieldA[].class);

        StringFieldA[] array1 = (StringFieldA[]) deserialize;
        Assert.assertEquals(array1.length, array.length);
        Assert.assertEquals(array1[0].a, array[0].a);
        Assert.assertEquals(array1[1].a, array[1].a);
        Assert.assertEquals(array1[2].a, array[2].a);

        // test reference sharing
        //noinspection SimplifiedTestNGAssertion
        Assert.assertTrue(array1[1] == array1[2]);
    }

    @Test(description = "Test deserialization of null value")
    public void testJsonDeserializeNullValue() {
        StringFieldA sf0 = new StringFieldA("A");
        StringFieldA sf1 = new StringFieldA("B");
        StringFieldA[] array = {sf0, sf1, null};

        String serialize = new JsonSerializer().serialize(array);
        Object deserialize = new JsonSerializer().deserialize(serialize, StringFieldA[].class);

        StringFieldA[] array1 = (StringFieldA[]) deserialize;
        Assert.assertEquals(array1.length, array.length);
        Assert.assertEquals(array1[0].a, array[0].a);
        Assert.assertEquals(array1[1].a, array[1].a);
        //noinspection SimplifiedTestNGAssertion
        Assert.assertTrue(array[2] == null);
    }

    @Test(description = "Test source array length == destination array len")
    public void testJsonDeserializeArraySize() {
        int[] array = new int[5];
        array[0] = 44;
        array[1] = 44;

        String serialize = new JsonSerializer().serialize(array);
        int[] array1 = new JsonSerializer().deserialize(serialize, int[].class);

        Assert.assertEquals(array1.length, array.length);
        Assert.assertEquals(array1[0], array[0]);
        Assert.assertEquals(array[1], array[1]);
    }

    @Test(description = "Test source array length == destination array list len")
    public void testJsonDeserializeArrayListSize() {
        ArrayList<Integer> integers = new ArrayList<>(5);
        integers.add(1);
        integers.add(1);
        integers.add(1);
        integers.add(1);

        String serialize = new JsonSerializer().serialize(integers);
        ArrayList array1 = new JsonSerializer().deserialize(serialize, ArrayList.class);

        Assert.assertEquals(array1.size(), integers.size());
    }

    @Test(description = "Test deserialization of StringFieldA[][]")
    public void testJsonDeserializeArrayOfArrayOfStringFieldA() {
        StringFieldA sf0 = new StringFieldA("A");
        StringFieldA sf1 = new StringFieldA("B");
        StringFieldA[][] array = {new StringFieldA[]{sf0, sf1, sf1}, new StringFieldA[]{sf0, sf1, sf1}};

        String serialize = new JsonSerializer().serialize(array);
        Object deserialize = new JsonSerializer().deserialize(serialize, StringFieldA[][].class);

        StringFieldA[][] array1 = (StringFieldA[][]) deserialize;
        Assert.assertEquals(array1.length, array.length);
        Assert.assertEquals(array1[0][0].a, "A");
        Assert.assertEquals(array1[0][1].a, "B");
        Assert.assertEquals(array1[0][2].a, "B");
        Assert.assertEquals(array1[1][0].a, "A");
        Assert.assertEquals(array1[1][1].a, "B");
        Assert.assertEquals(array1[1][2].a, "B");
    }

    @Test(description = "Test deserialization of class with a array field")
    public void testJsonDeserializeClassWithArrayField() {
        int[] array = {1, 2, 3, 4};
        ArrayField af = new ArrayField(array);

        String serialize = new JsonSerializer().serialize(af);
        ArrayField deserialize = new JsonSerializer().deserialize(serialize, ArrayField.class);

        Assert.assertEquals(deserialize.array[0], array[0]);
        Assert.assertEquals(deserialize.array[1], array[1]);
        Assert.assertEquals(deserialize.array[2], array[2]);
        Assert.assertEquals(deserialize.array[3], array[3]);
    }


    @Test(description = "Test serialize/deserialize Object array assignment")
    public void testJsonDeserializeObjectArrayAssignment() {
        TestClass[] items = {
                new TestClass("Item1"),
                new TestClass("Item2")};
        TestClass testClassInst = new TestClass(items);
        String serialize = new JsonSerializer().serialize(testClassInst);
        TestClass targetClass = new JsonSerializer().deserialize(serialize, TestClass.class);
        TestClass[] arr = (TestClass[]) targetClass.obj;

        Assert.assertEquals("Item1", arr[0].obj);
        //noinspection SimplifiedTestNGAssertion
        Assert.assertTrue(arr[1].obj.equals("Item2"));
    }

    @Test(description = "Test serializing Char/char")
    public void testCharSerialization() {
        JsonSerializer jsonSerializer = new JsonSerializer();

        char ch = '2';
        String serial = jsonSerializer.serialize(ch);
        Character deserialize = jsonSerializer.deserialize(serial, char.class);
        Assert.assertEquals(Character.valueOf(ch), deserialize);

        Character ch2 = ch; // box
        String ch2Json = jsonSerializer.serialize(ch2);
        Character ch2FromJ = jsonSerializer.deserialize(ch2Json, Character.class);
        Assert.assertEquals(ch2, ch2FromJ);
    }

    @Test(description = "Test serializing short")
    public void testShortSerialization() {
        short sh = 2;
        String serial = new JsonSerializer().serialize(sh);
        Short deserialize = new JsonSerializer().deserialize(serial, short.class);
        Assert.assertEquals(Short.valueOf(sh), deserialize);
    }

    @Test(description = "Test serializing byte")
    public void testByteSerialization() {
        byte b = 2;
        String serial = new JsonSerializer().serialize(b);
        Byte deserialize = new JsonSerializer().deserialize(serial, byte.class);
        Assert.assertEquals(Byte.valueOf(b), deserialize);
    }

    @Test(description = "Test serializing Char[]")
    public void testCharArraySerialization() {
        char[] chs = new char[]{'a', 'b', 'c'};
        String serial = new JsonSerializer().serialize(chs);
        char[] deserialize = new JsonSerializer().deserialize(serial, char[].class);

        Assert.assertEquals(chs, deserialize);
    }

    @Test(description = "Test serializing Enum")
    public void testCharEnumSerialization() {
        TestEnum[] enumArray = new TestEnum[]{TestEnum.Item_1, TestEnum.Item_N};
        String serial = new JsonSerializer().serialize(enumArray);
        TestEnum[] deserialize = new JsonSerializer().deserialize(serial, TestEnum[].class);

        Assert.assertEquals(deserialize, enumArray);
    }

    @Test(description = "Test serializing float array")
    public void testFloatArraySerialization() {
        float[] floats = new float[]{2.0f, 3.6f, 35066.22045f};
        String serial = new JsonSerializer().serialize(floats);
        float[] deserialize = new JsonSerializer().deserialize(serial, float[].class);

        Assert.assertEquals(deserialize, floats);
    }

    @Test(description = "Test serializing double array")
    public void testDoubleArraySerialization() {
        double[] doubles = new double[]{2.0, 3.6, 35066.22045};
        String serial = new JsonSerializer().serialize(doubles);
        double[] deserialize = new JsonSerializer().deserialize(serial, double[].class);

        Assert.assertEquals(deserialize, doubles);
    }

    @Test(description = "Test serializing int array")
    public void testIntArraySerialization() {
        int[] ints = new int[]{1, 2, 4, 4, 4};
        String serial = new JsonSerializer().serialize(ints);
        int[] deserialize = new JsonSerializer().deserialize(serial, int[].class);

        Assert.assertEquals(deserialize, ints);
    }

    @Test(description = "Test serializing long array")
    public void testLongArraySerialization() {
        long[] longs = new long[]{1, 2, 4, 4, 4};
        String serial = new JsonSerializer().serialize(longs);
        long[] deserialize = new JsonSerializer().deserialize(serial, long[].class);

        Assert.assertEquals(deserialize, longs);
    }

    @Test(description = "Test serializing byte array")
    public void testByteArraySerialization() {
        JsonSerializer serializer = new JsonSerializer();
        byte[] bytes = new byte[]{1, 2, 4, 4, 4};
        String serial = serializer.serialize(bytes);
        byte[] deserialize = serializer.deserialize(serial, byte[].class);

        Assert.assertEquals(deserialize, bytes);

        Byte[] bytes2 = new Byte[]{1, 2, 3, 4, 5};
        serial = serializer.serialize(bytes2);
        Byte[] deserialized2 = serializer.deserialize(serial, Byte[].class);

        Assert.assertEquals(bytes2, deserialized2);
    }

    @Test(description = "Test serializing string array")
    public void testStringArraySerialization() {
        String[] strings = new String[]{"Foo", "Bar", "Baz", "F"};
        String serial = new JsonSerializer().serialize(strings);
        String[] deserialize = new JsonSerializer().deserialize(serial, String[].class);

        Assert.assertEquals(deserialize, strings);
    }

    @Test(description = "Test serializing BArray")
    public void testBArraySerialization() {
        BArrayType bArrayType = new BArrayType(BTypes.typeString);
        String serial = new JsonSerializer().serialize(bArrayType);
        BArrayType deserialize = new JsonSerializer().deserialize(serial, BArrayType.class);

        Assert.assertEquals(deserialize, bArrayType);
    }

    @Test(description = "Test serializing class with primitive typed members")
    public void testPrimitiveMemberSerialization() {
        PrimitiveMembers intMemberClass = new PrimitiveMembers(5);
        String serial = new JsonSerializer().serialize(intMemberClass);
        PrimitiveMembers deserialize = new JsonSerializer().deserialize(serial, PrimitiveMembers.class);

        Assert.assertEquals(deserialize.i, intMemberClass.i);
    }

    @SuppressWarnings("unused")
    enum TestEnum {
        Item_1, Item_2, Item_N
    }

    static class TestClass {
        final Object obj;

        TestClass(Object obj) {
            this.obj = obj;
        }
    }

    /**
     * Class with a single String field.
     */
    public static class StringFieldA {
        public final String a;

        public StringFieldA(String a) {
            this.a = a;
        }
    }

    /**
     * Class with single String field which inherits from a similar class.
     */
    public static class StringFieldAB extends StringFieldA {
        public final String b;

        public StringFieldAB(String a, String b) {
            super(a);
            this.b = b;
        }
    }

    static class ArrayField {
        final int[] array;

        ArrayField(int[] array) {
            this.array = array;
        }
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    static class PrimitiveMembers {
        final int i;
        public byte b;
        public char c;
        public short s;
        public float f;
        public double d;

        PrimitiveMembers(int i) {
            this.i = i;
        }
    }
}
