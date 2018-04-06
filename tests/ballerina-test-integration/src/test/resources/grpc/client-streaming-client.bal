import ballerina/grpc;
import ballerina/io;
import ballerina/runtime;

string response;
int total = 0;
function testClientStreaming (string[] args) returns (string) {
    // Client endpoint configuration
    endpoint HelloWorldClient helloWorldEp {
        host:"localhost",
        port:9090
    };

    endpoint grpc:Client ep;
    // Executing unary non-blocking call registering server message listener.
    var res = helloWorldEp -> lotsOfGreetings(typeof HelloWorldMessageListener);
    match res {
        grpc:error err => {
            io:print("error");
        }
        grpc:Client con => {
            ep = con;
        }
    }

    io:print("Initialized connection sucessfully.");

    foreach greet in args {
        io:print("send greeting: " + greet);
        grpc:ConnectorError connErr = ep -> send(greet);
        if (connErr != ()) {
            io:println("Error at LotsOfGreetings : " + connErr.message);
        }
    }
    _ = ep -> complete();

    int wait = 0;
    while(total < 1) {
        runtime:sleepCurrentWorker(1000);
        io:println("msg count: " + total);
        if (wait > 10) {
            break;
        }
        wait++;
    }
    io:println("completed successfully");
    return response;
}

// Server Message Listener.
service<grpc:Listener> HelloWorldMessageListener {

    // Resource registered to receive server messages
    onMessage (string message) {
        response = untaint message;
        io:println("Response received from server: " + message);
        total = 1;
    }

    // Resource registered to receive server error messages
    onError (grpc:ServerError err) {
        if (err != ()) {
            io:println("Error reported from server: " + err.message);
        }
    }

    // Resource registered to receive server completed message.
    onComplete () {
        total = 1;
        io:println("Server Complete Sending Responses.");
    }
}

// Non-blocking client
public type HelloWorldStub object {
    public {
        grpc:Client clientEndpoint;
        grpc:ServiceStub serviceStub;
    }

    function initStub (grpc:Client clientEndpoint) {
        grpc:ServiceStub navStub = new;
        navStub.initStub(clientEndpoint, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        self.serviceStub = navStub;
    }

    function lotsOfGreetings (typedesc listener) returns (grpc:Client|error) {
        var res = self.serviceStub.streamingExecute("HelloWorld/lotsOfGreetings", listener);
        match res {
            grpc:ConnectorError err => {
                error err1 = {message:err.message};
                return err1;
            }
            grpc:Client con => {
                return con;
            }
        }
    }
}


// Non-blocking client endpoint
public type HelloWorldClient object {
    public {
        grpc:Client client;
        HelloWorldStub stub;
    }

    public function init (grpc:ClientEndpointConfiguration config) {
        // initialize client endpoint.
        grpc:Client client = new;
        client.init(config);
        self.client = client;
        // initialize service stub.
        HelloWorldStub stub = new;
        stub.initStub(client);
        self.stub = stub;
    }

    public function getClient () returns (HelloWorldStub) {
        return self.stub;
    }
}

@final string DESCRIPTOR_KEY = "HelloWorld.proto";
map descriptorMap =
{
    "HelloWorld.proto":"0A1048656C6C6F576F726C642E70726F746F1A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F325D0A0A48656C6C6F576F726C64124F0A0F6C6F74734F664772656574696E6773121B676F6F676C652E70726F746F6275662E537472696E6756616C75651A1B676F6F676C652E70726F746F6275662E537472696E6756616C756528013000620670726F746F33",

    "google.protobuf.google/protobuf/wrappers.proto":"0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565180120012801221B0A0A466C6F617456616C7565120D0A0576616C7565180120012802221B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A0576616C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C7565180120012808221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A427974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F676C652E70726F746F627566420D577261707065727350726F746F50015A2A6769746875622E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F776E5479706573620670726F746F33"

};
