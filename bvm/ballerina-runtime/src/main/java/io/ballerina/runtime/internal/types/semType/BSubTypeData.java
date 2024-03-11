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
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.types.BAnydataType;
import io.ballerina.runtime.internal.types.BJsonType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BUnionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: document
// A common interface to represent a union of BTypes, will create the compatible BType on demand
public class BSubTypeData implements ProperSubTypeData, BTypeComponent {

    private final List<Type> members;
    private final List<Type> cyclicMembers;
    boolean isCyclic;
    private BType bType;
    private BTypeClass typeClass = BTypeClass.BUnionType;
    private Boolean readonly = null;

    public enum BTypeClass {
        BUnionType,
        BAnyData,
        BJson
    }

    public BSubTypeData(List<Type> members) {
        Set<Type> uniqueMembers = new HashSet<>(members);
        this.members = List.copyOf(uniqueMembers);
        isCyclic = false;
        cyclicMembers = new ArrayList<>();
    }

    private BSubTypeData(List<Type> members, List<Type> cyclicMembers, boolean isCyclic) {
        Set<Type> uniqueMembers = new HashSet<>(members);
        this.members = List.copyOf(uniqueMembers);
        this.cyclicMembers = cyclicMembers;
        this.isCyclic = isCyclic;
    }

    @Override
    public ProperSubTypeData union(ProperSubTypeData other) {
        // We need to create new instance since (in the future) we'll use union as an operation
        if (other instanceof BSubTypeData typeUnion) {
            return unionWith(typeUnion);
        } else if (other instanceof BType bType) {
            return unionWith(bType);
        }
        throw new UnsupportedOperationException("BType union can't be calculate union with " + other);
    }

    private BSubTypeData unionWith(BSubTypeData other) {
//        List<Type> combinedMembers = new ArrayList<>();
//        if (this.isCyclic) {
//            combinedMembers.add(this.getBTypeComponent());
//        } else {
//            combinedMembers.addAll(this.members);
//        }
//        if (other.isCyclic) {
//            combinedMembers.add(other.getBTypeComponent());
//        } else {
//            combinedMembers.addAll(other.members);
//        }
//        // FIXME: this is a hack we need to do what BUnionType does in merge here as well
//        HashSet<Type> uniqueMembers = new HashSet<>(this.members);
//        uniqueMembers.addAll(other.members);
//        List<Type> combinedMembers = new ArrayList<>(uniqueMembers);
//
//        // TODO: handle the case where unique members already contain cyclic members
//        combinedMembers.addAll(this.cyclicMembers);
//        combinedMembers.addAll(other.cyclicMembers);
//        HashSet<Type> uniqueCyclicMembers = new HashSet<>(this.cyclicMembers);
//        uniqueCyclicMembers.addAll(other.cyclicMembers);
//        List<Type> combinedCyclicMembers = new ArrayList<>(uniqueCyclicMembers);

//        boolean isCyclic = this.isCyclic || other.isCyclic;
        return new BSubTypeData(List.of(this.getBTypeComponent(), other.getBTypeComponent()));
//        return new BSubTypeData(combinedMembers, List.of(), false);
    }

    private BSubTypeData unionWith(BType other) {
//        HashSet<Type> uniqueMembers = new HashSet<>(this.members);
//        uniqueMembers.add(other);
//        List<Type> combinedMembers = new ArrayList<>(uniqueMembers);
//        List<Type> combinedMembers = new ArrayList<>();
//        if (this.isCyclic) {
//            combinedMembers.add(this.getBTypeComponent());
//        } else {
//            combinedMembers.addAll(this.members);
//        }
//        combinedMembers.add(other);
        return new BSubTypeData(List.of(this.getBTypeComponent(), other));
    }

    @Override
    public ProperSubTypeData intersect(ProperSubTypeData other) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isSubType(ProperSubTypeData other) {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public BType getBTypeComponent() {
        if (bType == null) {
            // TODO: this is common code factor it
            boolean isReadonly = readonly != null ? readonly : false;
            List<Type> memberList = new ArrayList<>(members);
            BUnionType unionType = switch (typeClass) {
                case BUnionType -> new BUnionType(memberList, isReadonly);
                case BAnyData -> new BAnydataType(new BUnionType(memberList, isReadonly), TypeConstants.ANYDATA_TNAME,
                        isReadonly);
                case BJson ->
                        new BJsonType(new BUnionType(memberList, isReadonly), TypeConstants.JSON_TNAME, isReadonly);
            };
            bType = unionType;
            if (isCyclic) {
                unionType.isCyclic = true;
                unionType.addMembers(cyclicMembers.toArray(new Type[0]));
            }
        }
        return bType;
    }

    @Override
    public BType getBTypeComponent(String name, Module module) {
        if (bType == null) {
            boolean isReadonly = readonly != null ? readonly : false;
            List<Type> memberList = new ArrayList<>(members);
            BUnionType unionType = switch (typeClass) {
                case BUnionType -> new BUnionType(name, module, memberList, isReadonly);
                case BAnyData ->
                        new BAnydataType(new BUnionType(name, module, memberList, isReadonly), name, isReadonly);
                case BJson -> new BJsonType(new BUnionType(name, module, memberList, isReadonly), name, isReadonly);
            };
            bType = unionType;
            if (isCyclic) {
                unionType.isCyclic = true;
                unionType.addMembers(cyclicMembers.toArray(new Type[0]));
            }
        }
        return bType;
    }

    @Override
    public void addCyclicMembers(List<Type> members) {
        // TODO: not sure why we need to reset the cache but we need to do that
        bType = null;
        isCyclic = true;
        cyclicMembers.addAll(members);
    }

    protected void setReadonly(boolean readonly) {
        this.readonly = readonly;
        this.bType = null;
    }

    protected void setBTypeClass(BTypeClass typeClass) {
        this.typeClass = typeClass;
        this.bType = null;
    }

    protected BTypeClass getTypeClass() {
        return typeClass;
    }
}
