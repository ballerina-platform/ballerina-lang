import ballerina/http;

service <http:Service> serviceName1 {newResource (endpoint caller,http:Request request) {
http:Response res = new;
res.setPayload("sd");var d =0;_ =caller->respond(res);}
}

endpoint http:Listener listener {port: 8080};

service serviceName2   bind listener{}

service <http:Service> serviceName3   bind {port:9090}{}

service<http:Service> serviceName4{newResource1 (endpoint caller,http:Request request){
http:Response res = new;
res.setPayload("sd");
_ = caller->respond(res);}newResource2 (endpoint caller,http:Request request){
endpoint http:Listener listener1 {port: 8080};

http:Response res = new;
res.setPayload("sd");
_ = caller->respond(res);}

newResource3(endpoint caller, http:Request request) {
        worker  default {http:Response res = new;res.setPayload("sd"); _ = caller->respond(res);        }

  worker  worker1{http:Response res = new;
                   res.setPayload("sd");
       _ = caller->respond(res);}
    }
}