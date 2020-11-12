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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for foreach with error handling.
 *
 * @since 2.0.0
 */
public class ForeachErrorHandlingTests {

    private CompileResult program, negative;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach_error_handling.bal");
    }

    @Test
    public void testArrayForeachAndTrap() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayForeachAndTrap");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 14);
    }

    @Test(expectedExceptions = BLangRuntimeException.class, expectedExceptionsMessageRegExp =
            "error: \\{ballerina\\}TypeCastError \\{\"message\":\"incompatible types: 'error' cannot be cast to " +
                    "'int'\"\\}\n" +
                    "\tat foreach_error_handling:\\$lambda\\$_0\\(foreach_error_handling.bal:41\\)\n" +
                    "\t   foreach_error_handling:\\$lambda\\$_0\\$lambda0\\$\\(foreach_error_handling.bal:40\\)")
    public void testArrayForeachAndPanic() {
        BValue[] returns = BRunUtil.invoke(program, "testArrayForeachAndPanic");
    }
}
