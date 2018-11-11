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
package org.ballerinalang.test.record;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative Test cases for user defined record types with private fields expose and access attempts.
 */
public class RecordFieldsAccessNegativeTest {

    @Test(description = "Test private fields access in record 01")
    public void testRecordPrivateFieldsAccess1() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_fields_negative1.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 10);
        String expectedErrMsg1 = "attempt to expose non-public symbol ";
        String expectedErrMsg2 = "attempt to refer to non-accessible symbol ";

        BAssertUtil.validateError(compileResult, 0, expectedErrMsg1 + "'ChildFoo'", 4, 5);
        BAssertUtil.validateError(compileResult, 1, expectedErrMsg1 + "'PrivatePerson'", 16, 44);
        BAssertUtil.validateError(compileResult, 2, expectedErrMsg1 + "'PrivatePerson'", 24, 72);
        BAssertUtil.validateError(compileResult, 3, expectedErrMsg1 + "'FooFamily'", 12, 5);
        BAssertUtil.validateError(compileResult, 4, expectedErrMsg2 + "'ChildFoo'", 4, 33);
        BAssertUtil.validateError(compileResult, 5, expectedErrMsg2 + "'PrivatePerson'", 8, 13);
        BAssertUtil.validateError(compileResult, 6, expectedErrMsg2 + "'PrivatePerson'", 16, 13);
        BAssertUtil.validateError(compileResult, 7, expectedErrMsg2 + "'PrivatePerson'", 20, 5);
        BAssertUtil.validateError(compileResult, 8, "unknown type 'PrivatePerson'", 20, 5);
        BAssertUtil.validateError(compileResult, 9, "invalid literal for type 'other'", 20, 27);
    }

    @Test(description = "Test private fields access in record 02")
    public void testRecordPrivateFieldsAccess2() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_fields_negative2.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 6);
        String expectedErrMsg1 = "attempt to expose non-public symbol ";
        String expectedErrMsg2 = "attempt to refer to non-accessible symbol ";

        BAssertUtil.validateError(compileResult, 0, expectedErrMsg1 + "'ChildFoo'", 4, 5);
        BAssertUtil.validateError(compileResult, 1, expectedErrMsg1 + "'PrivatePerson'", 16, 44);
        BAssertUtil.validateError(compileResult, 2, expectedErrMsg1 + "'PrivatePerson'", 24, 72);
        BAssertUtil.validateError(compileResult, 3, expectedErrMsg1 + "'FooFamily'", 12, 5);
        BAssertUtil.validateError(compileResult, 4, expectedErrMsg2 + "'FooFamily'", 5, 13);
        BAssertUtil.validateError(compileResult, 5, expectedErrMsg2 + "'FooFamily'", 10, 13);
    }
}
