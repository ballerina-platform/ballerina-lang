/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.converters.util;

/**
 * Container for error messages of the JSON converter exception.
 *
 * @since 2.0.0
 */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static String parserException(String json) {
        return String.format("Couldn't read the JSON Schema from the given string: %s", json);
    }

    public static String unsupportedType() {
        return "Unsupported, Null or Missing type in Json";
    }

    public static String multipleTypes(String property) {
        return String.format("Properties must have a single non-null type. The property:%n'%s'%n" +
                        "has a type which is not one of: 'string','object','array','boolean' or numeric types",
                property);
    }

    public static String invalidReference(String property) {
        return String.format("Invalid reference value :'%s'%nBallerina only supports local reference values.",
                property);
    }
}
