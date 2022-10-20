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
     * @param identifier encoded identifier string
     * @return decoded identifier
     */
    public static String escapeSpecialCharacters(String identifier) {
        return Utils.escapeSpecialCharacters(identifier);
    }

    /**
     * <p>Unescapes any Java literals found in the {@code String}.
     * For example, it will turn a sequence of {@code '\'} and
     * {@code 'n'} into a newline character, unless the {@code '\'}
     * is preceded by another {@code '\'}.</p>
     *
     * @param str the {@code String} to unescape, may be null
     * @return a new unescaped {@code String}, {@code null} if null string input
     */
    public static String unescapeJava(String str) {
        return Utils.unescapeJava(str);
    }
}
