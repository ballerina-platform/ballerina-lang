/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.bala.types;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative Test for variable declaration with table constructor.
 */
public class TableCtrAssignmentTest {
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        negativeResult = BCompileUtil.compile("test-src/bala/test_bala/types/table_ctr_assignment.bal");
    }

    @Test
    public void testNegative() {
        BAssertUtil.validateError(negativeResult, 0, "incompatible types: expected 'testorg/foo:1.0.0:AB', " +
                "found 'table<record {| |}>'",   20, 25);
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
    }
}
