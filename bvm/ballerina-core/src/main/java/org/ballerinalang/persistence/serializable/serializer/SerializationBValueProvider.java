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
package org.ballerinalang.persistence.serializable.serializer;

import org.ballerinalang.model.values.BValue;

/**
 * Provide Mapping relationship between Java objects and BValue Objects.
 */
public interface SerializationBValueProvider {
    /**
     * Return typeName used to find the appropriate {@link SerializationBValueProvider} implementation.
     * @return
     */
    String typeName();
    Class<?> getType();
    BValue toBValue(Object object);
    Object toObject(BValue bValue);
}
