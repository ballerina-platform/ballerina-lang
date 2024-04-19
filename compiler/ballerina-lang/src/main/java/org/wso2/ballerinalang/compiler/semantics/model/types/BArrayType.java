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
import io.ballerina.types.definition.ListDefinition;
import org.ballerinalang.model.types.ArrayType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;

import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.types.PredefinedType.ANY;
import static io.ballerina.types.PredefinedType.NEVER;

/**
 * @since 0.94
 */
public class BArrayType extends BType implements ArrayType {

    private static final int NO_FIXED_SIZE = -1;
    public BType eType;

    public int size = NO_FIXED_SIZE;

    public BArrayState state = BArrayState.OPEN;

    public BArrayType mutableType;
    public final Env env;
    private ListDefinition ld = null;

    public BArrayType(Env env, BType elementType) {
        super(TypeTags.ARRAY, null);
        this.eType = elementType;
        this.env = env;
    }

    public BArrayType(Env env, BType elementType, BTypeSymbol tsymbol) {
        super(TypeTags.ARRAY, tsymbol);
        this.eType = elementType;
        this.env = env;
    }

    public BArrayType(Env env, BType elementType, BTypeSymbol tsymbol, int size, BArrayState state) {
        super(TypeTags.ARRAY, tsymbol);
        this.eType = elementType;
        this.size = size;
        this.state = state;
        this.env = env;
    }

    public BArrayType(Env env, BType elementType, BTypeSymbol tsymbol, int size, BArrayState state, long flags) {
        super(TypeTags.ARRAY, tsymbol, flags);
        this.eType = elementType;
        this.size = size;
        this.state = state;
        this.env = env;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public BType getElementType() {
        return eType;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ARRAY;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(eType.toString());
        String tempSize = (state == BArrayState.INFERRED) ? "*" : String.valueOf(size);
        if (eType.tag == TypeTags.ARRAY) {
            if (state != BArrayState.OPEN) {
                sb.insert(sb.indexOf("["), "[" + tempSize + "]");
            } else {
                sb.insert(sb.indexOf("["), "[]");
            }
        } else {
            if (state != BArrayState.OPEN) {
                sb.append("[").append(tempSize).append("]");
            } else {
                sb.append("[]");
            }
        }
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? sb.toString() : sb.append(" & readonly").toString();
    }

    private boolean hasTypeHoles() {
        return eType instanceof BNoType;
    }

    // If the element type has a semtype component then it will be represented by that component otherwise with never.
    // This means we depend on properly partitioning types to semtype components. Also, we need to ensure member types
    // are "ready" when we call this
    @Override
    public SemType semType() {
        if (ld != null) {
            return ld.getSemType(env);
        }
        ld = new ListDefinition();
        if (hasTypeHoles()) {
            return ld.defineListTypeWrapped(env, ANY);
        }
        SemType elementTypeSemType = eType.semType();
        if (elementTypeSemType == null) {
            elementTypeSemType = NEVER;
        }
        boolean isReadonly = Symbols.isFlagOn(flags, Flags.READONLY);
        CellAtomicType.CellMutability mut = isReadonly ? CELL_MUT_NONE : CELL_MUT_LIMITED;
        // Not entirely sure if I understand this correctly,
        //   if size == -1 it means T[]
        //   if size < 0 && not -1 it means T[abs(size)] (and size was inferred)
        //   else it is the fixed size
        if (size != NO_FIXED_SIZE) {
            return ld.defineListTypeWrapped(env, List.of(elementTypeSemType), Math.abs(size), NEVER, mut);
        } else {
            return ld.defineListTypeWrapped(env, List.of(), 0, elementTypeSemType, mut);
        }
    }

    // This is to ensure call to isNullable won't call semType. In case this is a member of a recursive union otherwise
    // this will have an invalid list type since parent union type call this while it is filling its members
    @Override
    public boolean isNullable() {
        return false;
    }
}
