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
package org.ballerinalang.nativeimpl.functions;

//import org.apache.log4j.Logger;
//import org.ballerinalang.util.program.BLangFunctions;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//import org.ballerinalang.model.BLangProgram;
//import org.ballerinalang.model.GlobalScope;
//import org.ballerinalang.model.NativeUnit;
//import org.ballerinalang.model.SymbolName;
//import org.ballerinalang.model.types.SimpleTypeName;
//import org.ballerinalang.model.values.BString;
//import org.ballerinalang.model.values.BValue;
//import org.ballerinalang.model.values.BValueType;
//import org.ballerinalang.natives.NativeUnitProxy;
//import org.ballerinalang.natives.exceptions.ArgumentOutOfRangeException;
//import org.ballerinalang.runtime.registry.PackageRegistry;
//import EchoStringNativeFunction;
//import IncorrectParamCountNativeFunction;
//import TestConstantsNativeFunction;
//import BTestUtils;

/**
 * Test Custom Native function Invocation.
 */
public class CustomNativeFunctionInvocationTest {

//    private BLangProgram bLangProgram;
//    private GlobalScope globalScope = GlobalScope.getInstance();
//
//    @BeforeClass
//    public void setup() {
//        // Add Native functions.
//        globalScope.define(new SymbolName("echoString.string", "ballerina.test.echo"), new NativeUnitProxy(() -> {
//            NativeUnit nativeCallableUnit = null;
//            try {
//                nativeCallableUnit = new EchoStringNativeFunction();
//                nativeCallableUnit.setName("echoString.string.string");
//                nativeCallableUnit.setPackagePath("ballerina.test.echo");
//                nativeCallableUnit.setArgTypeNames(new SimpleTypeName[] {
//                        new SimpleTypeName("string", false)
//                });
//                nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[] {
//                        new SimpleTypeName("string", false)
//                });
//                nativeCallableUnit.setStackFrameSize(1);
//                nativeCallableUnit.setSymbolName(new SymbolName("echoString.string", "ballerina.test.echo"));
//                return nativeCallableUnit;
//            } catch (Exception ignore) {
//            } finally {
//                return nativeCallableUnit;
//            }
//        }));
//        globalScope.define(new SymbolName("paramCount.string", "ballerina.test.incorrect"),
// new NativeUnitProxy(() -> {
//            NativeUnit nativeCallableUnit = null;
//            try {
//                nativeCallableUnit = new IncorrectParamCountNativeFunction();
//                nativeCallableUnit.setName("paramCount.string.string");
//                nativeCallableUnit.setPackagePath("ballerina.test.incorrect");
//                nativeCallableUnit.setArgTypeNames(new SimpleTypeName[] {
//                        new SimpleTypeName("string", false)
//                });
//                nativeCallableUnit.setReturnParamTypeNames(new SimpleTypeName[] {
//                        new SimpleTypeName("string", false)
//                });
//                nativeCallableUnit.setStackFrameSize(1);
//                nativeCallableUnit.setSymbolName(new SymbolName("paramCount.string", "ballerina.test.incorrect"));
//                return nativeCallableUnit;
//            } catch (Exception ignore) {
//            } finally {
//                return nativeCallableUnit;
//            }
//        }));
//    }
//
//    @Test
//    public void testCustomNativeFunctionInvocation() {
//        final String funcName = "invokeNativeFunction";
//        final String s1 = "Hello World...!!!";
//        BValueType[] args = { new BString(s1) };
//        bLangProgram = BTestUtils.parseBalFile("samples/customNative.bal", globalScope);
//        BValue[] returns = BLangFunctions.invoke(bLangProgram, funcName, args);
//        Assert.assertEquals(returns[0].stringValue(), s1);
//    }
//
//    @Test(expectedExceptions = ArgumentOutOfRangeException.class,
//          expectedExceptionsMessageRegExp = "Parameter index out of range2")
//    public void testInvalidParamCountNativeFunctionInvocation() {
//        final String funcName = "incorrectParamCountFunction";
//        final String s1 = "Hello World...!!!";
//        BValueType[] args = { new BString(s1) };
//        bLangProgram = BTestUtils.parseBalFile("samples/incorrectParamcustomNative.bal", globalScope);
//        BValue[] returns = BLangFunctions.invoke(bLangProgram, funcName, args);
//    }
//
//    //@Test
//    public void testNativeConstants() {
//        Logger rootLogger = Logger.getRootLogger();
//        SystemTest.TestLogAppender testLogAppender = new SystemTest.TestLogAppender();
//        try {
//            rootLogger.addAppender(testLogAppender);
//            PackageRegistry.getInstance().registerNativeFunction(new TestConstantsNativeFunction());
//
//            Assert.assertEquals(testLogAppender.getEvents().size(), 2, "Logging events didn't match.");
//            Assert.assertEquals(testLogAppender.events.get(0).getThrowableInformation().getThrowableStrRep()[0],
//                    "org.ballerinalang.natives.exceptions.MalformedEntryException:"
//                            + " XML not supported yet.");
//            Assert.assertEquals(testLogAppender.events.get(1).getThrowableInformation().getThrowableStrRep()[0],
//                    "org.ballerinalang.natives.exceptions.MalformedEntryException: "
//                            + "Error while processing value hello");
//        } finally {
//            rootLogger.removeAppender(testLogAppender);
//        }
//    }
}
