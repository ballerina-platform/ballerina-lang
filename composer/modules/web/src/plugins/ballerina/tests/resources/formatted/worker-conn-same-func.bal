import ballerina/lang.system;
import ballerina/http;

function function1 () {
    http:ClientConnector __endpoint1
    = create http:ClientConnector("", {});

    worker default {
    }
    worker worker1 {
    }
}
