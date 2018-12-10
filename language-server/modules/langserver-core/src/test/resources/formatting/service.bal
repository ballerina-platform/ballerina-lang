import ballerina/http;

service serviceName1 on new http:Listener(9090){
    resource function newResource(http:Caller caller, http:Request request) {
        http:Response res = new;
        res.setPayload("sd");
        var d = 0;
        _ = caller->respond(res);
    }
}

listener http:Listener listenerEP = new(8080);

service serviceName2 on listenerEP {

}

service serviceName3 on new http:Listener(9090) {

}

service serviceName4 on new http:Listener(9090) {
    resource function newResource1(http:Caller caller, http:Request request) {
        http:Response res = new;
        res.setPayload("sd");
        _ = caller->respond(res);
    }

    resource function newResource2(http:Caller caller, http:Request request) {
        http:Listener listener1 = new(8080);

        http:Response res = new;
        res.setPayload("sd");
        _ = caller->respond(res);
    }

    resource function newResource3(http:Caller caller, http:Request request) {
        worker default {
            http:Response res = new;
            res.setPayload("sd");
            _ = caller->respond(res);
        }

        worker worker1 {
            http:Response res = new;
            res.setPayload("sd");
            _ = caller->respond(res);
        }
    }
}
