/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.main.function;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.codegen.ProgramFile;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.security.Permission;

import static org.ballerinalang.launcher.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests related to the main function.
 *
 * @since 0.990.4
 */
public class MainFunctionsTest {

    private static final String MAIN_FUNCTION_TEST_SRC_DIR = "src/test/resources/test-src/main.function";

    private ProgramFile programFile;
    private ByteArrayOutputStream tempOutStream = new ByteArrayOutputStream();
    private PrintStream defaultOut;
    private SecurityManager defaultSecurityManager;


    @BeforeClass
    public void setup() {
        defaultOut = System.out;
        defaultSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new NoSecuritySecurityManager());
    }

    @Test
    public void basicMainInvocationTest() {
        CompileResult result = BCompileUtil.compile("test-src/main.function/test_basic_main_function.bal");
        assertEquals(result.getErrorCount(), 0);
        BValueArray args = new BValueArray(BTypes.typeString);
        args.add(0, "V1");
        args.add(1, "V2");
        BRunUtil.invoke(result, "main", new BValue[] { args });
    }

    @Test
    public void testNilReturningMain() throws IOException {
        programFile = LauncherUtils.compile(Paths.get(MAIN_FUNCTION_TEST_SRC_DIR),
                                            Paths.get ("test_main_with_nil_return.bal"), false, true);
        resetTempOut();
        BValue[] result = BLangProgramRunner.runMainFunc(programFile, new String[]{});
        assertEquals(tempOutStream.toString(), "nil returning main invoked",
                            "expected the main function to be invoked");
        assertEquals(result.length, 1, "expected the main function to return a single value");
        assertTrue(result[0] == null, "expected nil to be returned");
    }

    @Test
    public void testErrorReturningMain() throws IOException {
        programFile = LauncherUtils.compile(Paths.get(MAIN_FUNCTION_TEST_SRC_DIR),
                                            Paths.get ("test_main_with_error_return.bal"), false, true);
        resetTempOut();
        BValue[] result = BLangProgramRunner.runMainFunc(programFile, new String[]{});
        assertEquals(tempOutStream.toString(), "error returning main invoked",
                            "expected the main function to be invoked");
        assertEquals(result.length, 1, "expected the main function to return a single value");
        assertTrue(result[0] instanceof BError, "expected error to be returned");
        assertEquals(((BError) result[0]).reason, "error return", "invalid error reason");
    }

    @Test
    public void testErrorOrNilReturningMainReturningError() throws IOException {
        programFile = LauncherUtils.compile(Paths.get(MAIN_FUNCTION_TEST_SRC_DIR),
                                            Paths.get ("test_main_with_error_or_nil_return.bal"), false, true);
        resetTempOut();
        BValue[] result = BLangProgramRunner.runMainFunc(programFile, new String[]{"error", "1"});
        assertEquals(tempOutStream.toString(), "error? returning main invoked",
                            "expected the main function to be invoked");
        assertTrue(result[0] instanceof BError, "expected error to be returned");
        assertEquals(((BError) result[0]).reason, "generic error", "invalid error reason");
    }

    @Test
    public void testErrorOrNilReturningMainReturningNil() throws IOException {
        programFile = LauncherUtils.compile(Paths.get(MAIN_FUNCTION_TEST_SRC_DIR),
                                            Paths.get ("test_main_with_error_or_nil_return.bal"), false, true);
        resetTempOut();
        BValue[] result = BLangProgramRunner.runMainFunc(programFile, new String[]{"nil", "0"});
        assertEquals(tempOutStream.toString(), "error? returning main invoked",
                            "expected the main function to be invoked");
        assertTrue(result[0] == null, "expected nil to be returned");
    }

    @Test
    public void testErrorOrNilReturningMainReturningCustomError() throws IOException {
        programFile = LauncherUtils.compile(Paths.get(MAIN_FUNCTION_TEST_SRC_DIR),
                                            Paths.get ("test_main_with_error_or_nil_return.bal"), false, true);
        resetTempOut();
        BValue[] result = BLangProgramRunner.runMainFunc(programFile, new String[]{"user_def_error", "1"});
        assertEquals(tempOutStream.toString(), "error? returning main invoked",
                            "expected the main function to be invoked");
        assertTrue(result[0] instanceof BError, "expected error to be returned");
        assertEquals(((BError) result[0]).reason, "const error reason", "invalid error reason");
        assertEquals(((BString) ((BMap) ((BError) result[0]).details).get("message")).stringValue(),
                            "error message", "invalid error message");
    }

    @Test
    public void invalidMainFunctionSignatureTest() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/main.function/test_main_function_negative.bal");
        assertEquals(negativeResult.getErrorCount(), 5);
        validateError(negativeResult, 0, "the main function should be public", 17, 1);
        validateError(negativeResult, 1, "invalid type 'typedesc' as main function parameter, expected anydata",
                      17, 15);
        validateError(negativeResult, 2, "invalid type 'int|typedesc' as main function parameter, expected anydata",
                      17, 27);
        validateError(negativeResult, 3, "invalid type 'FooObject[]' as main function parameter, expected anydata",
                      17, 47);
        validateError(negativeResult, 4, "invalid main function return type 'string', expected a subtype of 'error?'",
                      17, 71);
    }

    @Test(dataProvider = "mainFunctionStatusCodes")
    public void testStatusCode(String specifiedCodeAsString, int expectedCode) throws IOException {
        try {
            programFile = LauncherUtils.compile(Paths.get(MAIN_FUNCTION_TEST_SRC_DIR),
                                                Paths.get ("test_main_with_error_or_nil_return.bal"), false, true);
            LauncherUtils.runMain(programFile, new String[]{"error", specifiedCodeAsString});
            fail("expected System.exit() to be called");
        } catch (NoSecuritySecurityManager.NoSecuritySecurityException e) {
            assertEquals(e.getStatus(), expectedCode);
        }
    }

    @AfterClass
    public void tearDown() throws IOException {
        tempOutStream.close();
        System.setOut(defaultOut);
        System.setSecurityManager(defaultSecurityManager);
    }

    private void resetTempOut() throws IOException {
        tempOutStream.close();
        tempOutStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(tempOutStream));
    }

    private class NoSecuritySecurityManager extends SecurityManager {
        private final class NoSecuritySecurityException extends SecurityException {
            private final int status;

            private NoSecuritySecurityException(final int status) {
                this.status = status;
            }

            public int getStatus() {
                return this.status;
            }
        }

        @Override
        public void checkPermission(Permission perm) {
        }

        @Override
        public void checkExit(final int status) {
            throw new NoSecuritySecurityException(status);
        }
    }

    @DataProvider(name = "mainFunctionStatusCodes")
    public Object[][] mainFunctionStatusCodes() {
        return new Object[][]{
                {"0", 1},
                {"1", 1},
                {"43", 43},
                {"255", 255},
                {"256", 1},
                {"-1", 1}
        };
    }
}
