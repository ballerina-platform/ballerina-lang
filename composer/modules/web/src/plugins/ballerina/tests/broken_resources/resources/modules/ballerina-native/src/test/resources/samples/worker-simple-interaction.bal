import ballerina/lang.system;
import ballerina/lang.messages;

const int index = 12;

function main(string... args)(message) {
    message result;
    message msg = {};
    int x = 100;
    float y;
    msg, x -> sampleWorker;
    system:println("Worker calling function test started");
    y, result <- sampleWorker;
    string s = messages:getStringPayload(result);
    system:println(s);
    return result;

    worker sampleWorker {
    message result;
    message m;
    int a;
    float b = 12.34;
    m, a <- default;
    system:println("constant value is " + index);
    json j;
    j = `{"name":"chanaka"}`;
    messages:setJsonPayload(m, j);
    b, m -> default;
}

}
