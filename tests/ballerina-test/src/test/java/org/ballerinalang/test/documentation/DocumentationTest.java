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
package org.ballerinalang.test.documentation;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.tree.PackageNode;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangDeprecatedNode;
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
                "``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "file");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "file path ``C:\\users\\OddThinking\\Documents\\My Source\\Widget\\foo.src``");
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

    @Test(description = "Test negative cases.")
    public void testDocumentationNegative() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/negative.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 215);
        BAssertUtil.validateWarning(compileResult, 197,
                "already documented attribute 'a' in annotation 'Test'", 4, 40);
        BAssertUtil.validateWarning(compileResult, 198,
                "no such documentable attribute 'c' in annotation 'Test'", 6, 40);
        BAssertUtil.validateWarning(compileResult, 199,
                "invalid usage of attribute 'abc': attributes are not allowed for " +
                        "constant and global variable documentation", 14, 53);
        BAssertUtil.validateWarning(compileResult, 200,
                "already documented attribute 'foo' in enum 'state'", 19, 38);
        BAssertUtil.validateWarning(compileResult, 201,
                "no such documentable attribute 'bar' in enum 'state'", 20, 38);
        BAssertUtil.validateWarning(compileResult, 202,
                "already documented attribute 'a' in struct 'Test'", 29, 36);
        BAssertUtil.validateWarning(compileResult, 203,
                "no such documentable attribute 'c' in struct 'Test'", 31, 36);
        BAssertUtil.validateWarning(compileResult, 204,
                "already documented attribute 'file' in function 'File.open'", 44, 78);
        BAssertUtil.validateWarning(compileResult, 205,
                "no such documentable attribute 'successfuls' in function 'File.open'", 46, 33);
        BAssertUtil.validateWarning(compileResult, 206,
                "no such documentable attribute 'pa' in transformer 'Foo'", 61, 36);
        BAssertUtil.validateWarning(compileResult, 207,
                "already documented attribute 'e' in transformer 'Foo'", 63, 64);
        BAssertUtil.validateWarning(compileResult, 208,
                "already documented attribute 's' in action 'testAction'", 93, 43);
        BAssertUtil.validateWarning(compileResult, 209,
                "no such documentable attribute 'ssss' in action 'testAction'", 94, 43);
        BAssertUtil.validateWarning(compileResult, 210,
                "already documented attribute 'url' in connector 'TestConnector'", 87, 24);
        BAssertUtil.validateWarning(compileResult, 211,
                "no such documentable attribute 'urls' in connector 'TestConnector'", 88, 24);
        BAssertUtil.validateWarning(compileResult, 212,
                "already documented attribute 'req' in resource 'orderPizza'", 109, 23);
        BAssertUtil.validateWarning(compileResult, 213,
                "no such documentable attribute 'reqest' in resource 'orderPizza'", 110, 23);
        BAssertUtil.validateWarning(compileResult, 214,
                "no such documentable attribute 'conn' in service 'PizzaService'", 102, 42);
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

    @Test(description = "Test annotation connector.")
    public void testDocConnector() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/connector.bal");
        PackageNode packageNode = compileResult.getAST();
        BLangConnector connector = (BLangConnector) packageNode.getConnectors().get(0);
        List<BLangDocumentation> docNodes = connector.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.documentationText.toString(), "\n" +
                "Test Connector");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "url");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "url for endpoint");

        dNode = connector.getActions().get(0).docAttachments.get(0);
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.documentationText.toString(),
                "Test Connector action testAction");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "s");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "which represent successful or not");

        dNode = connector.getActions().get(1).docAttachments.get(0);
        Assert.assertEquals(dNode.documentationText.toString(),
                "Test Connector action testSend");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "ep");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "which represent successful or not");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "s");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText.toString(),
                "which represent successful or not");
    }

    @Test(description = "Test annotation connector/function.")
    public void testDocConnectorFunction() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/connector_function.bal");
        PackageNode packageNode = compileResult.getAST();
        BLangConnector connector = (BLangConnector) packageNode.getConnectors().get(0);
        List<BLangDocumentation> docNodes = connector.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.documentationText.toString(), "\n" +
                "Test Connector");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "url");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "url for endpoint");

        dNode = connector.getActions().get(0).docAttachments.get(0);
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.documentationText.toString(),
                "Test Connector action testAction");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "s");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "which represent successful or not");

        dNode = connector.getActions().get(1).docAttachments.get(0);
        Assert.assertEquals(dNode.documentationText.toString(),
                "Test Connector action testSend");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "ep");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "which represent successful or not");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "s");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText.toString(),
                "which represent successful or not");

        docNodes = ((BLangFunction) packageNode.getFunctions().get(0)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), "\n" +
                "Gets a access parameter value (`true` or `false`) for a given key. " +
                "Please note that #foo will always be bigger than #bar.\n" +
                "Example:\n" +
                "``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "file");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "file path ``C:\\users\\OddThinking\\Documents\\My Source\\Widget\\foo.src``");
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

    @Test(description = "Test annotation inline code.")
    public void testInlineCode() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/doc_inline.bal");
        PackageNode packageNode = compileResult.getAST();
        BLangVariable connector = (BLangVariable) packageNode.getGlobalVariables().get(0);
        List<BLangDocumentation> docNodes = connector.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 0);
        Assert.assertEquals(dNode.documentationText.toString(), "\n" +
                "  Example of a string template:\n" +
                "    ``string s = string `hello {{name}}`;``\n" +
                "\n" +
                "  Example for an xml literal:\n" +
                "    ``xml x = xml `<{{tagName}}>hello</{{tagName}}>`;``\n");
    }

    @Test(description = "Test annotation multiple.")
    public void testMultiple() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/multiple.bal");
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

        docNodes = ((BLangEnum) packageNode.getEnums().get(0)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), " Documentation for state enum");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "foo");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText.toString(),
                "enum `field foo` documentation");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "bar");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText.toString(),
                "enum `field bar` documentation");

        docNodes = ((BLangTransformer) packageNode.getTransformers().get(0)).docAttachments;
        dNode = docNodes.get(0);
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

        BLangService service = (BLangService) packageNode.getServices().get(0);
        docNodes = service.docAttachments;
        dNode = docNodes.get(0);
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

    @Test(description = "Test annotation deprecated.")
    public void testDeprecated() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src", "documentation/deprecated.bal");
        PackageNode packageNode = compileResult.getAST();
        List<BLangDeprecatedNode> dNodes = ((BLangFunction) packageNode.getFunctions().get(0)).deprecatedAttachments;
        BLangDeprecatedNode dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), "\n" +
                "  This function is deprecated `openFile(string accessMode){}` instead.\n");

        dNodes = ((BLangStruct) packageNode.getStructs().get(0)).deprecatedAttachments;
        dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.toString(), "\n" +
                "  This Struct is deprecated `File2` instead.\n");
    }

    @Test(description = "Test annotation deprecated function use.")
    public void testDeprecatedFunctionUse() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src",
                "documentation/deprecate_function_use.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 73);
        BAssertUtil.validateWarning(compileResult, 72,
                "usage of deprecated function 'randomNumber'", 10, 12);
    }

}
