/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.launcher;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test Launcher utility methods
 *
 * @since 0.8.0
 */
public class LauncherTest {

    @Test(description = "Test getFileName method which returns the file name from the given path")
    public void testGetFileName() {
        Path p = Paths.get("/Users/john/Work/clones/ballerina/modules/foo.bal");
        String fileName = LauncherUtils.getFileName(p);
        Assert.assertEquals(fileName, "foo.bal");
    }

//    @Test(description = "Test getFileName method which returns the file name from the given Path")
//    public void testGetFileNameWindows() {
//        Path p = Paths.get("C:\\user\\docs\\foo.bal");
//        String fileName = Utils.getFileName(p);
//        Assert.assertEquals(fileName, "foo.bal");
//    }

    @Test(description = "Test makeFirstLetterUpperCase method which makes the first letter lowercase")
    public void testMakeFirstLetterUpperCase() {
        String actual = LauncherUtils.makeFirstLetterUpperCase("No file or directory found");
        Assert.assertEquals(actual, "no file or directory found");
    }


    @Test(description = "Test buildLangModel method which creates BallerinaFile from the given path")
    public void testBuildLangModel() {
        Path filePath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "balfiles", "foo.bal");
        BallerinaFile bFile = LauncherUtils.buildLangModel(filePath);

        Assert.assertEquals(bFile.getFunctions().length, 1);
    }

}
