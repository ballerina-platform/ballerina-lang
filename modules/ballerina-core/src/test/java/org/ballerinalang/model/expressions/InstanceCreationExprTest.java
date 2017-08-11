/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.model.expressions;

import org.ballerinalang.model.BLangProgram;

/**
 * Instance creation expression test.
 *
 * @since 0.8.0
 */
public class InstanceCreationExprTest {

    private BLangProgram bLangProgram;

 /*   @BeforeClass
    public void setup() {
        SymScope symScope = new SymScope(null);
        bLangProgram = ParserUtils.parseBalFile("model/expressions/instance-creation-expr.bal", symScope);
    }

    @Test(description = "Test instance creation expression")
    public void testArrayAccessExpr() {
        BValue[] args = { new BString("Test Payload") };
        BValue[] returns = Functions.invoke(bLangProgram, "testMessagePayload", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMessage.class);

        String actual = ((BMessage) returns[0]).getMessageDataSource().getMessageAsString();
        String expected = "Test Payload";
        Assert.assertEquals(actual, expected);
    }*/

}
