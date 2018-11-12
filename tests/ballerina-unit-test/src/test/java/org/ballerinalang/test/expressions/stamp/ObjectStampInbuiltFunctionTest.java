/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.expressions.stamp;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BAnyType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for stamping Object type variables.
 *
 * @since 0.983.0
 */
public class ObjectStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/object-stamp-expr-test.bal");
    }

    //----------------------------- Object Stamp Test cases ------------------------------------------------------


    @Test
    public void testStampObjectsV1() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampObjectsV1");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(mapValue.getMap().get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get("age").stringValue(), "10");

        Assert.assertEquals(mapValue.get("year").getType().getClass(), BAnyType.class);
        Assert.assertEquals(mapValue.get("year").stringValue(), "2014");

        Assert.assertEquals(mapValue.get("month").getType().getClass(), BAnyType.class);
        Assert.assertEquals(mapValue.get("month").stringValue(), "february");
    }

    @Test
    public void testStampObjectToAny() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampObjectsToAny");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.getType().getClass(), BAnyType.class);

        Assert.assertEquals(mapValue.getMap().get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get("age").stringValue(), "10");

        Assert.assertEquals(mapValue.get("year").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get("year").stringValue(), "2014");

        Assert.assertEquals(mapValue.get("month").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("month").stringValue(), "february");
    }


}

