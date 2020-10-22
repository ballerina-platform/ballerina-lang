/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BRefType;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * This class tests error lang module functionality.
 *
 * @since 1.0
 */
public class LangLibErrorTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/errorlib_test.bal");
    }

    @Test
    public void testTypeTestingErrorUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTypeTestingErrorUnion");
        assertEquals(returns[0].stringValue(), "GenericError");
        assertEquals(returns[1].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns[1].stringValue(), "{message:\"Test union of errors with type test\"}");
    }

    @Test
    public void testErrorCause() {
        CompileResult errorCtor = BCompileUtil.compile("test-src/errorlib_error_ctor_test.bal");
        BValue[] returns = BRunUtil.invoke(errorCtor, "testErrorCause");
        assertNull(returns[0]);
        assertEquals(returns[1].stringValue(), "This is the cause {}");
        assertEquals(returns[2].stringValue(), "This is the cause {}");
    }

    @Test(enabled = false)
    public void testErrorDestructureWithCause() {
        CompileResult errorCtor = BCompileUtil.compile("test-src/errorlib_error_ctor_test.bal");
        BValue[] returns = BRunUtil.invoke(errorCtor, "testErrorDestructureWithCause");
        assertEquals(returns[0].stringValue(), "This is the cause {}");
    }

    @Test
    public void testPassingErrorUnionToFunction() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPassingErrorUnionToFunction");
        assertEquals(returns[0].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns[0].stringValue(), "{message:\"Test passing error union to a function\"}");
    }

    @Test
    public void testGetErrorStackTrace() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getErrorStackTrace");
        assertEquals(returns[0].getType().getTag(), TypeTags.OBJECT_TYPE_TAG);
        BRefType<?>[] callStacks = ((BValueArray) ((BMap) returns[0]).get("callStack")).getValues();
        assertEquals(callStacks[0].stringValue(), "{callableName:\"getError\", moduleName:\"errorlib_test\", " +
                "fileName:\"errorlib_test.bal\", lineNumber:43}");
        assertEquals(callStacks[1].stringValue(), "{callableName:\"stack2\", moduleName:\"errorlib_test\", " +
                "fileName:\"errorlib_test.bal\", lineNumber:86}");
        assertEquals(callStacks[2].stringValue(), "{callableName:\"stack1\", moduleName:\"errorlib_test\", " +
                "fileName:\"errorlib_test.bal\", lineNumber:82}");
        assertEquals(callStacks[3].stringValue(), "{callableName:\"stack0\", moduleName:\"errorlib_test\", " +
                "fileName:\"errorlib_test.bal\", lineNumber:78}");
        assertEquals(callStacks[4].stringValue(), "{callableName:\"getErrorStackTrace\", " +
                "moduleName:\"errorlib_test\", fileName:\"errorlib_test.bal\", lineNumber:90}");
    }

    @Test
    public void testErrorStackTrace() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testErrorStackTrace");
        assertEquals(returns[0].stringValue(), "5");
        assertEquals(returns[1].stringValue(),
                "[\"getError:errorlib_test.bal\",\"stack2:errorlib_test.bal\",\"stack1:errorlib_test.bal\"," +
                        "\"stack0:errorlib_test.bal\",\"testErrorStackTrace:errorlib_test.bal\"]");
    }
}
