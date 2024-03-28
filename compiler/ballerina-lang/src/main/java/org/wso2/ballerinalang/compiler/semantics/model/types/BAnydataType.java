/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.model.types;

import io.ballerina.types.Env;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.LinkedHashSet;

/**
 * {@code BAnydataType} represents the data types in Ballerina.
 * 
 * @since 0.985.0
 */
public class BAnydataType extends BUnionType {
    private boolean nullable;
    private static final int INITIAL_CAPACITY = 10;

    public BAnydataType(Env env, BTypeSymbol tsymbol, Name name, long flags, boolean nullable) {
        super(env, tsymbol, new LinkedHashSet<>(INITIAL_CAPACITY), nullable, false);
        this.tag = TypeTags.ANYDATA;
        this.flags = flags;
        this.name = name;
        this.isCyclic = true;
        this.nullable = nullable;
    }

    public BAnydataType(BUnionType type) {
        super(type.env, type.tsymbol, new LinkedHashSet<>(type.memberTypes.size()), type.isNullable(),
                Symbols.isFlagOn(type.flags, Flags.READONLY));
        this.tag = TypeTags.ANYDATA;
        this.isCyclic = true;
        this.name = type.name;
        this.flags = type.flags;
        this.nullable = type.isNullable();
        mergeUnionType(type);
    }

    public BAnydataType(BAnydataType type, boolean nullable) {
        super(type.env, type.tsymbol, new LinkedHashSet<>(INITIAL_CAPACITY), nullable,
                Symbols.isFlagOn(type.flags, Flags.READONLY));
        this.flags = type.flags;
        this.tag = TypeTags.ANYDATA;
        this.isCyclic = true;
        this.nullable = nullable;
        mergeUnionType(type);
    }

    @Override
    public String toString() {
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ANYDATA;
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
