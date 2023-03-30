/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.ballerinalang.test.query;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This contains methods to test simple query action statements.
 *
 * @since 1.2.0
 */
public class QueryActionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/query-action.bal");
    }

    @Test(description = "Test simple query action statement")
    public void testSimpleQueryAction() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleQueryAction");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person3.get(StringUtils.fromString("lastName")).toString(), "David");
    }

    @Test(description = "Test simple query action - record variable definition")
    public void testSimpleQueryActionWithRecordVariable() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleQueryActionWithRecordVariable");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person3.get(StringUtils.fromString("lastName")).toString(), "David");
    }

    @Test(description = "Test simple query action - record variable definition")
    public void testSimpleSelectQueryWithRecordVariableV2() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithRecordVariableV2");
        Assert.assertNotNull(returnValues);

        BMap<String, Object> person1 = (BMap<String, Object>) returnValues.get(0);
        BMap<String, Object> person2 = (BMap<String, Object>) returnValues.get(1);
        BMap<String, Object> person3 = (BMap<String, Object>) returnValues.get(2);

        Assert.assertEquals(person1.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(person2.get(StringUtils.fromString("lastName")).toString(), "Fonseka");
        Assert.assertEquals(person3.get(StringUtils.fromString("lastName")).toString(), "David");
    }

    @Test(description = "Test simple query action statement v2")
    public void testSimpleQueryAction2() {
        Object returnValues = BRunUtil.invoke(result, "testSimpleQueryAction2");
        Assert.assertNotNull(returnValues);

        long countValue = (long) returnValues;
        Assert.assertEquals(countValue, 6);
    }

    @Test
    public void testSimpleQueryAction3() {
        BRunUtil.invoke(result, "testSimpleQueryAction3");
    }

    @Test(description = "Test simple query action with let clause")
    public void testSimpleSelectQueryWithLetClause() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithLetClause");
        Assert.assertNotNull(returnValues);
        BMap<String, Object> person = (BMap<String, Object>) returnValues.get(0);

        Assert.assertEquals(person.get(StringUtils.fromString("firstName")).toString(), "Alex");
    }

    @Test(description = "Test simple query action with where clause")
    public void testSimpleSelectQueryWithWhereClause() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithWhereClause");
        Assert.assertNotNull(returnValues);
        BMap<String, Object> person = (BMap<String, Object>) returnValues.get(0);

        Assert.assertEquals(person.get(StringUtils.fromString("firstName")).toString(), "Alex");
    }

    @Test(description = "Test simple query action with multiple from clauses")
    public void testSimpleSelectQueryWithMultipleFromClauses() {
        BArray returnValues = (BArray) BRunUtil.invoke(result, "testSimpleSelectQueryWithMultipleFromClauses");
        Assert.assertNotNull(returnValues);
        BMap<String, Object> employee = (BMap<String, Object>) returnValues.get(0);

        Assert.assertEquals(employee.get(StringUtils.fromString("firstName")).toString(), "Alex");
        Assert.assertEquals(employee.get(StringUtils.fromString("deptAccess")).toString(), "Human Resource");
    }

    @Test(description = "Test query expression iterating over xml<xml:Element> in from clause in query action")
    public void testQueryExpressionIteratingOverXMLInFromInQueryAction() {
        Object returnValues = BRunUtil.invoke(result, "testQueryExpressionIteratingOverXMLInFromInQueryAction");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals((returnValues), 149.93);
    }

    @Test(description = "Test type test in where clause")
    public void testTypeTestInWhereClause() {
        BRunUtil.invoke(result, "testTypeTestInWhereClause");
    }

    @Test(description = "Test type test in variable defined in let clause")
    public void testTypeNarrowingVarDefinedWithLet() {
        BRunUtil.invoke(result, "testTypeNarrowingVarDefinedWithLet");
    }

    @Test
    public void testWildcardBindingPatternInQueryAction1() {
        BRunUtil.invoke(result, "testWildcardBindingPatternInQueryAction1");
    }

    @Test
    public void testWildcardBindingPatternInQueryAction2() {
        BRunUtil.invoke(result, "testWildcardBindingPatternInQueryAction2");
    }

    @Test
    public void testQueryActionWithAsyncCalls() {
        BRunUtil.invoke(result, "testQueryActionWithAsyncCalls");
    }

    @Test
    public void testQueryExpWithinQueryAction() {
        BRunUtil.invoke(result, "testQueryExpWithinQueryAction");
    }

    @Test
    public void testErrorHandlingWithinQueryAction() {
        BRunUtil.invoke(result, "testErrorHandlingWithinQueryAction");
    }

    @Test
    public void testReturnStmtWithinQueryAction() {
        BRunUtil.invoke(result, "testReturnStmtWithinQueryAction");
    }

    @Test
    public void testQueryActionWithDoClauseContainsCheck() {
        BRunUtil.invoke(result, "testQueryActionWithDoClauseContainsCheck");
    }

    @Test
    public void testForeachStmtInsideDoClause() {
        BRunUtil.invoke(result, "testForeachStmtInsideDoClause");
    }

    @Test
    public void testIfStmtInsideDoClause() {
        BRunUtil.invoke(result, "testIfStmtInsideDoClause");
    }

    @Test(dataProvider = "dataToTestQueryActionWithVar")
    public void testQueryActionWithVar(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestQueryActionWithVar() {
        return new Object[]{
                "testUsingDestructuringRecordingBindingPatternWithAnIntersectionTypeInQueryAction",
                "testUsingDestructuringRecordingBindingPatternWithAnIntersectionTypeInQueryAction2"
        };
    }

    @Test
    public void testQueryWithOptionalFieldRecord() {
        BRunUtil.invoke(result, "testQueryWithOptionalFieldRecord");
    }

    @Test
    public void testQueryWithStream() {
        BRunUtil.invoke(result, "testQueryStreamWithDiffTargetTypes");
    }

    @Test(dataProvider = "dataToTestQueryAction")
    public void testQueryAction(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestQueryAction() {
        return new Object[]{
                "testQueryActionWithRegExp",
                "testQueryActionWithRegExpWithInterpolations",
                "testNestedQueryActionWithRegExp",
                "testJoinedQueryActionWithRegExp"
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
