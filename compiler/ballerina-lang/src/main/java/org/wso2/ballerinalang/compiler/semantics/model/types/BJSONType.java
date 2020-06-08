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

import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.util.Flags;

/**
 * @since 0.94
 */
public class BJSONType extends BBuiltInRefType implements SelectivelyImmutableReferenceType {

    private boolean nullable = true;
    public BIntersectionType immutableType;

    public BJSONType(int tag, BTypeSymbol tsymbol) {
        super(tag, tsymbol);
    }

    public BJSONType(int tag, BTypeSymbol tsymbol, boolean nullable) {
        this(tag, tsymbol);
        this.nullable = nullable;
    }

    public BJSONType(int tag, BTypeSymbol tsymbol, boolean nullable, int flags) {
        this(tag, tsymbol);
        this.nullable = nullable;
        this.flags = flags;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public BIntersectionType getImmutableType() {
        return this.immutableType;
    }

    @Override
    public String toString() {
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
    }
}
