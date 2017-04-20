import ballerina.net.http;

function main (string[] args) {
    http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090");
    message response = http:ClientConne<caret>ctor.get (nyseEP, "/nyseStock", m);
}

@doc:Description {value:"Native HTTP Client Connector"}
connector ClientConnector (string serviceUri) {

    @doc:Description {value:"GET action implementation of the HTTP Connector"}
    @doc:Param {value:"c: Connector"}
    @doc:Param {value:"path: Request path"}
    @doc:Param {value:"m: message"}
    @doc:Return {value:"message: The response message object"}
    native action get (ClientConnector c, string path, message m) (message);
}
