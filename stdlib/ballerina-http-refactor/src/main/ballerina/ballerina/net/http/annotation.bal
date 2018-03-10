package ballerina.net.http;

///////////////////////////
/// Service Annotations ///
///////////////////////////
@Description {value: "Configuration for HTTP service"}
@Field {value: "endpoints: array of endpoints the service would be attached to"}
@Field {value: "lifetime: The life time of the service"}
@Field {value: "basePath: Service base path"}
@Field {value:"compressionEnabled: The status of compressionEnabled {default value : true (enabled)}"}
@Field {value:"allowOrigins: The array of origins with which the response is shared by the service"}
@Field {value:"allowCredentials: Specifies whether credentials are required to access the service"}
@Field {value:"allowMethods: The array of allowed methods by the service"}
@Field {value:"allowHeaders: The array of allowed headers by the service"}
@Field {value:"maxAge: The maximum duration to cache the preflight from client side"}
@Field {value:"maxUriLength: Maximum length allowed for the URL"}
@Field {value:"maxHeaderSize: Maximum size allowed for the headers"}
@Field {value:"maxEntityBodySize: Maximum size allowed for the entity body"}
@Field {value:"webSocketConfig: Annotation to define HTTP to WebSocket upgrade"}
public struct HttpServiceConfiguration {
    ServiceEndpoint[] endpoints;
    HttpServiceLifeTime lifetime;
    string basePath;
    boolean compressionEnabled;
    string[] allowOrigins;
    boolean allowCredentials;
    string[] allowMethods;
    string[] allowHeaders;
    int maxAge;
    string[] exposeHeaders;
    int maxUriLength;
    int maxHeaderSize;
    int maxEntityBodySize;
    WebSocketUpgradeConfig webSocket;
}


public struct WebSocketUpgradeConfig {
    string upgradePath;
    type upgradeService;
}


@Description {value:"Configuration for a WebSocket service."}
@Field {value: "endpoints: array of endpoints the service would be attached to"}
@Field {value:"basePath: Path of the WebSocket service"}
@Field {value:"subProtocols: Negotiable sub protocol by the service"}
@Field {value:"idleTimeoutInSeconds: Idle timeout for the client connection. This can be triggered by putting onIdleTimeout resource in WS service."}
public struct WebSocketServiceConfiguration {
    ServiceEndpoint[] endpoints;
    string basePath;
    string[] subProtocols;
    int idleTimeoutInSeconds;
}

@Description {value: "The life time of the service"}
@Field {value: "REQUEST: create a new instance of the service to process this request"}
@Field {value: "CONNECTION: create a new instance of the service for each connection"}
@Field {value: "SESSION: create a new instance of the service for each session"}
@Field {value: "SINGLETON: create a single instance of the service and use it to process all requests from an endpoint"}
public enum HttpServiceLifeTime {
    REQUEST,
    CONNECTION,
    SESSION,
    SINGLETON
}

@Description {value:"HTTP Configuration for service"}
public annotation <service> httpServiceConfig HttpServiceConfiguration;

@Description {value:"WebSocket Configuration for service"}
public annotation <service> webSocketServiceConfig WebSocketServiceConfiguration;


////////////////////////////
/// Resource Annotations ///
////////////////////////////
@Description {value:"Configuration for HTTP resource"}
@Field {value:"methods: The array of allowed HTTP methods"}
@Field {value:"path: The path of resource"}
@Field {value:"body: Inbound request entity body name which declared in signature"}
@Field {value:"consumes: The media types which are accepted by resource"}
@Field {value:"produces: The media types which are produced by resource"}
@Field {value:"allowOrigins: The array of origins with which the response is shared by the resource"}
@Field {value:"allowCredentials: Specifies whether credentials are required to access the resource"}
@Field {value:"allowMethods: The array of allowed methods by the resource"}
@Field {value:"allowHeaders: The array of allowed headers by the resource"}
@Field {value:"maxAge: The duration to cache the preflight from client side"}
@Field {value:"exposeHeaders: The array of allowed headers which are exposed to the client"}
public struct ResourceConfig {
    string[] methods;
    string path;
    string body;
    string[] consumes;
    string[] produces;
    string[] allowOrigins;
    boolean allowCredentials;
    string[] allowMethods;
    string[] allowHeaders;
    int maxAge;
    string[] exposeHeaders;
}

public annotation <resource> resourceConfig ResourceConfig;