import ballerina/http;

function function1() {

}

endpoint http:Listener endpointName {
    port: 9095
};

service<http:Service> service1 bind endpointName {
    newResource(endpoint caller, http:Request request) {
        http:Response res = new;
        res.setPayload("Successful");
        _ = caller->respond(res);
    }
}

endpoint http:Listener wsEnpointName {
    port: 9090
};

service<http:WebSocketService> service2 bind wsEnpointName {
    onOpen(endpoint caller) {
    }
    onText(endpoint caller , string text , boolean final) {
    }
    onClose(endpoint caller , int statusCode , string reason) {
    }
}

public function main(string... args) {

}
