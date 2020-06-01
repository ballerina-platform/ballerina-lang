/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.observe.trace.extension.choreo.client.secret;

import org.ballerinalang.observe.trace.extension.choreo.client.ChoreoConfigHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

/**
 * Manages storage and retrieval of app secret for anonymous apps.
 */
public class AnonymousAppSecretHandler implements AppSecretHandler {
    public static final String PROJECT_SECRET_CONFIG_KEY = "PROJECT_SECRET";
    public static final String PROJECT_OBSERVABILITY_ID_CONFIG_KEY = "PROJECT_OBSERVABILITY_ID";

    private final String appSecret;
    private final Path propertiesFilePath;
    private boolean fileDirty = false;

    public AnonymousAppSecretHandler() throws IOException {
        propertiesFilePath = getPropertiesFilePath();
        appSecret = getAppSecretForProject(propertiesFilePath);
    }

    private Path getPropertiesFilePath() {
        final String workingDir = System.getProperty("user.dir");
        return Paths.get(workingDir + File.separator + ".choreoproject");
    }

    private String getAppSecretForProject(Path propertiesFilePath) throws IOException {
        if (Files.exists(propertiesFilePath)) {
            return readStoredSecret(propertiesFilePath);
        } else {
            fileDirty = true;
            return generateSecret();
        }
    }

    private String readStoredSecret(Path propertiesFileLocation) throws IOException {
        final String obsId = readStoredObsId(propertiesFileLocation);
        final Path projectSecretPath = getProjectSecretPath(obsId);

        if (!Files.exists(projectSecretPath)) {
            throw new FileNotFoundException(projectSecretPath.toString() + " is missing");
        }

        try (InputStream inputStream = Files.newInputStream(projectSecretPath)) {
            Properties props = new Properties();
            props.load(inputStream);
            return props.getProperty(PROJECT_SECRET_CONFIG_KEY);
        }
    }

    private String readStoredObsId(Path propertiesFileLocation) throws IOException {
        try (InputStream inputStream = Files.newInputStream(propertiesFileLocation)) {
            Properties props = new Properties();
            props.load(inputStream);
            return props.getProperty(PROJECT_OBSERVABILITY_ID_CONFIG_KEY);
        }
    }

    private String generateSecret() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getAppSecret() {
        return appSecret;
    }

    @Override
    public void associate(String obsId) throws IOException {
        if (!fileDirty) {
            return;
        }

        persistSecret(obsId);
    }

    private void persistSecret(String obsId) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(propertiesFilePath)) {
            Properties props = new Properties();
            props.setProperty(PROJECT_OBSERVABILITY_ID_CONFIG_KEY, obsId);
            props.store(outputStream, null);
        }

        createProjectSecretFile(obsId);
    }

    private void createProjectSecretFile(String obsId) throws IOException {
        final Path projectSecretPath = getProjectSecretPath(obsId);
        projectSecretPath.getParent().toFile().mkdirs();

        try (OutputStream outputStream = Files.newOutputStream(projectSecretPath)) {
            Properties props = new Properties();
            props.setProperty(PROJECT_SECRET_CONFIG_KEY, appSecret);
            props.store(outputStream, null);
        }
    }

    private Path getProjectSecretPath(String obsId) {
        return ChoreoConfigHelper.getGlobalChoreoConfigDir()
                                 .resolve(obsId)
                                 .resolve("projectsecret");
    }
}
