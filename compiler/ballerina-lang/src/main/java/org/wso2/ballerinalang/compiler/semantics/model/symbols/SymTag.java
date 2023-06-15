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
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

/**
 * @since 0.94
 */
public class SymTag {
    public static final long NIL = 0;
    public static final long IMPORT = 1;
    public static final long ANNOTATION = 1 << 1;
    public static final long MAIN = 1 << 2;
    public static final long TYPE = 1 << 3 | MAIN;
    public static final long VARIABLE_NAME = 1 << 4 | MAIN;
    public static final long VARIABLE = 1 << 5 | VARIABLE_NAME;
    public static final long STRUCT = 1 << 6 | TYPE | VARIABLE_NAME;
    public static final long SERVICE = 1 << 7 | MAIN;
    public static final long INVOKABLE = 1 << 8;
    public static final long FUNCTION = 1 << 9 | INVOKABLE | VARIABLE;
    public static final long WORKER = 1 << 10 | INVOKABLE | MAIN;
    public static final long LISTENER = 1 << 11 | MAIN;
    public static final long PACKAGE = 1 << 12 | IMPORT;
    public static final long XMLNS = 1 << 13 | IMPORT;
    public static final long ENDPOINT = 1 << 14 | VARIABLE;
    public static final long TYPE_DEF = 1 << 15 | TYPE | VARIABLE_NAME;
    public static final long OBJECT = 1 << 16 | TYPE_DEF | STRUCT;
    public static final long RECORD = 1 << 17 | TYPE_DEF | STRUCT;
    public static final long ERROR = 1 << 18 | TYPE_DEF;
    public static final long FINITE_TYPE = 1 << 19 | TYPE_DEF;
    public static final long UNION_TYPE = 1 << 20 | TYPE_DEF;
    public static final long INTERSECTION_TYPE = 1 << 21 | TYPE_DEF;
    public static final long TUPLE_TYPE = 1 << 22 | TYPE_DEF;
    public static final long ARRAY_TYPE = 1 << 23 | TYPE_DEF;
    public static final long CONSTANT = 1 << 24 | VARIABLE_NAME | TYPE;
    public static final long FUNCTION_TYPE = 1 << 25 | TYPE_DEF;
    public static final long CONSTRUCTOR = 1 << 26 | INVOKABLE;
    public static final long LET = 1 << 27;
    public static final long ENUM = 1 << 28 | TYPE_DEF;
    public static final long TYPE_REF = 1 << 29;
    public static final long ANNOTATION_ATTACHMENT = 1 << 30;
    public static final long RESOURCE_PATH_SEGMENT = 1 << 31;
    public static final long SEQUENCE = 1L << 32 | MAIN;
}
