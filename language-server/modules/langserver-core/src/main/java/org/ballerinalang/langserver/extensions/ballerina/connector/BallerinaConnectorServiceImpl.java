/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.connector;

import com.moandjiezana.toml.Toml;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.langserver.LSGlobalContext;
import org.eclipse.lsp4j.Position;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

import static org.ballerinalang.langserver.compiler.LSClientLogger.logError;

/**
 * Implementation of the BallerinaConnectorService.
 *
 * @since 2.0.0
 */
public class BallerinaConnectorServiceImpl implements BallerinaConnectorService {

    public static final String DEFAULT_CONNECTOR_FILE_KEY = "DEFAULT_CONNECTOR_FILE";
    private String connectorConfig;

    public BallerinaConnectorServiceImpl(LSGlobalContext lsGlobalContext) {
        connectorConfig = System.getenv(DEFAULT_CONNECTOR_FILE_KEY);
        if (connectorConfig == null) {
            connectorConfig = System.getProperty(DEFAULT_CONNECTOR_FILE_KEY);
        }
    }

    @Override
    public CompletableFuture<BallerinaConnectorsResponse> connectors() {
        try {
            BallerinaConnectorsResponse response = getConnectorConfig();
            return CompletableFuture.supplyAsync(() -> response);
        } catch (IOException e) {
            String msg = "Operation 'ballerinaConnector/connectors' failed!";
            logError(msg, e, null, (Position) null);
        }
        return CompletableFuture.supplyAsync(BallerinaConnectorsResponse::new);
    }

    private BallerinaConnectorsResponse getConnectorConfig() throws IOException {
        try (InputStream inputStream = new FileInputStream(new File(connectorConfig));) {
            Toml toml;
            try {
                toml = new Toml().read(inputStream);
            } catch (IllegalStateException e) {
                throw new BLangCompilerException("invalid connector.toml due to " + e.getMessage());
            }
            return toml.to(BallerinaConnectorsResponse.class);
        }
    }
}
