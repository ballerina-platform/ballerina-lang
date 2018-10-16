/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.expressions.varref;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for Record Variable Definitions.
 *
 * @since 0.982.0
 */
public class RecordVariableReference {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
//        result = BCompileUtil.compile("test-src/expressions/varref/record-variable-reference.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/varref/record-variable-reference-negative.bal");
    }

//    @Test(description = "Test simple record variable definition")
//    public void testNegative() {
//        BValue[] returns = BRunUtil.invoke(result, "testVariableAssignment");
//        Assert.assertEquals(returns.length, 2);
//        Assert.assertEquals(returns[0].stringValue(), "Peter");
//        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
//    }

    @Test
    public void testNegativeRecordVariables() {
        System.out.println(resultNegative);
        Assert.assertEquals(resultNegative.getErrorCount(), 8);
        final String undefinedSymbol = "undefined symbol ";
        final String EXPECTING_CLOSED_RECORD = "expecting a closed record pattern to match record variable reference";

        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, undefinedSymbol + "'format'", 39, 48);
        BAssertUtil.validateError(resultNegative, ++i, undefinedSymbol + "'theAge'", 39, 40);
        BAssertUtil.validateError(resultNegative, ++i, undefinedSymbol + "'married'", 39, 19);
        BAssertUtil.validateError(resultNegative, ++i, undefinedSymbol + "'fName'", 39, 12);
        BAssertUtil.validateError(resultNegative, ++i, undefinedSymbol + "'theMap'", 39, 66);
        BAssertUtil.validateError(resultNegative, ++i, EXPECTING_CLOSED_RECORD, 53, 112);
        BAssertUtil.validateError(resultNegative, ++i, EXPECTING_CLOSED_RECORD, 55, 112);
        BAssertUtil.validateError(resultNegative, ++i, EXPECTING_CLOSED_RECORD, 57, 5);
    }
}
