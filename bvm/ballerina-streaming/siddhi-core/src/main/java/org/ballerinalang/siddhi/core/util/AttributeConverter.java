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

package org.ballerinalang.siddhi.core.util;

import org.ballerinalang.siddhi.core.exception.SiddhiAppRuntimeException;
import org.ballerinalang.siddhi.query.api.definition.Attribute;


/**
 * Utility class to convert Object to the desired type using {@link Attribute.Type}.
 */
public class AttributeConverter {


    /**
     * Convert the given object to the given type.
     *
     * @param propertyValue the actual object
     * @param attributeType the desired data type
     * @return the converted object
     */
    public Object getPropertyValue(String propertyValue, Attribute.Type attributeType) {
        switch (attributeType) {
            case BOOL:
                return Boolean.parseBoolean(propertyValue);
            case DOUBLE:
                return Double.parseDouble(propertyValue);
            case FLOAT:
                return Float.parseFloat(propertyValue);
            case INT:
                return Integer.parseInt(propertyValue);
            case LONG:
                return Long.parseLong(propertyValue);
            case STRING:
                return propertyValue;
            default:
                throw new SiddhiAppRuntimeException("Attribute type: " + attributeType + " not supported by XML " +
                        "mapping.");
        }
    }

}
