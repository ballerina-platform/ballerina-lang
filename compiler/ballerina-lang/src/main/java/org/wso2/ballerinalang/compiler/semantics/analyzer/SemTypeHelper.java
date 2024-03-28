/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.types.BasicTypeBitSet;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.StringSubtype;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.SemNamedType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.types.BasicTypeCode.BT_BOOLEAN;
import static io.ballerina.types.BasicTypeCode.BT_DECIMAL;
import static io.ballerina.types.BasicTypeCode.BT_FLOAT;
import static io.ballerina.types.BasicTypeCode.BT_INT;
import static io.ballerina.types.BasicTypeCode.BT_STRING;
import static io.ballerina.types.Core.getComplexSubtypeData;
import static io.ballerina.types.Core.widenToBasicTypes;

/**
 * Contains helper methods related to sem-types.
 *
 * @since 2201.9.0
 */
public final class SemTypeHelper {

    private SemTypeHelper() {
    }

    public static SemType resolveSingletonType(BLangLiteral literal) {
        return resolveSingletonType(literal.value, literal.getDeterminedType().getKind());
    }

    public static SemType resolveSingletonType(Object value, TypeKind targetTypeKind) {
        switch (targetTypeKind) {
            case FLOAT:
                double doubleVal;
                if (value instanceof Long) {
                    doubleVal = ((Long) value).doubleValue();
                } else if (value instanceof Double) {
                    doubleVal = (double) value;
                } else {
                    // literal value will be a string if it wasn't within the bounds of what is supported by Java Long
                    // or Double when it was parsed in BLangNodeBuilder.
                    try {
                        doubleVal = Double.parseDouble((String) value);
                    } catch (NumberFormatException e) {
                        // We reach here when there is a syntax error. Mock the flow with default float value.
                        return FloatSubtype.floatConst(0);
                    }
                }
                return SemTypes.floatConst(doubleVal);
            case INT:
            case BYTE:
                return SemTypes.intConst(((Number) value).longValue());
            case STRING:
                return SemTypes.stringConst((String) value);
            case BOOLEAN:
                return SemTypes.booleanConst((Boolean) value);
            case DECIMAL:
                return SemTypes.decimalConst((String) value);
            case NIL:
                return PredefinedType.NIL;
            case OTHER:
                // We reach here when there is a semantic error
                return PredefinedType.NEVER;
            default:
                throw new UnsupportedOperationException("Finite type not implemented for: " + targetTypeKind);
        }
    }

    public static SemType semTypeComponent(BType t) {
        if (t == null) { // TODO: may be able to fix after tackling bir recursion issue
            return PredefinedType.NEVER;
        }

        if (t.tag == TypeTags.TYPEREFDESC) {
            return semTypeComponent(((BTypeReferenceType) t).referredType);
        }

        switch (t.tag) {
            case TypeTags.INTERSECTION:
            case TypeTags.UNION:
            case TypeTags.ANYDATA:
            case TypeTags.JSON:
            case TypeTags.ANY:
            case TypeTags.READONLY:
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                return t.semType();
            default:
                if (isFullSemType(t.tag)) {
                    return t.semType();
                }
                return PredefinedType.NEVER;
        }
    }

    /**
     * This method returns the same instance if the given type is not fully sem-type supported.
     * Hence, should be called very carefully.
     */
    @Deprecated
    public static BType bTypeComponent(BType t) {
        if (t == null) {
            BType neverType = BType.createNeverType();
            neverType.isBTypeComponent = true;
            return neverType;
        }

        if (t.tag == TypeTags.TYPEREFDESC) {
            return bTypeComponent(((BTypeReferenceType) t).referredType);
        }

        if (isFullSemType(t.tag)) {
            BType neverType = BType.createNeverType();
            neverType.isBTypeComponent = true;
            return neverType;
        }

        return t;
    }

    public static boolean includesNonSemTypes(BType t) {
        if (t.tag == TypeTags.TYPEREFDESC) {
            return includesNonSemTypes(((BTypeReferenceType) t).referredType);
        }

        if (isFullSemType(t.tag)) {
            return false;
        }

        if (t.tag == TypeTags.ANY || t.tag == TypeTags.ANYDATA || t.tag == TypeTags.JSON ||
                t.tag == TypeTags.READONLY) {
            return true;
        }

        if (t.tag == TypeTags.UNION) { // TODO: Handle intersection?
            BUnionType unionType = (BUnionType) t;
            unionType.populateMemberSemTypesAndNonSemTypes();
            return !unionType.memberNonSemTypes.isEmpty();
        }

        return true;
    }

    public static boolean isFullSemType(TypeKind kind) {
        switch (kind) {
            case NIL:
            case BOOLEAN:
            case INT:
            case BYTE:
            case FLOAT:
            case DECIMAL:
            case STRING:
            case FINITE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isFullSemType(int tag) {
        switch (tag) {
            case TypeTags.NIL:
            case TypeTags.BOOLEAN:
            case TypeTags.INT:
            case TypeTags.BYTE:
            case TypeTags.SIGNED32_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
            case TypeTags.FINITE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the basic type of singleton.
     * <p>
     * This will replace the existing <code>finiteType.getValueSpace().iterator().next().getBType()</code> calls
     *
     * @param t SemType component of BFiniteType
     */
    public static Optional<BType> singleShapeBroadType(SemType t, SymbolTable symTable) {
        if (PredefinedType.NIL.equals(t)) {
            return Optional.of(symTable.nilType);
        } else if (t instanceof BasicTypeBitSet) {
            return Optional.empty();
        } else if (SemTypes.isSubtypeSimple(t, PredefinedType.INT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, BT_INT);
            Optional<Long> value = IntSubtype.intSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.intType);
        } else if (SemTypes.isSubtypeSimple(t, PredefinedType.FLOAT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, BT_FLOAT);
            Optional<Double> value = FloatSubtype.floatSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.floatType);
        } else if (SemTypes.isSubtypeSimple(t, PredefinedType.STRING)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, BT_STRING);
            Optional<String> value = StringSubtype.stringSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.stringType);
        } else if (SemTypes.isSubtypeSimple(t, PredefinedType.BOOLEAN)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, BT_BOOLEAN);
            Optional<Boolean> value = BooleanSubtype.booleanSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.booleanType);
        } else if (SemTypes.isSubtypeSimple(t, PredefinedType.DECIMAL)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, BT_DECIMAL);
            Optional<BigDecimal> value = DecimalSubtype.decimalSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.decimalType);
        }
        return Optional.empty();
    }

    /**
     * Returns the basic types of singleton/union of singleton.
     * <p>
     * This will replace the existing <code>finiteType.getValueSpace().iterator()</code> calls
     *
     * @param t SemType component of BFiniteType
     */
    public static Set<BType> broadTypes(SemType t, SymbolTable symTable) { // Equivalent to getValueTypes()
        Set<BType> types = new LinkedHashSet<>(7);
        BasicTypeBitSet basicTypeBitSet = widenToBasicTypes(t);
        if ((basicTypeBitSet.bitset & PredefinedType.NIL.bitset) != 0) {
            types.add(symTable.nilType);
        }

        if ((basicTypeBitSet.bitset & PredefinedType.BOOLEAN.bitset) != 0) {
            types.add(symTable.booleanType);
        }

        if ((basicTypeBitSet.bitset & PredefinedType.INT.bitset) != 0) {
            types.add(symTable.intType);
        }

        if ((basicTypeBitSet.bitset & PredefinedType.FLOAT.bitset) != 0) {
            types.add(symTable.floatType);
        }

        if ((basicTypeBitSet.bitset & PredefinedType.DECIMAL.bitset) != 0) {
            types.add(symTable.decimalType);
        }

        if ((basicTypeBitSet.bitset & PredefinedType.STRING.bitset) != 0) {
            types.add(symTable.stringType);
        }

        return types;
    }

    public static Set<BType> broadTypes(BFiniteType finiteType, SymbolTable symTable) {
        Set<BType> types = new LinkedHashSet<>(7);
        for (SemNamedType semNamedType: finiteType.valueSpace) {
            SemType t = semNamedType.semType();
            BasicTypeBitSet basicTypeBitSet = widenToBasicTypes(t);
            if ((basicTypeBitSet.bitset & PredefinedType.NIL.bitset) != 0) {
                types.add(symTable.nilType);
            }

            if ((basicTypeBitSet.bitset & PredefinedType.BOOLEAN.bitset) != 0) {
                types.add(symTable.booleanType);
            }

            if ((basicTypeBitSet.bitset & PredefinedType.INT.bitset) != 0) {
                types.add(symTable.intType);
            }

            if ((basicTypeBitSet.bitset & PredefinedType.FLOAT.bitset) != 0) {
                types.add(symTable.floatType);
            }

            if ((basicTypeBitSet.bitset & PredefinedType.DECIMAL.bitset) != 0) {
                types.add(symTable.decimalType);
            }

            if ((basicTypeBitSet.bitset & PredefinedType.STRING.bitset) != 0) {
                types.add(symTable.stringType);
            }
        }
        return types;
    }

    /**
     * Counts number of bits set in bitset.
     * <p>
     * <i>Note: this is similar to <code>lib:bitCount()</code> in nBallerina</i>
     * </p><p>
     * This is the Brian Kernighan algorithm.
     * This won't work if bits is < 0.
     * <p/>
     *
     * @param bitset bitset for bits to be counted
     * @return the count
     */
    public static int bitCount(int bitset) {
        int n = 0;
        int v = bitset;
        while (v != 0) {
            v &= v - 1;
            n += 1;
        }
        return n;
    }
}
