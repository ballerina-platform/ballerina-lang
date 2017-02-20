/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.containers.docker.cmd;

import org.ballerinalang.containers.Constants;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Test DockerCmd functionality.
 */
public class DockerCmdTest {

    @Test
    public void testImageNameExtractionNoVersion()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        Method getImageNameDetailsMethod = cmdClass.getDeclaredMethod("getImageNameDetails",
                String.class, String.class);
        getImageNameDetailsMethod.setAccessible(true);
        String imageName = "someImageName";
        String packageName = "packageName";
        String[] imageNameParts = (String[]) getImageNameDetailsMethod.invoke(cmdClass, imageName, packageName);
        Assert.assertEquals(imageNameParts.length, 2, "Invalid number of values returned");
        Assert.assertEquals(imageNameParts[0], imageName.toLowerCase(Locale.getDefault()),
                "Invalid image name returned");
        Assert.assertEquals(imageNameParts[1], Constants.IMAGE_VERSION_LATEST,
                "Invalid image version returned");
    }

    @Test
    public void testImageNameExtractionWithVersion()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        Method getImageNameDetailsMethod = cmdClass.getDeclaredMethod("getImageNameDetails",
                String.class, String.class);
        getImageNameDetailsMethod.setAccessible(true);
        String imageName = "someImageName";
        String imageVersion = "0.0.1";
        String packageName = "packageName";
        String[] imageNameParts = (String[]) getImageNameDetailsMethod.invoke(cmdClass,
                imageName + ":" + imageVersion, packageName);
        Assert.assertEquals(imageNameParts.length, 2, "Invalid number of values returned");
        Assert.assertEquals(imageNameParts[0], imageName.toLowerCase(Locale.getDefault()),
                "Invalid image name returned");
        Assert.assertEquals(imageNameParts[1], imageVersion,
                "Invalid image version returned");
    }

    @Test
    public void testImageNameExtractionNoGivenName()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        Method getImageNameDetailsMethod = cmdClass.getDeclaredMethod("getImageNameDetails",
                String.class, String.class);
        getImageNameDetailsMethod.setAccessible(true);
        String packageName = "packageName";
        String[] imageNameParts = (String[]) getImageNameDetailsMethod.invoke(cmdClass, null, packageName);
        Assert.assertEquals(imageNameParts.length, 2, "Invalid number of values returned");
        Assert.assertEquals(imageNameParts[0], packageName.toLowerCase(Locale.getDefault()),
                "Invalid image name returned");
        Assert.assertEquals(imageNameParts[1], Constants.IMAGE_VERSION_LATEST,
                "Invalid image version returned");
    }
}
