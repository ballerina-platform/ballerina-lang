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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.types.BJsonType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BStringType;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

/**
 * Test cases for stamping Union type variables.
 *
 * @since 0.985.0
 */
public class UnionTypeStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {

        compileResult = BCompileUtil.compile("test-src/expressions/stamp/union-stamp-expr-test.bal");
    }

    //----------------------------- Union Type Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampUnionToRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampUnionToRecord");
        BMap<String, Object> employee0 = (BMap<String, Object>) results;

        Assert.assertEquals(employee0.size(), 4);
        Assert.assertEquals(getType(employee0).getClass(), BRecordType.class);
        Assert.assertEquals(getType(employee0).getName(), "Employee");

        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);

        Assert.assertEquals(employee0.get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
    }

    @Test
    public void testStampUnionToJSON() {

        Object results = BRunUtil.invoke(compileResult, "stampUnionToJSON");
        BMap<String, Object> employee0 = (BMap<String, Object>) results;

        Assert.assertEquals(employee0.size(), 4);
        Assert.assertEquals(getType(employee0).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) employee0.getType()).getConstrainedType().getClass(), BJsonType.class);

        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);

        Assert.assertEquals(employee0.get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
    }

    @Test
    public void testStampUnionToXML() {
        BRunUtil.invoke(compileResult, "stampUnionToXML");
    }

    @Test
    public void testStampUnionToIntMap() {
        BRunUtil.invoke(compileResult, "stampUnionToIntMap");
    }

    @Test
    public void testStampUnionToConstraintMap() {
        BRunUtil.invoke(compileResult, "stampUnionToConstraintMap");
    }

    @Test
    public void testStampUnionToAnydata() {

        Object results = BRunUtil.invoke(compileResult, "stampUnionToAnydata");
        Object stampedValue = results;

        Assert.assertEquals(getType(stampedValue).getClass(), BStringType.class);
    }

    @Test
    public void testStampUnionToTuple() {

        Object arr = BRunUtil.invoke(compileResult, "stampUnionToTuple");
        BArray results = (BArray) arr;
        Object stampedValue0 = results.get(0);
        Object stampedValue1 = results.get(1);

        Assert.assertEquals(stampedValue0.toString(), "mohan");
        Assert.assertEquals(getType(stampedValue0).getClass(), BStringType.class);

        Assert.assertEquals(stampedValue1.toString(), "LK2014");
        Assert.assertEquals(getType(stampedValue1).getClass(), BStringType.class);
    }

    @Test
    public void testStampUnionToAnydataV2() {

        Object results = BRunUtil.invoke(compileResult, "stampUnionToAnydataV2");
        Object stampedValue = results;

        Assert.assertEquals(getType(stampedValue).getClass(), BStringType.class);
    }

    @Test
    public void testStampUnionToConstraintMapToUnion() {

        Object results = BRunUtil.invoke(compileResult, "stampUnionToConstraintMapToUnion");
        BMap<String, Object> employee0 = (BMap<String, Object>) results;

        Assert.assertEquals(employee0.size(), 2);

        Assert.assertEquals(getType(employee0).getClass(), BMapType.class);
        Type constrainedType = TypeUtils.getReferredType(((BMapType) employee0.getType()).getConstrainedType());
        Assert.assertEquals(constrainedType.getClass(), BRecordType.class);
        Assert.assertEquals(constrainedType.getName(), "Teacher");

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("a"))).getName(), "Teacher");
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("a"))).get(
                        StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("a"))).get(
                        StringUtils.fromString("school"))).getClass(),
                BStringType.class);
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("a"))).get(
                        StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("b"))).getName(), "Teacher");
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("b"))).get(
                        StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("b"))).get(
                        StringUtils.fromString("school"))).getClass(),
                BStringType.class);
        Assert.assertEquals(getType(((BMap) employee0.get(StringUtils.fromString("b"))).get(
                        StringUtils.fromString("batch"))).getClass(),
                BStringType.class);
    }

    @AfterClass
    public void tearDown() {

        compileResult = null;
    }
}

