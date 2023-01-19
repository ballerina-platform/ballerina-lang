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
package org.wso2.ballerinalang.compiler.util;

import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;

/**
 * @since 0.94
 */
public class Names {

    public static final CompilerContext.Key<Names> NAMES_KEY =
            new CompilerContext.Key<>();

    public static final String STRING_SIGNED32 = "Signed32";
    public static final String STRING_SIGNED16 = "Signed16";
    public static final String STRING_SIGNED8 = "Signed8";
    public static final String STRING_UNSIGNED32 = "Unsigned32";
    public static final String STRING_UNSIGNED16 = "Unsigned16";
    public static final String STRING_UNSIGNED8 = "Unsigned8";
    public static final String STRING_CHAR = "Char";
    public static final String STRING_XML_ELEMENT = "Element";
    public static final String STRING_XML_PI = "ProcessingInstruction";
    public static final String STRING_XML_COMMENT = "Comment";
    public static final String STRING_XML_TEXT = "Text";
    public static final String STRING_REGEXP = "RegExp";

    public static final Name EMPTY = new Name("");
    public static final Name DOT = new Name(".");
    public static final Name DEFAULT_PACKAGE = DOT;
    public static final Name TEST_PACKAGE =  new Name("$test");
    public static final Name BALLERINA_ORG = new Name("ballerina");
    public static final Name BALLERINA_INTERNAL_ORG = new Name("ballerinai");
    public static final Name LANG = new Name("lang");
    public static final Name INTERNAL = new Name("__internal");
    public static final Name ANNOTATIONS = new Name("annotations");
    public static final Name JAVA = new Name("jballerina.java");
    public static final Name ARRAY = new Name("array");
    public static final Name DECIMAL = new Name("decimal");
    public static final Name ERROR = new Name("error");
    public static final Name FLOAT = new Name("float");
    public static final Name FUNCTION = new Name("function");
    public static final Name FUTURE = new Name("future");
    public static final Name INT = new Name("int");
    public static final Name BOOLEAN = new Name("boolean");
    public static final Name MAP = new Name("map");
    public static final Name OBJECT = new Name("object");
    public static final Name STREAM = new Name("stream");
    public static final Name QUERY = new Name("query");
    public static final Name RUNTIME = new Name("runtime");
    public static final Name TRANSACTION = new Name("transaction");
    public static final Name OBSERVE = new Name("observe");
    public static final Name CLOUD = new Name("cloud");
    public static final Name TABLE = new Name("table");
    public static final Name TEST = new Name("test");
    public static final Name TYPEDESC = new Name("typedesc");
    public static final Name STRING = new Name("string");
    public static final Name VALUE = new Name("value");
    public static final Name XML = new Name("xml");
    public static final Name JSON = new Name("json");
    public static final Name ANYDATA = new Name("anydata");
    public static final Name REGEXP = new Name("regexp");
    public static final Name UTILS_PACKAGE = new Name("utils");
    public static final Name BUILTIN_ORG = new Name("ballerina");
    public static final Name RUNTIME_PACKAGE = new Name("runtime");
    public static final Name IGNORE = new Name("_");
    public static final Name INVALID = new Name("><");
    public static final Name GEN_VAR_PREFIX = new Name("_$$_");
    public static final Name SERVICE = new Name("service");
    public static final Name LISTENER = new Name("Listener");
    public static final Name INIT_FUNCTION_SUFFIX = new Name(".<init>");
    public static final Name START_FUNCTION_SUFFIX = new Name(".<start>");
    public static final Name STOP_FUNCTION_SUFFIX = new Name(".<stop>");
    public static final Name SELF = new Name("self");
    public static final Name USER_DEFINED_INIT_SUFFIX = new Name("init");
    public static final Name GENERATED_INIT_SUFFIX = new Name("$init$");
    // TODO remove when current project name is read from manifest
    public static final Name ANON_ORG = new Name("$anon");
    public static final Name NIL_VALUE = new Name("()");
    public static final Name QUESTION_MARK = new Name("?");
    public static final Name ORG_NAME_SEPARATOR = new Name("/");
    public static final Name VERSION_SEPARATOR = new Name(":");
    public static final Name ALIAS_SEPARATOR = VERSION_SEPARATOR;
    public static final Name ANNOTATION_TYPE_PARAM = new Name("typeParam");
    public static final Name ANNOTATION_BUILTIN_SUBTYPE = new Name("builtinSubtype");
    public static final Name ANNOTATION_ISOLATED_PARAM = new Name("isolatedParam");

    public static final Name BIR_BASIC_BLOCK_PREFIX = new Name("bb");
    public static final Name BIR_LOCAL_VAR_PREFIX = new Name("%");
    public static final Name BIR_GLOBAL_VAR_PREFIX = new Name("#");

    public static final Name DETAIL_MESSAGE = new Name("message");
    public static final Name DETAIL_CAUSE = new Name("cause");

    public static final Name NEVER = new Name("never");
    public static final Name RAW_TEMPLATE = new Name("RawTemplate");
    public static final Name CLONEABLE = new Name("Cloneable");
    public static final Name CLONEABLE_INTERNAL = new Name("__Cloneable");
    public static final Name OBJECT_ITERABLE = new Name("Iterable");

    // Subtypes
    public static final Name SIGNED32 = new Name(STRING_SIGNED32);
    public static final Name SIGNED16 = new Name(STRING_SIGNED16);
    public static final Name SIGNED8 = new Name(STRING_SIGNED8);
    public static final Name UNSIGNED32 = new Name(STRING_UNSIGNED32);
    public static final Name UNSIGNED16 = new Name(STRING_UNSIGNED16);
    public static final Name UNSIGNED8 = new Name(STRING_UNSIGNED8);
    public static final Name CHAR = new Name(STRING_CHAR);
    public static final Name XML_ELEMENT = new Name(STRING_XML_ELEMENT);
    public static final Name XML_PI = new Name(STRING_XML_PI);
    public static final Name XML_COMMENT = new Name(STRING_XML_COMMENT);
    public static final Name XML_TEXT = new Name(STRING_XML_TEXT);

    // Names related to transactions.
    public static final Name TRANSACTION_PACKAGE = new Name("transactions");
    public static final Name TRANSACTION_INFO_RECORD = new Name("Info");
    public static final Name TRANSACTION_ORG = new Name("ballerina");
    public static final Name CREATE_INT_RANGE = new Name("createIntRange");
    public static final Name START_TRANSACTION = new Name("startTransaction");
    public static final Name CURRENT_TRANSACTION_INFO = new Name("info");
    public static final Name IS_TRANSACTIONAL = new Name("isTransactional");
    public static final Name ROLLBACK_TRANSACTION = new Name("rollbackTransaction");
    public static final Name END_TRANSACTION = new Name("endTransaction");
    public static final Name GET_AND_CLEAR_FAILURE_TRANSACTION = new Name("getAndClearFailure");
    public static final Name CLEAN_UP_TRANSACTION = new Name("cleanupTransactionContext");
    public static final Name BEGIN_REMOTE_PARTICIPANT = new Name("beginRemoteParticipant");
    public static final Name START_TRANSACTION_COORDINATOR = new Name("startTransactionCoordinator");

    // Names related to streams
    public static final Name CONSTRUCT_STREAM = new Name("construct");
    public static final Name ABSTRACT_STREAM_ITERATOR = new Name("_StreamImplementor");
    public static final Name ABSTRACT_STREAM_CLOSEABLE_ITERATOR = new Name("_CloseableStreamImplementor");

    // Module Versions
    public static final Name DEFAULT_VERSION = new Name("0.0.0");
    public static final Name DEFAULT_MAJOR_VERSION = new Name("0");

    public CompilerContext context;

    public static Names getInstance(CompilerContext context) {
        Names names = context.get(NAMES_KEY);
        if (names == null) {
            names = new Names(context);
            context.put(NAMES_KEY, names);
        }
        return names;
    }

    private Names(CompilerContext context) {
        this.context = context;
        this.context.put(NAMES_KEY, this);
    }

    public Name fromIdNode(BLangIdentifier identifier) {
        // identifier.value cannot be null
        return fromString(identifier.value);
    }

    public Name originalNameFromIdNode(BLangIdentifier identifier) {
        if (identifier.originalValue == null || identifier.value.equals(identifier.originalValue)) {
            return fromString(identifier.value);
        }
        return fromString(identifier.originalValue);
    }

    public static Name fromString(String value) {
        // value cannot be null
        if (value.equals("")) {
            return EMPTY;
        } else if (value.equals("_")) {
            return IGNORE;
        }
        return new Name(value);
    }

    public Name fromTypeKind(TypeKind typeKind) {
        return fromString(typeKind.typeName());
    }

    public Name merge(Name... names) {
        StringBuilder builder = new StringBuilder();
        for (Name name : names) {
            builder.append(name.value);
        }
        return new Name(builder.toString());
    }
}
