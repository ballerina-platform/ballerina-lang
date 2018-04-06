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

package org.ballerinalang.test.services.basics;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.Test;

/**
 * Resource signature validation test class.
 *
 * @since 0.95.6
 */
public class SignatureTest {

    private CompileResult compileResult;

    @Test(expectedExceptions = BallerinaConnectorException.class,
            expectedExceptionsMessageRegExp = "resource signature parameter count should be >= 2")
    public void testSignatureWithSingleParam() {
        compileResult = BCompileUtil.compile(getClass().getClassLoader().getResource(
                "test-src/services/signature/no-request-param.bal").getPath());
        BServiceUtil.runService(compileResult);
    }

    @Test(expectedExceptions = BallerinaConnectorException.class,
            expectedExceptionsMessageRegExp = "first parameter should be of type ballerina/http:ServerConnector")
    public void testSignatureWithoutConnectionParam() {
        compileResult = BCompileUtil.compile(getClass().getClassLoader().getResource(
                "test-src/services/signature/no-con-param.bal").getPath());
        BServiceUtil.runService(compileResult);
    }

    @Test(expectedExceptions = BallerinaConnectorException.class,
            expectedExceptionsMessageRegExp = "second parameter should be of type ballerina/http:Request")
    public void testSignatureWithResponseParam() {
        compileResult = BCompileUtil.compile(getClass().getClassLoader().getResource(
                "test-src/services/signature/with-res-param.bal").getPath());
        BServiceUtil.runService(compileResult);
    }

    @Test(expectedExceptions = BallerinaConnectorException.class,
            expectedExceptionsMessageRegExp = "second parameter should be of type ballerina/http:Request")
    public void testSignatureWithIntParamAsSecondParam() {
        compileResult = BCompileUtil.compile(getClass().getClassLoader().getResource(
                "test-src/services/signature/int-param.bal").getPath());
        BServiceUtil.runService(compileResult);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible resource signature parameter type.*")
    public void testSignatureWithBooleanParamAsThirdParam() {
        compileResult = BCompileUtil.compile(getClass().getClassLoader().getResource(
                "test-src/services/signature/boolean-param.bal").getPath());
        BServiceUtil.runService(compileResult);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible entity-body type : int.*")
    public void testSignatureWithInvalidBodyIntParam() {
        compileResult = BCompileUtil.compile(getClass().getClassLoader().getResource(
                "test-src/services/signature/invalid-body-param.bal").getPath());
        BServiceUtil.runService(compileResult);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*expected 'person' as param name, but found 'ballerina'.*")
    public void testSignatureWithMismatchedBodyParam() {
        compileResult = BCompileUtil.compile(getClass().getClassLoader().getResource(
                "test-src/services/signature/mismatched-body-param.bal").getPath());
        BServiceUtil.runService(compileResult);
    }
}
