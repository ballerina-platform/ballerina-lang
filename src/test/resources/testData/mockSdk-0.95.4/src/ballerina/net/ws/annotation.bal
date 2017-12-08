package ballerina.net.ws;

@Description {value:"Configuration for a WebSocket service."}
@Field {value:"basePath: Path of the WebSocket service"}
@Field {value:"subProtocols: Negotiable sub protocol by the service"}
@Field {value:"host: Host of the service"}
@Field {value:"port: Port number of the service"}
@Field {value:"wssPort: WSS port number of service"}
@Field {value:"idleTimeoutInSeconds: Idle timeout for the client connection. This can be triggered by putting onIdleTimeout resource in WS service."}
@Field {value:"keyStoreFile: File path to keystore file"}
@Field {value:"keyStorePassword: The keystore password"}
@Field {value:"certPassword: The certificate password"}
public annotation configuration attach service<> {
    string basePath;
    string[] subProtocols;
    string host;
    int port;
    int wssPort;
    int idleTimeoutInSeconds;
    string keyStoreFile;
    string keyStorePassword;
    string certPassword;
}

@Description {value:"Service annotation to mark a WS service as a client service for a WS client connector."}
public annotation clientService attach service<> {
}