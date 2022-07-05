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
package org.ballerinalang.core.model.util.serializer.providers.bvalue;

import org.ballerinalang.core.model.util.serializer.BPacket;
import org.ballerinalang.core.model.util.serializer.BValueDeserializer;
import org.ballerinalang.core.model.util.serializer.BValueSerializer;
import org.ballerinalang.core.model.util.serializer.SerializationBValueProvider;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BallerinaException;

/**
 * Provide mapping between {@link Class} and {@link BValue} representation of it.
 *
 * @since 0.982.0
 */
public class ClassBValueProvider implements SerializationBValueProvider<Class> {
    private static final String INT = "int";
    private static final String FLOAT = "float";
    private static final String DOUBLE = "double";
    private static final String LONG = "long";
    private static final String BYTE = "byte";
    private static final String CHAR = "char";

    @Override
    public String typeName() {
        return getType().getName();
    }

    @Override
    public Class<?> getType() {
        return Class.class;
    }

    @Override
    public BPacket toBValue(Class clazz, BValueSerializer serializer) {
        return BPacket.from(typeName(), new BString(clazz.getName()));
    }

    @Override
    public Class toObject(BPacket packet, BValueDeserializer bValueDeserializer) {
        String className = packet.getValue().stringValue();

        switch (className) {
            case INT:
                return int.class;
            case FLOAT:
                return float.class;
            case DOUBLE:
                return double.class;
            case LONG:
                return long.class;
            case BYTE:
                return byte.class;
            case CHAR:
                return char.class;
            default:
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new BallerinaException("Cannot find serialized class: " + className);
                }
        }
    }
}
