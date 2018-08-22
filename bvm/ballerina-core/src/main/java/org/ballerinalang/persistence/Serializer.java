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

import org.ballerinalang.model.util.serializer.BValueProvider;
import org.ballerinalang.model.util.serializer.InstanceProviderRegistry;
import org.ballerinalang.model.util.serializer.JsonSerializer;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.SerializedKeyBValueProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.SerializableBMapInstanceProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.SerializableBRefArrayInstanceProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.SerializableContextInstanceProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.SerializableStateInstanceProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.SerializableWorkerDataInstanceProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.SerializedKeyInstanceProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.WorkerStateInstanceProvider;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Class manages the serialization functionality.
 *
 * @since 0.981.1
 */
public class Serializer {

    private static final List<String> serializableClasses = new ArrayList<>();
    private static final BValueProvider bValueProvider = BValueProvider.getInstance();
    private static final InstanceProviderRegistry instanceProvider = InstanceProviderRegistry.getInstance();

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

        bValueProvider.register(new SerializedKeyBValueProvider());

        instanceProvider.add(new SerializableStateInstanceProvider());
        instanceProvider.add(new SerializableWorkerDataInstanceProvider());
        instanceProvider.add(new SerializableContextInstanceProvider());
        instanceProvider.add(new WorkerStateInstanceProvider());
        instanceProvider.add(new SerializableBMapInstanceProvider());
        instanceProvider.add(new SerializedKeyInstanceProvider());
        instanceProvider.add(new SerializableBRefArrayInstanceProvider());
    }

    public static boolean isSerializable(Object o) {
        if (o == null) {
            return true;
        }
        return serializableClasses.contains(o.getClass().getName());
    }

    public static JsonSerializer getJsonSerializer() {
        return new JsonSerializer();
    }
}
