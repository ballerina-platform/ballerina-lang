import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Connection conn, http:Request req) {
        endpoint<http:HttpClient> ep {
            create http:HttpClient("http://httpstat.us", {});
        }
        http:Response backendResponse;
        backendResponse, _ = ep.forward("/200", req);

        _ = conn.forward(backendResponse);
    }
}
