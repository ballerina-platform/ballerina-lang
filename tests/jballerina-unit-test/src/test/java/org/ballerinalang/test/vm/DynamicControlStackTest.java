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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] vals = BRunUtil.invoke(result, "f1", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(((BInteger) vals[0]).intValue(), 50025000);
    }

    @Test
    public void controlStackTest2() {
        BValue[] vals = BRunUtil.invoke(result, "f5", new BValue[0]);
        Assert.assertEquals(vals.length, 1);
        Assert.assertEquals(((BInteger) vals[0]).intValue(), 50125000);
    }

}
