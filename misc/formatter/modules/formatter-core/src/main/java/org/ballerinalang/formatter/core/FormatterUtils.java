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
     * Swap leading minutiae of the first import in the code and the first import when sorted.
     *
     * @param firstImportNode First ImportDeclarationNode in user code
     * @param importNodes     Sorted formatted ImportDeclarationNode nodes
     */
    static void swapLeadingMinutiae(ImportDeclarationNode firstImportNode, List<ImportDeclarationNode> importNodes) {
        String firstImportStr = getImportString(firstImportNode);
        int prevFirstIndex = -1;
        for (int i = 0; i < importNodes.size(); i++) {
            if (firstImportStr.equals(getImportString(importNodes.get(i)))) {
                prevFirstIndex = i;
                break;
            }
        }
        if (prevFirstIndex > 0) {
            List<MinutiaeList> chunks = getCommentChunks(firstImportNode.leadingMinutiae());
            int nChunks = chunks.size();
            if (nChunks == 1 && getCommentCount(chunks.get(0)) == 1) {
                return;
            }

            // remove comments from the previous first import
            ArrayList<Minutiae> leadingMinutiae = new ArrayList<>();
            if (nChunks == 2) {
                MinutiaeList lastCommentChunk = chunks.get(1);
                for (Minutiae minutiae: lastCommentChunk) {
                    leadingMinutiae.add(minutiae);
                }
            }
            ImportDeclarationNode prevFirstImportNode = importNodes.get(prevFirstIndex);
            Token prevFirstImportToken = prevFirstImportNode.importKeyword()
                    .modify(NodeFactory.createMinutiaeList(leadingMinutiae),
                            prevFirstImportNode.importKeyword().trailingMinutiae());
            prevFirstImportNode = prevFirstImportNode.modify().withImportKeyword(prevFirstImportToken).apply();
            importNodes.set(prevFirstIndex, prevFirstImportNode);

            // add leading comments from the previous first import
            MinutiaeList prevLeadingMinutiae = chunks.get(0);
            ImportDeclarationNode sortedFirstImportNode = importNodes.get(0);
            if (hasEmptyLine(firstImportNode.leadingMinutiae()) && !hasEmptyLine(prevLeadingMinutiae)) {
                prevLeadingMinutiae =
                        prevLeadingMinutiae.add(NodeFactory.createEndOfLineMinutiae(System.lineSeparator()));
            }
            MinutiaeList sortedLeadingMinutiae = sortedFirstImportNode.importKeyword().leadingMinutiae();
            for (int i = 0; i < sortedLeadingMinutiae.size(); i++) {
                Minutiae minutiae = sortedLeadingMinutiae.get(i);
                if (i == 0 && minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
                    continue;
                }
                prevLeadingMinutiae = prevLeadingMinutiae.add(minutiae);
            }

            Token sortedFirstImportToken = sortedFirstImportNode.importKeyword()
                    .modify(prevLeadingMinutiae, sortedFirstImportNode.importKeyword().trailingMinutiae());
            sortedFirstImportNode = sortedFirstImportNode.modify().withImportKeyword(sortedFirstImportToken).apply();
            importNodes.set(0, sortedFirstImportNode);
        }
    }

    private static List<MinutiaeList> getCommentChunks(MinutiaeList minutiaeList) {
        ArrayList<MinutiaeList> minutiaeLists = new ArrayList<>();
        ArrayList<Minutiae> currentChunk = new ArrayList<>();
        int consecutiveNLs = 0;
        boolean hasNonNLMinutiae = false;
        int index = minutiaeList.size() - 1;
        for (int i = index; i > -1; i--) {
            Minutiae minutiae = minutiaeList.get(i);
            if (minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
                consecutiveNLs++;
                if (consecutiveNLs > 1 && !hasNonNLMinutiae) {
                    continue;
                }
            } else {
                consecutiveNLs = 0;
                hasNonNLMinutiae = true;
            }
            if (consecutiveNLs > 1 && hasNonNLMinutiae) {
                currentChunk.remove(0);
                minutiaeLists.add(NodeFactory.createMinutiaeList(currentChunk));
                currentChunk.clear();
                index = i;
                break;
            }
            currentChunk.add(0, minutiae);
        }
        if (currentChunk.size() > 0 && hasNonNLMinutiae) {
            minutiaeLists.add(NodeFactory.createMinutiaeList(currentChunk));
        } else if (index > 0) {
            consecutiveNLs = 0;
            hasNonNLMinutiae = false;
            for (int i = 0; i < index; i++) {
                Minutiae minutiae = minutiaeList.get(i);
                if (minutiae.kind() == SyntaxKind.END_OF_LINE_MINUTIAE) {
                    consecutiveNLs++;
                } else {
                    consecutiveNLs = 0;
                    hasNonNLMinutiae = true;
                }
                if (consecutiveNLs < 3) {
                    currentChunk.add(minutiaeList.get(i));
                }
            }
            if (hasNonNLMinutiae) {
                MinutiaeList topList = NodeFactory.createMinutiaeList(currentChunk);
                if (!hasEmptyLine(topList)) {
                    topList = topList.add(NodeFactory.createEndOfLineMinutiae(System.lineSeparator()));
                }
                minutiaeLists.add(0, topList);
            }
        }
        return minutiaeLists;
    }

    private static String getImportString(ImportDeclarationNode node) {
        String orgName = node.orgName().isPresent() ? node.orgName().get().toSourceCode() : "";
        String moduleName = node.moduleName().stream()
                .map(n -> n.toSourceCode())
                .collect(Collectors.joining("."));
        return orgName + moduleName;
    }

    private static boolean hasEmptyLine(MinutiaeList minutiaeList) {
        int size = minutiaeList.size();
        if (size < 2) {
            return false;
        }
        return minutiaeList.get(size - 1).kind() == SyntaxKind.END_OF_LINE_MINUTIAE &&
                minutiaeList.get(size - 2).kind() == SyntaxKind.END_OF_LINE_MINUTIAE;
    }

    private static int getCommentCount(MinutiaeList minutiaeList) {
        int count = 0;
        for (Minutiae minutiae: minutiaeList) {
            if (minutiae.kind() == SyntaxKind.COMMENT_MINUTIAE) {
                count += 1;
            }
        }
        return count;
    }
}
