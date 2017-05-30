function main (string[] args) {
    ClientConnector nyseEP = create ClientConnector("http://localhost:9090");
    message response = ClientConnector.ge<caret>t (nyseEP, "/nyseStock", m);
}

@doc:Description {value:"Native HTTP Client Connector"}
connector ClientConnector (string serviceUri) {

    @doc:Description {value:"GET action implementation of the HTTP Connector"}
    @doc:Param {value:"c: Connector"}
    @doc:Param {value:"path: Request path"}
    @doc:Param {value:"m: message"}
    @doc:Return {value:"message: The response message object"}
    native action get (ClientConnector c, string path, message m) (message);

    @doc:Description { value:"The POST action implementation of the HTTP Connector."}
    @doc:Param { value:"c: A connector object" }
    @doc:Param { value:"path: Resource path " }
    @doc:Param { value:"m: A message object" }
    @doc:Return { value:"message: The response message object" }
    native action post (ClientConnector c, string path, message m) (message);
}
