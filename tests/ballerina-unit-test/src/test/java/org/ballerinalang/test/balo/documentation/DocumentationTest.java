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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.test.balo.BaloCreator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.Name;

/**
 * Test cases for Documentation in BALO.
 */
public class DocumentationTest {

    CompileResult result;
    private static final String CARRIAGE_RETURN_CHAR = "\r";
    private static final String EMPTY_STRING = "";

    @BeforeClass
    public void setup() {
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_documentation", "testDocOrg", "test");
        result = BCompileUtil.compile("test-src/balo/test_balo/documentation/test_documentation.bal");
    }

    @Test(description = "Test Doc attachments in Balo.")
    public void testDocAttachmentBalo() {
        PackageNode packageNode = result.getAST();
        BPackageSymbol symbol = ((BLangPackage) packageNode).symbol;

        BPackageSymbol testOrgPackage = (BPackageSymbol) symbol.scope.lookup(new Name("test")).symbol;
        BSymbol functionSymbol = testOrgPackage.scope.lookup(new Name("open")).symbol;

        Assert.assertNotNull(functionSymbol.documentation);
        Assert.assertEquals(functionSymbol.documentation.description.replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING),
                "\n" + "Gets a access parameter value (`true` or `false`) for a given key. "
                        + "Please note that #foo will always be bigger than #bar.\n" + "Example:\n"
                        + "``SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);``\n");
        Assert.assertEquals(functionSymbol.documentation.attributes.size(), 2);
        Assert.assertEquals(functionSymbol.documentation.attributes.get(0).description
                .replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING), " read or write mode\n");
        Assert.assertEquals(functionSymbol.documentation.attributes.get(1).description
                .replaceAll(CARRIAGE_RETURN_CHAR, EMPTY_STRING), " success or not\n");
    }
}
