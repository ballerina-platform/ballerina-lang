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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test join clause in query expression.
 *
 * @since 1.3.0
 */
public class JoinClauseTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/query/join-clause.bal");
    }

    @Test
    public void testSimpleJoinClause() {
        BValue[] values = BRunUtil.invoke(result, "testSimpleJoinClause");
        Assert.assertNotNull(values);

        Assert.assertEquals(values.length, 2, "Expected events are not received");

        BMap<String, BValue> deptPerson1 = (BMap<String, BValue>) values[0];
        BMap<String, BValue> deptPerson2 = (BMap<String, BValue>) values[1];

        Assert.assertEquals(deptPerson1.get("fname").stringValue(), "Alex");
        Assert.assertEquals(deptPerson1.get("lname").stringValue(), "George");
        Assert.assertEquals(deptPerson2.get("dept").stringValue(), "Eng");
    }


    @Test
    public void testJoinClauseWithStream() {
        BValue[] values = BRunUtil.invoke(result, "testJoinClauseWithStream", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }
}
