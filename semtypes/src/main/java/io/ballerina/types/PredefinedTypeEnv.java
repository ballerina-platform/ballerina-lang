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

package io.ballerina.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.types.Core.union;
import static io.ballerina.types.PredefinedType.CELL_SEMTYPE_INNER_MAPPING;
import static io.ballerina.types.PredefinedType.CELL_SEMTYPE_INNER_MAPPING_RO;
import static io.ballerina.types.PredefinedType.CELL_SEMTYPE_INNER_RO;
import static io.ballerina.types.PredefinedType.CELL_SEMTYPE_UNDEF;
import static io.ballerina.types.PredefinedType.CELL_SEMTYPE_VAL;
import static io.ballerina.types.PredefinedType.INNER;
import static io.ballerina.types.PredefinedType.INNER_READONLY;
import static io.ballerina.types.PredefinedType.MAPPING;
import static io.ballerina.types.PredefinedType.MAPPING_RO;
import static io.ballerina.types.PredefinedType.NEVER;
import static io.ballerina.types.PredefinedType.UNDEF;
import static io.ballerina.types.PredefinedType.VAL;
import static io.ballerina.types.TypeAtom.createTypeAtom;

/**
 * This is a utility class used to create various type atoms that needs to be initialized without an environment and
 * common to all environments. When we construct an {@code Env}, we can use {@code initializeEnv} to populate it with
 * those atoms.
 * NOTE: While this class lazy initialize all the atoms technically {@code PredefinedType} will cause it initialize
 * all the atoms currently.
 * @since 2201.10.0
 */
public final class PredefinedTypeEnv {

    private PredefinedTypeEnv() {
    }

    // Due to some reason spot bug thinks we are returning an array via getInstance(), if this is not public
    public static final PredefinedTypeEnv INSTANCE = new PredefinedTypeEnv();

    public static PredefinedTypeEnv getInstance() {
        return INSTANCE;
    }

    private final List<InitializedTypeAtom<CellAtomicType>> initializedCellAtoms = new ArrayList<>();
    private final List<InitializedTypeAtom<ListAtomicType>> initializedListAtoms = new ArrayList<>();
    private final List<ListAtomicType> initializedRecListAtoms = new ArrayList<>();
    private final List<MappingAtomicType> initializedRecMappingAtoms = new ArrayList<>();
    private final AtomicInteger nextAtomIndex = new AtomicInteger(0);

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

    private ListAtomicType listAtomicRO;
    private MappingAtomicType mappingAtomicRO;
    private TypeAtom atomCellInner;
    private TypeAtom atomCellInnerMapping;
    private TypeAtom atomCellInnerMappingRO;
    private TypeAtom atomCellInnerRO;
    private TypeAtom atomCellNever;
    private TypeAtom atomCellUndef;
    private TypeAtom atomCellVal;
    private TypeAtom atomListMapping;
    private TypeAtom atomListMappingRO;
    private TypeAtom atomListTwoElement;

    private void addInitializedCellAtom(CellAtomicType atom) {
        int index = nextAtomIndex.getAndIncrement();
        initializedCellAtoms.add(new InitializedTypeAtom<>(atom, index));
    }

    private void addInitializedListAtom(ListAtomicType atom) {
        int index = nextAtomIndex.getAndIncrement();
        initializedListAtoms.add(new InitializedTypeAtom<>(atom, index));
    }

    private int cellAtomIndex(CellAtomicType atom) {
        for (InitializedTypeAtom<CellAtomicType> initializedCellAtom : initializedCellAtoms) {
            if (initializedCellAtom.atomicType() == atom) {
                return initializedCellAtom.index();
            }
        }
        throw new IndexOutOfBoundsException();
    }

    private int listAtomIndex(ListAtomicType atom) {
        for (InitializedTypeAtom<ListAtomicType> initializedListAtom : initializedListAtoms) {
            if (initializedListAtom.atomicType() == atom) {
                return initializedListAtom.index();
            }
        }
        throw new IndexOutOfBoundsException();
    }

    synchronized CellAtomicType cellAtomicVal() {
        if (cellAtomicVal == null) {
            cellAtomicVal = CellAtomicType.from(VAL, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
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
            cellAtomicNever = CellAtomicType.from(NEVER, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
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
            callAtomicInner = CellAtomicType.from(INNER, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(callAtomicInner);
        }
        return callAtomicInner;
    }

    synchronized TypeAtom atomCellInner() {
        if (atomCellInner == null) {
            CellAtomicType cellAtomicInner = cellAtomicInner();
            atomCellInner = createTypeAtom(cellAtomIndex(cellAtomicInner), cellAtomicInner);
        }
        return atomCellInner;
    }

    synchronized CellAtomicType cellAtomicInnerMapping() {
        if (cellAtomicInnerMapping == null) {
            cellAtomicInnerMapping =
                    CellAtomicType.from(union(MAPPING, UNDEF), CellAtomicType.CellMutability.CELL_MUT_LIMITED);
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
                    CellAtomicType.from(union(MAPPING_RO, UNDEF), CellAtomicType.CellMutability.CELL_MUT_LIMITED);
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
            listAtomicMapping = ListAtomicType.from(
                    FixedLengthArray.empty(), CELL_SEMTYPE_INNER_MAPPING
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
            listAtomicMappingRO = ListAtomicType.from(FixedLengthArray.empty(), CELL_SEMTYPE_INNER_MAPPING_RO);
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
            cellAtomicInnerRO = CellAtomicType.from(INNER_READONLY, CellAtomicType.CellMutability.CELL_MUT_NONE);
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
            cellAtomicUndef = CellAtomicType.from(UNDEF, CellAtomicType.CellMutability.CELL_MUT_NONE);
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

    synchronized ListAtomicType listAtomicTwoElement() {
        if (listAtomicTwoElement == null) {
            listAtomicTwoElement =
                    ListAtomicType.from(FixedLengthArray.from(List.of(CELL_SEMTYPE_VAL), 2), CELL_SEMTYPE_UNDEF);
            addInitializedListAtom(listAtomicTwoElement);
        }
        return listAtomicTwoElement;
    }

    synchronized TypeAtom atomListTwoElement() {
        if (atomListTwoElement == null) {
            ListAtomicType listAtomicTwoElement = listAtomicTwoElement();
            atomListTwoElement = createTypeAtom(listAtomIndex(listAtomicTwoElement), listAtomicTwoElement);
        }
        return atomListTwoElement;
    }

    synchronized ListAtomicType listAtomicRO() {
        if (listAtomicRO == null) {
            listAtomicRO = ListAtomicType.from(FixedLengthArray.empty(), CELL_SEMTYPE_INNER_RO);
            initializedRecListAtoms.add(listAtomicRO);
        }
        return listAtomicRO;
    }

    synchronized MappingAtomicType mappingAtomicRO() {
        if (mappingAtomicRO == null) {
            mappingAtomicRO = MappingAtomicType.from(new String[]{}, new CellSemType[]{}, CELL_SEMTYPE_INNER_RO);
            initializedRecMappingAtoms.add(mappingAtomicRO);
        }
        return mappingAtomicRO;
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

    public int reservedRecAtomCount() {
        return Integer.max(initializedRecListAtoms.size(), initializedRecMappingAtoms.size());
    }

    private record InitializedTypeAtom<E extends AtomicType>(E atomicType, int index) {

    }

    public Optional<RecAtom> getPredefinedRecAtom(int index) {
        // NOTE: when adding new reserved rec atoms update the bir.ksy file as well
        if (isPredefinedRecAtom(index)) {
            return Optional.of(RecAtom.createRecAtom(index));
        }
        return Optional.empty();
    }

    public boolean isPredefinedRecAtom(int index) {
        return index < reservedRecAtomCount();
    }
}
