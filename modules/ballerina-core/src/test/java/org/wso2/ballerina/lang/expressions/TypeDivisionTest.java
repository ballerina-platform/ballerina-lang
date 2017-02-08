/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

public class TypeDivisionTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/expressions/type-division.bal");
    }

    @Test
    public void testIntDivisionFloat() {
        BValue[] args = {new BInteger(110), new BFloat(22L)};
        BValue[] returns = Functions.invoke(bFile, "intDivideByFloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        final String expected = "5.0";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatDivisionInt() {
        BValue[] args = {new BFloat(110f), new BInteger(22)};
        BValue[] returns = Functions.invoke(bFile, "floatDivideByInt", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        final String expected = "5.0";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntGTFloat() {
        BValue[] args = {new BInteger(110), new BFloat(22L)};
        BValue[] returns = Functions.invoke(bFile, "intGTFloat", args);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatGTInt() {
        BValue[] args = {new BFloat(110f), new BInteger(22)};
        BValue[] returns = Functions.invoke(bFile, "floatGTInt", args);
        Assert.assertTrue(returns[0] instanceof BBoolean);
        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }


}
