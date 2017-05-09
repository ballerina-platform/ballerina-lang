import ballerina.lang.system;
import ballerina.lang.messages;

const int index = 12;

function testWorker()(message) {
    message result;
    message msg = {};
    msg -> sampleWorker;
    system:println("Worker calling function test started");
    result <- sampleWorker;
    string s = messages:getStringPayload(result);
    system:println(s);
    return result;

    worker sampleWorker {
    message result;
    message m;
    m <- default;
    system:println("constant value is " + index);
    json j;
    j = `{"name":"chanaka"}`;
    messages:setJsonPayload(m, j);
    m -> default;
}

}
