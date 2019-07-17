package org.ballerinalang.openapi.typemodel;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.model.BallerinaServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Java representation for OpenApi object.
 */
public class OpenApiType {

    private String moduleName;
    private String serviceName;
    private String definitionPath;
    private List<BallerinaServer> servers = new ArrayList<>();
    private List<OpenApiPathType> paths;
    private OpenApiComponentType component;
    private List<Tag> tags;

    public List<OpenApiPathType> getPaths() {
        return paths;
    }

    public void setPaths(List<OpenApiPathType> paths) {
        this.paths = paths;
    }

    public OpenApiComponentType getComponent() {
        return component;
    }

    public void setComponent(OpenApiComponentType component) {
        this.component = component;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDefinitionPath() {
        return definitionPath;
    }

    public void setDefinitionPath(String definitionPath) {
        this.definitionPath = definitionPath;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
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
