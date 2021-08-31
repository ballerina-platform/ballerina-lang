/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.semtype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Env node.
 *
 * @since 2.0.0
 */
public class Env {
    private final HashMap<AtomicType, TypeAtom> atomTable;
    private final List<ListAtomicType> recListAtoms;
    private final List<MappingAtomicType> recMappingAtoms;
    private final List<FunctionAtomicType> recFunctionAtoms;

    public Env() {
        this.atomTable = new HashMap<>();
        // Set up index 0 for use by bddFixReadOnly
        this.recListAtoms = new ArrayList<>();
        this.recListAtoms.add(ListAtomicType.LIST_SUBTYPE_RO);

        this.recMappingAtoms = new ArrayList<>();
        this.recMappingAtoms.add(MappingAtomicType.MAPPING_SUBTYPE_RO);

        this.recFunctionAtoms = new ArrayList<>();
    }

    public synchronized RecAtom recFunctionAtom() {
        int result = this.recFunctionAtoms.size();
        // represents adding () in nballerina
        this.recFunctionAtoms.add(null);
        return new RecAtom(result);
    }

    public synchronized void setRecFunctionAtomType(RecAtom ra, FunctionAtomicType atomicType) {
        this.recFunctionAtoms.set(ra.index, atomicType);
    }

    public FunctionAtomicType getRecFunctionAtomType(RecAtom ra) {
        return this.recFunctionAtoms.get(ra.index);
    }

    public TypeAtom listAtom(ListAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    public TypeAtom mappingAtom(MappingAtomicType atomicType) {
        return this.typeAtom(atomicType);
    }

    private TypeAtom typeAtom(AtomicType atomicType) {
        TypeAtom ta = this.atomTable.get(atomicType);
        if (ta != null) {
            return ta;
        } else {
            TypeAtom result = TypeAtom.createTypeAtom(this.atomTable.size(), atomicType);
            this.atomTable.put(result.atomicType, result);
            return result;
        }
    }

    public ListAtomicType listAtomType(Atom atom) {
        if (atom instanceof RecAtom) {
            return getRecListAtomType((RecAtom) atom);
        } else {
            return (ListAtomicType) ((TypeAtom) atom).atomicType;
        }
    }

    public MappingAtomicType mappingAtomType(Atom atom) {
        if (atom instanceof RecAtom) {
            return getRecMappingAtomType((RecAtom) atom);
        } else {
            return (MappingAtomicType) ((TypeAtom) atom).atomicType;
        }
    }

    public RecAtom recListAtom() {
        int result = this.recListAtoms.size();
        this.recListAtoms.add(null);
        return RecAtom.createRecAtom(result);
    }

    public RecAtom recMappingAtom() {
        int result = this.recMappingAtoms.size();
        this.recMappingAtoms.add(null);
        return RecAtom.createRecAtom(result);
    }

    public void setRecListAtomType(RecAtom ra, ListAtomicType atomicType) {
        this.recListAtoms.set(ra.index, atomicType);
    }

    public void setRecMappingAtomType(RecAtom ra, MappingAtomicType atomicType) {
        this.recMappingAtoms.set(ra.index, atomicType);
    }

    public ListAtomicType getRecListAtomType(RecAtom ra) {
        return (ListAtomicType) this.recListAtoms.get(ra.index);
    }

    public MappingAtomicType getRecMappingAtomType(RecAtom ra) {
        return (MappingAtomicType) this.recMappingAtoms.get(ra.index);
    }
}
