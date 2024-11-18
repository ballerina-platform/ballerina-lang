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

import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import org.ballerinalang.model.types.IntersectionType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.Collections;
import java.util.LinkedHashSet;
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

    public BIntersectionType(BTypeSymbol tsymbol, LinkedHashSet<BType> types,
                             BType effectiveType) {
        super(TypeTags.INTERSECTION, tsymbol);
        this.constituentTypes = toFlatTypeSet(types);

        for (BType constituentType : this.constituentTypes) {
            if (constituentType.tag == TypeTags.READONLY) {
                this.addFlags(Flags.READONLY);
                break;
            }
        }
        this.effectiveType = effectiveType;
    }

    public BIntersectionType(BTypeSymbol tsymbol) {
        super(TypeTags.INTERSECTION, tsymbol);
    }

    public BIntersectionType(BTypeSymbol tsymbol, LinkedHashSet<BType> types, BType effectiveType,
                             long flags) {
        super(TypeTags.INTERSECTION, tsymbol, flags);
        this.constituentTypes = toFlatTypeSet(types);
        this.effectiveType = effectiveType;
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
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    public void setConstituentTypes(LinkedHashSet<BType> constituentTypes) {
        this.constituentTypes = toFlatTypeSet(constituentTypes);
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

    /**
     * When the type is mutated we need to reset resolved semType.
     */
    public void resetSemType() {
        this.semType = null;
    }

    @Override
    public SemType semType() {
        // We have to recalculate this everytime since the actual BTypes inside constituent types do mutate and we
        // can't detect those mutations.
        return computeResultantIntersection();
    }

    private SemType computeResultantIntersection() {
        SemType t = PredefinedType.VAL;
        for (BType constituentType : this.getConstituentTypes()) {
            t = SemTypes.intersect(t, constituentType.semType());
        }

        // TODO: this is a temporary workaround to propagate effective typeIds
        BType referredType = Types.getReferredType(this.effectiveType);
        if (referredType instanceof BErrorType effErr) {
            t = effErr.distinctIdWrapper(t);
        } else if (referredType instanceof BObjectType effObj) {
            t = effObj.distinctIdWrapper(t);
        }
        return t;
    }
}
