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

import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
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
public class BAnydataType extends BUnionType implements SelectivelyImmutableReferenceType {

    public BAnydataType(BTypeSymbol tsymbol, Name name, int flags) {
        this(tsymbol, name, flags, true);
    }

    public BAnydataType(BTypeSymbol tsymbol, boolean nullable) {
        super(tsymbol, new LinkedHashSet<>(), nullable, false);
        this.tag = TypeTags.ANYDATA;
        if (Symbols.isFlagOn(flags, Flags.READONLY)) {
            System.out.println("#### KRV 56");
        }
    }

    public BAnydataType(BTypeSymbol tsymbol, Name name, int flags, boolean nullable) {
        super(tsymbol, new LinkedHashSet<>(), nullable, false);
        this.tag = TypeTags.ANYDATA;
        this.name = name;
        this.flags = flags;
        if (Symbols.isFlagOn(flags, Flags.READONLY)) {
            System.out.println("#### KRV 66");
        }
    }

    public BAnydataType(BUnionType type) {
        super(type.tsymbol, new LinkedHashSet<>(type.getMemberTypes()), type.isNullable(), Symbols.isFlagOn(type.flags,
                Flags.READONLY));
        this.immutableType = type.immutableType;
        this.tag = TypeTags.ANYDATA;
        if (Symbols.isFlagOn(flags, Flags.READONLY)) {
            System.out.println("#### KRV 74");
        }
    }

    public BAnydataType(BAnydataType type, boolean nullable) {
        super(type.tsymbol, type.getMemberTypes(), nullable, Symbols.isFlagOn(type.flags, Flags.READONLY));
        this.tag = TypeTags.ANYDATA;
        if (Symbols.isFlagOn(flags, Flags.READONLY)) {
            System.out.println("#### KRV 77");
        }
    }

    @Override
    public String toString() {
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ANYDATA;
    }

}
