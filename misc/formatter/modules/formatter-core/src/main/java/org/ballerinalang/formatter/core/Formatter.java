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

import io.ballerinalang.compiler.syntax.tree.ModulePartNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.LineRange;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocuments;

/**
 * Class that exposes the formatting APIs.
 */
public class Formatter {

    // TODO: Add test cases for error scenarios also
    // TODO: Add some syntax errors and see how the parser behaves
    // TODO: overload these functions to have default formatting options
    // TODO: Doc comments
    public static String format(String source, FormattingOptions options) {
        TextDocument textDocument = TextDocuments.from(source);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        FormattingTreeModifier treeModifier = new FormattingTreeModifier();
        ModulePartNode newModulePart = treeModifier.transform((ModulePartNode) syntaxTree.rootNode());
        if (options != null) {
            treeModifier.setFormattingOptions(options);
        }
        return syntaxTree.modifyWith(newModulePart).toSourceCode();
    }

    public static SyntaxTree format(SyntaxTree syntaxTree, LineRange range, FormattingOptions options) {
        FormattingTreeModifier treeModifier = new FormattingTreeModifier();
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        treeModifier.setLineRange(range);
        if (options != null) {
            treeModifier.setFormattingOptions(options);
        }
        return syntaxTree.modifyWith(treeModifier.transform(modulePartNode));
    }

    public static SyntaxTree format(SyntaxTree syntaxTree, FormattingOptions options) {
        FormattingTreeModifier treeModifier = new FormattingTreeModifier();
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        if (options != null) {
            treeModifier.setFormattingOptions(options);
        }
        return syntaxTree.modifyWith(treeModifier.transform(modulePartNode));
    }
}
