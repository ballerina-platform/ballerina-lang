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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represent the environment in which {@code SemTypes} are defined in. Type checking types defined in different
 * environments with each other in undefined.
 *
 * @since 2201.10.0
 */
public final class Env {

    private static final Env INSTANCE = new Env();

    private final Map<AtomicType, TypeAtom> atomTable;
    private final ReadWriteLock atomTableLock = new ReentrantReadWriteLock();

    private final List<ListAtomicType> recListAtoms;

    private final Map<CellSemTypeCacheKey, SemType> cellTypeCache = new ConcurrentHashMap<>();

    private Env() {
        this.atomTable = new HashMap<>();
        this.recListAtoms = new ArrayList<>();
        // FIXME: add LIST_ATOMIC_RO
    }

    public static Env getInstance() {
        return INSTANCE;
    }

    public TypeAtom cellAtom(CellAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    private TypeAtom typeAtom(AtomicType atomicType) {
        this.atomTableLock.readLock().lock();
        try {
            TypeAtom ta = this.atomTable.get(atomicType);
            if (ta != null) {
                return ta;
            }
        } finally {
            this.atomTableLock.readLock().unlock();
        }

        this.atomTableLock.writeLock().lock();
        try {
            // we are double-checking since there may be 2 trying to add at the same time
            TypeAtom ta = this.atomTable.get(atomicType);
            if (ta != null) {
                return ta;
            } else {
                TypeAtom result = TypeAtom.createTypeAtom(this.atomTable.size(), atomicType);
                this.atomTable.put(result.atomicType(), result);
                return result;
            }
        } finally {
            this.atomTableLock.writeLock().unlock();
        }
    }

    Optional<SemType> getCachedCellType(SemType ty, CellAtomicType.CellMutability mut) {
        return Optional.ofNullable(this.cellTypeCache.get(new CellSemTypeCacheKey(ty, mut)));
    }

    void cacheCellType(SemType ty, CellAtomicType.CellMutability mut, SemType semType) {
        this.cellTypeCache.put(new CellSemTypeCacheKey(ty, mut), semType);
    }

    public RecAtom recListAtom() {
        // TODO: do we have seperate read and write operations, if so use rw lock
        synchronized (this.recListAtoms) {
            int result = this.recListAtoms.size();
            // represents adding () in nballerina
            this.recListAtoms.add(null);
            return RecAtom.createRecAtom(result);
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
        synchronized (this.recListAtoms) {
            return this.recListAtoms.get(ra.index);
        }
    }

    private record CellSemTypeCacheKey(SemType ty, CellAtomicType.CellMutability mut) {

    }
}
