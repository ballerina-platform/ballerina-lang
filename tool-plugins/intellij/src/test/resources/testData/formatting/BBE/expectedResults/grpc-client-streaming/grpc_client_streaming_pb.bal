// This is an auto generated client stub which is used to connect to gRPC server.
import ballerina/grpc;
import ballerina/io;

// Non-blocking client.
public type HelloWorldStub object {
    public {
        grpc:Client clientEndpoint;
        grpc:Stub stub;
    }

    function initStub(grpc:Client ep) {
        grpc:Stub navStub = new;
        navStub.initStub(ep, "non-blocking", DESCRIPTOR_KEY,
            descriptorMap);
        self.stub = navStub;
    }

    function lotsOfGreetings(typedesc listener, grpc:Headers? headers = ())
                 returns (grpc:Client|error) {
        var res = self.stub.streamingExecute("HelloWorld/lotsOfGreetings",
            listener, headers = headers);
        match res {
            error err => {
                return err;
            }
            grpc:Client con => {
                return con;
            }
        }
    }
};

// Non-blocking client endpoint.
public type HelloWorldClient object {
    public {
        grpc:Client client;
        HelloWorldStub stub;
    }

    public function init(grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;

        // initialize service stub.
        HelloWorldStub s = new;
        s.initStub(c);
        self.stub = s;

    }

    public function getCallerActions() returns (HelloWorldStub) {
        return self.stub;
    }
};

// Service descriptor data.
@final string DESCRIPTOR_KEY = "HelloWorld.proto";
map descriptorMap =
{
    "HelloWorld.proto":
        "0A1048656C6C6F576F726C642E70726F746F1A1E676F6F676C652F70726F746F6275662F77"
        + "726170706572732E70726F746F325D0A0A48656C6C6F576F726C64124F0A0F6C6F74734F"
        + "664772656574696E6773121B676F6F676C652E70726F746F6275662E537472696E675661"
        + "6C75651A1B676F6F676C652E70726F746F6275662E537472696E6756616C756528013000"
        + "620670726F746F33"
    ,

    "google.protobuf.google/protobuf/wrappers.proto":
        "0A1E676F6F676C652F70726F746F6275662F77726170706572732E70726F746F120F676F6F"
        + "676C652E70726F746F627566221C0A0B446F75626C6556616C7565120D0A0576616C7565"
        + "180120012801221B0A0A466C6F617456616C7565120D0A0576616C756518012001280222"
        + "1B0A0A496E74363456616C7565120D0A0576616C7565180120012803221C0A0B55496E74"
        + "363456616C7565120D0A0576616C7565180120012804221B0A0A496E74333256616C7565"
        + "120D0A0576616C7565180120012805221C0A0B55496E74333256616C7565120D0A057661"
        + "6C756518012001280D221A0A09426F6F6C56616C7565120D0A0576616C75651801200128"
        + "08221C0A0B537472696E6756616C7565120D0A0576616C7565180120012809221B0A0A42"
        + "7974657356616C7565120D0A0576616C756518012001280C427C0A13636F6D2E676F6F67"
        + "6C652E70726F746F627566420D577261707065727350726F746F50015A2A676974687562"
        + "2E636F6D2F676F6C616E672F70726F746F6275662F7074797065732F7772617070657273"
        + "F80101A20203475042AA021E476F6F676C652E50726F746F6275662E57656C6C4B6E6F77"
        + "6E5479706573620670726F746F33"

};
