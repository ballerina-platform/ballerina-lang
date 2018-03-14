import ballerina.net.http;

endpoint<http:Service> helloWorldEP {
    port:9090
}

endpoint<http:Client> ep {
    serviceUri: "http://httpstat.us"
}

@http:serviceConfig { basePath:"/hello", endpoints:[helloWorldEP] }
service<http:Service> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:ServerConnector conn, http:Request req) {
        http:Response backendResponse;
        backendResponse, _ = ep -> forward("/200", req);

        _ = conn -> forward(backendResponse);
    }
}
