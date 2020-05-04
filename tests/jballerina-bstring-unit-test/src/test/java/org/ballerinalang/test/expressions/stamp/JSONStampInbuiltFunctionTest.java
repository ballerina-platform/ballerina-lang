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
import org.ballerinalang.core.model.types.BArrayType;
import org.ballerinalang.core.model.types.BErrorType;
import org.ballerinalang.core.model.types.BJSONType;
import org.ballerinalang.core.model.types.BMapType;
import org.ballerinalang.core.model.types.BRecordType;
import org.ballerinalang.core.model.types.BStringType;
import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;

/**
 * Test cases for stamping JSON type variables.
 *
 * @since 0.985.0
 */
public class JSONStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/json-stamp-expr-test.bal");
    }


    //----------------------------- JSON Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampJSONToAnydata() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToAnydata");
        BValue anydataValue = results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(anydataValue.stringValue(), "3");
        Assert.assertEquals(anydataValue.getType().getTag(), TypeTags.INT_TAG);
    }

    @Test
    public void testStampJSONToAnydataV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToAnydataV2");
        Assert.assertEquals(results.length, 5);
    }

    @Test
    public void testStampJSONToRecord() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToRecord");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
    }

    @Test
    public void testStampJSONToRecordV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToRecordV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getName(), "Employee");

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("school").toString(), "Hindu College");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("school")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testStampJSONToJSON() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToJSON");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BJSONType.class);

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
    }

    @Test
    public void testStampJSONToMap() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToMap");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((mapValue0.getMap()).size(), 4);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get("name").stringValue(), "John");
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BStringType.class);
    }

    @Test
    public void testStampJSONToMapV2() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToMapV2");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(results.length, 1);
        Assert.assertEquals((mapValue0.getMap()).size(), 6);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get("name").stringValue(), "Raja");
        Assert.assertEquals(mapValue0.get("name").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("age").stringValue(), "25");
        Assert.assertEquals(mapValue0.get("age").getType().getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue0.get("status").stringValue(), "single");
        Assert.assertEquals(mapValue0.get("status").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("batch").stringValue(), "LK2014");
        Assert.assertEquals(mapValue0.get("batch").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("school").stringValue(), "Hindu College");
        Assert.assertEquals(mapValue0.get("school").getType().getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get("emp").size(), 3);
        Assert.assertEquals(mapValue0.get("emp").getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.get("emp").getType()).getConstrainedType().getClass(),
                BAnydataType.class);
    }

    @Test
    public void testStampJSONArrayToConstraintArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONArrayToConstraintArray");
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];
        BMap<String, BValue> mapValue1 = (BMap<String, BValue>) results[1];

        Assert.assertEquals(results.length, 2);

        Assert.assertEquals(mapValue0.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue0.getType().getName(), "Student");

        Assert.assertEquals((mapValue0.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

        Assert.assertEquals(mapValue1.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue1.getType().getName(), "Student");

        Assert.assertEquals((mapValue1.getMap()).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue1.getMap()).get("batch").toString(), "LK2014");
        Assert.assertEquals(((BValue) ((LinkedHashMap) mapValue0.getMap()).get("batch")).getType().getClass(),
                BStringType.class);

    }

    @Test
    public void testStampJSONArrayToAnydataTypeArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONArrayToAnyTypeArray");
        Assert.assertEquals(results.length, 4);
        Assert.assertEquals(results[0].stringValue(), "1");
        Assert.assertEquals(results[0].getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(results[1].stringValue(), "false");
        Assert.assertEquals(results[1].getType().getTag(), TypeTags.BOOLEAN_TAG);
        Assert.assertEquals(results[2].stringValue(), "foo");
        Assert.assertEquals(results[2].getType().getClass(), BStringType.class);
        Assert.assertEquals((((BMap) results[3]).getMap()).size(), 2);
        Assert.assertEquals(results[3].getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) results[3].getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampJSONToUnion() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToUnion");
        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(results[0].getType().getClass(), BMapType.class);
    }

    @Test
    public void testStampJSONToAnydataV3() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToAnydataV3");
        Assert.assertEquals(results.length, 1);
        Assert.assertEquals(results[0].getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) results[0].getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampJSONArrayWithNullToAnydataArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONArrayWithNullToAnydataArray");
        Assert.assertEquals(results.length, 5);
        Assert.assertEquals(results[0].stringValue(), "1");
        Assert.assertEquals(results[0].getType().getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(results[1].stringValue(), "false");
        Assert.assertEquals(results[1].getType().getTag(), TypeTags.BOOLEAN_TAG);
        Assert.assertEquals(results[2].stringValue(), "foo");
        Assert.assertEquals(results[2].getType().getClass(), BStringType.class);
        Assert.assertNull(results[3]);
        Assert.assertEquals((((BMap) results[4]).getMap()).size(), 2);
        Assert.assertEquals(results[4].getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) results[4].getType()).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampJSONToRecordWithArray() {

        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToRecordWithArray");
        Assert.assertEquals(results.length, 1);
        BMap<String, BValue> mapValue0 = (BMap<String, BValue>) results[0];

        Assert.assertEquals(mapValue0.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue0.getType().getName(), "Foo");

        Assert.assertEquals(((BArrayType) mapValue0.get("a").getType()).getElementType().getClass(), BStringType.class);
    }

    //----------------------------------- Negative Test cases ----------------------------------------------------

    @Test
    public void testStampJSONToRecordNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToRecordNegative");
        BValue error = results[0];

        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'map<json>' value cannot be converted to 'Student'");
    }

    @Test
    public void testStampJSONToMapNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "stampJSONToMapNegative");
        BValue error = results[0];

        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "'map<json>' value cannot be converted to 'map<string>'");
    }

    @Test
    public void testStampNullJSONToArrayNegative() {
        BValue[] results = BRunUtil.invoke(compileResult, "stampNullJSONToArrayNegative");
        BValue error = results[0];

        Assert.assertEquals(error.getType().getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results[0]).getDetails()).get("message").stringValue(),
                            "cannot convert '()' to type 'StringArray'");
    }
}
