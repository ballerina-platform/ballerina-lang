/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.testFramework.LexerTestCase;
import io.ballerina.plugins.idea.lexer.BallerinaLexerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;

/**
 * Lexer tests.
 */
public class BallerinaLexerTest extends LexerTestCase {

    private String getTestDataDirectoryPath() {
        return "../../composer/modules/integration-tests/src/test/resources/ballerina-examples/examples/";
    }

    private String getExpectedResultDirectoryPath() {
        return "src/test/resources/testData/lexer/BBE/expectedResults/";
    }

    // This test validates the lexer token generation for the ballerina-by-examples
    public void testForBBE() throws RuntimeException, FileNotFoundException {
        Path path = Paths.get(getTestDataDirectoryPath());
        if (!path.toFile().exists()) {
            throw new FileNotFoundException(path.toString());
        }
        doTestDirectory(path);
    }

    private void doTestDirectory(Path path) throws RuntimeException {
        try {
            File resource = path.toFile();
            if (resource.exists()) {
                if (resource.isFile() && resource.getName().endsWith(".bal")) {
                    doTestFile(resource);
                    //if the resource is a directory, recursively tests the sub directories/files accordingly,
                    // excluding tests folders
                } else if (resource.isDirectory() && !resource.getName().contains("tests")) {
                    DirectoryStream<Path> ds = Files.newDirectoryStream(path);
                    for (Path subPath : ds) {
                        doTestDirectory(subPath);
                    }
                    ds.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doTestFile(@NotNull File sourceFile) {
        try {
            String text = FileUtil.loadFile(sourceFile, CharsetToolkit.UTF8);
            String actual = printTokens(StringUtil.convertLineSeparators(text.trim()), 0);
            String relativePath = sourceFile.getPath().replace(getTestDataDirectoryPath(), EMPTY_STRING);
            String pathname = (getExpectedResultDirectoryPath() + relativePath).replace(".bal", "") + ".txt";
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
