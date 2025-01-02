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

import io.ballerina.runtime.internal.types.semtype.CellAtomicType;
import io.ballerina.runtime.internal.types.semtype.FunctionAtomicType;
import io.ballerina.runtime.internal.types.semtype.ListAtomicType;
import io.ballerina.runtime.internal.types.semtype.MappingAtomicType;
import io.ballerina.runtime.internal.types.semtype.MutableSemType;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Represent the environment in which {@code SemTypes} are defined in. Type
 * checking types defined in different
 * environments with each other in undefined. This is safe to be shared between
 * multiple threads.
 * 
 * @since 2201.11.0
 */
public final class Env {
    // Currently there is no reason to worry about above restrictions since Env is a singleton, but strictly speaking
    // there is not technical restriction preventing multiple instances of Env.

    private static final Env INSTANCE = new Env();

    // Each atom is created once but will be accessed multiple times during type checking. Also in perfect world we
    // will create atoms at the beginning of the execution and will eventually reach
    // a steady state.
    private final ReadWriteLock atomLock = new ReentrantReadWriteLock();
    private final Map<AtomicType, Reference<TypeAtom>> atomTable;

    private final ReadWriteLock recListLock = new ReentrantReadWriteLock();
    final List<ListAtomicType> recListAtoms;

    private final ReadWriteLock recMapLock = new ReentrantReadWriteLock();
    final List<MappingAtomicType> recMappingAtoms;

    private final ReadWriteLock recFunctionLock = new ReentrantReadWriteLock();
    private final List<FunctionAtomicType> recFunctionAtoms;

    private final ReadWriteLock cellTypeCacheLock = new ReentrantReadWriteLock();
    private final Map<CellSemTypeCacheKey, SemType> cellTypeCache = new HashMap<>();

    private final AtomicInteger distinctAtomCount = new AtomicInteger(0);
    private final TypeCheckSelfDiagnosticsRunner selfDiagnosticsRunner;

    private final AtomicLong pendingTypeResolutions = new AtomicLong(0);

    private Env() {
        this.atomTable = new WeakHashMap<>();
        this.recListAtoms = new ArrayList<>();
        this.recMappingAtoms = new ArrayList<>();
        this.recFunctionAtoms = new ArrayList<>();

        PredefinedTypeEnv.getInstance().initializeEnv(this);
        String diagnosticEnable = System.getenv("BAL_TYPE_CHECK_DIAGNOSTIC_ENABLE");
        if ("true".equalsIgnoreCase(diagnosticEnable)) {
            this.selfDiagnosticsRunner = new DebugSelfDiagnosticRunner(this);
        } else {
            this.selfDiagnosticsRunner = new NonOpSelfDiagnosticRunner();
        }
    }

    public static Env getInstance() {
        return INSTANCE;
    }

    public TypeAtom cellAtom(CellAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    private TypeAtom typeAtom(AtomicType atomicType) {
        atomLock.readLock().lock();
        try {
            Reference<TypeAtom> ref = this.atomTable.get(atomicType);
            if (ref != null) {
                TypeAtom atom = ref.get();
                if (atom != null) {
                    return atom;
                }
            }
        } finally {
            atomLock.readLock().unlock();
        }
        atomLock.writeLock().lock();
        try {
            TypeAtom result = TypeAtom.createTypeAtom(this.atomTable.size(), atomicType);
            this.atomTable.put(result.atomicType(), new WeakReference<>(result));
            return result;
        } finally {
            atomLock.writeLock().unlock();
        }
    }

    // Ideally this cache should be in the builder as well. But technically we can't cache cells across environments.
    // In practice this shouldn't be an issue since there should be only one environment, but I am doing this here
    // just in case.
    SemType getCachedCellType(SemType ty, CellAtomicType.CellMutability mut, Supplier<SemType> semTypeCreator) {
        if (ty.some() != 0) {
            return semTypeCreator.get();
        }
        CellSemTypeCacheKey key = new CellSemTypeCacheKey(ty, mut);
        try {
            cellTypeCacheLock.readLock().lock();
            SemType cached = this.cellTypeCache.get(key);
            if (cached != null) {
                return cached;
            }
        } finally {
            cellTypeCacheLock.readLock().unlock();
        }
        try {
            cellTypeCacheLock.writeLock().lock();
            SemType cached = this.cellTypeCache.get(key);
            if (cached != null) {
                return cached;
            }
            var result = semTypeCreator.get();
            this.cellTypeCache.put(key, result);
            return result;
        } finally {
            cellTypeCacheLock.writeLock().unlock();
        }
    }

    public RecAtom recListAtom() {
        recListLock.writeLock().lock();
        try {
            int result = this.recListAtoms.size();
            // represents adding () in nballerina
            this.recListAtoms.add(null);
            return RecAtom.createRecAtom(result);
        } finally {
            recListLock.writeLock().unlock();
        }
    }

    public void setRecListAtomType(RecAtom rec, ListAtomicType atomicType) {
        // NOTE: this is fine since we are not actually changing the recList
        recListLock.readLock().lock();
        try {
            this.recListAtoms.set(rec.index(), atomicType);
        } finally {
            recListLock.readLock().unlock();
        }

    }

    public Atom listAtom(ListAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    public ListAtomicType getRecListAtomType(RecAtom ra) {
        recListLock.readLock().lock();
        try {
            return this.recListAtoms.get(ra.index());
        } finally {
            recListLock.readLock().unlock();
        }
    }

    public ListAtomicType listAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return getRecListAtomType(recAtom);
        } else {
            return (ListAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public RecAtom recMappingAtom() {
        recMapLock.writeLock().lock();
        try {
            int result = this.recMappingAtoms.size();
            // represents adding () in nballerina
            this.recMappingAtoms.add(null);
            return RecAtom.createRecAtom(result);
        } finally {
            recMapLock.writeLock().unlock();
        }
    }

    public void setRecMappingAtomType(RecAtom rec, MappingAtomicType atomicType) {
        recMapLock.readLock().lock();
        try {
            this.recMappingAtoms.set(rec.index(), atomicType);
        } finally {
            recMapLock.readLock().unlock();
        }
    }

    public TypeAtom mappingAtom(MappingAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    public MappingAtomicType getRecMappingAtomType(RecAtom recAtom) {
        recMapLock.readLock().lock();
        try {
            return this.recMappingAtoms.get(recAtom.index());
        } finally {
            recMapLock.readLock().unlock();
        }
    }

    public RecAtom recFunctionAtom() {
        recFunctionLock.writeLock().lock();
        try {
            int result = this.recFunctionAtoms.size();
            // represents adding () in nballerina
            this.recFunctionAtoms.add(null);
            return RecAtom.createRecAtom(result);
        } finally {
            recFunctionLock.writeLock().unlock();
        }
    }

    public void setRecFunctionAtomType(RecAtom rec, FunctionAtomicType atomicType) {
        recFunctionLock.readLock().lock();
        try {
            this.recFunctionAtoms.set(rec.index(), atomicType);
        } finally {
            recFunctionLock.readLock().unlock();
        }
    }

    public FunctionAtomicType getRecFunctionAtomType(RecAtom recAtom) {
        recFunctionLock.readLock().lock();
        try {
            return this.recFunctionAtoms.get(recAtom.index());
        } finally {
            recFunctionLock.readLock().unlock();
        }
    }

    public Atom functionAtom(FunctionAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    private record CellSemTypeCacheKey(SemType ty, CellAtomicType.CellMutability mut) {

    }

    public int distinctAtomCountGetAndIncrement() {
        return this.distinctAtomCount.getAndIncrement();
    }

    // This is for debug purposes
    public Optional<AtomicType> atomicTypeByIndex(int index) {
        atomLock.readLock().lock();
        try {
            for (Map.Entry<AtomicType, Reference<TypeAtom>> entry : this.atomTable.entrySet()) {
                TypeAtom typeAtom = entry.getValue().get();
                if (typeAtom == null) {
                    continue;
                }
                if (typeAtom.index() == index) {
                    return Optional.of(entry.getKey());
                }
            }
            return Optional.empty();
        } finally {
            atomLock.readLock().unlock();
        }
    }

    // When it comes to types there are 2 distinct stages, first we need to resolve types (ie turn type
    // descriptor in to a semtype) and then do the type checking. In the compiler there is a clear temporal separation
    // between these stages, but in runtime since we allow creating type dynamically we must allow them to interleave.
    // As result, we have to treat both these stages of the type check. When a type is being used for type checking
    // it is resolved (modifying the type after this point is undefined behaviour). To understand concurrency model for
    // type checking we can break up type checking to 2 phases as type resolution and type checking. To allow
    // concurrent type checking we need ensure fallowing invariants.
    // 1. Phase 1 should be able to run in a non-blocking manner. Assume we are checking T1 < T2 and T3 < T4
    //    concurrently with T1 depending on T3 and T4 depending on T2. If they are blocking we can have a deadlock.
    // 2. Before starting phase 2 all the types involved in the type check must be resolved. In above example T3 which
    //    is needed for first type check is being resolved as a part of the second type check.
    // Furthermore, ideally we shouldn't resolve the same type multiple times and both type checks should be able to
    // run parallel as much as possible.
    // Given each (strand) thread has its own context, it is easier to reason about concurrency using Context. First
    // we require all phase changes to go via the context which will synchronize with other contexts via the shared Env.
    // First we allow any number of context to enter phase 1 and run without blocking(property 1). When context
    // need to move to phase 2 it must wait for all contexts in phase 1 to finish. To prevent starvation when a
    // context has indicated that it needs to move to phase 2 we stop any new context from entering phase 1. When all
    // the contexts have reached phase 2 again they all can continue in parallel. At the same time we can allow new
    // context to enter phase 1.

    void enterTypeResolutionPhase(Context cx, MutableSemType t) throws InterruptedException {
        pendingTypeResolutions.incrementAndGet();
        this.selfDiagnosticsRunner.registerTypeResolutionStart(cx, t);
    }

    void exitTypeResolutionPhaseAbruptly(Context cx, Exception ex) {
        try {
            pendingTypeResolutions.decrementAndGet();
            releaseLock((ReentrantReadWriteLock) atomLock);
            releaseLock((ReentrantReadWriteLock) recListLock);
            releaseLock((ReentrantReadWriteLock) recMapLock);
            releaseLock((ReentrantReadWriteLock) recFunctionLock);
        } catch (Exception ignored) {

        }
        this.selfDiagnosticsRunner.registerAbruptTypeResolutionEnd(cx, ex);
    }

    private void releaseLock(ReentrantReadWriteLock lock) {
        if (lock.writeLock().isHeldByCurrentThread()) {
            lock.writeLock().unlock();
        }
        if (lock.getReadHoldCount() > 0) {
            lock.readLock().unlock();
        }
    }

    void exitTypeResolutionPhase(Context cx) {
        long res = pendingTypeResolutions.decrementAndGet();
        assert res >= 0;
        this.selfDiagnosticsRunner.registerTypeResolutionExit(cx);
    }

    void enterTypeCheckingPhase(Context cx, SemType t1, SemType t2) {
        assert pendingTypeResolutions.get() >= 0;
        while (pendingTypeResolutions.get() != 0) {
            LockSupport.parkNanos(Duration.ofNanos(10).toNanos());
        }
        this.selfDiagnosticsRunner.registerTypeCheckStart(cx, t1, t2);
    }

    void exitTypeCheckingPhase(Context cx) {
        this.selfDiagnosticsRunner.registerTypeCheckEnd(cx);
    }

    void registerAbruptTypeCheckEnd(Context context, Exception ex) {
        this.selfDiagnosticsRunner.registerAbruptTypeCheckEnd(context, ex);
    }

    // These are helper methods for diagnostics that needs access to internal state of the environment
    List<ListAtomicType> getRecListAtomsCopy() {
        recListLock.readLock().lock();
        try {
            return new ArrayList<>(this.recListAtoms);
        } finally {
            recListLock.readLock().unlock();
        }
    }

    List<MappingAtomicType> getRecMappingAtomsCopy() {
        recMapLock.readLock().lock();
        try {
            return new ArrayList<>(this.recMappingAtoms);
        } finally {
            recMapLock.readLock().unlock();
        }
    }

    List<FunctionAtomicType> getRecFunctionAtomsCopy() {
        recFunctionLock.readLock().lock();
        try {
            return new ArrayList<>(this.recFunctionAtoms);
        } finally {
            recFunctionLock.readLock().unlock();
        }
    }
}
