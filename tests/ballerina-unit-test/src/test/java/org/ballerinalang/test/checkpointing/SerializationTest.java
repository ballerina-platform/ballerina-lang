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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.persistence.serializable.serializer.JsonSerializer;
import org.ballerinalang.persistence.serializable.serializer.ObjectToJsonSerializer;
import org.ballerinalang.persistence.states.State;
import org.ballerinalang.persistence.store.PersistenceStore;
import org.ballerinalang.persistence.store.StorageProvider;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.CharacterChannel;
import org.ballerinalang.test.nativeimpl.functions.io.MockByteChannel;
import org.ballerinalang.test.nativeimpl.functions.io.socket.ServerSocketTest;
import org.ballerinalang.test.nativeimpl.functions.io.util.TestUtil;
import org.ballerinalang.test.utils.debug.TestDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

/**
 * Serialization and Deserialization test case.
 *
 * @since 0.981.1
 */
public class SerializationTest {

    private static final String STATE_ID = "test123";
    public static final String INSTANCE_ID = "2334";
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
        Assert.assertTrue(serializedString.contains(INSTANCE_ID));
    }

    @Test(description = "Test case for JsonSerializer for both serialization and deserialization using mocked WorkerExecutionContext object.")
    public void testJsonSerializationAndDeserialization() {
        // setup
        WorkerExecutionContext weContext = new WorkerExecutionContext(compileResult.getProgFile());
        String prop1Value = "B-Prop1";
        weContext.globalProps.put("Prop1", new BString(prop1Value));
        State state = new State(weContext, INSTANCE_ID);

        // execute
        PersistenceStore.persistState(state);
        Assert.assertTrue(storageProvider.state.contains(INSTANCE_ID));
        ObjectToJsonSerializer serializer = new JsonSerializer();
        List<State> states = PersistenceStore.getStates(compileResult.getProgFile());

        // test
        State reconstructedState = states.get(0);
        BString prop1 = (BString) reconstructedState.getContext().globalProps.get("Prop1");
        Assert.assertEquals(prop1Value, prop1.stringValue());
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
