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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.persistence.adapters.ArrayListAdapter;
import org.ballerinalang.persistence.adapters.HashMapAdapter;
import org.ballerinalang.persistence.adapters.RefTypeAdaptor;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.serializer.JsonSerializer;
import org.ballerinalang.persistence.serializable.serializer.ObjectToJsonSerializer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class manages the serialization functionality.
 *
 * @since 0.981.1
 */
public class Serializer {

    private static List<String> serializableClasses = new ArrayList<>();

    private static Gson gson;
    static {
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
        serializableClasses.add(BBoolean.class.getName());
        serializableClasses.add(BFloat.class.getName());
        serializableClasses.add(BByte.class.getName());
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(SerializableRefType.class, new RefTypeAdaptor());
        gsonBuilder.registerTypeAdapter(HashMap.class, new HashMapAdapter());
        gsonBuilder.registerTypeAdapter(ArrayList.class, new ArrayListAdapter());
        gson = gsonBuilder.create();
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
}
