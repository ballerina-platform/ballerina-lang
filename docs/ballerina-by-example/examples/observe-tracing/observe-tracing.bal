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
    storeAdd(endpoint outboundEndpoint, http:Request req) {
        observe:Span span = observe:startSpan("Store", "Add Item", {"span.kind":"server"},
                                                                                    observe:REFERENCE_TYPE_ROOT, ());
        observe:Span childSpan = observe:startSpan("Store", "Update Manager Connector",
                                                        {"span.kind":"client"}, observe:REFERENCE_TYPE_CHILDOF, span);
        http:Request outRequest = childSpan.injectTraceContextToHttpHeader(new http:Request(), "group-1");
        var resp = clientEndpoint -> get("/store/update", outRequest);
        http:Response outboundResponse = new;
        match resp {
            http:Response response => {
                match (response.getStringPayload()) {
                    string payload => {
                        childSpan.addTag("Response", payload but { () => "No response" });
                        outboundResponse.setStringPayload("Item Added Successfully");
                        childSpan.finishSpan();
                    }
                    http:PayloadError payloadError => {
                        childSpan.logError("Payload Error", payloadError.message);
                        outboundResponse.setStringPayload("Payload Error");
                        childSpan.finishSpan();
                    }
                }
            }
            http:HttpConnectorError err => {
                childSpan.logError("Connection Error", err.message);
                outboundResponse.setStringPayload("Connection Error");
                childSpan.finishSpan();
            }
        }
        _ = outboundEndpoint -> respond(outboundResponse);
        span.finishSpan();
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/update"
    }
    storeUpdate(endpoint outboundEndpoint, http:Request req) {
        observe:SpanContext parentSpanContext = observe:extractTraceContextFromHttpHeader(req, "group-1");
        observe:Span span = observe:startSpan("Update Manager", "Update Item", {"span.kind":"server"},
                                                                    observe:REFERENCE_TYPE_CHILDOF, parentSpanContext);
        http:Response outboundResponse = new;
        outboundResponse.setStringPayload("Updated Successfully");
        _ = outboundEndpoint -> respond(outboundResponse);
        span.finishSpan();
    }
}
