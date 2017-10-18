function main (string[] args) {
    Client<caret>Connector nyseEP = create ClientConnector("http://localhost:9090");
    message response = nyseEP.get ("/nyseStock", m);
}

@doc:Description {value:"Native HTTP Client Connector"}
connector ClientConnector (string serviceUri) {

    @doc:Description {value:"GET action implementation of the HTTP Connector"}
    @doc:Param {value:"path: Request path"}
    @doc:Param {value:"m: message"}
    @doc:Return {value:"message: The response message object"}
    native action get (string path, message m) (message);

    @doc:Description { value:"The POST action implementation of the HTTP Connector."}
    @doc:Param { value:"path: Resource path " }
    @doc:Param { value:"m: A message object" }
    @doc:Return { value:"message: The response message object" }
    native action post (string path, message m) (message);
}
