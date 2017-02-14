/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.lang.constants;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

public class ConstantDefinitionTest {
    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/constants/constants-definitions.bal");
    }

    @Test
    public void testIntA() {
        BValue[] returns = Functions.invoke(bFile, "testIntA");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returnVal).intValue(), 5);
    }

    @Test
    public void testStringB() {
        BValue[] returns = Functions.invoke(bFile, "testStringB");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BString.class);
        Assert.assertEquals(returnVal.stringValue(), "AB");
    }

    @Test
    public void testSum() {
        int expected = 5 + 4 + 5 * 4;
        BValue[] returns = Functions.invoke(bFile, "testSum");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returnVal).intValue(), expected);
    }

    @Test
    public void testResult() {
        BValue[] returns = Functions.invoke(bFile, "testResult");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returnVal).booleanValue(), false);
    }

    @Test
    public void testDouble() {
        BValue[] returns = Functions.invoke(bFile, "testDouble");
        Assert.assertEquals(returns.length, 1);
        BValue returnVal = returns[0];
        Assert.assertEquals(returnVal.getClass(), BDouble.class);
        Assert.assertEquals(((BDouble) returnVal).doubleValue(), 6.923456);
    }
}
