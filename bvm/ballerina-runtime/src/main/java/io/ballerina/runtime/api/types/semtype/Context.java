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
 * Context in which semtype was defined in.
 *
 * @since 2201.10.0
 */
public final class Context {

    // Contains all BddMemo entries with isEmpty == PROVISIONAL
    private final List<BddMemo> memoStack = new ArrayList<>();
    public final Env env;
    public final Map<Bdd, BddMemo> listMemo = new HashMap<>();
    // SEMTYPE-TODO: Fill this in as needed, currently just a placeholder since basic types don't need it

    private Context(Env env) {
        this.env = env;
    }

    public static Context from(Env env) {
        return new Context(env);
    }

    public boolean memoSubtypeIsEmpty(Map<Bdd, BddMemo> memoTable, BddIsEmptyPredicate isEmptyPredicate, Bdd bdd) {
        BddMemo mm = memoTable.get(bdd);
        BddMemo m;
        if (mm != null) {
            switch (mm.isEmpty) {
                case CYCLIC:
                    // Since we define types inductively we consider these to be empty
                    return true;
                case TRUE, FALSE:
                    // We know whether b is empty or not for certain
                    return mm.isEmpty == BddMemo.Status.TRUE;
                case NULL:
                    // this is same as not having memo so fall through
                    m = mm;
                    break;
                case LOOP, PROVISIONAL:
                    // We've got a loop.
                    mm.isEmpty = BddMemo.Status.LOOP;
                    return true;
                default:
                    throw new AssertionError("Unexpected memo status: " + mm.isEmpty);
            }
        } else {
            m = new BddMemo();
            memoTable.put(bdd, m);
        }
        m.isEmpty = BddMemo.Status.PROVISIONAL;
        int initStackDepth = memoStack.size();
        memoStack.add(m);
        boolean isEmpty = isEmptyPredicate.apply(this, bdd);
        boolean isLoop = m.isEmpty == BddMemo.Status.LOOP;
        if (!isEmpty || initStackDepth == 0) {
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
        return isEmpty;
    }

    public ListAtomicType listAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return this.env.getRecListAtomType(recAtom);
        } else {
            return (ListAtomicType) ((TypeAtom) atom).atomicType();
        }
    }
}
