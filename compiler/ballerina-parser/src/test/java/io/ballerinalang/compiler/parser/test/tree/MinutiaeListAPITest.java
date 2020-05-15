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

import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerinalang.compiler.syntax.tree.Minutiae;
import io.ballerinalang.compiler.syntax.tree.MinutiaeList;
import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains cases to test {@code ChildNodeEntry} functionality.
 *
 * @since 2.0.0
 */
public class MinutiaeAPITest extends AbstractSyntaxTreeAPITest {

    @Test(description = "Leading and trailing minutiae lists contains zero elements")
    public void testGetMinutiaListAPIBasic() {
        String sourceFileName = "minutiae_test_01.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.modulePart();
        FunctionDefinitionNode funcDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);
        Token openParenToken = funcDefNode.functionSignature().openParenToken();

        testMinutiaList(openParenToken.leadingMinutiae(), new SyntaxKind[0]);
        testMinutiaList(openParenToken.trailingMinutiae(), new SyntaxKind[0]);
    }

    @Test
    public void testGetMinutiaListAPILeading() {
        String sourceFileName = "minutiae_test_02.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.modulePart();

        ImportDeclarationNode importDeclNode = modulePartNode.imports().get(0);
        Token importKw = importDeclNode.importKeyword();

        SyntaxKind[] expectedKinds = new SyntaxKind[]{
                SyntaxKind.END_OF_LINE_MINUTIAE, SyntaxKind.END_OF_LINE_MINUTIAE, SyntaxKind.COMMENT_MINUTIA,
                SyntaxKind.END_OF_LINE_MINUTIAE, SyntaxKind.WHITESPACE_MINUTIAE
        };
        testMinutiaList(importKw.leadingMinutiae(), expectedKinds);
    }

    @Test
    public void testGetMinutiaListAPITrailing() {
        String sourceFileName = "minutiae_test_02.bal";
        SyntaxTree syntaxTree = parseFile(sourceFileName);
        ModulePartNode modulePartNode = syntaxTree.modulePart();

        ImportDeclarationNode importDeclNode = modulePartNode.imports().get(1);
        Token semicolonToken = importDeclNode.semicolon();

        SyntaxKind[] expectedKinds = new SyntaxKind[]{
                SyntaxKind.WHITESPACE_MINUTIAE, SyntaxKind.COMMENT_MINUTIA,
                SyntaxKind.END_OF_LINE_MINUTIAE
        };
        testMinutiaList(semicolonToken.trailingMinutiae(), expectedKinds);
    }

    private void testMinutiaList(MinutiaeList minutiaeList, SyntaxKind[] expectedKinds) {
        Assert.assertEquals(minutiaeList.size(), expectedKinds.length);

        List<SyntaxKind> actualKindList = new ArrayList<>();
        int minutiaeSize = minutiaeList.size();
        for (int index = 0; index < minutiaeSize; index++) {
            actualKindList.add(minutiaeList.get(index).kind());
        }
        Assert.assertEquals(actualKindList.toArray(new SyntaxKind[0]), expectedKinds);

        actualKindList = new ArrayList<>();
        for (Minutiae minutiae : minutiaeList) {
            actualKindList.add(minutiae.kind());
        }
        Assert.assertEquals(actualKindList.toArray(new SyntaxKind[0]), expectedKinds);
    }
}
