/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.record;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for inferring rest filed.
 */
public class RecordDestructuringTest {

    private CompileResult compileResult, negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/record_destructuring.bal");
        negativeResult = BCompileUtil.compile("test-src/record/record_destructuring_negative.bal");
    }

    @Test(description = "Test resolving the rest field type during record restructuring")
    public void testResolvingRestField() {
        BRunUtil.invoke(compileResult, "testRestFieldResolving");
    }

    @Test(description = "Test rest field resolving negative cases")
    public void testResolvingRestFieldNegative() {
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'XY', " +
                "found 'record {| never x?; never y?; anydata...; |}'", 30, 12);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<int>', " +
                "found 'map<(int|string)>'", 44, 12);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'map<int>', " +
                "found 'record {| (int|anydata)...; |}'", 54, 12);
    }

    @AfterClass
    public void tearDown() {
        compileResult = negativeResult = null;
    }
}
