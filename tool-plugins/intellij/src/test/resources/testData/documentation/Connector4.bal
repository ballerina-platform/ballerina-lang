
function main (string... args) {
    ClientConnector nyseEP = create ClientConnector("http://localhost:9090");
    message response = nyseEP.ge<caret>t ("/nyseStock", m);
}

@Description {value:"Native HTTP Client Connector"}

connector ClientConnector (string serviceUri) {

    @Description {value:"GET action implementation of the HTTP Connector"}

    @Param {value:"path: Request path"}

    @Param {value:"m: message"}

    @Return {value:"message: The response message object"}

    native action get (string path, message m) (message);

    @Description { value:"The POST action implementation of the HTTP Connector."}

    @Param { value:"path: Resource path " }

    @Param { value:"m: A message object" }

    @Return { value:"message: The response message object" }

    native action post (string path, message m) (message);

}
