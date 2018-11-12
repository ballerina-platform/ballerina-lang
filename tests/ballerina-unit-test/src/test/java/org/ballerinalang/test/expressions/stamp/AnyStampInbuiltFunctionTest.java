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
import org.ballerinalang.model.types.BAnydataType;
import org.ballerinalang.model.types.BJSONType;
import org.ballerinalang.model.types.BObjectType;
import org.ballerinalang.model.types.BRecordType;
import org.ballerinalang.model.types.BStringType;
import org.ballerinalang.model.types.BXMLType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for stamping Any type variables.
 *
 * @since 0.983.0
 */
public class AnyStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/any-stamp-expr-test.bal");
    }


    //----------------------------- Any Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampAnyToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnyToJSON");
        BValue anyValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anyValue.stringValue(), "3");
        Assert.assertEquals(anyValue.getType().getClass(), BJSONType.class);
    }

    @Test
    public void testStampAnyToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnyToRecord");
        BMap<String, BValue> employee0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(employee0.get("age").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(employee0.get("age").stringValue(), "25");

        Assert.assertEquals(employee0.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(employee0.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(employee0.get("school").getType().getClass(), BAnydataType.class);
        Assert.assertEquals(employee0.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampAnyToJSONV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnyToJSONV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 5);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("school").toString(), "Hindu College");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("school")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testStampAnyToXML() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnyToXML");
        BValue anyValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anyValue.stringValue(), "<book>The Lost World</book>");
        Assert.assertEquals(anyValue.getType().getClass(), BXMLType.class);
    }

    @Test
    public void testStampAnyToObject() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnyToObject");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.getType().getClass(), BObjectType.class);

        Assert.assertEquals(mapValue.getMap().get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get("age").stringValue(), "10");

        Assert.assertEquals(mapValue.get("year").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get("year").stringValue(), "2014");

        Assert.assertEquals(mapValue.get("month").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("month").stringValue(), "february");
    }

    @Test
    public void testStampAnyToMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnyToMap");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(mapValue.getMap().get("a").getType().getName(), "Employee");
        Assert.assertEquals(((BMap) mapValue.getMap().get("a")).getMap().size(), 5);

        Assert.assertEquals(mapValue.getMap().get("b").getType().getName(), "Employee");
        Assert.assertEquals(((BMap) mapValue.getMap().get("b")).getMap().size(), 5);
    }

    @Test
    public void testStampAnyToRecordArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnyToRecordArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getName(), "Teacher");
        Assert.assertEquals(mapValue1.getType().getName(), "Teacher");
    }

    @Test
    public void testStampAnyToTuple() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnyToTuple");
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
    public void testStampAnyToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnyToAnydata");
        BMap<String, BValue> mapValue = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(mapValue.getType().getClass(), BAnydataType.class);
        Assert.assertEquals(mapValue.getMap().size(), 5);

        Assert.assertEquals(mapValue.get("age").getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(mapValue.get("age").stringValue(), "25");

        Assert.assertEquals(mapValue.get("batch").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("batch").stringValue(), "LK2014");

        Assert.assertEquals(mapValue.get("school").getType().getClass(), BStringType.class);
        Assert.assertEquals(mapValue.get("school").stringValue(), "Hindu College");
    }

    @Test
    public void testStampAnyToConstraintJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampAnytToConstraintJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);

        Assert.assertEquals(mapValue0.getType().getClass(), BJSONType.class);

        Assert.assertEquals(((BJSONType) mapValue0.getType()).getConstrainedType().getName(), "Person");
        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

    //---------------------------------- Negative Test cases ----------------------------------------------

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: incompatible stamp operation: 'PersonObj' value cannot be " +
                    "stamped as 'anydata'.*")
    public void testStampAnyObjectToAnydata() {
        BRunUtil.invoke(compileResult, "stampAnyObjectToAnydata");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: incompatible stamp operation: 'stream' value cannot be stamped " +
                    "as 'json<Person>'.*")
    public void testStampAnyInvalidInput() {
        BRunUtil.invoke(compileResult, "stampAnyInvalidInput");
    }
}

