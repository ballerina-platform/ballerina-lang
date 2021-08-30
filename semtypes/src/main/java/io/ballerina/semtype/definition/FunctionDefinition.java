/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.semtype.definition;

import io.ballerina.semtype.Definition;
import io.ballerina.semtype.Env;
import io.ballerina.semtype.FunctionAtomicType;
import io.ballerina.semtype.PredefinedType;
import io.ballerina.semtype.RecAtom;
import io.ballerina.semtype.SemType;
import io.ballerina.semtype.UniformTypeCode;
import io.ballerina.semtype.typeops.BddCommonOps;

/**
 * Represent function type desc.
 *
 * @since 2.0.0
 */
public class FunctionDefinition implements Definition {

    private RecAtom atom;
    private SemType semType;

    public FunctionDefinition(Env env) {
        FunctionAtomicType dummy = new FunctionAtomicType(PredefinedType.NEVER, PredefinedType.NEVER);
        this.atom = env.recFunctionAtom();
        this.semType = PredefinedType.uniformSubtype(UniformTypeCode.UT_FUNCTION, BddCommonOps.bddAtom(this.atom));
    }

    @Override
    public SemType getSemType(Env env) {
        return this.semType;
    }

    public SemType define(Env env, SemType args, SemType ret) {
        FunctionAtomicType t = new FunctionAtomicType(args, ret);
        env.setRecFunctionAtomType(this.atom, t);
        return this.semType;
    }
}
