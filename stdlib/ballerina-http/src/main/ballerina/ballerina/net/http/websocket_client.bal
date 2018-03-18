package ballerina.net.http;

public struct WebSocketClient {
    string epName;
    WebSocketClientEndpointConfig config;
}

@Description {value:"Configuration struct for WebSocket client connection"}
@Field {value: "subProtocols: Negotiable sub protocols for the client"}
@Field {value: "parentConnectionID: Connection ID of the parent connection to which it should be bound to when connecting"}
@Field {value: "customHeaders: Custom headers which should be sent to the server"}
@Field {value: "idleTimeoutInSeconds: Idle timeout of the client. Upon timeout, onIdleTimeout resource in the client service will be triggered (if there is one defined)."}
public struct WebSocketClientEndpointConfig {
    string url;
    type callbackService;
    string [] subProtocols;
    map<string> customHeaders;
    int idleTimeoutInSeconds = -1;
}

//TODO: This throws errors. Fix it.
//@Description { value:"Gets called when the endpoint is being initialize during package init time"}
//@Param { value:"epName: The endpoint name" }
//@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
//@Return { value:"Error occured during initialization" }
//public function <WebSocketClient ep> init (string epName, WebSocketClientEndpointConfig config){
//    ep.epName = epName;
//    ep.config = config;
//    ep.initEndpoint();
//}

@Description { value:"Gets called when the endpoint is being initialize during package init time"}
@Param { value:"epName: The endpoint name" }
@Param { value:"config: The ServiceEndpointConfiguration of the endpoint" }
@Return { value:"Error occured during initialization" }
public function <WebSocketClient ep> init (WebSocketClientEndpointConfig config){
    ep.config = config;
    ep.initEndpoint();
}

public native function<WebSocketClient ep> initEndpoint ();


@Description { value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
@Param { value:"conn: The server connector connection" }
@Param { value:"res: The outbound response message" }
@Return { value:"Error occured during registration" }
public native function <WebSocketClient h> register (type serviceType);

@Description { value:"Starts the registered service"}
@Return { value:"Error occured during registration" }
public native function <WebSocketClient h> start ();

//TODO:This throws errors. Fix it.
//@Description { value:"Returns the connector that client code uses"}
//@Return { value:"The connector that client code uses" }
//@Return { value:"Error occured during registration" }
//public function <WebSocketClient h> getClient () returns (WebSocketConnector) {
//
//    //WebSocketConnector wsConnector = new wsConnector(h.config.customHeaders);
//    //return wsConnector;
//}

@Description { value:"Returns the connector that client code uses"}
@Return { value:"The connector that client code uses" }
@Return { value:"Error occured during registration" }
public native function <WebSocketClient h> getClient () (WebSocketConnector);

@Description { value:"Stops the registered service"}
@Return { value:"Error occured during registration" }
public native function <WebSocketClient h> stop ();
