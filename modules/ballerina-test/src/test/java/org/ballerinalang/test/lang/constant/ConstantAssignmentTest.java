/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.lang.constant;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Constant assignment test cases.
 */
public class ConstantAssignmentTest {

    private static CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BTestUtils.compile("test-src/lang/constant/constant-assignment.bal");
    }

    @Test(description = "Test accessing constant evaluated by an expression.")
    public void testConstantAssignmentExpression() {
        setEnv("env_var", "test");
        BValue[] returns = BTestUtils.invoke(compileResult, "accessConstant");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "test");
    }

    private void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }

    @Test(description = "Test accessing constant evaluated by a function return value.")
    public void testConstantAssignmentViaFunction() {
        BValue[] returns = BTestUtils.invoke(compileResult, "accessConstantViaFunction");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "dummy");
    }

    @Test(description = "Test accessing constant evaluated by a native function return value.")
    public void testConstantAssignmentViaNativeFunction() {
        BValue[] returns = BTestUtils.invoke(compileResult, "accessConstantViaNativeFunction");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "ballerina is awesome");
    }

    @Test(description = "Test accessing constant evaluated by a integer addition expr.")
    public void testConstantAssignmentViaIntegerAddition() {
        BValue[] returns = BTestUtils.invoke(compileResult, "accessConstantEvalIntegerExpression");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 30);
    }

    @Test(description = "Test accessing constant evaluated by another already defined constant.")
    public void testConstantAssignmentViaConstant() {
        setEnv("env_var", "test");
        BValue[] returns = BTestUtils.invoke(compileResult, "accessConstantEvalWithMultipleConst");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "testdummyballerina is awesome");
    }

    @Test
    public void testConstantAssignmentNegative() {
        CompileResult compileResult = BTestUtils.compile("test-src/lang/constant/constant-assignment-negative.bal");
        // Todo - Fix duplicate error messages issue
        //        Assert.assertEquals(compileResult.getErrorCount(),3);
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "incompatible types: expected 'int', found 'float'");
        Assert.assertEquals(compileResult.getDiagnostics()[1].getMessage(),
                            "incompatible types: expected 'float', found 'string'");
        Assert.assertEquals(compileResult.getDiagnostics()[2].getMessage(),
                            "incompatible types: expected 'int', found 'string'");
    }
}
