package java.org.ballerinalang.grpc;
import ballerina.net.grpc;

public connector helloWorldBlockingStub(string host, int port){
 endpoint<grpc:GRPCConnector> ep {
        create grpc:GRPCConnector(host, port, "blocking", descriptorKey, descriptorMap);
    }


    action hello (HelloRequest req) (HelloResponse, error) {
        
        var res, err1 = ep.execute(req, "java.org.ballerinalang.grpc.helloWorld/hello", "");
        if (err1 != null) {
            error e = {msg:err1.msg};
            return null, e;
        }
        var response, err2 = (HelloResponse)res;
        if (err2 != null) {
            error e = {msg:err2.msg};
            return null, e;
        }
        return response, null;
    }
    action bye (ByeRequest req) (ByeResponse, error) {
        
        var res, err1 = ep.execute(req, "java.org.ballerinalang.grpc.helloWorld/bye", "");
        if (err1 != null) {
            error e = {msg:err1.msg};
            return null, e;
        }
        var response, err2 = (ByeResponse)res;
        if (err2 != null) {
            error e = {msg:err2.msg};
            return null, e;
        }
        return response, null;
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


const string descriptorKey = "java.org.ballerinalang.grpc.helloWorld.proto";
map descriptorMap ={
"java.org.ballerinalang.grpc.helloWorld.proto":"0A1068656C6C6F576F726C642E70726F746F121B6A6176612E6F72672E62616C6C6572696E616C616E672E6772706322220A0C48656C6C6F5265717565737412120A046E616D6518012001280952046E616D6522290A0D48656C6C6F526573706F6E736512180A076D65737361676518012001280952076D65737361676522220A0A4279655265717565737412140A05677265657418012001280952056772656574221F0A0B427965526573706F6E736512100A03736179180120012809520373617932C6010A0A68656C6C6F576F726C64125E0A0568656C6C6F12292E6A6176612E6F72672E62616C6C6572696E616C616E672E677270632E48656C6C6F526571756573741A2A2E6A6176612E6F72672E62616C6C6572696E616C616E672E677270632E48656C6C6F526573706F6E736512580A0362796512272E6A6176612E6F72672E62616C6C6572696E616C616E672E677270632E427965526571756573741A282E6A6176612E6F72672E62616C6C6572696E616C616E672E677270632E427965526573706F6E7365620670726F746F33"};
