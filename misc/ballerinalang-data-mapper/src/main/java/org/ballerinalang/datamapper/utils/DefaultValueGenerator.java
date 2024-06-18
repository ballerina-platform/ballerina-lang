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
public class DefaultValueGenerator {
    private DefaultValueGenerator() {
    }

    public static Serializable generateDefaultValues(String type) {
        switch (type) {
            case "int":
            case "Signed8":
            case "Unsigned8":
            case "Signed16":
            case "Unsigned16":
            case "Signed32":
            case "Unsigned32":
            case "byte":
                return 0;
            case "float":
            case "decimal":
                return 0.0;
            case "string":
            case "Char":
                return "\"\"";
            case "BOOLEAN":
                return false;
            case "nil":
            case "any":
            case "union":
            case "json":
                return "()";
            case "array":
            case "tuple":
                return "[]";
            case "object":
                return "new T()";
            case "record":
            case "map":
                return "{}";
            case "xml":
            case "Element":
            case "ProcessingInstruction":
            case "Comment":
            case "Text":
                return "``";
            default:
                return "";
        }
    }
}
