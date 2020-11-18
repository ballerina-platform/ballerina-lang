/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.typeof;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests to test thypeof operator over literal expressions.
 *
 * @since 0.995
 */
@Test(groups = "no-bvm-support")
public class TypeofOverLiteralExpressionTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/typeof/typeof.bal");
    }

    @Test
    public void testTypeDescOperatorOnLiterals() {
        BValue[] res = BRunUtil.invoke(result, "typeDescOfLiterals");
        Assert.assertEquals(res[0].stringValue(), "int");
        Assert.assertEquals(res[1].stringValue(), "float");
        Assert.assertEquals(res[2].stringValue(), "float");
        Assert.assertEquals(res[3].stringValue(), "string");
        Assert.assertEquals(res[4].stringValue(), "boolean");
        Assert.assertEquals(res[5].stringValue(), "boolean");
        Assert.assertEquals(res[6].stringValue(), "null");
    }

    @Test
    public void testTypeDescOperatorOnExpressionsOfLiterals() {
        BValue[] res = BRunUtil.invoke(result, "typeDescOfExpressionsOfLiterals");
        Assert.assertEquals(res[0].stringValue(), "int");
        Assert.assertEquals(res[1].stringValue(), "float");
    }

    @Test
    public void testUsingTypeofOperatorAtFunctionInvocationSite() {
        BValue[] res = BRunUtil.invoke(result, "passTypeofToAFunction");
        Assert.assertEquals(res[0].stringValue(), "int");
    }

    @Test
    public void getTypeDescOfASimpleRecordType() {
        BValue[] res = BRunUtil.invoke(result, "typeDescOfARecord");
        Assert.assertEquals(res[0].stringValue(), "RecType0");
    }

    @Test
    public void getTypeDescOfASimpleObjectType() {
        BValue[] res = BRunUtil.invoke(result, "typeDescOrAObject");
        Assert.assertEquals(res[0].stringValue(), "Obj0");
    }

    @Test
    public void testUsingTypeofOperatorAsRestArgs() {
        BValue[] res = BRunUtil.invoke(result, "passTypeofAsRestParams");
        Assert.assertEquals(res[0].stringValue(), "int");
        Assert.assertEquals(res[1].stringValue(), "int");
        Assert.assertEquals(res[2].stringValue(), "float");
    }
}
