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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Interner;
import io.ballerina.runtime.internal.types.semtype.BBooleanSubType;
import io.ballerina.runtime.internal.types.semtype.BCellSubType;
import io.ballerina.runtime.internal.types.semtype.BDecimalSubType;
import io.ballerina.runtime.internal.types.semtype.BFloatSubType;
import io.ballerina.runtime.internal.types.semtype.BIntSubType;
import io.ballerina.runtime.internal.types.semtype.BListSubType;
import io.ballerina.runtime.internal.types.semtype.BMappingSubType;
import io.ballerina.runtime.internal.types.semtype.BStringSubType;
import io.ballerina.runtime.internal.types.semtype.CacheFactory;
import io.ballerina.runtime.internal.types.semtype.CellAtomicType;
import io.ballerina.runtime.internal.types.semtype.FixedLengthArray;
import io.ballerina.runtime.internal.types.semtype.ListAtomicType;
import io.ballerina.runtime.internal.types.semtype.ListDefinition;
import io.ballerina.runtime.internal.types.semtype.MappingAtomicType;
import io.ballerina.runtime.internal.types.semtype.MappingDefinition;
import io.ballerina.runtime.internal.types.semtype.TableUtils;
import io.ballerina.runtime.internal.types.semtype.XmlUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.CODE_UNDEF;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.VT_INHERENTLY_IMMUTABLE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.VT_MASK;
import static io.ballerina.runtime.api.types.semtype.BddNode.bddAtom;
import static io.ballerina.runtime.api.types.semtype.Core.union;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.runtime.internal.types.semtype.CellAtomicType.CellMutability.CELL_MUT_NONE;

/**
 * Utility class for creating semtypes.
 *
 * @since 2201.11.0
 */
public final class Builder {

    private static final String[] EMPTY_STRING_ARR = new String[0];
    private static final SemType VAL = SemType.from(VT_MASK);
    private static final SemType OBJECT = from(BT_OBJECT);
    private static final SemType INHERENTLY_IMMUTABLE = SemType.from(VT_INHERENTLY_IMMUTABLE);

    private static final SemType INNER = getBasicTypeUnion(VAL.all() | from(BasicTypeCode.BT_UNDEF).all());
    private static final SemType ANY = getBasicTypeUnion(BasicTypeCode.VT_MASK & ~(1 << BasicTypeCode.BT_ERROR.code()));
    private static final SemType SIMPLE_OR_STRING =
            getBasicTypeUnion((1 << BasicTypeCode.BT_NIL.code())
                    | (1 << BasicTypeCode.BT_BOOLEAN.code())
                    | (1 << BasicTypeCode.BT_INT.code())
                    | (1 << BasicTypeCode.BT_FLOAT.code())
                    | (1 << BasicTypeCode.BT_DECIMAL.code())
                    | (1 << BT_REGEXP.code())
                    | (1 << BasicTypeCode.BT_STRING.code()));

    private static final SemType[] EMPTY_TYPES_ARR = new SemType[0];

    private static final ConcurrentLazySupplier<SemType> MAPPING_RO = new ConcurrentLazySupplier<>(() ->
            basicSubType(BT_MAPPING, BMappingSubType.createDelegate(getBddSubtypeRo()))
    );
    private static final ConcurrentLazySupplier<SemType> ANYDATA = new ConcurrentLazySupplier<>(
            () -> {
                Env env = Env.getInstance();
                ListDefinition listDef = new ListDefinition();
                MappingDefinition mapDef = new MappingDefinition();
                SemType tableTy = TableUtils.tableContaining(env, mapDef.getSemType(env));
                SemType accum = Stream.of(getSimpleOrStringType(), getXmlType(), listDef.getSemType(env),
                        mapDef.getSemType(env),
                        tableTy).reduce(Builder.getNeverType(), Core::union);
                listDef.defineListTypeWrapped(env, EMPTY_TYPES_ARR, 0, accum, CELL_MUT_LIMITED);
                mapDef.defineMappingTypeWrapped(env, new MappingDefinition.Field[0], accum, CELL_MUT_LIMITED);
                return accum;
            }
    );
    private static final ConcurrentLazySupplier<SemType> INNER_RO =
            new ConcurrentLazySupplier<>(() -> union(getReadonlyType(), getInnerType()));

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

    public static SemType getNeverType() {
        return SemType.from(0);
    }

    public static SemType getNilType() {
        return from(BasicTypeCode.BT_NIL);
    }

    public static SemType getUndefType() {
        return from(BasicTypeCode.BT_UNDEF);
    }

    public static SemType getCellType() {
        return from(BT_CELL);
    }

    public static SemType getInnerType() {
        return INNER;
    }

    public static SemType getIntType() {
        return from(BasicTypeCode.BT_INT);
    }

    public static SemType getDecimalType() {
        return from(BasicTypeCode.BT_DECIMAL);
    }

    public static SemType getFloatType() {
        return from(BasicTypeCode.BT_FLOAT);
    }

    public static SemType getBooleanType() {
        return from(BasicTypeCode.BT_BOOLEAN);
    }

    public static SemType getStringType() {
        return from(BasicTypeCode.BT_STRING);
    }

    public static SemType getCharType() {
        return StringTypeCache.charType;
    }

    public static SemType getListType() {
        return from(BT_LIST);
    }

    public static SemType getReadonlyType() {
        return PREDEFINED_TYPE_ENV.readonlyType();
    }

    static SemType getBasicTypeUnion(int bitset) {
        return switch (bitset) {
            case 0 -> getNeverType();
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
        assert checkDelegate(basicTypeCode, subType) : "BDd is wrapped in wrong delegate";
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

    public static SemType getIntConst(long value) {
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

    public static SemType getBooleanConst(boolean value) {
        return value ? BooleanTypeCache.TRUE : BooleanTypeCache.FALSE;
    }

    public static SemType createIntRange(long min, long max) {
        return basicSubType(BasicTypeCode.BT_INT, BIntSubType.createIntSubType(min, max));
    }

    public static SemType getDecimalConst(BigDecimal value) {
        BigDecimal[] values = {value};
        return basicSubType(BasicTypeCode.BT_DECIMAL, BDecimalSubType.createDecimalSubType(true, values));
    }

    public static SemType getFloatConst(double value) {
        Double[] values = {value};
        return basicSubType(BasicTypeCode.BT_FLOAT, BFloatSubType.createFloatSubType(true, values));
    }

    public static SemType getStringConst(String value) {
        return StringTypeCache.get(value);
    }

    private static SemType createStringSingleton(String value) {
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

    public static Interner<String> getStringInterner() {
        return StringTypeCache.interner;
    }

    static SubType[] initializeSubtypeArray(int some) {
        return new SubType[Integer.bitCount(some)];
    }

    public static SemType getRoCellContaining(Env env, SemType ty) {
        return getCellContaining(env, ty, CELL_MUT_NONE);
    }

    public static SemType getRwCellContaining(Env env, SemType ty) {
        return getCellContaining(env, ty, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
    }

    public static SemType getCellContaining(Env env, SemType ty, CellAtomicType.CellMutability mut) {
        return env.getCachedCellType(ty, mut, () -> createCellSemType(env, ty, mut));
    }

    private static SemType createCellSemType(Env env, SemType ty, CellAtomicType.CellMutability mut) {
        CellAtomicType atomicCell = CellAtomicType.from(ty, mut);
        TypeAtom atom = env.cellAtom(atomicCell);
        BddNode bdd = bddAtom(atom);
        return basicSubType(BT_CELL, BCellSubType.createDelegate(bdd));
    }

    public static SemType getValType() {
        return getBasicTypeUnion(VT_MASK);
    }

    public static SemType getAnyType() {
        return ANY;
    }

    public static SemType getMappingType() {
        return from(BT_MAPPING);
    }

    public static SemType getFunctionType() {
        return from(BT_FUNCTION);
    }

    public static SemType getErrorType() {
        return from(BT_ERROR);
    }

    public static SemType getXmlType() {
        return from(BT_XML);
    }

    public static SemType getXmlElementType() {
        return XML_ELEMENT.get();
    }

    public static SemType getXmlCommentType() {
        return XML_COMMENT.get();
    }

    public static SemType getXmlTextType() {
        return XML_TEXT.get();
    }

    public static SemType getXmlNeverType() {
        return XML_NEVER.get();
    }

    public static SemType getXmlPIType() {
        return XML_PI.get();
    }

    public static SemType getHandleType() {
        return from(BT_HANDLE);
    }

    public static SemType getFutureType() {
        return from(BT_FUTURE);
    }

    public static SemType getRegexType() {
        return from(BT_REGEXP);
    }

    public static SemType getTypeDescType() {
        return from(BT_TYPEDESC);
    }

    public static SemType getStreamType() {
        return from(BasicTypeCode.BT_STREAM);
    }

    public static SemType getAnyDataType() {
        return ANYDATA.get();
    }

    public static SemType getObjectType() {
        return OBJECT;
    }

    public static SemType getInherentlyImmutable() {
        return INHERENTLY_IMMUTABLE;
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

    public static BddNode getBddSubtypeRo() {
        return bddAtom(RecAtom.createUnBlockedRecAtom(0));
    }

    public static ListAtomicType getListAtomicInner() {
        return LIST_ATOMIC_INNER.get();
    }

    public static MappingAtomicType getMappingAtomicInner() {
        return MAPPING_ATOMIC_INNER.get();
    }

    public static BddNode getListSubtypeThreeElement() {
        return LIST_SUBTYPE_THREE_ELEMENT;
    }

    public static BddNode getListSubtypeThreeElementRO() {
        return LIST_SUBTYPE_THREE_ELEMENT_RO;
    }

    public static BddNode getListSubtypeTwoElement() {
        return LIST_SUBTYPE_TWO_ELEMENT;
    }

    public static SemType getSimpleOrStringType() {
        return SIMPLE_OR_STRING;
    }

    public static SemType getTableType() {
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

        private static final int MAX_LENGTH = 20;
        private static final Interner<String> interner = Interner.newWeakInterner();
        private static final SemType charType;
        static {
            BStringSubType subTypeData = BStringSubType.createStringSubType(false, Builder.EMPTY_STRING_ARR, true,
                    Builder.EMPTY_STRING_ARR);
            charType = basicSubType(BasicTypeCode.BT_STRING, subTypeData);
        }

        private static final Cache<String, SemType> cache = CacheFactory.createIdentityCache();

        public static SemType get(String value) {
            if (value.length() > MAX_LENGTH) {
                return createStringSingleton(value);
            }
            String canonicalValue = interner.intern(value);
            return cache.get(canonicalValue, Builder::createStringSingleton);
        }
    }

    private static final class BasicTypeCache {

        private static final SemType[] cache;
        static {
            cache = new SemType[CODE_UNDEF + 2];
            for (int i = 0; i < CODE_UNDEF + 1; i++) {
                cache[i] = SemType.from(1 << i);
            }
        }

        private static boolean isCached(BasicTypeCode code) {
            int i = code.code();
            return 0 < i && i <= CODE_UNDEF;
        }

        private static boolean isCached(int code) {
            return 0 < code && code <= CODE_UNDEF;
        }
    }
}
