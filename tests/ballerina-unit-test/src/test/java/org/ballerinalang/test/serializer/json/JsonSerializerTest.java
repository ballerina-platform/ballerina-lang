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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.serializer.JsonSerializer;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.persistence.store.PersistenceStore;
import org.ballerinalang.test.checkpointing.TestStorageProvider;
import org.ballerinalang.test.utils.debug.TestDebugger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Test Serialization of objects of various java types.
 * <p>
 * Note: make sure to test reference sharing.
 */
public class JsonSerializerTest {
    public static final String STRING_1 = "String1";
    public static final String STRING_2 = "String2";
    private CompileResult compileResult;
    private TestStorageProvider storageProvider;

    @BeforeClass
    public void setup() {
        storageProvider = new TestStorageProvider();
        PersistenceStore.setStorageProvider(storageProvider);
        compileResult = BCompileUtil.compile("test-src/checkpointing/checkpoint.bal");
        TestDebugger debugger = new TestDebugger(compileResult.getProgFile());
        compileResult.getProgFile().setDebugger(debugger);

    }

    @Test(description = "Test serializing any ArrayList")
    public void testObjectSerializationToJson() {
        JsonSerializer jsonSerializer = new JsonSerializer();
        String json = jsonSerializer.serialize(Arrays.asList("1", "2", "3"));

        Assert.assertTrue(json.contains("\"1\", \"2\", \"3\""));

        String numJson = jsonSerializer.serialize(Arrays.asList(3, 3, 3, 3, 3));
        Assert.assertTrue(numJson.contains("3, 3, 3, 3, 3"));
    }

    @Test(description = "Test deserialization of BRefValueArray")
    public void testJsonDeserializeBRefValueArrayReconstruction() {
        BRefValueArray array = new BRefValueArray(BTypes.typeAny);
        BString str1 = new BString(STRING_1);
        BString str2 = new BString(STRING_2);
        BInteger bint = new BInteger(4343);
        array.append(str1);
        array.append(str2);
        array.append(str1);
        array.append(bint);

        String serialize = new JsonSerializer().serialize(array);
        BRefValueArray deArray = new JsonSerializer().deserialize(serialize, BRefValueArray.class);

        BString string1 = (BString) deArray.get(0);
        Assert.assertEquals(string1.value(), STRING_1);
        // reference sharing test
        Assert.assertTrue(deArray.get(0) == deArray.get(2));

        Assert.assertTrue(((BInteger) deArray.get(3)).intValue() == 4343);
    }

    @Test(description = "Test deserialization of BRefValueArray when elements are maps")
    public void testJsonDeserializeBRefValueArrayReconstructionWithMapElements() {
        BRefValueArray array = new BRefValueArray(BTypes.typeMap);
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
        BRefValueArray deArray = new JsonSerializer().deserialize(serialize, BRefValueArray.class);

        BMap map = (BMap) deArray.get(0);
        Assert.assertEquals(((BString) map.get("A")).value(), STRING_1);
        // reference sharing test
        Assert.assertTrue(deArray.get(2) == deArray.get(3));
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
        Assert.assertTrue(array1[1] == array1[2]);
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
        Object deserialize = new JsonSerializer().deserialize(serialize, ArrayField.class);

        ArrayField temp = (ArrayField) deserialize;
        Assert.assertEquals(temp.array[0], array[0]);
        Assert.assertEquals(temp.array[1], array[1]);
        Assert.assertEquals(temp.array[2], array[2]);
        Assert.assertEquals(temp.array[3], array[3]);
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

        Assert.assertTrue(arr[0].obj.equals("Item1"));
        Assert.assertTrue(arr[1].obj.equals("Item2"));
    }

    @Test(description = "Test serializing Char/char")
    public void testCharSerialization() {
        char ch = '2';
        String serial = new JsonSerializer().serialize(ch);
        Character deserialize = new JsonSerializer().deserialize(serial, char.class);
        Assert.assertEquals(Character.valueOf(ch), deserialize);
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
        Byte b = 2;
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
    public void testCharFloatSerialization() {
        float[] floats = new float[]{2.0f, 3.6f, 35066.22045f};
        String serial = new JsonSerializer().serialize(floats);
        float[] deserialize = new JsonSerializer().deserialize(serial, float[].class);

        Assert.assertEquals(deserialize, floats);
    }

    @Test(description = "Test serializing double array")
    public void testCharDoubleSerialization() {
        double[] doubles = new double[]{2.0, 3.6, 35066.22045};
        String serial = new JsonSerializer().serialize(doubles);
        double[] deserialize = new JsonSerializer().deserialize(serial, double[].class);

        Assert.assertEquals(deserialize, doubles);
    }

    public static class TestClass {
        Object obj;

        public TestClass(Object obj) {
            this.obj = obj;
        }
    }

    public static class StringFieldA {
        public final String a;

        public StringFieldA(String a) {
            this.a = a;
        }
    }

    public static class StringFieldAB extends StringFieldA {
        public final String b;

        public StringFieldAB(String a, String b) {
            super(a);
            this.b = b;
        }
    }

    public static class ArrayField {
        public int[] array;

        public ArrayField(int[] array) {
            this.array = array;
        }
    }


    public enum TestEnum {
        Item_1, Item_2, Item_N
    }
}
