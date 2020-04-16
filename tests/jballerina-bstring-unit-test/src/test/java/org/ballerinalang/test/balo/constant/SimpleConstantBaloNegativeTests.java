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
package org.ballerinalang.test.balo.constant;

import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for reading constants.
 */
public class SimpleConstantBaloNegativeTests {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project_negative", "testorg",
                "simple_literal");
        compileResult = BCompileUtil.compile("test-src/balo/test_balo/constant/constant-negative.bal");
    }

    @Test
    public void testNegative() {
        Assert.assertEquals(compileResult.getErrorCount(), 10);

        int index = 0;
        int offset = 1;
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'false', found 'boolean'",
                offset += 7, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'true', found 'boolean'",
                offset += 7, 32);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '40', found 'int'",
                offset += 9, 25);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '20', found 'int'",
                offset += 7, 28);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '240', found 'int'",
                offset += 9, 26);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '4.0f', found 'float'",
                offset += 9, 27);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '2.0f', found 'float'",
                offset += 7, 30);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected '4.0d', found 'float'",
                offset += 9, 29);
        BAssertUtil.validateError(compileResult, index++, "incompatible types: expected 'Ballerina is awesome', found" +
                " 'string'", offset += 9, 28);
        BAssertUtil.validateError(compileResult, index, "incompatible types: expected 'Ballerina rocks', found " +
                "'string'", offset += 7, 31);
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project_negative", "testorg", "foo");
    }
}
