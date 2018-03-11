package ballerina.net.http;

@Description {value:"Configuration struct for WebSocket client connection"}
@Field {value: "subProtocols: Negotiable sub protocols for the client"}
@Field {value: "parentConnectionID: Connection ID of the parent connection to which it should be bound to when connecting"}
@Field {value: "customHeaders: Custom headers which should be sent to the server"}
@Field {value: "idleTimeoutInSeconds: Idle timeout of the client. Upon timeout, onIdleTimeout resource in the client service will be triggered (if there is one defined)."}
public struct WebSocketClientEndpoint {
    string url;
    type callbackService;
    string [] subProtocols;
    map<string> customHeaders;
    int idleTimeoutInSeconds = -1;
}

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public native function <WebSocketClientEndpoint ep> init (string epName, ServiceEndpointConfiguration config);

@Description { value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during registration" }
public native function <WebSocketClientEndpoint h> register (type serviceType);

@Description { value:"Starts the registered service"}
@Return { value:"Error occured during registration" }
public native function <WebSocketClientEndpoint h> start ();

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
@Return { value:"Error occured during registration" }
public native function <WebSocketClientEndpoint h> getConnector () returns (WebSocketConnector wsConnector);

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public native function <WebSocketClientEndpoint h> stop ();
