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

package org.ballerinalang.stdlib.services.basics;

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Resource signature validation test class.
 *
 * @since 0.95.6
 */
public class SignatureTest {

    @Test()
    public void testSignatureWithSingleParam() {
        CompileResult compileResult = BCompileUtil.compileOffline(new File(getClass().getClassLoader().getResource(
                "test-src/services/signature/no-request-param.bal").getPath()).getAbsolutePath());

        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].message(),
                "resource signature parameter count should be >= 2");
    }

    @Test()
    public void testSignatureWithoutConnectionParam() {
        CompileResult compileResult = BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/signature/no-con-param.bal").getPath()).getAbsolutePath());

        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].message(),
                "first parameter should be of type ballerina/http:1.0.0:Caller");
    }

    @Test()
    public void testSignatureWithResponseParam() {
        CompileResult compileResult = BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/signature/with-res-param.bal").getPath()).getAbsolutePath());

        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].message(),
                "second parameter should be of type ballerina/http:1.0.0:Request");
    }

    @Test()
    public void testSignatureWithIntParamAsSecondParam() {
        CompileResult compileResult = BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/signature/int-param.bal").getPath()).getAbsolutePath());

        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].message(),
                "second parameter should be of type ballerina/http:1.0.0:Request");
    }

    @Test()
    public void testSignatureWithBooleanParamAsThirdParam() {
        CompileResult compileResult = BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/signature/boolean-param.bal").getPath()).getAbsolutePath());
        Assert.assertEquals(compileResult.getErrorCount(), 0);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible entity-body type : int.*")
    public void testSignatureWithInvalidBodyIntParam() {
        CompileResult compileResult = BCompileUtil.compileOffline(new File(getClass().getClassLoader().getResource(
                "test-src/services/signature/invalid-body-param.bal").getPath()).getAbsolutePath());
    }

    @Test
    public void testSignatureWithMismatchedBodyParam() {
        CompileResult compileResult = BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/signature/mismatched-body-param.bal").getPath()).getAbsolutePath());
        Diagnostic[] diag = compileResult.getDiagnostics();
        Assert.assertEquals(diag.length, 2);
        Assert.assertEquals(diag[0].message(), "invalid resource parameter(s): cannot specify > 2 parameters " +
                "without specifying path config and/or body config in the resource annotation");
        Assert.assertEquals(diag[1].message(),
                "Invalid data binding param in the signature : expected 'person', but found 'ballerina'");
    }

    @Test
    public void testSignatureWithInvalidReturn() {
        CompileResult compileResult = BCompileUtil.compile(new File(getClass().getClassLoader().getResource(
                "test-src/services/signature/invalid-return.bal").getPath()).getAbsolutePath());

        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics().clone()[0].message(), "invalid resource " +
                "function return type 'int', expected a subtype of 'error?' containing '()'");
    }

    @Test
    public void testDuplicateResources() {
        CompileResult compileResult = BCompileUtil.compile(new File(getClass().getClassLoader()
                .getResource("test-src/services/resources/duplicate_resource_test.bal").getPath()).getAbsolutePath());
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        BAssertUtil.validateError(compileResult, 0, "redeclared symbol 'dataservice$$service$_0.employee'", 8, 23);
    }
}
