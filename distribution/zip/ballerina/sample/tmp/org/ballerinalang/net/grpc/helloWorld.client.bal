package org.ballerinalang.net.grpc;

function main (string[] args) {
    
endpoint<helloWorldBlockingStub> helloWorldStub {
        create BlockingStubhelloWorld("localhost", 8080);
    }

endpoint<helloWorldNonBlockingStub> helloWorldStub {
        create NonBlockingStubhelloWorld("localhost", 8080);
    }

}
