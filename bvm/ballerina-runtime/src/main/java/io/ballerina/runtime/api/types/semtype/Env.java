/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Represent the environment in which {@code SemTypes} are defined in. Type
 * checking types defined in different
 * environments with each other in undefined. This is safe to be shared between
 * multiple threads.
 *
 * @since 2201.12.0
 */
public final class Env {
    // Currently there is no reason to worry about above restrictions since Env is a singleton, but strictly speaking
    // there is not technical restriction preventing multiple instances of Env.

    private static final Env INSTANCE = new Env();

    // Each atom is created once but will be accessed multiple times during type checking. Also in perfect world we
    // will create atoms at the beginning of the execution and will eventually reach
    // a steady state.
    private final AtomTable atomTable;

    private final ReadWriteLock recListLock = new ReentrantReadWriteLock();
    final List<ListAtomicType> recListAtoms;

    private final ReadWriteLock recMapLock = new ReentrantReadWriteLock();
    final List<MappingAtomicType> recMappingAtoms;

    private final ReadWriteLock recFunctionLock = new ReentrantReadWriteLock();
    private final List<FunctionAtomicType> recFunctionAtoms;

    private final ReadWriteLock cellTypeCacheLock = new ReentrantReadWriteLock();
    private final Map<CellSemTypeCacheKey, SemType> cellTypeCache = new HashMap<>();

    private final AtomicInteger distinctAtomCount = new AtomicInteger(0);

    private Env() {
        this.atomTable = new AtomTable();
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
        return atomTable.getOrCreate(atomicType);
    }

    // Ideally this cache should be in the builder as well. But technically we can't cache cells across environments.
    // In practice this shouldn't be an issue since there should be only one environment, but I am doing this here
    // just in case.
    SemType getCachedCellType(SemType ty, CellAtomicType.CellMutability mut, Supplier<SemType> semTypeCreator) {
        if (ty.some() != 0) {
            return semTypeCreator.get();
        }
        CellSemTypeCacheKey key = new CellSemTypeCacheKey(ty.all(), mut);
        cellTypeCacheLock.readLock().lock();
        try {
            SemType cached = this.cellTypeCache.get(key);
            if (cached != null) {
                return cached;
            }
        } finally {
            cellTypeCacheLock.readLock().unlock();
        }
        cellTypeCacheLock.writeLock().lock();
        try {
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
        return allocateRecAtom(recListLock, recListAtoms);
    }

    public void setRecListAtomType(RecAtom rec, ListAtomicType atomicType) {
        setRecAtomType(recListLock, recListAtoms, rec, atomicType);
    }

    public Atom listAtom(ListAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    private ListAtomicType getRecListAtomType(RecAtom ra) {
        return getRecAtomType(recListLock, recListAtoms, ra);
    }

    public ListAtomicType listAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return getRecListAtomType(recAtom);
        } else {
            return (ListAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public RecAtom recMappingAtom() {
        return allocateRecAtom(recMapLock, recMappingAtoms);
    }

    public void setRecMappingAtomType(RecAtom rec, MappingAtomicType atomicType) {
        setRecAtomType(recMapLock, recMappingAtoms, rec, atomicType);
    }

    public TypeAtom mappingAtom(MappingAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    private MappingAtomicType getRecMappingAtomType(RecAtom recAtom) {
        return getRecAtomType(recMapLock, recMappingAtoms, recAtom);
    }

    public MappingAtomicType mappingAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return this.getRecMappingAtomType(recAtom);
        } else {
            return (MappingAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public RecAtom recFunctionAtom() {
        return allocateRecAtom(recFunctionLock, recFunctionAtoms);
    }

    public void setRecFunctionAtomType(RecAtom rec, FunctionAtomicType atomicType) {
        setRecAtomType(recFunctionLock, recFunctionAtoms, rec, atomicType);
    }

    private FunctionAtomicType getRecFunctionAtomType(RecAtom recAtom) {
        return getRecAtomType(recFunctionLock, recFunctionAtoms, recAtom);
    }

    public FunctionAtomicType functionAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return this.getRecFunctionAtomType(recAtom);
        } else {
            return (FunctionAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    private static <E extends AtomicType> void setRecAtomType(ReadWriteLock lock, List<E> recAtomList, RecAtom rec,
                                                              E atomicType) {
        // NOTE: this is fine since we are not actually changing the recList
        lock.readLock().lock();
        try {
            recAtomList.set(rec.index(), atomicType);
            rec.ready();
        } finally {
            lock.readLock().unlock();
        }
    }

    private static <E extends AtomicType> E getRecAtomType(ReadWriteLock lock, List<E> recAtomList, RecAtom rec) {
        rec.waitUntilReady();
        lock.readLock().lock();
        try {
            assert recAtomList.get(rec.index()) != null;
            return recAtomList.get(rec.index());
        } finally {
            lock.readLock().unlock();
        }
    }

    private static <E extends AtomicType> RecAtom allocateRecAtom(ReadWriteLock lock, List<E> recAtomList) {
        lock.writeLock().lock();
        try {
            int result = recAtomList.size();
            // represents adding () in nballerina
            recAtomList.add(null);
            return RecAtom.createRecAtom(result);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Atom functionAtom(FunctionAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    private record CellSemTypeCacheKey(int all, CellAtomicType.CellMutability mut) {

    }

    public int distinctAtomCountGetAndIncrement() {
        return this.distinctAtomCount.getAndIncrement();
    }

    private static final class AtomTable {

        private final ReadWriteLock cellLock = new ReentrantReadWriteLock();
        private final ReadWriteLock functionLock = new ReentrantReadWriteLock();
        private final ReadWriteLock listLock = new ReentrantReadWriteLock();
        private final ReadWriteLock mappingLock = new ReentrantReadWriteLock();
        private final Map<AtomicType, Reference<TypeAtom>> cellTable = new WeakHashMap<>();
        private final Map<AtomicType, Reference<TypeAtom>> listTable = new WeakHashMap<>();
        private final Map<AtomicType, Reference<TypeAtom>> functionTable = new WeakHashMap<>();
        private final Map<AtomicType, Reference<TypeAtom>> mappingTable = new WeakHashMap<>();
        private final AtomicInteger nextCellIndex = new AtomicInteger(0);
        private final AtomicInteger nextListIndex = new AtomicInteger(0);
        private final AtomicInteger nextFunctionIndex = new AtomicInteger(0);
        private final AtomicInteger nextMappingIndex = new AtomicInteger(0);

        private AtomTable() {

        }

        public TypeAtom getOrCreate(AtomicType atomicType) {
            switch (atomicType) {
                case CellAtomicType cellAtomicType -> {
                    return getOrCreateInner(cellLock, cellTable, nextCellIndex, cellAtomicType);
                }
                case FunctionAtomicType functionAtomicType -> {
                    return getOrCreateInner(functionLock, functionTable, nextFunctionIndex, functionAtomicType);
                }
                case ListAtomicType listAtomicType -> {
                    return getOrCreateInner(listLock, listTable, nextListIndex, listAtomicType);
                }
                case MappingAtomicType mappingAtomicType -> {
                    return getOrCreateInner(mappingLock, mappingTable, nextMappingIndex, mappingAtomicType);
                }
            }
        }

        private static TypeAtom getOrCreateInner(ReadWriteLock rwLock, Map<AtomicType, Reference<TypeAtom>> table,
                                                 AtomicInteger nextIndex, AtomicType atomicType) {
            rwLock.readLock().lock();
            try {
                Reference<TypeAtom> ref = table.get(atomicType);
                if (ref != null) {
                    TypeAtom atom = ref.get();
                    if (atom != null) {
                        return atom;
                    }
                }
            } finally {
                rwLock.readLock().unlock();
            }
            int index = nextIndex.getAndIncrement();
            TypeAtom result = TypeAtom.createTypeAtom(index, atomicType);
            AtomicType key = result.atomicType();
            WeakReference<TypeAtom> value = new WeakReference<>(result);
            rwLock.writeLock().lock();
            try {
                table.put(key, value);
                return result;
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

}
