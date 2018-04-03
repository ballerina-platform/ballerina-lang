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
        stringTypeKindMap.put("float", TypeKind.FLOAT);
        stringTypeKindMap.put("boolean", TypeKind.BOOLEAN);
        stringTypeKindMap.put("string", TypeKind.STRING);
        stringTypeKindMap.put("blob", TypeKind.BLOB);
        stringTypeKindMap.put("json", TypeKind.JSON);
        stringTypeKindMap.put("xml", TypeKind.XML);
        stringTypeKindMap.put("table", TypeKind.TABLE);
        stringTypeKindMap.put("stream", TypeKind.STREAM);
        stringTypeKindMap.put("any", TypeKind.ANY);
        stringTypeKindMap.put("map", TypeKind.MAP);
        stringTypeKindMap.put("future", TypeKind.FUTURE);
        stringTypeKindMap.put("typedesc", TypeKind.TYPEDESC);
        stringTypeKindMap.put("message", TypeKind.MESSAGE);
        stringTypeKindMap.put("()", TypeKind.NIL);
        stringTypeKindMap.put("null", TypeKind.NIL);
    }

    public static TypeKind stringToTypeKind(String typeName) {
        return stringTypeKindMap.get(typeName);
    }
    
}
