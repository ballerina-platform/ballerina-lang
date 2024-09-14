/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.datamapper.utils;

import java.io.Serializable;

/**
 * Return default value for TypeDescKind.
 */
public final class DefaultValueGenerator {

    private DefaultValueGenerator() {
    }

    public static Serializable generateDefaultValues(String type) {
        return switch (type) {
            case "int", "Signed8", "Unsigned8", "Signed16", "Unsigned16", "Signed32", "Unsigned32", "byte" -> 0;
            case "float", "decimal" -> 0.0;
            case "string", "Char" -> "\"\"";
            case "BOOLEAN" -> false;
            case "nil", "any", "union", "json" -> "()";
            case "array", "tuple" -> "[]";
            case "object" -> "new T()";
            case "record", "map" -> "{}";
            case "xml", "Element", "ProcessingInstruction", "Comment", "Text" -> "``";
            default -> "";
        };
    }
}
