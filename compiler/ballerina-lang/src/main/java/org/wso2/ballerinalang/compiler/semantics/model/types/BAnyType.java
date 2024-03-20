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
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import static io.ballerina.types.PredefinedType.IMPLEMENTED_TYPES;
import static io.ballerina.types.PredefinedType.VAL_READONLY;

/**
 * @since 0.94
 */
public class BAnyType extends BBuiltInRefType implements SelectivelyImmutableReferenceType {

    public BAnyType(BTypeSymbol tsymbol) {
        this(tsymbol, VAL_READONLY);
    }

    private BAnyType(BTypeSymbol tsymbol, SemType semType) {
        super(TypeTags.ANY, tsymbol, semType);
    }

    public BAnyType(BTypeSymbol tsymbol, Name name, long flag) {
        this(tsymbol, name, flag, VAL_READONLY);
    }

    public BAnyType(BTypeSymbol tsymbol, Name name, long flags, SemType semType) {
        super(TypeTags.ANY, tsymbol, VAL_READONLY);
        this.name = name;
        this.flags = flags;
    }

    public static BAnyType newNilLiftedBAnyType(BTypeSymbol tsymbol) {
        return new BAnyType(tsymbol, Core.diff(VAL_READONLY, PredefinedType.NIL));
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
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
        return !Symbols.isFlagOn(flags, Flags.READONLY) ? getKind().typeName() :
                getKind().typeName().concat(" & readonly");
    }

    @Override
    public SemType semType() {
        SemType implementedAnyType = Core.intersect(PredefinedType.ANY, IMPLEMENTED_TYPES);
        if (Symbols.isFlagOn(flags, Flags.READONLY)) {
            return Core.intersect(implementedAnyType, VAL_READONLY);
        }
        return implementedAnyType;
    }
}
