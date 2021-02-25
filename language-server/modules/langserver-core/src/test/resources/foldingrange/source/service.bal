import ballerina/http;
import ballerina/log;

# This is the hello service.
# Exposes an HTTP service via HTTP/1.1.
@http:ServiceConfig {}
service hello on new http:Listener(9090) {

    # The `body` annotation in the [ResourceConfig]
    # represents the entity body of the inbound request.
    @http:ResourceConfig {
        methods: ["POST"],
        body: "orderDetails"
    }
    resource function bindJson(http:Caller caller, http:Request req,
                               json orderDetails) {
        var details = orderDetails.Details;
        http:Response res = new;
        if (details is json) {
            res.setPayload(<@untainted>details);
        } else {
            res.statusCode = 400;
            res.setPayload("Order Details unavailable");
        }
        var result = caller->respond(res);
        if (result is error) {
            log:printError(result.message(), result);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        body: "store",
        consumes: ["application/xml"]
    }
    resource function bindXML(http:Caller caller, http:Request req, xml store) {
        //Accesses the XML content.
        xml city = store.selectDescendants("{http://www.test.com}city");
        http:Response res = new;
        res.setPayload(<@untainted>city);

        var result = caller->respond(res);
        if (result is error) {
            log:printError(result.message(), result);
        }
    }
}
