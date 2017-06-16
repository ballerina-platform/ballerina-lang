/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.siddhi.core.util;

import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class to convert Object to the desired type using {@link Attribute.Type}
 */
public class AttributeConverter {
    private Map<Attribute.Type, Function<String, Object>> functionMap = new HashMap<>();

    public AttributeConverter() {
        functionMap.put(Attribute.Type.BOOL, Boolean::parseBoolean);
        functionMap.put(Attribute.Type.DOUBLE, Double::parseDouble);
        functionMap.put(Attribute.Type.FLOAT, Float::parseFloat);
        functionMap.put(Attribute.Type.INT, Integer::parseInt);
        functionMap.put(Attribute.Type.LONG, Long::parseLong);
        functionMap.put(Attribute.Type.STRING, s -> s);
    }

    /**
     * Convert the given object to the given type.
     *
     * @param propertyValue the actual object
     * @param attributeType the desired data type
     * @return the converted object
     */
    public Object getPropertyValue(String propertyValue, Attribute.Type attributeType) {
        if (functionMap.containsKey(attributeType)) {
            return functionMap.get(attributeType).apply(propertyValue);
        } else {
            throw new SiddhiAppRuntimeException("Attribute type: " + attributeType + " not supported by XML " +
                    "mapping.");
        }
    }

}
