/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.semtype;

import io.ballerina.semtype.subtypedata.IntSubtype;

/**
 * Contain predefined types used for constructing other types.
 *
 * @since 2.0.0
 */
public class PredefinedType {
    public static final UniformTypeBitSet NEVER = uniformTypeUnion(0);
    public static final UniformTypeBitSet NIL = uniformType(UniformTypeCode.UT_NIL);
    public static final UniformTypeBitSet BOOLEAN = uniformType(UniformTypeCode.UT_BOOLEAN);
    public static final UniformTypeBitSet INT = uniformType(UniformTypeCode.UT_INT);
    public static final UniformTypeBitSet FLOAT = uniformType(UniformTypeCode.UT_FLOAT);
    public static final UniformTypeBitSet DECIMAL = uniformType(UniformTypeCode.UT_DECIMAL);
    public static final UniformTypeBitSet STRING = uniformType(UniformTypeCode.UT_STRING);
    public static final UniformTypeBitSet ERROR = uniformType(UniformTypeCode.UT_ERROR);
    public static final UniformTypeBitSet LIST_RW = uniformType(UniformTypeCode.UT_LIST_RW);
    public static final UniformTypeBitSet LIST =
            uniformTypeUnion((1 << UniformTypeCode.UT_LIST_RO) | (1 << UniformTypeCode.UT_LIST_RW));
    public static final UniformTypeBitSet MAPPING_RW = uniformType(UniformTypeCode.UT_MAPPING_RW);
    public static final UniformTypeBitSet MAPPING =
            uniformTypeUnion((1 << UniformTypeCode.UT_MAPPING_RO) | (1 << UniformTypeCode.UT_MAPPING_RW));

    // matches all functions
    public static final UniformTypeBitSet FUNCTION = uniformType(UniformTypeCode.UT_FUNCTION);
    public static final UniformTypeBitSet TYPEDESC = uniformType(UniformTypeCode.UT_TYPEDESC);
    public static final UniformTypeBitSet HANDLE = uniformType(UniformTypeCode.UT_HANDLE);

    public static final UniformTypeBitSet XML =
            uniformTypeUnion((1 << UniformTypeCode.UT_XML_RO) | (1 << UniformTypeCode.UT_XML_RW));
    public static final UniformTypeBitSet STREAM = uniformType(UniformTypeCode.UT_STREAM);
    public static final UniformTypeBitSet FUTURE = uniformType(UniformTypeCode.UT_FUTURE);

    // this is SubtypeData|error
    public static final UniformTypeBitSet TOP = uniformTypeUnion(UniformTypeCode.UT_MASK);
    public static final UniformTypeBitSet ANY =
            uniformTypeUnion(UniformTypeCode.UT_MASK & ~(1 << UniformTypeCode.UT_ERROR));
    public static final UniformTypeBitSet READONLY = uniformTypeUnion(UniformTypeCode.UT_READONLY);
    public static final UniformTypeBitSet SIMPLE_OR_STRING =
            uniformTypeUnion((1 << UniformTypeCode.UT_NIL)
                    | (1 << UniformTypeCode.UT_BOOLEAN)
                    | (1 << UniformTypeCode.UT_INT)
                    | (1 << UniformTypeCode.UT_FLOAT)
                    | (1 << UniformTypeCode.UT_DECIMAL)
                    | (1 << UniformTypeCode.UT_STRING));
    public static final SemType BYTE = IntSubtype.intWidthUnsigned(8);

    private static UniformTypeBitSet uniformTypeUnion(int bitset) {
        return new UniformTypeBitSet(bitset);
    }

    private static UniformTypeBitSet uniformType(int code) {
        return new UniformTypeBitSet(1 << code);
    }

    public static SemType uniformSubtype(int code, ProperSubtypeData data) {
        return ComplexSemType.createComplexSemType(0, new UniformSubtype(code, data));
    }
}
