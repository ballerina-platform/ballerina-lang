package ballerina.net.grpc;

public connector ServerConnector (int id) {
    @Description {value:"Sends outbound response to the caller"}
    @Param {value:"res: The outbound response message"}
    @Return {value:"Error occured during server connector respond"}
    native action send(any res) (ConnectorError);

    @Description {value:"Informs the caller, server finished sending messages"}
    @Return {value:"Error occured during server connector respond"}
    native action complete() (ConnectorError);

    @Description {value:"Checks whether the connection is closed by the caller."}
    @Return {value:"Returns true if the connection is closed, false otherwise"}
    native action isCancelled () (boolean);

    @Description {value:"Sends server error to the caller"}
    @Param {value:"serverError: The server error message"}
    @Return {value:"Error occured during server connector respond"}
    native action errorResponse(ServerError serverError) (ConnectorError);

    @Description {value:"Get connection ID which is unique to the connection"}
    @Return {value:"Connection ID"}
    native action getID() (string);
}
