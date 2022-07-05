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

package org.ballerinalang.test.types.constant;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Constant assignment test cases.
 */
public class ConstantAssignmentTest {

    private CompileResult positiveCompileResult;
    private CompileResult negativeCompileResult;

    @BeforeClass
    public void setup() {
        setEnv("env_var", "test");
        positiveCompileResult = BCompileUtil.compile("test-src/types/constant/constant-assignment.bal");
        negativeCompileResult = BCompileUtil.compile("test-src/types/constant/constant-assignment-negative.bal");
    }

    @Test(description = "Test accessing constant evaluated by an expression.")
    public void testConstantAssignmentExpression() {
        Object returns = BRunUtil.invoke(positiveCompileResult, "accessConstant");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "test");
    }

    private void setEnv(String key, String value) {
        try {
            if (System.getProperty("os.name").startsWith("Windows")) {
                Class<?> sc = Class.forName("java.lang.ProcessEnvironment");
                Field caseInsensitiveField = sc.getDeclaredField("theCaseInsensitiveEnvironment");
                caseInsensitiveField.setAccessible(true);
                Map<String, String> writableEnv = (Map<String, String>) caseInsensitiveField.get(null);
                writableEnv.put(key, value);
            } else {
                Map<String, String> env = System.getenv();
                Class<?> cl = env.getClass();
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Map<String, String> writableEnv = (Map<String, String>) field.get(env);
                writableEnv.put(key, value);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }

    @Test(description = "Test accessing constant evaluated by a function return value.")
    public void testConstantAssignmentViaFunction() {
        Object returns = BRunUtil.invoke(positiveCompileResult, "accessConstantViaFunction");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "dummy");
    }

    @Test(description = "Test accessing constant evaluated by a native function return value.")
    public void testConstantAssignmentViaNativeFunction() {
        Object returns = BRunUtil.invoke(positiveCompileResult, "accessConstantViaNativeFunction");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "ballerina is awesome");
    }

    @Test(description = "Test accessing constant evaluated by a integer addition expr.")
    public void testConstantAssignmentViaIntegerAddition() {
        Object returns = BRunUtil.invoke(positiveCompileResult, "accessConstantEvalIntegerExpression");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 30L);
    }

    @Test(description = "Test accessing constant evaluated by another already defined constant.")
    public void testConstantAssignmentViaConstant() {
        Object returns = BRunUtil.invoke(positiveCompileResult, "accessConstantEvalWithMultipleConst");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "dummyballerina is awesome");
    }

    @Test
    public void testConstantAssignmentNegative() {
        BAssertUtil.validateError(negativeCompileResult, 0, "incompatible types: expected 'int', found 'float'", 1, 16);
        BAssertUtil.validateError(negativeCompileResult, 1, "incompatible types: expected 'float', found 'string'", 3,
                31);
        BAssertUtil.validateError(negativeCompileResult, 2, "incompatible types: expected 'int', found 'string'", 5,
                27);
    }

    @AfterClass
    public void tearDown() {
        positiveCompileResult = null;
        negativeCompileResult = null;
    }
}
