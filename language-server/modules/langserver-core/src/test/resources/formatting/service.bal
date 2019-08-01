import ballerina/http;

http:Listener ln1 = new(9090);
http:Listener ln2 = new(9095);

    service    serviceName0    on  ln1 , ln2  {
         resource function newResource(http:Caller caller, http:Request request) {
            json? payload;
    (http:Response | error | ()) clientResponse;
           (json | error) jsonMsg = req.getJsonPayload();
     http:Response res = new;
           res.setPayload("sd");
     var d = 0;
           checkpanic caller->respond(res);
  }
}

service  serviceName1  on  new http:Listener(9090){
    resource function newResource(http:Caller caller, http:Request request) {
    json? payload;
          (http:Response | error | ()) clientResponse;
        (json | error) jsonMsg = req.getJsonPayload();
        http:Response res = new;
        res.setPayload("sd");
        var d = 0;
        checkpanic caller->respond(res);
    }
}

listener http:Listener listenerEP = new(8080);

service serviceName2 on listenerEP {

}

service serviceName3 on new http:Listener(9090) {

}

service serviceName4 on new http:Listener(9090) {
        public    resource     function newResource1(http:Caller caller, http:Request request) {
        http:Response res = new;
        res.setPayload("sd");
        checkpanic caller->respond(res);
    }

 private   resource     function newResource2(http:Caller caller, http:Request request) {
        http:Listener listener1 = new(8080);

        http:Response res = new;
        res.setPayload("sd");
        checkpanic caller->respond(res);
    }

  resource         function newResource3(http:Caller caller, http:Request request)=external;

    resource function send(http:Caller caller, http:Request request) {
                var result = caller->respond({
                    "send": "Success!!"
                });
            }

    resource function send2(http:Caller caller,
    http:Request request) {
            http:Client locationEP = new("http://www.mocky.io");
            var jsonMsg = req.getJsonPayload();

            if (jsonMsg is json) {
                string nameString = jsonMsg["name"].toString();
                http:Response | error clientResponse;

                if (nameString == "sanFrancisco") {
                    clientResponse =
                    locationEP->post("/v2/594e018c1100002811d6d39a", ());
                }
            }
        }
}

service serviceName5 on
    ln1
   ,
        ln2{
    resource function newResource(http:Caller caller, http:Request request) {
        json? payload;
        (http:Response | error | ()) clientResponse;
        (json | error) jsonMsg = req.getJsonPayload();
        http:Response res = new;
        res.setPayload("sd");
        var d = 0;
        checkpanic caller->respond(res);
    }
}

service serviceName6 on
    new
  http
     :
    Listener(9090){
    resource function newResource(http:Caller caller, http:Request request) {
        json? payload;
        (http:Response | error | ()) clientResponse;
        (json | error) jsonMsg = req.getJsonPayload();
        http:Response res = new;
        res.setPayload("sd");
        var d = 0;
        checkpanic caller->respond(res);
    }
}