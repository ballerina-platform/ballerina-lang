/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.string;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test BString support in Object.
 */
public class BStringTableValueTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        System.setProperty(BRunUtil.IS_STRING_VALUE_PROP, "true");
        result = BCompileUtil.compile("test-src/types/string/bstring-table-test.bal");
    }

    @Test
    public void testTableGeneration() {
        testAndAssert("testTableGeneration", 84);
    }

    @Test
    public void testTableWithArrayGeneration() {
        testAndAssert("testTableWithArrayGeneration", 37);
    }

    private void testAndAssert(String funcName, int i) {
        BValue[] returns = BRunUtil.invoke(result, funcName);
        Assert.assertEquals(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), i);
    }

    @AfterClass
    public void down() {
        System.clearProperty(BRunUtil.IS_STRING_VALUE_PROP);
    }

}
