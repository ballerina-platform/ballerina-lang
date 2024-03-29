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

import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Env;
import io.ballerina.types.SemType;
import io.ballerina.types.definition.ListDefinition;
import org.ballerinalang.model.types.TupleType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.types.PredefinedType.ANY;
import static io.ballerina.types.PredefinedType.NEVER;

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

    public BTupleType mutableType;
    public final Env env;
    private ListDefinition ld = null;

    public BTupleType(Env env, List<BTupleMember> members) {
        super(TypeTags.TUPLE, null);
        this.members = members;
        this.env = env;
    }

    public BTupleType(Env env, BTypeSymbol tsymbol, List<BTupleMember> members) {
        super(TypeTags.TUPLE, tsymbol);
        this.members = members;
        this.env = env;
    }

    public BTupleType(Env env, BTypeSymbol tsymbol, List<BTupleMember> members, boolean isCyclic) {
        super(TypeTags.TUPLE, tsymbol);
        this.members = members;
        this.isCyclic = isCyclic;
        this.env = env;
    }

    public BTupleType(Env env, BTypeSymbol tsymbol, List<BTupleMember> members, BType restType, long flags) {
        super(TypeTags.TUPLE, tsymbol, flags);
        this.members = members;
        this.restType = restType;
        this.env = env;
    }

    public BTupleType(Env env, BTypeSymbol tsymbol, List<BTupleMember> members, BType restType, long flags,
                      boolean isCyclic) {
        super(TypeTags.TUPLE, tsymbol, flags);
        this.members = members;
        this.restType = restType;
        this.isCyclic = isCyclic;
        this.env = env;
    }

    public BTupleType(Env env, BTypeSymbol tsymbol) {
        this(env, tsymbol, true);
    }

    private BTupleType(Env env, BTypeSymbol tsymbol, boolean readonly) {
        super(TypeTags.TUPLE, tsymbol);

        if (readonly) {
            this.flags |= Flags.READONLY;

            if (tsymbol != null) {
                this.tsymbol.flags |= Flags.READONLY;
            }
        }
        this.env = env;
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

    // In the case of a cyclic tuple, this aids in
    //adding resolved members to a previously defined empty tuple shell in main scope
    public boolean addMembers(BTupleMember member) {
        ld = null;
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
        ld = null;
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
        ld = null;
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

    // This is to ensure call to isNullable won't call semType. In case this is a member of a recursive union otherwise
    // this will have an invalid list type since parent union type call this while it is filling its members
    @Override
    public boolean isNullable() {
        return false;
    }

    private boolean hasTypeHoles() {
        if (members != null) {
            for (BTupleMember member : members) {
                if (member.type instanceof BNoType) {
                    return true;
                }
            }
        }
        if (restType != null) {
            return restType instanceof BNoType;
        }
        return false;
    }

    // If the member has a semtype component then it will be represented by that component otherwise with never. This
    // means we depend on properly partitioning types to semtype components. Also, we need to ensure member types are
    // "ready" when we call this
    @Override
    public SemType semType() {
        if (ld != null) {
            return ld.getSemType(env);
        }
        ld = new ListDefinition();
        if (hasTypeHoles()) {
            return ld.resolve(env, ANY);
        }
        boolean isReadonly = Symbols.isFlagOn(flags, Flags.READONLY);
        CellAtomicType.CellMutability mut = isReadonly ? CELL_MUT_NONE : CELL_MUT_LIMITED;
        if (members == null) {
            if (restType == null) {
                throw new IllegalStateException("Both members and rest type can't be null");
            }
            SemType restSemType = restType.semType();
            return ld.resolve(env, List.of(), 0, Objects.requireNonNullElse(restSemType, NEVER), mut);
        }
        List<SemType> memberSemTypes = new ArrayList<>(members.size());
        for (BTupleMember member : members) {
            BType memberType = member.type;
            SemType semType = memberType.semType();
            if (semType == null) {
                semType = NEVER;
            }
            memberSemTypes.add(semType);
        }
        SemType restSemType = restType != null ? restType.semType() : NEVER;
        if (restSemType == null) {
            restSemType = NEVER;
        }
        return ld.resolve(env, memberSemTypes, memberSemTypes.size(), restSemType, mut);
    }
}
