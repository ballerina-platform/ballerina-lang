/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.xmlutils;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.TableValueImpl;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.jvm.values.api.BString;

/**
 * This class work as a bridge with ballerina and a Java implementation of ballerina/xmlutils modules.
 *
 * @since 1.1.0
 */
public class ConvertUtils {

    private static final String OPTIONS_ATTRIBUTE_PREFIX = "attributePrefix";
    private static final String OPTIONS_ARRAY_ENTRY_TAG = "arrayEntryTag";

    private ConvertUtils() {
    }

    /**
     * Converts a JSON to the corresponding XML representation.
     *
     * @param json    JSON record object
     * @param options option details
     * @return XML object that construct from JSON
     */
    public static Object fromJSON(Object json, MapValue<BString, BString> options) {
        try {
            String attributePrefix = (options.get(StringUtils.fromString(OPTIONS_ATTRIBUTE_PREFIX))).getValue();
            String arrayEntryTag = (options.get(StringUtils.fromString(OPTIONS_ARRAY_ENTRY_TAG))).getValue();
            return JSONToXMLConverter.convertToXML(json, attributePrefix, arrayEntryTag);
        } catch (Exception e) {
            return BallerinaErrors.createError(StringUtils.fromString(e.getMessage()));
        }
    }

    /**
     * Converts a given table to its XML representation.
     *
     * @param tableValue Table record pointer
     * @return XML record that construct from the table
     */
    public static XMLValue fromTable(TableValueImpl tableValue) {
        return XMLFactory.tableToXML(tableValue);
    }
}
