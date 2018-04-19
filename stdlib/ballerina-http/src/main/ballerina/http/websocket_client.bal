package ballerina.http;

public type WebSocketClient object {
    public {
        string id;
        string negotiatedSubProtocol;
        boolean isSecure;
        boolean isOpen;
        map attributes;
    }

    private {
        WebSocketConnector conn;
        WebSocketClientEndpointConfig config;
    }

    @Description {value:"Gets called when the endpoint is being initialize during package init time"}
    @Param {value:"config: The ServiceEndpointConfiguration of the endpoint"}
    @Return {value:"Error occured during initialization"}
    public function init (WebSocketClientEndpointConfig config) {
        self.config = config;
        self.initEndpoint();
    }

    public native function initEndpoint();

    @Description {value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
    @Param {value:"res: The outbound response message"}
    @Return {value:"Error occured during registration"}
    public native function register(typedesc serviceType);

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    public function getCallerActions () returns (WebSocketConnector)  {
        return self.conn;
    }

    @Description {value:"Starts the registered service"}
    public native function start();

    @Description {value:"Stops the registered service"}
    public function stop () {
        WebSocketConnector webSocketConnector = self.getCallerActions();
        WebSocketConnectorError|() ignoredValue = webSocketConnector.close(1001, "The connection has been stopped");
    }
};

@Description {value:"Configuration struct for WebSocket client connection"}
@Field {value:"subProtocols: Negotiable sub protocols for the client"}
@Field {value:"parentConnectionID: Connection ID of the parent connection to which it should be bound to when connecting"}
@Field {value:"customHeaders: Custom headers which should be sent to the server"}
@Field {value:"idleTimeoutInSeconds: Idle timeout of the client. Upon timeout, onIdleTimeout resource in the client service will be triggered (if there is one defined)."}
public type WebSocketClientEndpointConfig {
    string url,
    typedesc? callbackService,
    string[] subProtocols,
    map<string> customHeaders,
    int idleTimeoutInSeconds = -1,
};
