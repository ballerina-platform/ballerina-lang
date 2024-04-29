/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types.definition;

import io.ballerina.types.Atom;
import io.ballerina.types.BasicTypeCode;
import io.ballerina.types.ComplexSemType;
import io.ballerina.types.Definition;
import io.ballerina.types.Env;
import io.ballerina.types.FunctionAtomicType;
import io.ballerina.types.RecAtom;
import io.ballerina.types.SemType;
import io.ballerina.types.subtypedata.BddNode;

import static io.ballerina.types.PredefinedType.basicSubtype;
import static io.ballerina.types.typeops.BddCommonOps.bddAtom;

/**
 * Represent function type desc.
 *
 * @since 2201.8.0
 */
public final class FunctionDefinition implements Definition {

    private RecAtom rec;
    private SemType semType;

    @Override
    public SemType getSemType(Env env) {
        if (semType != null) {
            return semType;
        }
        RecAtom rec = env.recFunctionAtom();
        this.rec = rec;
        return this.createSemType(rec);
    }

    private SemType createSemType(Atom rec) {
        BddNode bdd = bddAtom(rec);
        ComplexSemType s = basicSubtype(BasicTypeCode.BT_FUNCTION, bdd);
        this.semType = s;
        return s;
    }

    public SemType define(Env env, SemType args, SemType ret) {
        FunctionAtomicType atomicType = FunctionAtomicType.from(args, ret);
        Atom atom;
        RecAtom rec = this.rec;
        if (rec != null) {
            atom = rec;
            env.setRecFunctionAtomType(rec, atomicType);
        } else {
            atom = env.functionAtom(atomicType);
        }
        return this.createSemType(atom);
    }
}
