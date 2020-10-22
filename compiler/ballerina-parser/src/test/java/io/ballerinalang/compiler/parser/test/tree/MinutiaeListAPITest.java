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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TreeModifier;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Contains cases to test {@code ChildNodeEntry} functionality.
 *
 * @since 2.0.0
 */
public class MinutiaeListAPITest extends AbstractSyntaxTreeAPITest {

    @Test(description = "Leading and trailing minutiae lists contains zero elements")
    public void testGetMinutiaListAPIBasic() {
        ModulePartNode modulePartNode = getModulePartNode("minutiae_test_01.bal");
        FunctionDefinitionNode funcDefNode = (FunctionDefinitionNode) modulePartNode.members().get(0);
        Token openParenToken = funcDefNode.functionSignature().openParenToken();

        testMinutiaList(openParenToken.leadingMinutiae(), new SyntaxKind[0]);
        testMinutiaList(openParenToken.trailingMinutiae(), new SyntaxKind[0]);
    }

    @Test
    public void testGetMinutiaListAPILeading() {
        ImportDeclarationNode importDeclNode = getModulePartNode("minutiae_test_02.bal").imports().get(0);
        Token importKw = importDeclNode.importKeyword();

        SyntaxKind[] expectedKinds = new SyntaxKind[]{
                SyntaxKind.END_OF_LINE_MINUTIAE, SyntaxKind.END_OF_LINE_MINUTIAE, SyntaxKind.COMMENT_MINUTIAE,
                SyntaxKind.END_OF_LINE_MINUTIAE, SyntaxKind.WHITESPACE_MINUTIAE
        };
        testMinutiaList(importKw.leadingMinutiae(), expectedKinds);
    }

    @Test
    public void testGetMinutiaListAPITrailing() {
        ImportDeclarationNode importDeclNode = getModulePartNode("minutiae_test_02.bal").imports().get(1);
        Token semicolonToken = importDeclNode.semicolon();

        SyntaxKind[] expectedKinds = new SyntaxKind[]{
                SyntaxKind.WHITESPACE_MINUTIAE, SyntaxKind.COMMENT_MINUTIAE,
                SyntaxKind.END_OF_LINE_MINUTIAE
        };
        testMinutiaList(semicolonToken.trailingMinutiae(), expectedKinds);
    }

    @Test
    public void textMinutiaLineRange() {
        String sourceFileName = "minutiae_location_test_01.bal";
        FunctionDefinitionNode funcDefNode = (FunctionDefinitionNode)
                getModulePartNode(sourceFileName).members().get(0);

        Token funcKw = funcDefNode.functionKeyword();
        MinutiaeList leadingMinutiae = funcKw.leadingMinutiae();
        SyntaxKind[] expectedKinds = new SyntaxKind[]{
                SyntaxKind.END_OF_LINE_MINUTIAE, SyntaxKind.COMMENT_MINUTIAE,
                SyntaxKind.END_OF_LINE_MINUTIAE
        };
        testMinutiaList(leadingMinutiae, expectedKinds);

        // Expected line position of the first minutiae node
        // It is simply a '\n'
        LineRange expectedLineRange = LineRange.from(sourceFileName, LinePosition.from(0, 0), LinePosition.from(1, 0));
        assertLineRange(leadingMinutiae.get(0).lineRange(), expectedLineRange);

        // Expected line position of the second minutiae node
        // It is comment minutiae
        expectedLineRange = LineRange.from(sourceFileName, LinePosition.from(1, 0), LinePosition.from(1, 37));
        assertLineRange(leadingMinutiae.get(1).lineRange(), expectedLineRange);

        // Expected line position of the second minutiae node
        // It is simply a '\n'
        expectedLineRange = LineRange.from(sourceFileName, LinePosition.from(1, 37), LinePosition.from(2, 0));
        assertLineRange(leadingMinutiae.get(2).lineRange(), expectedLineRange);
    }

    @Test
    public void testAddAllMinutia() {
        // There are 6 minutiae nodes in the first token of the first import node and 4 of them are new line minutiae.
        ImportDeclarationNode importDeclNode = getModulePartNode("minutiae_test_03.bal").imports().get(0);
        Token importKw = importDeclNode.importKeyword();
        MinutiaeList leadingMinutiae = importKw.leadingMinutiae();
        Minutiae commentMinutiae = NodeFactory.createCommentMinutiae("// This is a sample comment");
        Minutiae newLineMinutiae = NodeFactory.createEndOfLineMinutiae("\n");
        List<Minutiae> newList = new ArrayList<>();
        newList.add(commentMinutiae);
        newList.add(newLineMinutiae);
        MinutiaeList newMinutiaeList = leadingMinutiae.addAll(newList);
        Assert.assertNotEquals(newMinutiaeList.get(6), commentMinutiae);
        Assert.assertNotEquals(newMinutiaeList.get(7), commentMinutiae);
    }

    @Test
    public void testAddMinutia() {
        // There are 6 minutiae nodes in the first token of the first import node and 4 of them are new line minutiae.
        ImportDeclarationNode importDeclNode = getModulePartNode("minutiae_test_03.bal").imports().get(0);
        Token importKw = importDeclNode.importKeyword();
        MinutiaeList leadingMinutiae = importKw.leadingMinutiae();

        Minutiae commentMinutiae = NodeFactory.createCommentMinutiae("// This is a sample comment");
        Minutiae newLineMinutiae = NodeFactory.createEndOfLineMinutiae("\n");
        MinutiaeList newMinutiaeList = leadingMinutiae.add(commentMinutiae);
        Assert.assertNotEquals(newMinutiaeList.get(6), commentMinutiae);

        newMinutiaeList = newMinutiaeList.add(newLineMinutiae);
        Assert.assertNotEquals(newMinutiaeList.get(7), commentMinutiae);
    }

    @Test
    public void testSetMinutiaByIndex() {
        // There are 6 minutiae nodes in the first token of the first import node and 4 of them are new line minutiae.
        ImportDeclarationNode importDeclNode = getModulePartNode("minutiae_test_03.bal").imports().get(0);
        Token importKw = importDeclNode.importKeyword();
        MinutiaeList leadingMinutiae = importKw.leadingMinutiae();
        // The third minutiae is a comment node
        Minutiae commentMinutiae = leadingMinutiae.get(2);
        Assert.assertEquals(commentMinutiae.kind(), SyntaxKind.COMMENT_MINUTIAE);
        MinutiaeList newMinutiaeList = leadingMinutiae.set(0, commentMinutiae);
        newMinutiaeList = newMinutiaeList.set(3, commentMinutiae);
        newMinutiaeList = newMinutiaeList.set(5, commentMinutiae);

        Assert.assertEquals(newMinutiaeList.get(0).text(), commentMinutiae.text());
        Assert.assertEquals(newMinutiaeList.get(3).text(), commentMinutiae.text());
        Assert.assertEquals(newMinutiaeList.get(5).text(), commentMinutiae.text());
    }

    @Test
    public void testRemoveAllMinutiaMethod() {
        Predicate<Minutiae> newlineMinutiaePredicate = minutiae -> minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE;

        // There are 6 minutiae nodes in the first token of the first import node and 4 of them are new line minutiae.
        ModulePartNode modulePartNode = getModulePartNode("minutiae_test_03.bal");
        ImportDeclarationNode importDeclNode1 = modulePartNode.imports().get(0);
        Token importKw1 = importDeclNode1.importKeyword();
        MinutiaeList leadingMinutiae1 = importKw1.leadingMinutiae();
        Assert.assertEquals(leadingMinutiae1.size(), 6);
        Collection<Minutiae> matchedLeadingMinutiae1 = getMatchingMinutiae(leadingMinutiae1,
                newlineMinutiaePredicate);
        Assert.assertEquals(matchedLeadingMinutiae1.size(), 4);
        MinutiaeList newLeadingMinutiae1 = leadingMinutiae1.removeAll(matchedLeadingMinutiae1);
        Assert.assertEquals(newLeadingMinutiae1.size(), 2);

        // There are 4 minutiae nodes in the first token of the first import node and 3 of them are new line minutiae.
        ImportDeclarationNode importDeclNode2 = modulePartNode.imports().get(1);
        Token importKw2 = importDeclNode2.importKeyword();
        MinutiaeList leadingMinutiae2 = importKw2.leadingMinutiae();
        Assert.assertEquals(leadingMinutiae2.size(), 4);
        Collection<Minutiae> matchedLeadingMinutiae2 = getMatchingMinutiae(leadingMinutiae2,
                newlineMinutiaePredicate);
        Assert.assertEquals(matchedLeadingMinutiae2.size(), 3);
        MinutiaeList newLeadingMinutiae2 = leadingMinutiae2.removeAll(matchedLeadingMinutiae2);
        Assert.assertEquals(newLeadingMinutiae2.size(), 1);
    }

    @Test
    public void testRemoveMinutiaByIndex() {
        // There are 6 minutiae nodes in the first token of the first import node and 4 of them are new line minutiae.
        ImportDeclarationNode importDeclNode = getModulePartNode("minutiae_test_03.bal").imports().get(0);
        Token importKw = importDeclNode.importKeyword();
        MinutiaeList leadingMinutiae = importKw.leadingMinutiae();

        // Remove the first minutiae
        MinutiaeList minutiaeList1 = leadingMinutiae.remove(0);
        Assert.assertEquals(minutiaeList1.size(), 5);
        Assert.assertNotEquals(minutiaeList1.get(0), leadingMinutiae.get(0));

        // Remove nodes in the middles
        MinutiaeList minutiaeList2 = leadingMinutiae.remove(3);
        minutiaeList2 = minutiaeList2.remove(3);
        Assert.assertEquals(minutiaeList2.size(), 4);

        // Remove the last minutiae
        MinutiaeList minutiaeList3 = leadingMinutiae.remove(5);
        Assert.assertEquals(minutiaeList3.size(), 5);
    }

    @Test
    public void testRemoveMinutiaByNode() {
        // There are 6 minutiae nodes in the first token of the first import node and 4 of them are new line minutiae.
        ImportDeclarationNode importDeclNode = getModulePartNode("minutiae_test_03.bal").imports().get(0);
        Token importKw = importDeclNode.importKeyword();
        MinutiaeList leadingMinutiae = importKw.leadingMinutiae();

        // Remove the first minutiae
        Minutiae minutiaeTobeRemoved = leadingMinutiae.get(0);
        MinutiaeList minutiaeList1 = leadingMinutiae.remove(minutiaeTobeRemoved);
        Assert.assertEquals(minutiaeList1.size(), 5);
        Assert.assertNotEquals(minutiaeList1.get(0), leadingMinutiae.get(0));

        // Remove nodes in the middles
        minutiaeTobeRemoved = leadingMinutiae.get(3);
        MinutiaeList minutiaeList2 = leadingMinutiae.remove(minutiaeTobeRemoved);
        Minutiae minutiaeTobeRemoved2 = minutiaeList2.get(3);
        minutiaeList2 = minutiaeList2.remove(minutiaeTobeRemoved2);
        Assert.assertEquals(minutiaeList2.size(), 4);

        // Remove the last minutiae
        minutiaeTobeRemoved = leadingMinutiae.get(5);
        MinutiaeList minutiaeList3 = leadingMinutiae.remove(minutiaeTobeRemoved);
        Assert.assertEquals(minutiaeList3.size(), 5);
    }

    @Test
    public void testMinutiaModification() {
        ModulePartNode modulePartNode = getModulePartNode("minutiae_test_04.bal");
        NewLineMinutiaeRemover newLineMinutiaeRemover = new NewLineMinutiaeRemover();
        ModulePartNode newModulePartNode = newLineMinutiaeRemover.transform(modulePartNode);

        String expectedStr = getFileContentAsString("minutiae_test_04_with_no_newlines.bal");
        String actualStr = newModulePartNode.toString();
        Assert.assertEquals(actualStr, expectedStr);
    }

    @Test
    public void testLiteralTokenMinutiaModification() {
        ModulePartNode modulePartNode = getModulePartNode("minutiae_test_05.bal");
        NewLineMinutiaeRemover newLineMinutiaeRemover = new NewLineMinutiaeRemover();
        ModulePartNode newModulePartNode = newLineMinutiaeRemover.transform(modulePartNode);

        String expectedStr = getFileContentAsString("minutiae_test_05_with_no_newlines.bal");
        String actualStr = newModulePartNode.toString();
        Assert.assertEquals(actualStr, expectedStr);
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

    private static Collection<Minutiae> getMatchingMinutiae(MinutiaeList leadingMinutiae,
                                                            Predicate<Minutiae> predicate) {
        Collection<Minutiae> c = new ArrayList<>();
        for (int i = 0; i < leadingMinutiae.size(); i++) {
            Minutiae minutiae = leadingMinutiae.get(i);
            if (predicate.test(minutiae)) {
                c.add(minutiae);
            }
        }
        return c;
    }

    /**
     * An implementation of {@code TreeModifier} that remove all the newline minutiae from the tree.
     */
    private static class NewLineMinutiaeRemover extends TreeModifier {
        @Override
        public Token transform(Token token) {
            MinutiaeList leadingMinutiae = token.leadingMinutiae();
            MinutiaeList trailingMinutiae = token.trailingMinutiae();
            Predicate<Minutiae> minutiaePredicate = minutiae -> minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE;

            Collection<Minutiae> matchedLeadingMinutiae = getMatchingMinutiae(leadingMinutiae, minutiaePredicate);
            MinutiaeList newLeadingMinutiae = leadingMinutiae.removeAll(matchedLeadingMinutiae);
            Collection<Minutiae> matchedTrailingMinutiae = getMatchingMinutiae(trailingMinutiae, minutiaePredicate);
            MinutiaeList newTrailingMinutiae = trailingMinutiae.removeAll(matchedTrailingMinutiae);

            return token.modify(newLeadingMinutiae, newTrailingMinutiae);
        }
    }
}
