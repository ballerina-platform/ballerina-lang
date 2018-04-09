/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test table literal syntax.
 */
public class TableLiteralSyntaxTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table-literal-syntax.bal");
    }

    @Test
    public void testTableAddOnUnconstrainedTable() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddOnUnconstrainedTable");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test
    public void testTableAddOnConstrainedTable() {
        BValue[] returns = BRunUtil.invoke(result, "testTableAddOnConstrainedTable");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test
    public void testValidTableVariable() {
        BValue[] returns = BRunUtil.invoke(result, "testValidTableVariable");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test
    public void testTableLiteralData() {
        BValue[] returns = BRunUtil.invoke(result, "testTableLiteralData");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test
    public void testTableLiteralDataAndAdd() {
        BValue[] returns = BRunUtil.invoke(result, "testTableLiteralDataAndAdd");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 5);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*initial data should be in struct type.*")
    public void testTableLiteralDataWithInit() {
        BRunUtil.invoke(result, "testTableLiteralDataWithInit");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*error:Unique index or primary key violation:.*")
    public void testTableAddOnConstrainedTableWithViolation() {
        BRunUtil.invoke(result, "testTableAddOnConstrainedTableWithViolation");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*error:Unique index or primary key violation:.*")
    public void testTableAddOnConstrainedTableWithViolation2() {
        BRunUtil.invoke(result, "testTableAddOnConstrainedTableWithViolation2");
    }
}
