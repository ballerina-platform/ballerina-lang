/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.types.PredefinedType.CELL_ATOMIC_UNDEF;
import static io.ballerina.types.PredefinedType.LIST_ATOMIC_RO;
import static io.ballerina.types.PredefinedType.CELL_ATOMIC_INNER;
import static io.ballerina.types.PredefinedType.CELL_ATOMIC_INNER_MAPPING;
import static io.ballerina.types.PredefinedType.CELL_ATOMIC_INNER_MAPPING_RO;
import static io.ballerina.types.PredefinedType.CELL_ATOMIC_INNER_RO;
import static io.ballerina.types.PredefinedType.CELL_ATOMIC_NEVER;
import static io.ballerina.types.PredefinedType.CELL_ATOMIC_VAL;
import static io.ballerina.types.PredefinedType.LIST_ATOMIC_MAPPING;
import static io.ballerina.types.PredefinedType.LIST_ATOMIC_MAPPING_RO;
import static io.ballerina.types.PredefinedType.LIST_ATOMIC_TWO_ELEMENT;
import static io.ballerina.types.PredefinedType.MAPPING_ATOMIC_RO;

/**
 * Env node.
 *
 * @since 2201.8.0
 */
public class Env {
    private final Map<AtomicType, TypeAtom> atomTable;
    private final List<ListAtomicType> recListAtoms;
    private final List<MappingAtomicType> recMappingAtoms;
    private final List<FunctionAtomicType> recFunctionAtoms;

    private final LinkedHashMap<String, SemType> types;

    public Env() {
        this.atomTable = new HashMap<>();
        this.recListAtoms = new ArrayList<>();
        recListAtoms.add(LIST_ATOMIC_RO);
        this.recMappingAtoms = new ArrayList<>();
        recMappingAtoms.add(MAPPING_ATOMIC_RO);
        this.recFunctionAtoms = new ArrayList<>();
        types = new LinkedHashMap<>();

        // Reserving the first two indexes of atomTable to represent cell VAL and cell NEVER typeAtoms.
        // This is to avoid passing down env argument when doing cell type operations.
        // Please refer to the cellSubtypeDataEnsureProper() in cell.bal
        this.cellAtom(CELL_ATOMIC_VAL);
        this.cellAtom(CELL_ATOMIC_NEVER);
        // Reserving the next index of atomTable to represent the typeAtom required to construct
        // equivalent subtypes of map<any|error> and (any|error)[].
        this.cellAtom(CELL_ATOMIC_INNER);
        // Reserving the next two indexes of atomTable to represent typeAtoms related to (map<any|error>)[].
        // This is to avoid passing down env argument when doing tableSubtypeComplement operation.
        this.cellAtom(CELL_ATOMIC_INNER_MAPPING);
        this.listAtom(LIST_ATOMIC_MAPPING);
        // Reserving the next three indexes of atomTable to represent typeAtoms related to readonly type.
        // This is to avoid requiring context when referring to readonly type.
        // CELL_ATOMIC_INNER_MAPPING_RO & LIST_ATOMIC_MAPPING_RO are typeAtoms required to construct
        // readonly & (map<readonly>)[] which is then used for readonly table type when constructing VAL_READONLY.
        this.cellAtom(CELL_ATOMIC_INNER_MAPPING_RO);
        this.listAtom(LIST_ATOMIC_MAPPING_RO);
        this.cellAtom(CELL_ATOMIC_INNER_RO);
        // Reserving the next two indexes of atomTable to represent typeAtoms related to [any|error, any|error].
        // This is to avoid passing down env argument when doing streamSubtypeComplement operation.
        this.cellAtom(CELL_ATOMIC_UNDEF);
        this.listAtom(LIST_ATOMIC_TWO_ELEMENT);
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
            TypeAtom ta = this.atomTable.get(atomicType);
            if (ta != null) {
                return ta;
            } else {
                TypeAtom result = TypeAtom.createTypeAtom(this.atomTable.size(), atomicType);
                this.atomTable.put(result.atomicType(), result);
                return result;
            }
        }
    }

    public void insertAtomAtIndex(int index, AtomicType atomicType) {
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
}
