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
package org.ballerinalang.langlib.test;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains unit tests to cover streamlib functionalities.
 *
 * @since 1.2
 */
public class LangLibStreamTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/streamlib_test.bal");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }

    @Test
    public void testFilterFunc() {
        Object values = BRunUtil.invoke(result, "testFilterFunc", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test
    public void testMapFunc() {
        Object values = BRunUtil.invoke(result, "testMapFunc", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test
    public void testFilterAndMapFunc() {
        Object values = BRunUtil.invoke(result, "testFilterAndMapFunc", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test
    public void testReduceFunc() {
        Object values = BRunUtil.invoke(result, "testReduce", new Object[]{});
        Assert.assertEquals(values, 135.0d);
    }

    @Test
    public void testForReachFunc() {
        Object values = BRunUtil.invoke(result, "testForEach", new Object[]{});
        Assert.assertEquals(values, 135.0d);
    }

    @Test
    public void testIteratorFunc() {
        Object values = BRunUtil.invoke(result, "testIterator", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test
    public void testMapFuncWithRecordType() {
        Object values = BRunUtil.invoke(result, "testMapFuncWithRecordType", new Object[]{});
        Assert.assertTrue((Boolean) values);
    }

    @Test
    public void testBasicTypeStream() {
        BRunUtil.invoke(result, "testBasicTypeStream");
    }
}
