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

package org.ballerina.formatting;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.ballerina.BallerinaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;

/**
 * Formatting tests.
 */
public class BallerinaFormattingTest extends BallerinaCodeInsightFixtureTestCase {

    private final String testDataPath = "src/test/resources/testData/formatting/BBE/";
    private final String expectedResultsPath = "src/test/resources/testData/formatting/BBE/expectedResults";

    @Override
    protected String getTestDataPath() {
        return testDataPath;
    }

    protected String getExpectedResultsPath() {
        return expectedResultsPath;
    }

    //this test validates the formatting for the ballerina-by-examples
    public void testForBBE() throws RuntimeException {
        //This flag is used to include/filter BBE testerina files for the formatting testing
        boolean includeTests = false;
        Path path = Paths.get(getTestDataPath());
        doTestDirectory(path, includeTests);
    }

    private void doTestDirectory(Path path, boolean includeTests) throws RuntimeException {
        try {
            File resource = path.toFile();
            if (!resource.exists()) {
                return;
            } else if (resource.isFile() && resource.getName().endsWith(".bal")) {
                doTest(resource, null);

                //if the resource is a directory, recursively test the sub directories/files accordingly
            } else if (resource.isDirectory() && (includeTests || !resource.getName().contains("tests"))) {
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

    private void doTest(File resource, @Nullable Character c) {
        String relativeFilePath = resource.getPath().replace(getTestDataPath(), EMPTY_STRING);
        myFixture.configureByFile(relativeFilePath);
        if (c == null) {
            WriteCommandAction.runWriteCommandAction(myFixture.getProject(), () -> {
                CodeStyleManager.getInstance(getProject()).reformat(myFixture.getFile());
            });
        } else {
            myFixture.type(c);
        }
        myFixture.checkResultByFile(relativeFilePath);
        // To debug - Comment the above line and uncomment these lines and add expected result.
        // String result = "";
        // myFixture.checkResult(result);
    }
}
