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
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import static io.ballerina.types.PredefinedType.VAL_READONLY;

/**
 * {@code BReadonlyType} represents the shapes that have their read-only bit on.
 * 
 * @since 1.3.0
 */
public class BReadonlyType extends BType {

    private boolean nullable = true;

    public BReadonlyType() {
        this(VAL_READONLY);
    }

    public BReadonlyType(long flag) {
        super(TypeTags.READONLY, null, flag | Flags.READONLY, VAL_READONLY);
    }

    private BReadonlyType(SemType semType) {
        super(TypeTags.READONLY, null, Flags.READONLY, semType);
    }

    public static BReadonlyType newNilLiftedBReadonlyType() {
        BReadonlyType result = new BReadonlyType(Core.diff(VAL_READONLY, PredefinedType.NIL));
        result.nullable = false;
        return result;
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
        return TypeKind.READONLY;
    }
}
