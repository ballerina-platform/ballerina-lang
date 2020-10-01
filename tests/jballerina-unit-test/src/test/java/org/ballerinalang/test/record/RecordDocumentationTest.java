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

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;

/**
 * Test cases for user defined documentation attachment in ballerina.
 */
public class RecordDocumentationTest {

    private static final String CARRIAGE_RETURN_CHAR = "\r";
    private static final String EMPTY_STRING = "";

    @BeforeClass
    public void setup() {
    }

    @Test(description = "Test doc annotation.", groups = { "disableOnOldParser" })
    public void testDocAnnotation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_annotation.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 3);
        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation dNode =
                ((BLangTypeDefinition) packageNode.getTypeDefinitions().get(0)).markdownDocumentationAttachment;
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getDocumentation().replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING), "Documentation " +
                "for Test annotation\n");
        Assert.assertEquals(dNode.getParameters().size(), 3);
        Assert.assertEquals(dNode.getParameters().get(0).getParameterName().getValue(), "a");
        Assert.assertEquals(dNode.getParameters().get(0).getParameterDocumentation().replaceAll(CARRIAGE_RETURN_CHAR,
                EMPTY_STRING), "annotation `field a` documentation");
        Assert.assertEquals(dNode.getParameters().get(1).getParameterName().getValue(), "b");
        Assert.assertEquals(dNode.getParameters().get(1).getParameterDocumentation().replaceAll(CARRIAGE_RETURN_CHAR,
                EMPTY_STRING), "annotation `field b` documentation");
        Assert.assertEquals(dNode.getParameters().get(2).getParameterName().getValue(), "c");
        Assert.assertEquals(dNode.getParameters().get(2).getParameterDocumentation().replaceAll(CARRIAGE_RETURN_CHAR,
                EMPTY_STRING), "annotation `field c` documentation");

        dNode = ((BLangAnnotation) packageNode.getAnnotations().get(0)).markdownDocumentationAttachment;
        Assert.assertNotNull(dNode);
    }

    @Test(description = "Test doc struct.", groups = { "disableOnOldParser" })
    public void testDocStruct() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_doc_annotation.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation dNode =
                ((BLangTypeDefinition) packageNode.getTypeDefinitions().get(0)).markdownDocumentationAttachment;

        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getDocumentation().replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING), "Documentation " +
                "for Test struct\n");
        Assert.assertEquals(dNode.getParameters().size(), 3);
        Assert.assertEquals(dNode.getParameters().get(0).getParameterName().getValue(), "a");
        Assert.assertEquals(dNode.getParameters().get(0).getParameterDocumentation().replaceAll(CARRIAGE_RETURN_CHAR,
                EMPTY_STRING), "struct `field a` documentation");
        Assert.assertEquals(dNode.getParameters().get(1).getParameterName().getValue(), "b");
        Assert.assertEquals(dNode.getParameters().get(1).getParameterDocumentation().replaceAll(CARRIAGE_RETURN_CHAR,
                EMPTY_STRING), "struct `field b` documentation");
        Assert.assertEquals(dNode.getParameters().get(2).getParameterName().getValue(), "c");
        Assert.assertEquals(dNode.getParameters().get(2).getParameterDocumentation().replaceAll(CARRIAGE_RETURN_CHAR,
                EMPTY_STRING), "struct `field c` documentation");
    }

    @Test(description = "Test doc negative cases.", groups = { "disableOnOldParser" }, enabled = false)
    public void testDocumentationNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/record/record_documentation_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0,
                            getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 20);
        int i = 0;
        BAssertUtil.validateWarning(compileResult, i++, "field 'a' already documented", 6, 5);
        BAssertUtil.validateWarning(compileResult, i++, "no such documentable field 'c'", 8, 5);
        BAssertUtil.validateWarning(compileResult, i++, "undocumented field 'cd'", 12, 5);
        BAssertUtil.validateWarning(compileResult, i++, "no such documentable parameter 'testConstd'", 19, 5);
        BAssertUtil.validateWarning(compileResult, i++, "field 'a' already documented", 25, 5);
        BAssertUtil.validateWarning(compileResult, i++, "no such documentable field 'c'", 27, 5);
        BAssertUtil.validateWarning(compileResult, i++, "undocumented field 'cdd'", 31, 5);
        BAssertUtil.validateWarning(compileResult, i++, "undocumented return parameter", 41, 5);
        BAssertUtil.validateWarning(compileResult, i++, "parameter 'accessMode' already documented", 46, 9);
        BAssertUtil.validateWarning(compileResult, i++, "no such documentable parameter 'successfuls'", 47, 9);
        BAssertUtil.validateWarning(compileResult, i++, "field 'url' already documented", 71, 5);
        BAssertUtil.validateWarning(compileResult, i++, "no such documentable field 'urls'", 72, 5);
        BAssertUtil.validateWarning(compileResult, i++, "no such documentable parameter 'conn'", 79, 5);
        BAssertUtil.validateWarning(compileResult, i++, "parameter 'req' already documented", 85, 9);
        BAssertUtil.validateWarning(compileResult, i++, "no such documentable parameter 'reqest'", 86, 9);
        BAssertUtil.validateWarning(compileResult, i++, "field 'abc' already documented", 96, 5);
        BAssertUtil.validateWarning(compileResult, i++, "invalid reference in documentation 'Baz' for type 'type'", 96,
                                    75);
        BAssertUtil.validateWarning(compileResult, i++, "invalid reference in documentation 'Baz' for type 'type'", 100,
                                    33);
        BAssertUtil.validateWarning(compileResult, i++, "undocumented field 'def'", 104, 5);
        BAssertUtil.validateWarning(compileResult, i, "invalid reference in documentation 'Baz' for type 'type'", 107,
                                    33);
    }

    private String getErrorString(Diagnostic[] diagnostics) {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics) {
            sb.append(diagnostic).append("\n");
        }
        return sb.toString();
    }
}
