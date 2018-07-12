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

package org.ballerina.parsing;

import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.testFramework.ParsingTestCase;
import io.ballerina.plugins.idea.BallerinaParserDefinition;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;

/**
 * Parsing tests.
 */
public class BallerinaParsingTest extends ParsingTestCase {

    private final String testDataPath = "../../composer/modules/integration-tests/src/test/resources/ballerina"
            + "-examples/examples";
    private final String expectedResultsPath = "src/test/resources/testData/parsing/BBE/expectedResults";

    public BallerinaParsingTest() {
        super("", "bal", new BallerinaParserDefinition());
    }

    @Override
    protected String getTestDataPath() {
        return testDataPath;
    }

    protected String getExpectedResultPath() {
        return expectedResultsPath;
    }

    @Override
    protected boolean skipSpaces() {
        return false;
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }

    public void testForBBE() {
        //This flag is used to include/filter BBE testerina files in lexer testing
        boolean includeTests = false;
        Path path = Paths.get(getTestDataPath());
        doTestDirectory(path, includeTests);
    }

    private void doTestDirectory(Path path, boolean includeTests) throws RuntimeException {
        try {
            File resource = path.toFile();
            if (!resource.exists()) {
                return;
            } else if (resource.isFile() && resource.getName().endsWith(myFileExt)) {
                doTest(resource, true);

                //if the resource is a directory, recursively test the sub directories/files accordingly
            } else if (resource.isDirectory() && (includeTests || !resource.getName().contains("tests"))) {
                DirectoryStream<Path> ds = Files.newDirectoryStream(path);
                for (Path subPath : ds) {
                    doTestDirectory(subPath, includeTests);
                }
                ds.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void doTest(File resource, boolean checkResult) {

        try {
            String name = resource.getName().replace("." + myFileExt, EMPTY_STRING);
            //Retrieves relative path of the file since loadFile() uses "myFullDataPath" as the source root
            String relativeFilePath = resource.getPath().replace(getTestDataPath(), EMPTY_STRING);
            String text = loadFile(relativeFilePath);
            myFile = createPsiFile(name, text);
            ensureParsed(myFile);
            assertEquals("light virtual file text mismatch", text,
                    ((LightVirtualFile) myFile.getVirtualFile()).getContent().toString());
            assertEquals("virtual file text mismatch", text, LoadTextUtil.loadText(myFile.getVirtualFile()));
            assertEquals("doc text mismatch", text, myFile.getViewProvider().getDocument().getText());
            assertEquals("psi text mismatch", text, myFile.getText());
            ensureCorrectReparse(myFile);
            if (checkResult) {
                checkResult(relativeFilePath, myFile);
            } else {
                toParseTreeText(myFile, skipSpaces(), includeRanges());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void checkResult(@NonNls String var1, PsiFile var2) throws IOException {
        doCheckResult((new File(getExpectedResultPath() + var1)).getParent(), var2, this.checkAllPsiRoots(),
                (new File(var1)).getName().replace("." + myFileExt, EMPTY_STRING), this.skipSpaces(),
                this.includeRanges());
    }
}

