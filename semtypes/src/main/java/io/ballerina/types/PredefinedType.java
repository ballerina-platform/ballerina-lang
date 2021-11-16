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
package io.ballerina.types;

import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.StringSubtype;

import java.util.StringJoiner;

import static io.ballerina.types.XmlPrimitive.XML_PRIMITIVE_COMMENT_RO;
import static io.ballerina.types.XmlPrimitive.XML_PRIMITIVE_COMMENT_RW;
import static io.ballerina.types.XmlPrimitive.XML_PRIMITIVE_ELEMENT_RO;
import static io.ballerina.types.XmlPrimitive.XML_PRIMITIVE_ELEMENT_RW;
import static io.ballerina.types.XmlPrimitive.XML_PRIMITIVE_PI_RO;
import static io.ballerina.types.XmlPrimitive.XML_PRIMITIVE_PI_RW;
import static io.ballerina.types.XmlPrimitive.XML_PRIMITIVE_TEXT;
import static io.ballerina.types.subtypedata.XmlSubtype.xmlSequence;
import static io.ballerina.types.subtypedata.XmlSubtype.xmlSingleton;

/**
 * Contain predefined types used for constructing other types.
 *
 * @since 3.0.0
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
            uniformTypeUnion((1 << UniformTypeCode.UT_LIST_RO.code) | (1 << UniformTypeCode.UT_LIST_RW.code));
    public static final UniformTypeBitSet MAPPING_RW = uniformType(UniformTypeCode.UT_MAPPING_RW);
    public static final UniformTypeBitSet MAPPING =
            uniformTypeUnion((1 << UniformTypeCode.UT_MAPPING_RO.code) | (1 << UniformTypeCode.UT_MAPPING_RW.code));

    // matches all functions
    public static final UniformTypeBitSet FUNCTION = uniformType(UniformTypeCode.UT_FUNCTION);
    public static final UniformTypeBitSet TYPEDESC = uniformType(UniformTypeCode.UT_TYPEDESC);
    public static final UniformTypeBitSet HANDLE = uniformType(UniformTypeCode.UT_HANDLE);

    public static final UniformTypeBitSet XML =
            uniformTypeUnion((1 << UniformTypeCode.UT_XML_RO.code) | (1 << UniformTypeCode.UT_XML_RW.code));
    public static final UniformTypeBitSet STREAM = uniformType(UniformTypeCode.UT_STREAM);
    public static final UniformTypeBitSet FUTURE = uniformType(UniformTypeCode.UT_FUTURE);

    // this is SubtypeData|error
    public static final UniformTypeBitSet TOP = uniformTypeUnion(UniformTypeCode.UT_MASK);
    public static final UniformTypeBitSet ANY =
            uniformTypeUnion(UniformTypeCode.UT_MASK & ~(1 << UniformTypeCode.UT_ERROR.code));
    public static final UniformTypeBitSet READONLY = uniformTypeUnion(UniformTypeCode.UT_READONLY);
    public static final UniformTypeBitSet SIMPLE_OR_STRING =
            uniformTypeUnion((1 << UniformTypeCode.UT_NIL.code)
                    | (1 << UniformTypeCode.UT_BOOLEAN.code)
                    | (1 << UniformTypeCode.UT_INT.code)
                    | (1 << UniformTypeCode.UT_FLOAT.code)
                    | (1 << UniformTypeCode.UT_DECIMAL.code)
                    | (1 << UniformTypeCode.UT_STRING.code));

    public static final UniformTypeBitSet NUMBER =
            uniformTypeUnion((1 << UniformTypeCode.UT_INT.code)
                    | (1 << UniformTypeCode.UT_FLOAT.code)
                    | (1 << UniformTypeCode.UT_DECIMAL.code));
    public static final SemType BYTE = IntSubtype.intWidthUnsigned(8);
    public static final SemType STRING_CHAR = StringSubtype.stringChar();

    public static final SemType XML_ELEMENT = xmlSingleton(XML_PRIMITIVE_ELEMENT_RO | XML_PRIMITIVE_ELEMENT_RW);
    public static final SemType XML_COMMENT = xmlSingleton(XML_PRIMITIVE_COMMENT_RO | XML_PRIMITIVE_COMMENT_RW);
    public static final SemType XML_TEXT = xmlSequence(xmlSingleton(XML_PRIMITIVE_TEXT));
    public static final SemType XML_PI = xmlSingleton(XML_PRIMITIVE_PI_RO | XML_PRIMITIVE_PI_RW);

    private PredefinedType() {
    }

    // Union of complete uniform types
    // bits is bit vecor indexed by UniformTypeCode
    // I would like to make the arg int:Unsigned32
    // but are language/impl bugs that make this not work well
    static UniformTypeBitSet uniformTypeUnion(int bitset) {
        return UniformTypeBitSet.from(bitset);
    }

    public static UniformTypeBitSet uniformType(UniformTypeCode code) {
        return UniformTypeBitSet.from(1 << code.code);
    }

    public static SemType uniformSubtype(UniformTypeCode code, ProperSubtypeData data) {
        return ComplexSemType.createComplexSemType(0, UniformSubtype.from(code, data));
    }

    static String toString(UniformTypeBitSet ut) {
        StringJoiner sb = new StringJoiner("|", Integer.toBinaryString(ut.bitset) + "[", "]");
        if ((ut.bitset & NEVER.bitset) != 0) {
            sb.add("never");
        }
        if ((ut.bitset & NIL.bitset) != 0) {
            sb.add("nil");
        }
        if ((ut.bitset & BOOLEAN.bitset) != 0) {
            sb.add("boolean");
        }
        if ((ut.bitset & INT.bitset) != 0) {
            sb.add("int");
        }
        if ((ut.bitset & FLOAT.bitset) != 0) {
            sb.add("float");
        }
        if ((ut.bitset & DECIMAL.bitset) != 0) {
            sb.add("decimal");
        }
        if ((ut.bitset & STRING.bitset) != 0) {
            sb.add("string");
        }
        if ((ut.bitset & ERROR.bitset) != 0) {
            sb.add("error");
        }
        if ((ut.bitset & LIST.bitset) != 0) {
            sb.add("list");
        }
        if ((ut.bitset & FUNCTION.bitset) != 0) {
            sb.add("function");
        }
        if ((ut.bitset & TYPEDESC.bitset) != 0) {
            sb.add("typedesc");
        }
        if ((ut.bitset & HANDLE.bitset) != 0) {
            sb.add("handle");
        }
        if ((ut.bitset & UniformTypeCode.UT_READONLY) != 0) {
            sb.add("readonly");
        }
        if ((ut.bitset & MAPPING.bitset) != 0) {
            sb.add("map");
        }
        if ((ut.bitset & XML.bitset) != 0) {
            sb.add("xml");
        }

        return sb.toString();
    }
}
