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
package io.ballerina.types;

import java.util.HashMap;
import java.util.Map;

/**
 * TypeCheckContext node.
 *
 * @since 3.0.0
 */
public class TypeCheckContext {
    private final Env env;
    public final Map<Bdd, BddMemo> functionMemo = new HashMap<>();
    public final Map<Bdd, BddMemo> listMemo = new HashMap<>();
    public final Map<Bdd, BddMemo> mappingMemo = new HashMap<>();

    private TypeCheckContext(Env env) {
        this.env = env;
    }

    static TypeCheckContext from(Env env) {
        return new TypeCheckContext(env);
    }

    public ListAtomicType listAtomType(Atom atom) {
        if (atom instanceof RecAtom) {
            return this.env.getRecListAtomType((RecAtom) atom);
        } else {
            return (ListAtomicType) ((TypeAtom) atom).atomicType;
        }
    }

    public MappingAtomicType mappingAtomType(Atom atom) {
        if (atom instanceof RecAtom) {
            return this.env.getRecMappingAtomType((RecAtom) atom);
        } else {
            return (MappingAtomicType) ((TypeAtom) atom).atomicType;
        }
    }

    public FunctionAtomicType functionAtomType(Atom atom) {
        return this.env.getRecFunctionAtomType((RecAtom) atom);
    }
}
