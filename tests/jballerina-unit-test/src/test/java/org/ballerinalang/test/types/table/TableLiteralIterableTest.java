/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.table;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test table literal iterable operation syntax.
 */
public class TableLiteralIterableTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/table/table_literal_syntax.bal");
    }

    @Test()
    public void testSelect() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testSelect");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"id\":1, \"salary\":100.0}, {\"id\":2, \"salary\":200.0}, {\"id\":3, " +
                        "\"salary\":300.0}]");
    }

    @Test()
    public void testSelectCompatibleLambdaInput() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testSelectCompatibleLambdaInput");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"id\":1, \"salary\":100.0}, {\"id\":2, \"salary\":200.0}, {\"id\":3, " +
                        "\"salary\":300.0}]");
    }

    @Test()
    public void testSelectCompatibleLambdaOutput() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testSelectCompatibleLambdaOutput");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"id\":1, \"salary\":100.0}, {\"id\":2, \"salary\":200.0}, {\"id\":3, " +
                        "\"salary\":300.0}]");
    }

    @Test()
    public void testSelectCompatibleLambdaInputOutput() {
        BValue[] returns = BRunUtil.invokeFunction(result, "testSelectCompatibleLambdaInputOutput");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"id\":1, \"salary\":100.0}, {\"id\":2, \"salary\":200.0}, {\"id\":3, " +
                        "\"salary\":300.0}]");
    }
}
