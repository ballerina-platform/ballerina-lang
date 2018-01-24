import ballerina.net.http;

service<http> sample {

    @http:resourceConfig {
        methods:["GET"],
        path:"/path/{foo}"
    }
    @Description {value:"PathParam and QueryParam extract values from the request URI."}
    resource params (http:Connection conn, http:InRequest req, string foo) {
        // Get QueryParam.
        map params = req.getQueryParams();
        var bar, _ = (string)params.bar;
        // Create json payload with the extracted values.
        json responseJson = {"pathParam":foo, "queryParam":bar};
        http:Response res = {};
        // A util method to set the json payload to the response message.
        res.setJsonPayload(responseJson);
        // Send a response to the client.
        _ = conn.respond(res);
    }
}
