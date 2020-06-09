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
import org.wso2.ballerinalang.util.Flags;

/**
 * {@code BAnydataType} represents the data types in Ballerina.
 * 
 * @since 0.985.0
 */
public class BAnydataType extends BBuiltInRefType implements SelectivelyImmutableReferenceType {

    private boolean nullable = true;
    public BIntersectionType immutableType;

    public BAnydataType(int tag, BTypeSymbol tsymbol) {
        super(tag, tsymbol);
    }

    public BAnydataType(int tag, BTypeSymbol tsymbol, Name name, int flag) {

        super(tag, tsymbol);
        this.name = name;
        this.flags = flag;
    }

    public BAnydataType(int tag, BTypeSymbol tsymbol, boolean nullable) {
        super(tag, tsymbol);
        this.nullable = nullable;
    }

    public BAnydataType(int tag, BTypeSymbol tsymbol, Name name, int flags, boolean nullable) {

        super(tag, tsymbol);
        this.name = name;
        this.flags = flags;
        this.nullable = nullable;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    public boolean isNullable() {
        return nullable;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ANYDATA;
    }

    @Override
    public String toString() {
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
    }

    @Override
    public BIntersectionType getImmutableType() {
        return this.immutableType;
    }
}
