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

    private static final List<InitializedTypeAtom<CellAtomicType>> initializedCellAtoms = new ArrayList<>();
    private static final List<InitializedTypeAtom<ListAtomicType>> initializedListAtoms = new ArrayList<>();
    private static final List<ListAtomicType> initializedRecListAtoms = new ArrayList<>();
    private static final List<MappingAtomicType> initializedRecMappingAtoms = new ArrayList<>();
    // 0 is reserved for BDD_REC_ATOM_READONLY
    private static AtomicInteger nextAtomIndex = new AtomicInteger(1);

    private static void addInitializedCellAtom(CellAtomicType atom) {
        int index = nextAtomIndex.getAndIncrement();
        initializedCellAtoms.add(new InitializedTypeAtom<>(atom, index));
    }

    private static void addInitializedListAtom(ListAtomicType atom) {
        int index = nextAtomIndex.getAndIncrement();
        initializedListAtoms.add(new InitializedTypeAtom<>(atom, index));
    }

    private static int cellAtomIndex(CellAtomicType atom) {
        for (InitializedTypeAtom<CellAtomicType> initializedCellAtom : initializedCellAtoms) {
            if (initializedCellAtom.atomicType() == atom) {
                return initializedCellAtom.index();
            }
        }
        throw new IndexOutOfBoundsException();
    }

    private static int listAtomIndex(ListAtomicType atom) {
        for (InitializedTypeAtom<ListAtomicType> initializedListAtom : initializedListAtoms) {
            if (initializedListAtom.atomicType() == atom) {
                return initializedListAtom.index();
            }
        }
        throw new IndexOutOfBoundsException();
    }

    private static CellAtomicType CELL_ATOMIC_VAL;

    synchronized static CellAtomicType cellAtomicVal() {
        if (CELL_ATOMIC_VAL == null) {
            assert VAL != null;
            CELL_ATOMIC_VAL = CellAtomicType.from(VAL, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(CELL_ATOMIC_VAL);
        }
        return CELL_ATOMIC_VAL;
    }

    private static TypeAtom ATOM_CELL_VAL;

    synchronized static TypeAtom atomCellVal() {
        if (ATOM_CELL_VAL == null) {
            CellAtomicType cellAtomicVal = cellAtomicVal();
            ATOM_CELL_VAL = createTypeAtom(cellAtomIndex(cellAtomicVal), cellAtomicVal);
        }
        return ATOM_CELL_VAL;
    }

    private static CellAtomicType CELL_ATOMIC_NEVER;

    synchronized static CellAtomicType cellAtomicNever() {
        if (CELL_ATOMIC_NEVER == null) {
            assert NEVER != null;
            CELL_ATOMIC_NEVER = CellAtomicType.from(NEVER, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(CELL_ATOMIC_NEVER);
        }
        return CELL_ATOMIC_NEVER;
    }

    private static TypeAtom ATOM_CELL_NEVER;

    synchronized static TypeAtom atomCellNever() {
        if (ATOM_CELL_NEVER == null) {
            CellAtomicType cellAtomicNever = cellAtomicNever();
            ATOM_CELL_NEVER = createTypeAtom(cellAtomIndex(cellAtomicNever), cellAtomicNever);
        }
        return ATOM_CELL_NEVER;
    }

    private static CellAtomicType CELL_ATOMIC_INNER;

    synchronized static CellAtomicType cellAtomicInner() {
        if (CELL_ATOMIC_INNER == null) {
            assert INNER != null;
            CELL_ATOMIC_INNER = CellAtomicType.from(INNER, CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(CELL_ATOMIC_INNER);
        }
        return CELL_ATOMIC_INNER;
    }

    private static TypeAtom ATOM_CELL_INNER;

    synchronized static TypeAtom atomCellInner() {
        if (ATOM_CELL_INNER == null) {
            CellAtomicType cellAtomicInner = cellAtomicInner();
            ATOM_CELL_INNER = createTypeAtom(cellAtomIndex(cellAtomicInner), cellAtomicInner);
        }
        return ATOM_CELL_INNER;
    }

    private static CellAtomicType CELL_ATOMIC_INNER_MAPPING;

    synchronized static CellAtomicType cellAtomicInnerMapping() {
        if (CELL_ATOMIC_INNER_MAPPING == null) {
            assert MAPPING != null && UNDEF != null;
            CELL_ATOMIC_INNER_MAPPING =
                    CellAtomicType.from(union(MAPPING, UNDEF), CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(CELL_ATOMIC_INNER_MAPPING);
        }
        return CELL_ATOMIC_INNER_MAPPING;
    }

    private static TypeAtom ATOM_CELL_INNER_MAPPING;

    synchronized static TypeAtom atomCellInnerMapping() {
        if (ATOM_CELL_INNER_MAPPING == null) {
            CellAtomicType cellAtomicInnerMapping = cellAtomicInnerMapping();
            ATOM_CELL_INNER_MAPPING = createTypeAtom(cellAtomIndex(cellAtomicInnerMapping), cellAtomicInnerMapping);
        }
        return ATOM_CELL_INNER_MAPPING;
    }

    private static CellAtomicType CELL_ATOMIC_INNER_MAPPING_RO;

    synchronized static CellAtomicType cellAtomicInnerMappingRO() {
        if (CELL_ATOMIC_INNER_MAPPING_RO == null) {
            assert MAPPING_RO != null && UNDEF != null;
            CELL_ATOMIC_INNER_MAPPING_RO =
                    CellAtomicType.from(union(MAPPING_RO, UNDEF), CellAtomicType.CellMutability.CELL_MUT_LIMITED);
            addInitializedCellAtom(CELL_ATOMIC_INNER_MAPPING_RO);
        }
        return CELL_ATOMIC_INNER_MAPPING_RO;
    }

    private static TypeAtom ATOM_CELL_INNER_MAPPING_RO;

    synchronized static TypeAtom atomCellInnerMappingRO() {
        if (ATOM_CELL_INNER_MAPPING_RO == null) {
            CellAtomicType cellAtomicInnerMappingRO = cellAtomicInnerMappingRO();
            ATOM_CELL_INNER_MAPPING_RO =
                    createTypeAtom(cellAtomIndex(cellAtomicInnerMappingRO), cellAtomicInnerMappingRO);
        }
        return ATOM_CELL_INNER_MAPPING_RO;
    }

    private static ListAtomicType LIST_ATOMIC_MAPPING;

    synchronized static ListAtomicType listAtomicMapping() {
        if (LIST_ATOMIC_MAPPING == null) {
            LIST_ATOMIC_MAPPING = ListAtomicType.from(
                    FixedLengthArray.empty(), CELL_SEMTYPE_INNER_MAPPING
            );
            addInitializedListAtom(LIST_ATOMIC_MAPPING);
        }
        return LIST_ATOMIC_MAPPING;
    }

    private static TypeAtom ATOM_LIST_MAPPING;

    synchronized static TypeAtom atomListMapping() {
        if (ATOM_LIST_MAPPING == null) {
            ListAtomicType listAtomicMapping = listAtomicMapping();
            ATOM_LIST_MAPPING = createTypeAtom(listAtomIndex(listAtomicMapping), listAtomicMapping);
        }
        return ATOM_LIST_MAPPING;
    }

    private static ListAtomicType LIST_ATOMIC_MAPPING_RO;

    synchronized static ListAtomicType listAtomicMappingRO() {
        if (LIST_ATOMIC_MAPPING_RO == null) {
            LIST_ATOMIC_MAPPING_RO = ListAtomicType.from(FixedLengthArray.empty(), CELL_SEMTYPE_INNER_MAPPING_RO);
            addInitializedListAtom(LIST_ATOMIC_MAPPING_RO);
        }
        return LIST_ATOMIC_MAPPING_RO;
    }

    private static TypeAtom ATOM_LIST_MAPPING_RO;

    synchronized static TypeAtom atomListMappingRO() {
        if (ATOM_LIST_MAPPING_RO == null) {
            ListAtomicType listAtomicMappingRO = listAtomicMappingRO();
            ATOM_LIST_MAPPING_RO = createTypeAtom(listAtomIndex(listAtomicMappingRO), listAtomicMappingRO);
        }
        return ATOM_LIST_MAPPING_RO;
    }

    private static CellAtomicType CELL_ATOMIC_INNER_RO;

    synchronized static CellAtomicType cellAtomicInnerRO() {
        if (CELL_ATOMIC_INNER_RO == null) {
            CELL_ATOMIC_INNER_RO = CellAtomicType.from(INNER_READONLY, CellAtomicType.CellMutability.CELL_MUT_NONE);
            addInitializedCellAtom(CELL_ATOMIC_INNER_RO);
        }
        return CELL_ATOMIC_INNER_RO;
    }

    private static TypeAtom ATOM_CELL_INNER_RO;

    synchronized static TypeAtom atomCellInnerRO() {
        if (ATOM_CELL_INNER_RO == null) {
            CellAtomicType cellAtomicInnerRO = cellAtomicInnerRO();
            ATOM_CELL_INNER_RO = createTypeAtom(cellAtomIndex(cellAtomicInnerRO), cellAtomicInnerRO);
        }
        return ATOM_CELL_INNER_RO;
    }

    private static CellAtomicType CELL_ATOMIC_UNDEF;

    synchronized static CellAtomicType cellAtomicUndef() {
        if (CELL_ATOMIC_UNDEF == null) {
            CELL_ATOMIC_UNDEF = CellAtomicType.from(UNDEF, CellAtomicType.CellMutability.CELL_MUT_NONE);
            addInitializedCellAtom(CELL_ATOMIC_UNDEF);
        }
        return CELL_ATOMIC_UNDEF;
    }

    private static TypeAtom ATOM_CELL_UNDEF;

    synchronized static TypeAtom atomCellUndef() {
        if (ATOM_CELL_UNDEF == null) {
            CellAtomicType cellAtomicUndef = cellAtomicUndef();
            ATOM_CELL_UNDEF = createTypeAtom(cellAtomIndex(cellAtomicUndef), cellAtomicUndef);
        }
        return ATOM_CELL_UNDEF;
    }

    private static ListAtomicType LIST_ATOMIC_TWO_ELEMENT;

    synchronized static ListAtomicType listAtomicTwoElement() {
        if (LIST_ATOMIC_TWO_ELEMENT == null) {
            LIST_ATOMIC_TWO_ELEMENT =
                    ListAtomicType.from(FixedLengthArray.from(List.of(CELL_SEMTYPE_VAL), 2), CELL_SEMTYPE_UNDEF);
            addInitializedListAtom(LIST_ATOMIC_TWO_ELEMENT);
        }
        return LIST_ATOMIC_TWO_ELEMENT;
    }

    private static TypeAtom ATOM_LIST_TWO_ELEMENT;

    synchronized static TypeAtom atomListTwoElement() {
        if (ATOM_LIST_TWO_ELEMENT == null) {
            ListAtomicType listAtomicTwoElement = listAtomicTwoElement();
            ATOM_LIST_TWO_ELEMENT = createTypeAtom(listAtomIndex(listAtomicTwoElement), listAtomicTwoElement);
        }
        return ATOM_LIST_TWO_ELEMENT;
    }

    private static ListAtomicType LIST_ATOMIC_RO;

    synchronized static ListAtomicType listAtomicRO() {
        if (LIST_ATOMIC_RO == null) {
            LIST_ATOMIC_RO = ListAtomicType.from(FixedLengthArray.empty(), CELL_SEMTYPE_INNER_RO);
            initializedRecListAtoms.add(LIST_ATOMIC_RO);
        }
        return LIST_ATOMIC_RO;
    }

    private static MappingAtomicType MAPPING_ATOMIC_RO;

    synchronized static MappingAtomicType mappingAtomicRO() {
        if (MAPPING_ATOMIC_RO == null) {
            MAPPING_ATOMIC_RO = MappingAtomicType.from(new String[]{}, new CellSemType[]{}, CELL_SEMTYPE_INNER_RO);
            initializedRecMappingAtoms.add(MAPPING_ATOMIC_RO);
        }
        return MAPPING_ATOMIC_RO;
    }

    static void initializeEnv(Env env) {
        fillRecAtoms(env.recListAtoms, initializedRecListAtoms);
        fillRecAtoms(env.recMappingAtoms, initializedRecMappingAtoms);
        initializedCellAtoms.forEach(each -> env.cellAtom(each.atomicType()));
        initializedListAtoms.forEach(each -> env.listAtom(each.atomicType()));
    }

    private static <E extends AtomicType> void fillRecAtoms(List<E> envRecAtomList, List<E> initializedRecAtoms) {
        int count = reservedAtomCount();
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

    private static int reservedAtomCount() {
        return Integer.max(initializedRecListAtoms.size(), initializedRecMappingAtoms.size());
    }

    private PredefinedTypeEnv() {
    }

    private record InitializedTypeAtom<E extends AtomicType>(E atomicType, int index) {

    }

    public static Optional<RecAtom> getPredefinedRecAtom(int index) {
        // NOTE: when adding new reserved rec atoms update the bir.ksy file as well
        if (isPredefinedRecAtom(index)) {
            return Optional.of(RecAtom.createRecAtom(index));
        }
        return Optional.empty();
    }

    public static boolean isPredefinedRecAtom(int index) {
        return index < reservedAtomCount();
    }
}
