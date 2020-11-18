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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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

    @Test
    public void testFilterFunc() {
        BValue[] values = BRunUtil.invoke(result, "testFilterFunc", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testMapFunc() {
        BValue[] values = BRunUtil.invoke(result, "testMapFunc", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testFilterAndMapFunc() {
        BValue[] values = BRunUtil.invoke(result, "testFilterAndMapFunc", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testReduceFunc() {
        BValue[] values = BRunUtil.invoke(result, "testReduce", new BValue[]{});
        Assert.assertEquals(((BFloat) values[0]).floatValue(), 135.0);
    }

    @Test
    public void testForReachFunc() {
        BValue[] values = BRunUtil.invoke(result, "testForEach", new BValue[]{});
        Assert.assertEquals(((BFloat) values[0]).floatValue(), 135.0);
    }

    @Test
    public void testIteratorFunc() {
        BValue[] values = BRunUtil.invoke(result, "testIterator", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }

    @Test
    public void testMapFuncWithRecordType() {
        BValue[] values = BRunUtil.invoke(result, "testMapFuncWithRecordType", new BValue[]{});
        Assert.assertTrue(((BBoolean) values[0]).booleanValue());
    }
}
