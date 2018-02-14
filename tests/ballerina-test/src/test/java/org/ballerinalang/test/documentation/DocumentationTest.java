/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.documentation;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.tree.PackageNode;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.util.List;

/**
 * Test cases for user defined documentation attachment in ballerina.
 */
public class DocumentationTest {

    @BeforeClass
    public void setup() {
    }

    @Test(description = "Test annotation documentation.")
    public void testDocAnnotation() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/annotation.bal");
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangAnnotation) packageNode.getAnnotations().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), " Documentation for Test annotation");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "annotation `field a` documentation");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText.toString(),
                "annotation `field b` documentation");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationText.toString(),
                "annotation `field c` documentation");
    }

    @Test(description = "Test annotation constant.")
    public void testDocConstant() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/constant.bal");
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangVariable) packageNode.getGlobalVariables().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), " Documentation for testConst constant ");
    }

    @Test(description = "Test annotation enum.")
    public void testDocEnum() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/enum.bal");
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangEnum) packageNode.getEnums().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), " Documentation for state enum");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "foo");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "enum `field foo` documentation");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "bar");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText.toString(),
                "enum `field bar` documentation");
    }

    @Test(description = "Test annotation struct.")
    public void testDocStruct() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/struct.bal");
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangStruct) packageNode.getStructs().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), " Documentation for Test struct");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "struct `field a` documentation");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText.toString(),
                "struct `field b` documentation");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationText.toString(),
                "struct `field c` documentation");
    }

    @Test(description = "Test annotation function.")
    public void testDocFunction() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/function.bal");
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangFunction) packageNode.getFunctions().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), "\n" +
                "Gets a access parameter value (`true` or `false`) for a given key. " +
                "Please note that #foo will always be bigger than #bar.\n" +
                "Example:\n" +
                "`SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);`");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "file");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "file path `C:\\users\\OddThinking\\Documents\\My Source\\Widget\\foo.src`");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "accessMode");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText.toString(),
                "read or write mode");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "successful");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationText.toString(),
                "boolean `true` or `false`");

        docNodes = ((BLangStruct) packageNode.getStructs().get(0)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), " Documentation for File struct");
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "path");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "struct `field path` documentation");

    }

    @Test(description = "Test global variable as annotation attribute value")
    public void testDocumentationNegative() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/negative.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 11);
        BAssertUtil.validateWarning(compileResult, 0,
                "already documented attribute 'a' in annotation 'Test'", 2, 40);
        BAssertUtil.validateWarning(compileResult, 1,
                "no such documentable attribute 'c' in annotation 'Test'", 4, 40);
        BAssertUtil.validateWarning(compileResult, 2,
                "invalid usage of attribute 'abc': attributes are not allowed for " +
                        "constant and global variable documentation", 12, 53);
        BAssertUtil.validateWarning(compileResult, 3,
                "already documented attribute 'foo' in enum 'state'", 17, 38);
        BAssertUtil.validateWarning(compileResult, 4,
                "no such documentable attribute 'bar' in enum 'state'", 18, 38);
        BAssertUtil.validateWarning(compileResult, 5,
                "already documented attribute 'a' in struct 'Test'", 27, 36);
        BAssertUtil.validateWarning(compileResult, 6,
                "no such documentable attribute 'c' in struct 'Test'", 29, 36);
        BAssertUtil.validateWarning(compileResult, 7,
                "already documented attribute 'file' in function 'File.open'", 42, 76);
        BAssertUtil.validateWarning(compileResult, 8,
                "no such documentable attribute 'successfuls' in function 'File.open'", 44, 33);
        BAssertUtil.validateWarning(compileResult, 9,
                "no such documentable attribute 'pa' in transformer 'Foo'", 59, 36);
        BAssertUtil.validateWarning(compileResult, 10,
                "already documented attribute 'e' in transformer 'Foo'", 61, 64);
    }

    @Test(description = "Test annotation transformer.")
    public void testDocTransformer() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/transformer.bal");
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangTransformer) packageNode.getTransformers().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), "\n" +
                " Transformer Foo Person -> Employee");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "p");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "input struct Person source used for transformation");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "e");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText.toString(),
                "output struct Employee struct which Person transformed to");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "defaultAddress");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationText.toString(),
                "address which serves Eg: `POSTCODE 112`");
    }

    @Test(description = "Test annotation service.")
    public void testDocService() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/service.bal");
        PackageNode packageNode = compileResult.getAST();
        BLangService service = (BLangService) packageNode.getServices().get(0);
        List<BLangDocumentation> docNodes = service.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), " PizzaService HTTP Service ");

        dNode = service.getResources().get(0).docAttachments.get(0);
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.documentationText.toString(), "\n" +
                "    Check orderPizza resource.");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "conn");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "HTTP connection.");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "req");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText.toString(),
                "In request.");

        dNode = service.getResources().get(1).docAttachments.get(0);
        Assert.assertEquals(dNode.documentationText.toString(), "\n" +
                "    Check status resource.");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "conn");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "HTTP connection.");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "req");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText.toString(),
                "In request.");
    }

}
