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
package org.ballerinalang.model.types;

/**
 * {@code TypeKind} represents kind of a type.
 *
 * @since 0.94
 */
public enum TypeKind {
    /**
     * The <em>integer</em> value type.
     */
    INT("int"),

    /**
     * The <em>byte</em> value type.
     */
    BYTE("byte"),

    /**
     * The <em>float</em> value type.
     */
    FLOAT("float"),

    /**
     * The <em>decimal</em> value type.
     */
    DECIMAL("decimal"),

    /**
     * The <em>string</em> value type.
     */
    STRING("string"),

    /**
     * The <em>boolean</em> value type.
     */
    BOOLEAN("boolean"),

    /**
     * The <em>blob</em> value type.
     */
    BLOB("blob"),

    TYPEDESC("typedesc"),
    STREAM("stream"),
    TABLE("table"),
    JSON("json"),
    XML("xml"),
    ANY("any"),
    ANYDATA("anydata"),
    MAP("map"),
    FUTURE("future"),
    PACKAGE("package"),
    SERVICE("service"),
    CONNECTOR("connector"),
    ENDPOINT("endpoint"),
    FUNCTION("function"),
    ANNOTATION("annotation"),
    ARRAY("[]"),
    UNION("|"),
    INTERSECTION("&"),
    VOID(""),
    NIL("null"),
    NEVER("never"),
    NONE(""),
    OTHER("other"),
    ERROR("error"),
    TUPLE("tuple"),
    OBJECT("object"),
    RECORD("record"),
    FINITE("finite"),
    CHANNEL("channel"),
    HANDLE("handle"),
    READONLY("readonly"),
    TYPEPARAM("typeparam"),
    ;

    private String name;

    TypeKind(String name) {
        this.name = name;
    }

    public String typeName() {
        return name;
    }
}
