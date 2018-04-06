/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.object;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.tree.PackageNode;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;

import java.util.List;

/**
 * Test cases for user defined documentation attachment in ballerina.
 */
@Test(groups = {"broken"})
public class ObjectDocumentationTest {

    @BeforeClass
    public void setup() {
    }

    @Test(description = "Test doc annotation.")
    public void testDocAnnotation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_annotation.bal");
        Assert.assertEquals(0, compileResult.getWarnCount());
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangStruct) packageNode.getStructs().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, " Documentation for Test annotation\n");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText,
                " annotation `field a` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText,
                " annotation `field b` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationText,
                " annotation `field c` documentation");
        docNodes = ((BLangAnnotation) packageNode.getAnnotations().get(0)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
    }

    @Test(description = "Test doc struct.")
    public void testDocStruct() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_doc_annotation.bal");
        Assert.assertEquals(0, compileResult.getWarnCount());
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangStruct) packageNode.getStructs().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, " Documentation for Test struct\n");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText,
                " struct `field a` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText,
                " struct `field b` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationText,
                " struct `field c` documentation");
    }

    @Test(description = "Test doc negative cases.")
    public void testDocumentationNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/object/object_annotation_negative.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 14);
        BAssertUtil.validateWarning(compileResult, 0,
                "already documented attribute 'a'", 5, 1);
        BAssertUtil.validateWarning(compileResult, 1,
                "no such documentable attribute 'c' with doc prefix 'F'", 7, 1);
        BAssertUtil.validateWarning(compileResult, 2,
                "already documented attribute 'foo'", 22, 1);
        BAssertUtil.validateWarning(compileResult, 3,
                "no such documentable attribute 'bar' with doc prefix 'F'", 23, 1);
        BAssertUtil.validateWarning(compileResult, 4,
                "already documented attribute 'a'", 31, 1);
        BAssertUtil.validateWarning(compileResult, 5,
                "no such documentable attribute 'c' with doc prefix 'F'", 33, 1);
        BAssertUtil.validateWarning(compileResult, 6,
                "already documented attribute 'file'", 45, 1);
        BAssertUtil.validateWarning(compileResult, 7,
                "no such documentable attribute 'successfuls' with doc prefix 'R'", 47, 1);
//        BAssertUtil.validateWarning(compileResult, 8,
//                "no such documentable attribute 'pa' with doc prefix 'T'", 63, 2);
//        BAssertUtil.validateWarning(compileResult, 9,
//                "already documented attribute 'e'", 65, 2);
        BAssertUtil.validateWarning(compileResult, 8,
                "already documented attribute 'url'", 89, 1);
        BAssertUtil.validateWarning(compileResult, 9,
                "no such documentable attribute 'urls' with doc prefix 'P'", 90, 1);
        /*BAssertUtil.validateWarning(compileResult, 12,
                "already documented attribute 's'", 96, 5);*//*Commented since no longer support named returns*/
        /*BAssertUtil.validateWarning(compileResult, 13,
                "no such documentable attribute 'ssss' with doc prefix 'R'", 97, 5);*/
        /*Commented since no longer support named returns*/
        BAssertUtil.validateWarning(compileResult, 10,
                "no such documentable attribute 'conn' with doc prefix 'P'", 104, 1);
        BAssertUtil.validateWarning(compileResult, 11,
                "already documented attribute 'req'", 110, 5);
        BAssertUtil.validateWarning(compileResult, 12,
                "no such documentable attribute 'reqest' with doc prefix 'P'", 111, 5);
        BAssertUtil.validateWarning(compileResult, 13,
                "no such documentable attribute 'testConstd' with doc prefix 'V'", 121, 1);
    }

}
