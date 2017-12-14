package ballerina.net.http;

const string CONTINUE_100 = "100-continue";

@Description { value:"This function can be used to check whether the client expects a 100-continue response."}
@Param { value:"req: A request struct" }
@Return { value:"Returns true if the client expects a 100-continue response. If not, returns false." }
public function <Request req> expects100Continue() (boolean) {
    string expectHeader = req.getHeader("Expect");

    if (expectHeader == CONTINUE_100) {
        return true;
    }

    return false;
}

@Description { value:"Sends a 100-continue response to the client."}
@Param { value:"res: A response struct" }
@Return { value:"Returns an HttpConnectorError if there was any issue in sending the response." }
public function <Response res> send100Continue() (HttpConnectorError) {
    res.setStatusCode(100);
    HttpConnectorError err = res.send();
    return err;
}
