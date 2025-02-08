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

import io.ballerina.types.subtypedata.CellSubtype;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_LIMITED;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_NONE;
import static io.ballerina.types.CellAtomicType.CellMutability.CELL_MUT_UNLIMITED;

/**
 * Tests subtyping rules of cell basic type.
 *
 * @since 2201.10.0
 */
public class CellTypeTest {

    Context ctx;

    public enum Relation {
        EQUAL("="),
        SUBTYPE("<"),
        NO_RELATION("<>");

        final String value;

        Relation(String value) {
            this.value = value;
        }
    }

    @BeforeClass
    public void beforeClass() {
        ctx = Context.from(new Env());
    }

    private CellSemType cell(SemType ty, CellAtomicType.CellMutability mut) {
        return CellSubtype.cellContaining(ctx.env, ty, mut);
    }

    private void assertSemTypeRelation(SemType t1, SemType t2, Relation relation) {
        Relation actual = getSemTypeRelation(t1, t2);
        Assert.assertEquals(actual, relation);
    }

    private Relation getSemTypeRelation(SemType t1, SemType t2) {
        boolean s1 = Core.isSubtype(ctx, t1, t2);
        boolean s2 = Core.isSubtype(ctx, t2, t1);
        if (s1 && s2) {
            return Relation.EQUAL;
        } else if (s1) {
            return Relation.SUBTYPE;
        } else if (s2) {
            throw new IllegalStateException("'>' relation found which can be converted to a '<' relation");
        } else {
            return Relation.NO_RELATION;
        }
    }

    @Test(description = "Test T and cell(T) having no relation", dataProvider = "typeCellDisparityDataProvider")
    public void testTypeCellDisparity(SemType t1, SemType t2, Relation relation) {
        assertSemTypeRelation(t1, t2, relation);
    }

    @DataProvider(name = "typeCellDisparityDataProvider")
    public Object[][] createTypeCellDisparityTestData() {
        return new Object[][]{
                {PredefinedType.INT, cell(PredefinedType.INT, CELL_MUT_NONE), Relation.NO_RELATION},
                {PredefinedType.INT, cell(PredefinedType.INT, CELL_MUT_LIMITED), Relation.NO_RELATION},
                {PredefinedType.INT, cell(PredefinedType.INT, CELL_MUT_UNLIMITED), Relation.NO_RELATION},
        };
    }

    @Test(description = "Test basic cell subtyping", dataProvider = "basicCellSubtypingDataProvider")
    public void testBasicCellSubtyping(SemType t1, SemType t2, Relation[] relations) {
        assert relations.length == 3;
        Relation[] actual = new Relation[3];

        CellAtomicType.CellMutability[] values = CellAtomicType.CellMutability.values();
        // Obtaining relation for each mutability kind
        for (int i = 0; i < values.length; i++) {
            CellAtomicType.CellMutability mut = values[i];
            CellSemType c1 = cell(t1, mut);
            CellSemType c2 = cell(t2, mut);
            actual[i] = getSemTypeRelation(c1, c2);
        }

        Assert.assertEquals(actual, relations);
    }

    @DataProvider(name = "basicCellSubtypingDataProvider")
    public Object[][] createBasicCellSubtypingTestData() {
        // This contains some of nBallerina 'cell-1.typetest' test data
        return new Object[][]{
                {
                        PredefinedType.INT, PredefinedType.INT,
                        new Relation[]{
                                Relation.EQUAL, Relation.EQUAL, Relation.EQUAL
                        }
                },
                {
                        PredefinedType.BOOLEAN, PredefinedType.BOOLEAN,
                        new Relation[]{
                                Relation.EQUAL, Relation.EQUAL, Relation.EQUAL
                        }
                },
                {
                        PredefinedType.BYTE, PredefinedType.INT,
                        new Relation[]{
                                Relation.SUBTYPE, Relation.SUBTYPE, Relation.SUBTYPE
                        }
                },
                {
                        PredefinedType.BOOLEAN, PredefinedType.INT,
                        new Relation[]{
                                Relation.NO_RELATION, Relation.NO_RELATION, Relation.NO_RELATION
                        }
                },
                {
                        PredefinedType.BOOLEAN, Core.union(PredefinedType.INT, PredefinedType.BOOLEAN),
                        new Relation[]{
                                Relation.SUBTYPE, Relation.SUBTYPE, Relation.SUBTYPE
                        }
                }
        };
    }

    @Test(dataProvider = "cellSubtypeDataProvider1")
    public void testCellSubtyping1(SemType t1, SemType t2, Relation relation) {
        assertSemTypeRelation(t1, t2, relation);
    }

    @DataProvider(name = "cellSubtypeDataProvider1")
    public Object[][] createCellSubtypeData1() {
        // This contains some of nBallerina 'cell-1.typetest' test data
        return new Object[][]{
                // Set 1
                {
                        SemTypes.union(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                cell(PredefinedType.BOOLEAN, CELL_MUT_NONE)
                        ),
                        cell(SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN), CELL_MUT_NONE),
                        Relation.EQUAL
                },
                {
                        SemTypes.union(
                                cell(PredefinedType.INT, CELL_MUT_LIMITED),
                                cell(PredefinedType.BOOLEAN, CELL_MUT_LIMITED)
                        ),
                        cell(SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN), CELL_MUT_LIMITED),
                        Relation.SUBTYPE
                },
                {
                        SemTypes.union(
                                cell(PredefinedType.INT, CELL_MUT_UNLIMITED),
                                cell(PredefinedType.BOOLEAN, CELL_MUT_UNLIMITED)
                        ),
                        cell(SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN), CELL_MUT_UNLIMITED),
                        Relation.EQUAL
                },
                // Set 2
                {
                        SemTypes.union(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                cell(PredefinedType.BOOLEAN, CELL_MUT_NONE),
                                cell(PredefinedType.STRING, CELL_MUT_NONE)
                        ),
                        cell(SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN, PredefinedType.STRING),
                                CELL_MUT_NONE),
                        Relation.EQUAL
                },
                {
                        SemTypes.union(
                                cell(PredefinedType.INT, CELL_MUT_LIMITED),
                                cell(PredefinedType.BOOLEAN, CELL_MUT_LIMITED),
                                cell(PredefinedType.STRING, CELL_MUT_LIMITED)
                        ),
                        cell(SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN, PredefinedType.STRING),
                                CELL_MUT_LIMITED),
                        Relation.SUBTYPE
                },
                {
                        SemTypes.union(
                                cell(PredefinedType.INT, CELL_MUT_UNLIMITED),
                                cell(PredefinedType.BOOLEAN, CELL_MUT_UNLIMITED),
                                cell(PredefinedType.STRING, CELL_MUT_UNLIMITED)
                        ),
                        cell(SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN, PredefinedType.STRING),
                                CELL_MUT_UNLIMITED),
                        Relation.EQUAL
                },
                // Set 3
                {
                        SemTypes.union(
                                cell(TypeTestUtils.roTuple(ctx.env, PredefinedType.INT), CELL_MUT_NONE),
                                cell(TypeTestUtils.roTuple(ctx.env, PredefinedType.BOOLEAN), CELL_MUT_NONE)
                        ),
                        cell(TypeTestUtils.roTuple(ctx.env, SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN)),
                                CELL_MUT_NONE),
                        Relation.EQUAL
                },
                {
                        SemTypes.union(
                                cell(TypeTestUtils.tuple(ctx.env, PredefinedType.INT), CELL_MUT_LIMITED),
                                cell(TypeTestUtils.tuple(ctx.env, PredefinedType.BOOLEAN), CELL_MUT_LIMITED)
                        ),
                        cell(TypeTestUtils.tuple(ctx.env, SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN)),
                                CELL_MUT_LIMITED),
                        Relation.SUBTYPE
                },
                {
                        SemTypes.union(
                                cell(TypeTestUtils.tuple(ctx.env, PredefinedType.INT), CELL_MUT_UNLIMITED),
                                cell(TypeTestUtils.tuple(ctx.env, PredefinedType.BOOLEAN), CELL_MUT_UNLIMITED)
                        ),
                        cell(TypeTestUtils.tuple(ctx.env, SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN)),
                                CELL_MUT_UNLIMITED),
                        Relation.SUBTYPE
                },
        };
    }

    @Test(dataProvider = "cellSubtypeDataProvider2")
    public void testCellSubtyping2(SemType t1, SemType t2, Relation relation) {
        assertSemTypeRelation(t1, t2, relation);
    }

    @DataProvider(name = "cellSubtypeDataProvider2")
    public Object[][] createCellSubtypeData2() {
        // This contains nBallerina 'cell-2.typetest' test data
        return new Object[][]{
                // test 1
                {
                        SemTypes.union(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                cell(PredefinedType.BOOLEAN, CELL_MUT_UNLIMITED),
                                cell(PredefinedType.STRING, CELL_MUT_LIMITED)
                        ),
                        cell(
                                SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN, PredefinedType.STRING),
                                CELL_MUT_UNLIMITED
                        ),
                        Relation.SUBTYPE
                },
                // test 2
                {
                        cell(
                                SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN, PredefinedType.STRING),
                                CELL_MUT_NONE
                        ),
                        SemTypes.union(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                cell(PredefinedType.BOOLEAN, CELL_MUT_UNLIMITED),
                                cell(PredefinedType.STRING, CELL_MUT_LIMITED)
                        ),
                        Relation.SUBTYPE
                },
                // test 3
                {
                        SemTypes.union(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                cell(PredefinedType.BOOLEAN, CELL_MUT_UNLIMITED),
                                cell(PredefinedType.STRING, CELL_MUT_LIMITED)
                        ),
                        cell(
                                SemTypes.union(PredefinedType.INT, PredefinedType.BOOLEAN, PredefinedType.STRING),
                                CELL_MUT_LIMITED
                        ),
                        Relation.NO_RELATION
                },
                // test 4
                {
                        SemTypes.union(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                cell(PredefinedType.INT, CELL_MUT_LIMITED),
                                cell(PredefinedType.INT, CELL_MUT_UNLIMITED)
                        ),
                        cell(PredefinedType.INT, CELL_MUT_UNLIMITED),
                        Relation.EQUAL
                },
                // test 5
                {
                        SemTypes.intersect(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                cell(PredefinedType.INT, CELL_MUT_LIMITED),
                                cell(PredefinedType.INT, CELL_MUT_UNLIMITED)
                        ),
                        cell(PredefinedType.INT, CELL_MUT_UNLIMITED),
                        Relation.SUBTYPE
                },
                // test 6
                {
                        SemTypes.intersect(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                cell(PredefinedType.INT, CELL_MUT_LIMITED),
                                cell(PredefinedType.INT, CELL_MUT_UNLIMITED)
                        ),
                        cell(PredefinedType.INT, CELL_MUT_LIMITED),
                        Relation.SUBTYPE
                },
                // test 7
                {
                        SemTypes.intersect(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                cell(PredefinedType.INT, CELL_MUT_LIMITED),
                                cell(PredefinedType.INT, CELL_MUT_UNLIMITED)
                        ),
                        cell(PredefinedType.INT, CELL_MUT_NONE),
                        Relation.EQUAL
                },
                // test 8
                {
                        SemTypes.intersect(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                cell(PredefinedType.INT, CELL_MUT_LIMITED),
                                cell(PredefinedType.BYTE, CELL_MUT_LIMITED)
                        ),
                        cell(PredefinedType.BYTE, CELL_MUT_LIMITED),
                        Relation.SUBTYPE
                },
                // test 9
                {
                        SemTypes.intersect(
                                cell(PredefinedType.INT, CELL_MUT_NONE),
                                SemTypes.union(
                                        cell(PredefinedType.BYTE, CELL_MUT_LIMITED),
                                        cell(PredefinedType.BOOLEAN, CELL_MUT_LIMITED)
                                )
                        ),
                        cell(PredefinedType.BYTE, CELL_MUT_NONE),
                        Relation.EQUAL
                },
        };
    }

    @AfterClass
    public void afterClass() {
        ctx = null;
    }
}
