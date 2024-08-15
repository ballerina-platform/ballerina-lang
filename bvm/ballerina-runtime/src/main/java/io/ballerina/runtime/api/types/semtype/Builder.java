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
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
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
import io.ballerina.runtime.internal.types.semtype.TableUtils;
import io.ballerina.runtime.internal.types.semtype.XmlUtils;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_CELL;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_ERROR;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_FUNCTION;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_FUTURE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_HANDLE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_LIST;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_MAPPING;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_OBJECT;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_REGEXP;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_TYPEDESC;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_XML;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_B_TYPE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.VT_MASK;
import static io.ballerina.runtime.api.types.semtype.BddNode.bddAtom;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.runtime.api.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.runtime.api.types.semtype.Core.union;

/**
 * Utility class for creating semtypes.
 *
 * @since 2201.10.0
 */
public final class Builder {

    private static final String[] EMPTY_STRING_ARR = new String[0];
    private static final SemType VAL = SemType.from(VT_MASK);
    private static final SemType OBJECT = from(BT_OBJECT);

    private static final SemType INNER = basicTypeUnion(VAL.all() | from(BasicTypeCode.BT_UNDEF).all());
    private static final SemType ANY = basicTypeUnion(BasicTypeCode.VT_MASK & ~(1 << BasicTypeCode.BT_ERROR.code()));
    private static final SemType SIMPLE_OR_STRING =
            basicTypeUnion((1 << BasicTypeCode.BT_NIL.code())
                    | (1 << BasicTypeCode.BT_BOOLEAN.code())
                    | (1 << BasicTypeCode.BT_INT.code())
                    | (1 << BasicTypeCode.BT_FLOAT.code())
                    | (1 << BasicTypeCode.BT_DECIMAL.code())
                    | (1 << BT_REGEXP.code())
                    | (1 << BasicTypeCode.BT_STRING.code()));

    private static final SemType[] EMPTY_TYPES_ARR = new SemType[0];

    private static final ConcurrentLazySupplier<SemType> MAPPING_RO = new ConcurrentLazySupplier<>(() ->
            basicSubType(BT_MAPPING, BMappingSubType.createDelegate(bddSubtypeRo()))
    );
    private static final ConcurrentLazySupplier<SemType> INNER_RO =
            new ConcurrentLazySupplier<>(() -> union(readonlyType(), inner()));

    private static final ConcurrentLazySupplier<ListAtomicType> LIST_ATOMIC_INNER =
            new ConcurrentLazySupplier<>(() -> new ListAtomicType(
                    FixedLengthArray.empty(), PredefinedTypeEnv.getInstance().cellSemTypeInner()));
    private static final ConcurrentLazySupplier<MappingAtomicType> MAPPING_ATOMIC_INNER =
            new ConcurrentLazySupplier<>(() -> new MappingAtomicType(
                    EMPTY_STRING_ARR, EMPTY_TYPES_ARR, PredefinedTypeEnv.getInstance().cellSemTypeInner()));

    private static final ConcurrentLazySupplier<SemType> XML_ELEMENT = new ConcurrentLazySupplier<>(() ->
            XmlUtils.xmlSingleton(XmlUtils.XML_PRIMITIVE_ELEMENT_RO | XmlUtils.XML_PRIMITIVE_ELEMENT_RW));
    private static final ConcurrentLazySupplier<SemType> XML_COMMENT = new ConcurrentLazySupplier<>(() ->
            XmlUtils.xmlSingleton(XmlUtils.XML_PRIMITIVE_COMMENT_RO | XmlUtils.XML_PRIMITIVE_COMMENT_RW));
    private static final ConcurrentLazySupplier<SemType> XML_TEXT = new ConcurrentLazySupplier<>(() ->
            XmlUtils.xmlSingleton(XmlUtils.XML_PRIMITIVE_TEXT));
    private static final ConcurrentLazySupplier<SemType> XML_PI = new ConcurrentLazySupplier<>(() ->
            XmlUtils.xmlSingleton(XmlUtils.XML_PRIMITIVE_PI_RO | XmlUtils.XML_PRIMITIVE_PI_RW));
    private static final ConcurrentLazySupplier<SemType> XML_NEVER = new ConcurrentLazySupplier<>(() ->
            XmlUtils.xmlSingleton(XmlUtils.XML_PRIMITIVE_NEVER));
    private static final PredefinedTypeEnv PREDEFINED_TYPE_ENV = PredefinedTypeEnv.getInstance();
    private static final BddNode LIST_SUBTYPE_THREE_ELEMENT = bddAtom(PREDEFINED_TYPE_ENV.atomListThreeElement());
    private static final BddNode LIST_SUBTYPE_THREE_ELEMENT_RO = bddAtom(PREDEFINED_TYPE_ENV.atomListThreeElementRO());
    private static final BddNode LIST_SUBTYPE_TWO_ELEMENT = bddAtom(PREDEFINED_TYPE_ENV.atomListTwoElement());

    private Builder() {
    }

    public static SemType from(BasicTypeCode typeCode) {
        if (BasicTypeCache.isCached(typeCode)) {
            return BasicTypeCache.cache[typeCode.code()];
        }
        return SemType.from(1 << typeCode.code());
    }

    public static SemType neverType() {
        return SemType.from(0);
    }

    public static SemType nilType() {
        return from(BasicTypeCode.BT_NIL);
    }

    public static SemType undef() {
        return from(BasicTypeCode.BT_UNDEF);
    }

    public static SemType cell() {
        return from(BT_CELL);
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
        return PREDEFINED_TYPE_ENV.readonlyType();
    }

    static SemType basicTypeUnion(int bitset) {
        return switch (bitset) {
            case 0 -> neverType();
            case VT_MASK -> VAL;
            default -> {
                if (Integer.bitCount(bitset) == 1) {
                    int code = Integer.numberOfTrailingZeros(bitset);
                    // TODO: what are the others?
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
        assert checkDelegate(basicTypeCode, subType);
        int some = 1 << basicTypeCode.code();
        SubType[] subTypes = initializeSubtypeArray(some);
        subTypes[0] = subType;
        return SemType.from(0, some, subTypes);
    }

    private static boolean checkDelegate(BasicTypeCode basicTypeCode, SubType subType) {
        return (basicTypeCode != BT_MAPPING || subType instanceof BMappingSubType)
                && (basicTypeCode != BT_LIST || subType instanceof BListSubType)
                && (basicTypeCode != BT_CELL || subType instanceof BCellSubType);
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

    static SubType[] initializeSubtypeArray(int some) {
        return new SubType[Integer.bitCount(some)];
    }

    public static Optional<SemType> readonlyShapeOf(Context cx, Object object) {
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
        } else if (object instanceof BValue bValue) {
            Type type = bValue.getType();
            if (type instanceof TypeWithShape typeWithShape) {
                return typeWithShape.readonlyShapeOf(cx, Builder::readonlyShapeOf, object);
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    // TODO: factor this to a separate class
    public static Optional<SemType> shapeOf(Context cx, Object object) {
        if (object instanceof BValue bValue) {
            return bValue.shapeOf(cx);
        }
        if (object == null) {
            return Optional.of(nilType());
        } else if (object instanceof Double doubleValue) {
            return Optional.of(floatConst(doubleValue));
        } else if (object instanceof Number intValue) {
            long value =
                    intValue instanceof Byte byteValue ? Byte.toUnsignedLong(byteValue) : intValue.longValue();
            return Optional.of(intConst(value));
        } else if (object instanceof Boolean booleanValue) {
            return Optional.of(booleanConst(booleanValue));
        }
        return Optional.empty();
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
        CellAtomicType atomicCell = CellAtomicType.from(ty, mut);
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
        return from(BT_MAPPING);
    }

    public static SemType functionType() {
        return from(BT_FUNCTION);
    }

    public static SemType errorType() {
        return from(BT_ERROR);
    }

    public static SemType xmlType() {
        return from(BT_XML);
    }

    public static SemType xmlElementType() {
        return XML_ELEMENT.get();
    }

    public static SemType xmlCommentType() {
        return XML_COMMENT.get();
    }

    public static SemType xmlTextType() {
        return XML_TEXT.get();
    }

    public static SemType xmlNeverType() {
        return XML_NEVER.get();
    }

    public static SemType xmlPIType() {
        return XML_PI.get();
    }

    public static SemType handleType() {
        return from(BT_HANDLE);
    }

    public static SemType futureType() {
        return from(BT_FUTURE);
    }

    public static SemType regexType() {
        return from(BT_REGEXP);
    }

    public static SemType typeDescType() {
        return from(BT_TYPEDESC);
    }

    public static SemType streamType() {
        return from(BasicTypeCode.BT_STREAM);
    }

    public static SemType anyDataType(Context context) {
        SemType memo = context.anydataMemo;
        if (memo != null) {
            return memo;
        }
        Env env = context.env;
        ListDefinition listDef = new ListDefinition();
        MappingDefinition mapDef = new MappingDefinition();
        SemType tableTy = TableUtils.tableContaining(env, mapDef.getSemType(env));
        SemType accum =
                unionOf(simpleOrStringType(), xmlType(), listDef.getSemType(env), mapDef.getSemType(env), tableTy);
        listDef.defineListTypeWrapped(env, EMPTY_TYPES_ARR, 0, accum, CELL_MUT_LIMITED);
        mapDef.defineMappingTypeWrapped(env, new MappingDefinition.Field[0], accum, CELL_MUT_LIMITED);
        context.anydataMemo = accum;
        return accum;
    }

    private static SemType unionOf(SemType... types) {
        SemType accum = types[0];
        for (int i = 1; i < types.length; i++) {
            accum = union(accum, types[i]);
        }
        return accum;
    }

    public static SemType objectType() {
        return OBJECT;
    }

    static SemType mappingRO() {
        return MAPPING_RO.get();
    }

    static SemType innerReadOnly() {
        return INNER_RO.get();
    }

    static CellAtomicType cellAtomicVal() {
        return PREDEFINED_TYPE_ENV.cellAtomicVal();
    }

    public static BddNode bddSubtypeRo() {
        return bddAtom(RecAtom.createRecAtom(0));
    }

    public static ListAtomicType listAtomicInner() {
        return LIST_ATOMIC_INNER.get();
    }

    public static MappingAtomicType mappingAtomicInner() {
        return MAPPING_ATOMIC_INNER.get();
    }

    public static BddNode listSubtypeThreeElement() {
        return LIST_SUBTYPE_THREE_ELEMENT;
    }

    public static BddNode listSubtypeThreeElementRO() {
        return LIST_SUBTYPE_THREE_ELEMENT_RO;
    }

    public static BddNode listSubtypeTwoElement() {
        return LIST_SUBTYPE_TWO_ELEMENT;
    }

    public static SemType simpleOrStringType() {
        return SIMPLE_OR_STRING;
    }

    public static SemType tableType() {
        return from(BasicTypeCode.BT_TABLE);
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
