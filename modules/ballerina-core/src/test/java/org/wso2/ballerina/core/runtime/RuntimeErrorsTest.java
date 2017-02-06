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

package org.wso2.ballerina.core.runtime;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.Get;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.HTTPConnector;
import org.wso2.ballerina.core.nativeimpl.lang.json.GetString;
import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandlerUtils;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.runtime.registry.PackageRegistry;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.MessageUtils;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.core.utils.Services;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Runtime Errors test class for ballerina filers.
 * This class test error handling for runtime errors in ballerina files.
 */
public class RuntimeErrorsTest {
    
    private BallerinaFile bFile;
    
    @BeforeClass
    public void setup() {
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        PackageRegistry.getInstance().registerNativeFunction(new GetString());
        PackageRegistry.getInstance().registerNativeConnector(new HTTPConnector());
        PackageRegistry.getInstance().registerNativeAction(new Get());
        bFile = ParserUtils.parseBalFile("lang/errors/runtime-errors.bal", symScope);
        EnvironmentInitializer.initialize("lang/errors/undeclared-package-errors.bal");
    }

    @Test
    public void testStackTraceOnError() {
        Exception ex = null;
        Context bContext = new Context();
        String expectedStackTrace = "\t at test.lang:getApple(runtime-errors.bal:26)\n" +
                "\t at test.lang:getFruit2(runtime-errors.bal:22)\n" +
                "\t at test.lang:getFruit1(runtime-errors.bal:18)\n" +
                "\t at test.lang:testStackTrace(runtime-errors.bal:15)\n";
        try {
            Functions.invoke(bFile, "testStackTrace", bContext);
        } catch (BallerinaException e) {
            ex = e;
        } finally {
            Assert.assertTrue(ex instanceof BallerinaException, "Expected a " + BallerinaException.class.getName() +
                ", but found: " + ex + ".");
            Assert.assertEquals(ex.getMessage(), "Array index out of range: Index: 24, Size: 0", 
                    "Incorrect error message printed.");
            // Check the stack trace
            String stackTrace = ErrorHandlerUtils.getServiceStackTrace(bContext, ex);
            Assert.assertEquals(stackTrace, expectedStackTrace);
        }
    }
    
    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "Failed to get string from json. Invalid jsonpath: Path must not end " +
            "with a '.' or '..'")
    public void testNativeFunctionError() {
        Functions.invoke(bFile, "nativeFunctionErrorTest");
    }

    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "Failed to invoke 'get' action in HTTPConnector. Malformed url " +
            "specified. no protocol: malformed/url/context")
    public void testNativeConnectorError() {
        Functions.invoke(bFile, "nativeConnectorErrorTest");
    }
    
    @Test
    public void testStackOverflowError() {
        Throwable ex = null;
        Context bContext = new Context();
        String expectedStackTrace = getStackOverflowTrace();
        try {
            Functions.invoke(bFile, "testStackOverflow", bContext);
        } catch (Throwable e) {
            ex = e;
        } finally {
            Assert.assertTrue(ex instanceof StackOverflowError, "Expected a " + StackOverflowError.class.getName() +
                ", but found: " + ex + ".");
            
            // Check the stack trace
            String stackTrace = ErrorHandlerUtils.getServiceStackTrace(bContext, ex);
            Assert.assertEquals(stackTrace, expectedStackTrace);
        }
    }
    
    @Test(description = "Test error of a service in default package")
    public void testDefaultPackageServiceError() {
        Throwable ex = null;
        String expectedStackTrace = "\t at getApple(undeclared-package-errors.bal:22)\n" +
                                    "\t at getFruit2(undeclared-package-errors.bal:18)\n" +
                                    "\t at getFruit1(undeclared-package-errors.bal:14)\n" +
                                    "\t at testStackTrace(undeclared-package-errors.bal:6)\n" +
                                    "\t at echo(undeclared-package-errors.bal:5)\n" +
                                    "\t at echo(undeclared-package-errors.bal:2)\n";
        try {
            CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/test/error", "GET");
            Services.invoke(cMsg);
        } catch (BallerinaException e) {
            ex = e;
        } finally {
            // Check exception type
            Assert.assertTrue(ex instanceof BallerinaException, "Expected a " + BallerinaException.class.getName() +
                    ", but found: " + ex + ".");
            
            // Check error message
            String errorMsg = ex.getCause().getMessage();
            Assert.assertEquals(errorMsg, "error in ballerina program: array index out of range: Index: 24, Size: 0",
                    "Incorrect error message printed.");
            
            // Check the stack trace
            String stackTrace = ErrorHandlerUtils.getServiceStackTrace(
                    ((BallerinaException) ex.getCause()).getContext(), ex);
            Assert.assertEquals(stackTrace, expectedStackTrace);
        }
    }
    
    private static String getStackOverflowTrace() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            if (i == 20 || i == 21) {
                sb.append("\t ...\n");
            } else {
                sb.append("\t at test.lang:infiniteRecurse(runtime-errors.bal:51)\n");
            }
        }
        sb.append("\t at test.lang:infiniteRecurse(runtime-errors.bal:47)\n");
        sb.append("\t at test.lang:testStackOverflow(runtime-errors.bal:46)\n");
        return sb.toString();
    }
}
