/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.TypeWithShape;
import io.ballerina.runtime.internal.types.semtype.BBooleanSubType;
import io.ballerina.runtime.internal.types.semtype.BCellSubType;
import io.ballerina.runtime.internal.types.semtype.BDecimalSubType;
import io.ballerina.runtime.internal.types.semtype.BFloatSubType;
import io.ballerina.runtime.internal.types.semtype.BIntSubType;
import io.ballerina.runtime.internal.types.semtype.BListSubType;
import io.ballerina.runtime.internal.types.semtype.BMappingSubType;
import io.ballerina.runtime.internal.types.semtype.BStringSubType;
import io.ballerina.runtime.internal.types.semtype.FixedLengthArray;
import io.ballerina.runtime.internal.types.semtype.ListDefinition;
import io.ballerina.runtime.internal.types.semtype.MappingDefinition;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_CELL;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_LIST;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_MAPPING;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_B_TYPE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.VT_INHERENTLY_IMMUTABLE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.VT_MASK;
import static io.ballerina.runtime.api.types.semtype.BddNode.bddAtom;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.runtime.api.types.semtype.Core.union;
import static io.ballerina.runtime.api.types.semtype.TypeAtom.createTypeAtom;

/**
 * Utility class for creating semtypes.
 *
 * @since 2201.10.0
 */
public final class Builder {

    private static final String[] EMPTY_STRING_ARR = new String[0];
    private static final SemType NEVER = SemType.from(0);
    private static final SemType VAL = SemType.from(VT_MASK);
    private static final SemType UNDEF = from(BasicTypeCode.BT_UNDEF);
    private static final SemType INNER = basicTypeUnion(VAL.all | UNDEF.all);
    static final CellAtomicType CELL_ATOMIC_VAL = new CellAtomicType(
            valType(), CellAtomicType.CellMutability.CELL_MUT_LIMITED
    );
    public static final SemType MAPPING = from(BT_MAPPING);
    static final TypeAtom ATOM_CELL_VAL = createTypeAtom(0, CELL_ATOMIC_VAL);
    static final CellAtomicType CELL_ATOMIC_NEVER = new CellAtomicType(
            neverType(), CellAtomicType.CellMutability.CELL_MUT_LIMITED
    );

    public static final int BDD_REC_ATOM_READONLY = 0;
    // represents both readonly & map<readonly> and readonly & readonly[]
    private static final BddNode BDD_SUBTYPE_RO = bddAtom(RecAtom.createRecAtom(BDD_REC_ATOM_READONLY));
    public static final SemType MAPPING_RO = basicSubType(BT_MAPPING, BMappingSubType.createDelegate(BDD_SUBTYPE_RO));
    public static final CellAtomicType CELL_ATOMIC_INNER_MAPPING_RO =
            new CellAtomicType(union(MAPPING_RO, UNDEF), CellAtomicType.CellMutability.CELL_MUT_LIMITED);

    static final CellAtomicType CELL_ATOMIC_INNER = new CellAtomicType(
            inner(), CellAtomicType.CellMutability.CELL_MUT_LIMITED);
    static final SemType CELL_SEMTYPE_INNER = basicSubType(BT_CELL,
            BCellSubType.createDelegate(bddAtom(createTypeAtom(2, CELL_ATOMIC_INNER))));
    public static final ListAtomicType LIST_ATOMIC_INNER = new ListAtomicType(
            FixedLengthArray.empty(), CELL_SEMTYPE_INNER);
    private static final SemType VAL_READONLY = unionOf(SemType.from(VT_INHERENTLY_IMMUTABLE),
            basicSubType(BT_LIST, BListSubType.createDelegate(BDD_SUBTYPE_RO)),
            basicSubType(BT_MAPPING, BMappingSubType.createDelegate(BDD_SUBTYPE_RO))
    );
    public static final SemType INNER_READONLY = union(VAL_READONLY, UNDEF);
    public static final CellAtomicType CELL_ATOMIC_INNER_RO
            = new CellAtomicType(INNER_READONLY, CELL_MUT_NONE);
    public static final CellAtomicType CELL_ATOMIC_INNER_MAPPING
            = new CellAtomicType(union(MAPPING, UNDEF), CellAtomicType.CellMutability.CELL_MUT_LIMITED);
    static final SemType CELL_SEMTYPE_INNER_MAPPING = basicSubType(
            BT_CELL, BCellSubType.createDelegate(bddAtom(createTypeAtom(3, CELL_ATOMIC_INNER_MAPPING))));
    public static final ListAtomicType LIST_ATOMIC_MAPPING =
            new ListAtomicType(FixedLengthArray.empty(), CELL_SEMTYPE_INNER_MAPPING);

    static final SemType CELL_SEMTYPE_INNER_MAPPING_RO = basicSubType(
            BT_CELL,
            BCellSubType.createDelegate(bddAtom(createTypeAtom(5, CELL_ATOMIC_INNER_MAPPING_RO))));
    public static final ListAtomicType LIST_ATOMIC_MAPPING_RO =
            new ListAtomicType(FixedLengthArray.empty(), CELL_SEMTYPE_INNER_MAPPING_RO);

    private static final TypeAtom ATOM_CELL_INNER_RO = createTypeAtom(7, CELL_ATOMIC_INNER_RO);
    static final SemType CELL_SEMTYPE_INNER_RO = basicSubType(
            BT_CELL, BCellSubType.createDelegate(bddAtom(ATOM_CELL_INNER_RO)));
    public static final ListAtomicType LIST_ATOMIC_RO = new ListAtomicType(
            FixedLengthArray.empty(), CELL_SEMTYPE_INNER_RO);
    private static final SemType ANY = basicTypeUnion(BasicTypeCode.VT_MASK & ~(1 << BasicTypeCode.BT_ERROR.code()));
    public static final SemType[] EMPTY_TYPES_ARR = new SemType[0];
    public static final MappingAtomicType MAPPING_ATOMIC_INNER = new MappingAtomicType(
            EMPTY_STRING_ARR, EMPTY_TYPES_ARR, CELL_SEMTYPE_INNER);

    public static final SemType SIMPLE_OR_STRING =
            basicTypeUnion((1 << BasicTypeCode.BT_NIL.code())
                    | (1 << BasicTypeCode.BT_BOOLEAN.code())
                    | (1 << BasicTypeCode.BT_INT.code())
                    | (1 << BasicTypeCode.BT_FLOAT.code())
                    | (1 << BasicTypeCode.BT_DECIMAL.code())
                    | (1 << BasicTypeCode.BT_STRING.code()));
    private static final Env env = Env.getInstance();

    private Builder() {
    }

    public static SemType from(BasicTypeCode typeCode) {
        if (BasicTypeCache.isCached(typeCode)) {
            return BasicTypeCache.cache[typeCode.code()];
        }
        return SemType.from(1 << typeCode.code());
    }

    public static SemType from(Context cx, Type type) {
        if (type instanceof SemType semType) {
            return semType;
        } else if (type instanceof BType bType) {
            return fromBType(cx, bType);
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    private static SemType fromBType(Context cx, BType innerType) {
        int staringSize = cx.addProvisionalType(innerType);
        SemType result = innerType.get(cx);
        cx.emptyProvisionalTypes(staringSize);
        return result;
    }

    public static SemType neverType() {
        return basicTypeUnion(0);
    }

    public static SemType nilType() {
        return from(BasicTypeCode.BT_NIL);
    }

    public static SemType undef() {
        return UNDEF;
    }

    public static SemType cell() {
        return from(BT_CELL);
    }

    protected static SemType cellSemTypeInner() {
        return CELL_SEMTYPE_INNER;
    }

    public static SemType inner() {
        return INNER;
    }

    public static SemType intType() {
        return from(BasicTypeCode.BT_INT);
    }

    public static SemType bType() {
        return from(BasicTypeCode.BT_B_TYPE);
    }

    public static SemType decimalType() {
        return from(BasicTypeCode.BT_DECIMAL);
    }

    public static SemType floatType() {
        return from(BasicTypeCode.BT_FLOAT);
    }

    public static SemType booleanType() {
        return from(BasicTypeCode.BT_BOOLEAN);
    }

    public static SemType stringType() {
        return from(BasicTypeCode.BT_STRING);
    }

    public static SemType charType() {
        return StringTypeCache.charType;
    }

    public static SemType listType() {
        return from(BT_LIST);
    }

    public static SemType readonlyType() {
        return VAL_READONLY;
    }

    public static SemType basicTypeUnion(int bitset) {
        return switch (bitset) {
            case 0 -> NEVER;
            case VT_MASK -> VAL;
            default -> {
                if (Integer.bitCount(bitset) == 1) {
                    int code = Integer.numberOfTrailingZeros(bitset);
                    if (BasicTypeCache.isCached(code)) {
                        yield BasicTypeCache.cache[code];
                    }
                }
                yield SemType.from(bitset);
            }
        };
    }

    public static SemType basicSubType(BasicTypeCode basicTypeCode, SubType subType) {
        assert !(subType instanceof Bdd) : "BDD should always be wrapped with a delegate";
        SubType[] subTypes = initializeSubtypeArray();
        subTypes[basicTypeCode.code()] = subType;
        return SemType.from(0, 1 << basicTypeCode.code(), subTypes);
    }

    public static SemType intConst(long value) {
        if (value >= IntTypeCache.CACHE_MIN_VALUE && value <= IntTypeCache.CACHE_MAX_VALUE) {
            return IntTypeCache.cache[(int) value - IntTypeCache.CACHE_MIN_VALUE];
        }
        return createIntSingletonType(value);
    }

    private static SemType createIntSingletonType(long value) {
        List<Long> values = new ArrayList<>(1);
        values.add(value);
        return basicSubType(BasicTypeCode.BT_INT, BIntSubType.createIntSubType(values));
    }

    public static SemType booleanConst(boolean value) {
        return value ? BooleanTypeCache.TRUE : BooleanTypeCache.FALSE;
    }

    public static SemType intRange(long min, long max) {
        return basicSubType(BasicTypeCode.BT_INT, BIntSubType.createIntSubType(min, max));
    }

    public static SemType decimalConst(BigDecimal value) {
        BigDecimal[] values = {value};
        return basicSubType(BasicTypeCode.BT_DECIMAL, BDecimalSubType.createDecimalSubType(true, values));
    }

    public static SemType floatConst(double value) {
        Double[] values = {value};
        return basicSubType(BasicTypeCode.BT_FLOAT, BFloatSubType.createFloatSubType(true, values));
    }

    public static SemType stringConst(String value) {
        BStringSubType subType;
        String[] values = {value};
        String[] empty = EMPTY_STRING_ARR;
        if (value.length() == 1 || value.codePointCount(0, value.length()) == 1) {
            subType = BStringSubType.createStringSubType(true, values, true, empty);
        } else {
            subType = BStringSubType.createStringSubType(true, empty, true, values);
        }
        return basicSubType(BasicTypeCode.BT_STRING, subType);
    }

    static SubType[] initializeSubtypeArray() {
        return new SubType[CODE_B_TYPE + 2];
    }

    public static Optional<SemType> shapeOf(Context cx, Object object) {
        if (object == null) {
            return Optional.of(nilType());
        } else if (object instanceof DecimalValue decimalValue) {
            return Optional.of(decimalConst(decimalValue.value()));
        } else if (object instanceof Double doubleValue) {
            return Optional.of(floatConst(doubleValue));
        } else if (object instanceof Number intValue) {
            long value =
                    intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
            return Optional.of(intConst(value));
        } else if (object instanceof Boolean booleanValue) {
            return Optional.of(booleanConst(booleanValue));
        } else if (object instanceof BString stringValue) {
            return Optional.of(stringConst(stringValue.getValue()));
        } else if (object instanceof BArray arrayValue) {
            return typeOfArray(cx, arrayValue);
        } else if (object instanceof BMap mapValue) {
            return typeOfMap(cx, mapValue);
        }
        return Optional.empty();
    }

    private static Optional<SemType> typeOfMap(Context cx, BMap mapValue) {
        TypeWithShape typeWithShape = (TypeWithShape) mapValue.getType();
        return typeWithShape.shapeOf(cx, mapValue);
    }

    private static Optional<SemType> typeOfArray(Context cx, BArray arrayValue) {
        int size = arrayValue.size();
        SemType[] memberTypes = new SemType[size];
        for (int i = 0; i < size; i++) {
            Optional<SemType> memberType = shapeOf(cx, arrayValue.get(i));
            if (memberType.isEmpty()) {
                return Optional.empty();
            }
            memberTypes[i] = memberType.get();
        }
        ListDefinition ld = new ListDefinition();
        // TODO: cache this in the array value
        return Optional.of(
                ld.defineListTypeWrapped(env, memberTypes, memberTypes.length, neverType(), CELL_MUT_NONE));
    }

    public static SemType roCellContaining(Env env, SemType ty) {
        return cellContaining(env, ty, CELL_MUT_NONE);
    }

    public static SemType cellContaining(Env env, SemType ty) {
        return cellContaining(env, ty, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
    }

    public static SemType cellContaining(Env env, SemType ty, CellAtomicType.CellMutability mut) {
        Optional<SemType> cachedSemType = env.getCachedCellType(ty, mut);
        return cachedSemType.orElseGet(() -> {
            SemType semType = createCellSemType(env, ty, mut);
            env.cacheCellType(ty, mut, semType);
            return semType;
        });
    }

    private static SemType createCellSemType(Env env, SemType ty, CellAtomicType.CellMutability mut) {
        CellAtomicType atomicCell = new CellAtomicType(ty, mut);
        TypeAtom atom = env.cellAtom(atomicCell);
        BddNode bdd = bddAtom(atom);
        return basicSubType(BT_CELL, BCellSubType.createDelegate(bdd));
    }

    public static SemType valType() {
        return basicTypeUnion(VT_MASK);
    }

    public static SemType anyType() {
        return ANY;
    }

    public static SemType mappingType() {
        return MAPPING;
    }

    public static SemType anyDataType(Context context) {
        SemType memo = context.anydataMemo;
        if (memo != null) {
            return memo;
        }
        Env env = context.env;
        ListDefinition listDef = new ListDefinition();
        MappingDefinition mapDef = new MappingDefinition();
        // TODO: add table, xml
        SemType accum = unionOf(SIMPLE_OR_STRING, listDef.getSemType(env), mapDef.getSemType(env));
        listDef.defineListTypeWrapped(env, EMPTY_TYPES_ARR, 0, accum, CELL_MUT_LIMITED);
        mapDef.defineMappingTypeWrapped(env, new MappingDefinition.Field[0], accum, CELL_MUT_LIMITED);
        context.anydataMemo = accum;
        return accum;
    }

    private static SemType unionOf(SemType type1, SemType type2) {
        return union(type1, type2);
    }

    private static SemType unionOf(SemType... types) {
        SemType accum = types[0];
        for (int i = 1; i < types.length; i++) {
            accum = union(accum, types[i]);
        }
        return accum;
    }

    private static final class IntTypeCache {

        private static final int CACHE_MAX_VALUE = 127;
        private static final int CACHE_MIN_VALUE = -128;
        private static final SemType[] cache;
        static {
            cache = new SemType[CACHE_MAX_VALUE - CACHE_MIN_VALUE + 1];
            for (int i = CACHE_MIN_VALUE; i <= CACHE_MAX_VALUE; i++) {
                cache[i - CACHE_MIN_VALUE] = createIntSingletonType(i);
            }
        }
    }

    private static final class BooleanTypeCache {

        private static final SemType TRUE = createBooleanSingletonType(true);
        private static final SemType FALSE = createBooleanSingletonType(false);

        private static SemType createBooleanSingletonType(boolean value) {
            return basicSubType(BasicTypeCode.BT_BOOLEAN, BBooleanSubType.from(value));
        }
    }

    private static final class StringTypeCache {

        private static final SemType charType;
        static {
            BStringSubType subTypeData = BStringSubType.createStringSubType(false, Builder.EMPTY_STRING_ARR, true,
                    Builder.EMPTY_STRING_ARR);
            charType = basicSubType(BasicTypeCode.BT_STRING, subTypeData);
        }
    }

    private static final class BasicTypeCache {

        private static final SemType[] cache;
        static {
            cache = new SemType[CODE_B_TYPE + 2];
            for (int i = 0; i < CODE_B_TYPE + 1; i++) {
                cache[i] = SemType.from(1 << i);
            }
        }

        private static boolean isCached(BasicTypeCode code) {
            int i = code.code();
            return 0 < i && i <= CODE_B_TYPE;
        }

        private static boolean isCached(int code) {
            return 0 < code && code <= CODE_B_TYPE;
        }
    }
}
