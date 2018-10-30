import ballerina/grpc;
import ballerina/io;

public type helloWorldBlockingStub object {
    public grpc:Client clientEndpoint;
    public grpc:Stub stub;

    function initStub (grpc:Client ep) {
        grpc:Stub navStub = new;
        navStub.initStub(ep, "blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }
    
    function hello (HelloRequest req, grpc:Headers? headers = ()) returns ((HelloResponse, grpc:Headers)|error) {
        
        var unionResp = self.stub.blockingExecute("helloWorld/hello", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                grpc:Headers resHeaders;
                any result;
                (result, resHeaders) = payload;
                return (check <HelloResponse>result, resHeaders);
            }
        }
    }
    
    function bye (ByeRequest req, grpc:Headers? headers = ()) returns ((ByeResponse, grpc:Headers)|error) {
        
        var unionResp = self.stub.blockingExecute("helloWorld/bye", req, headers = headers);
        match unionResp {
            error payloadError => {
                return payloadError;
            }
            (any, grpc:Headers) payload => {
                grpc:Headers resHeaders;
                any result;
                (result, resHeaders) = payload;
                return (check <ByeResponse>result, resHeaders);
            }
        }
    }
    
};

public type helloWorldStub object {
    public grpc:Client clientEndpoint;
    public grpc:Stub stub;

    function initStub (grpc:Client ep) {
        grpc:Stub navStub = new;
        navStub.initStub(ep, "non-blocking", DESCRIPTOR_KEY, descriptorMap);
        self.stub = navStub;
    }
    
    function hello (HelloRequest req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        
        return self.stub.nonBlockingExecute("helloWorld/hello", req, listener, headers = headers);
    }
    
    function bye (ByeRequest req, typedesc listener, grpc:Headers? headers = ()) returns (error?) {
        
        return self.stub.nonBlockingExecute("helloWorld/bye", req, listener, headers = headers);
    }
    
};


public type helloWorldBlockingClient object {
    public grpc:Client client;
    public helloWorldBlockingStub stub;

    public function init (grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;
        // initialize service stub.
        helloWorldBlockingStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions () returns helloWorldBlockingStub {
        return self.stub;
    }
};

public type helloWorldClient object {
    public grpc:Client client;
    public helloWorldStub stub;

    public function init (grpc:ClientEndpointConfig config) {
        // initialize client endpoint.
        grpc:Client c = new;
        c.init(config);
        self.client = c;
        // initialize service stub.
        helloWorldStub s = new;
        s.initStub(c);
        self.stub = s;
    }

    public function getCallerActions () returns helloWorldStub {
        return self.stub;
    }
};


type HelloRequest record {
    string name;
    
};

type HelloResponse record {
    string message;
    
};

type ByeRequest record {
    string greet;
    
};

type ByeResponse record {
    string say;
    
};


@final string DESCRIPTOR_KEY = "helloWorld.proto";
map descriptorMap = {
"helloWorld.proto":"0A1068656C6C6F576F726C642E70726F746F22220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D65737361676522220A0A4279655265717565737412140A05677265657418012001280952056772656574221F0A0B427965526573706F6E736512100A03736179180120012809520373617932560A0A68656C6C6F576F726C6412260A0568656C6C6F120D2E48656C6C6F526571756573741A0E2E48656C6C6F526573706F6E736512200A03627965120B2E427965526571756573741A0C2E427965526573706F6E7365620670726F746F33"

};
