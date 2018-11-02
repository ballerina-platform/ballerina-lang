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
package org.ballerinalang.test.expressions.seal;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.types.BAnyType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.types.BXMLType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for sealing Union type variables.
 *
 * @since 0.983.0
 */
public class UnionTypeSealInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/seal/union-seal-expr-test.bal");
    }


    //----------------------------- Union Type Seal Test cases ------------------------------------------------------

    @Test
    public void testSealUnionToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealUnionToRecord");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 4);
        Assert.assertEquals(employee0.getType().getClass(), BRecordType.class);
        Assert.assertEquals(employee0.getType().getName(), "Employee");

        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(employee0.get("school").getType().getClass(), BAnyType.class);
    }

    @Test
    public void testSealUnionToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealUnionToJSON");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 4);
        Assert.assertEquals(employee0.getType().getClass(), BJSONType.class);

        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(employee0.get("school").getType().getClass(), BStringType.class);
    }

    @Test
    public void testSealUnionToObject() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealUnionToObject");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.getMap().get("age").getType().getName(), "int");
        Assert.assertEquals(employee0.get("age").stringValue(), "10");

        Assert.assertEquals(employee0.get("year").getType().getClass(), BAnyType.class);
        Assert.assertEquals(employee0.get("year").stringValue(), "2014");

        Assert.assertEquals(employee0.get("month").getType().getClass(), BAnyType.class);
        Assert.assertEquals(employee0.get("month").stringValue(), "february");
    }

    @Test
    public void testSealUnionToXML() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealUnionToXML");
        BValue xmlValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((xmlValue).getType().getClass(), BXMLType.class);
    }

    @Test
    public void testSealUnionToIntMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealUnionToIntMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue.getType()).getConstrainedType().getTag(), TypeTags.INT_TAG);
    }

    @Test
    public void testSealUnionToConstraintMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "sealUnionToConstraintMap");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(employee0.size(), 2);

        Assert.assertEquals(employee0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) employee0.getType()).getConstrainedType().getClass(), BRecordType.class);
        Assert.assertEquals(((BMapType) employee0.getType()).getConstrainedType().getName(), "Employee");

        Assert.assertEquals(employee0.getMap().get("a").getType().getName(), "Employee");
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("a")).getMap().get("age")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("a")).getMap().get("school")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("a")).getMap().get("batch")).getType().getClass(),
                BStringType.class);


        Assert.assertEquals(employee0.getMap().get("b").getType().getName(), "Employee");
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("b")).getMap().get("age")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("b")).getMap().get("school")).getType().getClass(),
                BAnyType.class);
        Assert.assertEquals(((BValue) ((BMap) employee0.getMap().get("b")).getMap().get("batch")).getType().getClass(),
                BStringType.class);
    }

}

