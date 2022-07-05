/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests related to BVM dynamic control stack growth.
 */
public class DynamicControlStackTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/vm/control-stack-test.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test
    public void controlStackTest1() {
        Object vals = BRunUtil.invoke(result, "f1", new Object[0]);
        Assert.assertEquals(vals, 50025000L);
    }

    @Test
    public void controlStackTest2() {
        Object vals = BRunUtil.invoke(result, "f5", new Object[0]);
        Assert.assertEquals(vals, 50125000L);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
