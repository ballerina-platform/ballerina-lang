/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import static io.ballerina.types.PredefinedType.VAL_READONLY;

/**
 * {@code BReadonlyType} represents the shapes that have their read-only bit on.
 * 
 * @since 1.3.0
 */
public class BReadonlyType extends BBuiltInRefType {

    public BReadonlyType(BTypeSymbol tsymbol) {
        this(tsymbol, VAL_READONLY);
    }

    private BReadonlyType(BTypeSymbol tsymbol, SemType semType) {
        super(TypeTags.READONLY, tsymbol, semType);
        this.flags |= Flags.READONLY;
    }

    public BReadonlyType(BTypeSymbol tsymbol, Name name, long flag) {
        super(TypeTags.READONLY, tsymbol, VAL_READONLY);
        this.name = name;
        this.flags = flag;
        this.flags |= Flags.READONLY;
    }

    public static BReadonlyType newNilLiftedBReadonlyType(BTypeSymbol tsymbol) {
        return new BReadonlyType(tsymbol, Core.diff(VAL_READONLY, PredefinedType.NIL));
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.READONLY;
    }
}
