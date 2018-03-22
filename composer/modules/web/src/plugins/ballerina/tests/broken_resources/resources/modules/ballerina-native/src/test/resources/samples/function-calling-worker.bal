import ballerina/lang.system;
import ballerina/lang.messages;

function testworker(message msg)(message) {
    message result;
    msg -> sampleWorker;
    system:println("Function calling worker test started");
    result <- sampleWorker;
    string s = messages:getStringPayload(result);
    system:println(s);
    return result;

    worker sampleWorker  {
    json j;
    message m;
    m <- default;
    j = `{"name":"chanaka"}`;
    messages:setJsonPayload(m, j);
    m -> default;
}

}




