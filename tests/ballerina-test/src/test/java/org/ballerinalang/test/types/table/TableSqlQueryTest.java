/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.table;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TableSqlQueryTest {

    private CompileResult result;
    private CompileResult resultHelper;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table-sql.bal");
        resultHelper = BCompileUtil.compile("test-src/types/table/table-test-helper.bal");
    }

    @Test(groups = "TableQueryTest", description = "Do a simple select all")
    public void testSimpleSelectAll() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testSimpleSelectAll", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple select with few fields from a table")
    public void testSimpleSelectFewFields() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testSimpleSelectFewFields", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple join with the select")
    public void testSimpleSelectWithJoin() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testSimpleSelectWithJoin", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple join with the select and where")
    public void testSelectWithJoinAndWhere() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testSelectWithJoinAndWhere", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple join with the select and where along with group by")
    public void testSelectWithJoinAndWhereWithGroupBy() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testSelectWithJoinAndWhereWithGroupBy", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test(dependsOnGroups = "TableQueryTest")
    public void testSessionCount() {
        BValue[] returns = BRunUtil.invoke(resultHelper, "getSessionCount");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple select all with limit")
    public void testSimpleSelectAllWithLimit() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testSimpleSelectAllWithLimit", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple join with the select and where along with group by " +
                                                   "with limit")
    public void testSelectWithJoinAndWhereWithGroupByWithLimit() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testSelectWithJoinAndWhereWithGroupByWithLimit", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }
}
