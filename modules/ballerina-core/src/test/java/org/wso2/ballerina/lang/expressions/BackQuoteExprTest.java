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

package org.wso2.ballerina.lang.expressions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Primitive add expression test.
 */
public class BackQuoteExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        SymScope symScope = GlobalScopeHolder.getInstance().getScope();
        bFile = ParserUtils.parseBalFile("lang/expressions/back-quote-expr.bal", symScope);
    }

    @Test(description = "Test two int add expression")
    public void testIntAddExpr() {

        BValue[] returns = Functions.invoke(bFile, "getProduct");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BJSON.class);
        String actual = ((BJSON) returns[0]).getMessageAsString();
        String expected = "{\"Product\":{\"ID\":\"123456\",\"Name\":\"XYZ\",\"Description\":\"Sample product.\"}}";
        Assert.assertEquals(actual, expected);
    }
}
