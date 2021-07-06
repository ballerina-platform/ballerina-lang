/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.typedefs;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Type definition test cases.
 */
public class DistinctTypeDefTest {

    private CompileResult neg;

    @BeforeClass
    public void setup() {
        neg = BCompileUtil.compile("test-src/typedefs/distinct-type-negative.bal");
    }

    @Test
    public void testDistinctTypeNegative() {
        int i  = 0;
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 6, 26);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 7, 22);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 8, 27);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 12, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 13, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 14, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 15, 20);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 16, 23);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 20, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 21, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 22, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 23, 20);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 24, 23);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 28, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 29, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 30, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 31, 20);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 32, 23);

        Assert.assertEquals(i, neg.getErrorCount());

    }

    @AfterClass
    public void tearDown() {
        neg = null;
    }
}
