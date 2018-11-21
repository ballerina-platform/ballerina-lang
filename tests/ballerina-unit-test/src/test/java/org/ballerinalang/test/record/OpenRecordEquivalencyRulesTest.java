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
 * Test cases for verifying the equivalency/assignability rules for open records.
 *
 * @since 0.985.0
 */
public class OpenRecordEquivalencyRulesTest {

    private CompileResult closedRecToOpenRec;
    private CompileResult openRecToOpenRec;

    @BeforeClass
    public void setup() {
        closedRecToOpenRec = BCompileUtil.compile("test-src/record/equivalency_rules_cr_to_or.bal");
        openRecToOpenRec = BCompileUtil.compile("test-src/record/equivalency_rules_or_to_or.bal");
    }

    @Test(description = "Negative tests for when both are open records")
    public void testORToORNeg() {
        CompileResult negative = BCompileUtil.compile("test-src/record/equiv_rules_neg_or_to_or.bal");
        int index = 0;
        assertEquals(negative.getErrorCount(), 4);
        validateError(negative, index++, "incompatible types: expected 'AnotherPerson', found 'Person1'", 28, 24);
        validateError(negative, index++, "incompatible types: expected 'AnotherPerson', found 'Person2'", 38, 24);
        validateError(negative, index++, "incompatible types: expected 'AnotherPerson', found 'Person3'", 50, 24);
        validateError(negative, index, "incompatible types: expected 'AnotherPerson', found 'Person4'", 61, 24);
    }

    @Test(description = "Negative tests for when LHS is open and RHS is closed")
    public void testCRToORNeg() {
        CompileResult negative = BCompileUtil.compile("test-src/record/equiv_rules_neg_cr_to_or.bal");
        int index = 0;
        assertEquals(negative.getErrorCount(), 3);
        validateError(negative, index++, "incompatible types: expected 'AnotherPerson', found 'Person1'", 29, 24);
        validateError(negative, index++, "incompatible types: expected 'AnotherPerson', found 'Person2'", 40, 24);
        validateError(negative, index, "incompatible types: expected 'AnotherPerson', found 'Person3'", 53, 24);
    }

    @Test(description = "Test assigning a closed record to an open record type variable")
    public void testCRToORClosedToOpenAssignment1() {
        BValue[] returns = BRunUtil.invoke(closedRecToOpenRec, "testClosedToOpenAssignment1");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS closed and LHS open with RHS field types which are assignable to LHS field types")
    public void testCRToORClosedToOpenAssignment2() {
        BValue[] returns = BRunUtil.invoke(closedRecToOpenRec, "testClosedToOpenAssignment2");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS closed and LHS open with additional fields (optional) than RHS")
    public void testCRToORClosedToOpenAssignment3() {
        BValue[] returns = BRunUtil.invoke(closedRecToOpenRec, "testClosedToOpenAssignment3");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS closed and LHS open with RHS required fields corresponding to LHS optional fields")
    public void testCRToORReqFieldToOptField() {
        BValue[] returns = BRunUtil.invoke(closedRecToOpenRec, "testReqFieldToOptField");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS closed and LHS open with RHS optional fields corresponding to LHS optional fields")
    public void testCRToOROptFieldToOptField1() {
        BValue[] returns = BRunUtil.invoke(closedRecToOpenRec, "testOptFieldToOptField1");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS closed and LHS open with RHS optional fields corresponding to LHS optional fields",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*cannot find key 'age'.*")
    public void testCRToOROptFieldToOptField2() {
        BRunUtil.invoke(closedRecToOpenRec, "testOptFieldToOptField2");
    }

    @Test(description = "RHS closed and LHS open with RHS having additional fields which are assignable to the rest " +
            "field of the LHS type")
    public void testCRToORAdditionalFieldsToRest() {
        BValue[] returns = BRunUtil.invoke(closedRecToOpenRec, "testAdditionalFieldsToRest");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25, address:\"Colombo, Sri Lanka\", " +
                "weight:70.0}");
    }

    @Test(description = "Test assigning an open record to an open record type variable")
    public void testORToOROpenToOpenAssignment1() {
        BValue[] returns = BRunUtil.invoke(openRecToOpenRec, "testOpenToOpenAssignment1");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS and LHS both open with RHS field types which are assignable to LHS field types")
    public void testORToOROpenToOpenAssignment2() {
        BValue[] returns = BRunUtil.invoke(openRecToOpenRec, "testOpenToOpenAssignment2");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS and LHS both open with additional fields (optional) than RHS")
    public void testORToOROpenToOpenAssignment3() {
        BValue[] returns = BRunUtil.invoke(openRecToOpenRec, "testOpenToOpenAssignment3");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS and LHS both open with RHS required fields corresponding to LHS optional fields")
    public void testORToORReqFieldToOptField() {
        BValue[] returns = BRunUtil.invoke(openRecToOpenRec, "testReqFieldToOptField");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS and LHS both open with RHS optional fields corresponding to LHS optional fields")
    public void testORToOROptFieldToOptField1() {
        BValue[] returns = BRunUtil.invoke(openRecToOpenRec, "testOptFieldToOptField1");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(description = "RHS and LHS both open with RHS optional fields corresponding to LHS optional fields",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*cannot find key 'age'.*")
    public void testORToOROptFieldToOptField2() {
        BRunUtil.invoke(openRecToOpenRec, "testOptFieldToOptField2");
    }

    @Test(description = "RHS and LHS both open with RHS having additional fields which are assignable to the rest " +
            "field of the LHS type")
    public void testORToORAdditionalFieldsToRest() {
        BValue[] returns = BRunUtil.invoke(openRecToOpenRec, "testAdditionalFieldsToRest");
        assertEquals(returns[0].stringValue(), "{name:\"John Doe\", age:25, address:\"Colombo, Sri Lanka\", " +
                "weight:70.0}");
    }

    @Test(description = "Both open with RHS having a union rest field which a sub type of the LHS rest field type")
    public void testORToORRestFieldToRestField1() {
        BValue[] returns = BRunUtil.invoke(openRecToOpenRec, "testRestFieldToRestField1");
        assertEquals(returns[0].stringValue(), "{s:\"qwerty\", rest1:\"asdf\", rest2:123}");
    }

    @Test(description = "Adding an invalid rest field type",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*invalid map insertion: expected value of type 'string\\|int', found " +
                  "'float'.*")
    public void testORToORRestFieldToRestField2() {
        BRunUtil.invoke(openRecToOpenRec, "testRestFieldToRestField2");
    }
}
