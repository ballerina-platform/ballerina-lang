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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class tests if the attribute selected in the projection is invalid, a proper error is thrown.
 *
 * @since 0.990.0
 */
public class InvalidSelectAttributeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/negative/invalid-attribute-select-test.bal");
    }

    @Test(description = "Test if the proper error is thrown if an attribute is invalid in projection ")
    public void testInvalidAttributeInSelectClause() {
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0, "undefined stream attribute 'batches' found in select clause", 36, 53);
        BAssertUtil.validateError(result, 1, "fields defined in select clause, incompatible " +
                "with output fields in type 'Teacher', expected '[name, age, status, batch, school]' but found " +
                "'[batches, school, name, age, status]'", 37, 9);
    }
}
