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
public class BindgenConstants {

    private BindgenConstants() {
    }

    public static final String BALLERINA_BINDINGS_DIR = "ballerina_bindings";
    public static final String BINDINGS_DIR = "bindings";
    public static final String ACCESS_FIELD = "access";
    public static final String ACCESS_FIELD_INTEROP_TYPE = "@java:FieldGet";
    public static final String BAL_EXTENSION = ".bal";
    public static final String BBGEN_CLASS_TEMPLATE_NAME = "bridge_class";
    public static final String CONSTANTS_TEMPLATE_NAME = "constants";
    public static final String ERROR_TEMPLATE_NAME = "exception";
    public static final String COMPONENT_IDENTIFIER = "bindgen";
    public static final String CONSTRUCTOR_INTEROP_TYPE = "@java:Constructor";
    public static final String METHOD_INTEROP_TYPE = "@java:Method";
    public static final String MUTATE_FIELD = "mutate";
    public static final String MUTATE_FIELD_INTEROP_TYPE = "@java:FieldSet";
    public static final String DEFAULT_TEMPLATE_DIR = "/templates";
    public static final String USER_DIR = "user.dir";
    public static final String UTILS_DIR = "utils";
    public static final String CONSTANTS_FILE_NAME = "Constants.bal";
    public static final String DEPENDENCIES_DIR = "dependencies";
    public static final String ERROR_TYPES_DIR = "error_types";
    public static final String ARRAY_BRACKETS = "[]";
    static final String TARGET_DIR = "target";
    static final String MVN_REPO = "platform-libs";
    static final String FILE_SEPARATOR = "file.separator";
    static final String MUSTACHE_FILE_EXTENSION = ".mustache";
    static final String TEMPLATES_DIR_PATH_KEY = "templates.dir.path";
    static final String BALLERINA_TOML = "Ballerina.toml";
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
