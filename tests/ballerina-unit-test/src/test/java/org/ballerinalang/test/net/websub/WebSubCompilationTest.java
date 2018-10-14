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

package org.ballerinalang.test.net.websub;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test WebSub Service Compilation Errors.
 */
public class WebSubCompilationTest {

    private CompileResult negativeCompilationResult =
            BCompileUtil.compile("test-src/net/websub/compilation/test_compilation_failure.bal");

    @Test(description = "Verify compilation error count")
    public void testErrorDiagLength() {
        Assert.assertEquals(negativeCompilationResult.getDiagnostics().length, 12);
    }

    @Test(description = "Test specifying > 1 SubscriberServiceConfig annotations")
    public void testInvalidAnnotationCount() {
        BAssertUtil.validateError(negativeCompilationResult, 0,
                                  "cannot have more than one 'SubscriberServiceConfig' annotation", 34, 1);
    }

    @Test(description = "Test invalid onIntentVerification signature params")
    public void testInvalidOnIntentVerificationSignatureParams() {
        BAssertUtil.validateError(negativeCompilationResult, 1,
                                  "invalid resource signature for 'onIntentVerification', expected "
                                                + "'ballerina/websub:Listener' as first parameter", 49, 27);
        BAssertUtil.validateError(negativeCompilationResult, 2,
                                  "invalid resource signature for 'onIntentVerification', expected "
                                                + "'ballerina/websub:IntentVerificationRequest' as second parameter",
                                  49, 49);
    }

    @Test(description = "Test invalid onNotification signature params")
    public void testInvalidOnNotificationSignatureParams() {
        BAssertUtil.validateError(negativeCompilationResult, 3,
                                  "invalid resource signature for 'onNotification', expected "
                                          + "'ballerina/websub:Notification' as first parameter", 52, 21);
    }

    @Test(description = "Test invalid resource")
    public void testInvalidResource() {
        BAssertUtil.validateError(negativeCompilationResult, 5,
                                  "invalid resource name 'onNotificationTwo' only two resources allowed "
                                      + "with ballerina/websub:Service, 'onIntentVerification' and 'onNotification'",
                                  63, 5);
    }

    @Test(description = "Test not specifying onNotification resource")
    public void testOnNotificationUnspecified() {
        String errorMessage = "required resource 'onNotification' not specified with ballerina/websub:Service";
        BAssertUtil.validateError(negativeCompilationResult, 4, errorMessage, 62, 1);
        BAssertUtil.validateError(negativeCompilationResult, 6, errorMessage, 73, 1);
    }

    @Test(description = "Test onIntentVerification missing param")
    public void testOnIntentVerificationMissingParam() {
        String errorMessage = "invalid param count for WebSub Resource 'onIntentVerification', expected: 2 found: 1";
        BAssertUtil.validateError(negativeCompilationResult, 7, errorMessage, 85, 5);
        BAssertUtil.validateError(negativeCompilationResult, 9, errorMessage, 99, 5);
    }

    @Test(description = "Test onNotification missing param")
    public void testOnNotificationMissingParam() {
        BAssertUtil.validateError(negativeCompilationResult, 8,
                                  "invalid param count for WebSub Resource 'onNotification', expected: 1 found: 0",
                                  88, 5);
    }

    @Test(description = "Test onIntentVerification extra param")
    public void testOnIntentVerificationExtraParam() {
        BAssertUtil.validateError(negativeCompilationResult, 10,
                                  "invalid param count for WebSub Resource 'onIntentVerification', "
                                          + "expected: 2 found: 3", 113, 5);
    }

    @Test(description = "Test onNotification extra param")
    public void testOnNotificationExtraParam() {
        BAssertUtil.validateError(negativeCompilationResult, 11,
                                  "invalid param count for WebSub Resource 'onNotification', expected: 1 found: 2",
                                  116, 5);
    }
}
