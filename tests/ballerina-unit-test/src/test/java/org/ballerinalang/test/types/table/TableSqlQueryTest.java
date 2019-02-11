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

/**
 * Testcase for table sql queries.
 */
public class TableSqlQueryTest {

    private CompileResult result;
    private CompileResult resultHelper;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table_sql.bal");
        resultHelper = BCompileUtil.compile("test-src/types/table/table_test_helper.bal");
    }

    @Test(groups = "TableQueryTest", description = "Do a simple select all")
    public void testSimpleSelectAll() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleSelectAll");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple select with few fields from a table")
    public void testSimpleSelectFewFields() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleSelectFewFields");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple join with the select")
    public void testSimpleSelectWithJoin() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleSelectWithJoin");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple join with the select and where")
    public void testSelectWithJoinAndWhere() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectWithJoinAndWhere");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple join with the select and where along with group by")
    public void testSelectWithJoinAndWhereWithGroupBy() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectWithJoinAndWhereWithGroupBy");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test(dependsOnGroups = "TableQueryTest", enabled = false)
    public void testSessionCount() {
        BValue[] returns = BRunUtil.invoke(resultHelper, "getSessionCount");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple select all with limit")
    public void testSimpleSelectAllWithLimit() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleSelectAllWithLimit");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple select within an if block")
    public void testSimpleSelectAllWithCondition() {
        BValue[] returns = BRunUtil.invoke(result, "testSimpleSelectAllWithCondition");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(groups = "TableQueryTest", description = "Do a simple join with the select and where along with group by " +
                                                   "with limit")
    public void testSelectWithJoinAndWhereWithGroupByWithLimit() {
        BValue[] returns = BRunUtil.invoke(result, "testSelectWithJoinAndWhereWithGroupByWithLimit");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(groups = "TableTest",
          description = "Verify string conversion of a table returned from ballerina sql query with no indices/primary "
                  + "keys")
    public void testTableToString() throws Exception {
        BValue[] returns = BRunUtil.invoke(result, "testTableToString");
        Assert.assertEquals(returns[0].stringValue(), "table<Person> {index: [], primaryKey: [], data: [{id:1, "
                + "age:25, salary:300.5, name:\"jane\", married:true}, {id:2, age:26, salary:400.5, name:\"kane\", "
                + "married:false}, {id:3, age:27, salary:500.5, name:\"jack\", married:true}, {id:4, age:28, "
                + "salary:600.5, name:\"alex\", married:false}]}");
    }
}
