/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.test;

import org.apache.commons.io.FilenameUtils;
import org.ballerinalang.langserver.test.template.RootTemplate;
import org.ballerinalang.langserver.test.template.io.FileTemplate;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class is responsible for generating tests for a given source file.
 *
 * @since 0.981.2
 */
public class TestGenerator {

    private TestGenerator() {
    }

    /**
     * Creates a test file for a given BLangPackage in source file path.
     *
     * @param sourceFilePath source file path
     * @param bLangPackage   {@link BLangPackage}
     * @throws TestGeneratorException when test case generation fails
     */
    public static String generateTestFile(Path sourceFilePath, BLangPackage bLangPackage)
            throws TestGeneratorException {
        //Check for tests folder, if not exists create a new folder
        File testsDir = createTestFolderIfNotExists(sourceFilePath);

        //Generate a unique name for the tests file
        String generatedFilename = generateTestFileName(sourceFilePath, testsDir);

        //Create root template
        RootTemplate ballerinaTemplate = new RootTemplate(sourceFilePath.toFile().getName(), bLangPackage);
        FileTemplate rootFileTemplate = new FileTemplate("rootTest.bal");

        //Render root template and write into test file.
        File testFile = testsDir.toPath().resolve(generatedFilename).toFile();
        try (BufferedWriter writer = Files.newBufferedWriter(testFile.toPath(), StandardCharsets.UTF_8)) {
            ballerinaTemplate.render(rootFileTemplate);
            writer.write(rootFileTemplate.getRenderedContent());
        } catch (Exception e) {
            throw new TestGeneratorException("Error occurred while writing test file: " + testFile.toString(), e);
        }

        return testFile.toString();
    }

    private static String generateTestFileName(Path sourceFilePath, File testsDir) {
        String fileName = FilenameUtils.removeExtension(sourceFilePath.toFile().getName());
        String generatedFilename = fileName + "_test" + ProjectDirConstants.BLANG_SOURCE_EXT;
        int count = 0;
        while (testsDir.toPath().resolve(generatedFilename).toFile().exists()) {
            count++;
            generatedFilename = fileName + count + "_test" + ProjectDirConstants.BLANG_SOURCE_EXT;
        }
        return generatedFilename;
    }

    private static File createTestFolderIfNotExists(Path sourceFilePath) {
        File testsDir = sourceFilePath.getParent().resolve(ProjectDirConstants.TEST_DIR_NAME).toFile();

        //Check for tests folder, if not exists create a new folder
        if (!testsDir.exists()) {
            testsDir.mkdir();
        }
        return testsDir;
    }
}
