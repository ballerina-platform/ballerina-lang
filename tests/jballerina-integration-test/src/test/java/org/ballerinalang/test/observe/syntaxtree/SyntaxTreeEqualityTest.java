/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.observe.syntaxtree;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BMainInstance;
import org.ballerinalang.test.context.BallerinaTestException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class to test functionality of Ballerina syntax tree.
 *
 * @since 2201.6.0
 */
public class SyntaxTreeEqualityTest extends BaseTest {

    private static final String testFileLocation = Paths.get("src", "test", "resources", "observability")
            .toAbsolutePath().toString();
    private BMainInstance bMainInstance;

    @BeforeClass
    public void setup() throws BallerinaTestException {
        bMainInstance = new BMainInstance(balServer);
    }

    @Test
    public void testSyntaxTreeEquality() throws BallerinaTestException, IOException {
        String sourceRoot = testFileLocation + "/";
        String packageName = "syntaxtree_test";
        String jarPath = Paths.get(Paths.get(sourceRoot, packageName).toString(), "target", "bin",
                packageName + ".jar").toFile().getPath();
        String jsonFilePath = Paths.get("syntax-tree", "syntax-tree.json").toFile().getPath();

        // Build the package
        bMainInstance.runMain("build", new String[]{packageName}, null, null, null, sourceRoot);
        ZipFile jarFile = new ZipFile(jarPath);
        ZipEntry jsonFileEntry = jarFile.getEntry(jsonFilePath);
        if (jsonFileEntry == null) {
            throw new BallerinaTestException("Syntax tree json file not found in the jar");
        }
        InputStream jsonFileInputStream = jarFile.getInputStream(jsonFileEntry);
        String firstBuildJsonFileContent = new String(jsonFileInputStream.readAllBytes());

        // Build the package again
        bMainInstance.runMain("build", new String[]{packageName}, null, null, null, sourceRoot);
        jarFile = new ZipFile(jarPath);
        jsonFileEntry = jarFile.getEntry(jsonFilePath);
        if (jsonFileEntry == null) {
            throw new BallerinaTestException("Syntax tree json file not found in the jar");
        }
        jsonFileInputStream = jarFile.getInputStream(jsonFileEntry);
        String secondBuildJsonFileContent = new String(jsonFileInputStream.readAllBytes());

        if (!firstBuildJsonFileContent.equals(secondBuildJsonFileContent)) {
            throw new BallerinaTestException("Syntax tree json file content is not equal between two builds");
        }
    }
}
