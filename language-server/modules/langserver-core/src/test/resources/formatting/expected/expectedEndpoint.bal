import ballerina/http;

# test listener
listener http:Listener listenerEP = new (9091, config = {host: ""});

function name() {
    http:Listener listenerEP2 = new (9090, config = {host: ""});
}
