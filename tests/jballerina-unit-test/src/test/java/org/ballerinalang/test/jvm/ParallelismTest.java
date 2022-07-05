/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.ballerinalang.test.jvm;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for multi threaded scheduler for jballerina.
 */
public class ParallelismTest {
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/parallelism.bal");
    }

    @Test
    public void testOrdering() {
        Object result = BRunUtil.invoke(compileResult, "orderingTest");
        Assert.assertEquals(result.toString(), "[\"child: end\",\"parent: end\"]");
    }

    @Test
    public void testMergeSort() {
        long[] sample = new long[10];
        for (int i = 0; i < sample.length; i++) {
            sample[i] = sample.length - i;
        }

        Object[] args = new Object[1];
        BArray arr = ValueCreator.createArrayValue(sample);
        args[0] = arr;
        Object result = BRunUtil.invoke(compileResult, "testMergeSort", args);
        Assert.assertTrue(result instanceof  BArray);
        Assert.assertEquals(result.toString(), "[1,2,3,4,5,6,7,8,9,10]");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
