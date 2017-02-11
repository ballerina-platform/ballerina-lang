/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.lang.expressions;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Test map initializer expression.
 *
 * @since 0.8.0
 */
public class MapInitializerExprTest {

    private BallerinaFile bFile;

    @BeforeTest
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/expressions/map-initializer-expr.bal");
    }

    @Test(description = "Test map initializer expression")
    public void testMapInitExpr() {
        BValue[] args = {};
        BValue[] returns = Functions.invoke(bFile, "mapInitTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);

        BMap<BString, BString> mapValue = (BMap) returns[0];
        Assert.assertEquals(mapValue.size(), 4);

        Assert.assertEquals(mapValue.get(new BString("animal1")).stringValue(), "Lion");
        Assert.assertEquals(mapValue.get(new BString("animal2")).stringValue(), "Cat");
        Assert.assertEquals(mapValue.get(new BString("animal4")).stringValue(), "Dog");
    }

    @Test(description = "Test map initializing with different types")
    public void testMultiTypeMapInit() {
        ParserUtils.parseBalFile("lang/expressions/multi-type-map-initializer.bal");
    }
}
