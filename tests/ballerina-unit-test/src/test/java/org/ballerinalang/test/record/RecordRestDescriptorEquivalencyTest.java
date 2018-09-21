/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.record;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.launcher.util.BAssertUtil.validateError;
import static java.lang.String.format;

/**
 * Test case for equivalency of open records with different rest type descriptors.
 *
 * @since 0.982.0
 */
public class RecordRestDescriptorEquivalencyTest {

    @Test
    public void testNegatives() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/record/record_rest_descriptor_equivalency_negative.bal");
        int index = 0;
        String errMsgFmt = "incompatible types: expected '%1$2s', found '%2$2s'";

        Assert.assertEquals(compileResult.getErrorCount(), 11);
        validateError(compileResult, index++, format(errMsgFmt, "IntRest", "StringRest"), 27, 17);
        validateError(compileResult, index++, format(errMsgFmt, "StringRest", "IntRest"), 30, 21);
        validateError(compileResult, index++, format(errMsgFmt, "IntRest", "FloatRest"), 35, 17);
        validateError(compileResult, index++, format(errMsgFmt, "FloatRest", "IntRest"), 38, 20);
        validateError(compileResult, index++, format(errMsgFmt, "IntRest", "BooleanRest"), 43, 17);
        validateError(compileResult, index++, format(errMsgFmt, "BooleanRest", "IntRest"), 46, 22);
        validateError(compileResult, index++, format(errMsgFmt, "StringRest", "FloatRest"), 51, 20);
        validateError(compileResult, index++, format(errMsgFmt, "FloatRest", "StringRest"), 54, 20);
        validateError(compileResult, index++, format(errMsgFmt, "StringRest", "BooleanRest"), 59, 20);
        validateError(compileResult, index++, format(errMsgFmt, "BooleanRest", "StringRest"), 62, 22);
        validateError(compileResult, index, format(errMsgFmt, "RecordRest2", "RecordRest1"), 95, 23);
    }
}
