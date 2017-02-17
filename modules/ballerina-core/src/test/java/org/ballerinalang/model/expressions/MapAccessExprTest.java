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
package org.ballerinalang.model.expressions;

import org.ballerinalang.bre.SymScope;
import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Map access expression test.
 *
 * @since 0.8.0
 */
public class MapAccessExprTest {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        // Linking Native functions.
        SymScope symScope = new SymScope(null);
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/map-access-expr.bal");
    }

    @Test(description = "Test map access expression")
    public void testMapAccessExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "mapAccessTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 105;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test map return value")
    public void testArrayReturnValueTest() {
        BValue[] args = {new BString("Chanaka"), new BString("Fernando")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "mapReturnTest", args);

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
        BTestUtils.parseBalFile("lang/expressions/incorrect-map-access.bal");
    }
}
