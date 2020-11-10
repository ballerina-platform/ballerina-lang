/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.MimeConstants.ASSIGNMENT;
import static org.ballerinalang.mime.util.MimeConstants.BOUNDARY;
import static org.ballerinalang.mime.util.MimeConstants.FIRST_ELEMENT;
import static org.ballerinalang.mime.util.MimeConstants.INVALID_HEADER_PARAM_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.INVALID_HEADER_VALUE_ERROR;
import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_AS_PRIMARY_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.SEMICOLON;

/**
 * Utility methods for parsing headers.
 *
 * @since 0.963.0
 */
public class HeaderUtil {

    private static final MapType stringMapType = TypeCreator.createMapType(PredefinedTypes.TYPE_STRING);

    /**
     * Given a header value, get it's parameters.
     *
     * @param headerValue Header value as a string
     * @return Parameter map
     */
    public static BMap<BString, Object> getParamMap(String headerValue) {
        BMap<BString, Object> paramMap = null;
        if (headerValue.contains(SEMICOLON)) {
            extractValue(headerValue);
            List<String> paramList = Arrays.stream(headerValue.substring(headerValue.indexOf(SEMICOLON) + 1)
                                                   .split(SEMICOLON)).map(String::trim).collect(Collectors.toList());
            paramMap = validateParams(paramList) ? getHeaderParamMap(paramList) : getEmptyMap();
        } else {
            paramMap = getEmptyMap();
        }
        return paramMap;
    }

    /**
     * Get header value without parameters.
     *
     * @param headerValue Header value with parameters as a string
     * @return Header value without parameters
     */
    public static String getHeaderValue(String headerValue) {
        return extractValue(headerValue.trim());
    }

    /**
     * Extract header value.
     *
     * @param headerValue Header value with parameters as a string
     * @return Header value without parameters
     */
    private static String extractValue(String headerValue) {
        String value = headerValue.substring(0, headerValue.indexOf(SEMICOLON)).trim();
        if (value.isEmpty()) {
            throw MimeUtil.createError(INVALID_HEADER_VALUE_ERROR, "invalid header value: " + headerValue);
        }
        return value;
    }

    private static boolean validateParams(List<String> paramList) {
        //validate header values which ends with semicolon without params
        return !(paramList.size() == 1 && paramList.get(0).isEmpty());
    }

    /**
     * Given a list of string parameter list, create ballerina specific header parameter map.
     *
     * @param paramList List of parameters
     * @return Ballerina map
     */
    private static BMap<BString, Object> getHeaderParamMap(List<String> paramList) {
        BMap<BString, Object> paramMap = getEmptyMap();
        for (String param : paramList) {
            if (param.contains("=")) {
                String[] keyValuePair = param.split("=", 2);
                if (keyValuePair.length != 2 || keyValuePair[0].isEmpty() || keyValuePair[1].isEmpty()) {
                    throw MimeUtil.createError(INVALID_HEADER_PARAM_ERROR, "invalid header parameter: " + param);
                }
                paramMap.put(StringUtils.fromString(keyValuePair[0].trim()),
                             StringUtils.fromString(keyValuePair[1].trim()));
            } else {
                //handle when parameter value is optional
                paramMap.put(StringUtils.fromString(param.trim()), null);
            }
        }
        return paramMap;
    }

    static boolean isHeaderExist(List<String> headers) {
        return headers != null && headers.get(FIRST_ELEMENT) != null && !headers.get(FIRST_ELEMENT).isEmpty();
    }

    /**
     * Get the header value intact with parameters.
     *
     * @param headerValue Header value as a string
     * @param map         Represent a parameter map
     * @return Header value along with it's parameters as a string
     */
    public static String appendHeaderParams(StringBuilder headerValue, BMap<BString, BString> map) {
        int index = 0;
        if (map != null && !map.isEmpty()) {
            BString[] keys = map.getKeys();
            if (keys.length != 0) {
                for (BString key : keys) {
                    String paramValue = map.get(key).getValue();
                    if (index == keys.length - 1) {
                        headerValue.append(key).append(ASSIGNMENT).append(paramValue);
                    } else {
                        headerValue.append(key).append(ASSIGNMENT).append(paramValue).append(SEMICOLON);
                        index = index + 1;
                    }
                }
            }
        }
        return headerValue.toString();
    }

    public static boolean isMultipart(String contentType) {
        return contentType != null && contentType.startsWith(MULTIPART_AS_PRIMARY_TYPE);
    }

    /**
     * Given a Content-Type, extract the boundary parameter value out of it.
     *
     * @param contentType Represent the value of Content-Type header including parameters
     * @return A ballerina string that has the boundary parameter value
     */
    public static String extractBoundaryParameter(String contentType) {
        BMap<BString, Object> paramMap = HeaderUtil.getParamMap(contentType);
        return paramMap.get(StringUtils.fromString(BOUNDARY)) != null ?
                paramMap.getStringValue(StringUtils.fromString(BOUNDARY)).getValue() : null;
    }

    public static void setHeaderToEntity(BObject entity, String key, String value) {
        BMap<BString, Object> httpHeaders = EntityHeaderHandler.getEntityHeaderMap(entity);
        EntityHeaderHandler.addHeader(entity, httpHeaders, key, value);
    }

    public static String getBaseType(BObject entityStruct) throws MimeTypeParseException {
        String contentType = EntityHeaderHandler.getHeaderValue(entityStruct, MimeConstants.CONTENT_TYPE);
        if (contentType != null) {
            return new MimeType(contentType).getBaseType();
        }
        return null;
    }

    private static BMap<BString, Object> getEmptyMap() {
        return ValueCreator.createMapValue(stringMapType);
    }
}
