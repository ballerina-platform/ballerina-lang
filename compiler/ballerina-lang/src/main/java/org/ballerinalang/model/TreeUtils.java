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
public class TreeUtils {
    
    private static Map<String, TypeKind> stringTypeKindMap = new HashMap<>();
    
    static {
        stringTypeKindMap.put("int", TypeKind.INT);
        stringTypeKindMap.put("byte", TypeKind.BYTE);
        stringTypeKindMap.put("float", TypeKind.FLOAT);
        stringTypeKindMap.put("decimal", TypeKind.DECIMAL);
        stringTypeKindMap.put("boolean", TypeKind.BOOLEAN);
        stringTypeKindMap.put("string", TypeKind.STRING);
        stringTypeKindMap.put("json", TypeKind.JSON);
        stringTypeKindMap.put("xml", TypeKind.XML);
        stringTypeKindMap.put("stream", TypeKind.STREAM);
        stringTypeKindMap.put("table", TypeKind.TABLE);
        stringTypeKindMap.put("any", TypeKind.ANY);
        stringTypeKindMap.put("anydata", TypeKind.ANYDATA);
        stringTypeKindMap.put("map", TypeKind.MAP);
        stringTypeKindMap.put("future", TypeKind.FUTURE);
        stringTypeKindMap.put("typedesc", TypeKind.TYPEDESC);
        stringTypeKindMap.put("error", TypeKind.ERROR);
        stringTypeKindMap.put("()", TypeKind.NIL);
        stringTypeKindMap.put("null", TypeKind.NIL);
        stringTypeKindMap.put("never", TypeKind.NEVER);
        stringTypeKindMap.put("channel", TypeKind.CHANNEL);
        stringTypeKindMap.put("service", TypeKind.SERVICE);
        stringTypeKindMap.put("handle", TypeKind.HANDLE);
        stringTypeKindMap.put("readonly", TypeKind.READONLY);
    }

    public static TypeKind stringToTypeKind(String typeName) {
        return stringTypeKindMap.get(typeName);
    }
    
}
