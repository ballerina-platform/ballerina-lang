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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.elements.DocTag;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangDeprecatedNode;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.List;

/**
 * Test cases for user defined documentation attachment in ballerina.
 */
@Test(enabled = false)
public class DocumentationTest {

    private static final String CARRIAGE_RETURN_CHAR = "\r";
    private static final String EMPTY_STRING = "";

    @BeforeClass
    public void setup() {
    }

    @Test(description = "Test doc annotation.")
    public void testDocAnnotation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/annotation.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangTypeDefinition) packageNode.getTypeDefinitions()
                .get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertTrue(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("Documentation for Test annotation"));
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.FIELD);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertTrue(dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("annotation `field a` documentation"));
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertTrue(dNode.getAttributes().get(1).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains(" annotation `field b` documentation"));
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertTrue(dNode.getAttributes().get(2).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("annotation `field c` documentation"));
        docNodes = ((BLangAnnotation) packageNode.getAnnotations().get(0)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertTrue(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("Documentation for Test annotation"));
    }

    @Test(description = "Test doc constant.")
    public void testDocConstant() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/constant.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangSimpleVariable) packageNode.getGlobalVariables().get(0)).
                docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertTrue(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains(" Documentation for testConst constant"));
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.VARIABLE);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "testConst");
        Assert.assertTrue(dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("constant variable `testConst`"));
    }

    @Test(description = "Test doc annotation enum.")
    public void testDocEnum() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/enum.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangTypeDefinition) packageNode.getTypeDefinitions()
                .get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " Documentation for state enum");
        //TODO need to come up with a proper way to document an enum
        //        Assert.assertEquals(dNode.getAttributes().size(), 2);
        //        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.VARIABLE);
        //        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "foo");
        //        Assert.assertEquals(dNode.getAttributes().get(0).documentationText
        // .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING)," enum `field foo` documentation\n");
        //        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "bar");
        //        Assert.assertEquals(dNode.getAttributes().get(1).documentationText
        // .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING)," enum `field bar` documentation");
    }

    @Test(description = "Test doc struct.")
    public void testDocStruct() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/struct.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangTypeDefinition) packageNode.getTypeDefinitions()
                .get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertTrue(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("Documentation for Test type"));
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.FIELD);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertTrue(dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("type `field a` documentation"));
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertTrue(dNode.getAttributes().get(1).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("type `field b` documentation"));
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertTrue(dNode.getAttributes().get(2).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("type `field c` documentation"));
    }

    @Test(description = "Test doc function.")
    public void testDocFunction() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/function.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangFunction) packageNode.getFunctions().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "\n" + "Gets a access parameter value (`true` or `false`) for a " + "" + "given key. "
                        + "Please note that #foo will always be bigger than #bar.\n" + "Example:\n"
                        + "``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``\n");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.PARAM);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "accessMode");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " read or write mode\n");
        Assert.assertEquals(dNode.getAttributes().get(1).docTag, DocTag.RETURN);
        Assert.assertEquals(
                dNode.getAttributes().get(1).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " success or not\n");
        Assert.assertEquals(dNode.getAttributes().get(1).type.tag, TypeTags.BOOLEAN);

        docNodes = ((BLangTypeDefinition) packageNode.getTypeDefinitions().get(0)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " Documentation for File type\n");
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "path");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " file path. Example:" + " ``C:\\users\\OddThinking\\Documents\\My Source\\Widget\\foo.src``\n");

        // test union param
        docNodes = ((BLangFunction) packageNode.getFunctions().get(1)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.PARAM);
        Assert.assertEquals(dNode.getAttributes().get(0).type.tag, TypeTags.UNION);
        Assert.assertEquals(dNode.getAttributes().get(0).type.toString(), "string|int|float");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " value of param1\n");
        Assert.assertEquals(dNode.getAttributes().get(1).docTag, DocTag.PARAM);

        // test union return
        docNodes = ((BLangFunction) packageNode.getFunctions().get(2)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.RETURN);
        Assert.assertEquals(dNode.getAttributes().get(0).type.tag, TypeTags.UNION);
        Assert.assertEquals(dNode.getAttributes().get(0).type.toString(), "string|error");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " `string` value of the X will be " + "returned if found, else an `error` will be returned\n");

    }

    @Test(description = "Test doc negative cases.",
          enabled = true)
    public void testDocumentationNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 11);
        BAssertUtil.validateWarning(compileResult, 0, "already documented attribute 'a'", 5, 1);
        BAssertUtil.validateWarning(compileResult, 1, "no such documentable attribute 'c' with doc prefix 'F'", 7, 1);
        BAssertUtil.validateWarning(compileResult, 2, "already documented attribute 'a'", 22, 1);
        BAssertUtil.validateWarning(compileResult, 3, "no such documentable attribute 'c' with doc prefix 'F'", 24, 1);
        BAssertUtil.validateWarning(compileResult, 4, "already documented attribute 'accessMode'", 36, 1);
        BAssertUtil.validateWarning(compileResult, 5, "already documented attribute 'url'", 83, 1);
        BAssertUtil
                .validateWarning(compileResult, 6, "no such documentable attribute 'urls' with doc prefix 'P'", 84, 1);
        BAssertUtil
                .validateWarning(compileResult, 7, "no such documentable attribute 'conn' with doc prefix 'P'", 98, 1);
        BAssertUtil.validateWarning(compileResult, 8, "already documented attribute 'req'", 110, 5);
        BAssertUtil
                .validateWarning(compileResult, 9, "no such documentable attribute 'reqest' with doc prefix 'P'", 111,
                        5);
        BAssertUtil.validateWarning(compileResult, 10,
                "no such documentable attribute 'testConstd' with doc prefix " + "'V'", 120, 1);
    }

    @Test(description = "Test doc service.")
    public void testDocService() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/service.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangService service = (BLangService) packageNode.getServices().get(0);
        List<BLangDocumentation> docNodes = service.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "PizzaService HTTP Service");

        dNode = service.getResources().get(0).docAttachments.get(0);
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "Check orderPizza resource. ");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "conn");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " HTTP connection. ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "req");
        Assert.assertEquals(
                dNode.getAttributes().get(1).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " In request.");

        dNode = service.getResources().get(1).docAttachments.get(0);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "Check status resource. ");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "conn");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " HTTP connection. ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "req");
        Assert.assertEquals(
                dNode.getAttributes().get(1).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " In request.");
    }

    @Test(description = "Test doc connector/function.")
    public void testDocConnectorFunction() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/connector_function.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangTypeDefinition connector = (BLangTypeDefinition) packageNode.getTypeDefinitions().get(0);
        List<BLangDocumentation> docNodes = connector.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING), "Test Connector\n");
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.FIELD);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "url");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " url for endpoint\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "path");
        Assert.assertEquals(
                dNode.getAttributes().get(1).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " path for endpoint\n");

        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) connector.typeNode;

        dNode = objectTypeNode.getFunctions().get(0).docAttachments.get(0);
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "Test Connector action testAction ");
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.RETURN);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "value");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " whether successful or not");

        dNode = objectTypeNode.getFunctions().get(1).docAttachments.get(0);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "Test Connector action testSend ");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.PARAM);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "ep");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " endpoint url ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "value");
        Assert.assertEquals(
                dNode.getAttributes().get(1).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " whether successful or not");

    }

    @Test(description = "Test doc inline code.")
    public void testInlineCode() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/doc_inline.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangSimpleVariable connector = (BLangSimpleVariable) packageNode.getGlobalVariables().get(0);
        List<BLangDocumentation> docNodes = connector.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 0);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "\n" + "  Example of a string template:\n" + "    ``string s = " + "string `hello {{name}}`;``\n" + "\n"
                        + "  Example for an xml literal:\n" + "    ``xml x = xml "
                        + "`<{{tagName}}>hello</{{tagName}}>`;``\n");
    }

    @Test(description = "Test doc inline code with triple backtics.")
    public void testInlineCodeEnclosedTripleBackTicks() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/doc_inline_triple.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangSimpleVariable constant = (BLangSimpleVariable) packageNode.getGlobalVariables().get(0);
        List<BLangDocumentation> docNodes = constant.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 0);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "\n" + "  Example of a string template:\n" + "    ```string s = " + "string `hello {{name}}`;```\n"
                        + "\n" + "  Example for an xml literal:\n" + "    ```xml x = xml "
                        + "`<{{tagName}}>hello</{{tagName}}>`;```\n");
    }

    @Test(description = "Test doc multiple.")
    public void testMultiple() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/multiple.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();

        List<BLangDocumentation> docNodes = ((BLangTypeDefinition) packageNode.getTypeDefinitions()
                .get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " Documentation for Tst struct\n");
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

        //        docNodes = ((BLangEnum) packageNode.getEnums().get(0)).docAttachments;
        //        dNode = docNodes.get(0);
        //        Assert.assertNotNull(dNode);
        //        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING),
        //                  "Documentation for state enum\n");
        //        Assert.assertEquals(dNode.getAttributes().size(), 2);
        //        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "foo");
        //        Assert.assertEquals(dNode.getAttributes().get(0).documentationText
        //        .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING)," enum `field foo` documentation\n");
        //        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "bar");
        //        Assert.assertEquals(dNode.getAttributes().get(1).documentationText
        //        .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING)," enum `field bar` documentation");

        //        docNodes = ((BLangTransformer) packageNode.getTransformers().get(0)).docAttachments;
        //        dNode = docNodes.get(0);
        //        Assert.assertNotNull(dNode);
        //        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING), "\n" +
        //                " Transformer Foo Person -> Employee\n" + " ");
        //        Assert.assertEquals(dNode.getAttributes().size(), 3);
        //        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "p");
        //        Assert.assertEquals(dNode.getAttributes().get(0).documentationText
        //        .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING), " input struct Person source used for
        //         transformation\n ");
        //        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "e");
        //        Assert.assertEquals(dNode.getAttributes().get(1).documentationText
        //        .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING)," output struct Employee struct which Person
        //         transformed to\n ");
        //        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "defaultAddress");
        //        Assert.assertEquals(dNode.getAttributes().get(2).documentationText.
        //        replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING)," address which serves Eg: `POSTCODE 112`\n");

        BLangService service = (BLangService) packageNode.getServices().get(0);
        docNodes = service.docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "PizzaService HTTP Service");

        /*
        // Commented due to https://github.com/ballerina-lang/ballerina/issues/5586 issue
        dNode = service.getResources().get(0).docAttachments.get(0);
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.documentationText.
        replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING), "Check orderPizza resource. ");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "conn");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText
        .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING)," HTTP connection. ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "req");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText
        .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING)," In request.");

        dNode = service.getResources().get(1).docAttachments.get(0);
        Assert.assertEquals(dNode.documentationText
        .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING), "Check status resource. ");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "conn");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText
        .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING)," HTTP connection. ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "req");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText
        .replaceAll(CARRIAGE_RETURN_CHAR,EMPTY_STRING)," In request.");*/
    }

    @Test(description = "Test doc deprecated function use.")
    public void testDeprecatedFunctionUse() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/deprecate_function_use.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 1);
        BAssertUtil.validateWarning(compileResult, 0, "usage of deprecated function 'randomNumber'", 11, 12);
    }

    @Test(description = "Test doc deprecated.")
    public void testDeprecated() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/deprecated.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        PackageNode packageNode = compileResult.getAST();
        List<BLangDeprecatedNode> dNodes = ((BLangFunction) packageNode.getFunctions().get(0)).deprecatedAttachments;
        BLangDeprecatedNode dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertTrue(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("This function is deprecated " + "use `openFile(string accessMode){}` instead."));

        dNodes = ((BLangTypeDefinition) packageNode.getTypeDefinitions().stream()
                .filter(node -> node.getName().getValue().equals("File")).findFirst().get()).deprecatedAttachments;
        dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertTrue(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("This Object is deprecated use `File2` instead."));

        //        dNodes = ((BLangEnum) packageNode.getEnums().get(0)).deprecatedAttachments;
        //        dNode = dNodes.get(0);
        //        Assert.assertNotNull(dNode);
        //        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
        //                "\n" + "  This Enum is deprecated use `Enum2` instead.\n");
        //
        //        dNodes = ((BLangEnum) packageNode.getEnums().get(0)).deprecatedAttachments;
        //        dNode = dNodes.get(0);
        //        Assert.assertNotNull(dNode);
        //        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
        //                "\n" + "  This Enum is deprecated use `Enum2` instead.\n");

        dNodes = ((BLangSimpleVariable) packageNode.getGlobalVariables().get(0)).deprecatedAttachments;
        dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "use ```const string testConst = " + "\"TestConstantDocumentation\";``` instead");

        //        dNodes = ((BLangObject) packageNode.getObjects().get(0)).deprecatedAttachments;
        //        dNode = dNodes.get(0);
        //        Assert.assertNotNull(dNode);
        //        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
        //                "\n" + "  This Connector is deprecated use `Connector(string url2){}` instead.\n");
        //
        //        dNodes = ((BLangConnector) packageNode.getConnectors().get(0)).getActions().get(0).
        //                  deprecatedAttachments;
        //        dNode = dNodes.get(0);
        //        Assert.assertNotNull(dNode);
        //        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
        //        "\n" + "      This action is deprecated use `Connector.test(string url2){}` instead.\n" + "    ");

        dNodes = ((BLangService) packageNode.getServices().get(0)).deprecatedAttachments;
        dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertTrue(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING)
                .contains("This Service is deprecated " + "use `PizzaHutService{}` instead."));

        dNodes = ((BLangService) packageNode.getServices().get(0)).getResources().get(0).deprecatedAttachments;
        dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "This Resource is deprecated use `PizzaHutService.orderFromPizza" + "()` instead.");

    }

    @Test(description = "Test doc native function.")
    public void testDocNativeFunction() {
        CompileResult compileResult = BCompileUtil
                .compile("test-src/documentation/native_function.bal", CompilerPhase.TYPE_CHECK);
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(1, compileResult.getWarnCount());
        BAssertUtil.validateWarning(compileResult, 0,
                "no such documentable attribute 'successful' with doc prefix " + "'P'", 6, 1);
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangFunction) packageNode.getFunctions().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "\n" + "Gets a access parameter value (`true` or `false`) for a " + "given key. "
                        + "Please note that #foo will always be bigger than #bar.\n" + "Example:\n"
                        + "``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``\n");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "accessMode");
        Assert.assertEquals(
                dNode.getAttributes().get(0).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " read or write mode\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "successful");
        Assert.assertEquals(
                dNode.getAttributes().get(1).documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                " boolean `true` or `false`\n");

    }

    @Test(description = "Test doc nested inline.")
    public void testNestedInline() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/nested_inline.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangSimpleVariable constant = (BLangSimpleVariable) packageNode.getGlobalVariables().get(0);
        List<BLangDocumentation> docNodes = constant.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 0);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "\n" + "  Example of a string template:\n" + "  ``` This starts "
                        + "ends triple backtick  ``string s = string `hello {{name}}`;`` " + "ends triple backtick```\n"
                        + "\n" + "  Example for an xml literal:\n"
                        + "    ``xml x = xml `<{{tagName}}>hello</{{tagName}}>`;``\n");
    }

    @Test(description = "Test doc nested inline inside deprecated tag.")
    public void testNestedInlineDeprecated() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/nested_inline_deprecated.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangSimpleVariable constant = (BLangSimpleVariable) packageNode.getGlobalVariables().get(0);
        List<BLangDeprecatedNode> docNodes = constant.deprecatedAttachments;
        BLangDeprecatedNode dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "\n" + "  Example of a string templates:\n" + "  ``` This starts"
                        + " ends triple backtick  ``string s = string `hello {{name}}`;`` "
                        + "ends triple backtick```\n" + "\n" + "  Example for an xml literal:\n"
                        + "    ``xml x = xml `<{{tagName}}>hello</{{tagName}}>`;``\n");
    }

    private String getErrorString(Diagnostic[] diagnostics) {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics) {
            sb.append(diagnostic + "\n");
        }
        return sb.toString();
    }

}
