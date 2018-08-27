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
package org.ballerinalang.persistence.serializable.serializer.providers.bvalue;

import org.ballerinalang.model.util.serializer.BValueDeserializer;
import org.ballerinalang.model.util.serializer.BValueSerializer;
import org.ballerinalang.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.model.util.serializer.providers.bvalue.BValueProviderHelper;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.SerializedKey;

/**
 * Convert {@link SerializedKey} into {@link BValue} object and back to facilitate serialization.
 *
 * @since 0.98.1
 */
public class SerializedKeyBValueProvider implements SerializationBValueProvider<SerializedKey> {
    @Override
    public Class<?> getType() {
        return SerializedKey.class;
    }

    @Override
    public BValue toBValue(SerializedKey object, BValueSerializer serializer) {
        return BValueProviderHelper.wrap(typeName(), new BString(object.key));
    }

    @Override
    @SuppressWarnings("unchecked")
    public SerializedKey toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
        String s = BValueProviderHelper.getPayload((BMap<String, BValue>) bValue).stringValue();
        return new SerializedKey(s);
    }
}
