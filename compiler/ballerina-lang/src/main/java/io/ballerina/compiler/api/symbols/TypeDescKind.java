/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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
package io.ballerina.compiler.api.symbols;

/**
 * Represents the Type Kinds.
 *
 * @since 2.0.0
 */
public enum TypeDescKind {
    INT("int"),
    INT_SIGNED8("Signed8"),
    INT_UNSIGNED8("Unsigned8"),
    INT_SIGNED16("Signed16"),
    INT_UNSIGNED16("Unsigned16"),
    INT_SIGNED32("Signed32"),
    INT_UNSIGNED32("Unsigned32"),
    BYTE("byte"),
    FLOAT("float"),
    DECIMAL("decimal"),
    STRING("string"),
    STRING_CHAR("Char"),
    BOOLEAN("boolean"),
    NIL("nil"),
    ANY("any"),
    ANYDATA("anydata"),
    ARRAY("array"),
    OBJECT("object"),
    RECORD("record"),
    MAP("map"),
    ERROR("error"),
    FUNCTION("function"),
    TUPLE("tuple"),
    STREAM("stream"),
    FUTURE("future"),
    TYPEDESC("typedesc"),
    TYPE_REFERENCE("typeReference"),
    UNION("union"),
    INTERSECTION("intersection"),
    JSON("json"),
    XML("xml"),
    XML_ELEMENT("Element"),
    XML_PROCESSING_INSTRUCTION("ProcessingInstruction"),
    XML_COMMENT("Comment"),
    XML_TEXT("Text"),
    HANDLE("handle"),
    TABLE("table"),
    SINGLETON("singleton"),
    READONLY("readonly"),
    NEVER("never"),
    COMPILATION_ERROR("CompilationError"),
    NONE("None"),
    REGEXP("regexp");

    private final String name;

    TypeDescKind(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isIntegerType() {
        return switch (this) {
            case INT,
                 INT_SIGNED8,
                 INT_UNSIGNED8,
                 INT_SIGNED16,
                 INT_UNSIGNED16,
                 INT_SIGNED32,
                 INT_UNSIGNED32,
                 BYTE -> true;
            default -> false;
        };

    }

    public boolean isXMLType() {
        return switch (this) {
            case XML, XML_COMMENT, XML_ELEMENT, XML_PROCESSING_INSTRUCTION, XML_TEXT -> true;
            default -> false;
        };

    }

    public boolean isStringType() {
        return this == STRING || this == STRING_CHAR;
    }
}
