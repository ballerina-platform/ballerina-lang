/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Type definition negative tests.
 */
public class TypeDefinitionsNegativeTest {

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/typedefs/type-definitions-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "uninitialized field 'g'", 7, 12);
    }

    @Test
    public void testSemanticsNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/typedefs/type-definitions-semantics-negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 21);

        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "unknown type 'A'", 1, 9);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'B'", 3, 10);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'C'", 3, 13);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'D'", 5, 13);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'E'", 7, 9);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'F'", 9, 18);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'G'", 11, 18);

        BAssertUtil.validateError(compileResult, index++, "unknown type 'A'", 13, 15);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'B'", 13, 20);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'C'", 13, 23);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'D'", 13, 42);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'E'", 13, 45);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'F'", 13, 60);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'G'", 13, 76);

        BAssertUtil.validateError(compileResult, index++, "unknown type 'A'", 15, 17);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'B'", 15, 23);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'C'", 15, 26);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'D'", 15, 47);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'E'", 15, 51);
        BAssertUtil.validateError(compileResult, index++, "unknown type 'F'", 15, 68);
        BAssertUtil.validateError(compileResult, index, "unknown type 'G'", 15, 85);
    }
}
