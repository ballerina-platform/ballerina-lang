package org.ballerinalang.langserver.codeaction.toml;

/**
 * Represents Listener information of a ballerina document.
 *
 * @since 2.0.0
 */
public class ListenerInfo {
    private String name;
    private int port;

    public ListenerInfo(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ListenerInfo{" +
                "name='" + name + '\'' +
                ", port=" + port +
                '}';
    }
}
