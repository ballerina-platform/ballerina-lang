/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.core;

import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that contains the util functions used by the formatting tree modifier.
 */
class FormatterUtils {

    private FormatterUtils() {

    }

    static final String NEWLINE_SYMBOL = System.getProperty("line.separator");

    static boolean isInlineRange(Node node, LineRange lineRange) {
        if (lineRange == null) {
            return true;
        }

        int nodeStartLine = node.lineRange().startLine().line();
        int nodeStartColumn = node.lineRange().startLine().offset();
        int nodeEndLine = node.lineRange().endLine().line();
        int nodeEndColumn = node.lineRange().endLine().offset();

        int rangeStartLine = lineRange.startLine().line();
        int rangeStartColumn = lineRange.startLine().offset();
        int rangeEndLine = lineRange.endLine().line();
        int rangeEndColumn = lineRange.endLine().offset();

        // Node ends before the range
        if (nodeEndLine < rangeStartLine) {
            return false;
        } else if (nodeEndLine == rangeStartLine) {
            return nodeEndColumn > rangeStartColumn;
        }

        // Node starts after the range
        if (nodeStartLine > rangeEndLine) {
            return false;
        } else if (nodeStartLine == rangeEndLine) {
            return nodeStartColumn < rangeEndColumn;
        }

        return true;
    }

    /**
     * Sort ImportDeclaration nodes based on orgName and the moduleName in-place.
     *
     * @param importDeclarationNodes ImportDeclarations nodes
     */
    static void sortImportDeclarations(List<ImportDeclarationNode> importDeclarationNodes) {
        importDeclarationNodes.sort((node1, node2) -> new CompareToBuilder()
                .append(node1.orgName().isPresent() ? node1.orgName().get().orgName().text() : "",
                        node2.orgName().isPresent() ? node2.orgName().get().orgName().text() : "")
                .append(node1.moduleName().stream().map(node -> node.toString().trim()).collect(Collectors.joining()),
                        node2.moduleName().stream().map(node -> node.toString().trim()).collect(Collectors.joining()))
                .toComparison());
    }

    /**
     * Swap leading minutiae of the first import in original code and the first import when sorted.
     *
     * @param firstImportNode First ImportDeclarationNode in original code
     * @param importNodes     Sorted formatted ImportDeclarationNode nodes
     */
    static void swapLeadingMinutiae(ImportDeclarationNode firstImportNode, List<ImportDeclarationNode> importNodes) {
        int prevFirstImportIndex = -1;
        String firstImportOrgName = firstImportNode.orgName().map(ImportOrgNameNode::toSourceCode).orElse("");
        String firstImportModuleName = getImportModuleName(firstImportNode);
        for (int i = 0; i < importNodes.size(); i++) {
            if (doesImportMatch(firstImportOrgName, firstImportModuleName, importNodes.get(i))) {
                prevFirstImportIndex = i;
                break;
            }
        }
        if (prevFirstImportIndex == 0) {
            return;
        }

        // remove comments from the previous first import
        ImportDeclarationNode prevFirstImportNode = importNodes.get(prevFirstImportIndex);
        MinutiaeList prevFirstLeadingMinutiae = prevFirstImportNode.leadingMinutiae();

        MinutiaeList prevFirstNewLeadingMinutiae = NodeFactory.createEmptyMinutiaeList();
        Minutiae prevFirstMinutiae = prevFirstLeadingMinutiae.get(0);
        if (prevFirstMinutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
            // if the prevFirstImport now is the first of a group of imports, handle the added leading newline
            prevFirstNewLeadingMinutiae = prevFirstNewLeadingMinutiae.add(prevFirstMinutiae);
            prevFirstLeadingMinutiae = prevFirstLeadingMinutiae.remove(0);
        }

        importNodes.set(prevFirstImportIndex,
                modifyImportDeclLeadingMinutiae(prevFirstImportNode, prevFirstNewLeadingMinutiae));

        if (!hasEmptyLineAtEnd(prevFirstLeadingMinutiae)) {
            // adds a newline after prevFirstImport's leading minutiae if not present
            prevFirstLeadingMinutiae =
                    prevFirstLeadingMinutiae.add(NodeFactory.createEndOfLineMinutiae(System.lineSeparator()));
        }

        ImportDeclarationNode newFirstImportNode = importNodes.get(0);
        MinutiaeList newFirstLeadingMinutiae = newFirstImportNode.importKeyword().leadingMinutiae();
        for (int i = 0; i < newFirstLeadingMinutiae.size(); i++) {
            Minutiae minutiae = newFirstLeadingMinutiae.get(i);
            if (i == 0 && minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
                // since we added a new line after prevFirstImport's leading minutiae we can skip th newline here
                continue;
            }
            prevFirstLeadingMinutiae = prevFirstLeadingMinutiae.add(minutiae);
        }

        importNodes.set(0, modifyImportDeclLeadingMinutiae(newFirstImportNode, prevFirstLeadingMinutiae));
    }

    private static ImportDeclarationNode modifyImportDeclLeadingMinutiae(ImportDeclarationNode importDecl,
                                                                         MinutiaeList leadingMinutiae) {
        Token importToken = importDecl.importKeyword();
        Token modifiedImportToken = importToken.modify(leadingMinutiae, importToken.trailingMinutiae());
        return importDecl.modify().withImportKeyword(modifiedImportToken).apply();
    }

    private static boolean doesImportMatch(String orgName, String moduleName, ImportDeclarationNode importDeclNode) {
        String importDeclOrgName = importDeclNode.orgName().map(ImportOrgNameNode::toSourceCode).orElse("");
        return orgName.equals(importDeclOrgName) && moduleName.equals(getImportModuleName(importDeclNode));
    }

    private static String getImportModuleName(ImportDeclarationNode importDeclarationNode) {
        return importDeclarationNode.moduleName().stream().map(Node::toSourceCode).collect(Collectors.joining("."));
    }

    private static boolean hasEmptyLineAtEnd(MinutiaeList minutiaeList) {
        int size = minutiaeList.size();
        return minutiaeList.get(size - 1).kind() == SyntaxKind.END_OF_LINE_MINUTIAE &&
                minutiaeList.get(size - 2).kind() == SyntaxKind.END_OF_LINE_MINUTIAE;
    }

    static boolean isBlockOnASingleLine(FormattingOptions options, BlockStatementNode node) {
        if (node.lineRange().startLine().line() != node.lineRange().endLine().line()) {
            return false;
        }

        if (options.allowShortBlocksOnASingleLine()) {
            return true;
        }

        SyntaxKind parentKind = node.parent().kind();
        return (options.allowShortIfStatementsOnASingleLine() && parentKind == SyntaxKind.IF_ELSE_STATEMENT) ||
                (options.allowShortLoopsOnASingleLine() && parentKind == SyntaxKind.WHILE_STATEMENT) ||
                (options.allowShortMatchLabelsOnASingleLine() && parentKind == SyntaxKind.MATCH_CLAUSE);
    }

    static int getConstDefLength(ConstantDeclarationNode node) {
        int size = node.visibilityQualifier().isPresent() ? node.visibilityQualifier().get().text().length() : 0;
        size += node.constKeyword().text().length();
        size += node.variableName().text().length();
        return size;
    }
}
