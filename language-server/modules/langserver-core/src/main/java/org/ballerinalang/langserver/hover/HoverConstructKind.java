/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.hover;

/**
 * Hover Construct Kind.
 * 
 * since @2201.2.0
 */
public enum HoverConstructKind {
    Function("functions"),
    Class("classes"),
    Listener("listeners"),
    Client("clients"),
    Record("records"),
    Enum("enums"),
    Variable("variables"),
    Constant("constants"),
    ObjectType("objectTypes"),
    Annotation("annotations"),
    Error("errors"),
    Type("types");
    
    private String value;

    HoverConstructKind(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
