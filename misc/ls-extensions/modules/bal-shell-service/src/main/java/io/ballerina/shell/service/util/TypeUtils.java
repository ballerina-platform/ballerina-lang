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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;

/**
 * Utility functions related with type.
 *
 * @since 2201.1.1
 */
public class TypeUtils {
    /**
     * Get applicable mime type for a given type.
     *
     * @param type runtime type of the value
     * @return mime type
     */
    public static String getMimeTypeFromName(Type type) {
        switch (type.getTag()) {
            case TypeTags.JSON_TAG:
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.MAP_TAG:
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
                return Constants.MIME_TYPE_JSON;
            case TypeTags.TABLE_TAG:
                return Constants.MIME_TYPE_TABLE;
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_TEXT_TAG:
                return Constants.MIME_TYPE_XML;
            default:
                return Constants.MIME_TYPE_PLAIN_TEXT;
        }
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
        switch (type.getTag()) {
            case TypeTags.JSON_TAG:
            case TypeTags.RECORD_TYPE_TAG:
            case TypeTags.MAP_TAG:
            case TypeTags.ARRAY_TAG:
            case TypeTags.TUPLE_TAG:
            case TypeTags.TABLE_TAG:
                return StringUtils.getJsonString(value);
            case TypeTags.XML_TAG:
            case TypeTags.XML_ELEMENT_TAG:
            case TypeTags.XML_COMMENT_TAG:
            case TypeTags.XML_PI_TAG:
            case TypeTags.XML_TEXT_TAG:
            case TypeTags.OBJECT_TYPE_TAG:
                return StringUtils.getStringValue(value, null);
            default:
                return StringUtils.getExpressionStringValue(value, null);
        }
    }
}
