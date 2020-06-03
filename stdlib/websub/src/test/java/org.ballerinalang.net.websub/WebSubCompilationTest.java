/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.websub;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test WebSub Service Compilation Errors.
 */
public class WebSubCompilationTest {

    private CompileResult negativeCompilationResult;

    @BeforeClass
    public void setup() {
        negativeCompilationResult =
                BCompileUtil.compile("test-src/compilation/test_compilation_failure.bal");
        Assert.assertEquals(negativeCompilationResult.getDiagnostics().length, 12);
    }

    @Test(description = "Test specifying > 1 SubscriberServiceConfig annotations")
    public void testInvalidAnnotationCount() {
        CompileResult invalidAnnotsResult = BCompileUtil.compile("test-src/compilation/test_multiple_annots.bal");
        BAssertUtil.validateError(invalidAnnotsResult, 0,
                                  "cannot specify more than one annotation value for annotation " +
                                          "'SubscriberServiceConfig'", 20, 1);
    }

    @Test(description = "Test invalid onIntentVerification signature params")
    public void testInvalidOnIntentVerificationSignatureParams() {
        BAssertUtil.validateError(negativeCompilationResult, 0,
                                  "invalid resource signature for 'onIntentVerification', expected "
                                                + "'ballerina/websub:1.0.0:Caller' as first parameter", 43, 45);
        BAssertUtil.validateError(negativeCompilationResult, 1,
                                  "invalid resource signature for 'onIntentVerification', expected " +
                                          "'ballerina/websub:1.0.0:IntentVerificationRequest' as second " +
                                          "parameter", 43, 67);
    }

    @Test(description = "Test invalid onNotification signature params")
    public void testInvalidOnNotificationSignatureParams() {
        BAssertUtil.validateError(negativeCompilationResult, 2,
                                  "invalid resource signature for 'onNotification', expected "
                                          + "'ballerina/websub:1.0.0:Notification' as first parameter", 46, 39);
    }

    @Test(description = "Test invalid resource")
    public void testInvalidResource() {
        BAssertUtil.validateError(negativeCompilationResult, 4,
                                  "invalid resource name 'onNotificationTwo' only two resources allowed "
                                      + "with ballerina/websub:Service, 'onIntentVerification' and 'onNotification'",
                                  57, 5);
    }

    @Test(description = "Test not specifying onNotification resource")
    public void testOnNotificationUnspecified() {
        String errorMessage = "required resource 'onNotification' not specified with ballerina/websub:Service";
        BAssertUtil.validateError(negativeCompilationResult, 3, errorMessage, 51, 1);
        BAssertUtil.validateError(negativeCompilationResult, 5, errorMessage, 62, 1);
    }

    @Test(description = "Test onIntentVerification missing param")
    public void testOnIntentVerificationMissingParam() {
        String errorMessage = "invalid param count for WebSub Resource 'onIntentVerification', expected: 2 found: 1";
        BAssertUtil.validateError(negativeCompilationResult, 6, errorMessage, 79, 5);
        BAssertUtil.validateError(negativeCompilationResult, 8, errorMessage, 93, 5);
    }

    @Test(description = "Test onNotification missing param")
    public void testOnNotificationMissingParam() {
        BAssertUtil.validateError(negativeCompilationResult, 7,
                                  "invalid param count for WebSub Resource 'onNotification', expected: 1 found: 0",
                                  82, 5);
    }

    @Test(description = "Test onIntentVerification extra param")
    public void testOnIntentVerificationExtraParam() {
        BAssertUtil.validateError(negativeCompilationResult, 9,
                                  "invalid param count for WebSub Resource 'onIntentVerification', "
                                          + "expected: 2 found: 3", 107, 5);
    }

    @Test(description = "Test onNotification extra param")
    public void testOnNotificationExtraParam() {
        BAssertUtil.validateError(negativeCompilationResult, 10,
                                  "invalid param count for WebSub Resource 'onNotification', expected: 1 found: 2",
                                  110, 5);
    }

    @Test(description = "Test subscriber without SubscriberServiceConfig")
    public void testNoSubscriberServiceConfig() {
        BAssertUtil.validateError(negativeCompilationResult, 11, "'SubscriberServiceConfig' annotation is compulsory",
                                  115, 1);
    }

    @Test(description = "Test invalid resource return type")
    public void testInvalidResourceReturnType() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/compilation/test_invalid_return_negative.bal");
        BAssertUtil.validateError(negativeResult, 0, "invalid resource function return type 'int?', expected a " +
                "subtype of 'error?' containing '()'", 26, 80);
    }
}
