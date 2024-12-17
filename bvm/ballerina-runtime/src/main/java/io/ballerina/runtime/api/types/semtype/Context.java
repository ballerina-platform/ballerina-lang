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
    private int drainedPermits = 0;
    private int nTypeChecking = 0;
    private Phase phase = Phase.INIT;
    List<PhaseData> typeResolutionPhases = new ArrayList<>();
    List<PhaseData> typeCheckPhases = new ArrayList<>();
    private final boolean collectDiagnostic;

    private Context(Env env) {
        this.env = env;
        this.typeCheckCacheMemo = createTypeCheckCacheMemo();
        this.collectDiagnostic = "true".equalsIgnoreCase(System.getenv("BAL_TYPE_CHECK_DIAGNOSTIC_ENABLE"));
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
                env.enterTypeResolutionPhase(this, type);
                phase = Phase.TYPE_RESOLUTION;
                if (collectDiagnostic) {
                    typeResolutionPhases.add(new PhaseData());
                }
            }
            case TYPE_RESOLUTION -> {
            }
            case TYPE_CHECKING -> {
                throw new IllegalStateException("Cannot enter type resolution phase while in type checking phase");
            }
        }
    }

    public void exitTypeResolutionPhaseAbruptly(Exception ex) {
        env.exitTypeResolutionPhaseAbruptly(this, ex);
    }

    public void enterTypeCheckingPhase(SemType t1, SemType t2) {
        nTypeChecking += 1;
        switch (phase) {
            case INIT -> {
                // This can happen if both types are immutable semtypes
                if (collectDiagnostic) {
                    typeCheckPhases.add(new PhaseData());
                }
                phase = Phase.TYPE_CHECKING;
            }
            case TYPE_RESOLUTION -> {
                drainedPermits = env.enterTypeCheckingPhase(this, t1, t2);
                if (collectDiagnostic) {
                    typeCheckPhases.add(new PhaseData());
                    typeResolutionPhases.removeLast();
                }
                phase = Phase.TYPE_CHECKING;
            }
            case TYPE_CHECKING -> {
            }
        }
    }

    public void exitTypeResolutionPhase() {
        if (phase == Phase.TYPE_RESOLUTION) {
            env.exitTypeResolutionPhase(this);
            phase = Phase.INIT;
            if (collectDiagnostic) {
                typeResolutionPhases.removeLast();
            }
        }
    }

    public void exitTypeCheckingPhase() {
        nTypeChecking -= 1;
        switch (phase) {
            case INIT -> throw new IllegalStateException("Cannot exit type checking phase without entering it");
            case TYPE_RESOLUTION ->
                    throw new IllegalStateException("Cannot exit type checking phase while in type resolution phase");
            case TYPE_CHECKING -> {
                assert nTypeChecking >= 0;
                if (nTypeChecking == 0) {
                    env.exitTypeCheckingPhase(this, drainedPermits + 1);
                    if (collectDiagnostic) {
                        typeCheckPhases.removeLast();
                    }
                    phase = Phase.INIT;
                    drainedPermits = 0;
                }
                assert nTypeChecking >= 0;
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

    record PhaseData(StackTraceElement[] stackTrace) {

        PhaseData() {
            this(Thread.currentThread().getStackTrace());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement element : stackTrace) {
                builder.append("\tat ").append(element).append("\n");
            }
            return builder.toString();
        }
    }
}
