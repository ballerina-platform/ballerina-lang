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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBMap;
import org.ballerinalang.persistence.store.PersistenceStore;
import org.ballerinalang.test.utils.debug.TestDebugger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public class JsonSerializerTest {
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
    public void jsonSerializerWithMockedSerializableState() {
        CompileResult compileResult = BCompileUtil.compile("test-src/checkpointing/checkpoint.bal");
        WorkerExecutionContext weContext = new WorkerExecutionContext(compileResult.getProgFile());
        SerializableState serializableState = new SerializableState(weContext);
        mock(serializableState);
        String json = serializableState.serialize();

        Assert.assertTrue(json.contains("\"Item-1\",\"Item-2\",\"Item-3\""));
        Assert.assertTrue(json.contains("\"var_r1\":{"));
        Assert.assertTrue(json.contains("bmap_str_val"));
    }

    private void mock(SerializableState serializableState) {
        serializableState.globalProps.put("gProp1", new BString("gProp1:BString"));
        serializableState.globalProps.put("gProp2", Arrays.asList("Item-1", "Item-2", "Item-3"));

        BMap<String, BString> bMap = new BMap<>();
        bMap.put("bmapKey1", new BString("bmap_str_val"));
        serializableState.sRefTypes.put("var_r1", new SerializableBMap(bMap, serializableState));
    }
}
