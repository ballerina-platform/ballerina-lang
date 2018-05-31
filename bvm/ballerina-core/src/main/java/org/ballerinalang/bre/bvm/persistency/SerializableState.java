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
import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.persistency.reftypes.SerializableBStruct;
import org.ballerinalang.bre.bvm.persistency.reftypes.SerializableRefType;
import org.ballerinalang.bre.bvm.persistency.reftypes.SerializedKey;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.persistence.StateStore;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SerializableState {

    private String instanceId;
    private String serializationId;
    private String currentContextKey;
    private Map<String, SerializableContext> sContexts = new HashMap<>();
    private Map<String, SerializableRespContext> sRespContexts = new HashMap<>();
    private Map<String, SerializableRefType> sRefTypes = new HashMap<>();

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getSerializationId() {
        return serializationId;
    }

    public static SerializableState deserialize(String json) {
        Gson gson = PersistenceUtils.getGson();
        SerializableState serializableState = gson.fromJson(json, SerializableState.class);
        return serializableState;
    }

    public SerializableState(WorkerExecutionContext executionContext) {
        if (executionContext == null) {
            return;
        }
//        serializationId = UUID.randomUUID().toString();
        serializationId = "s_";
        currentContextKey = serializationId + executionContext.hashCode();
        SerializableContext serializableContext = new SerializableContext(currentContextKey, executionContext, this);
        sContexts.put(currentContextKey, serializableContext);
    }

    public String serialize() {
        Gson gson = PersistenceUtils.getGson();
        return gson.toJson(this);
    }

    public WorkerExecutionContext getExecutionContext(ProgramFile programFile) {
        SerializableContext serializableContext = sContexts.get(currentContextKey);
        WorkerExecutionContext context = serializableContext.getWorkerExecutionContext(programFile, this);
        return context;
    }

    public String addContext(WorkerExecutionContext context) {
        if (context == null) {
            return null;
        }
        String contextKey = serializationId + context.hashCode();
        if (!sContexts.containsKey(contextKey)) {
            SerializableContext serializableContext = new SerializableContext(contextKey, context, this);
            sContexts.put(contextKey, serializableContext);
        }
        return contextKey;
    }

    public WorkerExecutionContext getContext(String contextKey, ProgramFile programFile) {
        SerializableContext serializableContext = sContexts.get(contextKey);
        WorkerExecutionContext context = serializableContext.getWorkerExecutionContext(programFile, this);
        return context;
    }

    public String addRespContext(CallableWorkerResponseContext responseContext) {
        if (responseContext == null) {
            return null;
        }
        String key = serializationId + responseContext.hashCode();
        if (!sRespContexts.containsKey(key)) {
            SerializableRespContext serializableRespContext = new SerializableRespContext(key, responseContext, this);
            sRespContexts.put(key, serializableRespContext);
        }
        return key;
    }

    public CallableWorkerResponseContext getResponseContext(
            String respCtxKey, ProgramFile programFile, CallableUnitInfo callableUnitInfo) {
        CallableWorkerResponseContext responseContext = PersistenceUtils.tempRespContexts.get(respCtxKey);
        if (responseContext == null) {
            SerializableRespContext serializableRespContext = sRespContexts.get(respCtxKey);
            responseContext =
                    serializableRespContext.getResponseContext(programFile, callableUnitInfo, this);
            PersistenceUtils.tempRespContexts.put(respCtxKey, responseContext);
        }
        return responseContext;
    }

    public ArrayList<Object> serializeRefFields(BRefType[] bRefFields) {
        if (bRefFields == null) {
            return null;
        }
        ArrayList<Object> refFields = new ArrayList<>(bRefFields.length);
//        Object[] refFields = new Object[bRefFields.length];
        for (int i = 0; i < bRefFields.length; i++) {
            BRefType refType = bRefFields[i];
            refFields.add(i, serialize(refType));
        }
        return refFields;
    }

    public BRefType[] deserializeRefFields(List<Object> sRefFields, ProgramFile programFile) {
        if (sRefFields == null) {
            return null;
        }
        BRefType[] bRefFields = new BRefType[sRefFields.size()];
        for (int i = 0; i < sRefFields.size(); i++) {
            Object s = sRefFields.get(i);
            bRefFields[i] = (BRefType) deserialize(s, programFile);
        }
        return bRefFields;
    }

    public Object serialize(Object o) {
        if (o == null || PersistenceUtils.isSerializable(o)) {
            return o;
        } else if (o instanceof BRefType) {
            SerializedKey serializedKey = addRefType((BRefType) o);
            return serializedKey;
        } else {
            return null;
        }
    }

    public Object deserialize(Object o, ProgramFile programFile) {
        if (o == null || !(o instanceof SerializedKey)) {
            return o;
        } else {
            SerializedKey key = (SerializedKey) o;
            if (PersistenceUtils.getTempRefType(serializationId, key.key) != null) {
                return PersistenceUtils.getTempRefType(serializationId, key.key);
            } else {
                SerializableRefType sRefType = sRefTypes.get(key.key);
                BRefType refType = sRefType.getBRefType(programFile, this);
                PersistenceUtils.addTempRefType(serializationId, key.key, refType);
                return refType;
            }
        }
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

//    public String addBStruct(BStruct bStruct) {
//        String bStructKey = serializationId + bStruct.hashCode();
//        if (sBStructs.containsKey(bStructKey)) {
//            return bStructKey;
//        }
//        SerializableBStruct serializableBStruct = new SerializableBStruct(bStruct, this);
//        sBStructs.put(bStructKey, serializableBStruct);
//        return bStructKey;
//    }
//
//    public BStruct getBStruct(String bStructKey, ProgramFile programFile) {
//        Map<String, BStruct> bStructs = StateStore.tempBStructs.get(serializationId);
//        BStruct bStruct = bStructs.get(bStructKey);
//        if (bStruct != null) {
//            return bStruct;
//        }
//        SerializableBStruct serializableBStruct = sBStructs.get(bStructKey);
//        bStruct = (BStruct) serializableBStruct.getBRefType(programFile, this);
//        bStructs.put(bStructKey, bStruct);
//        return bStruct;
//    }
}
