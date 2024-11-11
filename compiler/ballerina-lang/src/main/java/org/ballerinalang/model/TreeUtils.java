/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model;

import org.ballerinalang.model.types.TypeKind;

import java.util.HashMap;
import java.util.Map;

/**
 * This contains model tree related utility functions. 
 */
public final class TreeUtils {

    private static final Map<String, TypeKind> STRING_TYPE_KIND_MAP = new HashMap<>();
    
    static {
        STRING_TYPE_KIND_MAP.put("int", TypeKind.INT);
        STRING_TYPE_KIND_MAP.put("byte", TypeKind.BYTE);
        STRING_TYPE_KIND_MAP.put("float", TypeKind.FLOAT);
        STRING_TYPE_KIND_MAP.put("decimal", TypeKind.DECIMAL);
        STRING_TYPE_KIND_MAP.put("boolean", TypeKind.BOOLEAN);
        STRING_TYPE_KIND_MAP.put("string", TypeKind.STRING);
        STRING_TYPE_KIND_MAP.put("json", TypeKind.JSON);
        STRING_TYPE_KIND_MAP.put("xml", TypeKind.XML);
        STRING_TYPE_KIND_MAP.put("stream", TypeKind.STREAM);
        STRING_TYPE_KIND_MAP.put("table", TypeKind.TABLE);
        STRING_TYPE_KIND_MAP.put("any", TypeKind.ANY);
        STRING_TYPE_KIND_MAP.put("anydata", TypeKind.ANYDATA);
        STRING_TYPE_KIND_MAP.put("map", TypeKind.MAP);
        STRING_TYPE_KIND_MAP.put("future", TypeKind.FUTURE);
        STRING_TYPE_KIND_MAP.put("typedesc", TypeKind.TYPEDESC);
        STRING_TYPE_KIND_MAP.put("error", TypeKind.ERROR);
        STRING_TYPE_KIND_MAP.put("()", TypeKind.NIL);
        STRING_TYPE_KIND_MAP.put("null", TypeKind.NIL);
        STRING_TYPE_KIND_MAP.put("never", TypeKind.NEVER);
        STRING_TYPE_KIND_MAP.put("channel", TypeKind.CHANNEL);
        STRING_TYPE_KIND_MAP.put("service", TypeKind.SERVICE);
        STRING_TYPE_KIND_MAP.put("handle", TypeKind.HANDLE);
        STRING_TYPE_KIND_MAP.put("readonly", TypeKind.READONLY);
    }

    private TreeUtils() {
    }

    public static TypeKind stringToTypeKind(String typeName) {
        return STRING_TYPE_KIND_MAP.get(typeName);
    }
    
}
