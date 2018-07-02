/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.adapters.ArrayListAdapter;
import org.ballerinalang.persistence.adapters.HashMapAdapter;
import org.ballerinalang.persistence.adapters.RefTypeAdaptor;
import org.ballerinalang.persistence.serializable.DataMapper;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBJSON;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBMap;
import org.ballerinalang.persistence.states.ActiveStates;
import org.ballerinalang.persistence.states.FailedStates;
import org.ballerinalang.persistence.states.State;
import org.ballerinalang.persistence.store.PersistenceStore;
import org.ballerinalang.runtime.Constants;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class with helper methods for persistence functionality.
 *
 * @since 0.976.0
 *
 */
public class PersistenceUtils {

    private static boolean initialized = false;

    private static List<String> serializableClasses = new ArrayList<>();

    private static Map<String, Map<String, BRefType>> tempRefTypes = new HashMap<>();

    private static Map<String, WorkerExecutionContext> tempContexts = new HashMap<>();

    private static Map<String, CallableWorkerResponseContext> tempRespContexts = new HashMap<>();

    private static DataMapper dataMapper;

    private static Gson gson;

    public static void addTempRefType(String stateId, String key, BRefType refType) {
        Map<String, BRefType> stateRefTypes = tempRefTypes.computeIfAbsent(stateId, k -> new HashMap<>());
        stateRefTypes.put(key, refType);
    }

    public static BRefType getTempRefType(String stateId, String key) {
        Map<String, BRefType> stateRefTypes = tempRefTypes.get(stateId);
        if (stateRefTypes == null) {
            return null;
        }
        return stateRefTypes.get(key);
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        PersistenceStore.init();
        serializableClasses.add(String.class.getName());
        serializableClasses.add(Integer.class.getName());
        serializableClasses.add(Long.class.getName());
        serializableClasses.add(Double.class.getName());
        serializableClasses.add(Float.class.getName());
        serializableClasses.add(Boolean.class.getName());
        serializableClasses.add(byte[].class.getName());

        serializableClasses.add(InetSocketAddress.class.getName());
        serializableClasses.add(BString.class.getName());
        serializableClasses.add(BInteger.class.getName());

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(SerializableRefType.class, new RefTypeAdaptor());
        gsonBuilder.registerTypeAdapter(HashMap.class, new HashMapAdapter());
        gsonBuilder.registerTypeAdapter(ArrayList.class, new ArrayListAdapter());
        gson = gsonBuilder.create();
        dataMapper = new DataMapper();
        initialized = true;
    }

    public static Gson getGson() {
        return gson;
    }

    public static boolean isSerializable(Object o) {
        if (o == null) {
            return true;
        }
        return serializableClasses.contains(o.getClass().getName());
    }

    public static WorkerExecutionContext getMainPackageContext(WorkerExecutionContext context) {
        if (context.callableUnitInfo.getPkgPath().equals(".")) {
            return context;
        }
        return getMainPackageContext(context.parent);
    }

    public static SerializableRefType serializeRefType(BRefType refType, SerializableState state) {
        if (refType instanceof BMap) {
            return new SerializableBMap<>((BMap<?, ? extends BValue>) refType, state);
        } else if (refType instanceof BJSON) {
            return new SerializableBJSON((BJSON) refType);
        } else {
            return null;
        }
    }

    public static void handleErrorState(WorkerExecutionContext parentCtx) {
        Object o = parentCtx.globalProps.get(Constants.INSTANCE_ID);
        if (o != null) {
            String instanceId = o.toString();
            State state = new State(parentCtx, instanceId);
            state.setIp(parentCtx.ip);
            FailedStates.add(instanceId, state);
            ActiveStates.remove(instanceId);
        }
    }

    public static Map<String, Map<String, BRefType>> getTempRefTypes() {
        return tempRefTypes;
    }

    public static Map<String, WorkerExecutionContext> getTempContexts() {
        return tempContexts;
    }

    public static Map<String, CallableWorkerResponseContext> getTempRespContexts() {
        return tempRespContexts;
    }

    public static DataMapper getDataMapper() {
        return dataMapper;
    }
}
