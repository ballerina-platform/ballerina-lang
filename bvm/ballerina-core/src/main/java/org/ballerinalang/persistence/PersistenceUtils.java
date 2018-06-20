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
import org.ballerinalang.persistence.adapters.ArrayListAdapter;
import org.ballerinalang.persistence.adapters.HashMapAdapter;
import org.ballerinalang.persistence.adapters.RefTypeAdaptor;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBJSON;
import org.ballerinalang.persistence.serializable.reftypes.impl.SerializableBStruct;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.persistence.serializable.SerializableState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceUtils {
    public static boolean reloaded = false;

    private static boolean initialized = false;

//    private static Map<String, RefTypeHandler> refTypeHandlers = new HashMap<>();

    private static List<String> serializableClasses = new ArrayList<>();

    public static Map<String, Map<String, BRefType>> tempRefTypes = new HashMap<>();
    public static Map<String, WorkerExecutionContext> tempContexts = new HashMap<>();
    public static Map<String, CallableWorkerResponseContext> tempRespContexts = new HashMap<>();

    public static List<WorkerExecutionContext> persistableContexts = new ArrayList<>();

    private static Gson gson;

    public static void addTempRefType(String stateId, String key, BRefType refType) {
        Map<String, BRefType> stateRefTypes = tempRefTypes.get(stateId);
        if (stateRefTypes == null) {
            stateRefTypes = new HashMap<>();
            tempRefTypes.put(stateId, stateRefTypes);
        }
        stateRefTypes.put(key, refType);
    }

    public static BRefType getTempRefType(String stateId, String key) {
        Map<String, BRefType> stateRefTypes = tempRefTypes.get(stateId);
        if (stateRefTypes == null) {
            return null;
        }
        else return stateRefTypes.get(key);
    }

    public static void clearTempRefTypes(String stateId) {
        tempRefTypes.remove(stateId);
    }

    public static void clearTempContexts() {
        tempContexts.clear();
    }

    public synchronized static void init() {
        if (initialized) {
            return;
        }
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
        gsonBuilder.registerTypeAdapter(new HashMap<String, Object>().getClass(), new HashMapAdapter());
        gsonBuilder.registerTypeAdapter(new ArrayList<Object>().getClass(), new ArrayListAdapter());
        gson = gsonBuilder.create();

//        refTypeHandlers.put(BStruct.class.getName(), new BStructHandler());

        initialized = true;
    }

    public static Gson getGson() {
        return gson;
    }

    //    public static RefTypeHandler getRefTypeHandler(BRefType refType) {
//        if (refType == null) {
//            return null;
//        }
//        RefTypeHandler refTypeHandler = refTypeHandlers.get(refType.getClass().getName());
//        return refTypeHandler;
//    }

    public static boolean isSerializable(Object o) {
        if (o == null) {
            return true;
        }
        if (serializableClasses.contains(o.getClass().getName())) {
            return true;
        }
        return false;
    }

    public static boolean isPrimitiveType(Object v) {
        if (v instanceof String ||
                v instanceof Integer ||
                v instanceof Long ||
                v instanceof Double ||
                v instanceof Float ||
                v instanceof Boolean) {
            return true;
        }
        return false;
    }

    public static String getJson(WorkerExecutionContext ctx) {
        return "{}";
    }

    public static WorkerExecutionContext getMainPackageContext(WorkerExecutionContext context) {
        if (context.callableUnitInfo.getPkgPath().equals(".")) {
            return context;
        }
        return getMainPackageContext(context.parent);
    }

    public static void saveJsonFIle(String jsonStr, File file) {
        //        File f = new File("test.json");
        //        if (!f.exists()) {
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(jsonStr);
            out.close();
        } catch (FileNotFoundException e) {
            //ignore
        }
    }

    public static SerializableRefType serializeRefType(BRefType refType, SerializableState state) {
        if (refType instanceof BStruct) {
            return new SerializableBStruct((BStruct) refType, state);
        } else if (refType instanceof BJSON) {
            return new SerializableBJSON((BJSON) refType);
        } else {
            return null;
        }
    }
}