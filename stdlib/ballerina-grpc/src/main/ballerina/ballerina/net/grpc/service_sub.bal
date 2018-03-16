package ballerina.net.grpc;

@Description {value:"gRPC Service Stub for outbound gRPC requests"}
public struct ServiceStub {
}

@Description {value:"The execute action implementation of the gRPC Connector."}
@Param {value:"Connection stub."}
@Param {value:"Any type of request parameters."}
public native function<ServiceStub ep>  blockingExecute (string methodID, any payload) (any , ConnectorError);

@Description {value:"The execute action implementation of the gRPC Connector."}
@Param {value:"Connection stub."}
@Param {value:"Any type of request parameters."}
public native function<ServiceStub ep>  nonBlockingExecute (string methodID, any payload, string listenerService) (ConnectorError);

@Description {value:"The execute action implementation of the gRPC Connector."}
@Param {value:"Connection stub."}
@Param {value:"Any type of request parameters."}
public native function<ServiceStub ep>  streamingExecute (string methodID, string listenerService) (ClientConnection , ConnectorError);


@Description {value:"Represents the gRPC client connector connection"}
@Field {value:"host: The server host name"}
@Field {value:"port: The server port"}
public struct ClientConnection {
    int port;
    string host;
}

@Description {value:"Sends outbound response to the caller"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The outbound response message"}
@Return {value:"Error occured during HTTP server connector respond"}
public native function <ClientConnection conn> send (any res) (ConnectorError);

@Description {value:"Informs the caller, server finished sending messages."}
@Param {value:"conn: The server connector connection"}
@Return {value:"Error occured during HTTP server connector respond"}
public native function <ClientConnection conn> complete () (ConnectorError);

@Description {value:"Forwards inbound response to the caller"}
@Param {value:"conn: The server connector connection"}
@Param {value:"res: The inbound response message"}
@Return {value:"Error occured during HTTP server connector forward"}
public native function <ClientConnection conn> error (ClientError clientError) (ConnectorError);