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
        byte[] content = Files.readAllBytes(Paths.get("foo_large.bal"));

        String sourceText = new String(content);
//        String sourceText = "\npublic function getValue() { int boxer; int java;}\npublic function foo() {int i; int j;}\n";

        long currentNanos = System.currentTimeMillis();


        TextDocument textDocument = TextDocuments.from(sourceText);
        BLModules.parse(textDocument);
        SyntaxTree oldTree = BLModules.parse(textDocument);


        long endNano = System.currentTimeMillis();
        System.out.println(endNano - currentNanos);

        currentNanos = System.currentTimeMillis();
        dfTraverse(oldTree.getModulePart().getInternalNode());
        endNano = System.currentTimeMillis();
        System.out.println(endNano - currentNanos);
////
//        currentNanos = System.currentTimeMillis();
//        dfTraverse(oldTree.getModulePart());
//        endNano = System.currentTimeMillis();
//        System.out.println(endNano - currentNanos);




        currentNanos = System.currentTimeMillis();
        Token token = oldTree.getModulePart().findToken(10000);
        endNano = System.currentTimeMillis();
        System.out.println(endNano - currentNanos);
        System.out.println(token);




//        System.out.println(oldTree.toString());

//        Token token = oldTree.getModulePart().findToken(40);
//        System.out.println(token);

        TextRange textRange4 = new TextRange(0, 1);
        TextEdit textEdit4 = new TextEdit(textRange4, "");

        TextRange textRange1 = new TextRange(39, 40);
        TextEdit textEdit1 = new TextEdit(textRange1, "kkkk");

        TextRange textRange2 = new TextRange(54, 57);
        TextEdit textEdit2 = new TextEdit(textRange2, "j");
        TextEdit[] textEdits = new TextEdit[]{textEdit4, textEdit1, textEdit2};

        currentNanos = System.currentTimeMillis();
        TextDocumentChange textDocumentChange = new TextDocumentChange(textEdits);
        SyntaxTree newTree = BLModules.parse(oldTree, textDocumentChange);
        endNano = System.currentTimeMillis();
        System.out.println(endNano - currentNanos);

//        System.out.println(newTree.toString());


//        Token token = oldTree.getModulePart().findToken(33);
//        System.out.println(token);
    }


//        dfTraverse(newTree.getModulePart());


    //        Token blToken;
////        NodePointer nodePointer = new NodePointer(oldTree.getModulePart());
////        do {
////            nodePointer = nodePointer.nextToken();
////            blToken = nodePointer.currentToken();
////            System.out.println(blToken);
////        } while(blToken.getKind() != SyntaxKind.EOF_TOKEN);


//    public static void main(String[] args) {
//        String sourceText = "public function getValue() {int i; int j;}public function foo() {int i; int j;}\n";
//        System.out.println("Source text len: " + sourceText.length());
//        CharacterReader reader = new CharacterReader(sourceText);
//        BallerinaLexer lexer = new BallerinaLexer(reader);
//        BallerinaParser parser = new BallerinaParser(lexer);
//
//        STModulePart modulePart = parser.parserModulePart();
//        ModulePart blModulePart = (ModulePart) modulePart.createFacade(0, null);
////        ModuleMemberDeclaration moduleMember0 = blModulePart.getMembers().childInBucket(0);
////        ModuleMemberDeclaration moduleMember1 = blModulePart.getMembers().childInBucket(1);
//
////        System.out.println(modulePart.width());
////        System.out.println(modulePart.toString());
//////        dfTraverse(modulePart);
////        System.out.println(moduleMember0);
////        System.out.println(moduleMember1);
//
//        dfTraverse(blModulePart);
//        System.out.println(blModulePart);
//    }
//
//    public static void main1(String[] args) {
//        String sourceText = "import ballerinax/java.arrays;\n\npublic function getValue() {\n\tint i = 5 + \t6;\n\tint k = i  * 4 + 6;\n}\n";
//        System.out.println("Source text len: " + sourceText.length());
//        CharacterReader reader = new CharacterReader(sourceText);
//
//        BallerinaLexer lexer = new BallerinaLexer(reader);
//
//        int tokenLength = 0;
//        STToken token = lexer.lexSyntaxToken();
//        while (token.kind != SyntaxKind.EOF_TOKEN) {
//            tokenLength += token.width();
//            print(token);
//            token = lexer.lexSyntaxToken();
//        }
//        System.out.println("Total token length: " + tokenLength);
//    }

//    public static void printToken(STToken token) {
////        System.out.println(token.getClass().getName() + " |" + token.toString() + "|");
//        System.out.println(token.width() + " |" + token.toString() + "|");
//    }
//
//    public static void print(STToken token) {
//        System.out.print(token);
//    }
//
//    // Recursive depth-firth traversal
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


    // Node -> NonTerminalNode(they have children), Token
    // NonTerminalNode -> Node[]
}
