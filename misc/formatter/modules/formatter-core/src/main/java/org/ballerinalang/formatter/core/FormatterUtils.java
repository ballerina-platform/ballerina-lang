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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        int prevFirstIndex = -1;
        Optional<ImportOrgNameNode> orgNameNode = firstImportNode.orgName();
        String firstImportOrgName = orgNameNode.isPresent() ? orgNameNode.get().toSourceCode() : "";
        String firstImportModuleName = getImportModuleName(firstImportNode);
        for (int i = 0; i < importNodes.size(); i++) {
            if (isImportsEqual(firstImportOrgName, firstImportModuleName, importNodes.get(i))) {
                prevFirstIndex = i;
                break;
            }
        }
        if (prevFirstIndex == 0) {
            return;
        }
        // remove comments from the previous first import
        ImportDeclarationNode prevFirstImportNode = importNodes.get(prevFirstIndex);
        MinutiaeList prevLeadingMinutiae = prevFirstImportNode.leadingMinutiae();
        List<Minutiae> leadingMinutiae = new ArrayList<>();
        Minutiae prevFirstMinutiae = prevLeadingMinutiae.get(0);
        if (prevFirstMinutiae.kind() != SyntaxKind.COMMENT_MINUTIAE) {
            leadingMinutiae.add(prevFirstMinutiae);
            prevLeadingMinutiae = prevLeadingMinutiae.remove(0);
        }

        Token prevFirstImportToken = prevFirstImportNode.importKeyword();
        Token modifiedPrevFirstImportToken =
                prevFirstImportToken.modify(NodeFactory.createMinutiaeList(leadingMinutiae),
                        prevFirstImportToken.trailingMinutiae());
        importNodes.set(prevFirstIndex,
                prevFirstImportNode.modify().withImportKeyword(modifiedPrevFirstImportToken).apply());

        // add leading comments from the previous first import
        if (!hasEmptyLineAtEnd(prevLeadingMinutiae)) {
            prevLeadingMinutiae =
                    prevLeadingMinutiae.add(NodeFactory.createEndOfLineMinutiae(System.lineSeparator()));
        }
        ImportDeclarationNode sortedFirstImportNode = importNodes.get(0);
        Token sortedFirstImportToken = sortedFirstImportNode.importKeyword();
        MinutiaeList sortedLeadingMinutiae = sortedFirstImportToken.leadingMinutiae();
        for (int i = 0; i < sortedLeadingMinutiae.size(); i++) {
            Minutiae minutiae = sortedLeadingMinutiae.get(i);
            if (i == 0 && minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
                continue;
            }
            prevLeadingMinutiae = prevLeadingMinutiae.add(minutiae);
        }
        Token modifiedSortedFirstImportToken =
                sortedFirstImportToken.modify(prevLeadingMinutiae, sortedFirstImportToken.trailingMinutiae());
        importNodes.set(0,
                sortedFirstImportNode.modify().withImportKeyword(modifiedSortedFirstImportToken).apply());
    }

    private static boolean isImportsEqual(String firstImportOrgName, String firstImportModuleName,
                                          ImportDeclarationNode secondImportNode) {
        Optional<ImportOrgNameNode> orgNameNode = secondImportNode.orgName();
        String importOrgName = orgNameNode.isPresent() ? orgNameNode.get().toSourceCode() : "";
        if (firstImportOrgName.equals(importOrgName)) {
            return firstImportModuleName.equals(getImportModuleName(secondImportNode));
        }
        return false;
    }

    private static String getImportModuleName(ImportDeclarationNode importDeclarationNode) {
        return importDeclarationNode.moduleName().stream().map(Node::toSourceCode).collect(Collectors.joining("."));
    }

    private static boolean hasEmptyLineAtEnd(MinutiaeList minutiaeList) {
        int size = minutiaeList.size();
        return minutiaeList.get(size - 1).kind() == SyntaxKind.END_OF_LINE_MINUTIAE &&
                minutiaeList.get(size - 2).kind() == SyntaxKind.END_OF_LINE_MINUTIAE;
    }
}
