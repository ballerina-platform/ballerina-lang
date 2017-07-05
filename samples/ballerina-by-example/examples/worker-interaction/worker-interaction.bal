import ballerina.lang.system;
import ballerina.doc;
import ballerina.lang.jsons;

@doc:Description {value:"Worker interactions in ballerina allows users to share data across multiple workers."}
function main (string[] args) {
    int i = 100;
    float k = 2.34;
    system:println("[default] i: " + i + " k: " + k);
    // Send data to worker w1.
    i, k -> w1;
    // Receive data from worker w1.
    json j = {};
    j <- w1;
    system:println("[default] recieved from w1: " +
                   jsons:toString(j));

    // Define the worker and it's execution logic.
    worker w1 {
        // Receive data from default worker
        int iw;
        float kw;
        iw, kw <- default;
        system:println("[w1] received from default worker");
        system:println("[w1] iw: " + iw + " kw: " + kw);
        // Prepare data to send back to default worker
        json jw = {"name":"Ballerina"};
        system:println("[w1] sending to default worker: " +
                       jsons:toString(jw));
        // Send data to default worker
        jw -> default;
    }
}
