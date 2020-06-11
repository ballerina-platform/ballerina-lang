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
import org.ballerinalang.util.BLangCompilerConstants;
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

    public static final Name EMPTY = new Name("");
    public static final Name DOT = new Name(".");
    public static final Name DEFAULT_PACKAGE = DOT;
    public static final Name BALLERINA_ORG = new Name("ballerina");
    public static final Name LANG = new Name("lang");
    public static final Name INTERNAL = new Name("__internal");
    public static final Name ANNOTATIONS = new Name("annotations");
    public static final Name ARRAY = new Name("array");
    public static final Name DECIMAL = new Name("decimal");
    public static final Name ERROR = new Name("error");
    public static final Name FLOAT = new Name("float");
    public static final Name FUTURE = new Name("future");
    public static final Name INT = new Name("int");
    public static final Name BOOLEAN = new Name("boolean");
    public static final Name MAP = new Name("map");
    public static final Name OBJECT = new Name("object");
    public static final Name STREAM = new Name("stream");
    public static final Name QUERY = new Name("query");
    public static final Name TABLE = new Name("table");
    public static final Name TYPEDESC = new Name("typedesc");
    public static final Name STRING = new Name("string");
    public static final Name VALUE = new Name("value");
    public static final Name XML = new Name("xml");
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
    public static final Name USER_DEFINED_INIT_SUFFIX = new Name("__init");
    public static final Name GENERATED_INIT_SUFFIX = new Name("$__init$");
    // TODO remove when current project name is read from manifest
    public static final Name ANON_ORG = new Name("$anon");
    public static final Name NIL_VALUE = new Name("()");
    public static final Name QUESTION_MARK = new Name("?");
    public static final Name ORG_NAME_SEPARATOR = new Name("/");
    public static final Name VERSION_SEPARATOR = new Name(":");
    public static final Name ALIAS_SEPARATOR = VERSION_SEPARATOR;
    public static final Name ANNOTATION_TYPE_PARAM = new Name("typeParam");
    public static final Name ANNOTATION_BUILTIN_SUBTYPE = new Name("builtinSubtype");

    public static final Name BIR_BASIC_BLOCK_PREFIX = new Name("bb");
    public static final Name BIR_LOCAL_VAR_PREFIX = new Name("%");
    public static final Name BIR_GLOBAL_VAR_PREFIX = new Name("#");

    public static final Name DETAIL_MESSAGE = new Name("message");
    public static final Name DETAIL_CAUSE = new Name("cause");

    public static final Name NEVER = new Name("never");

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
    public static final Name TRANSACTION_ORG = new Name("ballerina");
    public static final Name TRANSACTION_PACKAGE_VERSION = new Name("0.5.0");

    public static final Name TRX_INITIATOR_BEGIN_FUNCTION = new Name("beginTransactionInitiator");
    public static final Name TRX_LOCAL_PARTICIPANT_BEGIN_FUNCTION = new Name("beginLocalParticipant");
    public static final Name TRX_REMOTE_PARTICIPANT_BEGIN_FUNCTION = new Name("beginRemoteParticipant");
    public static final Name CREATE_INT_RANGE = new Name("createIntRange");
    public static final Name CONSTRUCT_STREAM = new Name("construct");

    // Module Versions
    public static final Name DEFAULT_VERSION = new Name("0.0.0");
    public static final Name INTERNAL_VERSION = new Name(BLangCompilerConstants.INTERNAL_VERSION);
    public static final Name ANNOTATIONS_VERSION = new Name(BLangCompilerConstants.ANNOTATIONS_VERSION);
    public static final Name ARRAY_VERSION = new Name(BLangCompilerConstants.ARRAY_VERSION);
    public static final Name DECIMAL_VERSION = new Name(BLangCompilerConstants.DECIMAL_VERSION);
    public static final Name ERROR_VERSION = new Name(BLangCompilerConstants.ERROR_VERSION);
    public static final Name FLOAT_VERSION = new Name(BLangCompilerConstants.FLOAT_VERSION);
    public static final Name FUTURE_VERSION = new Name(BLangCompilerConstants.FUTURE_VERSION);
    public static final Name INT_VERSION = new Name(BLangCompilerConstants.INT_VERSION);
    public static final Name MAP_VERSION = new Name(BLangCompilerConstants.MAP_VERSION);
    public static final Name OBJECT_VERSION = new Name(BLangCompilerConstants.OBJECT_VERSION);
    public static final Name STREAM_VERSION = new Name(BLangCompilerConstants.STREAM_VERSION);
    public static final Name STRING_VERSION = new Name(BLangCompilerConstants.STRING_VERSION);
    public static final Name TABLE_VERSION = new Name(BLangCompilerConstants.TABLE_VERSION);
    public static final Name TYPEDESC_VERSION = new Name(BLangCompilerConstants.TYPEDESC_VERSION);
    public static final Name VALUE_VERSION = new Name(BLangCompilerConstants.VALUE_VERSION);
    public static final Name XML_VERSION = new Name(BLangCompilerConstants.XML_VERSION);
    public static final Name BOOLEAN_VERSION = new Name(BLangCompilerConstants.BOOLEAN_VERSION);
    public static final Name QUERY_VERSION = new Name(BLangCompilerConstants.QUERY_VERSION);

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

    public Name fromString(String value) {
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
        StringBuilder builder = new StringBuilder("");
        for (Name name : names) {
            builder.append(name.value);
        }
        return new Name(builder.toString());
    }
}
