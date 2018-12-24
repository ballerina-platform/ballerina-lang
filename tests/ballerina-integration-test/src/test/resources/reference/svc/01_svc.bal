import soos/multiFileReference;
import ballerina/http;

service s on new http:Listener(8080) {
    resource function k(http:Caller caller, http:Request r) {
        http:Response res = new;

        var p = multiFileReference:employee.name;
        res.setTextPayload("hello " + p);

        var status = caller->respond(res);
    }
}