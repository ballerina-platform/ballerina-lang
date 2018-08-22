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
import org.ballerinalang.model.util.serializer.JsonSerializer;
import org.ballerinalang.model.util.serializer.ObjectToJsonSerializer;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBMap;
import org.ballerinalang.persistence.states.State;
import org.ballerinalang.persistence.store.PersistenceStore;
import org.ballerinalang.persistence.store.StorageProvider;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.socket.ServerSocketTest;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.ballerinalang.test.serializer.json.JsonSerializerTest;
import org.ballerinalang.test.utils.debug.TestDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Serialization and Deserialization test case.
 *
 * @since 0.981.1
 */
public class SerializationTest {

    public static final String KEY_STR = "bmapKey1";
    public static final BigDecimal BIG_DECIMAL = new BigDecimal("123456789999");
    private static final String STATE_ID = "test123";
    private static final String INSTANCE_ID = "2334";
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
    private static final String BSRING_CONTENT = "BString Content";
    private static final String BSTRING = "bstring";
    private CompileResult compileResult;
    private List<State> stateList;
    private String serializedString;
    private org.ballerinalang.test.checkpointing.TestStorageProvider storageProvider;

    @BeforeClass
    public void setup() {
        storageProvider = new org.ballerinalang.test.checkpointing.TestStorageProvider();
        PersistenceStore.setStorageProvider(storageProvider);
        compileResult = BCompileUtil.compile("test-src/checkpointing/checkpoint.bal");
        TestDebugger debugger = new TestDebugger(compileResult.getProgFile());
        compileResult.getProgFile().setDebugger(debugger);
    }

    @Ignore
    @Test(description = "Test case for deserialize state")
    public void testDeserialize() {
        stateList = PersistenceStore.getStates(compileResult.getProgFile());
        Assert.assertEquals(stateList.size(), 1);
        State state = stateList.get(0);
        Assert.assertEquals(state.getContext().globalProps.get(Constants.STATE_ID), STATE_ID);
    }

    @Ignore
    @Test(description = "Test case for serialize state and persist")
    public void testSerialize() {
        PersistenceStore.persistState(stateList.get(0));
        Assert.assertTrue(serializedString.contains(STATE_ID));
    }

    @Test(description = "Test case for JsonSerializer using mocked WorkerExecutionContext object.")
    public void testJsonSerializerWithMockedWorkerExecutionContex() {
        WorkerExecutionContext weContext = new WorkerExecutionContext(compileResult.getProgFile());
        State state = new State(weContext, INSTANCE_ID);
        PersistenceStore.persistState(state);

        Assert.assertTrue(storageProvider.state.contains(INSTANCE_ID));
    }

    @Test(description = "Test case for JsonSerializer for both serialization and deserialization using mocked" +
            " WorkerExecutionContext object.")
    public void testJsonSerializationAndDeserialization() {
        // setup
        WorkerExecutionContext weContext = new WorkerExecutionContext(compileResult.getProgFile());
        String value = "B-Prop1";
        String key = "Prop1";
        weContext.globalProps.put(key, new BString(value));
        State state = new State(weContext, INSTANCE_ID);

        // execute
        PersistenceStore.persistState(state);
        Assert.assertTrue(storageProvider.state.contains(INSTANCE_ID));
        ObjectToJsonSerializer serializer = new JsonSerializer();
        List<State> states = PersistenceStore.getStates(compileResult.getProgFile());

        // test
        State reconstructedState = states.get(0);
        BString prop1 = (BString) reconstructedState.getContext().globalProps.get(key);
        Assert.assertEquals(value, prop1.stringValue());
    }

    @Test(description = "Test deserialization of JSON into SerializableState object")
    public void testJsonDeserializeSerializableState() throws NoSuchFieldException, IllegalAccessException {
        WorkerExecutionContext weContext = new WorkerExecutionContext(compileResult.getProgFile());
        SerializableState serializableState = new SerializableState(weContext, 0);
        serializableState.setId(INSTANCE_ID);
        mock(serializableState);
        String json = serializableState.serialize();

        SerializableState state = serializableState.deserialize(json);
        Assert.assertEquals(state.getId(), INSTANCE_ID);

        List list = (List) state.globalProps.get(PROP_KEY_2);
        Assert.assertEquals(VALUE_ITEM_1, list.get(0));
        Assert.assertEquals(VALUE_ITEM_2, list.get(1));
        Assert.assertEquals(VALUE_ITEM_3, list.get(2));
    }

    @Test(description = "Test deserialization of Deeply nested BMap in SerializableState")
    public void testJsonDeserializeSerializableStateDeepBMapReconstruction() throws
            NoSuchFieldException, IllegalAccessException {
        WorkerExecutionContext weContext = new WorkerExecutionContext(compileResult.getProgFile());
        SerializableState serializableState = new SerializableState(weContext, 0);
        serializableState.setId(INSTANCE_ID);
        mock(serializableState);
        String json = serializableState.serialize();

        SerializableState state = serializableState.deserialize(json);


        SerializableRefType bmapKey1 = getSRefTypesMap(state).get(BMAP_KEY);;

        BMap<String, BValue> bmap =
                (BMap) bmapKey1.getBRefType(compileResult.getProgFile(), state, new Deserializer());
        BString value = (BString) bmap.get(KEY_STR);
        Assert.assertEquals(value.value(), BSRING_CONTENT);

        JsonSerializerTest.StringFieldAB multiLevel =
                (JsonSerializerTest.StringFieldAB) state.globalProps.get(MULTI_LEVEL_KEY);
        Assert.assertEquals(multiLevel.b, "B");
        Assert.assertEquals(multiLevel.a, "A");

        BigDecimal dec = (BigDecimal) state.globalProps.get(DEC_KEY);
        Assert.assertTrue(dec.equals(BIG_DECIMAL));

        // reference sharing test for BString object
        Object gProp1 = state.globalProps.get(PROP_KEY_1);
        BValue bstring = bmap.get(BSTRING);
        Assert.assertTrue(gProp1 == bstring);
    }

    private Map<String, SerializableRefType> getSRefTypesMap(SerializableState state) throws
            NoSuchFieldException, IllegalAccessException {
        Field sRefTypes = state.getClass().getDeclaredField("sRefTypes");
        sRefTypes.setAccessible(true);
        Map<String, SerializableRefType> map = (Map<String, SerializableRefType>) sRefTypes.get(state);
        return map;
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

    private void mock(SerializableState serializableState) throws NoSuchFieldException, IllegalAccessException {
        BString sharedString = new BString(BSTRING_VALUE); // use in multiple places to test reference sharing.

        serializableState.globalProps.put(PROP_KEY_1, sharedString);
        serializableState.globalProps.put(PROP_KEY_2, Arrays.asList(VALUE_ITEM_1, VALUE_ITEM_2, VALUE_ITEM_3));
        serializableState.globalProps.put(MULTI_LEVEL_KEY, new JsonSerializerTest.StringFieldAB("A", "B"));
        serializableState.globalProps.put(DEC_KEY, BIG_DECIMAL);

        BMap<String, BRefType> bMap = new BMap<>();
        bMap.put(KEY_STR, new BString(BSRING_CONTENT));
        bMap.put(INNER_BMAP_KEY, getInnerBMap());
        bMap.put(BSTRING, sharedString);
        SerializableBMap value = new SerializableBMap(bMap, serializableState);
        getSRefTypesMap(serializableState).put(BMAP_KEY, value);
    }

    /**
     * Class implements @{@link StorageProvider} use as storage provider for test cases.
     */
    public class TestStorageProvider implements StorageProvider {

        private final Logger log = LoggerFactory.getLogger(ServerSocketTest.class);

        @Override
        public void persistState(String instanceId, String stateString) {
            serializedString = stateString;
        }

        @Override
        public void removeActiveState(String instanceId) {
            // nothing to do
        }

        @Override
        public List<String> getAllSerializedStates() {
            List<String> stateList = new LinkedList<>();
            try {
                ByteChannel byteChannel = TestUtil.openForReading("datafiles/checkpointing/state.json");
                Channel channel = new MockByteChannel(byteChannel);
                CharacterChannel characterChannel = new CharacterChannel(channel, StandardCharsets.UTF_8.name());
                String state = characterChannel.readAll();
                stateList.add(state);
            } catch (IOException | URISyntaxException e) {
                log.error(e.getMessage(), e);
            }
            return stateList;
        }
    }
}
