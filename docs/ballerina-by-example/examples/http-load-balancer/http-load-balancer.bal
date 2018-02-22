import ballerina.net.http;
import ballerina.net.http.resiliency;
import ballerina.mime;

service<http> loadBalancerService {

    // Set of HttpClient connectors to be Load Balanced.
    http:HttpClient endPoint1 =
                    create http:HttpClient("http://localhost:9090/mock1", {});
    http:HttpClient endPoint2 =
                    create http:HttpClient("http://localhost:9090/mock2", {});
    http:HttpClient endPoint3 =
                    create http:HttpClient("http://localhost:9090/mock3", {});

    http:HttpClient[] loadbalanceGruop = [ endPoint1, endPoint2, endPoint3];

    @Description {value:"Requests which contain any HTTP method will be directed to passthrough resource."}
    @http:resourceConfig {
        path:"/"
    }

    resource loadBalanceResource (http:Connection conn, http:InRequest req) {

        // LoadBalancer connector takes the input as an array of HttpClient connectors and an algorithm as a function pointer.
        // The pointed function contains the implementation of the load balancing strategy.
        // With this approach users will be able to implement their own load balancing strategies and plug it into the connector.
        endpoint<http:HttpClient> ep {
            create resiliency:LoadBalancer (loadbalanceGruop, resiliency:roundRobin);
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


// Below sample services are used mock the backend services.
// Mock services are run separately from the Load Balancer Service.
service<http> mock1 {
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource mock1Resource (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Mock1 Resource is invoked.");
        _ = conn.respond(res);
    }
}

service<http> mock2 {
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource mock2Resource (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.statusCode = 200;
        res.setStringPayload("Mock2 Resource is Invoked.");
        _ = conn.respond(res);
    }
}

service<http> mock3 {
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource mock3Resource (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.statusCode = 200;
        res.setStringPayload("Mock3 Resource is Invoked.");
        _ = conn.respond(res);
    }
}
