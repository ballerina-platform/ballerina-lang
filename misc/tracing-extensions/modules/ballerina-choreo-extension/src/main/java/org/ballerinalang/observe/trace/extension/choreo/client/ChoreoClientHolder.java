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

import io.ballerina.runtime.observability.ObservabilityConstants;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.observe.trace.extension.choreo.client.error.ChoreoClientException;
import org.ballerinalang.observe.trace.extension.choreo.client.secret.AnonymousAppSecretHandler;
import org.ballerinalang.observe.trace.extension.choreo.client.secret.AppSecretHandler;
import org.ballerinalang.observe.trace.extension.choreo.client.secret.LinkedAppSecretHandler;
import org.ballerinalang.observe.trace.extension.choreo.logging.LogFactory;
import org.ballerinalang.observe.trace.extension.choreo.logging.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.ballerinalang.observe.trace.extension.choreo.Constants.APPLICATION_ID_CONFIG;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.CHOREO_EXTENSION_NAME;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.DEFAULT_REPORTER_HOSTNAME;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.DEFAULT_REPORTER_PORT;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.DEFAULT_REPORTER_USE_SSL;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.EMPTY_APPLICATION_SECRET;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.REPORTER_HOST_NAME_CONFIG;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.REPORTER_PORT_CONFIG;
import static org.ballerinalang.observe.trace.extension.choreo.Constants.REPORTER_USE_SSL_CONFIG;

/**
 * Manages the Choreo Client used to communicate with the Choreo cloud.
 *
 * @since 2.0.0
 */
public class ChoreoClientHolder {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static ChoreoClient choreoClient;
    private static Set<AutoCloseable> choreoClientDependents = new HashSet<>();

    /**
     * Get the client that can be used to communicate with Choreo cloud.
     *
     * @return ChoreoClient
     */
    public static synchronized ChoreoClient getChoreoClient() throws ChoreoClientException {
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
            AppSecretHandler appSecretHandler;
            try {
                appSecretHandler = getAppSecretHandler(configRegistry);
            } catch (IOException e) {
                LOGGER.error("Failed to initialize Choreo client. " + e.getMessage());
                return null;
            }

            final ChoreoClient newChoreoClient =
                    initializeChoreoClient(configRegistry, appSecretHandler.getAppSecret());

            String nodeId = getNodeId();

            ChoreoClient.RegisterResponse registerResponse = newChoreoClient.register(metadataReader, nodeId);
            try {
                appSecretHandler.associate(registerResponse.getObsId());
            } catch (IOException e) {
                LOGGER.error("Error occurred while associating observability ID with secret. " + e.getMessage());
                return null;
            }
            LOGGER.info("visit " + registerResponse.getObsUrl().replaceAll("%", "%%")
                    + " to access observability data");

            createShutdownHook();
            choreoClient = newChoreoClient;
        }

        return choreoClient;
    }

    private static void createShutdownHook() {
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

    private static ChoreoClient initializeChoreoClient(ConfigRegistry configRegistry, String projectSecret) {
        String hostname = configRegistry.getConfigOrDefault(getFullQualifiedConfig(REPORTER_HOST_NAME_CONFIG),
                DEFAULT_REPORTER_HOSTNAME);
        int port = Integer.parseInt(configRegistry.getConfigOrDefault(getFullQualifiedConfig(REPORTER_PORT_CONFIG),
                String.valueOf(DEFAULT_REPORTER_PORT)));
        boolean useSSL = Boolean.parseBoolean(configRegistry.getConfigOrDefault(
                getFullQualifiedConfig(REPORTER_USE_SSL_CONFIG), String.valueOf(DEFAULT_REPORTER_USE_SSL)));
        return new ChoreoClient(hostname, port, useSSL, projectSecret);
    }

    private static AppSecretHandler getAppSecretHandler(ConfigRegistry configRegistry) throws IOException,
            ChoreoClientException {
        String appSecretFromConfig = configRegistry.getConfigOrDefault(getFullQualifiedConfig(APPLICATION_ID_CONFIG),
                EMPTY_APPLICATION_SECRET);

        if (EMPTY_APPLICATION_SECRET.equals(appSecretFromConfig)) {
            return new AnonymousAppSecretHandler();
        } else {
            return new LinkedAppSecretHandler(appSecretFromConfig);
        }
    }

    /**
     * Get the client that can be used to communicate with Choreo cloud. When the Choreo client is
     * closed the passed dependent object will also be closed.
     *
     * @param dependentObj Object to be closed when the Choreo client is closed
     * @return ChoreoClient
     */
    public static synchronized ChoreoClient getChoreoClient(AutoCloseable dependentObj) throws ChoreoClientException {
        final ChoreoClient client = getChoreoClient();
        choreoClientDependents.add(dependentObj);
        return client;
    }

    private static String getNodeId() {
        Path instanceIdConfigFilePath = ChoreoConfigHelper.getGlobalChoreoConfigDir().resolve("nodeId");

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

    public static String getFullQualifiedConfig(String configName) {
        return ObservabilityConstants.CONFIG_TABLE_OBSERVABILITY + "." + CHOREO_EXTENSION_NAME + "." + configName;
    }

}
