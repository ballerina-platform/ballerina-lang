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

    public static final int NIL = 0;
    public static final int TYPE = 1;
    public static final int VARIABLE_NAME = 1 << 1;
    public static final int VARIABLE = 1 << 2 | VARIABLE_NAME;
    public static final int STRUCT = 1 << 3 | TYPE | VARIABLE_NAME;
    public static final int ERROR = 1 << 4 | TYPE | VARIABLE_NAME;
    public static final int SERVICE = 1 << 5 | TYPE | VARIABLE_NAME;
    public static final int INVOKABLE = 1 << 6;
    public static final int FUNCTION = 1 << 7 | INVOKABLE | VARIABLE;
    public static final int ACTION = 1 << 8 | INVOKABLE;
    public static final int RESOURCE = 1 << 9 | INVOKABLE;
    public static final int WORKER = 1 << 10 | INVOKABLE;
    public static final int ANNOTATION = 1 << 11;
    public static final int ANNOTATION_ATTRIBUTE = 1 << 12;
    public static final int IMPORT = 1 << 13;
    public static final int PACKAGE = 1 << 14 | IMPORT;
    public static final int XMLNS = 1 << 15 | IMPORT;
    public static final int SEMANTIC_ERROR = 1 << 16;
    public static final int ENDPOINT = 1 << 17 | VARIABLE;
    public static final int OBJECT = 1 << 18 | TYPE | VARIABLE_NAME | STRUCT;
    public static final int RECORD = 1 << 19 | TYPE | VARIABLE_NAME | STRUCT;
    public static final int TYPE_DEF = 1 << 20 | TYPE | VARIABLE_NAME;
    public static final int FINITE_TYPE = 1 << 21 | TYPE | VARIABLE_NAME;
    public static final int UNION_TYPE = 1 << 22 | TYPE | VARIABLE_NAME;
    public static final int TUPLE_TYPE = 1 << 23 | TYPE | VARIABLE_NAME;
    public static final int ARRAY_TYPE = 1 << 25 | TYPE | VARIABLE_NAME;
    public static final int SCOPE = 1 << 26;
    public static final int CHANNEL = 1 << 27;
    public static final int CONSTANT = 1 << 28 | VARIABLE_NAME;
}
