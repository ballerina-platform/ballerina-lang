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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.commons.TypeValuePair;
import io.ballerina.runtime.internal.types.semType.BSemType;
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

// TODO: once we have properly implemented semtypes we can inline this again in to TypeChecker
class SemanticTypeEngine {

    static boolean checkIsType(Object sourceVal, Type targetType) {
        return checkIsType(null, sourceVal, getType(sourceVal), targetType);
    }

    static boolean checkIsType(List<String> errors, Object sourceVal, Type sourceType, Type targetType) {
        if (sourceType instanceof BSemType sourceSemType && targetType instanceof BSemType targetSemType) {
            return switch (isSubType(sourceSemType, targetSemType)) {
                case TRUE -> true;
                case FALSE -> false;
                case UNDEFINED -> SyntacticTypeEngine.checkIsType(errors, sourceVal,
                        sourceSemType.getBTypeComponent(),
                        targetSemType.getBTypeComponent());
            };
        }
//        throw new RuntimeException("unwrapped type");
        return SyntacticTypeEngine.checkIsType(errors, sourceVal, unwrap(sourceType), unwrap(targetType));
    }

    static boolean checkIsType(Object sourceVal, Type sourceType, Type targetType,
                               List<TypeChecker.TypePair> unresolvedTypes) {
        if (sourceType instanceof BSemType sourceSemType && targetType instanceof BSemType targetSemType) {
            return switch (isSubType(sourceSemType, targetSemType)) {
                case TRUE -> true;
                case FALSE -> false;
                case UNDEFINED -> SyntacticTypeEngine.checkIsType(sourceVal,
                        sourceSemType.getBTypeComponent(),
                        targetSemType.getBTypeComponent(), unresolvedTypes);
            };
        }
        return SyntacticTypeEngine.checkIsType(sourceVal, unwrap(sourceType), unwrap(targetType), unresolvedTypes);
    }

    static boolean checkIsType(Type sourceType, Type targetType) {
        return checkIsType(sourceType, targetType, null);
    }

    static boolean checkIsType(Type sourceType, Type targetType, List<TypeChecker.TypePair> unresolvedTypes) {
        if (sourceType instanceof BSemType sourceSemType && targetType instanceof BSemType targetSemType) {
            return switch (isSubType(sourceSemType, targetSemType)) {
                case TRUE -> true;
                case FALSE -> false;
                case UNDEFINED -> SyntacticTypeEngine.checkIsType(sourceSemType.getBTypeComponent(),
                        targetSemType.getBTypeComponent(), unresolvedTypes);
            };
        }
        return SyntacticTypeEngine.checkIsType(unwrap(sourceType), unwrap(targetType), unresolvedTypes);
    }

    static boolean isSameType(Type sourceType, Type targetType) {
        if (sourceType instanceof BSemType sourceSemType && targetType instanceof BSemType targetSemType) {
            return switch (isSubType(sourceSemType, targetSemType)) {
                // if we got a boolean once we can't get undefined other way round
                case TRUE -> isSubType(targetSemType, sourceSemType) == SubTypeCheckResult.TRUE;
                case FALSE -> false;
                case UNDEFINED -> SyntacticTypeEngine.isSameType(sourceSemType.getBTypeComponent(),
                        targetSemType.getBTypeComponent());
            };
        }
        return SyntacticTypeEngine.isSameType(unwrap(sourceType), unwrap(targetType));
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

    static boolean checkIsLikeType(Object sourceValue, Type targetType, boolean allowNumericConversion) {
        return checkIsLikeType(null, sourceValue, targetType, new ArrayList<>(), allowNumericConversion,
                null);
    }

    static boolean checkIsLikeType(List<String> errors, Object sourceValue, Type targetType,
                                   List<TypeValuePair> unresolvedValues, boolean allowNumericConversion,
                                   String varName) {
        return switch (targetType.getTag()) {
            case TypeTags.UNION_TAG -> SyntacticTypeEngine.checkIsLikeUnionType(
                    errors, sourceValue, unwrap(targetType), unresolvedValues, allowNumericConversion, varName);
            default -> SyntacticTypeEngine.checkIsLikeType(errors, sourceValue, unwrap(targetType), unresolvedValues,
                    allowNumericConversion, varName);
        };
    }

    static boolean checkIsLikeType(Object sourceValue, Type targetType, List<TypeValuePair> unresolvedValues,
                                   boolean allowNumericConversion) {
        return switch (targetType.getTag()) {
            case TypeTags.TABLE_TAG -> SyntacticTypeEngine.TableTypeChecker.checkIsLikeTableType(
                    sourceValue, unwrap(targetType), unresolvedValues, allowNumericConversion);
            case TypeTags.MAP_TAG -> SyntacticTypeEngine.MapTypeChecker.checkIsLikeMapType(
                    sourceValue, unwrap(targetType), unresolvedValues, allowNumericConversion);
            case TypeTags.ARRAY_TAG -> SyntacticTypeEngine.ListTypeChecker.checkIsLikeArrayType(
                    sourceValue, unwrap(targetType), unresolvedValues, allowNumericConversion);
            case TypeTags.TUPLE_TAG -> SyntacticTypeEngine.ListTypeChecker.checkIsLikeTupleType(
                    sourceValue, unwrap(targetType), unresolvedValues, allowNumericConversion);
            case TypeTags.ERROR_TAG -> SyntacticTypeEngine.ErrorTypeChecker.checkIsLikeErrorType(
                    sourceValue, unwrap(targetType), unresolvedValues, allowNumericConversion);
            default -> throw new UnsupportedOperationException("Type is not supported: " + targetType);
        };
    }

    static boolean checkIsLikeType(Object sourceValue, Type targetType, List<TypeValuePair> unresolvedValues,
                                   boolean allowNumericConversion, String varName, List<String> errors) {
        return switch (targetType.getTag()) {
            case TypeTags.RECORD_TYPE_TAG -> SyntacticTypeEngine.MapTypeChecker.checkIsLikeRecordType(
                    sourceValue, unwrap(targetType), unresolvedValues, allowNumericConversion, varName, errors);
            default -> throw new UnsupportedOperationException("Type is not supported: " + targetType);
        };
    }

    static boolean checkIsLikeType(Object sourceValue, Type sourceType, Type targetType,
                                   List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        return switch (targetType.getTag()) {
            case TypeTags.JSON_TAG -> SyntacticTypeEngine.MapTypeChecker.checkIsLikeJSONType(
                    sourceValue, unwrap(sourceType), unwrap(targetType), unresolvedValues, allowNumericConversion);
            default -> throw new UnsupportedOperationException("Type is not supported: " + targetType);
        };
    }

    static boolean checkIsLikeType(Object sourceValue, Type targetType) {
        return switch (targetType.getTag()) {
            case TypeTags.STREAM_TAG -> SyntacticTypeEngine.StreamTypeChecker.checkIsLikeStreamType(
                    sourceValue, unwrap(targetType));
            default -> checkIsLikeType(sourceValue, targetType, false);
        };
    }

    static boolean checkFiniteTypeAssignable(Object sourceValue, Type sourceType, Type targetType,
                                             List<TypeValuePair> unresolvedValues, boolean allowNumericConversion) {
        return SyntacticTypeEngine.checkFiniteTypeAssignable(sourceValue, unwrap(sourceType), unwrap(targetType),
                unresolvedValues, allowNumericConversion);
    }

    public static boolean isInherentlyImmutableType(Type sourceType) {
        return SyntacticTypeEngine.isInherentlyImmutableType(unwrap(sourceType));
    }

    protected static boolean isSimpleBasicType(Type type) {
        return SyntacticTypeEngine.isSimpleBasicType(unwrap(type));
    }

    static boolean isFiniteTypeValue(Object sourceValue, Type sourceType, Object valueSpaceItem,
                                     boolean allowNumericConversion) {
        return SyntacticTypeEngine.isFiniteTypeValue(sourceValue, sourceType, valueSpaceItem, allowNumericConversion);
    }

    protected static boolean isNumericType(Type type) {
        return SyntacticTypeEngine.isNumericType(unwrap(type));
    }

    public static boolean isSelectivelyImmutableType(Type type, Set<Type> unresolvedTypes) {
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
        return SyntacticTypeEngine.MapTypeChecker.checkIsJSONType(unwrap(sourceType), unresolvedTypes);
    }

    static boolean isFiniteTypeMatch(Type sourceType, Type targetType) {
        return SyntacticTypeEngine.isFiniteTypeMatch(unwrap(sourceType), unwrap(targetType));
    }
}
