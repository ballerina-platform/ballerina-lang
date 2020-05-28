/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.debugger;

/**
 * Ballerina variable types(Language Specification v2020R1).
 */
public enum BallerinaValueType {

    // basic, simple types.
    NIL("nil"),
    BOOLEAN("boolean"),
    INT("int"),
    FLOAT("float"),
    DECIMAL("decimal"),
    // basic, sequence types.
    STRING("string"),
    XML("xml"),
    // basic, structured
    ARRAY("array"),
    TUPLE("tuple"),
    MAP("map"),
    RECORD("record"),
    TABLE("table"),
    ERROR("error"),
    // basic, behavioral types.
    FUNCTION("function"),
    FUTURE("future"),
    OBJECT("object"),
    SERVICE("service"),
    TYPE_DESC("typedesc"),
    HANDLE("handle"),
    STREAM("stream"),
    // other types.
    SINGLETON("singleton"),
    UNION("union"),
    OPTIONAL("optional"),
    ANY("any"),
    ANYDATA("anydata"),
    NEVER("never"),
    BYTE("byte"),
    JSON("json"),

    // Note: This is not a valid ballerina variable type. This is only used for labeling jvm variables, which don't
    // map to any of the above ballerina standard variable types.
    UNKNOWN("unknown");

    private final String value;

    BallerinaValueType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
