import ballerina/grpc;
import ballerina/io;

public type helloWorldBlockingClient client object {
    private grpc:Client grpcClient;

    function __init(string url, grpc:ClientEndpointConfig? config = ()) {
        // initialize client endpoint.
        grpc:Client c = new(url, config = config);
        error? result = c.initStub("blocking", ROOT_DESCRIPTOR, getDescriptorMap());
        if (result is error) {
            panic result;
        } else {
            self.grpcClient = c;
        }
    }


    remote function hello(HelloRequest req, grpc:Headers? headers = ()) returns ((HelloResponse, grpc:Headers)|error) {
        
        var payload = check self.grpcClient->blockingExecute("service.helloWorld/hello", req, headers = headers);
        grpc:Headers resHeaders = new;
        any result = ();
        (result, resHeaders) = payload;
        var value = HelloResponse.convert(result);
        if (value is HelloResponse) {
            return (value, resHeaders);
        } else {
            error err = error("{ballerina/grpc}INTERNAL", {"message": value.reason()});
            return err;
        }
    }

    remote function bye(ByeRequest req, grpc:Headers? headers = ()) returns ((ByeResponse, grpc:Headers)|error) {
        
        var payload = check self.grpcClient->blockingExecute("service.helloWorld/bye", req, headers = headers);
        grpc:Headers resHeaders = new;
        any result = ();
        (result, resHeaders) = payload;
        var value = ByeResponse.convert(result);
        if (value is ByeResponse) {
            return (value, resHeaders);
        } else {
            error err = error("{ballerina/grpc}INTERNAL", {"message": value.reason()});
            return err;
        }
    }

};

public type helloWorldClient client object {
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


    remote function hello(HelloRequest req, service msgListener, grpc:Headers? headers = ()) returns (error?) {
        
        return self.grpcClient->nonBlockingExecute("service.helloWorld/hello", req, msgListener, headers = headers);
    }

    remote function bye(ByeRequest req, service msgListener, grpc:Headers? headers = ()) returns (error?) {
        
        return self.grpcClient->nonBlockingExecute("service.helloWorld/bye", req, msgListener, headers = headers);
    }

};

type HelloRequest record {|
    string name;
    
|};


type HelloResponse record {|
    string message;
    
|};


type ByeRequest record {|
    string greet;
    
|};


type ByeResponse record {|
    string say;
    
|};



const string ROOT_DESCRIPTOR = "0A1B68656C6C6F576F726C64576974685061636B6167652E70726F746F12077365727669636522220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D65737361676522220A0A4279655265717565737412140A05677265657418012001280952056772656574221F0A0B427965526573706F6E736512100A03736179180120012809520373617932760A0A68656C6C6F576F726C6412360A0568656C6C6F12152E736572766963652E48656C6C6F526571756573741A162E736572766963652E48656C6C6F526573706F6E736512300A0362796512132E736572766963652E427965526571756573741A142E736572766963652E427965526573706F6E7365620670726F746F33";
function getDescriptorMap() returns map<string> {
    return {
        "helloWorldWithPackage.proto":"0A1B68656C6C6F576F726C64576974685061636B6167652E70726F746F12077365727669636522220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D65737361676522220A0A4279655265717565737412140A05677265657418012001280952056772656574221F0A0B427965526573706F6E736512100A03736179180120012809520373617932760A0A68656C6C6F576F726C6412360A0568656C6C6F12152E736572766963652E48656C6C6F526571756573741A162E736572766963652E48656C6C6F526573706F6E736512300A0362796512132E736572766963652E427965526571756573741A142E736572766963652E427965526573706F6E7365620670726F746F33"
        
    };
}

