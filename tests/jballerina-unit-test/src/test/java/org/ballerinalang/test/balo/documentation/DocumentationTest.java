/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.balo.documentation;

import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.Name;

/**
 * Test cases for Documentation in BALO.
 */
public class DocumentationTest {

    private static final String CARRIAGE_RETURN_CHAR = "\r";
    private static final String EMPTY_STRING = "";
    private BPackageSymbol symbol;

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_documentation", "testDocOrg", "test");
        CompileResult result = BCompileUtil.compile("test-src/balo/test_balo/documentation/test_documentation.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
        symbol = ((BLangPackage) result.getAST()).symbol;
    }

    @Test(description = "Test Doc attachments in Balo.")
    public void testDocAttachmentBalo() {
        BPackageSymbol testOrgPackage = (BPackageSymbol) symbol.scope.lookup(new Name("test")).symbol;
        BSymbol functionSymbol = testOrgPackage.scope.lookup(new Name("open")).symbol;

        Assert.assertNotNull(functionSymbol.markdownDocumentation);
        Assert.assertEquals(functionSymbol.markdownDocumentation.description.replaceAll(CARRIAGE_RETURN_CHAR,
                EMPTY_STRING), "Gets a access parameter value (`true` or `false`) for a given key. Please note that " +
                "#foo will always be bigger than #bar.\nExample:\n``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get" +
                "(pkgNode.symbol);``");
        Assert.assertEquals(functionSymbol.markdownDocumentation.parameters.size(), 1);
        Assert.assertEquals(functionSymbol.markdownDocumentation.parameters.get(0).description
                .replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING), "read or write mode");

        Assert.assertNotNull(functionSymbol.markdownDocumentation.returnValueDescription);
        Assert.assertEquals(functionSymbol.markdownDocumentation.returnValueDescription.replaceAll
                (CARRIAGE_RETURN_CHAR, EMPTY_STRING), "success or not");

        BSymbol personSymbol = testOrgPackage.scope.lookup(new Name("Person")).symbol;
        Assert.assertNotNull(personSymbol.markdownDocumentation);
        Assert.assertEquals(personSymbol.markdownDocumentation.description.replaceAll(CARRIAGE_RETURN_CHAR,
                EMPTY_STRING),
                "Represents a Person type in ballerina.");
        Assert.assertEquals(personSymbol.markdownDocumentation.parameters.size(), 1);
        Assert.assertEquals(personSymbol.markdownDocumentation.parameters.get(0).description
                .replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING), "name of the person.");

        BSymbol personNameSymbol = personSymbol.scope.lookup(new Name("name")).symbol;
        Assert.assertNotNull(personNameSymbol.markdownDocumentation);
        Assert.assertNull(personNameSymbol.markdownDocumentation.description);
        Assert.assertEquals(personNameSymbol.markdownDocumentation.parameters.size(), 0);
        Assert.assertNull(personNameSymbol.markdownDocumentation.returnValueDescription);

        BObjectTypeSymbol personObjSymbol = (BObjectTypeSymbol) personSymbol;

        BSymbol getNameFuncSymbol = personObjSymbol.scope.lookup(new Name("Person.getName")).symbol;
        Assert.assertNotNull(getNameFuncSymbol.markdownDocumentation);
        Assert.assertEquals(getNameFuncSymbol.markdownDocumentation.description.replaceAll
                (CARRIAGE_RETURN_CHAR, EMPTY_STRING), "get the users name.");
        Assert.assertEquals(getNameFuncSymbol.markdownDocumentation.parameters.size(), 1);
        Assert.assertEquals(getNameFuncSymbol.markdownDocumentation.parameters.get(0).description
                .replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING), "integer value");

        BSymbol isMaleFuncSymbol = personObjSymbol.scope.lookup(new Name("Person.isMale")).symbol;
        Assert.assertNotNull(isMaleFuncSymbol.markdownDocumentation);
        Assert.assertEquals(isMaleFuncSymbol.markdownDocumentation.description.replaceAll(CARRIAGE_RETURN_CHAR,
                EMPTY_STRING), "Indicate whether this is a male or female.");
        Assert.assertEquals(isMaleFuncSymbol.markdownDocumentation.parameters.size(), 0);
        Assert.assertNotNull(isMaleFuncSymbol.markdownDocumentation.returnValueDescription);
        Assert.assertEquals(isMaleFuncSymbol.markdownDocumentation.returnValueDescription
                .replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING), "True if male");
    }

    @Test(description = "Test doc attachments in annotations")
    public void testAnnotationDoc() {
        BPackageSymbol testOrgPackage = (BPackageSymbol) symbol.scope.lookup(new Name("test")).symbol;
        BSymbol annotationSymbol = testOrgPackage.scope.lookup(new Name("Test")).symbol;

        MarkdownDocAttachment markdownDocumentation = annotationSymbol.markdownDocumentation;

        Assert.assertNotNull(annotationSymbol.markdownDocumentation);
        Assert.assertEquals(markdownDocumentation.description, "Documentation for Test annotation");
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_documentation", "testorg", "foo");
    }
}
