/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import io.ballerina.types.Core;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import io.ballerina.types.definition.Member;
import io.ballerina.types.definition.ObjectDefinition;
import io.ballerina.types.definition.ObjectQualifiers;
import org.ballerinalang.model.types.ObjectType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * {@code BObjectType} represents object type in Ballerina.
 *
 * @since 0.971.0
 */
public class BObjectType extends BStructureType implements ObjectType {
    private static final String OBJECT = "object";
    private static final String SPACE = " ";
    private static final String PUBLIC = "public";
    private static final String PRIVATE = "private";
    private static final String LEFT_CURL = "{";
    private static final String RIGHT_CURL = "}";
    private static final String SEMI_COLON = ";";
    private static final String READONLY = "readonly";
    public final Env env;
    public boolean markedIsolatedness;

    public BObjectType mutableType = null;
    public BLangClassDefinition classDef = null;

    public BTypeIdSet typeIdSet = new BTypeIdSet();

    private ObjectDefinition od = null;
    private final DistinctIdSupplier distinctIdSupplier;

    public BObjectType(Env env, BTypeSymbol tSymbol) {
        super(TypeTags.OBJECT, tSymbol);
        assert env != null;
        this.env = env;
        this.distinctIdSupplier = new DistinctIdSupplier(env);
    }

    public BObjectType(Env env, BTypeSymbol tSymbol, long flags) {
        super(TypeTags.OBJECT, tSymbol, flags);
        assert env != null;
        this.env = env;
        this.distinctIdSupplier = new DistinctIdSupplier(env);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.OBJECT;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    private boolean hasTypeHoles() {
        return fields.values().stream().anyMatch(field -> field.type instanceof BNoType);
    }

    @Override
    public SemType semType() {
        return distinctIdSupplier.get().stream().map(SemTypes::objectDistinct).reduce(semTypeInner(), Core::intersect);
    }

    private SemType semTypeInner() {
        if (od != null) {
            return od.getSemType(env);
        }
        od = new ObjectDefinition();
        // I don't think this is actually possible
        assert !hasTypeHoles() : "unimplemented";
        List<Member> members = new ArrayList<>(fields.size());
        ObjectQualifiers qualifiers = getObjectQualifiers();
        Set<String> memberNames = new HashSet<>();
        for (BField field : fields.values()) {
            Optional<Member> member = createMember(field, qualifiers.readonly(), memberNames);
            member.ifPresent(members::add);
        }

        BObjectTypeSymbol objectSymbol = (BObjectTypeSymbol) this.tsymbol;
        for (BAttachedFunction fun : objectSymbol.attachedFuncs) {
            Optional<Member> member = createMember(fun, memberNames);
            member.ifPresent(members::add);
        }
        return od.define(env, qualifiers, members);
    }

    private static Optional<Member> createMember(BAttachedFunction func, Set<String> visitedFields) {
        String name = func.funcName.value;
        if (visitedFields.contains(name)) {
            return Optional.empty();
        }
        visitedFields.add(name);
        Member.Visibility visibility = Symbols.isFlagOn(func.symbol.flags, Flags.PUBLIC) ?
                Member.Visibility.Public : Member.Visibility.Private;
        SemType type = func.type.semType();
        assert type != null : "function type is fully implemented";
        assert !Core.isNever(type) : "method can't be never";
        return Optional.of(new Member(name, type, Member.Kind.Method, visibility, true));
    }

    private static Optional<Member> createMember(BField field, boolean readonlyObject, Set<String> visitedFields) {
        String name = field.name.value;
        if (visitedFields.contains(name)) {
            return Optional.empty();
        }
        visitedFields.add(name);
        Member.Visibility visibility = Symbols.isFlagOn(field.symbol.flags, Flags.PUBLIC) ?
                Member.Visibility.Public : Member.Visibility.Private;
        SemType type = field.type.semType();
        if (type == null) {
            type = PredefinedType.NEVER;
        }
        boolean immutableField;
        if (readonlyObject || Symbols.isFlagOn(field.symbol.flags, Flags.READONLY)) {
            type = Core.intersect(type, PredefinedType.VAL_READONLY);
            immutableField = true;
        } else {
            immutableField = false;
        }
        return Optional.of(new Member(name, type, Member.Kind.Field, visibility, immutableField));
    }

    private ObjectQualifiers getObjectQualifiers() {
        long flags = tsymbol.flags;
        boolean isolated = Symbols.isFlagOn(this.tsymbol.flags, Flags.ISOLATED);
        ObjectQualifiers.NetworkQualifier networkQualifier;
        if (Symbols.isFlagOn(flags, Flags.SERVICE)) {
            networkQualifier = ObjectQualifiers.NetworkQualifier.Service;
        } else if (Symbols.isFlagOn(flags, Flags.CLIENT)) {
            networkQualifier = ObjectQualifiers.NetworkQualifier.Client;
        } else {
            networkQualifier = ObjectQualifiers.NetworkQualifier.None;
        }
        boolean readonly = Symbols.isFlagOn(this.tsymbol.flags, Flags.READONLY);
        return new ObjectQualifiers(isolated, readonly, networkQualifier);
    }

    @Override
    public String toString() {
        if (shouldPrintShape()) {
            StringBuilder sb = new StringBuilder();

            var symbolFlags = tsymbol.flags;
            if (Symbols.isFlagOn(symbolFlags, Flags.ISOLATED)) {
                sb.append("isolated ");
            }

            sb.append(OBJECT).append(SPACE).append(LEFT_CURL);
            for (BField field : fields.values()) {
                var flags = field.symbol.flags;
                if (Symbols.isFlagOn(flags, Flags.PUBLIC)) {
                    sb.append(SPACE).append(PUBLIC);
                } else if (Symbols.isFlagOn(flags, Flags.PRIVATE)) {
                    sb.append(SPACE).append(PRIVATE);
                }

                if (Symbols.isFlagOn(flags, Flags.FINAL)) {
                    sb.append(SPACE).append("final");
                }

                sb.append(SPACE).append(field.type).append(SPACE).append(field.name).append(";");
            }
            BObjectTypeSymbol objectSymbol = (BObjectTypeSymbol) this.tsymbol;
            for (BAttachedFunction fun : objectSymbol.attachedFuncs) {
                if (!Symbols.isResource(fun.symbol)) {
                    if (Symbols.isFlagOn(fun.symbol.flags, Flags.PUBLIC)) {
                        sb.append(SPACE).append(PUBLIC);
                    } else if (Symbols.isFlagOn(fun.symbol.flags, Flags.PRIVATE)) {
                        sb.append(SPACE).append(PRIVATE);
                    }
                }

                sb.append(SPACE).append(fun).append(SEMI_COLON);
            }
            sb.append(SPACE).append(RIGHT_CURL);

            if (Symbols.isFlagOn(symbolFlags, Flags.READONLY)) {
                sb.append(" & readonly");
            }

            return sb.toString();
        }
        return this.tsymbol.toString();
    }

    // This is to ensure call to isNullable won't call semType. In case this is a member of a recursive union otherwise
    // this will have an invalid object type since parent union type call this while it is filling its members
    @Override
    public boolean isNullable() {
        return false;
    }

    private final class DistinctIdSupplier implements Supplier<List<Integer>> {

        private List<Integer> ids = null;
        private static final Map<BTypeIdSet.BTypeId, Integer> allocatedIds = new ConcurrentHashMap<>();
        private final Env env;

        private DistinctIdSupplier(Env env) {
            this.env = env;
        }

        public synchronized List<Integer> get() {
            if (ids != null) {
                return ids;
            }
            ids = typeIdSet.getAll().stream()
                    .map(each -> allocatedIds.computeIfAbsent(each, (key) -> env.distinctAtomCountGetAndIncrement()))
                    .toList();
            return ids;
        }
    }
}
