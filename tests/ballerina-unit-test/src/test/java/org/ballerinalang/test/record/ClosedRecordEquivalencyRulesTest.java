/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.record;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.launcher.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for verifying the equivalency/assignability rules for closed records.
 *
 * @since 0.985.0
 */
public class ClosedRecordEquivalencyRulesTest {

    private CompileResult closedRecToClosedRec;

    @BeforeClass
    public void setup() {
        closedRecToClosedRec = BCompileUtil.compile("test-src/record/equivalency_rules_cr_to_cr.bal");
    }

    @Test(description = "Negative tests for when both LHS and RHS are closed")
    public void testClosedRecordNegatives() {
        CompileResult negative = BCompileUtil.compile("test-src/record/equiv_rules_neg_cr_to_cr.bal");
        assertEquals(negative.getErrorCount(), 2);
        validateError(negative, 0, "incompatible types: expected 'AnotherPerson', found 'Person1'", 30, 24);
        validateError(negative, 1, "incompatible types: expected 'AnotherPerson', found 'Person2'", 41, 24);
    }

    @Test(description = "Test assigning a closed record to a cloesd record type variable")
    public void testCRToCRClosedToClosedAssignment1() {
        BValue[] returns = BRunUtil.invoke(closedRecToClosedRec, "testClosedToClosedAssignment1");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS and LHS closed with RHS field types which are assignable to LHS field types")
    public void testCRToCRClosedToClosedAssignment2() {
        BValue[] returns = BRunUtil.invoke(closedRecToClosedRec, "testClosedToClosedAssignment2");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS and LHS closed with additional fields (optional) than RHS")
    public void testCRToCRClosedToClosedAssignment3() {
        BValue[] returns = BRunUtil.invoke(closedRecToClosedRec, "testClosedToClosedAssignment3");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS and LHS closed with RHS required fields corresponding to LHS optional fields")
    public void testCRToCRReqFieldToOptField() {
        BValue[] returns = BRunUtil.invoke(closedRecToClosedRec, "testReqFieldToOptField");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS and LHS closed with RHS optional fields corresponding to LHS optional fields")
    public void testCRToCROptFieldToOptField1() {
        BValue[] returns = BRunUtil.invoke(closedRecToClosedRec, "testOptFieldToOptField1");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS and LHS closed with RHS optional fields corresponding to LHS optional fields",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*cannot find key 'age'.*")
    public void testCRToCROptFieldToOptField2() {
        BRunUtil.invoke(closedRecToClosedRec, "testOptFieldToOptField2");
    }

    @Test(description = "RHS open and LHS closed is disallowed")
    public void testORToCR() {
        CompileResult openRecToClosedRec = BCompileUtil.compile("test-src/record/equivalency_rules_or_to_cr.bal");
        int index = 0;
        assertEquals(openRecToClosedRec.getErrorCount(), 5);
        validateError(openRecToClosedRec, index++, "incompatible types: expected 'AnotherPerson1', found 'Person1'", 30,
                      25);
        validateError(openRecToClosedRec, index++, "incompatible types: expected 'AnotherPerson2', found 'Person1'", 42,
                      25);
        validateError(openRecToClosedRec, index++, "incompatible types: expected 'AnotherPerson3', found 'Person1'", 55,
                      25);
        validateError(openRecToClosedRec, index++, "incompatible types: expected 'AnotherPerson4', found 'Person1'", 67,
                      25);
        validateError(openRecToClosedRec, index, "incompatible types: expected 'AnotherPerson4', found 'Person2'", 78,
                      25);
    }
}
