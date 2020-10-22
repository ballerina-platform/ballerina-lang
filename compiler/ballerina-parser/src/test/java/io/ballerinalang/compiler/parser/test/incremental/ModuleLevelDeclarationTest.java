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
package io.ballerinalang.compiler.parser.test.incremental;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.tools.text.TextEdit;
import io.ballerina.tools.text.TextRange;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Contains module level declarations incremental parsing tests.
 *
 * @since 1.3.0
 */
public class ModuleLevelDeclarationTest extends AbstractIncrementalParserTest {

    @Test(enabled = false)
    public void testVariableNameChange() {
        SyntaxTree oldTree = parseFile("module_declarations/function_name_old.bal");
        SyntaxTree newTree = parse(oldTree, "module_declarations/function_name_new.bal");
        Node[] newNodes = populateNewNodes(oldTree, newTree);

        // TODO This is fragile way to test. Improve
        Assert.assertEquals(newNodes.length, 5);
    }

    @Test
    public void testUpdatingEmptyDocument() {
        String input = " \n  ";
        TextDocument textDocument = TextDocuments.from(input);
        SyntaxTree oldTree = SyntaxTree.from(textDocument);

        // Applying a change
        TextEdit[] edits = new TextEdit[]{TextEdit.from(TextRange.from(0, 0), "public function main() {\n }\n")};
        TextDocumentChange textDocumentChange = TextDocumentChange.from(edits);
        SyntaxTree newTree = SyntaxTree.from(oldTree, textDocumentChange);

        ModulePartNode modulePartNode = newTree.rootNode();
        FunctionDefinitionNode functionDefinitionNode = (FunctionDefinitionNode) modulePartNode.members().get(0);
        IdentifierToken funcName = functionDefinitionNode.functionName();

        Assert.assertEquals(funcName.text(), "main");
    }

    @Test
    public void testReusingModuleLevelDeclerations() {
        SyntaxTree oldTree = parseFile("module_declarations/module_declarations_old.bal");
        SyntaxTree newTree = parse(oldTree, "module_declarations/module_declarations_new.bal");
        Node[] newNodes = populateNewNodes(oldTree, newTree);
        Assert.assertEquals(newNodes.length, 6);
        Assert.assertEquals(((Token) newNodes[0]).text(), "public");
        Assert.assertEquals(((Token) newNodes[1]).text(), "function");
        Assert.assertEquals(((Token) newNodes[2]).text(), "updatedFoo");
        Assert.assertEquals(newNodes[3].kind(), SyntaxKind.FUNCTION_SIGNATURE);
        Assert.assertEquals(newNodes[4].kind(), SyntaxKind.FUNCTION_DEFINITION);
        Assert.assertEquals(newNodes[5].kind(), SyntaxKind.MODULE_PART);
    }
}
