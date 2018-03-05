package org.ballerinalang.net.grpc;
import ballerina.io;

function main (string[] args) {
    
endpoint<helloWorldBlockingStub> helloWorldStubBlocking {
        create helloWorldBlockingStub("localhost", 9090);
    }
endpoint<helloWorldNonBlockingStub> helloWorldStubNonBlocking {
        create helloWorldNonBlockingStub("localhost", 9090);
    }
 io:println("------Event 2-----------");
    var res,ee = helloWorldStubBlocking.hello("Danesh");
    if (ee != null) {
        io:println("Error from Connector: " + ee.message);        
    } else {
    	io:println("Client Got Response : ");
    	io:println(res);
   }


}
