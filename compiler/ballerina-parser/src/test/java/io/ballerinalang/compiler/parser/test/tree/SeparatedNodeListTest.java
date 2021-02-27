/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test.tree;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains cases to test the {@code ChildNodeList} API.
 *
 * @since 1.3.0
 */
public class SeparatedNodeListTest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testModuleNameCountInImportDeclaration() {
        SyntaxTree syntaxTree = parseFile("separated_node_list_import_decl.bal");
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        NodeList<ImportDeclarationNode> imports = modulePartNode.imports();

        SeparatedNodeList<IdentifierToken> moduleName = imports.get(0).moduleName();
        Assert.assertEquals(moduleName.size(), 1, "Module name part size does not match");
        Assert.assertEquals(moduleName.separatorSize(), 0, "Module name separator count does not match");

        moduleName = imports.get(1).moduleName();
        Assert.assertEquals(moduleName.size(), 2, "Module name part size does not match");
        Assert.assertEquals(moduleName.separatorSize(), 1, "Module name separator count does not match");

        moduleName = imports.get(2).moduleName();
        Assert.assertEquals(moduleName.size(), 8, "Module name part size does not match");
        Assert.assertEquals(moduleName.separatorSize(), 7, "Module name separator count does not match");
    }

    @Test
    public void testModuleNamePartsInImportDeclaration() {
        SyntaxTree syntaxTree = parseFile("separated_node_list_import_decl.bal");
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        NodeList<ImportDeclarationNode> imports = modulePartNode.imports();

        SeparatedNodeList<IdentifierToken> moduleName = imports.get(0).moduleName();
        Assert.assertEquals(moduleName.get(0).text(), "foo", "Module name parts do not match");

        moduleName = imports.get(1).moduleName();
        Set<String> expected = new HashSet<>(Arrays.asList("foo", "bar"));
        Set<String> actual = new HashSet<>(Arrays.asList(moduleName.get(0).text(), moduleName.get(1).text()));
        Assert.assertEqualsDeep(actual, expected, "Module name parts do not match");

        moduleName = imports.get(2).moduleName();
        expected = new HashSet<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h"));
        actual = new HashSet<>(moduleName.size());
        for (IdentifierToken identifierToken : moduleName) {
            actual.add(identifierToken.text());
        }
        Assert.assertEqualsDeep(actual, expected, "Module name parts do not match");

        for (int sepIndex = 0; sepIndex < moduleName.separatorSize(); sepIndex++) {
            Assert.assertEquals(moduleName.getSeparator(sepIndex).text(), ".",
                    "Separator in " + sepIndex + " does not match");
        }
    }
}
