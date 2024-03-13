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
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANYDATA
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.stamp;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeBuilder;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.TypeHelper;
import io.ballerina.runtime.internal.types.BMapType;
import io.ballerina.runtime.internal.types.BRecordType;
import io.ballerina.runtime.internal.types.BXmlType;
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
 * Test cases for stamping Anydata type variables.
 *
 * @since 0.985.0
 */
public class AnydataStampInbuiltFunctionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/stamp/anydata-stamp-expr-test.bal");
    }

    //----------------------------- Anydata Stamp Test cases ------------------------------------------------------

    @Test
    public void testStampAnydataToJSON() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataToJSON");
        Object anydataValue = results;

        Assert.assertEquals(anydataValue.toString(), "3");
        Assert.assertEquals(getType(anydataValue).getTag(), TypeTags.INT_TAG);
    }

    @Test
    public void testStampAnydataToRecord() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataToRecord");
        BMap<String, Object> employee0 = (BMap<String, Object>) results;


        Assert.assertEquals(getType(employee0.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);
        Assert.assertEquals(employee0.get(StringUtils.fromString("age")).toString(), "25");
        TypeChecker.checkIsType(getType(employee0.get(StringUtils.fromString("batch"))), PredefinedTypes.TYPE_STRING);
        Assert.assertEquals(employee0.get(StringUtils.fromString("batch")).toString(), "LK2014");

        TypeChecker.checkIsType(getType(employee0.get(StringUtils.fromString("school"))), PredefinedTypes.TYPE_STRING);
        Assert.assertEquals(employee0.get(StringUtils.fromString("school")).toString(), "Hindu College");
    }

    @Test
    public void testStampAnydataToJSONV2() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataToJSONV2");
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results;

        Assert.assertTrue(TypeBuilder.unwrap(getType(mapValue0)) instanceof BMapType);
        Assert.assertTrue(
                TypeChecker.checkIsType(TypeHelper.typeConstraint(getType(mapValue0)), PredefinedTypes.TYPE_JSON));

        Assert.assertEquals((mapValue0).size(), 5);
        Assert.assertEquals(((LinkedHashMap) mapValue0).get(StringUtils.fromString("school")).toString(),
                "Hindu College");
        TypeChecker.checkIsType(getType(((LinkedHashMap) mapValue0).get(
                StringUtils.fromString("school"))), PredefinedTypes.TYPE_STRING);

    }

    @Test
    public void testStampAnydataToXML() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataToXML");
        Object anydataValue = results;

        Assert.assertEquals(anydataValue.toString(), "<book>The Lost World</book>");
        Assert.assertTrue(TypeBuilder.unwrap(getType(anydataValue)) instanceof BXmlType);
    }

    @Test
    public void testStampAnydataToMap() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataToMap");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 2);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("a"))).getName(), "Employee");
        Assert.assertEquals(((BMap) mapValue.get(StringUtils.fromString("a"))).size(), 5);

        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("b"))).getName(), "Employee");
        Assert.assertEquals(((BMap) mapValue.get(StringUtils.fromString("b"))).size(), 5);
    }

    @Test
    public void testStampAnydataToRecordArray() {

        Object arr = BRunUtil.invoke(compileResult, "stampAnydataToRecordArray");
        BArray results = (BArray) arr;
        BMap<String, Object> mapValue0 = (BMap<String, Object>) results.get(0);
        BMap<String, Object> mapValue1 = (BMap<String, Object>) results.get(1);

        Assert.assertEquals(results.size(), 2);

        Assert.assertEquals(getType(mapValue0).getName(), "Teacher");
        Assert.assertEquals(getType(mapValue1).getName(), "Teacher");
    }

    @Test
    public void testStampAnydataToTuple() {

        Object arr = BRunUtil.invoke(compileResult, "stampAnydataToTuple");
        BArray results = (BArray) arr;
        Assert.assertEquals(results.size(), 2);

        Object tupleValue1 = results.get(0);
        Object tupleValue2 = results.get(1);

        Assert.assertEquals(tupleValue1.toString(), "Mohan");
        TypeChecker.checkIsType(getType(tupleValue1), PredefinedTypes.TYPE_STRING);

        Assert.assertTrue(TypeBuilder.unwrap(getType(tupleValue2)) instanceof BRecordType);
        Assert.assertEquals(getType(tupleValue2).getName(), "Teacher");

        Assert.assertEquals(((BMap) tupleValue2).size(), 5);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("name")).toString(), "Raja");
        TypeChecker.checkIsType(
                getType(((BMap) tupleValue2).get(StringUtils.fromString("name"))), PredefinedTypes.TYPE_STRING);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(((BMap) tupleValue2).get(StringUtils.fromString("age"))).getTag(),
                TypeTags.INT_TAG);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("status")).toString(), "single");
        TypeChecker.checkIsType(getType(((BMap) tupleValue2).get(StringUtils.fromString("status"))),
                PredefinedTypes.TYPE_STRING);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("batch")).toString(), "LK2014");
        TypeChecker.checkIsType(getType(((BMap) tupleValue2).get(StringUtils.fromString("batch"))),
                PredefinedTypes.TYPE_STRING);

        Assert.assertEquals(((BMap) tupleValue2).get(StringUtils.fromString("school")).toString(), "Hindu College");
        TypeChecker.checkIsType(getType(((BMap) tupleValue2).get(StringUtils.fromString("school"))),
                PredefinedTypes.TYPE_STRING);
    }

    @Test
    public void testStampAnydataMapToAnydataMap() {
        BRunUtil.invoke(compileResult, "stampAnydataMapToAnydataMap");
    }

    @Test
    public void testStampAnydataToAnydata() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataToAnydata");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertTrue(TypeBuilder.unwrap(getType(mapValue)) instanceof BMapType);
        Assert.assertTrue(
                TypeChecker.checkIsType(TypeHelper.typeConstraint(getType(mapValue)), PredefinedTypes.TYPE_ANYDATA));
    }

    @Test
    public void testStampAnydataMapToUnion() {

        Object results = BRunUtil.invoke(compileResult, "stampAnydataMapToUnion");
        BMap<String, Object> mapValue = (BMap<String, Object>) results;

        Assert.assertEquals(mapValue.size(), 5);

        Assert.assertTrue(TypeBuilder.unwrap(getType(mapValue)) instanceof BMapType);
        Assert.assertTrue(
                TypeChecker.checkIsType(TypeHelper.typeConstraint(getType(mapValue)), PredefinedTypes.TYPE_JSON));

        Assert.assertEquals(mapValue.get(StringUtils.fromString("name")).toString(), "Raja");
        TypeChecker.checkIsType(getType(mapValue.get(StringUtils.fromString("name"))), PredefinedTypes.TYPE_STRING);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("age")).toString(), "25");
        Assert.assertEquals(getType(mapValue.get(StringUtils.fromString("age"))).getTag(), TypeTags.INT_TAG);

        Assert.assertEquals(mapValue.get(StringUtils.fromString("status")).toString(), "single");
        TypeChecker.checkIsType(getType(mapValue.get(StringUtils.fromString("status"))), PredefinedTypes.TYPE_STRING);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
