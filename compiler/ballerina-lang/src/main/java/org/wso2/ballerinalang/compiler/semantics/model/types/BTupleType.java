/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.TupleType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@code {@link BTupleType }} represents the tuple type.
 *
 * @since 0.966.0
 */
public class BTupleType extends BType implements TupleType {

    private List<BTupleMember> members;
    private List<BType> memberTypes;
    public BType restType;
    public Boolean isAnyData = null;
    public boolean resolvingToString = false;
    public boolean isCyclic = false;

    private BIntersectionType intersectionType = null;

    public BTupleType(List<BTupleMember> members) {
        super(TypeTags.TUPLE, null);
        this.members = members;
    }

    public BTupleType(BTypeSymbol tsymbol, List<BTupleMember> members) {
        super(TypeTags.TUPLE, tsymbol);
        this.members = members;
    }

    public BTupleType(BTypeSymbol tsymbol, List<BTupleMember> members, boolean isCyclic) {
        super(TypeTags.TUPLE, tsymbol);
        this.members = members;
        this.isCyclic = isCyclic;
    }

    public BTupleType(BTypeSymbol tsymbol, List<BTupleMember> members, BType restType, long flags) {
        super(TypeTags.TUPLE, tsymbol, flags);
        this.members = members;
        this.restType = restType;
    }

    public BTupleType(BTypeSymbol tsymbol, List<BTupleMember> members, BType restType, long flags,
                      boolean isCyclic) {
        super(TypeTags.TUPLE, tsymbol, flags);
        this.members = members;
        this.restType = restType;
        this.isCyclic = isCyclic;
    }

    public BTupleType(BTypeSymbol tsymbol) {
        this(tsymbol, true);
    }

    private BTupleType(BTypeSymbol tsymbol, boolean readonly) {
        super(TypeTags.TUPLE, tsymbol);

        if (readonly) {
            this.flags |= Flags.READONLY;

            if (tsymbol != null) {
                this.tsymbol.flags |= Flags.READONLY;
            }
        }
    }

    @Override
    public List<BType> getTupleTypes() {
        if (memberTypes == null) {
            memberTypes = new ArrayList<>(members.size());
            members.forEach(member -> memberTypes.add(member.type));
        }
        return memberTypes;
    }

    public List<BTupleMember> getMembers() {
        return members;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.TUPLE;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        // This logic is added to prevent duplicate recursive calls to toString
        if (this.resolvingToString) {
            if (tsymbol != null && !tsymbol.getName().getValue().isEmpty()) {
                return this.tsymbol.toString();
            }
            return "...";
        }
        this.resolvingToString = true;

        String stringRep = "[" + members.stream().map(BTupleMember::toString).collect(Collectors.joining(","))
                + ((restType != null) ? (members.size() > 0 ? "," : "") + restType.toString() + "...]" : "]");

        this.resolvingToString = false;
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? stringRep : stringRep.concat(" & readonly");
    }

    @Override
    public Optional<BIntersectionType> getIntersectionType() {
        return Optional.ofNullable(this.intersectionType);
    }

    @Override
    public void setIntersectionType(BIntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }

    // In the case of a cyclic tuple, this aids in
    //adding resolved members to a previously defined empty tuple shell in main scope
    public boolean addMembers(BTupleMember member) {
        // Prevent cyclic types of same type ex: type Foo [int, Foo];
        if (member.type instanceof BTupleType && ((BTupleType) member.type).isCyclic &&
                member.type.getQualifiedTypeName().equals(this.getQualifiedTypeName())) {
            return false;
        }
        this.members.add(member);
        if (this.memberTypes != null) {
            this.memberTypes.add(member.type);
        }
        if (Symbols.isFlagOn(this.flags, Flags.READONLY) && !Symbols.isFlagOn(member.type.flags, Flags.READONLY)) {
            this.flags ^= Flags.READONLY;
        }
        setCyclicFlag(member.type);
        return true;
    }

    // In the case of a cyclic tuple, this aids in
    // adding rest type of resolved node to a previously defined
    // empty tuple shell in main scope
    public boolean addRestType(BType restType) {
        if (restType != null && restType instanceof BTupleType && ((BTupleType) restType).isCyclic &&
                restType.getQualifiedTypeName().equals(this.getQualifiedTypeName()) && this.members.isEmpty()) {
            return false;
        }
        this.restType = restType;
        if (Symbols.isFlagOn(this.flags, Flags.READONLY) && !Symbols.isFlagOn(restType.flags, Flags.READONLY)) {
            this.flags ^= Flags.READONLY;
        }
        setCyclicFlag(restType);
        return true;
    }

    public void setMembers(List<BTupleMember> members) {
        assert members.size() == 0;
        this.memberTypes = null;
        this.members = members;
    }

    private void setCyclicFlag(BType type) {
        if (isCyclic) {
            return;
        }

        if (type instanceof BArrayType) {
            BArrayType arrayType = (BArrayType) type;
            if (arrayType.eType == this) {
                isCyclic = true;
            }
        }

        if (type instanceof BMapType) {
            BMapType mapType = (BMapType) type;
            if (mapType.constraint == this) {
                isCyclic = true;
            }
        }

        if (type instanceof BTableType) {
            BTableType tableType = (BTableType) type;
            if (tableType.constraint == this) {
                isCyclic = true;
            }

            if (tableType.constraint instanceof BMapType) {
                BMapType mapType = (BMapType) tableType.constraint;
                if (mapType.constraint == this) {
                    isCyclic = true;
                }
            }
        }
    }
}
