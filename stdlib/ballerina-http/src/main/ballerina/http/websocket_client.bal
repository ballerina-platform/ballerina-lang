package ballerina.http;

public type WebSocketClient object {
    public {
    //ToDo: Make private: note private comes after public
        WebSocketConnector conn;
        WebSocketClientEndpointConfig config;
    //Public attributes
        map attributes;
        string id;
        string negotiatedSubProtocol;
        boolean isSecure;
        boolean isOpen;
    }
    new () {

    }
    @Description {value:"Gets called when the endpoint is being initialize during package init time"}
    @Param {value:"epName: The endpoint name"}
    @Param {value:"config: The ServiceEndpointConfiguration of the endpoint"}
    @Return {value:"Error occured during initialization"}
    public function init(WebSocketClientEndpointConfig config) {
        ep.config = config;
        ep.initEndpoint();
    }
    public native function initEndpoint();

    @Description {value:"gets called every time a service attaches itself to this endpoint - also happens at package init time"}
    @Param {value:"conn: The server connector connection"}
    @Param {value:"res: The outbound response message"}
    @Return {value:"Error occured during registration"}
    public native function register(typedesc serviceType);

    @Description {value:"Returns the connector that client code uses"}
    @Return {value:"The connector that client code uses"}
    @Return {value:"Error occured during registration"}
    public function getClient() returns (WebSocketConnector) {
        return h.conn;
    }

    @Description {value:"Starts the registered service"}
    @Return {value:"Error occured during registration"}
    public native function start();

    @Description {value:"Stops the registered service"}
    @Return {value:"Error occured during registration"}
    public function stop () {
        _ = h.getClient().close(1001, "The connection has been stopped");
    }
};

@Description {value:"Configuration struct for WebSocket client connection"}
@Field {value:"subProtocols: Negotiable sub protocols for the client"}
@Field {value:"parentConnectionID: Connection ID of the parent connection to which it should be bound to when connecting"}
@Field {value:"customHeaders: Custom headers which should be sent to the server"}
@Field {value:"idleTimeoutInSeconds: Idle timeout of the client. Upon timeout, onIdleTimeout resource in the client service will be triggered (if there is one defined)."}
public type WebSocketClientEndpointConfig {
    string url,
    typedesc callbackService,
    string[] subProtocols,
    map<string> customHeaders,
    int idleTimeoutInSeconds = -1,
};
