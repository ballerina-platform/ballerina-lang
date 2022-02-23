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

package io.ballerina.projects.utils;

import io.ballerina.projects.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test {@code FileUtils} class.
 *
 * @since 2.0.0
 */
public class TestFileUtils {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources");
    private static final Path UTIL_RESOURCES = RESOURCE_DIRECTORY.resolve("utils");

    @DataProvider(name = "providePngFiles")
    public Object[][] providePngFiles() {
        Path pngsDir = UTIL_RESOURCES.resolve("pngs");
        return new Object[][]{
                {pngsDir.resolve("samplePng01.png"), true},
                {pngsDir.resolve("sampleSvgRenamedAsPng.png"), false},
                {pngsDir.resolve("sampleJpgRenamedAsPng.jpg"), false}
        };
    }

    @Test(dataProvider = "providePngFiles")
    public void testIsValidPng(Path imgPath, boolean isPng) throws IOException {
        Assert.assertEquals(FileUtils.isValidPng(imgPath), isPng);
    }
}
