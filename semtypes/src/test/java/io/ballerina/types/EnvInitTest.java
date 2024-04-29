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
package io.ballerina.types;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static io.ballerina.types.Core.union;

/**
 * Test class for {@link Env} initialization.
 *
 * @since 2201.10.0
 */
public class EnvInitTest {

    @Test
    public void testEnvInitAtomTable() throws NoSuchFieldException, IllegalAccessException {
        final Env env = new Env();

        // Access the private field atomTable using reflection
        Field atomTableField = Env.class.getDeclaredField("atomTable");
        atomTableField.setAccessible(true);
        Map<AtomicType, TypeAtom> atomTable = (Map) atomTableField.get(env);

        // Check that the atomTable contains the expected entries
        Assert.assertEquals(atomTable.size(), 8);

        // -------------------------------------------------------------------------------
        // Index 0
        // -------------------------------------------------------------------------------
        CellAtomicType cellAtomicVal = CellAtomicType.from(
                PredefinedType.VAL, CellAtomicType.CellMutability.CELL_MUT_LIMITED
        );

        TypeAtom typeAtom0 = atomTable.get(cellAtomicVal);
        Assert.assertEquals(typeAtom0.index(), 0);
        Assert.assertEquals(typeAtom0.atomicType(), cellAtomicVal);

        // -------------------------------------------------------------------------------
        // Index 1
        // -------------------------------------------------------------------------------
        CellAtomicType cellAtomicNever = CellAtomicType.from(
                PredefinedType.NEVER, CellAtomicType.CellMutability.CELL_MUT_LIMITED
        );

        TypeAtom typeAtom1 = atomTable.get(cellAtomicNever);
        Assert.assertEquals(typeAtom1.index(), 1);
        Assert.assertEquals(typeAtom1.atomicType(), cellAtomicNever);

        // -------------------------------------------------------------------------------
        // Index 2
        // -------------------------------------------------------------------------------
        CellAtomicType cellAtomicInner = CellAtomicType.from(
                PredefinedType.INNER, CellAtomicType.CellMutability.CELL_MUT_LIMITED
        );

        TypeAtom typeAtom2 = atomTable.get(cellAtomicInner);
        Assert.assertEquals(typeAtom2.index(), 2);
        Assert.assertEquals(typeAtom2.atomicType(), cellAtomicInner);

        // -------------------------------------------------------------------------------
        // Index 3
        // -------------------------------------------------------------------------------
        CellAtomicType cellAtomicInnerMapping = CellAtomicType.from(
                union(PredefinedType.MAPPING, PredefinedType.UNDEF),
                CellAtomicType.CellMutability.CELL_MUT_LIMITED
        );

        TypeAtom typeAtom3 = atomTable.get(cellAtomicInnerMapping);
        Assert.assertEquals(typeAtom3.index(), 3);
        Assert.assertEquals(typeAtom3.atomicType(), cellAtomicInnerMapping);

        // -------------------------------------------------------------------------------
        // Index 4
        // -------------------------------------------------------------------------------
        ListAtomicType listAtomicMapping = ListAtomicType.from(
                FixedLengthArray.empty(), PredefinedType.CELL_SEMTYPE_INNER_MAPPING
        );

        TypeAtom typeAtom4 = atomTable.get(listAtomicMapping);
        Assert.assertEquals(typeAtom4.index(), 4);
        Assert.assertEquals(typeAtom4.atomicType(), listAtomicMapping);

        // -------------------------------------------------------------------------------
        // Index 5
        // ------------------------------------------------------------------------------
        TypeAtom typeAtom5 = atomTable.get(PredefinedType.CELL_ATOMIC_INNER_MAPPING_RO);
        Assert.assertEquals(typeAtom5.index(), 5);
        Assert.assertEquals(typeAtom5.atomicType(), PredefinedType.CELL_ATOMIC_INNER_MAPPING_RO);

        // -------------------------------------------------------------------------------
        // Index 6
        // -------------------------------------------------------------------------------
        ListAtomicType listAtomicMappingRo = ListAtomicType.from(
                FixedLengthArray.empty(), PredefinedType.CELL_SEMTYPE_INNER_MAPPING_RO
        );

        TypeAtom typeAtom6 = atomTable.get(listAtomicMappingRo);
        Assert.assertEquals(typeAtom6.index(), 6);
        Assert.assertEquals(typeAtom6.atomicType(), listAtomicMappingRo);

        // -------------------------------------------------------------------------------
        // Index 7
        // -------------------------------------------------------------------------------
        CellAtomicType cellAtomicInnerRo = CellAtomicType.from(
                PredefinedType.INNER_READONLY, CellAtomicType.CellMutability.CELL_MUT_NONE
        );

        TypeAtom typeAtom7 = atomTable.get(cellAtomicInnerRo);
        Assert.assertEquals(typeAtom7.index(), 7);
        Assert.assertEquals(typeAtom7.atomicType(), cellAtomicInnerRo);
    }

    @Test
    public void testEnvInitRecAtoms() throws NoSuchFieldException, IllegalAccessException {
        Env env = new Env();

        Field recListAtomsField = Env.class.getDeclaredField("recListAtoms");
        recListAtomsField.setAccessible(true);
        List<ListAtomicType> recListAtoms = (List) recListAtomsField.get(env);
        Assert.assertEquals(recListAtoms.size(), 1);
        ListAtomicType listAtomicRo = ListAtomicType.from(
                FixedLengthArray.empty(), PredefinedType.CELL_SEMTYPE_INNER_RO
        );
        Assert.assertEquals(recListAtoms.get(0), listAtomicRo);

        Field recMappingAtomsField = Env.class.getDeclaredField("recMappingAtoms");
        recMappingAtomsField.setAccessible(true);
        List<MappingAtomicType> recMappingAtoms = (List) recMappingAtomsField.get(env);
        Assert.assertEquals(recMappingAtoms.size(), 1);
        Assert.assertEquals(recMappingAtoms.get(0), PredefinedType.MAPPING_ATOMIC_RO);

        Field recFunctionAtomsField = Env.class.getDeclaredField("recFunctionAtoms");
        recFunctionAtomsField.setAccessible(true);
        List<FunctionAtomicType> recFunctionAtoms = (List) recFunctionAtomsField.get(env);
        Assert.assertEquals(recFunctionAtoms.size(), 0);
    }
}
