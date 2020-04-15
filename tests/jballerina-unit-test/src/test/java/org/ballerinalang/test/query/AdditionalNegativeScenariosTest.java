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

package org.ballerinalang.test.query;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Additional negative test cases for query expressions and query actions
 *
 * @since 1.2.0
 */
public class AdditionalNegativeScenariosTest {

    @Test
    public void testAllAdditionalNegativeScenarios() {
        CompileResult compileResult = BCompileUtil.compile("test-src/query/additional-negative-scenarios.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 9);
        int index = 0;

        validateError(compileResult, index++, "missing non-defaultable required record field 'lastName'", 33, 10);
        validateError(compileResult, index++, "incompatible types: expected 'float', found 'int'", 54, 13);
        validateError(compileResult, index++, "undefined function 'calculateScore'", 69, 22);
        validateError(compileResult, index++, "invalid record binding pattern; unknown field 'fname' in record type 'Student'"
                , 106, 12);
        validateError(compileResult, index++, "undefined symbol 'fname'", 108, 15);
        validateError(compileResult, index++, "incompatible types: expected 'Student', found '(string|float)'", 123, 10);
        validateError(compileResult, index++, "cannot assign a value to final 'twiceScore'", 140, 10);
        validateError(compileResult, index++, "incompatible types: expected 'FullName[]', found 'error?'", 147, 13);
        validateError(compileResult, index, "incompatible types: expected 'Address', found 'map<string>'", 164, 13);
    }
}
