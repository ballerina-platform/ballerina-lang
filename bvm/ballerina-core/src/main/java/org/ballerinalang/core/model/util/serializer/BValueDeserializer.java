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

import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;

/**
 * Convert {@link BValue} representation into Java objects.
 *
 * @since 0.982.0
 */
public interface BValueDeserializer {
    /**
     * Covert {@link BValue} object tree into Java object of class {@code targetType}.
     *
     * @param jValue     Data to deserialize.
     * @param targetType Expected Java object type.
     * @return Populated object of {@code targetType}.
     */
    Object deserialize(BValue jValue, Class<?> targetType);

    /**
     * Add deserialized object for reference tracking.
     * <p>
     * This reference tracking is used when deserializing objects that links to existing objects.
     *
     * @param jBMap  source payload containing hashCode reference information.
     * @param object Referenced object represented by this reference.
     */
    void addObjReference(BMap<String, BValue> jBMap, Object object);

    /**
     * Find the object this reference key is pointing to.
     *
     * @param key Reference pointer.
     * @return Object referenced by {@code key} pointer.
     */
    Object getExistingObjRef(long key);
}
