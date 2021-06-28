/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.test.jvm;

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases to cover modules related tests on JBallerina.
 *
 * @since 0.995.0
 */
public class ModuleTest {

    private CompileResult compileResult;
    private CompileResult compileResultNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/TestProject");
        compileResultNegative = BCompileUtil.compile("test-src/jvm/TestProjectNegative");
    }

    @Test(description = "Test module")
    public void testModule() {
        BValue[] result = BRunUtil.invoke(compileResult, "testModule", new BValue[] { new BInteger(2) });
        Assert.assertTrue(result[0] instanceof BInteger);
        BInteger calculatedValue = (BInteger) result[0];
        Assert.assertEquals(calculatedValue.intValue(), 12);
    }

    @Test(description = "Test module negative")
    public void testModuleNegative() {
        int i = 0;
        validateError(compileResultNegative, i++, "invalid expression statement", 20, 5);
        validateError(compileResultNegative, i++, "undefined symbol 'shapes'", 20, 5);
        validateError(compileResultNegative, i++, "missing key expr in member access expr", 20, 23);
        validateError(compileResultNegative, i++, "missing semicolon token", 20, 25);
        validateError(compileResultNegative, i++, "undefined symbol 'unitSquare1'", 20, 25);
        validateError(compileResultNegative, i++, "undefined symbol 'shapes'", 21, 39);
        validateError(compileResultNegative, i++, "invalid expression statement", 22, 5);
        validateError(compileResultNegative, i++, "undefined symbol 'shapes'", 22, 5);
        validateError(compileResultNegative, i++, "missing key expr in member access expr", 22, 23);
        validateError(compileResultNegative, i++, "missing semicolon token", 22, 25);
        validateError(compileResultNegative, i++, "undefined symbol 'unitSquare3'", 22, 25);
        validateError(compileResultNegative, i++, "undefined symbol 'shapes'", 22, 39);
        Assert.assertEquals(compileResultNegative.getErrorCount(), i);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        compileResultNegative = null;
    }
}
