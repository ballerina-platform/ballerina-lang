/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.swagger.model;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Wrapper for {@link Server} from Swagger parser. This model is used to extract and keep Ballerina endpoint details
 * from a swagger {@link Server} instance.
 */
public class BallerinaServer implements BallerinaSwaggerObject<BallerinaServer, Server> {
    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;

    private String host;
    private int port;
    private String basePath;
    private String description;
    private Server server;

    /**
     * Builds a {@code BallerinaServer} instance from a {@link Server} instance.
     * If default server definition was found result of {@link BallerinaOpenApi#getDefaultValue()}
     * will be set as the server.
     *
     * @param server Open Api server definition
     * @return Parsed version of {@link Server} as a {@link BallerinaServer}
     * @throws BallerinaOpenApiException
     */
    @Override
    public BallerinaServer buildContext(Server server) throws BallerinaOpenApiException {
        this.server = server;

        // OAS spec default for empty server definition is object with url as "/".
        // This check is to overcome possible errors due to that
        if (server == null || "/".equals(server.getUrl())) {
            return getDefaultValue();
        }
        this.description = server.getDescription();

        URL url;
        try {
            String resolvedUrl = buildUrl(server.getUrl(), server.getVariables());
            url = new URL(resolvedUrl);
            this.host = url.getHost();
            this.basePath = url.getPath();
            this.port = url.getPort();
            boolean isHttps = "https".equalsIgnoreCase(url.getProtocol());

            if (isHttps) {
                this.port = this.port == -1 ? HTTPS_PORT : this.port;
            } else {
                this.port = this.port == -1 ? HTTP_PORT : this.port;
            }
        } catch (MalformedURLException e) {
            throw new BallerinaOpenApiException("Failed to read endpoint details of the server: " + server.getUrl(), e);
        }

        return this;
    }

    @Override
    public BallerinaServer buildContext(Server server, OpenAPI openAPI) throws BallerinaOpenApiException {
        return buildContext(server);
    }

    @Override
    public BallerinaServer getDefaultValue() {
        this.host = "localhost";
        this.port = 80;
        this.basePath = "/";

        return this;
    }

    /**
     * If there are template values in the {@code absUrl} derive resolved url using {@code variables}.
     *
     * @param absUrl abstract url with template values
     * @param variables variable values to populate the url template
     * @return resolved url
     */
    private String buildUrl(String absUrl, ServerVariables variables) {
        String url = absUrl;

        if (variables != null) {
            for (Map.Entry<String, ServerVariable> entry : variables.entrySet()) {
                // According to the oas spec, default value must be specified
                String replaceKey = "\\{" + entry.getKey() + '}';
                url = url.replaceAll(replaceKey, entry.getValue().getDefault());
            }
        }

        return url;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getDescription() {
        return description;
    }

    public Server getServer() {
        return server;
    }
}
