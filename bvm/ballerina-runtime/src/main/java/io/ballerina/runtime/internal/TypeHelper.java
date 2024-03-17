/*
 *
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
 * /
 *
 */

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeBuilder;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.BAnnotatableType;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BFutureType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BParameterizedType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BStreamType;
import io.ballerina.runtime.internal.types.BTableType;
import io.ballerina.runtime.internal.types.BTupleType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BTypeReferenceType;
import io.ballerina.runtime.internal.types.BTypedescType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.types.BXmlType;
import io.ballerina.runtime.internal.types.semtype.BSemType;
import io.ballerina.runtime.internal.types.semtype.BTypeComponent;
import io.ballerina.runtime.internal.types.semtype.Core;
import io.ballerina.runtime.internal.types.semtype.SemTypeUtils;
import io.ballerina.runtime.internal.values.MapValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static io.ballerina.runtime.api.TypeBuilder.unwrap;
import static io.ballerina.runtime.api.TypeBuilder.wrap;
import static io.ballerina.runtime.internal.types.semtype.Core.containsSimple;
import static io.ballerina.runtime.internal.types.semtype.Core.intersect;
import static io.ballerina.runtime.internal.types.semtype.Core.union;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_BOOLEAN;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_BTYPE;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_DECIMAL;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_FLOAT;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_INT;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_NEVER;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_NIL;
import static io.ballerina.runtime.internal.types.semtype.SemTypeUtils.BasicTypeCodes.BT_STRING;

// TODO: type utils factor these to a separate protected class
public class TypeHelper {

    public static Type typeConstraint(Type type) {
        Type unwrappedType = unwrap(type);
        if (unwrappedType instanceof BXmlType xmlType) {
            return xmlType.constraint;
        } else if (unwrappedType instanceof BMapType mapType) {
            return mapType.getConstrainedType();
        } else if (unwrappedType instanceof BTypedescType typedescType) {
            return typedescType.getConstraint();
        } else if (unwrappedType instanceof BTypeReferenceType referenceType) {
            return typeConstraint(referenceType.getReferredType());
        } else if (unwrappedType instanceof BIntersectionType intersectionType) {
            return typeConstraint(intersectionType.getEffectiveType());
        } else if (unwrappedType instanceof BTableType tableType) {
            return tableType.getConstrainedType();
        } else if (unwrappedType instanceof BStreamType streamType) {
            return streamType.getConstrainedType();
        } else if (unwrappedType instanceof BFutureType futureType) {
            return futureType.getConstrainedType();
        }
        throw new UnsupportedOperationException("Type constraint not supported for type: " + type);
    }

    public static String[] tableFieldNames(Type type) {
        BTableType tableType = unwrap(type);
        return tableType.getFieldNames();
    }

    public static Map<String, Field> mappingRequiredFields(Type type) {
        BRecordType recordType = unwrap(type);
        return recordType.getFields();
    }

    static boolean mappingTypeSealed(Type type) {
        BRecordType recordType = unwrap(type);
        return recordType.sealed;
    }

    static Type mappingRestFieldType(Type type) {
        BRecordType recordType = unwrap(type);
        return recordType.restFieldType;
    }

    public static List<Type> listMemberTypes(Type type) {
        BTupleType tupleType = unwrap(type);
        return tupleType.getTupleTypes();
    }

    public static Type listRestType(Type type) {
        Type unwrappedType = unwrap(type);
        if (unwrappedType instanceof ArrayType arrayType) {
            return arrayType.getElementType();
        } else if (unwrappedType instanceof TupleType tupleType) {
            return tupleType.getRestType();
        } else if (unwrappedType instanceof BTypeReferenceType referenceType) {
            return listRestType(referenceType.getReferredType());
        } else if (unwrappedType instanceof BIntersectionType intersectionType) {
            return listRestType(intersectionType.getEffectiveType());
        }
        throw new UnsupportedOperationException("rest type not supported for type: " + type);
    }

    public static ArrayType.ArrayState arrayState(Type type) {
        Type unwrappedType = unwrap(type);
        if (unwrappedType instanceof ArrayType arrayType) {
            return arrayType.getState();
        } else if (unwrappedType instanceof BTypeReferenceType referenceType) {
            return arrayState(referenceType.getReferredType());
        } else if (unwrappedType instanceof IntersectionType intersectionType) {
            return arrayState(intersectionType.getEffectiveType());
        }
        throw new UnsupportedOperationException("array state not supported for type: " + type);
    }

    public static boolean hasFillerValue(Type type) {
        BArrayType arrayType = unwrap(type);
        return arrayType.hasFillerValue();
    }

    public static int listFixedSize(Type type) {
        BArrayType arrayType = unwrap(type);
        return arrayType.getSize();
    }

    public static MemberTypeIterable members(Type type) {
        return new MemberTypeIterable(type);
    }

    public static List<Type> memberList(Type type) {
        List<Type> memberTypes = new ArrayList<>();
        for (Type each : members(type)) {
            memberTypes.add(each);
        }
        return Collections.unmodifiableList(memberTypes);
    }

    public static Type effectiveType(Type type) {
        if (type instanceof BIntersectionType intersectionType) {
            return intersectionType.getEffectiveType();
        }
        // TODO: figure out a better workaround for this currently this is exploiting the fact that BType component is
        //  the original intersection type
        BSemType semType = (BSemType) type;
        BTypeComponent bTypeComponent = (BTypeComponent) semType.subTypeData[BT_BTYPE];
        BIntersectionType intersectionType = (BIntersectionType) bTypeComponent.getBTypeComponent();
        return intersectionType.getEffectiveType();
    }

    public static List<Type> constituentTypes(Type type) {
        if (type instanceof BSemType semType) {
            List<Type> members = new ArrayList<>();
            for (int i = 0; i < SemTypeUtils.BasicTypeCodes.N_TYPES; i++) {
                // TODO: this is not handling the subType correctly
                if ((semType.all & (1 << i)) != 0) {
                    members.add(fromUniformType(i));
                }
            }
            if (containsSimple(semType, BT_BTYPE)) {
                BTypeComponent bTypeComponent = (BTypeComponent) semType.subTypeData[BT_BTYPE];
                BType bType = bTypeComponent.getBTypeComponent();
                if (bType instanceof BUnionType bUnionType) {
                    members.addAll(bUnionType.getMemberTypes());
                } else if (bType instanceof BIntersectionType intersectionType) {
                    members.addAll(intersectionType.constituentTypes); // TODO: this is going to add duplicate types
                } else {
                    members.add(bType);
                }
            }
            return members;
        }
        Type unwrappedType = unwrap(type);
        if (unwrappedType instanceof BUnionType unionType) {
            return memberList(unionType);
        } else if (unwrappedType instanceof BIntersectionType intersectionType) {
            return intersectionType.getConstituentTypes();
        }
        throw new UnsupportedOperationException("constituent types not supported for type: " + type);
    }

    private static Type fromUniformType(int typeCode) {
        return switch (typeCode) {
            case BT_NEVER -> PredefinedTypes.TYPE_NEVER;
            case BT_NIL -> PredefinedTypes.TYPE_NULL;
            case BT_BOOLEAN -> PredefinedTypes.TYPE_BOOLEAN;
            case BT_STRING -> PredefinedTypes.TYPE_STRING;
            case BT_DECIMAL -> PredefinedTypes.TYPE_DECIMAL;
            case BT_FLOAT -> PredefinedTypes.TYPE_FLOAT;
            case BT_INT -> PredefinedTypes.TYPE_INT;
            default -> throw new UnsupportedOperationException("uniform type not supported for type code: " + typeCode);
        };
    }

    public static Type referredType(Type type) {
        BTypeReferenceType referenceType = unwrap(type);
        return referenceType.getReferredType();
    }

    static Type paramValueType(Type type) {
        BParameterizedType parameterizedType = unwrap(type);
        return parameterizedType.getParamValueType();
    }

    // TODO: ideally if we should always use proper TypeChecks to check if two types are equal
    @Deprecated
    public static boolean typeEqual(Type sourceType, Type targetType) {
        if (sourceType == targetType) {
            return true;
        }
        Type sourceUnwrapped = unwrap(sourceType);
        Type targetUnwrapped = unwrap(targetType);
        return sourceUnwrapped.equals(targetUnwrapped);
    }

    public static final class MemberTypeIterable implements Iterable<Type> {

        private final List<Type> memberTypes;
        private int index = 0;

        private MemberTypeIterable(Type type) {
            memberTypes = new ArrayList<>();
            Queue<Type> remainingMembers;
            if (type instanceof BUnionType unionType) {
                remainingMembers = new LinkedList<>(unionType.getMemberTypes());
            } else if (type instanceof BSemType semType) {
                remainingMembers = new LinkedList<>();
                // TODO: factor a simple type code and Type array for all implemented semtypes
                if (containsSimple(semType, BT_NIL)) {
                    remainingMembers.add(PredefinedTypes.TYPE_NULL);
                }
                if (containsSimple(semType, BT_BOOLEAN)) {
                    remainingMembers.add(Core.intersect(semType, (BSemType) PredefinedTypes.TYPE_BOOLEAN));
                }
                if (containsSimple(semType, BT_STRING)) {
                    remainingMembers.add(Core.intersect(semType, (BSemType) PredefinedTypes.TYPE_STRING));
                }
                if (containsSimple(semType, BT_DECIMAL)) {
                    remainingMembers.add(Core.intersect(semType, (BSemType) PredefinedTypes.TYPE_DECIMAL));
                }
                if (containsSimple(semType, BT_FLOAT)) {
                    remainingMembers.add(Core.intersect(semType, (BSemType) PredefinedTypes.TYPE_FLOAT));
                }
                if (containsSimple(semType, BT_INT)) {
                    remainingMembers.add(Core.intersect(semType, (BSemType) PredefinedTypes.TYPE_INT));
                }
                if (containsSimple(semType, BT_BTYPE)) {
                    BTypeComponent bTypeComponent = (BTypeComponent) semType.subTypeData[BT_BTYPE];
                    BType bType = bTypeComponent.getBTypeComponent();
                    if (bType instanceof BUnionType bUnionType) {
                        remainingMembers.addAll(bUnionType.getMemberTypes());
                    } else {
                        remainingMembers.add(bType);
                    }
                }
            } else {
                throw new UnsupportedOperationException("member types not supported for type: " + type);
            }
            while (!remainingMembers.isEmpty()) {
                Type member = remainingMembers.poll();
                if (member instanceof BUnionType uType) {
                    remainingMembers.addAll(uType.getMemberTypes());
                } else {
                    memberTypes.add(member);
                }
            }
        }

        @Override
        public Iterator<Type> iterator() {
            return new Iterator<Type>() {
                @Override
                public boolean hasNext() {
                    return index < memberTypes.size();
                }

                @Override
                public Type next() {
                    return memberTypes.get(index++);
                }
            };
        }
    }

    public static void setAnnotations(Type describingType, MapValue annotations) {
        BAnnotatableType annotatableType = TypeBuilder.unwrap(describingType);
        annotatableType.setAnnotations(annotations);
    }
}
