import ballerina/grpc;
import ballerina/io;

public type helloWorldServerStreamingClient client object {
    private grpc:Client grpcClient;

    function __init(string url, grpc:ClientEndpointConfig? config = ()) {
        // initialize client endpoint.
        grpc:Client c = new(url, config = config);
        error? result = c.initStub("non-blocking", ROOT_DESCRIPTOR, getDescriptorMap());
        if (result is error) {
            panic result;
        } else {
            self.grpcClient = c;
        }
    }


    remote function lotsOfReplies(HelloRequest req, service msgListener, grpc:Headers? headers = ()) returns (error?) {
        
        return self.grpcClient->nonBlockingExecute("helloWorldServerStreaming/lotsOfReplies", req, msgListener, headers = headers);
    }

};

type HelloRequest record {|
    string name;
    
|};


type HelloResponse record {|
    string message;
    
|};



const string ROOT_DESCRIPTOR = "0A1F68656C6C6F576F726C6453657276657253747265616D696E672E70726F746F22220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D657373616765324D0A1968656C6C6F576F726C6453657276657253747265616D696E6712300A0D6C6F74734F665265706C696573120D2E48656C6C6F526571756573741A0E2E48656C6C6F526573706F6E73653001620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "helloWorldServerStreaming.proto":"0A1F68656C6C6F576F726C6453657276657253747265616D696E672E70726F746F22220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D657373616765324D0A1968656C6C6F576F726C6453657276657253747265616D696E6712300A0D6C6F74734F665265706C696573120D2E48656C6C6F526571756573741A0E2E48656C6C6F526573706F6E73653001620670726F746F33"
        
    };
}

