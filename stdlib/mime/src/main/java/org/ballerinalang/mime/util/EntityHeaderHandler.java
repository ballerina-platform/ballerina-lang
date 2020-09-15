/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.mime.util;

import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.api.BValueCreator;
import org.jvnet.mimepull.Header;

import java.util.List;
import java.util.Locale;

import static org.ballerinalang.mime.util.MimeConstants.HEADERS_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.HEADER_NAMES_ARRAY_FIELD;

/**
 * Handler to communicate with Entity Header map and Header name array.
 *
 * @since slp3
 */
public class EntityHeaderHandler {

    private static BMapType mapType = new BMapType(new BArrayType(BTypes.typeString));

    /**
     * Get the entity header map. If not exist, creates new one.
     *
     * @param entity Represent a ballerina entity
     * @return the header map
     */
    @SuppressWarnings("unchecked")
    public static MapValue<BString, Object> getEntityHeaderMap(ObjectValue entity) {
        MapValue<BString, Object> httpHeaders = (MapValue<BString, Object>) entity.get(HEADERS_MAP_FIELD);
        if (httpHeaders == null) {
            httpHeaders = getNewHeaderMap();
            entity.set(HEADERS_MAP_FIELD, httpHeaders);
        }
        return httpHeaders;
    }

    private static ArrayValue getEntityHeaderNameArray(ObjectValue entity) {
        ArrayValue headerNames = (ArrayValue) entity.get(HEADER_NAMES_ARRAY_FIELD);
        if (headerNames == null) {
            headerNames = getNewHeaderNamesArray();
            entity.set(HEADER_NAMES_ARRAY_FIELD, headerNames);
        }
        return headerNames;
    }

    static void populateBodyPartHeaders(ObjectValue partStruct, List<? extends Header> bodyPartHeaders) {
        MapValue<BString, Object> httpHeaders = getNewHeaderMap();
        ArrayValue headerNames = getNewHeaderNamesArray();

        int index = 0;
        for (final Header header : bodyPartHeaders) {
            httpHeaders.put(StringUtils.fromString(header.getName().toLowerCase(Locale.getDefault())),
                            BValueCreator.createArrayValue(new BString[]{StringUtils.fromString(header.getValue())}));
            headerNames.add(index++, StringUtils.fromString(header.getName()));
        }
        partStruct.set(MimeConstants.HEADERS_MAP_FIELD, httpHeaders);
        partStruct.set(MimeConstants.HEADER_NAMES_ARRAY_FIELD, headerNames);
    }

    /**
     * Extract the header value from a body part for a given header name.
     *
     * @param entity     Represent a ballerina entity
     * @param headerName Represent an http header name
     * @return a header value for the given header name. If header map or the value does not exist, returns null
     */
    @SuppressWarnings("unchecked")
    public static String getHeaderValue(ObjectValue entity, String headerName) {
        MapValue<BString, Object> headerMap = (MapValue<BString, Object>) entity.get(HEADERS_MAP_FIELD);
        if (headerMap == null) {
            return null;
        }
        ArrayValue headerValues = (ArrayValue) headerMap.get(StringUtils.fromString(headerName));
        if (headerValues == null || headerValues.size() < 1) {
            return null;
        }
        return headerValues.getBString(0).getValue();
    }

    /**
     * Adds header to the given header map and add header name to the entity header names array.
     *
     * @param entity  Represent a ballerina entity
     * @param headers Represent a ballerina entity header map
     * @param key     Represent header name
     * @param value   Represent header value
     */
    public static void addHeader(ObjectValue entity, MapValue<BString, Object> headers, String key,
                                 String value) {
        headers.put(StringUtils.fromString(key.toLowerCase(Locale.getDefault())),
                    BValueCreator.createArrayValue(new BString[]{StringUtils.fromString(value)}));

        // update header name array
        ArrayValue headerNames = getEntityHeaderNameArray(entity);
        headerNames.add(headerNames.size(), StringUtils.fromString(key));
    }

    /**
     * Creates new entity header map representing Entity header map.
     *
     * @return a header header map
     */
    public static MapValue<BString, Object> getNewHeaderMap() {
        return new MapValueImpl<>(mapType);
    }

    private static ArrayValue getNewHeaderNamesArray() {
        return (ArrayValue) BValueCreator.createArrayValue(new BString[0]);
    }
}
