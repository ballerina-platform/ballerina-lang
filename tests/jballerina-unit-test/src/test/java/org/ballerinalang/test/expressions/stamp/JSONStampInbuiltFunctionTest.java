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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.TypeHelper;
import io.ballerina.runtime.internal.types.BErrorType;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BStringType;
import org.ballerinalang.test.BAssertUtil;
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
        BAssertUtil.assertTypeClass(getType(((LinkedHashMap) mapValue0).get(StringUtils.fromString("school"))),
                BStringType.class);

    }

    @Test
    public void testStampJSONToJSON() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToJSON");
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results;

        BAssertUtil.assertTypeClass(mapValue0.getType(), BMapType.class);
        Assert.assertTrue(
                TypeChecker.checkIsType(TypeHelper.typeConstraint(mapValue0.getType()), PredefinedTypes.TYPE_JSON));

        Assert.assertEquals((mapValue0).size(), 4);
    }

    @Test
    public void testStampJSONToMap() {
        BRunUtil.invoke(compileResult, "stampJSONToMap");
    }

    @Test
    public void testStampJSONToMapV2() {
        BRunUtil.invoke(compileResult, "stampJSONToMapV2");
    }

    @Test
    public void testStampJSONArrayToConstraintArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampJSONArrayToConstraintArray");
        BArray results = (BArray) arr;
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results.get(0);
        BMap<String, Object> mapValue1 = (BMap<String, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        BAssertUtil.assertTypeClass(mapValue0.getType(), BRecordType.class);
        Assert.assertEquals(mapValue0.getType().getName(), "Student");

        Assert.assertEquals((mapValue0).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue0).get(StringUtils.fromString("batch")).toString(), "LK2014");
        BAssertUtil.assertTypeClass(getType(((LinkedHashMap) mapValue0).get(StringUtils.fromString("batch"))),
                BStringType.class);

        BAssertUtil.assertTypeClass(mapValue1.getType(), BRecordType.class);
        Assert.assertEquals(mapValue1.getType().getName(), "Student");

        Assert.assertEquals((mapValue1).size(), 4);
        Assert.assertEquals(((LinkedHashMap) mapValue1).get(StringUtils.fromString("batch")).toString(), "LK2014");
        BAssertUtil.assertTypeClass(getType(((LinkedHashMap) mapValue1).get(StringUtils.fromString("batch"))),
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
        BAssertUtil.assertTypeClass(getType(results.get(2)), BStringType.class);
        Assert.assertEquals((((BMap) results.get(3))).size(), 2);
        BAssertUtil.assertTypeClass(getType(results.get(3)), BMapType.class);
        TypeChecker.checkIsType(TypeHelper.typeConstraint(getType(results.get(3))), PredefinedTypes.TYPE_ANYDATA);
    }

    @Test
    public void testStampJSONToUnion() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToUnion");
        BAssertUtil.assertTypeClass(getType(results), BMapType.class);
    }

    @Test
    public void testStampJSONToAnydataV3() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToAnydataV3");
        BAssertUtil.assertTypeClass(getType(results), BMapType.class);
        TypeChecker.checkIsType(TypeHelper.typeConstraint(getType(results)), PredefinedTypes.TYPE_ANYDATA);
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
        BAssertUtil.assertTypeClass(getType(results.get(2)), BStringType.class);
        Assert.assertNull(results.get(3));
        Assert.assertEquals((((BMap) results.get(4))).size(), 2);

        BAssertUtil.assertTypeClass(getType(results.get(4)), BMapType.class);
        TypeChecker.checkIsType(TypeHelper.typeConstraint(getType(results.get(4))), PredefinedTypes.TYPE_ANYDATA);
    }

    @Test
    public void testStampJSONToRecordWithArray() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToRecordWithArray");
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results;

        BAssertUtil.assertTypeClass(mapValue0.getType(), BRecordType.class);
        Assert.assertEquals(mapValue0.getType().getName(), "Foo");

        BAssertUtil.assertTypeClass(
                TypeHelper.listRestType(getType(mapValue0.get(StringUtils.fromString("a")))),
                BStringType.class);
    }

    //----------------------------------- Negative Test cases ----------------------------------------------------

    @Test
    public void testStampJSONToRecordNegative() {

        Object results = BRunUtil.invoke(compileResult, "stampJSONToRecordNegative");
        Object error = results;

        BAssertUtil.assertTypeClass(getType(error), BErrorType.class);
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

        BAssertUtil.assertTypeClass(getType(error), BErrorType.class);
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

        BAssertUtil.assertTypeClass(getType(error), BErrorType.class);
        Assert.assertEquals(((BMap<String, BString>) ((BError) results).getDetails()).get(
                        StringUtils.fromString("message")).toString(),
                "cannot convert '()' to type 'StringArray'");
    }

    @AfterClass
    public void tearDown() {

        compileResult = null;
    }
}
