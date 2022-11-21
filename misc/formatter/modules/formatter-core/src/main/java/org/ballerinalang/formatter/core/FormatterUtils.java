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
import io.ballerina.compiler.syntax.tree.Node;
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

    static void sortImportDeclarations(List<ImportDeclarationNode> importDeclarationNodes) {
        importDeclarationNodes.sort((node1, node2) -> new CompareToBuilder()
                .append(node1.orgName().isPresent() ? node1.orgName().get().orgName().text() : "",
                        node2.orgName().isPresent() ? node2.orgName().get().orgName().text() : "")
                .append(node1.moduleName().stream().map(node -> node.toString().trim()).collect(Collectors.joining()),
                        node2.moduleName().stream().map(node -> node.toString().trim()).collect(Collectors.joining()))
                .toComparison());
    }
}
