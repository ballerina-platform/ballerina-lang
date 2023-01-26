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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test record constraint table type.
 *
 * @since 1.3.0
 */
public class TableWithRecordKeySpecifierTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/record-type-table-key.bal");
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

    @Test(description = "Test Table with var type")
    public void testTableWithVarType() {
        BRunUtil.invoke(result, "runTableTestcasesWithVarType");
    }

    @Test(description = "Test table with default record fields as key fields",
            dataProvider = "tableWithDefaultableRecordFieldAsKeyFieldTestFunctions")
    public void testTableWithDefaultValueFieldAsKeyField(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider
    public  Object[] tableWithDefaultableRecordFieldAsKeyFieldTestFunctions() {
        return new String[] {
                "testDefaultValueFieldAsKeyField",
                "testDefaultValueFieldAsKeyField2",
                "testDefaultValueFieldAsKeyField3",
                "testDefaultValueFieldAsKeyField4",
                "testDefaultValueFieldAsKeyField5",
                "testDefaultValueFieldAsKeyField6"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

