/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.langlib.test.statements.foreach;

import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for foreach with error handling.
 *
 * @since 2.0.0
 */
public class ForeachErrorHandlingTests {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach_error_handling.bal");
    }

    @AfterClass
    public void tearDown() {
        program = null;
    }
    
    @Test
    public void testArrayForeachAndTrap() {
        BRunUtil.invoke(program, "testArrayForeachAndTrap");
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp =
            "error: \\{ballerina/lang.int\\}NumberParsingError \\{\"message\":\"'string' value 'waruna' cannot be " +
                    "converted to 'int'\"\\}\n" +
                    "\tat ballerina.lang.int.0:fromString\\(int.bal:173\\)\n" +
                    "\t   foreach_error_handling:\\$lambda\\$_0\\(foreach_error_handling.bal:41\\)")
    public void testArrayForeachAndPanic() {
        BRunUtil.invoke(program, "testArrayForeachAndPanic");
    }
}
