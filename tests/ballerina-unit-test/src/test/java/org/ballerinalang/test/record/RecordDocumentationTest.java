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
package org.ballerinalang.test.record;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;

import java.util.List;

/**
 * Test cases for user defined documentation attachment in ballerina.
 */
public class RecordDocumentationTest {

    private static final String CARRIAGE_RETURN_CHAR = "\r";
    private static final String EMPTY_STRING = "";

    @BeforeClass
    public void setup() {
    }

    @Test(description = "Test doc annotation.")
    public void testDocAnnotation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_annotation.bal");
        Assert.assertEquals(0, compileResult.getWarnCount());
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangTypeDefinition) packageNode.getTypeDefinitions()
                .get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " Documentation for Test annotation\n");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " annotation `field a` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertEquals(
                dNode.getAttributes().get(1).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " annotation `field b` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertEquals(
                dNode.getAttributes().get(2).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " annotation `field c` documentation");
        docNodes = ((BLangAnnotation) packageNode.getAnnotations().get(0)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
    }

    @Test(description = "Test doc struct.")
    public void testDocStruct() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_doc_annotation.bal");
        Assert.assertEquals(0, compileResult.getWarnCount());
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangTypeDefinition) packageNode.getTypeDefinitions()
                .get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " Documentation for Test struct\n");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " struct `field a` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertEquals(
                dNode.getAttributes().get(1).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " struct `field b` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertEquals(
                dNode.getAttributes().get(2).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " struct `field c` documentation");
    }

    @Test(description = "Test doc negative cases.")
    public void testDocumentationNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_annotation_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 11);
        BAssertUtil.validateWarning(compileResult, 0, "already documented attribute 'a'", 5, 1);
        BAssertUtil.validateWarning(compileResult, 1, "no such documentable attribute 'c' with doc prefix 'F'", 7, 1);
        BAssertUtil.validateWarning(compileResult, 2, "no such documentable attribute 'testConstd' with doc prefix 'V'",
                17, 1);
        BAssertUtil.validateWarning(compileResult, 3, "already documented attribute 'a'", 31, 1);
        BAssertUtil.validateWarning(compileResult, 4, "no such documentable attribute 'c' with doc prefix 'F'", 33, 1);
        BAssertUtil.validateWarning(compileResult, 5, "already documented attribute 'accessMode'", 45, 1);
        BAssertUtil.validateWarning(compileResult, 6, "already documented attribute 'url'", 92, 1);
        BAssertUtil
                .validateWarning(compileResult, 7, "no such documentable attribute 'urls' with doc prefix 'P'", 93, 1);
        BAssertUtil
                .validateWarning(compileResult, 8, "no such documentable attribute 'conn' with doc prefix 'P'", 107, 1);
        BAssertUtil.validateWarning(compileResult, 9, "already documented attribute 'req'", 113, 5);
        BAssertUtil
                .validateWarning(compileResult, 10, "no such documentable attribute 'reqest' with doc prefix 'P'", 114,
                        5);
    }

    private String getErrorString(Diagnostic[] diagnostics) {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics) {
            sb.append(diagnostic + "\n");
        }
        return sb.toString();
    }

}
