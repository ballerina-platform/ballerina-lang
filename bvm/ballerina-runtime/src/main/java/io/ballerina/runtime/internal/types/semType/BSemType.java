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
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.BType;

import java.util.BitSet;
import java.util.List;

import static io.ballerina.runtime.internal.types.semType.SemTypeUtils.UniformTypeCodes.N_TYPES;

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

    protected BSemType() {
        this.all = new BitSet();
        this.some = new BitSet();
        this.subTypeData = new ProperSubTypeData[N_TYPES];
    }
    protected BSemType(BitSet all, BitSet some, ProperSubTypeData[] subtypeData) {
        this.all = all;
        this.some = some;
        this.subTypeData = subtypeData;
    }

    private boolean onlyBType() {
        return some.cardinality() <= 1 && some.get(SemTypeUtils.UniformTypeCodes.UT_BTYPE);
    }

    public void setIdentifiers(String name, Module module) {
        this.name = name;
        this.module = module;
    }

    private BType getBType() {
        if (!onlyBType()) {
            throw new IllegalStateException("This semType does not contain only a BType");
        }
        return getBTypeComponent();
    }

    @Deprecated
    public BType getBTypeComponent() {
        BTypeComponent bTypeComponent = (BTypeComponent) subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
        if (this.name != null || this.module != null) {
            return bTypeComponent.getBTypeComponent(this.name, this.module);
        }
        return bTypeComponent.getBTypeComponent();
    }

    public BType getInnerType() {
        return getBType();
    }

    @Override
    public <V> V getZeroValue() {
        return getBType().getZeroValue();
    }

    @Override
    public <V> V getEmptyValue() {
        return getBType().getEmptyValue();
    }

    @Override
    public int getTag() {
        return getBType().getTag();
    }

    @Override
    public boolean isNilable() {
        return getBType().isNilable();
    }

    @Override
    public String getName() {
        return getBType().getName();
    }

    @Override
    public String getQualifiedName() {
        return getBType().getQualifiedName();
    }

    @Override
    public Module getPackage() {
        return getBType().getPackage();
    }

    @Override
    public boolean isPublic() {
        return getBType().isPublic();
    }

    @Override
    public boolean isNative() {
        return getBType().isNative();
    }

    @Override
    public boolean isAnydata() {
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
        return getBType().isAnydata();
    }

    @Override
    public boolean isPureType() {
        return getBType().isPureType();
    }

    @Override
    public boolean isReadOnly() {
        return getBType().isReadOnly();
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
        return getBType().toString();
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
        BSubTypeData bTypeComponent = (BSubTypeData) subTypeData[SemTypeUtils.UniformTypeCodes.UT_BTYPE];
        bTypeComponent.setReadonly(readonly);
    }

    public void setOrderedUnionMembers(Type[] orderedUnionMembers) {
        this.orderedUnionMembers = orderedUnionMembers;
    }
}
