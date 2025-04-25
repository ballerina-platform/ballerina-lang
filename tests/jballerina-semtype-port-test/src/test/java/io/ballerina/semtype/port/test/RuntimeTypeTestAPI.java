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

import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Context;
import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.ListProj;
import io.ballerina.runtime.api.types.semtype.MappingProj;
import io.ballerina.runtime.api.types.semtype.SemType;

public class RuntimeTypeTestAPI implements TypeTestAPI<SemType> {

    private static final RuntimeTypeTestAPI INSTANCE = new RuntimeTypeTestAPI();

    private RuntimeTypeTestAPI() {
    }

    public static RuntimeTypeTestAPI getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isSubtype(TypeTestContext<SemType> cx, SemType t1, SemType t2) {
        return Core.isSubType(from(cx), t1, t2);
    }

    private static Context from(TypeTestContext<SemType> cx) {
        return (Context) cx.getInnerContext();
    }

    @Override
    public boolean isSubtypeSimple(SemType t1, SemType t2) {
        return Core.isSubtypeSimple(t1, t2);
    }

    @Override
    public boolean isListType(SemType t) {
        return Core.isSubtypeSimple(t, Builder.getListType());
    }

    @Override
    public boolean isMapType(SemType t) {
        return Core.isSubtypeSimple(t, Builder.getMappingType());
    }

    @Override
    public SemType intConst(long l) {
        return Builder.getIntConst(l);
    }

    @Override
    public SemType mappingMemberTypeInnerVal(TypeTestContext<SemType> context, SemType t, SemType key) {
        return MappingProj.mappingMemberTypeInnerVal(from(context), t, key);
    }

    @Override
    public SemType listProj(TypeTestContext<SemType> context, SemType t, SemType key) {
        return ListProj.listProjInnerVal(from(context), t, key);
    }

    @Override
    public SemType listMemberType(TypeTestContext<SemType> context, SemType t, SemType key) {
        return Core.listMemberTypeInnerVal(from(context), t, key);
    }
}
