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
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.MessageFormat;

/**
 * TestCases for Record Variable Definitions.
 *
 * @since 0.982.0
 */
public class RecordVariableReference {

    private CompileResult result, resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/varref/record-variable-reference.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/varref/record-variable-reference-negative.bal");
    }

    @Test(description = "Test simple record variable definition")
    public void testVariableAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testVariableAssignment");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "Peter");
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 12);
        Assert.assertEquals(returns[3].stringValue(), "Y");
    }

    @Test(description = "Test simple record variable definition")
    public void testRecVarRefInsideRecVarRefInsideRecVarRef() {
        BValue[] returns = BRunUtil.invoke(result, "testRecVarRefInsideRecVarRefInsideRecVarRef");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(((BInteger) ((BMap) returns[0]).get("mKey1")).intValue(), 1);
        Assert.assertEquals(((BInteger) ((BMap) returns[0]).get("mKey2")).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 12);
        Assert.assertEquals(returns[2].stringValue(), "SomeVar1");
        Assert.assertNull(returns[3]);
    }

    @Test(description = "Test simple record variable definition")
    public void testRestParam() {
        BValue[] returns = BRunUtil.invoke(result, "testRestParam");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) ((BMap) returns[0]).get("var3")).intValue(), 12);
        Assert.assertEquals(((BMap) returns[0]).get("var4").stringValue(), "text");
    }

    @Test(description = "Test simple record variable definition")
    public void testRecordTypeInRecordVarRef() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordTypeInRecordVarRef");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BInteger) ((BMap) returns[0]).get("mKey1")).intValue(), 1);
        Assert.assertEquals(((BInteger) ((BMap) returns[0]).get("mKey2")).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 12);
        Assert.assertEquals((((BMap) returns[2]).get("var1")).stringValue(), "SomeVar1");
        Assert.assertNull(((BMap) returns[2]).get("var2"));
    }

    @Test
    public void testNegativeRecordVariables() {
        Assert.assertEquals(resultNegative.getErrorCount(), 13);
        final String UNDEFINED_SYMBOL = "undefined symbol ";
        final String EXPECTING_CLOSED_RECORD = "can not match record variable reference, type {0} is not a closed record type";

        int i = -1;
        BAssertUtil.validateError(resultNegative, ++i, UNDEFINED_SYMBOL + "'format'", 45, 48);
        BAssertUtil.validateError(resultNegative, ++i, UNDEFINED_SYMBOL + "'theAge'", 45, 40);
        BAssertUtil.validateError(resultNegative, ++i, UNDEFINED_SYMBOL + "'married'", 45, 19);
        BAssertUtil.validateError(resultNegative, ++i, UNDEFINED_SYMBOL + "'fName'", 45, 12);
        BAssertUtil.validateError(resultNegative, ++i, UNDEFINED_SYMBOL + "'theMap'", 45, 66);
        BAssertUtil.validateError(resultNegative, ++i,
                MessageFormat.format(EXPECTING_CLOSED_RECORD, "'Age'"), 45, 35);
        BAssertUtil.validateError(resultNegative, ++i,
                MessageFormat.format(EXPECTING_CLOSED_RECORD, "'Age'"), 69, 35);
        BAssertUtil.validateError(resultNegative, ++i,
                MessageFormat.format(EXPECTING_CLOSED_RECORD, "'Person'"), 71, 5);
        BAssertUtil.validateError(resultNegative, ++i, "variable assignment is required", 92, 5);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'Bar', found 'string'", 93, 5);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'string', found 'Bar'", 93, 5);
        BAssertUtil.validateError(resultNegative, ++i, "incompatible types: expected 'record type', found 'int'", 94, 5);
        BAssertUtil.validateError(resultNegative, ++i, "invalid literal for type 'record variable reference'", 95, 5);
    }
}
