/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.bindgen.model.BindgenNodeFactory;
import org.ballerinalang.bindgen.model.BindingsGenerator;
import org.ballerinalang.bindgen.model.JClassNew;
import org.ballerinalang.bindgen.model.JMethod;
import org.ballerinalang.bindgen.utils.BindgenEnv;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Test the ballerina bindgen syntax tree generated.
 *
 * @since 2.0.0
 */
public class SyntaxTreeGeneratorTest {

    @Test()
    public void test() throws FormatterException, ClassNotFoundException, BindgenException {
        ImportDeclarationNode importDeclarationNode = BindgenNodeFactory.createImportDeclarationNode("ballerina",
                "jarrays", new LinkedList<>(Arrays.asList("java", ".", "arrays")));

//      MarkdownDocumentationNode classDocumentation = BindgenNodeFactory.documentationNode("FileInputStream", "class");
//        AnnotationNode annotationNode = BindgenNodeFactory.bindingAnnotation("java.util.Collection");
//        Collection emptyClassDescription = BindgenNodeFactory.emptyClassComment("java.util.Map$Entry");
//        TextDocument textDocument = null;
//        try {
//            textDocument = TextDocuments.from(Files.readString(Paths.get("src", "main", "resources",
//                    "templates", "error.bal")));
//            new PrintStream(System.out).println(textDocument);
//        } catch (IOException e) {
//            new PrintStream(System.out).println(e);
//        }
//        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
//        if (syntaxTree.containsModulePart()) {
//            SyntaxTree newSyntaxTree = syntaxTree.modifyWith(new BindgenTreeModifier()
//                    .transform((ModulePartNode) syntaxTree.rootNode()));
//            new PrintStream(System.out).println(newSyntaxTree);
//        }
        BindgenEnv bindgenEnv = new BindgenEnv();
        bindgenEnv.setDirectJavaClass(true);
        bindgenEnv.setPublicFlag(true);
        bindgenEnv.setModulesFlag(true);
        bindgenEnv.setPackageName("testx");
        BindingsGenerator bindingsGenerator = new BindingsGenerator(bindgenEnv);
        SyntaxTree syntaxTree1 = bindingsGenerator.generate(new JClassNew(this.getClass().getClassLoader()
                .loadClass("java.io.File"), bindgenEnv));
        new PrintStream(System.out).println("******" + Formatter.format(syntaxTree1));

        try {
            JMethod jMethod = new JMethod(FileInputStream.class.getMethod("nullInputStream"),
                    FileInputStream.class, bindgenEnv);
            FunctionDefinitionNode ff = BindgenNodeFactory.createFunctionDefinitionNode(jMethod, false);
            new PrintStream(System.out).println("******" + Formatter.format(ff.syntaxTree().toSourceCode()));
        } catch (NoSuchMethodException e) {
            new PrintStream(System.out).println(e);
        }
//        importDeclarationNode = importDeclarationNode.importKeyword().leadingMinutiae().addAll(emptyClassDescription);
//        new PrintStream(System.out).println("******" + Formatter.format().toSourceCode()));
//        new PrintStream(System.out).println("******" + Formatter.format(annotationNode.toSourceCode()));
//        new PrintStream(System.out).println("******" + Formatter.format(emptyClassDescription.toSourceCode()));
    }
}
