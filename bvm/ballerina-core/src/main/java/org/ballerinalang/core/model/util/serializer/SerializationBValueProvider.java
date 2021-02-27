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
package org.ballerinalang.core.model.util.serializer;

import org.ballerinalang.core.model.values.BValue;

/**
 * Provide Mapping relationship between Java objects and BValue Objects.
 * <p>
 * While deserializing if the type to be serialized implement Serializable interface and contains a readResolve method,
 * you  don't have to execute it in {@code toBValue} method as it will be executed by {@link JsonDeserializer} class.
 *
 * @param <T> Type to be serialized
 * @since 0.982.0
 */
public interface SerializationBValueProvider<T> {
    /**
     * Return typeName used to find the appropriate {@link SerializationBValueProvider} implementation.
     *
     * @return String representation of type handled by this class.
     */
    default String typeName() {
        return getType().getName();
    }

    /**
     * Return the class of type that this {@link SerializationBValueProvider} providing for.
     *
     * @return Class instance of the type provided by this {@link SerializationBValueProvider} implementation
     */
    Class<?> getType();

    /**
     * Convert Java object into BValue tree.
     * <p>
     * BValueSerializer instance is injected to support serialize inner objects with in the type.
     * <p>
     * For example: When implementing {@link SerializationBValueProvider} for a array
     * we could use provided BValueSerializer instance to serialize elements of the array.
     *
     * @param object     Java object to be serialized.
     * @param serializer Instance of BValueSerializer to support inner object serialization.
     * @return Metadata tree representing target object.
     */
    BPacket toBValue(T object, BValueSerializer serializer);

    /**
     * Convert {@link BValue} tree or sub-tree into Java object of type T.
     * <p>
     * Similar to {@code toBValue} method an instance of BValueDeserializer is injected
     * to help deserialize child elements.
     *
     * @param packet             Json data to be converted.
     * @param bValueDeserializer Instance of {@link BValueDeserializer}.
     * @return Java object representing given BValue tree.
     */
    T toObject(BPacket packet, BValueDeserializer bValueDeserializer);
}
