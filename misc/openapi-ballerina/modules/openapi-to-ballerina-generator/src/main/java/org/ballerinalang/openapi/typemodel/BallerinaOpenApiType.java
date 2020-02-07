package org.ballerinalang.openapi.typemodel;

import io.swagger.v3.oas.models.OpenAPI;
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
