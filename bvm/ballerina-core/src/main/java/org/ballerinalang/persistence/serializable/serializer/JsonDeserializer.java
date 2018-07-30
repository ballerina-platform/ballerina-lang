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

import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class JsonDeserializer {
    private JsonNode jsonNode;
    private static Logger logger = LoggerFactory.getLogger(JsonDeserializer.class);
    private static HashMap<String, Class<?>> typeMap = new HashMap<>();

    static {
        typeMap.put("SerializableState", classForName("org.ballerinalang.persistence.serializable.SerializableState"));
    }

    private static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            logger.warn(String.format("Couldn't load class: %s", className));
            return null;
        }
    }

    public JsonDeserializer(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    public SerializableState deserialize() {
        return (SerializableState)deserializeInternal(jsonNode);
    }

    private Object deserializeInternal(JsonNode jsonNode) {
        switch (jsonNode.getType()) {
            case OBJECT:
                return deserializeObject(jsonNode);
            case ARRAY:
                break;
            case STRING:
                break;
            case LONG:
                break;
            case DOUBLE:
                break;
            case BOOLEAN:
                break;
            case NULL:
                break;
        }
        return null;
    }

    private Object deserializeObject(JsonNode jsonNode) {
        String objType = jsonNode.get("type").stringValue();
        JsonNode payload = jsonNode.get("payload");

        return null;
    }
}
