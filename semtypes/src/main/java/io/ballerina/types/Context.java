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
package io.ballerina.types;

import java.util.HashMap;
import java.util.Map;

/**
 * TypeCheckContext node.
 *
 * @since 2201.8.0
 */
public class Context {
    public final Env env;
    public final Map<Bdd, BddMemo> functionMemo = new HashMap<>();
    public final Map<Bdd, BddMemo> listMemo = new HashMap<>();
    public final Map<Bdd, BddMemo> mappingMemo = new HashMap<>();

    private static volatile Context instance;
    public SemType anydataMemo;

    private Context(Env env) {
        this.env = env;
    }

    public static Context from(Env env) {
        if (instance == null) {
            synchronized (Context.class) {
                if (instance == null) {
                    instance = new Context(env);
                }
            }
        }
        if (instance.env == env) {
            return instance;
        } else {
            instance = new Context(env);
        }
        return instance;
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
