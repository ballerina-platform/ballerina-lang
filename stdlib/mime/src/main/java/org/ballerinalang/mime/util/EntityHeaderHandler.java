/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.jvnet.mimepull.Header;

import java.util.List;
import java.util.Locale;

import static org.ballerinalang.mime.util.MimeConstants.HEADERS_MAP_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.HEADER_NAMES_ARRAY_FIELD;

/**
 * Utilities related to entity headers.
 *
 * @since slp4
 */
public class EntityHeaderHandler {

    private static BMapType mapType = new BMapType(new BArrayType(BTypes.typeString));

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
                            new ArrayValueImpl(new BString[]{StringUtils.fromString(header.getValue())}));
            headerNames.add(index++, StringUtils.fromString(header.getName()));
        }
        partStruct.set(MimeConstants.HEADERS_MAP_FIELD, httpHeaders);
        partStruct.set(MimeConstants.HEADER_NAMES_ARRAY_FIELD, headerNames);
    }

    public static void addHeader(ObjectValue entity, MapValue<BString, Object> httpHeaders, String key,
                                 String value) {
        httpHeaders.put(StringUtils.fromString(key.toLowerCase(Locale.getDefault())),
                        new ArrayValueImpl(new BString[]{StringUtils.fromString(value)}));

        // update header name array
        ArrayValue headerNames = getEntityHeaderNameArray(entity);
        headerNames.add(headerNames.size(), StringUtils.fromString(key));
    }

    public static MapValue<BString, Object> getNewHeaderMap() {
        return new MapValueImpl<>(mapType);
    }

    private static ArrayValue getNewHeaderNamesArray() {
        return new ArrayValueImpl(new BString[0]);
    }

}
