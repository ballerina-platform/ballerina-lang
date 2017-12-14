package ballerina.net.http;

const string HEADER_VAL_100_CONTINUE = "100-continue";
const string HEADER_KEY_EXPECT = "Expect";

@Description { value:"Checks whether the client expects a 100-continue response."}
@Param { value:"req: A request struct" }
@Return { value:"Returns true if the client expects a 100-continue response. If not, returns false." }
public function <Request req> expects100Continue() (boolean) {
    string expectHeader = req.getHeader(HEADER_KEY_EXPECT);
    if (expectHeader == HEADER_VAL_100_CONTINUE) {
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
