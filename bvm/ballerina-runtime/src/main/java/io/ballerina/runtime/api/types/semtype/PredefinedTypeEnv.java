/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.runtime.api.types.semtype;

import io.ballerina.runtime.internal.types.semtype.BCellSubType;
import io.ballerina.runtime.internal.types.semtype.BMappingSubType;
import io.ballerina.runtime.internal.types.semtype.FixedLengthArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_CELL;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_MAPPING;
import static io.ballerina.runtime.api.types.semtype.BddNode.bddAtom;
import static io.ballerina.runtime.api.types.semtype.Builder.basicSubType;
import static io.ballerina.runtime.api.types.semtype.Builder.stringConst;
import static io.ballerina.runtime.api.types.semtype.Core.union;
import static io.ballerina.runtime.api.types.semtype.TypeAtom.createTypeAtom;

final class PredefinedTypeEnv {

    private PredefinedTypeEnv() {
    }

    private void initilizeEnv() {
        // Initialize RecAtoms
        mappingAtomicRO();
        listAtomicRO();
        mappingAtomicObjectRO();

        // initialize atomic types
        cellAtomicVal();
        cellAtomicNever();
        cellAtomicInner();
        cellAtomicInnerMapping();
        listAtomicMapping();
        cellAtomicInner();
        listAtomicMappingRO();
        cellAtomicInnerRO();
    }

    private static PredefinedTypeEnv INSTANCE;

    public synchronized static PredefinedTypeEnv getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PredefinedTypeEnv();
            INSTANCE.initilizeEnv();
        }
        return INSTANCE;
    }

    private final List<InitializedTypeAtom<CellAtomicType>> initializedCellAtoms = new ArrayList<>();
    private final List<InitializedTypeAtom<ListAtomicType>> initializedListAtoms = new ArrayList<>();
    private final List<InitializedTypeAtom<MappingAtomicType>> initializedMappingAtoms = new ArrayList<>();
    private final List<ListAtomicType> initializedRecListAtoms = new ArrayList<>();
    private final List<MappingAtomicType> initializedRecMappingAtoms = new ArrayList<>();
    private final AtomicInteger nextAtomIndex = new AtomicInteger(0);

    // FIXME: instead use enums and enum map
    // This is to avoid passing down env argument when doing cell type operations.
    // Please refer to the cellSubtypeDataEnsureProper() in cell.bal
    private CellAtomicType cellAtomicVal;
    private CellAtomicType cellAtomicNever;

    // Represent the typeAtom required to construct equivalent subtypes of map<any|error> and (any|error)[].
    private CellAtomicType callAtomicInner;

    // TypeAtoms related to (map<any|error>)[]. This is to avoid passing down env argument when doing
    // tableSubtypeComplement operation.
    private CellAtomicType cellAtomicInnerMapping;
    private ListAtomicType listAtomicMapping;

    // TypeAtoms related to readonly type. This is to avoid requiring context when referring to readonly type.
    // CELL_ATOMIC_INNER_MAPPING_RO & LIST_ATOMIC_MAPPING_RO are typeAtoms required to construct
    // readonly & (map<readonly>)[] which is then used for readonly table type when constructing VAL_READONLY
    private CellAtomicType cellAtomicInnerMappingRO;
    private ListAtomicType listAtomicMappingRO;
    private CellAtomicType cellAtomicInnerRO;

    // TypeAtoms related to [any|error, any|error]. This is to avoid passing down env argument when doing
    // streamSubtypeComplement operation.
    private CellAtomicType cellAtomicUndef;
    private ListAtomicType listAtomicTwoElement;

    private CellAtomicType cellAtomicObjectMember;
    private CellAtomicType cellAtomicObjectMemberKind;
    private CellAtomicType cellAtomicObjectMemberRO;
    private CellAtomicType cellAtomicObjectMemberVisibility;
    private CellAtomicType cellAtomicValRO;
    private ListAtomicType listAtomicRO;
    private MappingAtomicType mappingAtomicObject;
    private MappingAtomicType mappingAtomicObjectMember;
    private MappingAtomicType mappingAtomicObjectMemberRO;
    private MappingAtomicType mappingAtomicObjectRO;
    private MappingAtomicType mappingAtomicRO;
    private TypeAtom atomCellInner;
    private TypeAtom atomCellInnerMapping;
    private TypeAtom atomCellInnerMappingRO;
    private TypeAtom atomCellInnerRO;
    private TypeAtom atomCellNever;
    private TypeAtom atomCellObjectMember;
    private TypeAtom atomCellObjectMemberKind;
    private TypeAtom atomCellObjectMemberRO;
    private TypeAtom atomCellObjectMemberVisibility;
    private TypeAtom atomCellUndef;
    private TypeAtom atomCellVal;
    private TypeAtom atomCellValRO;
    private TypeAtom atomListMapping;
    private TypeAtom atomListMappingRO;
    private TypeAtom atomMappingObject;
    private TypeAtom atomMappingObjectMember;
    private TypeAtom atomMappingObjectMemberRO;

    private void addInitializedCellAtom(CellAtomicType atom) {
        addInitializedAtom(initializedCellAtoms, atom);
    }

    private void addInitializedListAtom(ListAtomicType atom) {
        addInitializedAtom(initializedListAtoms, atom);
    }

    private void addInitializedMapAtom(MappingAtomicType atom) {
        addInitializedAtom(initializedMappingAtoms, atom);
    }

    private <E extends AtomicType> void addInitializedAtom(Collection<? super InitializedTypeAtom<E>> atoms, E atom) {
        atoms.add(new InitializedTypeAtom<>(atom, nextAtomIndex.getAndIncrement()));
    }

    private int cellAtomIndex(CellAtomicType atom) {
        return atomIndex(initializedCellAtoms, atom);
    }

    private int listAtomIndex(ListAtomicType atom) {
        return atomIndex(initializedListAtoms, atom);
    }

    private int mappingAtomIndex(MappingAtomicType atom) {
        return atomIndex(initializedMappingAtoms, atom);
    }

    private <E extends AtomicType> int atomIndex(List<InitializedTypeAtom<E>> initializedAtoms, E atom) {
        for (InitializedTypeAtom<E> initializedListAtom : initializedAtoms) {
            if (initializedListAtom.atomicType() == atom) {
                return initializedListAtom.index();
            }
        }
        throw new IndexOutOfBoundsException();
    }

    synchronized CellAtomicType cellAtomicVal() {
        if (cellAtomicVal == null) {
            cellAtomicVal = CellAtomicType.from(Builder.valType(), CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(cellAtomicVal);
        }
        return cellAtomicVal;
    }

    synchronized TypeAtom atomCellVal() {
        if (atomCellVal == null) {
            CellAtomicType cellAtomicVal = cellAtomicVal();
            atomCellVal = createTypeAtom(cellAtomIndex(cellAtomicVal), cellAtomicVal);
        }
        return atomCellVal;
    }

    synchronized CellAtomicType cellAtomicNever() {
        if (cellAtomicNever == null) {
            cellAtomicNever = CellAtomicType.from(Builder.neverType(), CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(cellAtomicNever);
        }
        return cellAtomicNever;
    }

    synchronized TypeAtom atomCellNever() {
        if (atomCellNever == null) {
            CellAtomicType cellAtomicNever = cellAtomicNever();
            atomCellNever = createTypeAtom(cellAtomIndex(cellAtomicNever), cellAtomicNever);
        }
        return atomCellNever;
    }

    synchronized CellAtomicType cellAtomicInner() {
        if (callAtomicInner == null) {
            callAtomicInner = CellAtomicType.from(Builder.inner(), CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(callAtomicInner);
        }
        return callAtomicInner;
    }

    synchronized TypeAtom atomCellInner() {
        if (atomCellInner == null) {
            CellAtomicType cellAtomicInner = this.cellAtomicInner();
            atomCellInner = createTypeAtom(cellAtomIndex(cellAtomicInner), cellAtomicInner);
        }
        return atomCellInner;
    }

    synchronized CellAtomicType cellAtomicInnerMapping() {
        if (cellAtomicInnerMapping == null) {
            cellAtomicInnerMapping =
                    CellAtomicType.from(union(Builder.mappingType(), Builder.undef()),
                            CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(cellAtomicInnerMapping);
        }
        return cellAtomicInnerMapping;
    }

    synchronized TypeAtom atomCellInnerMapping() {
        if (atomCellInnerMapping == null) {
            CellAtomicType cellAtomicInnerMapping = cellAtomicInnerMapping();
            atomCellInnerMapping = createTypeAtom(cellAtomIndex(cellAtomicInnerMapping), cellAtomicInnerMapping);
        }
        return atomCellInnerMapping;
    }

    synchronized CellAtomicType cellAtomicInnerMappingRO() {
        if (cellAtomicInnerMappingRO == null) {
            cellAtomicInnerMappingRO =
                    CellAtomicType.from(union(Builder.mappingRO(), Builder.undef()),
                            CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(cellAtomicInnerMappingRO);
        }
        return cellAtomicInnerMappingRO;
    }

    synchronized TypeAtom atomCellInnerMappingRO() {
        if (atomCellInnerMappingRO == null) {
            CellAtomicType cellAtomicInnerMappingRO = cellAtomicInnerMappingRO();
            atomCellInnerMappingRO =
                    createTypeAtom(cellAtomIndex(cellAtomicInnerMappingRO), cellAtomicInnerMappingRO);
        }
        return atomCellInnerMappingRO;
    }

    synchronized ListAtomicType listAtomicMapping() {
        if (listAtomicMapping == null) {
            listAtomicMapping = new ListAtomicType(
                    FixedLengthArray.empty(), basicSubType(
                    BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellInnerMapping())))
            );
            addInitializedListAtom(listAtomicMapping);
        }
        return listAtomicMapping;
    }

    synchronized TypeAtom atomListMapping() {
        if (atomListMapping == null) {
            ListAtomicType listAtomicMapping = listAtomicMapping();
            atomListMapping = createTypeAtom(listAtomIndex(listAtomicMapping), listAtomicMapping);
        }
        return atomListMapping;
    }

    synchronized ListAtomicType listAtomicMappingRO() {
        if (listAtomicMappingRO == null) {
            listAtomicMappingRO = new ListAtomicType(FixedLengthArray.empty(), basicSubType(
                    BT_CELL,
                    BCellSubType.createDelegate(bddAtom(atomCellInnerMappingRO()))));
            addInitializedListAtom(listAtomicMappingRO);
        }
        return listAtomicMappingRO;
    }

    synchronized TypeAtom atomListMappingRO() {
        if (atomListMappingRO == null) {
            ListAtomicType listAtomicMappingRO = listAtomicMappingRO();
            atomListMappingRO = createTypeAtom(listAtomIndex(listAtomicMappingRO), listAtomicMappingRO);
        }
        return atomListMappingRO;
    }

    synchronized CellAtomicType cellAtomicInnerRO() {
        if (cellAtomicInnerRO == null) {
            cellAtomicInnerRO =
                    CellAtomicType.from(Builder.innerReadOnly(), CellAtomicType.CellMutability.CELL_MUT_NONE);
            addInitializedCellAtom(cellAtomicInnerRO);
        }
        return cellAtomicInnerRO;
    }

    synchronized TypeAtom atomCellInnerRO() {
        if (atomCellInnerRO == null) {
            CellAtomicType cellAtomicInnerRO = cellAtomicInnerRO();
            atomCellInnerRO = createTypeAtom(cellAtomIndex(cellAtomicInnerRO), cellAtomicInnerRO);
        }
        return atomCellInnerRO;
    }

    synchronized CellAtomicType cellAtomicUndef() {
        if (cellAtomicUndef == null) {
            cellAtomicUndef = CellAtomicType.from(Builder.undef(), CellAtomicType.CellMutability.CELL_MUT_NONE);
            addInitializedCellAtom(cellAtomicUndef);
        }
        return cellAtomicUndef;
    }

    synchronized TypeAtom atomCellUndef() {
        if (atomCellUndef == null) {
            CellAtomicType cellAtomicUndef = cellAtomicUndef();
            atomCellUndef = createTypeAtom(cellAtomIndex(cellAtomicUndef), cellAtomicUndef);
        }
        return atomCellUndef;
    }

    synchronized CellAtomicType cellAtomicValRO() {
        if (cellAtomicValRO == null) {
            cellAtomicValRO = CellAtomicType.from(
                    Builder.readonlyType(), CellAtomicType.CellMutability.CELL_MUT_NONE
            );
            addInitializedCellAtom(cellAtomicValRO);
        }
        return cellAtomicValRO;
    }

    synchronized TypeAtom atomCellValRO() {
        if (atomCellValRO == null) {
            CellAtomicType cellAtomicValRO = cellAtomicValRO();
            atomCellValRO = createTypeAtom(cellAtomIndex(cellAtomicValRO), cellAtomicValRO);
        }
        return atomCellValRO;
    }

    synchronized MappingAtomicType mappingAtomicObjectMemberRO() {
        if (mappingAtomicObjectMemberRO == null) {
            mappingAtomicObjectMemberRO = new MappingAtomicType(
                    new String[]{"kind", "value", "visibility"},
                    new SemType[]{cellSemTypeObjectMemberKind(), cellSemTypeValRO(),
                            cellSemTypeObjectMemberVisibility()},
                    cellSemTypeUndef());
            addInitializedMapAtom(mappingAtomicObjectMemberRO);
        }
        return mappingAtomicObjectMemberRO;
    }

    synchronized TypeAtom atomMappingObjectMemberRO() {
        if (atomMappingObjectMemberRO == null) {
            MappingAtomicType mappingAtomicObjectMemberRO = mappingAtomicObjectMemberRO();
            atomMappingObjectMemberRO = createTypeAtom(mappingAtomIndex(mappingAtomicObjectMemberRO),
                    mappingAtomicObjectMemberRO);
        }
        return atomMappingObjectMemberRO;
    }

    synchronized CellAtomicType cellAtomicObjectMemberRO() {
        if (cellAtomicObjectMemberRO == null) {
            cellAtomicObjectMemberRO = CellAtomicType.from(
                    mappingSemTypeObjectMemberRO(), CellAtomicType.CellMutability.CELL_MUT_NONE
            );
            addInitializedCellAtom(cellAtomicObjectMemberRO);
        }
        return cellAtomicObjectMemberRO;
    }

    synchronized TypeAtom atomCellObjectMemberRO() {
        if (atomCellObjectMemberRO == null) {
            CellAtomicType cellAtomicObjectMemberRO = cellAtomicObjectMemberRO();
            atomCellObjectMemberRO = createTypeAtom(cellAtomIndex(cellAtomicObjectMemberRO), cellAtomicObjectMemberRO);
        }
        return atomCellObjectMemberRO;
    }

    synchronized CellAtomicType cellAtomicObjectMemberKind() {
        if (cellAtomicObjectMemberKind == null) {
            cellAtomicObjectMemberKind = CellAtomicType.from(
                    union(stringConst("field"), stringConst("method")),
                    CellAtomicType.CellMutability.CELL_MUT_NONE
            );
            addInitializedCellAtom(cellAtomicObjectMemberKind);
        }
        return cellAtomicObjectMemberKind;
    }

    synchronized TypeAtom atomCellObjectMemberKind() {
        if (atomCellObjectMemberKind == null) {
            CellAtomicType cellAtomicObjectMemberKind = cellAtomicObjectMemberKind();
            atomCellObjectMemberKind =
                    createTypeAtom(cellAtomIndex(cellAtomicObjectMemberKind), cellAtomicObjectMemberKind);
        }
        return atomCellObjectMemberKind;
    }

    synchronized CellAtomicType cellAtomicObjectMemberVisibility() {
        if (cellAtomicObjectMemberVisibility == null) {
            cellAtomicObjectMemberVisibility = CellAtomicType.from(
                    union(stringConst("public"), stringConst("private")),
                    CellAtomicType.CellMutability.CELL_MUT_NONE
            );
            addInitializedCellAtom(cellAtomicObjectMemberVisibility);
        }
        return cellAtomicObjectMemberVisibility;
    }

    synchronized TypeAtom atomCellObjectMemberVisibility() {
        if (atomCellObjectMemberVisibility == null) {
            CellAtomicType cellAtomicObjectMemberVisibility = cellAtomicObjectMemberVisibility();
            atomCellObjectMemberVisibility = createTypeAtom(cellAtomIndex(cellAtomicObjectMemberVisibility),
                    cellAtomicObjectMemberVisibility);
        }
        return atomCellObjectMemberVisibility;
    }

    synchronized MappingAtomicType mappingAtomicObjectMember() {
        if (mappingAtomicObjectMember == null) {
            mappingAtomicObjectMember = new MappingAtomicType(
                    new String[]{"kind", "value", "visibility"},
                    new SemType[]{cellSemTypeObjectMemberKind(), cellSemTypeVal(),
                            cellSemTypeObjectMemberVisibility()},
                    cellSemTypeUndef());
            ;
            addInitializedMapAtom(mappingAtomicObjectMember);
        }
        return mappingAtomicObjectMember;
    }

    synchronized TypeAtom atomMappingObjectMember() {
        if (atomMappingObjectMember == null) {
            MappingAtomicType mappingAtomicObjectMember = mappingAtomicObjectMember();
            atomMappingObjectMember = createTypeAtom(mappingAtomIndex(mappingAtomicObjectMember),
                    mappingAtomicObjectMember);
        }
        return atomMappingObjectMember;
    }

    synchronized CellAtomicType cellAtomicObjectMember() {
        if (cellAtomicObjectMember == null) {
            cellAtomicObjectMember = CellAtomicType.from(
                    mappingSemTypeObjectMember(), CellAtomicType.CellMutability.CELL_MUT_UNLIMITED
            );
            addInitializedCellAtom(cellAtomicObjectMember);
        }
        return cellAtomicObjectMember;
    }

    synchronized TypeAtom atomCellObjectMember() {
        if (atomCellObjectMember == null) {
            CellAtomicType cellAtomicObjectMember = cellAtomicObjectMember();
            atomCellObjectMember = createTypeAtom(cellAtomIndex(cellAtomicObjectMember), cellAtomicObjectMember);
        }
        return atomCellObjectMember;
    }

    synchronized MappingAtomicType mappingAtomicObject() {
        if (mappingAtomicObject == null) {
            mappingAtomicObject = new MappingAtomicType(
                    new String[]{"$qualifiers"}, new SemType[]{cellSemTypeVal()},
                    cellSemTypeObjectMember()
            );
            addInitializedMapAtom(mappingAtomicObject);
        }
        return mappingAtomicObject;
    }

    synchronized TypeAtom atomMappingObject() {
        if (atomMappingObject == null) {
            MappingAtomicType mappingAtomicObject = mappingAtomicObject();
            atomMappingObject = createTypeAtom(mappingAtomIndex(mappingAtomicObject), mappingAtomicObject);
        }
        return atomMappingObject;
    }

    synchronized ListAtomicType listAtomicRO() {
        if (listAtomicRO == null) {
            listAtomicRO = new ListAtomicType(FixedLengthArray.empty(), cellSemTypeInnerRO());
            initializedRecListAtoms.add(listAtomicRO);
        }
        return listAtomicRO;
    }

    synchronized MappingAtomicType mappingAtomicRO() {
        if (mappingAtomicRO == null) {
            mappingAtomicRO = new MappingAtomicType(new String[]{}, new SemType[]{}, cellSemTypeInnerRO());
            initializedRecMappingAtoms.add(mappingAtomicRO);
        }
        return mappingAtomicRO;
    }

    synchronized MappingAtomicType mappingAtomicObjectRO() {
        if (mappingAtomicObjectRO == null) {
            mappingAtomicObjectRO = new MappingAtomicType(
                    new String[]{"$qualifiers"}, new SemType[]{cellSemTypeVal()},
                    cellSemTypeObjectMemberRO()
            );
            initializedRecMappingAtoms.add(mappingAtomicObjectRO);
        }
        return mappingAtomicObjectRO;
    }

    // Due to some reason SpotBug thinks this method is overrideable if we don't put final here as well.
    final void initializeEnv(Env env) {
        fillRecAtoms(env.recListAtoms, initializedRecListAtoms);
        fillRecAtoms(env.recMappingAtoms, initializedRecMappingAtoms);
        initializedCellAtoms.forEach(each -> env.cellAtom(each.atomicType()));
        initializedListAtoms.forEach(each -> env.listAtom(each.atomicType()));
    }

    private <E extends AtomicType> void fillRecAtoms(List<E> envRecAtomList, List<E> initializedRecAtoms) {
        int count = reservedRecAtomCount();
        for (int i = 0; i < count; i++) {
            if (i < initializedRecAtoms.size()) {
                envRecAtomList.add(initializedRecAtoms.get(i));
            } else {
                // This is mainly to help with bir serialization/deserialization logic. Given the number of such atoms
                // will be small this shouldn't be a problem.
                envRecAtomList.add(null);
            }
        }
    }

    private int reservedRecAtomCount() {
        return Integer.max(initializedRecListAtoms.size(), initializedRecMappingAtoms.size());
    }

    // FIXME: avoid creating these multiple times
    private SemType cellSemTypeObjectMemberKind() {
        return Builder.basicSubType(
                BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellObjectMemberKind()))
        );
    }

    private SemType cellSemTypeValRO() {
        return basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellValRO())));
    }

    private SemType cellSemTypeObjectMemberVisibility() {
        return basicSubType(
                BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellObjectMemberVisibility()))
        );
    }

    private SemType cellSemTypeUndef() {
        return basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellUndef())));
    }

    private SemType mappingSemTypeObjectMemberRO() {
        return basicSubType(BT_MAPPING, BMappingSubType.createDelegate(bddAtom(atomMappingObjectMemberRO())));
    }

    private SemType cellSemTypeVal() {
        return basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellVal())));
    }

    private SemType mappingSemTypeObjectMember() {
        return basicSubType(BT_MAPPING, BMappingSubType.createDelegate(bddAtom(atomMappingObjectMember())));
    }

    private SemType cellSemTypeObjectMember() {
        return basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellObjectMember())));
    }

    private SemType cellSemTypeObjectMemberRO() {
        return basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellObjectMemberRO())));
    }

    SemType cellSemTypeInner() {
        return basicSubType(BT_CELL,
                BCellSubType.createDelegate(bddAtom(atomCellInner())));
    }

    private SemType cellSemTypeInnerRO() {
        return basicSubType(
                BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellInnerRO())));
    }

    private record InitializedTypeAtom<E extends AtomicType>(E atomicType, int index) {

    }
}
