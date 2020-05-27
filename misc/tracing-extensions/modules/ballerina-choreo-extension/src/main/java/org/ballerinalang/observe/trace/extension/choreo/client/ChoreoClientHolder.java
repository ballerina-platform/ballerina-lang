/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
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

package org.ballerinalang.observe.trace.extension.choreo.client;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.jvm.observability.ObservabilityConstants;
import org.ballerinalang.observe.trace.extension.choreo.logging.LogFactory;
import org.ballerinalang.observe.trace.extension.choreo.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.ballerinalang.observe.trace.extension.choreo.Constants.APPLICATION_ID_CONFIG;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.DEFAULT_REPORTER_HOSTNAME;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.DEFAULT_REPORTER_PORT;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.DEFAULT_REPORTER_USE_SSL;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.EMPTY_APPLICATION_SECRET;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.EXTENSION_NAME;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.REPORTER_HOST_NAME_CONFIG;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.REPORTER_PORT_CONFIG;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.REPORTER_USE_SSL_CONFIG;

/**
 * Manages the Choreo Client used to communicate with the Choreo cloud.
 */
public class ChoreoClientHolder {
    private static final Logger LOGGER = LogFactory.getLogger();
    public static final String PROJECT_SECRET_CONFIG_KEY = "PROJECT_SECRET";

    private static ChoreoClient choreoClient;
    private static Set<AutoCloseable> choreoClientDependents = new HashSet<>();

    /**
     * Get the client that can be used to communicate with Choreo cloud.
     *
     * @return ChoreoClient
     */
    public static synchronized ChoreoClient getChoreoClient() {
        if (choreoClient == null) {
            MetadataReader metadataReader;
            try {
                metadataReader = new MetadataReader();
                LOGGER.debug("Successfully read sequence diagram symbols");
            } catch (IOException e) {
                LOGGER.error("Failed to initialize Choreo client. " + e.getMessage());
                return null;
            }

            ConfigRegistry configRegistry = ConfigRegistry.getInstance();
            String hostname = configRegistry.getConfigOrDefault(getFullQualifiedConfig(REPORTER_HOST_NAME_CONFIG),
                    DEFAULT_REPORTER_HOSTNAME);
            int port = Integer.parseInt(configRegistry.getConfigOrDefault(getFullQualifiedConfig(REPORTER_PORT_CONFIG),
                    String.valueOf(DEFAULT_REPORTER_PORT)));
            boolean useSSL = Boolean.parseBoolean(configRegistry.getConfigOrDefault(
                    getFullQualifiedConfig(REPORTER_USE_SSL_CONFIG), String.valueOf(DEFAULT_REPORTER_USE_SSL)));

            String appSecret;
            try {
                appSecret = getAppSecret(configRegistry);
            } catch (IOException e) {
                LOGGER.error("Failed to initialize Choreo client. " + e.getMessage());
                return null;
            }

            String nodeId = getNodeId();
            initializeLinkWithChoreo(hostname, port, useSSL, metadataReader, nodeId, appSecret);
            Thread shutdownHook = new Thread(() -> {
                try {
                    choreoClientDependents.forEach(dependent -> {
                        try {
                            dependent.close();
                        } catch (Exception e) {
                            LOGGER.debug("failed to close dependent object" + e.getMessage());
                        }
                    });
                    choreoClient.close();
                } catch (Exception e) {
                    LOGGER.error("failed to close link with Choreo cloud");
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
        return choreoClient;
    }

    private static String getAppSecret(ConfigRegistry configRegistry) throws IOException {
        String appSecretFromConfig = configRegistry.getConfigOrDefault(getFullQualifiedConfig(APPLICATION_ID_CONFIG),
                EMPTY_APPLICATION_SECRET);

        if (EMPTY_APPLICATION_SECRET.equals(appSecretFromConfig)) {
            return locallyStoredAppSecret();
        } else {
            return appSecretFromConfig;
        }
    }

    private static String locallyStoredAppSecret() throws IOException {
        final String workingDir = System.getProperty("user.dir");
        Path projectPropertiesFileLocation = Paths.get(workingDir + File.separator + ".choreoProject");
        if (Files.exists(projectPropertiesFileLocation)) {
            return readStoreSecret(projectPropertiesFileLocation);
        } else {
            return generateAndStoreSecret(projectPropertiesFileLocation);
        }
    }

    private static String readStoreSecret(Path projectPropertiesFileLocation) throws IOException {
        try (InputStream inputStream = Files.newInputStream(projectPropertiesFileLocation)) {
            Properties props = new Properties();
            props.load(inputStream);
            return props.getProperty(PROJECT_SECRET_CONFIG_KEY);
        }
    }

    private static String generateAndStoreSecret(Path projectPropertiesFileLocation) throws IOException {
        final String generatedProjectSecret = UUID.randomUUID().toString();
        try (OutputStream outputStream = Files.newOutputStream(projectPropertiesFileLocation)) {
            Properties props = new Properties();
            props.setProperty(PROJECT_SECRET_CONFIG_KEY, generatedProjectSecret);
            props.store(outputStream, null);
        }
        return generatedProjectSecret;
    }

    /**
     * Get the client that can be used to communicate with Choreo cloud. When the Choreo client is
     * closed the passed dependent object will also be closed.
     *
     * @param dependentObj Object to be closed when the Choreo client is closed
     * @return ChoreoClient
     */
    public static synchronized ChoreoClient getChoreoClient(AutoCloseable dependentObj) {
        choreoClientDependents.add(dependentObj);
        return getChoreoClient();
    }

    private static Path getGlobalChoreoConfigDir() {
        final String userHome = System.getProperty("user.home");
        return Paths.get(userHome + File.separator + ".config" + File.separator + "choreo");
    }

    private static String getNodeId() {
        Path instanceIdConfigFilePath = getGlobalChoreoConfigDir().resolve("instanceId");

        String instanceId;
        if (!Files.exists(instanceIdConfigFilePath)) {
            instanceIdConfigFilePath.getParent().toFile().mkdirs();
            instanceId = UUID.randomUUID().toString();
            try {
                Files.write(instanceIdConfigFilePath, instanceId.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                LOGGER.error("could not write to " + instanceIdConfigFilePath.toString());
            }
        } else {
            try {
                instanceId = new String(Files.readAllBytes(instanceIdConfigFilePath), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.error("could not read from " + instanceIdConfigFilePath.toString());
                instanceId = UUID.randomUUID().toString();
            }
        }

        return instanceId;
    }

    private static void initializeLinkWithChoreo(String hostname, int port, boolean useSSL,
                                                 MetadataReader metadataReader, String nodeId, String appSecret) {
        choreoClient = new ChoreoClient(hostname, port, useSSL);
        String observabilityUrl = choreoClient.register(metadataReader, nodeId, appSecret);
        LOGGER.info("visit " + observabilityUrl.replaceAll("%", "%%") + " to access observability data");
    }

    public static String getFullQualifiedConfig(String configName) {
        return ObservabilityConstants.CONFIG_TABLE_TRACING + "." + EXTENSION_NAME + "." + configName;
    }
}
