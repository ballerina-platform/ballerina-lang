/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.shell.service.util;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;

/**
 * Utility functions related with type.
 *
 * @since 2201.1.1
 */
public final class TypeUtils {

    private TypeUtils() {
    }

    /**
     * Get applicable mime type for a given type.
     *
     * @param type runtime type of the value
     * @return mime type
     */
    public static String getMimeTypeFromName(Type type) {
        return switch (type.getTag()) {
            case TypeTags.JSON_TAG,
                 TypeTags.RECORD_TYPE_TAG,
                 TypeTags.MAP_TAG,
                 TypeTags.ARRAY_TAG,
                 TypeTags.TUPLE_TAG -> Constants.MIME_TYPE_JSON;
            case TypeTags.TABLE_TAG -> Constants.MIME_TYPE_TABLE;
            case TypeTags.XML_TAG,
                 TypeTags.XML_ELEMENT_TAG,
                 TypeTags.XML_PI_TAG,
                 TypeTags.XML_COMMENT_TAG,
                 TypeTags.XML_TEXT_TAG -> Constants.MIME_TYPE_XML;
            default -> Constants.MIME_TYPE_PLAIN_TEXT;
        };
    }

    /**
     * Returns json string for a given object if that object is accepted as json convertible type
     * otherwise returns the string representation for the object.
     *
     * @param value object needs to be converted
     * @return converted string
     */
    public static String convertToJsonIfAcceptable(Object value) {
        Type type = io.ballerina.runtime.api.utils.TypeUtils.getType(value);
        return switch (type.getTag()) {
            case TypeTags.JSON_TAG,
                 TypeTags.RECORD_TYPE_TAG,
                 TypeTags.MAP_TAG,
                 TypeTags.ARRAY_TAG,
                 TypeTags.TUPLE_TAG,
                 TypeTags.TABLE_TAG -> StringUtils.getJsonString(value);
            case TypeTags.XML_TAG,
                 TypeTags.XML_ELEMENT_TAG,
                 TypeTags.XML_COMMENT_TAG,
                 TypeTags.XML_PI_TAG,
                 TypeTags.XML_TEXT_TAG,
                 TypeTags.OBJECT_TYPE_TAG -> StringUtils.getStringValue(value);
            default -> StringUtils.getExpressionStringValue(value);
        };
    }
}
