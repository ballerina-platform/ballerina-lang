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
import io.ballerina.runtime.api.TypeBuilder;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.semType.BSemType;
import io.ballerina.runtime.internal.types.semType.BSubTypeData;
import io.ballerina.runtime.internal.types.semType.SemTypeUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_BOOLEAN;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_BYTE;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_FLOAT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_NULL;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_STRING;
import static io.ballerina.runtime.api.TypeBuilder.unwrap;
import static io.ballerina.runtime.api.TypeBuilder.wrap;
import static io.ballerina.runtime.api.utils.TypeUtils.getImpliedType;

// TODO: once we have properly implemented semtypes we can inline this again in to TypeChecker
class SemanticTypeEngine {

    static boolean checkIsType(Object sourceVal, Type targetType) {
        return checkIsType(null, sourceVal, getType(sourceVal), targetType);
    }

    static <T extends BType> T getBTypePart(BSemType semType) {
        return unwrap(SemTypeUtils.TypeOperation.intersection(semType, TypeBuilder.PURE_B_TYPE));
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
        BitSet all = (BitSet) t1.all.clone();
        all.andNot(t2.all);
        if (!all.isEmpty()) {
            // t1 has all of some time that t2 don't have all of (it may have some but not all)
            return SubTypeCheckResult.FALSE;
        }
        BitSet some = (BitSet) t1.some.clone();

        BitSet t2Any = (BitSet) t2.all.clone(); // Set of types t2 has all or some of
        t2Any.or(t2.some);

        some.andNot(t2Any);
        if (!some.isEmpty()) {
            // t1 has something that t2 don't have partially or completely
            return SubTypeCheckResult.FALSE;
        }
        // Everything t1 has partially t2 also has something that belong to same basic type partially or completely
        for (int i = 0; i < SemTypeUtils.UniformTypeCodes.UT_BTYPE; i++) {
            if (!t1.some.get(i) || t2.all.get(i)) {
                // t1 don't have it or t2 has all of this type so no point checking
                continue;
            }
            if (!t1.subTypeData[i].isSubType(t2.subTypeData[i])) {
                return SubTypeCheckResult.FALSE;
            }
        }
        return t1.some.get(SemTypeUtils.UniformTypeCodes.UT_BTYPE) ? SubTypeCheckResult.UNDEFINED :
                SubTypeCheckResult.TRUE;
    }

    static Type getType(Object value) {
        if (value == null) {
            return TYPE_NULL;
        } else if (value instanceof Number) {
            if (value instanceof Long) {
                return TYPE_INT;
            } else if (value instanceof Double) {
                return TYPE_FLOAT;
            } else if (value instanceof Integer || value instanceof Byte) {
                return TYPE_BYTE;
            }
        } else if (value instanceof BString) {
            return TYPE_STRING;
        } else if (value instanceof Boolean) {
            return TYPE_BOOLEAN;
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

    static boolean checkIsLikeType(List<String> errors, Object sourceValue, Type targetType,
                                   List<TypeValuePair> unresolvedValues, boolean allowNumericConversion,
                                   String varName) {
        BSemType targetSemType = wrap(targetType);
        return switch (checkIsLikeTypeInner(sourceValue, targetSemType)) {
            case TRUE -> true;
            case FALSE -> false;
            default -> {
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
            case FALSE -> false;
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
            case FALSE -> false;
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
            targetSemType.setBTypeClass(BSubTypeData.BTypeClass.BJson);
        }
        BSemType sourceSemType = wrap(getType(sourceValue));
        return switch (isSubType(sourceSemType, targetSemType)) {
            case TRUE -> true;
            case FALSE -> false;
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
        if (isSimpleBasicType(type)) {
            return true;
        }
        return !(type instanceof BSemType) && SyntacticTypeEngine.isInherentlyImmutableType(unwrap(type));
    }

    protected static boolean isSimpleBasicType(Type type) {
        if (type instanceof BSemType semType) {
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
        if (!semType.some.get(SemTypeUtils.UniformTypeCodes.UT_BTYPE)) {
            return false;
        }
        return SyntacticTypeEngine.isNumericType(getBTypePart(semType));
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

    static boolean hasFillerValue(Type type) {
        BSemType semType = wrap(type);
        if (semType.all.cardinality() > 0) {
            return true;
        }
        return SyntacticTypeEngine.hasFillerValue(type, new ArrayList<>());
    }

    static Object handleAnydataValues(Object sourceVal, Type targetType) {
        return SyntacticTypeEngine.handleAnydataValues(sourceVal, targetType);
    }
}
