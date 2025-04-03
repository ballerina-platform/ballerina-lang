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
package io.ballerina.types;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Env node.
 *
 * @since 2201.12.0
 */
public class Env {

    private static final int COMPACT_INDEX = 3;
    final List<ListAtomicType> recListAtoms;
    final List<MappingAtomicType> recMappingAtoms;
    final List<FunctionAtomicType> recFunctionAtoms;
    private final AtomicInteger distinctAtomCount;
    private final Map<AtomicType, Reference<TypeAtom>> atomTable;

    private final LinkedHashMap<String, SemType> types;

    public Env() {
        this.atomTable = new WeakHashMap<>();
        this.recListAtoms = new ArrayList<>();
        this.recMappingAtoms = new ArrayList<>();
        this.recFunctionAtoms = new ArrayList<>();
        types = new LinkedHashMap<>();
        distinctAtomCount = new AtomicInteger(0);

        PredefinedTypeEnv.getInstance().initializeEnv(this);
    }

    public int recListAtomCount() {
        return this.recListAtoms.size();
    }

    public int recMappingAtomCount() {
        return this.recMappingAtoms.size();
    }

    public int recFunctionAtomCount() {
        return this.recFunctionAtoms.size();
    }

    public int distinctAtomCount() {
        return this.distinctAtomCount.get();
    }

    public int distinctAtomCountGetAndIncrement() {
        return this.distinctAtomCount.getAndIncrement();
    }

    public RecAtom recFunctionAtom() {
        synchronized (this.recFunctionAtoms) {
            int result = this.recFunctionAtoms.size();
            // represents adding () in nballerina
            this.recFunctionAtoms.add(null);
            return RecAtom.createRecAtom(result);
        }
    }

    public void setRecFunctionAtomType(RecAtom ra, FunctionAtomicType atomicType) {
        synchronized (this.recFunctionAtoms) {
            ra.setKind(Atom.Kind.FUNCTION_ATOM);
            this.recFunctionAtoms.set(ra.index, atomicType);
        }
    }

    public FunctionAtomicType getRecFunctionAtomType(RecAtom ra) {
        synchronized (this.recFunctionAtoms) {
            return this.recFunctionAtoms.get(ra.index);
        }
    }

    public TypeAtom listAtom(ListAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    public TypeAtom mappingAtom(MappingAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    public TypeAtom functionAtom(FunctionAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    public TypeAtom cellAtom(CellAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    private TypeAtom typeAtom(AtomicType atomicType) {
        synchronized (this.atomTable) {
            Reference<TypeAtom> ref = this.atomTable.get(atomicType);
            if (ref != null) {
                TypeAtom ta = ref.get();
                if (ta != null) {
                    return ta;
                }
            }
            TypeAtom result = TypeAtom.createTypeAtom(this.atomTable.size(), atomicType);
            this.atomTable.put(result.atomicType(), new WeakReference<>(result));
            return result;
        }
    }

    public void deserializeTypeAtom(TypeAtom typeAtom) {
        synchronized (this.atomTable) {
            this.atomTable.put(typeAtom.atomicType(), new WeakReference<>(typeAtom));
        }
    }

    public void insertRecAtomAtIndex(int index, AtomicType atomicType) {
        if (atomicType instanceof MappingAtomicType mappingAtomicType) {
            insertAtomAtIndexInner(index, this.recMappingAtoms, mappingAtomicType);
        } else if (atomicType instanceof ListAtomicType listAtomicType) {
            insertAtomAtIndexInner(index, this.recListAtoms, listAtomicType);
        } else if (atomicType instanceof FunctionAtomicType functionAtomicType) {
            insertAtomAtIndexInner(index, this.recFunctionAtoms, functionAtomicType);
        } else {
            throw new UnsupportedOperationException("Unknown atomic type " + atomicType);
        }
    }

    private <E extends AtomicType> void insertAtomAtIndexInner(int index, List<E> atoms, E atomicType) {
        // atoms are always private final fields therefore synchronizing on them should be safe.
        synchronized (atoms) {
            if (atoms.size() > index && atoms.get(index) != null) {
                return;
            }
            while (atoms.size() < index + 1) {
                atoms.add(null);
            }
            atoms.set(index, atomicType);
        }
    }

    public ListAtomicType listAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return getRecListAtomType(recAtom);
        } else {
            return (ListAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public FunctionAtomicType functionAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return getRecFunctionAtomType(recAtom);
        } else {
            return (FunctionAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public MappingAtomicType mappingAtomType(Atom atom) {
        if (atom instanceof RecAtom recAtom) {
            return getRecMappingAtomType(recAtom);
        } else {
            return (MappingAtomicType) ((TypeAtom) atom).atomicType();
        }
    }

    public RecAtom recListAtom() {
        synchronized (this.recListAtoms) {
            int result = this.recListAtoms.size();
            this.recListAtoms.add(null);
            return RecAtom.createRecAtom(result);
        }
    }

    public RecAtom recMappingAtom() {
        synchronized (this.recMappingAtoms) {
            int result = this.recMappingAtoms.size();
            this.recMappingAtoms.add(null);
            return RecAtom.createRecAtom(result);
        }
    }

    public void setRecListAtomType(RecAtom ra, ListAtomicType atomicType) {
        synchronized (this.recListAtoms) {
            ra.setKind(Atom.Kind.LIST_ATOM);
            this.recListAtoms.set(ra.index, atomicType);
        }
    }

    public void setRecMappingAtomType(RecAtom ra, MappingAtomicType atomicType) {
        synchronized (this.recListAtoms) {
            ra.setKind(Atom.Kind.MAPPING_ATOM);
            this.recMappingAtoms.set(ra.index, atomicType);
        }
    }

    public ListAtomicType getRecListAtomType(RecAtom ra) {
        synchronized (this.recListAtoms) {
            return this.recListAtoms.get(ra.index);
        }
    }

    public MappingAtomicType getRecMappingAtomType(RecAtom ra) {
        synchronized (this.recMappingAtoms) {
            return this.recMappingAtoms.get(ra.index);
        }
    }

    public static CellAtomicType cellAtomType(Atom atom) {
        return (CellAtomicType) ((TypeAtom) atom).atomicType();
    }

    public void addTypeDef(String typeName, SemType semType) {
        this.types.put(typeName, semType);
    }

    public Map<String, SemType> getTypeNameSemTypeMap() {
        return new LinkedHashMap<>(this.types);
    }

    public int atomCount() {
        synchronized (this.atomTable) {
            return this.atomTable.size();
        }
    }

    // TODO: instead of compact index we should analyze the environment before serialization, but a naive bumping index
    //   in the BIRTypeWriter created incorrect indexes in the BIR. This is a temporary workaround.
    private CompactionData compactionData = null;

    /**
     * During type checking we create recursive type atoms that are not parts of the actual ballerina module which will
     * not marshalled. This leaves "holes" in the rec atom lists when we unmarshall the BIR, which will then get
     * propagated from one module to next. This method will return a new index corrected for such holes.
     *
     * @param recAtom atom for which you need the corrected index
     * @return index corrected for "holes" in rec atom list
     */
    public synchronized int compactRecIndex(RecAtom recAtom) {
        if (compactionData == null || !compactionData.state().equals(EnvState.from(this))) {
            compactionData = compaction();
        }
        if (recAtom.index < COMPACT_INDEX) {
            return recAtom.index;
        }
        return switch (recAtom.kind()) {
            case LIST_ATOM -> compactionData.listMap().get(recAtom.index());
            case MAPPING_ATOM -> compactionData.mapMap().get(recAtom.index());
            case FUNCTION_ATOM -> compactionData.funcMap().get(recAtom.index());
            case CELL_ATOM, XML_ATOM, DISTINCT_ATOM -> recAtom.index;
        };
    }

    private CompactionData compaction() {
        EnvState state = EnvState.from(this);
        Map<Integer, Integer> listMap = recListCompaction(this.recListAtoms);
        Map<Integer, Integer> mapMap = recListCompaction(this.recMappingAtoms);
        Map<Integer, Integer> funcMap = recListCompaction(this.recFunctionAtoms);
        return new CompactionData(state, listMap, mapMap, funcMap);
    }

    private <E extends AtomicType> Map<Integer, Integer> recListCompaction(List<E> recAtomList) {
        Map<Integer, Integer> map = new HashMap<>();
        int compactIndex = COMPACT_INDEX;
        for (int i = COMPACT_INDEX; i < recAtomList.size(); i++) {
            if (recAtomList.get(i) != null) {
                map.put(i, compactIndex);
                compactIndex++;
            }
        }
        return map;
    }

    record EnvState(int recListAtomCount, int recMappingAtomCount, int recFunctionAtomCount) {

        public static EnvState from(Env env) {
            return new EnvState(env.recListAtomCount(), env.recMappingAtomCount(),
                    env.recFunctionAtomCount());
        }
    }

    private record CompactionData(EnvState state, Map<Integer, Integer> listMap, Map<Integer, Integer> mapMap,
                                  Map<Integer, Integer> funcMap) {

    }
}
