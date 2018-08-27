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
package org.ballerinalang.model.util.serializer.providers.bvalue;

import org.ballerinalang.model.util.serializer.BValueDeserializer;
import org.ballerinalang.model.util.serializer.BValueSerializer;
import org.ballerinalang.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Provide mapping between {@link Class} and {@link BValue} representation of it.
 *
 * @since 0.98.1
 */
public class ClassBValueProvider implements SerializationBValueProvider<Class> {
    @Override
    public String typeName() {
        return getType().getName();
    }

    @Override
    public Class<?> getType() {
        return Class.class;
    }

    @Override
    public BValue toBValue(Class clazz, BValueSerializer serializer) {
        return BValueProviderHelper.wrap(typeName(), new BString(clazz.getName()));
    }

    @Override
    public Class toObject(BValue bValue, BValueDeserializer bValueDeserializer) {
        if (bValue instanceof BMap) {
            @SuppressWarnings("unchecked")
            BMap<String, BValue> wrapper = (BMap<String, BValue>) bValue;
            BValue payload = BValueProviderHelper.getPayload(wrapper);
            String className = payload.stringValue();

            switch (className) {
                case "int":
                    return int.class;
                case "float":
                    return float.class;
                case "double":
                    return double.class;
                case "long":
                    return long.class;
                case "byte":
                    return byte.class;
                case "char":
                    return char.class;
                default:
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new BallerinaException("Cannot find serialized class: " + className);
                    }
            }
        }
        throw BValueProviderHelper.deserializationIncorrectType(bValue, typeName());
    }
}
