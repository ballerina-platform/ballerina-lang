/*
 *   Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

// TODO: move these under the current foreach tests once we have implemented the feature
package org.ballerinalang.test.jvm;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ForeachDesugarTests {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/jvm/foreach-desugar.bal");
    }

    @Test
    public void test() {
        BRunUtil.invoke(result, "test");
    }

    @Test
    public void test1() {
        BRunUtil.invoke(result, "test2");
    }

    @Test
    public void test2() {
        BRunUtil.invoke(result, "test3");
    }

    @Test
    public void testWithControlFlow() {
        BRunUtil.invoke(result, "testWithControlFlow");
    }

    @Test
    public void testIterOnArray() {
        BRunUtil.invoke(result, "testIterOnArray");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
