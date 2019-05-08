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
package org.ballerinalang.test.types.constant;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Constant negative tests.
 */
public class RecordConstantNegativeTest {

    @Test
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/" +
                "record-constant-negative.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 39);

        String expectedErrMsg = "not a constant expression";

        int index = 0;
        int offset = 0;

        // boolean
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 16, 46);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 46);
        BAssertUtil.validateError(compileResult, index++, "invalid key: only identifiers are allowed for record " +
                "literal keys", offset += 2, 41);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 41);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 51);

        // int
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 21, 42);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 42);
        BAssertUtil.validateError(compileResult, index++, "invalid key: only identifiers are allowed for record " +
                "literal keys", offset += 2, 37);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 37);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 47);

        // byte
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 21, 46);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 46);
        BAssertUtil.validateError(compileResult, index++, "invalid key: only identifiers are allowed for record " +
                "literal keys", offset += 2, 41);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 41);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 51);

        // float
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 21, 44);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 44);
        BAssertUtil.validateError(compileResult, index++, "invalid key: only identifiers are allowed for record " +
                "literal keys", offset += 2, 39);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 39);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 49);

        // decimal
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 21, 46);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 46);
        BAssertUtil.validateError(compileResult, index++, "invalid key: only identifiers are allowed for record " +
                "literal keys", offset += 2, 41);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 41);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 51);

        // string
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 21, 45);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 45);
        BAssertUtil.validateError(compileResult, index++, "invalid key: only identifiers are allowed for record " +
                "literal keys", offset += 2, 40);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 40);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 50);

        // nil
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 21, 41);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset += 3, 41);
        BAssertUtil.validateError(compileResult, index++, "invalid key: only identifiers are allowed for record " +
                "literal keys", offset += 2, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 36);
        BAssertUtil.validateError(compileResult, index++, expectedErrMsg, offset, 46);

        // Invalid record fields.
        BAssertUtil.validateError(compileResult, index++, "unsupported constant record field type 'any'", offset += 15,
                5);
        BAssertUtil.validateError(compileResult, index++, "unsupported rest field type 'any' found in the record type",
                offset += 7, 7);
        BAssertUtil.validateError(compileResult, index++, "unsupported constant record field type 'any'",
                offset += 5, 5);
        BAssertUtil.validateError(compileResult, index, "unsupported rest field type 'anydata|error' found in the " +
                "record type", offset += 6, 7);
    }
}
