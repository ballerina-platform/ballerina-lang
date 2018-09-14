/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
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
import org.ballerinalang.persistence.RuntimeStates;
import org.ballerinalang.persistence.State;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBMap;
import org.ballerinalang.persistence.store.PersistenceStore;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.test.serializer.json.JsonSerializerTest;
import org.ballerinalang.test.utils.debug.TestDebugger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Serialization and Deserialization test case.
 *
 * @since 0.981.1
 */
public class SerializationTest {

    private static final String KEY_STR = "bmapKey1";
    private static final BigDecimal BIG_DECIMAL = new BigDecimal("123456789999");
    private static final String STATE_ID = "test123";
    private static final String BMAP_KEY = "key";
    private static final String BSTRING_VALUE = "gProp1:BString";
    private static final String PROP_KEY_1 = "gProp1";
    private static final String PROP_KEY_2 = "gProp2";
    private static final String MULTI_LEVEL_KEY = "multiLevel";
    private static final String DEC_KEY = "dec";
    private static final String VALUE_ITEM_1 = "Item-1";
    private static final String VALUE_ITEM_2 = "Item-2";
    private static final String VALUE_ITEM_3 = "Item-3";
    private static final String INNER_BMAP_KEY = "obj1";
    private static final String BSTRING_CONTENT = "BString Content";
    private static final String BSTRING = "bString";
    private CompileResult compileResult;
    private TestStorageProvider storageProvider;
    private Deserializer deserializer = new Deserializer();

    @BeforeClass
    public void setup() {
        storageProvider = new TestStorageProvider();
        PersistenceStore.setStorageProvider(storageProvider);
        compileResult = BCompileUtil.compile("test-src/checkpointing/checkpoint.bal");
        TestDebugger debugger = new TestDebugger(compileResult.getProgFile());
        compileResult.getProgFile().setDebugger(debugger);
        RuntimeStates.add(STATE_ID, new WorkerExecutionContext(compileResult.getProgFile()));
    }

    @Test(description = "Test case for JsonSerializer using mocked WorkerExecutionContext object.")
    public void testJsonSerializerWithMockedWorkerExecutionContex() {
        WorkerExecutionContext ctx = new WorkerExecutionContext(compileResult.getProgFile());
        ctx.globalProps.put(Constants.STATE_ID, STATE_ID);
        PersistenceStore.persistState(ctx, ctx.ip);
        Assert.assertTrue(storageProvider.state.contains(STATE_ID));
    }

    @Test(description = "Test case for JsonSerializer for both serialization and deserialization using mocked" +
            " WorkerExecutionContext object.")
    public void testJsonSerializationAndDeserialization() {
        // setup
        WorkerExecutionContext ctx = new WorkerExecutionContext(compileResult.getProgFile());
        String value = "B-Prop1";
        String key = "Prop1";
        ctx.globalProps.put(key, new BString(value));

        // execute
        ctx.globalProps.put(Constants.STATE_ID, STATE_ID);
        PersistenceStore.persistState(ctx, ctx.ip);
        Assert.assertTrue(storageProvider.state.contains(STATE_ID));
        List<State> states = PersistenceStore.getStates(compileResult.getProgFile(), deserializer);

        // test
        State reconstructedState = states.get(0);
        BString prop1 = (BString) reconstructedState.executableCtxList.get(0).globalProps.get(key);
        Assert.assertEquals(value, prop1.stringValue());
    }

    @Test(description = "Test deserialization of JSON into SerializableState object")
    public void testJsonDeserializeSerializableState() throws NoSuchFieldException, IllegalAccessException {
        WorkerExecutionContext ctx = new WorkerExecutionContext(compileResult.getProgFile());
        SerializableState serializableState = new SerializableState(STATE_ID, ctx);
        serializableState.setId(STATE_ID);
        mock(serializableState);
        String json = serializableState.serialize();

        SerializableState state = PersistenceStore.deserialize(json);
        Assert.assertEquals(state.getId(), STATE_ID);

        List list = (List) state.globalProps.get(PROP_KEY_2);
        Assert.assertEquals(VALUE_ITEM_1, list.get(0));
        Assert.assertEquals(VALUE_ITEM_2, list.get(1));
        Assert.assertEquals(VALUE_ITEM_3, list.get(2));
    }

    @SuppressWarnings("unchecked")
    @Test(description = "Test deserialization of Deeply nested BMap in SerializableState")
    public void testJsonDeserializeSerializableStateDeepBMapReconstruction() throws
            NoSuchFieldException, IllegalAccessException {
        WorkerExecutionContext ctx = new WorkerExecutionContext(compileResult.getProgFile());
        SerializableState serializableState = new SerializableState("0", ctx);
        serializableState.setId(STATE_ID);
        mock(serializableState);
        String json = serializableState.serialize();
        SerializableState state = PersistenceStore.deserialize(json);
        SerializableRefType bmapKey1 = getSRefTypesMap(state).get(BMAP_KEY);

        BMap<String, BValue> bmap =
                (BMap) bmapKey1.getBRefType(compileResult.getProgFile(), state, new Deserializer());
        BString value = (BString) bmap.get(KEY_STR);
        Assert.assertEquals(value.value(), BSTRING_CONTENT);

        JsonSerializerTest.StringFieldAB multiLevel =
                (JsonSerializerTest.StringFieldAB) state.globalProps.get(MULTI_LEVEL_KEY);
        Assert.assertEquals(multiLevel.b, "B");
        Assert.assertEquals(multiLevel.a, "A");

        BigDecimal dec = (BigDecimal) state.globalProps.get(DEC_KEY);
        Assert.assertEquals(dec, BIG_DECIMAL);

        // reference sharing test for BString object
        Object gProp1 = state.globalProps.get(PROP_KEY_1);
        BValue bstring = bmap.get(BSTRING);
        Assert.assertSame(gProp1, bstring);
    }

    @SuppressWarnings("unchecked")
    private Map<String, SerializableRefType> getSRefTypesMap(SerializableState state) throws
            NoSuchFieldException, IllegalAccessException {
        Field sRefTypes = state.getClass().getDeclaredField("sRefTypes");
        sRefTypes.setAccessible(true);
        return (Map<String, SerializableRefType>) sRefTypes.get(state);
    }

    private BRefType getInnerBMap() {
        BMap<String, BRefType> map = new BMap<>();
        map.put("map.A", new BString("A"));
        map.put("map.B", new BString("B"));
        BRefValueArray value = new BRefValueArray(new BArrayType(BTypes.typeString));
        value.append(new BString("List item 1"));
        value.append(new BString("List item 2"));
        map.put("map.C", value);
        return map;
    }

    @SuppressWarnings("unchecked")
    private void mock(SerializableState serializableState) throws NoSuchFieldException, IllegalAccessException {
        BString sharedString = new BString(BSTRING_VALUE); // use in multiple places to test reference sharing.

        serializableState.globalProps.put(PROP_KEY_1, sharedString);
        serializableState.globalProps.put(PROP_KEY_2, Arrays.asList(VALUE_ITEM_1, VALUE_ITEM_2, VALUE_ITEM_3));
        serializableState.globalProps.put(MULTI_LEVEL_KEY, new JsonSerializerTest.StringFieldAB("A", "B"));
        serializableState.globalProps.put(DEC_KEY, BIG_DECIMAL);

        BMap<String, BRefType> bMap = new BMap<>();
        bMap.put(KEY_STR, new BString(BSTRING_CONTENT));
        bMap.put(INNER_BMAP_KEY, getInnerBMap());
        bMap.put(BSTRING, sharedString);
        SerializableBMap value = new SerializableBMap(bMap, serializableState, new HashSet<>());
        getSRefTypesMap(serializableState).put(BMAP_KEY, value);
    }

    @AfterClass
    public void cleanup() {
        deserializer.cleanUpDeserializer();
    }
}
