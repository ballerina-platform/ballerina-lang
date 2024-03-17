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
import io.ballerina.runtime.internal.types.semtype.BSemType;
import io.ballerina.runtime.internal.types.semtype.BSubType;
import io.ballerina.runtime.internal.types.semtype.Core;
import io.ballerina.runtime.internal.types.semtype.SemTypeUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_NULL;
import static io.ballerina.runtime.api.TypeBuilder.booleanSubType;
import static io.ballerina.runtime.api.TypeBuilder.unwrap;
import static io.ballerina.runtime.api.TypeBuilder.wrap;
import static io.ballerina.runtime.api.utils.TypeUtils.getImpliedType;
import static io.ballerina.runtime.internal.types.semtype.Core.belongToBasicType;
import static io.ballerina.runtime.internal.types.semtype.Core.containsNil;
import static io.ballerina.runtime.internal.types.semtype.Core.containsSimple;
import static io.ballerina.runtime.internal.types.semtype.Core.intersect;
import static io.ballerina.runtime.internal.types.semtype.Core.isSubTypeSimple;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_BOOLEAN;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_BTYPE;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_DECIMAL;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_FLOAT;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_INT;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_NIL;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_STRING;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.cardinality;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.isSet;

// TODO: once we have properly implemented semtypes we can inline this again in to TypeChecker
class SemanticTypeEngine {

    public static final int BASIC_TYPE_BIT_SET =
            (1 << BT_INT) | (1 << BT_FLOAT) | (1 << BT_DECIMAL) | (1 << BT_STRING) |
                    (1 << BT_BOOLEAN);

    static boolean checkIsType(Object sourceVal, Type targetType) {
        int tag = getTypeTag(sourceVal);
        BSemType targetSemType = wrap(targetType);
        int bits = targetSemType.all | targetSemType.some;
        if ((tag & bits) == 0) {
            return false;
        }
        return checkIsType(null, sourceVal, getType(sourceVal), targetSemType);
    }

    static <T extends BType> T getBTypePart(BSemType semType) {
        return unwrap(intersect(semType, SemTypeUtils.ALL_BTYPE));
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

    // t1 < t2
    static SubTypeCheckResult isSubType(BSemType t1, BSemType t2) {
        if (t1 == t2) {
            return SubTypeCheckResult.TRUE;
        }
        // this assumes each of the BTypes are not empty
        if (containsSimple(t1, BT_BTYPE) && !containsSimple(t2, BT_BTYPE)) {
            return SubTypeCheckResult.FALSE;
        }
        if (!Core.isSubType(t1, t2)) {
            return SubTypeCheckResult.FALSE;
        }
        if (isSet(t1.some, BT_BTYPE)) {
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

    static int getTypeTag(Object value) {
        if (value == null) {
            return 1 << BT_NIL;
        } else if (value instanceof Number) {
            if (value instanceof Double) {
                return 1 << BT_FLOAT;
            }
            return 1 << BT_INT;
        } else if (value instanceof BString) {
            return 1 << BT_STRING;
        } else if (value instanceof BDecimal) {
            return 1 << BT_DECIMAL;
        } else if (value instanceof Boolean) {
            return 1 << BT_BOOLEAN;
        }
        return 1 << BT_BTYPE;
    }

    static SubTypeCheckResult checkIsLikeTypeInner(Object sourceValue, BSemType targetType) {
        int tag = getTypeTag(sourceValue);
        int bits = targetType.all | targetType.some;
        if ((tag & bits) == 0) {
            return SubTypeCheckResult.FALSE;
        }
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
                // NOTE: we have do this since BType part may just a single type
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
        // TODO: check the conversion possible if the source is int?
        return isNumericType(sourceType) && isNumericType(targetType);
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
                // NOTE: bTypePart could be a single type
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
        // TODO: see if can use the sourceType here
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
        if (containsSimple(semType, BT_BTYPE)) {
            return SyntacticTypeEngine.isInherentlyImmutableType(getBTypePart(semType));
        }
        // SemType parts are currently immutable
        return true;
    }

    protected static boolean isSimpleBasicType(Type type) {
        if (type instanceof BSemType semType) {
            if (cardinality(semType.all) + cardinality(semType.some) > 1) {
                return false;
            }
            if (belongToBasicType(semType, BT_BTYPE)) {
                return SyntacticTypeEngine.isSimpleBasicType(getBTypePart(semType));
            }
            return isSubTypeSimple(semType, BASIC_TYPE_BIT_SET);
        }
        return SyntacticTypeEngine.isSimpleBasicType(unwrap(type));
    }

    static boolean isFiniteTypeValue(Object sourceValue, Type sourceType, Object valueSpaceItem,
                                     boolean allowNumericConversion) {
        return SyntacticTypeEngine.isFiniteTypeValue(sourceValue, sourceType, valueSpaceItem, allowNumericConversion);
    }

    private static final int NUMERIC_TYPE_UNION = (1 << BT_INT) | (1 << BT_FLOAT) | (1 << BT_DECIMAL);

    protected static boolean isNumericType(Type type) {
        BSemType semType = wrap(type);
        return isSubTypeSimple(semType, NUMERIC_TYPE_UNION);
    }

    public static boolean isSelectivelyImmutableType(Type type, Set<Type> unresolvedTypes) {
        if (type instanceof BSemType semType) {
            if (containsSimple(semType, BT_BTYPE)) {
                return SyntacticTypeEngine.isSelectivelyImmutableType(getBTypePart(semType), unresolvedTypes);
            }
            return false;
        }
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
        if (belongToBasicType(semType, BT_BTYPE)) {
            return SyntacticTypeEngine.hasFillerValue(getBTypePart(semType), unanalyzedTypes);
        }
        return false;
    }

    private static boolean hasFillerValueInner(BSemType semType, int basicTypeCode) {
        if (belongToBasicType(semType, basicTypeCode)) {
            if (isSet(semType.all, basicTypeCode)) {
                return true;
            }
            return semType.getZeroValue() != null;
        }
        return false;
    }

    private static Optional<Boolean> semtypeHasFillerValue(BSemType semType) {
        if (containsNil(semType)) {
            return Optional.of(true);
        }
        if (belongToBasicType(semType, BT_BOOLEAN)) {
            return Optional.of(true);
        }
        int[] basicTypeCodes = {BT_STRING, BT_DECIMAL, BT_FLOAT, BT_INT};
        for (int basicTypeCode : basicTypeCodes) {
            if (hasFillerValueInner(semType, basicTypeCode)) {
                return Optional.of(true);
            }
        }
        return Optional.empty();
    }

    static boolean hasFillerValue(Type type) {
        BSemType semType = wrap(type);
        Optional<Boolean> result = semtypeHasFillerValue(semType);
        if (result.isPresent()) {
            return result.get();
        }
        if (belongToBasicType(semType, BT_BTYPE)) {
            return SyntacticTypeEngine.hasFillerValue(getBTypePart(semType), new ArrayList<>());
        }
        return false;
    }

    static Object handleAnydataValues(Object sourceVal, Type targetType) {
        return SyntacticTypeEngine.handleAnydataValues(sourceVal, targetType);
    }
}
