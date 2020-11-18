/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.commons.codeaction;

/**
 * Represents the Code Action Node Type.
 * 
 * @since 1.0.2
 */
public enum CodeActionNodeType {
    FUNCTION,
    OBJECT_FUNCTION,
    CLASS_FUNCTION,
    RESOURCE,
    SERVICE,
    OBJECT,
    CLASS,
    RECORD,
    IMPORTS,
    NONE;

    /**
     * Get the node type by name.
     * 
     * @param name node type name
     * @return {@link CodeActionNodeType} node type
     */
    public static CodeActionNodeType getNodeTypeByName(String name) {
        for (CodeActionNodeType codeActionNodeType : CodeActionNodeType.values()) {
            if (name.equals(codeActionNodeType.name())) {
                return codeActionNodeType;
            }
        }
        
        return CodeActionNodeType.NONE;
    }
}
