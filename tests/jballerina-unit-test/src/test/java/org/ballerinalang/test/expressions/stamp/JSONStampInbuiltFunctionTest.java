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
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BAnydataType;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.types.BErrorType;
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

import java.util.LinkedHashMap;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

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

        Object results = BRunUtil.invoke(compileResult, "stampJSONToAnydata");
        Object anydataValue = results;

        Assert.assertEquals(anydataValue.toString(), "3");
        Assert.assertEquals(getType(anydataValue).getTag(), TypeTags.INT_TAG);
    }

    @Test
    public void testStampJSONToAnydataV2() {

        Object arr = BRunUtil.invoke(compileResult, "stampJSONToAnydataV2");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 5);
    }

    @Test
    public void testStampJSONToRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToRecord");
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");
    }

    @Test
    public void testStampJSONToRecordV2() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToRecordV2");
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue0.getType().getName(), "Employee");

        Assert.assertEquals((mapValue0).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0).get(StringUtils.fromString("school")).toString(),
                "Hindu College");
        Assert.assertEquals(getType(((LinkedHashMap) mapValue0).get(StringUtils.fromString("school"))).getClass(),
                BStringType.class);

    }

    @Test
    public void testStampJSONToJSON() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToJSON");
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) mapValue0.getType()).getConstrainedType().getClass(), BJsonType.class);

        Assert.assertEquals((mapValue0).size(), 4);
    }

    @Test
    public void testStampJSONToMap() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToMap");
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results;

        Assert.assertEquals((mapValue0).size(), 4);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("name")).toString(), "John");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("name"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("status"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);
    }

    @Test
    public void testStampJSONToMapV2() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToMapV2");
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results;

        Assert.assertEquals((mapValue0).size(), 6);

        Assert.assertEquals(mapValue0.getType().getClass(), BMapType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("name")).toString(), "Raja");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("name"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("status")).toString(), "single");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("status"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("batch"))).getClass(), BStringType.class);

        Assert.assertEquals(mapValue0.get(StringUtils.fromString("school")).toString(), "Hindu College");
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("school"))).getClass(), BStringType.class);

        Assert.assertEquals(((BMap) mapValue0.get(StringUtils.fromString("emp"))).size(), 3);
        Assert.assertEquals(getType(mapValue0.get(StringUtils.fromString("emp"))).getClass(), BMapType.class);
        Assert.assertEquals(
                ((BMapType) getType(mapValue0.get(StringUtils.fromString("emp")))).getConstrainedType().getClass(),
                BAnydataType.class);
    }

    @Test
    public void testStampJSONArrayToConstraintArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampJSONArrayToConstraintArray");
        BArray results = (BArray) arr;
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results.get(0);
        BMap<String, Object> mapValue1 = (BMap<String, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(mapValue0.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue0.getType().getName(), "Student");

        Assert.assertEquals((mapValue0).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0).get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(((LinkedHashMap) mapValue0).get(StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

        Assert.assertEquals(mapValue1.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue1.getType().getName(), "Student");

        Assert.assertEquals((mapValue1).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue1).get(StringUtils.fromString("batch")).toString(), "LK2014");
        Assert.assertEquals(getType(((LinkedHashMap) mapValue0).get(StringUtils.fromString("batch"))).getClass(),
                BStringType.class);

    }

    @Test
    public void testStampJSONArrayToAnydataTypeArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampJSONArrayToAnyTypeArray");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 4);
        Assert.assertEquals(results.get(0).toString(), "1");
        Assert.assertEquals(getType(results.get(0)).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(results.get(1).toString(), "false");
        Assert.assertEquals(getType(results.get(1)).getTag(), TypeTags.BOOLEAN_TAG);
        Assert.assertEquals(results.get(2).toString(), "foo");
        Assert.assertEquals(getType(results.get(2)).getClass(), BStringType.class);
        Assert.assertEquals((((BMap) results.get(3))).size(), 2);
        Assert.assertEquals(getType(results.get(3)).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) getType(results.get(3))).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampJSONToUnion() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToUnion");
        Assert.assertEquals(getType(results).getClass(), BMapType.class);
    }

    @Test
    public void testStampJSONToAnydataV3() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToAnydataV3");
        Assert.assertEquals(getType(results).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) getType(results)).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampJSONArrayWithNullToAnydataArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampJSONArrayWithNullToAnydataArray");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 5);
        Assert.assertEquals(results.get(0).toString(), "1");
        Assert.assertEquals(getType(results.get(0)).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(results.get(1).toString(), "false");
        Assert.assertEquals(getType(results.get(1)).getTag(), TypeTags.BOOLEAN_TAG);
        Assert.assertEquals(results.get(2).toString(), "foo");
        Assert.assertEquals(getType(results.get(2)).getClass(), BStringType.class);
        Assert.assertNull(results.get(3));
        Assert.assertEquals((((BMap) results.get(4))).size(), 2);
        Assert.assertEquals(getType(results.get(4)).getClass(), BMapType.class);
        Assert.assertEquals(((BMapType) getType(results.get(4))).getConstrainedType().getClass(), BAnydataType.class);
    }

    @Test
    public void testStampJSONToRecordWithArray() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToRecordWithArray");
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue0.getType().getClass(), BRecordType.class);
        Assert.assertEquals(mapValue0.getType().getName(), "Foo");

        Assert.assertEquals(
                ((BArrayType) getType(mapValue0.get(StringUtils.fromString("a")))).getElementType().getClass(),
                BStringType.class);
    }

    //----------------------------------- Negative Test cases ----------------------------------------------------

    @Test
    public void testStampJSONToRecordNegative() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToRecordNegative");
        Object error = results;

        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'map<json>' value cannot be converted to 'Student': " +
                        "\n\t\tfield 'age' cannot be added to the closed record 'Student'");
    }

    @Test
    public void testStampJSONToMapNegative() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToMapNegative");
        Object error = results;

        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(
                ((BMap<String, BString>) ((BError) results).getDetails()).get(StringUtils.fromString("message"))
                        .toString(),
                "'map<json>' value cannot be converted to 'StringMap': " +
                        "\n\t\tmap field 'age' should be of type 'string', found '23'");
    }

    @Test
    public void testStampNullJSONToArrayNegative() {

        Object results = BRunUtil.invoke(compileResult, "stampNullJSONToArrayNegative");
        Object error = results;

        Assert.assertEquals(getType(error).getClass(), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results).getDetails()).get(
                        StringUtils.fromString("message")).toString(),
                "cannot convert '()' to type 'StringArray'");
    }

    @AfterClass
    public void tearDown() {

        compileResult = null;
    }
}
