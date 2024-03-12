/*
 *   Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.semType.BSemType;
import io.ballerina.runtime.internal.types.semType.BSubType;
import io.ballerina.runtime.internal.types.semType.Core;
import io.ballerina.runtime.internal.types.semType.SemTypeUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_NULL;
import static io.ballerina.runtime.api.TypeBuilder.booleanSubType;
import static io.ballerina.runtime.api.TypeBuilder.unwrap;
import static io.ballerina.runtime.api.TypeBuilder.wrap;
import static io.ballerina.runtime.api.utils.TypeUtils.getImpliedType;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.N_TYPES;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BOOLEAN;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BTYPE;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_DECIMAL;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_FLOAT;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_INT;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_NEVER;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_NIL;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_STRING;

// TODO: once we have properly implemented semtypes we can inline this again in to TypeChecker
class SemanticTypeEngine {

    private static final BitSet NumericTypeMask = new BitSet(N_TYPES);

    static {
        NumericTypeMask.set(UT_FLOAT);
        NumericTypeMask.set(UT_DECIMAL);
        NumericTypeMask.set(UT_INT);
    }

    static boolean checkIsType(Object sourceVal, Type targetType) {
        return checkIsType(null, sourceVal, getType(sourceVal), targetType);
    }

    static <T extends BType> T getBTypePart(BSemType semType) {
        return unwrap(SemTypeUtils.TypeOperation.intersection(semType, SemTypeUtils.SemTypeBuilder.ALL_BTYPE));
    }

    static boolean checkIsType(List<String> errors, Object sourceVal, Type sourceType, Type targetType) {
        BSemType sourceSemType = wrap(sourceType);
        BSemType targetSemType = wrap(targetType);
        return switch (isSubType(sourceSemType, targetSemType)) {
            case TRUE -> true;
            case FALSE -> false;
            case UNDEFINED -> SyntacticTypeEngine.checkIsType(errors, sourceVal, getBTypePart(sourceSemType),
                    getBTypePart(targetSemType));
        };
    }

    static boolean checkIsType(Object sourceVal, Type sourceType, Type targetType,
                               List<TypeChecker.TypePair> unresolvedTypes) {
        BSemType sourceSemType = wrap(getImpliedType(sourceType));
        BSemType targetSemType = wrap(getImpliedType(targetType));
        return switch (isSubType(sourceSemType, targetSemType)) {
            case TRUE -> true;
            case FALSE -> false;
            case UNDEFINED -> SyntacticTypeEngine.checkIsType(sourceVal,
                    getBTypePart(sourceSemType), getBTypePart(targetSemType), unresolvedTypes);
        };
    }

    static boolean checkIsType(Type sourceType, Type targetType) {
        return checkIsType(sourceType, targetType, null);
    }

    static boolean checkIsType(Type sourceType, Type targetType, List<TypeChecker.TypePair> unresolvedTypes) {
        BSemType sourceSemType = wrap(sourceType);
        BSemType targetSemType = wrap(targetType);
        return switch (isSubType(sourceSemType, targetSemType)) {
            case TRUE -> true;
            case FALSE -> false;
            case UNDEFINED -> SyntacticTypeEngine.checkIsType(getBTypePart(sourceSemType), getBTypePart(targetSemType),
                    unresolvedTypes);
        };
    }

    static boolean isSameType(Type sourceType, Type targetType) {
        BSemType sourceSemType = wrap(sourceType);
        BSemType targetSemType = wrap(targetType);
        return switch (isSubType(sourceSemType, targetSemType)) {
            // if we got a boolean once we can't get undefined other way round
            case TRUE -> isSubType(targetSemType, sourceSemType) == SubTypeCheckResult.TRUE;
            case FALSE -> false;
            case UNDEFINED -> SyntacticTypeEngine.isSameType(getBTypePart(sourceSemType), getBTypePart(targetSemType));
        };
    }

    enum SubTypeCheckResult {
        TRUE, FALSE, UNDEFINED
    }

    static boolean isSubTypeSimple(BSemType t1, int uniformTypeCode) {
        if (uniformTypeCode == UT_NEVER) {
            return t1.some.isEmpty() && t1.all.isEmpty();
        }
        return t1.some.cardinality() + t1.all.cardinality() == 1 &&
                (t1.some.get(uniformTypeCode) || t1.all.get(uniformTypeCode));
    }

    // t1 < t2
    static SubTypeCheckResult isSubType(BSemType t1, BSemType t2) {
        if (t1 == t2) {
            return SubTypeCheckResult.TRUE;
        }
        if (t1.some.get(UT_BTYPE) && !t2.some.get(UT_BTYPE)) {
            return SubTypeCheckResult.FALSE;
        }
        boolean semTypeResult = Core.isSubType(t1, t2);
        if (!semTypeResult) {
            return SubTypeCheckResult.FALSE;
        }
        if (t1.some.get(UT_BTYPE)) {
            return SubTypeCheckResult.UNDEFINED;
        }
        return SubTypeCheckResult.TRUE;
    }

    static Type getType(Object value) {
        if (value == null) {
            return TYPE_NULL;
        } else if (value instanceof Number number) {
            if (value instanceof Double floatVal) {
                return SemTypeUtils.SemTypeBuilder.floatSubType(floatVal);
            }
            BSemType intSingletonType = (BSemType) SemTypeUtils.SemTypeBuilder.intSubType(number.longValue());
            if (value instanceof Byte || value instanceof Integer) {
                intSingletonType.setByteClass();
            }
            return intSingletonType;
        } else if (value instanceof BString bString) {
            String stringValue = bString.getValue();
            if (stringValue.length() == 1) {
                return SemTypeUtils.SemTypeBuilder.charSubType(stringValue);
            } else {
                return SemTypeUtils.SemTypeBuilder.stringSubType(stringValue);
            }
        } else if (value instanceof BDecimal decimal) {
            BigDecimal valueBigDecimal = decimal.value();
            return SemTypeUtils.SemTypeBuilder.decimalSubType(valueBigDecimal);
        } else if (value instanceof Boolean booleanValue) {
            return booleanSubType(booleanValue);
        } else if (value instanceof BObject) {
            return ((BObject) value).getOriginalType();
        }

        return ((BValue) value).getType();
    }

    static SubTypeCheckResult checkIsLikeTypeInner(Object sourceValue, BSemType targetType) {
        BSemType sourceType = wrap(getType(sourceValue));
        return isSubType(sourceType, targetType);
    }

    static boolean checkIsLikeType(Object sourceValue, Type targetType, boolean allowNumericConversion) {
        return checkIsLikeType(null, sourceValue, targetType, new ArrayList<>(), allowNumericConversion,
                null);
    }

    static boolean checkIsLikeUnionType(List<String> errors, Object sourceValue, Type targetType,
                                        List<TypeValuePair> unresolvedValues, boolean allowNumericConversion,
                                        String varName) {
        BSemType targetSemType = wrap(targetType);
        return switch (checkIsLikeTypeInner(sourceValue, targetSemType)) {
            case TRUE -> true;
            case FALSE -> allowNumericConversion &&
                    numericConvertPossible(sourceValue, wrap(getType(sourceValue)), targetSemType);
            default -> {
                // FIXME: we have do this since BType part may just a single type
                BType bTypePart = getBTypePart(targetSemType);
                if (bTypePart.getTag() == TypeTags.UNION_TAG) {
                    yield SyntacticTypeEngine.checkIsLikeUnionType(
                            errors, sourceValue, (BUnionType) bTypePart, unresolvedValues,
                            allowNumericConversion, varName);
                } else {
                    yield SyntacticTypeEngine.checkIsLikeType(errors, sourceValue, bTypePart, unresolvedValues,
                            allowNumericConversion, varName);
                }
            }
        };
    }

    static boolean numericConvertPossible(Object sourceValue, BSemType sourceType, BSemType targetType) {
        BitSet sourceWidenedType = new BitSet(N_TYPES);
        sourceWidenedType.or(sourceType.some);
        sourceWidenedType.or(sourceType.all);
        sourceWidenedType.and(NumericTypeMask);

        BitSet targetWidenedType = new BitSet(N_TYPES);
        targetWidenedType.or(targetType.some);
        targetWidenedType.or(targetType.all);
        targetWidenedType.and(NumericTypeMask);

        // TODO: throw error based on value for int
        return targetWidenedType.cardinality() > 0 && sourceWidenedType.cardinality() > 0;
    }

    static boolean checkIsLikeType(List<String> errors, Object sourceValue, Type targetType,
                                   List<TypeValuePair> unresolvedValues, boolean allowNumericConversion,
                                   String varName) {
        BSemType targetSemType = wrap(targetType);
        return switch (checkIsLikeTypeInner(sourceValue, targetSemType)) {
            case TRUE -> true;
            case FALSE -> allowNumericConversion &&
                    numericConvertPossible(sourceValue, wrap(getType(sourceValue)), targetSemType);
            default -> {
                // FIXME:
                BType bTypePart = getBTypePart(targetSemType);
                if (bTypePart.getTag() == TypeTags.UNION_TAG) {
                    yield SyntacticTypeEngine.checkIsLikeUnionType(
                            errors, sourceValue, (BUnionType) bTypePart, unresolvedValues,
                            allowNumericConversion, varName);
                } else {
                    // TODO: we need to pass in the target type itself since this try to type check against the value
                    // type. Decided to keep this in semantic type check for the time will revisit this when doing
                    // records
                    yield SyntacticTypeEngine.checkIsLikeType(errors, sourceValue, targetType, unresolvedValues,
                            allowNumericConversion, varName);
                }
            }
        };
    }

    static boolean checkIsLikeType(Object sourceValue, Type targetType, List<TypeValuePair> unresolvedValues,
                                   boolean allowNumericConversion) {
        BSemType targetSemType = wrap(targetType);
        return switch (checkIsLikeTypeInner(sourceValue, targetSemType)) {
            case TRUE -> true;
            case FALSE -> allowNumericConversion &&
                    numericConvertPossible(sourceValue, wrap(getType(sourceValue)), targetSemType);
            default -> switch (targetType.getTag()) {
                case TypeTags.TABLE_TAG -> SyntacticTypeEngine.TableTypeChecker.checkIsLikeTableType(
                        sourceValue, getBTypePart(targetSemType), unresolvedValues, allowNumericConversion);
                case TypeTags.MAP_TAG -> SyntacticTypeEngine.MapTypeChecker.checkIsLikeMapType(
                        sourceValue, getBTypePart(targetSemType), unresolvedValues, allowNumericConversion);
                case TypeTags.ARRAY_TAG -> SyntacticTypeEngine.ListTypeChecker.checkIsLikeArrayType(
                        sourceValue, getBTypePart(targetSemType), unresolvedValues, allowNumericConversion);
                case TypeTags.TUPLE_TAG -> SyntacticTypeEngine.ListTypeChecker.checkIsLikeTupleType(
                        sourceValue, getBTypePart(targetSemType), unresolvedValues, allowNumericConversion);
                case TypeTags.ERROR_TAG -> SyntacticTypeEngine.ErrorTypeChecker.checkIsLikeErrorType(
                        sourceValue, getBTypePart(targetSemType), unresolvedValues, allowNumericConversion);
                default -> throw new UnsupportedOperationException("Type is not supported: " + targetType);
            };
        };
    }

    static boolean checkIsLikeType(Object sourceValue, Type targetType, List<TypeValuePair> unresolvedValues,
                                   boolean allowNumericConversion, String varName, List<String> errors) {
        BSemType targetSemType = wrap(targetType);
        return switch (checkIsLikeTypeInner(sourceValue, targetSemType)) {
            case TRUE -> true;
            case FALSE -> allowNumericConversion &&
                    numericConvertPossible(sourceValue, wrap(getType(sourceValue)), targetSemType);
            default -> switch (targetType.getTag()) {
                case TypeTags.RECORD_TYPE_TAG -> SyntacticTypeEngine.MapTypeChecker.checkIsLikeRecordType(
                        sourceValue, getBTypePart(targetSemType), unresolvedValues, allowNumericConversion, varName,
                        errors);
                default -> throw new UnsupportedOperationException("Type is not supported: " + targetType);
            };
        };
    }

    static boolean checkIsLikeType(Object sourceValue, Type sourceType, Type targetType,
                                   List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        BSemType targetSemType = wrap(targetType);
        if (targetType.getTag() == TypeTags.JSON_TAG) {
            targetSemType.setBTypeClass(BSubType.BTypeClass.BJson);
        }
        BSemType sourceSemType = wrap(getType(sourceValue));
        return switch (isSubType(sourceSemType, targetSemType)) {
            case TRUE -> true;
            case FALSE -> allowNumericConversion &&
                    numericConvertPossible(sourceValue, wrap(getType(sourceValue)), targetSemType);
            default -> switch (targetType.getTag()) {
                case TypeTags.JSON_TAG -> SyntacticTypeEngine.MapTypeChecker.checkIsLikeJSONType(
                        sourceValue, getBTypePart(sourceSemType), getBTypePart(targetSemType), unresolvedValues,
                        allowNumericConversion);
                default -> throw new UnsupportedOperationException("Type is not supported: " + targetType);
            };
        };
    }

    static boolean checkIsLikeType(Object sourceValue, Type targetType) {
        BSemType targetSemType = wrap(targetType);
        return switch (checkIsLikeTypeInner(sourceValue, targetSemType)) {
            case TRUE -> true;
            case FALSE -> false;
            default -> switch (targetType.getTag()) {
                case TypeTags.STREAM_TAG -> SyntacticTypeEngine.StreamTypeChecker.checkIsLikeStreamType(
                        sourceValue, getBTypePart(targetSemType));
                default -> checkIsLikeType(sourceValue, targetType, false);
            };
        };
    }

    static boolean checkFiniteTypeAssignable(Object sourceValue, Type sourceType, Type targetType,
                                             List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        return SyntacticTypeEngine.checkFiniteTypeAssignable(sourceValue, unwrap(sourceType), unwrap(targetType),
                unresolvedValues, allowNumericConversion);
    }

    public static boolean isInherentlyImmutableType(Type type) {
        BSemType semType = wrap(type);
        if (semType.some.get(UT_BTYPE)) {
            return SyntacticTypeEngine.isInherentlyImmutableType(getBTypePart(semType));
        }
        // SemType parts are currently immutable
        return true;
    }

    // FIXME:
    protected static boolean isSimpleBasicType(Type type) {
        if (type instanceof BSemType semType) {
            if (semType.some.cardinality() + semType.all.cardinality() == 1) {
                if (!semType.some.get(UT_BTYPE) && !semType.all.get(UT_NIL)) {
                    return true;
                }
            }
            if (semType.some.isEmpty()) {
                return semType.all.isEmpty();
            } else {
                return semType.all.isEmpty() && SyntacticTypeEngine.isSimpleBasicType(getBTypePart(semType));
            }
            // NOTE: null is not a simple basic type
        }
        return SyntacticTypeEngine.isSimpleBasicType(unwrap(type));
    }

    static boolean isFiniteTypeValue(Object sourceValue, Type sourceType, Object valueSpaceItem,
                                     boolean allowNumericConversion) {
        return SyntacticTypeEngine.isFiniteTypeValue(sourceValue, sourceType, valueSpaceItem, allowNumericConversion);
    }

    protected static boolean isNumericType(Type type) {
        BSemType semType = wrap(type);
        BitSet types = new BitSet(N_TYPES);
        types.or(semType.some);
        types.or(semType.all);
        if (types.cardinality() == 1) {
            types.and(NumericTypeMask);
            return types.isEmpty();
        }
        return false;
    }

    public static boolean isSelectivelyImmutableType(Type type, Set<Type> unresolvedTypes) {
        if (type instanceof BSemType semType) {
            if (semType.some.isEmpty()) {
                return false;
            } else {
                return SyntacticTypeEngine.isSelectivelyImmutableType(getBTypePart(semType), unresolvedTypes);
            }
        }
        // TODO: can't do this since we are calling this while creating the type builder itself
//        if (isSameType(type, PredefinedTypes.TYPE_ANY)) {
//            return true;
//        }
//        if (isSameType(type, PredefinedTypes.TYPE_ANYDATA)) {
//            return true;
//        }
//        if (isSameType(type, PredefinedTypes.TYPE_JSON)) {
//            return true;
//        }
//        if (isSameType(type, PredefinedTypes.TYPE_XML)) {
//            return true;
//        }
        return SyntacticTypeEngine.isSelectivelyImmutableType(unwrap(type), unresolvedTypes);
    }

    static boolean checkIsUnionType(Type sourceType, Type targetType,
                                    List<TypeChecker.TypePair> unresolvedTypes) {
        return SyntacticTypeEngine.checkIsUnionType(unwrap(sourceType), unwrap(targetType), unresolvedTypes);
    }

    static boolean isUnionTypeMatch(Type sourceType, Type targetType,
                                    List<TypeChecker.TypePair> unresolvedTypes) {
        return SyntacticTypeEngine.isUnionTypeMatch(unwrap(sourceType), unwrap(targetType), unresolvedTypes);
    }

    static boolean checkIsJSONType(Type sourceType, List<TypeChecker.TypePair> unresolvedTypes) {
        BSemType sourceSemType = wrap(sourceType);
        BSemType targetSemType = (BSemType) PredefinedTypes.TYPE_JSON;
        return switch (isSubType(sourceSemType, targetSemType)) {
            case TRUE -> true;
            case FALSE -> false;
            case UNDEFINED ->
                    SyntacticTypeEngine.MapTypeChecker.checkIsJSONType(getBTypePart(sourceSemType), unresolvedTypes);
        };
    }

    static boolean isFiniteTypeMatch(Type sourceType, Type targetType) {
        BSemType sourceSemType = wrap(sourceType);
        BSemType targetSemType = wrap(targetType);
        return switch (isSubType(sourceSemType, targetSemType)) {
            case TRUE -> true;
            case FALSE -> false;
            case UNDEFINED ->
                    SyntacticTypeEngine.isFiniteTypeMatch(getBTypePart(sourceSemType), getBTypePart(targetSemType));
        };
    }

    static boolean hasFillerValue(Type type, List<Type> unanalyzedTypes) {
        BSemType semType = wrap(type);
        Optional<Boolean> result = semtypeHasFillerValue(semType);
        if (result.isPresent()) {
            return result.get();
        }
        if (SemTypeUtils.belongToSingleUniformType(semType, UT_BTYPE)) {
            return SyntacticTypeEngine.hasFillerValue(getBTypePart(semType), unanalyzedTypes);
        }
        return false;
    }

    private static Optional<Boolean> semtypeHasFillerValue(BSemType semType) {
        if (semType.all.get(UT_NIL)) {
            return Optional.of(true);
        }
        if (semType.some.isEmpty() && semType.all.cardinality() == 1) {
            return Optional.of(true);
        }
        if (isSubTypeSimple(semType, UT_BOOLEAN)) {
            return Optional.of(true);
        }
        // TODO: factor common code
        if (isSubTypeSimple(semType, UT_STRING)) {
            if (semType.all.get(UT_STRING)) {
                return Optional.of(true);
            }
            return Optional.of(semType.getZeroValue() != null);
        }
        if (isSubTypeSimple(semType, UT_DECIMAL)) {
            if (semType.all.get(UT_DECIMAL)) {
                return Optional.of(true);
            }
            return Optional.of(semType.getZeroValue() != null);
        }
        if (isSubTypeSimple(semType, UT_FLOAT)) {
            if (semType.all.get(UT_FLOAT)) {
                return Optional.of(true);
            }
            return Optional.of(semType.getZeroValue() != null);
        }
        if (isSubTypeSimple(semType, UT_INT)) {
            if (semType.all.get(UT_INT)) {
                return Optional.of(true);
            }
            return Optional.of(semType.getZeroValue() != null);
        }
        return Optional.empty();
    }

    static boolean hasFillerValue(Type type) {
        BSemType semType = wrap(type);
        Optional<Boolean> result = semtypeHasFillerValue(semType);
        if (result.isPresent()) {
            return result.get();
        }
        if (SemTypeUtils.belongToSingleUniformType(semType, UT_BTYPE)) {
            return SyntacticTypeEngine.hasFillerValue(getBTypePart(semType), new ArrayList<>());
        }
        return false;
    }

    static Object handleAnydataValues(Object sourceVal, Type targetType) {
        return SyntacticTypeEngine.handleAnydataValues(sourceVal, targetType);
    }
}
