import ballerina/grpc;
import ballerina/io;

public type helloWorldClientStreamingClient client object {
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


    remote function LotsOfGreetings(service msgListener, grpc:Headers? headers = ()) returns (grpc:StreamingClient|error)  {
        return self.grpcClient->streamingExecute("helloWorldClientStreaming/LotsOfGreetings", msgListener, headers = headers);
    }
};

type HelloRequest record {|
    string name;
    
|};


type HelloResponse record {|
    string message;
    
|};



const string ROOT_DESCRIPTOR = "0A1F68656C6C6F576F726C64436C69656E7453747265616D696E672E70726F746F22220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D657373616765324F0A1968656C6C6F576F726C64436C69656E7453747265616D696E6712320A0F4C6F74734F664772656574696E6773120D2E48656C6C6F526571756573741A0E2E48656C6C6F526573706F6E73652801620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "helloWorldClientStreaming.proto":"0A1F68656C6C6F576F726C64436C69656E7453747265616D696E672E70726F746F22220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D657373616765324F0A1968656C6C6F576F726C64436C69656E7453747265616D696E6712320A0F4C6F74734F664772656574696E6773120D2E48656C6C6F526571756573741A0E2E48656C6C6F526573706F6E73652801620670726F746F33"
        
    };
}

