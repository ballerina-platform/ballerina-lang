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

package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.ReferenceType;
import io.ballerina.runtime.api.types.SemType.BasicTypeCode;
import io.ballerina.runtime.api.types.SemType.Builder;
import io.ballerina.runtime.api.types.SemType.Core;
import io.ballerina.runtime.api.types.SemType.SemType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.semtype.BSubType;
import io.ballerina.runtime.internal.values.DecimalValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// NOTE: this is so that we don't have to expose any utility constructors as public to builder
final class BTypeConverter {

    private BTypeConverter() {
    }

    private static final SemType READONLY_SEMTYPE_PART =
            unionOf(Builder.intType(), Builder.floatType(), Builder.nilType(), Builder.decimalType());
    private static final SemType ANY_SEMTYPE_PART =
            unionOf(Builder.intType(), Builder.floatType(), Builder.nilType(), Builder.decimalType());

    private static SemType unionOf(SemType... semTypes) {
        SemType result = Builder.neverType();
        for (SemType semType : semTypes) {
            result = Core.union(result, semType);
        }
        return result;
    }

    private static SemType from(Type type) {
        if (type instanceof SemType semType) {
            return semType;
        } else if (type instanceof BType bType) {
            return from(bType);
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    // TODO: ideally this should be only called by BTypes (ie. no need for this to be public) and they should call
    //  the correct method (ie. no need for this instance of thing)
    public static SemType from(BType innerType) {
        return innerType.get();
    }

    static SemType fromReadonly(BReadonlyType readonlyType) {
        SemType bTypePart = wrapAsPureBType(readonlyType);
        return Core.union(READONLY_SEMTYPE_PART, bTypePart);
    }

    static SemType fromTypeReference(ReferenceType referenceType) {
        return from(referenceType.getReferredType());
    }

    static SemType fromTupleType(BTupleType tupleType) {
        for (Type type : tupleType.getTupleTypes()) {
            if (Core.isNever(from(type))) {
                return Builder.neverType();
            }
        }
        return wrapAsPureBType(tupleType);
    }

    static SemType wrapAsPureBType(BType tupleType) {
        return Builder.basicSubType(BasicTypeCode.BT_B_TYPE, BSubType.wrap(tupleType));
    }

    static SemType fromAnyType(BAnyType anyType) {
        SemType bTypePart = wrapAsPureBType(anyType);
        return Core.union(ANY_SEMTYPE_PART, bTypePart);
    }

    static SemType fromRecordType(BRecordType recordType) {
        for (Field field : recordType.fields.values()) {
            if (!SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.OPTIONAL)) {
                SemType fieldType = from(field.getFieldType());
                if (Core.isNever(fieldType)) {
                    return Builder.neverType();
                }
            }
        }
        return wrapAsPureBType(recordType);
    }

    static SemType fromFiniteType(BFiniteType finiteType) {
        BTypeParts parts = splitFiniteType(finiteType);
        if (parts.bTypeParts().isEmpty()) {
            return parts.semTypePart();
        }
        BType newFiniteType = (BType) parts.bTypeParts().get(0);
        SemType bTypePart = wrapAsPureBType(newFiniteType);
        return Core.union(parts.semTypePart(), bTypePart);
    }

    static SemType fromUnionType(BUnionType unionType) {
        BTypeParts parts = splitUnion(unionType);
        if (parts.bTypeParts().isEmpty()) {
            return parts.semTypePart();
        }
        SemType bTypePart = Builder.basicSubType(BasicTypeCode.BT_B_TYPE, BSubType.wrap(unionType));
        return Core.union(parts.semTypePart(), bTypePart);
    }

    private record BTypeParts(SemType semTypePart, List<Type> bTypeParts) {

    }

    private static BTypeParts split(Type type) {
        if (isSemType(type)) {
            return new BTypeParts(from(type), Collections.emptyList());
        } else if (type instanceof BUnionType unionType) {
            return splitUnion(unionType);
        } else if (type instanceof BAnyType anyType) {
            return splitAnyType(anyType);
        } else if (type instanceof BTypeReferenceType referenceType) {
            return split(referenceType.getReferredType());
        } else if (type instanceof BIntersectionType intersectionType) {
            return split(intersectionType.getEffectiveType());
        } else if (type instanceof BReadonlyType readonlyType) {
            return splitReadonly(readonlyType);
        } else if (type instanceof BFiniteType finiteType) {
            return splitFiniteType(finiteType);
        } else {
            return new BTypeParts(Builder.neverType(), List.of(type));
        }
    }

    private static BTypeParts splitAnyType(BAnyType anyType) {
        return new BTypeParts(ANY_SEMTYPE_PART, List.of(anyType));
    }

    private static BTypeParts splitFiniteType(BFiniteType finiteType) {
        Set<Object> newValueSpace = new HashSet<>(finiteType.valueSpace.size());
        SemType semTypePart = Builder.neverType();
        for (var each : finiteType.valueSpace) {
            // TODO: lift this to Builder (Object) -> Type
            if (each == null) {
                semTypePart = Core.union(semTypePart, Builder.nilType());
            } else if (each instanceof DecimalValue decimalValue) {
                semTypePart = Core.union(semTypePart, Builder.decimalConst(decimalValue.value()));
            } else if (each instanceof Double doubleValue) {
                semTypePart = Core.union(semTypePart, Builder.floatConst(doubleValue));
            } else if (each instanceof Number intValue) {
                semTypePart = Core.union(semTypePart, Builder.intConst(intValue.longValue()));
            } else {
                newValueSpace.add(each);
            }
        }
        if (newValueSpace.isEmpty()) {
            return new BTypeParts(semTypePart, List.of());
        }
        BFiniteType newFiniteType = finiteType.cloneWithValueSpace(newValueSpace);
        return new BTypeParts(semTypePart, List.of(newFiniteType));
    }

    private static BTypeParts splitReadonly(BReadonlyType readonlyType) {
        // TODO: this is not exactly correct
        return new BTypeParts(READONLY_SEMTYPE_PART, List.of(readonlyType));
    }

    private static BTypeParts splitUnion(BUnionType unionType) {
        List<Type> members = Collections.unmodifiableList(unionType.getMemberTypes());
        List<Type> bTypeMembers = new ArrayList<>(members.size());
        SemType semTypePart = Builder.neverType();
        for (Type member : members) {
            BTypeParts memberParts = split(member);
            semTypePart = Core.union(memberParts.semTypePart(), semTypePart);
            bTypeMembers.addAll(memberParts.bTypeParts());
        }
        return new BTypeParts(semTypePart, Collections.unmodifiableList(bTypeMembers));
    }

    private static boolean isSemType(Type type) {
        // FIXME: can't we replace this with instanceof check?
        return switch (type.getTag()) {
            case TypeTags.NEVER_TAG, TypeTags.NULL_TAG, TypeTags.DECIMAL_TAG, TypeTags.FLOAT_TAG,
                 TypeTags.INT_TAG, TypeTags.BYTE_TAG,
                 TypeTags.SIGNED8_INT_TAG, TypeTags.SIGNED16_INT_TAG, TypeTags.SIGNED32_INT_TAG,
                 TypeTags.UNSIGNED8_INT_TAG, TypeTags.UNSIGNED16_INT_TAG, TypeTags.UNSIGNED32_INT_TAG -> true;
            default -> false;
        };
    }
}
