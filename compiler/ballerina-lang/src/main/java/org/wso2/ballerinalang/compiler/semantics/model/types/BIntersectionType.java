/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.types.IntersectableReferenceType;
import org.ballerinalang.model.types.IntersectionType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

/**
 * {@code BIntersectionType} represents an intersection type in Ballerina.
 *
 * @since 2.0.0
 */
public class BIntersectionType extends BType implements IntersectionType {

    public BType effectiveType;

    private LinkedHashSet<BType> constituentTypes;
    private BIntersectionType intersectionType;

    public BIntersectionType(BTypeSymbol tsymbol, LinkedHashSet<BType> types,
                             IntersectableReferenceType effectiveType) {
        super(TypeTags.INTERSECTION, tsymbol);
        this.constituentTypes = toFlatTypeSet(types);
        this.effectiveType = (BType) effectiveType;

        for (BType constituentType : this.constituentTypes) {
            if (constituentType.tag == TypeTags.READONLY) {
                this.flags |= Flags.READONLY;
                break;
            }
        }
        effectiveType.setIntersectionType(this);
    }

    public BIntersectionType(BTypeSymbol tsymbol, LinkedHashSet<BType> types, IntersectableReferenceType effectiveType,
                             long flags) {
        super(TypeTags.INTERSECTION, tsymbol, flags);
        this.constituentTypes = toFlatTypeSet(types);
        this.effectiveType = (BType) effectiveType;
        effectiveType.setIntersectionType(this);
    }

    @Override
    public Set<BType> getConstituentTypes() {
        return Collections.unmodifiableSet(this.constituentTypes);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.INTERSECTION;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isNullable() {
        return this.effectiveType.isNullable();
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    public void setConstituentTypes(LinkedHashSet<BType> constituentTypes) {
        this.constituentTypes =  toFlatTypeSet(constituentTypes);
    }

    @Override
    public String toString() {
        Name name = this.tsymbol.name;
        if (!Symbols.isFlagOn(this.tsymbol.flags, Flags.ANONYMOUS) && !name.value.isEmpty()) {
            return name.value;
        }

        StringJoiner joiner = new StringJoiner(" & ", "(", ")");

        for (BType constituentType : this.constituentTypes) {
            if (constituentType.tag == TypeTags.NIL) {
                joiner.add("()");
                continue;
            }

            joiner.add(constituentType.toString());
        }

        return joiner.toString();
    }

    private static LinkedHashSet<BType> toFlatTypeSet(LinkedHashSet<BType> types) {
        LinkedHashSet<BType> flatSet = new LinkedHashSet<>();

        for (BType type : types) {
            if (type.tag != TypeTags.INTERSECTION) {
                flatSet.add(type);
                continue;
            }

            flatSet.addAll(((BIntersectionType) type).constituentTypes);
        }

        return flatSet;
    }

    public BType getEffectiveType() {
        return this.effectiveType;
    }

    @Override
    public BIntersectionType getImmutableType() {
        return Symbols.isFlagOn(this.flags, Flags.READONLY) ? this : null;
    }

    @Override
    public Optional<BIntersectionType> getIntersectionType() {
        return Optional.ofNullable(this.intersectionType);
    }

    @Override
    public void setIntersectionType(BIntersectionType intersectionType) {
        this.intersectionType = intersectionType;
    }
}
