/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.documentation;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.ClassDefinition;
import org.ballerinalang.model.tree.DocumentationReferenceType;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.LinkedList;
import java.util.List;

/**
 * Test cases for markdown documentations.
 */
public class MarkdownDocumentationTest {

    @Test(description = "Test doc annotation")
    public void testDocAnnotation() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_annotation.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 0);

        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation documentationAttachment =
                packageNode.getTypeDefinitions().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Documentation for Test annotation");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 3);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "a");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "'field a' documentation");
        Assert.assertEquals(parameters.get(1).getParameterName().getValue(), "b");
        Assert.assertEquals(parameters.get(1).getParameterDocumentation(), "'field b' documentation");
        Assert.assertEquals(parameters.get(2).getParameterName().getValue(), "c");
        Assert.assertEquals(parameters.get(2).getParameterDocumentation(), "'field c' documentation");
    }

    @Test(description = "Test doc constant")
    public void testDocConstant() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_constant.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 8);

        PackageNode packageNode = compileResult.getAST();

        SimpleVariableNode variableNode = packageNode.getGlobalVariables().get(1);
        Assert.assertNotNull(variableNode);
        BLangMarkdownDocumentation documentationAttachment = variableNode.getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Documentation for final global variable.");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 0);

        BLangMarkdownReturnParameterDocumentation returnParameter = documentationAttachment.getReturnParameter();
        Assert.assertNull(returnParameter);

        variableNode = packageNode.getGlobalVariables().get(2);
        Assert.assertNotNull(variableNode);
        documentationAttachment = variableNode.getMarkdownDocumentationAttachment();
        Assert.assertNull(documentationAttachment);

        BLangConstant constantNode = (BLangConstant) packageNode.getConstants().get(0);
        Assert.assertNotNull(constantNode);
        documentationAttachment = constantNode.getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Documentation for constant with type.");

        constantNode = (BLangConstant) packageNode.getConstants().get(1);
        Assert.assertNotNull(constantNode);
        documentationAttachment = constantNode.getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Documentation for constant without type.");

        constantNode = (BLangConstant) packageNode.getConstants().get(2);
        Assert.assertNotNull(constantNode);
        documentationAttachment = constantNode.getMarkdownDocumentationAttachment();

        LinkedList<BLangMarkdownReferenceDocumentation> references = documentationAttachment.getReferences();
        Assert.assertEquals(references.size(), 8);

        Assert.assertEquals(references.get(0).type, DocumentationReferenceType.TYPE);
        Assert.assertEquals(references.get(0).referenceName, "typeDef");

        Assert.assertEquals(references.get(1).type, DocumentationReferenceType.SERVICE);
        Assert.assertEquals(references.get(1).referenceName, "helloWorld");

        Assert.assertEquals(references.get(2).type, DocumentationReferenceType.VARIABLE);
        Assert.assertEquals(references.get(2).referenceName, "testVar");

        Assert.assertEquals(references.get(3).type, DocumentationReferenceType.VAR);
        Assert.assertEquals(references.get(3).referenceName, "testVar");

        Assert.assertEquals(references.get(4).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(4).referenceName, "add");

        Assert.assertEquals(references.get(5).type, DocumentationReferenceType.PARAMETER);
        Assert.assertEquals(references.get(5).referenceName, "x");

        Assert.assertEquals(references.get(6).type, DocumentationReferenceType.CONST);
        Assert.assertEquals(references.get(6).referenceName, "constant");

        Assert.assertEquals(references.get(7).type, DocumentationReferenceType.ANNOTATION);
        Assert.assertEquals(references.get(7).referenceName, "annot");
    }

    @Test(description = "Test doc finite types")
    public void testDocFiniteTypes() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_finite_types.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 0);

        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation documentationAttachment =
                packageNode.getTypeDefinitions().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Documentation for state enum");

        // Todo - need to come up with a proper way to document finite types

        List<? extends SimpleVariableNode> globalVariables = packageNode.getGlobalVariables();

        SimpleVariableNode variableNode = globalVariables.get(1);
        Assert.assertNotNull(variableNode);
        documentationAttachment = variableNode.getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 0);
        BLangMarkdownReturnParameterDocumentation returnParameter = documentationAttachment.getReturnParameter();
        Assert.assertNull(returnParameter);

        variableNode = globalVariables.get(2);
        Assert.assertNotNull(variableNode);
        documentationAttachment = variableNode.getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 0);
        returnParameter = documentationAttachment.getReturnParameter();
        Assert.assertNull(returnParameter);
    }

    @Test(description = "Test doc type", groups = { "disableOnOldParser" })
    public void testDocType() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_type.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 11);

        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation documentationAttachment =
                packageNode.getTypeDefinitions().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Documentation for Test type");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 3);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "a");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "`field a` documentation");
        Assert.assertEquals(parameters.get(1).getParameterName().getValue(), "b");
        Assert.assertEquals(parameters.get(1).getParameterDocumentation(), "`field b` documentation");
        Assert.assertEquals(parameters.get(2).getParameterName().getValue(), "c");
        Assert.assertEquals(parameters.get(2).getParameterDocumentation(), "`field c` documentation");

        documentationAttachment = packageNode.getTypeDefinitions().get(1).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);

        LinkedList<BLangMarkdownReferenceDocumentation> references = documentationAttachment.getReferences();
        Assert.assertEquals(references.size(), 8);

        Assert.assertEquals(references.get(0).type, DocumentationReferenceType.TYPE);
        Assert.assertEquals(references.get(0).referenceName, "typeDef");

        Assert.assertEquals(references.get(1).type, DocumentationReferenceType.SERVICE);
        Assert.assertEquals(references.get(1).referenceName, "helloWorld");

        Assert.assertEquals(references.get(2).type, DocumentationReferenceType.VARIABLE);
        Assert.assertEquals(references.get(2).referenceName, "testVar");

        Assert.assertEquals(references.get(3).type, DocumentationReferenceType.VAR);
        Assert.assertEquals(references.get(3).referenceName, "testVar");

        Assert.assertEquals(references.get(4).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(4).referenceName, "add");

        Assert.assertEquals(references.get(5).type, DocumentationReferenceType.PARAMETER);
        Assert.assertEquals(references.get(5).referenceName, "x");

        Assert.assertEquals(references.get(6).type, DocumentationReferenceType.CONST);
        Assert.assertEquals(references.get(6).referenceName, "constant");

        Assert.assertEquals(references.get(7).type, DocumentationReferenceType.ANNOTATION);
        Assert.assertEquals(references.get(7).referenceName, "annot");
    }

    @Test(description = "Test doc function", groups = { "disableOnOldParser" })
    public void testDocFunction() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_function.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 6);

        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation documentationAttachment =
                packageNode.getFunctions().get(5).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Gets a access parameter value (`true` or " +
                "`false`) for a given key. Please note that #foo will always be bigger than #bar.\n" +
                "Example:\n" +
                "`SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);`");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 1);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "accessMode");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "read or write mode");

        BLangMarkdownReturnParameterDocumentation returnParameter = documentationAttachment.getReturnParameter();
        Assert.assertNotNull(returnParameter);
        Assert.assertEquals(returnParameter.getReturnParameterDocumentation(), "success or not");

        documentationAttachment = packageNode.getClassDefinitions().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Documentation for File type");

        parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 1);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "path");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "file path. Example:" +
                " `C:\\users\\OddThinking\\Documents\\My Source\\Widget\\foo.src`");

        // Test union param.
        documentationAttachment = packageNode.getFunctions().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);

        parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 3);

        Assert.assertEquals(parameters.get(0).getSymbol().getType().tag, TypeTags.UNION);
        Assert.assertEquals(parameters.get(0).getSymbol().getType().toString(), "(string|int|float)");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "value of param1");

        Assert.assertEquals(parameters.get(1).getSymbol().getType().tag, TypeTags.INT);
        Assert.assertEquals(parameters.get(1).getSymbol().getType().toString(), "int");
        Assert.assertEquals(parameters.get(1).getParameterDocumentation(), "value of param2");

        Assert.assertEquals(parameters.get(2).getSymbol().getType().tag, TypeTags.ARRAY);
        Assert.assertEquals(parameters.get(2).getSymbol().getType().toString(), "string[]");
        Assert.assertEquals(parameters.get(2).getParameterDocumentation(), "value of rest param");

        // Test union return.
        documentationAttachment = packageNode.getFunctions().get(1).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);

        returnParameter = documentationAttachment.getReturnParameter();
        Assert.assertNotNull(returnParameter);

        Assert.assertEquals(returnParameter.getReturnType().tag, TypeTags.UNION);
        Assert.assertEquals(returnParameter.getReturnType().toString(), "(string|error)");
        Assert.assertEquals(returnParameter.getReturnParameterDocumentation(), "`string` value of the X will be " +
                "returned if found, else an `error` will be returned");

        documentationAttachment = packageNode.getFunctions().get(4).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);

        LinkedList<BLangMarkdownReferenceDocumentation> references = documentationAttachment.getReferences();
        Assert.assertEquals(references.size(), 8);

        Assert.assertEquals(references.get(0).type, DocumentationReferenceType.TYPE);
        Assert.assertEquals(references.get(0).referenceName, "typeDef");

        Assert.assertEquals(references.get(1).type, DocumentationReferenceType.SERVICE);
        Assert.assertEquals(references.get(1).referenceName, "helloWorld");

        Assert.assertEquals(references.get(2).type, DocumentationReferenceType.VARIABLE);
        Assert.assertEquals(references.get(2).referenceName, "testVar");

        Assert.assertEquals(references.get(3).type, DocumentationReferenceType.VAR);
        Assert.assertEquals(references.get(3).referenceName, "testVar");

        Assert.assertEquals(references.get(4).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(4).referenceName, "add");

        Assert.assertEquals(references.get(5).type, DocumentationReferenceType.PARAMETER);
        Assert.assertEquals(references.get(5).referenceName, "x");

        Assert.assertEquals(references.get(6).type, DocumentationReferenceType.CONST);
        Assert.assertEquals(references.get(6).referenceName, "constant");

        Assert.assertEquals(references.get(7).type, DocumentationReferenceType.ANNOTATION);
        Assert.assertEquals(references.get(7).referenceName, "annot");

    }

    @Test(description = "Test doc function with function keyword", groups = { "disableOnOldParser" })
    public void testDocFunctionSpecial() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_function_special.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 3);

        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation documentationAttachment =
                packageNode.getFunctions().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);

        LinkedList<BLangMarkdownReferenceDocumentation> references = documentationAttachment.getReferences();
        Assert.assertEquals(references.size(), 6);

        Assert.assertEquals(references.get(0).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(0).referenceName, "foo");
        Assert.assertEquals(references.get(0).identifier, "foo");

        Assert.assertEquals(references.get(1).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(1).referenceName, "foo()");
        Assert.assertEquals(references.get(1).identifier, "foo");

        Assert.assertEquals(references.get(2).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(2).referenceName, "bar.baz()");
        Assert.assertEquals(references.get(2).typeName, "bar");
        Assert.assertEquals(references.get(2).identifier, "baz");

        Assert.assertEquals(references.get(3).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(3).referenceName, "m:foo");
        Assert.assertEquals(references.get(3).qualifier, "m");
        Assert.assertEquals(references.get(3).identifier, "foo");

        Assert.assertEquals(references.get(4).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(4).referenceName, "m:foo()");
        Assert.assertEquals(references.get(4).qualifier, "m");
        Assert.assertEquals(references.get(4).identifier, "foo");

        Assert.assertEquals(references.get(5).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(5).referenceName, "m:bar.baz()");
        Assert.assertEquals(references.get(5).qualifier, "m");
        Assert.assertEquals(references.get(5).typeName, "bar");
        Assert.assertEquals(references.get(5).identifier, "baz");
    }

    @Test(description = "Test doc negative cases.", groups = { "disableOnOldParser" }, enabled = false)
    public void testDocumentationNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 37);

        int index = 0;

        BAssertUtil.validateWarning(compileResult, index++,
                "invalid identifier in documentation reference '9invalidFunc'", 4, 13);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid reference in documentation 'invalidFunc' for type 'function'", 5, 3);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid usage of parameter reference outside of function definition 'invalidParameter'", 6, 3);
        BAssertUtil.validateWarning(compileResult, index++, "field 'a' already documented", 8, 5);
        BAssertUtil.validateWarning(compileResult, index++, "no such documentable field 'c'", 10, 5);
        BAssertUtil.validateWarning(compileResult, index++, "no documentable return parameter", 11, 1);
        BAssertUtil.validateWarning(compileResult, index++, "undocumented field 'cd'", 15, 5);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid identifier in documentation reference '9invalidServ'", 21, 12);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid reference in documentation 'invalidServ' for type 'service'", 22, 3);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid usage of parameter reference outside of function definition 'invalidParameter'", 23, 3);
        BAssertUtil.validateWarning(compileResult, index++, "field 'a' already documented", 28, 5);
        BAssertUtil.validateWarning(compileResult, index++, "no such documentable field 'c'", 30, 5);
        BAssertUtil.validateWarning(compileResult, index++, "undocumented field 'cdd'", 34, 5);
        BAssertUtil.validateWarning(compileResult, index++, "field 'path' already documented", 39, 5);
        BAssertUtil.validateWarning(compileResult, index++, "no such documentable field 'path2'", 40, 5);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid identifier in documentation reference '9invalidConst'", 41, 10);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid reference in documentation 'invalidConst' for type 'const'", 42, 3);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid usage of parameter reference outside of function definition 'invalidParameter'", 43, 3);
        BAssertUtil.validateWarning(compileResult, index++, "undocumented field 'path3'", 46, 5);
        BAssertUtil.validateWarning(compileResult, index++, "undocumented return parameter", 48, 5);
        BAssertUtil.validateWarning(compileResult, index++, "parameter 'accessMode' already documented", 52, 9);
        BAssertUtil.validateWarning(compileResult, index++, "no such documentable parameter 'successful'", 53, 9);
        BAssertUtil.validateWarning(compileResult, index++, "field 'url' already documented", 76, 5);
        BAssertUtil.validateWarning(compileResult, index++, "no such documentable field 'urls'", 77, 5);
        BAssertUtil.validateWarning(compileResult, index++, "undocumented field 'url2'", 80, 3);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid identifier in documentation reference '9invalidConst'", 85, 10);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid reference in documentation 'invalidConst' for type 'const'", 86, 3);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid usage of parameter reference outside of function definition 'invalidParameter'", 87, 3);
        BAssertUtil.validateWarning(compileResult, index++, "no such documentable parameter 'conn'", 88, 5);
        BAssertUtil.validateWarning(compileResult, index++, "no documentable return parameter", 89, 1);
        BAssertUtil.validateWarning(compileResult, index++, "parameter 'req' already documented", 97, 9);
        BAssertUtil.validateWarning(compileResult, index++, "no such documentable parameter 'reqest'", 98, 9);
        BAssertUtil.validateWarning(compileResult, index++, "no such documentable parameter 'testConstd'", 109, 5);
        BAssertUtil.validateWarning(compileResult, index++, "no documentable return parameter", 110, 1);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid identifier in documentation reference '9function'", 115, 13);
        BAssertUtil.validateWarning(compileResult, index++,
                "invalid reference in documentation 'filePath1' for type 'parameter'", 116, 3);
        BAssertUtil.validateWarning(compileResult, index, "undocumented parameter 'filePath'", 117, 22);
    }

    @Test(description = "Test doc service", enabled = false)
    public void testDocService() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_service.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 8);

        PackageNode packageNode = compileResult.getAST();
        ServiceNode serviceNode = packageNode.getServices().get(0);
        BLangMarkdownDocumentation documentationAttachment = serviceNode.getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "PizzaService HTTP Service");

        documentationAttachment = serviceNode.getResources().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Check orderPizza resource.");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 2);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "conn");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "HTTP connection.");
        Assert.assertEquals(parameters.get(1).getParameterName().getValue(), "req");
        Assert.assertEquals(parameters.get(1).getParameterDocumentation(), "In request.");

        documentationAttachment = serviceNode.getResources().get(1).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Check status resource.");

        parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 2);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "conn");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "HTTP connection.");
        Assert.assertEquals(parameters.get(1).getParameterName().getValue(), "req");
        Assert.assertEquals(parameters.get(1).getParameterDocumentation(), "In request.");

        documentationAttachment = packageNode.getServices().get(1).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);

        LinkedList<BLangMarkdownReferenceDocumentation> references = documentationAttachment.getReferences();
        Assert.assertEquals(references.size(), 8);

        Assert.assertEquals(references.get(0).type, DocumentationReferenceType.TYPE);
        Assert.assertEquals(references.get(0).referenceName, "typeDef");

        Assert.assertEquals(references.get(1).type, DocumentationReferenceType.SERVICE);
        Assert.assertEquals(references.get(1).referenceName, "helloWorld");

        Assert.assertEquals(references.get(2).type, DocumentationReferenceType.VARIABLE);
        Assert.assertEquals(references.get(2).referenceName, "testVar");

        Assert.assertEquals(references.get(3).type, DocumentationReferenceType.VAR);
        Assert.assertEquals(references.get(3).referenceName, "testVar");

        Assert.assertEquals(references.get(4).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(4).referenceName, "add");

        Assert.assertEquals(references.get(5).type, DocumentationReferenceType.PARAMETER);
        Assert.assertEquals(references.get(5).referenceName, "x");

        Assert.assertEquals(references.get(6).type, DocumentationReferenceType.CONST);
        Assert.assertEquals(references.get(6).referenceName, "constant");

        Assert.assertEquals(references.get(7).type, DocumentationReferenceType.ANNOTATION);
        Assert.assertEquals(references.get(7).referenceName, "annot");
    }

    @Test(description = "Test doc connector/function.")
    public void testDocConnectorFunction() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_object.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 8);

        PackageNode packageNode = compileResult.getAST();
        ClassDefinition classDef = packageNode.getClassDefinitions().get(0);
        BLangMarkdownDocumentation documentationAttachment = classDef.getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Test Connector");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 2);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "url");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "url for endpoint");
        Assert.assertEquals(parameters.get(1).getParameterName().getValue(), "path");
        Assert.assertEquals(parameters.get(1).getParameterDocumentation(), "path for endpoint");

        List<BLangFunction> functions = classDef.getFunctions();
        Assert.assertEquals(functions.size(), 2);

        documentationAttachment = functions.get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Test Connector action testAction");

        BLangMarkdownReturnParameterDocumentation returnParameter = documentationAttachment.getReturnParameter();
        Assert.assertNotNull(returnParameter);
        Assert.assertEquals(returnParameter.getReturnParameterDocumentation(), "whether successful or not");

        documentationAttachment = functions.get(1).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Test Connector action testSend");

        parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 1);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "ep");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "endpoint url");

        returnParameter = documentationAttachment.getReturnParameter();
        Assert.assertNotNull(returnParameter);
        Assert.assertEquals(returnParameter.getReturnParameterDocumentation(), "whether successful or not");

        documentationAttachment = packageNode.getClassDefinitions().get(1).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);

        LinkedList<BLangMarkdownReferenceDocumentation> references = documentationAttachment.getReferences();
        Assert.assertEquals(references.size(), 8);

        Assert.assertEquals(references.get(0).type, DocumentationReferenceType.TYPE);
        Assert.assertEquals(references.get(0).referenceName, "typeDef");

        Assert.assertEquals(references.get(1).type, DocumentationReferenceType.SERVICE);
        Assert.assertEquals(references.get(1).referenceName, "helloWorld");

        Assert.assertEquals(references.get(2).type, DocumentationReferenceType.VARIABLE);
        Assert.assertEquals(references.get(2).referenceName, "testVar");

        Assert.assertEquals(references.get(3).type, DocumentationReferenceType.VAR);
        Assert.assertEquals(references.get(3).referenceName, "testVar");

        Assert.assertEquals(references.get(4).type, DocumentationReferenceType.FUNCTION);
        Assert.assertEquals(references.get(4).referenceName, "add");

        Assert.assertEquals(references.get(5).type, DocumentationReferenceType.PARAMETER);
        Assert.assertEquals(references.get(5).referenceName, "x");

        Assert.assertEquals(references.get(6).type, DocumentationReferenceType.CONST);
        Assert.assertEquals(references.get(6).referenceName, "constant");

        Assert.assertEquals(references.get(7).type, DocumentationReferenceType.ANNOTATION);
        Assert.assertEquals(references.get(7).referenceName, "annot");
    }

    @Test(description = "Test doc inline code.")
    public void testInlineCode() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_doc_inline.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 0);

        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation documentationAttachment =
                packageNode.getGlobalVariables().get(1).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Example of a string template:\n" +
                "  ``string s = string `hello ${name}`;``\n\n" +
                "Example for an xml literal:\n" +
                "  ``xml x = xml `<{{tagName}}>hello</{{tagName}}>`;``");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 0);

        BLangMarkdownReturnParameterDocumentation returnParameter = documentationAttachment.getReturnParameter();
        Assert.assertNull(returnParameter);
    }

    @Test(description = "Test doc inline code with triple backtics.")
    public void testInlineCodeEnclosedTripleBackTicks() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_doc_inline_triple.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 0);

        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation documentationAttachment =
                packageNode.getGlobalVariables().get(1).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Example of a string template:\n" +
                "  ```string s = string `hello ${name}`;```\n" +
                "Example for an xml literal:\n" +
                "  ```xml x = xml `<{{tagName}}>hello</{{tagName}}>`;```");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 0);

        BLangMarkdownReturnParameterDocumentation returnParameter = documentationAttachment.getReturnParameter();
        Assert.assertNull(returnParameter);
        documentationAttachment = packageNode.getFunctions().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentationLines().get(1).text, "``` Purpose of adding\n" +
                "    # this documentation is\n" +
                "    # to check backtic documentations ```");

        documentationAttachment = packageNode.getFunctions().get(1).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentationLines().get(1).text, "```\n" +
                "    # Purpose of adding\n" +
                "    # this documentation is\n" +
                "    # to check backtic documentations\n" +
                "    # ```");
    }

    @Test(description = "Test doc multiple.", groups = { "disableOnOldParser" }, enabled = false)
    public void testMultiple() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_multiple.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 0);

        PackageNode packageNode = compileResult.getAST();
        TypeDefinition typeDefinition = packageNode.getTypeDefinitions().get(0);
        BLangMarkdownDocumentation documentationAttachment = typeDefinition.getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Documentation for Tst struct");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 3);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "a");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "`field a` documentation");
        Assert.assertEquals(parameters.get(1).getParameterName().getValue(), "b");
        Assert.assertEquals(parameters.get(1).getParameterDocumentation(), "`field b` documentation");
        Assert.assertEquals(parameters.get(2).getParameterName().getValue(), "c");
        Assert.assertEquals(parameters.get(2).getParameterDocumentation(), "`field c` documentation");

        documentationAttachment = packageNode.getServices().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "PizzaService HTTP Service");
    }

    // Stopping on arbitrary compiler phase is not supported.
    @Test(description = "Test doc native function")
    public void testDocNativeFunction() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_native_function.bal",
                CompilerPhase.TYPE_CHECK);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 0);

        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation documentationAttachment =
                packageNode.getFunctions().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Gets a access parameter value (`true` or " +
                "`false`) for a given key. Please note that `foo` will always be bigger than `bar`.\n" +
                "Example:\n" +
                "`SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);`");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 2);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "accessMode");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "read or write mode");
        Assert.assertEquals(parameters.get(1).getParameterName().getValue(), "successful");
        Assert.assertEquals(parameters.get(1).getParameterDocumentation(), "boolean `true` or `false`");
    }

    @Test(description = "Test multiline docs")
    public void testMultilineDocs() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_multiline_documentation" +
                ".bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 0);

        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation documentationAttachment =
                packageNode.getFunctions().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "description line 1\n" +
                "description line 2\n" + "description line 3");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 2);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "param1");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "param1 description line 1\n" +
                "           param1 description line 2\n" +
                "           param1 description line 3");
        Assert.assertEquals(parameters.get(1).getParameterName().getValue(), "param2");
        Assert.assertEquals(parameters.get(1).getParameterDocumentation(), "param2 description line 1\n" +
                "           param2 description line 2\n" +
                "           param2 description line 3");

        BLangMarkdownReturnParameterDocumentation returnParameter = documentationAttachment.getReturnParameter();
        Assert.assertNotNull(returnParameter);
        Assert.assertEquals(returnParameter.getReturnParameterDocumentation(), "return description line 1\n" +
                "           return description line 2\n" +
                "           return description line 3");
    }

    @Test(description = "Test lambda in object init")
    public void testMarkdownWithLambdaInObjectInit() {
        CompileResult compileResult = BCompileUtil.compile("test-src/documentation/markdown_with_lambda.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 0);

        PackageNode packageNode = compileResult.getAST();
        BLangMarkdownDocumentation documentationAttachment =
                packageNode.getClassDefinitions().get(0).getMarkdownDocumentationAttachment();
        Assert.assertNotNull(documentationAttachment);
        Assert.assertEquals(documentationAttachment.getDocumentation(), "Documentation for TimeOrderWindow.\n");

        LinkedList<BLangMarkdownParameterDocumentation> parameters = documentationAttachment.getParameters();
        Assert.assertEquals(parameters.size(), 1);
        Assert.assertEquals(parameters.get(0).getParameterName().getValue(), "f");
        Assert.assertEquals(parameters.get(0).getParameterDocumentation(), "documentation");
    }

    @Test(description = "Test on methods of object type def")
    public void testMarkdownOnMethodOfObjectTypeDef() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/documentation/markdown_on_method_object_type_def.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 0);
        Assert.assertEquals(compileResult.getWarnCount(), 1);
        BAssertUtil.validateWarning(compileResult, 0, "undocumented parameter 'i'", 4, 25);

        PackageNode packageNode = compileResult.getAST();
        TypeDefinition typeDefinition = packageNode.getTypeDefinitions().get(0);
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeDefinition.getTypeNode();
        BLangFunction bLangFunction = objectTypeNode.functions.get(0);
        BLangMarkdownDocumentation markdownDocumentationAttachment = bLangFunction.getMarkdownDocumentationAttachment();
        Assert.assertEquals(markdownDocumentationAttachment.getDocumentation(), "This is the doc");
    }
}
