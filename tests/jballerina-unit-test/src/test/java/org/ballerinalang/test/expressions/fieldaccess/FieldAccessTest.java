/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.fieldaccess;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test cases for field access.
 *
 * @since 1.0
 */
public class FieldAccessTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/fieldaccess/field_access.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/fieldaccess/field_access_negative.bal");
    }

    @Test
    public void testNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 5);
        int i = 0;
        validateError(negativeResult, i++, "invalid operation: type 'Employee' does not support field access " +
                              "for non-required field 'id'", 31, 9);
        validateError(negativeResult, i++, "invalid operation: type 'Employee' does not support field access " +
                              "for non-required field 'salary'", 32, 9);
        validateError(negativeResult, i++, "invalid operation: type 'Employee|Person' does not support field access " +
                "for non-required field 'salary'", 38, 9);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'int|string'", 55, 17);
        validateError(negativeResult, i, "incompatible types: expected 'int', found 'int|string'", 56, 15);
    }

    @Test(dataProvider = "fieldAccessFunctions")
    public void testFieldAccessPositive(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }


    @DataProvider(name = "fieldAccessFunctions")
    public Object[][] fieldAccessFunctions() {
        return new Object[][] {
                { "testFieldAccess1" },
                { "testFieldAccess2" },
                { "testFieldAccess3" }
        };
    }
}
