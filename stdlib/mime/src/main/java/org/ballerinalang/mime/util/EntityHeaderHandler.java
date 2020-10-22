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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.TypeCreator;
import io.ballerina.runtime.api.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
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

    private static MapType mapType =
            TypeCreator.createMapType(TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING));

    /**
     * Get the entity header map. If not exist, creates new one.
     *
     * @param entity Represent a ballerina entity
     * @return the header map
     */
    @SuppressWarnings("unchecked")
    public static BMap<BString, Object> getEntityHeaderMap(BObject entity) {
        BMap<BString, Object> httpHeaders = (BMap<BString, Object>) entity.get(HEADERS_MAP_FIELD);
        if (httpHeaders == null) {
            httpHeaders = getNewHeaderMap();
            entity.set(HEADERS_MAP_FIELD, httpHeaders);
        }
        return httpHeaders;
    }

    private static BArray getEntityHeaderNameArray(BObject entity) {
        BArray headerNames = (BArray) entity.get(HEADER_NAMES_ARRAY_FIELD);
        if (headerNames == null) {
            headerNames = getNewHeaderNamesArray();
            entity.set(HEADER_NAMES_ARRAY_FIELD, headerNames);
        }
        return headerNames;
    }

    static void populateBodyPartHeaders(BObject partStruct, List<? extends Header> bodyPartHeaders) {
        BMap<BString, Object> httpHeaders = getNewHeaderMap();
        BArray headerNames = getNewHeaderNamesArray();

        int index = 0;
        for (final Header header : bodyPartHeaders) {
            httpHeaders.put(StringUtils.fromString(header.getName().toLowerCase(Locale.getDefault())),
                            ValueCreator.createArrayValue(new BString[]{StringUtils.fromString(header.getValue())}));
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
    public static String getHeaderValue(BObject entity, String headerName) {
        BMap<BString, Object> headerMap = (BMap<BString, Object>) entity.get(HEADERS_MAP_FIELD);
        if (headerMap == null) {
            return null;
        }
        BArray headerValues = (BArray) headerMap.get(StringUtils.fromString(headerName));
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
    public static void addHeader(BObject entity, BMap<BString, Object> headers, String key,
                                 String value) {
        headers.put(StringUtils.fromString(key.toLowerCase(Locale.getDefault())),
                    ValueCreator.createArrayValue(new BString[]{StringUtils.fromString(value)}));

        // update header name array
        BArray headerNames = getEntityHeaderNameArray(entity);
        headerNames.add(headerNames.size(), StringUtils.fromString(key));
    }

    /**
     * Creates new entity header map representing Entity header map.
     *
     * @return a header header map
     */
    public static BMap<BString, Object> getNewHeaderMap() {
        return ValueCreator.createMapValue(mapType);
    }

    private static BArray getNewHeaderNamesArray() {
        return (BArray) ValueCreator.createArrayValue(new BString[0]);
    }
}
