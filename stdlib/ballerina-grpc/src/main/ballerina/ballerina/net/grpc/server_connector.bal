package ballerina.net.grpc;

public connector ServerConnector (int id) {
    @Description {value:"Sends outbound response to the caller"}
    @Param {value:"conn: The server connector connection"}
    @Param {value:"res: The outbound response message"}
    @Return {value:"Error occured during HTTP server connector respond"}
    native action send(any res) (ConnectorError);

    @Description {value:"Informs the caller, server finished sending messages."}
    @Param {value:"conn: The server connector connection"}
    @Return {value:"Error occured during HTTP server connector respond"}
    native action complete() (ConnectorError);

    @Description {value:"Checks whether the connection is closed by the caller."}
    @Param {value:"conn: The server connector connection"}
    @Return {value:"Returns true if the connection is closed, false otherwise"}
    native action isCancelled () (boolean);

    @Description {value:"Forwards inbound response to the caller"}
    @Param {value:"conn: The server connector connection"}
    @Param {value:"res: The inbound response message"}
    @Return {value:"Error occured during HTTP server connector forward"}
    native action errorResponse(ServerError serverError) (ConnectorError);
}


