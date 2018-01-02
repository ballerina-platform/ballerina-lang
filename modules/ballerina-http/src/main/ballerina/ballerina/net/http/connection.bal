package ballerina.net.http;

@Description { value:"Sends a 100-continue response to the client."}
@Param { value:"conn: The server connector connection" }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
public function <Connection conn> respondContinue() (HttpConnectorError) {
    Response res = {};
    res.setStatusCode(100);
    HttpConnectorError err = conn.respond(res);
    return err;
}
