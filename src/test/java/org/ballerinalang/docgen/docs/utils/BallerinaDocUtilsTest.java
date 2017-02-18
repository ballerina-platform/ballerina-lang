/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.docs.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Tests utility methods.
 */
public class BallerinaDocUtilsTest {

    @Test(description = "Test copy resources functionality")
    public void testCopyResources() throws IOException {
        String userDir = System.getProperty("user.dir");

        String outputFolderPath = userDir + File.separator + "target" + File.separator + "api-docs3";
        try {
            File outputPathFile = new File(outputFolderPath);
            Files.createDirectories(Paths.get(outputPathFile.getPath()));
            BallerinaDocUtils.copyResources("test-theme", outputFolderPath);

            File file1 =
                    new File(outputFolderPath + File.separator + "test-theme" + File.separator + "bar" + File.separator
                            + "file1.css");
            Assert.assertTrue(file1.exists());

            File file2 =
                    new File(outputFolderPath + File.separator + "test-theme" + File.separator + "bar" + File.separator
                            + "xyz" + File.separator + "file2.css");
            Assert.assertTrue(file2.exists());

            File file3 =
                    new File(outputFolderPath + File.separator + "test-theme" + File.separator + "foo" + File.separator
                            + "file3.css");
            Assert.assertTrue(file3.exists());

            File file4 =
                    new File(outputFolderPath + File.separator + "test-theme" + File.separator + "foo" + File.separator
                            + "xyz" + File.separator + "file4.css");
            Assert.assertTrue(file4.exists());
        } finally {
            BallerinaDocGenTestUtils.deleteDirectory(outputFolderPath);
        }
    }
}
