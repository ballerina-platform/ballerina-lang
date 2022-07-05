/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.core.model;

/**
 * {@code AttachmentPoint} represents places where an annotation can be attached.
 * 
 * @since 0.85
 */
public enum AttachmentPoint {
    SERVICE("service"),
    RESOURCE("resource"),
    CONNECTOR("connector"),
    ACTION("action"),
    FUNCTION("function"),
    STRUCT("struct"),
    CONSTANT("const"),
    PARAMETER("parameter"),
    ANNOTATION("annotation");
    
    private String value;

    AttachmentPoint(String value) {
        this.value = value;
    }

    /**
     * Get the string value of the attachment point.
     * 
     * @return string value of the attachment point
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
};
