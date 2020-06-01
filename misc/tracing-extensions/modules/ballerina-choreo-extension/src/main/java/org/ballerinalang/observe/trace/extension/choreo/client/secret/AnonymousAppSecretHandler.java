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

import java.io.File;
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
            return readStoreSecret(propertiesFilePath);
        } else {
            return generateSecret();
        }
    }

    private String readStoreSecret(Path propertiesFileLocation) throws IOException {
        try (InputStream inputStream = Files.newInputStream(propertiesFileLocation)) {
            Properties props = new Properties();
            props.load(inputStream);
            return props.getProperty(PROJECT_SECRET_CONFIG_KEY);
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
        // TODO store file in .config/choreo
        try (OutputStream outputStream = Files.newOutputStream(propertiesFilePath)) {
            Properties props = new Properties();
            props.setProperty(PROJECT_SECRET_CONFIG_KEY, appSecret);
            props.setProperty(PROJECT_OBSERVABILITY_ID_CONFIG_KEY, obsId);
            props.store(outputStream, null);
        }
    }
}
