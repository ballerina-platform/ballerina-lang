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
import io.ballerina.runtime.internal.types.semtype.BListSubType;
import io.ballerina.runtime.internal.types.semtype.BMappingSubType;
import io.ballerina.runtime.internal.types.semtype.BObjectSubType;
import io.ballerina.runtime.internal.types.semtype.BTableSubType;
import io.ballerina.runtime.internal.types.semtype.FixedLengthArray;
import io.ballerina.runtime.internal.types.semtype.XmlUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_CELL;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_LIST;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_MAPPING;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_OBJECT;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_TABLE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.BT_XML;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.VT_INHERENTLY_IMMUTABLE;
import static io.ballerina.runtime.api.types.semtype.BasicTypeCode.VT_MASK;
import static io.ballerina.runtime.api.types.semtype.BddNode.bddAtom;
import static io.ballerina.runtime.api.types.semtype.Builder.basicSubType;
import static io.ballerina.runtime.api.types.semtype.Builder.basicTypeUnion;
import static io.ballerina.runtime.api.types.semtype.Builder.from;
import static io.ballerina.runtime.api.types.semtype.Builder.stringConst;
import static io.ballerina.runtime.api.types.semtype.Core.union;
import static io.ballerina.runtime.api.types.semtype.TypeAtom.createTypeAtom;

final class PredefinedTypeEnv {

    private static PredefinedTypeEnv instance;
    private static final int BDD_REC_ATOM_OBJECT_READONLY = 1;
    private static final RecAtom OBJECT_RO_REC_ATOM = RecAtom.createRecAtom(BDD_REC_ATOM_OBJECT_READONLY);
    private static final BddNode MAPPING_SUBTYPE_OBJECT_RO = bddAtom(OBJECT_RO_REC_ATOM);

    private final List<InitializedTypeAtom<CellAtomicType>> initializedCellAtoms = new ArrayList<>();
    private final List<InitializedTypeAtom<ListAtomicType>> initializedListAtoms = new ArrayList<>();
    private final List<InitializedTypeAtom<MappingAtomicType>> initializedMappingAtoms = new ArrayList<>();
    private final List<ListAtomicType> initializedRecListAtoms = new ArrayList<>();
    private final List<MappingAtomicType> initializedRecMappingAtoms = new ArrayList<>();
    private final AtomicInteger nextAtomIndex = new AtomicInteger(0);
    // This is to avoid passing down env argument when doing cell type operations.
    // Please refer to the cellSubtypeDataEnsureProper() in cell.bal
    private final Supplier<CellAtomicType> cellAtomicVal = new ConcurrentLazySupplierWithCallback<>(
            () -> CellAtomicType.from(basicTypeUnion(VT_MASK), CellAtomicType.CellMutability.CELL_MUT_LIMITED),
            this::addInitializedCellAtom
    );
    private final Supplier<TypeAtom> atomCellVal =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicVal, this::cellAtomIndex);
    private final Supplier<SemType> cellSemTypeVal = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellVal()))));
    private final Supplier<CellAtomicType> cellAtomicNever = new ConcurrentLazySupplierWithCallback<>(
            () -> CellAtomicType.from(SemType.from(0), CellAtomicType.CellMutability.CELL_MUT_LIMITED),
            this::addInitializedCellAtom
    );
    private final Supplier<TypeAtom> atomCellNever =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicNever, this::cellAtomIndex);
    // Represent the typeAtom required to construct equivalent subtypes of map<any|error> and (any|error)[].

    private final ConcurrentLazySupplier<SemType> inner =
            new ConcurrentLazySupplier<>(() -> SemType.from(VT_MASK | from(BasicTypeCode.BT_UNDEF).all()));
    private final Supplier<CellAtomicType> cellAtomicInner = new ConcurrentLazySupplierWithCallback<>(
            () -> CellAtomicType.from(inner.get(), CellAtomicType.CellMutability.CELL_MUT_LIMITED),
            this::addInitializedCellAtom
    );
    private final Supplier<TypeAtom> atomCellInner =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicInner, this::cellAtomIndex);
    private final Supplier<SemType> cellSemTypeInner = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellInner()))));
    // TypeAtoms related to (map<any|error>)[]. This is to avoid passing down env argument when doing
    // tableSubtypeComplement operation.
    private final Supplier<CellAtomicType> cellAtomicInnerMapping = new ConcurrentLazySupplierWithCallback<>(
            () -> CellAtomicType.from(union(Builder.mappingType(), Builder.undef()),
                    CellAtomicType.CellMutability.CELL_MUT_LIMITED),
            this::addInitializedCellAtom
    );
    private final Supplier<TypeAtom> atomCellInnerMapping =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicInnerMapping, this::cellAtomIndex);
    private final Supplier<ListAtomicType> listAtomicMapping = new ConcurrentLazySupplierWithCallback<>(
            () -> new ListAtomicType(FixedLengthArray.empty(), basicSubType(
                    BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellInnerMapping())))),
            this::addInitializedListAtom
    );
    private final Supplier<TypeAtom> atomListMapping =
            createTypeAtomSupplierFromCellAtomicSupplier(listAtomicMapping, this::listAtomIndex);
    // TypeAtoms related to readonly type. This is to avoid requiring context when referring to readonly type.
    // CELL_ATOMIC_INNER_MAPPING_RO & LIST_ATOMIC_MAPPING_RO are typeAtoms required to construct
    // readonly & (map<readonly>)[] which is then used for readonly table type when constructing VAL_READONLY
    private final Supplier<CellAtomicType> cellAtomicInnerMappingRO = new ConcurrentLazySupplierWithCallback<>(
            () -> CellAtomicType.from(union(Builder.mappingRO(), Builder.undef()),
                    CellAtomicType.CellMutability.CELL_MUT_LIMITED),
            this::addInitializedCellAtom
    );
    private final Supplier<TypeAtom> atomCellInnerMappingRO =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicInnerMappingRO, this::cellAtomIndex);
    private final Supplier<ListAtomicType> listAtomicMappingRO = new ConcurrentLazySupplierWithCallback<>(
            () -> new ListAtomicType(FixedLengthArray.empty(), basicSubType(
                    BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellInnerMappingRO())))),
            this::addInitializedListAtom
    );

    private final Supplier<BddNode> listSubtypeMapping = new ConcurrentLazySupplier<>(
            () -> bddAtom(atomListMapping.get()));
    private final Supplier<SemType> mappingArray = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_LIST, BListSubType.createDelegate(listSubtypeMapping.get())));
    private final Supplier<CellAtomicType> cellAtomicMappingArray = new ConcurrentLazySupplierWithCallback<>(() ->
            CellAtomicType.from(mappingArray.get(), CellAtomicType.CellMutability.CELL_MUT_LIMITED),
            this::addInitializedCellAtom);
    private final Supplier<TypeAtom> atomCellMappingArray = new ConcurrentLazySupplier<>(() -> {
        CellAtomicType cellAtom = cellAtomicMappingArray.get();
        return createTypeAtom(cellAtomIndex(cellAtom), cellAtom);
    });
    private final Supplier<SemType> cellSemTypeListSubtypeMapping = new ConcurrentLazySupplier<>(() ->
            basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellMappingArray.get()))));
    private final Supplier<ListAtomicType> listAtomicThreeElement = new ConcurrentLazySupplierWithCallback<>(
            () -> new ListAtomicType(
                    FixedLengthArray.from(new SemType[]{cellSemTypeListSubtypeMapping.get(), cellSemTypeVal.get()}, 3),
                    cellSemTypeVal.get()),
            this::addInitializedListAtom
    );
    private final Supplier<TypeAtom> atomListThreeElement = new ConcurrentLazySupplier<>(() -> {
        ListAtomicType listAtomic = listAtomicThreeElement.get();
        return createTypeAtom(listAtomIndex(listAtomic), listAtomic);
    });
    private final Supplier<TypeAtom> atomListMappingRO =
            createTypeAtomSupplierFromCellAtomicSupplier(listAtomicMappingRO, this::listAtomIndex);

    private final Supplier<CellAtomicType> cellAtomicUndef = new ConcurrentLazySupplierWithCallback<>(
            () -> CellAtomicType.from(Builder.undef(), CellAtomicType.CellMutability.CELL_MUT_NONE),
            this::addInitializedCellAtom
    );
    private final Supplier<TypeAtom> atomCellUndef =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicUndef, this::cellAtomIndex);
    private final Supplier<SemType> cellSemTypeUndef = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellUndef.get()))));

    private final Supplier<BddNode> listSubtypeMappingRO = new ConcurrentLazySupplier<>(() -> bddAtom(
            atomListMappingRO.get()));
    private final Supplier<SemType> mappingArrayRO = new ConcurrentLazySupplier<>(() -> basicSubType(
            BT_LIST, BListSubType.createDelegate(listSubtypeMappingRO.get())));
    private final Supplier<CellAtomicType> cellAtomicMappingArrayRO = new ConcurrentLazySupplierWithCallback<>(
            () -> CellAtomicType.from(mappingArrayRO.get(), CellAtomicType.CellMutability.CELL_MUT_LIMITED),
            this::addInitializedCellAtom
    );
    private final Supplier<TypeAtom> atomCellMappingArrayRO = new ConcurrentLazySupplier<>(() -> {
        CellAtomicType cellAtom = cellAtomicMappingArrayRO.get();
        return createTypeAtom(cellAtomIndex(cellAtom), cellAtom);
    });
    private final Supplier<SemType> cellSemTypeListSubtypeMappingRO = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellMappingArrayRO.get()))));
    private final Supplier<ListAtomicType> listAtomicThreeElementRO = new ConcurrentLazySupplierWithCallback<>(
            () -> new ListAtomicType(
                    FixedLengthArray.from(new SemType[]{cellSemTypeListSubtypeMappingRO.get(), cellSemTypeVal.get()},
                            3),
                    cellSemTypeUndef.get()),
            this::addInitializedListAtom
    );
    private final Supplier<TypeAtom> atomListThreeElementRO = new ConcurrentLazySupplier<>(() -> {
        ListAtomicType listAtomic = listAtomicThreeElementRO.get();
        return createTypeAtom(listAtomIndex(listAtomic), listAtomic);
    });

    private final Supplier<SemType> readonlyType = new ConcurrentLazySupplier<>(() -> unionOf(
            SemType.from(VT_INHERENTLY_IMMUTABLE),
            basicSubType(BT_LIST, BListSubType.createDelegate(bddSubtypeRo())),
            basicSubType(BT_MAPPING, BMappingSubType.createDelegate(bddSubtypeRo())),
            basicSubType(BT_OBJECT, BObjectSubType.createDelegate(MAPPING_SUBTYPE_OBJECT_RO)),
            basicSubType(BT_TABLE, BTableSubType.createDelegate(bddAtom(atomListThreeElementRO.get()))),
            basicSubType(BT_XML, XmlUtils.XML_SUBTYPE_RO)
    ));

    private final ConcurrentLazySupplier<SemType> innerReadOnly =
            new ConcurrentLazySupplier<>(() -> union(readonlyType.get(), inner.get()));
    private final Supplier<CellAtomicType> cellAtomicInnerRO = new ConcurrentLazySupplierWithCallback<>(
            () -> CellAtomicType.from(innerReadOnly.get(), CellAtomicType.CellMutability.CELL_MUT_NONE),
            this::addInitializedCellAtom
    );
    private final Supplier<TypeAtom> atomCellInnerRO =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicInnerRO, this::cellAtomIndex);
    private final Supplier<SemType> cellSemTypeInnerRO = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellInnerRO.get()))));
    private final Supplier<ListAtomicType> listAtomicRO = new ConcurrentLazySupplierWithCallback<>(
            () -> new ListAtomicType(FixedLengthArray.empty(), cellSemTypeInnerRO.get()),
            this.initializedRecListAtoms::add
    );
    private final Supplier<MappingAtomicType> mappingAtomicRO = new ConcurrentLazySupplierWithCallback<>(
            () -> new MappingAtomicType(new String[]{}, new SemType[]{}, cellSemTypeInnerRO.get()),
            initializedRecMappingAtoms::add
    );
    // TypeAtoms related to [any|error, any|error]. This is to avoid passing down env argument when doing
    // streamSubtypeComplement operation.
    private final Supplier<CellAtomicType> cellAtomicObjectMemberKind =
            new ConcurrentLazySupplierWithCallback<>(
                    () -> CellAtomicType.from(
                            union(stringConst("field"), stringConst("method")),
                            CellAtomicType.CellMutability.CELL_MUT_NONE),
                    this::addInitializedCellAtom);
    private final Supplier<TypeAtom> atomCellObjectMemberKind =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicObjectMemberKind, this::cellAtomIndex);
    private final Supplier<SemType> cellSemTypeObjectMemberKind = new ConcurrentLazySupplier<>(
            () -> Builder.basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellObjectMemberKind()))));
    private final Supplier<CellAtomicType> cellAtomicObjectMemberVisibility =
            new ConcurrentLazySupplierWithCallback<>(
                    () -> CellAtomicType.from(
                            union(stringConst("public"), stringConst("private")),
                            CellAtomicType.CellMutability.CELL_MUT_NONE),
                    this::addInitializedCellAtom);
    private final Supplier<TypeAtom> atomCellObjectMemberVisibility =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicObjectMemberVisibility, this::cellAtomIndex);
    private final Supplier<SemType> cellSemTypeObjectMemberVisibility = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellObjectMemberVisibility()))));
    private final Supplier<MappingAtomicType> mappingAtomicObjectMember = new ConcurrentLazySupplierWithCallback<>(
            () -> new MappingAtomicType(
                    new String[]{"kind", "value", "visibility"},
                    new SemType[]{cellSemTypeObjectMemberKind.get(), cellSemTypeVal.get(),
                            cellSemTypeObjectMemberVisibility.get()},
                    cellSemTypeUndef.get()),
            this::addInitializedMapAtom
    );
    private final Supplier<TypeAtom> atomMappingObjectMember =
            createTypeAtomSupplierFromCellAtomicSupplier(mappingAtomicObjectMember, this::mappingAtomIndex);
    private final Supplier<SemType> mappingSemTypeObjectMember = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_MAPPING, BMappingSubType.createDelegate(bddAtom(atomMappingObjectMember()))));
    private final Supplier<CellAtomicType> cellAtomicObjectMember =
            new ConcurrentLazySupplierWithCallback<>(
                    () -> CellAtomicType.from(
                            mappingSemTypeObjectMember.get(), CellAtomicType.CellMutability.CELL_MUT_UNLIMITED),
                    this::addInitializedCellAtom);
    private final Supplier<TypeAtom> atomCellObjectMember =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicObjectMember, this::cellAtomIndex);
    private final Supplier<SemType> cellSemTypeObjectMember = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellObjectMember()))));
    private final Supplier<MappingAtomicType> mappingAtomicObject = new ConcurrentLazySupplierWithCallback<>(
            () -> new MappingAtomicType(
                    new String[]{"$qualifiers"}, new SemType[]{cellSemTypeVal.get()},
                    cellSemTypeObjectMember.get()
            ),
            this::addInitializedMapAtom
    );
    private final Supplier<TypeAtom> atomMappingObject =
            createTypeAtomSupplierFromCellAtomicSupplier(mappingAtomicObject, this::mappingAtomIndex);
    private final Supplier<CellAtomicType> cellAtomicValRO =
            new ConcurrentLazySupplierWithCallback<>(
                    () -> CellAtomicType.from(
                            readonlyType.get(), CellAtomicType.CellMutability.CELL_MUT_NONE),
                    this::addInitializedCellAtom);
    private final Supplier<TypeAtom> atomCellValRO =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicValRO, this::cellAtomIndex);
    private final Supplier<SemType> cellSemTypeValRo = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellValRO()))));
    private final Supplier<MappingAtomicType> mappingAtomicObjectMemberRO = new ConcurrentLazySupplierWithCallback<>(
            () -> new MappingAtomicType(
                    new String[]{"kind", "value", "visibility"},
                    new SemType[]{cellSemTypeObjectMemberKind.get(), cellSemTypeValRo.get(),
                            cellSemTypeObjectMemberVisibility.get()},
                    cellSemTypeUndef.get()),
            this::addInitializedMapAtom
    );
    private final Supplier<TypeAtom> atomMappingObjectMemberRO =
            createTypeAtomSupplierFromCellAtomicSupplier(mappingAtomicObjectMemberRO, this::mappingAtomIndex);
    private final Supplier<SemType> mappingSemTypeObjectMemberRO = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_MAPPING, BMappingSubType.createDelegate(bddAtom(atomMappingObjectMemberRO()))));
    private final Supplier<CellAtomicType> cellAtomicObjectMemberRO =
            new ConcurrentLazySupplierWithCallback<>(
                    () -> CellAtomicType.from(
                            mappingSemTypeObjectMemberRO.get(), CellAtomicType.CellMutability.CELL_MUT_NONE),
                    this::addInitializedCellAtom);
    private final Supplier<TypeAtom> atomCellObjectMemberRO =
            createTypeAtomSupplierFromCellAtomicSupplier(cellAtomicObjectMemberRO, this::cellAtomIndex);
    private final Supplier<SemType> cellSemTypeObjectMemberRO = new ConcurrentLazySupplier<>(
            () -> basicSubType(BT_CELL, BCellSubType.createDelegate(bddAtom(atomCellObjectMemberRO()))));
    private final Supplier<MappingAtomicType> mappingAtomicObjectRO = new ConcurrentLazySupplierWithCallback<>(
            () -> new MappingAtomicType(
                    new String[]{"$qualifiers"}, new SemType[]{cellSemTypeVal.get()},
                    cellSemTypeObjectMemberRO.get()
            ),
            initializedRecMappingAtoms::add
    );

    private final Supplier<ListAtomicType> listAtomicTwoElement = new ConcurrentLazySupplierWithCallback<>(
            () -> new ListAtomicType(
                    FixedLengthArray.from(new SemType[]{cellSemTypeVal.get()}, 2),
                    cellSemTypeUndef.get()),
            this::addInitializedListAtom
    );
    private final Supplier<TypeAtom> atomListTwoElement = new ConcurrentLazySupplier<>(() -> {
        ListAtomicType listAtomic = listAtomicTwoElement.get();
        return createTypeAtom(listAtomIndex(listAtomic), listAtomic);
    });

    private PredefinedTypeEnv() {
    }

    private static SemType unionOf(SemType... types) {
        SemType accum = types[0];
        for (int i = 1; i < types.length; i++) {
            accum = union(accum, types[i]);
        }
        return accum;
    }

    private static BddNode bddSubtypeRo() {
        return bddAtom(RecAtom.createRecAtom(0));
    }


    public static synchronized PredefinedTypeEnv getInstance() {
        if (instance == null) {
            instance = new PredefinedTypeEnv();
            instance.initilize();
        }
        return instance;
    }

    private static <E extends AtomicType> Supplier<TypeAtom> createTypeAtomSupplierFromCellAtomicSupplier(
            Supplier<E> atomicTypeSupplier, IndexSupplier<E> indexSupplier) {
        return new ConcurrentLazySupplier<>(() -> {
            E atomicType = atomicTypeSupplier.get();
            int index = indexSupplier.get(atomicType);
            return createTypeAtom(index, atomicType);
        });
    }

    private void initilize() {
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

    CellAtomicType cellAtomicVal() {
        return cellAtomicVal.get();
    }

    TypeAtom atomCellVal() {
        return atomCellVal.get();
    }

    CellAtomicType cellAtomicNever() {
        return cellAtomicNever.get();
    }

    TypeAtom atomCellNever() {
        return atomCellNever.get();
    }

    CellAtomicType cellAtomicInner() {
        return cellAtomicInner.get();
    }

    TypeAtom atomCellInner() {
        return atomCellInner.get();
    }

    CellAtomicType cellAtomicInnerMapping() {
        return cellAtomicInnerMapping.get();
    }

    TypeAtom atomCellInnerMapping() {
        return atomCellInnerMapping.get();
    }

    CellAtomicType cellAtomicInnerMappingRO() {
        return cellAtomicInnerMappingRO.get();
    }

    TypeAtom atomCellInnerMappingRO() {
        return atomCellInnerMappingRO.get();
    }

    ListAtomicType listAtomicMapping() {
        return listAtomicMapping.get();
    }

    TypeAtom atomListMapping() {
        return atomListMapping.get();
    }

    ListAtomicType listAtomicMappingRO() {
        return listAtomicMappingRO.get();
    }

    TypeAtom atomListMappingRO() {
        return atomListMappingRO.get();
    }

    CellAtomicType cellAtomicInnerRO() {
        return cellAtomicInnerRO.get();
    }

    TypeAtom atomCellInnerRO() {
        return atomCellInnerRO.get();
    }

    CellAtomicType cellAtomicUndef() {
        return cellAtomicUndef.get();
    }

    TypeAtom atomCellUndef() {
        return atomCellUndef.get();
    }

    CellAtomicType cellAtomicValRO() {
        return cellAtomicValRO.get();
    }

    TypeAtom atomCellValRO() {
        return atomCellValRO.get();
    }

    MappingAtomicType mappingAtomicObjectMemberRO() {
        return mappingAtomicObjectMemberRO.get();
    }

    TypeAtom atomMappingObjectMemberRO() {
        return atomMappingObjectMemberRO.get();
    }

    CellAtomicType cellAtomicObjectMemberRO() {
        return cellAtomicObjectMemberRO.get();
    }

    TypeAtom atomCellObjectMemberRO() {
        return atomCellObjectMemberRO.get();
    }

    CellAtomicType cellAtomicObjectMemberKind() {
        return cellAtomicObjectMemberKind.get();
    }

    TypeAtom atomCellObjectMemberKind() {
        return atomCellObjectMemberKind.get();
    }

    CellAtomicType cellAtomicObjectMemberVisibility() {
        return cellAtomicObjectMemberVisibility.get();
    }

    TypeAtom atomCellObjectMemberVisibility() {
        return atomCellObjectMemberVisibility.get();
    }

    MappingAtomicType mappingAtomicObjectMember() {
        return mappingAtomicObjectMember.get();
    }

    TypeAtom atomMappingObjectMember() {
        return atomMappingObjectMember.get();
    }

    CellAtomicType cellAtomicObjectMember() {
        return cellAtomicObjectMember.get();
    }

    TypeAtom atomCellObjectMember() {
        return atomCellObjectMember.get();
    }

    MappingAtomicType mappingAtomicObject() {
        return mappingAtomicObject.get();
    }

    TypeAtom atomMappingObject() {
        return atomMappingObject.get();
    }

    ListAtomicType listAtomicRO() {
        return listAtomicRO.get();
    }

    MappingAtomicType mappingAtomicRO() {
        return mappingAtomicRO.get();
    }

    MappingAtomicType mappingAtomicObjectRO() {
        return mappingAtomicObjectRO.get();
    }

    TypeAtom atomListThreeElement() {
        return atomListThreeElement.get();
    }

    TypeAtom atomListThreeElementRO() {
        return atomListThreeElementRO.get();
    }

    SemType readonlyType() {
        return readonlyType.get();
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

    SemType cellSemTypeInner() {
        return cellSemTypeInner.get();
    }

    public Atom atomListTwoElement() {
        return atomListTwoElement.get();
    }

    @FunctionalInterface
    private interface IndexSupplier<E extends AtomicType> {

        int get(E atomicType);
    }

    private record InitializedTypeAtom<E extends AtomicType>(E atomicType, int index) {

    }
}
