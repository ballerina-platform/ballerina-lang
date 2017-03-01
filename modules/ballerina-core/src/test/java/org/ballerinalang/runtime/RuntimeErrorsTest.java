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

package org.ballerinalang.runtime;

import org.ballerinalang.bre.Context;
import org.ballerinalang.core.EnvironmentInitializer;
import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.core.utils.MessageUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.util.Services;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Runtime Errors test class for ballerina filers.
 * This class test error handling for runtime errors in ballerina files.
 */
public class RuntimeErrorsTest {
    
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/errors/runtime");
    }

    @Test
    public void testStackTraceOnError() {
        Exception ex = null;
        Context bContext = new Context();
        String expectedStackTrace = "\t at lang.errors.runtime:getFruit2(runtime-errors.bal:23)\n" +
                "\t at lang.errors.runtime:getFruit1(runtime-errors.bal:19)\n" + 
                "\t at lang.errors.runtime:testStackTrace(runtime-errors.bal:15)\n";
        try {
            BLangFunctions.invoke(bLangProgram, "testStackTrace", new BString[0], bContext);
        } catch (Exception e) {
            ex = e;
        } finally {
            Assert.assertTrue(ex instanceof BallerinaException, "Expected a " + BallerinaException.class.getName() +
                ", but found: " + ex + ".");
            Assert.assertEquals(getRootCause(ex).getMessage(), "arrays index out of range: Index: 24, Size: 0", 
                    "Incorrect error message printed.");
            // Check the stack trace
            String stackTrace = ErrorHandlerUtils.getMainFuncStackTrace(bContext, ex);
            Assert.assertEquals(stackTrace, expectedStackTrace);
        }
    }
    
    @Test
    public void testStackOverflowError() {
        Throwable ex = null;
        Context bContext = new Context();
        String expectedStackTrace = getStackOverflowTrace();
        try {
            BLangFunctions.invoke(bLangProgram, "testStackOverflow", new BString[0], bContext);
        } catch (Throwable e) {
            ex = e;
        } finally {
            Assert.assertTrue(ex instanceof StackOverflowError, "Expected a " + StackOverflowError.class.getName() +
                ", but found: " + ex + ".");
            
            // Check the stack trace
            String stackTrace = ErrorHandlerUtils.getMainFuncStackTrace(bContext, ex);
            Assert.assertEquals(stackTrace, expectedStackTrace);
        }
    }
    
    @Test(description = "Test error of a service in default package")
    public void testDefaultPackageServiceError() {
        BLangProgram undeclaredPackageProgram = EnvironmentInitializer.setup(
                "lang/errors/undeclared-package-errors.bal");
        Throwable ex = null;
        String expectedStackTrace = "\t at getFruit2(undeclared-package-errors.bal:22)\n" +
                                    "\t at getFruit1(undeclared-package-errors.bal:18)\n" +
                                    "\t at testStackTrace(undeclared-package-errors.bal:14)\n" +
                                    "\t at echoResource(undeclared-package-errors.bal:6)\n" +
                                    "\t at echoService(undeclared-package-errors.bal:5)\n";
        try {
            CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/test/error", "GET");
            Services.invoke(cMsg);
        } catch (Exception e) {
            ex = e;
        } finally {
            // Check exception type
            Assert.assertTrue(ex instanceof BallerinaException, "Expected a " + BallerinaException.class.getName() +
                    ", but found: " + ex + ".");
            
            // Check error message
            String errorMsg = getRootCause(ex).getMessage();
            Assert.assertEquals(errorMsg, "error in ballerina program: arrays index out of range: Index: 24, Size: 0",
                    "Incorrect error message printed.");
            
            // Check the stack trace
            String stackTrace = ErrorHandlerUtils.getServiceStackTrace(
                    ((BallerinaException) getRootCause(ex)).getContext(), ex);
            Assert.assertEquals(stackTrace, expectedStackTrace);
            
            // cleanup
            EnvironmentInitializer.cleanup(undeclaredPackageProgram);
        }
    }

    @Test(description = "Test if a cast exception is thrown in an invalid type cast")
    public void testTypeCastError() {
        Throwable ex = null;
        Context bContext = new Context();

        try {
            BLangFunctions.invoke(bLangProgram, "testTypeCastException", new BString[0], bContext);
        } catch (Exception e) {
            ex = e;
        } finally {
            Assert.assertTrue(ex != null);
            Assert.assertTrue(ex instanceof BallerinaException, "Expected a " + BallerinaException.class.getName() +
                    ", but found: " + ex.getClass() + ".");
            Assert.assertEquals(getRootCause(ex).getMessage(), "input value value cannot be cast to integer");

        }
    }
    
    @Test
    public void testStackTraceOnCrossPkgError() {
        BLangProgram muplitplePackageAccessProg = EnvironmentInitializer.setup("lang/errors/services");
        Throwable ex = null;
        String expectedStackTrace = "\t at lang.errors.runtime:getFruit2(runtime-errors.bal:23)\n" +
                                    "\t at lang.errors.runtime:getFruit1(runtime-errors.bal:19)\n" +
                                    "\t at lang.errors.runtime:testStackTrace(runtime-errors.bal:15)\n" +
                                    "\t at lang.errors.services:echoResource(multiple-package-errors.bal:10)\n" +
                                    "\t at lang.errors.services:echoService(multiple-package-errors.bal:9)\n";
        try {
            CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/test/crossPkg", "GET");
            Services.invoke(cMsg);
        } catch (Exception e) {
            ex = e;
        } finally {
            // Check exception type
            Assert.assertTrue(ex instanceof BallerinaException, "Expected a " + BallerinaException.class.getName() +
                    ", but found: " + ex + ".");
            
            // Check error message
            String errorMsg = getRootCause(ex).getMessage();
            Assert.assertEquals(errorMsg, "error in ballerina program: arrays index out of range: Index: 24, Size: 0",
                    "Incorrect error message printed.");
            
            // Check the stack trace
            String stackTrace = ErrorHandlerUtils.getServiceStackTrace(
                    ((BallerinaException) getRootCause(ex)).getContext(), ex);
            Assert.assertEquals(stackTrace, expectedStackTrace);
            
            // cleanup
            EnvironmentInitializer.cleanup(muplitplePackageAccessProg);
        }
    }
    
    @Test
    public void testStackTraceOnConnectorError() {
        BLangProgram connectorErrorProg = EnvironmentInitializer.setup("lang/errors/connectors");
        Throwable ex = null;
        String expectedStackTrace = "\t at lang.errors.runtime:getFruit2(runtime-errors.bal:23)\n" +
                                    "\t at lang.errors.runtime:getFruit1(runtime-errors.bal:19)\n" +
                                    "\t at lang.errors.runtime:testStackTrace(runtime-errors.bal:15)\n" +
                                    "\t at lang.errors.connectors:action1(connector-runtime-errors.bal:18)\n" +
                                    "\t at lang.errors.connectors:echoResource(connector-runtime-errors.bal:11)\n" +
                                    "\t at lang.errors.connectors:echoService(connector-runtime-errors.bal:9)\n";
        try {
            CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/test/connector", "GET");
            Services.invoke(cMsg);
        } catch (Exception e) {
            ex = e;
        } finally {
            // Check exception type
            Assert.assertTrue(ex instanceof BallerinaException, "Expected a " + BallerinaException.class.getName() +
                    ", but found: " + ex + ".");
            
            // Check error message
            String errorMsg = getRootCause(ex).getMessage();
            Assert.assertEquals(errorMsg, "error in ballerina program: arrays index out of range: Index: 24, Size: 0",
                    "Incorrect error message printed.");
            
            // Check the stack trace
            String stackTrace = ErrorHandlerUtils.getServiceStackTrace(
                    ((BallerinaException) getRootCause(ex)).getContext(), ex);
            Assert.assertEquals(stackTrace, expectedStackTrace);
            
            // cleanup
            EnvironmentInitializer.cleanup(connectorErrorProg);
        }
    }

    private static String getStackOverflowTrace() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 41; i++) {
            if (i == 20 || i == 21) {
                sb.append("\t ...\n");
            } else {
                sb.append("\t at lang.errors.runtime:infiniteRecurse(runtime-errors.bal:35)\n");
            }
        }
        sb.append("\t at lang.errors.runtime:infiniteRecurse(runtime-errors.bal:31)\n");
        return sb.toString();
    }
    
    private Throwable getRootCause(Throwable t) {
        if (t.getCause() != null) {
            return getRootCause(t.getCause());
        }
        return t;
    }
}
