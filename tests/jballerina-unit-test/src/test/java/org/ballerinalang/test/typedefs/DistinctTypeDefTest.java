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
import org.testng.annotations.Test;

/**
 * Type definition test cases.
 */
public class DistinctTypeDefTest {
    @Test
    public void testDistinctTypeNegative() {
        CompileResult neg = BCompileUtil.compile("test-src/typedefs/distinct-type-negative.bal");
        int i  = 0;
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 22, 26);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 23, 22);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 24, 27);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 27, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 28, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 29, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 30, 20);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 31, 23);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 35, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 36, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 37, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 38, 20);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 39, 23);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 43, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 44, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 45, 14);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 46, 20);
        BAssertUtil.validateError(neg, i++, "distinct typing is only supported for object type and error type", 47, 23);
        Assert.assertEquals(i, neg.getErrorCount());

    }
}
