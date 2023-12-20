/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.test.bala.record;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for type referencing in closed records.
 *
 * @since 0.985.0
 */
public class ClosedRecordTypeInclusionTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        compileResult = BCompileUtil.compile("test-src/record/closed_record_type_inclusion.bal");
    }

    @Test(description = "Negative tests")
    public void negativeTests() {
        CompileResult negative = BCompileUtil.compile("test-src/record/closed_record_type_inclusion_negative.bal");
        int index = 0;
        BAssertUtil.validateError(negative, index++, "incompatible types: 'PersonObj' is not a record", 28, 6);
        BAssertUtil.validateError(negative, index++, "incompatible types: 'IntOrFloat' is not a record", 35, 6);
        BAssertUtil.validateError(negative, index++, "incompatible types: 'FiniteT' is not a record", 41, 6);
        BAssertUtil.validateError(negative, index++, "only type references are allowed as type inclusions", 45, 6);
        BAssertUtil.validateError(negative, index++, "only type references are allowed as type inclusions", 46, 6);
        BAssertUtil.validateError(negative, index++, "only type references are allowed as type inclusions", 47, 6);
        BAssertUtil.validateError(negative, index++, "only type references are allowed as type inclusions", 48, 6);
        BAssertUtil.validateError(negative, index++, "only type references are allowed as type inclusions", 49, 6);
        BAssertUtil.validateError(negative, index++, "only type references are allowed as type inclusions", 50, 6);
        BAssertUtil.validateError(negative, index++, "only type references are allowed as type inclusions", 51, 6);
        BAssertUtil.validateError(negative, index++, "missing non-defaultable required record field 'gender'", 67, 18);
        BAssertUtil.validateError(negative, index++, "redeclared symbol 'name'", 72, 6);
        BAssertUtil.validateError(negative, index++, "unknown type 'Data'", 76, 6);
        BAssertUtil.validateError(negative, index++, "unknown type 'Data'", 81, 6);
        BAssertUtil.validateError(negative, index++, "cannot use type inclusion with more than one open record with " +
                "different rest descriptor types", 99, 20);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'string', found 'float'", 135, 60);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'string', found 'error'", 136, 50);
        BAssertUtil.validateError(negative, index++, "incompatible types: expected 'anydata', found 'error'", 137, 50);
        BAssertUtil.validateError(negative, index++, "cannot use type inclusion with more than one open record with " +
                "different rest descriptor types", 158, 10);
        BAssertUtil.validateError(negative, index++, "invalid cyclic type reference in '[PersonOne, PersonOne]'",
                163, 1);
        BAssertUtil.validateError(negative, index++, "invalid cyclic type reference in " +
                "'[PersonTwo, Employee, PersonTwo]'", 168, 1);
        BAssertUtil.validateError(negative, index++, "included field 'body' of type 'float' cannot " +
                "be overridden by a field of type 'Baz2': expected a subtype of 'float'", 185, 5);
        BAssertUtil.validateError(negative, index++, "included field 'body' of type 'anydata' cannot be overridden by" +
                " a field of type 'Qux': expected a subtype of 'anydata'", 207, 5);
        assertEquals(negative.getErrorCount(), index);
    }

    @Test(description = "Test case for type referencing all value-typed fields")
    public void testValRefType() {
        Object returns = BRunUtil.invoke(compileResult, "testValRefType");
        BMap foo1 = (BMap) returns;
        assertEquals(foo1.get(StringUtils.fromString("a")), 10L);
        assertEquals(foo1.get(StringUtils.fromString("b")), 23.45);
        assertEquals(foo1.get(StringUtils.fromString("s")).toString(), "hello foo");
        assertEquals(foo1.get(StringUtils.fromString("ri")), 20L);
        assertEquals(foo1.get(StringUtils.fromString("rf")), 45.6);
        assertEquals(foo1.get(StringUtils.fromString("rs")).toString(), "asdf");
        Assert.assertTrue((Boolean) foo1.get(StringUtils.fromString("rb")));
        assertEquals(foo1.get(StringUtils.fromString("cri")), 20L);
        assertEquals(foo1.get(StringUtils.fromString("cry")), 254);
        assertEquals(foo1.get(StringUtils.fromString("crf")), 12.34);
        assertEquals(foo1.get(StringUtils.fromString("crs")).toString(), "qwerty");
        Assert.assertTrue((Boolean) foo1.get(StringUtils.fromString("crb")));
        assertEquals(foo1.get(StringUtils.fromString("ry")), 255);
    }

    @Test(description = "Test case for type referencing records with complex ref types")
    public void testRefTypes() {
        Object returns = BRunUtil.invoke(compileResult, "testRefTypes");
        BMap foo2 = (BMap) returns;
        assertEquals(foo2.get(StringUtils.fromString("s")).toString(), "qwerty");
        assertEquals(foo2.get(StringUtils.fromString("i")), 10L);
        assertEquals(getType(foo2.get(StringUtils.fromString("rj"))).getTag(),
                io.ballerina.runtime.api.TypeTags.MAP_TAG);
        assertEquals(foo2.get(StringUtils.fromString("rj")).toString(),
                "{\"name\":\"apple\",\"color\":\"red\",\"price\":40}");
        assertEquals(getType(foo2.get(StringUtils.fromString("rx"))).getTag(),
                TypeTags.XML_ELEMENT_TAG);
        assertEquals(foo2.get(StringUtils.fromString("rx")).toString(), "<book>Count of Monte Cristo</book>");
        assertEquals(getType(foo2.get(StringUtils.fromString("rp"))).getTag(),
                io.ballerina.runtime.api.TypeTags.OBJECT_TYPE_TAG);
        assertEquals(((BObject) foo2.get(StringUtils.fromString("rp"))).get(StringUtils.fromString("name")).toString(),
                "John Doe");
        assertEquals(getType(foo2.get(StringUtils.fromString("ra"))).getTag(),
                io.ballerina.runtime.api.TypeTags.RECORD_TYPE_TAG);
        assertEquals(foo2.get(StringUtils.fromString("ra")).toString(), "{\"city\":\"Colombo\",\"country\":\"Sri " +
                "Lanka\"}");
        assertEquals(getType(foo2.get(StringUtils.fromString("crx"))).getTag(), TypeTags.XML_ELEMENT_TAG);
        assertEquals(foo2.get(StringUtils.fromString("crx")).toString(), "<book>Count of Monte Cristo</book>");
        assertEquals(getType(foo2.get(StringUtils.fromString("crj"))).getTag(),
                io.ballerina.runtime.api.TypeTags.MAP_TAG);
        assertEquals(foo2.get(StringUtils.fromString("crj")).toString(),
                "{\"name\":\"apple\",\"color\":\"red\",\"price\":40}");
        assertEquals(getType(foo2.get(StringUtils.fromString("rp"))).getTag(),
                io.ballerina.runtime.api.TypeTags.OBJECT_TYPE_TAG);
        assertEquals(((BObject) foo2.get(StringUtils.fromString("crp"))).get(StringUtils.fromString("name")).toString(),
                "Jane Doe");
        assertEquals(getType(foo2.get(StringUtils.fromString("cra"))).getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(foo2.get(StringUtils.fromString("cra")).toString(), "{\"city\":\"Colombo\",\"country\":\"Sri " +
                "Lanka\"}");
    }

    @Test(description = "Test case for order of resolving")
    public void testOrdering() {
        Object returns = BRunUtil.invoke(compileResult, "testOrdering");
        BMap foo3 = (BMap) returns;
        assertEquals(foo3.get(StringUtils.fromString("s")).toString(), "qwerty");
        assertEquals(foo3.get(StringUtils.fromString("ri")), 10L);
        assertEquals(foo3.get(StringUtils.fromString("rs")).toString(), "asdf");
    }

    @Test(description = "Test case for reference chains")
    public void testReferenceChains() {
        Object returns = BRunUtil.invoke(compileResult, "testReferenceChains");
        BMap foo4 = (BMap) returns;
        assertEquals(foo4.get(StringUtils.fromString("s")).toString(), "qwerty");
        assertEquals(foo4.get(StringUtils.fromString("abi")), 10L);
        assertEquals(foo4.get(StringUtils.fromString("efs")).toString(), "asdf");
        assertEquals(foo4.get(StringUtils.fromString("cdr")).toString(), "{\"abi\":123}");
    }

    @Test(description = "Test case for type referencing in BALAs")
    public void testTypeReferencingInBALAs() {
        Object returns = BRunUtil.invoke(compileResult, "testTypeReferencingInBALAs");
        BMap manager = (BMap) returns;
        assertEquals(manager.get(StringUtils.fromString("name")).toString(), "John Doe");
        assertEquals(manager.get(StringUtils.fromString("age")), 25L);
        assertEquals(manager.get(StringUtils.fromString("adr")).toString(), "{\"city\":\"Colombo\",\"country\":\"Sri " +
                "Lanka\"}");
        assertEquals(manager.get(StringUtils.fromString("company")).toString(), "WSO2");
        assertEquals(manager.get(StringUtils.fromString("dept")).toString(), "Engineering");
    }

    @Test(description = "Test case for default value initializing in type referenced fields")
    public void testDefaultValueInit() {
        Object returns = BRunUtil.invoke(compileResult, "testDefaultValueInit");
        BMap manager = (BMap) returns;
        assertEquals(manager.get(StringUtils.fromString("name")).toString(), "John Doe");
        assertEquals(manager.get(StringUtils.fromString("age")), 25L);
        assertEquals(manager.get(StringUtils.fromString("adr")).toString(), "{\"city\":\"Colombo\",\"country\":\"Sri " +
                "Lanka\"}");
        assertEquals(manager.get(StringUtils.fromString("company")).toString(), "WSO2");
        assertEquals(manager.get(StringUtils.fromString("dept")).toString(), "");
    }

    @Test(description = "Test case for default value initializing in type referenced fields from a bala")
    public void testDefaultValueInitInBALAs() {
        Object returns = BRunUtil.invoke(compileResult, "testDefaultValueInitInBALAs");
        BMap manager = (BMap) returns;
        assertEquals(manager.get(StringUtils.fromString("name")).toString(), "anonymous");
        assertEquals(manager.get(StringUtils.fromString("age")), 0L);
        assertEquals(manager.get(StringUtils.fromString("adr")).toString(), "{\"city\":\"\",\"country\":\"\"}");
        assertEquals(manager.get(StringUtils.fromString("company")).toString(), "");
        assertEquals(manager.get(StringUtils.fromString("dept")).toString(), "");
    }

    @Test(dataProvider = "FunctionList")
    public void testSimpleFunctions(String funcName) {
        BRunUtil.invoke(compileResult, funcName);
    }

    @DataProvider(name = "FunctionList")
    public Object[] testFunctions() {
        return new Object[]{
                "testRestTypeOverriding",
                "testOutOfOrderFieldOverridingFieldFromTypeInclusion",
                "testTypeInclusionWithFiniteField",
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
