import ballerina/lang.system;
import ballerina/doc;

@doc:Description {value:"Workers in ballerina allow users to delegate tasks to a new worker thread."}
function main (string... args) {
    // Define variables within the default (main) worker.
    int i = 100;
    float k = 2.34;
    system:println("[default] i: " + i + " k: " + k);
    // Define the worker and it's execution logic.
    // Define variables within the w1 worker.
    worker w1 {
        int iw = 200;
        float kw = 5.44;
        system:println("[w1] iw: " + iw + " kw: " + kw);
    }
}
