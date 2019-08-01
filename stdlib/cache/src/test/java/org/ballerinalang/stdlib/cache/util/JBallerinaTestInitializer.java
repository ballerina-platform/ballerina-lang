/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.cache.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * A test suit listener for jballerina test cases initialization.
 *
 * @since 0.995.0
 */
public class JBallerinaTestInitializer implements ITestListener {

    private static Logger log = LoggerFactory.getLogger(JBallerinaTestInitializer.class);
    private static final String ENABLE_JBALLERINA_TESTS = "enableJBallerinaTests";

    @Override
    public void onStart(ITestContext context) {
        String property = context.getCurrentXmlTest().getParameter(ENABLE_JBALLERINA_TESTS);
        if (property != null && Boolean.valueOf(property)) {
            log.info("JBallerina tests initialized...");
            System.setProperty(ENABLE_JBALLERINA_TESTS, "true");
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        String property = context.getCurrentXmlTest().getParameter(ENABLE_JBALLERINA_TESTS);
        if (property != null && Boolean.valueOf(property)) {
            log.info("JBallerina tests disabled...");
            System.clearProperty(ENABLE_JBALLERINA_TESTS);
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        //ignore
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        //ignore
    }

    @Override
    public void onTestFailure(ITestResult result) {
        //ignore
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        //ignore
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        //ignore
    }
}
