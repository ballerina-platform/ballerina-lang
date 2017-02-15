/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerina.nativeimpl.functions;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.GlobalScope;
import org.wso2.ballerina.core.model.NativeUnit;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.types.SimpleTypeName;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueType;
import org.wso2.ballerina.core.nativeimpl.NativeUnitProxy;
import org.wso2.ballerina.core.runtime.registry.PackageRegistry;
import org.wso2.ballerina.nativeimpl.functions.impl.EchoStringNativeFunction;
import org.wso2.ballerina.nativeimpl.functions.impl.IncorrectParamCountNativeFunction;
import org.wso2.ballerina.nativeimpl.functions.impl.TestConstantsNativeFunction;
import org.wso2.ballerina.nativeimpl.util.Functions;
import org.wso2.ballerina.nativeimpl.util.ParserUtils;

/**
 * Test Custom Native function Invocation.
 */
public class CustomNativeFunctionInvocationTest {

    private BallerinaFile bFile;
    private GlobalScope globalScope = GlobalScope.getInstance();

    @BeforeClass
    public void setup() {
        // Add Native functions.
        globalScope.define(new SymbolName("echoString.string", "ballerina.test.echo"), new NativeUnitProxy(() -> {
            NativeUnit nativeCallableUnit = null;
            try {
                nativeCallableUnit = new EchoStringNativeFunction();
                nativeCallableUnit.setName("echoString.string.string");
                nativeCallableUnit.setPackagePath("ballerina.test.echo");
                nativeCallableUnit.setArgTypeNames(new SimpleTypeName[] {
                        new SimpleTypeName("string", false)
                });
                nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[] {
                        new SimpleTypeName("string", false)
                });
                nativeCallableUnit.setStackFrameSize(1);
                nativeCallableUnit.setSymbolName(new SymbolName("echoString.string", "ballerina.test.echo"));
                return nativeCallableUnit;
            } catch (Exception ignore) {
            } finally {
                return nativeCallableUnit;
            }
        }));
        globalScope.define(new SymbolName("paramCount.string", "ballerina.test.incorrect"), new NativeUnitProxy(() -> {
            NativeUnit nativeCallableUnit = null;
            try {
                nativeCallableUnit = new IncorrectParamCountNativeFunction();
                nativeCallableUnit.setName("paramCount.string.string");
                nativeCallableUnit.setPackagePath("ballerina.test.incorrect");
                nativeCallableUnit.setArgTypeNames(new SimpleTypeName[] {
                        new SimpleTypeName("string", false)
                });
                nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[] {
                        new SimpleTypeName("string", false)
                });
                nativeCallableUnit.setStackFrameSize(1);
                nativeCallableUnit.setSymbolName(new SymbolName("paramCount.string", "ballerina.test.incorrect"));
                return nativeCallableUnit;
            } catch (Exception ignore) {
            } finally {
                return nativeCallableUnit;
            }
        }));
    }

    @Test
    public void testCustomNativeFunctionInvocation() {
        final String funcName = "invokeNativeFunction";
        final String s1 = "Hello World...!!!";
        BValueType[] args = { new BString(s1) };
        bFile = ParserUtils.parseBalFile("samples/customNative.bal", globalScope);
        BValue[] returns = Functions.invoke(bFile, funcName, args);
        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test(expectedExceptions = BallerinaException.class,
          expectedExceptionsMessageRegExp = "Parameter index out of range2")
    public void testInvalidParamCountNativeFunctionInvocation() {
        final String funcName = "incorrectParamCountFunction";
        final String s1 = "Hello World...!!!";
        BValueType[] args = { new BString(s1) };
        bFile = ParserUtils.parseBalFile("samples/incorrectParamcustomNative.bal", globalScope);
        BValue[] returns = Functions.invoke(bFile, funcName, args);
    }

    //@Test
    public void testNativeConstants() {
        Logger rootLogger = Logger.getRootLogger();
        SystemTest.TestLogAppender testLogAppender = new SystemTest.TestLogAppender();
        try {
            rootLogger.addAppender(testLogAppender);
            PackageRegistry.getInstance().registerNativeFunction(new TestConstantsNativeFunction());

            Assert.assertEquals(testLogAppender.getEvents().size(), 2, "Logging events didn't match.");
            Assert.assertEquals(testLogAppender.events.get(0).getThrowableInformation().getThrowableStrRep()[0],
                    "org.wso2.ballerina.core.nativeimpl.exceptions.MalformedEntryException:"
                            + " XML not supported yet.");
            Assert.assertEquals(testLogAppender.events.get(1).getThrowableInformation().getThrowableStrRep()[0],
                    "org.wso2.ballerina.core.nativeimpl.exceptions.MalformedEntryException: "
                            + "Error while processing value hello");
        } finally {
            rootLogger.removeAppender(testLogAppender);
        }
    }
}
