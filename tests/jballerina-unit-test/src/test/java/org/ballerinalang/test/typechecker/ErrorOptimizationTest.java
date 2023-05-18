/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.typechecker;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * Tests the optimizations done for error related type checks, to not pass through the TypeChecker.
 */
public class ErrorOptimizationTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(
                Paths.get("test-src", "typechecker", "error_optimizations.bal").toString());
    }

    @Test(dataProvider = "FunctionList")
    public void testErrorOptimizations(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @DataProvider(name = "FunctionList")
    public Object[] getTestFunctions() {
        return new Object[]{
                "testWithValue",
                "testWithError",
                "testWithOnlyError",
                "testWithMultipleErrors",
                "testWithMultipleErrorsAndError",
                "testWithRecordAndError",
                "testMultipleErrorUnionWithError"
        };
    }
}
