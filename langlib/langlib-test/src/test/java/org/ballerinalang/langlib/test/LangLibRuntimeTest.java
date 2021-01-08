/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.langlib.test;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Test cases for runtime.
 */
public class LangLibRuntimeTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/runtimelib_test.bal");
    }

    @Test
    public void testGetStackTrace() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getCallStackTest");
        assertEquals(returns[0].stringValue(), "{callableName:\"externGetStackTrace\"," +
                " moduleName:\"ballerina.lang$0046runtime.0_0_1.runtime\", fileName:\"runtime.bal\", lineNumber:120}");
        assertEquals(returns[1].stringValue(), "{callableName:\"getStackTrace\", " +
                "moduleName:\"ballerina.lang$0046runtime.0_0_1.runtime\", fileName:\"runtime.bal\", lineNumber:68}");
        assertEquals(returns[2].stringValue(), "{callableName:\"getCallStackTest\", " +
                "moduleName:\"runtimelib_test\", fileName:\"runtimelib_test.bal\", lineNumber:20}");
    }

    @Test
    public void testGetStackTraceToString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getCallStackTest");
        assertEquals(returns[0].toString(), "{callableName:\"externGetStackTrace\", " +
                "moduleName:\"ballerina.lang$0046runtime.0_0_1.runtime\", fileName:\"runtime.bal\", lineNumber:120}");
        assertEquals(returns[1].toString(), "{callableName:\"getStackTrace\", " +
                "moduleName:\"ballerina.lang$0046runtime.0_0_1.runtime\", fileName:\"runtime.bal\", lineNumber:68}");
        assertEquals(returns[2].toString(), "{callableName:\"getCallStackTest\", " +
                "moduleName:\"runtimelib_test\", fileName:\"runtimelib_test.bal\", lineNumber:20}");
    }
}
