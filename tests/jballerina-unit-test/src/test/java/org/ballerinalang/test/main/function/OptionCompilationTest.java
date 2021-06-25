/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests operand parameters of the main function.
 *
 * @since 2.0.0
 */

public class OptionCompilationTest {

    private static final String MAIN_FUNCTION_TEST_SRC_DIR = "test-src/main.function/options/";

    @Test
    public void testSuccessful() {
        CompileResult result = BCompileUtil.compile(MAIN_FUNCTION_TEST_SRC_DIR + "successful.bal");
        assertEquals(result.getErrorCount(), 0);
    }

    @Test
    public void invalidSignatureTest() {
        CompileResult negativeResult = BCompileUtil.compile(
                MAIN_FUNCTION_TEST_SRC_DIR + "invalid.bal");
        assertEquals(negativeResult.getErrorCount(), 6);
        validateError(negativeResult, 0, "main function option parameter 't' via included record " +
                "'option' has invalid type 'typedesc'", 18, 5);
        validateError(negativeResult, 1, "main function option parameter 'm' via included record " +
                "'option' has invalid type '(int|string)'", 19, 5);
        validateError(negativeResult, 2, "main function option parameter 'q' via included record " +
                "'option' has invalid type 'map<int>'", 20, 5);
        validateError(negativeResult, 3, "main function option parameter 'i' via included record " +
                "'option' has invalid type '(int|typedesc)'", 21, 5);
        validateError(negativeResult, 4, "main function option parameter 'r' via included record " +
                "'option' has invalid type '(float|string)'", 22, 5);
        validateError(negativeResult, 5, "main function option parameter 'st' via included record " +
                "'option' has invalid type 'Student'", 23, 5);
    }
}
