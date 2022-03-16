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
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.types.BAnydataType;
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
 * Test cases for stamping Tuple type variables.
 *
 * @since 0.985.0
 */
public class TupleTypeStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/tuple-stamp-expr-test.bal");
    }

    //----------------------------- Tuple Type Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampTupleValueV1() {

        Object arr = BRunUtil.invoke(compileResult, "stampTupleValueV1");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);

        Object tupleValue1 = results.get(0);
        Object tupleValue2 = results.get(1);

        Assert.assertEquals(tupleValue1.toString(), "Mohan");
        Assert.assertEquals(getType(tupleValue1).getClass(), BStringType.class);

        Assert.assertEquals(getType(tupleValue2).getClass(), BRecordType.class);
        Assert.assertEquals(getType(tupleValue2).getName(), "Teacher");

        Assert.assertEquals(((BMap) tupleValue2).size(), 5);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("name")).toString(), "Raja");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("name"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("status"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("school"))).getClass(),
                BStringType.class);
    }

    @Test
    public void testStampTupleValueV2() {

        Object arr = BRunUtil.invoke(compileResult, "stampTupleValueV2");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);

        Object tupleValue1 = results.get(0);
        Object tupleValue2 = results.get(1);

        Assert.assertEquals(tupleValue1.toString(), "Mohan");
        Assert.assertEquals(getType(tupleValue1).getClass(), BStringType.class);

        Assert.assertEquals(getType(tupleValue2).getClass(), BRecordType.class);
        Assert.assertEquals(getType(tupleValue2).getName(), "Employee");

        Assert.assertEquals(((BMap) tupleValue2).size(), 5);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("name")).toString(), "Raja");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("name"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("status"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("school"))).getClass(),
                BStringType.class);
    }

    @Test
    public void testStampTupleToAnydata() {

        Object arr = BRunUtil.invoke(compileResult, "stampTupleToAnydata");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);

        Object tupleValue1 = results.get(0);
        Object tupleValue2 = results.get(1);

        Assert.assertEquals(tupleValue1.toString(), "Mohan");
        Assert.assertEquals(getType(tupleValue1).getClass(), BStringType.class);

        Assert.assertEquals(getType(tupleValue2).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) getType(tupleValue2)).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampTupleValueToArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampTupleValueToArray");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);

        Object arrayValue1 = results.get(0);
        Object arrayValue2 = results.get(1);

        Assert.assertEquals(getType(arrayValue1).getClass(), BRecordType.class);
        Assert.assertEquals(getType(arrayValue1).getName(), "Employee");

        Assert.assertEquals(getType(arrayValue2).getClass(), BRecordType.class);
        Assert.assertEquals(getType(arrayValue2).getName(), "Employee");

        Assert.assertEquals(((BMap) arrayValue2).size(), 4);

        Assert.assertEquals(((BMap) arrayValue2).get(StringUtils.fromString("name")).toString(), "Raja");
        Assert.assertEquals(getType(((BMap) arrayValue2).get(StringUtils.fromString("name"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) arrayValue2).get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(((BMap) arrayValue2).get(StringUtils.fromString("status"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) arrayValue2).get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(((BMap) arrayValue2).get(StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

        Assert.assertEquals(((BMap) arrayValue2).get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(((BMap) arrayValue2).get(StringUtils.fromString("school"))).getClass(),
                BStringType.class);
    }

    @Test
    public void testStampTupleToBasicArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampTupleToBasicArray");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0), 1L);
        Assert.assertEquals(results.get(1), 2L);
    }

    @Test
    public void testStampTupleToAnydataTuple() {

        Object arr = BRunUtil.invoke(compileResult, "stampTupleToAnydataTuple");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0), 1L);
        Assert.assertEquals(results.get(1), 2L);
    }

    @Test
    public void testStampAnydataTupleToBasicTypeTuple() {

        Object arr = BRunUtil.invoke(compileResult, "stampAnydataTupleToBasicTypeTuple");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.get(0), 1L);
        Assert.assertEquals(results.get(1), 2L);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
