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

package org.ballerinalang.test.balo.record;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import static org.testng.Assert.assertEquals;

/**
 * Test cases for type referencing in closed records.
 *
 * @since 0.985.0
 */
public class ClosedRecordTypeReferenceTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "records");
        compileResult = BCompileUtil.compile("test-src/record/closed_record_type_reference.bal");
    }

    @Test(description = "Negative tests" , groups = {"disableOnOldParser"})
    public void negativeTests() {
        CompileResult negative = BCompileUtil.compile("test-src/record/closed_record_type_reference_negative.bal");
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
        assertEquals(negative.getErrorCount(), index);
    }

    @Test(description = "Test case for type referencing all value-typed fields")
    public void testValRefType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testValRefType");
        BMap foo1 = (BMap) returns[0];
        assertEquals(((BInteger) foo1.get("a")).intValue(), 10);
        assertEquals(((BFloat) foo1.get("b")).floatValue(), 23.45);
        assertEquals(foo1.get("s").stringValue(), "hello foo");
        assertEquals(((BInteger) foo1.get("ri")).intValue(), 20);
        assertEquals(((BFloat) foo1.get("rf")).floatValue(), 45.6);
        assertEquals(foo1.get("rs").stringValue(), "asdf");
        Assert.assertTrue(((BBoolean) foo1.get("rb")).booleanValue());
        assertEquals(((BInteger) foo1.get("cri")).intValue(), 20);
        assertEquals(((BByte) foo1.get("cry")).intValue(), 254);
        assertEquals(((BFloat) foo1.get("crf")).floatValue(), 12.34);
        assertEquals(foo1.get("crs").stringValue(), "qwerty");
        Assert.assertTrue(((BBoolean) foo1.get("crb")).booleanValue());
        assertEquals(((BByte) foo1.get("ry")).intValue(), 255);
    }

    @Test(description = "Test case for type referencing records with complex ref types")
    public void testRefTypes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRefTypes");
        BMap foo2 = (BMap) returns[0];
        assertEquals(foo2.get("s").stringValue(), "qwerty");
        assertEquals(((BInteger) foo2.get("i")).intValue(), 10);
        assertEquals(foo2.get("rj").getType().getTag(), TypeTags.MAP);
        assertEquals(foo2.get("rj").stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
        assertEquals(foo2.get("rx").getType().getTag(), TypeTags.XML);
        assertEquals(foo2.get("rx").stringValue(), "<book>Count of Monte Cristo</book>");
        assertEquals(foo2.get("rp").getType().getTag(), TypeTags.OBJECT);
        assertEquals(((BMap) foo2.get("rp")).get("name").stringValue(), "John Doe");
        assertEquals(foo2.get("ra").getType().getTag(), TypeTags.RECORD);
        assertEquals(foo2.get("ra").stringValue(), "{city:\"Colombo\", country:\"Sri Lanka\"}");
        assertEquals(foo2.get("crx").getType().getTag(), TypeTags.XML);
        assertEquals(foo2.get("crx").stringValue(), "<book>Count of Monte Cristo</book>");
        assertEquals(foo2.get("crj").getType().getTag(), TypeTags.MAP);
        assertEquals(foo2.get("crj").stringValue(), "{\"name\":\"apple\", \"color\":\"red\", \"price\":40}");
        assertEquals(foo2.get("rp").getType().getTag(), TypeTags.OBJECT);
        assertEquals(((BMap) foo2.get("crp")).get("name").stringValue(), "Jane Doe");
        assertEquals(foo2.get("cra").getType().getTag(), TypeTags.RECORD);
        assertEquals(foo2.get("cra").stringValue(), "{city:\"Colombo\", country:\"Sri Lanka\"}");
    }

    @Test(description = "Test case for order of resolving")
    public void testOrdering() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testOrdering");
        BMap foo3 = (BMap) returns[0];
        assertEquals(foo3.get("s").stringValue(), "qwerty");
        assertEquals(((BInteger) foo3.get("ri")).intValue(), 10);
        assertEquals(foo3.get("rs").stringValue(), "asdf");
    }

    @Test(description = "Test case for reference chains")
    public void testReferenceChains() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReferenceChains");
        BMap foo4 = (BMap) returns[0];
        assertEquals(foo4.get("s").stringValue(), "qwerty");
        assertEquals(((BInteger) foo4.get("abi")).intValue(), 10);
        assertEquals(foo4.get("efs").stringValue(), "asdf");
        assertEquals(foo4.get("cdr").stringValue(), "{abi:123}");
    }

    @Test(description = "Test case for type referencing in BALOs")
    public void testTypeReferencingInBALOs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTypeReferencingInBALOs");
        BMap manager = (BMap) returns[0];
        assertEquals(manager.get("name").stringValue(), "John Doe");
        assertEquals(((BInteger) manager.get("age")).intValue(), 25);
        assertEquals(manager.get("adr").stringValue(), "{city:\"Colombo\", country:\"Sri Lanka\"}");
        assertEquals(manager.get("company").stringValue(), "WSO2");
        assertEquals(manager.get("dept").stringValue(), "Engineering");
    }

    @Test(description = "Test case for default value initializing in type referenced fields")
    public void testDefaultValueInit() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDefaultValueInit");
        BMap manager = (BMap) returns[0];
        assertEquals(manager.get("name").stringValue(), "John Doe");
        assertEquals(((BInteger) manager.get("age")).intValue(), 25);
        assertEquals(manager.get("adr").stringValue(), "{city:\"Colombo\", country:\"Sri Lanka\"}");
        assertEquals(manager.get("company").stringValue(), "WSO2");
        assertEquals(manager.get("dept").stringValue(), "");
    }

    @Test(description = "Test case for default value initializing in type referenced fields from a BALO")
    public void testDefaultValueInitInBALOs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDefaultValueInitInBALOs");
        BMap manager = (BMap) returns[0];
        assertEquals(manager.get("name").stringValue(), "anonymous");
        assertEquals(((BInteger) manager.get("age")).intValue(), 0);
        assertEquals(manager.get("adr").stringValue(), "{city:\"\", country:\"\"}");
        assertEquals(manager.get("company").stringValue(), "");
        assertEquals(manager.get("dept").stringValue(), "");
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg", "foo");
    }
}
