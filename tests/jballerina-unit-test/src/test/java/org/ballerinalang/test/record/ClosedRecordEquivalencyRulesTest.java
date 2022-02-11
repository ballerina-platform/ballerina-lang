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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

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
        int i = 0;
        validateError(negative, i++, "incompatible types: expected 'AnotherPerson', found 'Person1'", 28, 24);
        validateError(negative, i++, "incompatible types: expected 'AnotherPerson', found 'Person2'", 38, 24);
        assertEquals(negative.getErrorCount(), i);
    }

    @Test(description = "Test assigning a closed record to a cloesd record type variable")
    public void testCRToCRClosedToClosedAssignment1() {
        Object returns = JvmRunUtil.invoke(closedRecToClosedRec, "testClosedToClosedAssignment1");
        assertEquals(returns.toString(), "{\"name\":\"John Doe\",\"age\":25}");
    }

    @Test(description = "RHS and LHS closed with RHS field types which are assignable to LHS field types")
    public void testCRToCRClosedToClosedAssignment2() {
        Object returns = JvmRunUtil.invoke(closedRecToClosedRec, "testClosedToClosedAssignment2");
        assertEquals(returns.toString(), "{\"name\":\"John Doe\",\"age\":25}");
    }

    @Test(description = "RHS and LHS closed with RHS required fields corresponding to LHS optional fields")
    public void testCRToCRReqFieldToOptField() {
        Object returns = JvmRunUtil.invoke(closedRecToClosedRec, "testReqFieldToOptField");
        assertEquals(returns.toString(), "{\"name\":\"John Doe\",\"age\":25}");
    }

    @Test(description = "RHS and LHS closed with RHS optional fields corresponding to LHS optional fields")
    public void testCRToCROptFieldToOptField1() {
        Object returns = JvmRunUtil.invoke(closedRecToClosedRec, "testOptFieldToOptField1");
        assertEquals(returns.toString(), "{\"name\":\"John Doe\",\"age\":25}");
    }

    @Test(description = "RHS and LHS closed with RHS optional fields corresponding to LHS optional fields")
    public void testCRToCROptFieldToOptField2() {
        BArray returns = (BArray) JvmRunUtil.invoke(closedRecToClosedRec, "testOptFieldToOptField2");
        assertEquals(returns.get(0).toString(), "{\"name\":\"John Doe\",\"age\":25}");
        assertNull(returns.get(1));
    }

    @Test(description = "RHS and LHS closed with RHS type being a public typedesc")
    public void testCRToCRHeterogeneousTypedescEq() {
        Object returns = JvmRunUtil.invoke(closedRecToClosedRec, "testHeterogeneousTypedescEq");
        assertEquals(returns.toString(), "{\"name\":\"John Doe\",\"age\":25}");
    }

    @Test(description = "RHS and LHS closed with LHS type being a public typedesc")
    public void testCRToCRHeterogeneousTypedescEq2() {
        Object returns = JvmRunUtil.invoke(closedRecToClosedRec, "testHeterogeneousTypedescEq2");
        assertEquals(returns.toString(), "{\"name\":\"John Doe\",\"age\":25}");
    }

    @Test(description = "RHS open and LHS closed is disallowed")
    public void testORToCR() {
        CompileResult openRecToClosedRec = BCompileUtil.compile("test-src/record/equivalency_rules_or_to_cr.bal");
        int index = 0;
        assertEquals(openRecToClosedRec.getErrorCount(), 5);
        validateError(openRecToClosedRec, index++, "incompatible types: expected 'AnotherPerson1', found 'Person1'", 29,
                25);
        validateError(openRecToClosedRec, index++, "incompatible types: expected 'AnotherPerson2', found 'Person1'", 40,
                25);
        validateError(openRecToClosedRec, index++, "incompatible types: expected 'AnotherPerson3', found 'Person1'", 52,
                25);
        validateError(openRecToClosedRec, index++, "incompatible types: expected 'AnotherPerson4', found 'Person1'", 63,
                25);
        validateError(openRecToClosedRec, index, "incompatible types: expected 'AnotherPerson4', found 'Person2'", 74,
                25);
    }

    @AfterClass
    public void tearDown() {
        closedRecToClosedRec = null;
    }
}
