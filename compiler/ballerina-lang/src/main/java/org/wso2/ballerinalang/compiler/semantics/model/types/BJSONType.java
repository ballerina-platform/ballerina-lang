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

import io.ballerina.types.Context;
import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.LinkedHashSet;

/**
 * @since 0.94
 */
public class BJSONType extends BUnionType {
    private boolean nullable = true;
    private static final int INITIAL_CAPACITY = 8;
    private final Context typeCtx;

    public BJSONType(Context typeCtx, BUnionType type) {
        super(type.env, type.tsymbol, new LinkedHashSet<>(INITIAL_CAPACITY), Symbols.isFlagOn(type.getFlags(),
                Flags.READONLY));
        mergeUnionType(type);
        this.tag = TypeTags.JSON;
        this.isCyclic = true;
        this.typeCtx = typeCtx;
    }

    private BJSONType(Context typeCtx, BTypeSymbol typeSymbol, boolean nullable, long flags) {
        super(typeCtx.env, typeSymbol, new LinkedHashSet<>(INITIAL_CAPACITY), Symbols.isFlagOn(flags, Flags.READONLY));
        this.setFlags(flags);
        this.tag = TypeTags.JSON;
        this.isCyclic = true;
        this.nullable = nullable;
        this.typeCtx = typeCtx;
    }

    public static BJSONType newNilLiftedBJSONType(BJSONType type) {
        BJSONType result = new BJSONType(type.typeCtx, type);
        result.nullable = false;
        return result;
    }

    public static BJSONType newImmutableBJSONType(BJSONType type, BTypeSymbol typeSymbol, boolean nullable) {
        return new BJSONType(type.typeCtx, typeSymbol, nullable, type.getFlags() | Flags.READONLY);
    }

    @Override
    public String toString() {
        return !Symbols.isFlagOn(getFlags(), Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.JSON;
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
    public SemType semType() {
        SemType json = Core.createJson(typeCtx);
        // TODO: refer to https://github.com/ballerina-platform/ballerina-lang/issues/43343#issuecomment-2485247172
//        if (!nullable) {
//            json = Core.diff(json, PredefinedType.NIL);
//        }
        if (Symbols.isFlagOn(getFlags(), Flags.READONLY)) {
            json = Core.intersect(json, PredefinedType.VAL_READONLY);
        }
        return json;
    }
}
