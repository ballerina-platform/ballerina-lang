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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.ballerina.types.Core.union;

/**
 * Test class for {@link Env} initialization.
 *
 * @since 2201.10.0
 */
public class EnvInitTest {

    @BeforeClass
    public void setup() {
        // All the types in PredefinedTypeEnv are populated by the loading of PredefinedType class.
        try {
            Class.forName("io.ballerina.types.PredefinedType");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEnvInitAtomTable() throws NoSuchFieldException, IllegalAccessException {
        final Env env = new Env();

        // Access the private field atomTable using reflection
        Field atomTableField = Env.class.getDeclaredField("atomTable");
        atomTableField.setAccessible(true);
        Map<AtomicType, TypeAtom> atomTable = (Map) atomTableField.get(env);

        // Check that the atomTable contains the expected entries
        Assert.assertEquals(atomTable.size(), 10);

        CellAtomicType cellAtomicVal = CellAtomicType.from(
                PredefinedType.VAL, CellAtomicType.CellMutability.CELL_MUT_LIMITED
        );

        TypeAtom typeAtom0 = atomTable.get(cellAtomicVal);
        Assert.assertNotNull(typeAtom0);
        Assert.assertEquals(typeAtom0.atomicType(), cellAtomicVal);

        CellAtomicType cellAtomicNever = CellAtomicType.from(
                PredefinedType.NEVER, CellAtomicType.CellMutability.CELL_MUT_LIMITED
        );

        TypeAtom typeAtom1 = atomTable.get(cellAtomicNever);
        Assert.assertNotNull(typeAtom1);
        Assert.assertEquals(typeAtom1.atomicType(), cellAtomicNever);

        CellAtomicType cellAtomicInner = CellAtomicType.from(
                PredefinedType.INNER, CellAtomicType.CellMutability.CELL_MUT_LIMITED
        );

        TypeAtom typeAtom2 = atomTable.get(cellAtomicInner);
        Assert.assertNotNull(typeAtom2);
        Assert.assertEquals(typeAtom2.atomicType(), cellAtomicInner);

        CellAtomicType cellAtomicInnerMapping = CellAtomicType.from(
                union(PredefinedType.MAPPING, PredefinedType.UNDEF),
                CellAtomicType.CellMutability.CELL_MUT_LIMITED
        );

        TypeAtom typeAtom3 = atomTable.get(cellAtomicInnerMapping);
        Assert.assertNotNull(typeAtom3);
        Assert.assertEquals(typeAtom3.atomicType(), cellAtomicInnerMapping);

        ListAtomicType listAtomicMapping = ListAtomicType.from(
                FixedLengthArray.empty(), PredefinedType.CELL_SEMTYPE_INNER_MAPPING
        );

        TypeAtom typeAtom4 = atomTable.get(listAtomicMapping);
        Assert.assertNotNull(typeAtom4);
        Assert.assertEquals(typeAtom4.atomicType(), listAtomicMapping);

        TypeAtom typeAtom5 = atomTable.get(PredefinedType.CELL_ATOMIC_INNER_MAPPING_RO);
        Assert.assertNotNull(typeAtom5);
        Assert.assertEquals(typeAtom5.atomicType(), PredefinedType.CELL_ATOMIC_INNER_MAPPING_RO);

        ListAtomicType listAtomicMappingRo = ListAtomicType.from(
                FixedLengthArray.empty(), PredefinedType.CELL_SEMTYPE_INNER_MAPPING_RO
        );

        TypeAtom typeAtom6 = atomTable.get(listAtomicMappingRo);
        Assert.assertNotNull(typeAtom6);
        Assert.assertEquals(typeAtom6.atomicType(), listAtomicMappingRo);

        CellAtomicType cellAtomicInnerRo = CellAtomicType.from(
                PredefinedType.INNER_READONLY, CellAtomicType.CellMutability.CELL_MUT_NONE
        );

        TypeAtom typeAtom7 = atomTable.get(cellAtomicInnerRo);
        Assert.assertNotNull(typeAtom7);
        Assert.assertEquals(typeAtom7.atomicType(), cellAtomicInnerRo);

        CellAtomicType cellAtomicUndef = CellAtomicType.from(
                PredefinedType.UNDEF, CellAtomicType.CellMutability.CELL_MUT_NONE
        );

        TypeAtom typeAtom8 = atomTable.get(cellAtomicUndef);
        Assert.assertNotNull(typeAtom8);
        Assert.assertEquals(typeAtom8.atomicType(), cellAtomicUndef);

        ListAtomicType listAtomicTwoElement = ListAtomicType.from(
                FixedLengthArray.from(List.of(PredefinedType.CELL_SEMTYPE_VAL), 2),
                PredefinedType.CELL_SEMTYPE_UNDEF
        );

        TypeAtom typeAtom9 = atomTable.get(listAtomicTwoElement);
        Assert.assertNotNull(typeAtom8);
        Assert.assertEquals(typeAtom9.atomicType(), listAtomicTwoElement);
    }

    @Test
    public void testTypeAtomIndices() throws NoSuchFieldException, IllegalAccessException {
        Env env = new Env();
        Field atomTableField = Env.class.getDeclaredField("atomTable");
        atomTableField.setAccessible(true);
        Map<AtomicType, TypeAtom> recListAtoms = (Map<AtomicType, TypeAtom>) atomTableField.get(env);
        Collection<Integer> indices = new HashSet<>();
        for (var each : recListAtoms.values()) {
            Assert.assertTrue(indices.add(each.index()), "Duplicate index found: " + each.index());
        }
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
