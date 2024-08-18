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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represent the environment in which {@code SemTypes} are defined in. Type checking types defined in different
 * environments with each other in undefined. This is safe to be shared between multiple threads.
 * @since 2201.10.0
 */
public final class Env {
    // Currently there is no reason to worry about above restrictions since Env is a singleton, but strictly speaking
    // there is not technical restriction to have multiple instances of Env.

    private static final Env INSTANCE = new Env();

    private final Map<AtomicType, Reference<TypeAtom>> atomTable;

    private final ReadWriteLock recListLock = new ReentrantReadWriteLock();
    final List<ListAtomicType> recListAtoms;

    private final ReadWriteLock recMapLock = new ReentrantReadWriteLock();
    final List<MappingAtomicType> recMappingAtoms;

    private final ReadWriteLock recFunctionLock = new ReentrantReadWriteLock();
    private final List<FunctionAtomicType> recFunctionAtoms;

    private final Map<CellSemTypeCacheKey, SemType> cellTypeCache = new ConcurrentHashMap<>();

    private final AtomicInteger distinctAtomCount = new AtomicInteger(0);

    private Env() {
        this.atomTable = new WeakHashMap<>();
        this.recListAtoms = new ArrayList<>();
        this.recMappingAtoms = new ArrayList<>();
        this.recFunctionAtoms = new ArrayList<>();

        PredefinedTypeEnv.getInstance().initializeEnv(this);
    }

    public static Env getInstance() {
        return INSTANCE;
    }

    public TypeAtom cellAtom(CellAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    private TypeAtom typeAtom(AtomicType atomicType) {
        synchronized (this.atomTable) {
            Reference<TypeAtom> ref = this.atomTable.get(atomicType);
            if (ref != null) {
                TypeAtom atom = ref.get();
                if (atom != null) {
                    return atom;
                }
            }
            TypeAtom result = TypeAtom.createTypeAtom(this.atomTable.size(), atomicType);
            this.atomTable.put(result.atomicType(), new WeakReference<>(result));
            return result;
        }
    }

    Optional<SemType> getCachedCellType(SemType ty, CellAtomicType.CellMutability mut) {
        if (ty.some() != 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.cellTypeCache.get(new CellSemTypeCacheKey(ty, mut)));
    }

    void cacheCellType(SemType ty, CellAtomicType.CellMutability mut, SemType semType) {
        if (ty.some() != 0) {
            return;
        }
        this.cellTypeCache.put(new CellSemTypeCacheKey(ty, mut), semType);
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
        synchronized (this.recListAtoms) {
            this.recListAtoms.set(rec.index(), atomicType);
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
        recMapLock.writeLock().lock();
        try {
            this.recMappingAtoms.set(rec.index(), atomicType);
        } finally {
            recMapLock.writeLock().unlock();
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
        recFunctionLock.writeLock().lock();
        try {
            this.recFunctionAtoms.set(rec.index(), atomicType);
        } finally {
            recFunctionLock.writeLock().unlock();
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
}
