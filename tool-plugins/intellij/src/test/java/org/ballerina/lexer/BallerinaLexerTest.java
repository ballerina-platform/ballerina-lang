/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerina.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.testFramework.LexerTestCase;
import io.ballerina.plugins.idea.lexer.BallerinaLexerAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Lexer tests.
 */
public class BallerinaLexerTest extends LexerTestCase {

    private String getTestDataDirectoryPath() {
        return "src/test/resources/testData/lexer/BBE/";
    }

    //this test validates the lexer token generation for all the ballerina-by-example files
    public void testBBE() throws RuntimeException {

        //This flag is used to include/filter BBE testerina files in lexer testing
        boolean includeTests = false;

        Path path = Paths.get(getTestDataDirectoryPath());
        doTestDirectory(path, includeTests);
    }

    private void doTestDirectory(Path path, boolean includeTests) throws RuntimeException {
        try {
            File resource = path.toFile();
            if (!resource.exists()) {
                return;
            } else if (resource.isFile() && resource.getName().endsWith(".bal")) {
                doTestFile(resource);

                //if the resource is a directory, recursively test the sub directories/files accordingly
            } else if (resource.isDirectory() && (includeTests || !resource.getName().equals("tests"))) {
                DirectoryStream<Path> ds = Files.newDirectoryStream(path);
                for (Path subPath : ds) {
                    doTestDirectory(subPath, includeTests);
                }
                ds.close();
            }
        } catch (IOException e) {
            //throw new RuntimeException(e);
        }
    }

    private void doTestFile(File sourceFile) {
        try {
            String text = FileUtil.loadFile(sourceFile, CharsetToolkit.UTF8);
            String actual = printTokens(StringUtil.convertLineSeparators(text.trim()), 0);
            String pathname = sourceFile.getAbsolutePath().replace(".bal", "") + ".txt";
            File expectedResultFile = new File(pathname);
            assertSameLinesWithFile(expectedResultFile.getAbsolutePath(), actual);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Lexer createLexer() {
        return new BallerinaLexerAdapter();
    }

    @Override
    protected String getDirPath() {
        return getTestDataDirectoryPath();
    }
}
