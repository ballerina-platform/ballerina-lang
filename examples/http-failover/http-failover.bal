import ballerina.net.http;
import ballerina.net.http.resiliency;
import ballerina.mime;
import ballerina.runtime;

service<http> failoverService {

    http:Retry retryConfiguration = {count:0};

    // Set of HttpClient connectors to be failover.
    http:HttpClient endPoint1 =
            create http:HttpClient("http://localhost:909990/mock", {});
    http:HttpClient endPoint2 =
            create http:HttpClient("http://localhost:9090/echo",
                                        {endpointTimeout:5000,
                                         retryConfig:retryConfiguration});
    http:HttpClient endPoint3 =
            create http:HttpClient("http://localhost:9090/mock", {});

    http:HttpClient[] failoverGroup = [endPoint1, endPoint2, endPoint3];

    // Set of Http Status codes needs to be failover.
    int[] errorCodes = [400, 404, 500];

    resiliency:FailoverConfig errorCode = {failoverCodes:errorCodes};

    @Description {value:"Requests which contain any HTTP method will be directed to passthrough resource."}
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
        endpoint<http:HttpClient> ep {
            create resiliency:Failover(failoverGroup, errorCode);
        }

        http:InResponse clientResponse = {};
        http:HttpConnectorError err;

        http:OutRequest outReq = {};
        json requestPayload = {"name":"Ballerina"};
        outReq.setJsonPayload(requestPayload);
        clientResponse, err = ep.post("/", outReq);

        http:OutResponse res = {};
        if (err != null) {
            res.statusCode = err.statusCode;
            res.setStringPayload(err.message);
            _ = conn.respond(res);
        } else {
            _ = conn.forward(clientResponse);
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
        http:OutResponse res = {};
        runtime:sleepCurrentWorker(30000);
        res.setStringPayload("Resource is invoked");
        _ = conn.respond(res);
    }
}

service<http> mock {
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource mockResource (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.statusCode = 200;
        res.setStringPayload("Mock Resource is Invoked.");
        _ = conn.respond(res);
    }
}
