import ballerina.lang.messages;
import ballerina.lang.system;

@http:BasePath {value:"/passthrough"}
service passthrough {

    @http:POST {}
    resource passthrough (message m) {
        m ->sampleWorker;
        system:println("After worker");
        result <-sampleWorker;
        string s = messages:getStringPayload(result);
	    system:println(s);
      	reply result;

        worker sampleWorker {
            msg <-default;
            json j;
            message msg;
	        j = `{"name":"chanaka"}`;
	        messages:setJsonPayload(msg,j);
	        system:println("constant value is " + index);
            msg ->default;
        }
    }
}
