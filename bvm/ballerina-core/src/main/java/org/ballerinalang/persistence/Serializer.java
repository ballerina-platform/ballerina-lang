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
import org.ballerinalang.model.values.BXMLAttributes;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLQName;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.persistence.serializable.SerializableContext;
import org.ballerinalang.persistence.serializable.responses.SerializableResponseContextFactory;
import org.ballerinalang.persistence.serializable.serializer.providers.bvalue.SerializedKeyBValueProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.SerializableBRefArrayInstanceProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.SerializableContextInstanceProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.SerializableWorkerDataInstanceProvider;
import org.ballerinalang.persistence.serializable.serializer.providers.instance.WorkerStateInstanceProvider;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Class manages the serialization functionality.
 *
 * @since 0.981.1
 */
public class Serializer {

    private static final List<String> serializableClasses = new ArrayList<>();
    private static final JsonSerializer JSON_SERIALIZER = new JsonSerializer();
    private static final BValueProvider BVALUE_PROVIDER_REGISTRY = JSON_SERIALIZER.getBValueProviderRegistry();
    private static final InstanceProviderRegistry INSTANCE_PROVIDER_REGISTRY =
            JSON_SERIALIZER.getInstanceProviderRegistry();

    public static SerializableResponseContextFactory sRspCtxFactory = new SerializableResponseContextFactory();

    public static final HashSet<SerializableContext.Type> REMOVABLE_TYPES_INITIATION = new HashSet<>(Arrays.asList
            (SerializableContext.Type.DEFAULT, SerializableContext.Type.WORKER));

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
        serializableClasses.add(BXMLItem.class.getName());
        serializableClasses.add(BXMLAttributes.class.getName());
        serializableClasses.add(BXMLQName.class.getName());
        serializableClasses.add(BXMLSequence.class.getName());

        BVALUE_PROVIDER_REGISTRY.register(new SerializedKeyBValueProvider());

        INSTANCE_PROVIDER_REGISTRY.add(new SerializableWorkerDataInstanceProvider());
        INSTANCE_PROVIDER_REGISTRY.add(new SerializableContextInstanceProvider());
        INSTANCE_PROVIDER_REGISTRY.add(new WorkerStateInstanceProvider());
        INSTANCE_PROVIDER_REGISTRY.add(new SerializableBRefArrayInstanceProvider());
    }

    public static boolean isSerializable(Object o) {
        if (o == null) {
            return true;
        }
        return serializableClasses.contains(o.getClass().getName());
    }

    public static JsonSerializer getJsonSerializer() {
        return JSON_SERIALIZER;
    }
}
