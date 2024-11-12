/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Env;
import io.ballerina.types.SemType;
import io.ballerina.types.definition.MappingDefinition;
import org.ballerinalang.model.types.ConstrainedType;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;

import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.PredefinedType.VAL;

/**
 * @since 0.94
 */
public class BMapType extends BType implements ConstrainedType, SelectivelyImmutableReferenceType {
    public BType constraint;
    public BMapType mutableType;

    public final Env env;
    private MappingDefinition md = null;

    public BMapType(Env env, int tag, BType constraint, BTypeSymbol tsymbol) {
        super(tag, tsymbol);
        this.constraint = constraint;
        this.env = env;
    }

    public BMapType(Env env, int tag, BType constraint, BTypeSymbol tsymbol, long flags) {
        super(tag, tsymbol, flags);
        this.constraint = constraint;
        this.env = env;
    }

    /**
     * It is required to reset {@link #md} when the type gets mutated.
     * This method is used for that. e.g. When changing Flags.READONLY
     */
    protected void restMd() {
        md = null;
    }

    @Override
    public BType getConstraint() {
        return constraint;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.MAP;
    }

    @Override
    public String toString() {
        String stringRep;

        if (constraint.tag == TypeTags.ANY) {
            stringRep = super.toString();
        } else {
            stringRep = super.toString() + "<" + constraint + ">";
        }

        return !Symbols.isFlagOn(getFlags(), Flags.READONLY) ? stringRep : stringRep.concat(" & readonly");
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    private boolean hasTypeHoles() {
        return constraint instanceof BNoType;
    }

    /**
     * When the type is mutated we need to reset the definition used for the semType.
     */
    @Override
    public void resetSemType() {
        md = null;
    }

    // If the member has a semtype component then it will be represented by that component otherwise with never. This
    // means we depend on properly partitioning types to semtype components. Also, we need to ensure member types are
    // "ready" when we call this
    @Override
    public SemType semType() {
        if (md != null) {
            return md.getSemType(env);
        }
        md = new MappingDefinition();
        if (hasTypeHoles()) {
            return md.defineMappingTypeWrapped(env, List.of(), VAL);
        }
        SemType elementTypeSemType = constraint.semType();
        if (elementTypeSemType == null) {
            elementTypeSemType = NEVER;
        }
        boolean isReadonly = Symbols.isFlagOn(getFlags(), Flags.READONLY);
        CellAtomicType.CellMutability mut = isReadonly ? CELL_MUT_NONE : CELL_MUT_LIMITED;
        return md.defineMappingTypeWrapped(env, List.of(), elementTypeSemType, mut);
    }

    // This is to ensure call to isNullable won't call semType. In case this is a member of a recursive union otherwise
    // this will have an invalid map type since parent union type call this while it is filling its members
    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public void setFlags(long flags) {
        super.setFlags(flags);
        restMd();
    }

    @Override
    public void addFlags(long flags) {
        super.addFlags(flags);
        restMd();
    }
}
