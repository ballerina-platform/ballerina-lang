/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.bvm.persistency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.persistency.reftypes.SerializableBStruct;
import org.ballerinalang.bre.bvm.persistency.reftypes.SerializableRefType;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.persistence.StateStore;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SerializableState {

    private String serializationId;
    private String currentContextKey;
    private Map<String, SerializableContext> sContexts = new HashMap<>();
    private Map<String, SerializableBStruct> sBStructs = new HashMap<>();
    private Map<String, SerializableRefType> sRefTypes = new HashMap<>();

    public String getSerializationId() {
        return serializationId;
    }

    public static SerializableState deserialize(String json) {
        Gson gson = new GsonBuilder().create();
        SerializableState serializableState = gson.fromJson(json, SerializableState.class);
        return serializableState;
    }

    public SerializableState(WorkerExecutionContext executionContext) {
        if (executionContext == null) {
            return;
        }
        serializationId = UUID.randomUUID().toString();
        currentContextKey = serializationId + executionContext.hashCode();
        SerializableContext serializableContext = new SerializableContext(executionContext, this);
        sContexts.put(currentContextKey, serializableContext);
    }

    public String serialize() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public WorkerExecutionContext getExecutionContext(ProgramFile programFile) {
        StateStore.tempBStructs.put(serializationId, new HashMap<>());
        SerializableContext serializableContext = sContexts.get(currentContextKey);
        WorkerExecutionContext context = serializableContext.getWorkerExecutionContext(programFile, this);
        StateStore.tempBStructs.remove(serializationId);
        return context;
    }

    public String addContext(WorkerExecutionContext context) {
        SerializableContext serializableContext = new SerializableContext(context, this);
        String contextKey = serializationId + context.hashCode();
        sContexts.put(contextKey, serializableContext);
        return contextKey;
    }

    public WorkerExecutionContext getContext(String contextKey, ProgramFile programFile) {
        SerializableContext serializableContext = sContexts.get(contextKey);
        WorkerExecutionContext context = serializableContext.getWorkerExecutionContext(programFile, this);
        return context;
    }

    public SerializedKey addRefType(BRefType refType) {
        String refKey = serializationId + refType.hashCode();
        if (sRefTypes.containsKey(refKey)) {
            return new SerializedKey(refKey);
        }
        SerializableRefType sRefType = PersistenceUtils.serializeRefType(refType, this);
        if (sRefType != null) {
            sRefTypes.put(refKey, sRefType);
            return new SerializedKey(refKey);
        }
        return null;
    }

    public String addBStruct(BStruct bStruct) {
        String bStructKey = serializationId + bStruct.hashCode();
        if (sBStructs.containsKey(bStructKey)) {
            return bStructKey;
        }
        SerializableBStruct serializableBStruct = new SerializableBStruct(bStruct, this);
        sBStructs.put(bStructKey, serializableBStruct);
        return bStructKey;
    }

    public BStruct getBStruct(String bStructKey, ProgramFile programFile) {
        Map<String, BStruct> bStructs = StateStore.tempBStructs.get(serializationId);
        BStruct bStruct = bStructs.get(bStructKey);
        if (bStruct != null) {
            return bStruct;
        }
        SerializableBStruct serializableBStruct = sBStructs.get(bStructKey);
        bStruct = serializableBStruct.getBSturct(programFile, this);
        bStructs.put(bStructKey, bStruct);
        return bStruct;
    }

    class SerializedKey {
        public String key;

        public SerializedKey(String key) {
            this.key = key;
        }
    }
}
