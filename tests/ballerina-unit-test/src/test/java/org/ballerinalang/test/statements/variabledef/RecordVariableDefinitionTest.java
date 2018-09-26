/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.statements.variabledef;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases Record Variable Definitions.
 *
 * @since 0.982.0
 */
public class RecordVariableDefinitionTest {

    private CompileResult basic;

    @BeforeClass
    public void setup() {
        basic = BCompileUtil.
                compile("test-src/statements/variabledef/record-variable-definition-stmt.bal");
    }

    @Test(description = "Test simple record variable definition")
    public void simpleDefinition() {
        BValue[] returns = BRunUtil.invoke(basic, "simpleDefinition");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "Peter");
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test(description = "Test record variable inside record variable")
    public void recordVarInRecordVar() {
        BValue[] returns = BRunUtil.invoke(basic, "recordVarInRecordVar");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "Peter");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 29);
        Assert.assertEquals(returns[2].stringValue(), "Y");
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
    }

    @Test(description = "Test record variable inside record variable inside record variable")
    public void recordVarInRecordVarInRecordVar() {
        BValue[] returns = BRunUtil.invoke(basic, "recordVarInRecordVarInRecordVar");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(returns[0].stringValue(), "Peter");
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1000);
        Assert.assertEquals(returns[3].stringValue(), "PG");
        Assert.assertEquals(returns[4].stringValue(), "Colombo 10");
    }

//    @Test(description = "Test tuple variable inside record variable")
//    public void tupleVarInRecordVar() {
//        BValue[] returns = BRunUtil.invoke(basic, "tupleVarInRecordVar");
//        Assert.assertEquals(returns.length, 3);
//        Assert.assertEquals(returns[0].stringValue(), "John");
//        Assert.assertEquals(((BInteger) returns[1]).intValue(), 20);
//        Assert.assertEquals(returns[2].stringValue(), "PG");
//    }
}
