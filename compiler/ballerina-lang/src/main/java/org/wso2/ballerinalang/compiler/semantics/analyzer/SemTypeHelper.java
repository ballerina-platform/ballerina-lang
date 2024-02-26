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

import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.SubtypeData;
import io.ballerina.types.UniformTypeBitSet;
import io.ballerina.types.subtypedata.BooleanSubtype;
import io.ballerina.types.subtypedata.DecimalSubtype;
import io.ballerina.types.subtypedata.FloatSubtype;
import io.ballerina.types.subtypedata.IntSubtype;
import io.ballerina.types.subtypedata.StringSubtype;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BReadonlyType;
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

import static io.ballerina.types.Core.getComplexSubtypeData;
import static io.ballerina.types.Core.widenToBasicTypes;
import static io.ballerina.types.UniformTypeCode.UT_BOOLEAN;
import static io.ballerina.types.UniformTypeCode.UT_DECIMAL;
import static io.ballerina.types.UniformTypeCode.UT_FLOAT;
import static io.ballerina.types.UniformTypeCode.UT_INT;
import static io.ballerina.types.UniformTypeCode.UT_STRING;

/**
 * Contains helper methods related to sem-types.
 *
 * @since 2201.9.0
 */
public class SemTypeHelper {

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

    public static void resolveBUnionSemTypeComponent(BUnionType type) {
        LinkedHashSet<BType> memberTypes = type.getMemberTypes();
        LinkedHashSet<BType> nonSemMemberTypes = new LinkedHashSet<>();

        SemType semType = PredefinedType.NEVER;
        for (BType memberType : memberTypes) {
            semType = SemTypes.union(semType, semTypeComponent(memberType));

            if (memberType.tag == TypeTags.UNION) {
                nonSemMemberTypes.addAll(((BUnionType) memberType).nonSemMemberTypes);
            } else if (bTypeComponent(memberType).tag != TypeTags.NEVER) {
                nonSemMemberTypes.add(memberType);
            }
        }

        type.nonSemMemberTypes = nonSemMemberTypes;
        type.setSemTypeComponent(semType);
    }

    public static void resolveBIntersectionSemTypeComponent(BIntersectionType type) {
        SemType semType = PredefinedType.TOP;
        for (BType constituentType : type.getConstituentTypes()) {
            semType = SemTypes.intersection(semType, semTypeComponent(constituentType));
        }
        type.setSemTypeComponent(semType);
    }

    public static SemType semTypeComponent(BType t) { // TODO: refactor
        if (t == null) {
            return PredefinedType.NEVER;
        }

        if (t.tag == TypeTags.TYPEREFDESC) {
            return semTypeComponent(((BTypeReferenceType) t).referredType);
        }

        if (t.tag == TypeTags.UNION || t.tag == TypeTags.ANYDATA || t.tag == TypeTags.JSON) {
            return ((BUnionType) t).getSemTypeComponent();
        }

        if (t.tag == TypeTags.INTERSECTION) {
            return ((BIntersectionType) t).getSemTypeComponent();
        }

        if (t.tag == TypeTags.ANY) {
            return ((BAnyType) t).getSemTypeComponent();
        }

        if (t.tag == TypeTags.READONLY) {
            return ((BReadonlyType) t).getSemTypeComponent();
        }

        if (semTypeSupported(t.tag)) {
            return t.semType();
        }

        return PredefinedType.NEVER;
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

        if (semTypeSupported(t.tag)) {
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

        if (semTypeSupported(t.tag)) {
            return false;
        }

        if (t.tag == TypeTags.ANY || t.tag == TypeTags.ANYDATA || t.tag == TypeTags.JSON ||
                t.tag == TypeTags.READONLY) {
            return true;
        }

        if (t.tag == TypeTags.UNION) { // TODO: Handle intersection
            return !((BUnionType) t).nonSemMemberTypes.isEmpty();
        }

        return true;
    }

    protected static boolean semTypeSupported(TypeKind kind) {
        return switch (kind) {
            case NIL, BOOLEAN, INT, BYTE, FLOAT, DECIMAL, STRING, FINITE -> true;
            default -> false;
        };
    }

    protected static boolean semTypeSupported(int tag) {
        return switch (tag) {
            case TypeTags.NIL, TypeTags.BOOLEAN, TypeTags.INT, TypeTags.BYTE,
                    TypeTags.SIGNED32_INT, TypeTags.SIGNED16_INT, TypeTags.SIGNED8_INT,
                    TypeTags.UNSIGNED32_INT, TypeTags.UNSIGNED16_INT, TypeTags.UNSIGNED8_INT ,
                    TypeTags.FLOAT, TypeTags.DECIMAL,
                    TypeTags.STRING, TypeTags.CHAR_STRING,
                    TypeTags.FINITE-> true;
            default -> false;
        };
    }

    public static final SemType READONLY_SEM_COMPONENT = SemTypes.union(PredefinedType.NIL,
                                            SemTypes.union(PredefinedType.BOOLEAN,
                                            SemTypes.union(PredefinedType.INT,
                                            SemTypes.union(PredefinedType.FLOAT,
                                            SemTypes.union(PredefinedType.DECIMAL, PredefinedType.STRING)))));

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
        } else if (t instanceof UniformTypeBitSet) {
            return Optional.empty();
        } else if (Core.isSubtypeSimple(t, PredefinedType.INT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_INT);
            Optional<Long> value = IntSubtype.intSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.intType);
        } else if (Core.isSubtypeSimple(t, PredefinedType.FLOAT)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_FLOAT);
            Optional<Double> value = FloatSubtype.floatSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.floatType);
        } else if (Core.isSubtypeSimple(t, PredefinedType.STRING)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_STRING);
            Optional<String> value = StringSubtype.stringSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.stringType);
        } else if (Core.isSubtypeSimple(t, PredefinedType.BOOLEAN)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_BOOLEAN);
            Optional<Boolean> value = BooleanSubtype.booleanSubtypeSingleValue(sd);
            return value.isEmpty() ? Optional.empty() : Optional.of(symTable.booleanType);
        } else if (Core.isSubtypeSimple(t, PredefinedType.DECIMAL)) {
            SubtypeData sd = getComplexSubtypeData((ComplexSemType) t, UT_DECIMAL);
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
    public static Set<BType> singletonBroadTypes(SemType t, SymbolTable symTable) { // Equivalent to getValueTypes()
        Set<BType> types = new LinkedHashSet<>(7);
        UniformTypeBitSet uniformTypeBitSet = widenToBasicTypes(t);
        if ((uniformTypeBitSet.bitset & PredefinedType.NIL.bitset) != 0) {
            types.add(symTable.nilType);
        }

        if ((uniformTypeBitSet.bitset & PredefinedType.BOOLEAN.bitset) != 0) {
            types.add(symTable.booleanType);
        }

        if ((uniformTypeBitSet.bitset & PredefinedType.INT.bitset) != 0) {
            types.add(symTable.intType);
        }

        if ((uniformTypeBitSet.bitset & PredefinedType.FLOAT.bitset) != 0) {
            types.add(symTable.floatType);
        }

        if ((uniformTypeBitSet.bitset & PredefinedType.DECIMAL.bitset) != 0) {
            types.add(symTable.decimalType);
        }

        if ((uniformTypeBitSet.bitset & PredefinedType.STRING.bitset) != 0) {
            types.add(symTable.stringType);
        }

        return types;
    }

    public static Set<BType> singletonBroadTypes(BFiniteType finiteType, SymbolTable symTable) {
        Set<BType> types = new LinkedHashSet<>(7);
        for (SemNamedType semNamedType: finiteType.valueSpace) {
            SemType t = semNamedType.semType();
            UniformTypeBitSet uniformTypeBitSet = widenToBasicTypes(t);
            if ((uniformTypeBitSet.bitset & PredefinedType.NIL.bitset) != 0) {
                types.add(symTable.nilType);
            }

            if ((uniformTypeBitSet.bitset & PredefinedType.BOOLEAN.bitset) != 0) {
                types.add(symTable.booleanType);
            }

            if ((uniformTypeBitSet.bitset & PredefinedType.INT.bitset) != 0) {
                types.add(symTable.intType);
            }

            if ((uniformTypeBitSet.bitset & PredefinedType.FLOAT.bitset) != 0) {
                types.add(symTable.floatType);
            }

            if ((uniformTypeBitSet.bitset & PredefinedType.DECIMAL.bitset) != 0) {
                types.add(symTable.decimalType);
            }

            if ((uniformTypeBitSet.bitset & PredefinedType.STRING.bitset) != 0) {
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
