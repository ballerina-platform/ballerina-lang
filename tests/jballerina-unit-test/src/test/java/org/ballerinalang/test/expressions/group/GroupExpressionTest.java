/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.group;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for combinations of index/field access and invocations on group expressions.
 *
 * @since 1.1.0
 */
public class GroupExpressionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/group/group-expr.bal");
    }

    @Test(dataProvider = "GroupExpressionAccessFunctions")
    public void testGroupExpressionAccessFunctions(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "GroupExpressionAccessFunctions")
    public Object[][] fieldAndOptionalFieldAccessFunctions() {
        return new Object[][]{
                {"testGroupedMapRef"},
                {"testGroupedArrayRef"},
                {"testGroupedFieldVariableRef"},
                {"testGroupedQuotedStringRef"},
                {"testGroupedInvocationRef"},
                {"testGroupedTypeDescRef"},
                {"testGroupedBuiltInInvocationRef"},
                {"testGroupedTypedescLibInvocation"},
                {"testNestedGroupedInvocationRef"},
                {"testGroupedLangLibInvocationRef"}
        };
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
