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

import org.ballerinalang.core.model.types.BAnydataType;
import org.ballerinalang.core.model.types.BMapType;
import org.ballerinalang.core.model.types.BRecordType;
import org.ballerinalang.core.model.types.BStringType;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

        BValue[] results = BRunUtil.invoke(compileResult, "stampTupleValueV1");
        Assert.assertEquals(results.length, 2);

        BValue tupleValue1 = results[0];
        BValue tupleValue2 = results[1];

        Assert.assertEquals(tupleValue1.stringValue(), "Mohan");
        Assert.assertEquals(tupleValue1.getType().getClass(), BStringType.class);

        Assert.assertEquals(tupleValue2.getType().getClass(), BRecordType.class);
        Assert.assertEquals(tupleValue2.getType().getName(), "Teacher");

        Assert.assertEquals(((BMap) tupleValue2).size(), 5);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("name")).stringValue(), "Raja");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("name")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("age")).stringValue(), "25");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("age")).getType().getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("status")).stringValue(), "single");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("status")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("batch")).stringValue(), "LK2014");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("school")).stringValue(), "Hindu College");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("school")).getType().getClass(),
                BStringType.class);
    }

    @Test
    public void testStampTupleValueV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampTupleValueV2");
        Assert.assertEquals(results.length, 2);

        BValue tupleValue1 = results[0];
        BValue tupleValue2 = results[1];

        Assert.assertEquals(tupleValue1.stringValue(), "Mohan");
        Assert.assertEquals(tupleValue1.getType().getClass(), BStringType.class);

        Assert.assertEquals(tupleValue2.getType().getClass(), BRecordType.class);
        Assert.assertEquals(tupleValue2.getType().getName(), "Employee");

        Assert.assertEquals(((BMap) tupleValue2).size(), 5);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("name")).stringValue(), "Raja");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("name")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("age")).stringValue(), "25");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("age")).getType().getTag(),
                TypeTags.INT_TAG);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("status")).stringValue(), "single");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("status")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("batch")).stringValue(), "LK2014");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("school")).stringValue(), "Hindu College");
        Assert.assertEquals(((BValue) ((BMap) tupleValue2).getMap().get("school")).getType().getClass(),
                BStringType.class);
    }

    @Test
    public void testStampTupleToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampTupleToAnydata");
        Assert.assertEquals(results.length, 2);

        BValue tupleValue1 = results[0];
        BValue tupleValue2 = results[1];

        Assert.assertEquals(tupleValue1.stringValue(), "Mohan");
        Assert.assertEquals(tupleValue1.getType().getClass(), BStringType.class);

        Assert.assertEquals(tupleValue2.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) tupleValue2.getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampTupleValueToArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampTupleValueToArray");
        Assert.assertEquals(results.length, 2);

        BValue arrayValue1 = results[0];
        BValue arrayValue2 = results[1];

        Assert.assertEquals(arrayValue1.getType().getClass(), BRecordType.class);
        Assert.assertEquals(arrayValue1.getType().getName(), "Employee");

        Assert.assertEquals(arrayValue2.getType().getClass(), BRecordType.class);
        Assert.assertEquals(arrayValue2.getType().getName(), "Employee");

        Assert.assertEquals(arrayValue2.size(), 4);

        Assert.assertEquals(((BValue) ((BMap) arrayValue2).getMap().get("name")).stringValue(), "Raja");
        Assert.assertEquals(((BValue) ((BMap) arrayValue2).getMap().get("name")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) arrayValue2).getMap().get("status")).stringValue(), "single");
        Assert.assertEquals(((BValue) ((BMap) arrayValue2).getMap().get("status")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) arrayValue2).getMap().get("batch")).stringValue(), "LK2014");
        Assert.assertEquals(((BValue) ((BMap) arrayValue2).getMap().get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(((BValue) ((BMap) arrayValue2).getMap().get("school")).stringValue(), "Hindu College");
        Assert.assertEquals(((BValue) ((BMap) arrayValue2).getMap().get("school")).getType().getClass(),
                BStringType.class);
    }

    @Test
    public void testStampTupleToBasicArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampTupleToBasicArray");
        Assert.assertEquals(((BValueArray) results[0]).getInt(0), 1);
        Assert.assertEquals(((BValueArray) results[0]).getInt(1), 2);
    }

    @Test
    public void testStampTupleToAnydataTuple() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampTupleToAnydataTuple");
        Assert.assertEquals(((BInteger) results[0]).value().intValue(), 1);
        Assert.assertEquals(((BInteger) results[1]).value().intValue(), 2);
    }

    @Test
    public void testStampAnydataTupleToBasicTypeTuple() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnydataTupleToBasicTypeTuple");
        Assert.assertEquals(((BInteger) results[0]).value().intValue(), 1);
        Assert.assertEquals(((BInteger) results[1]).value().intValue(), 2);
    }
}
