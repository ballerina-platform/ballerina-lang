/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.bala.types;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;


/**
 * Test never type with bala.
 */
public class NeverTypeBalaTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        CompileResult compileResult = BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        if (compileResult.getErrorCount() > 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains errors");
        }
        result = BCompileUtil.compile("test-src/bala/test_bala/types/never_type_test.bal");
    }

    @Test
    public void testTypeOfNeverReturnTypedFunction() {
        BRunUtil.invoke(result, "testTypeOfNeverReturnTypedFunction");
    }

    @Test
    public void testNeverReturnTypedFunctionCall() {
        BRunUtil.invoke(result, "testNeverReturnTypedFunctionCall");
    }

    @Test
    public void testInclusiveRecordTypeWithNeverTypedField() {
        BRunUtil.invoke(result, "testInclusiveRecord");
    }

    @Test
    public void testExclusiveRecordTypeWithNeverTypedField() {
        BRunUtil.invoke(result, "testExclusiveRecord");
    }

    @Test
    public void testNeverWithKeyLessTable() {
        BRunUtil.invoke(result, "testNeverWithKeyLessTable");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
