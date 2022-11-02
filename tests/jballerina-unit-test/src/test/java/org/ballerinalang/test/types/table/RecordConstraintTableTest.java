/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.table;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test record constraint table type.
 *
 * @since 1.3.0
 */
public class RecordConstraintTableTest {

    private CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/record-constraint-table-value.bal");
        negativeResult = BCompileUtil.compile("test-src/types/table/table-value-negative.bal");
    }

    @Test
    public void testDuplicateKeysInTableConstructorExpr() {
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "duplicate key found " +
                "in table row key('name') : 'AAA'", 18, 5);
        BAssertUtil.validateError(negativeResult, index++, "duplicate key found " +
                "in table row key('id, name') : '13, Foo'", 23, 5);
        BAssertUtil.validateError(negativeResult, index++, "duplicate key found in table" +
                " row key('m') : ' {AAA: DDDD}'", 36, 7);
        BAssertUtil.validateError(negativeResult, index++, "duplicate key found in table row key('idNum') : 'idNum'",
                46, 9);
        BAssertUtil.validateError(negativeResult, index++, "duplicate key found in table row key('idNum') : 'idNum'",
                54, 9);
        BAssertUtil.validateError(negativeResult, index++, "duplicate key found in table" +
                " row key('idNum, name') : 'idNum, Jo'", 62, 9);
        BAssertUtil.validateError(negativeResult, index++, "duplicate key found in table" +
                " row key('idNum, name') : 'idNum, Jo'", 70, 9);
        BAssertUtil.validateError(negativeResult, index++, "duplicate key found in table row key('m')" +
                " : ' {AAA: DDDD}'", 85, 7);
        BAssertUtil.validateError(negativeResult, index++, "duplicate key found in table" +
                " row key('m, age') : ' {AAA: DDDD}, 11'", 101, 7);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }

    @Test(description = "Test global table constructor expr")
    public void testGlobalTableConstructExpr() {
        Object values = BRunUtil.invoke(result, "testGlobalTableConstructExpr", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test key specifier and key type constraint options")
    public void testKeySpecifierAndTypeConstraintOptions() {
        BRunUtil.invoke(result, "runKeySpecifierTestCases");
    }

    @Test(description = "Test member access")
    public void testMemberAccessExpr() {
        BRunUtil.invoke(result, "runMemberAccessTestCases");
    }

    @Test(description = "Test keyless table")
    public void testKeylessTable() {
        BRunUtil.invoke(result, "testKeylessTable");
    }

    @Test(description = "Test invalid member access in table with a single key field")
    public void testMemberAccessWithInvalidSingleKey() {
        BRunUtil.invoke(result, "testMemberAccessWithInvalidSingleKey");
    }

    @Test(description = "Test invalid member access in table with multiple key fields")
    public void testMemberAccessWithInvalidMultiKey() {
        BRunUtil.invoke(result, "testMemberAccessWithInvalidMultiKey");
    }

    @Test(description = "Test table with var type")
    public void testTableWithVarType() {
        BRunUtil.invoke(result, "runTableTestcasesWithVarType");
        BRunUtil.invoke(result, "testTableTypeInferenceWithVarType");
    }

    @Test(description = "Test invalid member access in table")
    public void testVarTypeTableInvalidMemberAccess() {
        BRunUtil.invoke(result, "testVarTypeTableInvalidMemberAccess");
    }

    @Test(description = "Test member access in table in store operation")
    public void testTableMemberAccessStore() {
        Object values = BRunUtil.invoke(result, "testTableMemberAccessStore", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test member access in table in load operation")
    public void testTableMemberAccessLoad() {
        Object values = BRunUtil.invoke(result, "testTableMemberAccessLoad", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test(description = "Test table as record field")
    public void testTableAsRecordField() {
        BRunUtil.invoke(result, "testTableAsRecordField", new Object[]{});
    }

    @Test(description = "Test table equality")
    public void testTableEquality() {
        BRunUtil.invoke(result, "testTableEquality", new Object[]{});
    }

    @Test(description = "Test member access in table having members with nilable/optional fields")
    public void testMemberAccessHavingNilableFields() {
        BRunUtil.invoke(result, "testMemberAccessHavingNilableFields");
    }

    @Test(description = "Test table iteration with a union constrained table")
    public void testUnionConstrainedTableIteration() {
        BRunUtil.invoke(result, "testUnionConstrainedTableIteration");
    }

    @Test(description = "Test using spread field in table constructor")
    public void testSpreadFieldInConstructor() {
        BRunUtil.invoke(result, "testSpreadFieldInConstructor");
    }

    @Test(dataProvider = "functionsToTestEmptyKeyedKeylessTbl")
    public void testEmptyKeyedKeylessTbl(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider
    public  Object[] functionsToTestEmptyKeyedKeylessTbl() {
        return new String[] {
                "testAssignabilityWithEmptyKeyedKeylessTbl",
                "testEqualityWithEmptyKeyedKeylessTbl",
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
