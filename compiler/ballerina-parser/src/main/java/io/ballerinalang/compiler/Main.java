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
package io.ballerinalang.compiler;

import io.ballerinalang.compiler.internal.parser.tree.STToken;
import io.ballerinalang.compiler.internal.parser.tree.STNode;
import io.ballerinalang.compiler.syntax.BLModules;
import io.ballerinalang.compiler.syntax.tree.Token;
import io.ballerinalang.compiler.text.TextDocumentChange;
import io.ballerinalang.compiler.text.TextEdit;
import io.ballerinalang.compiler.text.TextRange;
import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextDocuments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    // In the current AST we have only leading WSs
    public static void main(String[] args) throws IOException {
//        byte[] content = Files.readAllBytes(Paths.get("foo_small.bal"));

//        String sourceText = new String(content);
//        String sourceText = "\npublic function getValue() { int boxer; int java;}\n";
        String sourceText = "\npublic function getValue() { int boxer; int java;}\npublic function foo() {int i; int j;}\n";
        System.out.println(sourceText.length());
//        String sourceText = "\npublic function getValue() { int boxer; int java;}\n";

//        long currentNanos = System.currentTimeMillis();


        TextDocument textDocument = TextDocuments.from(sourceText);
        SyntaxTree oldTree = BLModules.parse(textDocument);
        System.out.println(oldTree.getModulePart());

//        TextRange textRange4 = new TextRange(0, 1);
//        TextEdit textEdit4 = new TextEdit(textRange4, "");

        TextRange textRange1 = new TextRange(36, 37);
        TextEdit textEdit1 = new TextEdit(textRange1, "kkjjjjkk");

        TextRange textRange2 = new TextRange(54, 57);
        TextEdit textEdit2 = new TextEdit(textRange2, "j");
        TextEdit[] textEdits = new TextEdit[]{textEdit1};

        TextDocumentChange textDocumentChange = new TextDocumentChange(textEdits);
        SyntaxTree newTree = BLModules.parse(oldTree, textDocumentChange);
        System.out.println(newTree.getTextDocument().toString().length());
        System.out.println(newTree.getModulePart().getSpanWithMinutiae().width());
    }

    public static void dfTraverse(STNode root) {
        if (root instanceof STToken) {
//            System.out.println(root.toString());
            return;
        }

        for (int i = 0; i < root.bucketCount(); i++) {
            STNode child = root.childInBucket(i);
            if (child == null) {
                continue;
            }

            dfTraverse(child);
        }
    }

    public static void dfTraverse(NonTerminalNode root) {
        for (int i = 0; i < root.bucketCount(); i++) {
            Node child = root.childInBucket(i);
            if (child == null) {
                continue;
            }

            if (child instanceof Token) {
//                System.out.println(child.toString());
                continue;
            }

            dfTraverse((NonTerminalNode) child);
        }
    }
}
