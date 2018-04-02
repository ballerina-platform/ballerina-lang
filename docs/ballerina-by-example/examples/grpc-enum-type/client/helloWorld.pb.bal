package client;

import ballerina/grpc;
import ballerina/io;

struct helloWorldBlockingStub {
    grpc:Client clientEndpoint;
    grpc:ServiceStub serviceStub;
}

function <helloWorldBlockingStub stub> initStub (grpc:Client clientEndpoint) {
    grpc:ServiceStub navStub = {};
    navStub.initStub(clientEndpoint, "blocking", descriptorKey, descriptorMap);
    stub.serviceStub = navStub;
}

struct helloWorldStub {
    grpc:Client clientEndpoint;
    grpc:ServiceStub serviceStub;
}

function <helloWorldStub stub> initStub (grpc:Client clientEndpoint) {
    grpc:ServiceStub navStub = {};
    navStub.initStub(clientEndpoint, "non-blocking", descriptorKey, descriptorMap);
    stub.serviceStub = navStub;
}

function <helloWorldBlockingStub stub> hello (Request req) returns (Response|error) {
    any|grpc:ConnectorError unionResp = stub.serviceStub.blockingExecute("helloWorld/hello", req);
    match unionResp {
        grpc:ConnectorError payloadError => {
            error e = {message:payloadError.message};
            return e;
        }
        any|Response payload => {
            match payload {
                Response s => {
                    return s;
                }
                any nonOccurrence => {
                    error e = {message:"Unexpected type."};
                    return e;
                }
            }
        }
    }
}

function <helloWorldStub stub> hello (Request req, typedesc listener) returns (error| null) {
    var err1 = stub.serviceStub.nonBlockingExecute("helloWorld/hello", req, listener);
    if (err1 != null && err1.message != null) {
        error e = {message:err1.message};
        return e;
    }
    return null;
}


public struct helloWorldBlockingClient {
    grpc:Client client;
    helloWorldBlockingStub stub;
}

public function <helloWorldBlockingClient ep> init (grpc:ClientEndpointConfiguration config) {
    // initialize client endpoint.
    grpc:Client client = {};
    client.init(config);
    ep.client = client;
    // initialize service stub.
    helloWorldBlockingStub stub = {};
    stub.initStub(client);
    ep.stub = stub;
}

public function <helloWorldBlockingClient ep> getClient () returns (helloWorldBlockingStub) {
    return ep.stub;
}
public struct helloWorldClient {
    grpc:Client client;
    helloWorldStub stub;
}

public function <helloWorldClient ep> init (grpc:ClientEndpointConfiguration config) {
    // initialize client endpoint.
    grpc:Client client = {};
    client.init(config);
    ep.client = client;
    // initialize service stub.
    helloWorldStub stub = {};
    stub.initStub(client);
    ep.stub = stub;
}

public function <helloWorldClient ep> getClient () returns (helloWorldStub) {
    return ep.stub;
}


enum Foo {
WSO2,
IBM,
ORACLE

}

enum Bar {
SL,
US,
UK

}

const string descriptorKey = "helloWorld.proto";
map descriptorMap =
{ 
    "helloWorld.proto":"0A1068656C6C6F576F726C642E70726F746F224A0A0752657175657374120A0A046E616D6518012809120D0A046B696E6418023203466F6F22240A03466F6F12080A0457534F32100012070A0349424D1001120A0A064F5241434C45100222440A08526573706F6E7365120A0A046E616D6518012809120D0A046B696E6418023203426172221D0A0342617212060A02534C100012060A025553100112060A02554B10022A240A03466F6F12080A0457534F32100012070A0349424D1001120A0A064F5241434C4510022A1D0A0342617212060A02534C100012060A025553100112060A02554B1002322C0A0A68656C6C6F576F726C64121E0A0568656C6C6F1207526571756573741A08526573706F6E736528003000620670726F746F33"
  
};
