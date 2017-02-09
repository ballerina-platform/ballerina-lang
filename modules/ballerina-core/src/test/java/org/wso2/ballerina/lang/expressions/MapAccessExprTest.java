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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Map access expression test.
 *
 * @since 0.8.0
 */
public class MapAccessExprTest {
    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        // Linking Native functions.
        SymScope symScope = new SymScope(null);
        bFile = ParserUtils.parseBalFile("lang/expressions/map-access-expr.bal", symScope);
    }

    @Test(description = "Test map access expression")
    public void testMapAccessExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = Functions.invoke(bFile, "mapAccessTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 105;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test map return value")
    public void testArrayReturnValueTest() {
        BValue[] args = {new BString("Chanaka"), new BString("Fernando")};
        BValue[] returns = Functions.invoke(bFile, "mapReturnTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);

        BMap<BString, BString> mapValue = (BMap<BString, BString>) returns[0];
        Assert.assertEquals(mapValue.size(), 3);

        Assert.assertEquals(mapValue.get(new BString("fname")).stringValue(), "Chanaka");
        Assert.assertEquals(mapValue.get(new BString("lname")).stringValue(), "Fernando");
        Assert.assertEquals(mapValue.get(new BString("ChanakaFernando")).stringValue(), "ChanakaFernando");

    }

    @Test(description = "Test map access with an index",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "incorrect-map-access.bal:4: non-string map index type 'int'",
    dependsOnMethods = {"testMapAccessExpr", "testArrayReturnValueTest"})
    public void testMapAccessWithIndex() {
        ParserUtils.parseBalFile("lang/expressions/incorrect-map-access.bal");
    }
}
