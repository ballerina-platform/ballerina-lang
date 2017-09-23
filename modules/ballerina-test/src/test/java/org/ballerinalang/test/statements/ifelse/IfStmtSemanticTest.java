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
package org.ballerinalang.test.statements.ifelse;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IfStmtSemanticTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("org/ballerinalang/test/statements/ifelse/invalidIfStmt.bal");
    }

    @Test
    public void ifStmtTest() {
        Assert.assertEquals(result.getErrorCount(), 1);
        BTestUtils.validateError(result, 0, "incompatible types: expected 'boolean', found 'int'", 2, 6);
    }
    
    @Test
    public void invokeFunctionTest() {
        BValue[] values = BTestUtils.invoke(result.getProgFile(), "foo", new BValue[0]);
        Assert.assertEquals(values[0].stringValue(), "returning from if");
    }
}
