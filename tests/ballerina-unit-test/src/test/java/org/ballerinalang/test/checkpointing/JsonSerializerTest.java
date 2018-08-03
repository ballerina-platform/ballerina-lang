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

import java.util.Arrays;
import java.util.List;

public class JsonSerializerTest {
    private static final String INSTANCE_ID = "ABC123";
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
    }

    private void mock(SerializableState serializableState) {
        serializableState.globalProps.put("gProp1", new BString("gProp1:BString"));
        serializableState.globalProps.put("gProp2", Arrays.asList("Item-1", "Item-2", "Item-3"));

        BMap<String, BRefType> bMap = new BMap<>();
        bMap.put("bmapKey1", new BString("bmap_str_val"));
        bMap.put("obj1", getInnerBMap());
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
}
