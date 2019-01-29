/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.typecast;


import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Local function invocation with implicit cast test.
 *
 * @since 0.8.3
 */
public class FuncInvocationWithImplicitCastTest {

    private CompileResult result;


    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/typecast/func-invocation-with-implicit-cast.bal");
    }

    @Test(description = "Test Function invocation with implicit cast")
    public void testFuncInvocationWithImplicitCast() {
        BValue[] returns = BRunUtil.invoke(result, "testImplicitCastInvocation", new BValue[]{});

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "7.modified";
        Assert.assertEquals(actual, expected);
    }
}
