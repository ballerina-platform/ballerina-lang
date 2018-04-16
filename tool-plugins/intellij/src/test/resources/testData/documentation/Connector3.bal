package org.test;

function main (string... args) {
    ClientConnector nyseEP = create ClientConnector("http://localhost:9090");
    message response = ClientConne<caret>ctor.get (nyseEP, "/nyseStock", m);
}

@Description {value:"Native HTTP Client Connector"}
connector ClientConnector (string serviceUri) {

    @Description {value:"GET action implementation of the HTTP Connector"}
    @Param {value:"c: Connector"}
    @Param {value:"path: Request path"}
    @Param {value:"m: message"}
    @Return {value:"message: The response message object"}
    native action get (ClientConnector c, string path, message m) (message);

    @Description { value:"The POST action implementation of the HTTP Connector."}
    @Param { value:"c: A connector object" }
    @Param { value:"path: Resource path " }
    @Param { value:"m: A message object" }
    @Return { value:"message: The response message object" }
    native action post (ClientConnector c, string path, message m) (message);
}
