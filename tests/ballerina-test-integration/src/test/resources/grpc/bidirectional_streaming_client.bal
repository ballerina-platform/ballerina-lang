// This is client implementation for bidirectional streaming scenario

import ballerina/grpc;
import ballerina/io;
import ballerina/log;
import ballerina/runtime;

int total = 0;
function main(string... args) {

    endpoint ChatClient chatEp {
        url:"http://localhost:9090"
    };

    endpoint grpc:Client ep;
    // Executing unary non-blocking call registering server message listener.
    var res = chatEp->chat(ChatMessageListener);
    match res {
        grpc:error err => {
            io:print("error");
        }
        grpc:Client con => {
            ep = con;
        }
    }
    ChatMessage mes = new;
    mes.name = "Sam";
    mes.message = "Hi ";
    error? connErr = ep->send(mes);
    io:println(err.message but { () => "" });
    //this will hold forever since this is chat application
    runtime:sleep(6000);
    _ = ep->complete();
}


service<grpc:Service> ChatMessageListener {

    onMessage(string message) {
        io:println("Response received from server: " + message);
    }

    onError(error err) {
        if (err != ()) {
            io:println("Error reported from server: " + err.message);
        }
    }

    onComplete() {
        io:println("Server Complete Sending Responses.");
    }
}

// Non-blocking client
public type ChatStub object {
    public {
        grpc:Client clientEndpoint;
        grpc:Stub stub;
    }

    function initStub(grpc:Client clientEndpoint) {
        grpc:Stub navStub = new;
        navStub.initStub(clientEndpoint, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }

    function chat(typedesc listener, grpc:Headers... headers) returns (grpc:Client|error) {
        var res = stub.stub.streamingExecute("Chat/chat", listener, ...header);
        match res {
            error err1 => {
                return err1;
            }
            grpc:Client con => {
                return con;
            }
        }
    }
};


// Non-blocking client endpoint
public type ChatClient object {
    public {
        grpc:Client client;
        ChatStub stub;
    }

    public function init(grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client client = new;
        client.init(config);
        self.client = client;
        // initialize service stub.
        ChatStub stub = new;
        stub.initStub(client);
        self.stub = stub;
    }
    public function getCallerActions() returns (ChatStub) {
        return self.stub;
    }
};

type ChatMessage {
    string name;
    string message;
};

@final string DESCRIPTOR_KEY = "Chat.proto";
map descriptorMap =
{
    "Chat.proto":
    "0A0A436861742E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F22280A0B436861744D657373616765120A0A046E616D6518012809120D0A076D65737361676518022809323C0A044368617412340A0463686174120B436861744D6573736167651A1B676F6F676C652E70726F746F6275662E537472696E6756616C756528013001620670726F746F33"
    ,

    "google.protobuf.google/protobuf/wrappers.proto":
    "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};

