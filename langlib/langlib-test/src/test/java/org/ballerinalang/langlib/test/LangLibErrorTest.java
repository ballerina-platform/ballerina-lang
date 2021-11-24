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
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * This class tests error lang module functionality.
 *
 * @since 1.0
 */
@Ignore
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
        BRunUtil.invoke(errorCtor, "testErrorCause");
    }

    @Test
    public void testErrorDestructureWithCause() {
        CompileResult errorCtor = BCompileUtil.compile("test-src/errorlib_error_ctor_test.bal");
        BRunUtil.invoke(errorCtor, "testErrorDestructureWithCause");
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
        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);
        String[] callStacks = ((BValueArray) returns[0]).getStringArray();
        assertEquals(callStacks[0], "callableName: getError  fileName: errorlib_test.bal lineNumber: 45");
        assertEquals(callStacks[1], "callableName: stack2  fileName: errorlib_test.bal lineNumber: 88");
        assertEquals(callStacks[2], "callableName: stack1  fileName: errorlib_test.bal lineNumber: 84");
        assertEquals(callStacks[3], "callableName: stack0  fileName: errorlib_test.bal lineNumber: 80");
        assertEquals(callStacks[4], "callableName: getErrorStackTrace  fileName: errorlib_test.bal lineNumber: 92");
    }

    @Test
    public void testErrorStackTrace() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testErrorStackTrace");
        assertEquals(returns[0].stringValue(), "5");
        assertEquals(returns[1].stringValue(),
                "[\"callableName: getError  fileName: errorlib_test.bal lineNumber: 45\"," +
                        "\"callableName: stack2  fileName: errorlib_test.bal lineNumber: 88\"," +
                        "\"callableName: stack1  fileName: errorlib_test.bal lineNumber: 84\"," +
                        "\"callableName: stack0  fileName: errorlib_test.bal lineNumber: 80\"," +
                        "\"callableName: testErrorStackTrace  fileName: errorlib_test.bal lineNumber: 97\"]");
    }

    @Test
    public void testErrorCallStack() {
        BRunUtil.invoke(compileResult, "testErrorCallStack");
    }

    @Test
    public void testRetriableTest() {
        BRunUtil.invoke(compileResult, "testRetriableTest");
    }

    @Test
    public void testStacktraceStrRepresentation() {
        BRunUtil.invoke(compileResult, "testStacktraceStrRepresentation");
    }
}
