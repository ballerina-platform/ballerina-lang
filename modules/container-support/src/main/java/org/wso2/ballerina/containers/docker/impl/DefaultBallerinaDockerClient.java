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
 *
 */

package org.wso2.ballerina.containers.docker.impl;

import io.fabric8.docker.api.model.Image;
import io.fabric8.docker.api.model.ImageDelete;
import io.fabric8.docker.client.Config;
import io.fabric8.docker.client.ConfigBuilder;
import io.fabric8.docker.client.DockerClient;
import io.fabric8.docker.client.DockerClientException;
import io.fabric8.docker.dsl.EventListener;
import io.fabric8.docker.dsl.OutputHandle;
import org.apache.commons.io.FileUtils;
import org.wso2.ballerina.containers.docker.BallerinaDockerClient;
import org.wso2.ballerina.containers.docker.exception.DockerHandlerException;
import org.wso2.ballerina.containers.docker.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

/**
 * Default implementation of the {@link BallerinaDockerClient}.
 */
public class DefaultBallerinaDockerClient implements BallerinaDockerClient {

    private final CountDownLatch buildDone = new CountDownLatch(1);
    private final boolean[] buildErrors = {false};

    public String createServiceImage(String packageName, String dockerEnv, Path bPackagePath)
            throws DockerHandlerException, IOException, InterruptedException {

        return createImage(packageName, dockerEnv, bPackagePath, true);
    }

    public String createMainImage(String packageName, String dockerEnv, Path bPackagePath)
            throws DockerHandlerException, IOException, InterruptedException {

        return createImage(packageName, dockerEnv, bPackagePath, false);
    }

    private String createImage(String packageName, String dockerEnv, Path bPackagePath, boolean service)
            throws DockerHandlerException, IOException, InterruptedException {

        if (!Files.exists(bPackagePath)) {
            throw new DockerHandlerException("Cannot find Ballerina Package file: " + bPackagePath.toString());
        }

        String imageName = packageName.toLowerCase(Locale.getDefault()) + ":latest";

        // 1. Create a tmp docker context
        String tempDirName = "ballerina-docker-" + String.valueOf(Instant.now().getEpochSecond());
        Path tmpDir = Files.createTempDirectory(tempDirName);
        Files.createDirectory(Paths.get(tmpDir.toString() + "/files"));
        Files.copy(Paths.get(Utils.getResourceFile("docker/image/Dockerfile").getAbsolutePath()),
                Paths.get(tmpDir.toString() + "/Dockerfile"), StandardCopyOption.REPLACE_EXISTING);

        // 2. Copy Ballerina package
        Files.copy(bPackagePath, tmpDir.getRoot());

        // 3. Create a docker image from the temp context
        DockerClient client = getDockerClient(dockerEnv);
        OutputHandle buildHandle = client.image()
                .build()
                .withRepositoryName(imageName)
                .withNoCache()
                .alwaysRemovingIntermediate()
                .withBuildArgs("{\"SVC_MODE\":\"" + String.valueOf(service) + "\"}")
                .usingListener(new DockerBuilderEventListener())
                .writingOutput(System.out)
                .fromFolder(tmpDir.toString());

        buildDone.await();
        buildHandle.close();
        client.close();
        FileUtils.deleteDirectory(new File(tmpDir.toString()));

        return buildErrors[0] ? null : imageName;
//        return (!buildErrors[0]);
//        return true;
    }

    public boolean deleteImage(String packageName, String dockerEnv) {
        List<ImageDelete> imageDeleteList;
        try {
            imageDeleteList = getDockerClient(dockerEnv).image()
                    .withName(packageName.toLowerCase(Locale.getDefault()) + ":latest")
                    .delete()
                    .force()
                    .andPrune();

        } catch (DockerClientException e) {
            if (e.getMessage().contains("No such image")) {
                return false;
            }
            throw e;
        }

        for (ImageDelete imageDelete : imageDeleteList) {
            imageDelete.getDeleted();
            imageDelete.getUntagged();

//            if (StringUtils.isNotEmpty(imageDelete.getDeleted())) {
////                ("Deleted:" + imageDelete.getDeleted());
//            }
//            if (StringUtils.isNotEmpty(imageDelete.getUntagged())) {
////                ("Untagged:" + imageDelete.getUntagged());
//            }
        }

        return true;
    }

    public String getImage(String packageName, String dockerEnv) {
        DockerClient client = getDockerClient(dockerEnv);
        List<Image> images = client.image().list().filter(packageName).endImages();
        for (Image image : images) {
            String imageName = image.getRepoTags().get(0);
            if (imageName.equals(packageName + ":latest")) {
                return imageName;
            }
        }

        return null;
    }

//    public String runMainContainer(String dockerEnv, String serviceName)
//            throws InterruptedException, IOException, DockerHandlerException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        DockerClient client = getDockerClient(dockerEnv);
//        if (!isFunctionImage(client, serviceName)) {
//            throw new DockerHandlerException("Invalid image to run: " + serviceName.toLowerCase(Locale.getDefault()) +
//                    ":latest");
//        }
//
//        ContainerCreateResponse container = client.container().createNew()
//                .withName(serviceName + "-latest")
//                .withImage(serviceName.toLowerCase(Locale.getDefault()) + ":latest")
//                .done();
//
//        // TODO: throws EOFException here.
//        try (
//                OutputHandle logHandle = client.container().
//                        withName(container.getId())
//                        .logs()
//                        .writingOutput(outputStream)
//                        .writingError(outputStream)
//                        .display()
//        ) {
//
//            if (client.container().withName(container.getId()).start()) {
//////                ("Container started: " + container.getId());
//                Thread.sleep(10000);
////                client.container().withName(container.getId()).stop();
////                return IOUtils.toString(logHandle.getOutput(), "UTF-8");
//                client.container().withName(container.getId()).remove();
//                return new String(outputStream.toByteArray(), Charset.defaultCharset());
////                return "";
//            }
//        }
//
//        client.container().withName(container.getId()).remove();
//        return "";
//
//    }
//
//    public String runServiceContainer(String packageName, String dockerEnv) throws DockerHandlerException {
//        DockerClient client = getDockerClient(dockerEnv);
//        if (isFunctionImage(client, packageName)) {
//            throw new DockerHandlerException("Invalid image to run: " + packageName.toLowerCase(Locale.getDefault()) +
//                    ":latest");
//        }
//
//        ContainerCreateResponse container = client.container().createNew()
//                .withName(packageName + "-latest")
//                .withImage(packageName.toLowerCase(Locale.getDefault()) + ":latest")
//                .done();
//
//        client.container().withName(container.getId()).start();
////        if (client.container().withName(container.getId()).start()) {
//////            ("Container started: " + container.getId());
////        }
//
//        String dockerUrl;
//        if (dockerEnv == null) {
//            dockerUrl = "http://localhost:" + "9090" + "/";
//        } else {
//            dockerUrl = dockerEnv.substring(0, dockerEnv.lastIndexOf(":")) + "9090" + "/";
//        }
//
//        return dockerUrl;
//    }
//
//    public void stopContainer(String packageName, String dockerEnv) throws DockerHandlerException {
////        DockerClient client = getDockerClient(dockerEnv);
//        throw new DockerHandlerException("Not implemented!");
//    }

    private static DockerClient getDockerClient(String env) {
        DockerClient client;
        if (env == null) {
            env = "unix:///var/run/docker.sock";
        }

        Config dockerClientConfig = new ConfigBuilder()
                .withDockerUrl(env)
                .build();

        client = new io.fabric8.docker.client.DefaultDockerClient(dockerClientConfig);
        return client;
    }

//    private static boolean isFunctionImage(DockerClient client, String serviceName) {
//        for (String envVar : client.image()
//                .withName(serviceName.toLowerCase(Locale.getDefault()) + ":latest")
//                .inspect()
//                .getConfig()
//                .getEnv()) {
//
//            String[] envVarValue = envVar.split("=");
//            if (envVarValue[0].equals("SVC_MODE") && envVarValue[1].equals("false")) {
//                return true;
//            }
//        }
//
//        return false;
//    }

    /**
     * An {@link EventListener} implementation to listen to Docker build events.
     */
    private class DockerBuilderEventListener implements EventListener {
        @Override
        public void onSuccess(String successEvent) {
            buildDone.countDown();
        }

        @Override
        public void onError(String errorEvent) {
            buildErrors[0] = true;
            buildDone.countDown();
        }

        @Override
        public void onEvent(String event) {
            //..
        }
    }
}
