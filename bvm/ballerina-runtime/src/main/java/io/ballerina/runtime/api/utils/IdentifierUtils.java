/*
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.runtime.api.utils;

import io.ballerina.identifier.Utils;

/**
 * Utils class that provides methods to decode identifiers with special characters.
 *
 * @since 2.0.0
 */
public class IdentifierUtils {

    private IdentifierUtils() {
    }

    /**
     * Decode the encoded identifiers for runtime calls.
     *
     * @param encodedIdentifier encoded identifier string
     * @return decoded identifier
     */
    public static String decodeIdentifier(String encodedIdentifier) {
        return Utils.decodeIdentifier(encodedIdentifier);
    }

    /**
     * Escape the special characters in an identifier with a preceding `\`.
     *
     * @param identifier identifier string
     * @return a string of characters with special characters converted to their escaped form
     */
    public static String escapeSpecialCharacters(String identifier) {
        return Utils.escapeSpecialCharacters(identifier);
    }

    /**
     * Unescapes a ballerina string.
     *
     * @param text ballerina string to unescape
     * @return a string of characters with any escaped characters converted to their unescaped form
     */
    public static String unescapeBallerina(String text) {
        return Utils.unescapeBallerina(text);
    }
}
