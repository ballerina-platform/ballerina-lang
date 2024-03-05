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

package io.ballerina.runtime.internal.types.semType;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.types.BAnyType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BType;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.N_TYPES;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_BTYPE;
import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.UT_NIL;

public class BSemType implements Type {

    public final BitSet all;
    public final BitSet some;
    public final ProperSubTypeData[] subTypeData;
    private String name;
    private Module module;
    // NOTE: we are keeping these around instead of eagerly building the string representation to reduce the cost of
    // creating type. We need the string representation only to create errors (?). Need to check if this don't create
    // any undesired gc pressure
    private Type[] orderedUnionMembers = null;
    private int tag = -1;

    protected BSemType() {
        this.all = new BitSet(N_TYPES);
        this.some = new BitSet(N_TYPES);
        this.subTypeData = new ProperSubTypeData[N_TYPES];
    }
    protected BSemType(BitSet all, BitSet some, ProperSubTypeData[] subtypeData) {
        this.all = all;
        this.some = some;
        this.subTypeData = subtypeData;
    }

    private boolean onlyBType() {
        return some.cardinality() == 1 && some.get(SemTypeUtils.UniformTypeCodes.UT_BTYPE) && all.cardinality() == 0;
    }

    public void setIdentifiers(String name, Module module) {
        this.name = name;
        this.module = module;
    }

    @Deprecated
    public BType getBType() {
        if (!onlyBType()) {
            throw new IllegalStateException("This semType does not contain only a BType");
        }
        return getBTypePart();
    }

    private BType getBTypePart() {
        BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
        if (this.name != null || this.module != null) {
            return bTypeComponent.getBTypeComponent(this.name, this.module);
        }
        return bTypeComponent.getBTypeComponent();
    }

    @Override
    public <V> V getZeroValue() {
        if (onlyBType()) {
            return getBType().getZeroValue();
        }
        if (all.cardinality() == 1 && all.get(UT_NIL)) {
            return null;
        }
        throw new RuntimeException("unimplemented");
    }

    @Override
    public <V> V getEmptyValue() {
        if (onlyBType()) {
            return getBType().getEmptyValue();
        }
        if (all.cardinality() == 1 && all.get(UT_NIL)) {
            return null;
        }
        throw new RuntimeException("unimplemented");
    }

    @Override
    public int getTag() {
        if (tag == -1) {
            tag = calculateTag();
        }
        return tag;
    }

    private int calculateTag() {
        if (some.get(UT_BTYPE)) {
            BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
            if (bTypeComponent instanceof BSubTypeData subTypeData) {
                switch (subTypeData.getTypeClass()) {
                    case BAnyData -> {
                        return TypeTags.ANYDATA_TAG;
                    }
                    case BJson -> {
                        return TypeTags.JSON_TAG;
                    }
                }
            } else if (bTypeComponent instanceof BAnyType) {
                return TypeTags.ANY_TAG;
            } else if (bTypeComponent instanceof BIntersectionType) {
                return TypeTags.INTERSECTION_TAG;
            }
        }
        if (some.cardinality() + all.cardinality() > 1) {
            return TypeTags.UNION_TAG;
        }
        if (some.cardinality() == 0) {
            if (all.cardinality() == 1 && all.get(UT_NIL)) {
                return TypeTags.NULL_TAG;
            }
            return TypeTags.NEVER_TAG;
        }
        return getBTypePart().getTag();
    }

    @Override
    public boolean isNilable() {
        // FIXME:
        if (onlyBType()) {
            return getBType().isNilable();
        }
        if (all.cardinality() == 1 && all.get(UT_NIL)) {
            return true;
        }
        throw new RuntimeException("unimplemented");
    }

    @Override
    public String getName() {
        return name == null ? "" : name;
    }

    @Override
    public String getQualifiedName() {
        if (name == null) {
            return "";
        }
        return module == null ? name : module.toString() + ":" + name;
    }

    @Override
    public Module getPackage() {
        return module;
    }

    @Override
    public boolean isPublic() {
        // FIXME:
        if (onlyBType()) {
            return getBType().isPublic();
        }
        return false;
    }

    @Override
    public boolean isNative() {
        if (onlyBType()) {
            return getBType().isNative();
        }
        return false;
    }

    @Override
    public boolean isAnydata() {
        // Error type is always a BType
        if (!some.get(SemTypeUtils.UniformTypeCodes.UT_BTYPE)) {
            return true;
        }
        BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
        if (bTypeComponent instanceof BSubTypeData subTypeData) {
            // NOTE: this is because for cyclic types trying to get the BType going to put us to infinite loop
            // TODO: fix this with something similar to what we have in BUnionType
            if (subTypeData.isCyclic) {
                BSubTypeData.BTypeClass typeClass = subTypeData.getTypeClass();
                return typeClass == BSubTypeData.BTypeClass.BAnyData ||
                        typeClass == BSubTypeData.BTypeClass.BJson;
            }
        }
        return bTypeComponent.getBTypeComponent().isAnydata();
    }

    @Override
    public boolean isPureType() {
        if (onlyBType()) {
            return getBType().isPureType();
        }
        if (all.cardinality() == 1 && all.get(UT_NIL)) {
            return false;
        }
        throw new RuntimeException("unimplemented");
    }

    @Override
    public boolean isReadOnly() {
        if (onlyBType()) {
            return getBType().isReadOnly();
        }
        // If we have only basic types for the subset we have implmented it is always readonly
        if (all.cardinality() == 1 && all.get(UT_NIL) && some.cardinality() == 0) {
            return true;
        }
        if (some.get(UT_BTYPE)) {
            return getBTypePart().isReadOnly();
        }
        throw new RuntimeException("unimplemented");
    }

    @Override
    public long getFlags() {
        return getBType().getFlags();
    }

    @Override
    public Type getImmutableType() {
        return getBType().getImmutableType();
    }

    @Override
    public void setImmutableType(IntersectionType immutableType) {
        getBType().setImmutableType(immutableType);
    }

    @Override
    public Module getPkg() {
        return getBType().getPkg();
    }

    @Override
    public String toString() {
        if (name != null) {
            return (module == null || module.getName() == null || module.getName().equals(".")) ? name :
                    module.toString() + ":" + name;
        }
        boolean containsNull = false;
        boolean addPrefix = false;
        if (orderedUnionMembers != null) {
            StringBuilder sb = new StringBuilder();
            for (Type member : orderedUnionMembers) {
                if (member.getTag() == TypeTags.NULL_TAG) {
                    containsNull = true;
                    continue;
                }
                String memberStr = member.toString();
                if (addPrefix) {
                    sb.append("|");
                }
                if (memberStr.length() > 2 && memberStr.charAt(0) == '(' &&
                        memberStr.charAt(memberStr.length() - 1) == ')') {
                    sb.append(memberStr, 1, memberStr.length() - 1);
                } else {
                    sb.append(memberStr);
                }
                addPrefix = true;
            }
            return containsNull ? "(" + sb + ")?" : "(" + sb + ")";
        }
        if (TypeChecker.isSameType(this, PredefinedTypes.TYPE_NULL)) {
            return "()";
        }
        if (TypeChecker.isSameType(this, PredefinedTypes.TYPE_ANY)) {
            return "any";
        }
        if (TypeChecker.isSameType(this, PredefinedTypes.TYPE_JSON)) {
            return "json";
        }
        if (TypeChecker.isSameType(this, PredefinedTypes.TYPE_READONLY)) {
            return "readonly";
        }
        if (onlyBType()) {
            return getBType().toString();
        }
        return some.get(UT_BTYPE) ? getBTypePart().toString() + "?" : "()";
    }

    public void addCyclicMembers(List<Type> members) {
        BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
        bTypeComponent.addCyclicMembers(members);
    }

    public void setBTypeClass(BSubTypeData.BTypeClass typeClass) {
        BSubTypeData bTypeComponent = (BSubTypeData) subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
        bTypeComponent.setBTypeClass(typeClass);
    }

    public void setReadonly(boolean readonly) {
        ProperSubTypeData subTypeData = this.subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
        if (subTypeData instanceof BSubTypeData bTypeComponent) {
            bTypeComponent.setReadonly(readonly);
        }
    }

    public void setOrderedUnionMembers(Type[] orderedUnionMembers) {
        this.orderedUnionMembers = orderedUnionMembers;
    }

    @Override
    public int hashCode() {
        return all.hashCode() + 31 * some.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BSemType other) {
            return all.equals(other.all) && some.equals(other.some) && Arrays.equals(subTypeData, other.subTypeData);
        }
        return false;
    }
}
