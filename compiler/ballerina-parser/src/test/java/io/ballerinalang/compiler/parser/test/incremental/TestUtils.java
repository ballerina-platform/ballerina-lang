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

import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.internal.parser.tree.SyntaxUtils;
import io.ballerinalang.compiler.syntax.BLModules;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocumentChange;
import io.ballerinalang.compiler.text.TextDocuments;
import io.ballerinalang.compiler.text.TextEdit;
import io.ballerinalang.compiler.text.TextRange;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.Diff;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Contains helper methods for incremental parser tests.
 *
 * @since 1.3.0
 */
public class TestUtils {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    /**
     * Returns a {@code SyntaxTree} after parsing the give source code.
     *
     * @param sourceFilePath Path to the ballerina file
     */
    public static SyntaxTree parse(String sourceFilePath) {
        String text = getSourceText(sourceFilePath);
        TextDocument textDocument = TextDocuments.from(text);
        return BLModules.parse(textDocument);
    }

    public static SyntaxTree parse(SyntaxTree oldTree, String sourceFilePath) {
        return BLModules.parse(oldTree, getTextChange(oldTree, sourceFilePath));
    }

    public static String getSourceText(String sourceFilePath) {
        try {
            return new String(Files.readAllBytes(RESOURCE_DIRECTORY.resolve(Paths.get("incremental/", sourceFilePath))),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TextDocumentChange getTextChange(SyntaxTree oldTree, String newSourceFilePath) {
        return getTextChange(oldTree.toString(), getSourceText(newSourceFilePath));
    }

    public static TextDocumentChange getTextChange(String oldText, String newText) {
        DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
        LinkedList<Diff> diffList = diffMatchPatch.diffMain(oldText, newText);
        diffMatchPatch.diffCleanupSemantic(diffList);
        return new TextDocumentChange(getTextEdits(diffList));
    }

    public static Node[] populateNewNodes(SyntaxTree oldTree, SyntaxTree newTree) {
        Set<STNode> oldNodeSet = new HashSet<>();
        populateNodes(oldTree.getModulePart(), oldNodeSet);

        List<Node> newNodeList = new ArrayList<>();
        populateNewNodes(oldNodeSet, newTree.getModulePart(), newNodeList);
        return newNodeList.toArray(new Node[0]);
    }

    private static void populateNewNodes(Set<STNode> oldNodeSet, NonTerminalNode node, List<Node> newNodeList) {
        if (oldNodeSet.contains(node.getInternalNode())) {
            return;
        }

        for (int bucket = 0; bucket < node.bucketCount(); bucket++) {
            Node child = node.childInBucket(bucket);
            if (SyntaxUtils.isToken(child)) {
                if (!oldNodeSet.contains(child.getInternalNode())) {
                    // TODO Checking for width seems not correct in this situation. Double check!!!
                    if (child.getSpanWithMinutiae().width() != 0) {
                        newNodeList.add(child);
                    }
                }
            } else {
                populateNewNodes(oldNodeSet, (NonTerminalNode) child, newNodeList);
            }
        }

        if (node.getSpanWithMinutiae().width() != 0) {
            newNodeList.add(node);
        }
    }

    private static void populateNodes(NonTerminalNode node, Set<STNode> nodeSet) {
        for (int bucket = 0; bucket < node.bucketCount(); bucket++) {
            Node child = node.childInBucket(bucket);
            if (SyntaxUtils.isToken(child)) {
                // TODO Checking for width seems not correct in this situation. Double check!!!
                if (child.getSpanWithMinutiae().width() != 0) {
                    nodeSet.add(child.getInternalNode());
                }
            } else {
                populateNodes((NonTerminalNode) child, nodeSet);
            }
        }
        if (node.getSpanWithMinutiae().width() != 0) {
            nodeSet.add(node.getInternalNode());
        }
    }

    private static TextEdit[] getTextEdits(List<Diff> diffList) {
        List<TextEdit> textEditList = new ArrayList<>();
        int oldTextOffset = 0;
        Diff deleteDiff = null;
        Diff insertDiff = null;
        int diffIndex = 0;
        int diffCount = diffList.size();
        while (diffIndex < diffCount) {
            Diff diff = diffList.get(diffIndex);
            switch (diff.operation) {
                case DELETE:
                    deleteDiff = diff;
                    break;
                case INSERT:
                    insertDiff = diff;
                    break;
            }

            diffIndex++;
            if (diffIndex != diffCount && diff.operation != DiffMatchPatch.Operation.EQUAL) {
                continue;
            }

            if (deleteDiff != null || insertDiff != null) {
                TextEdit textEdit = getTextEdit(deleteDiff, insertDiff, oldTextOffset);
                textEditList.add(textEdit);
                oldTextOffset += textEdit.range().length();
                insertDiff = null;
                deleteDiff = null;
            }
            oldTextOffset += diff.text.length();
        }
        return textEditList.toArray(new TextEdit[0]);
    }

    private static TextEdit getTextEdit(Diff deleteDiff, Diff insertDiff, int diffStart) {
        String newTextChange;
        int diffEnd;
        if (deleteDiff != null && insertDiff != null) {
            newTextChange = insertDiff.text;
            diffEnd = diffStart + deleteDiff.text.length();
        } else if (deleteDiff != null) {
            newTextChange = "";
            diffEnd = diffStart + deleteDiff.text.length();
        } else {
            newTextChange = insertDiff.text;
            diffEnd = diffStart;
        }
        return new TextEdit(new TextRange(diffStart, diffEnd), newTextChange);
    }
}
