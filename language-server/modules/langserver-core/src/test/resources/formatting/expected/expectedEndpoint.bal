import ballerina/http;

@Description {
    value: "asd"
}
endpoint http:Listener listener {
    port: 9091,
    host: ""
};

function name() {
    endpoint http:Listener listener2 {
        port: 9090,
        host: ""
    };
}
