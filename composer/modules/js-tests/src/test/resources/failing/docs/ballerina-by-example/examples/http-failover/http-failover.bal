import ballerina.net.http;
import ballerina.net.http.resiliency;
import ballerina.runtime;

service<http> failoverService {

    // Set of Http Status codes needs to be failover.
    int[] failoverHttpStatusCodes = [400, 404, 500];
    resiliency:FailoverConfig errorCode = {failoverCodes:failoverHttpStatusCodes};

    @Description {value:"Requests which contain any HTTP method will be directed to failoverPostResource resource."}
    @http:resourceConfig {
        path:"/"
    }

    resource failoverPostResource (http:Connection conn, http:InRequest req) {
        // Failover Connector takes the input as an array of HttpClient connectors and FailoverConfig struct.
        // The FailoverConfig struct should contain int array of HTTP status codes and interval of failover in  milliseconds.
        //      struct FailoverConfig {
        //             int[] failoverCodes;
        //             int interval;
        //      }
        endpoint<http:HttpClient> endPoint {
            create resiliency:Failover(
                    [create http:HttpClient("http://localhost:23456/mock", {}),
                     create http:HttpClient("http://localhost:9090/echo",
                                    {endpointTimeout:5000}),
                     create http:HttpClient("http://localhost:9090/mock", {})],
                     errorCode);
        }

        http:InResponse inResponse = {};
        http:HttpConnectorError err;

        http:OutRequest outRequest = {};
        json requestPayload = {"name":"Ballerina"};
        outRequest.setJsonPayload(requestPayload);
        inResponse, err = endPoint.post("/", outRequest);

        http:OutResponse outResponse = {};
        if (err != null) {
            outResponse.statusCode = err.statusCode;
            outResponse.setStringPayload(err.message);
            _ = conn.respond(outResponse);
        } else {
            _ = conn.forward(inResponse);
        }
    }

}

// Below sample services are used to mock backend services which include timeouts.
// Mock services are run separately from the Failover Service.
service<http> echo {
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource echoResource (http:Connection conn, http:InRequest req) {
        http:OutResponse outResponse = {};
        runtime:sleepCurrentWorker(30000);
        outResponse.setStringPayload("Resource is invoked");
        _ = conn.respond(outResponse);
    }
}

service<http> mock {
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource mockResource (http:Connection conn, http:InRequest req) {
        http:OutResponse outResponse = {};
        outResponse.setStringPayload("Mock Resource is Invoked.");
        _ = conn.respond(outResponse);
    }
}
