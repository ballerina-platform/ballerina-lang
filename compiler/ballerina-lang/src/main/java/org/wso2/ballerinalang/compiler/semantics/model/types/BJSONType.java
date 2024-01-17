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

    private static final int INITIAL_CAPACITY = 8;

    public BJSONType(BJSONType type, boolean nullable) {
        super(type.tsymbol, new LinkedHashSet<>(INITIAL_CAPACITY), nullable,
                Symbols.isFlagOn(type.flags, Flags.READONLY));
        mergeUnionType(type);
        this.tag = TypeTags.JSON;
        this.isCyclic = true;
    }

    public BJSONType(BUnionType type) {
        super(type.tsymbol, new LinkedHashSet<>(INITIAL_CAPACITY), type.isNullable(), Symbols.isFlagOn(type.flags,
                Flags.READONLY));
        mergeUnionType(type);
        this.tag = TypeTags.JSON;
    }

    public BJSONType(BTypeSymbol typeSymbol, boolean nullable, long flags) {
        super(typeSymbol, new LinkedHashSet<>(INITIAL_CAPACITY), nullable, Symbols.isFlagOn(flags, Flags.READONLY));
        this.flags = flags;
        this.tag = TypeTags.JSON;
        this.isCyclic = true;
    }

    @Override
    public String toString() {
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
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
}
