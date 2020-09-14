/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.openapi.typemodel;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.model.BallerinaServer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the OpenApi Type Object.
 */
public class BallerinaOpenApiType {
    private String balModule;
    private String balServiceName;
    private String defPath;
    private List<BallerinaServer> servers = new ArrayList<>();
    private List<Tag> tags;
    private List<BallerinaOpenApiPath> pathList;
    private BallerinaOpenApiComponent component;
    private List<Tag> filteredTags;
    private List<Operation> operations;

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public List<Tag> getFilteredTags() {
        return filteredTags;
    }

    public void setFilteredTags(List<Tag> filteredTags) {
        this.filteredTags = filteredTags;
    }

    public BallerinaOpenApiComponent getComponent() {
        return component;
    }

    public void setComponent(BallerinaOpenApiComponent component) {
        this.component = component;
    }

    public List<BallerinaServer> getServers() {
        return servers;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getBalModule() {
        return balModule;
    }

    public void setBalModule(String balModule) {
        this.balModule = balModule;
    }

    public String getBalServiceName() {
        return balServiceName;
    }

    public void setBalServiceName(String balServiceName) {
        this.balServiceName = balServiceName;
    }

    public String getDefPath() {
        return defPath;
    }

    public void setDefPath(String defPath) {
        this.defPath = defPath;
    }

    public List<BallerinaOpenApiPath> getPathList() {
        return pathList;
    }

    public void setPathList(List<BallerinaOpenApiPath> pathList) {
        this.pathList = pathList;
    }

    /**
     * Extract endpoint information from OpenAPI server list.
     * If no servers were found, default {@link BallerinaServer} will be set as the server
     *
     * @param openAPI <code>OpenAPI</code> definition object with server details
     * @throws BallerinaOpenApiException on failure to parse {@code Server} list
     */
    public void setServers(OpenAPI openAPI) throws BallerinaOpenApiException {
        List<Server> serverList = openAPI.getServers();
        if (serverList == null) {
            BallerinaServer server = new BallerinaServer().getDefaultValue();
            servers.add(server);
            return;
        }

        serverList.forEach(server -> {
            try {
                // Note that only one base path is allowed. Though we extract base path per each server
                // defined in the Open Api definition, only the base path of first server will be used
                // in ballerina code generation. Ballerina all endpoints to be in a single base path
                BallerinaServer balServer = new BallerinaServer().buildContext(server);
                servers.add(balServer);
            } catch (BallerinaOpenApiException e) {
                // Ignore the exception, set default value for this server and move forward
                servers.add(new BallerinaServer().getDefaultValue());
            }
        });
    }
}
