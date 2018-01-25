import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> conn {
            create http:HttpClient("http://httpstat.us", {});
        }
        http:OutRequest request = {};
        http:InResponse backendResponse;
        backendResponse, _ = conn.forward("/200", request);

        _ = conn.forward(backendResponse);
    }
}
