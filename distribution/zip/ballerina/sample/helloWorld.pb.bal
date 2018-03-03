package org.ballerinalang.net.grpc;
import ballerina.net.grpc;

public connector helloWorldBlockingStub(string host, int port){
 endpoint<grpc:GRPCConnector> ep {
        create grpc:GRPCConnector(host, port, "blocking", descriptorKey, descriptorMap);
    }


    action hello (HelloRequest req) (HelloResponse, error) {
        var res, err1 = ep.blockingExecute("org.ballerinalang.net.grpc.helloWorld/hello", req);
        if (err1 != null) {
            error e = {message:err1.message};
            return null, e;
        }
        var response, err2 = (HelloResponse)res;
        if (err2 != null) {
            error e = {message:err2.message};
            return null, e;
        }
        return response, null;
    }
    action bye (ByeRequest req) (ByeResponse, error) {
        var res, err1 = ep.blockingExecute("org.ballerinalang.net.grpc.helloWorld/bye", req);
        if (err1 != null) {
            error e = {message:err1.message};
            return null, e;
        }
        var response, err2 = (ByeResponse)res;
        if (err2 != null) {
            error e = {message:err2.message};
            return null, e;
        }
        return response, null;
    }
}

public connector helloWorldNonBlockingStub(string host, int port){
 endpoint<grpc:GRPCConnector> ep {
        create grpc:GRPCConnector(host, port, "non-blocking", descriptorKey, descriptorMap);
    }


    action hello (HelloRequest req, string serviceName) (error) {
        var res, err1 = ep.nonBlockingExecute("HelloResponse", req, serviceName);
        if (err1 != null) {
            error e = {message:err1.message};
            return e;
        }
        return null;
    }
    action bye (ByeRequest req, string serviceName) (error) {
        var res, err1 = ep.nonBlockingExecute("ByeResponse", req, serviceName);
        if (err1 != null) {
            error e = {message:err1.message};
            return e;
        }
        return null;
    }
}


struct HelloRequest {  
  string name;
}


struct HelloResponse {  
  string message;
}


struct ByeRequest {  
  string greet;
}


struct ByeResponse {  
  string say;
}


const string descriptorKey = "org.ballerinalang.net.grpc.helloWorld.proto";
map descriptorMap ={
"org.ballerinalang.net.grpc.helloWorld.proto":"0A1068656C6C6F576F726C642E70726F746F121A6F72672E62616C6C6572696E616C616E672E6E65742E6772706322220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D65737361676522220A0A4279655265717565737412140A05677265657418012001280952056772656574221F0A0B427965526573706F6E736512100A03736179180120012809520373617932C2010A0A68656C6C6F576F726C64125C0A0568656C6C6F12282E6F72672E62616C6C6572696E616C616E672E6E65742E677270632E48656C6C6F526571756573741A292E6F72672E62616C6C6572696E616C616E672E6E65742E677270632E48656C6C6F526573706F6E736512560A0362796512262E6F72672E62616C6C6572696E616C616E672E6E65742E677270632E427965526571756573741A272E6F72672E62616C6C6572696E616C616E672E6E65742E677270632E427965526573706F6E7365620670726F746F33"};
