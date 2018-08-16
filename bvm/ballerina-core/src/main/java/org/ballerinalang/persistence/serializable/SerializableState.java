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
package org.ballerinalang.persistence.serializable;

import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.Serializer;
import org.ballerinalang.persistence.serializable.reftypes.Serializable;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.serializer.ObjectToJsonSerializer;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a serializable state. This holds the required functionality to persist the context.
 *
 * @since 0.981.1
 */
public class SerializableState {
    private String id;

    public String currentContextKey;

    public Map<String, SerializableContext> sContexts = new HashMap<>();

    public Map<String, SerializableRespContext> sRespContexts = new HashMap<>();

    public Map<String, SerializableRefType> sRefTypes = new HashMap<>();

    public HashMap<String, Object> globalProps = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static SerializableState deserialize(String json) {
        ObjectToJsonSerializer stateSerializer = Serializer.getStateSerializer();
        return stateSerializer.deserialize(json, SerializableState.class);
    }

    public SerializableState(WorkerExecutionContext executionContext, int ip) {
        if (executionContext == null) {
            return;
        }
        currentContextKey = String.valueOf(executionContext.hashCode());
        SerializableContext serializableContext = new SerializableContext(currentContextKey, executionContext,
                this, ip);
        sContexts.put(currentContextKey, serializableContext);
    }

    public String serialize() {
        ObjectToJsonSerializer stateSerializer = Serializer.getStateSerializer();
        return stateSerializer.serialize(this);
    }

    public WorkerExecutionContext getExecutionContext(ProgramFile programFile, Deserializer deserializer) {
        SerializableContext serializableContext = sContexts.get(currentContextKey);
        return serializableContext.getWorkerExecutionContext(programFile, this, deserializer);
    }

    public String addContext(WorkerExecutionContext context, int ip) {
        if (context == null) {
            return null;
        }
        String contextKey = String.valueOf(context.hashCode());
        if (!sContexts.containsKey(contextKey)) {
            SerializableContext serializableContext = new SerializableContext(contextKey, context, this, ip);
            sContexts.put(contextKey, serializableContext);
        }
        return contextKey;
    }

    public WorkerExecutionContext getContext(String contextKey, ProgramFile programFile, Deserializer deserializer) {
        SerializableContext serializableContext = sContexts.get(contextKey);
        return serializableContext.getWorkerExecutionContext(programFile, this, deserializer);
    }

    public String addRespContext(CallableWorkerResponseContext responseContext) {
        if (responseContext == null) {
            return null;
        }
        String key = String.valueOf(responseContext.hashCode());
        if (!sRespContexts.containsKey(key)) {
            SerializableRespContext serializableRespContext = new SerializableRespContext(key, responseContext, this);
            sRespContexts.put(key, serializableRespContext);
        }
        return key;
    }

    public CallableWorkerResponseContext getResponseContext(String respCtxKey, ProgramFile programFile,
                                                            CallableUnitInfo callableUnitInfo,
                                                            Deserializer deserializer) {
        CallableWorkerResponseContext responseContext = deserializer.getTempRespContexts().get(respCtxKey);
        if (responseContext == null) {
            SerializableRespContext serializableRespContext = sRespContexts.get(respCtxKey);
            responseContext =
                    serializableRespContext.getResponseContext(programFile, callableUnitInfo,
                            this, deserializer);
            deserializer.getTempRespContexts().put(respCtxKey, responseContext);
        }
        return responseContext;
    }

    public ArrayList<Object> serializeRefFields(BRefType[] bRefFields) {
        if (bRefFields == null) {
            return null;
        }
        ArrayList<Object> refFields = new ArrayList<>(bRefFields.length);
        for (int i = 0; i < bRefFields.length; i++) {
            BRefType refType = bRefFields[i];
            refFields.add(i, serialize(refType));
        }
        return refFields;
    }

    public BRefType[] deserializeRefFields(List<Object> sRefFields, ProgramFile programFile,
                                           Deserializer deserializer) {
        if (sRefFields == null) {
            return null;
        }
        BRefType[] bRefFields = new BRefType[sRefFields.size()];
        for (int i = 0; i < sRefFields.size(); i++) {
            Object s = sRefFields.get(i);
            bRefFields[i] = (BRefType) deserialize(s, programFile, deserializer);
        }
        return bRefFields;
    }

    public Object serialize(Object o) {
        if (o == null || Serializer.isSerializable(o)) {
            return o;
        } else {
            if (o instanceof Serializable) {
                return addRefType((Serializable) o);
            } else {
                return null;
            }
        }
    }

    public Object deserialize(Object o, ProgramFile programFile, Deserializer deserializer) {
        if (o instanceof SerializedKey) {
            SerializedKey key = (SerializedKey) o;
            BRefType bRefType = deserializer.getRefTypes().get(key.key);
            if (bRefType != null) {
                return bRefType;
            } else {
                SerializableRefType sRefType = sRefTypes.get(key.key);
                bRefType = sRefType.getBRefType(programFile, this, deserializer);
                deserializer.getRefTypes().put(key.key, bRefType);
                return bRefType;
            }
        } else {
            return o;
        }
    }

    private SerializedKey addRefType(Serializable serializable) {
        String refKey = String.valueOf(serializable.hashCode());
        if (sRefTypes.containsKey(refKey)) {
            return new SerializedKey(refKey);
        }
        SerializableRefType sRefType = serializable.serialize(this);
        if (sRefType != null) {
            sRefTypes.put(refKey, sRefType);
            return new SerializedKey(refKey);
        }
        return null;
    }
}
