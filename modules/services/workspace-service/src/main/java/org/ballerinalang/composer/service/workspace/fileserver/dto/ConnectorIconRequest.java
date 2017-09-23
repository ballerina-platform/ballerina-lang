package org.ballerinalang.composer.service.workspace.fileserver.dto;

/**
 * DTO for Connector Icon Request
 */
public class ConnectorIconRequest {

    private String connectorName;

    private String iconPath;

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public String getIconPath() {
        return iconPath;
    }
}
