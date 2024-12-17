/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen.utils;

/**
 * Class for storing constants related to Ballerina Bindgen CLI tool.
 *
 * @since 1.2.0
 */
public final class BindgenConstants {

    private BindgenConstants() {
    }

    public static final String BAL_EXTENSION = ".bal";
    public static final String COMPONENT_IDENTIFIER = "bindgen";
    static final String DEFAULT_TEMPLATE_DIR = "templates";
    public static final String USER_DIR = "user.dir";
    public static final String ARRAY_BRACKETS = "[]";
    public static final String QUESTION_MARK = "?";
    public static final String MODULES_DIR = "modules";
    static final String NAME = "name";
    static final String CLASS = "'class";
    static final String PARAM_TYPES = "paramTypes";
    static final String TARGET_DIR = "target";
    static final String MVN_REPO = "platform-libs";
    static final String FILE_SEPARATOR = "file.separator";
    static final String FLOAT = "float";
    static final String INT = "int";
    static final String BOOLEAN = "boolean";
    static final String BYTE = "byte";
    static final String FLOAT_ARRAY = "float[]";
    static final String INT_ARRAY = "int[]";
    static final String BOOLEAN_ARRAY = "boolean[]";
    static final String BYTE_ARRAY = "byte[]";
    static final String SHORT = "short";
    static final String CHAR = "char";
    static final String DOUBLE = "double";
    static final String LONG = "long";
    public static final String JAVA_STRING = "String";
    public static final String JAVA_STRING_ARRAY = "String[]";
    static final String HANDLE = "handle";
    public static final String BALLERINA_STRING = "string";
    public static final String BALLERINA_STRING_ARRAY = "string[]";
    public static final String BALLERINA_NILLABLE_STRING = "string?";
    public static final String BALLERINA_NILLABLE_STRING_ARRAY = "string?[]?";
    public static final String EXCEPTION_CLASS_PREFIX = "J";
    public static final String[] BALLERINA_RESERVED_WORDS = {"import", "as", "public", "private", "external", "final",
            "service", "resource", "function", "object", "record", "annotation", "parameter", "transformer",
            "worker", "listener", "remote", "xmlns", "returns", "version", "channel", "abstract", "client", "const",
            "typeof", "source", "on", "int", "byte", "float", "decimal", "boolean", "string", "error", "map", "json",
            "xml", "table", "stream", "any", "typedesc", "type", "future", "anydata", "handle", "var", "new", "init",
            "if", "match", "else", "foreach", "while", "continue", "break", "fork", "join", "some", "all", "try",
            "catch", "finally", "throw", "panic", "trap", "return", "transaction", "abort", "retry", "onretry",
            "retries", "committed", "aborted", "with", "in", "lock", "untaint", "start", "but", "check", "checkpanic",
            "primarykey", "is", "flush", "wait", "default", "from", "select", "where", "limit", "order", "field",
            "let", "Deprecated", "equals", "enum", "readonly", "outer", "conflict", "key", "rollback", "commit",
            "transactional", "distinct", "never", "do"};
}
