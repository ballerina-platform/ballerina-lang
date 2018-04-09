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
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;

import java.util.List;

/**
 * Test cases for user defined documentation attachment in ballerina.
 */
public class DocumentationTest {

    @BeforeClass
    public void setup() {
    }

    @Test(description = "Test doc annotation.")
    public void testDocAnnotation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/annotation.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangStruct) packageNode.getStructs().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, " Documentation for Test annotation\n");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.FIELD);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " annotation `field a` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText, " annotation `field b` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationText, " annotation `field c` documentation");
        docNodes = ((BLangAnnotation) packageNode.getAnnotations().get(0)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
    }


    @Test(description = "Test doc constant.")
    public void testDocConstant() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/constant.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangVariable) packageNode.getGlobalVariables().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, " Documentation for testConst constant\n");
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.VARIABLE);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "testConst");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " constant variable `testConst`");
    }

    @Test(description = "Test doc annotation enum.")
    public void testDocEnum() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/enum.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangTypeDefinition) packageNode.getTypeDefinitions().get(0))
                .docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, " Documentation for state enum");
        //TODO need to come up with a proper way to document an enum
//        Assert.assertEquals(dNode.getAttributes().size(), 2);
//        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.VARIABLE);
//        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "foo");
//        Assert.assertEquals(dNode.getAttributes().get(0).documentationText,
//                " enum `field foo` documentation\n");
//        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "bar");
//        Assert.assertEquals(dNode.getAttributes().get(1).documentationText,
//                " enum `field bar` documentation");
    }

    @Test(description = "Test doc struct.")
    public void testDocStruct() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/struct.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangStruct) packageNode.getStructs().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, " Documentation for Test type\n");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.FIELD);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " type `field a` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText, " type `field b` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationText, " type `field c` documentation");
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
        Assert.assertEquals(dNode.documentationText, "\n" + "Gets a access parameter value (`true` or `false`) for a " +
                "given key. " + "Please note that #foo will always be bigger than #bar.\n" + "Example:\n" +
                "``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``\n");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.RECEIVER);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "file");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " file path " +
                "``C:\\users\\OddThinking\\Documents\\My Source\\Widget\\foo.src``\n");
        Assert.assertEquals(dNode.getAttributes().get(1).docTag, DocTag.PARAM);
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "accessMode");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText, " read or write mode\n");

        docNodes = ((BLangStruct) packageNode.getStructs().get(0)).docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, " Documentation for File type\n");
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "path");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " type `field path` documentation\n");

    }

    @Test(description = "Test doc negative cases.", enabled = true)
    public void testDocumentationNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 11);
        BAssertUtil.validateWarning(compileResult, 0, "already documented attribute 'a'", 5, 1);
        BAssertUtil.validateWarning(compileResult, 1, "no such documentable attribute 'c' with doc prefix 'F'", 7, 1);
//        BAssertUtil.validateWarning(compileResult, 2,
//                "already documented attribute 'foo'", 22, 1);
//        BAssertUtil.validateWarning(compileResult, 3,
//                "no such documentable attribute 'bar' with doc prefix 'F'", 23, 1);
        BAssertUtil.validateWarning(compileResult, 2, "already documented attribute 'a'", 22, 1);
        BAssertUtil.validateWarning(compileResult, 3, "no such documentable attribute 'c' with doc prefix 'F'", 24, 1);
        BAssertUtil.validateWarning(compileResult, 4, "already documented attribute 'file'", 36, 1);
//        BAssertUtil.validateWarning(compileResult, 5,
//                "no such documentable attribute 'successfuls' with doc prefix 'R'", 38, 1);
//        BAssertUtil.validateWarning(compileResult, 8,
//                "no such documentable attribute 'pa' with doc prefix 'T'", 63, 2);
//        BAssertUtil.validateWarning(compileResult, 9,
//                "already documented attribute 'e'", 65, 2);
        BAssertUtil.validateWarning(compileResult, 5, "already documented attribute 'url'", 80, 1);
        BAssertUtil.validateWarning(compileResult, 6, "no such documentable attribute 'urls' with doc prefix 'P'",
                81, 1);
        /*BAssertUtil.validateWarning(compileResult, 12,
                "already documented attribute 's'", 96, 5);*//*Commented since no longer support named returns*/
        /*BAssertUtil.validateWarning(compileResult, 13,
                "no such documentable attribute 'ssss' with doc prefix 'R'", 97, 5);*/
        /*Commented since no longer support named returns*/
        BAssertUtil.validateWarning(compileResult, 7, "no such documentable attribute 'conn' with doc prefix 'P'",
                95, 1);
        BAssertUtil.validateWarning(compileResult, 8, "already documented attribute 'req'", 107, 5);
        BAssertUtil.validateWarning(compileResult, 9, "no such documentable attribute 'reqest' with doc prefix 'P'",
                108, 5);
        BAssertUtil.validateWarning(compileResult, 10, "no such documentable attribute 'testConstd' with doc prefix " +
                "'V'", 117, 1);
    }

    //    @Test(description = "Test doc transformer.")
    public void testDocTransformer() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/transformer.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangTransformer) packageNode.getTransformers().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, "\n" + " Transformer Foo Person -> Employee\n" + " ");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "p");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " input struct Person source used for " +
                "transformation\n ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "e");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText, " output struct Employee struct which " +
                "Person transformed to\n ");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "defaultAddress");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationText, " address which serves Eg: `POSTCODE " +
                "112`\n");
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
        Assert.assertEquals(dNode.documentationText, "PizzaService HTTP Service");

        dNode = service.getResources().get(0).docAttachments.get(0);
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.documentationText, "Check orderPizza resource. ");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "conn");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " HTTP connection. ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "req");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText, " In request.");

        dNode = service.getResources().get(1).docAttachments.get(0);
        Assert.assertEquals(dNode.documentationText, "Check status resource. ");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "conn");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " HTTP connection. ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "req");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText, " In request.");
    }

    @Test(description = "Test doc connector/function.")
    public void testDocConnectorFunction() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/connector_function.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangObject connector = (BLangObject) packageNode.getObjects().get(0);
        List<BLangDocumentation> docNodes = connector.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.documentationText, "Test Connector\n");
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.FIELD);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "url");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " url for endpoint\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "path");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText, " path for endpoint\n");

        dNode = ((BLangFunction) connector.getFunctions().get(0)).docAttachments.get(0);
        Assert.assertEquals(dNode.getAttributes().size(), 1);
        Assert.assertEquals(dNode.documentationText, "Test Connector action testAction ");
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.RETURN);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "value");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " whether successful or not");

        dNode = ((BLangFunction) connector.getFunctions().get(1)).docAttachments.get(0);
        Assert.assertEquals(dNode.documentationText, "Test Connector action testSend ");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).docTag, DocTag.PARAM);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "ep");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " endpoint url ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "value");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText, " whether successful or not");

    }

    @Test(description = "Test doc inline code.")
    public void testInlineCode() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/doc_inline.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangVariable connector = (BLangVariable) packageNode.getGlobalVariables().get(0);
        List<BLangDocumentation> docNodes = connector.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 0);
        Assert.assertEquals(dNode.documentationText, "\n" + "  Example of a string template:\n" + "    ``string s = " +
                "string `hello {{name}}`;``\n" + "\n" + "  Example for an xml literal:\n" + "    ``xml x = xml " +
                "`<{{tagName}}>hello</{{tagName}}>`;``\n");
    }

    @Test(description = "Test doc inline code with triple backtics.")
    public void testInlineCodeEnclosedTripleBackTicks() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/doc_inline_triple.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangVariable constant = (BLangVariable) packageNode.getGlobalVariables().get(0);
        List<BLangDocumentation> docNodes = constant.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 0);
        Assert.assertEquals(dNode.documentationText, "\n" + "  Example of a string template:\n" + "    ```string s = " +
                "string `hello {{name}}`;```\n" + "\n" + "  Example for an xml literal:\n" + "    ```xml x = xml " +
                "`<{{tagName}}>hello</{{tagName}}>`;```\n");
    }

    @Test(description = "Test doc multiple.")
    public void testMultiple() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/multiple.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();

        List<BLangDocumentation> docNodes = ((BLangStruct) packageNode.getStructs().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, " Documentation for Tst struct\n");
        Assert.assertEquals(dNode.getAttributes().size(), 3);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "a");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " annotation `field a` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "b");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText, " annotation `field b` documentation\n");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "c");
        Assert.assertEquals(dNode.getAttributes().get(2).documentationText, " annotation `field c` documentation");

//        docNodes = ((BLangEnum) packageNode.getEnums().get(0)).docAttachments;
//        dNode = docNodes.get(0);
//        Assert.assertNotNull(dNode);
//        Assert.assertEquals(dNode.documentationText, " Documentation for state enum\n");
//        Assert.assertEquals(dNode.getAttributes().size(), 2);
//        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "foo");
//        Assert.assertEquals(dNode.getAttributes().get(0).documentationText,
//                " enum `field foo` documentation\n");
//        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "bar");
//        Assert.assertEquals(dNode.getAttributes().get(1).documentationText,
//                " enum `field bar` documentation");

//        docNodes = ((BLangTransformer) packageNode.getTransformers().get(0)).docAttachments;
//        dNode = docNodes.get(0);
//        Assert.assertNotNull(dNode);
//        Assert.assertEquals(dNode.documentationText, "\n" +
//                " Transformer Foo Person -> Employee\n" +
//                " ");
//        Assert.assertEquals(dNode.getAttributes().size(), 3);
//        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "p");
//        Assert.assertEquals(dNode.getAttributes().get(0).documentationText,
//                " input struct Person source used for transformation\n ");
//        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "e");
//        Assert.assertEquals(dNode.getAttributes().get(1).documentationText,
//                " output struct Employee struct which Person transformed to\n ");
//        Assert.assertEquals(dNode.getAttributes().get(2).documentationField.getValue(), "defaultAddress");
//        Assert.assertEquals(dNode.getAttributes().get(2).documentationText,
//                " address which serves Eg: `POSTCODE 112`\n");

        BLangService service = (BLangService) packageNode.getServices().get(0);
        docNodes = service.docAttachments;
        dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, "PizzaService HTTP Service");

        /*
        // Commented due to https://github.com/ballerina-lang/ballerina/issues/5586 issue
        dNode = service.getResources().get(0).docAttachments.get(0);
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.documentationText, "Check orderPizza resource. ");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "conn");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText,
                " HTTP connection. ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "req");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText,
                " In request.");

        dNode = service.getResources().get(1).docAttachments.get(0);
        Assert.assertEquals(dNode.documentationText, "Check status resource. ");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "conn");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText,
                " HTTP connection. ");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "req");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText,
                " In request.");*/
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
        Assert.assertEquals(dNode.documentationText, "\n" + "  This function is deprecated use `openFile(string " +
                "accessMode){}` instead.\n");

        dNodes = ((BLangStruct) packageNode.getStructs().stream().filter(node -> node.getName().getValue().equals
                ("File")).findFirst().get()).deprecatedAttachments;
        dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, "\n" + "  This Struct is deprecated use `File2` instead.\n");

//        dNodes = ((BLangEnum) packageNode.getEnums().get(0)).deprecatedAttachments;
//        dNode = dNodes.get(0);
//        Assert.assertNotNull(dNode);
//        Assert.assertEquals(dNode.documentationText, "\n" +
//                "  This Enum is deprecated use `Enum2` instead.\n");

//        dNodes = ((BLangEnum) packageNode.getEnums().get(0)).deprecatedAttachments;
//        dNode = dNodes.get(0);
//        Assert.assertNotNull(dNode);
//        Assert.assertEquals(dNode.documentationText, "\n" +
//                "  This Enum is deprecated use `Enum2` instead.\n");

        dNodes = ((BLangVariable) packageNode.getGlobalVariables().get(0)).deprecatedAttachments;
        dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, "use ```const string testConst = " +
                "\"TestConstantDocumentation\";``` instead");

//        dNodes = ((BLangObject) packageNode.getObjects().get(0)).deprecatedAttachments;
//        dNode = dNodes.get(0);
//        Assert.assertNotNull(dNode);
//        Assert.assertEquals(dNode.documentationText, "\n" +
//                "  This Connector is deprecated use `Connector(string url2){}` instead.\n");
//
//        dNodes = ((BLangConnector) packageNode.getConnectors().get(0)).getActions().get(0).deprecatedAttachments;
//        dNode = dNodes.get(0);
//        Assert.assertNotNull(dNode);
//        Assert.assertEquals(dNode.documentationText, "\n" +
//                "      This action is deprecated use `Connector.test(string url2){}` instead.\n" +
//                "    ");

        dNodes = ((BLangService) packageNode.getServices().get(0)).deprecatedAttachments;
        dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, "\n" + "  This Service is deprecated use `PizzaHutService{}` " +
                "instead.\n");

        dNodes = ((BLangService) packageNode.getServices().get(0)).getResources().get(0).deprecatedAttachments;
        dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, "This Resource is deprecated use `PizzaHutService.orderFromPizza" +
                "()` instead.");

    }

    //    @Test(description = "Test doc deprecated Transformer.")
    public void testDeprecatedTransformer() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/deprecated_transformer.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        PackageNode packageNode = compileResult.getAST();
        List<BLangDeprecatedNode> dNodes = ((BLangTransformer) packageNode.getTransformers().get(0))
                .deprecatedAttachments;
        BLangDeprecatedNode dNode = dNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, "\n" + "  This Transformer is deprecated use\n" + "  " +
                "`transformer <Person p, Employee e> Bar(any defaultAddress) { e.name = p.firstName; }\n" + "  ` " +
                "instead.\n");

        List<BLangDocumentation> docNodes = ((BLangTransformer) packageNode.getTransformers().get(0)).docAttachments;
        BLangDocumentation docNode = docNodes.get(0);
        Assert.assertNotNull(docNode);
        Assert.assertEquals(docNode.documentationText, "\n Transformer Foo Person -> Employee\n ");
        Assert.assertEquals(docNode.getAttributes().size(), 3);
        Assert.assertEquals(docNode.getAttributes().get(0).documentationField.getValue(), "p");
        Assert.assertEquals(docNode.getAttributes().get(0).documentationText, " input struct Person source used for " +
                "transformation\n ");
        Assert.assertEquals(docNode.getAttributes().get(1).documentationField.getValue(), "e");
        Assert.assertEquals(docNode.getAttributes().get(1).documentationText, " output struct Employee struct which " +
                "Person transformed to\n ");
        Assert.assertEquals(docNode.getAttributes().get(2).documentationField.getValue(), "defaultAddress");
        Assert.assertEquals(docNode.getAttributes().get(2).documentationText, " address which serves Eg: `POSTCODE " +
                "112`\n");
    }

    @Test(description = "Test doc native function.")
    public void testDocNativeFunction() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/native_function.bal",
                CompilerPhase.TYPE_CHECK);
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(1, compileResult.getWarnCount());
        BAssertUtil.validateWarning(compileResult, 0, "no such documentable attribute 'successful' with doc prefix " +
                "'P'", 6, 1);
        PackageNode packageNode = compileResult.getAST();
        List<BLangDocumentation> docNodes = ((BLangFunction) packageNode.getFunctions().get(0)).docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, "\n" + "Gets a access parameter value (`true` or `false`) for a " +
                "given key. " + "Please note that #foo will always be bigger than #bar.\n" + "Example:\n" +
                "``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``\n");
        Assert.assertEquals(dNode.getAttributes().size(), 2);
        Assert.assertEquals(dNode.getAttributes().get(0).documentationField.getValue(), "accessMode");
        Assert.assertEquals(dNode.getAttributes().get(0).documentationText, " read or write mode\n");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationField.getValue(), "successful");
        Assert.assertEquals(dNode.getAttributes().get(1).documentationText, " boolean `true` or `false`\n");

    }

    @Test(description = "Test doc nested inline.")
    public void testNestedInline() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/nested_inline.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangVariable constant = (BLangVariable) packageNode.getGlobalVariables().get(0);
        List<BLangDocumentation> docNodes = constant.docAttachments;
        BLangDocumentation dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.getAttributes().size(), 0);
        Assert.assertEquals(dNode.documentationText, "\n" + "  Example of a string template:\n" + "  ``` This starts " +
                "ends triple backtick  ``string s = string `hello {{name}}`;`` " + "ends triple backtick```\n" + "\n"
                + "  Example for an xml literal:\n" + "    ``xml x = xml `<{{tagName}}>hello</{{tagName}}>`;``\n");
    }

    @Test(description = "Test doc nested inline inside deprecated tag.")
    public void testNestedInlineDeprecated() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/nested_inline_deprecated.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0, getErrorString(compileResult.getDiagnostics()));
        Assert.assertEquals(compileResult.getWarnCount(), 0, getErrorString(compileResult.getDiagnostics()));
        PackageNode packageNode = compileResult.getAST();
        BLangVariable constant = (BLangVariable) packageNode.getGlobalVariables().get(0);
        List<BLangDeprecatedNode> docNodes = constant.deprecatedAttachments;
        BLangDeprecatedNode dNode = docNodes.get(0);
        Assert.assertNotNull(dNode);
        Assert.assertEquals(dNode.documentationText, "\n" + "  Example of a string templates:\n" + "  ``` This starts" +
                " ends triple backtick  ``string s = string `hello {{name}}`;`` " + "ends triple backtick```\n" +
                "\n" + "  Example for an xml literal:\n" + "    ``xml x = xml `<{{tagName}}>hello</{{tagName}}>`;``\n");
    }

    private String getErrorString(Diagnostic[] diagnostics) {
        StringBuilder sb = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics) {
            sb.append(diagnostic + "\n");
        }
        return sb.toString();
    }

}
