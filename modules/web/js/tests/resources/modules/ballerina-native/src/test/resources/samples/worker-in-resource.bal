import ballerina.lang.messages;
import ballerina.lang.system;

// Global constants are visible to worker
const int index = 12;

@BasePath ("/passthrough")
service passthrough {

    @POST
    resource passthrough (message m) {
      worker sampleWorker (message msg)  {
	json j;
	j = `{"name":"chanaka"}`;
	messages:setJsonPayload(msg, j);
	system:println("constant value is " + index);
	reply msg;
      }
	message result;
	m -> sampleWorker;
	system:println("After worker");
	result <- sampleWorker;
	string s = messages:getStringPayload(result);
	system:println(s);
      	reply result;
    }
}
