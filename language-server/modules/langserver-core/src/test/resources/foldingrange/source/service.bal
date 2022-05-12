import ballerina/httpx;
import ballerina/logx;

# This is the hello service.
# Exposes an HTTP service via HTTP/1.1.
@httpx:ServiceConfig {}
service hello on new httpx:Listener(9090) {

    # The `body` annotation in the [ResourceConfig]
    # represents the entity body of the inbound request.
    @httpx:ResourceConfig {
        methods: ["POST"],
        body: "orderDetails"
    }
    resource function bindJson(httpx:Caller caller, httpx:Request req,
                               json orderDetails) {
        var details = orderDetails.Details;
        httpx:Response res = new;
        if (details is json) {
            res.setPayload(<@untainted>details);
        } else {
            res.statusCode = 400;
            res.setPayload("Order Details unavailable");
        }
        var result = caller->respond(res);
        if (result is error) {
            logx:printError(result.message(), result);
        }
    }

    @httpx:ResourceConfig {
        methods: ["POST"],
        body: "store",
        consumes: ["application/xml"]
    }
    resource function bindXML(httpx:Caller caller, httpx:Request req, xml store) {
        //Accesses the XML content.
        xml city = store.selectDescendants("{http://www.test.com}city");
        httpx:Response res = new;
        res.setPayload(<@untainted>city);

        var result = caller->respond(res);
        if (result is error) {
            logx:printError(result.message(), result);
        }
    }
}
