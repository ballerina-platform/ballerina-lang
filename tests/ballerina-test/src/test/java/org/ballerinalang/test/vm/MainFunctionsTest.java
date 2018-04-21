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
package org.ballerinalang.test.vm;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests related to BVM dynamic control stack growth. 
 */
public class MainFunctionsTest {

    private CompileResult result, result2;
    
    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/vm/main-functions.bal");
        this.result2 = BCompileUtil.compile("test-src/vm/main-without-arguments.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
        Assert.assertEquals(result2.getErrorCount(), 0);
    }
    
    @Test
    public void basicMainInvocationTest() {
        Assert.assertTrue(result.getProgFile().isMainEPAvailable());
        BStringArray args = new BStringArray();
        args.add(0, "V1");
        args.add(1, "V2");
        BRunUtil.invoke(result, "main", new BValue[] { args });
    }
    
    @Test
    public void mainWithoutArgumentsTest() {
        Assert.assertTrue(result.getProgFile().isMainEPAvailable());
        BRunUtil.invoke(result2, "main", new BValue[0]);
    }
    
    @Test
    public void publicMainFunctionInvocationTest() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/vm/main-functions-negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
        Assert.assertTrue(negativeResult.getDiagnostics()[0].getMessage().contains(
                "the main function cannot be explicitly marked as public"));
    }
    
    @Test
    public void invalidSigMainFunctionInvocationTest() {
        CompileResult result = BCompileUtil.compile("test-src/vm/main-functions-negative2.bal");
        Assert.assertFalse(result.getProgFile().isMainEPAvailable());
    }
    
}
