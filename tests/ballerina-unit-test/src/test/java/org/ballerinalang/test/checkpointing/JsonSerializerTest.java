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
package org.ballerinalang.test.checkpointing;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBMap;
import org.ballerinalang.persistence.serializable.serializer.JsonSerializer;
import org.ballerinalang.persistence.store.PersistenceStore;
import org.ballerinalang.test.utils.debug.TestDebugger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonSerializerTest {
    private static final String INSTANCE_ID = "ABC123";
    private CompileResult compileResult;
    private TestStorageProvider storageProvider;
    private BigDecimal bigDecimal;

    @BeforeClass
    public void setup() {
        storageProvider = new TestStorageProvider();
        PersistenceStore.setStorageProvider(storageProvider);
        compileResult = BCompileUtil.compile("test-src/checkpointing/checkpoint.bal");
        TestDebugger debugger = new TestDebugger(compileResult.getProgFile());
        compileResult.getProgFile().setDebugger(debugger);

    }

    @Test(description = "Test serializing simple mocked SerializableState object")
    public void testJsonSerializerWithMockedSerializableState() {
        WorkerExecutionContext weContext = new WorkerExecutionContext(compileResult.getProgFile());
        SerializableState serializableState = new SerializableState(weContext);
        mock(serializableState);

        String json = serializableState.serialize();

        Assert.assertTrue(json.matches(".*?\"Item-1\", ?\"Item-2\", ?\"Item-3\".*"));
        Assert.assertTrue(json.matches(".*?\"var_r1\" ?: ?\\{.*"));
        Assert.assertTrue(json.contains("bmap_str_val"));
    }

    @Test(description = "Text serializing any Object")
    public void testObjectSerializationToJson() {
        JsonSerializer jsonSerializer = new JsonSerializer();
        String json = jsonSerializer.serialize(Arrays.asList("1", "2", "3"));

        Assert.assertTrue(json.contains("\"1\", \"2\", \"3\""));

        String numJson = jsonSerializer.serialize(Arrays.asList(3, 3, 3, 3, 3));
        Assert.assertTrue(numJson.contains("3, 3, 3, 3, 3"));
    }

    @Test(description = "Test deserialization of JSON into SerializableState object")
    public void testJsonDeserializeSerializableState() {
        WorkerExecutionContext weContext = new WorkerExecutionContext(compileResult.getProgFile());
        SerializableState serializableState = new SerializableState(weContext);
        serializableState.setId(INSTANCE_ID);
        mock(serializableState);
        String json = serializableState.serialize();

        SerializableState state = serializableState.deserialize(json);
        Assert.assertEquals(state.getId(), INSTANCE_ID);

        List list = (List) state.globalProps.get("gProp2");
        Assert.assertEquals("Item-1", list.get(0));
        Assert.assertEquals("Item-2", list.get(1));
        Assert.assertEquals("Item-3", list.get(2));
    }

    @Test(description = "Test deserialization of Deeply nested BMap")
    public void testJsonDeserializeSerializableStateDeepBMapReconstruction() {
        WorkerExecutionContext weContext = new WorkerExecutionContext(compileResult.getProgFile());
        SerializableState serializableState = new SerializableState(weContext);
        serializableState.setId(INSTANCE_ID);
        mock(serializableState);
        String json = serializableState.serialize();

        SerializableState state = serializableState.deserialize(json);
        SerializableRefType bmapKey1 = state.sRefTypes.get("var_r1");
        BMap<String, BValue> bmap = (BMap) bmapKey1.getBRefType(compileResult.getProgFile(), state, new Deserializer());
        BString value = (BString) bmap.get("bmapKey1");
        Assert.assertEquals(value.value(), "bmap_str_val");
        StringFieldAB multiLevel = (StringFieldAB) state.globalProps.get("multiLevel");
        Assert.assertEquals(multiLevel.B, "B");
        Assert.assertEquals(multiLevel.A, "A");

        BigDecimal dec = (BigDecimal) state.globalProps.get("dec");
        Assert.assertTrue(dec.equals(bigDecimal));

        // reference sharing test for BString object
        Object gProp1 = state.globalProps.get("gProp1");
        BValue bstring = bmap.get("bstring");
        Assert.assertEquals(gProp1, bstring);
    }

    private void mock(SerializableState serializableState) {
        BString bstring = new BString("gProp1:BString");
        serializableState.globalProps.put("gProp1", bstring);
        serializableState.globalProps.put("gProp2", Arrays.asList("Item-1", "Item-2", "Item-3"));
        serializableState.globalProps.put("multiLevel", new StringFieldAB("A", "B"));
        bigDecimal = new BigDecimal("123456789999");
        serializableState.globalProps.put("dec", bigDecimal);

        BMap<String, BRefType> bMap = new BMap<>();
        bMap.put("bmapKey1", new BString("bmap_str_val"));
        bMap.put("obj1", getInnerBMap());
        bMap.put("bstring", bstring);
        serializableState.sRefTypes.put("var_r1", new SerializableBMap(bMap, serializableState));
    }

    private BRefType getInnerBMap() {
        BMap<String, BRefType> map = new BMap<>();
        map.put("A", new BString("A"));
        map.put("B", new BString("B"));
        BRefValueArray value = new BRefValueArray(new BArrayType(BTypes.typeString));
        value.append(new BString("List item 1"));
        value.append(new BString("List item 2"));
        map.put("C", value);
        return map;
    }

    @Test(description = "Test complex keys in a Map")
    public void testComplexKeysInAMap() {
        JsonSerializer jsonSerializer = new JsonSerializer();
        String serialize = jsonSerializer.serialize(mockComplexKeyMap());
        Object destinationMap = jsonSerializer.deserialize(serialize.getBytes(), HashMap.class);

        Map map = (Map) destinationMap;
        boolean matchedKey1 = map.keySet().stream().anyMatch(k -> ((StringFieldA) k).A.equals("Key1"));
        boolean matchedKey2 = map.keySet().stream().anyMatch(k -> ((StringFieldA) k).A.equals("Key2"));
        Assert.assertTrue(matchedKey1 && matchedKey2);
    }

    @Test(description = "Test deserialization of BNewArray")
    public void testJsonDeserializeBNewArrayReconstruction() {
        BRefValueArray array = new BRefValueArray(BTypes.typeString);
        BString str1 = new BString("String1");
        BString str2 = new BString("String2");
        array.append(str1);
        array.append(str2);
        array.append(str1);

        String serialize = new JsonSerializer().serialize(array);
        Object deserialize = new JsonSerializer().deserialize(serialize.getBytes(), BRefValueArray.class);

        Assert.assertTrue(deserialize instanceof BRefValueArray);
        BRefValueArray deArray = (BRefValueArray) deserialize;
        BString string1 = (BString) deArray.get(0);
        Assert.assertEquals(string1.value(), "String1");
        // reference sharing test
        Assert.assertTrue(deArray.get(0) == deArray.get(2));
    }

    @Test(description = "Test deserialization of BNewArray when elements are maps")
    public void testJsonDeserializeBNewArrayReconstructionWithMapElements() {
        BRefValueArray array = new BRefValueArray(BTypes.typeMap);
        BString str1 = new BString("String1");
        BString str2 = new BString("String2");
        BMap<String, BString> map1 = new BMap<>();
        map1.put("A", str1);
        map1.put("B", str2);
        BMap<String, BString> map2 = new BMap<>();
        map1.put("A", str1);
        BMap<String, BString> map3 = new BMap<>();
        array.append(map1);
        array.append(map2);
        array.append(map3);
        array.append(map3);

        String serialize = new JsonSerializer().serialize(array);
        Object deserialize = new JsonSerializer().deserialize(serialize.getBytes(), BRefValueArray.class);

        Assert.assertTrue(deserialize instanceof BRefValueArray);
        BRefValueArray deArray = (BRefValueArray) deserialize;
        BMap map = (BMap) deArray.get(0);
        Assert.assertEquals(((BString) map.get("A")).value(), "String1");
        // reference sharing test
        Assert.assertTrue(deArray.get(2) == deArray.get(3));
    }

    @Test(description = "Test deserialization of StringFieldA[]")
    public void testJsonDeserializeArrayOfStringFieldA() {
        StringFieldA sf0 = new StringFieldA("A");
        StringFieldA sf1 = new StringFieldA("B");
        StringFieldA[] array = {sf0, sf1, sf1};

        String serialize = new JsonSerializer().serialize(array);
        Object deserialize = new JsonSerializer().deserialize(serialize.getBytes(), StringFieldA[].class);

        StringFieldA[] array1 = (StringFieldA[]) deserialize;
        Assert.assertEquals(array1.length, array.length);
        Assert.assertEquals(array1[0].A, array[0].A);
        Assert.assertEquals(array1[1].A, array[1].A);
        Assert.assertEquals(array1[2].A, array[2].A);

        // test reference sharing
        Assert.assertTrue(array1[1] == array1[2]);
    }

    @Test(description = "Test deserialization of StringFieldA[][]")
    public void testJsonDeserializeArrayOfArrayOfStringFieldA() {
        StringFieldA sf0 = new StringFieldA("A");
        StringFieldA sf1 = new StringFieldA("B");
        StringFieldA[][] array = { new StringFieldA[]{sf0, sf1, sf1}, new StringFieldA[]{sf0, sf1, sf1}};

        String serialize = new JsonSerializer().serialize(array);
        Object deserialize = new JsonSerializer().deserialize(serialize.getBytes(), StringFieldA[][].class);

        StringFieldA[][] array1 = (StringFieldA[][]) deserialize;
        Assert.assertEquals(array1.length, array.length);
        Assert.assertEquals(array1[0][0].A, "A");
        Assert.assertEquals(array1[0][1].A, "B");
        Assert.assertEquals(array1[0][2].A, "B");
        Assert.assertEquals(array1[1][0].A, "A");
        Assert.assertEquals(array1[1][1].A, "B");
        Assert.assertEquals(array1[1][2].A, "B");
    }

    @Test(description = "Test deserialization of class with a array field")
    public void testJsonDeserializeClassWithArrayField() {
        ArrayField af = new ArrayField(new int[]{1,2,3,4});

        String serialize = new JsonSerializer().serialize(af);
        Object deserialize = new JsonSerializer().deserialize(serialize.getBytes(), ArrayField.class);

        ArrayField temp = (ArrayField) deserialize;
        Assert.assertEquals(temp.array[0], 1);
        Assert.assertEquals(temp.array[1], 2);
        Assert.assertEquals(temp.array[2], 3);
        Assert.assertEquals(temp.array[3], 4);
    }

    private Map mockComplexKeyMap() {
        Map<StringFieldA, String> map = new HashMap<>();
        map.put(new StringFieldA("Key1"), "Key1");
        map.put(new StringFieldA("Key2"), "Key2");

        return map;
    }

    public static class StringFieldA {
        public final String A;

        public StringFieldA(String a) {
            A = a;
        }
    }

    public static class StringFieldAB extends StringFieldA {
        public final String B;

        public StringFieldAB(String a, String b) {
            super(a);
            this.B = b;
        }
    }

    public static class ArrayField {
        public int[] array;

        public ArrayField(int[] array) {
            this.array = array;
        }
    }
}
