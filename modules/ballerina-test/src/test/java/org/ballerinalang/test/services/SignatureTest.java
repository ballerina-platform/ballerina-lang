/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.services;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Resource signature validation test class.
 */
public class SignatureTest {

    CompileResult compileResult;

    @Test(description = "Test resource signature with a single param")
    public void testSignatureWithSingleParam() {
        String error = null;
        try {
            compileResult = BServiceUtil
                    .setupProgramFile(this, "test-src/services/signature/no-request-param.bal");
        } catch (Exception e) {
            error = e.getMessage();
        }
        Assert.assertTrue(error.contains("resource signature parameter count should be more than two"));
    }

    @Test(description = "Test resource signature without connection param")
    public void testSignatureWithoutConnectionParam() {
        String error = null;
        try {
            compileResult = BServiceUtil
                    .setupProgramFile(this, "test-src/services/signature/no-con-param.bal");
        } catch (Exception e) {
            error = e.getMessage();
        }
        Assert.assertTrue(error.contains("first parameter should be of type - ballerina.net.http:Connection"));
    }

    @Test(description = "Test resource signature with a response param")
    public void testSignatureWithResponseParam() {
        String error = null;
        try {
            compileResult = BServiceUtil
                    .setupProgramFile(this, "test-src/services/signature/with-res-param.bal");
        } catch (Exception e) {
            error = e.getMessage();
        }
        Assert.assertTrue(error.contains("second parameter should be of type - ballerina.net.http:Request"));
    }

    @Test(description = "Test resource signature with an int param as second")
    public void testSignatureWithIntParamAsSecondParam() {
        String error = null;
        try {
            compileResult = BServiceUtil
                    .setupProgramFile(this, "test-src/services/signature/int-param.bal");
        } catch (Exception e) {
            error = e.getMessage();
        }
        Assert.assertTrue(error.contains("second parameter should be of type - ballerina.net.http:Request"));
    }

    @Test(description = "Test resource signature with a boolean param as third param")
    public void testSignatureWithBooleanParamAsThirdParam() {
        String error = null;
        try {
            compileResult = BServiceUtil
                    .setupProgramFile(this, "test-src/services/signature/boolean-param.bal");
        } catch (Exception e) {
            error = e.getMessage();
        }
        Assert.assertTrue(error.contains("incompatible resource signature parameter type"));
    }
}
