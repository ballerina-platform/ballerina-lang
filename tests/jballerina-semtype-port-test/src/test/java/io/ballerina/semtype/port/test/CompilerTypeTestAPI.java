/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.semtype.port.test;

import io.ballerina.types.BasicTypeBitSet;
import io.ballerina.types.Context;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;

public final class CompilerTypeTestAPI implements TypeTestAPI<SemType> {

    private static final CompilerTypeTestAPI INSTANCE = new CompilerTypeTestAPI();

    private CompilerTypeTestAPI() {
    }

    public static CompilerTypeTestAPI getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isSubtype(TypeTestContext<SemType> cx, SemType t1, SemType t2) {
        return SemTypes.isSubtype(form(cx), t1, t2);
    }

    private static Context form(TypeTestContext<SemType> cx) {
        return (Context) cx.getInnerContext();
    }

    @Override
    public boolean isSubtypeSimple(SemType t1, SemType t2) {
        return SemTypes.isSubtypeSimple(t1, (BasicTypeBitSet) t2);
    }

    @Override
    public boolean isListType(SemType t) {
        return SemTypes.isSubtypeSimple(t, PredefinedType.LIST);
    }

    @Override
    public boolean isMapType(SemType t) {
        return SemTypes.isSubtypeSimple(t, PredefinedType.MAPPING);
    }

    @Override
    public SemType intConst(long l) {
        return SemTypes.intConst(l);
    }

    @Override
    public SemType mappingMemberTypeInnerVal(TypeTestContext<SemType> context, SemType semType, SemType m) {
        return SemTypes.mappingMemberTypeInnerVal((Context) context.getInnerContext(), semType, m);
    }

    @Override
    public SemType listProj(TypeTestContext<SemType> context, SemType t, SemType key) {
        return SemTypes.listProj((Context) context.getInnerContext(), t, key);
    }

    @Override
    public SemType listMemberType(TypeTestContext<SemType> context, SemType t, SemType key) {
        return SemTypes.listMemberType((Context) context.getInnerContext(), t, key);
    }
}
