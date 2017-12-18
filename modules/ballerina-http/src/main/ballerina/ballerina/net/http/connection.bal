package ballerina.net.http;

@Description { value:"Sends a 100-continue response to the client."}
@Param { value:"con: The server connector connection" }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
public function <Connection con> respondContinue() (HttpConnectorError) {
    Response res = {};
    res.setStatusCode(100);
    HttpConnectorError err = con.respond(res);
    return err;
}
