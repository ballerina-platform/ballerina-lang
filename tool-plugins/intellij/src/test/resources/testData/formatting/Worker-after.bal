import ballerina.lang.system;
import ballerina.lang.messages;

function main (string[] args) {
    worker sampleWorker {
        message m;
        int a;
        float b = 12.34;
        m, a <- default;
        system:println("Passed in integer value is " + a);
        json j;
        j = {"name":"tom"};
        messages:setJsonPayload(m, j);
        m, b -> default;
    }
}
