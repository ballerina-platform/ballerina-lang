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

    public static final Name EMPTY = new Name("");
    public static final Name DOT = new Name(".");
    public static final Name DEFAULT_PACKAGE = DOT;
    public static final Name BUILTIN_PACKAGE = new Name("builtin");
    public static final Name BUILTIN_ORG = new Name("ballerina");
    public static final Name RUNTIME_PACKAGE = new Name("runtime");
    public static final Name TRANSACTION_ORG = new Name("ballerina");
    public static final Name TRANSACTION_PACKAGE = new Name("transactions");
    public static final Name IGNORE = new Name("_");
    public static final Name INVALID = new Name("><");
    public static final Name GEN_VAR_PREFIX = new Name("_$$_");
    public static final Name DEFAULT_VERSION = new Name("0.0.0");
    public static final Name CAST_OP = new Name("(<type>)");
    public static final Name CONVERSION_OP = new Name("<<type>>");
    public static final Name TRANSFORMER = new Name("transformer");
    public static final Name ERROR = new Name("error");
    public static final Name INIT_FUNCTION_SUFFIX = new Name(".<init>");
    public static final Name START_FUNCTION_SUFFIX = new Name(".<start>");
    public static final Name STOP_FUNCTION_SUFFIX = new Name(".<stop>");
    public static final Name INIT_ACTION_SUFFIX = new Name("<init>");
    public static final Name CONNECTOR = new Name("connector");
    public static final Name SELF = new Name("self");
    public static final Name OBJECT_INIT_SUFFIX = new Name("new");
    // TODO remove when current project name is read from manifest
    public static final Name ANON_ORG = new Name("$anon");
    public static final Name NIL_VALUE = new Name("()");
    public static final Name QUESTION_MARK = new Name("?");

    // Names related to Endpoint SPI.
    public static final Name EP_SERVICE_GET_ENDPOINT = new Name("getEndpoint");
    public static final Name EP_SPI_INIT = new Name("init");
    public static final Name EP_SPI_REGISTER = new Name("register");
    public static final Name EP_SPI_GET_CALLER_ACTIONS = new Name("getCallerActions");
    public static final Name EP_SPI_START = new Name("start");
    public static final Name EP_SPI_STOP = new Name("stop");
    public static final Name ANNOTATION_FINAL = new Name("final");
    public static final Name ANNOTATION_READONLY = new Name("readonly");

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
