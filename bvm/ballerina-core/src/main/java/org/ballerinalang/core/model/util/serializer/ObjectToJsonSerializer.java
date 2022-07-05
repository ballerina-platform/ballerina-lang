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

/**
 * Serialize any object into JSON structure.
 *
 * @since 0.982.0
 */
public interface ObjectToJsonSerializer {
    /**
     * Generate JSON serialized output from the given Java object instance.
     *
     * @param object instance to be serialized.
     * @return Json String.
     */
    String serialize(Object object);

    /**
     * Deserialize Json string into Java object starting from {@code targetClass}.
     *
     * @param jsonString  Json string to be deserialized.
     * @param targetClass Target type to start deserialization.
     * @param <T>         Generic type of target type.
     * @return Deserialized object from {@code jsonString}.
     */
    <T> T deserialize(String jsonString, Class<T> targetClass);
}
