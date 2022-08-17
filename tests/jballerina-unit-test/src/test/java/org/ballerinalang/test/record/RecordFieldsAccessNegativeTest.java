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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * Negative Test cases for user defined record types with private fields expose and access attempts.
 */
public class RecordFieldsAccessNegativeTest {

    @Test(description = "Test private fields access in record 01")
    public void testRecordPrivateFieldsAccess1() {
        CompileResult result = BCompileUtil.compile("test-src/record/record_project_access_neg_1");

        Assert.assertEquals(result.getWarnCount(), 6);
        Assert.assertEquals(result.getErrorCount(), 2);
        int i = 0;
        validateWarning(result, i++, "attempt to expose non-public symbol 'ChildFoo'", 4, 5);
        validateWarning(result, i++, "attempt to expose non-public symbol 'PrivatePerson'", 16, 44);
        validateWarning(result, i++, "attempt to expose non-public symbol 'PrivatePerson'", 20, 1);
        validateWarning(result, i++, "attempt to expose non-public symbol 'PrivatePerson'", 24, 1);
        validateWarning(result, i++, "attempt to expose non-public symbol 'PrivatePerson'", 24, 72);
        validateWarning(result, i++, "attempt to expose non-public symbol 'FooFamily'", 12, 5);
        validateError(result, i++, "attempt to refer to non-accessible symbol 'PrivatePerson'", 20, 5);
        validateError(result, i, "unknown type 'PrivatePerson'", 20, 5);
    }

    @Test(description = "Test private fields access in record 02")
    public void testRecordPrivateFieldsAccess2() {
        CompileResult result = BCompileUtil.compile("test-src/record/record_project_access_neg_2");

        Assert.assertEquals(result.getWarnCount(), 6);
        Assert.assertEquals(result.getErrorCount(), 2);
        int i = 0;
        validateWarning(result, i++, "attempt to expose non-public symbol 'ChildFoo'", 4, 5);
        validateWarning(result, i++, "attempt to expose non-public symbol 'PrivatePerson'", 16, 44);
        validateWarning(result, i++, "attempt to expose non-public symbol 'PrivatePerson'", 20, 1);
        validateWarning(result, i++, "attempt to expose non-public symbol 'PrivatePerson'", 24, 1);
        validateWarning(result, i++, "attempt to expose non-public symbol 'PrivatePerson'", 24, 72);
        validateWarning(result, i++, "attempt to expose non-public symbol 'FooFamily'", 12, 5);
        validateError(result, i++, "attempt to refer to non-accessible symbol 'FooFamily'", 5, 13);
        validateError(result, i, "attempt to refer to non-accessible symbol 'FooFamily'", 10, 13);
    }
}
