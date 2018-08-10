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

import org.apache.log4j.Logger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.persistence.serializable.serializer.BValueDeserializer;
import org.ballerinalang.persistence.serializable.serializer.BValueSerializer;
import org.ballerinalang.persistence.serializable.serializer.SerializationBValueProvider;

/**
 * Provide mapping between {@link org.apache.log4j.Logger} and {@link BValue} representation of it.
 */
public class Log4jLoggerBValueProvider implements SerializationBValueProvider {
    @Override
    public String typeName() {
        return Logger.class.getName();
    }

    @Override
    public Class<?> getType() {
        return Logger.class;
    }

    @Override
    public BValue toBValue(Object object, BValueSerializer serializer) {
        if (object instanceof org.apache.log4j.Logger) {
            String name = ((Logger) object).getName();
            return BValueProviderHelper.wrap(typeName(), new BString(name));
        }
        throw BValueProviderHelper.incorrectObjectType(object, typeName());
    }

    @Override
    public Object toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
        if (bValue instanceof BMap) {
            @SuppressWarnings("unchecked")
            BMap<String, BValue> wrapper = (BMap<String, BValue>) bValue;
            BValue payload = BValueProviderHelper.getPayload(wrapper);
            return Logger.getLogger(payload.stringValue());
        }
        throw BValueProviderHelper.deserializationIncorrectType(bValue, typeName());
    }
}
