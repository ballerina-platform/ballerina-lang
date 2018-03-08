package client;
import ballerina.io;

function main (string[] args) {

    endpoint<helloWorldBlockingStub> helloWorldStubBlocking {
        create helloWorldBlockingStub("localhost", 9090);
    }

    var res, err = helloWorldStubBlocking.hello("WSO2");
    if (err != null) {
        io:println("Error from Connector: " + err.message);
    } else {
        io:println("Client Got Responce : ");
        io:println(res);
    }

}
