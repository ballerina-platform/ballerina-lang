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

import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import static io.ballerina.types.PredefinedType.ANY;
import static io.ballerina.types.PredefinedType.VAL_READONLY;

/**
 * @since 0.94
 */
public class BAnyType extends BType implements SelectivelyImmutableReferenceType {

    private boolean nullable = true;

    public BAnyType() {
        this(ANY);
    }

    public BAnyType(Name name, long flag) {
        this(name, flag, Symbols.isFlagOn(flag, Flags.READONLY) ? Core.intersect(ANY, VAL_READONLY) : ANY);
    }

    private BAnyType(Name name, long flags, SemType semType) {
        super(TypeTags.ANY, null, semType);
        this.name = name;
        this.setFlags(flags);
    }

    private BAnyType(SemType semType) {
        super(TypeTags.ANY, null, semType);
    }

    public static BAnyType newNilLiftedBAnyType() {
        BAnyType result = new BAnyType(Core.diff(ANY, PredefinedType.NIL));
        result.nullable = false;
        return result;
    }

    public static BAnyType newImmutableBAnyType() {
        return new BAnyType(Types.getImmutableTypeName(TypeKind.ANY.typeName()), Flags.READONLY);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ANY;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return !Symbols.isFlagOn(getFlags(), Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
    }
}
