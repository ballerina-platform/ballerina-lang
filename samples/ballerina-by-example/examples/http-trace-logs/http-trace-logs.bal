import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Request request, http:Response response) {
        endpoint<http:HttpClient> conn {
            create http:HttpClient("http://httpstat.us", {});
        }

        http:Response backendResponse;
        backendResponse, _ = conn.forward("/200", request);

        _ = response.forward(backendResponse);
    }
}
