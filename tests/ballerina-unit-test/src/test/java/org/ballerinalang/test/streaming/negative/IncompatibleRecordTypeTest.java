/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.streaming.negative;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test the behavior when there are incompatible record attribute types.
 *
 * @since 0.982.0
 */
public class IncompatibleRecordTypeTest {

    private CompileResult resultNegative;
    private CompileResult resultNegativeForInvalidOrder;

    @BeforeClass
    public void setup() {

        resultNegative = BCompileUtil.
                compile("test-src/streaming/negative/streaming-invalid-record-type-negative-test.bal");
        resultNegativeForInvalidOrder = BCompileUtil.
                compile("test-src/streaming/negative/streaming-output-attribute-order-negative-test.bal");
    }

    @Test(description = "Test filter streaming query with invalid stream attribute type")
    public void testFilterQueryWithInvalidStreamAttributeType() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0,
                "invalid stream attribute type found. it should be either integer or long or " +
                        "string or boolean type attribute",
                43, 9);
    }

    @Test(description = "Test filter streaming query with invalid stream attribute order")
    public void testFilterQueryWithInvalidStreamAttributeOrder() {
        Assert.assertEquals(resultNegativeForInvalidOrder.getErrorCount(), 3);
        BAssertUtil.validateError(resultNegativeForInvalidOrder, 0,
                "incompatible stream action argument type 'Employee' defined",
                45, 9);

        BAssertUtil.validateError(resultNegativeForInvalidOrder, 1,
                "incompatible types: expected type 'int' for attribute 'age', found 'string'",
                45, 9);

        BAssertUtil.validateError(resultNegativeForInvalidOrder, 2,
                "incompatible types: expected type 'string' for attribute 'status', found 'int'",
                45, 9);

    }
}
