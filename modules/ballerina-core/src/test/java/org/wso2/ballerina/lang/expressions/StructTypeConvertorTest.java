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
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BStruct;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.BTestUtils;
import org.ballerinalang.util.program.BLangFunctions;


public class StructTypeConvertorTest {
    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = BTestUtils.parseBalFile("lang/expressions/struct-type-convertor.bal");
    }

    @Test
    public void testStructConvertor() {
        BValue[] returns = BLangFunctions.invoke(bFile, "testStructConvertor");
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct bStruct = (BStruct) returns[0];
        final String expectedName = "Jack";
        Assert.assertEquals(bStruct.getValue(0).stringValue(), expectedName);
        Assert.assertTrue(bStruct.getValue(1) instanceof BMap);
        final BMap<BString, BValue> resultMap = (BMap<BString, BValue>) bStruct.getValue(1);
        Assert.assertEquals(resultMap.get(new BString("country")).stringValue(), "USA");
        Assert.assertEquals(resultMap.get(new BString("state")).stringValue(), "CA");
        Assert.assertTrue(bStruct.getValue(2) instanceof BInteger);
        final String expectedAge = "25";
        Assert.assertEquals(bStruct.getValue(2).stringValue(), expectedAge);

    }


}


