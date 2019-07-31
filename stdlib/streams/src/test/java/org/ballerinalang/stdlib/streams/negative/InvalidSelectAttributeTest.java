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

package org.ballerinalang.stdlib.streams.negative;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class tests if the attribute selected in the projection is invalid, a proper error is thrown.
 *
 * @since 0.990.0
 */
public class InvalidSelectAttributeTest {

    private CompileResult resultInvalidAttr, resultUndefinedAttr;

    @BeforeClass
    public void setup() {
        resultInvalidAttr = BCompileUtil.compile("test-src/negative/invalid-attribute-select-test.bal");
        resultUndefinedAttr = BCompileUtil.compile("test-src/negative/undefined-attribute-select-test.bal");
    }

    @Test(description = "Test if the proper error is thrown if an attribute is invalid in projection ")
    public void testInvalidAttributeInSelectClause() {
        Assert.assertEquals(resultInvalidAttr.getErrorCount(), 3);
        BAssertUtil.validateError(resultInvalidAttr, 0, "invalid operation: type 'Teacher' " +
                "does not support field access for non-required field 'batches'", 36, 53);
        BAssertUtil.validateError(resultInvalidAttr, 1, "undefined stream attribute 'batches' " +
                "found in select clause", 36, 53);
        BAssertUtil.validateError(resultInvalidAttr, 2, "fields defined in select clause, " +
                "incompatible with output fields in type 'Teacher', expected '[name, age, status, batch, school]' " +
                "but found '[batches, school, name, age, status]'", 37, 9);
    }

    @Test(description = "Test if the proper error is thrown if an attribute is undefined in projection ")
    public void testUndefinedAttributeInSelectClause() {
        Assert.assertEquals(resultUndefinedAttr.getErrorCount(), 1);
        BAssertUtil.validateError(resultUndefinedAttr, 0, "undefined symbol 'age'", 69, 53);
    }
}
