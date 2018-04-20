import ballerina/http;
import ballerina/mime;
import ballerina/observe;

endpoint http:Listener storeService {
    port:9090
};

endpoint http:Client clientEP {
    targets:[{url:"http://localhost:9090"}]
};

@http:ServiceConfig {
    basePath:"/store"
}
service StoreService bind storeService {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/add"
    }
    storeAdd(endpoint outboundEP, http:Request req) {
        //Start a root span with service name `Store`, span name `Add Item` and `span.kind` tag as `server`
        observe:Span span = observe:startSpan("Store", "Add Item", {"span.kind":"server"},
                                                                                    observe:REFERENCE_TYPE_ROOT, ());
        //Start a span with CHILDOF reference to the root span with service name `Store`, span name
        //`Update Manager Connector`. Add `span.kind` tag as `client`
        observe:Span childSpan = observe:startSpan("Store", "Update Manager Connector",
                                                        {"span.kind":"client"}, observe:REFERENCE_TYPE_CHILDOF, span);
        //In order to propogate the span across http requests, the span is injected to http headers as a span context
        http:Request outRequest = childSpan.injectSpanContextToHttpHeader(new http:Request(), "group-1");
        var resp = clientEP -> get("/store/update", outRequest);
        http:Response outboundResponse = new;
        match resp {
            http:Response response => {
                match (response.getStringPayload()) {
                    string payload => {
                        //Add tag with response payload to Update Manager Connector Span
                        childSpan.addTag("Response", payload);
                        outboundResponse.setStringPayload("Item Added Successfully");
                        //Finishing Update Manager Connector Span
                        childSpan.finishSpan();
                    }
                    http:PayloadError payloadError => {
                        //Add error log to Update Manager Connector Span if an error present in payload
                        childSpan.logError("Payload Error", payloadError.message);
                        outboundResponse.setStringPayload("Payload Error");
                        //Finishing Update Manager Connector Span
                        childSpan.finishSpan();
                    }
                }
            }
            http:HttpConnectorError err => {
                //Add error log to Update Manager Connector Span if an error present in connection
                childSpan.logError("Connection Error", err.message);
                outboundResponse.setStringPayload("Connection Error");
                //Finishing Update Manager Connector Span
                childSpan.finishSpan();
            }
        }
        _ = outboundEP -> respond(outboundResponse);
        //Finishing root span. Spans that are not finished will not be reported
        span.finishSpan();
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/update"
    }
    storeUpdate(endpoint outboundEP, http:Request req) {
        //Extracting the span context received via http
        observe:SpanContext parentSpanContext = observe:extractSpanContextFromHttpHeader(req, "group-1");
        //Start a span with a CHILDOF reference to the span context received via http
        observe:Span span = observe:startSpan("Update Manager", "Update Item", {"span.kind":"server"},
                                                                    observe:REFERENCE_TYPE_CHILDOF, parentSpanContext);
        http:Response outboundResponse = new;
        outboundResponse.setStringPayload("Updated Successfully");
        _ = outboundEP -> respond(outboundResponse);
        //Finishing span. Spans that are not finished will not be reported
        span.finishSpan();
    }
}
