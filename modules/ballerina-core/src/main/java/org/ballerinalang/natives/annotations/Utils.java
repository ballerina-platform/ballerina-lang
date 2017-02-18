/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.natives.annotations;

import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.exceptions.MalformedEntryException;

/**
 * Common Utils methods used in Annotations.
 */
public class Utils {

    /**
     * Get BValue from the TypeEnum and Value.
     *
     * @param typeEnum Ballerina Type.
     * @param value    assigned value.
     * @return BValue.
     * @throws MalformedEntryException when Type is not supported or when value conversion fails.
     */
    public static BValue getBValueFromTypeEnum(TypeEnum typeEnum, String value) throws MalformedEntryException {
        BValue bValue;
        try {
            switch (typeEnum) {
                case BOOLEAN:
                    bValue = new BBoolean(Boolean.parseBoolean(value));
                    break;
                case INT:
                    bValue = new BInteger(Integer.parseInt(value));
                    break;
                case LONG:
                    bValue = new BLong(Long.parseLong(value));
                    break;
                case DOUBLE:
                    bValue = new BDouble(Double.parseDouble(value));
                    break;
                case FLOAT:
                    bValue = new BFloat(Float.parseFloat(value));
                    break;
                case STRING:
                    bValue = new BString(value);
                    break;
                case JSON:
                    bValue = new BJSON(value);
                    break;
                case XML:
                    // TODO: Fix this.
                    throw new MalformedEntryException("XML not supported yet.");
//                case MAP:
//                    // TODO: Fix this.
//                    bValue = new MapValue();
//                    break;
//                case ARRAY:
//                    // TODO: improve logic. Current Native Annotation support only Predefined String[] only.
//                    String[] values = value.substring(1, value.length() - 1).split(",");
//                    bValue = new ArrayValueOld<>(values);
//                    break;
                default:
                    throw new MalformedEntryException("Not supported entry " + typeEnum);
            }
        } catch (NumberFormatException e) {
            throw new MalformedEntryException("Error while processing value " + value, e);
        }
        return bValue;
    }
}
