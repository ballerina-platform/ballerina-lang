/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Atom;
import io.ballerina.runtime.api.types.semtype.BasicTypeCode;
import io.ballerina.runtime.api.types.semtype.BddNode;
import io.ballerina.runtime.api.types.semtype.Builder;
import io.ballerina.runtime.api.types.semtype.Definition;
import io.ballerina.runtime.api.types.semtype.Env;
import io.ballerina.runtime.api.types.semtype.RecAtom;
import io.ballerina.runtime.api.types.semtype.SemType;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code Definition} used to create function subtypes.
 *
 * @since 2201.11.0
 */
public class FunctionDefinition extends Definition {

    private volatile RecAtom rec;
    private volatile SemType semType;
    private final Lock lock = new ReentrantLock();

    @Override
    public SemType getSemType(Env env) {
        try {
            this.lock.lock();
            if (this.semType != null) {
                return this.semType;
            } else {
                RecAtom rec = env.recFunctionAtom();
                this.rec = rec;
                return this.createSemType(rec);
            }
        } finally {
            this.lock.unlock();
        }
    }

    private SemType createSemType(Atom atom) {
        BddNode bdd = BddNode.bddAtom(atom);
        SemType semType = Builder.basicSubType(BasicTypeCode.BT_FUNCTION, BFunctionSubType.createDelegate(bdd));
        this.semType = semType;
        return semType;
    }

    public SemType define(Env env, SemType args, SemType ret, FunctionQualifiers qualifiers) {
        FunctionAtomicType atomicType = new FunctionAtomicType(args, ret, qualifiers.toSemType(env));
        try {
            lock.lock();
            RecAtom rec = this.rec;
            Atom atom;
            if (rec != null) {
                atom = rec;
                env.setRecFunctionAtomType(rec, atomicType);
            } else {
                atom = env.functionAtom(atomicType);
            }
            return this.createSemType(atom);
        } finally {
            lock.unlock();
        }
    }

}
