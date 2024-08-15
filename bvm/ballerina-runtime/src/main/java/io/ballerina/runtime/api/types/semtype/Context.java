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

package io.ballerina.runtime.api.types.semtype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Context in which type checking operations are performed. Note context is not thread safe, requiring external
 * synchronization if shared between threads. Multiple contexts may share same environment without issue.
 *
 * @since 2201.10.0
 */
public final class Context {

    // Contains all BddMemo entries with isEmpty == PROVISIONAL
    private final List<BddMemo> memoStack = new ArrayList<>();
    public final Env env;
    public final Map<Bdd, BddMemo> listMemo = new HashMap<>();
    public final Map<Bdd, BddMemo> mappingMemo = new HashMap<>();
    public final Map<Bdd, BddMemo> functionMemo = new HashMap<>();

    SemType anydataMemo;
    private Context(Env env) {
        this.env = env;
    }

    public static Context from(Env env) {
        return new Context(env);
    }

    public boolean memoSubtypeIsEmpty(Map<Bdd, BddMemo> memoTable, BddIsEmptyPredicate isEmptyPredicate, Bdd bdd) {
        BddMemo m = memoTable.computeIfAbsent(bdd, ignored -> new BddMemo());
        return m.isEmpty().orElseGet(() -> memoSubTypeIsEmptyInner(isEmptyPredicate, bdd, m));
    }

    private boolean memoSubTypeIsEmptyInner(BddIsEmptyPredicate isEmptyPredicate, Bdd bdd, BddMemo m) {
        m.isEmpty = BddMemo.Status.PROVISIONAL;
        int initStackDepth = memoStack.size();
        memoStack.add(m);
        boolean isEmpty = isEmptyPredicate.apply(this, bdd);
        boolean isLoop = m.isEmpty == BddMemo.Status.LOOP;
        if (!isEmpty || initStackDepth == 0) {
            resetMemoizedValues(initStackDepth, isEmpty, isLoop, m);
        }
        return isEmpty;
    }

    private void resetMemoizedValues(int initStackDepth, boolean isEmpty, boolean isLoop, BddMemo m) {
        for (int i = initStackDepth + 1; i < memoStack.size(); i++) {
            BddMemo.Status memoStatus = memoStack.get(i).isEmpty;
            if (Objects.requireNonNull(memoStatus) == BddMemo.Status.PROVISIONAL ||
                    memoStatus == BddMemo.Status.LOOP || memoStatus == BddMemo.Status.CYCLIC) {
                memoStack.get(i).isEmpty = isEmpty ? BddMemo.Status.TRUE : BddMemo.Status.NULL;
            }
        }
        if (memoStack.size() > initStackDepth) {
            memoStack.subList(initStackDepth, memoStack.size()).clear();
        }
        // The only way that we have found that this can be empty is by going through a loop.
        // This means that the shapes in the type would all be infinite.
        // But we define types inductively, which means we only consider finite shapes.
        if (isLoop && isEmpty) {
            m.isEmpty = BddMemo.Status.CYCLIC;
        } else {
            m.isEmpty = isEmpty ? BddMemo.Status.TRUE : BddMemo.Status.FALSE;
        }
    }

    public ListAtomicType listAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return this.env.getRecListAtomType(recAtom);
        } else {
            return (ListAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public MappingAtomicType mappingAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return this.env.getRecMappingAtomType(recAtom);
        } else {
            return (MappingAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public FunctionAtomicType functionAtomicType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return this.env.getRecFunctionAtomType(recAtom);
        } else {
            return (FunctionAtomicType) ((TypeAtom) atom).atomicType();
        }
    }
}
