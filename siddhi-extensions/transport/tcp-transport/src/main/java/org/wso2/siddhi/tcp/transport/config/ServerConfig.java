package org.wso2.siddhi.tcp.transport.config;

public class ServerConfig {
    private int receiverThreads = 0;
    private int workerThreads = 0;
    private int port = 8080;
    private String host = "0.0.0.0";

    public int getReceiverThreads() {
        return receiverThreads;
    }

    public void setReceiverThreads(int receiverThreads) {
        this.receiverThreads = receiverThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
