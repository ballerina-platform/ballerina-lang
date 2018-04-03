/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.nativeimpl.functions.file;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test file operations.
 */
public class FileOperationTest {
    private CompileResult fileOperationProgramFile;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        fileOperationProgramFile = BCompileUtil.compile("test-src/file/file_ops.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/target";
    }

    /**
     * Will identify the absolute path from the relative.
     *
     * @param relativePath the relative file path location.
     * @return the absolute path.
     */
    private String getAbsoluteFilePath(String relativePath) throws URISyntaxException {
        URL fileResource = BServiceUtil.class.getClassLoader().getResource(relativePath);
        String pathValue = "";
        if (null != fileResource) {
            Path path = Paths.get(fileResource.toURI());
            pathValue = path.toAbsolutePath().toString();
        }
        return pathValue;
    }

    @Test(description = "Test 'writeRecords' function in ballerina.io package")
    public void testWriteRecords() throws URISyntaxException {
        String resourceToRead = "datafiles/io/text/utf8file.txt";
        BString absolutePathReturned;

        //Will initialize the channel
        String absoluteFilePath = getAbsoluteFilePath(resourceToRead);
        BValue[] args = {new BString(absoluteFilePath)};
        BValue[] returns = BRunUtil.invoke(fileOperationProgramFile, "testAbsolutePath", args);
        absolutePathReturned = (BString) returns[0];

        Assert.assertEquals(absolutePathReturned.stringValue(), absoluteFilePath);
    }
}
