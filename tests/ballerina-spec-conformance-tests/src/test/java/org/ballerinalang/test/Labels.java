/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.ballerinalang.test;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Labels for spec conformance tests.
 *
 * @since 2.0.0
 */
public class Labels {

    // Float
    public static final String FLOATING_POINT_TYPE_DESCRIPTOR = "floating-point-type-descriptor";
    public static final String FLOATING_POINT_LITERAL = "floating-point-literal";

    // Int
    public static final String INT_TYPE_DESCRIPTOR = "int-type-descriptor";
    public static final String INT_LITERAL = "int-literal";

    // Nil
    public static final String NIL_TYPE_DESCRIPTOR = "nil-type-descriptor";
    public static final String NIL_LITERAL = "nil-literal";
    public static final String STRING_TYPE_DESCRIPTOR = "string-type-descriptor";
    public static final String STRING_LITERAL = "string-literal";

    // Object
    public static final String OBJECT_TYPE_DESCRIPTOR = "object-type-descriptor";

    // Expressions
    public static final String LIST_CONSTRUCTOR_EXPR = "list-constructor-expr";

    // Boolean
    public static final String BOOLEAN_TYPE_DESCRIPTOR = "boolean-type-descriptor";
    public static final String BOOLEAN_LITERAL = "boolean-literal";
    public static final String JSON_TYPE_DESCRIPTOR = "json-type-descriptor";
    public static final String READONLY_TYPE_DESCRIPTOR = "readonly-type-descriptor";
    public static final String ANY_TYPE_DESCRIPTOR = "any-type-descriptor";

    public static final String[] SET_VALUES = new String[] {FLOATING_POINT_TYPE_DESCRIPTOR, INT_TYPE_DESCRIPTOR,
            NIL_TYPE_DESCRIPTOR, STRING_TYPE_DESCRIPTOR, OBJECT_TYPE_DESCRIPTOR, BOOLEAN_LITERAL, INT_LITERAL,
            NIL_LITERAL, STRING_LITERAL, BOOLEAN_TYPE_DESCRIPTOR, FLOATING_POINT_LITERAL, LIST_CONSTRUCTOR_EXPR,
            JSON_TYPE_DESCRIPTOR, READONLY_TYPE_DESCRIPTOR, ANY_TYPE_DESCRIPTOR
    };

    public static final HashSet<String> LABELS = new HashSet<>(Arrays.asList(SET_VALUES));
}
