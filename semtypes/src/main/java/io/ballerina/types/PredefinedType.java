/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types;

import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.StringSubtype;
import io.ballerina.types.typeops.BddCommonOps;

import java.util.StringJoiner;

import static io.ballerina.types.BasicTypeCode.BT_CELL;
import static io.ballerina.types.BasicTypeCode.BT_LIST;
import static io.ballerina.types.ComplexSemType.createComplexSemType;
import static io.ballerina.types.Core.intersect;
import static io.ballerina.types.Core.union;
import static io.ballerina.types.TypeAtom.createTypeAtom;
import static io.ballerina.types.subtypedata.XmlSubtype.XML_PRIMITIVE_COMMENT_RO;
import static io.ballerina.types.subtypedata.XmlSubtype.XML_PRIMITIVE_COMMENT_RW;
import static io.ballerina.types.subtypedata.XmlSubtype.XML_PRIMITIVE_ELEMENT_RO;
import static io.ballerina.types.subtypedata.XmlSubtype.XML_PRIMITIVE_ELEMENT_RW;
import static io.ballerina.types.subtypedata.XmlSubtype.XML_PRIMITIVE_PI_RO;
import static io.ballerina.types.subtypedata.XmlSubtype.XML_PRIMITIVE_PI_RW;
import static io.ballerina.types.subtypedata.XmlSubtype.XML_PRIMITIVE_TEXT;
import static io.ballerina.types.subtypedata.XmlSubtype.xmlSequence;
import static io.ballerina.types.subtypedata.XmlSubtype.xmlSingleton;
import static io.ballerina.types.typeops.BddCommonOps.bddAtom;

/**
 * Contain predefined types used for constructing other types.
 *
 * @since 2201.8.0
 */
public class PredefinedType {
    public static final BasicTypeBitSet NEVER = basicTypeUnion(0);
    public static final CellAtomicType CELL_ATOMIC_NEVER =
            CellAtomicType.from(NEVER, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
    public static final TypeAtom ATOM_CELL_NEVER = createTypeAtom(1, CELL_ATOMIC_NEVER);
    public static final BasicTypeBitSet NIL = basicType(BasicTypeCode.BT_NIL);
    public static final BasicTypeBitSet BOOLEAN = basicType(BasicTypeCode.BT_BOOLEAN);
    public static final BasicTypeBitSet INT = basicType(BasicTypeCode.BT_INT);
    public static final BasicTypeBitSet FLOAT = basicType(BasicTypeCode.BT_FLOAT);
    public static final BasicTypeBitSet DECIMAL = basicType(BasicTypeCode.BT_DECIMAL);
    public static final BasicTypeBitSet STRING = basicType(BasicTypeCode.BT_STRING);
    public static final BasicTypeBitSet ERROR = basicType(BasicTypeCode.BT_ERROR);
    public static final BasicTypeBitSet LIST = basicType(BasicTypeCode.BT_LIST);
    public static final BasicTypeBitSet MAPPING = basicType(BasicTypeCode.BT_MAPPING);
    public static final BasicTypeBitSet TABLE = basicType(BasicTypeCode.BT_TABLE);
    public static final BasicTypeBitSet CELL = basicType(BT_CELL);
    public static final BasicTypeBitSet UNDEF = basicType(BasicTypeCode.BT_UNDEF);

    // matches all functions
    public static final BasicTypeBitSet FUNCTION = basicType(BasicTypeCode.BT_FUNCTION);
    public static final BasicTypeBitSet TYPEDESC = basicType(BasicTypeCode.BT_TYPEDESC);
    public static final BasicTypeBitSet HANDLE = basicType(BasicTypeCode.BT_HANDLE);

    public static final BasicTypeBitSet XML = basicType(BasicTypeCode.BT_XML);
    public static final BasicTypeBitSet OBJECT = basicType(BasicTypeCode.BT_OBJECT);
    public static final BasicTypeBitSet STREAM = basicType(BasicTypeCode.BT_STREAM);
    public static final BasicTypeBitSet FUTURE = basicType(BasicTypeCode.BT_FUTURE);

    // this is SubtypeData|error
    public static final BasicTypeBitSet VAL = basicTypeUnion(BasicTypeCode.VT_MASK);
    public static final CellAtomicType CELL_ATOMIC_VAL =
            CellAtomicType.from(VAL, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
    public static final TypeAtom ATOM_CELL_VAL = createTypeAtom(0, CELL_ATOMIC_VAL);
    public static final BasicTypeBitSet INNER = BasicTypeBitSet.from(VAL.bitset | UNDEF.bitset);
    public static final CellAtomicType CELL_ATOMIC_INNER =
            CellAtomicType.from(INNER, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
    static final TypeAtom ATOM_CELL_INNER = createTypeAtom(2, CELL_ATOMIC_INNER);
    public static final BasicTypeBitSet ANY =
            basicTypeUnion(BasicTypeCode.VT_MASK & ~(1 << BasicTypeCode.BT_ERROR.code));

    public static final CellAtomicType CELL_ATOMIC_INNER_MAPPING = CellAtomicType.from(
            Core.union(PredefinedType.MAPPING, PredefinedType.UNDEF), CellAtomicType.CellMutability.CELL_MUT_LIMITED
    );

    private static final int IMPLEMENTED_INHERENTLY_IMMUTABLE =
            (1 << BasicTypeCode.BT_NIL.code)
                    | (1 << BasicTypeCode.BT_BOOLEAN.code)
                    | (1 << BasicTypeCode.BT_INT.code)
                    | (1 << BasicTypeCode.BT_FLOAT.code)
                    | (1 << BasicTypeCode.BT_DECIMAL.code)
                    | (1 << BasicTypeCode.BT_STRING.code);

    public static final BasicTypeBitSet SIMPLE_OR_STRING =
            basicTypeUnion((1 << BasicTypeCode.BT_NIL.code)
                    | (1 << BasicTypeCode.BT_BOOLEAN.code)
                    | (1 << BasicTypeCode.BT_INT.code)
                    | (1 << BasicTypeCode.BT_FLOAT.code)
                    | (1 << BasicTypeCode.BT_DECIMAL.code)
                    | (1 << BasicTypeCode.BT_STRING.code));

    public static final SemType IMPLEMENTED_TYPES = union(SIMPLE_OR_STRING, LIST);
    public static final SemType IMPLEMENTED_ANY_TYPE = intersect(ANY, IMPLEMENTED_TYPES);

    public static final int BDD_REC_ATOM_READONLY = 0;
    private static final BddNode BDD_SUBTYPE_RO = BddCommonOps.bddAtom(RecAtom.createRecAtom(BDD_REC_ATOM_READONLY));
    public static final SemType VAL_READONLY =
            createComplexSemType(IMPLEMENTED_INHERENTLY_IMMUTABLE, BasicSubtype.from(BT_LIST, BDD_SUBTYPE_RO));

    protected static final SemType INNER_READONLY = union(VAL_READONLY, UNDEF);
    public static final CellSemType CELL_SEMTYPE_INNER =
            (CellSemType) basicSubtype(BT_CELL, bddAtom(ATOM_CELL_INNER));
    public static final CellAtomicType CELL_ATOMIC_INNER_RO =
            CellAtomicType.from(PredefinedType.INNER_READONLY, CellAtomicType.CellMutability.CELL_MUT_NONE);
    public static final TypeAtom ATOM_CELL_INNER_RO = createTypeAtom(4, CELL_ATOMIC_INNER_RO);
    public static final CellSemType CELL_SEMTYPE_INNER_RO =
            (CellSemType) basicSubtype(BT_CELL, bddAtom(ATOM_CELL_INNER_RO));

    public static final TypeAtom ATOM_CELL_INNER_MAPPING = createTypeAtom(3, CELL_ATOMIC_INNER_MAPPING);

    public static final BasicTypeBitSet NUMBER =
            basicTypeUnion((1 << BasicTypeCode.BT_INT.code)
                    | (1 << BasicTypeCode.BT_FLOAT.code)
                    | (1 << BasicTypeCode.BT_DECIMAL.code));
    public static final SemType BYTE = IntSubtype.intWidthUnsigned(8);
    public static final SemType STRING_CHAR = StringSubtype.stringChar();

    public static final SemType XML_ELEMENT = xmlSingleton(XML_PRIMITIVE_ELEMENT_RO | XML_PRIMITIVE_ELEMENT_RW);
    public static final SemType XML_COMMENT = xmlSingleton(XML_PRIMITIVE_COMMENT_RO | XML_PRIMITIVE_COMMENT_RW);
    public static final SemType XML_TEXT = xmlSequence(xmlSingleton(XML_PRIMITIVE_TEXT));
    public static final SemType XML_PI = xmlSingleton(XML_PRIMITIVE_PI_RO | XML_PRIMITIVE_PI_RW);

    public static final MappingAtomicType MAPPING_ATOMIC_INNER = MappingAtomicType.from(
            new String[]{}, new CellSemType[]{}, CELL_SEMTYPE_INNER
    );

    private PredefinedType() {
    }

    // Union of complete basic types
    // bits is bit vector indexed by BasicTypeCode
    // I would like to make the arg int:Unsigned32
    // but are language/impl bugs that make this not work well
    static BasicTypeBitSet basicTypeUnion(int bitset) {
        return BasicTypeBitSet.from(bitset);
    }

    public static BasicTypeBitSet basicType(BasicTypeCode code) {
        return BasicTypeBitSet.from(1 << code.code);
    }

    public static ComplexSemType basicSubtype(BasicTypeCode code, ProperSubtypeData data) {
        // TODO: We need a more efficient way to do this
        if (code.equals(BT_CELL)) {
            return CellSemType.from(new ProperSubtypeData[]{data});
        }
        return ComplexSemType.createComplexSemType(0, BasicSubtype.from(code, data));
    }

    static String toString(BasicTypeBitSet ut) {
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
        if ((ut.bitset & BasicTypeCode.VT_INHERENTLY_IMMUTABLE) != 0) { // TODO: fix when porting readonly
            sb.add("readonly");
        }
        if ((ut.bitset & MAPPING.bitset) != 0) {
            sb.add("map");
        }
        if ((ut.bitset & XML.bitset) != 0) {
            sb.add("xml");
        }
        if ((ut.bitset & CELL.bitset) != 0) {
            sb.add("cell");
        }
        if ((ut.bitset & UNDEF.bitset) != 0) {
            sb.add("undef");
        }
        return sb.toString();
    }
}
