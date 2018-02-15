package org.ballerinalang.launcher.toml.model;

/**
 * Describes the proxy object.
 */
public class Proxy {
    private String host;
    private String port;
    private String userName;
    private String password;

    /**
     * Get host name.
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Set host name.
     * @param host host name
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get port.
     * @return port proxy server
     */
    public String getPort() {
        return port;
    }

    /**
     * Set the port of the proxy server.
     * @param port port of the proxy
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Get the username of the proxy server.
     * @return username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the username of the proxy server.
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Ge the password of the proxy server.
     * @return password of the proxy server
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password for the proxy server.
     * @param password password of the proxy
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
