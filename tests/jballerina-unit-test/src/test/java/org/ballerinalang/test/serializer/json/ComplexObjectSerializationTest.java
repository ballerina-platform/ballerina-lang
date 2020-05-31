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

import org.ballerinalang.core.model.types.BRecordType;
import org.ballerinalang.core.model.types.BTupleType;
import org.ballerinalang.core.model.util.serializer.JsonSerializer;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test serialization and deserialization of complex object structures.
 */
public class ComplexObjectSerializationTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/serializer/json/types-bal.bal");
    }

    @SuppressWarnings("unchecked")
    @Test(description = "Test serializing record type object")
    public void testRecordSerialization() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getThatStudent");
        BMap<String, BValue> thatStudent = (BMap<String, BValue>) returns[0];

        JsonSerializer jsonSerializer = new JsonSerializer();
        String serializedStudent = jsonSerializer.serialize(thatStudent);
        BMap reincarnatedStudent = jsonSerializer.deserialize(serializedStudent, BMap.class);

        BRecordType origType = (BRecordType) thatStudent.getType();
        BRecordType newType = (BRecordType) reincarnatedStudent.getType();

        Assert.assertEquals(origType.isPublic(), newType.isPublic());
        Assert.assertEquals(origType.getValueClass(), newType.getValueClass());
        Assert.assertEquals(origType.getName(), newType.getName());
        Assert.assertEquals(origType.toString(), newType.toString());
        Assert.assertEquals(origType.sealed, origType.sealed);
        Assert.assertEquals(origType.restFieldType.toString(), newType.restFieldType.toString());
        Assert.assertEquals(((BRecordType) origType.getFields().get("grades").fieldType).sealed,
                ((BRecordType) newType.getFields().get("grades").fieldType).sealed);
    }

    @SuppressWarnings("unchecked")
    @Test(description = "Test serializing complex keys in a Map")
    public void testComplexKeysInAMap() {
        JsonSerializer jsonSerializer = new JsonSerializer();
        String serialize = jsonSerializer.serialize(mockComplexKeyMap());

        HashMap map = jsonSerializer.deserialize(serialize, HashMap.class);
        boolean matchedKey1 = map.keySet().stream()
                .anyMatch(k -> ((JsonSerializerTest.StringFieldA) k).a.equals("Key1"));
        boolean matchedKey2 = map.keySet().stream()
                .anyMatch(k -> ((JsonSerializerTest.StringFieldA) k).a.equals("Key2"));
        Assert.assertTrue(matchedKey1 && matchedKey2);
    }

    private Map mockComplexKeyMap() {
        Map<JsonSerializerTest.StringFieldA, Map<String, Boolean>> map = new HashMap<>();
        Map<String, Boolean> innerMap = new HashMap<>();
        innerMap.put("InnerK", true);
        map.put(new JsonSerializerTest.StringFieldA("Key1"), innerMap);
        map.put(new JsonSerializerTest.StringFieldA("Key2"), innerMap);
        return map;
    }

    @Test(description = "Test serialize/deserialize field shadowing on inherited class")
    public void testJsonDeserializeFieldShadowing() {
        Shadower sh = new Shadower(55.55, 2);
        String serialize = new JsonSerializer().serialize(sh);
        Shadower newSh = new JsonSerializer().deserialize(serialize, Shadower.class);
        Assert.assertEquals(newSh.i, sh.i);
        Assert.assertEquals(((Shadowee) newSh).i, ((Shadowee) sh).i);
    }

    @SuppressWarnings("unchecked")
    @Test(description = "Test BMap containing BMaps")
    public void testNestedBMaps() {
        BMap<String, BMap<String, BString>> nested = new BMap<>();
        BMap<String, BString> child1 = new BMap<>();
        child1.put("foo", new BString("Foo"));
        child1.put("bar", new BString("Bar"));
        child1.put("baz", new BString("Baz"));
        nested.put("child1", child1);

        BMap<String, BString> child2 = new BMap<>();
        child2.put("foo+", new BString("Foo"));
        child2.put("bar+", new BString("Bar"));
        child2.put("baz+", new BString("Baz"));
        nested.put("child2", child2);

        String serialize = new JsonSerializer().serialize(nested);
        BMap deserialized = new JsonSerializer().deserialize(serialize, BMap.class);

        BMap<String, BString> dChild1 = (BMap<String, BString>) deserialized.get("child1");
        BString dFoo = dChild1.get("foo");
        Assert.assertEquals(dFoo.value(), "Foo");

        BMap<String, BString> dChild2 = (BMap<String, BString>) deserialized.get("child2");
        BString dBar = dChild2.get("bar+");
        Assert.assertEquals(dBar.value(), "Bar");
    }

    @Test(description = "Respect readResolve method in java.io.Serializable")
    public void testSerializableReadResolveMethod() {
        ReadResolverClass obj = new ReadResolverClass(42);
        String serialize = new JsonSerializer().serialize(obj);
        ReadResolverClass deserialize = new JsonSerializer().deserialize(serialize, ReadResolverClass.class);

        // readResolve method of ReadResolverClass makes this.resolved field true.
        Assert.assertTrue(deserialize.resolved);
        Assert.assertEquals(deserialize.i, 42);
    }

    @Test(description = "Test serializing a ballerina tuple")
    public void testTupleSerialization() {
        BValue[] returns = BRunUtil.invoke(compileResult, "giveATuple");
        BValueArray tuple = (BValueArray) returns[1];
        String serialize = new JsonSerializer().serialize(tuple);
        BValueArray deserialize = new JsonSerializer().deserialize(serialize, BValueArray.class);
        Assert.assertEquals(deserialize.getType().getTag(), tuple.getType().getTag());
        List<Integer> tupleTypesTags = ((BTupleType) tuple.getType()).getTupleTypes().stream()
                .map(t -> t.getTag())
                .collect(Collectors.toList());
        List<Integer> deserializedUupleTypesTags = ((BTupleType) deserialize.getType()).getTupleTypes().stream()
                .map(t -> t.getTag())
                .collect(Collectors.toList());
        Assert.assertEquals(tupleTypesTags, deserializedUupleTypesTags);
    }

    static class Shadowee {
        private final int i;

        Shadowee(int i) {
            this.i = i;
        }
    }

    @SuppressWarnings("SameParameterValue")
    static class Shadower extends Shadowee {
        private final double i;

        Shadower(double i, int j) {
            super(j);
            this.i = i;
        }
    }

    @SuppressWarnings("SameParameterValue")
    static class ReadResolverClass implements Serializable {
        final int i;
        boolean resolved = false;

        ReadResolverClass(int i) {
            this.i = i;
        }

        private Object readResolve() {
            resolved = true;
            return this;
        }
    }
}
