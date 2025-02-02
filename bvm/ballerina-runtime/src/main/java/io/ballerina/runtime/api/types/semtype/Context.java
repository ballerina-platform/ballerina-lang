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

import io.ballerina.runtime.internal.types.semtype.BddMemo;
import io.ballerina.runtime.internal.types.semtype.FunctionAtomicType;
import io.ballerina.runtime.internal.types.semtype.ListAtomicType;
import io.ballerina.runtime.internal.types.semtype.MappingAtomicType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Context in which type checking operations are performed. Note context is not
 * thread safe, and multiple type check operations should not use the same
 * context concurrently. Multiple contexts may share same environment without
 * issue.
 *
 * @since 2201.11.0
 */
public final class Context {

    // Contains all BddMemo entries with isEmpty == PROVISIONAL
    private final List<BddMemo> memoStack = new ArrayList<>();
    public final Env env;
    public final Map<Bdd, BddMemo> listMemo = new WeakHashMap<>();
    public final Map<Bdd, BddMemo> mappingMemo = new WeakHashMap<>();
    public final Map<Bdd, BddMemo> functionMemo = new WeakHashMap<>();

    private Context(Env env) {
        this.env = env;
    }

    public static Context from(Env env) {
        return new Context(env);
    }

    /**
     * Memoization logic
     * Castagna's paper does not deal with this fully. Although he calls it memoization, it is not, strictly speaking,
     * just memoization, since it is not just an optimization, but required for correct handling of recursive types.
     * The handling of recursive types depends on our types being defined inductively, rather than coinductively.
     * This means that each shape that is a member of the  set denoted by the type is finite. There is a tricky problem
     * here with memoizing results that rely on assumptions that subsequently turn out to be false. Memoization/caching
     * is discussed in section 7.1.2 of the Frisch thesis. This follows Frisch's approach of undoing memoizations that
     * turn out to be wrong. (I did not succeed in fully understanding his approach, so I am not  completely sure if we
     * are doing the same.)
     * @param memoTable corresponding memo table for the Bdd
     * @param isEmptyPredicate predicate to be applied on the Bdd
     * @param bdd Bdd to be checked
     * @return result of applying predicate on the bdd
     */
    public boolean memoSubtypeIsEmpty(Map<Bdd, BddMemo> memoTable, BddIsEmptyPredicate isEmptyPredicate, Bdd bdd) {
        BddMemo m = memoTable.computeIfAbsent(bdd, ignored -> new BddMemo());
        return m.isEmpty().orElseGet(() -> memoSubTypeIsEmptyInner(isEmptyPredicate, bdd, m));
    }

    private boolean memoSubTypeIsEmptyInner(BddIsEmptyPredicate isEmptyPredicate, Bdd bdd, BddMemo m) {
        // We are staring the type check with the assumption our type is empty (see: inductive type)
        m.isEmpty = BddMemo.Status.PROVISIONAL;
        int initStackDepth = memoStack.size();
        memoStack.add(m);
        boolean isEmpty = isEmptyPredicate.apply(this, bdd);
        boolean isLoop = m.isEmpty == BddMemo.Status.LOOP;
        // if not empty our assumption is wrong so we need to reset the memoized values, otherwise we cleanup the stack
        // at the end
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
                // We started with the assumption our type is empty. Now we know for sure if we are empty or not
                // if we are empty all of these who don't have anything except us should be empty as well.
                // Otherwise, we don't know if they are empty or not
                memoStack.get(i).isEmpty = isEmpty ? BddMemo.Status.TRUE : BddMemo.Status.NULL;
            }
        }
        if (memoStack.size() > initStackDepth) {
            memoStack.subList(initStackDepth, memoStack.size()).clear();
        }
        if (isLoop && isEmpty) {
            // The only way that we have found that this can be empty is by going through a loop.
            // This means that the shapes in the type would all be infinite.
            // But we define types inductively, which means we only consider finite shapes.
            m.isEmpty = BddMemo.Status.CYCLIC;
        } else {
            m.isEmpty = isEmpty ? BddMemo.Status.TRUE : BddMemo.Status.FALSE;
        }
    }

    public ListAtomicType listAtomType(Atom atom) {
        return env.listAtomType(atom);
    }

    public MappingAtomicType mappingAtomType(Atom atom) {
        return env.mappingAtomType(atom);
    }

    public FunctionAtomicType functionAtomicType(Atom atom) {
        return env.functionAtomType(atom);
    }

}
