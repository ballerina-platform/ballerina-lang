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
import io.ballerina.runtime.internal.types.semType.BSemType;
import io.ballerina.runtime.internal.types.semType.BTypeComponent;
import io.ballerina.runtime.internal.types.semType.Core;
import io.ballerina.runtime.internal.types.semType.SemTypeUtils;
import io.ballerina.runtime.internal.values.MapValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static io.ballerina.runtime.api.TypeBuilder.unwrap;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BOOLEAN;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BTYPE;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_NEVER;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_NIL;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_STRING;

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

    // FIXME: is this the same not having a rest type (if so return a optional type for rest)
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
        // the original intersection type
        BSemType semType = (BSemType) type;
        BTypeComponent bTypeComponent = (BTypeComponent) semType.subTypeData[UT_BTYPE];
        BIntersectionType intersectionType = (BIntersectionType) bTypeComponent.getBTypeComponent();
        return intersectionType.getEffectiveType();
        // This causes an stack overflow (TODO: fix this)
//        List<Type> members = new ArrayList<>(N_TYPES);
//        for (int i = 0; i < N_TYPES; i++) {
//            if (semType.all.get(i)) {
//                members.add(fromUniformType(i));
//            }
//            if (semType.some.get(i)) {
//                // FIXME: when we have proper subtypes we should handle it here
//                // I think it is best to introduce an interface that convert type component to a Type
//                BTypeComponent bTypeComponent = (BTypeComponent) semType.subTypeData[i];
//                members.add(bTypeComponent.getBTypeComponent());
//            }
//        }
//        Iterator<Type> it = members.iterator();
//        if (!it.hasNext()) {
//            return PredefinedTypes.TYPE_NEVER;
//        }
//        Type t1 = it.next();
//        if (!it.hasNext()) {
//            return t1;
//        }
//        Type t2 = it.next();
//        Type[] rest = it.hasNext() ? members.subList(1, members.size()).toArray(new Type[0]) : new Type[0];
//        return TypeBuilder.union(t1, t2, rest);
    }

    public static List<Type> constituentTypes(Type type) {
        // SemType don't have a concept of "constituent types". For union types how ever this simply translate to
        // all the positive subtypes (TODO: haven't think about negative). However we can't breakup BIntesection type
        // the same way Instead we are going to simulate the same behaviour as BInterSection Type using fallowing
        // invariants (enforced by how we create SemType in SemTypeUtils)
        // 1. Any member that is implemented by the semtypes will be now properly represented as either a some or all
        // 2. Whole intersection will be the BIntersection type (TODO: ideally we should create a new object with just
        // the remining parts, so far haven't caused problems)
        if (type instanceof BSemType semType) {
            List<Type> members = new ArrayList<>();
            for (int i = 0; i < SemTypeUtils.UniformTypeCodes.N_TYPES; i++) {
                if (semType.all.get(i)) {
                    // TODO: make this cleaner
                    members.add(fromUniformType(i));
                }
            }
            if (semType.some.get(UT_BTYPE)) {
                BTypeComponent bTypeComponent = (BTypeComponent) semType.subTypeData[UT_BTYPE];
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
            case UT_NEVER -> PredefinedTypes.TYPE_NEVER;
            case UT_NIL -> PredefinedTypes.TYPE_NULL;
            case UT_BOOLEAN -> PredefinedTypes.TYPE_BOOLEAN;
            case UT_STRING -> PredefinedTypes.TYPE_STRING;
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
                // TODO: factor a simple type code and Type array for all implemnted semtypes
                if (Core.containsSimple(semType, UT_NIL)) {
                    remainingMembers.add(PredefinedTypes.TYPE_NULL);
                }
                if (Core.containsSimple(semType, UT_BOOLEAN)) {
                    remainingMembers.add(PredefinedTypes.TYPE_BOOLEAN);
                }
                if (Core.containsSimple(semType, UT_STRING)) {
                    remainingMembers.add(PredefinedTypes.TYPE_STRING);
                }
                if (semType.some.get(UT_BTYPE)) {
                    BTypeComponent bTypeComponent = (BTypeComponent) semType.subTypeData[UT_BTYPE];
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
