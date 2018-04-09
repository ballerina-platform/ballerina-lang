import ballerina/http;
import ballerina/mime;
import ballerina/observe;

endpoint http:Listener storeServiceEndpoint {
    port:9090
};

endpoint http:Client clientEndpoint {
    targets:[{url:"http://localhost:9090"}]
};

@http:ServiceConfig {
    basePath:"/store"
}
service StoreService bind storeServiceEndpoint {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/add"
    }
    addStore(endpoint outboundEndpoint, http:Request req) {
        observe:Span span = observe:startSpan("Store", "Add Item", (), observe:REFERENCE_TYPE_ROOT, ());
        http:Response res = new;
        http:Request outReq = span.injectTraceContextToHttpHeader(new http:Request(), "group-1");
        var resp = clientEndpoint -> get("/store/update", outReq);
        match resp {
            http:HttpConnectorError err => {
                span.logError("Connection error", err.message);
            }
            http:Response response => {
                var responseBody = response.getStringPayload() but {
                    http:PayloadError payloadError => span.logError("Payload error", payloadError.message)
                };
                span.addTag("Response", responseBody but { () => "No response" });
            }
        }
        res.setStringPayload("Item added successfully");
        _ = outboundEndpoint -> respond(res);
        span.finishSpan();
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/update"
    }
    updateStore(endpoint outboundEndpoint, http:Request req) {
        observe:SpanContext parentSpanContext = observe:extractTraceContextFromHttpHeader(req, "group-1");
        observe:Span span = observe:startSpan("Order", "Update Item", (),
                                                        observe:REFERENCE_TYPE_CHILDOF, parentSpanContext);
        http:Response res = new;
        res.setStringPayload("Updated Successfully");
        _ = outboundEndpoint -> respond(res);
        span.finishSpan();
    }
}
