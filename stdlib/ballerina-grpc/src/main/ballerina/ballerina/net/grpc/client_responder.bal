package ballerina.net.grpc;

public struct ClientResponder {
    int id;
}

@Description {value:"Sends outbound response to the caller"}
@Param {value:"res: The outbound response message"}
@Return {value:"Error occured during server connector respond"}
public native function<ClientResponder ep> send(any res) (ConnectorError);

@Description {value:"Informs the caller, server finished sending messages"}
@Return {value:"Error occured during server connector respond"}
public native function<ClientResponder ep> complete() (ConnectorError);

@Description {value:"Checks whether the connection is closed by the caller."}
@Return {value:"Returns true if the connection is closed, false otherwise"}
public native function<ClientResponder ep> isCancelled () (boolean);

@Description {value:"Sends server error to the caller"}
@Param {value:"serverError: The server error message"}
@Return {value:"Error occured during server connector respond"}
public native function<ClientResponder ep> errorResponse(ServerError serverError) (ConnectorError);

@Description {value:"Get connection ID which is unique to the connection"}
@Return {value:"Connection ID"}
public native function<ClientResponder ep> getID() (string);

public struct Endpoint {
}

function <Endpoint s> getEndpoint() returns (Service) {
    return null;
}
