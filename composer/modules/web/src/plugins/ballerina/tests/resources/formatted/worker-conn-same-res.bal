import ballerina/lang.system;import ballerina/http;
@http:configuration {
}
service<http> service1 {
    resource echo1 (http:Request req, http:Response res) {
        http:ClientConnector __endpoint1 = create http:ClientConnector("", {});
        worker default {
            var var1 = 1;
        }
        worker worker1 {
        }
    }
}



