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
     * The <em>float</em> value type.
     */
    FLOAT("float"),

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
    TABLE("table"),
    STREAM("stream"),
    JSON("json"),
    XML("xml"),
    ANY("any"),
    MAP("map"),
    FUTURE("future"),
    PACKAGE("package"),
    STRUCT("struct"),
    ENUM("enum"),
    SERVICE("service"),
    CONNECTOR("connector"),
    ENDPOINT("endpoint"),
    FUNCTION("function"),
    ANNOTATION("annotation"),
    ARRAY("[]"),
    UNION("|"),
    VOID(""),
    NIL("null"),
    NONE(""),
    OTHER("other"),
    MESSAGE("message"),
    XML_ATTRIBUTES("xml-attributes"),
    INTERMEDIATE_COLLECTION("intermediate_collection"),
    TUPLE("tuple"),
    RECORD("record"),
    FINITE("finite"),
    SINGLETON("singleton"),
    ;

    private String name;

    TypeKind(String name) {
        this.name = name;
    }

    public String typeName() {
        return name;
    }
}
