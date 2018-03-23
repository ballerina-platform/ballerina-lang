///*
// *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
//package org.ballerinalang.lexer;
//
//import com.intellij.lexer.Lexer;
//import com.intellij.openapi.util.io.FileUtil;
//import com.intellij.openapi.util.text.StringUtil;
//import com.intellij.openapi.vfs.CharsetToolkit;
//import com.intellij.testFramework.LexerTestCase;
//import org.antlr.jetbrains.adaptor.lexer.ANTLRLexerAdaptor;
//import org.antlr.jetbrains.adaptor.lexer.PSIElementTypeFactory;
//import org.ballerinalang.plugins.idea.BallerinaLanguage;
//import org.ballerinalang.plugins.idea.grammar.BallerinaLexer;
//import org.ballerinalang.plugins.idea.grammar.BallerinaParser;
//
//import java.io.File;
//import java.io.IOException;
//
///**
// * Lexer tests.
// */
//public class BallerinaLexerTest extends LexerTestCase {
//
//    private String getTestDataPath() {
//        return "src/test/resources/testData/lexer";
//    }
//
//    @Override
//    protected void setUp() throws Exception {
//        PSIElementTypeFactory.defineLanguageIElementTypes(BallerinaLanguage.INSTANCE,
//                BallerinaParser.tokenNames, BallerinaParser.ruleNames);
//    }
//
//    @Override
//    protected void tearDown() throws Exception {
//        // This is needed because otherwise the superclass tries to delete a non existing temp directory.
//    }
//
//    public void testEchoService() {
//        doTest();
//    }
//
//    public void testHelloWorld() {
//        doTest();
//    }
//
//    public void testHelloWorldService() {
//        doTest();
//    }
//
//    public void testTweetMediumFeed() {
//        doTest();
//    }
//
//    public void testTweetOpenPR() {
//        doTest();
//    }
//
//    public void testTwitterConnector() {
//        doTest();
//    }
//
//    private void doTest() {
//        try {
//            File sourceFileName = new File(getDirPath() + "/" + getTestName(false) + ".bal");
//            String text = FileUtil.loadFile(sourceFileName, CharsetToolkit.UTF8);
//
//            String actual = printTokens(StringUtil.convertLineSeparators(text.trim()), 0);
//
//            String pathname = getDirPath() + "/" + getTestName(false) + ".txt";
//            File expectedResultFile = new File(pathname);
//            assertSameLinesWithFile(expectedResultFile.getAbsolutePath(), actual);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    protected Lexer createLexer() {
//        BallerinaLexer lexer = new BallerinaLexer(null);
//        return new ANTLRLexerAdaptor(BallerinaLanguage.INSTANCE, lexer);
//    }
//
//    @Override
//    protected String getDirPath() {
//        return getTestDataPath();
//    }
//}
