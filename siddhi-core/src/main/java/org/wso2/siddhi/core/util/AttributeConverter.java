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

import org.wso2.siddhi.query.api.definition.Attribute;

/**
 * Utility class to convert Object to the desired type using {@link Attribute.Type}
 */
public class AttributeConverter {

    /**
     * Convert the given object to the given type.
     *
     * @param propertyValue the actual object
     * @param attributeType the desired data type
     * @return the converted object
     */
    public static Object getPropertyValue(Object propertyValue, Attribute.Type attributeType) {

        if ((!Attribute.Type.STRING.equals(attributeType)) && propertyValue == null) {
            throw new RuntimeException("Found Invalid property value 'null' for attribute of type " + attributeType);
        }

        if (Attribute.Type.BOOL.equals(attributeType)) {
            return Boolean.parseBoolean(propertyValue.toString());
        } else if (Attribute.Type.DOUBLE.equals(attributeType)) {
            return Double.parseDouble(propertyValue.toString());
        } else if (Attribute.Type.FLOAT.equals(attributeType)) {
            return Float.parseFloat(propertyValue.toString());
        } else if (Attribute.Type.INT.equals(attributeType)) {
            return Integer.parseInt(propertyValue.toString());
        } else if (Attribute.Type.LONG.equals(attributeType)) {
            return Long.parseLong(propertyValue.toString());
        } else {
            return propertyValue == null ? null : propertyValue.toString();
        }
    }

}
