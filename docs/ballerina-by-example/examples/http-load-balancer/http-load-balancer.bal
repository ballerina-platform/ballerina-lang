import ballerina.net.http;
import ballerina.net.http.resiliency;

service<http> loadBalancerService {

    @Description {value:"Requests which contain any HTTP method will be directed to loadBalanceResource resource."}
    @http:resourceConfig {
        path:"/"
    }

    resource loadBalanceResource (http:Connection conn, http:InRequest req) {

        endpoint<http:HttpClient> endPoint {
            create resiliency:LoadBalancer(
            [create http:HttpClient("http://localhost:9090/mock1", {}),
             create http:HttpClient("http://localhost:9090/mock2", {}),
             create http:HttpClient("http://localhost:9090/mock3", {})],
            resiliency:roundRobin);
        }

        http:InResponse inResponse = {};
        http:HttpConnectorError httpConnectorError;

        http:OutRequest outRequest = {};
        json requestPayload = {"name":"Ballerina"};
        outRequest.setJsonPayload(requestPayload);
        inResponse, httpConnectorError = endPoint.post("/", outRequest);

        http:OutResponse outResponse = {};
        if (httpConnectorError != null) {
            outResponse.statusCode = httpConnectorError.statusCode;
            outResponse.setStringPayload(httpConnectorError.message);
            _ = conn.respond(outResponse);
        } else {
            _ = conn.forward(inResponse);
        }
    }
}

service<http> mock1 {
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource mock1Resource (http:Connection conn, http:InRequest req) {
        http:OutResponse outResponse = {};
        outResponse.setStringPayload("Mock1 Resource is invoked.");
        _ = conn.respond(outResponse);
    }
}

service<http> mock2 {
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource mock2Resource (http:Connection conn, http:InRequest req) {
        http:OutResponse outResponse = {};
        outResponse.setStringPayload("Mock2 Resource is Invoked.");
        _ = conn.respond(outResponse);
    }
}

service<http> mock3 {
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource mock3Resource (http:Connection conn, http:InRequest req) {
        http:OutResponse outResponse = {};
        outResponse.setStringPayload("Mock3 Resource is Invoked.");
        _ = conn.respond(outResponse);
    }
}
