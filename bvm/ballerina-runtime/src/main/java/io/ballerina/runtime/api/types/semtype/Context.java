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
import io.ballerina.runtime.internal.types.semtype.MutableSemType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private static final int MAX_CACHE_SIZE = 100;
    private final Map<CacheableTypeDescriptor, TypeCheckCache<CacheableTypeDescriptor>> typeCheckCacheMemo;
    private Phase phase = Phase.INIT;
    private int typeCheckDepth = 0;
    private int typeResolutionDepth = 0;

    private Context(Env env) {
        this.env = env;
        this.typeCheckCacheMemo = createTypeCheckCacheMemo();
    }

    private static Map<CacheableTypeDescriptor, TypeCheckCache<CacheableTypeDescriptor>> createTypeCheckCacheMemo() {
        // This is fine since this map is not going to get leaked out of the context and
        // context is unique to a thread. So there will be no concurrent modifications
        return new LinkedHashMap<>(MAX_CACHE_SIZE, 1f, true) {
            @Override
            protected boolean removeEldestEntry(
                    Map.Entry<CacheableTypeDescriptor, TypeCheckCache<CacheableTypeDescriptor>> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
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

    public void enterTypeResolutionPhase(MutableSemType type) throws InterruptedException {
        switch (phase) {
            case INIT -> {
                typeResolutionDepth++;
                env.enterTypeResolutionPhase(this, type);
                phase = Phase.TYPE_RESOLUTION;
            }
            case TYPE_RESOLUTION -> typeResolutionDepth++;
            case TYPE_CHECKING -> throw new IllegalStateException(
                    "Cannot enter type resolution phase while in type checking phase\n");
        }
    }

    public void exitTypeResolutionPhaseAbruptly(Exception ex) {
        env.exitTypeResolutionPhaseAbruptly(this, ex);
    }

    public void enterTypeCheckingPhase(SemType t1, SemType t2) {
        typeCheckDepth++;
        switch (phase) {
            case INIT -> {
                env.enterTypeCheckingPhase(this, t1, t2);
                phase = Phase.TYPE_CHECKING;
            }
            case TYPE_RESOLUTION -> throw new IllegalStateException(
                    "Cannot enter type checking phase while in type resolution phase\n");
            case TYPE_CHECKING -> {
            }
        }
    }

    public void exitTypeResolutionPhase() {
        if (phase == Phase.TYPE_RESOLUTION) {
            typeResolutionDepth--;
            if (typeResolutionDepth == 0) {
                env.exitTypeResolutionPhase(this);
                phase = Phase.INIT;
            }
        } else {
            throw new IllegalStateException("Cannot exit type resolution phase without entering it");
        }
    }

    public void exitTypeCheckingPhase() {
        switch (phase) {
            case INIT -> throw new IllegalStateException("Cannot exit type checking phase without entering it");
            case TYPE_RESOLUTION ->
                    throw new IllegalStateException("Cannot exit type checking phase while in type resolution phase\n");
            case TYPE_CHECKING -> {
                env.exitTypeCheckingPhase(this);
                typeCheckDepth--;
                if (typeCheckDepth == 0) {
                    phase = Phase.INIT;
                }
            }
        }
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
        if (atom instanceof RecAtom recAtom) {
            assert this.env.getRecListAtomType(recAtom) != null;
            return this.env.getRecListAtomType(recAtom);
        } else {
            return (ListAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public MappingAtomicType mappingAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            assert this.env.getRecMappingAtomType(recAtom) != null;
            return this.env.getRecMappingAtomType(recAtom);
        } else {
            return (MappingAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public FunctionAtomicType functionAtomicType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            assert this.env.getRecFunctionAtomType(recAtom) != null;
            return this.env.getRecFunctionAtomType(recAtom);
        } else {
            return (FunctionAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public TypeCheckCache<CacheableTypeDescriptor> getTypeCheckCache(CacheableTypeDescriptor typeDescriptor) {
        return typeCheckCacheMemo.computeIfAbsent(typeDescriptor, TypeCheckCache::new);
    }

    public void registerAbruptTypeCheckEnd(Exception ex) {
        env.registerAbruptTypeCheckEnd(this, ex);
    }

    enum Phase {
        INIT, TYPE_RESOLUTION, TYPE_CHECKING
    }
}
