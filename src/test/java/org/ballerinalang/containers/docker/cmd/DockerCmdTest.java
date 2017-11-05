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
import org.ballerinalang.containers.docker.BallerinaDockerClient;
import org.ballerinalang.containers.docker.exception.BallerinaDockerClientException;
import org.ballerinalang.containers.docker.utils.TestUtils;
import org.ballerinalang.containers.docker.utils.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    @Test
    public void testGetName() {
        Assert.assertEquals(new DockerCmd().getName(), "docker", "The Docker command name should be \"docker\"");
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testExecuteFailedMainImageCreation()
            throws NoSuchFieldException, IllegalAccessException, InstantiationException,
            FileNotFoundException, NoSuchMethodException, InvocationTargetException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        DockerCmd dockerCmd = cmdClass.newInstance();

        Field dockerClientField = cmdClass.getDeclaredField("dockerClient");
        dockerClientField.setAccessible(true);
        TestBallerinaDockerClient testDockerClient = new TestBallerinaDockerClient();
        testDockerClient.setMethodException("createMainImage", BallerinaDockerClientException.class);
        dockerClientField.set(dockerCmd, testDockerClient);

        Field packageNameField = cmdClass.getDeclaredField("packagePathNames");
        packageNameField.setAccessible(true);
        List<Path> packageList = TestUtils.getTestFunctionAsPathList();
        List<String> packageListStr = new ArrayList<>();
        packageListStr.add(packageList.get(0).toString());
        packageNameField.set(dockerCmd, packageListStr);

        Field assumeYesField = cmdClass.getDeclaredField("assumeYes");
        assumeYesField.setAccessible(true);
        assumeYesField.set(dockerCmd, true);

        ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
        PrintStream testPrintStream = new PrintStream(testOutputStream);
        Field sysout = cmdClass.getDeclaredField("outStream");
        sysout.setAccessible(true);
        sysout.set(dockerCmd, testPrintStream);

        dockerCmd.execute();
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testExecuteFailedServiceImageCreation()
            throws NoSuchFieldException, IllegalAccessException, InstantiationException,
            FileNotFoundException, NoSuchMethodException, InvocationTargetException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        DockerCmd dockerCmd = cmdClass.newInstance();

        Field dockerClientField = cmdClass.getDeclaredField("dockerClient");
        dockerClientField.setAccessible(true);
        TestBallerinaDockerClient testDockerClient = new TestBallerinaDockerClient();
        testDockerClient.setMethodException("createServiceImage", BallerinaDockerClientException.class);
        dockerClientField.set(dockerCmd, testDockerClient);

        Field packageNameField = cmdClass.getDeclaredField("packagePathNames");
        packageNameField.setAccessible(true);
        List<Path> packageList = TestUtils.getTestServiceAsPathList();
        List<String> packageListStr = new ArrayList<>();
        packageListStr.add(packageList.get(0).toString());
        packageNameField.set(dockerCmd, packageListStr);

        Field assumeYesField = cmdClass.getDeclaredField("assumeYes");
        assumeYesField.setAccessible(true);
        assumeYesField.set(dockerCmd, true);

        ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
        PrintStream testPrintStream = new PrintStream(testOutputStream);
        Field sysout = cmdClass.getDeclaredField("outStream");
        sysout.setAccessible(true);
        sysout.set(dockerCmd, testPrintStream);

        dockerCmd.execute();
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testExecuteInvalidArchiveType()
            throws NoSuchFieldException, IllegalAccessException, InstantiationException,
            FileNotFoundException, NoSuchMethodException, InvocationTargetException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        DockerCmd dockerCmd = cmdClass.newInstance();

        Field dockerClientField = cmdClass.getDeclaredField("dockerClient");
        dockerClientField.setAccessible(true);
        TestBallerinaDockerClient testDockerClient = new TestBallerinaDockerClient();
        dockerClientField.set(dockerCmd, testDockerClient);

        Field packageNameField = cmdClass.getDeclaredField("packagePathNames");
        packageNameField.setAccessible(true);
        List<String> packageListStr = new ArrayList<>();
        packageListStr.add(Utils.getResourceFile("ballerina/TestFunction.blz").getAbsolutePath());
        packageNameField.set(dockerCmd, packageListStr);

        Field assumeYesField = cmdClass.getDeclaredField("assumeYes");
        assumeYesField.setAccessible(true);
        assumeYesField.set(dockerCmd, true);

        ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
        PrintStream testPrintStream = new PrintStream(testOutputStream);
        Field sysout = cmdClass.getDeclaredField("outStream");
        sysout.setAccessible(true);
        sysout.set(dockerCmd, testPrintStream);

        dockerCmd.execute();
    }

    @Test(expectedExceptions = {RuntimeException.class})
    public void testExecuteNonExistingPackage()
            throws NoSuchFieldException, IllegalAccessException, InstantiationException,
            FileNotFoundException, NoSuchMethodException, InvocationTargetException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        DockerCmd dockerCmd = cmdClass.newInstance();

        Field dockerClientField = cmdClass.getDeclaredField("dockerClient");
        dockerClientField.setAccessible(true);
        TestBallerinaDockerClient testDockerClient = new TestBallerinaDockerClient();
        dockerClientField.set(dockerCmd, testDockerClient);

        Field packageNameField = cmdClass.getDeclaredField("packagePathNames");
        packageNameField.setAccessible(true);
        List<String> packageListStr = new ArrayList<>();
        ClassLoader classLoader = Utils.class.getClassLoader();
        URL resource = classLoader.getResource("ballerina");
        packageListStr.add(resource.toString() + "/NonExistent.arc");
        packageNameField.set(dockerCmd, packageListStr);

        Field assumeYesField = cmdClass.getDeclaredField("assumeYes");
        assumeYesField.setAccessible(true);
        assumeYesField.set(dockerCmd, true);

        ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
        PrintStream testPrintStream = new PrintStream(testOutputStream);
        Field sysout = cmdClass.getDeclaredField("outStream");
        sysout.setAccessible(true);
        sysout.set(dockerCmd, testPrintStream);

        dockerCmd.execute();
    }


    @Test(expectedExceptions = {RuntimeException.class})
    public void testExecuteNullPackageNames()
            throws NoSuchFieldException, IllegalAccessException, InstantiationException,
            FileNotFoundException, NoSuchMethodException, InvocationTargetException {

        Class<DockerCmd> cmdClass = DockerCmd.class;
        DockerCmd dockerCmd = cmdClass.newInstance();

        Field dockerClientField = cmdClass.getDeclaredField("dockerClient");
        dockerClientField.setAccessible(true);
        TestBallerinaDockerClient testDockerClient = new TestBallerinaDockerClient();
        dockerClientField.set(dockerCmd, testDockerClient);

        Field assumeYesField = cmdClass.getDeclaredField("assumeYes");
        assumeYesField.setAccessible(true);
        assumeYesField.set(dockerCmd, true);

        ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
        PrintStream testPrintStream = new PrintStream(testOutputStream);
        Field sysout = cmdClass.getDeclaredField("outStream");
        sysout.setAccessible(true);
        sysout.set(dockerCmd, testPrintStream);

        dockerCmd.execute();
    }

    /**
     * Test implementation of {@link BallerinaDockerClient} to mock Docker layer.
     */
    class TestBallerinaDockerClient implements BallerinaDockerClient {
        private Map<String, Class> methodExceptions = new HashMap<>();

        public void setMethodException(String methodName, Class exceptionClass) {
            methodExceptions.put(methodName, exceptionClass);
        }

        @Override
        public String createServiceImage(String packageName, String dockerEnv, List<Path> bPackagePaths,
                                         String imageName, String imageVersion)
                throws BallerinaDockerClientException, IOException, InterruptedException {

//            Thread.currentThread().getStackTrace()[1] // get currently running method name
            String methodName = "createServiceImage";
            if (methodExceptions.containsKey(methodName)) {
                Class exceptionClass = methodExceptions.get(methodName);
                String name = exceptionClass.getSimpleName();
                switch (name) {
                    case "BallerinaDockerClientException":
                        throw new BallerinaDockerClientException("Test exception");
                    case "IOException":
                        throw new IOException("Test exception");
                    case "InterruptedException":
                        throw new InterruptedException("Test exception");
                }
            }

            return packageName.toLowerCase(Locale.getDefault()) + ":" + Constants.IMAGE_VERSION_LATEST;
        }

        @Override
        public String createServiceImage(String serviceName, String dockerEnv, String ballerinaConfig,
                                         String imageName, String imageVersion)
                throws InterruptedException, BallerinaDockerClientException, IOException {

            throw new BallerinaDockerClientException("Not Implemented.");
        }

        @Override
        public String createMainImage(String packageName, String dockerEnv, List<Path> bPackagePaths,
                                      String imageName, String imageVersion)
                throws BallerinaDockerClientException, IOException, InterruptedException {

            String methodName = "createMainImage";
            if (methodExceptions.containsKey(methodName)) {
                Class exceptionClass = methodExceptions.get(methodName);
                String name = exceptionClass.getSimpleName();
                switch (name) {
                    case "BallerinaDockerClientException":
                        throw new BallerinaDockerClientException("Test exception");
                    case "IOException":
                        throw new IOException("Test exception");
                    case "InterruptedException":
                        throw new InterruptedException("Test exception");
                }
            }

            return packageName.toLowerCase(Locale.getDefault()) + ":" + Constants.IMAGE_VERSION_LATEST;
        }

        @Override
        public String createMainImage(String packageName, String dockerEnv, Path bPackagePaths,
                                      String imageName, String imageVersion)
                throws BallerinaDockerClientException, IOException, InterruptedException {
            throw new BallerinaDockerClientException("Not Implemented.");
        }

        @Override
        public String createMainImage(String mainPackageName, String dockerEnv, String ballerinaConfig,
                                      String imageName, String imageVersion)
                throws InterruptedException, BallerinaDockerClientException, IOException {

            throw new BallerinaDockerClientException("Not Implemented.");
        }

        @Override
        public boolean deleteImage(String packageName, String dockerEnv, String imageName, String imageVersion)
                throws BallerinaDockerClientException {

            throw new BallerinaDockerClientException("Not Implemented.");
        }

        @Override
        public String getImage(String packageName, String dockerEnv) {
            return null;
        }

        @Override
        public String getBuildError() {
            return null;
        }
    }
}
