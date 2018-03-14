package ballerina.net.http;

/////////////////////////////
/// HTTP Server Connector ///
/////////////////////////////

@Description { value:"Represents a connector which can be used by an HTTP service to communicate with an HTTP client."}
public connector ServerConnector (){

    Connection conn;

    @Description { value:"Sends outbound response to the caller"}
    @Param { value:"res: The outbound response message" }
    @Return { value:"Error occured during HTTP server connector respond" }
    action respond(Response res) (HttpConnectorError) {
        return conn.respond(res);
    }

    @Description { value:"Forwards inbound response to the caller"}
    @Param { value:"res: The inbound response message" }
    @Return { value:"Error occured during HTTP server connector forward" }
    action forward(Response res) (HttpConnectorError) {
        return conn.forward(res);
    }

    @Description { value:"Sends a push promise to the caller."}
    @Param { value:"promise: Push promise message" }
    @Return { value:"Error occured during HTTP server connector forward" }
    action promise (PushPromise promise) (HttpConnectorError) {
        return conn.promise(promise);
    }

    @Description { value:"Sends a promised push response to the caller."}
    @Param { value:"promise: Push promise message" }
    @Param { value:"res: The outbound response message" }
    @Return { value:"Error occured during HTTP server connector forward" }
    action pushPromisedResponse (PushPromise promise, Response res) (HttpConnectorError) {
        return conn.pushPromisedResponse(promise, res);
    }

    @Description { value:"Gets the Session struct for a valid session cookie from the connection. Otherwise creates a new Session struct." }
    @Return { value:"HTTP Session struct" }
    action createSessionIfAbsent() (Session) {
        return conn.createSessionIfAbsent();
    }

    @Description { value:"Gets the Session struct from the connection if it is present" }
    @Return { value:"The HTTP Session struct assoicated with the request" }
    action getSession() (Session) {
        return conn. getSession();
    }

    @Description { value:"Sends a 100-continue response to the client."}
    @Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
    action respondContinue() (HttpConnectorError) {
        return conn.respondContinue();
    }

    @Description { value:"Sends a redirect response to the user with given redirection status code." }
    @Param { value:"response: Response to be sent to client." }
    @Param { value:"redirectCode: Status code of the specific redirect." }
    @Param { value:"locations: Array of locations where the redirection can happen." }
    @Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
    action redirect(Response response, RedirectCode code, string[] locations) (HttpConnectorError) {
        return conn.redirect(response, code, locations);
    }
}
